/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Minestar.de
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.minestar.conair.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import de.minestar.conair.api.SmallPacketHandler;
import de.minestar.conair.api.WrappedPacket;
import de.minestar.conair.api.packets.SmallPacket;

/*
 * Test for serialization and parsing of packets
 */
public class PacketTest {

    @Test
    public void singlePacketTest() throws IOException, ClassNotFoundException {
        // Create test packet
        ChatPacket sentPacket = new ChatPacket("Das Pferd frisst keinen Gurkensalat!");
        // Serialize packet while wrapping
        List<WrappedPacket> wrappedPackets = WrappedPacket.create(sentPacket, "", "");
        // Parse packet
        Optional<ChatPacket> possibleResult = wrappedPackets.get(0).getPacket();
        assertTrue(possibleResult.isPresent());
        ChatPacket receivedPacket = possibleResult.get();
        // Check if messages are equal
        assertEquals(sentPacket.getMessage(), receivedPacket.getMessage());
    }

    @Test
    public void multiPacketTest() throws IOException, ClassNotFoundException {
        // Create test packet
        ResourcePacket sentPacket = new ResourcePacket(new File("send.jpg"));
        // Serialize packet while wrapping
        List<WrappedPacket> wrappedPackets = WrappedPacket.create(sentPacket, "", "");
        SmallPacketHandler smallPacketHandler = new SmallPacketHandler();
        WrappedPacket result = null;
        for (WrappedPacket packet : wrappedPackets) {
            result = smallPacketHandler.handle(packet, (SmallPacket) packet.getPacket().get());
        }

        // Parse packet
        Optional<ResourcePacket> possibleResult = result.getPacket();
        assertTrue(possibleResult.isPresent());
        ResourcePacket receivedPacket = possibleResult.get();

        // Check if the contents are equal
        assertEquals(sentPacket.getData().length, receivedPacket.getData().length);
        for (int i = 0; i < sentPacket.getData().length; i++) {
            assertEquals(sentPacket.getData()[i], receivedPacket.getData()[i]);
        }
    }
}
