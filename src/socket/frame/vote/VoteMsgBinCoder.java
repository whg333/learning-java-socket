package socket.frame.vote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class VoteMsgBinCoder implements VoteMsgCoder {

	private static final byte MAGIC_BYTE = 16+4+1;
	private static final short MAGIC_SHORT = MAGIC_BYTE << (2+8);
	private static final short RESP = 2 << 8;
	private static final short QUERY = 1 << 8;
	
	private static final short RESP_FLAG = 0x0200;
	private static final short QUERY_FLAG = 0x0100;
	
	@Override
	public byte[] encode(VoteMsg msg) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buffer);
		
		short magicAndFlag = MAGIC_SHORT;
		if(msg.isResponse()){
			magicAndFlag |= RESP;
		}
		if(msg.isInquiry()){
			magicAndFlag |= QUERY;
		}
		
		try {
			out.writeShort(magicAndFlag);
			out.writeShort((short)msg.getCandidateID());
			if(msg.isResponse()){
				out.writeLong(msg.getVoteCount());
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return buffer.toByteArray();
	}

	@Override
	public VoteMsg decode(byte[] input) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(input));
		try {
			short magicAndFlag = in.readShort();
			checkLegal(magicAndFlag);
			boolean isResp = ((magicAndFlag & RESP_FLAG) != 0);
			boolean isQuery = ((magicAndFlag & QUERY_FLAG) != 0);
			short voteId = in.readShort();
			long voteCount = 0;
			if(isResp){
				voteCount = in.readLong();
			}
			return new VoteMsg(isResp, isQuery, voteId, voteCount);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void checkLegal(short magicAndFlag){
		byte magic = (byte)(magicAndFlag >> 10);
		if(magic != MAGIC_BYTE){
			throw new IllegalArgumentException("bag magic byte:"+magic);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(MAGIC_SHORT);
		System.out.println(Integer.toBinaryString(MAGIC_SHORT));
	}

}
