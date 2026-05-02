package tree;

import data.Attribute;
import data.Data;

/**
 * Modella un nodo interno dell'albero che suddivide gli esempi in piu' rami.
 *
 * La classe memorizza l'attributo usato per lo split, l'insieme dei rami
 * prodotti e la varianza complessiva risultante dalla partizione.
 */
abstract class SplitNode extends Node {

    /**
     * Raccoglie le informazioni associate a un singolo ramo dello split.
     */
    class SplitInfo {
        /** Valore che definisce lo split. */
        Object splitValue;
        /** Indice di inizio della partizione coperta dallo split. */
        int beginIndex;
        /** Indice di fine della partizione coperta dallo split. */
        int endIndex;
        /** Numero di split (nodi figli) originanti dal nodo corrente. */
        int numberChild;
        /** Operatore matematico che definisce il test nel nodo corrente. */
        String comparator = "=";

        /**
         * Costruttore che avvalora gli attributi di classe per split a valori discreti.
         *
         * @param splitValue Valore che definisce lo split.
         * @param beginIndex Indice di inizio della partizione.
         * @param endIndex Indice di fine della partizione.
         * @param numberChild Numero del figlio originato.
         */
        SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild) {
            this.splitValue = splitValue;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.numberChild = numberChild;
        }

        /**
         * Costruttore che avvalora gli attributi di classe per split generici (es. continui).
         *
         * @param splitValue Valore che definisce lo split.
         * @param beginIndex Indice di inizio della partizione.
         * @param endIndex Indice di fine della partizione.
         * @param numberChild Numero del figlio originato.
         * @param comparator Operatore matematico utilizzato nel test.
         */
        SplitInfo(Object splitValue, int beginIndex, int endIndex, int numberChild, String comparator) {
            this.splitValue = splitValue;
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
            this.numberChild = numberChild;
            this.comparator = comparator;
        }

        /**
         * Restituisce l'indice di inizio della partizione.
         *
         * @return Il valore di beginIndex.
         */
        int getBeginindex() {
            return beginIndex;
        }

        /**
         * Restituisce l'indice di fine della partizione.
         *
         * @return Il valore di endIndex.
         */
        int getEndIndex() {
            return endIndex;
        }

        /**
         * Restituisce il valore dello split.
         *
         * @return Il valore di splitValue come Object.
         */
        Object getSplitValue() {
            return splitValue;
        }

        /**
         * Concatena in un oggetto String i valori interni di SplitInfo.
         *
         * @return La stringa finale con le informazioni.
         */
        @Override
        public String toString() {
            return "child " + numberChild + " split value" + comparator + splitValue + "[Examples:" + beginIndex + "-" + endIndex + "]";
        }

        /**
         * Restituisce l'operatore matematico che definisce il test.
         *
         * @return L'operatore matematico.
         */
        String getComparator() {
            return comparator;
        }
    }

    /** Oggetto Attribute che modella l'attributo indipendente sul quale lo split è generato. */
    Attribute attribute;

    /** Array per memorizzare gli split candidati. */
    SplitInfo mapSplit[];

    /** Attributo che contiene il valore di varianza a seguito del partizionamento. */
    double splitVariance;

    /**
     * Genera le informazioni necessarie per ciascuno split candidato.
     *
     * @param trainingSet Training set complessivo.
     * @param beginExampelIndex Indice di inizio.
     * @param endExampleIndex Indice di fine.
     * @param attribute Attributo indipendente.
     */
    abstract void setSplitInfo(Data trainingSet, int beginExampelIndex, int endExampleIndex, Attribute attribute);

    /**
     * Modella la condizione di test che seleziona un ramo dello split.
     *
     * @param value Valore dell'attributo che si vuole testare.
     * @return Identificativo del ramo di split.
     */
    abstract int testCondition(Object value);

    /**
     * Costruisce un nodo di split sul sottoinsieme specificato.
     *
     * Il costruttore ordina il training set rispetto all'attributo scelto,
     * popola i rami candidati e calcola la varianza totale indotta dallo split.
     *
     * @param trainingSet Training set complessivo.
     * @param beginExampleIndex Indice estremo iniziale.
     * @param endExampleIndex Indice estremo finale.
     * @param attribute Attributo indipendente su cui si definisce lo split.
     */
    SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex);
        this.attribute = attribute;
        trainingSet.sort(attribute, beginExampleIndex, endExampleIndex); // order by attribute
        setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);

        //compute variance
        splitVariance = 0;
        for(int i = 0; i < mapSplit.length; i++) {
            double localVariance = new LeafNode(trainingSet, mapSplit[i].getBeginindex(), mapSplit[i].getEndIndex()).getVariance();
            splitVariance += (localVariance);
        }
    }

    /**
     * Restituisce l'oggetto per l'attributo usato per lo split.
     *
     * @return L'attributo dello split.
     */
    Attribute getAttribute() {
        return attribute;
    }

    /**
     * Restituisce la varianza complessiva dello split corrente.
     *
     * @return Il valore di splitVariance.
     */
    double getVariance() {
        return splitVariance;
    }

    /**
     * Restituisce il numero dei rami originanti nel nodo corrente.
     *
     * @return La lunghezza dell'array mapSplit.
     */
    int getNumberOfChildren() {
        return mapSplit.length;
    }

    /**
     * Restituisce le informazioni per il ramo indicizzato da child.
     *
     * @param child Indice del ramo in mapSplit.
     * @return Oggetto SplitInfo associato.
     */
    SplitInfo getSplitInfo(int child) {
        return mapSplit[child];
    }

    /**
     * Costruisce una rappresentazione testuale delle condizioni associate ai rami.
     *
     * @return La query formattata per la predizione.
     */
    String formulateQuery() {
        String query = "";
        for(int i = 0; i < mapSplit.length; i++)
            query += (i + ":" + attribute + mapSplit[i].getComparator() + mapSplit[i].getSplitValue()) + "\n";
        return query;
    }

    /**
     * Restituisce una rappresentazione testuale del nodo di split.
     *
     * @return Stringa finale con i dettagli del nodo.
     */
    @Override
    public String toString() {
        String v = "SPLIT : attribute=" + attribute + " " + super.toString() + " Split Variance: " + getVariance() + "\n";

        for(int i = 0; i < mapSplit.length; i++) {
            v += "\t" + mapSplit[i] + "\n";
        }

        return v;
    }
}
