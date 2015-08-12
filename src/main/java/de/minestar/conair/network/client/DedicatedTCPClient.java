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

import java.io.IOException;

import de.minestar.conair.network.packets.NetworkPacket;

public class DedicatedTCPClient {

    private Thread _clientThread;
    private TCPClient _client;

    private String _clientName;
    private ClientPacketHandler _packetHandler;
    private String _host;
    private int _port;

    public DedicatedTCPClient(String clientName, ClientPacketHandler packetHandler, String host, int port) throws IOException {
        try {
            _clientName = clientName;
            _packetHandler = packetHandler;
            _host = host;
            _port = port;

            _client = new TCPClient(clientName, packetHandler, host, port);
            _clientThread = new Thread(_client);
            _clientThread.start();
        } catch (IOException e) {
            stop();
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        if (_client != null) {
            _client.stop();
            _client = null;
            if (_clientThread != null) {
                _clientThread.stop();
                _clientThread = null;
            }
        }
    }

    public String getClientName() {
        return _clientName;
    }

    public ClientPacketHandler getPacketHandler() {
        return _packetHandler;
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    public <P extends NetworkPacket> void sendPacket(P packet) {
        _client.sendPacket(packet);
    }
}
