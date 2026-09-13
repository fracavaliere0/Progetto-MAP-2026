package tree;

import java.io.Serializable;

import data.Data;

/**
 * Nodo di un albero di regressione associato a un intervallo inclusivo di esempi.
 * Il campo {@code variance} rappresenta la somma degli errori quadratici (SSE)
 * rispetto alla media della classe, non la varianza normalizzata.
 */
public abstract class Node implements Serializable {
	/** Contatore condiviso usato per assegnare gli identificativi dei nodi. */
	protected static int idNodeCount = 0;
	/** Identificativo assegnato alla costruzione. */
	protected int idNode;
	/** Indice iniziale incluso nell'ordinamento del training set alla costruzione. */
	protected int beginExampleIndex;
	/** Indice finale incluso nell'ordinamento del training set alla costruzione. */
	protected int endExampleIndex;
	/** Somma degli errori quadratici della classe nel sottoinsieme. */
	protected double variance;

	/**
	 * Assegna un identificativo e calcola lo SSE; per un intervallo vuoto usa zero.
	 * @param trainingSet training set da cui leggere i valori della classe
	 * @param beginExampleIndex indice iniziale incluso
	 * @param endExampleIndex indice finale incluso
	 */
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

	/**
	 * Restituisce l'identificativo del nodo.
	 * @return identificativo assegnato alla costruzione
	 */
	public int getIdNode() {
		return idNode;
	}

	/**
	 * Restituisce l'inizio dell'intervallo coperto dal nodo.
	 * @return indice iniziale incluso
	 */
	public int getBeginExampleIndex() {
		return beginExampleIndex;
	}

	/**
	 * Restituisce la fine dell'intervallo coperto dal nodo.
	 * @return indice finale incluso
	 */
	public int getEndExampleIndex() {
		return endExampleIndex;
	}

	/**
	 * Restituisce l'errore quadratico del nodo.
	 * Le sottoclassi di split restituiscono invece la somma degli SSE dei rami.
	 * @return SSE non normalizzato
	 */
	public double getVariance() {
		return variance;
	}

	/**
	 * Conta i rami uscenti dal nodo.
	 * @return numero di figli, oppure zero per una foglia
	 */
	public abstract int getNumberOfChildren();

	@Override
	public String toString() {
		return "Nodo: [Examples:" + beginExampleIndex + "-" + endExampleIndex + "] variance:" + variance;
	}
}
