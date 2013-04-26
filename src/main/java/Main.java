import java.util.ArrayList;

import de.minestar.conair.network.NetworkPacket;
import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.client.ChatClient;
import de.minestar.conair.network.packets.HelloWorldPacket;
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

            ChatServer server = new ChatServer(9002, new ArrayList<String>());

            Thread t = new Thread(server);
            t.start();

            ChatClient client = new ChatClient(new MainPacketHandler(), "localhost", 9002);
            Thread cThread = new Thread(client);

            PacketType.registerPacket(HelloWorldPacket.class);

            cThread.start();

            NetworkPacket packet = new HelloWorldPacket("Hallo Welt!");
            for (int i = 0; i < 300; i++) {
                client.sendPacket(packet);
            }

            Thread.sleep(2000);
            client.stop();
            cThread.stop();

            server.stop();
            t.stop();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
