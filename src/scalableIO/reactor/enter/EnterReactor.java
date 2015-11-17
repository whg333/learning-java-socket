package scalableIO.reactor.enter;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import scalableIO.ServerContext;
import scalableIO.reactor.Acceptor;
import scalableIO.reactor.Reactor;

public class EnterReactor extends Reactor {
	
	public EnterReactor(int port, ServerSocketChannel serverChannel, boolean isMainReactor, boolean useMultipleReactors, long timeout){
		super(port, serverChannel, isMainReactor, useMultipleReactors, timeout);
	}

	@Override
	public Acceptor newAcceptor(Selector selector) {
		return new EnterAcceptor(selector, serverChannel, useMultipleReactors);
	}
	
	public static void main(String[] args) {
		//new EnterReactor(9003).start();
		ServerContext.start(EnterReactor.class);
	}

}
