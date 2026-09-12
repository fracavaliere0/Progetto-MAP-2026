package estensioni.gui;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

/** Verifica le statistiche dell'albero visuale. */
public final class VisualTreeStatisticsTest {

    /** Impedisce l'istanziazione. */
    private VisualTreeStatisticsTest() {}

    /**
     * Esegue la verifica su {@code prova.dat}.
     *
     * @param args argomenti non usati
     * @throws TrainingDataException se il training set non è valido
     */
    public static void main(String[] args) throws TrainingDataException {
        RegressionTree tree = new RegressionTree(new Data("distribution/standalone/prova.dat"));
        String statistics = VisualTree.from(tree).getStatistics();
        assert statistics.equals("5 nodi  |  3 foglie  |  profondità 2") : statistics;
    }
}
