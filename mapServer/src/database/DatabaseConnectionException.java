package database;

public class DatabaseConnectionException extends Exception {

	public DatabaseConnectionException(String message) {
		super(message);
	}

	public DatabaseConnectionException(Exception cause) {
		super(cause);
	}
}
