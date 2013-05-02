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
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    protected final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    protected final Map<String, PluginClassLoader> loaders = new HashMap<String, PluginClassLoader>();

    public ServerPlugin loadPlugin(File file) {
        ServerPlugin result = null;
        PluginDescription pluginDescription = null;

        if (!file.exists()) {
            System.out.println("ERROR: plugin '" + file.getName() + "' not found!");
            return result;
        }

        try {
            JarFile jarFile = new JarFile(file);
            JarEntry jarEntry = jarFile.getJarEntry("plugin.yml");
            if (jarEntry == null) {
                System.out.println("Jar '" + file.getName() + "' does not contain plugin.yml");
            }
            InputStream stream = jarFile.getInputStream(jarEntry);
            pluginDescription = new PluginDescription(stream);
            stream.close();
            jarFile.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        PluginClassLoader classLoader = null;
        try {
            URL[] urls = new URL[1];
            urls[0] = file.toURI().toURL();
            classLoader = new PluginClassLoader(this, urls, getClass().getClassLoader());
            Class<?> jarClass = Class.forName(pluginDescription.getMain(), true, classLoader);
            if (jarClass.newInstance() instanceof ServerPlugin) {
                Class<? extends ServerPlugin> plugin = jarClass.asSubclass(ServerPlugin.class);
                Constructor<? extends ServerPlugin> constructor = plugin.getConstructor();
                result = constructor.newInstance();
                result.initialize(pluginDescription.getName(), pluginDescription);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        loaders.put(pluginDescription.getName(), classLoader);
        return result;
    }

    public Class<?> getClassByName(final String name) {
        Class<?> cachedClass = classes.get(name);

        if (cachedClass != null) {
            return cachedClass;
        } else {
            for (String current : loaders.keySet()) {
                PluginClassLoader loader = loaders.get(current);

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
        if (!classes.containsKey(name)) {
            classes.put(name, clazz);
        }
    }
}