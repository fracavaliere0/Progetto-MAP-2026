package tree;

import data.Data;

/**
 * Modella un nodo fogliare dell'albero di regressione.
 *
 * La foglia memorizza il valore di classe predetto, calcolato come media dei
 * valori di classe degli esempi coperti dal nodo.
 */
public class LeafNode extends Node {

    /** Valore dell'attributo di classe espresso nella foglia corrente. */
    Double predictedClassValue;

    /**
     * Costruisce un nodo fogliare e ne calcola il valore predetto.
     *
     * @param trainingSet Oggetto di classe Data contenente il training set completo.
     * @param beginExampleIndex Indice del primo esempio coperto nella foglia.
     * @param endExampleIndex Indice dell'ultimo esempio coperto nella foglia.
     */
    public LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
        super(trainingSet, beginExampleIndex, endExampleIndex);

        int numberOfExamples = endExampleIndex - beginExampleIndex + 1;
        if (numberOfExamples <= 0) {
            predictedClassValue = 0.0;
            return;
        }

        double sum = 0.0;
        for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
            sum += trainingSet.getClassValue(i);
        }
        predictedClassValue = sum / numberOfExamples;
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
     * Restituisce una rappresentazione testuale del nodo fogliare.
     *
     * @return Stringa formattata con le informazioni del nodo fogliare.
     */
    @Override
    public String toString() {
        return "LEAF : class=" + predictedClassValue + " Nodo: " + super.toString();
    }
}
