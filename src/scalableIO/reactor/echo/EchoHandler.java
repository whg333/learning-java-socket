package scalableIO.reactor.echo;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import scalableIO.reactor.Handler;

public class EchoHandler extends Handler {

	EchoHandler(Selector selector, SocketChannel clientSocket){
		super(selector, clientSocket);
	}
	
	@Override
	public int byteBufferSize() {
		return 1;
	}
	
	@Override
	public boolean inputIsComplete() {
		System.out.println(input);
		return readData.length() > 0;
	}
	
	@Override
	public void process() {
		output.clear();
		output.put(readData.toString().getBytes());
//		output.put((byte)53);
//		output.put((byte)54);
//		output.put((byte)55);
//		output.put((byte)56);
	}

	@Override
	public boolean outputIsComplete() {
		return !output.hasRemaining();
	}

}
