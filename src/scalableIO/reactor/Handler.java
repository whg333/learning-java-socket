package scalableIO.reactor;

import static scalableIO.Logger.log;

import java.io.IOException;
import java.net.SocketAddress;
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
	
	protected final SocketChannel clientChannel;
	protected final SelectionKey key;
	
	protected final ByteBuffer input;
	protected final StringBuilder readData = new StringBuilder();
	protected ByteBuffer output;
	
	public Handler(Selector selector, SocketChannel clientChannel){
		SelectionKey key = null;
		try {
			clientChannel.configureBlocking(false);
			key = clientChannel.register(selector, SelectionKey.OP_READ);
			key.attach(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		selector.wakeup();
		this.clientChannel = clientChannel;
		this.key = key;
		this.input = ByteBuffer.allocate(byteBufferSize());
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
				disconnect();
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
			while((readSize = clientChannel.read(input)) > 0){
				readData.append(new String(Arrays.copyOfRange(input.array(), 0, readSize)));
				input.clear();
			}
			if(readSize == -1){
				//key.cancel();
				disconnect();
				return;
			}
			
			log("received from client:"+readData+", "+readData.length());
			if(inputIsComplete()){
				process();
				state = State.WRITING;
				key.interestOps(SelectionKey.OP_WRITE);
			}
			//input.clear();
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
	}
	
	//TODO 修改为复用output，即当output容量不足的时候就反复write，而不是每次都使用wrap来new一个新的
	public void process(){
		output = ByteBuffer.wrap(readData.toString().getBytes());
		readData.delete(0, readData.length());
	}
	
	private void write(){
		try {
			do{
				clientChannel.write(output);
			}while(!outputIsComplete());
			
			log("writed to client:"+readData+", "+readData.length());
			if(isQuit()){
				//key.cancel();
				disconnect();
			}else{
				state = State.READING;
				key.interestOps(SelectionKey.OP_READ);
				//感兴趣key变化后必须重置附件？！否则连续2次回车后，read事件读取原来的附件读取不到（readSize==0）
				//key.attach(this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isQuit(){
		return false;
	}
	
	private void disconnect(){
		try {
			clientChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log("\nclientAddress=【"+clientAddress(clientChannel)+"】 had already closed!!! ");
	}
	
	private static SocketAddress clientAddress(SocketChannel clientChannel){
		return clientChannel.socket().getRemoteSocketAddress();
	}
	
	public abstract int byteBufferSize();

	public abstract boolean inputIsComplete();

	public abstract boolean outputIsComplete();

}
