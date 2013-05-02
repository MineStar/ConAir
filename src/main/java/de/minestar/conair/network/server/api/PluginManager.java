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
import java.util.ArrayList;
import java.util.HashMap;

public class PluginManager {

    public static final String PLUGIN_FOLDER = "plugins" + System.getProperty("file.separator");

    private PluginLoader pluginLoader;

    private HashMap<String, ServerPlugin> pluginMap;

    public PluginManager() {
        this.pluginLoader = new PluginLoader();
        this.pluginMap = new HashMap<String, ServerPlugin>();
    }

    public void loadPlugins() {
        // create PluginFolder
        File pluginFolder = new File(PluginManager.PLUGIN_FOLDER);
        pluginFolder.mkdir();

        // disable old plugins first
        this.disablePlugins();

        // iterate over files...
        for (File jarFile : pluginFolder.listFiles()) {
            if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
                // retrieve ServerPlugin
                ServerPlugin plugin = pluginLoader.loadPlugin(jarFile);
                if (plugin != null) {
                    // pluginnames MUST be unique!
                    if (this.pluginMap.containsKey(plugin.getPluginName())) {
                        System.out.println("A plugin named '" + plugin.getPluginName() + "' is already registered! Ignoring '" + jarFile.getName() + "'...");
                        continue;
                    } else {
                        this.pluginMap.put(plugin.getPluginName(), plugin);
                    }
                }
            }
        }

        // enable plugins
        this.enablePlugins();
    }
    /**
     * Enables all ServerPlugins
     */
    private void enablePlugins() {
        ArrayList<ServerPlugin> failedPlugins = new ArrayList<ServerPlugin>();
        for (ServerPlugin plugin : this.pluginMap.values()) {
            try {
                // enable the plugin
                System.out.println("Enabling plugin: '" + plugin.getPluginName() + "'");
                plugin.onEnable();
                System.out.println("Plugin enabled: " + plugin.getPluginName() + "'");
            } catch (Exception e) {
                // queue plugin and print stacktrace
                failedPlugins.add(plugin);
                e.printStackTrace();
            }
        }

        // remove all failed pluginsFS
        for (ServerPlugin plugin : failedPlugins) {
            this.pluginMap.remove(plugin.getPluginName());
        }
    }

    /**
     * Disables all ServerPlugins
     */
    private void disablePlugins() {
        for (ServerPlugin plugin : this.pluginMap.values()) {
            try {
                // disable the plugin
                System.out.println("Disabling plugin: '" + plugin.getPluginName() + "'");
                plugin.onDisable();
                System.out.println("Plugin disabled: '" + plugin.getPluginName() + "'");
            } catch (Exception e) {
                // print stacktrace
                e.printStackTrace();
            }
        }
        this.pluginMap.clear();
    }
}
