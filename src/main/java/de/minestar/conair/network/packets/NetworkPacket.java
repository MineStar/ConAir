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

package de.minestar.conair.network.packets;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import de.minestar.conair.network.PacketBuffer;
import de.minestar.conair.network.PacketType;
import de.minestar.conair.network.utils.XmlUtils;

public abstract class NetworkPacket {

    private static final String PREFIX_ARRAY = "[ARRAY]";
    private static final String PREFIX_COLLECTION = "[COLLECTION]";
    private static final String PREFIX_OBJECT = "[OBJECT]";

    public static final byte PACKET_SEPERATOR = 3;

    protected int _packetID = -1;

    /**
     * Empty constructor. Used for creation of packets to be sent.
     */
    protected NetworkPacket() {
    }

    /**
     * Constructor used for received packets.
     * 
     * @param packetID
     * @param buffer
     * @throws IOException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public NetworkPacket(int packetID, PacketBuffer buffer) throws IOException, IllegalArgumentException, IllegalAccessException {
        this._packetID = packetID;
        onReceive(buffer);
    }

    public boolean pack(PacketBuffer buffer) {
        Integer packetID = PacketType.getID(this.getClass());
        if (packetID != null) {
            buffer.writeInt(0); // Size
            buffer.writeInt(packetID); // Type
            try {
                onSend(buffer);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
            buffer.writeInt(0, buffer.getBuffer().position()); // Write size
            buffer.put(PACKET_SEPERATOR); // Close packet
            return true;
        } else {
            return false;
        }
    }

    public final int getPacketID() {
        return _packetID;
    }

    public boolean isBroadcastPacket() {
        return true;
    }

    private Collection<Field> getFields() {
        final Field[] declaredFields = getClass().getDeclaredFields();
        final Map<String, Field> map = new TreeMap<String, Field>();
        for (final Field field : declaredFields) {
            if (field.getModifiers() == Modifier.TRANSIENT || field.getModifiers() == Modifier.VOLATILE) {
                continue;
            }
            map.put(field.getName(), field);
        }
        return map.values();
    }

    private final void onSend(PacketBuffer buffer) throws IllegalArgumentException, IllegalAccessException {
        final Collection<Field> fields = getFields();

        // finally write the fields
        for (final Field field : fields) {
            field.setAccessible(true);
            if (!writeObjectIntoBuffer(buffer, field.get(this))) {
                throw new IllegalArgumentException("Field '" + field.getName() + "' could not be written (unknown datatype)!");
            }
            field.setAccessible(false);
        }
    }

    private boolean writeObjectIntoBuffer(final PacketBuffer buffer, final Object value) {
        if (value.getClass().equals(boolean.class) || value.getClass().equals(Boolean.class)) {
            boolean bool = (boolean) value;
            buffer.writeByte(bool ? (byte) 1 : (byte) 0);
            return true;
        }
        if (value.getClass().equals(byte.class) || value.getClass().equals(Byte.class)) {
            buffer.writeByte((byte) value);
            return true;
        }
        if (value.getClass().equals(short.class) || value.getClass().equals(Short.class)) {
            buffer.writeShort((short) value);
            return true;
        }
        if (value.getClass().equals(int.class) || value.getClass().equals(Integer.class)) {
            buffer.writeInt((int) value);
            return true;
        }
        if (value.getClass().equals(long.class) || value.getClass().equals(Long.class)) {
            buffer.writeLong((long) value);
            return true;
        }
        if (value.getClass().equals(float.class) || value.getClass().equals(Float.class)) {
            buffer.writeFloat((float) value);
            return true;
        }
        if (value.getClass().equals(double.class) || value.getClass().equals(Double.class)) {
            buffer.writeDouble((double) value);
            return true;
        }
        if (value.getClass().equals(String.class)) {
            buffer.writeString((String) value);
            return true;
        }
        // arrays should always be stored as xml
        if (value.getClass().isArray()) {
            buffer.writeString(convertObjectToString(value));
            return true;
        }

        // collections stored as xml
        if (Collection.class.isAssignableFrom(value.getClass())) {
            buffer.writeString(convertObjectToString(value));
            return true;
        }

        // serializables stored as xml
        if (Serializable.class.isAssignableFrom(value.getClass())) {
            buffer.writeString(convertObjectToString(value));
            return true;
        }

        return false;
    }

    private static String convertObjectToString(final Object object) {
        final StringBuilder stringBuilder = new StringBuilder();
        if (object.getClass().isArray()) {
            stringBuilder.append(PREFIX_ARRAY);
            stringBuilder.append(XmlUtils.objectToXml(object));
        } else if (Collection.class.isAssignableFrom(object.getClass())) {
            stringBuilder.append(PREFIX_COLLECTION);
            stringBuilder.append(XmlUtils.objectToXml(object));
        } else if (Serializable.class.isAssignableFrom(object.getClass())) {
            stringBuilder.append(PREFIX_OBJECT);
            stringBuilder.append(XmlUtils.serializeObject((Serializable) object));
        }
        return stringBuilder.toString();
    }

    private final void onReceive(PacketBuffer buffer) throws IllegalArgumentException, IllegalAccessException {
        final Collection<Field> fields = getFields();
        for (final Field field : fields) {
            field.setAccessible(true);
            if (!readObjectFromBuffer(buffer, field)) {
                throw new IllegalArgumentException("Field '" + field.getName() + "' could not be read (unknown datatype)!");
            }
            field.setAccessible(false);
        }
    }

    private boolean readObjectFromBuffer(final PacketBuffer buffer, final Field field) throws IllegalArgumentException, IllegalAccessException {
        final Class<?> clazz = field.getType();
        if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
            field.set(this, buffer.readByte() == 1);
            return true;
        }
        if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
            field.set(this, buffer.readByte());
            return true;
        }
        if (clazz.equals(short.class) || clazz.equals(Short.class)) {
            field.set(this, buffer.readShort());
            return true;
        }
        if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
            field.set(this, buffer.readInt());
            return true;
        }
        if (clazz.equals(long.class) || clazz.equals(Long.class)) {
            field.set(this, buffer.readLong());
            return true;
        }
        if (clazz.equals(float.class) || clazz.equals(Float.class)) {
            field.set(this, buffer.readFloat());
            return true;
        }
        if (clazz.equals(double.class) || clazz.equals(Double.class)) {
            field.set(this, buffer.readDouble());
            return true;
        }
        if (clazz.equals(String.class)) {
            String text = buffer.readString();
            if (text.startsWith(PREFIX_ARRAY)) {
                text = text.substring(PREFIX_ARRAY.length());
                field.set(this, (Object[]) XmlUtils.objectFromXml(text));
                return true;
            } else if (text.startsWith(PREFIX_COLLECTION)) {
                text = text.substring(PREFIX_COLLECTION.length());
                field.set(this, (Collection<?>) XmlUtils.objectFromXml(text));
                return true;
            } else if (text.startsWith(PREFIX_OBJECT)) {
                text = text.substring(PREFIX_OBJECT.length());
                field.set(this, XmlUtils.deserializeObject(text));
                return true;
            }
            field.set(this, text);
            return true;
        }
        return false;
    }

}
