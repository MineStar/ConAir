/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Minestar.de
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

package de.minestar.conair.api;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Packet with additional information about the target of the packet. Contains a
 * serialized version of the packet (currently as JSON string)
 */
public class WrappedPacket {

    /**
     * The name for the server. No client can have this name
     */
    public static final String TARGET_SERVER = "ConAirServer";

    // The JSON serializer
    private static final Gson JSON_MAPPER = new Gson();

    private final String packetAsJSON;
    private final String packetClassName;

    private final List<String> targets;

    /**
     * Used when client is sending a packet to the network. Wraps the packet,
     * stores its content as JSON and add target information to the packet.
     * 
     * @param packet
     *            The packet to wrap. Will be serialized as JSON.
     * @param targets
     *            The target clients in the network.
     */
    private WrappedPacket(Packet packet, List<String> targets) {
        this.packetAsJSON = JSON_MAPPER.toJson(packet);
        this.packetClassName = packet.getClass().getName();
        this.targets = targets;
    }

    /**
     * Used when server is sending the packet to a client in the network. The
     * server set the target field as the packets source.
     * 
     * @param wrappedPacket
     *            The wrapped packet, will stay unmodified.
     * @param source
     *            The source of the packet.
     */
    private WrappedPacket(final WrappedPacket wrappedPacket, String source) {
        this.packetAsJSON = wrappedPacket.packetAsJSON;
        this.packetClassName = wrappedPacket.packetClassName;
        this.targets = Arrays.asList(source);
    }

    /**
     * Check if this packet contains an object of the class.
     * 
     * @param clazz
     *            The class
     * @return <code>true</code> if, and only if, this packet class name is
     *         equals to {@link Class#getName()}
     */
    public boolean is(Class<? extends Packet> clazz) {
        return packetClassName.equals(clazz.getName());
    }

    @SuppressWarnings("unchecked")
    /**
     * Parse the JSON content of this packet. Invoking this method will always produce a new object!
     * @return 
     */
    public <T extends Packet> Optional<T> getPacket() {

        try {
            T result = (T) JSON_MAPPER.fromJson(packetAsJSON, Class.forName(packetClassName));
            return Optional.of(result);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Get a list of targets. If the packet is received from ConAir server, use
     * {@link #getSource()} as an easier way to receive the packets source.
     * Otherwise, the list will contain all (or no one for a broadcast) targeted
     * clients.
     * 
     * @return Empty list for broadcast or targeted client names.
     */
    public List<String> getTargets() {
        return targets;
    }

    /**
     * Shortcut for <code>getTargets().get(0)</code> with an additional check,
     * if the list contains only one element. Otherwise it will return an empty
     * string.
     * 
     * @return
     */
    public String getSource() {
        return targets.size() == 1 ? targets.get(0) : "";
    }

    @Override
    public String toString() {
        return "WrappedPacket [packetAsJSON=" + packetAsJSON + ", packetClassName=" + packetClassName + ", targets=" + targets + "]";
    }

    /**
     * Wrap a packet. See {@link #WrappedPacket(Packet, List)} for description.
     * 
     * @param packet
     *            Packet to wrap.
     * @param targets
     *            List of targets.
     * @return A wrapped packet, ready to send to server.
     */
    public static WrappedPacket create(Packet packet, String... targets) {
        return new WrappedPacket(packet, Arrays.asList(targets));
    }

    /**
     * Change target list to single source entry. See
     * {@link #WrappedPacket(WrappedPacket, String)} for description.
     * 
     * @param packet
     *            Packet to wrap. Will not be modified.
     * @param source
     *            The source of the packet.
     * @return A wrapped packet, ready to send to clients.
     */
    public static WrappedPacket rePack(final WrappedPacket packet, String source) {
        return new WrappedPacket(packet, source);
    }

}
