// License: GPL. For details, see LICENSE file.
package edu.cornell.rocketry.xbee;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;

public class XBeeListenerThread extends Thread {

	private String data = "";
	private boolean keepListening;
	private boolean receiving = false;
	private XBeeListenerGui mainWindow;

	public XBeeListenerThread(XBeeListenerGui gui) {
		mainWindow = gui;
		keepListening = true;
	}
	
	public void stopListening() { keepListening = false; }

	private int bintodec(int bin){
		//for 8-bit binary
		int dec=0;
		for(int i=0;i<8;i++){
			dec = ((int) Math.pow(2, (double)i))*(bin%10);
			bin = bin/10;
		}
		return dec;
	}
	
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
	
	@Override
	public void run() {
		while (keepListening) {
			try {
				XBeeResponse response = mainWindow.xbee.getResponse();
				// System.out.println(response.getApiId().toString());
				// System.out.println(response.getClass().toString());
				if (response.getApiId() == ApiId.ZNET_RX_RESPONSE) {

					ZNetRxResponse ioSample = (ZNetRxResponse) response;

					int[] longdata = new int[15];
					longdata = ioSample.getData();
					
					String lat = "", longi = "", alt = "", flag = "";
					final int latMarker = 0xA;
					final int longMarker = 0xB;
					final int altMarker = 0xC;
					final int flagMarker = 0xD;
					//System.out.println(longdata.length);
					long result = 0;
					try {
						if(longdata[0] == latMarker){
							int[] newLat = new int[4];
							System.arraycopy(longdata,1,newLat,0,4);
							lat = String.valueOf(convertToDecimal(newLat));
						}
						if(longdata[5] == longMarker){
							int[] newLong = new int[4];
							System.arraycopy(longdata,6,newLong,0,4);
							longi = String.valueOf(convertToDecimal(newLong));
						}
						if(longdata[10] == altMarker){
							int[] newAlt = new int[2];
							System.arraycopy(longdata,11,newAlt,0,2);
							alt = String.valueOf(convertToDecimal(newAlt));
						}
						if(longdata[14] == flagMarker){
							flag = String.valueOf(longdata[15]);
						}
						mainWindow.updateData(lat, longi, alt, flag);
					}
					catch(ArrayIndexOutOfBoundsException e) {
						mainWindow.incNumError();
						mainWindow.addToReceiveText("Error (" + mainWindow.getNumError() + "): Malformed Packet");
					}
					mainWindow.incNumRec();
					mainWindow.addToReceiveText("Received (" + mainWindow.getNumRec() + "): "
							+ result);
					//String packet = ByteUtils.toString(ioSample.getData());
					// System.out.println("Recieved Data: " + packet);
					// nLabel.setText("" + nr);

					//packet is not deformed
					/*
					if (packet.charAt(0) == '<') {
						// long incoming packet, formated "<[packet]>"
						//System.out.println("start!");
						if (receiving) {
							// System.out.println("ERROR: new packet before last data stream ended");
							mainWindow.incNumError();
							mainWindow.addToReceiveText("Error (" + mainWindow.getNumError() + "): New packet before last data stream ended...");
							mainWindow.incNumRec();
							mainWindow.addToReceiveText("Incomplete Received (" + mainWindow.getNumRec() + "): " + data);
							mainWindow.incNumRec();
							mainWindow.addToReceiveText("New Received (" + mainWindow.getNumRec() + "): " + packet);
						}
						receiving = true;
					} 
					else if (!receiving) {
						mainWindow.incNumRec();
						mainWindow.addToReceiveText("Received (" + mainWindow.getNumRec() + "): "
								+ result);
					} 
					else
					{
						// System.out.println("recieved...!");
						// System.out.println(packet);
						//data += packet;
						if (packet.charAt(packet.length() - 1) == '>') {
							// end of data stream reached--> return data and
							// reset...
							// System.out.println(data);
							mainWindow.incNumRec();
							mainWindow.addToReceiveText("Received (" + mainWindow.getNumRec() + "): " + data);
							receiving = false;
							data = "";

						}
					}*/
				}
			} 
			catch (XBeeTimeoutException e) {
				System.out.println("timeout");
				// we timed out without a response
			} catch (XBeeException e) {
				mainWindow.incNumError();
				mainWindow.addToReceiveText("Error (" + mainWindow.getNumError() + "): XBee Problem: "+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
