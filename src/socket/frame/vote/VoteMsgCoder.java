package socket.frame.vote;

public interface VoteMsgCoder {

	byte[] encode(VoteMsg msg);
	VoteMsg decode(byte[] input);
	
}
