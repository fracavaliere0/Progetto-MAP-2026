package Server;

/** Segnala un indice di ramo non valido ricevuto dal client durante la predizione. */
public class UnknownValueException extends Exception {

	/**
	 * Crea un errore di selezione del ramo.
	 * @param message descrizione del valore non valido e delle alternative ammesse
	 */
	public UnknownValueException(String message) {
		super(message);
	}
}
