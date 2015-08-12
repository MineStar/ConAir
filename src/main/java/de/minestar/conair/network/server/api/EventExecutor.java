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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.minestar.conair.network.server.api.annotations.Priority;
import de.minestar.conair.network.server.api.events.Event;
import de.minestar.conair.network.server.api.exceptions.EventException;

public class EventExecutor {

    private final EventListener _eventListener;
    private final Method _method;
    private final Priority _priority;
    private final boolean _ignoreCancelled;

    public EventExecutor(EventListener eventListener, Method method, Priority priority, boolean ignoreCancelled) {
        _eventListener = eventListener;
        _method = method;
        _priority = priority;
        _ignoreCancelled = ignoreCancelled;
    }

    public void execute(Event event) throws EventException {
        try {
            if (!Event.class.isAssignableFrom(event.getClass())) {
                return;
            }
            _method.invoke(_eventListener, event);
        } catch (InvocationTargetException ex) {
            throw new EventException(ex.getCause());
        } catch (Throwable t) {
            throw new EventException(t);
        }
    }

    public Priority getPriority() {
        return _priority;
    }

    public boolean isIgnoreCancelled() {
        return _ignoreCancelled;
    }
}