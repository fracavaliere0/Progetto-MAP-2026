package data;

import java.io.Serializable;

/**
 * Modella un attributo generico.
 */
public abstract class Attribute implements Serializable {
    private static final long serialVersionUID = 1L;
    /** Nome simbolico dell'attributo. */
    private String name;

    /** Identificativo numerico dell'attributo. */
    private int index;

    /**
     * Costruisce un attributo.
     *
     * @param name Nome simbolico dell'attributo.
     * @param index Identificativo numerico dell'attributo.
     */
    public Attribute(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Restituisce il nome simbolico.
     *
     * @return nome simbolico dell'attributo.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Restituisce l'identificativo numerico.
     *
     * @return identificativo numerico dell'attributo.
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Restituisce il nome simbolico.
     *
     * @return nome simbolico dell'attributo.
     */
    @Override
    public String toString() {
        return name;
    }
}
