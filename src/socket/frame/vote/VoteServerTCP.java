package socket.frame.vote;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import socket.Constant;
import socket.frame.DelimFramer;
import socket.frame.Framer;

public class VoteServerTCP {

	public static void main(String[] args) throws IOException {
		VoteService service = new VoteService();
		VoteMsgCoder coder = new VoteMsgBinCoder();
		ServerSocket serverSocket = new ServerSocket(Constant.TCP_PORT);
		System.out.println("VoteServerTCP listen on "+Constant.TCP_PORT);
		
		while(true){
			Socket socket = serverSocket.accept();
			Framer framer = new DelimFramer(socket.getInputStream(), socket.getOutputStream());
			try {
				byte[] req;
				while((req=framer.readFrame()) != null){
					VoteMsg msg = service.handleRequest(coder.decode(req));
					framer.writeFrame(coder.encode(msg));
				}
			} finally {
				socket.close();
			}
		}
	}
	
}
