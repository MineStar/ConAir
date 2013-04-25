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

import de.minestar.conair.network.packets.RAWPacket;

public class ServerPacketHandler {

    protected final PacketBuffer packetBuffer;

    public static final byte PACKET_SEPERATOR = 3;

    public ServerPacketHandler(ByteBuffer buffer) {
        this.packetBuffer = new PacketBuffer(buffer);
    }

    public boolean isPacketComplete(ByteBuffer buffer) {
        buffer.flip();
        int len = 0;
        if (buffer.hasRemaining()) {
            len = buffer.getInt();
        } else {
            return false;
        }
        if (buffer.remaining() <= len - 4) {
            return false;
        }
        return (buffer.get(len) == PACKET_SEPERATOR);
    }

    public RAWPacket extractPacket(ByteBuffer src) {
        src.rewind();
        int len = src.getInt();
        int limit = src.limit();
        src.limit(len);
        packetBuffer.clear();
        packetBuffer.writeByteBuffer(src);
        packetBuffer.getBuffer().flip();
        src.limit(limit);
        src.compact();
        return createPacket(len);
    }

    public boolean packPacket(RAWPacket packet) {
        packetBuffer.clear();
        boolean result = packet.pack(packetBuffer);
        packetBuffer.getBuffer().flip();
        return result;
    }

    private RAWPacket createPacket(int datalength) {
        try {
            // reduce the datalength, because we read two integers first.
            // One integer is 4 bytes long
            datalength -= 8;

            // get packettype
            int packetID = packetBuffer.readInt();

            // read data...
            byte[] data = new byte[datalength];
            packetBuffer.readBytes(data);

            // ... and create a new PacketBuffer
            PacketBuffer newBuffer = new PacketBuffer(data.length);
            newBuffer.writeBytes(data);
            newBuffer.getBuffer().rewind();

            // finally create the packet and return it
            return new RAWPacket(packetID, newBuffer.getBuffer());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
