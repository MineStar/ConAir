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

package de.minestar.conair.network.packets;

import java.io.IOException;

import de.minestar.conair.network.NetworkPacket;
import de.minestar.conair.network.PacketBuffer;

public class HelloWorldPacket extends NetworkPacket {

    private String text;

    public HelloWorldPacket(String text) {
        this.text = text;
    }

    public HelloWorldPacket(int packetID, PacketBuffer buffer) throws IOException {
        super(packetID, buffer);
    }

    @Override
    public void onSend(PacketBuffer buffer) {
        buffer.writeString(this.text);
    }

    @Override
    public void onReceive(PacketBuffer buffer) {
        this.text = buffer.readString();

    }

    public String getText() {
        return text;
    }

}
