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

    private final static byte BRACKET_LEFT = '{';
    private final static byte BRACKET_RIGHT = '}';
    private final static byte QUOTE = '"';

    private int _openBracesCount = 0;
    private boolean _inQuote = false;
    private final int _maxLength;
    private final ByteBuf _frame = Unpooled.buffer(1024);


    public JsonFrameDecoder(int maxObjectLength) {
        this._maxLength = maxObjectLength;
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
                _inQuote = !_inQuote;
            }

            if (!_inQuote) {
                if (next == BRACKET_LEFT) { // {
                    _openBracesCount++;
                } else if (next == BRACKET_RIGHT) { // }
                    _openBracesCount--;
                }
            }
            _frame.writeByte(next);
            if (_openBracesCount == 0) {
                out.add(_frame.toString(Charset.defaultCharset()));
                _frame.clear();
            }
        }

        // Check for corrupt frame
        if (_frame.readableBytes() > _maxLength) {
            throw new TooLongFrameException("object length of " + _frame.readableBytes() + " exceeds " + _maxLength + "bytes");
        }
    }

}