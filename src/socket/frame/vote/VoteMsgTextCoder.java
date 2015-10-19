package socket.frame.vote;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * VOTE PROTO
 * magic | RESPFLAG | <v or q> | VOTEID | VOTECOUNT
 * ensure charset on read and write is same
 */
public class VoteMsgTextCoder implements VoteMsgCoder {

	private static final String MAGIC = "Voting";
	
	private static final String VOTE = "v";
	private static final String QUERY = "q";
	
	private static final String RESP = "R";
	private static final String NO_RESP = "N";
	
	private static final String CHARSET = "US-ASCII";
	private static final String DELIMSTR = " ";
	
	public static final int MAX_MSG_LENGTH = 2000;
	
	@Override
	public byte[] encode(VoteMsg msg) {
		StringBuilder sb = new StringBuilder();
		sb.append(MAGIC).append(DELIMSTR);
		sb.append(msg.isResponse() ? RESP : NO_RESP).append(DELIMSTR);
		sb.append(msg.isInquiry() ? QUERY : VOTE).append(DELIMSTR);
		sb.append(Integer.toString(msg.getCandidateID())).append(DELIMSTR);
		sb.append(Long.toString(msg.getVoteCount()));
		
		String msgStr = sb.toString();
		try {
			return msgStr.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public VoteMsg decode(byte[] input) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new InputStreamReader(new ByteArrayInputStream(input), CHARSET));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		checkLegal(scanner);
		
		boolean isResp = parseIsResp(scanner);
		boolean isQuery = parseIsQuery(scanner);
		int voteId = parseVoteId(scanner);
		long voteCount = parseVoteCount(scanner, isResp);
		
		return new VoteMsg(isResp, isQuery, voteId, voteCount);
	}

	private void checkLegal(Scanner scanner) {
		String token = scanner.next();
		if(!MAGIC.equals(token)){
			throw new IllegalArgumentException("bag magic string:"+token);
		}
	}
	
	private boolean parseIsResp(Scanner scanner) {
		if(RESP.equals(scanner.next())){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean parseIsQuery(Scanner scanner) {
		String token = scanner.next();
		if(VOTE.equals(token)){
			return false;
		}
		if(!QUERY.equals(token)){
			throw new IllegalArgumentException("bag vote/query indicator:"+token);
		}
		return true;
	}
	
	private int parseVoteId(Scanner scanner) {
		return scanner.nextInt();
	}
	
	private long parseVoteCount(Scanner scanner, boolean isResp) {
		return isResp ? scanner.nextLong() : 0;
	}

}
