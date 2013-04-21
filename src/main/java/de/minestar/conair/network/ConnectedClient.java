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
import java.nio.channels.SocketChannel;

public class ConnectedClient {

    private final ByteBuffer clientBuffer = ByteBuffer.allocateDirect(4096);

    private String name;

    public ConnectedClient(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void readFrom(SocketChannel channel) throws Exception {
        channel.read(clientBuffer);
    }

    public String getName() {
        return name;
    }

    public boolean isPacketComplete() {
        // TODO: Implement search for packet seperator
        return false;
    }

}
