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

import de.minestar.conair.network.client.ClientPacketHandler;
import de.minestar.conair.network.client.packets.ChatPacket;

public class TestPacketHandler extends ClientPacketHandler {

    public void handleChatPacket(ChatPacket packet) {
        System.out.println(packet.getMessage());
    }

//    public void handleResourcePacket(ResourcePacket packet) {
//        System.out.println("packet received...");
//        File file = new File(packet.getFileName());
//        if (file.exists()) {
//            file.delete();
//        }
//
//        try {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream(packet.getData().length);
//            bos.write(packet.getData());
//            bos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
