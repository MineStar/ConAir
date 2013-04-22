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

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class PacketHandlerTest {

    @Test
    public void test() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        buffer.putInt("Hello World".length());
        buffer.put("Hello World".getBytes());
        buffer.put(PacketHandler.PACKET_SEPERATOR);

        buffer.putInt("Bye World".length());
        buffer.put("Bye World".getBytes());
        buffer.put(PacketHandler.PACKET_SEPERATOR);

        buffer.putInt("Spast GeMo".length());
        buffer.put("Spast GeMo".getBytes());
        buffer.put(PacketHandler.PACKET_SEPERATOR);

        PacketHandler ph = new PacketHandler();
        byte[] b = null;
        String s = null;
        if (ph.isPacketComplete(buffer)) {
            ph.extractPacket(buffer);
            b = ph.read();
            s = new String(b, 4, b.length - 4);
            assertEquals("Hello World", s);
        }
        if (ph.isPacketComplete(buffer)) {
            ph.extractPacket(buffer);
            b = ph.read();
            s = new String(b, 4, b.length - 4);
            assertEquals("Bye World", s);
        }
        if (ph.isPacketComplete(buffer)) {
            ph.extractPacket(buffer);
            b = ph.read();
            s = new String(b, 4, b.length - 4);
            assertEquals("Spast GeMo", s);
        }
    }

    @Test
    public void packetBufferTest() {
        PacketBuffer buffer = new PacketBuffer();
        buffer.putString("Hallo World");
        buffer.getBuffer().flip();
        assertEquals("Hallo World", buffer.getString());
    }

}
