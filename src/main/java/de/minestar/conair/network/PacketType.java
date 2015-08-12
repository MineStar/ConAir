package de.minestar.conair.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.minestar.conair.network.packets.NetworkPacket;

public final class PacketType {

    private static Map<String, Integer> PACKET_ID_MAP;
    private static Map<Integer, Class<? extends NetworkPacket>> PACKET_CLASS_MAP;

    static {
        PACKET_ID_MAP = new HashMap<String, Integer>();
        PACKET_CLASS_MAP = new HashMap<Integer, Class<? extends NetworkPacket>>();
    }

    public static <P extends NetworkPacket> Integer getID(Class<P> packetClazz) {
        return PACKET_ID_MAP.get(packetClazz.getName());
    }

    @SuppressWarnings("unchecked")
    public static <P extends NetworkPacket> Class<P> getClassByID(int ID) {
        return (Class<P>) PACKET_CLASS_MAP.get(ID);
    }

    public static <P extends NetworkPacket> boolean registerPacket(Class<P> packetClazz) {
        try {
            int ID = getUniqueID(packetClazz.getName());
            if (PACKET_CLASS_MAP.containsKey(ID)) {
                throw new RuntimeException("NetworkPacket '" + packetClazz.getSimpleName() + "' is already registered!");
            }
            PACKET_ID_MAP.put(packetClazz.getName(), ID);
            PACKET_CLASS_MAP.put(ID, packetClazz);
            System.out.println("Registering '" + packetClazz.getSimpleName() + "' , ID: " + ID);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <P extends NetworkPacket> boolean unregisterPacket(Class<P> packet) {
        int ID = getUniqueID(packet.getName());
        if (!PACKET_CLASS_MAP.containsKey(ID)) {
            throw new RuntimeException("NetworkPacket '" + packet.getSimpleName() + "' is not registered!");
        }
        PACKET_ID_MAP.remove(packet.getName());
        PACKET_CLASS_MAP.remove(ID);
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
