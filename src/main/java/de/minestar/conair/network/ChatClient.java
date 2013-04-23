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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Logger;

import de.minestar.conair.core.Core;

public class ChatClient implements Runnable {

    private final ByteBuffer networkBuffer;

    private Selector selector;

    private SocketChannel socketChannel;

    private ConnectedClient client;

    private boolean isRunning;

    private PacketHandler packetHandler;

    public ChatClient(String host, int port) throws Exception {

        this.networkBuffer = ByteBuffer.allocateDirect(4096);

        this.packetHandler = new PacketHandler(networkBuffer);

        this.selector = Selector.open();

        // Create Connection to the server
        this.socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        // Non-Blocking for Selector activity
        this.socketChannel.configureBlocking(false);
        this.socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        this.isRunning = true;

        this.client = new ConnectedClient("localhost");
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                int rdyChannels = selector.select();
                // No channel want something
                if (rdyChannels == 0) {
                    continue;
                }

                // Iterate over all channel which want something
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    // client want to send something
                    if (key.isReadable()) {
                        onClientRead();
                    }
                    // client can receive something
                    if (key.isWritable()) {
                        onClientWrite();
                    }
                    it.remove();
                }

            }
        } catch (Exception e) {
            Logger.getLogger(Core.NAME).throwing("de.minestar.conair.core.network.ChatServer", "run", e);
            isRunning = false;
        }
    }

    public void stop() {
        this.isRunning = false;
    }

    /*
     * READING
     */
    public void onClientRead() throws Exception {
        // When readfrom fails the client has disconnected
        if (!client.readFrom(this.socketChannel)) {
            isRunning = false;
            return;
        }

        if (packetHandler.isPacketComplete(client.getClientBuffer())) {
            NetworkPacket packet = packetHandler.extractPacket(client.getClientBuffer());
            handlePacket(packet);
        }

    }

    public void sendPacket(NetworkPacket packet) {
        packetHandler.packPacket(packet);
        this.client.addPacket(packetHandler.packetbuffer.getBuffer());
    }

    // Handle a single packet
    private void handlePacket(NetworkPacket packet) {

    }

    /*
     * WRITINGG
     */
    private void onClientWrite() throws Exception {

        if (client.hasDataToSend()) {
            // If write fails the client has disconnected
            if (!client.write(socketChannel)) {
                this.isRunning = false;
                return;
            }
        }
    }

}
