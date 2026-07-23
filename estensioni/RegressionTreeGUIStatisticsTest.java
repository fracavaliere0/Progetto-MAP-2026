package estensioni;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

/** Verifica il calcolo delle statistiche dell'albero grafico. */
public final class RegressionTreeGUIStatisticsTest {

    /**
     * Esegue la verifica sul training set di esempio.
     *
     * @param args Argomenti da linea di comando, non usati.
     * @throws TrainingDataException se il training set non è valido.
     */
    public static void main(String[] args) throws TrainingDataException {
        Data trainingSet = new Data("prova.dat");
        RegressionTree tree = new RegressionTree(trainingSet);
        String statistics = RegressionTreeGUI.treeStatistics(tree);

        assert statistics.equals("5 nodi  |  3 foglie  |  profondità 2")
            : statistics;
    }
}
