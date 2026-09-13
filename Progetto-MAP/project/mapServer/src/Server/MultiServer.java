package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Accetta connessioni TCP e avvia un {@link ServerOneClient} per ciascun client. */
public class MultiServer {
	private int PORT = 8080;

	/**
	 * Apre la porta e avvia immediatamente il ciclo bloccante di accettazione.
	 * Gli errori di I/O vengono stampati sulla console.
	 * @param port porta TCP di ascolto; 0 richiede una porta assegnata dal sistema
	 * @throws IllegalArgumentException se la porta non e' compresa tra 0 e 65535
	 */
	public MultiServer(int port) {
		PORT = port;
		run();
	}

	private void run() {
		try (ServerSocket server = new ServerSocket(PORT)) {
			while (true) {
				Socket socket = server.accept();
				try {
					new ServerOneClient(socket);
				} catch (IOException e) {
					System.out.println(e.toString());
					try {
						socket.close();
					} catch (IOException closeError) {
						System.out.println(closeError.toString());
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
}
