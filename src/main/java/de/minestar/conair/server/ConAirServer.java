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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.minestar.conair.api.ConAir;
import de.minestar.conair.api.Packet;
import de.minestar.conair.api.event.Listener;
import de.minestar.conair.common.ConAirMember;
import de.minestar.conair.common.PacketSender;
import de.minestar.conair.common.codec.JsonDecoder;
import de.minestar.conair.common.codec.JsonEncoder;
import de.minestar.conair.common.codec.JsonFrameDecoder;
import de.minestar.conair.common.packets.WrappedPacket;
import de.minestar.conair.common.plugin.PluginManager;


public final class ConAirServer implements PacketSender {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel serverChannel;

    private boolean isRunning;
    private final Map<String, Listener> listenerMap;
    private final Map<String, Channel> clientMap;
    private final PluginManager _pluginManager;
    private ConAirServerHandler packetHandler;


    public ConAirServer(int port) throws Exception {
        this(port, ConAir.DEFAULT_PLUGIN_FOLDER);
    }


    public ConAirServer(int port, String pluginFolder) throws Exception {
        this.isRunning = false;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.listenerMap = Collections.synchronizedMap(new HashMap<>());
        this.clientMap = Collections.synchronizedMap(new HashMap<>());
        _pluginManager = new PluginManager(this, pluginFolder);
        start(port);
    }


    @SuppressWarnings("unchecked")
    private void start(final int port) throws Exception {
        if (isRunning) {
            throw new IllegalStateException("Server is already running!");
        }
        ServerBootstrap bootStrap = new ServerBootstrap();
        bootStrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
        bootStrap.handler(new LoggingHandler(LogLevel.INFO));
        bootStrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                // Wait until the buffer contains the complete JSON object
                pipeline.addLast("frameDecoder", new JsonFrameDecoder(16 * 1024));

                // Decode and encode the buffer bytes arrays to readable strings
                pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
                pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));

                // Decode and encode the readable JSON strings as WrappedPacket
                // objects
                pipeline.addLast("jsonDecoder", new JsonDecoder());
                pipeline.addLast("jsonEncoder", new JsonEncoder());

                pipeline.addLast("handshakeHandler", new ServerHandshakeHandler(ConAirServer.this));

                // Add server logic
                ConAirServer.this.packetHandler = new ConAirServerHandler(ConAirServer.this);
                for (final Listener listener : ConAirServer.this.listenerMap.values()) {
                    ConAirServer.this.packetHandler.registerPacketListener(listener);
                }
                pipeline.addLast(packetHandler);
            }

        });
        // Start server
        this.serverChannel = bootStrap.bind(port).sync().channel();
        this.serverChannel.closeFuture().addListeners(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                _pluginManager.disablePlugins();
            }
        });
        this.isRunning = true;
    }


    String[] getClientMap() {
        synchronized (clientMap) {
            return clientMap.keySet().toArray(new String[clientMap.keySet().size()]);
        }
    }


    void addClient(String clientName, Channel channel) {
        synchronized (clientMap) {
            this.clientMap.put(clientName, channel);
        }
    }


    void removeClient(Channel channel) {
        synchronized (clientMap) {
            String clientToRemove = null;
            for (Map.Entry<String, Channel> entry : this.clientMap.entrySet()) {
                if (channel.id() == entry.getValue().id()) {
                    clientToRemove = entry.getKey();
                    break;
                }
            }
            if (clientToRemove != null) {
                this.clientMap.remove(clientToRemove);
            }
        }
    }


    /**
     * Send a packet to the targets. If targets are empty, the packet will be broadcasted to every registered client, but not this client.
     * 
     * @param packet
     *            The data to send.
     * @param targets
     *            The target
     * @throws Exception
     *             Something went wrong
     */
    void sendPacket(Packet packet, ConAirMember target, Channel channel) throws Exception {
        List<WrappedPacket> packetList = WrappedPacket.create(packet, ConAir.SERVER, target);
        for (final WrappedPacket wrappedPacket : packetList) {
            ChannelFuture result = channel.writeAndFlush(wrappedPacket);
            if (result != null) {
                result.sync();
            }
        }
    }


    /**
     * Send a packet to the targets. If targets are empty, the packet will be broadcasted to every registered client, but not this client.
     * 
     * @param packet
     *            The data to send.
     * @param targets
     *            The target
     * @throws Exception
     *             Something went wrong
     */
    @Override
    public void sendPacket(Packet packet, ConAirMember... targets) throws Exception {
        if ((targets == null || targets.length < 1) && this.clientMap.values().size() > 0) {
            // broadcast
            final List<WrappedPacket> packetList = WrappedPacket.create(packet, ConAir.SERVER, targets);
            for (final Entry<String, Channel> entry : this.clientMap.entrySet()) {
                for (final WrappedPacket wrappedPacket : packetList) {
                    ChannelFuture result = entry.getValue().writeAndFlush(wrappedPacket);
                    if (result != null) {
                        result.sync();
                    }
                }
            }
        } else {
            for (final ConAirMember client : targets) {
                Channel channel = this.clientMap.get(client.getName());
                if (channel == null) {
                    continue;
                }
                List<WrappedPacket> packetList = WrappedPacket.create(packet, ConAir.SERVER, client);
                for (final WrappedPacket wrappedPacket : packetList) {
                    ChannelFuture result = channel.writeAndFlush(wrappedPacket);
                    if (result != null) {
                        result.sync();
                    }
                }
            }
        }
    }


    public boolean isRunning() {
        return isRunning;
    }


    @Override
    public <L extends Listener> void registerPacketListener(L listener) {
        this.listenerMap.put(listener.getClass().toString(), listener);
        if (isRunning && this.packetHandler != null) {
            this.packetHandler.registerPacketListener(listener);
        }
    }


    @Override
    public ConAirMember getMember(final String name) throws IllegalArgumentException {
        if (!clientMap.containsKey(name)) {
            throw new IllegalArgumentException("Member '" + name + "' not found!");
        }
        return new ConAirMember(name);
    }


    public void stop() throws Exception {
        if (!isRunning) {
            throw new IllegalStateException("Server isn't running!");
        }
        this.serverChannel.close().sync();
        bossGroup.shutdownGracefully().sync();
        workerGroup.shutdownGracefully().sync();
        this.isRunning = false;
    }


    @Override
    public String getName() {
        return ConAir.SERVER.getName();
    }

}
