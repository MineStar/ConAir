/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Minestar.de
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

package de.minestar.conair.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;

import java.nio.charset.Charset;
import java.util.List;

public class JsonFrameDecoder extends ByteToMessageDecoder {

    private final int maxObjectLength;

    private int openBracesCount = 0;
    private boolean inQuote = false;
    private final ByteBuf frame = Unpooled.buffer(1024);
    private final static byte BRACKET_LEFT = '{';
    private final static byte BRACKET_RIGHT = '}';
    private final static byte QUOTE = '"';

    public JsonFrameDecoder(int maxObjectLength) {
        this.maxObjectLength = maxObjectLength;
    }

    public JsonFrameDecoder() {
        this(1024 * 1024);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Process frames like a boss
        while (in.isReadable()) {
            byte next = in.readByte();

            if (next == QUOTE) { // "
                inQuote = !inQuote;
            }

            if (!inQuote) {
                if (next == BRACKET_LEFT) { // {
                    openBracesCount++;
                } else if (next == BRACKET_RIGHT) { // }
                    openBracesCount--;
                }
            }
            frame.writeByte(next);
            if (openBracesCount == 0) {
                out.add(frame.toString(Charset.defaultCharset()));
                frame.clear();
            }
        }

        // Check for corrupt frame
        if (frame.readableBytes() > maxObjectLength) {
            throw new TooLongFrameException("object length of " + frame.readableBytes() + " exceeds " + maxObjectLength + "bytes");
        }

    }

}