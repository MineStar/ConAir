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

package de.minestar.conair.network.server.api;

import java.io.File;

import de.minestar.conair.network.server.DedicatedTCPServer;

public abstract class ServerPlugin {

    private String pluginName;
    private File dataFolder;
    private PluginDescription pluginDescription;
    private DedicatedTCPServer server;

    public ServerPlugin() {
    }

    public final void initialize(DedicatedTCPServer server, String pluginName, PluginDescription pluginDescription) {
        // set server
        this.server = server;

        // set PluginName
        this.pluginName = pluginName;

        // create PluginFolder
        File pluginFolder = new File(PluginManager.PLUGIN_FOLDER);
        this.dataFolder = new File(pluginFolder + pluginName + System.getProperty("file.separator"));
        this.dataFolder.mkdirs();

        // set PluginDescription
        this.pluginDescription = pluginDescription;
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public String getPluginName() {
        return pluginName;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public PluginDescription getPluginDescription() {
        return pluginDescription;
    }

    public DedicatedTCPServer getServer() {
        return server;
    }
}
