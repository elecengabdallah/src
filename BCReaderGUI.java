/*
 * This program developed by Abd-Allah Farag
 * It is a simple program to communicate with Cognex DataMan 260 Barcode Reader
 * Apache Telnet API is used here according to Apache license
 * Developed in 15/10/2016 10:00 PM
 * Version 0.1
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
//import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
//import javax.swing.SwingUtilities;


@SuppressWarnings("serial")

public class BCReaderGUI extends JFrame implements ActionListener, WindowListener{
	
	private BCReader bcreader = null;
	
	
	private JLabel lPort, lAdd, lCmd, lCurrentCode, lTotalGood, lTotalBad;
	private JTextField tfPort, tfAdd, tfCmd, tfCurrentCode, tfTotalGood, tfTotalBad;
	private JTextArea taResults;
	private JButton btnStart, btnStop, btnSend;
	
	private int status;
	
	public static final int ERROR = 0;
	public static final int SUCCESS_STATRT = 1;
	public static final int FAILED_START = 2;
	public static final int STOP = 3;
	public static final int CMD = 4;
	public static final int AUTO_STOP = 5;
	public static final int NORMAL = 6;
	
	
	
	/**
	 * Constructor
	 * @param host
	 * @param port
	 */
	BCReaderGUI() {
//		set Frame title
		super("BarCodeReader");
		
//		set Frame to Exit on close
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
//		add window listener to perform system exit
		addWindowListener(this);
		
//		create connection data and start/stop communication panel 
		JPanel connectionPanel = new JPanel(new GridLayout(2,3));
		
		lPort = new JLabel("Port:");
		tfPort = new JTextField("2003"); // use 23 as default port
		
		lAdd = new JLabel("Address:");
		tfAdd = new JTextField("127.0.0.1"); // use 169.254.58.77 as default IP address
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(this);
		
		btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		btnStop.addActionListener(this);
		
		connectionPanel.add(lPort);
		connectionPanel.add(tfPort);
		connectionPanel.add(btnStart);
		connectionPanel.add(lAdd);
		connectionPanel.add(tfAdd);
		connectionPanel.add(btnStop);
		
//		create Text area performs as console
		JPanel resultsPanel = new JPanel();
		
		taResults = new JTextArea(20,80);
		resultsPanel.add(new JScrollPane(taResults));
		taResults.setEditable(true);
		
		
//		create commands text field panel
		JPanel cmdPanel = new JPanel();
		
		lCmd = new JLabel("cmd");
		tfCmd = new JTextField(60);
		tfCmd.setEditable(false);
		tfCmd.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				int i = e.getKeyCode();
				if(i == KeyEvent.VK_ENTER) {
					bcreader.sendMsg(tfCmd.getText() + "\r\n");
					tfCmd.setText("");
				}
				
			}
		});
		
		btnSend = new JButton("Send");
		btnSend.setEnabled(false);
		btnSend.addActionListener(this);
		
		cmdPanel.add(lCmd);
		cmdPanel.add(tfCmd);
		cmdPanel.add(btnSend);
		
//		create total reads panel
		JPanel totalPanel = new JPanel(new GridLayout(3,2));
		
		lCurrentCode = new JLabel("Product:");
		lTotalGood = new JLabel("Total:");
		lTotalBad = new JLabel("Bad Reads:");
		
		tfCurrentCode = new JTextField(15);
		tfTotalGood = new JTextField(7);
		tfTotalBad = new JTextField(7);
		
		totalPanel.add(lCurrentCode);
		totalPanel.add(tfCurrentCode);
		totalPanel.add(lTotalGood);
		totalPanel.add(tfTotalGood);
		totalPanel.add(lTotalBad);
		totalPanel.add(tfTotalBad);
		
//		add panels to main frame
		this.add(connectionPanel, BorderLayout.NORTH);
		this.add(resultsPanel, BorderLayout.CENTER);
		this.add(totalPanel, BorderLayout.EAST);
		this.add(cmdPanel, BorderLayout.SOUTH);
		
//		not resizable frame 
		this.setResizable(false);
		
		this.pack();
		this.setVisible(true);
		
	}
	
	/***
	 * writes strings to the Text Area (console)
	 * @param string input parameter ... the string to be written
	 */
	void append(String string) {
		
		taResults.append(string + "\n");
	}
	
	/***
	 * update total reads in total panel
	 * @param code
	 * @param totalGood
	 * @param totalBad
	 */
	void updateTotalPanel(String code, long totalGood, long totalBad) {
		tfCurrentCode.setText(code);
		tfTotalGood.setText(String.valueOf(totalGood));
		tfTotalBad.setText(String.valueOf(totalBad));
	}
	
	/***
	 * set buttons actions
	 * 
	 */
	
	@Override
	public void actionPerformed(ActionEvent e) {

		Object o = e.getSource();
		
//		Start button action
		if(o == btnStart) {
			
			taResults.setText("");
			tfCurrentCode.setText("");
			tfTotalGood.setText("");
			tfTotalBad.setText("");
			
			String tempAdd = tfAdd.getText().trim(); // get address text field data
			
			if(tempAdd.length() == 0) {
				append("Invalid IP Address");
				setStatus(BCReaderGUI.ERROR);
				return;
			}
				
			String tempPort = tfPort.getText().trim(); // get port text field data
			
			if(tempPort.length() == 0) {
				append("Invalid port n6o.!!!!");
				setStatus(BCReaderGUI.ERROR);
				return;
			}
			
			int tempPortInt;
			
			try {
				tempPortInt = Integer.parseInt(tempPort);
			} catch (Exception intConversion) {
				append("Invalid port no.!!!!");
				setStatus(BCReaderGUI.ERROR);
				return;
			}
			
			append("Start communication...");
			
			
			bcreader = new BCReader(tempAdd, tempPortInt, this);
			
//			starts the BCReader object and check the start status
			if(bcreader.start()) {
				
				append("object created....");
				btnStart.setEnabled(false);
				tfAdd.setEditable(false);
				tfPort.setEditable(false);
				btnStop.setEnabled(true);
				tfCmd.setEditable(true);
				tfCmd.requestFocus();
				btnSend.setEnabled(true);
				setStatus(BCReaderGUI.SUCCESS_STATRT);
			} else {
				append("Failed to communicate!!!");
				setStatus(BCReaderGUI.FAILED_START);
			}	
		}
		
//		Stop button action
		else if(o == btnStop) {
			append("Disconneting communication...");
			bcreader.disconnect();
			
			updateGUI();
			setStatus(BCReaderGUI.STOP);
		}
		
//		Send button action
		else if(o == btnSend) {
			String cmdString = tfCmd.getText();
			bcreader.sendMsg(cmdString + "\r\n");
			tfCmd.setText("");
//			setStatus(BCReaderGUI.CMD);
		}
		
	}
	
	
	/***
	 * return the GUI to initial state
	 */
	public void updateGUI() {
		
		btnStart.setEnabled(true);
		tfAdd.setEditable(true);
		tfPort.setEditable(true);
		btnStop.setEnabled(false);
		tfCmd.setText("");
		tfCmd.setEditable(false);
		btnSend.setEnabled(false);
	}
	
	
	public int clickStart() {
		btnStart.doClick();
		return getStatus();
	}
	
	public void clickStop() {
		btnStop.doClick();
		setStatus(BCReaderGUI.AUTO_STOP);
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return this.status;
	}

	/***
	 * main method
	 * @param args
	 */
//	public static void main(String[] args) {
//		try {
//			SwingUtilities.invokeAndWait(new Runnable() {
//				
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					new BCReaderGUI();
//				}
//			});
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}

	@Override
	public void windowOpened(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {
		
		bcreader.disconnect();
		System.exit(0);
	}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowDeactivated(WindowEvent e) {}
	
	/***
	 * when window close button pressed exits the system
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		
	}
}
