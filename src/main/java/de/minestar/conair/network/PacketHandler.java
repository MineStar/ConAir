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

public class PacketHandler {

    private final PacketBuffer packetbuffer = new PacketBuffer();
    private int pos = -1;

    public static final byte PACKET_SEPERATOR = 3;

    public PacketHandler() {

    }

    public boolean isPacketComplete(ByteBuffer buffer) {

        buffer.flip();
        while (buffer.hasRemaining()) {
            if (buffer.get() == PACKET_SEPERATOR) {
                pos = buffer.position() - 1;
                buffer.rewind();
                return true;
            }
        }
        buffer.limit(buffer.capacity());
        return false;
    }

    // TODO: Return a network packet
    public void extractPacket(ByteBuffer buffer) {
        int limit = buffer.limit();
        buffer.clear();
        buffer.limit(pos);

        packetbuffer.put(buffer);
        buffer.limit(limit);
        buffer.position(pos + 1);
        buffer.compact();

        pos = -1;
    }

    public byte[] read() {
        packetbuffer.getBuffer().flip();
        byte[] b = new byte[packetbuffer.getBuffer().limit()];
        packetbuffer.get(b);

        packetbuffer.clear();
        return b;

    }
}
