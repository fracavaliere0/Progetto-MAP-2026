package estensioni.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import support.TestData;
import tree.ContinuousNode;
import tree.DiscreteNode;
import tree.LeafNode;
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

    /**
     * Verifica le etichette continue senza dipendere dall'induzione ricorsiva.
     *
     * @throws Exception se la preparazione dell'albero fallisce
     */
    @Test
    public void fromPreservesContinuousComparators() throws Exception {
        Data data = new Data(TestData.create(
            "@schema 1\n@desc amount\n@target result\n@data 4\n" +
            "0,0\n1,0\n2,10\n3,10\n"
        ));
        RegressionTree tree = new RegressionTree();
        ContinuousNode root = new ContinuousNode(
            data, 0, 3, (ContinuousAttribute) data.getExplanatoryAttribute(0)
        );
        RegressionTree lower = new RegressionTree();
        RegressionTree upper = new RegressionTree();
        Field rootField = RegressionTree.class.getDeclaredField("root");
        rootField.setAccessible(true);
        rootField.set(tree, root);
        rootField.set(lower, new LeafNode(data, 0, 1));
        rootField.set(upper, new LeafNode(data, 2, 3));
        Field childrenField = RegressionTree.class.getDeclaredField("childTree");
        childrenField.setAccessible(true);
        childrenField.set(tree, new RegressionTree[] {lower, upper});

        VisualTree visual = VisualTree.from(tree);

        assertEquals("amount", visual.getRoot().title);
        assertEquals("<=1.0", visual.getRoot().children.get(0).branch);
        assertEquals(">1.0", visual.getRoot().children.get(1).branch);
        assertEquals("0", visual.getRoot().children.get(0).title);
        assertEquals("10", visual.getRoot().children.get(1).title);
        assertEquals("3 nodi  |  2 foglie  |  profondità 1", visual.getStatistics());
    }

    /**
     * Verifica che l'adattatore non modifichi la base e legga anche gli archivi.
     *
     * @throws Exception se la serializzazione fallisce
     */
    @Test
    public void accessIsReadOnlyAndSupportsSerializedBaseTrees() throws Exception {
        RegressionTree tree = new RegressionTree(new Data(TestData.createTreeData()));
        String original = tree.toString();
        RegressionTreeAccess access = new RegressionTreeAccess();
        RegressionTree[] children = access.childrenOf(tree);
        children[0] = null;
        assertTrue(access.childrenOf(tree)[0] != null);
        assertEquals(0, access.childrenOf(new RegressionTree()).length);

        Path archive = Files.createTempFile("map-visual-", ".dmp");
        try {
            tree.salva(archive.toString());
            VisualTree visual = VisualTree.from(RegressionTree.carica(archive.toString()));
            assertEquals(VisualTree.from(tree).getStatistics(), visual.getStatistics());
            assertEquals("first", visual.getRoot().title);
            assertEquals(original, tree.toString());
        } finally {
            Files.deleteIfExists(archive);
        }
    }

    /**
     * Verifica che un figlio mancante sia segnalato come albero incompleto.
     *
     * @throws Exception se la preparazione fallisce
     */
    @Test(expected = IllegalStateException.class)
    public void fromRejectsANullChild() throws Exception {
        RegressionTree tree = new RegressionTree(new Data(TestData.createTreeData()));
        Field childrenField = RegressionTree.class.getDeclaredField("childTree");
        childrenField.setAccessible(true);
        RegressionTree[] children = (RegressionTree[]) childrenField.get(tree);
        children[0] = null;
        VisualTree.from(tree);
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
