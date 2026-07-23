package estensioni;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

/** Verifica il calcolo delle statistiche dell'albero visuale. */
public final class VisualTreeStatisticsTest {

    /** Impedisce l'istanziazione della classe di test. */
    private VisualTreeStatisticsTest() {}

    /**
     * Esegue la verifica sul training set di esempio.
     *
     * @param args Argomenti da linea di comando, non usati.
     * @throws TrainingDataException se il training set non è valido.
     */
    public static void main(String[] args) throws TrainingDataException {
        Data trainingSet = new Data("prova.dat");
        RegressionTree tree = new RegressionTree(trainingSet);
        String statistics = VisualTree.from(tree).getStatistics();

        assert statistics.equals("5 nodi  |  3 foglie  |  profondità 2")
            : statistics;
    }
}
