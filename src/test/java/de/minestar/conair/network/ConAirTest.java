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

import de.minestar.conair.api.ConAirClient;
import de.minestar.conair.api.impl.PluginConAirClient;
import de.minestar.conair.api.packets.ChatPacket;

public class ConAirTest {

    private static final int PORT = 8977;

    public static void main(String[] args) {
        try {

            ConAirServer server = new ConAirServer();
            server.start(PORT);
            Thread.sleep(500); // Just for test

            ConAirClient client1 = new PluginConAirClient();
            client1.registerPacketListener(ChatPacket.class, (ChatPacket packet, String source) -> {
                onPacketReceive(packet, source);
            });
            client1.connect("Client1", "localhost", PORT); // domain

            Thread.sleep(500); // Just for test
            ConAirClient client2 = new PluginConAirClient();
            client2.registerPacketListener(ChatPacket.class, (p, source) -> System.out.println("C2 (from " + source + ") " + p.getMessage()));
            client2.connect("Client2", "127.0.0.1", PORT); // ipv4

            Thread.sleep(500); // Just for test
            ConAirClient client3 = new PluginConAirClient();
            client3.registerPacketListener(ChatPacket.class, (p, source) -> System.out.println("C3 (from " + source + ") " + p.getMessage()));
            client3.connect("Client3", "::1", PORT); // ipv6

            Thread.sleep(500); // Just for test
            client1.sendPacket(new ChatPacket("Hi!"));
            client2.sendPacket(new ChatPacket("Hello!"));
            client3.sendPacket(new ChatPacket("Moin!"));

            Thread.sleep(500);
            client1.sendPacket(new ChatPacket("Pssst...client3....can you hear me?"), "Client3");
            Thread.sleep(50);
            client3.sendPacket(new ChatPacket("Roger Client 1, I hear you loud and clear."), "Client1");

            Thread.sleep(50);
            client1.disconnect();
            client2.disconnect();
            client3.disconnect();
            server.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onPacketReceive(ChatPacket packet, String source) {
        System.out.println("C2 (from " + source + ") " + packet.getMessage());
    }
}
