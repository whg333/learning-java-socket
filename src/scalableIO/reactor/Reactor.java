package scalableIO.reactor;

import static scalableIO.Logger.log;
import static scalableIO.ServerContext.isMainReactor;
import static scalableIO.ServerContext.selectTimeOut;
import static scalableIO.ServerContext.serverChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public abstract class Reactor extends Thread{

	protected final int port;
	protected Selector selector;
//	protected ServerSocketChannel serverChannel;
	
	public Reactor(int port){
		this.port = port;
	}
	
	public void configure(){
		ServerSocketChannel serverChannel = serverChannel();
		try {
			selector = Selector.open();
			log(selector+" isMainReactor="+isMainReactor(this));
			if(isMainReactor(this)){
				log(getClass().getSimpleName()+" start on "+port+" ..."+"\n");
				//serverChannel = ServerSocketChannel.open();
				serverChannel.socket().bind(new InetSocketAddress(port));
				serverChannel.configureBlocking(false);
				SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				key.attach(newAcceptor(selector, serverChannel));
			}else{
				
			}
			//如果使用阻塞的select方式，且开启下面的代码的话，相当于开启了多个reactor池，而不是mainReactor和subReactor的关系了
			//SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			//key.attach(newAcceptor(selector, serverChannel));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public abstract Acceptor newAcceptor(Selector selector, ServerSocketChannel serverChannel);
	
	@Override
	public void run(){
		try {
			while(!Thread.interrupted()){
				//不可以使用阻塞的select方式，否则accept后subReactor的selector在register的时候会一直阻塞
				//但是修改为带有超时的select或者selectNow后，subReactor的selector在register就不会阻塞了
				//最终选择了带有超时的select是因为使用selectNow的无限循环会导致CPU飙高特别快
				//selector.select();
				//if(selector.selectNow() > 0){
				if(selector.select(selectTimeOut) > 0){
					//log(selector+" isMainReactor="+isMainReactor(this)+" select...");
					Iterator<SelectionKey> keyIt = selector.selectedKeys().iterator();
					while(keyIt.hasNext()){
						SelectionKey key = keyIt.next();
						dispatch(key);
						keyIt.remove();
					}
				}
			}
			log(getClass().getSimpleName()+" end on "+port+" ..."+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void dispatch(SelectionKey key){
		Runnable r = (Runnable)key.attachment();
		if(r != null){
			r.run();
		}
	}
	
//	public void wakeup(){
//		selector.wakeup();
//		log(selector+" isMainReactor="+isMainReactor(this)+" wakeup...");
//	}
	
}
