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
		CONNECTING(0), 
		READING(SelectionKey.OP_READ),
		WRITING(SelectionKey.OP_WRITE);
		
		private final int opBit;
		private State(int operateBit){
			opBit = operateBit;
		}
	}
	
	private State state;
	protected final SocketChannel clientChannel;
	protected final SelectionKey key;
	
	protected final ByteBuffer input;
	protected final StringBuilder readData = new StringBuilder();
	protected ByteBuffer output;
	
	public Handler(Selector selector, SocketChannel clientChannel){
		this.state = State.CONNECTING;
		SelectionKey key = null;
		try {
			clientChannel.configureBlocking(false);
			key = clientChannel.register(selector, this.state.opBit);
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
			default:
				throw new IllegalArgumentException("Unsupported State:"+state);
		}
	}
	
	private void connect() {
		interestOps(State.READING);
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
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
		
		log("received from client:"+readData+", "+readData.length());
		if(readIsComplete() && process()){
			interestOps(State.WRITING);
		}
	}
	
	//TODO 修改为复用output，即当output容量不足的时候就反复write，而不是每次都使用wrap来new一个新的
	public boolean process(){
		log("readData="+readData.toString());
		if(isQuit()){
			//key.cancel();
			disconnect();
			return false;
		}
		output = ByteBuffer.wrap(readData.toString().getBytes());
		readData.delete(0, readData.length());
		return true;
	}
	
	private void write(){
		try {
			do{
				clientChannel.write(output);
			}while(!writeIsComplete());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		log("writed to client:"+readData+", "+readData.length());
		
		interestOps(State.READING);
		//感兴趣key变化后必须重置附件？！否则连续2次回车后，read事件读取原来的附件读取不到（readSize==0）
		//key.attach(this);
	}
	
	private void interestOps(State state){
		this.state = state;
		key.interestOps(state.opBit);
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

	public abstract boolean readIsComplete();

	public abstract boolean writeIsComplete();

}
