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

package de.minestar.conair.utils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;

public class BufferUtils {

    private static final String PREFIX_ARRAY = "[ARRAY]";
    private static final String PREFIX_COLLECTION = "[COLLECTION]";
    private static final String PREFIX_OBJECT = "[OBJECT]";

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

    public static boolean readObjectFromBuffer(final ObjectInputStream buffer, final Object instance, final Field field) {
        try {
            final Class<?> clazz = field.getType();
            if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) {
                field.set(instance, buffer.readByte() == 1);
                return true;
            }
            if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
                field.set(instance, buffer.readByte());
                return true;
            }
            if (clazz.equals(byte[].class) || clazz.equals(Byte[].class)) {
                field.set(instance, buffer.readObject());
                return true;
            }
            if (clazz.equals(short.class) || clazz.equals(Short.class)) {
                field.set(instance, buffer.readShort());
                return true;
            }
            if (clazz.equals(int.class) || clazz.equals(Integer.class)) {
                field.set(instance, buffer.readInt());
                return true;
            }
            if (clazz.equals(long.class) || clazz.equals(Long.class)) {
                field.set(instance, buffer.readLong());
                return true;
            }
            if (clazz.equals(float.class) || clazz.equals(Float.class)) {
                field.set(instance, buffer.readFloat());
                return true;
            }
            if (clazz.equals(double.class) || clazz.equals(Double.class)) {
                field.set(instance, buffer.readDouble());
                return true;
            }
            if (clazz.equals(String.class)) {
                String text = buffer.readUTF();
                if (text.startsWith(PREFIX_ARRAY)) {
                    text = text.substring(PREFIX_ARRAY.length());
                    field.set(instance, (Object[]) XmlUtils.objectFromXml(text));
                    return true;
                } else if (text.startsWith(PREFIX_COLLECTION)) {
                    text = text.substring(PREFIX_COLLECTION.length());
                    field.set(instance, (Collection<?>) XmlUtils.objectFromXml(text));
                    return true;
                } else if (text.startsWith(PREFIX_OBJECT)) {
                    text = text.substring(PREFIX_OBJECT.length());
                    field.set(instance, XmlUtils.deserializeObject(text));
                    return true;
                }
                field.set(instance, text);
                return true;
            }
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
            if (value.getClass().equals(byte[].class) || value.getClass().equals(Byte[].class)) {
                buffer.writeObject(value);
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
            // arrays should always be stored as xml
            if (value.getClass().isArray()) {
                System.out.println("is array");
                buffer.writeUTF(convertObjectToString(value));
                return true;
            }

            // collections stored as xml
            if (Collection.class.isAssignableFrom(value.getClass())) {
                buffer.writeUTF(convertObjectToString(value));
                return true;
            }

            // serializables stored as xml
            if (Serializable.class.isAssignableFrom(value.getClass())) {
                buffer.writeUTF(convertObjectToString(value));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
