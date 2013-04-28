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

public class Settings {

    public static String serverPrefix = "", serverName = "";
    public static ChatColor prefixColor = ChatColor.AQUA;

    public static int port;
    public static String host;

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
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        Settings.port = config.getInt("port", 9000);
        Settings.serverPrefix = config.getString("prefix", "[M]");
        Settings.serverName = config.getString("servername", "Main");
        Settings.prefixColor = ChatColor.values()[config.getInt("color", ChatColor.AQUA.ordinal())];
        if (Settings.prefixColor == null) {
            Settings.prefixColor = ChatColor.AQUA;
        }
        Settings.host = config.getString("host", "localhost");
    }
}
