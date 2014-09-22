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

package de.minestar.conair.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import de.minestar.conair.api.Packet;
import de.minestar.conair.api.impl.WrappedPacket;
import de.minestar.conair.api.packets.HandshakePaket;

public class ConAirServerHandler extends SimpleChannelInboundHandler<WrappedPacket> {

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private static final AttributeKey<Boolean> KEY_IS_INITIALIZED = AttributeKey.valueOf("initialized");
    private static final AttributeKey<String> KEY_CLIENT_NAME = AttributeKey.valueOf("clientName");

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
    public void messageReceived(ChannelHandlerContext ctx, WrappedPacket msg) throws Exception {
        if (msg.getTargets().contains(Packet.TARGET_SERVER)) {
            // Returns true, if the packet is handled ONLY by the server
            if (handleServerPacket(ctx, msg))
                return;
        }
        // Wrap the packet and store the source name (maybe useful for target to
        // know, who the source is)
        WrappedPacket packet = WrappedPacket.create(msg.getPacket(), getClientName(ctx.channel()));
        // Broadcast packet - except for the channel, which is the sender of the
        // packet
        if (msg.getTargets().isEmpty()) {
            for (Channel target : channels) {
                if (target != ctx.channel())
                    target.writeAndFlush(packet);
            }
        } else {
            // Send packet to designated clients
            for (Channel target : channels) {
                if (target != ctx.channel() && msg.getTargets().contains(getClientName(target)))
                    target.writeAndFlush(packet);
            }
        }
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }

    private boolean handleServerPacket(ChannelHandlerContext ctx, WrappedPacket msg) {
        // Check, if client was already initialized
        if (msg.is(HandshakePaket.class)) {
            HandshakePaket hPacket = msg.getPacket();
            if (!isInitialized(ctx)) {
                // Mark the client as initialized and assign a client name
                ctx.channel().attr(KEY_CLIENT_NAME).set(hPacket.getClientName());
                ctx.channel().attr(KEY_IS_INITIALIZED).set(Boolean.TRUE);
            }
            return true;
        }

        return false;
    }

    private String getClientName(Channel channel) {
        return channel.attr(KEY_CLIENT_NAME).get();
    }

    private boolean isInitialized(ChannelHandlerContext ctx) {
        return ctx.attr(KEY_IS_INITIALIZED).get() == Boolean.TRUE;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}