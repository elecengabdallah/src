import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;


public class DataRecorder {
	
	Calendar calendar;
	Timer timer;
	TimerTask timerTask;
	
	String datePattern = "yyyy-MM-dd";
	String timePattern = "HH-mm-ss";
	
	long period = 86400000; // 24 Hr in milliseconds
//	long period = 120000;  // 2 mins for test
	
	PrintWriter outLOG = null;
	PrintWriter outTotal = null;
	
	int total = 0;
	int badCode = 0;
	
	BCReaderGUI gui;
	
	ArrayList<BCRLastProduct> productAL = new ArrayList<BCRLastProduct>();
	
	String lastGoodCode = null;
	int index = 0;

	
	/***
	 * construct and initialize database 
	 * need to create new table every day and run timer every 24hrs to create new table 
	 * and a timer for every bad code detected to help in correction 
	 * @param userName
	 * @param password
	 * @param gui
	 */
	public DataRecorder(BCReaderGUI gui) {		
		
		this.gui = gui;
		
		calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		Date firstTime = new Date();
		firstTime = calendar.getTime();
		
		timerTask = new TimerTask() {
			
			@Override
			synchronized public void run() {
				if(outLOG == null && outTotal == null) {
					createFiles();
				}
				
				else {
					disconnect();
					createFiles();
				}
			}
		};
		
		timer = new Timer(true);
		timer.scheduleAtFixedRate(timerTask, firstTime, period);
		
	}
	
	synchronized public void createFiles() {
		
		Long currentTime = System.currentTimeMillis();
		
		String dateString = dateTimeFormat(datePattern, currentTime);
		String timeString = dateTimeFormat(timePattern, currentTime);
		
		try {
			outLOG = new PrintWriter(new FileWriter("logFile " + dateString + " " + timeString +".txt"));
			outLOG.println("Start: " + dateString + " " + timeString);
			outLOG.flush();
			
			outTotal = new PrintWriter(new FileWriter("totalFile " + dateString + " " + timeString + ".txt"));
			outTotal.println("Start: " + dateString + " " + timeString);
			outTotal.flush();
			
		}catch (Exception e){
			gui.append("Error creating File");
		}
		
	}
	
	private String dateTimeFormat(String pattern, long time) {
		
		SimpleDateFormat sdtf = new SimpleDateFormat(pattern);
		return sdtf.format(new Date(time));
	}
	
	
	/***
	 * analyse data if the string has new code updates the total and correct the last bad one 
	 * @param string
	 */
	synchronized public void analyseData(String string, String MAC) {
		
		boolean found = false;
		index = 0;
		total = 0;
		badCode = 0;
		long currentTime = System.currentTimeMillis();
		
		for(BCRLastProduct e:productAL) {
			if(e.bcrMAC.equals(MAC)) {
				lastGoodCode = e.lastProduct;
				found = true;
				total = e.lastTotal;
				if(total == 0) {
					e.firstTime = currentTime;
				}
				e.lastTime = currentTime;
				break;
			};
			index++;
		}
		
		if(!found) {
			productAL.add(new BCRLastProduct(MAC, null, currentTime));
			lastGoodCode = null;
			
			
		}
		
		outLOG.println(string);
		outLOG.flush();
		
		StringTokenizer st = new StringTokenizer(string);
		int count = st.countTokens();
		
		if(count == 3) {
			
			String firstToken = st.nextToken();
			
			if(lastGoodCode == null) {
				productAL.get(index).lastProduct = firstToken;
				lastGoodCode = firstToken;
				
				if(productAL.get(index).lastBad > 0) {
					total = (++productAL.get(index).lastTotal) + productAL.get(index).lastBad;
					productAL.get(index).lastTotal = total;
					productAL.get(index).lastBad = 0;
				}
				else 
					total = (++productAL.get(index).lastTotal);
				
			}
			
			else if(lastGoodCode.equals(firstToken)) {
				
				if(productAL.get(index).lastBad > 0) {
					total = (++productAL.get(index).lastTotal) + productAL.get(index).lastBad;
					productAL.get(index).lastTotal = total;
					productAL.get(index).lastBad = 0;
				}
				else 
					total = (++productAL.get(index).lastTotal);
				
			}
			
			else if(!lastGoodCode.equals(firstToken)) {
				
//				this section add the last total values to totalFile
				outTotal.println(lastGoodCode + "\t" + total + "\t" + badCode + "\t" + "corrected total value:\t" + (total + badCode) + 
						         "\t" +"start time:" + "\t" + dateTimeFormat(timePattern, productAL.get(index).firstTime)
						         + "\t" + "stop time:" + "\t" + dateTimeFormat(timePattern, productAL.get(index).lastTime) );
				outTotal.flush();
//				end of added code
				
				
				lastGoodCode = firstToken;
				if(productAL.get(index).lastBad > 0) {
					total = productAL.get(index).lastBad + 1;
					productAL.get(index).lastBad = 0;
				}
				else 
					total = 1;
				productAL.get(index).lastProduct = firstToken;
				productAL.get(index).lastTotal = total;
			}
			
		}
		
		else if(count == 2) {
			badCode = ++productAL.get(index).lastBad;
		}
		
		if(gui == null) {
			System.out.println(lastGoodCode + " " + total + " " + badCode); 
			}
		else gui.updateTotalPanel(lastGoodCode, total, badCode);
	}

	
	synchronized public void disconnect() {
		
		if(lastGoodCode != null) {
			outTotal.println(lastGoodCode + "\t" + total + "\t" + badCode + "\t" + "corrected total value:\t" + (total + badCode) + 
			         "\t" +"start time:" + "\t" + dateTimeFormat(timePattern, productAL.get(index).firstTime)
			         + "\t" + "stop time:" + "\t" + dateTimeFormat(timePattern, productAL.get(index).lastTime) );
			outTotal.flush();
			productAL.get(index).lastTotal = 0;
			
			
		}
		
		outLOG.flush();
		outLOG.close();
		outTotal.close();
		
	}

}
