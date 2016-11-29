/*
 * This update in 18th, Nov., 2016 :
 * 1- add timer to record every 8 hrs
 */

/*
 * this file have two problems:
 * 1- in case of new month it writes in the new month file not the last month
 * 2- in case of stop dosent write the last total
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class DataAnalyserAndRecorder {
	
	private Calendar calendar;
	private Timer timer;
	private TimerTask timerTask;
	
//	added code
	private Timer shiftTimer;
	private TimerTask shiftTimerTask;
	private long shiftPeriod = 8 * 60 * 60 * 1000; // 8hrs in mSec
	private long shiftTotal;
	private String shiftCode;
	private int firstShiftHour = 8; // first shift is at 8 am by default
	private Calendar currentCalendar;
	private FileWriter shiftFileWriter;
	private long shiftProductStartTime;
	private Calendar shiftCalendar;
	
	private long period = 86400000; // 24 * 60 * 60 * 1000 24hrs in mSec
	
	private static String datePattern = "yyyy-MM-dd";
	private static String timePattern = "HH:mm:ss";
	private static String dateTimePattern = datePattern + "\t" + timePattern;
	private static String shortDatePattern = "yyyy-MM";
	
	private String machineName;
	private String code1 = "62230003533";
	private String code2 = "622201433";
	
	private FileWriter outLogFileWriter;
	private FileWriter outTotalFileWriter;
	
	private BCReaderGUI gui;
	
	private String lastCode = null;
	private long totalBad = 0;
	private long totalGood = 0;
	private long startTime;
	private long stopTime;
	
	private String tempCode = null;
	private long tempCodeCount = 0;
	private long tempCodeTime;
	private long tempBad = 0;
	
	
	/***
	 * Constructs data analyser and recorder object and set timer task to 
	 * close files every 24 hrs and creates new files.
	 * @param MAC
	 * @param gui
	 */
	public DataAnalyserAndRecorder(String machineName,BCReaderGUI gui) {
		
		this.machineName = machineName;
		this.gui = gui;
		
//		this added code to run timer every 8 hrs
		
		int nextShiftDifference = nextShiftTime();
		shiftCalendar = Calendar.getInstance();
		shiftCalendar.add(Calendar.HOUR, nextShiftDifference);
		shiftCalendar.set(Calendar.MINUTE, 0);
		shiftCalendar.set(Calendar.SECOND, 0);
		
		Date nextShiftStartTime = new Date();
		nextShiftStartTime = shiftCalendar.getTime();
		
		shiftTimerTask = new TimerTask() {
			
			@Override
			synchronized public void run() {
				
				updateShiftFile();
//				timerTask.notify();
				
			}
		};
		
		shiftTimer = new Timer(true);
		shiftTimer.scheduleAtFixedRate(shiftTimerTask, nextShiftStartTime, shiftPeriod);
		
		currentCalendar = Calendar.getInstance();
		
//---------------------------------------------------------------------------------------------------
		
		calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		Date firstTime = new Date();
		firstTime = calendar.getTime();
		
		timerTask = new TimerTask() {
			
			@Override
			synchronized public void run() {
				
				Long currentTime = System.currentTimeMillis();
				String currentDate = dateTimeFormat(datePattern, currentTime);
				String shortDate = currentDate.substring(0, 7);
				
				if(outLogFileWriter != null)
					disconnectLogFile();
				
				createLogFile(currentDate);
//				createTotalFile(shortDate);
			}
		};
		

		
		timer = new Timer(true);
		timer.scheduleAtFixedRate(timerTask, firstTime, period);
		

		
	}
	
	/***
	 * calculate the next shift hour
	 * @return int value represents the difference between the current day hour and next shift hour
	 */
	private int nextShiftTime() {
		String currentHourString = dateTimeFormat("HH", System.currentTimeMillis());
		int currentHour = Integer.valueOf(currentHourString);
		if(currentHour < firstShiftHour)
			return (8 - currentHour);
		else if(currentHour < firstShiftHour + 8)
			return (16 - currentHour);
		else
			return (24 - currentHour);
	}
	
	private synchronized void updateShiftFile() {
		if(lastCode != null) {
			if(shiftCode != null) {
				if(totalGood > shiftTotal) {
					shiftTotal = totalGood - shiftTotal;
					openShiftTotalFile();
					writeDataInShiftFile();
					closeShiftTotalFile();
				}
			}
			else {
				shiftCode = lastCode;
				shiftTotal = totalGood;
				openShiftTotalFile();
				writeDataInShiftFile();
				closeShiftTotalFile();
			}
			
			shiftProductStartTime = System.currentTimeMillis();
		}
	}
	

	private void writeDataInShiftFile() {
		try {
			shiftFileWriter.write("StartTime:\t" + dateTimeFormat(dateTimePattern, shiftProductStartTime) + "\t" +
								   lastCode + "\t" + shiftTotal + "\t"+ 										
								   "StopTime:"+"\t" + dateTimeFormat(timePattern, stopTime) + "\r\n");
			shiftFileWriter.flush();
		} catch (IOException e) {
			
		}
	}
	private void openShiftTotalFile() {
		
		String date = shiftFileDate();
		
		File file = new File("shiftTotalFile " + machineName + " " + date + ".txt");
		try {
			shiftFileWriter = new FileWriter(file, true);
		} catch (IOException e) {
			
			gui.append("Error in shift file!!!");
		}
		
	}
	
	private void closeShiftTotalFile() {
		
		try {
			shiftFileWriter.flush();
			shiftFileWriter.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}		
	}
	
	private String shiftFileDate() {
		Calendar newCalendar = Calendar.getInstance();
		int newMonth = newCalendar.get(Calendar.MONTH);
//		int newYear = newCalnendar.get(Calendar.YEAR);
		int currentMonth = currentCalendar.get(Calendar.MONTH);
//		int currentYear = currentCalendar.get(Calendar.YEAR);
		String date;
		
		if(newMonth != currentMonth) {
			int newHour = newCalendar.getMaximum(Calendar.HOUR);
			if(newHour == 0) {
				date = dateTimeFormat(shortDatePattern, currentCalendar.getTimeInMillis());
//				date = currentCalendar.get
			}
			else
				date = dateTimeFormat(shortDatePattern, newCalendar.getTimeInMillis());
			
		}
		else
			date = dateTimeFormat(shortDatePattern, currentCalendar.getTimeInMillis());
		
		currentCalendar = newCalendar;
		return date;
		
	}
	
	private void codeChanged() {
		updateShiftFile();
		shiftCode = null;
	}
	
	/***
	 * Creates total files if not exist for the current month
	 */
	synchronized private void createTotalFile(String date) {
		
		File file = new File("totalFile " + machineName + " " + date + ".txt");
		boolean fileExists = file.exists();
		
		if(fileExists && outTotalFileWriter != null)
			return;
		
		if(!fileExists && outTotalFileWriter != null)
//			disconncetTotalFile();
		
		try {
			outTotalFileWriter = new FileWriter(file, true);
		} catch(Exception e) {
			gui.append("Error creating files");
		}
		
	}
	
	
	/***
	 * create log file if not exist for the same day
	 * 
	 */
	synchronized private void createLogFile(String date) {
		
		try {
			outLogFileWriter = new FileWriter(new File("logFile " + machineName + " " + date + ".txt"), true);
		} catch(Exception e) {
			gui.append("Error creating log file");
		}
	}
	
//	/***
//	 * Close total file writer object
//	 */
//	synchronized private void disconncetTotalFile() {
//		if(lastCode != null) {
//			try {
//				outTotalFileWriter.write("StartTime:\t" + dateTimeFormat(dateTimePattern, startTime) + "\t" +
//										lastCode + "\t" + totalGood + "\t" + totalBad + "\t"+
//										"corrected total value:\t" + (totalGood + totalBad) + "\t" + 										
//										"StopTime:"+"\t" + dateTimeFormat(timePattern, stopTime) + "\r\n");
//				outTotalFileWriter.flush();
//				outTotalFileWriter.close();
//				
//			} catch (Exception e) {
//				gui.append("Error closing total file writer object");
//			}
//			
////			try {
////				timerTask.wait();
////			} catch (InterruptedException e) {
////				
////				e.printStackTrace();
////			}
//			totalBad = 0;
//			totalGood = 0;
//			lastCode = null;
//		}
//	}
	
	/***
	 * Close log file writer object
	 */
	synchronized private void disconnectLogFile() {
		try {
			outLogFileWriter.flush();
			outLogFileWriter.close();
		} catch(Exception e) {
			gui.append("Error closing log file writer object");
		}
	}
	
	private void disconncetShiftTotalFile() {
		updateShiftFile();
	}
	
	public void analyseData(String string) {
		
		long time = System.currentTimeMillis();
		
		try {
			outLogFileWriter.write(dateTimeFormat(timePattern, time) + " " +
									string + "\r\n");
			outLogFileWriter.flush();
		} catch (IOException e) {
			gui.append("Error in log file writing");
		}
		
		StringTokenizer st = new StringTokenizer(string);
		int count = st.countTokens();
		
		if(count == 3) {
			
			String firstToken = st.nextToken();
//			stopTime = time;
			
			if(lastCode == null) {
				
				if(checkCode(firstToken)) {
					lastCode = firstToken;
					startTime = time;
					shiftProductStartTime = startTime;
					
					if(totalBad > 0) {
						totalGood = totalGood + 1 + totalBad;
						totalBad = 0;
					}
					else 
						totalGood++;
					
					stopTime = time;
				}
				
				else 
					totalBad++;
			}
			
			else if(lastCode.equals(firstToken)) {
				
				if(totalBad > 0) {
					totalGood = totalGood + totalBad + 1;
					totalBad = 0;
				}
				else
					totalGood++;
				
				if(tempCode != null)
					tempCode = null;
				
				stopTime = time;
			}
			
//			else if(!lastCode.equals(firstToken)) {
//				
//				if(checkCode(firstToken)) {
//					if(tempCode == null || !tempCode.equals(firstToken)) {
//						tempCode = firstToken;
//						tempCodeCount++;
//						totalBad++;
//						tempCodeTime = time;
//						gui.updateTotalPanel(lastCode, totalGood, totalBad);
//						return;
//					}
//					
//					else if(tempCode.equals(firstToken)) {
//						if(tempCodeCount > 10) {
//							totalBad--;
//							tempCodeCount = 0;
//							try {
//								outTotalFileWriter.write("StartTime:\t" + dateTimeFormat(dateTimePattern, startTime) + "\t" +
//														lastCode + "\t" + totalGood + "\t" + totalBad + "\t"+
//														"corrected total value:\t" + (totalGood + totalBad) + "\t" + 										
//														"StopTime:\t" + dateTimeFormat(timePattern, stopTime) + "\r\n");
//								outTotalFileWriter.flush();
//							
//							} catch (IOException e) {
//								gui.append("Error writing in totalFile");
//							}
//						}
//						else {
//							tempCodeCount++;
//							totalBad++;
//							return;
//						}
//						
//					}
//						
//					lastCode = firstToken;
//					startTime = tempCodeTime;
//					stopTime = time;
//					totalBad = 0;
//					totalGood = 2;
//					tempCode = null;
//				}
//				
//				else 
//					totalBad++;
//			}
			
			else if(!lastCode.equals(firstToken)) {
				
				if(checkCode(firstToken)) {
					if(tempCode == null) {
						tempBad = totalBad;
						tempCode = firstToken;
						tempCodeCount = 1;
						totalBad++;
						tempCodeTime = time;
						gui.updateTotalPanel(lastCode, totalGood, totalBad);
						return;
					}
					
					else {
						if(tempCode.equals(firstToken)) {
							tempCodeCount++;
							totalBad++;
							if(tempCodeCount > 10) {
								
//								codeChanged();
								
//								try {
//									outTotalFileWriter.write("StartTime:\t" + dateTimeFormat(dateTimePattern, startTime) + "\t" +
//															lastCode + "\t" + totalGood + "\t" + tempBad + "\t"+
//															"corrected total value:\t" + (totalGood + tempBad) + "\t" + 										
//															"StopTime:\t" + dateTimeFormat(timePattern, stopTime) + "\r\n");
//									outTotalFileWriter.flush();
//								
//								} catch (IOException e) {
//									gui.append("Error writing in totalFile");
//								}
								
								totalGood += tempBad;
								codeChanged();
								
								lastCode = tempCode;
								totalGood = totalBad - tempBad;
								startTime = tempCodeTime;
								stopTime = time;
								
								totalBad = 0;
								tempCodeCount = 0;
								tempCode = null;
							}
							
						}
						
						else {
							tempCode = firstToken;
							tempCodeTime = time;
							tempCodeCount = 1;
							totalBad++;
//							gui.updateTotalPanel(lastCode, totalGood, totalBad);
//							return;
						}
						
					}
				}
				
				else 
					totalBad++;
			}
				
		}
		
		else if(count == 2) {
			totalBad++;
		}
		
		gui.updateTotalPanel(lastCode, totalGood, totalBad);
	}
	
	/***
	 * Check if code starts with correct code and the code length is 13 digit
	 * @param code
	 * @return 
	 */
	private boolean checkCode(String code) {
		
		if(code.length() != 13)
			return false;
		
		if(code.startsWith(code1) || code.startsWith(code2))
			return true;
		else
			return false;
	}
	
	/***
	 * Close opened log and total file writer objects
	 */
	synchronized public void disconnect() {
		
		disconncetShiftTotalFile();
//		disconncetTotalFile();
		disconnectLogFile();
		
		
	}

	/***
	 * Generate formated date and time 
	 * @param pattern Required date and time format
	 * @param time Time in msec to be formated
	 * @return Formated date and time string
	 */
	private String dateTimeFormat(String pattern, long time) {
		SimpleDateFormat sdtf = new SimpleDateFormat(pattern);
		return sdtf.format(new Date(time));
	}
}
