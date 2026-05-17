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
		System.out.println("Training set:");
		String fileName = Keyboard.readString();
		System.out.println("Starting data acquisition phase!");

		try {
			Data trainingSet = new Data(fileName);
			System.out.println("Starting learning phase!");

			RegressionTree tree = new RegressionTree(trainingSet);
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
		} catch (TrainingDataException e) {
			System.out.println(e);
		}
	}

}
