package tree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TreeSet;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;
import utility.Keyboard;

/**
 * Modella l'albero di regressione come insieme ricorsivo di sotto-alberi.
 *
 * Ogni istanza mantiene il nodo radice del sotto-albero corrente ed eventuali
 * sotto-alberi figli generati da uno split.
 */
public class RegressionTree implements Serializable {

    /** Radice del sotto-albero corrente. */
    private Node root;
    
    /** Array dei sotto-alberi originanti dalla radice corrente. */
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
    public RegressionTree(Data trainingSet){
        learnTree(trainingSet,0,trainingSet.getNumberOfExamples()-1,trainingSet.getNumberOfExamples()*10/100);
    }

    /**
     * Verifica se il sottoinsieme corrente puo' essere coperto da un nodo foglia.
     *
     * Il criterio adottato confronta il numero di esempi compresi tra gli indici
     * {@code begin} e {@code end} con la soglia minima richiesta per una foglia.
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
     *
     * Il metodo prova tutti gli attributi discreti, seleziona il nodo con
     * varianza di split minima e ordina il sottoinsieme corrente rispetto
     * all'attributo scelto.
     *
     * @param trainingSet training set complessivo
     * @param begin       indice iniziale del sotto-insieme di training
     * @param end         indice finale del sotto-insieme di training
     * @return il miglior nodo di split per il sotto-insieme corrente
     */
    SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
        TreeSet<SplitNode> ts = new TreeSet<SplitNode>();

        for(int i=0;i<trainingSet.getNumberOfExplanatoryAttributes();i++){
            Attribute a=trainingSet.getExplanatoryAttribute(i);
            SplitNode currentNode = null;
            if(a instanceof DiscreteAttribute){
                DiscreteAttribute attribute=(DiscreteAttribute)trainingSet.getExplanatoryAttribute(i);
                currentNode = new DiscreteNode(trainingSet,begin,end,attribute);
            }
            else
            {
                if (hasDistinctContinuousValues(trainingSet, begin, end, a)) {
                    ContinuousAttribute attribute=(ContinuousAttribute)trainingSet.getExplanatoryAttribute(i);
                    currentNode = new ContinuousNode(trainingSet,begin,end,attribute);
                }
            }
            if (currentNode != null) {
                ts.add(currentNode);
            }
        }

        if (ts.isEmpty()) {
            return null;
        }

        SplitNode bestNode = ts.first();
        trainingSet.sort(bestNode.getAttribute(), begin, end);
        return bestNode;
    }

    private boolean hasDistinctContinuousValues(Data trainingSet, int begin, int end, Attribute attribute) {
        Double firstValue = (Double) trainingSet.getExplanatoryValue(begin, attribute.getIndex());
        for (int i = begin + 1; i <= end; i++) {
            Double currentValue = (Double) trainingSet.getExplanatoryValue(i, attribute.getIndex());
            if (currentValue.doubleValue() != firstValue.doubleValue()) {
                return true;
            }
        }
        return false;
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
            SplitNode bestSplitNode = determineBestSplitNode(trainingSet, begin, end);
            if (bestSplitNode == null) {
                root = new LeafNode(trainingSet, begin, end);
                return;
            }

            root=bestSplitNode;
            
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
    public void printTree(){
        System.out.println("********* TREE **********\n");
        System.out.println(toString());
        System.out.println("*************************\n");
    }

    /**
     * Stampa le regole dell'albero dalla radice alle foglie.
     */
    public void printRules() {
        System.out.println("********* RULES **********");
        printRules("");
        System.out.println("*************************");
    }

    /**
     * Supporta la stampa ricorsiva delle regole del sotto-albero corrente.
     *
     * @param current Condizioni accumulate lungo il cammino dalla radice.
     */
    void printRules(String current) {
        if (root == null) {
            return;
        }

        if (root instanceof LeafNode) {
            if (current.equals("")) {
                System.out.println("Class=" + ((LeafNode) root).getPredictedClassValue());
            } else {
                System.out.println(current + " ==> Class=" + ((LeafNode) root).getPredictedClassValue());
            }
            return;
        }

        SplitNode splitRoot = (SplitNode) root;
        for (int i = 0; i < childTree.length; i++) {
            String condition = splitRoot.getAttribute().getName()
                    + splitRoot.getSplitInfo(i).getComparator()
                    + splitRoot.getSplitInfo(i).getSplitValue();

            String nextCurrent;
            if (current.equals("")) {
                nextCurrent = condition;
            } else {
                nextCurrent = current + " AND " + condition;
            }

            childTree[i].printRules(nextCurrent);
        }
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

    public Double predictClass()throws UnknownValueException {
        if(root instanceof LeafNode)
            return ((LeafNode) root).getPredictedClassValue();
        else {
            int risp;
            System.out.println(((SplitNode)root).formulateQuery());
            risp=Keyboard.readInt();
            if(risp==-1 || risp>=root.getNumberOfChildren())
                throw new UnknownValueException("The answer should be an integer between 0 and " +(root.getNumberOfChildren()-1)+"!");
            else
                return childTree[risp].predictClass();
        }
    }

	public void salva(String nomeFile) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nomeFile));
		out.writeObject(this);
		out.close();
	}

	public static RegressionTree carica(String nomeFile)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(nomeFile));
		RegressionTree tree = (RegressionTree) in.readObject();
		in.close();
		return tree;
	}
        
}
		
