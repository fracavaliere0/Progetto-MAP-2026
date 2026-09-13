package data;

import java.io.Serializable;

/** Attributo del training set, identificato da un nome e da un indice di colonna. */
public abstract class Attribute implements Serializable {
	/** Nome simbolico dell'attributo. */
	private String name;
	/** Indice di colonna a partire da zero. */
	private int index;

	/**
	 * Inizializza i metadati dell'attributo.
	 * @param name nome della colonna
	 * @param index posizione della colonna, a partire da zero
	 */
	public Attribute(String name, int index) {
		this.name = name;
		this.index = index;
	}

	/**
	 * Restituisce il nome simbolico.
	 * @return nome dell'attributo
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Restituisce la posizione nel training set.
	 * @return indice di colonna a partire da zero
	 */
	public int getIndex() {
		return this.index;
	}

	@Override
	public String toString() {
		return name;
	}
}
