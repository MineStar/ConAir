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
import java.util.List;
import java.util.Map;

import de.minestar.conair.network.server.DedicatedTCPServer;
import de.minestar.conair.network.server.api.events.Cancellable;
import de.minestar.conair.network.server.api.events.Event;
import de.minestar.conair.network.server.api.exceptions.EventException;

public class PluginManager {

    private PluginLoader _pluginLoader;
    private DedicatedTCPServer _dedicatedTCPServer;
    private final String _pluginFolder;

    private HashMap<String, ServerPlugin> _pluginMap;
    private final Map<String, List<EventExecutor>> _registeredEvents = new HashMap<String, List<EventExecutor>>();

    public PluginManager(DedicatedTCPServer dedicatedTCPServer) {
        this(dedicatedTCPServer, "plugins" + System.getProperty("file.separator"));
    }

    public PluginManager(DedicatedTCPServer dedicatedTCPServer, String pluginFolder) {
        _pluginLoader = new PluginLoader();
        _pluginMap = new HashMap<String, ServerPlugin>();
        _dedicatedTCPServer = dedicatedTCPServer;
        _pluginFolder = pluginFolder;
    }

    public void loadPlugins() {
        // create PluginFolder
        File pluginFolder = new File(_pluginFolder);
        pluginFolder.mkdir();

        // disable old plugins first
        disablePlugins();

        // iterate over files...
        for (File jarFile : pluginFolder.listFiles()) {
            if (jarFile.isFile() && jarFile.getName().endsWith(".jar")) {
                // retrieve ServerPlugin
                ServerPlugin plugin = _pluginLoader.loadPlugin(this, _dedicatedTCPServer, jarFile);
                if (plugin != null) {
                    // pluginnames MUST be unique!
                    if (_pluginMap.containsKey(plugin.getPluginName())) {
                        System.out.println("A plugin named '" + plugin.getPluginName() + "' is already registered! Ignoring '" + jarFile.getName() + "'...");
                        continue;
                    } else {
                        _pluginMap.put(plugin.getPluginName(), plugin);
                    }
                }
            }
        }

        // enable plugins
        System.out.println("Plugins found: " + _pluginMap.size());
        enablePlugins();
    }

    /**
     * Enables all ServerPlugins
     */
    private void enablePlugins() {
        ArrayList<ServerPlugin> failedPlugins = new ArrayList<ServerPlugin>();
        for (ServerPlugin plugin : _pluginMap.values()) {
            try {
                // enable the plugin
                System.out.println("Enabling plugin: '" + plugin.getPluginName() + "'");
                plugin.onEnable();
                System.out.println("Plugin enabled: '" + plugin.getPluginName() + "'");
            } catch (Exception e) {
                // queue plugin and print stacktrace
                failedPlugins.add(plugin);
                e.printStackTrace();
            }
        }

        // remove all failed pluginsFS
        for (ServerPlugin plugin : failedPlugins) {
            _pluginMap.remove(plugin.getPluginName());
        }
    }

    /**
     * Disables all ServerPlugins
     */
    public void disablePlugins() {
        for (ServerPlugin plugin : _pluginMap.values()) {
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
        _pluginMap.clear();
    }

    /**
     * Register events
     * 
     * @param eventListener
     */
    public final void registerEvents(EventListener eventListener, ServerPlugin serverPlugin) {
        // catch the eventlist
        Map<Class<? extends Event>, List<EventExecutor>> executorMap = _pluginLoader.createEventExecutorList(eventListener, serverPlugin);

        // finally register the events
        for (Map.Entry<Class<? extends Event>, List<EventExecutor>> event : executorMap.entrySet()) {
            for (EventExecutor executor : event.getValue()) {
                registerSingleEvent(eventListener, serverPlugin, event.getKey(), executor);
            }
        }
    }

    /**
     * Register single events
     * 
     * @param eventListener
     * @param serverPlugin
     * @param clazz
     * @param executor
     */
    private void registerSingleEvent(EventListener eventListener, ServerPlugin serverPlugin, Class<? extends Event> clazz, EventExecutor executor) {
        // get the current list for the eventclass
        List<EventExecutor> executorList = _registeredEvents.get(clazz.getSimpleName());

        // create a new list, if there is none
        if (executorList == null) {
            executorList = new ArrayList<EventExecutor>();
            _registeredEvents.put(clazz.getSimpleName(), executorList);
        }

        // add the EventExecutor to the list
        executorList.add(executor);
    }

    public DedicatedTCPServer getDedicatedTCPServer() {
        return _dedicatedTCPServer;
    }

    public String getPluginFolder() {
        return _pluginFolder;
    }

    public void callEvent(Event event) {
        // get the current list for the eventclass
        List<EventExecutor> executorList = _registeredEvents.get(event.getClass().getSimpleName());

        // return, if there is no event
        if (executorList == null) {
            return;
        }

        if (event instanceof Cancellable) {
            Cancellable cancelEvent = (Cancellable) event;
            for (EventExecutor executor : executorList) {
                // ignore cancelled events
                if (cancelEvent.isCancelled() && executor.isIgnoreCancelled()) {
                    continue;
                }

                try {
                    executor.execute(event);
                } catch (EventException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } else {
            for (EventExecutor executor : executorList) {
                try {
                    executor.execute(event);
                } catch (EventException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }
}
