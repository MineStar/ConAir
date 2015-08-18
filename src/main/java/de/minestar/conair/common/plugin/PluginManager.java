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


    PluginManager(PacketSender packetSender, String pluginFolder) {
        _pluginLoader = new PluginLoader();
        _pluginMap = new HashMap<String, ConAirPlugin>();
        _packetSender = packetSender;
        if (pluginFolder.endsWith("/")) {
            _pluginFolder = pluginFolder;
        } else {
            _pluginFolder = pluginFolder + '/';
        }
    }


    Class<?> getClassByName(final String name) {
        return _pluginLoader.getClassByName(name);
    }


    void loadPlugins() {
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
                    if (_pluginMap.containsKey(plugin.getClass().getName())) {
                        System.out.println("A plugin named '" + plugin.getPluginName() + "' is already registered! Ignoring '" + plugin.getPluginName() + "' from '" + jarFile.getName() + "'...");
                        continue;
                    } else {
                        _pluginMap.put(plugin.getClass().getName(), plugin);
                        plugin.onLoad();
                    }
                }
            }
        }

        // enable plugins
        System.out.println("Plugins found: " + _pluginMap.size());
        enablePlugins();
    }


    void onConnected() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                plugin.onConnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    void onDisconnected() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                plugin.onDisconnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void preEnablePlugins() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                plugin.onPreEnable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Enables all ConAirPlugins
     */
    private void enablePlugins() {
        preEnablePlugins();
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                // enable the plugin
                System.out.println("Enabling plugin: '" + plugin.getPluginName() + "'");
                plugin.onEnable();
                System.out.println("Plugin enabled: '" + plugin.getPluginName() + "'");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        postEnablePlugins();
    }


    private void postEnablePlugins() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                plugin.onPostEnable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void preDisablePlugins() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                plugin.onPreDisable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Disables all ServerPlugins
     */
    void disablePlugins() {
        preDisablePlugins();
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                // disable the plugin
                System.out.println("Disabling plugin: '" + plugin.getPluginName() + "'");
                plugin.onDisable();
                System.out.println("Plugin disabled: '" + plugin.getPluginName() + "'");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        postDisablePlugins();
        _pluginMap.clear();
    }


    private void postDisablePlugins() {
        for (ConAirPlugin plugin : _pluginMap.values()) {
            try {
                plugin.onPostDisable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Get a plugin by the given {@link Class ConAirPlugin-Class}.
     * 
     * @param pluginClass
     *            the class of the plugin to look for
     * @return the {@link Optional Optional[ConAirPlugin]}
     */
    public Optional<ConAirPlugin> getPlugin(final Class<? extends ConAirPlugin> pluginClass) {
        if (_pluginMap.containsKey(pluginClass.getName())) {
            return Optional.of(_pluginMap.get(pluginClass.getName()));
        }
        return Optional.<ConAirPlugin> empty();
    }


    /**
     * Get the plugin folder.
     * 
     * @return the plugin folder
     */
    public String getPluginFolder() {
        return _pluginFolder;
    }
}
