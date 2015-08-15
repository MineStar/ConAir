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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.junit.Assert;
import org.junit.Test;

import de.minestar.conair.api.ConAir;
import de.minestar.conair.api.event.Listener;
import de.minestar.conair.api.event.RegisterEvent;
import de.minestar.conair.client.ConAirClient;
import de.minestar.conair.common.ConAirMember;
import de.minestar.conair.common.PacketSender;
import de.minestar.conair.server.ConAirServer;

/**
 * Demonstration of the ConAir API starting a server and three clients are connecting to the server. After the handshake, clients are sending chat packets.
 */
public class ConAirTest {

    private static final int PORT = 8977;

    public static long crc(byte[] bytes) {
        // create CRC32
        Checksum checksum = new CRC32();

        // update the current checksum with the specified array of bytes
        checksum.update(bytes, 0, bytes.length);

        // get the current checksum value
        return checksum.getValue();
    }

    private static long CRC_CHECK;

    @Test
    public void testConAir() {
        try {

            // Create the server
            ConAirServer server = new ConAirServer(PORT);
            server.registerPacketListener(new TestListener("S"));
            Assert.assertTrue(server.isRunning());

            // Create first client and connect to server
            ConAirClient client1 = new ConAirClient("Client1", "localhost", PORT);
            client1.registerPacketListener(new TestListener("C1"));
            Assert.assertTrue(client1.isConnected());

            // Create second client and connect to server
            ConAirClient client2 = new ConAirClient("Client2\"", "127.0.0.1", PORT);
            client2.registerPacketListener(new TestListener("C2"));
            Assert.assertTrue(client2.isConnected());

            // Create third client and connect to server
            ConAirClient client3 = new ConAirClient("Client3", "::1", PORT);
            client3.registerPacketListener(new TestListener("C3"));
            Assert.assertTrue(client3.isConnected());

            // Create third client and connect to server, This client hasn't a
            // packet registered!
            ConAirClient client4 = new ConAirClient("Client4", "::1", PORT);
            Assert.assertTrue(client4.isConnected());

            // Clients are sending packets to everyone in the network
            client1.sendPacket(new ChatPacket("Hi Client2!"), client1.getMember("Client2"));
            Thread.sleep(50);
            client2.sendPacket(new ChatPacket("Hello!"));
            Thread.sleep(50);
            client3.sendPacket(new ChatPacket("Moin!"));
            // Client 1 talks to client 3
            client1.sendPacket(new ChatPacket("Pssst...client3....can you hear me?"), client1.getMember("Client3"));

            // send a resourcepacket
            ResourcePacket resourcePacket = new ResourcePacket(new File("send.jpg"));
            CRC_CHECK = crc(resourcePacket.getData());
            client2.sendPacket(resourcePacket, client2.getMember("Client1"));

            // send a packet for the server only
            client3.sendPacket(new ChatPacket("Just for the server."), ConAir.SERVER);

            // send a packet from the server to client 3
            server.sendPacket(new ChatPacket("Thank you!"), server.getMember("Client3"));

            Thread.sleep(1000); // Just for test

            // Clients are disconnecting, server is shutting down
            System.out.println("Shutting down...");
            client1.disconnect();
            client2.disconnect();
            client3.disconnect();
            client4.disconnect();
            Assert.assertFalse(client1.isConnected());
            Assert.assertFalse(client2.isConnected());
            Assert.assertFalse(client3.isConnected());
            Assert.assertFalse(client4.isConnected());

            server.stop();
            Assert.assertFalse(server.isRunning());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class TestListener implements Listener {

        private String name;

        public TestListener(String n) {
            name = n;
        }

        @RegisterEvent
        public void onChatPacket(final PacketSender receiver, final ConAirMember source, final ChatPacket packet) throws Exception {
            if (source.getName().equals("Client1") && packet.getMessage().contains("Pssst...client3....can you hear me?")) {
                System.out.println("[WHISPER] [ to: " + name + " ] [ from: " + source + " ] " + packet.getMessage());
                receiver.sendPacket(new ChatPacket("Roger " + source + ", I hear you loud and clear."), source);
            } else {
                System.out.println("[ to: " + name + " ] [ from: " + source + " ] " + packet.getMessage());
            }
        }

        @RegisterEvent
        public void onResourcePacket(final PacketSender receiver, final ConAirMember source, final ResourcePacket packet) {
            System.out.println("[ to: " + name + " ] [ from: " + source + " ] ResourcePacket: " + packet.toString());
            Assert.assertEquals("CRC IS DIFFERENT!!!!", crc(packet.getData()), CRC_CHECK);
            try {
                String filename = "rec.jpg";
                new File(filename).createNewFile();
                Files.write(Paths.get(filename), packet.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
