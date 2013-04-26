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
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import de.minestar.conair.network.ConnectedClient;
import de.minestar.conair.network.NetworkPacket;

public final class ChatClient implements Runnable {

    private Selector selector;

    private SocketChannel socketChannel;

    private ConnectedClient client;

    private boolean isRunning;

    private ClientPacketHandler packetHandler;

    public ChatClient(ClientPacketHandler packetHandler, String host, int port) throws Exception {
        this.packetHandler = packetHandler;

        this.selector = Selector.open();

        // Create Connection to the server
        this.socketChannel = SocketChannel.open(new InetSocketAddress(host, port));

        // Non-Blocking for Selector activity
        this.socketChannel.configureBlocking(false);
        this.socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        this.isRunning = true;

        this.client = new ConnectedClient("localhost");
    }

    /*
     * RUNNING
     */
    @Override
    public final void run() {
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
                // sleep for 1 nanosecond...
                Thread.sleep(0, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            isRunning = false;
        }
    }

    /*
     * STOPPING
     */
    public final void stop() {
        try {
            this.isRunning = false;
            System.out.println("Stopping client...");
            this.socketChannel.close();
            System.out.println("Client stopped!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * READING
     */
    public final void onClientRead() throws Exception {
        // When readfrom fails the client has disconnected
        if (!client.readFrom(this.socketChannel)) {
            isRunning = false;
            return;
        }

        if (packetHandler.isPacketComplete(client.getClientBuffer())) {
            // extract the packet
            NetworkPacket packet = packetHandler.extractPacket(client.getClientBuffer());

            // if we have found a packet, we handle it...
            if (packet != null) {
                this.handlePacket(packet);
            }

            // clear the clientBuffer
            client.getClientBuffer().clear();
        } else {
            System.out.println("Packet incomplete: " + client.getClientBuffer());
        }
    }

    /*
     * HANDLING
     */
    private void handlePacket(NetworkPacket packet) {
        this.packetHandler.handlePacket(packet);
    }

    /*
     * QUEUEING
     */
    public final void sendPacket(NetworkPacket packet) {
        this.packetHandler.sendPacket(packet);
    }

    /*
     * WRITING
     */
    private final void onClientWrite() throws Exception {
        if (client.hasDataToSend()) {
            // If write fails the client has disconnected
            if (!client.write(socketChannel)) {
                this.stop();
                return;
            }
        } else {
            if (!client.hasDataToSend()) {
                this.packetHandler.updateQueue(client);
            }
        }
    }

}
