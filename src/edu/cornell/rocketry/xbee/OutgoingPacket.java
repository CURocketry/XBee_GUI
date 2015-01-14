package edu.cornell.rocketry.xbee;

/**
 * An instance of OutgoingPacket contains the outgoing packet 
 * @author Matt
 *
 */
public class OutgoingPacket implements Packet{
	public static final int DIR_TEST = 0xAB;
	public static final int DIR_PAYLOAD = 0xAC;
	
	private OutgoingPacketType type;
	private int[] payload;
	private final int size;
	
	/**
	 * Constructor
	 * @param t		outgoing packet type
	 */
	public OutgoingPacket(OutgoingPacketType t) {
		type = t;
		switch (type) {
		case TEST:
			payload = new int[1];
			payload[0] = DIR_TEST;
			break;
		case PAYLOAD_LAUNCH:
			payload = new int[1];
			payload[0] = DIR_PAYLOAD;
			break;
		}
		size = payload.length;
	}
	
	/**
	 * get payload of packet
	 * @return int[] payload
	 */
	public int[] getPayload() {
		return payload;
	}
	
	/**
	 * get size of packet
	 * @return int size
	 */
	public int getSize() {
		return size;
	}
	
	@Override public String toString() {
		String data = "";
		for (int i=0;i < payload.length;i++){
			data += String.format("%8s", Integer.toHexString(payload[i])).replaceAll(" ", "").toUpperCase() + " ";
		}
		return data;
	}

}
