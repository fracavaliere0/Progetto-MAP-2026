/**
 * La classe astratta Node modella l'astrazione dell'entità nodo (fogliare o intermedio) dell'albero di decisione.
 */
public abstract class Node {

    /** Contatore dei nodi generati nell'albero. */
    static int idNodeCount = 0;

    /** Identificativo numerico del nodo. */
    int idNode;

    /** Indice nell'array del training set del primo esempio coperto dal nodo corrente. */
    int beginExampleIndex;

    /** Indice nell'array del training set dell'ultimo esempio coperto dal nodo corrente. */
    int endExampleIndex;

    /** Valore dello SSE calcolato nel sotto-insieme di training del nodo. */
    double variance;

    /**
     * Costruttore che avvalora gli attributi primitivi di classe.
     * @param trainingSet Oggetto di classe Data contenente il training set completo.
     * @param beginExampleIndex Indice del primo esempio del sotto-insieme.
     * @param endExampleIndex Indice dell'ultimo esempio del sotto-insieme.
     */
    public Node(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
        this.idNode = idNodeCount++;
        this.beginExampleIndex = beginExampleIndex;
        this.endExampleIndex = endExampleIndex;

        // TODO: La varianza andrà calcolata invocando un metodo sull'oggetto trainingSet.
        // Attualmente impostata a 0 in attesa dell'implementazione del Membro B nella classe Data.
        this.variance = 0.0;
    }

    /**
     * Restituisce l'identificativo numerico del nodo
     * @return il valore del membro idNode.
     */
    int getIdNode() {
        return idNode;
    }

    /**
     * Restituisce l'indice del primo esempio del sotto-insieme.
     * @return il valore del membro beginExampleIndex.
     */
    int getBeginExampleIndex() {
        return beginExampleIndex;
    }

    /**
     * Restituisce l'indice dell'ultimo esempio del sotto-insieme.
     * @return il valore del membro endExampleIndex.
     */
    int getEndExampleIndex() {
        return endExampleIndex;
    }

    /**
     * Restituisce il valore dello SSE rispetto al nodo corrente.
     * @return il valore del membro variance.
     */
    double getVariance() {
        return variance;
    }

    /**
     * Restituisce il numero di nodi figli originanti dal nodo corrente.
     * Metodo astratto che verrà implementato dalle sottoclassi.
     * @return Valore del numero di nodi sottostanti.
     */
    abstract int getNumberOfChildren();

    /**
     * Concatena in un oggetto String le informazioni del nodo.
     * @return La stringa finale con indici e varianza.
     */
    @Override
    public String toString() {
        return "[Examples:" + beginExampleIndex + "-" + endExampleIndex + "] variance: " + variance;
    }

   }