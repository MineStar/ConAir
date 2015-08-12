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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class ConnectedClient {

    private final ByteBuffer _inBuffer = ByteBuffer.allocateDirect(32 * 1024);
    private final ByteBuffer _outBuffer = ByteBuffer.allocateDirect(4 * 1024);

    private boolean dataToSend = false;

    private String name;

    public ConnectedClient(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    boolean readFrom(SocketChannel channel) throws Exception {
        int b = 0;
        try {
            b = channel.read(_inBuffer);
        } catch (IOException e) {
            return false;
        }
        return b != -1;
    }

    void addByteBuffer(ByteBuffer buffer) {
        if (!this.dataToSend) {
            _outBuffer.put(buffer);
            _outBuffer.flip();
            buffer.rewind();
            this.dataToSend = true;
        }
    }

    boolean hasDataToSend() {
        return dataToSend;
    }

    boolean write(SocketChannel channel) throws IOException {
        int b = 0;
        try {
            b = channel.write(_outBuffer);
        } catch (IOException e) {
            return false;
        }
        if (b == 0) {
            dataToSend = false;
            _outBuffer.clear();
        }
        return b != -1;
    }

    ByteBuffer getClientBuffer() {
        return _inBuffer;
    }

}
