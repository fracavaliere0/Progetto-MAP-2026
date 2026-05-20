import java.io.IOException;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;
import tree.UnknownValueException;
import utility.Keyboard;


/**
 * Classe di test dell'applicazione.
 */
class MainTest {

	/**
	 * Avvia acquisizione, apprendimento e predizione.
	 *
	 * @param args Argomenti da linea di comando, non usati.
	 */
	public static void main(String[] args) {
		RegressionTree tree = null;
		int decision = 0;
		do {
			System.out.println("Learn Regression Tree from Training Set [1]");
			System.out.println("Load Regression Tree from Archive [2]");
			decision = Keyboard.readInt();
		} while (!(decision == 1) && !(decision == 2));

		if (decision == 1) {
			System.out.println("Training set:");
			String fileName = Keyboard.readString();
			System.out.println("Archive file:");
			String archiveFile = Keyboard.readString();
			System.out.println("Starting data acquisition phase!");

			try {
				Data trainingSet = new Data(fileName);
				System.out.println("Starting learning phase!");

				tree = new RegressionTree(trainingSet);
				tree.salva(archiveFile);
			} catch (TrainingDataException e) {
				System.out.println(e);
				return;
			} catch (IOException e) {
				System.out.println(e);
				return;
			}
		} else {
			System.out.println("Archive file:");
			String archiveFile = Keyboard.readString();
			try {
				tree = RegressionTree.carica(archiveFile);
			} catch (IOException e) {
				System.out.println(e);
				return;
			} catch (ClassNotFoundException e) {
				System.out.println(e);
				return;
			}
		}

		tree.printRules();
		tree.printTree();

		char answer = 'y';
		while (answer == 'y') {
		try {
				System.out.println("Starting prediction phase!");
				Double predictedValue = tree.predictClass();
				System.out.println(predictedValue);
			} catch (UnknownValueException e) {
				System.out.println(e);
		}

			System.out.println("Would you repeat ? (y/n)");
			answer = Keyboard.readChar();
		}
	}

}
