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

package de.minestar.conair.common.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import de.minestar.conair.api.Packet;


public class SplittedPacket implements Packet, Comparable<SplittedPacket> {

    private static final Random RANDOM = new Random();


    public static <P extends Packet> Collection<SplittedPacket> split(final int MAX_PACKET_SIZE, final int dataLength, final P packet, final String data) {
        final Collection<SplittedPacket> packets = new ArrayList<SplittedPacket>();

        final long id = getNextFreeId();
        int totalPackets = (int) (((double) dataLength / (double) MAX_PACKET_SIZE));
        if (dataLength % MAX_PACKET_SIZE != 0) {
            totalPackets++;
        }
        int currentPacketId = 1;
        for (int i = 0; i < dataLength; i += MAX_PACKET_SIZE) {
            packets.add(new SplittedPacket(packet, id, currentPacketId, totalPackets, data.substring(i, Math.min(i + MAX_PACKET_SIZE, dataLength))));
            currentPacketId++;
        }
        return packets;
    }


    private static long getNextFreeId() {
        return RANDOM.nextLong();
    }

    private final long _id;
    private final String _packetClass;
    private final long _currentPacketId;
    private final long _totalPackets;
    private final String _data;


    private <P extends Packet> SplittedPacket(final P packetClass, final long id, final long currentPacketId, final long totalPackets, final String data) {
        _id = id;
        _packetClass = packetClass.getClass().getName();
        _currentPacketId = currentPacketId;
        _totalPackets = totalPackets;
        _data = data;
    }


    public long getId() {
        return _id;
    }


    public String getPacketClass() {
        return _packetClass;
    }


    public long getCurrentPacketId() {
        return _currentPacketId;
    }


    public long getTotalPackets() {
        return _totalPackets;
    }


    public String getData() {
        return _data;
    }


    @Override
    public String toString() {
        return "SmallPacket [id=" + _id + ", totalPackets=" + _totalPackets + ", data=" + _data + "]";
    }


    @Override
    public int compareTo(SplittedPacket o) {
        if (_currentPacketId < o._currentPacketId) {
            return -1;
        }
        if (_currentPacketId > o._currentPacketId) {
            return +1;
        }
        return 0;

    }
}
