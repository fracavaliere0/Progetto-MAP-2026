package database;

/** Errore di caricamento del driver JDBC o di apertura della connessione MySQL. */
public class DatabaseConnectionException extends Exception {

	/**
	 * Crea un errore con una descrizione.
	 * @param message descrizione del problema di connessione
	 */
	public DatabaseConnectionException(String message) {
		super(message);
	}

	/**
	 * Conserva l'eccezione originaria.
	 * @param cause errore del driver o di JDBC
	 */
	public DatabaseConnectionException(Exception cause) {
		super(cause);
	}
}
