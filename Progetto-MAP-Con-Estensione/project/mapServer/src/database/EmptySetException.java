package database;

/** Segnala che una query non ha restituito esempi per il training set. */
public class EmptySetException extends Exception {

	/** Crea un errore di insieme vuoto senza messaggio aggiuntivo. */
	public EmptySetException() {
		super();
	}

	/**
	 * Crea un errore di insieme vuoto con una descrizione.
	 * @param message descrizione della query o dell'insieme vuoto
	 */
	public EmptySetException(String message) {
		super(message);
	}
}
