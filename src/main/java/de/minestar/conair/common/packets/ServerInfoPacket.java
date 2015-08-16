/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Minestar.de
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.minestar.conair.common.packets;

import de.minestar.conair.api.Packet;


public class ServerInfoPacket implements Packet {

    private final String _serverName;
    private final String[] _connectedClients;


    public ServerInfoPacket(String serverName, String... connectedClients) {
        _serverName = serverName;
        _connectedClients = new String[connectedClients.length];
        for (int i = 0; i < connectedClients.length; i++) {
            _connectedClients[i] = connectedClients[i];
        }
    }


    public String getServerName() {
        return _serverName;
    }


    public String[] getConnectedClients() {
        return _connectedClients;
    }


    @Override
    public String toString() {
        return "ConnectedClientsPacket [serverName=" + _serverName + ", connectedClients=" + _connectedClients + "]";
    }

}
