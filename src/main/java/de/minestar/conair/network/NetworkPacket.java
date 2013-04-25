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

import java.io.IOException;

public abstract class NetworkPacket {

    private int packetID = -1;

    /**
     * Empty constructor. Used for creation of packets to be sent.
     */
    protected NetworkPacket() {
    }

    /**
     * Constructor used for received packets.
     * 
     * @param packetID
     * @param buffer
     * @throws IOException
     */
    public NetworkPacket(int packetID, PacketBuffer buffer) throws IOException {
        this.packetID = packetID;
        onReceive(buffer);
    }

    protected final boolean pack(PacketBuffer buffer) {
        Integer packetID = PacketType.getID(this.getClass());
        if (packetID != null) {
            buffer.writeInt(0); // Size
            buffer.writeInt(packetID); // Type
            onSend(buffer); // Content
            buffer.writeInt(0, buffer.getBuffer().position()); // Write size
            buffer.put(PacketHandler.PACKET_SEPERATOR); // Close packet
            return true;
        } else {
            return false;
        }
    }
    public abstract void onSend(PacketBuffer buffer);

    public abstract void onReceive(PacketBuffer buffer);

    public int getPacketID() {
        return packetID;
    }
}
