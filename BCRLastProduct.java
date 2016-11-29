public class BCRLastProduct {
	

	String bcrMAC;
	String lastProduct;
	int lastTotal = 0;
	int lastBad = 0;
	long firstTime;
	long lastTime;
	
	BCRLastProduct(String mac , String code, long time) {
		bcrMAC = mac;
		lastProduct = code;
		firstTime = time;
		lastTime = time;
	}
	
}