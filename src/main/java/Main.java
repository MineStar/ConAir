import java.util.ArrayList;

import de.minestar.conair.core.TestPacketHandler;
import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.client.ChatClient;
import de.minestar.conair.network.client.packets.ChatPacket;
import de.minestar.conair.network.server.ChatServer;

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
            ChatClient client = new ChatClient("TestClient", new TestPacketHandler(), "localhost", 9002);
            Thread cThread = new Thread(client);
            cThread.start();

            ChatClient client2 = new ChatClient("TestClient2", new TestPacketHandler(), "localhost", 9002);
            Thread cThread2 = new Thread(client2);
            cThread2.start();
           

            Thread.sleep(500);
            
            for (int i = 1; i <= 1; i++) {
                client.sendPacket(new ChatPacket("Hallo Welt! " + i));
            }

            Thread.sleep(1000);

            client.stop();
            cThread.stop();
            

            client2.stop();
            cThread2.stop();

           } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
