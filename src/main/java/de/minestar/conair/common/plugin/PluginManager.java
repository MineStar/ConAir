/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Minestar.de
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.minestar.conair.common.plugin;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import de.minestar.conair.api.plugin.ConAirPlugin;
import de.minestar.conair.common.PacketSender;


public final class PluginManager {

    private final PluginLoader _pluginLoader;
    private final PacketSender _packetSender;
    private final String _pluginFolder;

    private final HashMap<String, ConAirPlugin> _pluginMap;


    public PluginManager(PacketSender packetSender, String pluginFolder) {
        _pluginLoader = new PluginLoader();
        _pluginMap = new HashMap<String, ConAirPlugin>();
        _packetSender = packetSender;
        if (pluginFolder.endsWith("/")) {
            _pluginFolder = pluginFolder;
        } else {
            _pluginFolder = pluginFolder + '/';
        }
        loadPlugins();
    }


    private void loadPlugins() {
        // create PluginFolder
        File pluginFolder = new File(_pluginFolder);
        pluginFolder.mkdir();

        // disable old plugins first
        disablePlugins();

        // iterate over files...
        for (File jarFile : pluginFolder.listFiles()) {
            if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
                // retrieve ServerPlugin
                final Collection<ConAirPlugin> plugins = _pluginLoader.loadPlugins(this, _packetSender, jarFile);
                for (final ConAirPlugin plugin : plugins) {
                    // pluginnames MUST be unique!
                    if (_pluginMap.containsKey(plugin.getPluginName())) {
                        System.out.println("A plugin named '" + plugin.getPluginName() + "' is already registered! Ignoring '" + plugin.getPluginName() + "' from '" + jarFile.getName() + "'...");
                        continue;
                    } else {
                        _pluginMap.put(plugin.getPluginName(), plugin);
                        plugin.onLoad();
                    }
                }
            }
        }

        // enable plugins
        System.out.println("Plugins found: " + _pluginMap.size());
        enablePlugins();
    }


    public void onConnect() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                plugin.onConnect();
            } catch (Exception e) {
                // print stacktrace
                e.printStackTrace();
            }
        }
    }


    public void onDisconnect() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                plugin.onDisconnect();
            } catch (Exception e) {
                // print stacktrace
                e.printStackTrace();
            }
        }
    }


    /**
     * Enables all ConAirPlugins
     */
    private void enablePlugins() {
        ArrayList<ConAirPlugin> failedPlugins = new ArrayList<ConAirPlugin>();
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                // enable the plugin
                plugin.onPreEnable();
                System.out.println("Enabling plugin: '" + plugin.getPluginName() + "'");
                plugin.onEnable();
                System.out.println("Plugin enabled: '" + plugin.getPluginName() + "'");
                plugin.onPostEnable();
            } catch (Exception e) {
                // queue plugin and print stacktrace
                failedPlugins.add(plugin);
                e.printStackTrace();
            }
        }

        // remove all failed pluginsFS
        for (ConAirPlugin plugin : failedPlugins) {
            _pluginMap.remove(plugin.getPluginName());
        }
    }


    /**
     * Disables all ServerPlugins
     */
    public void disablePlugins() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                // disable the plugin
                plugin.onPreDisable();
                System.out.println("Disabling plugin: '" + plugin.getPluginName() + "'");
                plugin.onDisable();
                System.out.println("Plugin disabled: '" + plugin.getPluginName() + "'");
                plugin.onPostDisable();
            } catch (Exception e) {
                // print stacktrace
                e.printStackTrace();
            }
        }
        _pluginMap.clear();
    }


    public Optional<ConAirPlugin> getPlugin(final String pluginName) {
        if (_pluginMap.containsKey(pluginName)) {
            return Optional.of(_pluginMap.get(pluginName));
        }
        return Optional.<ConAirPlugin> empty();
    }


    public PacketSender getPacketSender() {
        return _packetSender;
    }


    public String getPluginFolder() {
        return _pluginFolder;
    }
}
