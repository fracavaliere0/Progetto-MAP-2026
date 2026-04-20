import java.util.Set;
import java.util.HashSet;

/*
 * La classe DiscreteAttribute estende Attribute e rappresenta un attributo discreto.
 * Gestisce un insieme di valori simbolici definiti tramite un array.
 */
public class DiscreteAttribute extends Attribute {
    /* Array di oggetti String per i valori discreti. */
    private String[] values;

    /*
     * Costruttore: invoca il costruttore della super-classe e inizializza l'array
     * values.
     * 
     * @param name Nome dell'attributo.
     * 
     * @param index Indice dell'attributo.
     * 
     * @param values Array di stringhe con i valori possibili.
     */
    public DiscreteAttribute(String name, int index, String[] values) {
        // Chiama il costruttore di Attribute
        super(name, index);
        this.values = values;
    }

    /*
     * Restituisce la cardinalità dell'array values.
     * 
     * @return Numero di valori distinti.
     */
    public int getNumberOfDistinctValues() {
        Set<String> uniqueValues = new HashSet<>();

        for (String s : values) {
            uniqueValues.add(s);
        }

        return uniqueValues.size();
    }

    /*
     * Restituisce il valore in posizione i.
     * 
     * @param i indice del valore.
     * 
     * @return Il valore corrispondente.
     */
    public String getValue(int i) {
        if (i >= values.length || i < 0) {
            throw new IndexOutOfBoundsException(
                    "Indice " + i + "non valido per la lunghezza della stringa");
        }
        return values[i];
    }
}
