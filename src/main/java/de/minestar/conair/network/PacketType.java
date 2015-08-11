package de.minestar.conair.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.minestar.conair.network.packets.NetworkPacket;

public final class PacketType {

    private static Map<String, Integer> packetIDMap;
    private static Map<Integer, Class<? extends NetworkPacket>> packetClassMap;

    static {
        packetIDMap = new HashMap<String, Integer>();
        packetClassMap = new HashMap<Integer, Class<? extends NetworkPacket>>();
    }

    public static <P extends NetworkPacket> Integer getID(Class<P> packetClazz) {
        return packetIDMap.get(packetClazz.getName());
    }

    @SuppressWarnings("unchecked")
    public static <P extends NetworkPacket> Class<P> getClassByID(int ID) {
        return (Class<P>) packetClassMap.get(ID);
    }

    public static <P extends NetworkPacket> boolean registerPacket(Class<P> packetClazz) {
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

    public static <P extends NetworkPacket> boolean unregisterPacket(Class<P> packet) {
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
