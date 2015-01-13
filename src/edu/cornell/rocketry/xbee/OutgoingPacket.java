package edu.cornell.rocketry.xbee;

public class OutgoingPacket {
	public static final int FLAG_PAYLOAD = 0xAB;
	
	private OutgoingPacketType type;
	private int[] payload;
	
	public OutgoingPacket(OutgoingPacketType t) {
		type = t;
		switch (type) {
		case PAYLOAD_LAUNCH:
			payload = new int[1];
			payload[0] = FLAG_PAYLOAD;
			break;
		}
	}
	
	public int[] getPayload() {
		return payload;
	}

}
