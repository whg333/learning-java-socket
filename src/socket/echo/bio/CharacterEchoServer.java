package socket.echo.bio;

import static socket.Constant.TCP_PORT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class CharacterEchoServer {

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(TCP_PORT);
		while (true) {
			Socket s = ss.accept();
			BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
			String line;
			while ((line = reader.readLine()) != null) {
				writer.println(line);
				writer.flush();
			}
			reader.close();
			writer.close();
			s.close();
		}
	}

}
