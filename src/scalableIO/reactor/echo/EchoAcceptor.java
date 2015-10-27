package scalableIO.reactor.echo;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import scalableIO.reactor.Acceptor;

public class EchoAcceptor extends Acceptor {

	EchoAcceptor(Selector selector, ServerSocketChannel serverChannel){
		super(selector, serverChannel);
	}
	
	@Override
	public void handle(Selector selector, SocketChannel clientSocket) {
		new EchoHandler(selector, clientSocket).run();
	}

}
