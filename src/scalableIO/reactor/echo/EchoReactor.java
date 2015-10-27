package scalableIO.reactor.echo;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import scalableIO.reactor.Acceptor;
import scalableIO.reactor.Reactor;

public class EchoReactor extends Reactor {
	
	EchoReactor(int port){
		super(port);
	}

	@Override
	public Acceptor newAcceptor(Selector selector, ServerSocketChannel serverChannel) {
		return new EchoAcceptor(selector, serverChannel);
	}
	
	public static void main(String[] args) {
		new EchoReactor(9002).start();
	}

}
