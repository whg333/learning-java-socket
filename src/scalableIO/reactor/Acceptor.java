package scalableIO.reactor;

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
		try {
			 SocketChannel clientChannel = serverChannel.accept();
			 if(clientChannel != null){
				 handle(selector, clientChannel);
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void handle(Selector selector, SocketChannel clientSocket);

}
