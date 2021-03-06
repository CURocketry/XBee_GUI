package edu.cornell.rocketry.xbee;

import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;

/**
 * An instance of XBeeSender coordinates packet sending
 */
public class XBeeSender {
	
	private OutgoingPacket payload;
	private XBeeAddress64 destination;
	private XBee xbee;
	
	/**
	 * 
	 * @param x		local XBee instance
	 * @param a		remote XBee address
	 * @param p		outgoing packet type
	 */
	public XBeeSender(XBee x, XBeeAddress64 a, OutgoingPacket p) {
		payload = p;
		destination = a;
		xbee = x;
	}
	
	/**
	 * Send a packet to remote XBee
	 * @param r		packet to send
	 */
	public void send() throws XBeeSenderException{
		
		try {
			// send a request and wait up to 10 seconds for the response
			final ZNetTxRequest request = new ZNetTxRequest(destination, payload.getPayload());
			
			ZNetTxStatusResponse response = (ZNetTxStatusResponse) xbee.sendSynchronous(request,1000);
			//System.out.println(response.isSuccess());
			
			if (response.isSuccess()) {
				// packet was delivered successfully
				System.out.println("success");
			} else {
				// packet was not delivered
				throw new XBeeSenderException("Packet not delivered");
			}
			
		} catch (XBeeTimeoutException e) {
			// timeout after 10 seconds
			throw new XBeeSenderException("Packet delivery timed out");
			
			// no response was received in the allotted time

		} catch (XBeeException e) {
			e.printStackTrace();
			throw new XBeeSenderException("Packet not delivered, XBee Exception: " + e.getMessage());
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new XBeeSenderException("Java Error. Make sure XBee is initialized: " + e.getMessage());
		}

	}
}
