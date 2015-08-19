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

package de.minestar.conair.api.plugin;

import java.io.File;

import de.minestar.conair.api.Packet;
import de.minestar.conair.api.event.Listener;
import de.minestar.conair.client.ConAirClient;
import de.minestar.conair.common.PacketSender;
import de.minestar.conair.common.plugin.PluginManager;
import de.minestar.conair.server.ConAirServer;


/**
 * This class represents a plugin for ConAir ({@link ConAirServer ServerSide} & {@link ConAirClient ClientSide} ). <br>
 * <br>
 * The lifecycle of a plugin is the following:
 * <ul>
 * <li>{@link #onLoad}</li>
 * <li>{@link #onPreEnable}</li>
 * <li>{@link #onEnable}</li>
 * <li>{@link #onPostEnable}</li>
 * <li>{@link #onConnected} (Client for the ONLY)</li>
 * <li>{@link #onDisconnected} (Client for the ONLY)</li>
 * <li>{@link #onPreDisable}</li>
 * <li>{@link #onDisable}</li>
 * <li>{@link #onPostDisable}</li>
 * </ul>
 */
public abstract class ConAirPlugin {

    private String _pluginName;
    private File _dataFolder;
    private PacketSender _packetSender;
    private PluginManager _pluginManager;


    /**
     * This method initializes the plugin. It is automatically called via reflections in the {@link PluginManager#loadPlugins PluginManager} when this plugin is about to get loaded.
     * 
     * @param packetSender
     *            the {@link PacketSender}
     * @param pluginName
     *            the {@link #getPluginName() name} of the plugin
     * @param pluginManager
     *            the {@link PluginManager}
     */
    @SuppressWarnings("unused")
    private final void _initialize(PacketSender packetSender, String pluginName, PluginManager pluginManager) {
        _packetSender = packetSender;
        _pluginName = pluginName;
        _pluginManager = pluginManager;

        // create PluginFolder
        _dataFolder = new File(pluginManager.getPluginFolder() + pluginName + "/");
        _dataFolder.mkdir();
    }


    /**
     * Called when the plugin is loaded.
     */
    public void onLoad() {
    }


    /**
     * Called before the plugin is enabled.
     */
    public void onPreEnable() {
    }


    /**
     * Called when the plugin is enabled.
     */
    public void onEnable() {
    }


    /**
     * Called after the plugin is enabled.
     */
    public void onPostEnable() {
    }


    /**
     * Called after the client is successfully connected to a {@link ConAirServer}. (Client ONLY)
     */
    public void onConnected() {
    }


    /**
     * Called after the client successfully disconnected from a {@link ConAirServer}. (Client ONLY)
     */
    public void onDisconnected() {
    }


    /**
     * Called before the plugin is disabled.
     */
    public void onPreDisable() {
    }


    /**
     * Called when the plugin is disabled.
     */
    public void onDisable() {
    }


    /**
     * Called after the plugin is disabled.
     */
    public void onPostDisable() {
    }


    /**
     * Get the name of the plugin. The Name is always the {@link Class#getSimpleName() classname}.
     * 
     * @return the name of the plugin
     */
    public final String getPluginName() {
        return _pluginName;
    }


    /**
     * Register a {@link Listener} at the {@link PacketSender}. The given {@link Listener} can be used to handle incoming {@link Packet packets}.
     * 
     * @param listener
     *            the {@link Listener} to register
     */
    public final <L extends Listener> void registerPacketListener(L listener) {
        _packetSender.registerPacketListener(listener);
    }


    /**
     * Unregister a {@link Listener listener} for {@link Packet packets}.
     * 
     * @param listenerClass
     *            the class of the {@link Listener listener} to unregister
     */
    public final <L extends Listener> void unregisterPacketListener(Class<L> listener) {
        _packetSender.unregisterPacketListener(listener);
    }


    /**
     * Get the datafolder of the plugin. All related files should be stored here.
     * 
     * @return the datafolder of the plugin
     */
    public final File getDataFolder() {
        return _dataFolder;
    }


    /**
     * Get the {@link PluginManager}.
     * 
     * @return the {@link PluginManager}
     */
    public final PluginManager getPluginManager() {
        return _pluginManager;
    }


    /**
     * Get the {@link PacketSender}. The {@link PacketSender} can be a {@link ConAirClient} or a {@link ConAirServer}.
     * 
     * @return the {@link PacketSender}
     */
    public final PacketSender getPacketSender() {
        return _packetSender;
    }
}
