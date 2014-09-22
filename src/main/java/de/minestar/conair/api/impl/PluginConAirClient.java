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

package de.minestar.conair.api.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import de.minestar.conair.api.ConAirClient;
import de.minestar.conair.api.Packet;
import de.minestar.conair.api.packets.HandshakePaket;

public class PluginConAirClient implements ConAirClient {

    private boolean isConnected;

    private final EventLoopGroup group;
    private Channel channel;

    private Map<Class<? extends Packet>, BiConsumer<? super Packet, String>> registeredListener;

    public PluginConAirClient() {
        this.isConnected = false;
        this.group = new NioEventLoopGroup();
        this.registeredListener = new HashMap<>();
    }

    @Override
    public void connect(String clientName, String host, int port) throws Exception {
        if (isConnected) {
            throw new IllegalStateException("Client is already connected!");
        }
        Bootstrap bootStrap = new Bootstrap();
        bootStrap.group(group).channel(NioSocketChannel.class);
        // Add initializer
        bootStrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                // Enable object serialization
                pipeline.addLast(new ObjectEncoder());
                pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                // And then the logic itself
                pipeline.addLast(new PluginConAirClientHandler());
            }
        });

        // bootStrap.option(ChannelOption.SO_KEEPALIVE, true);
        channel = bootStrap.connect(host, port).sync().channel();
        // Register at server with unique name
        sendPacket(new HandshakePaket(clientName), Packet.TARGET_SERVER);
        isConnected = true;
    }

    private class PluginConAirClientHandler extends SimpleChannelInboundHandler<WrappedPacket> {

        @Override
        protected void messageReceived(ChannelHandlerContext ctx, WrappedPacket msg) throws Exception {
            onPacketReceived(msg);
        }
    }

    private void onPacketReceived(WrappedPacket wrappedPacket) {
        Packet packet = wrappedPacket.getPacket();
        // Inform observer

        BiConsumer<? super Packet, String> consumer = registeredListener.get(packet.getClass());
        if (consumer != null) {
            consumer.accept(packet, wrappedPacket.getTargets().get(0));
        } else {
            // Do nothing
        }
    }

    @Override
    public void sendPacket(Packet packet, String... targets) throws Exception {
        ChannelFuture result = channel.writeAndFlush(WrappedPacket.create(packet, targets));
        if (result != null)
            result.sync();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Packet> void registerPacketListener(Class<T> packetClass, BiConsumer<T, String> handler) {
        registeredListener.put(packetClass, (BiConsumer<? super Packet, String>) handler);
    }

    @Override
    public void disconnect() throws Exception {
        if (!isConnected) {
            throw new IllegalStateException("Client is not connected!");
        }
        channel.close().sync();

        group.shutdownGracefully().sync();
    }

}
