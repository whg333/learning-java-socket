package scalableIO.reactor;

import static scalableIO.Logger.log;
import static scalableIO.ServerContext.nextSubReactor;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public abstract class Acceptor extends Thread {

	protected final Selector selector;
	protected final ServerSocketChannel serverChannel;
	
	public Acceptor(Selector selector, ServerSocketChannel serverChannel){
		this.selector = selector;
		this.serverChannel = serverChannel;
	}
	
	@Override
	public void run() {
		log(selector+" accept...");
		try {
			 SocketChannel clientChannel = serverChannel.accept();
			 if(clientChannel != null){
				 log(selector+" clientChannel not null...");
				 //如果使用阻塞的select方式，且目的是开启了多个reactor池，而不是mainReactor和subReactor的关系的话，
				 //则下面就不是nextSubSelector().selector，而是改为传递selector即可
				 handle(nextSubReactor().selector/*selector*/, clientChannel);
			 }else{
				 log(selector+" clientChannel is null...");
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void handle(Selector selector, SocketChannel clientSocket);

}
