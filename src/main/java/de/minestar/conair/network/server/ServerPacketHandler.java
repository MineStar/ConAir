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

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import de.minestar.conair.network.PacketBuffer;
import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.packets.NetworkPacket;
import de.minestar.conair.network.server.packets.RAWPacket;

public final class ServerPacketHandler {

    protected final PacketBuffer packetBuffer;

    public static final byte PACKET_SEPERATOR = 3;

    public ServerPacketHandler(ByteBuffer buffer) {
        this.packetBuffer = new PacketBuffer(buffer);
    }

    // //////////////////////////////////////////////////////////
    //
    // Both packets
    //
    // //////////////////////////////////////////////////////////

    public boolean isPacketComplete(ByteBuffer buffer) {
        buffer.flip();
        int len = 0;
        if (buffer.hasRemaining()) {
            len = buffer.getInt();
        } else {
            return false;
        }
        if (buffer.remaining() <= len - 4) {
            return false;
        }
        return (buffer.get(len) == PACKET_SEPERATOR);
    }

    public boolean packPacket(NetworkPacket packet) {
        packetBuffer.clear();
        boolean result = packet.pack(packetBuffer);
        packetBuffer.getBuffer().flip();
        return result;
    }

    // //////////////////////////////////////////////////////////
    //
    // RAWPacket
    //
    // //////////////////////////////////////////////////////////

//    public RAWPacket extractRAWPacket(ByteBuffer src) {
//
//        src.rewind();
//        int len = src.getInt();
//        int limit = src.limit();
//        src.limit(len);
//        
//        packetBuffer.clear();
//        packetBuffer.writeByteBuffer(src);
//        packetBuffer.getBuffer().flip();
//        
//        src.limit(limit);
//        src.compact();
//
//        // create packet
//        RAWPacket packet = createRAWPacket(len);
//
//        // reset
//        src.rewind();
//        packetBuffer.clear();
//
//        // return
//        return packet;
//    }
//
//    private RAWPacket createRAWPacket(int datalength) {
//        try {
//            // reduce the datalength, because we read two integers first.
//            // One integer is 4 bytes long
//            datalength -= 8;
//
//            // get packettype
//            int packetID = packetBuffer.readInt();
//
//            // read data...
//            byte[] data = new byte[datalength];
//            packetBuffer.readBytes(data);
//
//            // ... and create a new PacketBuffer
//            PacketBuffer newBuffer = new PacketBuffer(data.length);
//            newBuffer.writeBytes(data);
//            newBuffer.getBuffer().rewind();
//
//            // finally create the packet and return it
//            return new RAWPacket(packetID, newBuffer.getBuffer());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    // //////////////////////////////////////////////////////////
    //
    // NetworkPacket
    //
    // //////////////////////////////////////////////////////////

    public final NetworkPacket extractNetworkPacket(ByteBuffer src) {
        src.rewind();
        int len = src.getInt();
        int limit = src.limit();
        src.limit(len);
        packetBuffer.clear();
        packetBuffer.writeByteBuffer(src);
        packetBuffer.getBuffer().flip();
        src.limit(limit);
        src.compact();

        // create packet
        NetworkPacket packet = createNetworkPacket(len);

        // reset
        src.rewind();
        packetBuffer.clear();

        // return
        return packet;
    }

    private final NetworkPacket createNetworkPacket(int datalength) {
        try {
            // reduce the datalength, because we read two integers first.
            // One integer is 4 bytes long
            datalength -= 8;

            // get packettype
            int packetID = packetBuffer.readInt();
            Class<? extends NetworkPacket> packetClazz = PacketType.getClassByID(packetID);

            // packet not found...
            if (packetClazz == null) {
                // CREATE THE RAWPACKET

                // read data...
                byte[] data = new byte[datalength];
                packetBuffer.readBytes(data);

                // ... and create a new PacketBuffer
                PacketBuffer newBuffer = new PacketBuffer(data.length);
                newBuffer.writeBytes(data);
                newBuffer.getBuffer().rewind();

                System.out.println("String: " + new String(data));
                // finally create the packet and return it
                return new RAWPacket(packetID, newBuffer.getBuffer());
            }

            // get the constructor
            Constructor<? extends NetworkPacket> packetConstructor = packetClazz.getDeclaredConstructor(int.class, PacketBuffer.class);
            if (packetConstructor == null) {
                return null;
            }

            // read data...
            byte[] data = new byte[datalength];
            packetBuffer.readBytes(data);

            // ... and create a new PacketBuffer
            PacketBuffer newBuffer = new PacketBuffer(data.length);
            newBuffer.writeBytes(data);
            newBuffer.getBuffer().rewind();

            // finally create the packet and return it
            return packetConstructor.newInstance(packetID, newBuffer);
        } catch (Exception e) {
            return null;
        }
    }

    // //////////////////////////////////////////////////////////
    //
    // NetworkPacket
    //
    // //////////////////////////////////////////////////////////

    public NetworkPacket extractPacket(ByteBuffer src) {
        NetworkPacket packet = this.extractNetworkPacket(src);
//        if (packet == null) {
//            packet = this.extractRAWPacket(src);
//        }
        return packet;
    }
}