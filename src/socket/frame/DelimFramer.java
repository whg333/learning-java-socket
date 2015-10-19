package socket.frame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DelimFramer implements Framer {

	private static final byte DELIMITER = '\n';
	
	private final InputStream in;
	private final OutputStream out;
	
	public DelimFramer(InputStream in, OutputStream out){
		this.in = in;
		this.out = out;
	}

	@Override
	public void writeFrame(byte[] msg) {
		for(byte b:msg){
			if(b == DELIMITER){
				throw new IllegalArgumentException("message contains delimiter");
			}
		}
		try {
			out.write(msg);
			out.write(DELIMITER);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] readFrame() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int readByte;
		
		try {
			while((readByte = in.read()) != DELIMITER){
				if(readByte == -1){
					if(buffer.size() == 0){
						return null;
					}else{
						throw new IllegalArgumentException("Non-empty message without delimiter");
					}
				}
				buffer.write(readByte);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return buffer.toByteArray();
	}

}
