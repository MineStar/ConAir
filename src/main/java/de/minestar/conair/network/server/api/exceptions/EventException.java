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

package de.minestar.conair.network.server.api.exceptions;

public class EventException extends Exception {

    private static final long serialVersionUID = 8780413669728435313L;

    private final Throwable cause;

    public EventException(Throwable throwable) {
        this.cause = throwable;
    }

    public EventException() {
        this.cause = null;
    }

    public EventException(Throwable cause, String message) {
        super(message);
        this.cause = cause;
    }

    public EventException(String message) {
        super(message);
        this.cause = null;
    }

    public Throwable getCause() {
        return this.cause;
    }
}
