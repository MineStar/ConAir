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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.packets.NetworkPacket;
import de.minestar.conair.network.packets.RegisterDenyPacket;
import de.minestar.conair.network.packets.RegisterOKPacket;
import de.minestar.conair.network.packets.RegisterRequestPacket;

public final class TCPClient implements Runnable {

    private Selector _selector;
    private SocketChannel _socketChannel;
    private ConnectedClient _client;
    private boolean _isRunning;
    private final String _clientName;
    private ClientPacketHandler _packetHandler;
    private ClientSidePacketHandler _clientSidePacketHandler;

    private Map<Class<? extends NetworkPacket>, Method> _methodMap;
    private Set<Class<? extends NetworkPacket>> _searchedMethods;

    public TCPClient(String name, ClientPacketHandler packetHandler, String host, int port) throws Exception {
        _clientName = name;

        _packetHandler = packetHandler;

        _selector = Selector.open();

        // Create Connection to the server
        _socketChannel = SocketChannel.open(new InetSocketAddress(host, port));

        // Non-Blocking for Selector activity
        _socketChannel.configureBlocking(false);
        _socketChannel.register(_selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        _isRunning = true;

        _client = new ConnectedClient("localhost");

        _methodMap = new HashMap<Class<? extends NetworkPacket>, Method>();
        _searchedMethods = new HashSet<Class<? extends NetworkPacket>>();

        // create ClientSidePacketHandler
        _clientSidePacketHandler = new ClientSidePacketHandler(this);

        // register standardpackets
        registerStandardPacketTypes();

        // send RegisterRequestPacket
        sendPacket(new RegisterRequestPacket(_clientName));
    }

    private final <P extends NetworkPacket> Method searchMethod(Class<P> packetClass) {
        if (_searchedMethods.contains(packetClass)) {
            return _methodMap.get(packetClass);
        }
        final Method[] declaredMethods = _packetHandler.getClass().getDeclaredMethods();
        for (final Method method : declaredMethods) {
            if (method.getParameterCount() != 1) {
                continue;
            }
            if (method.getParameterTypes()[0].equals(packetClass)) {
                _methodMap.put(packetClass, method);
                return method;
            }
        }
        _searchedMethods.add(packetClass);
        return null;
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

    /*
     * RUNNING
     */
    @Override
    public final void run() {
        try {
            while (_isRunning) {
                int rdyChannels = _selector.select();

                // No channel want something
                if (rdyChannels == 0) {
                    continue;
                }

                // Iterate over all channel which want something
                Iterator<SelectionKey> it = _selector.selectedKeys().iterator();
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
            if (!(e instanceof CancelledKeyException)) {
                e.printStackTrace();
            }
            _isRunning = false;
        }
    }

    /*
     * STOPPING
     */
    public final void stop() {
        try {
            _isRunning = false;
            System.out.println("Stopping client '" + _clientName + "' ...");
            _socketChannel.socket().close();
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
        if (!_client.readFrom(_socketChannel)) {
            _isRunning = false;
            return;
        }

        if (_packetHandler.isPacketComplete(_client.getClientBuffer())) {
            // extract the packet
            NetworkPacket packet = _packetHandler.extractPacket(_client.getClientBuffer());

            // if we have found a packet, we handle it...
            if (packet != null) {
                handlePacket(packet);
            }

            // clear the clientBuffer
            _client.getClientBuffer().clear();
        } else {
            System.out.println("Packet incomplete: " + _client.getClientBuffer());
        }
    }

    /*
     * HANDLING
     */
    private <P extends NetworkPacket> void handlePacket(P packet) {
        if (!_clientSidePacketHandler.handlePacket(packet)) {
            Method method = searchMethod(packet.getClass());
            if (method != null) {
                method.setAccessible(true);
                try {
                    method.invoke(_packetHandler, packet);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                method.setAccessible(false);
            }
        }
    }

    /*
     * QUEUEING
     */
    public final <P extends NetworkPacket> void sendPacket(P packet) {
        _packetHandler.sendPacket(packet);
    }

    /*
     * WRITING
     */
    private final void onClientWrite() throws Exception {
        if (_client.hasDataToSend()) {
            // If write fails the client has disconnected
            if (!_client.write(_socketChannel)) {
                stop();
                return;
            }
        } else {
            if (!_client.hasDataToSend()) {
                _packetHandler.updateQueue(_client);
            }
        }
    }

}
