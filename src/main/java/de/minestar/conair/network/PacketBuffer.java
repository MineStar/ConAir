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

    private final ByteBuffer buffer;

    public PacketBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public PacketBuffer(int bufferSize) {
        this(ByteBuffer.allocateDirect(bufferSize));
    }

    public PacketBuffer() {
        this(4096);
    }

    public PacketBuffer clear() {
        buffer.clear();
        return this;
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    /*
     * BYTES AND BYTE ARRAYS
     */

    public byte readByte() {
        return this.buffer.get();
    }

    public PacketBuffer put(byte b) {
        this.buffer.put(b);
        return this;
    }

    public PacketBuffer readBytes(byte[] dst) {
        this.buffer.get(dst);
        return this;
    }

    public PacketBuffer writeBytes(byte[] src) {
        this.buffer.put(src);
        return this;
    }

    public PacketBuffer getBytes(byte[] dst, int offset, int length) {
        this.buffer.get(dst, offset, length);
        return this;
    }

    public PacketBuffer writeBytes(byte[] src, int offset, int length) {
        this.buffer.put(src, offset, length);
        return this;
    }

    public PacketBuffer writeByteBuffer(ByteBuffer src) {
        this.buffer.put(src);
        return this;
    }

    public PacketBuffer writePacketBuffer(PacketBuffer src) {
        this.buffer.put(src.buffer);
        return this;
    }

    public byte readByte(int position) {
        return this.buffer.get(position);
    }

    public PacketBuffer writeByte(byte b) {
        this.buffer.put(b);
        return this;
    }

    public PacketBuffer writeByte(int position, byte b) {
        this.buffer.put(position, b);
        return this;
    }

    /*
     * CHARS
     */
    public char readChar() {
        return this.buffer.getChar();
    }

    public PacketBuffer writeChar(char value) {
        this.buffer.putChar(value);
        return this;
    }

    public char readChar(int position) {
        return this.buffer.getChar(position);
    }

    public PacketBuffer writeChar(int position, char value) {
        this.buffer.putChar(value);
        return this;
    }

    /*
     * DOUBLES
     */
    public double readDouble() {
        return this.buffer.getDouble();
    }

    public PacketBuffer writeDouble(double value) {
        this.buffer.putDouble(value);
        return this;
    }

    public double readDouble(int position) {
        return this.buffer.getDouble(position);
    }

    public PacketBuffer writeDouble(int position, double value) {
        this.buffer.putDouble(position, value);
        return this;
    }

    /*
     * FLOATS
     */
    public float readFloat() {
        return this.buffer.getFloat();
    }

    public PacketBuffer writeFloat(float value) {
        this.buffer.putFloat(value);
        return this;
    }

    public float readFloat(int position) {
        return this.buffer.getFloat(position);
    }

    public PacketBuffer writeFloat(int position, float value) {
        this.buffer.putFloat(position, value);
        return this;
    }

    /*
     * INTEGER
     */
    public int readInt() {
        return this.buffer.getInt();
    }

    public PacketBuffer writeInt(int value) {
        this.buffer.putInt(value);
        return this;
    }

    public int readInt(int position) {
        return this.buffer.getInt(position);
    }

    public PacketBuffer writeInt(int position, int value) {
        this.buffer.putInt(position, value);
        return this;
    }

    /*
     * LONG
     */
    public long readLong() {
        return this.buffer.getLong();
    }

    public PacketBuffer writeLong(long value) {
        this.buffer.putLong(value);
        return this;
    }

    public long readLong(int position) {
        return this.buffer.getLong(position);
    }

    public PacketBuffer writeLong(int position, long value) {
        this.buffer.putLong(position, value);
        return this;
    }

    /*
     * SHORTS
     */
    public short readShort() {
        return this.buffer.getShort();
    }

    public PacketBuffer writeShort(short value) {
        this.buffer.putShort(value);
        return this;
    }

    public short readShort(int position) {
        return this.buffer.getShort(position);
    }

    public PacketBuffer writeShort(int position, short value) {
        this.buffer.putShort(position, value);
        return this;
    }

    /*
     * STRINGS
     */
    private final static Charset CHARSET = Charset.forName("UTF-8");

    public void writeString(String s) {
        byte[] b = s.getBytes(CHARSET);
        buffer.putInt(b.length);
        buffer.put(b);
    }

    public String readString() {
        int len = buffer.getInt();
        byte[] b = new byte[len];
        buffer.get(b);
        return new String(b, CHARSET);
    }
}
