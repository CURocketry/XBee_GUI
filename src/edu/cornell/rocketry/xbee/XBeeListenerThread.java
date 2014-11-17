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
					final int MARKER_LAT = 0xB;
					final int LEN_LAT = 4;
					final int MARKER_LON = 0xC;
					final int LEN_LON =4;
					final int MARKER_ALT = 0xD;
					final int LEN_ALT = 2;
					final int MARKER_FLAG = 0xE;
					final int LEN_FLAG = 1;
					//System.out.println(longdata.length);
					int readerIndex = 0;
					long result = 0;
					System.out.println(longdata);
					System.out.println(longdata[0]);
					try {
						if(longdata[readerIndex] == MARKER_LAT){
							readerIndex++;
							int[] newLat = new int[LEN_LAT];
							System.arraycopy(longdata,readerIndex,newLat,0,LEN_LAT);
							lat = String.valueOf(convertToDecimal(newLat));
							readerIndex = readerIndex + LEN_LAT;
						}
						if(longdata[readerIndex] == MARKER_LON){
							readerIndex++;
							int[] newLon = new int[LEN_LON];
							System.arraycopy(longdata,readerIndex,newLon,0,LEN_LON);
							longi = String.valueOf(convertToDecimal(newLon));
							readerIndex = readerIndex + LEN_LON;
						}
						if(longdata[readerIndex] == MARKER_ALT){
							readerIndex++;
							int[] newAlt = new int[LEN_ALT];
							System.arraycopy(longdata,readerIndex,newAlt,0,LEN_ALT);
							alt = String.valueOf(convertToDecimal(newAlt));
							readerIndex = readerIndex + LEN_ALT;
						}
						if(longdata[readerIndex] == MARKER_FLAG){
							readerIndex++;
							//flag = String.valueOf([15]);
							readerIndex = readerIndex + LEN_FLAG;
						}
						//else throw new ArrayIndexOutOfBoundsException();
						if (readerIndex != longdata.length) {
							System.out.println(readerIndex);
							System.out.println("Packet reader error.");
							//TODO throw error
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
