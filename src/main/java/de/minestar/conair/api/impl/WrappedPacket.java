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

package de.minestar.conair.api.impl;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.minestar.conair.api.Packet;

public class WrappedPacket {

    public static final String TARGET_SERVER = "Server";
    private static final Gson JSON_MAPPER = new Gson();

    private final String packetAsJSON;
    private final String packetClassName;
    private final List<String> targets;

    private WrappedPacket(Packet packet, List<String> targets) {
        this.packetAsJSON = JSON_MAPPER.toJson(packet);
        // this
        this.packetClassName = packet.getClass().getName();
        this.targets = targets;
    }

    private WrappedPacket(WrappedPacket wrappedPacket, String source) {
        this.packetAsJSON = wrappedPacket.packetAsJSON;
        this.packetClassName = wrappedPacket.packetClassName;
        this.targets = Arrays.asList(source);
    }

    public boolean is(Class<? extends Packet> clazz) {
        return packetClassName.equals(clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> T getPacket() {
        T result = null;
        try {
            result = (T) JSON_MAPPER.fromJson(packetAsJSON, Class.forName(packetClassName));
        } catch (JsonSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getTargets() {
        return targets;
    }

    public String getPacketClassName() {
        return packetClassName;
    }

    @Override
    public String toString() {
        return "WrappedPacket [packetAsJSON=" + packetAsJSON + ", packetClassName=" + packetClassName + ", targets=" + targets + "]";
    }

    public static WrappedPacket create(Packet packet, String... destination) {
        return new WrappedPacket(packet, Arrays.asList(destination));
    }

    public static WrappedPacket rePack(WrappedPacket packet, String source) {
        return new WrappedPacket(packet, source);
    }

}
