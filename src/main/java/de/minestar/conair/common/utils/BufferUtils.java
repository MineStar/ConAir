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

package de.minestar.conair.common.utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;


public class BufferUtils {

    private static String convertObjectToString(final Object object) {
        if (object.getClass().isArray()) {
            return XmlUtils.objectToXml(object);
        } else if (Collection.class.isAssignableFrom(object.getClass())) {
            return XmlUtils.objectToXml(object);
        } else if (Serializable.class.isAssignableFrom(object.getClass())) {
            return XmlUtils.serializeObject((Serializable) object);
        }
        return null;
    }


    private static Object readNextObjectFromBuffer(final ObjectInputStream buffer, final Class<?> clazz) {
        try {
            if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
                return buffer.readBoolean();
            }
            if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
                return buffer.readByte();
            }
            if (clazz.equals(short.class) || clazz.equals(Short.class)) {
                return buffer.readShort();
            }
            if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
                return buffer.readInt();
            }
            if (clazz.equals(long.class) || clazz.equals(Long.class)) {
                return buffer.readLong();
            }
            if (clazz.equals(float.class) || clazz.equals(Float.class)) {
                return buffer.readFloat();
            }
            if (clazz.equals(double.class) || clazz.equals(Double.class)) {
                return buffer.readDouble();
            }
            if (clazz.equals(String.class)) {
                return buffer.readUTF();
            }
            // arrays & serializable
            if (clazz.isArray() && Serializable.class.isAssignableFrom(clazz)) {
                return buffer.readObject();
            }

            return XmlUtils.objectFromXml(buffer.readUTF());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean readObjectFromBuffer(final ObjectInputStream buffer, final Object instance, final Field field) {
        try {
            field.set(instance, readNextObjectFromBuffer(buffer, field.getType()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean writeObjectIntoBuffer(final ObjectOutputStream buffer, final Object value) {
        try {
            if (value.getClass().equals(boolean.class) || value.getClass().equals(Boolean.class)) {
                buffer.writeBoolean((boolean) value);
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
                buffer.writeUTF((String) value);
                return true;
            }

            // arrays & serializable
            if (value.getClass().isArray() && Serializable.class.isAssignableFrom(value.getClass())) {
                buffer.writeObject(value);
                return true;
            }

            // collections stored as xml
            buffer.writeUTF(convertObjectToString(value));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
