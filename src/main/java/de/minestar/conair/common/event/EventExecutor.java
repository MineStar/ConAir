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

package de.minestar.conair.common.event;

import java.lang.reflect.Method;

import de.minestar.conair.api.Packet;
import de.minestar.conair.api.event.Listener;
import de.minestar.conair.common.ConAirMember;
import de.minestar.conair.common.PacketSender;


public class EventExecutor {

    private final Listener _listener;
    private final Method _method;


    public EventExecutor(final Listener listener, final Method method) {
        _listener = listener;
        _method = method;
    }


    public <P extends Packet> void execute(final PacketSender receiver, final ConAirMember source, final P packet) {
        try {
            _method.setAccessible(true);
            _method.invoke(_listener, receiver, source, packet);
            _method.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}