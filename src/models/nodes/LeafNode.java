/**
 * La classe LeafNode estende Node e modella l'entità nodo fogliare.
 */
public class LeafNode extends Node {

    /** Valore dell'attributo di classe espresso nella foglia corrente. */
    Double predictedClassValue;

    /**
     * Istanzia un oggetto invocando il costruttore della superclasse e avvalora l'attributo predictedClassValue.
     *
     * @param trainingSet Oggetto di classe Data contenente il training set completo.
     * @param beginExampleIndex Indice del primo esempio coperto nella foglia.
     * @param endExampleIndex Indice dell'ultimo esempio coperto nella foglia.
     */
    public LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
        super(trainingSet, beginExampleIndex, endExampleIndex);

        // TODO (Membro B): Avvalorare predictedClassValue come media dei valori dell'attributo
        // di classe che ricadono nella partizione (beginExampleIndex - endExampleIndex).
        // Si appoggerà alla classe Data, es: this.predictedClassValue = trainingSet.computeAverage(...);
    }

    /**
     * Restituisce il valore del membro predictedClassValue.
     *
     * @return Il valore della classe predetta.
     */
    public Double getPredictedClassValue() {
        return predictedClassValue;
    }

    /**
     * Restituisce il numero di split originanti dal nodo foglia.
     *
     * @return Sempre 0, essendo un nodo fogliare.
     */
    @Override
    public int getNumberOfChildren() {
        return 0;
    }

    /**
     * Invoca il metodo della superclasse assegnando anche il valore di classe della foglia.
     *
     * @return Stringa formattata con le informazioni del nodo fogliare.
     */
    @Override
    public String toString() {
        return "LEAF class=" + predictedClassValue + " Nodo: " + super.toString();
    }
}