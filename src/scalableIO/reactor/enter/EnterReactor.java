package scalableIO.reactor.enter;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import scalableIO.ServerContext;
import scalableIO.reactor.Acceptor;
import scalableIO.reactor.Reactor;

public class EnterReactor extends Reactor {
	
	public EnterReactor(int port){
		super(port);
	}

	@Override
	public Acceptor newAcceptor(Selector selector, ServerSocketChannel serverChannel) {
		return new EnterAcceptor(selector, serverChannel);
	}
	
	public static void main(String[] args) {
		//new EnterReactor(9003).start();
		ServerContext.start(EnterReactor.class, 9003);
	}

}
