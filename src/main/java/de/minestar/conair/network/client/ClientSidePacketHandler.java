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

package de.minestar.conair.network.client;

import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.packets.NetworkPacket;
import de.minestar.conair.network.packets.RegisterDenyPacket;
import de.minestar.conair.network.packets.RegisterOKPacket;

public class ClientSidePacketHandler extends AbstractClientPacketHandler {

    private final TCPClient client;

    public ClientSidePacketHandler(TCPClient client) {
        this.client = client;
    }

    @Override
    public <P extends NetworkPacket> boolean handlePacket(P packet) {
        if (packet.getPacketID() == PacketType.getID(RegisterOKPacket.class)) {
            this.handleRegisterOKPacket((RegisterOKPacket) packet);
            return true;
        }
        if (packet.getPacketID() == PacketType.getID(RegisterDenyPacket.class)) {
            this.handleRegisterDenyPacket((RegisterDenyPacket) packet);
            return true;
        }
        return false;
    }

    private void handleRegisterDenyPacket(RegisterDenyPacket packet) {
        System.out.println("Server denied connection! Disconnecting...");
        this.client.stop();
    }

    private void handleRegisterOKPacket(RegisterOKPacket packet) {
        System.out.println("Successfully connected to the server!");
    }
}
