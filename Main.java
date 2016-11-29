import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

public class Main {
	
	
	BCReaderGUI gui = null;
	BCReader bcreader = null;
	
	Main() {
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					gui = new BCReaderGUI();
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void start() {
		
		while(true) {
		
			while(gui.clickStart() == BCReaderGUI.FAILED_START) {
				
				gui.clickStop();
				try {
					Thread.sleep(300000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			while(gui.getStatus() == BCReaderGUI.SUCCESS_STATRT) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			gui.clickStop();
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Main m = new Main();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m.start();
		

	}

}
