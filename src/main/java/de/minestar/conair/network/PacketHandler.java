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

import de.minestar.conair.network.packets.HelloWorldPacket;

public class PacketHandler {

    protected final PacketBuffer packetbuffer;

    public static final byte PACKET_SEPERATOR = 3;

    public PacketHandler(ByteBuffer buffer) {
        this.packetbuffer = new PacketBuffer(buffer);
    }

    public boolean isPacketComplete(ByteBuffer buffer) {
        buffer.flip();
        int len = 0;
        if (buffer.hasRemaining())
            len = buffer.getInt();
        else {
            System.out.println(1);
            return false;
        }
        if (buffer.remaining() <= len - 4) {
            System.out.println(2);
            return false;
        }
        boolean end = (buffer.get(len) == PACKET_SEPERATOR);
        System.out.println("complete: " + end);
        return end;
    }

    public NetworkPacket extractPacket(ByteBuffer src) {
        src.rewind();
        int len = src.getInt();
        int limit = src.limit();
        src.limit(len);
        packetbuffer.clear();
        packetbuffer.put(src);
        packetbuffer.getBuffer().flip();
        src.limit(limit);
        src.compact();
        return createPacket();
    }

    public void packPacket(NetworkPacket packet) {
        packetbuffer.clear();
        packet.pack(packetbuffer);
        packetbuffer.getBuffer().flip();
    }

    private NetworkPacket createPacket() {
        int type = packetbuffer.getInt();
        switch (type) {
            case 0 :
                return new HelloWorldPacket(packetbuffer);
            default :
                return null;
        }
    }

}
