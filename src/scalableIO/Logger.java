package scalableIO;

public class Logger {

	public static final boolean isLog = true;
	
	public static void log(String info){
		if(isLog){
			System.out.println(info);
		}
	}
	
}
