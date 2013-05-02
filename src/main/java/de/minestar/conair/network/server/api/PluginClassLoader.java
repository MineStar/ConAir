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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PluginClassLoader extends URLClassLoader {
    private final PluginLoader loader;
    private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

    public PluginClassLoader(PluginLoader loader, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.loader = loader;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    protected Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if (result == null) {
            if (checkGlobal) {
                result = loader.getClassByName(name);
            }
            if (result == null) {
                result = super.findClass(name);

                if (result != null) {
                    loader.setClass(name, result);
                }
            }
            classes.put(name, result);
        }
        return result;
    }

    public Set<String> getClasses() {
        return this.classes.keySet();
    }
}