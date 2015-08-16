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
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.minestar.conair.api.plugin.ConAirPlugin;
import de.minestar.conair.common.PacketSender;


final class PluginLoader {

    private final Map<String, Class<?>> _classes = new HashMap<String, Class<?>>();
    private final Map<String, PluginClassLoader> _loaders = new HashMap<String, PluginClassLoader>();


    public Collection<ConAirPlugin> loadPlugins(PluginManager pluginManager, PacketSender server, File file) {
        final Collection<ConAirPlugin> result = new ArrayList<ConAirPlugin>();

        if (!file.exists()) {
            System.out.println("ERROR: plugin '" + file.getName() + "' not found!");
            return result;
        }

        final Set<String> pluginClasses = new HashSet<String>();
        try {
            // scan for classes
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                pluginClasses.add(entry.getName().replaceAll("/", "\\."));
            }
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            final URL[] urls = new URL[1];
            urls[0] = file.toURI().toURL();
            final PluginClassLoader classLoader = new PluginClassLoader(this, urls, getClass().getClassLoader());
            for (final String className : pluginClasses) {
                try {
                    final Class<?> jarClass = Class.forName(className, true, classLoader);
                    if (!ConAirPlugin.class.isAssignableFrom(jarClass)) {
                        continue;
                    }
                    final Class<? extends ConAirPlugin> plugin = jarClass.asSubclass(ConAirPlugin.class);
                    final Constructor<? extends ConAirPlugin> constructor = plugin.getConstructor();
                    final ConAirPlugin pluginInstance = constructor.newInstance();
                    pluginInstance.initialize(server, result.getClass().getSimpleName(), pluginManager);
                    result.add(pluginInstance);
                    _loaders.put(pluginInstance.getPluginName(), classLoader);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public Class<?> getClassByName(final String name) {
        Class<?> cachedClass = _classes.get(name);

        if (cachedClass != null) {
            return cachedClass;
        } else {
            for (String current : _loaders.keySet()) {
                PluginClassLoader loader = _loaders.get(current);

                try {
                    cachedClass = loader.findClass(name, false);
                } catch (ClassNotFoundException cnfe) {
                }
                if (cachedClass != null) {
                    return cachedClass;
                }
            }
        }
        return null;
    }


    public void setClass(final String name, final Class<?> clazz) {
        if (!_classes.containsKey(name)) {
            _classes.put(name, clazz);
        }
    }

}