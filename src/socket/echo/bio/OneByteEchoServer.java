package socket.echo.bio;

import static socket.Constant.TCP_PORT;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class OneByteEchoServer {

	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(TCP_PORT);
		while (true) {
			Socket s = ss.accept();
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
			in.close();
			out.close();
			s.close();
		}
	}

}
