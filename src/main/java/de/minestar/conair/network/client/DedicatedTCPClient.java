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

import de.minestar.conair.network.packets.NetworkPacket;

public class DedicatedTCPClient {

    private Thread clientThread;
    private TCPClient client;

    private String clientName;
    private ClientPacketHandler packetHandler;
    private String host;
    private int port;

    public DedicatedTCPClient(String clientName, ClientPacketHandler packetHandler, String host, int port) {
        try {
            this.clientName = clientName;
            this.packetHandler = packetHandler;
            this.host = host;
            this.port = port;

            this.client = new TCPClient(clientName, packetHandler, host, port);
            this.clientThread = new Thread(this.client);
            this.clientThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            this.stop();
        }
    }
    @SuppressWarnings("deprecation")
    public void stop() {
        if (this.client != null) {
            this.client.stop();
            this.client = null;
            if (this.clientThread != null) {
                this.clientThread.stop();
                this.clientThread = null;
            }
        }
    }

    public String getClientName() {
        return clientName;
    }

    public ClientPacketHandler getPacketHandler() {
        return packetHandler;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void sendPacket(NetworkPacket packet) {
        this.client.sendPacket(packet);
    }
}
