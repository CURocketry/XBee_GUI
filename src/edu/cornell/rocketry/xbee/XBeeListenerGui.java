package edu.cornell.rocketry.xbee;

import gnu.io.CommPortIdentifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;

import org.apache.log4j.Logger;


/**
 * An instance of XBeeListenerGui contains the GUI
 */
public class XBeeListenerGui extends javax.swing.JFrame {
	private static final long serialVersionUID = -4915109019152721192L;
	public static final Integer[] baudRates = {4800, 9600, 19200, 38400, 57600, 115200};
	public static final String[] addresses = { 
		"1: 0013A200 / 40BF5647", 
		"2: 0013A200 / 40BF56A5",
		"3: 0013A200 / 409179A7",
		"4: 0013A200 / 4091796F"
	};
	public static final XBeeAddress64 addr[] = {
		  new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0xbf, 0x56, 0x47),	//long cable
		  new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0xbf, 0x56, 0xa5),	//new xbees, small cable
		  new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x91, 0x79, 0xa7),
		  new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x91, 0x79, 0x6f)
	};
	private int selectedBaud = 57600; //serial comm rate
	private XBeeAddress64 selectedAddress;				//selected address
	private XBeeListenerThread xbeeListener;
	public XBee xbee = new XBee(); //keep as public reference @see XBeeListenerThread.java
	
	private int numRec = 0; 	//number received packets
	private int numSent = 0;	//number sent packets
	private int numErr = 0; 	//number error packets

	private JLabel packetLabel;
	private JLabel nLabel;

	private JTextArea receiveText;
	private JTextArea rocketText, payloadText;
	private JTextField sendEdit;
	private final static Font titleFont = new Font("Arial", Font.BOLD, 20);
	private final static Font textAreaFont = new Font("Arial", Font.PLAIN, 10);

	private JComboBox<String> serialPortsList, addressesList;
	private JComboBox<Integer> baudList;
	
	private JPanel fullPanel, statusPanel, dataPanel, tablePanel;
	private static JLabel lat,longi,alt,flag;
	
	
	private static Logger log = Logger.getLogger(XBeeListenerGui.class.getName());
	
	
	/* Getters and Setters for packet counters*/
	public int getNumSent() { return numSent;}
	public void incNumSent() { numSent++; }
	public int getNumRec() {return numRec;}
	public void incNumRec() { numRec++; }
	public int getNumError() {return numErr;}
	public void incNumError() { numErr++; }
	public void resetPacketCounters() { numSent=0; numRec=0; numErr=0; }

	/**
	 * Constructor 
	 */
	public XBeeListenerGui() {

		PropertyConfigurator.configure("./lib/log4j.properties");

		// Layout GUI
		JPanel fullPanel = new JPanel(new BorderLayout());
		setContentPane(fullPanel);

		/*-----------------------------Setup XBees Panel----------------------------*/
		JPanel xbeeInitPanel = new JPanel(new BorderLayout());
		JLabel xbeeInitLabel = new JLabel("Setup XBees", JLabel.CENTER);
		xbeeInitLabel.setFont(titleFont);
		xbeeInitPanel.add(xbeeInitLabel, BorderLayout.NORTH);
		JPanel xbeeInitGrid = new JPanel(new GridLayout(5, 2));
		
		//XBee Serial Port Label
		JPanel serialPortPanel = new JPanel(new BorderLayout());
		serialPortPanel.add(new JLabel("GS XBee Serial Port: "), BorderLayout.WEST);

		//Serial port dropdown
		serialPortsList = new JComboBox<String>(); //initialize empty dropdown
		updateSerialPortsList();
		serialPortsList.setSelectedIndex(serialPortsList.getItemCount() - 1);

		//Refresh serial ports button
		serialPortPanel.add(serialPortsList, BorderLayout.CENTER);
		JButton refreshPortsBtn = new JButton("Refresh");
		refreshPortsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSerialPortsList();
			}
		});
		serialPortPanel.add(refreshPortsBtn, BorderLayout.EAST);
		xbeeInitGrid.add(serialPortPanel);

		//Wireless Address Dropdown
		JPanel addressPanel = new JPanel(new BorderLayout());
		addressPanel.add(new JLabel("Remote XBee Address: "), BorderLayout.WEST);
		addressesList = new JComboBox<String>(addresses);
		addressesList.setSelectedIndex(0);
		selectedAddress = addr[addressesList.getSelectedIndex()]; //set default address
		addressesList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedAddress = addr[addressesList.getSelectedIndex()]; //set active address
			}
		});
		addressPanel.add(addressesList, BorderLayout.CENTER);
		xbeeInitGrid.add(addressPanel);
		
		//Baud rate dropdown
		JPanel baudPanel = new JPanel(new BorderLayout());
		baudPanel.add(new JLabel("XBee Baud Rate: "), BorderLayout.WEST);
		baudList = new JComboBox<Integer>(baudRates);
		baudList.setSelectedIndex(4);
		selectedBaud = (int) baudList.getSelectedItem(); //set default address
		addressesList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedBaud = (int) baudList.getSelectedItem(); //set active address
			}
		});
		baudPanel.add(baudList, BorderLayout.CENTER);
		xbeeInitGrid.add(baudPanel);
		

		//Initialize GS XBee Button
		JButton initXBeeButton = new JButton("Initialize GS XBee");
		initXBeeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					initXbee();
					addToReceiveText("Success! Initialized GS XBee :)");
					addToReceiveText("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -"
							+ System.getProperty("line.separator"));
				} catch (XBeeException e1) {
				e1.printStackTrace();
					numErr++;
					addToReceiveText("Error ("
							+ numErr
							+ "): Could not connect to XBee :( make sure port isn't being used by another program (including this one)!");
				}
			}
		});
		xbeeInitGrid.add(initXBeeButton);
		xbeeInitPanel.add(xbeeInitGrid, BorderLayout.CENTER);

		
		//Send Packet Title and Button
		JPanel sendPacketsPanel = new JPanel(new BorderLayout());
		JPanel sendPacketsGrid = new JPanel(new GridLayout(5, 2));
		JLabel sendTitle = new JLabel("Send Packets", JLabel.CENTER);
		sendTitle.setFont(titleFont);
		sendPacketsPanel.add(sendTitle, BorderLayout.NORTH);

		//Test Send Button
		JButton testSendBtn = new JButton("Send Test");
		testSendBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendXBeePacket("(Test Packet)");
			}
		});
		sendPacketsGrid.add(testSendBtn);
		
		//Send custom data box
		JButton customDataBtn = new JButton("Send Data");
		customDataBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendXBeePacket(sendEdit.getText());
			}

		});

		sendPacketsGrid.add(customDataBtn, BorderLayout.CENTER);
		
		//Send Custom Packet Textbox
		JPanel customPacketEntry = new JPanel(new BorderLayout());
		customPacketEntry.add(new JLabel("Send Packet: "), BorderLayout.WEST);
		sendEdit = new JTextField("", 20);
		customPacketEntry.add(sendEdit, BorderLayout.CENTER);
		sendPacketsGrid.add(customPacketEntry,BorderLayout.SOUTH);
		
		sendPacketsPanel.add(sendPacketsGrid, BorderLayout.CENTER);
		
		JPanel PContainer = new JPanel(new BorderLayout());
		PContainer.add(xbeeInitPanel, BorderLayout.NORTH);
		PContainer.add(sendPacketsPanel, BorderLayout.CENTER);

		/*----------------------------Received Packets Panel-----------------------------*/
		JPanel receivePanel = new JPanel(new BorderLayout());
		receiveText = new JTextArea(40, 60);
		receiveText.setBackground(Color.white);
		receiveText.setFont(textAreaFont);
		receiveText.setLineWrap(true);
		receiveText.setWrapStyleWord(true);
		receiveText.setEditable(false);
		JScrollPane receiveScrollPlane = new JScrollPane(receiveText);

		JLabel receiveTitle = new JLabel("Received Packets", JLabel.CENTER);
		receiveTitle.setFont(titleFont);
		receivePanel.add(receiveTitle, BorderLayout.NORTH);
		receivePanel.add(receiveScrollPlane,BorderLayout.EAST);
		
		/*-----------------Status Panel------------------*/
		statusPanel = new JPanel();
		JLabel statusTitle = new JLabel ("STATUS",JLabel.LEFT);
		statusTitle.setFont(titleFont);
		statusPanel.add(statusTitle);
		
		dataPanel = new JPanel (new BorderLayout());
		tablePanel = new JPanel (new GridLayout(3,5));
		JLabel rocketTitle = new JLabel ("Rocket",JLabel.LEFT);
		rocketTitle.setFont(titleFont);
		JLabel payloadTitle = new JLabel ("Payload",JLabel.LEFT);
		payloadTitle.setFont(titleFont);
		JLabel latTitle = new JLabel ("Latitude",JLabel.LEFT);
		latTitle.setFont(titleFont);
		JLabel longTitle = new JLabel ("Longitude",JLabel.LEFT);
		longTitle.setFont(titleFont);
		JLabel altTitle = new JLabel ("Altitude",JLabel.LEFT);
		altTitle.setFont(titleFont);
		JLabel enableTitle = new JLabel ("Enabled (Yes/No)",JLabel.LEFT);
		enableTitle.setFont(titleFont);

		tablePanel.add(new JLabel("", JLabel.LEFT));
		tablePanel.add(latTitle);
		tablePanel.add(longTitle);
		tablePanel.add(altTitle);
		tablePanel.add(enableTitle);
		tablePanel.add(rocketTitle);	
		lat = new JLabel("0", JLabel.LEFT);
		tablePanel.add(lat);
		longi = new JLabel("0",JLabel.LEFT);
		tablePanel.add(longi); 
		alt = new JLabel("0",JLabel.LEFT);
		tablePanel.add(alt);
		flag = new JLabel("-",JLabel.LEFT);
		tablePanel.add(flag);
		tablePanel.add(payloadTitle);
		tablePanel.add(new JLabel("N/A", JLabel.LEFT));
		tablePanel.add(new JLabel("N/A", JLabel.LEFT));
		tablePanel.add(new JLabel("N/A", JLabel.LEFT));
		tablePanel.add(new JLabel("N/A", JLabel.LEFT));	
		
		dataPanel.add(statusPanel, BorderLayout.NORTH);
		dataPanel.add(tablePanel, BorderLayout.SOUTH);

		fullPanel.add(dataPanel, BorderLayout.SOUTH);
		
		fullPanel.add(PContainer,BorderLayout.WEST);
		fullPanel.add(receivePanel,BorderLayout.CENTER);
		

		
		//Main window props
		setTitle("XBee Coordinator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setVisible(true);

		// Text area stuff...

	}

	public void initXbee() throws XBeeException {

		// get selected serial port...
		String selSerial = (String) serialPortsList.getSelectedItem();

		if (xbee != null && xbee.isConnected()) {
			xbee.close();
			xbeeListener.stopListening();
		}
		

		System.out.println(selSerial);
		xbee.open(selSerial, selectedBaud); //open port
		xbeeListener = new XBeeListenerThread(this); //init a new listener thread
		xbeeListener.start();

		resetPacketCounters();
	}
	
	public boolean sendXBeePacket(String msg) {
		OutgoingPacket payload = new OutgoingPacket(OutgoingPacketType.TEST);
		try {
			XBeeSender mailman = new XBeeSender(xbee, selectedAddress, payload);
			mailman.send();
			addToReceiveText("Sent (" + numSent + "): " + msg);
			return true;
		}
		catch (XBeeSenderException e) {
			addToReceiveText("Error (" + numErr + "): " + e.getMessage());
			incNumError();
			return false;
		}
	}
	
	
	//get updated data from XBee and display it
	public void updateData (String updateLat, String updateLongi, String updateAlt, String updateFlag) {
		lat.setText(""+updateLat);
		longi.setText(""+updateLongi);
		alt.setText(""+updateAlt);
		flag.setText(""+updateFlag);
	}

	
	/**
	 * updated the Serial Port List (i.e. after a refresh)
	 * @void
	 */
	public void updateSerialPortsList() {
		ArrayList<String> comboBoxList = new ArrayList<String>();
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();// this line was false
		while (portList.hasMoreElements()) {
			CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				comboBoxList.add(portId.getName());
				// System.out.println(portId.getName());
			} else {
				// System.out.println(portId.getName());
			}
		}

		// update list...
		serialPortsList.removeAllItems();
		for (String s : comboBoxList) {
			serialPortsList.addItem(s);
		}
	}

	/**
	 * Adds text to the Received Packets Box
	 * @param txt			text to add
	 */
	public void addToReceiveText(String txt) {
		receiveText.setText(receiveText.getText() + "- " + txt + System.getProperty("line.separator"));
		receiveText.setCaretPosition(receiveText.getDocument().getLength()); // locks scroll at bottom
		logMessage(txt);
	}
	
	/**
	 * Write a message to the log file
	 * @param msg			msg to write
	 */
	public void logMessage(String msg) {
		log.info(msg);
	}

}