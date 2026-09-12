package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

public class ServerOneClient extends Thread {
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Data trainingSet;
	private RegressionTree tree;
	private String fileName;

	public ServerOneClient(Socket s) throws IOException {
		socket = s;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		start();
	}

	public void run() {
		try {
			while (true) {
				int request = ((Integer) in.readObject()).intValue();
				switch (request) {
				case 0:
					loadData();
					break;
				case 1:
					learnTree();
					break;
				case 2:
					loadTree();
					break;
				case 3:
					predict();
					break;
				default:
					return;
				}
			}
		} catch (EOFException e) {
		} catch (IOException e) {
			System.out.println(e.toString());
		} catch (ClassNotFoundException e) {
			System.out.println(e.toString());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}

	private void loadData() throws IOException, ClassNotFoundException {
		String tableName = in.readObject().toString();
		try {
			trainingSet = new Data(tableName);
			fileName = tableName;
			out.writeObject("OK");
		} catch (TrainingDataException e) {
			out.writeObject(e.toString());
		}
		out.flush();
	}

	private void learnTree() throws IOException {
		if (trainingSet == null || fileName == null) {
			out.writeObject("No training set loaded");
			out.flush();
			return;
		}
		try {
			tree = new RegressionTree(trainingSet);
			tree.salva(fileName);
			out.writeObject("OK");
		} catch (IOException e) {
			out.writeObject(e.toString());
		}
		out.flush();
	}

	private void loadTree() throws IOException, ClassNotFoundException {
		String archiveName = in.readObject().toString();
		try {
			tree = RegressionTree.carica(archiveName);
			fileName = archiveName;
			out.writeObject("OK");
		} catch (IOException e) {
			out.writeObject(e.toString());
		} catch (ClassNotFoundException e) {
			out.writeObject(e.toString());
		}
		out.flush();
	}

	private void predict() throws IOException, ClassNotFoundException {
		if (tree == null) {
			out.writeObject("No tree loaded");
			out.flush();
			return;
		}
		try {
			Double predictedValue = predictClass(tree);
			out.writeObject("OK");
			out.writeObject(predictedValue);
		} catch (UnknownValueException e) {
			out.writeObject(e.toString());
		}
		out.flush();
	}

	private Double predictClass(RegressionTree currentTree)
			throws IOException, ClassNotFoundException, UnknownValueException {
		if (currentTree.isLeaf()) {
			return currentTree.getPredictedClassValue();
		}

		out.writeObject("QUERY");
		out.writeObject(currentTree.formulateQuery());
		out.flush();

		int path = ((Integer) in.readObject()).intValue();
		if (path < 0 || path >= currentTree.getNumberOfChildren()) {
			throw new UnknownValueException(
					"The answer should be an integer between 0 and " + (currentTree.getNumberOfChildren() - 1) + "!");
		}
		return predictClass(currentTree.getChild(path));
	}
}
