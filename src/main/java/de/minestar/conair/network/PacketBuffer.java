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

public class PacketBuffer {

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

    protected ByteBuffer getBuffer() {
        return this.buffer;
    }

    /*
     * BYTES AND BYTE ARRAYS
     */

    public byte get() {
        return this.buffer.get();
    }

    public PacketBuffer put(byte b) {
        this.buffer.put(b);
        return this;
    }

    public PacketBuffer get(byte[] dst) {
        this.buffer.get(dst);
        return this;
    }

    public PacketBuffer put(byte[] src) {
        this.buffer.put(src);
        return this;
    }

    public PacketBuffer get(byte[] dst, int offset, int length) {
        this.buffer.get(dst, offset, length);
        return this;
    }

    public PacketBuffer put(byte[] src, int offset, int length) {
        this.buffer.put(src, offset, length);
        return this;
    }

    public PacketBuffer put(ByteBuffer src) {
        this.buffer.put(src);
        return this;
    }

    public PacketBuffer put(PacketBuffer src) {
        this.buffer.put(src.buffer);
        return this;
    }

    public byte get(int index) {
        return this.buffer.get(index);
    }

    public PacketBuffer put(int index, byte b) {
        this.buffer.put(index, b);
        return this;
    }

    /*
     * CHARS
     */
    public char getChar() {
        return this.buffer.getChar();
    }

    public PacketBuffer putChar(char value) {
        this.buffer.putChar(value);
        return this;
    }

    public char getChar(int index) {
        return this.buffer.getChar(index);
    }

    public PacketBuffer putChar(int index, char value) {
        this.buffer.putChar(value);
        return this;
    }

    /*
     * DOUBLES
     */
    public double getDouble() {
        return this.buffer.getDouble();
    }

    public PacketBuffer putDouble(double value) {
        this.buffer.putDouble(value);
        return this;
    }

    public double getDouble(int index) {
        return this.buffer.getDouble(index);
    }

    public PacketBuffer putDouble(int index, double value) {
        this.buffer.putDouble(index, value);
        return this;
    }

    /*
     * FLOATS
     */
    public float getFloat() {
        return this.buffer.getFloat();
    }

    public PacketBuffer putFloat(float value) {
        this.buffer.putFloat(value);
        return this;
    }

    public float getFloat(int index) {
        return this.buffer.getFloat(index);
    }

    public PacketBuffer putFloat(int index, float value) {
        this.buffer.putFloat(index, value);
        return this;
    }

    /*
     * INTEGER
     */
    public int getInt() {
        return this.buffer.getInt();
    }

    public PacketBuffer putInt(int value) {
        this.buffer.putInt(value);
        return this;
    }

    public int getInt(int index) {
        return this.buffer.getInt(index);
    }

    public PacketBuffer putInt(int index, int value) {
        this.buffer.putInt(index, value);
        return this;
    }

    /*
     * LONG
     */
    public long getLong() {
        return this.buffer.getLong();
    }

    public PacketBuffer putLong(long value) {
        this.buffer.putLong(value);
        return this;
    }

    public long getLong(int index) {
        return this.buffer.getLong(index);
    }

    public PacketBuffer putLong(int index, long value) {
        this.buffer.putLong(index, value);
        return this;
    }

    /*
     * SHORTS
     */
    public short getShort() {
        return this.buffer.getShort();
    }

    public PacketBuffer putShort(short value) {
        this.buffer.putShort(value);
        return this;
    }

    public short getShort(int index) {
        return this.buffer.getShort(index);
    }

    public PacketBuffer putShort(int index, short value) {
        this.buffer.putShort(index, value);
        return this;
    }

    /*
     * STRINGS
     */
    private final static Charset CHARSET = Charset.forName("UTF-8");

    public void putString(String s) {
        byte[] b = s.getBytes(CHARSET);
        buffer.putInt(b.length);
        buffer.put(b);
    }

    public String getString() {
        int len = buffer.getInt();
        byte[] b = new byte[len];
        buffer.get(b);
        return new String(b, CHARSET);
    }
}
