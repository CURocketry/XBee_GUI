package edu.cornell.rocketry.xbee;

public class IncomingPacket {
	final static public int MARKER_LAT = 0xB;
	final static public int LEN_LAT = 4;
	final static public int MARKER_LON = 0xC;
	final static public int LEN_LON =4;
	final static public int MARKER_ALT = 0xD;
	final static public int LEN_ALT = 2;
	final static public int MARKER_FLAG = 0xE;
	final static public int LEN_FLAG = 1;
	
	private int[] packetData;
	private long latitude;
	private long longitude;
	private int altitude;
	private byte flag;
	
	public String getLatitude() { return latitude+""; }
	public String getLongitude() { return longitude+""; }
	public String getAltitude() { return altitude+""; }
	public String getFlag() { return flag+"";}
	
	private int convertToDecimal(int[] array){
		int result = 0;
		for (int i=array.length-1; i>=0; i--) {
			//System.out.println(longdata[i]);
			if (i>0)
				result = (result | array[i]) << 8;
			else result = result | array[i];
		}
		return result;
	}
	
	
	public IncomingPacket(int[] data) {
		packetData = data;
		int readerIndex = 0;
		//long result = 0;
		try {
			if(data[readerIndex] == MARKER_LAT){
				readerIndex++;
				int[] newLat = new int[LEN_LAT];
				System.arraycopy(data,readerIndex,newLat,0,LEN_LAT);
				latitude = convertToDecimal(newLat);
				readerIndex = readerIndex + LEN_LAT;
			}
			if(data[readerIndex] == MARKER_LON){
				readerIndex++;
				int[] newLon = new int[LEN_LON];
				System.arraycopy(data,readerIndex,newLon,0,LEN_LON);
				longitude = convertToDecimal(newLon);
				readerIndex = readerIndex + LEN_LON;
			}
			if(data[readerIndex] == MARKER_ALT){
				readerIndex++;
				int[] newAlt = new int[LEN_ALT];
				System.arraycopy(data,readerIndex,newAlt,0,LEN_ALT);
				altitude = convertToDecimal(newAlt);
				readerIndex = readerIndex + LEN_ALT;
			}
			if(data[readerIndex] == MARKER_FLAG){
				readerIndex++;
				//flag = String.valueOf([15]);
				flag = (byte)data[readerIndex];
				readerIndex = readerIndex + LEN_FLAG;
			}
			//else throw new ArrayIndexOutOfBoundsException();
			if (readerIndex != data.length) {
				System.out.println(readerIndex);
				System.out.println("Packet reader error. Check markers and order");
				//TODO throw error
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("Error: Malformed Packet");
			//mainWindow.incNumError();
			//mainWindow.addToReceiveText("Error (" + mainWindow.getNumError() + "): Malformed Packet");
		}
	}
	
	
}
