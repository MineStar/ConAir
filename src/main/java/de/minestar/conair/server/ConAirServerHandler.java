/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Minestar.de
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.minestar.conair.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.minestar.conair.api.ConAir;
import de.minestar.conair.api.Packet;
import de.minestar.conair.api.SmallPacketHandler;
import de.minestar.conair.api.WrappedPacket;
import de.minestar.conair.api.event.EventExecutor;
import de.minestar.conair.api.event.Listener;
import de.minestar.conair.api.packets.SmallPacket;

public class ConAirServerHandler extends SimpleChannelInboundHandler<WrappedPacket> {

    private Set<String> registeredClasses;
    private Map<Class<? extends Packet>, Map<Class<? extends Listener>, EventExecutor>> registeredListener;

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final AttributeKey<Boolean> KEY_IS_INITIALIZED = AttributeKey.valueOf("initialized");
    protected static final AttributeKey<String> KEY_CLIENT_NAME = AttributeKey.valueOf("clientName");

    private SmallPacketHandler smallPacketHandler;

    public ConAirServerHandler() {
        this.registeredListener = Collections.synchronizedMap(new HashMap<>());
        this.registeredClasses = Collections.synchronizedSet(new HashSet<>());
        this.smallPacketHandler = new SmallPacketHandler();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Pre-Initialize the channel
        channels.add(ctx.channel());
        // Mark channel as not initialized - waiting for handshake
        ctx.channel().attr(KEY_IS_INITIALIZED).getAndSet(Boolean.FALSE);
    }

    @Override
    /** 
     * Method is invoked, when a client sends a packet to the server
     */
    public void channelRead0(ChannelHandlerContext ctx, WrappedPacket wrappedPacket) throws Exception {

        // handle splitted packets
        if (wrappedPacket.getPacketClassName().equals(SmallPacket.class.getName())) {
            final WrappedPacket reconstructedPacket = smallPacketHandler.handle(wrappedPacket, (SmallPacket) wrappedPacket.getPacket().get());
            if (reconstructedPacket != null) {
                if (reconstructedPacket.getTargets().contains(ConAir.SERVER)) {
                    // Returns true, if the packet is handled ONLY by the server
                    handleServerPacket(ctx, reconstructedPacket);
                }
            }
        }

        // handle packets dedicated for the server
        if (wrappedPacket.getTargets().contains(ConAir.SERVER)) {
            // Returns true, if the packet is handled ONLY by the server
            handleServerPacket(ctx, wrappedPacket);
            if (wrappedPacket.getTargets().size() == 1) {
                return;
            }
        }

        // Wrap the packet and store the source name (maybe useful for target to
        // know, who the source is)
        WrappedPacket packet = WrappedPacket.rePack(wrappedPacket, wrappedPacket.getSource(), getClientName(ctx.channel()));
        // Broadcast packet - except for the channel, which is the sender of the
        // packet and which haven't finished handhake with the server.
        if (wrappedPacket.getTargets().isEmpty()) {
            for (Channel target : channels) {
                if (target != ctx.channel()) {
                    target.writeAndFlush(packet);
                }
            }
        } else {
            // Send packet to designated clients
            for (Channel target : channels) {
                if (target != ctx.channel() && wrappedPacket.getTargets().contains(getClientName(target))) {
                    target.writeAndFlush(packet);
                }
            }
        }
    }

    private boolean handleServerPacket(ChannelHandlerContext ctx, WrappedPacket wrappedPacket) {
        if (!registeredClasses.contains(wrappedPacket.getPacketClassName())) {
            // The packet is not registered
            return false;
        }
        Optional<Packet> result = wrappedPacket.getPacket();
        if (!result.isPresent()) {
            System.err.println("Error while parsing " + wrappedPacket + "!");
            return true;
        }
        // Inform listener
        Packet packet = result.get();
        Map<Class<? extends Listener>, EventExecutor> map = registeredListener.get(packet.getClass());
        if (map != null) {
            for (final EventExecutor executor : map.values()) {
                executor.execute(wrappedPacket.getSource(), packet);
            }
        }
        return true;
    }

    private String getClientName(Channel channel) {
        return channel.attr(KEY_CLIENT_NAME).get();
    }

    public <L extends Listener> void registerPacketListener(L listener) {
        final Method[] declaredMethods = listener.getClass().getDeclaredMethods();
        for (final Method method : declaredMethods) {
            // ignore static methods & we need exactly two params
            if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 2) {
                continue;
            }

            // accept the filter if it is true
            if (String.class.isAssignableFrom(method.getParameterTypes()[0]) && Packet.class.isAssignableFrom(method.getParameterTypes()[1])) {
                @SuppressWarnings("unchecked")
                Class<? extends Packet> packetClass = (Class<? extends Packet>) method.getParameterTypes()[1];

                // register the packet class
                registeredClasses.add(packetClass.getName());

                // register the EventExecutor
                Map<Class<? extends Listener>, EventExecutor> map = registeredListener.get(packetClass);
                if (map == null) {
                    map = Collections.synchronizedMap(new HashMap<>());
                    registeredListener.put(packetClass, map);
                }
                map.put(listener.getClass(), new EventExecutor(listener, method));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof CorruptedFrameException) {
            System.err.println("Invalid packet received!");
        }
        cause.printStackTrace();
        ctx.close();
    }
}
