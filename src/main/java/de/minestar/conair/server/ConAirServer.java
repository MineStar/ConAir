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

import java.net.InetSocketAddress;
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
import de.minestar.conair.common.plugin.PluginManagerFactory;


public class ConAirServer implements PacketSender {

    private boolean _isRunning;
    private final EventLoopGroup _bossGroup;
    private final EventLoopGroup _workerGroup;
    private final Channel _serverChannel;

    private final ConAirMember _serverMember;

    private final Map<String, Listener> _listenerMap;
    private final Map<String, Channel> _clientMap;
    private final Map<InetSocketAddress, ConAirServerHandler> _packetHandler;

    final PluginManagerFactory _pluginManagerFactory;


    public ConAirServer(int port) throws Exception {
        this("ConAirServer", port, ConAir.DEFAULT_PLUGIN_FOLDER);
    }


    public ConAirServer(String serverName, int port) throws Exception {
        this(serverName, port, ConAir.DEFAULT_PLUGIN_FOLDER);
    }


    public ConAirServer(String serverName, int port, String pluginFolder) throws Exception {
        _serverMember = new ConAirMember(serverName);
        _isRunning = false;
        _bossGroup = new NioEventLoopGroup(1);
        _workerGroup = new NioEventLoopGroup();
        _listenerMap = Collections.synchronizedMap(new HashMap<>());
        _clientMap = Collections.synchronizedMap(new HashMap<>());
        _pluginManagerFactory = PluginManagerFactory.get(pluginFolder);
        _packetHandler = Collections.synchronizedMap(new HashMap<>());
        _serverChannel = _start(port);
        _afterStart();
    }


    private void _afterStart() {
        afterStart();
        _pluginManagerFactory.loadPlugins(this);
    }


    @SuppressWarnings("unchecked")
    private Channel _start(final int port) throws Exception {
        if (_isRunning) {
            throw new IllegalStateException("Server is already running!");
        }
        final ServerBootstrap bootStrap = new ServerBootstrap();
        bootStrap.group(_bossGroup, _workerGroup).channel(NioServerSocketChannel.class);
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
                ConAirServerHandler packetHandler = new ConAirServerHandler(ConAirServer.this);
                for (final Listener listener : _listenerMap.values()) {
                    packetHandler.registerPacketListener(listener);
                }
                pipeline.addLast(packetHandler);

                // save the handler
                final InetSocketAddress localAddress = ch.localAddress();
                _packetHandler.put(localAddress, packetHandler);
                ch.closeFuture().addListener(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        _packetHandler.remove(localAddress);
                    }
                });
            }
        });
        // Start server
        final Channel serverChannel = bootStrap.bind(port).sync().channel();
        serverChannel.closeFuture().addListeners(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                _pluginManagerFactory.disablePlugins();
            }
        });
        _isRunning = true;
        return serverChannel;
    }


    public final void stop() throws Exception {
        if (!_isRunning) {
            throw new IllegalStateException("Server isn't running!");
        }
        _serverChannel.close().sync();
        _bossGroup.shutdownGracefully().sync();
        _workerGroup.shutdownGracefully().sync();
        _isRunning = false;
        afterStop();
    }


    protected void afterStart() {
        // NOTHING TO DO IN THE STANDARD SERVER
    }


    protected void afterStop() {
        // NOTHING TO DO IN THE STANDARD SERVER
    }


    String[] getClientMap() {
        synchronized (_clientMap) {
            return _clientMap.keySet().toArray(new String[_clientMap.keySet().size()]);
        }
    }


    void addClient(String clientName, Channel channel) {
        synchronized (_clientMap) {
            _clientMap.put(clientName, channel);
        }
    }


    void removeClient(String clientName) {
        synchronized (_clientMap) {
            _clientMap.remove(clientName);
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
        List<WrappedPacket> packetList = WrappedPacket.create(packet, getServer(), target);
        for (final WrappedPacket wrappedPacket : packetList) {
            ChannelFuture result = channel.writeAndFlush(wrappedPacket);
            if (result != null) {
                result.sync();
            }
        }
    }


    public final boolean isRunning() {
        return _isRunning;
    }


    @Override
    public final ConAirMember getMember(final String name) throws IllegalArgumentException {
        if (!_clientMap.containsKey(name)) {
            throw new IllegalArgumentException("Member '" + name + "' not found!");
        }
        return new ConAirMember(name);
    }


    @Override
    public final String getName() {
        return _serverMember.getName();
    }


    @Override
    public final ConAirMember getServer() {
        return _serverMember;
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
    public final boolean sendPacket(Packet packet, ConAirMember... targets) {
        try {
            if ((targets == null || targets.length < 1) && _clientMap.values().size() > 0) {
                // broadcast
                final List<WrappedPacket> packetList = WrappedPacket.create(packet, getServer(), targets);
                for (final Entry<String, Channel> entry : _clientMap.entrySet()) {
                    for (final WrappedPacket wrappedPacket : packetList) {
                        ChannelFuture result = entry.getValue().writeAndFlush(wrappedPacket);
                        if (result != null) {
                            result.sync();
                        }
                    }
                }
            } else {
                for (final ConAirMember client : targets) {
                    Channel channel = _clientMap.get(client.getName());
                    if (channel == null) {
                        continue;
                    }
                    List<WrappedPacket> packetList = WrappedPacket.create(packet, getServer(), client);
                    for (final WrappedPacket wrappedPacket : packetList) {
                        ChannelFuture result = channel.writeAndFlush(wrappedPacket);
                        if (result != null) {
                            result.sync();
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public final <L extends Listener> void registerPacketListener(L listener) {
        _listenerMap.put(listener.getClass().toString(), listener);
        if (_isRunning) {
            for (ConAirServerHandler packetHandler : _packetHandler.values()) {
                packetHandler.registerPacketListener(listener);
            }
        }
    }


    @Override
    public final <L extends Listener> void unregisterPacketListener(Class<L> listenerClass) {
        _listenerMap.remove(listenerClass.toString());
        if (_isRunning) {
            for (ConAirServerHandler packetHandler : _packetHandler.values()) {
                packetHandler.unregisterPacketListener(listenerClass);
            }
        }
    }

}
