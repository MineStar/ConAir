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

import java.util.concurrent.ConcurrentLinkedQueue;

import de.minestar.conair.network.packets.NetworkPacket;

public final class PacketQueue {

    private ConcurrentLinkedQueue<NetworkPacket> _packetQueue;
    private NetworkPacket _activePacket;
    private boolean _active;

    public PacketQueue() {
        _packetQueue = new ConcurrentLinkedQueue<NetworkPacket>();
        _activePacket = null;
        _active = false;
    }

    public <P extends NetworkPacket> boolean addUnsafePacket(P packet) {
        synchronized (_packetQueue) {
            _packetQueue.add(packet);
            return true;
        }
    }

    public <P extends NetworkPacket> boolean addPacket(P packet) {
        synchronized (_packetQueue) {
            if (PacketType.getID(packet.getClass()) != null) {
                _packetQueue.add(packet);
                return true;
            }
            return false;
        }
    }

    public boolean updateQueue() {
        synchronized (_packetQueue) {
            if (_packetQueue.isEmpty()) {
                return false;
            }
            _activePacket = _packetQueue.poll();
            _active = (_activePacket != null);
            return _active;
        }
    }

    public boolean isActive() {
        return _active;
    }

    public NetworkPacket getActivePacket() {
        return _activePacket;
    }

    public boolean packPacket(PacketBuffer packetBuffer) {
        if (isActive()) {
            packetBuffer.clear();
            boolean result = _activePacket.pack(packetBuffer);
            packetBuffer.getBuffer().flip();
            return result;
        }
        return false;
    }

    public int getSize() {
        return _packetQueue.size();
    }

}
