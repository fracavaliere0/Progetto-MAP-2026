/**
 * 
 */
public class DiscreteNode extends SplitNode {
    
    public DiscreteNode(Data trainingSet,int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
        super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
}

void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute) {
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