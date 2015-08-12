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

package de.minestar.conair.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.bukkit.gemo.FalseBook.Chat.ChatManager;

import de.minestar.conair.network.client.ClientPacketHandler;
import de.minestar.conair.network.client.packets.ChatPacket;

public class BukkitPacketHandler extends ClientPacketHandler {

    private boolean useFBChat = false;

    public BukkitPacketHandler() {
        this.searchFBChat();
    }

    private void searchFBChat() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("FalseBookChat");
        if (plugin != null && plugin.isEnabled()) {
            useFBChat = true;
        }
    }

    public void handleChatPacket(ChatPacket packet) {
        if (!ClientSettings.informChat) {
            return;
        }

        if (!useFBChat) {
            Bukkit.getServer().broadcastMessage(packet.getMessage());
        } else {
            ChatManager.broadcast(packet.getMessage());
        }
    }
}
