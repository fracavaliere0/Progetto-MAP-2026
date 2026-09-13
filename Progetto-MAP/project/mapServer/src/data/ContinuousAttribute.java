package data;

/** Attributo numerico, usato come variabile esplicativa o come classe da predire. */
public class ContinuousAttribute extends Attribute {

	/**
	 * Crea un attributo numerico.
	 * @param name nome della colonna
	 * @param index indice della colonna a partire da zero
	 */
	public ContinuousAttribute(String name, int index) {
		super(name, index);
	}
}
