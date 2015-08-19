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

package de.minestar.conair.common.packets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.minestar.conair.common.plugin.PluginManagerFactory;


public class SplittedPacketHandler {

    private final Map<Long, List<SplittedPacket>> _map = Collections.synchronizedMap(new HashMap<>());


    public WrappedPacket handle(final WrappedPacket wrappedPacket, final Optional<SplittedPacket> optionalPacket, final PluginManagerFactory pluginManagerFactory) throws ClassNotFoundException {
        // add the packet to the list

        if (!optionalPacket.isPresent()) {
            return null;
        }

        final SplittedPacket packet = optionalPacket.get();
        List<SplittedPacket> list = _map.get(packet.getId());
        if (list == null) {
            list = new ArrayList<SplittedPacket>();
            _map.put(packet.getId(), list);
        }
        list.add(packet);

        // all packets received => reconstruct the packet
        if (list.size() == packet.getTotalPackets()) {
            WrappedPacket completePacket = WrappedPacket.construct(wrappedPacket, list, pluginManagerFactory, packet.getPacketClass());
            _map.remove(packet.getId());
            list.clear();
            return completePacket;
        }
        return null;
    }
}
