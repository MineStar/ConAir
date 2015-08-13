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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import de.minestar.conair.api.ConAir;
import de.minestar.conair.api.Packet;
import de.minestar.conair.api.WrappedPacket;

public class ConAirServerHandler extends SimpleChannelInboundHandler<WrappedPacket> {

    private Set<String> registeredClasses;
    private Map<Class<? extends Packet>, BiConsumer<? super Packet, String>> registeredListener;

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final AttributeKey<Boolean> KEY_IS_INITIALIZED = AttributeKey.valueOf("initialized");
    protected static final AttributeKey<String> KEY_CLIENT_NAME = AttributeKey.valueOf("clientName");

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Pre-Initialize the channel
        channels.add(ctx.channel());
        // Mark channel as not initialized - waiting for handshake
        ctx.channel().attr(KEY_IS_INITIALIZED).getAndSet(Boolean.FALSE);

        this.registeredListener = new HashMap<>();
        this.registeredClasses = new HashSet<>();
    }

    @Override
    /** 
     * Method is invoked, when a client sends a packet to the server
     */
    public void channelRead0(ChannelHandlerContext ctx, WrappedPacket wrappedPacket) throws Exception {
        if (wrappedPacket.getTargets().contains(ConAir.SERVER)) {
            // Returns true, if the packet is handled ONLY by the server
            if (handleServerPacket(ctx, wrappedPacket) && wrappedPacket.getTargets().size() == 1) {
                return;
            }
        }
        // Wrap the packet and store the source name (maybe useful for target to
        // know, who the source is)
        WrappedPacket packet = WrappedPacket.rePack(wrappedPacket, getClientName(ctx.channel()));
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
        // Inform observer
        Packet packet = result.get();
        BiConsumer<? super Packet, String> consumer = registeredListener.get(packet.getClass());
        if (consumer != null) {
            consumer.accept(packet, wrappedPacket.getSource());
        }
        return true;
    }

    private String getClientName(Channel channel) {
        return channel.attr(KEY_CLIENT_NAME).get();
    }

    /**
     * Register listener for a Packet type to receive and handle.
     * 
     * @param packetClass
     *            The class of the packet this listener registers to
     * @param handler
     *            Packet handler for this type
     */
    @SuppressWarnings("unchecked")
    public <T extends Packet> void registerPacketListener(Class<T> packetClass, BiConsumer<T, String> handler) {
        registeredListener.put(packetClass, (BiConsumer<? super Packet, String>) handler);
        registeredClasses.add(packetClass.getName());
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
