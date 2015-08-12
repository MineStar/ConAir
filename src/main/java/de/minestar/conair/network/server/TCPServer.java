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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.packets.NetworkPacket;
import de.minestar.conair.network.packets.RegisterDenyPacket;
import de.minestar.conair.network.packets.RegisterOKPacket;
import de.minestar.conair.network.packets.RegisterRequestPacket;
import de.minestar.conair.network.server.api.PluginManager;
import de.minestar.conair.network.server.api.events.BroadcastPacketReceivedEvent;

public final class TCPServer implements Runnable {

    private final ByteBuffer _networkBuffer;
    private Selector _selector;
    private ServerSocketChannel _serverSocket;
    private boolean _isRunning;
    private List<String> _addressWhitelist;
    private ServerPacketHandler _packetHandler;
    private ServerSidePacketHandler _serverSidePacketHandler;
    private PluginManager _pluginManager;

    public TCPServer(int port, List<String> addressWhitelist) throws Exception {
        System.out.println("--------------------");
        System.out.println("Starting server on port " + port + "...");

        _networkBuffer = ByteBuffer.allocateDirect(8 * 1024);

        _packetHandler = new ServerPacketHandler(_networkBuffer);

        _selector = Selector.open();

        // Listening on the port
        _serverSocket = ServerSocketChannel.open();
        _serverSocket.socket().bind(new InetSocketAddress(port));

        // Non-Blocking for Selector activity
        _serverSocket.configureBlocking(false);
        _serverSocket.register(_selector, SelectionKey.OP_ACCEPT);

        _isRunning = true;

        if (addressWhitelist == null) {
            addressWhitelist = new ArrayList<String>();
        }
        if (addressWhitelist.isEmpty()) {
            addressWhitelist.add("127.0.0.1");
        }
        _addressWhitelist = addressWhitelist;

        // register standardpackets
        registerStandardPacketTypes();

        // create ServerSidePacketHandler
        _serverSidePacketHandler = new ServerSidePacketHandler();
    }

    private final void registerStandardPacketTypes() {
        registerSinglePacket(RegisterRequestPacket.class);
        registerSinglePacket(RegisterOKPacket.class);
        registerSinglePacket(RegisterDenyPacket.class);
    }

    private final <P extends NetworkPacket> void registerSinglePacket(Class<P> packetClazz) {
        Integer ID = PacketType.getID(packetClazz);
        if (ID == null) {
            PacketType.registerPacket(packetClazz);
        }
    }

    @Override
    public void run() {
        System.out.println("Server started!");
        while (_isRunning) {
            try {
                int rdyChannels = _selector.select();
                // No channel want something
                if (rdyChannels == 0) {
                    continue;
                }

                // Iterate over all channel which want something
                Iterator<SelectionKey> it = _selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    // New client wants to connect
                    if (key.isAcceptable()) {
                        // accept new client
                        onClientAccept();
                    }
                    // client is sending something
                    if (key.isReadable()) {
                        onClientRead(key);
                    }
                    // client can receive something
                    if (key.isWritable()) {
                        onClientWrite(key);
                    }
                    it.remove();
                }
                // sleep for 1 nanosecond...
                Thread.sleep(0, 1);
            } catch (Exception e) {
                if (!(e instanceof java.nio.channels.CancelledKeyException)) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * STOPPING
     */
    public void stop() {
        _isRunning = false;
        System.out.println("--------------------");
        System.out.println("Stopping server...");
        try {
            _selector.close();
            _serverSocket.socket().close();
            _serverSocket.socket().getChannel().close();
            _serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server stopped!");
        System.out.println("--------------------");
    }
    /*
     * ACCEPTING
     */

    public void onClientAccept() throws Exception {
        // accept new client
        SocketChannel clientSocket = _serverSocket.accept();

        // Is client allowed to connect?
        String address = clientSocket.getRemoteAddress().toString();

        // Remove the port number
        int i = address.indexOf(':');
        if (i != -1)
            address = address.substring(0, i);

        if (address.startsWith("/")) {
            address = address.substring(1, address.length());
        }

        // Client is not allowed to connect - refuse connection
        if (!_addressWhitelist.contains(address)) {
            clientSocket.close();
            System.out.println("Client is not whitelisted: " + address);
            return;
        }

        address = address + ":" + clientSocket.socket().getPort();
        clientSocket.configureBlocking(false);
        clientSocket.register(_selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE).attach(new ConnectedServerClient(address));
        System.out.println("Client connected from: " + address);
    }

    /*
     * READING
     */
    public void onClientRead(SelectionKey key) throws Exception {
        if (!(key.channel() instanceof SocketChannel)) {
            return;
        }

        // Read into the clients specific buffer
        SocketChannel channel = (SocketChannel) key.channel();
        ConnectedServerClient client = (ConnectedServerClient) key.attachment();
        // When readfrom fails the client has disconnected
        if (!client.readFrom(channel)) {
            key.cancel();
            _serverSidePacketHandler.unregisterClient(client);
            System.out.println("Client '" + client.getName() + "' disconnected!");
        }

        if (_packetHandler.isPacketComplete(client.getClientBuffer())) {
            // extract the packet
            NetworkPacket packet = _packetHandler.extractPacket(client.getClientBuffer());

            // if we have found a packet, we handle it...
            if (packet != null) {
                handlePacket(client, packet);
            }

            // clear the clientBuffer
            client.getClientBuffer().clear();
        }
    }

    // Handle a single packet
    private <P extends NetworkPacket> void handlePacket(ConnectedServerClient client, P packet) {
        // We have a broadcast server - broadcast all packages
        if (packet.isBroadcastPacket()) {
            /*
             * CALL EVENT - BroadcastPacketReceivedEvent
             */
            BroadcastPacketReceivedEvent event = new BroadcastPacketReceivedEvent(packet);
            _pluginManager.callEvent(event);

            // ignore the packet, if it is cancelled
            if (!event.isCancelled()) {
                broadcastPacket(client, packet);
            }
        } else {
            boolean result = _serverSidePacketHandler.handlePacket(client, packet);
            if (!result) {
                // TODO: call NonBroadcastPacketReceivedEvent on ServerSide
            }
        }
    }

    // Deliver the packet the all other clients
    private <P extends NetworkPacket> void broadcastPacket(ConnectedServerClient src, P packet) {
        Set<SelectionKey> keys = _selector.keys();

        for (SelectionKey key : keys) {
            if (!(key.channel() instanceof SocketChannel))
                continue;

            ConnectedServerClient client = (ConnectedServerClient) key.attachment();

            // ignore if the client is the sender
            if (client.equals(src))
                continue;

            // add packetdata to clientbuffer
            client.sendPacket(packet);
        }

        // clear the networkBuffer
        _networkBuffer.clear();
    }

    /*
     * WRITING
     */
    private void onClientWrite(SelectionKey key) throws Exception {
        if (!(key.channel() instanceof SocketChannel)) {
            return;
        }

        ConnectedServerClient client = (ConnectedServerClient) key.attachment();
        if (client.hasDataToSend()) {
            // If write fails the client has disconnected
            if (!client.write((SocketChannel) key.channel())) {
                key.cancel();
            }
        }
    }

    public void setPluginManager(PluginManager pluginManager) {
        _pluginManager = pluginManager;
        _serverSidePacketHandler.setPluginManager(pluginManager);
    }
}
