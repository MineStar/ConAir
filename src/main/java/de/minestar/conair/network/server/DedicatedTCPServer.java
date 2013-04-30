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

package de.minestar.conair.network.server;

import java.util.List;

public class DedicatedTCPServer {

    private int port;
    private TCPServer server;
    private Thread serverThread;

    public DedicatedTCPServer(int port, List<String> whiteList) {
        try {
            this.port = port;
            this.server = new TCPServer(port, whiteList);
            this.serverThread = new Thread(this.server);
            this.serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            this.stop();
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        if (this.server != null) {
            this.server.stop();
            this.server = null;
            if (this.serverThread != null) {
                this.serverThread.stop();
                this.serverThread = null;
            }
        }
    }

    public int getPort() {
        return port;
    }
}
