package tree;

import java.io.Serializable;

import data.Data;

public abstract class Node implements Serializable {
	protected static int idNodeCount = 0;
	protected int idNode;
	protected int beginExampleIndex;
	protected int endExampleIndex;
	protected double variance;

	public Node(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
		this.idNode = idNodeCount++;
		this.beginExampleIndex = beginExampleIndex;
		this.endExampleIndex = endExampleIndex;

		int numberOfExamples = endExampleIndex - beginExampleIndex + 1;
		if (numberOfExamples <= 0) {
			this.variance = 0.0;
			return;
		}

		double sum = 0.0;
		for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
			sum += trainingSet.getClassValue(i);
		}

		double average = sum / numberOfExamples;
		this.variance = 0.0;
		for (int i = beginExampleIndex; i <= endExampleIndex; i++) {
			double difference = trainingSet.getClassValue(i) - average;
			this.variance += difference * difference;
		}
	}

	public int getIdNode() {
		return idNode;
	}

	public int getBeginExampleIndex() {
		return beginExampleIndex;
	}

	public int getEndExampleIndex() {
		return endExampleIndex;
	}

	public double getVariance() {
		return variance;
	}

	public abstract int getNumberOfChildren();

	@Override
	public String toString() {
		return "[Examples:" + beginExampleIndex + "-" + endExampleIndex + "] variance: " + variance;
	}
}
