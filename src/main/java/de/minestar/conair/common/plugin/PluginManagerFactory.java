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

import de.minestar.conair.common.PacketSender;


public final class PluginManagerFactory {

    public static PluginManagerFactory get(final String pluginFolder) {
        return new PluginManagerFactory(pluginFolder);
    }

    private final PluginManager _pluginManager;


    private PluginManagerFactory(final String pluginFolder) {
        _pluginManager = new PluginManager(pluginFolder);
    }


    public Class<?> classForName(final String name) throws ClassNotFoundException {
        return _pluginManager.classForName(name);
    }


    public void loadPlugins(final PacketSender packetSender) {
        _pluginManager.loadPlugins(packetSender);
    }


    public void disablePlugins() {
        _pluginManager.disablePlugins();
    }


    public void onConnect() {
        _pluginManager.onConnected();
    }


    public void onDisconnect() {
        _pluginManager.onDisconnected();
    }
}
