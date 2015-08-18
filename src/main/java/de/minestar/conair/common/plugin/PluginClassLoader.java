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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


final class PluginClassLoader extends URLClassLoader {

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
                result = _loader.classForName(name);
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
