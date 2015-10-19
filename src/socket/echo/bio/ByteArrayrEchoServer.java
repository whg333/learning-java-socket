package socket.echo.bio;

import static socket.Constant.BUF_SIZE;
import static socket.Constant.TCP_PORT;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ByteArrayrEchoServer {

	public static void main(String[] args) throws Exception {
		int readSize;
		byte[] readBuf = new byte[BUF_SIZE];
		ServerSocket ss = new ServerSocket(TCP_PORT);
		while (true) {
			Socket s = ss.accept();
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();
			while ((readSize = in.read(readBuf)) != -1) {
				out.write(readBuf, 0, readSize);
			}
			in.close();
			out.close();
			s.close();
		}
	}

}
