package estensioni.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import data.Data;
import data.DiscreteAttribute;
import java.lang.reflect.Field;
import org.junit.Test;
import support.TestData;
import tree.DiscreteNode;
import tree.RegressionTree;

/** Verifica la conversione dell'albero nella rappresentazione della GUI. */
public class VisualTreeTest {

    /** Crea il test dell'albero visuale. */
    public VisualTreeTest() {}

    /**
     * Verifica la conversione di una foglia.
     *
     * @throws Exception se i dati di test non sono validi
     */
    @Test
    public void fromConvertsALeafTree() throws Exception {
        RegressionTree tree = new RegressionTree(
            new Data(TestData.createSingleValueData())
        );

        VisualTree visual = VisualTree.from(tree);

        assertEquals("2", visual.getRoot().title);
        assertEquals("esempi 0-2", visual.getRoot().meta);
        assertEquals("", visual.getRoot().branch);
        assertTrue(visual.getRoot().leaf);
        assertEquals(1, visual.getLevelCount());
        assertEquals("1 nodi  |  1 foglie  |  profondità 0", visual.getStatistics());
    }

    /**
     * Verifica la conversione dei rami.
     *
     * @throws Exception se i dati di test non sono validi
     */
    @Test
    public void fromConvertsLowerMiddleAndUpperBranches() throws Exception {
        RegressionTree tree = new RegressionTree(new Data(TestData.createTreeData()));

        VisualTree visual = VisualTree.from(tree);
        VisualNode root = visual.getRoot();

        assertEquals("first", root.title);
        assertFalse(root.leaf);
        assertEquals(3, root.children.size());
        assertEquals("= A", root.children.get(0).branch);
        assertEquals("= B", root.children.get(1).branch);
        assertEquals("= C", root.children.get(2).branch);
        assertEquals(3, visual.getLevelCount());
        assertEquals("10 nodi  |  6 foglie  |  profondità 2", visual.getStatistics());
    }

    /** Verifica il rifiuto di un albero vuoto. */
    @Test(expected = IllegalStateException.class)
    public void fromRejectsATreeWithoutRoot() {
        VisualTree.from(new RegressionTree());
    }

    /**
     * Verifica il rifiuto di uno split senza figli.
     *
     * @throws Exception se la preparazione fallisce
     */
    @Test(expected = IllegalStateException.class)
    public void fromRejectsASplitWithoutChildren() throws Exception {
        Data data = new Data(TestData.createTreeData());
        DiscreteNode root = new DiscreteNode(
            data,
            0,
            data.getNumberOfExamples() - 1,
            (DiscreteAttribute) data.getExplanatoryAttribute(0)
        );
        RegressionTree incomplete = new RegressionTree();
        Field rootField = RegressionTree.class.getDeclaredField("root");
        rootField.setAccessible(true);
        rootField.set(incomplete, root);

        VisualTree.from(incomplete);
    }

    /**
     * Esegue la verifica autonoma delle statistiche.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void existingStatisticsCheckRunsOnTheSampleData() throws Exception {
        VisualTreeStatisticsTest.main(new String[] {"lower", "middle", "upper"});
    }

    /** Verifica i valori di un nodo visuale. */
    @Test
    public void visualNodeStoresBoundaryValues() {
        VisualNode lower = new VisualNode("", "", "", true);
        VisualNode middle = new VisualNode(
            "middle",
            "metadata",
            "= M",
            false
        );
        VisualNode upper = new VisualNode(
            "a very long title",
            "a very long metadata value",
            "= Z",
            false
        );
        middle.children.add(lower);
        middle.children.add(upper);

        assertEquals("", lower.title);
        assertEquals("middle", middle.title);
        assertEquals("a very long title", upper.title);
        assertEquals(2, middle.children.size());
    }
}
