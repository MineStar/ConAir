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

package de.minestar.conair.core;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import de.minestar.conair.listener.ChatListener;
import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.client.ChatClient;
import de.minestar.conair.network.client.packets.ChatPacket;

public class Core extends JavaPlugin {

    public static final String NAME = "ConAir";

    private ChatClient chatClient;
    private BukkitTask clientTask = null;

    private ChatListener chatListener;

    @Override
    public void onEnable() {
        ClientSettings.loadConfig(this);

        this.registerPackets();

        if (createChatClient()) {
            System.out.println("Connected to " + ClientSettings.host + ":" + ClientSettings.port);
        } else {
            System.out.println("NO CHATCLIENT CREATED!");
        }

        enableListener(this.getServer().getPluginManager());
    }

    private void registerPackets() {
        PacketType.registerPacket(ChatPacket.class);
    }

    private void enableListener(PluginManager pm) {
        this.chatListener = new ChatListener(this.chatClient);
        pm.registerEvents(this.chatListener, this);
    }

    private boolean createChatClient() {
        try {
            this.chatClient = new ChatClient(ClientSettings.serverName, new BukkitPacketHandler(), ClientSettings.host, ClientSettings.port);
            clientTask = this.getServer().getScheduler().runTaskAsynchronously(this, this.chatClient);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDisable() {
        if (this.chatClient != null) {
            this.chatClient.stop();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
            return true;
        }

        if (!label.startsWith("/"))
            label = "/" + label;

        label = label.toLowerCase();

        if (label.equalsIgnoreCase("/reconnect")) {
            if (this.chatClient != null) {
                this.clientTask.cancel();
                this.chatClient.stop();
                this.chatClient = null;
            }

            ClientSettings.loadConfig(this);

            if (createChatClient()) {
                sender.sendMessage(ChatColor.GREEN + "Connected to " + ClientSettings.host + ":" + ClientSettings.port);
            } else {
                sender.sendMessage(ChatColor.RED + "Could not connect!");
            }
            this.chatListener.setClient(this.chatClient);
        }
        return true;
    }
}
