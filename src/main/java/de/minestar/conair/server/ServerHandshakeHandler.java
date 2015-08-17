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

import de.minestar.conair.common.ConAirMember;
import de.minestar.conair.common.packets.ConnectionPacket;
import de.minestar.conair.common.packets.ErrorPacket;
import de.minestar.conair.common.packets.ErrorPacket.ErrorType;
import de.minestar.conair.common.packets.HandshakePacket;
import de.minestar.conair.common.packets.ServerInfoPacket;
import de.minestar.conair.common.packets.WrappedPacket;


class ServerHandshakeHandler extends SimpleChannelInboundHandler<WrappedPacket> {

    private static final AttributeKey<Boolean> HANDSHAKE_COMPLETE = AttributeKey.valueOf("HANDSHAKE_COMPLETE");

    private final ConAirServer _server;


    ServerHandshakeHandler(final ConAirServer server) {
        _server = server;
    }


    private boolean isInitialized(ChannelHandlerContext ctx) {
        return ctx.attr(HANDSHAKE_COMPLETE).get() == Boolean.TRUE;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(HANDSHAKE_COMPLETE).getAndSet(Boolean.FALSE);
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
                ctx.channel().attr(ConAirServerHandler.CONAIR_CLIENT_NAME).set(handshakePacket.getClientName());
                ctx.channel().attr(HANDSHAKE_COMPLETE).set(Boolean.TRUE);
                _server.sendPacket(new ConnectionPacket(handshakePacket.getClientName(), true));
                _server.sendPacket(new ServerInfoPacket(_server.getName(), _server.getClientMap()), new ConAirMember(handshakePacket.getClientName()), ctx.channel());
                _server.addClient(handshakePacket.getClientName(), ctx.channel());
                ctx.channel().closeFuture().addListeners(new ChannelFutureListener() {

                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        _server.removeClient(ctx.channel());
                        _server.sendPacket(new ConnectionPacket(handshakePacket.getClientName(), false));
                    }
                });
            }
        }
        // Channel tries a twice handshake
        else if (isInitialized(ctx) && msg.is(HandshakePacket.class)) {
            ErrorPacket packet = new ErrorPacket(ErrorType.DUPLICATE_HANDSHAKE);
            ctx.writeAndFlush(WrappedPacket.create(packet, _server.getServer()));
            throw new IllegalStateException("Channel did two handshakes!");
        }
        // Channel tries to sent packets without a handshake
//        else {
//            ErrorPacket packet = new ErrorPacket(ErrorType.NO_HANDSHAKE);
//            ctx.writeAndFlush(WrappedPacket.create(packet, _server.getServer()));
//            throw new IllegalStateException("Channel cannot broadcast before a handshake!");
//        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

}
