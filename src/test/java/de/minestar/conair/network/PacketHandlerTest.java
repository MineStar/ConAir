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

import static org.junit.Assert.fail;

import java.nio.ByteBuffer;

import org.junit.Test;

import de.minestar.conair.network.packets.HelloWorldPacket;

public class PacketHandlerTest {

    @Test
    public void test() {
        PacketHandler packetHandler = new PacketHandler();

        NetworkPacket packet = new HelloWorldPacket();

        packetHandler.packPacket(packet);
        packetHandler.packPacket(packet);

        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
        buffer.put(packetHandler.packetbuffer.getBuffer());
        if (packetHandler.isPacketComplete(buffer)) {
            HelloWorldPacket packet2 = (HelloWorldPacket) packetHandler.extractPacket(buffer);

            System.out.println(packet2.getHelloWorld());



        } else
            fail();

    }

}
