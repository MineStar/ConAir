package de.minestar.conair.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PacketType {

    private static Map<String, Integer> packetIDMap;
    private static Map<Integer, Class<? extends NetworkPacket>> packetClassMap;

    static {
        packetIDMap = new HashMap<String, Integer>();
        packetClassMap = new HashMap<Integer, Class<? extends NetworkPacket>>();
    }

    public static Integer getID(Class<? extends NetworkPacket> packetClazz) {
        return packetIDMap.get(packetClazz.getName());
    }

    public static Class<? extends NetworkPacket> getClassByID(int ID) {
        return packetClassMap.get(ID);
    }

    public static boolean registerPacket(Class<? extends NetworkPacket> packetClazz) {
        try {
            int ID = getUniqueID(packetClazz.getName());
            if (packetClassMap.containsKey(ID)) {
                throw new RuntimeException("NetworkPacket '" + packetClazz.getSimpleName() + "' is already registered!");
            }
            packetIDMap.put(packetClazz.getName(), ID);
            packetClassMap.put(ID, packetClazz);
            System.out.println("Registering '" + packetClazz.getSimpleName() + "' , ID: " + ID);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean unregisterPacket(Class<? extends NetworkPacket> packet) {
        int ID = getUniqueID(packet.getName());
        if (!packetClassMap.containsKey(ID)) {
            throw new RuntimeException("NetworkPacket '" + packet.getSimpleName() + "' is not registered!");
        }
        packetIDMap.remove(packet.getName());
        packetClassMap.remove(ID);
        return true;
    }

    /**
     * This method will (hopefully) return a unique ID for a given string.
     * 
     * @param string
     * @return the ID
     */
    private static int getUniqueID(String string) {
        return UUID.nameUUIDFromBytes(string.getBytes()).hashCode();
    }
}
