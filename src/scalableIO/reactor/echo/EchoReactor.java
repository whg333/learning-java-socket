package scalableIO.reactor.echo;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import scalableIO.ServerContext;
import scalableIO.reactor.Acceptor;
import scalableIO.reactor.Reactor;

public class EchoReactor extends Reactor {
	
	public EchoReactor(int port, ServerSocketChannel serverChannel, boolean isMainReactor, boolean useMultipleReactors, long timeout){
		super(port, serverChannel, isMainReactor, useMultipleReactors, timeout);
	}

	@Override
	public Acceptor newAcceptor(Selector selector) {
		return new EchoAcceptor(selector, serverChannel, useMultipleReactors);
	}
	
	public static void main(String[] args) {
		//new EchoReactor(9002).start();
		ServerContext.start(EchoReactor.class);
	}

}
