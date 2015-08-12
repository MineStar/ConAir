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
    private final PluginLoader _loader;
    private final Map<String, Class<?>> _classes = new HashMap<String, Class<?>>();

    public PluginClassLoader(PluginLoader loader, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        _loader = loader;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    protected Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = _classes.get(name);
        if (result == null) {
            if (checkGlobal) {
                result = _loader.getClassByName(name);
            }
            if (result == null) {
                result = super.findClass(name);

                if (result != null) {
                    _loader.setClass(name, result);
                }
            }
            _classes.put(name, result);
        }
        return result;
    }

    public Set<String> getClasses() {
        return _classes.keySet();
    }
}