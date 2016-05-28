package scalableIO.reactor;

import static scalableIO.Logger.log;
import static scalableIO.ServerContext.nextSubReactor;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 连接接收者
 * <ul>
 * <li>处理TCP/IP网络的连接，子SubReactor即在此处做分配</li>
 * </ul>
 */
public abstract class Acceptor extends Thread {

	protected final Selector selector;
	protected final ServerSocketChannel serverChannel;
	protected final boolean useMultipleReactors;
	
	public Acceptor(Selector selector, ServerSocketChannel serverChannel, boolean useMultipleReactors){
		this.selector = selector;
		this.serverChannel = serverChannel;
		this.useMultipleReactors = useMultipleReactors;
	}
	
	@Override
	public void run() {
		log(selector+" accept...");
		try {
			 SocketChannel clientChannel = serverChannel.accept();
			 if(clientChannel != null){
				 log(selector+" clientChannel not null...");
				 //如果使用阻塞的select方式，且目的是开启了多个reactor池，而不是mainReactor和subReactor的关系的话，
				 //则下面就不是nextSubSelector().selector，而是改为传递当前实例的selector对象即可
				 handle(useMultipleReactors ? nextSubReactor().selector : selector, clientChannel);
			 }else{
				 log(selector+" clientChannel is null...");
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 在每个具体的Handler下调用run方法是为了令其从connecting状态变为reading状态，
	 * 和原pdf版本下的做法是一样的，只不过原pdf版本是在构造函数直接修改设置了感兴趣为read事件
	 */
	public abstract void handle(Selector selector, SocketChannel clientSocket);

}
