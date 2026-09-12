package tree;

import data.Data;
import org.junit.After;
import org.junit.Test;
import testsupport.TestSupport;

import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegressionTreeTest {
    @After
    public void restoreKeyboardDiagnostics() {
        utility.Keyboard.setPrintErrors(true);
        utility.Keyboard.resetErrorCount(0);
    }

    private Data load(String text) throws Exception {
        return new Data(TestSupport.dataFile(text).toString());
    }

    @Test
    public void isLeafCoversMinimumMediumAndMaximumBoundaryValues() throws Exception {
        Data data = load(TestSupport.mixedData());
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
    public void forcedLeafPredictsMeanForMinimumMediumAndMaximumRanges() throws Exception {
        Data data = load(TestSupport.mixedData());
        int[][] ranges = {{0, 0}, {1, 3}, {0, 4}};
        double[] predictions = {50.0, 300.0, 210.0};

        for (int i = 0; i < ranges.length; i++) {
            RegressionTree tree = new RegressionTree();
            tree.learnTree(data, ranges[i][0], ranges[i][1], ranges[i][1] - ranges[i][0] + 1);
            assertEquals(predictions[i], tree.predictClass(), 0.0);
            assertTrue(tree.toString().startsWith("LEAF : class=" + predictions[i]));
        }
    }

    @Test
    public void oneWaySplitFallsBackToLeaf() throws Exception {
        Data data = load("@schema 1\n@desc constant same\n@target y\n@data 10\n"
                + "same,1\nsame,2\nsame,3\nsame,4\nsame,5\n"
                + "same,6\nsame,7\nsame,8\nsame,9\nsame,10\n");
        RegressionTree tree = new RegressionTree(data);
        assertTrue(tree.toString().startsWith("LEAF : class=5.5"));
        assertEquals(5.5, tree.predictClass(), 0.0);
    }

    @Test
    public void learnsRecursiveTreeAndPrintsNestedRules() throws Exception {
        Data data = load(TestSupport.nestedDiscreteData());
        RegressionTree tree = new RegressionTree(data);

        String representation = tree.toString();
        assertTrue(representation.startsWith("DISCRETE SPLIT"));
        assertTrue(representation.contains("LEAF : class=0.0"));
        assertTrue(representation.contains("LEAF : class=30.0"));

        String rules = TestSupport.captureOutput(tree::printRules);
        assertTrue(rules.contains("********* RULES **********"));
        assertTrue(rules.contains("first=A AND second=u ==> Class=0.0"));
        assertTrue(rules.contains("first=B AND second=v ==> Class=30.0"));
        assertTrue(rules.contains("*************************"));

        String printedTree = TestSupport.captureOutput(tree::printTree);
        assertTrue(printedTree.contains("********* TREE **********"));
        assertTrue(printedTree.contains("DISCRETE SPLIT"));
    }

    @Test
    public void printRulesHandlesEmptyAndLeafTrees() throws Exception {
        RegressionTree empty = new RegressionTree();
        String emptyOutput = TestSupport.captureOutput(empty::printRules);
        assertFalse(emptyOutput.contains("Class="));
        TestSupport.expect(NullPointerException.class, empty::toString);

        Data data = load("@schema 1\n@desc x A\n@target y\n@data 1\nA,7\n");
        RegressionTree leaf = new RegressionTree();
        leaf.learnTree(data, 0, 0, 1);
        String leafOutput = TestSupport.captureOutput(leaf::printRules);
        assertTrue(leafOutput.contains("Class=7.0"));
        assertFalse(leafOutput.contains("==>"));
    }

    @Test
    public void interactivePredictionCoversMinimumMediumAndMaximumBranches() throws Exception {
        RegressionTree tree = new RegressionTree(load(TestSupport.nestedDiscreteData()));

        TestSupport.keyboardInput("0\n0\n");
        assertEquals(0.0, tree.predictClass(), 0.0);

        TestSupport.keyboardInput("0\n1\n");
        assertEquals(10.0, tree.predictClass(), 0.0);

        TestSupport.keyboardInput("1\n1\n");
        assertEquals(30.0, tree.predictClass(), 0.0);
    }

    @Test
    public void interactivePredictionRejectsBothInvalidBoundaryAnswers() throws Exception {
        RegressionTree tree = new RegressionTree(load(TestSupport.nestedDiscreteData()));

        TestSupport.keyboardInput("-1\n");
        UnknownValueException low = TestSupport.expect(UnknownValueException.class, tree::predictClass);
        assertEquals("The answer should be an integer between 0 and 1!", low.getMessage());

        TestSupport.keyboardInput("2\n");
        UnknownValueException high = TestSupport.expect(UnknownValueException.class, tree::predictClass);
        assertEquals("The answer should be an integer between 0 and 1!", high.getMessage());

        TestSupport.keyboardInput("-2\n");
        TestSupport.expect(ArrayIndexOutOfBoundsException.class, tree::predictClass);
    }

    @Test
    public void serializationRoundTripPreservesTreeAndPredictions() throws Exception {
        RegressionTree original = new RegressionTree(load(TestSupport.nestedDiscreteData()));
        Path archive = Files.createTempFile("regression-tree-", ".dmp");
        original.salva(archive.toString());

        RegressionTree restored = RegressionTree.carica(archive.toString());
        assertEquals(original.toString(), restored.toString());
        TestSupport.keyboardInput("1\n0\n");
        assertEquals(20.0, restored.predictClass(), 0.0);
    }

    @Test
    public void serializationReportsMissingDirectoryMissingFileCorruptionAndWrongType() throws Exception {
        Path directory = Files.createTempDirectory("tree-output-directory-");
        TestSupport.expect(FileNotFoundException.class, () -> new RegressionTree().salva(directory.toString()));
        TestSupport.expect(FileNotFoundException.class,
                () -> RegressionTree.carica(directory.resolve("missing.dmp").toString()));

        Path corrupt = Files.createTempFile("corrupt-tree-", ".dmp");
        Files.write(corrupt, new byte[] {1, 2, 3, 4});
        TestSupport.expect(java.io.StreamCorruptedException.class,
                () -> RegressionTree.carica(corrupt.toString()));

        Path wrongType = Files.createTempFile("wrong-tree-type-", ".dmp");
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(wrongType))) {
            output.writeObject("not a tree");
        }
        TestSupport.expect(ClassCastException.class, () -> RegressionTree.carica(wrongType.toString()));
    }

    @Test
    public void customExceptionsRetainMessagesAndCauses() {
        UnknownValueException unknown = new UnknownValueException("bad branch");
        assertEquals("bad branch", unknown.getMessage());

        data.TrainingDataException message = new data.TrainingDataException("bad data");
        assertEquals("bad data", message.getMessage());
        Exception cause = new Exception("root cause");
        data.TrainingDataException wrapped = new data.TrainingDataException(cause);
        assertEquals(cause, wrapped.getCause());
    }
}
