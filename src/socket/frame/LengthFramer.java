package socket.frame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LengthFramer implements Framer {

	private final DataInputStream in;
	private final OutputStream out;
	
	public LengthFramer(InputStream in, OutputStream out){
		this.in = new DataInputStream(new BufferedInputStream(in));
		this.out = out;
	}
	
	@Override
	public void writeFrame(byte[] msg) {
		if(msg.length > Math.pow(2, Short.SIZE)){
			throw new IllegalArgumentException("message too long");
		}
		
		DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out));
		try {
			dataOut.writeShort(msg.length);
			dataOut.write(msg);
			dataOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] readFrame() {
		int length;
		try {
			length = in.readUnsignedShort();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		byte[] msg = new byte[length];
		try {
			in.readFully(msg);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return msg;
	}

}
