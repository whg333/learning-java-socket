package socket.frame;

public interface Framer {

	void writeFrame(byte[] msg);
	byte[] readFrame();
	
}
