package data;

/**
 * Rappresenta un attributo discreto.
 */
public class DiscreteAttribute extends Attribute {
    /** Valori discreti dell'attributo. */
    private String[] values;

    /**
     * Costruisce un attributo discreto.
     *
     * @param name Nome simbolico dell'attributo.
     * @param index Identificativo numerico dell'attributo.
     * @param values Valori discreti dell'attributo.
     */
    public DiscreteAttribute(String name, int index, String[] values) {
        super(name, index);
        this.values = values;
    }

    /**
     * Restituisce il numero di valori discreti.
     *
     * @return numero di valori discreti.
     */
    public int getNumberOfDistinctValues() {
        return values.length;
    }

    /**
     * Restituisce il valore discreto in posizione {@code i}.
     *
     * @param i Indice del valore.
     * @return valore discreto con indice {@code i}.
     */
    public String getValue(int i) {
        return values[i];
    }
}
