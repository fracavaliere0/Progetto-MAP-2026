package tree;

import java.util.ArrayList;

import data.Attribute;
import data.Data;
import data.DiscreteAttribute;

/** Nodo con un ramo per ciascun valore discreto osservato nel sottoinsieme. */
public class DiscreteNode extends SplitNode {

	/**
	 * Ordina sul posto il sottoinsieme e raggruppa gli esempi per valore discreto.
	 * @param trainingSet training set da partizionare
	 * @param beginExampleIndex indice iniziale incluso di un intervallo non vuoto
	 * @param endExampleIndex indice finale incluso
	 * @param attribute attributo categorico su cui costruire i rami
	 */
	public DiscreteNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, DiscreteAttribute attribute) {
		super(trainingSet, beginExampleIndex, endExampleIndex, attribute);
	}

	void setSplitInfo(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute) {
		int valoriDistinti = 1;
		for (int i = beginExampleIndex; i < endExampleIndex; i++) {
			Object tempVal = trainingSet.getExplanatoryValue(i, attribute.getIndex());
			Object nextVal = trainingSet.getExplanatoryValue(i + 1, attribute.getIndex());
			if (!tempVal.equals(nextVal)) {
				valoriDistinti++;
			}
		}

		mapSplit = new ArrayList<SplitInfo>(valoriDistinti);
		int splitIndex = 0;
		int currentBegin = beginExampleIndex;

		for (int i = beginExampleIndex; i < endExampleIndex; i++) {
			Object tempVal = trainingSet.getExplanatoryValue(i, attribute.getIndex());
			Object nextVal = trainingSet.getExplanatoryValue(i + 1, attribute.getIndex());
			if (!tempVal.equals(nextVal)) {
				mapSplit.add(new SplitInfo(tempVal, currentBegin, i, splitIndex));

				splitIndex++;
				currentBegin = i + 1;
			}
		}

		Object lastVal = trainingSet.getExplanatoryValue(endExampleIndex, attribute.getIndex());
		mapSplit.add(new SplitInfo(lastVal, currentBegin, endExampleIndex, splitIndex));
	}

	/**
	 * Cerca il ramo associato a un valore discreto.
	 * @param value valore dell'attributo da confrontare con quelli osservati
	 * @return indice del ramo, oppure -1 se il valore non e' presente
	 */
	@Override
	public int testCondition(Object value) {
		for (int i = 0; i < mapSplit.size(); i++) {
			Object tempVal = mapSplit.get(i).getSplitValue();
			if (tempVal.equals(value)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		return "DISCRETE " + super.toString();
	}
}
