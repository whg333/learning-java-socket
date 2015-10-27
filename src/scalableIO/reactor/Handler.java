package scalableIO.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public abstract class Handler extends Thread {

	private enum State{
		CONNECTING, READING, WRITING, DISCONNECTING;
	}
	private State state = State.CONNECTING;
	
	protected final SocketChannel clientSocket;
	protected final SelectionKey key;
	
	protected final ByteBuffer input;
	protected final StringBuilder readData = new StringBuilder();
	protected ByteBuffer output;
	
	public Handler(Selector selector, SocketChannel clientSocket){
		this.clientSocket = clientSocket;
		SelectionKey key = null;
		try {
			this.clientSocket.configureBlocking(false);
			key = clientSocket.register(selector, SelectionKey.OP_READ, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.key = key;
		selector.wakeup();
		
		input = ByteBuffer.allocate(byteBufferSize());
	}
	
	
	
	@Override
	public void run() {
		switch (state) {
			case CONNECTING:
				connect();
				break;
			case READING:
				read();
				break;
			case WRITING:
				write();
				break;
			case DISCONNECTING:
				break;
			default:
				throw new IllegalArgumentException("Unsupported State:"+state);
		}
	}
	
	private void connect() {
		state = State.READING;
	}

	private void read(){
		int readSize;
		try {
			while((readSize = clientSocket.read(input)) > 0){
				readData.append(new String(Arrays.copyOfRange(input.array(), 0, readSize)));
				input.clear();
			}
			if(readSize == -1){
				key.cancel();
				return;
			}
			
			System.out.println("received from client:"+readData+", "+readData.length());
			//if(inputIsComplete()){
				//process();
				state = State.WRITING;
				key.interestOps(SelectionKey.OP_WRITE);
			//}
			//input.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private void write(){
		try {
			output = ByteBuffer.wrap(readData.toString().getBytes());
			do{
				clientSocket.write(output);
			}while(!outputIsComplete());
			
			System.out.println("writed to client:"+readData+", "+readData.length());
			if(isQuit()){
				key.cancel();
			}else{
				state = State.READING;
				key.interestOps(SelectionKey.OP_READ);
				//key.attach(this);
				readData.delete(0, readData.length());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isQuit(){
		return false;
	}
	
	public abstract int byteBufferSize();

	public abstract boolean inputIsComplete();
	
	public abstract void process();

	public abstract boolean outputIsComplete();

}
