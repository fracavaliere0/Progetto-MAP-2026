package tree;

import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;
import org.junit.Test;
import testsupport.TestSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NodeAndSplitTest {
    private Data load(String text) throws Exception {
        return new Data(TestSupport.dataFile(text).toString());
    }

    @Test
    public void leafComputesMinimumMediumAndMaximumRangeStatistics() throws Exception {
        Data data = load("@schema 1\n@desc x A,B,C\n@target y\n@data 5\n"
                + "A,-10\nB,0\nC,10\nA,20\nB,30\n");

        LeafNode minimum = new LeafNode(data, 0, 0);
        assertEquals(-10.0, minimum.getPredictedClassValue(), 0.0);
        assertEquals(0.0, minimum.getVariance(), 0.0);
        assertEquals(0, minimum.getBeginExampleIndex());
        assertEquals(0, minimum.getEndExampleIndex());
        assertEquals(0, minimum.getNumberOfChildren());

        LeafNode medium = new LeafNode(data, 1, 3);
        assertEquals(10.0, medium.getPredictedClassValue(), 0.0);
        assertEquals(200.0, medium.getVariance(), 0.0);

        LeafNode maximum = new LeafNode(data, 0, 4);
        assertEquals(10.0, maximum.getPredictedClassValue(), 0.0);
        assertEquals(1000.0, maximum.getVariance(), 0.0);
        assertTrue(maximum.toString().startsWith("LEAF : class=10.0 Nodo: [Examples:0-4] variance:1000.0"));
    }

    @Test
    public void emptyRangeHasZeroPredictionAndVariance() throws Exception {
        Data data = load("@schema 1\n@desc x\n@target y\n@data 1\n1,2\n");
        LeafNode empty = new LeafNode(data, 1, 0);
        assertEquals(0.0, empty.getPredictedClassValue(), 0.0);
        assertEquals(0.0, empty.getVariance(), 0.0);
        assertEquals(1, empty.getBeginExampleIndex());
        assertEquals(0, empty.getEndExampleIndex());
    }

    @Test
    public void nodeIdsIncreaseForEveryCreatedNode() throws Exception {
        Data data = load("@schema 1\n@desc x A,B\n@target y\n@data 2\nA,1\nB,2\n");
        LeafNode first = new LeafNode(data, 0, 0);
        LeafNode second = new LeafNode(data, 1, 1);
        assertEquals(first.getIdNode() + 1, second.getIdNode());
        assertNotEquals(first.getIdNode(), second.getIdNode());
    }

    @Test
    public void discreteSplitCoversMinimumMediumAndMaximumValues() throws Exception {
        Data data = load("@schema 1\n@desc level low,medium,high\n@target y\n@data 6\n"
                + "medium,5\nhigh,10\nlow,0\nhigh,12\nmedium,6\nlow,1\n");
        DiscreteNode node = new DiscreteNode(data, 0, 5,
                (DiscreteAttribute) data.getExplanatoryAttribute(0));

        assertEquals(3, node.getNumberOfChildren());
        assertEquals(0, node.testCondition("high"));
        assertEquals(1, node.testCondition("low"));
        assertEquals(2, node.testCondition("medium"));
        assertEquals(-1, node.testCondition("missing"));
        assertEquals(-1, node.testCondition(null));
        assertEquals("level", node.getAttribute().getName());
        assertEquals("high", node.getSplitInfo(0).getSplitValue());
        assertEquals(0, node.getSplitInfo(0).getBeginindex());
        assertEquals(1, node.getSplitInfo(0).getEndIndex());
        assertEquals("=", node.getSplitInfo(0).getComparator());
        assertEquals("0:level=high\n1:level=low\n2:level=medium\n", node.formulateQuery());
        assertTrue(node.toString().contains("DISCRETE SPLIT : attribute=level"));
        assertTrue(node.toString().contains("child 2 split value=medium[Examples:4-5]"));
    }

    @Test
    public void discreteSplitWithOneDistinctValueProducesOneChild() throws Exception {
        Data data = load("@schema 1\n@desc x only\n@target y\n@data 3\n"
                + "only,1\nonly,2\nonly,3\n");
        DiscreteNode node = new DiscreteNode(data, 0, 2,
                (DiscreteAttribute) data.getExplanatoryAttribute(0));
        assertEquals(1, node.getNumberOfChildren());
        assertEquals(0, node.testCondition("only"));
        assertEquals(2.0, node.getVariance(), 0.0);
    }

    @Test
    public void continuousSplitTestsValuesBelowAtAndAboveThreshold() throws Exception {
        Data data = load(TestSupport.continuousData());
        ContinuousNode node = new ContinuousNode(data, 0, 5,
                (ContinuousAttribute) data.getExplanatoryAttribute(0));
        assertEquals(2, node.getNumberOfChildren());

        double threshold = (Double) node.getSplitInfo(0).getSplitValue();
        assertEquals(0, node.testCondition(-Double.MAX_VALUE));
        assertEquals(0, node.testCondition(threshold));
        assertEquals(1, node.testCondition(Double.MAX_VALUE));
        assertEquals(0, node.testCondition(Integer.valueOf((int) threshold)));
        assertEquals(-1, node.testCondition("not a number"));
        assertEquals(-1, node.testCondition(null));
        assertEquals("<=", node.getSplitInfo(0).getComparator());
        assertEquals(">", node.getSplitInfo(1).getComparator());
        assertTrue(node.formulateQuery().contains("0:x<=" + threshold));
        assertTrue(node.toString().startsWith("CONTINUOUS SPLIT : attribute=x"));
    }

    @Test
    public void continuousSplitDropsSingletonRightPartitionAsImplemented() throws Exception {
        Data data = load("@schema 1\n@desc x\n@target y\n@data 3\n0,0\n0,0\n10,100\n");
        ContinuousNode node = new ContinuousNode(data, 0, 2,
                (ContinuousAttribute) data.getExplanatoryAttribute(0));
        assertEquals(1, node.getNumberOfChildren());
        assertEquals(0, node.testCondition(Double.NEGATIVE_INFINITY));
        assertEquals(0, node.testCondition(0.0));
        assertEquals(-1, node.testCondition(10.0));
    }

    @Test
    public void continuousSplitRejectsAConstantRangeWithTheLegacyFailure() throws Exception {
        final Data data = load("@schema 1\n@desc x\n@target y\n@data 3\n1,1\n1,2\n1,3\n");
        TestSupport.expect(NullPointerException.class, () -> new ContinuousNode(
                data, 0, 2, (ContinuousAttribute) data.getExplanatoryAttribute(0)));
    }

    @Test
    public void splitNodesCompareLessEqualAndGreaterVariance() throws Exception {
        Data firstData = load("@schema 1\n@desc x A,B\n@target y\n@data 4\nA,0\nA,1\nB,10\nB,11\n");
        Data secondData = load("@schema 1\n@desc x A,B\n@target y\n@data 4\nA,0\nA,1\nB,10\nB,11\n");
        DiscreteNode first = new DiscreteNode(firstData, 0, 3,
                (DiscreteAttribute) firstData.getExplanatoryAttribute(0));
        DiscreteNode second = new DiscreteNode(secondData, 0, 3,
                (DiscreteAttribute) secondData.getExplanatoryAttribute(0));

        assertEquals(0, first.compareTo(second));
        first.splitVariance = -Double.MAX_VALUE;
        second.splitVariance = 0.0;
        assertEquals(-1, first.compareTo(second));
        first.splitVariance = Double.MAX_VALUE;
        assertEquals(1, first.compareTo(second));
    }
}
