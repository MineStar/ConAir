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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import java.util.Optional;

import de.minestar.conair.api.ConAir;
import de.minestar.conair.api.WrappedPacket;
import de.minestar.conair.api.packets.ConnectedClientsPacket;
import de.minestar.conair.api.packets.ConnectionPacket;
import de.minestar.conair.api.packets.ErrorPacket;
import de.minestar.conair.api.packets.ErrorPacket.ErrorType;
import de.minestar.conair.api.packets.HandshakePacket;

public class ServerHandshakeHandler extends SimpleChannelInboundHandler<WrappedPacket> {

    private static final AttributeKey<Boolean> KEY_IS_INITIALIZED = AttributeKey.valueOf("initialized");

    private final ConAirServer _server;

    public ServerHandshakeHandler(final ConAirServer server) {
        _server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(KEY_IS_INITIALIZED).getAndSet(Boolean.FALSE);
        // TODO Auto-generated method stub
        super.channelActive(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WrappedPacket msg) throws Exception {
        // Channel is initialized and packet is not a handshake - client will be
        // handled in later handlers.
        if (isInitialized(ctx) && !msg.is(HandshakePacket.class)) {
            ctx.fireChannelRead(msg);
            return;
        }

        // Channel starts handshake
        if (!isInitialized(ctx) && msg.is(HandshakePacket.class)) {
            Optional<HandshakePacket> result = msg.getPacket();
            if (!result.isPresent()) {
                throw new Exception("Error while parsing " + msg + " as HandshakePacket!");
            }
            HandshakePacket handshakePacket = result.get();
            if (!isInitialized(ctx)) {
                // Mark the client as initialized and assign a client name
                ctx.channel().attr(ConAirServerHandler.KEY_CLIENT_NAME).set(handshakePacket.getClientName());
                ctx.channel().attr(KEY_IS_INITIALIZED).set(Boolean.TRUE);
//                _server.sendPacket(new ConnectionPacket(handshakePacket.getClientName(), true));
//                _server.sendPacket(new ConnectedClientsPacket(_server.getClientMap()));
                _server.addClient(handshakePacket.getClientName(), ctx.channel());
                ctx.channel().closeFuture().addListeners(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        _server.removeClient(ctx.channel());
//                        _server.sendPacket(new ConnectionPacket(handshakePacket.getClientName(), false));
                    }
                });
            }
        }
        // Channel tries a twice handshake
        else if (isInitialized(ctx) && msg.is(HandshakePacket.class)) {
            ErrorPacket packet = new ErrorPacket(ErrorType.DUPLICATE_HANDSHAKE);
            ctx.writeAndFlush(WrappedPacket.create(packet, ConAir.SERVER));
            throw new IllegalStateException("Channel did two handshakes!");
        }
        // Channel tries to sent packets without a handshake
        else {
            ErrorPacket packet = new ErrorPacket(ErrorType.NO_HANDSHAKE);
            ctx.writeAndFlush(WrappedPacket.create(packet, ConAir.SERVER));
            throw new IllegalStateException("Channel cannot broadcast before a handshake!");
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private boolean isInitialized(ChannelHandlerContext ctx) {
        return ctx.attr(KEY_IS_INITIALIZED).get() == Boolean.TRUE;
    }

}
