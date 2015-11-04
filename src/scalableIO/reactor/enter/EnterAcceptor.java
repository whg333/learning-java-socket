package scalableIO.reactor.enter;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import scalableIO.reactor.Acceptor;

public class EnterAcceptor extends Acceptor {

	EnterAcceptor(Selector selector, ServerSocketChannel serverChannel){
		super(selector, serverChannel);
	}
	
	@Override
	public void handle(Selector selector, SocketChannel clientChannel) {
		new EnterHandler(selector, clientChannel).run();
	}

}
