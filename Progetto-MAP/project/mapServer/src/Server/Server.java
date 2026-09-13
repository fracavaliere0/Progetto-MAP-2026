package Server;

/** Punto di ingresso del server TCP multiclient per gli alberi di regressione. */
public class Server {

	/**
	 * Avvia il ciclo di ascolto, che rimane bloccante fino a un errore di I/O.
	 * Una porta non valida viene segnalata senza avviare il server.
	 * @param args porta TCP facoltativa in posizione 0; il valore predefinito e' 8080
	 */
	public static void main(String[] args) {
		int port = 8080;
		try {
			if (args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			new MultiServer(port);
		} catch (IllegalArgumentException e) {
			System.out.println("Error: invalid server port. Use an integer between 0 and 65535.");
		}
	}
}
