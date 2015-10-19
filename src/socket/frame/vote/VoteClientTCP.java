package socket.frame.vote;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import socket.Constant;
import socket.frame.DelimFramer;
import socket.frame.Framer;

public class VoteClientTCP {
	
	private static final int VOTE_ID = 888;

	public static void main(String[] args) throws UnknownHostException, IOException {
		VoteMsgCoder coder = new VoteMsgBinCoder();
		Socket socket = new Socket(Constant.LOCALHOST, Constant.TCP_PORT);
		Framer framer = new DelimFramer(socket.getInputStream(), socket.getOutputStream());
		
		VoteMsg msg = new VoteMsg(false, true, VOTE_ID, 0);
		framer.writeFrame(coder.encode(msg));
		
		msg.setInquiry(false);
		framer.writeFrame(coder.encode(msg));
		
		msg = coder.decode(framer.readFrame());
		System.out.println("received query:"+msg);
		
		msg = coder.decode(framer.readFrame());
		System.out.println("received vote:"+msg);
		
		socket.close();
	}
	
}
