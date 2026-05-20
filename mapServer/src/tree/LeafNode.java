package tree;

import data.Data;

public class LeafNode extends Node {
	private Double predictedClassValue;

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

	public Double getPredictedClassValue() {
		return predictedClassValue;
	}

	@Override
	public int getNumberOfChildren() {
		return 0;
	}

	@Override
	public String toString() {
		return "LEAF : class=" + predictedClassValue + " Nodo: " + super.toString();
	}
}
