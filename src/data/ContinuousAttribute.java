package data;

/**
 * Rappresenta un attributo continuo.
 *
 * Questa classe estende {@link Attribute} senza introdurre nuovi campi, perche'
 * per un attributo continuo sono sufficienti nome e indice.
 */
public class ContinuousAttribute extends Attribute {

    /**
     * Costruisce un attributo continuo.
     *
     * @param name Nome simbolico dell'attributo.
     * @param index Identificativo numerico dell'attributo.
     */
    public ContinuousAttribute(String name, int index) {
        super(name, index);
    }
}
