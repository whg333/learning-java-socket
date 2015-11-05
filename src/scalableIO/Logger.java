package scalableIO;

import static scalableIO.ServerContext.isLog;

public class Logger {

	public static void log(String info){
		if(isLog){
			System.out.println(info);
		}
	}
	
}
