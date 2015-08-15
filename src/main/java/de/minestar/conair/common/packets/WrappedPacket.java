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

package de.minestar.conair.common.packets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.minestar.conair.api.Packet;
import de.minestar.conair.common.ConAirMember;
import de.minestar.conair.common.utils.BufferUtils;
import de.minestar.conair.common.utils.Unsafe;

/**
 * Packet with additional information about the target of the packet. Contains a serialized version of the packet (currently as JSON string)
 */
public class WrappedPacket {

    private final static int MAX_PACKET_SIZE = 1 * 1024; // 1 KB

    private final String packetAsJSON;
    private final String packetClassName;

    private final List<String> targets;
    private final String source;

    /**
     * Used when client is sending a packet to the network. Wraps the packet, stores its content as JSON and add target information to the packet.
     * 
     * @param packet
     *            The packet to wrap. Will be serialized as JSON.
     * @param targets
     *            The target clients in the network.
     * @throws IOException
     */
    private WrappedPacket(Packet packet, String source, List<String> targets) throws IOException {
        this(packet.getClass(), encodePacket(packet), source, targets);
    }

    /**
     * Used when client is sending a packet to the network. Wraps the packet, stores its content as JSON and add target information to the packet.
     * 
     * @param packet
     *            The packet to wrap. Will be serialized as JSON.
     * @param targets
     *            The target clients in the network.
     */
    private <P extends Packet> WrappedPacket(Class<P> packetClass, String packetData, String source, List<String> targets) {
        this.packetAsJSON = packetData;
        this.source = source;
        this.packetClassName = packetClass.getName();
        this.targets = targets;
    }

    private static String encodePacket(Packet packet) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        final Field[] fields = packet.getClass().getDeclaredFields();
        for (final Field field : fields) {
            // ignore "static", "volatile" and "transient" fields
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isVolatile(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            try {
                BufferUtils.writeObjectIntoBuffer(oos, field.get(packet));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            field.setAccessible(false);
        }
        oos.close();
        String encodedString = Base64.getEncoder().encodeToString(bos.toByteArray());
        return encodedString;
    }

    /**
     * Used when server is sending the packet to a client in the network. The server set the target field as the packets source.
     * 
     * @param wrappedPacket
     *            The wrapped packet, will stay unmodified.
     * @param target
     *            The source of the packet.
     */
    private WrappedPacket(final WrappedPacket wrappedPacket, String source, String target) {
        this.packetAsJSON = wrappedPacket.packetAsJSON;
        this.packetClassName = wrappedPacket.packetClassName;
        this.source = source;
        this.targets = Arrays.asList(target);
    }

    /**
     * Check if this packet contains an object of the class.
     * 
     * @param clazz
     *            The class
     * @return <code>true</code> if, and only if, this packet class name is equals to {@link Class#getName()}
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
        T result;
        try {
            result = decodePacket(packetAsJSON, (Class<T>) Class.forName(packetClassName));
            return Optional.of(result);
        } catch (ClassNotFoundException | IOException | InstantiationException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Packet> T decodePacket(String data, Class<T> packetClass) throws IOException, InstantiationException {
        // get the constructor
        ByteArrayInputStream bos = new ByteArrayInputStream(Base64.getDecoder().decode(data.getBytes()));
        ObjectInputStream oos = new ObjectInputStream(bos);

        @SuppressWarnings("restriction")
        T instance = (T) Unsafe.get().allocateInstance(packetClass);

        final Field[] fields = packetClass.getDeclaredFields();
        for (final Field field : fields) {
            // ignore "static", "volatile" and "transient" fields
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isVolatile(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            if (!BufferUtils.readObjectFromBuffer(oos, instance, field)) {
                throw new IllegalArgumentException("Field '" + field.getName() + "' could not be read (unknown datatype)!");
            }
            field.setAccessible(false);
        }

        oos.close();

        return instance;
    }
    /**
     * Get a list of targets. If the packet is received from ConAir server, use {@link #getSource()} as an easier way to receive the packets source. Otherwise, the list will contain all (or no one for a broadcast) targeted clients.
     * 
     * @return Empty list for broadcast or targeted client names.
     */
    public List<String> getTargets() {
        return targets;
    }

    /**
     * @return The complete name of the encapsulated class name
     */
    public String getPacketClassName() {
        return packetClassName;
    }

    /**
     * Shortcut for <code>getTargets().get(0)</code> with an additional check, if the list contains only one element. Otherwise it will return an empty string.
     * 
     * @return
     */
    public String getSource() {
        return source;
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
     * @throws IOException
     */
    public static List<WrappedPacket> create(Packet packet, ConAirMember source, ConAirMember... targets) throws IOException {
        final List<WrappedPacket> packets = new ArrayList<WrappedPacket>();
        final List<String> targetList = new ArrayList<>();
        for (ConAirMember target : targets) {
            targetList.add(target.getName().replaceAll("\"", ""));
        }
        final WrappedPacket completePacket = new WrappedPacket(packet, source.getName(), targetList);

        final byte[] packetData = completePacket.packetAsJSON.getBytes();
        if (packetData.length > MAX_PACKET_SIZE) {
            Collection<SplittedPacket> split = SplittedPacket.split(MAX_PACKET_SIZE, packetData.length, packet, completePacket.packetAsJSON);
            for (final SplittedPacket smallPacket : split) {
                packets.add(new WrappedPacket(smallPacket, completePacket.source, completePacket.targets));
            }
        } else {
            packets.add(completePacket);
        }
        return packets;
    }

    /**
     * Change target list to single source entry. See {@link #WrappedPacket(WrappedPacket, String)} for description.
     * 
     * @param packet
     *            Packet to wrap. Will not be modified.
     * @param source
     *            The source of the packet.
     * @return A wrapped packet, ready to send to clients.
     */
    public static WrappedPacket rePack(final WrappedPacket packet, String source, String target) {
        return new WrappedPacket(packet, source, target);
    }

    @SuppressWarnings("unchecked")
    static <P extends Packet> WrappedPacket construct(WrappedPacket wrappedPacket, List<SplittedPacket> packets, String packetClassName) throws ClassNotFoundException {
        Collections.sort(packets);
        StringBuilder builder = new StringBuilder();
        for (final SplittedPacket packet : packets) {
            builder.append(packet.getData());
        }
        return new WrappedPacket((Class<P>) Class.forName(packetClassName), builder.toString(), wrappedPacket.source, wrappedPacket.targets);
    }
}
