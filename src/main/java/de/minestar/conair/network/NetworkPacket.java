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

public abstract class NetworkPacket {

    private final PacketType type;

    protected NetworkPacket(PacketType type) {
        this.type = type;
    }

    protected NetworkPacket(PacketType type, PacketBuffer buffer) {
        this(type);
        onReceive(buffer);
    }

    public final PacketType getType() {
        return type;
    }

    protected final void pack(PacketBuffer buffer) {
        buffer.putInt(0); // Size
        buffer.putInt(type.ordinal()); // Type
        onSend(buffer); // Content
        buffer.putInt(0, buffer.getBuffer().position()); // Write size
        buffer.put(PacketHandler.PACKET_SEPERATOR); // Close packet
    }

    public abstract void onSend(PacketBuffer buffer);

    public abstract void onReceive(PacketBuffer buffer);

}
