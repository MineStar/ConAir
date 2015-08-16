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

import de.minestar.conair.api.event.Listener;
import de.minestar.conair.common.PacketSender;
import de.minestar.conair.common.plugin.PluginManager;


public abstract class ConAirPlugin {

    private String _pluginName;
    private File _dataFolder;
    private PacketSender _packetSender;
    private PluginManager _pluginManager;


    @SuppressWarnings("unused")
    // called via reflection
    private final void initialize(PacketSender packetSender, String pluginName, PluginManager pluginManager) {
        _packetSender = packetSender;
        _pluginName = pluginName;
        _pluginManager = pluginManager;

        // create PluginFolder
        _dataFolder = new File(pluginManager.getPluginFolder() + pluginName + "/");
        _dataFolder.mkdir();
    }


    /**
     * Called on client side ONLY.
     */
    public void onConnected() {
    }


    /**
     * Called on client side ONLY.
     */
    public void onDisconnected() {
    }


    public void onLoad() {
    }


    public void onPreEnable() {
    }


    public void onEnable() {
    }


    public void onPostEnable() {
    }


    public void onPreDisable() {
    }


    public void onDisable() {
    }


    public void onPostDisable() {
    }


    public final String getPluginName() {
        return _pluginName;
    }


    public final <L extends Listener> void registerPacketListener(L listener) {
        _packetSender.registerPacketListener(listener);
    }


    public final File getDataFolder() {
        return _dataFolder;
    }


    public final PluginManager getPluginManager() {
        return _pluginManager;
    }


    public final PacketSender getPacketSender() {
        return _packetSender;
    }
}
