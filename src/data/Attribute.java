package data;

import java.io.Serializable;

/**
 * Modella un attributo generico del dataset.
 *
 * La classe memorizza il nome simbolico e l'indice numerico di un attributo,
 * lasciando alle sottoclassi la specializzazione per il caso discreto o
 * continuo.
 */

public abstract class Attribute implements Serializable {
    /** Nome simbolico dell'attributo. */
    private String name;
    /** Identificativo numerico dell'attributo. */
    private int index;

    /**
     * Inizializza il nome e l'indice dell'attributo.
     *
     * @param name Nome simbolico dell'attributo.
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

    /**
     * Restituisce il nome dell'attributo in forma testuale.
     *
     * @return Il nome simbolico dell'attributo.
     */
    @Override
    public String toString() {
        return name;
    }
}
