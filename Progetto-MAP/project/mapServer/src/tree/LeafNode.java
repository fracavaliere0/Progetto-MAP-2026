package tree;

import data.Data;

/** Foglia che predice la media dei valori di classe degli esempi coperti. */
public class LeafNode extends Node {
	/** Media della classe, oppure zero per un intervallo vuoto. */
	private Double predictedClassValue;

	/**
	 * Calcola media e SSE della classe nell'intervallo indicato.
	 * @param trainingSet training set da cui leggere gli esempi
	 * @param beginExampleIndex indice iniziale incluso
	 * @param endExampleIndex indice finale incluso
	 */
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

	/**
	 * Restituisce il valore predetto dalla foglia.
	 * @return media della classe, oppure 0.0 per un intervallo vuoto
	 */
	public Double getPredictedClassValue() {
		return predictedClassValue;
	}

	@Override
	public int getNumberOfChildren() {
		return 0;
	}

	@Override
	public String toString() {
		return "LEAF : class=" + predictedClassValue + " " + super.toString();
	}
}
