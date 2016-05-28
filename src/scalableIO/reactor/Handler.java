package scalableIO.reactor;

import static scalableIO.Logger.err;
import static scalableIO.Logger.log;
import static scalableIO.ServerContext.execute;
import static scalableIO.ServerContext.useThreadPool;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Handlers处理器
 * <ul>
 * <li>处理非阻塞读/写IO事件所对应的业务逻辑</li>
 * <li>类似AWT中的ActionListeners处理器</li>
 * </ul>
 */
public abstract class Handler extends Thread {

	private enum State{
		CONNECTING(0),
		READING(SelectionKey.OP_READ),
		PROCESSING(2),
		WRITING(SelectionKey.OP_WRITE);
		
		private final int opBit;
		private State(int operateBit){
			opBit = operateBit;
		}
	}
	
	private State state;
	protected final SocketChannel clientChannel;
	protected final SelectionKey key;
	
	protected final ByteBuffer readBuf;
	protected final StringBuilder readData = new StringBuilder();
	protected ByteBuffer writeBuf;
	
	public Handler(Selector selector, SocketChannel clientChannel){
		this.state = State.CONNECTING;
		SelectionKey key = null;
		try {
			clientChannel.configureBlocking(false);
			//这里在使用subSelector的时候会阻塞，为什么？是因为使用了阻塞的select方法，非阻塞的才可以
			//但如果使用reactor池的话，那是因为需要serverChannel注册selector的accept事件！？必须对应上才可以通过，否则阻塞
			key = clientChannel.register(selector, this.state.opBit);
			key.attach(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.clientChannel = clientChannel;
		this.key = key;
		this.readBuf = ByteBuffer.allocate(byteBufferSize());
		log(selector+" connect success...");
	}
	
	@Override
	public void run() {
		switch (state) {
			case CONNECTING:
				connect();
				break;
			case READING:
				readAndProcess();
				break;
			case WRITING:
				write();
				break;
			default:
				err("\nUnsupported State: "+state+" ! overlap processing with IO...");
		}
	}
	
	private void connect() {
		interestOps(State.READING);
	}

	/**
	 * But harder to overlap processing with IO<br/>
	 * Best when can first read all input a buffer<br/>
	 * <br>
	 * That why we used synchronized on read method!<br/>
	 * Just to protected read buffer And handler state...<br/>
	 * <br>
	 * 其实就是害怕重叠IO和工作线程处理不一致：例如Reactor单线程读某个key的IO完毕后立马开启工作线程的处理，
	 * 紧接着Reactor单线程处理第二个IO key的时候发现还是之前的那个key的读IO事件，但是之前同一个key的处理还未完成，
	 * 不等待之前的处理完成的话，就会出现多个线程同时访问修改Handler里面数据的情况，导致出错，
	 * 但是最好先把数据都全部读入buffer中就可以规避了！？
	 * 
	 * <p>此处的synchronized同步是为了防止state状态以及读写buffer在多线程访问中出现读脏数据，
	 * Debug调试的时候同时访问一个SelectionKey有2个线程：
	 * <br>1、Reactor单线程
	 * <br>2、读数据完毕后多线程处理的话，线程池里面执行processAndHandOff的线程
	 * <br>
	 * 不能单一使用volatile或者原子变量的原因是因为该方法为复合操作（check and act）
	 */
	private synchronized void readAndProcess(){
		doRead();
		doProcess();
	}
	
	private void doRead(){
		int readSize;
		try {
			while((readSize = clientChannel.read(readBuf)) > 0){
				readData.append(new String(Arrays.copyOfRange(readBuf.array(), 0, readSize)));
				readBuf.clear();
			}
			if(readSize == -1){
				disconnect();
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
		
		log("readed from client:"+readData+", "+readData.length());
	}
	
	private void doProcess(){
		if(readIsComplete()){
			state = State.PROCESSING;
			processAndInterestWrite();
		}
	}
	
	/**
	 * 处理过程可能是比较耗时的，所以可考虑将其交由线程池处理，处理完毕后才注册感兴趣的write事件<p>
	 * 然而正是由于交由线程池处理所以可能造成重叠IO的多线程处理的状态问题，最好能一次性全部读入buffer，否则考虑同步状态处理问题
	 */
	private void processAndInterestWrite(){
		Processor processor = new Processor();
		if(useThreadPool){
			execute(processor);
		}else{
			processor.run();
		}
	}
	
	private final class Processor implements Runnable{
		@Override 
		public void run() { 
			processAndHandOff(); 
		}
	}
	
	private synchronized void processAndHandOff(){
		if(process()){
			interestOps(State.WRITING);
		}
	}
	
	//TODO 修改为复用output，即当output容量不足的时候就反复write，而不是每次都使用wrap来new一个新的
	public boolean process(){
		log("process readData="+readData.toString());
		if(isQuit()){
			disconnect();
			return false;
		}
		
		writeBuf = ByteBuffer.wrap(readData.toString().getBytes());
		readData.delete(0, readData.length());
		return true;
	}
	
	private void write(){
		try {
			do{
				clientChannel.write(writeBuf);
			}while(!writeIsComplete());
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
		
		String writeData = new String(Arrays.copyOf(writeBuf.array(), writeBuf.array().length));
		log("writed to client:"+writeData+", "+writeData.length());
		interestOps(State.READING);
	}
	
	/**
	 * 事件和事件处理器的绑定
	 * <ul>
	 * <li>类似AWT中的addActionListener添加监听器/观察者</li>
	 * </ul>
	 * 不需要重置key的附件（key.attach）是因为key一直绑定使用的是当前this实例，
	 * 在Reactor dispatch的时候如果是接受（accept）该附件就是Acceptor实例，
	 * 否则就是绑定到该key的同一个Handler实例
	 */
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
		log("\nclient Address=【"+clientAddress(clientChannel)+"】 had already closed!!! ");
	}
	
	private static SocketAddress clientAddress(SocketChannel clientChannel){
		return clientChannel.socket().getRemoteSocketAddress();
	}
	
	public abstract int byteBufferSize();

	public abstract boolean readIsComplete();

	public abstract boolean writeIsComplete();

}
