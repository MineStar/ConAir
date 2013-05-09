import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.client.DedicatedTCPClient;
import de.minestar.conair.network.client.packets.ChatPacket;

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

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            PacketType.registerPacket(ChatPacket.class);
            DedicatedTCPClient client1 = new DedicatedTCPClient("Client-1", new TestPacketHandler(), "localhost", 9000);
            DedicatedTCPClient client2 = new DedicatedTCPClient("Client-2", new TestPacketHandler(), "localhost", 9000);
            Thread.sleep(500);

            for (int i = 1; i <= 5; i++) {
                client1.sendPacket(new ChatPacket("Hello World from '" + client1.getClientName() + "' -> " + i));
            }

            Thread.sleep(1000);

            client1.stop();
            client2.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
