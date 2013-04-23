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

import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.minestar.conair.listener.ChatListener;
import de.minestar.conair.network.ChatClient;
import de.minestar.conair.network.ChatServer;

public class Core extends JavaPlugin {

    public static final String NAME = "ConAir";

    private boolean isServer;

    private int port;
    private String host;

    private ChatServer chatServer;
    private ChatClient chatClient;

    @Override
    public void onEnable() {
        enableListener(this.getServer().getPluginManager());
        readConfig();

        if (isServer) {
            createChatServer(port);
        }

        createChatClient(port, host);
    }

    private void enableListener(PluginManager pm) {
        pm.registerEvents(new ChatListener(), this);
    }

    private void readConfig() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(this.getResource("config.yml"));

        isServer = config.getBoolean("isServer", false);

        port = config.getInt("port", 13371);
        host = config.getString("host", "localhost");
    }

    private boolean createChatServer(int port) {
        try {
            this.chatServer = new ChatServer(port);
            this.getServer().getScheduler().runTaskAsynchronously(this, this.chatServer);
            return true;
        } catch (Exception e) {
            Logger.getLogger(NAME).throwing("de.minestar.conair.core.Core", "createChatServer", e);
            return false;
        }
    }

    private boolean createChatClient(int port, String host) {
        try {
            this.chatClient = new ChatClient(host, port);
            this.getServer().getScheduler().runTaskAsynchronously(this, this.chatClient);
            return true;
        } catch (Exception e) {
            Logger.getLogger(NAME).throwing("de.minestar.conair.core.Core", "createChatClient", e);
            return false;
        }
    }

    @Override
    public void onDisable() {
        // TODO Auto-generated method stub
        super.onDisable();
    }

}
