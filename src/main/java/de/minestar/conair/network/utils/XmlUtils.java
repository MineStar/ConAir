/*
 * Copyright (C) 2015 Minestar.de
 * 
 * This file is part of PermissionSystem.
 * 
 * PermissionSystem is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * PermissionSystem is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PermissionSystem.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.conair.network.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * 
 * Utilityclass for XML handling.
 *
 */
public class XmlUtils {

    private XmlUtils() {
    }

    /**
     * Read an object from a xml-string.
     * 
     * @param xml
     *            the xml-string
     * 
     * @return the object.
     */
    public static Object objectFromXml(final String xml) {
        final XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(xml.getBytes())));
        final Object object = decoder.readObject();
        decoder.close();
        return object;
    }

    /**
     * Convert a object to a xml-string.
     * 
     * @param object
     *            the object to convert
     * 
     * @return the xml-string.
     */
    public static String objectToXml(final Object object) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(byteArrayOutputStream));
        encoder.writeObject(object);
        encoder.close();
        try {
            return byteArrayOutputStream.toString("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Deserialize a {@link Base64} encoded string to an Object.
     * 
     * @param encodedString
     *            the {@link Base64} encoded string
     * 
     * @return the deserialized Object.
     */
    public static Object deserializeObject(final String encodedString) {
        try {
            final byte[] data = Base64.getDecoder().decode(encodedString.getBytes());
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            final Object obj = objectInputStream.readObject();
            objectInputStream.close();
            return obj;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serialize an Object to a {@link Base64} encoded string.
     * 
     * @param object
     *            the object to serialize
     * 
     * @return the {@link Base64} encoded string.
     */
    public static String serializeObject(final Serializable object) {
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            byteArrayOutputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (final Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
