import java.io.FileNotFoundException;

import data.Data;
import tree.RegressionTree;


/**
 * Classe di test dell'applicazione.
 *
 * Il metodo {@code main} carica il dataset, costruisce l'albero di regressione
 * e ne stampa sia le regole sia la struttura completa.
 */
class MainTest {

	/**
	 * Costruttore vuoto della classe di test.
	 */
	MainTest() {
	}

	/**
	 * Avvia il test completo del progetto.
	 *
	 * @param args Argomenti da linea di comando, non usati.
	 * @throws FileNotFoundException Se il file {@code servo.dat} non e' presente.
	 */
	public static void main(String[] args) throws FileNotFoundException{
		Data trainingSet= new Data("servo.dat");
		
		RegressionTree tree=new RegressionTree(trainingSet);
		
		tree.printRules();
		
		tree.printTree();
		
	}

}
