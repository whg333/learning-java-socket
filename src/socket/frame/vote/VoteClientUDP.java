package socket.frame.vote;

import static socket.frame.vote.VoteMsgTextCoder.MAX_MSG_LENGTH;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import socket.Constant;

public class VoteClientUDP {

	private static final int VOTE_ID = 888;
	
	public static void main(String[] args) throws IOException {
		VoteMsgCoder coder = new VoteMsgBinCoder();
		DatagramSocket socket = new DatagramSocket();
		socket.connect(InetAddress.getByName(Constant.LOCALHOST), Constant.UDP_PORT);
		
		VoteMsg msg = new VoteMsg(false, true, VOTE_ID, 0);
		byte[] msgBytes = coder.encode(msg);
		DatagramPacket reqPacket = new DatagramPacket(msgBytes, msgBytes.length);
		socket.send(reqPacket);
		
		msg.setInquiry(false);
		msgBytes = coder.encode(msg);
		//reqPacket = new DatagramPacket(msgBytes, msgBytes.length);
		//注释掉上面的new的DatagramPacket，send的时候复用packet，但需要设置Data
		reqPacket.setData(msgBytes);
		socket.send(reqPacket);
		
		DatagramPacket respPacket = new DatagramPacket(new byte[MAX_MSG_LENGTH], MAX_MSG_LENGTH);
		socket.receive(respPacket);
		msgBytes = Arrays.copyOfRange(respPacket.getData(), 0, respPacket.getLength());
		msg = coder.decode(msgBytes);
		System.out.println("received query:"+msg);
		
		//respPacket = new DatagramPacket(new byte[MAX_MSG_LENGTH], MAX_MSG_LENGTH);
		//注释掉上面的new的DatagramPacket，receive的时候复用packet，但需要设置缓冲区的长度避免前一个receive的截断
		respPacket.setLength(MAX_MSG_LENGTH);
		socket.receive(respPacket);
		msgBytes = Arrays.copyOfRange(respPacket.getData(), 0, respPacket.getLength());
		msg = coder.decode(msgBytes);
		System.out.println("received vote:"+msg);
		
		socket.close();
	}
	
}
