package tree;

import java.util.ArrayList;
import java.util.List;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;

/** Modella un nodo di split basato su un attributo continuo. */
public class ContinuousNode extends SplitNode {

    /**
     * Costruisce un nodo continuo e determina il miglior valore di soglia.
     */
    public ContinuousNode(Data trainingSet, int beginExampleIndex,
            int endExampleIndex, ContinuousAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
    }

    /**
     * Codice fornito dal docente per determinare la migliore soglia continua.
     */
    @Override
    public void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute){
            //Update mapSplit defined in SplitNode -- contiene gli indici del partizionamento
            Double currentSplitValue= (Double)trainingSet.getExplanatoryValue(beginExampleIndex,attribute.getIndex());
            double bestInfoVariance=0;
            List <SplitInfo> bestMapSplit=null;

            for(int i=beginExampleIndex+1;i<=endExampleIndex;i++){
                Double value=(Double)trainingSet.getExplanatoryValue(i,attribute.getIndex());
                if(value.doubleValue()!=currentSplitValue.doubleValue()){
                //  System.out.print(currentSplitValue +" var ");
                    double localVariance=new LeafNode(trainingSet, beginExampleIndex,i-1).getVariance();
                    double candidateSplitVariance=localVariance;
                    localVariance=new LeafNode(trainingSet, i,endExampleIndex).getVariance();
                    candidateSplitVariance+=localVariance;
                    //System.out.println(candidateSplitVariance);
                    if(bestMapSplit==null){
                        bestMapSplit=new ArrayList<SplitInfo>();
                        bestMapSplit.add(new SplitInfo(currentSplitValue, beginExampleIndex, i-1,0,"<="));
                        bestMapSplit.add(new SplitInfo(currentSplitValue, i, endExampleIndex,1,">"));
                        bestInfoVariance=candidateSplitVariance;
                    }
                    else{

                        if(candidateSplitVariance<bestInfoVariance){
                            bestInfoVariance=candidateSplitVariance;
                            bestMapSplit.set(0, new SplitInfo(currentSplitValue, beginExampleIndex, i-1,0,"<="));
                            bestMapSplit.set(1, new SplitInfo(currentSplitValue, i, endExampleIndex,1,">"));
                        }
                    }
                    currentSplitValue=value;
                }
            }
            // Il codice fornito presume almeno due valori distinti. Se l'attributo
            // è costante, rappresentiamo un unico ramo così RegressionTree userà
            // correttamente una foglia invece di dereferenziare una lista nulla.
            if (bestMapSplit == null) {
                mapSplit = new ArrayList<SplitInfo>();
                mapSplit.add(new SplitInfo(currentSplitValue, beginExampleIndex,
                        endExampleIndex, 0, "="));
                return;
            }

            mapSplit=bestMapSplit;
            //rimuovo split inutili (che includono tutti gli esempi nella stessa partizione)

            if((mapSplit.get(1).beginIndex==mapSplit.get(1).getEndIndex())){
                mapSplit.remove(1);

            }

     }

    /** Returns the branch selected by a numeric value, or -1 for invalid input. */
    @Override
    public int testCondition(Object value) {
        if (!(value instanceof Number) || mapSplit == null || mapSplit.isEmpty())
            return -1;

        double numericValue = ((Number) value).doubleValue();
        double threshold = ((Number) mapSplit.get(0).getSplitValue()).doubleValue();
        return numericValue <= threshold ? 0 : (mapSplit.size() > 1 ? 1 : -1);
    }

    /** Specializes the textual representation for a continuous split. */
    @Override
    public String toString() {
        return "CONTINUOUS " + super.toString();
    }
}
