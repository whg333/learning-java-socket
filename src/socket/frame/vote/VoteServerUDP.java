package socket.frame.vote;

import static socket.frame.vote.VoteMsgTextCoder.MAX_MSG_LENGTH;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import socket.Constant;

public class VoteServerUDP {

	public static void main(String[] args) throws IOException {
		VoteService service = new VoteService();
		VoteMsgCoder coder = new VoteMsgBinCoder();
		DatagramSocket socket = new DatagramSocket(Constant.UDP_PORT);
		System.out.println("VoteServerUDP listen on "+Constant.UDP_PORT);
		
		byte[] buffer = new byte[MAX_MSG_LENGTH];
		while(true){
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			
			byte[] req = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
			VoteMsg msg = service.handleRequest(coder.decode(req));
			
			packet.setData(coder.encode(msg));
			socket.send(packet);
		}
	}
	
}
