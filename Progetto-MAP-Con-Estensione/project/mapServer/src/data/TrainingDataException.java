package data;

/** Segnala l'impossibilita' di caricare un training set valido dal database. */
public class TrainingDataException extends Exception {

	/**
	 * Crea un errore di validazione dei dati.
	 * @param message descrizione del problema
	 */
	public TrainingDataException(String message) {
		super(message);
	}

	/**
	 * Conserva la causa dell'errore di acquisizione.
	 * @param cause errore di connessione, query o insieme vuoto
	 */
	public TrainingDataException(Exception cause) {
		super(cause);
	}
}
