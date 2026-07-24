package tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import data.Data;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;
import support.KeyboardInput;
import support.TestData;

/** Verifica induzione, stampa e predizione dell'albero di regressione. */
public class RegressionTreeTest {

    private Data data;

    @Before
    public void setUp() throws Exception {
        data = new Data(TestData.createTreeData());
    }

    @Test
    public void emptyTreeHasNoRootOrChildren() {
        RegressionTree tree = new RegressionTree();

        assertNull(tree.getRoot());
        assertEquals(0, tree.getChildren().length);
    }

    @Test
    public void isLeafChecksLowerMiddleAndUpperSizes() {
        RegressionTree tree = new RegressionTree();

        assertTrue(tree.isLeaf(data, 0, 1, 3));
        assertTrue(tree.isLeaf(data, 0, 2, 3));
        assertFalse(tree.isLeaf(data, 0, 3, 3));
    }

    @Test
    public void determineBestSplitChecksLowerMiddleAndUpperRanges() {
        RegressionTree tree = new RegressionTree();

        SplitNode lower = tree.determineBestSplitNode(data, 0, 0);
        SplitNode middle = tree.determineBestSplitNode(data, 1, 4);
        SplitNode upper = tree.determineBestSplitNode(data, 0, 5);

        assertEquals("first", lower.getAttribute().getName());
        assertEquals("first", middle.getAttribute().getName());
        assertEquals("first", upper.getAttribute().getName());
        assertEquals(1, lower.getNumberOfChildren());
        assertEquals(3, middle.getNumberOfChildren());
        assertEquals(3, upper.getNumberOfChildren());
    }

    @Test
    public void determineBestSplitReturnsNullWithoutExplanatoryAttributes()
        throws Exception {
        Data targetOnly = new Data(
            TestData.create(
                "@schema 0\n" +
                "@target target\n" +
                "@data 1\n" +
                "5\n"
            )
        );

        assertNull(new RegressionTree().determineBestSplitNode(targetOnly, 0, 0));
    }

    @Test
    public void learnTreeBuildsLeafAtTheSizeLimit() {
        RegressionTree tree = new RegressionTree();

        tree.learnTree(data, 0, 5, 6);

        assertTrue(tree.getRoot() instanceof LeafNode);
        assertEquals(0, tree.getChildren().length);
        assertEquals(68.0 / 6.0, ((LeafNode) tree.getRoot()).getPredictedClassValue(), 0.000001);
    }

    @Test
    public void learnTreeBuildsSplitAndLeafChildren() {
        RegressionTree tree = createShallowTree(data);
        RegressionTree[] children = tree.getChildren();

        assertTrue(tree.getRoot() instanceof DiscreteNode);
        assertEquals(3, children.length);
        assertTrue(children[0].getRoot() instanceof LeafNode);
        assertTrue(children[1].getRoot() instanceof LeafNode);
        assertTrue(children[2].getRoot() instanceof LeafNode);
        assertEquals(2.0, ((LeafNode) children[0].getRoot()).getPredictedClassValue(), 0.000001);
        assertEquals(11.0, ((LeafNode) children[1].getRoot()).getPredictedClassValue(), 0.000001);
        assertEquals(21.0, ((LeafNode) children[2].getRoot()).getPredictedClassValue(), 0.000001);
    }

    @Test
    public void learnTreeReplacesAOneBranchSplitWithALeaf() throws Exception {
        Data singleValue = new Data(TestData.createSingleValueData());
        RegressionTree tree = new RegressionTree();

        tree.learnTree(singleValue, 0, 2, 0);

        assertTrue(tree.getRoot() instanceof LeafNode);
        assertEquals(2.0, ((LeafNode) tree.getRoot()).getPredictedClassValue(), 0.000001);
    }

    @Test
    public void dataConstructorBuildsACompleteTree() {
        RegressionTree tree = new RegressionTree(data);

        assertNotNull(tree.getRoot());
        assertTrue(tree.getRoot() instanceof SplitNode);
        assertEquals(3, tree.getChildren().length);
        assertTrue(tree.toString().contains("DISCRETE SPLIT"));
        assertTrue(tree.toString().contains("LEAF : class="));
    }

    @Test
    public void getChildrenReturnsADefensiveCopy() {
        RegressionTree tree = createShallowTree(data);
        RegressionTree[] children = tree.getChildren();

        children[0] = null;

        assertNotNull(tree.getChildren()[0]);
    }

    @Test
    public void printMethodsDescribeLeafAndNestedRules() {
        RegressionTree leaf = new RegressionTree();
        leaf.learnTree(data, 0, 5, 6);
        String leafOutput = captureOutput(new Runnable() {
            @Override
            public void run() {
                leaf.printRules();
                leaf.printTree();
            }
        });

        RegressionTree nested = new RegressionTree(data);
        String nestedOutput = captureOutput(new Runnable() {
            @Override
            public void run() {
                nested.printRules();
            }
        });

        assertTrue(leafOutput.contains("Class=11.333333333333334"));
        assertTrue(leafOutput.contains("********* TREE **********"));
        assertTrue(leafOutput.contains("LEAF : class="));
        assertTrue(nestedOutput.contains("first=A AND second=X ==> Class=1.0"));
        assertTrue(nestedOutput.contains("first=C AND second=Y ==> Class=22.0"));
    }

    @Test
    public void printRulesSupportsAnExistingConditionAndAnEmptyTree() {
        RegressionTree leaf = new RegressionTree();
        leaf.learnTree(data, 0, 0, 1);
        RegressionTree empty = new RegressionTree();

        String output = captureOutput(new Runnable() {
            @Override
            public void run() {
                leaf.printRules("prefix");
                empty.printRules("ignored");
            }
        });

        assertEquals("prefix ==> Class=1.0\n", normalizeNewLines(output));
    }

    @Test
    public void predictClassChecksLowerMiddleAndUpperBranches() throws Exception {
        RegressionTree tree = createShallowTree(data);

        KeyboardInput.set("0\n");
        assertEquals(2.0, predictSilently(tree), 0.000001);

        KeyboardInput.set("1\n");
        assertEquals(11.0, predictSilently(tree), 0.000001);

        KeyboardInput.set("2\n");
        assertEquals(21.0, predictSilently(tree), 0.000001);
    }

    @Test
    public void predictClassReturnsLeafValueWithoutReadingInput() throws Exception {
        RegressionTree tree = new RegressionTree();
        tree.learnTree(data, 0, 5, 6);

        assertEquals(68.0 / 6.0, predictSilently(tree), 0.000001);
    }

    @Test
    public void predictClassRejectsAnswerBelowLowerLimit() throws Exception {
        assertInvalidAnswer("-1\n", "between 0 and 2");
    }

    @Test
    public void predictClassRejectsAnswerAboveUpperLimit() throws Exception {
        assertInvalidAnswer("3\n", "between 0 and 2");
    }

    private RegressionTree createShallowTree(Data trainingSet) {
        RegressionTree tree = new RegressionTree();
        tree.learnTree(trainingSet, 0, 5, 2);
        return tree;
    }

    private void assertInvalidAnswer(String answer, String expectedMessage)
        throws Exception {
        RegressionTree tree = createShallowTree(data);
        KeyboardInput.set(answer);

        try {
            predictSilently(tree);
            fail("Era attesa UnknownValueException");
        } catch (UnknownValueException exception) {
            assertTrue(exception.getMessage().contains(expectedMessage));
        }
    }

    private Double predictSilently(RegressionTree tree)
        throws UnknownValueException {
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            return tree.predictClass();
        } finally {
            System.setOut(original);
        }
    }

    private String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output));
            action.run();
            return normalizeNewLines(output.toString());
        } finally {
            System.setOut(original);
        }
    }

    private String normalizeNewLines(String value) {
        return value.replace("\r\n", "\n");
    }
}
