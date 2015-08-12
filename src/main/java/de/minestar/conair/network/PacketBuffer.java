/*
 * Copyright (C) 2013 MineStar.de 
 * 
 * This file is part of ConAir.
 * 
 * ConAir is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * ConAir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ConAir.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.conair.network;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public final class PacketBuffer {

    private final ByteBuffer _buffer;

    public PacketBuffer(ByteBuffer buffer) {
        _buffer = buffer;
    }

    public PacketBuffer(int bufferSize) {
        this(ByteBuffer.allocateDirect(bufferSize));
    }

    public PacketBuffer() {
        this(128 * 1000);
    }

    public PacketBuffer clear() {
        _buffer.clear();
        return this;
    }

    public ByteBuffer getBuffer() {
        return _buffer;
    }

    /*
     * BYTES AND BYTE ARRAYS
     */

    public byte readByte() {
        return _buffer.get();
    }

    public PacketBuffer put(byte b) {
        _buffer.put(b);
        return this;
    }

    public PacketBuffer readBytes(byte[] dst) {
        _buffer.get(dst);
        return this;
    }

    public PacketBuffer writeBytes(byte[] src) {
        _buffer.put(src);
        return this;
    }

    public PacketBuffer getBytes(byte[] dst, int offset, int length) {
        _buffer.get(dst, offset, length);
        return this;
    }

    public PacketBuffer writeBytes(byte[] src, int offset, int length) {
        _buffer.put(src, offset, length);
        return this;
    }

    public byte[] readByteArray() {
        final byte[] dest = new byte[readInt()];
        for (int i = 0; i < dest.length; i++) {
            dest[i] = readByte();
        }
        return dest;
    }

    public PacketBuffer writeByteArray(byte[] src) {
        writeInt(src.length);
        for (final byte b : src) {
            put(b);
        }
        return this;
    }

    public PacketBuffer writeByteBuffer(ByteBuffer src) {
        _buffer.put(src);
        return this;
    }

    public PacketBuffer writePacketBuffer(PacketBuffer src) {
        _buffer.put(src._buffer);
        return this;
    }

    public byte readByte(int position) {
        return _buffer.get(position);
    }

    public PacketBuffer writeByte(byte b) {
        _buffer.put(b);
        return this;
    }

    public PacketBuffer writeByte(int position, byte b) {
        _buffer.put(position, b);
        return this;
    }

    /*
     * CHARS
     */
    public char readChar() {
        return _buffer.getChar();
    }

    public PacketBuffer writeChar(char value) {
        _buffer.putChar(value);
        return this;
    }

    public char readChar(int position) {
        return _buffer.getChar(position);
    }

    public PacketBuffer writeChar(int position, char value) {
        _buffer.putChar(value);
        return this;
    }

    /*
     * DOUBLES
     */
    public double readDouble() {
        return _buffer.getDouble();
    }

    public PacketBuffer writeDouble(double value) {
        _buffer.putDouble(value);
        return this;
    }

    public double readDouble(int position) {
        return _buffer.getDouble(position);
    }

    public PacketBuffer writeDouble(int position, double value) {
        _buffer.putDouble(position, value);
        return this;
    }

    /*
     * FLOATS
     */
    public float readFloat() {
        return _buffer.getFloat();
    }

    public PacketBuffer writeFloat(float value) {
        _buffer.putFloat(value);
        return this;
    }

    public float readFloat(int position) {
        return _buffer.getFloat(position);
    }

    public PacketBuffer writeFloat(int position, float value) {
        _buffer.putFloat(position, value);
        return this;
    }

    /*
     * INTEGER
     */
    public int readInt() {
        return _buffer.getInt();
    }

    public PacketBuffer writeInt(int value) {
        _buffer.putInt(value);
        return this;
    }

    public int readInt(int position) {
        return _buffer.getInt(position);
    }

    public PacketBuffer writeInt(int position, int value) {
        _buffer.putInt(position, value);
        return this;
    }

    /*
     * LONG
     */
    public long readLong() {
        return _buffer.getLong();
    }

    public PacketBuffer writeLong(long value) {
        _buffer.putLong(value);
        return this;
    }

    public long readLong(int position) {
        return _buffer.getLong(position);
    }

    public PacketBuffer writeLong(int position, long value) {
        _buffer.putLong(position, value);
        return this;
    }

    /*
     * SHORTS
     */
    public short readShort() {
        return _buffer.getShort();
    }

    public PacketBuffer writeShort(short value) {
        _buffer.putShort(value);
        return this;
    }

    public short readShort(int position) {
        return _buffer.getShort(position);
    }

    public PacketBuffer writeShort(int position, short value) {
        _buffer.putShort(position, value);
        return this;
    }

    /*
     * STRINGS
     */
    private final static Charset CHARSET = Charset.forName("UTF-8");

    public void writeString(String s) {
        byte[] b = s.getBytes(CHARSET);
        _buffer.putInt(b.length);
        _buffer.put(b);
    }

    public String readString() {
        int len = _buffer.getInt();
        byte[] b = new byte[len];
        _buffer.get(b);
        return new String(b, CHARSET);
    }
}
