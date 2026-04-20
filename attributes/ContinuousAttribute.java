/*
 * La classe ContinuousAttribute estende Attribute e rappresenta un attributo continuo.
 * Modella un attributo numerico passando nome e indice alla superclasse.
 */
public class ContinuousAttribute extends Attribute {

    /*
     * Costruttore: invoca il costruttore della super-classe.
     * @param name Nome simbolico dell'attributo.
     * @param index Identificativo numerico dell'attributo.
     */
    public ContinuousAttribute(String name, int index) {
        super(name, index);
    }
}
