package scalableIO.reactor;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public abstract class Reactor extends Thread{

	protected final int port;
	protected final Selector selector;
	protected final ServerSocketChannel serverChannel;
	
	public Reactor(int port){
		this.port = port;
		Selector selector = null;
		ServerSocketChannel serverChannel = null;
		try {
			selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(port));
			serverChannel.configureBlocking(false);
			SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			key.attach(newAcceptor(selector, serverChannel));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.selector = selector;
		this.serverChannel = serverChannel;
	}
	
	public abstract Acceptor newAcceptor(Selector selector, ServerSocketChannel serverChannel);
	
	@Override
	public void run(){
		try {
			while(!Thread.interrupted()){
				selector.select();
				Iterator<SelectionKey> keyIt = selector.selectedKeys().iterator();
				while(keyIt.hasNext()){
					SelectionKey key = keyIt.next();
					keyIt.remove();
					dispatch(key);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void dispatch(SelectionKey key){
		Runnable r = (Runnable)key.attachment();
		if(r != null){
			r.run();
		}
	}
	
	public int getPort() {
		return port;
	}
	
}
