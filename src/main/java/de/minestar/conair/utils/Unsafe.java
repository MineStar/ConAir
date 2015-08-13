/*
 * Copyright (C) 2015 MineStar.de
 * 
 * This file is part of ConAir.
 * 
 * ConAir is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 3 of the License.
 * 
 * ConAir is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ConAir. If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.conair.utils;

import java.lang.reflect.Field;

public class Unsafe {

    @SuppressWarnings("restriction")
    private static sun.misc.Unsafe _unsafe;

    /**
     * Get the {@link Unsafe}.
     * 
     * @return the {@link Unsafe}.
     */
    @SuppressWarnings("restriction")
    public static final sun.misc.Unsafe get() {
        if (_unsafe != null) {
            return _unsafe;
        }
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            _unsafe = (sun.misc.Unsafe) f.get(null);
            return _unsafe;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
