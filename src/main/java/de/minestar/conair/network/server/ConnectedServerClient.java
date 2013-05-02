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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import de.minestar.conair.network.PacketBuffer;
import de.minestar.conair.network.PacketQueue;
import de.minestar.conair.network.packets.NetworkPacket;

public final class ConnectedServerClient {

    private final PacketBuffer inBuffer = new PacketBuffer(ByteBuffer.allocateDirect(32 * 1024));
    private final PacketBuffer outBuffer = new PacketBuffer(ByteBuffer.allocateDirect(4 * 1024));

    private boolean dataToSend = false;

    private String name;

    private final PacketQueue packetQueue;

    public ConnectedServerClient(String name) {
        this.name = name;
        this.packetQueue = new PacketQueue();
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean readFrom(SocketChannel channel) throws Exception {
        int b = 0;
        try {
            b = channel.read(inBuffer.getBuffer());
        } catch (IOException e) {
            return false;
        }
        return b != -1;
    }

    public void sendPacket(NetworkPacket packet) {
        this.packetQueue.addUnsafePacket(packet);
        boolean wasEmpty = this.packetQueue.getSize() == 1;
        // the queue was empty, so we send the first packet
        if (wasEmpty) {
            if (this.packetQueue.updateQueue()) {
                this.packetQueue.packPacket(this.outBuffer);
                this.dataToSend = true;
            }
        }
    }

//    private void addByteBuffer(ByteBuffer buffer) {
//        if (!this.dataToSend) {
//            this.outBuffer.getBuffer().put(buffer);
//            this.outBuffer.getBuffer().flip();
//            buffer.rewind();
//            this.dataToSend = true;
//        }
//    }

    public boolean hasDataToSend() {
        return dataToSend;
    }

    public boolean write(SocketChannel channel) throws IOException {
        int b = 0;
        try {
            b = channel.write(outBuffer.getBuffer());
        } catch (IOException e) {
            return false;
        }
        if (b == 0) {
            dataToSend = false;
            this.outBuffer.clear();

            if (this.packetQueue.updateQueue()) {
                this.packetQueue.packPacket(this.outBuffer);

                this.dataToSend = true;
            }
        }
        return b != -1;
    }

    public String getName() {
        return name;
    }

    public ByteBuffer getClientBuffer() {
        return inBuffer.getBuffer();
    }

}
