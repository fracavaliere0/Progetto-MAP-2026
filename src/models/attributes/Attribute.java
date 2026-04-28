/**
 * La classe astratta Attribute modella un generico attributo (discreto o
 * continuo).
 *
 * Fornisce le basi per memorizzare il nome e l'identificativo numerico.
 */

public abstract class Attribute {
    /** Nome simbolico dell'attributo. */
    private String name;
    /** Identificativo numerico dell'attributo. */
    private int index;

    /**
     * Costruttore di classe. Inizializza il nome e l'indice dell'attributo.
     * @param name Nome simbolico dell'attributo.
     * 
     * @param index Identificativo numerico dell'attributo.
     */
    public Attribute(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Restituisce il nome dell'attributo.
     *
     * @return Il nome simbolico dell'attributo.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Restituisce l'indice dell'attributo.
     * 
     * @return L'identificativo numerico dell'attributo.
     */
    public int getIndex() {
        return this.index;
    }
}