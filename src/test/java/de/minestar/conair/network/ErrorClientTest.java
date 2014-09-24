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
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.minestar.conair.api.ConAirClient;
import de.minestar.conair.api.Packet;
import de.minestar.conair.api.WrappedPacket;
import de.minestar.conair.api.codec.JsonDecoder;
import de.minestar.conair.api.codec.JsonEncoder;
import de.minestar.conair.api.packets.ErrorPacket;
import de.minestar.conair.server.ConAirServer;

public class ErrorClientTest {

    private static ConAirServer server;

    private static final int PORT = 8977;

    @BeforeClass
    public static void beforeClass() throws Exception {
        server = new ConAirServer();
        server.start(PORT);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }

    @Test
    public void test() throws Exception {
        ErrorClient client = new ErrorClient();
        client.connect("error1", "localhost", PORT);
        // Create an exception
        client.sendPacket(new ChatPacket("Need a handhake? lol, not interested!"));
    }

    private class ErrorClient extends ConAirClient {

        private final EventLoopGroup group;
        private Channel channel;

        private Set<String> registeredClasses;
        private Map<Class<? extends Packet>, BiConsumer<? super Packet, String>> registeredListener;

        public ErrorClient() {
            this.group = new NioEventLoopGroup();
            this.registeredListener = new HashMap<>();
            this.registeredClasses = new HashSet<>();
        }

        public void connect(String clientName, String host, int port) throws Exception {
            Bootstrap bootStrap = new Bootstrap();
            bootStrap.group(group).channel(NioSocketChannel.class);
            // Add initializer
            bootStrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    // Wait until the buffer contains the complete JSON object
                    pipeline.addLast("frameDecoder", new JsonObjectDecoder());
                    // Decode and encode the buffer bytes arrays to readable
                    // strings
                    pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));

                    // Decode and encode the readable JSON strings as
                    // WrappedPacket
                    // objects
                    pipeline.addLast("jsonDecoder", new JsonDecoder());
                    pipeline.addLast("jsonEncoder", new JsonEncoder());

                    // And then the logic itself
                    pipeline.addLast(new PluginConAirClientHandler());
                }
            });

            channel = bootStrap.connect(host, port).sync().channel();
            // ERROR: This client does not send a Handshake packet, but should!
            // See
            // ConAirClient for correct
        }

        public class PluginConAirClientHandler extends SimpleChannelInboundHandler<WrappedPacket> {

            @Override
            protected void channelRead0(ChannelHandlerContext ctx, WrappedPacket msg) throws Exception {
                onPacketReceived(msg);
            }
        }

        private void onPacketReceived(WrappedPacket wrappedPacket) {
            if (wrappedPacket.is(ErrorPacket.class)) {
                Optional<ErrorPacket> result = wrappedPacket.getPacket();
                ErrorPacket errorPacket = result.get();
                System.err.println(errorPacket);
                return;
            }
            if (!registeredClasses.contains(wrappedPacket.getPacketClassName())) {
                // TODO: Write debug logger
                // The packet isn't for this client
                return;
            }
            Optional<Packet> result = wrappedPacket.getPacket();
            if (!result.isPresent()) {
                System.err.println("Error while parsing " + wrappedPacket + "!");
                return;
            }
            Packet packet = result.get();
            // Inform observer
            BiConsumer<? super Packet, String> consumer = registeredListener.get(packet.getClass());
            if (consumer != null) {
                consumer.accept(packet, wrappedPacket.getSource());
            } else {
                // Do nothing
            }
        }

        public void sendPacket(Packet packet, String... targets) throws Exception {
            ChannelFuture result = channel.writeAndFlush(WrappedPacket.create(packet, targets));
            if (result != null)
                result.sync();
        }

        @SuppressWarnings("unchecked")
        public <T extends Packet> void registerPacketListener(Class<T> packetClass, BiConsumer<T, String> handler) {
            registeredListener.put(packetClass, (BiConsumer<? super Packet, String>) handler);
            registeredClasses.add(packetClass.getName());
        }

    }

}
