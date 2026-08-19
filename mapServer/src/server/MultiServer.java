package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {
	private int PORT = 8080;

	public MultiServer(int port) {
		PORT = port;
		run();
	}

	private void run() {
		try {
			ServerSocket server = new ServerSocket(PORT);
			while (true) {
				Socket socket = server.accept();
				new ServerOneClient(socket);
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
}
