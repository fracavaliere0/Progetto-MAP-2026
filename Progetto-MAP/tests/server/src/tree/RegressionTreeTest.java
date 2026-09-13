package tree;

import data.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testsupport.TestSupport;

import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegressionTreeTest {
    @Before
    public void configureDatabase() {
        TestSupport.configureFixtures();
    }

    @After
    public void clearDatabase() {
        TestSupport.clearJdbcFailures();
    }

    @Test
    public void isLeafCoversMinimumMediumAndMaximumBoundaries() throws Exception {
        Data data = new Data("mixed");
        RegressionTree tree = new RegressionTree();
        assertTrue(tree.isLeaf(data, 1, 0, 0));
        assertFalse(tree.isLeaf(data, 0, 0, 0));
        assertTrue(tree.isLeaf(data, 0, 0, 1));
        assertFalse(tree.isLeaf(data, 0, 2, 2));
        assertTrue(tree.isLeaf(data, 0, 2, 3));
        assertFalse(tree.isLeaf(data, 0, 4, 4));
        assertTrue(tree.isLeaf(data, 0, 4, Integer.MAX_VALUE));
    }

    @Test
    public void forcedLeafCoversMinimumMediumAndMaximumPredictions() throws Exception {
        int[][] ranges = {{0, 0}, {1, 3}, {0, 4}};
        double[] expected = {50.0, 300.0, 210.0};
        for (int i = 0; i < ranges.length; i++) {
            Data data = new Data("mixed");
            RegressionTree tree = new RegressionTree();
            tree.learnTree(data, ranges[i][0], ranges[i][1], ranges[i][1] - ranges[i][0] + 1);
            assertTrue(tree.isLeaf());
            assertEquals(expected[i], tree.getPredictedClassValue(), 0.0);
            assertEquals(0, tree.getNumberOfChildren());
            assertTrue(tree.toString().startsWith("LEAF : class=" + expected[i]));
            TestSupport.expect(ClassCastException.class, tree::formulateQuery);
        }
    }

    @Test
    public void oneWaySplitFallsBackToLeaf() throws Exception {
        RegressionTree tree = new RegressionTree(new Data("constant"));
        assertTrue(tree.isLeaf());
        assertEquals(5.5, tree.getPredictedClassValue(), 0.0);
        assertEquals(0, tree.getNumberOfChildren());
    }

    @Test
    public void learnsRecursiveTreeAndExposesChildrenAndQueries() throws Exception {
        RegressionTree tree = new RegressionTree(new Data("nested"));
        assertFalse(tree.isLeaf());
        assertEquals(2, tree.getNumberOfChildren());
        assertEquals("0:first=A\n1:first=B\n", tree.formulateQuery());
        assertFalse(tree.getChild(0).isLeaf());
        assertFalse(tree.getChild(1).isLeaf());
        assertTrue(tree.getChild(0).getChild(0).isLeaf());
        assertEquals(0.0, tree.getChild(0).getChild(0).getPredictedClassValue(), 0.0);
        assertEquals(30.0, tree.getChild(1).getChild(1).getPredictedClassValue(), 0.0);
        TestSupport.expect(ArrayIndexOutOfBoundsException.class, () -> tree.getChild(-1));
        TestSupport.expect(ArrayIndexOutOfBoundsException.class, () -> tree.getChild(2));
        TestSupport.expect(ClassCastException.class, tree::getPredictedClassValue);

        String text = tree.toString();
        assertTrue(text.startsWith("DISCRETE SPLIT"));
        assertTrue(text.contains("LEAF : class=0.0"));
        assertTrue(text.contains("LEAF : class=30.0"));
    }

    @Test
    public void bestSplitEvaluationCoversBothDiscreteAndContinuousAttributes() throws Exception {
        Data data = new Data("mixed");
        RegressionTree tree = new RegressionTree();
        Method method = RegressionTree.class.getDeclaredMethod(
                "determineBestSplitNode", Data.class, int.class, int.class);
        method.setAccessible(true);
        SplitNode split = (SplitNode) method.invoke(tree, data, 0, 4);
        assertTrue(split instanceof DiscreteNode || split instanceof ContinuousNode);
        assertTrue(split.getNumberOfChildren() >= 1);
    }

    @Test
    public void printMethodsCoverEmptyLeafAndNestedRulePaths() throws Exception {
        RegressionTree empty = new RegressionTree();
        assertFalse(empty.isLeaf());
        String emptyRules = TestSupport.captureOutput(empty::printRules);
        assertFalse(emptyRules.contains("Class="));
        TestSupport.expect(NullPointerException.class, empty::toString);
        TestSupport.expect(NullPointerException.class, empty::getNumberOfChildren);

        RegressionTree leaf = new RegressionTree();
        Data minimum = new Data("minimum");
        leaf.learnTree(minimum, 0, 0, 1);
        String leafRules = TestSupport.captureOutput(leaf::printRules);
        assertTrue(leafRules.contains("Class=42.0"));
        assertFalse(leafRules.contains("==>"));

        RegressionTree nested = new RegressionTree(new Data("nested"));
        String rules = TestSupport.captureOutput(nested::printRules);
        assertTrue(rules.contains("first=A AND second=u ==> Class=0.0"));
        assertTrue(rules.contains("first=B AND second=v ==> Class=30.0"));
        String printed = TestSupport.captureOutput(nested::printTree);
        assertTrue(printed.contains("********* TREE **********"));
        assertTrue(printed.contains("DISCRETE SPLIT"));
    }

    @Test
    public void serializationRoundTripPreservesMinimumAndMaximumPaths() throws Exception {
        RegressionTree original = new RegressionTree(new Data("nested"));
        Path archive = Files.createTempFile("map-server-tree-", ".dmp");
        original.salva(archive.toString());
        RegressionTree loaded = RegressionTree.carica(archive.toString());

        assertEquals(original.toString(), loaded.toString());
        assertEquals(0.0, loaded.getChild(0).getChild(0).getPredictedClassValue(), 0.0);
        assertEquals(30.0, loaded.getChild(1).getChild(1).getPredictedClassValue(), 0.0);
    }

    @Test
    public void serializationReportsMissingDirectoryMissingFileCorruptAndWrongType() throws Exception {
        Path directory = Files.createTempDirectory("map-server-save-directory-");
        TestSupport.expect(FileNotFoundException.class,
                () -> new RegressionTree(new Data("constant")).salva(directory.toString()));

        Path missing = directory.resolve("missing.dmp");
        TestSupport.expect(FileNotFoundException.class, () -> RegressionTree.carica(missing.toString()));

        Path corrupt = Files.createTempFile("map-server-corrupt-", ".dmp");
        Files.write(corrupt, new byte[] {1, 2, 3, 4});
        TestSupport.expect(java.io.StreamCorruptedException.class, () -> RegressionTree.carica(corrupt.toString()));

        Path wrong = Files.createTempFile("map-server-wrong-", ".dmp");
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(wrong))) {
            output.writeObject("not a tree");
        }
        TestSupport.expect(ClassCastException.class, () -> RegressionTree.carica(wrong.toString()));
    }
}
