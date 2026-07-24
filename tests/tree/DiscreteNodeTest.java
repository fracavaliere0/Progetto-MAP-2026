package tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import data.Data;
import data.DiscreteAttribute;
import org.junit.Before;
import org.junit.Test;
import support.TestData;

/** Verifica la generazione e l'uso degli split discreti. */
public class DiscreteNodeTest {

    private static final double DELTA = 0.000001;

    private Data data;
    private DiscreteAttribute attribute;

    @Before
    public void setUp() throws Exception {
        data = new Data(TestData.createTreeData());
        attribute = (DiscreteAttribute) data.getExplanatoryAttribute(0);
    }

    @Test
    public void constructorBuildsLowerMiddleAndUpperSplitRanges() {
        DiscreteNode node = new DiscreteNode(data, 0, 5, attribute);

        assertEquals(3, node.getNumberOfChildren());
        assertSplit(node.getSplitInfo(0), "A", 0, 1);
        assertSplit(node.getSplitInfo(1), "B", 2, 3);
        assertSplit(node.getSplitInfo(2), "C", 4, 5);
        assertEquals(6.0, node.getVariance(), DELTA);
        assertSame(attribute, node.getAttribute());
    }

    @Test
    public void constructorSupportsSingleMiddleAndCompleteRanges() throws Exception {
        DiscreteNode lower = new DiscreteNode(data, 0, 0, attribute);
        DiscreteNode middle = new DiscreteNode(data, 1, 4, attribute);
        DiscreteNode upper = new DiscreteNode(data, 0, 5, attribute);

        assertEquals(1, lower.getNumberOfChildren());
        assertEquals(3, middle.getNumberOfChildren());
        assertEquals(3, upper.getNumberOfChildren());
        assertSplit(lower.getSplitInfo(0), "A", 0, 0);
        assertSplit(middle.getSplitInfo(1), "B", 2, 3);
        assertSplit(upper.getSplitInfo(2), "C", 4, 5);
    }

    @Test
    public void testConditionChecksLowerMiddleUpperAndUnknownValues() {
        DiscreteNode node = new DiscreteNode(data, 0, 5, attribute);

        assertEquals(0, node.testCondition("A"));
        assertEquals(1, node.testCondition("B"));
        assertEquals(2, node.testCondition("C"));
        assertEquals(-1, node.testCondition("unknown"));
        assertEquals(-1, node.testCondition(null));
    }

    @Test
    public void formulateQueryAndToStringDescribeEveryBranch() {
        DiscreteNode node = new DiscreteNode(data, 0, 5, attribute);

        assertEquals(
            "0:first=A\n" +
            "1:first=B\n" +
            "2:first=C\n",
            node.formulateQuery()
        );
        assertTrue(node.toString().startsWith("DISCRETE SPLIT : attribute=first"));
        assertTrue(node.toString().contains("child 1 split value=B[Examples:2-3]"));
    }

    @Test
    public void splitInfoSupportsDefaultAndExplicitComparators() {
        DiscreteNode node = new DiscreteNode(data, 0, 5, attribute);
        SplitNode.SplitInfo defaultInfo = node.new SplitInfo("middle", 1, 3, 1);
        SplitNode.SplitInfo explicitInfo = node.new SplitInfo(5.0, 0, 2, 0, "<=");

        assertEquals("=", defaultInfo.getComparator());
        assertEquals("<=", explicitInfo.getComparator());
        assertEquals("middle", defaultInfo.getSplitValue());
        assertEquals(1, defaultInfo.getBeginindex());
        assertEquals(3, defaultInfo.getEndIndex());
        assertEquals(
            "child 0 split value<=5.0[Examples:0-2]",
            explicitInfo.toString()
        );
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getSplitInfoRejectsIndexAboveUpperLimit() {
        DiscreteNode node = new DiscreteNode(data, 0, 5, attribute);

        node.getSplitInfo(3);
    }

    private void assertSplit(
        SplitNode.SplitInfo info,
        Object value,
        int begin,
        int end
    ) {
        assertEquals(value, info.getSplitValue());
        assertEquals(begin, info.getBeginindex());
        assertEquals(end, info.getEndIndex());
    }
}
