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

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class ClientSettings {

    public static String serverPrefix = "[H]", serverName = "Main";
    public static ChatColor prefixColor = ChatColor.AQUA;

    public static int port = 9000;
    public static String host = "localhost";

    public static boolean informJoin = true, informQuit = true, informChat = true;

    public static void loadConfig(Core core) {
        File configFile = new File(core.getDataFolder(), "config.yml");
        // Create default config when no config was found
        if (!configFile.exists()) {
            try {
                YamlConfiguration.loadConfiguration(core.getResource("config.yml")).save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);

            ClientSettings.port = config.getInt("port", port);
            ClientSettings.serverPrefix = config.getString("prefix", serverPrefix);
            ClientSettings.serverName = config.getString("servername", serverName);
            ClientSettings.prefixColor = ChatColor.values()[config.getInt("color", ChatColor.AQUA.ordinal())];
            if (ClientSettings.prefixColor == null) {
                ClientSettings.prefixColor = ChatColor.AQUA;
            }
            ClientSettings.host = config.getString("host", host);

            // inform settings
            ClientSettings.informChat = config.getBoolean("inform.chat", ClientSettings.informChat);
            ClientSettings.informJoin = config.getBoolean("inform.join", ClientSettings.informJoin);
            ClientSettings.informQuit = config.getBoolean("inform.quit", ClientSettings.informQuit);

            // save config
            saveConfig(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveConfig(File configFile) {
        try {
            // Create default config when no config was found
            if (configFile.exists()) {
                configFile.delete();
            }
            YamlConfiguration config = new YamlConfiguration();

            config.set("port", port);
            config.set("prefix", serverPrefix);
            config.set("servername", serverName);
            config.set("color", prefixColor.ordinal());
            config.set("host", host);

            // inform settings
            config.set("inform.chat", ClientSettings.informChat);
            config.set("inform.join", ClientSettings.informJoin);
            config.set("inform.quit", ClientSettings.informQuit);

            config.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
