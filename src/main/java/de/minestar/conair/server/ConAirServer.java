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

import java.util.function.BiConsumer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import de.minestar.conair.api.Packet;
import de.minestar.conair.api.codec.JsonDecoder;
import de.minestar.conair.api.codec.JsonEncoder;

public class ConAirServer {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel serverChannel;

    private boolean isRunning;
    private ConAirServerHandler packetHandler;

    public ConAirServer() {
        this.isRunning = false;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }

    public void start(final int port) throws Exception {
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
                pipeline.addLast("frameDecoder", new JsonObjectDecoder(10 * 1024 * 1024));
                // Decode and encode the buffer bytes arrays to readable strings
                pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
                pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));

                // Decode and encode the readable JSON strings as WrappedPacket
                // objects
                pipeline.addLast("jsonDecoder", new JsonDecoder());
                pipeline.addLast("jsonEncoder", new JsonEncoder());

                pipeline.addLast("handshakeHandler", new ServerHandshakeHandler());

                // Add server logic
                packetHandler = new ConAirServerHandler();
                pipeline.addLast(packetHandler);
            }
        });
        // Start server
        this.serverChannel = bootStrap.bind(port).sync().channel();
        this.isRunning = true;
    }

    /**
     * Register listener for a Packet type to receive and handle.
     * 
     * @param packetClass
     *            The class of the packet this listener registers to
     * @param handler
     *            Packet handler for this type
     */
    public <T extends Packet> void registerPacketListener(Class<T> packetClass, BiConsumer<T, String> handler) {
        if (this.packetHandler != null) {
            this.packetHandler.registerPacketListener(packetClass, handler);
        } else {
            throw new RuntimeException("Cannot register packet. Server is not started yet!");
        }
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
}
