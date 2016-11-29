import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product {
	
	
	private SimpleStringProperty code;
	private SimpleLongProperty startTime;
	private SimpleLongProperty stopTime;
	private SimpleLongProperty totalGood;
	private SimpleLongProperty totalBad;
	
	public Product(String code, long time) {
		this.code = new SimpleStringProperty(code);
		this.startTime = new SimpleLongProperty(time);
		this.stopTime = new SimpleLongProperty(System.currentTimeMillis());
		this.totalGood = new SimpleLongProperty(0);
		this.totalBad = new SimpleLongProperty(0);
	}
	
	public String getCode() {
		return code.get();
	}
	
	public long getStartTime() {
		return startTime.get();
	}
	
	public void setStopTime(long time) {
		stopTime.set(time);
	}
	
	public long getStopTime() {
		return stopTime.get();
	}
	
	public void addTotalGood(int c) {
		totalGood.add(c);
	}
	
	public long getTotalGood() {
		return totalGood.get();
	}
	
	public void addTotalBad(int c) {
		totalBad.add(c);
	}
	
	public long getTotalBad() {
		return totalBad.get();
	}
}