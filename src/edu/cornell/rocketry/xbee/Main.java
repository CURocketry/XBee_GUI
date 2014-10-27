package edu.cornell.rocketry.xbee;

import com.rapplogic.xbee.api.XBeeException;

public class Main {

	/**
	 * Entry point of the program
	 * @param args
	 * @throws XBeeException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws XBeeException, InterruptedException {

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new XBeeListenerGui().setVisible(true);
			}
		});
	}
}
