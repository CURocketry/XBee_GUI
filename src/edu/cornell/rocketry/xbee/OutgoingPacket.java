package edu.cornell.rocketry.xbee;

public class OutgoingPacket {
	public static final int DIR_TEST = 0xAB;
	public static final int DIR_PAYLOAD = 0xAC;
	
	private OutgoingPacketType type;
	private int[] payload;
	
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
	}
	
	public int[] getPayload() {
		return payload;
	}

}
