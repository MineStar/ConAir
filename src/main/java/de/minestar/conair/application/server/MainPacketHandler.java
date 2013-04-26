package de.minestar.conair.application.server;
import de.minestar.conair.network.NetworkPacket;
import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.client.ClientPacketHandler;
import de.minestar.conair.network.packets.HelloWorldPacket;

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

public class MainPacketHandler extends ClientPacketHandler {

    private int pc = 0;

    @Override
    public void handlePacket(NetworkPacket packet) {
        if (packet.getPacketID() == PacketType.getID(HelloWorldPacket.class)) {
            HelloWorldPacket incoming = (HelloWorldPacket) packet;
            System.out.println("Text: " + incoming.getText());
        }
        System.out.println("Packetcount: " + (++pc));
    }
}
