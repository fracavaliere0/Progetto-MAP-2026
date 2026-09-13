package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestisce una connessione JDBC a MySQL.
 * La configurazione corrente usa {@code localhost:3306/MapDB} e l'utente
 * {@code MapUser}; richiede MySQL Connector/J nel classpath di esecuzione.
 * La connessione viene aperta solo chiamando {@link #initConnection()}.
 */
public class DbAccess {
	private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
	private final String DBMS = "jdbc:mysql";
	private String SERVER = "localhost";
	private String DATABASE = "MapDB";
	private final String PORT = "3306";
	private String USER_ID = "MapUser";
	private String PASSWORD = "map";
	private Connection conn;

	/**
	 * Carica il driver MySQL e apre la connessione al database configurato.
	 * @throws DatabaseConnectionException se il driver manca o la connessione SQL fallisce
	 */
	public void initConnection() throws DatabaseConnectionException {
		try {
			Class.forName(DRIVER_CLASS_NAME);
			conn = DriverManager.getConnection(
					DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE + "?serverTimezone=UTC",
					USER_ID, PASSWORD);
		} catch (ClassNotFoundException e) {
			throw new DatabaseConnectionException(e);
		} catch (SQLException e) {
			throw new DatabaseConnectionException(e);
		}
	}

	/**
	 * Restituisce la connessione gestita, senza aprirne una nuova.
	 * @return connessione corrente, oppure {@code null} prima dell'apertura
	 */
	public Connection getConnection() {
		return conn;
	}

	/**
	 * Chiude la connessione se presente; gli eventuali errori SQL vengono stampati
	 * sulla console e non propagati.
	 */
	public void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.out.println(e);
			}
		}
	}
}
