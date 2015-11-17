package scalableIO;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import scalableIO.reactor.Reactor;

public class ServerContext{

	public static final boolean isLog = true;
	public static final int porn = 9005;
	
	public static final boolean useThreadPool = true;
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	
	public static final boolean useReactorPool = true;
	private static final int subReactorLength = 3;
	public static final long selectTimeOut = TimeUnit.MILLISECONDS.toMillis(10);
	private static final AtomicLong nextIndex = new AtomicLong();
	
	private static ServerSocketChannel serverChannel;
	static{
		try {
			serverChannel = ServerSocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static Reactor mainReactor;
	private static Reactor[] subReactors = new Reactor[subReactorLength];
	
	public static <T extends Reactor> void start(Class<T> clazz, int port){
		Constructor<T> constructor = null;
		try {
			constructor = clazz.getConstructor(int.class);
			mainReactor = constructor.newInstance(port);
			mainReactor.configure();
			
			for(int i=0;i<subReactors.length;i++){
				subReactors[i] = constructor.newInstance(port);
				subReactors[i].configure();
			}
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} 
		
		mainReactor.start();
		for(int i=0;i<subReactorLength;i++){
			subReactors[i].start();
		}
	}
	
	public static ServerSocketChannel serverChannel(){
		return serverChannel;
	}
	
	public static Reactor mainReactor(){
		return mainReactor;
	}
	
	public static boolean isMainReactor(Reactor reactor){
		return reactor == mainReactor;
	}
	
	public static Reactor nextSubReactor(){
		long nextIndexValue = nextIndex.getAndIncrement();
		if(nextIndexValue < 0){
			nextIndex.set(0);
			nextIndexValue = 0;
		}
		return subReactors[(int) (nextIndexValue%subReactors.length)];
	}
	
	/**
	 * 必须加上同步，确保所有的Selector都被安全的唤醒
	 */
//	public synchronized static void wakeupAll(){
//		mainReactor.wakeup();
//		for(Reactor subReactor:subReactors){
//			subReactor.wakeup();
//		}
//	}
	
	public static <T> Future<T> submit(Callable<T> task){
		return executor.submit(task);
	}
	
	public static <T> void execute(Runnable runnable){
		executor.execute(runnable);
	}
	
}
