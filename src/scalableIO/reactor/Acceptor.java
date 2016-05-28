package scalableIO.reactor;

import static scalableIO.Logger.log;
import static scalableIO.ServerContext.nextSubReactor;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * 连接接收者
 * <ul>
 * <li>处理TCP/IP网络的连接，子SubReactor即在此处做分配</li>
 * </ul>
 */
public abstract class Acceptor extends Thread {

	protected final Selector selector;
	protected final ServerSocketChannel serverChannel;
	protected final boolean useMultipleReactors;
	
	public Acceptor(Selector selector, ServerSocketChannel serverChannel, boolean useMultipleReactors){
		this.selector = selector;
		this.serverChannel = serverChannel;
		this.useMultipleReactors = useMultipleReactors;
	}
	
	@Override
	public void run() {
		log(selector+" accept...");
		try {
			 SocketChannel clientChannel = serverChannel.accept();
			 if(clientChannel != null){
				 log(selector+" clientChannel not null...");
				 //���ʹ��������select��ʽ����Ŀ���ǿ����˶��reactor�أ�������mainReactor��subReactor�Ĺ�ϵ�Ļ���
				 //������Ͳ���nextSubSelector().selector�����Ǹ�Ϊ���ݵ�ǰʵ����selector���󼴿�
				 handle(useMultipleReactors ? nextSubReactor().selector : selector, clientChannel);
			 }else{
				 log(selector+" clientChannel is null...");
			 }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ÿ�������Handler�µ���run������Ϊ�������connecting״̬��Ϊreading״̬��
	 * ��ԭpdf�汾�µ�������һ���ģ�ֻ����ԭpdf�汾���ڹ��캯��ֱ���޸������˸���ȤΪread�¼�
	 */
	public abstract void handle(Selector selector, SocketChannel clientSocket);

}
