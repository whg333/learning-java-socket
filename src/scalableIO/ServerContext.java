package scalableIO;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServerContext {

	public static final boolean isLog = true;
	
	public static final boolean useThreadPool = false;
	
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	
	public static <T> Future<T> submit(Callable<T> task){
		return executor.submit(task);
	}
	
	public static <T> void execute(Runnable runnable){
		executor.execute(runnable);
	}
	
}
