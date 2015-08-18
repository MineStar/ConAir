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
import de.minestar.conair.common.utils.Unsafe;


final class PluginLoader {

    private final Map<String, Class<?>> _classes = new HashMap<String, Class<?>>();
    private final Map<String, PluginClassLoader> _loaders = new HashMap<String, PluginClassLoader>();


    public Collection<ConAirPlugin> loadPlugins(PluginManager pluginManager, File file) {
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
                pluginClasses.add(entry.getName().substring(0, entry.getName().length() - ".class".length()).replaceAll("/", "\\."));
            }
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            final URL[] urls = new URL[]{file.toURI().toURL()};
            final PluginClassLoader classLoader = new PluginClassLoader(this, urls, getClass().getClassLoader());
            for (final String className : pluginClasses) {
                try {
                    final Class<?> jarClass = Class.forName(className);
                    _loaders.put(className, classLoader);
                    if (!ConAirPlugin.class.isAssignableFrom(jarClass)) {
                        continue;
                    }

                    final ConAirPlugin pluginInstance = (ConAirPlugin) Unsafe.get().allocateInstance(jarClass);
                    result.add(pluginInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public Class<?> classForName(final String name) throws ClassNotFoundException {
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
        return getClass().getClassLoader().loadClass(name);
    }


    public void setClass(final String name, final Class<?> clazz) {
        if (!_classes.containsKey(name)) {
            _classes.put(name, clazz);
        }
    }

}