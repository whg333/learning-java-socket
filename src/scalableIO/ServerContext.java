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
	public static final int port = 9003;
	
	private static final boolean useMultipleReactors = true;
	//public static final boolean useReactorPool = true;
	private static final int subReactorLength = 3;
	public static final long selectTimeOut = TimeUnit.MILLISECONDS.toMillis(10);
	private static final AtomicLong nextIndex = new AtomicLong();
	
	private static ServerSocketChannel serverChannel;
	private static Reactor mainReactor;
	private static Reactor[] subReactors;
	
	public static final boolean useThreadPool = true;
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	
	public static <T extends Reactor> void start(Class<T> clazz){
		try{
			serverChannel = ServerSocketChannel.open();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		try {
			Constructor<T> constructor = clazz.getConstructor(int.class, ServerSocketChannel.class, boolean.class, boolean.class, long.class);
			mainReactor = constructor.newInstance(port, serverChannel, true, useMultipleReactors, selectTimeOut);
			mainReactor.init();
			
			if(useMultipleReactors){
				subReactors = new Reactor[subReactorLength];
				for(int i=0;i<subReactors.length;i++){
					subReactors[i] = constructor.newInstance(port, serverChannel, false, useMultipleReactors, selectTimeOut);
					subReactors[i].init();
				}
			}
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} 
		
		mainReactor.start();
		if(useMultipleReactors){
			for(Reactor subReactor:subReactors){
				subReactor.start();
			}
		}
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
