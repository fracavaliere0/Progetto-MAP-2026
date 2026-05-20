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

public class RegressionTree implements Serializable {
	private Node root;
	private RegressionTree[] childTree;

	public RegressionTree() {

	}

	public RegressionTree(Data trainingSet) {
		learnTree(trainingSet, 0, trainingSet.getNumberOfExamples() - 1,
				trainingSet.getNumberOfExamples() * 10 / 100);
	}

	boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		int numberOfExamples = end - begin + 1;
		if (numberOfExamples <= numberOfExamplesPerLeaf) {
			return true;
		}
		return false;
	}

	SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
		TreeSet<SplitNode> ts = new TreeSet<SplitNode>();

		for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
			Attribute a = trainingSet.getExplanatoryAttribute(i);
			SplitNode currentNode = null;
			if (a instanceof DiscreteAttribute) {
				DiscreteAttribute attribute = (DiscreteAttribute) trainingSet.getExplanatoryAttribute(i);
				currentNode = new DiscreteNode(trainingSet, begin, end, attribute);
			} else {
				if (hasDistinctContinuousValues(trainingSet, begin, end, a)) {
					ContinuousAttribute attribute = (ContinuousAttribute) trainingSet.getExplanatoryAttribute(i);
					currentNode = new ContinuousNode(trainingSet, begin, end, attribute);
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

	void learnTree(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		if (isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)) {
			root = new LeafNode(trainingSet, begin, end);
		} else {
			SplitNode bestSplitNode = determineBestSplitNode(trainingSet, begin, end);
			if (bestSplitNode == null) {
				root = new LeafNode(trainingSet, begin, end);
				return;
			}

			root = bestSplitNode;

			if (root.getNumberOfChildren() > 1) {
				childTree = new RegressionTree[root.getNumberOfChildren()];
				for (int i = 0; i < root.getNumberOfChildren(); i++) {
					childTree[i] = new RegressionTree();
					childTree[i].learnTree(trainingSet, ((SplitNode) root).getSplitInfo(i).beginIndex,
							((SplitNode) root).getSplitInfo(i).endIndex, numberOfExamplesPerLeaf);
				}
			} else
				root = new LeafNode(trainingSet, begin, end);
		}
	}

	public boolean isLeaf() {
		return root instanceof LeafNode;
	}

	public Double getPredictedClassValue() {
		return ((LeafNode) root).getPredictedClassValue();
	}

	public String formulateQuery() {
		return ((SplitNode) root).formulateQuery();
	}

	public int getNumberOfChildren() {
		return root.getNumberOfChildren();
	}

	public RegressionTree getChild(int index) {
		return childTree[index];
	}

	public void printTree() {
		System.out.println("********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}

	public void printRules() {
		System.out.println("********* RULES **********");
		printRules("");
		System.out.println("*************************");
	}

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
			String condition = splitRoot.getAttribute().getName() + splitRoot.getSplitInfo(i).getComparator()
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

	public String toString() {
		String tree = root.toString() + "\n";

		if (root instanceof LeafNode) {

		} else {
			for (int i = 0; i < childTree.length; i++)
				tree += childTree[i];
		}
		return tree;
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
