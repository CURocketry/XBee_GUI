// License: GPL. For details, see LICENSE file.
package edu.cornell.rocketry.xbee;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;

public class XBeeListenerThread extends Thread {

	private boolean keepListening;
	private XBeeListenerGui mainWindow;

	public XBeeListenerThread(XBeeListenerGui gui) {
		mainWindow = gui;
		keepListening = true;
	}
	
	public void stopListening() { keepListening = false; }
	public void startListening() {keepListening = true; }
	
	@Override
	public void run() {
		while (keepListening) {
			try {
				XBeeResponse response = mainWindow.xbee.getResponse();
				
				if (response.getApiId() == ApiId.ZNET_RX_RESPONSE) {

					ZNetRxResponse ioSample = (ZNetRxResponse) response;
					IncomingPacket packet = new IncomingPacket(ioSample);
					
					mainWindow.updateData(packet.getLatitude(), packet.getLongitude(), packet.getAltitude(), packet.getFlag());
					
					mainWindow.addToGuiLog(packet.toString(), logMsgType.RECEIVED);

				}
			} 
			catch (XBeeTimeoutException e) {
				System.out.println("timeout");
				// we timed out without a response
			} catch (XBeeException e) {
				mainWindow.addToGuiLog("XBee Problem: "+ e.getMessage(), logMsgType.ERROR);
				e.printStackTrace();
			}
		}
	}

}
