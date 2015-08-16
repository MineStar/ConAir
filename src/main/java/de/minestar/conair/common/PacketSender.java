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

package de.minestar.conair.common;

import de.minestar.conair.api.ConAir;
import de.minestar.conair.api.Packet;
import de.minestar.conair.api.event.Listener;
import de.minestar.conair.client.ConAirClient;


/**
 * This interface represents a single participant of the {@link ConAir}-Network. A participant is always a {@link ConAirServer} or a {@link ConAirClient}.
 */
public interface PacketSender {

    /**
     * Send a single {@link Packet packet} to all given {@link ConAirMember targets}.
     * 
     * @param packet
     *            the {@link Packet packet} to send
     * @param targets
     *            the {@link ConAirMember targets} to send the packet to. If no targets are given, the packet will be broadcasted to everyone.
     * @throws Exception
     */
    void sendPacket(final Packet packet, final ConAirMember... targets) throws Exception;


    /**
     * Get a single {@link ConAirMember member} of this sender. All members of a ConAir-Network can be accessed.
     * 
     * @param name
     *            the name to lookup
     * @return the {@link ConAirMember}
     */
    ConAirMember getMember(final String name);


    /**
     * Register a {@link Listener listener} for {@link Packet packets}.
     * 
     * @param listener
     *            the @link Listener listener} to register.
     */
    <L extends Listener> void registerPacketListener(L listener);


    /**
     * Unregister a {@link Listener listener} for {@link Packet packets}.
     * 
     * @param listener
     *            the class of the @link Listener listener} to unregister.
     */
    <L extends Listener> void unregisterPacketListener(Class<L> listenerClass);


    /**
     * Get the name of this {@link PacketSender}. The name will be the name of the {@link ConAir#SERVER server} or the name of the {@link ConAirClient client}.
     * 
     * @return
     */
    String getName();
}
