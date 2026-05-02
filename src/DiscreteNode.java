/**
 * Modella l'entità nodo di split relativo ad un attributo indipendente discreto.
 * Estende la superclasse astratta SplitNode ed è responsabile della creazione 
 * dei rami (SplitInfo) basati sui valori discreti assunti dall'attributo.
 */
public class DiscreteNode extends SplitNode {
    
    /**
     * Istanzia un oggetto invocando il costruttore della superclasse con il parametro attribute.
     *
     * @param trainingSet       training set complessivo
     * @param beginExampleIndex indice nell'array del training set del primo esempio
     * @param endExampleIndex   indice nell'array del training set dell'ultimo esempio
     * @param attribute         attributo indipendente discreto sul quale si definisce lo split
     */
    public DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
    }

    /**
     * Istanzia oggetti SplitInfo con ciascuno dei valori discreti dell'attributo 
     * relativamente al sotto-insieme di training corrente, popolando l'array mapSplit.
     *
     * @param trainingSet       training set complessivo
     * @param beginExampleIndex indice del primo esempio del sotto-insieme di training
     * @param endExampleIndex   indice dell'ultimo esempio del sotto-insieme di training
     * @param attribute         attributo indipendente sul quale si definisce lo split
     */
    void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
        int valoriDistinti = 1;
        for (int i = beginExampleIndex; i < endExampleIndex; i++) {
            Object tempVal = trainingSet.getExplanatoryValue(i, attribute.getIndex());
            Object nextVal = trainingSet.getExplanatoryValue(i+1, attribute.getIndex());
            if (!tempVal.equals(nextVal)) {
                valoriDistinti++;
            }
        }
            
        mapSplit = new SplitInfo[valoriDistinti];
        int splitIndex = 0;
        int currentBegin = beginExampleIndex;

        for (int i = beginExampleIndex; i < endExampleIndex; i++) {
            Object tempVal = trainingSet.getExplanatoryValue(i, attribute.getIndex());
            Object nextVal = trainingSet.getExplanatoryValue(i+1, attribute.getIndex());
            if (!tempVal.equals(nextVal)) {
                mapSplit[splitIndex] = new SplitInfo(tempVal, currentBegin, i, 0);

                splitIndex++;
                currentBegin = i + 1;
            }
        }
            
        // Estraggo il valore dell'ultimo blocco (basta leggere l'ultimo elemento in assoluto)
        Object lastVal = trainingSet.getExplanatoryValue(endExampleIndex, attribute.getIndex());

        // Creo lo SplitInfo finale e lo salvo
        mapSplit[splitIndex] = new SplitInfo(lastVal, currentBegin, endExampleIndex, 0);
    }

    /**
     * Effettua il confronto del valore in input rispetto al valore contenuto 
     * nell'attributo splitValue di ciascuno degli oggetti SplitInfo in mapSplit.
     *
     * @param value valore discreto dell'attributo che si vuole testare
     * @return l'identificativo dello split (indice dell'array mapSplit) con cui il test è positivo, -1 se non trovato
     */
    public int testCondition(Object value) {
        for (int i = 0; i < mapSplit.length; i++) {
            Object tempVal = mapSplit[i].getSplitValue();
            if (tempVal.equals(value)) {
                return i;
            }
        }
        return -1;
    }

}