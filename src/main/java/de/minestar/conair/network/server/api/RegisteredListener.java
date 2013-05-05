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

import de.minestar.conair.network.server.api.annotations.Priority;
import de.minestar.conair.network.server.api.exceptions.EventException;

public class RegisteredListener {
    private final EventListener listener;
    private final Priority priority;
    private final ServerPlugin plugin;
    private final EventExecutor executor;
    private final boolean ignoreCancelled;

    public RegisteredListener(EventListener listener, EventExecutor executor, Priority priority, ServerPlugin plugin, boolean ignoreCancelled) {
        this.listener = listener;
        this.priority = priority;
        this.plugin = plugin;
        this.executor = executor;
        this.ignoreCancelled = ignoreCancelled;
    }

    public EventListener getListener() {
        return this.listener;
    }

    public ServerPlugin getPlugin() {
        return this.plugin;
    }

    public Priority getPriority() {
        return this.priority;
    }

    public void callEvent(Event event) throws EventException {
        this.executor.execute(this.listener, event);
    }

    public boolean isIgnoringCancelled() {
        return this.ignoreCancelled;
    }
}