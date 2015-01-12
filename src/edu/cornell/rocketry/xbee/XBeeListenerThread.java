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
					IncomingPacket packet = new IncomingPacket(longdata);
					mainWindow.updateData(packet.getLatitude(), packet.getLongitude(), packet.getAltitude(), packet.getFlag());

					//System.out.println(longdata.length);
					String data="";
					for (int i=0;i<longdata.length;i++){
						data+=longdata[i];
					}
					mainWindow.incNumRec();
					mainWindow.addToReceiveText("Received (" + mainWindow.getNumRec() + "): "
							+ data);

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
