package data;

/**
 * Rappresenta un attributo discreto.
 *
 * L'attributo e' descritto dal suo nome, dal suo indice e dall'insieme dei
 * valori discreti che puo' assumere.
 */
public class DiscreteAttribute extends Attribute {
    /** Array di oggetti String per i valori discreti. */
    private String[] values;

    /**
     * Costruisce un attributo discreto e ne memorizza i valori ammessi.
     *
     * @param name Nome dell'attributo.
     * @param index Indice dell'attributo.
     * @param values Array di stringhe con i valori possibili.
     */
    public DiscreteAttribute(String name, int index, String[] values) {
        // Chiama il costruttore di Attribute
        super(name, index);
        this.values = values;
    }

    /**
     * Restituisce la cardinalita' dell'array {@code values}.
     *
     * @return Numero di valori discreti memorizzati nell'array.
     */
    public int getNumberOfDistinctValues() {
        return values.length;
    }

    /**
     * Restituisce il valore in posizione i.
     *
     * @param i indice del valore.
     * @return Il valore corrispondente.
     * @throws IndexOutOfBoundsException Se {@code i} non e' compreso tra 0 e
     *         {@code values.length - 1}.
     */
    public String getValue(int i) {
        if (i >= values.length || i < 0) {
            throw new IndexOutOfBoundsException(
                    "Indice " + i + "non valido per la lunghezza della stringa");
        }
        return values[i];
    }
}
