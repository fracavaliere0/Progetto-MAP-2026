package tree;

import data.Attribute;
import data.Data;
import data.DiscreteAttribute;

/**
 * Modella un nodo di split basato su un attributo discreto.
 *
 * Ogni ramo dello split corrisponde a uno dei valori distinti assunti
 * dall'attributo nel sottoinsieme corrente del training set.
 */
public class DiscreteNode extends SplitNode {
    
    /**
     * Costruisce un nodo di split discreto.
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
     * Popola {@code mapSplit} con un ramo per ogni valore distinto dell'attributo
     * nel sottoinsieme corrente.
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
                mapSplit[splitIndex] = new SplitInfo(tempVal, currentBegin, i, splitIndex);

                splitIndex++;
                currentBegin = i + 1;
            }
        }
            
        // Estraggo il valore dell'ultimo blocco (basta leggere l'ultimo elemento in assoluto)
        Object lastVal = trainingSet.getExplanatoryValue(endExampleIndex, attribute.getIndex());

        // Creo lo SplitInfo finale e lo salvo
        mapSplit[splitIndex] = new SplitInfo(lastVal, currentBegin, endExampleIndex, splitIndex);
    }

    /**
     * Effettua il confronto del valore in input rispetto al valore contenuto
     * nell'attributo splitValue di ciascuno degli oggetti SplitInfo in mapSplit.
     *
     * @param value valore discreto dell'attributo che si vuole testare
     * @return l'identificativo dello split (indice dell'array mapSplit) con cui il
     *         test e' positivo, oppure {@code -1} se non viene trovato alcun ramo.
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

    /**
     * Specializza la stampa del nodo di split per attributi discreti.
     *
     * @return Stringa formattata con le informazioni del nodo discreto.
     */
    @Override
    public String toString() {
        return "DISCRETE " + super.toString();
    }

}
