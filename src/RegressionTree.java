/**
 * Modella l'intero albero di decisione come insieme di sotto-alberi.
 * Gestisce l'induzione dell'albero a partire dai dati di training e la sua struttura ricorsiva.
 */
class RegressionTree {

    /** Radice del sotto-albero corrente */
    private Node root;
    
    /** Array di sotto-alberi originanti nel nodo root: vi è un elemento per ogni figlio */
    private RegressionTree[] childTree;

    /**
     * Istanzia un sotto-albero vuoto dell'intero albero.
     */
    public RegressionTree() {

    }
    
    /**
     * Istanzia un sotto-albero dell'intero albero e avvia l'induzione dell'albero 
     * dagli esempi di training in input.
     *
     * @param trainingSet training set complessivo
     */
    RegressionTree(Data trainingSet){
        learnTree(trainingSet,0,trainingSet.getNumberOfExamples()-1,trainingSet.getNumberOfExamples()*10/100);
    }

    /**
     * Verifica se il sotto-insieme corrente può essere coperto da un nodo foglia
     * controllando che il numero di esempi sia minore o uguale a numberOfExamplesPerLeaf.
     *
     * @param trainingSet             training set complessivo
     * @param begin                   indice iniziale del sotto-insieme di training
     * @param end                     indice finale del sotto-insieme di training
     * @param numberOfExamplesPerLeaf numero massimo di esempi che una foglia può contenere
     * @return true se il nodo deve diventare una foglia, false altrimenti
     */
    boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
        int numberOfExamples = end - begin + 1;
        if (numberOfExamples <= numberOfExamplesPerLeaf) {
            return true;
        }
        return false;
    }

    /**
     * Determina il miglior nodo di split per il sotto-insieme di training.
     * Prova tutti gli attributi discreti, calcola la varianza minore e ordina 
     * la porzione di dataset rispetto all'attributo selezionato.
     *
     * @param trainingSet training set complessivo
     * @param begin       indice iniziale del sotto-insieme di training
     * @param end         indice finale del sotto-insieme di training
     * @return il miglior nodo di split per il sotto-insieme corrente
     */
    SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
        SplitNode bestNode = null;
        double minVariance = Double.MAX_VALUE;

        for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
            DiscreteAttribute currentAttribute = (DiscreteAttribute) trainingSet.getExplanatoryAttribute(i);
            DiscreteNode tempNode = new DiscreteNode(trainingSet, begin, end, currentAttribute);
        
            double currentVariance = tempNode.getVariance();
        
            // Se è la varianza più bassa trovata finora (o se è il primo giro), assegno proprio tempNode a bestNode
            if (bestNode == null || currentVariance < minVariance) {
                minVariance = currentVariance;
                bestNode = tempNode;
            }
        }

        // Una volta trovato il bestNode, ordino i dati del dataset in base a quell'attributo
        if (bestNode != null) {
            trainingSet.sort(bestNode.getAttribute(), begin, end);
        }
        return bestNode;
    }

        
    /**
     * Genera un sotto-albero con il sotto-insieme di input istanziando un nodo 
     * fogliare o un nodo di split. Richiama se stesso ricorsivamente sui figli.
     *
     * @param trainingSet             training set complessivo
     * @param begin                   indice iniziale del sotto-insieme di training
     * @param end                     indice finale del sotto-insieme di training
     * @param numberOfExamplesPerLeaf soglia limite di esempi per istanziare una foglia
     */
    void learnTree(Data trainingSet,int begin, int end,int numberOfExamplesPerLeaf){
        if (isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)){
            //determina la classe che compare più frequentemente nella partizione corrente
            root=new LeafNode(trainingSet,begin,end);
        }
        else //split node
        {
            root=determineBestSplitNode(trainingSet, begin, end);
            
            if (root.getNumberOfChildren()>1){
                childTree=new RegressionTree[root.getNumberOfChildren()];
                for(int i=0;i<root.getNumberOfChildren();i++){
                    childTree[i]=new RegressionTree();
                    childTree[i].learnTree(trainingSet, ((SplitNode)root).getSplitInfo(i).beginIndex, ((SplitNode)root).getSplitInfo(i).endIndex, numberOfExamplesPerLeaf);
                }
            }
            else
                root=new LeafNode(trainingSet,begin,end);   
        }
    }
            

    /**
     * Stampa a console le informazioni dell'intero albero avvolte in un'intestazione.
     */
    void printTree(){
        System.out.println("********* TREE **********\n");
        System.out.println(toString());
        System.out.println("*************************\n");
    }
        
    /**
     * Concatena in una stringa tutte le informazioni della radice (root) e dei 
     * sotto-alberi figli (childTree) invocando ricorsivamente i loro metodi toString().
     *
     * @return stringa testuale con la struttura dell'albero
     */
    public String toString(){
        String tree=root.toString()+"\n";
            
        if( root instanceof LeafNode){
            
        }
        else //split node
        {
            for(int i=0;i<childTree.length;i++)
                tree +=childTree[i];
            }
            return tree;
    }
        
}
		
