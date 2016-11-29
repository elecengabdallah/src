/*
 * This program developed by Abd-Allah Farag
 * It is a simple program to communicate with Cognex DataMan 260 Barcode Reader
 * Apache Telnet API is used here according to Apache license
 * 
 * Features: 
 * 1- sign in and start automatically
 * 2- get MAC address and record data in MySQL with MAC
 * 3- MUST:: the barcode reader output string must be in this format "C:<Full String><total reads><total no reads><CR/LF>"
 * Developed in 09/06/2016 11:00 PM
 * Version 0.7
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;




public class BCReader implements TelnetNotificationHandler {
	
	
	private InputStream in;
	private OutputStream out;
	private TelnetClient telnetSocket;
	private BCReaderGUI gui = null;
//	public  DataRecorder dataRecorder;
	public  DataAnalyserAndRecorder dataAnalyserAndRecorder;
	
	private String serverAddress = null;
	private int serverPort = 0;
	
	private String MAC;
	
	private String name = "MACHINE NAME"; 
	
	
	
	/***
	 * BCReader constructor
	 * @param serverAddress
	 * @param serverPort
	 * @param gui
	 */
	BCReader(String serverAddress, int serverPort, BCReaderGUI gui) {
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.gui = gui;		
	}
	
	
	@Override
	public void receivedNegotiation(int negotioation_code, int option_code) {
		String command = null;
		switch (negotioation_code) {
		case TelnetNotificationHandler.RECEIVED_DO:
			command = "DO";
			break;
		case TelnetNotificationHandler.RECEIVED_DONT:
			command = "DONT";
			break;
		case TelnetNotificationHandler.RECEIVED_WILL:
			command = "WILL";
			break;
		case TelnetNotificationHandler.RECEIVED_WONT:
			command = "WONT";
			break;
		case TelnetNotificationHandler.RECEIVED_COMMAND:
			command = "COMMAND";
			break;
		default:
			command = Integer.toString(negotioation_code);
			break;
		}
		gui.append("Received " + command + " for option code " + option_code);
	}
	
	/***
	 * start the telnet communication
	 * @return true if communication started
	 */
	public boolean start() {
//		create telnet client socket object
		telnetSocket = new TelnetClient();
		
//		set option handlers
		TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
		EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
		SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
		
//		try registering option handlers
		try {
			telnetSocket.addOptionHandler(ttopt);
			telnetSocket.addOptionHandler(echoopt);
			telnetSocket.addOptionHandler(gaopt);
		} catch (InvalidTelnetOptionException e) {
			gui.append("Error registering option handlers1: " + e.getMessage());
		} catch (IOException e) {
			gui.append("Error registering option handlers2: " + e.getMessage());
		} 
		
//		try to connect Barcode Reader
		try {
			telnetSocket.connect(serverAddress, serverPort);
			/*
			 * add this code to set timeout for 2 min and set keep alive flag
			 */
			telnetSocket.setSoTimeout(120000); 
			telnetSocket.setKeepAlive(true);
			/*
			 * end of added code
			 */
			gui.append("Connected to Barcode Reader");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			gui.append("Error connecting to Baracode Reader1: " + e.getMessage());
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			gui.append("Error connecting to Baracode Reader2: " + e.getMessage());
			return false;
		}
		
//		try to get input and output streams
		try {
			in = telnetSocket.getInputStream();
			out = telnetSocket.getOutputStream();
			gui.append("Input/output streams Created");
		} catch (Exception ioE) {
			gui.append("Error creating input/output streams");
			try {
				telnetSocket.disconnect();
			} catch (IOException e){}
			return false;
		}		
		
//		dataRecorder = new DataRecorder(gui);
		dataAnalyserAndRecorder = new DataAnalyserAndRecorder(name, gui);
		
//		modified*******************
//		send admin as user name and no password then get the device MAC address;
		
		sendMsg("admin\r\n");
		sendMsg("\r\n");
		sendMsg("||>get device.mac-address\r\n");
		
		new ListenFromBCReader().start();

		return true;
	}

	
	/***
	 * send messages to Barcode Reader
	 * @param msg input ... string to be sent.
	 */
	public void sendMsg(String msg) {
		
		byte[] outstr = new byte[1024];
		int outstrLength = 0;
		
		outstr = msg.getBytes();
		outstrLength = outstr.length;
		
		try {
			out.write(outstr, 0, outstrLength);
			out.flush();
		} catch (Exception e) {
			gui.append("Error sending msg to server!!!!");
		}
	}
	
	/***
	 * disconnect method if error occurs or stop button pressed
	 */
	public void disconnect() {
		
		try {
			telnetSocket.disconnect();
//			dataRecorder.disconnect();
			dataAnalyserAndRecorder.disconnect();
			
			gui.append("Communication disconnected.");
		} catch (Exception e) {
			gui.append("Error disconnecting communication!!!");
			e.printStackTrace();
		}
		
	}
	
	// Listening thread gets data from Barcode Reader
	class ListenFromBCReader extends Thread{

		public void run() {
			
			BufferedReader bIn = new BufferedReader(new InputStreamReader(in));
			
			String str = null;
			try {
				do {
					
					str = bIn.readLine();
					
					if(str.startsWith("C:")){
						if(MAC == null)
							continue;
						else {
//							dataRecorder.analyseData(str.substring(2), MAC);
							dataAnalyserAndRecorder.analyseData(str.substring(2));
//						    gui.append(str);
						    continue;
						}
					}
					
					if(str.contains("Username: ") || str.contains("Password: ") || str.contains("Login succedded")) {
						gui.append(str);
						continue;
					}
					
					if(str.contains("-") && MAC == null) {
						MAC = str;
						gui.append(MAC);
						continue;
					}
					
					gui.append(str);
					
				} while (true);
			} catch (Exception e) {
				gui.append("Error while reading from Barcode Reader: " + e.getMessage());
				gui.setStatus(BCReaderGUI.ERROR);
				e.printStackTrace();
			}
			
			try {
				disconnect();
			} catch(Exception e) {
				gui.append("Error disconnecting from listening thread");
			}
		}
	}

}

