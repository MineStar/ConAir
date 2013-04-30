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

package de.minestar.conair.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.minestar.conair.core.ClientSettings;
import de.minestar.conair.network.client.DedicatedTCPClient;
import de.minestar.conair.network.client.packets.ChatPacket;

public class ChatListener implements Listener {

    private DedicatedTCPClient client;

    public ChatListener(DedicatedTCPClient client) {
        this.client = client;
    }

    public void setClient(DedicatedTCPClient client) {
        this.client = client;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChatEvent(AsyncPlayerChatEvent event) {
        if (client == null || !ClientSettings.informChat) {
            return;
        }
        client.sendPacket(new ChatPacket(ClientSettings.prefixColor + ClientSettings.serverPrefix + " " + event.getMessage()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (client == null || !ClientSettings.informJoin) {
            return;
        }
        client.sendPacket(new ChatPacket(ChatColor.YELLOW + event.getPlayer().getName() + " joined the server: " + ClientSettings.prefixColor + ClientSettings.serverName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (client == null || !ClientSettings.informQuit) {
            return;
        }
        client.sendPacket(new ChatPacket(ChatColor.YELLOW + event.getPlayer().getName() + " left the server: " + ClientSettings.prefixColor + ClientSettings.serverName));
    }
}
