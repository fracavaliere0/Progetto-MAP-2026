package Server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

/**
 * Gestisce una sessione client con training set e albero indipendenti.
 *
 * <p>Il protocollo base usa oggetti Java serializzati: 0 carica una tabella,
 * 1 apprende e salva l'albero, 2 carica un archivio e 3 esegue una predizione.
 * Per 0 e 2 il client invia nomi fino alla risposta {@code Table found!}, seguita
 * da {@code OK}. I nomi degli archivi non includono il suffisso {@code .dmp}.
 * Durante la predizione {@code QUERY} precede ogni domanda e anche la risposta
 * finale {@code OK}, seguita dal valore predetto di tipo {@code Double}.</p>
 */
public class ServerOneClient extends Thread {
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Data trainingSet;
	private RegressionTree tree;
	private String fileName;

	/**
	 * Inizializza gli stream e avvia subito il thread della sessione.
	 * @param s socket gia' connesso al client; viene chiuso al termine della sessione
	 * @throws IOException se non e' possibile inizializzare gli stream di oggetti
	 */
	public ServerOneClient(Socket s) throws IOException {
		socket = s;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		start();
	}

	/**
	 * Elabora le richieste fino alla disconnessione o a un errore di protocollo.
	 * I codici sconosciuti vengono ignorati; socket e stream vengono sempre chiusi.
	 */
	public void run() {
		try (Socket sessionSocket = socket;
				ObjectInputStream input = in;
				ObjectOutputStream output = out) {
			while (true) {
				Object request = in.readObject();
				if (!(request instanceof Integer)) {
					throw new IOException("Expected an integer action code.");
				}
				switch ((Integer) request) {
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
					break;
				}
			}
		} catch (EOFException e) {
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e.toString());
		}
	}

	private String readName() throws IOException, ClassNotFoundException {
		Object name = in.readObject();
		if (!(name instanceof String)) {
			throw new IOException("Expected a table or archive name.");
		}
		return (String) name;
	}

	private void loadData() throws IOException, ClassNotFoundException {
		while (true) {
			String tableName = readName();
			try {
				trainingSet = new Data(tableName);
				fileName = tableName;
			} catch (TrainingDataException | RuntimeException e) {
				out.writeObject("No such table!");
				out.flush();
				continue;
			}
			out.writeObject("Table found!");
			out.writeObject("OK");
			out.flush();
			return;
		}
	}

	private void learnTree() throws IOException {
		if (trainingSet == null) {
			out.writeObject("No training data loaded.");
			out.flush();
			return;
		}
		String answer;
		try {
			tree = new RegressionTree(trainingSet);
			tree.salva(fileName + ".dmp");
			answer = "OK";
		} catch (RuntimeException e) {
			answer = "Error building tree: " + e.toString();
		} catch (IOException e) {
			answer = "Error saving tree: " + e.toString();
		}
		out.writeObject(answer);
		out.flush();
	}

	private void loadTree() throws IOException, ClassNotFoundException {
		while (true) {
			String archiveName = readName();
			try {
				RegressionTree loaded = RegressionTree.carica(archiveName + ".dmp");
				loaded.getNumberOfChildren();
				tree = loaded;
			} catch (IOException | ClassNotFoundException | RuntimeException e) {
				out.writeObject("The table " + archiveName + " doesn't exist!");
				out.flush();
				continue;
			}
			out.writeObject("Table found!");
			out.writeObject("OK");
			out.flush();
			return;
		}
	}

	private void predict() throws IOException, ClassNotFoundException {
		if (tree == null) {
			out.writeObject("No tree available.");
			out.flush();
			return;
		}
		out.writeObject("QUERY");
		out.flush();
		try {
			Double predictedValue = predictClass(tree);
			out.writeObject("OK");
			out.writeObject(predictedValue);
		} catch (UnknownValueException e) {
			out.writeObject(e.getMessage());
		}
		out.flush();
	}

	private Double predictClass(RegressionTree currentTree)
			throws IOException, ClassNotFoundException, UnknownValueException {
		if (currentTree.isLeaf()) {
			return currentTree.getPredictedClassValue();
		}

		out.writeObject(currentTree.formulateQuery());
		out.flush();

		Object answer = in.readObject();
		if (!(answer instanceof Integer) || (Integer) answer < 0
				|| (Integer) answer >= currentTree.getNumberOfChildren()) {
			throw new UnknownValueException(
					"The answer should be an integer between 0 and " + (currentTree.getNumberOfChildren() - 1) + "!");
		}
		out.writeObject("QUERY");
		out.flush();
		return predictClass(currentTree.getChild((Integer) answer));
	}
}
