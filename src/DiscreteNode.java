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
}


}