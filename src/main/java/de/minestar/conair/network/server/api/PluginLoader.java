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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.minestar.conair.network.server.DedicatedTCPServer;
import de.minestar.conair.network.server.api.annotations.RegisterEvent;
import de.minestar.conair.network.server.api.events.Event;

public class PluginLoader {

    private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    private final Map<String, PluginClassLoader> loaders = new HashMap<String, PluginClassLoader>();

    public ServerPlugin loadPlugin(PluginManager pluginManager, DedicatedTCPServer server, File file) {
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
                result.initialize(server, pluginDescription.getName(), pluginDescription, pluginManager);
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

    public Map<Class<? extends Event>, EventExecutor> createEventExecutorList(EventListener eventListener, ServerPlugin serverPlugin) {
        Map<Class<? extends Event>, EventExecutor> executorMap = new HashMap<Class<? extends Event>, EventExecutor>();

        Set<Method> methods;
        // catch methods
        try {
            methods = this.getPublicMethods(eventListener, serverPlugin);
        } catch (NoClassDefFoundError e) {
            System.out.println("Plugin " + serverPlugin.getPluginName() + " failed to register events for " + eventListener.getClass() + ".");
            return executorMap;
        }

        // iterate over every method to create our list of EventExecutors...
        for (final Method method : methods) {
            // lookup @RegisterEvent-Annotation
            RegisterEvent registeredEvent = (RegisterEvent) method.getAnnotation(RegisterEvent.class);
            if (registeredEvent == null) {
                continue;
            }

            // check parameterlength
            if (method.getParameterTypes().length != 1) {
                System.out.println("Method '" + method.getName() + "' has a wrong argumentcount ( != 1 )!");
                continue;
            }

            // check if the first argument is a subtype of Event
            final Class<?> checkClass = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(checkClass)) {
                System.out.println("Argument in method '" + method.getName() + "' is not a valid event!");
                continue;
            }

            // set the method accessible
            final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);

            // create the EventExecutor
            EventExecutor executor = new EventExecutor(eventListener, method, registeredEvent.priority(), registeredEvent.ignoreCancelled());
            executorMap.put(eventClass, executor);
        }
        return executorMap;
    }

    private Set<Method> getPublicMethods(EventListener eventListener, ServerPlugin serverPlugin) {
        Set<Method> methods;

        // get all methods
        Method[] publicMethods = eventListener.getClass().getMethods();
        methods = new HashSet<Method>(publicMethods.length);
        for (Method method : publicMethods) {
            methods.add(method);
        }
        // get all declared methods
        for (Method method : eventListener.getClass().getDeclaredMethods()) {
            methods.add(method);
        }
        // return
        return methods;
    }

}