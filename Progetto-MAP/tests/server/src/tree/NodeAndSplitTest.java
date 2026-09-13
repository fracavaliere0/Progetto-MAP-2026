package tree;

import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testsupport.TestSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NodeAndSplitTest {
    @Before
    public void configureDatabase() {
        TestSupport.configureFixtures();
    }

    @After
    public void clearDatabase() {
        TestSupport.clearJdbcFailures();
    }

    @Test
    public void leafComputesMinimumMediumAndMaximumStatistics() throws Exception {
        Data data = new Data("mixed");

        LeafNode minimum = new LeafNode(data, 0, 0);
        assertEquals(50.0, minimum.getPredictedClassValue(), 0.0);
        assertEquals(0.0, minimum.getVariance(), 0.0);
        assertEquals(0, minimum.getBeginExampleIndex());
        assertEquals(0, minimum.getEndExampleIndex());
        assertEquals(0, minimum.getNumberOfChildren());

        LeafNode medium = new LeafNode(data, 1, 3);
        assertEquals(300.0, medium.getPredictedClassValue(), 0.0);
        assertEquals(740000.0, medium.getVariance(), 0.0);

        LeafNode maximum = new LeafNode(data, 0, 4);
        assertEquals(210.0, maximum.getPredictedClassValue(), 0.0);
        assertEquals(802000.0, maximum.getVariance(), 0.0);
        assertTrue(maximum.toString().contains("LEAF : class=210.0 Nodo: [Examples:0-4]"));
    }

    @Test
    public void emptyRangeReturnsZerosWithoutReadingData() throws Exception {
        Data data = new Data("minimum");
        LeafNode empty = new LeafNode(data, 1, 0);
        assertEquals(0.0, empty.getPredictedClassValue(), 0.0);
        assertEquals(0.0, empty.getVariance(), 0.0);
    }

    @Test
    public void nodeIdentifiersIncrease() throws Exception {
        Data data = new Data("minimum");
        LeafNode first = new LeafNode(data, 0, 0);
        LeafNode second = new LeafNode(data, 0, 0);
        assertEquals(first.getIdNode() + 1, second.getIdNode());
        assertNotEquals(first.getIdNode(), second.getIdNode());
    }

    @Test
    public void discreteSplitCoversMinimumMediumMaximumAndUnknownValues() throws Exception {
        Data data = new Data("mixed");
        DiscreteNode node = new DiscreteNode(data, 0, 4,
                (DiscreteAttribute) data.getExplanatoryAttribute(0));

        assertEquals(3, node.getNumberOfChildren());
        assertEquals(0, node.testCondition("high"));
        assertEquals(1, node.testCondition("low"));
        assertEquals(2, node.testCondition("medium"));
        assertEquals(-1, node.testCondition("unknown"));
        assertEquals(-1, node.testCondition(null));
        assertEquals("kind", node.getAttribute().getName());
        assertEquals("high", node.getSplitInfo(0).getSplitValue());
        assertEquals(0, node.getSplitInfo(0).getBeginindex());
        assertEquals(0, node.getSplitInfo(0).getEndIndex());
        assertEquals("=", node.getSplitInfo(0).getComparator());
        assertEquals("0:kind=high\n1:kind=low\n2:kind=medium\n", node.formulateQuery());
        assertTrue(node.toString().startsWith("DISCRETE SPLIT : attribute=kind"));
        assertTrue(node.toString().contains("child 2 split value=medium[Examples:3-4]"));
    }

    @Test
    public void discreteOneValueSplitHasOneChild() throws Exception {
        Data data = new Data("constant");
        DiscreteNode node = new DiscreteNode(data, 0, 9,
                (DiscreteAttribute) data.getExplanatoryAttribute(0));
        assertEquals(1, node.getNumberOfChildren());
        assertEquals(0, node.testCondition("same"));
        assertEquals(82.5, node.getVariance(), 0.0);
    }

    @Test
    public void continuousSplitTestsMinimumThresholdAndMaximum() throws Exception {
        Data data = new Data("continuous");
        ContinuousNode node = new ContinuousNode(data, 0, 5,
                (ContinuousAttribute) data.getExplanatoryAttribute(0));
        assertEquals(2, node.getNumberOfChildren());
        double threshold = (Double) node.getSplitInfo(0).getSplitValue();
        assertEquals(0, node.testCondition(-Double.MAX_VALUE));
        assertEquals(0, node.testCondition(threshold));
        assertEquals(1, node.testCondition(Double.MAX_VALUE));
        assertEquals("<=", node.getSplitInfo(0).getComparator());
        assertEquals(">", node.getSplitInfo(1).getComparator());
        assertTrue(node.formulateQuery().contains("0:x<=" + threshold));
        assertTrue(node.toString().startsWith("CONTINUOUS SPLIT : attribute=x"));
        TestSupport.expect(ClassCastException.class, () -> node.testCondition(Integer.valueOf(0)));
        TestSupport.expect(NullPointerException.class, () -> node.testCondition(null));
    }

    @Test
    public void continuousSplitDropsSingletonRightPartition() throws Exception {
        // provaC's best split leaves a non-singleton branch; use a dedicated in-memory
        // table shape from the continuous fixture's first three sorted points instead.
        Data data = new Data("continuous");
        ContinuousNode node = new ContinuousNode(data, 0, 1,
                (ContinuousAttribute) data.getExplanatoryAttribute(0));
        assertEquals(1, node.getNumberOfChildren());
        assertEquals(0, node.testCondition(Double.NEGATIVE_INFINITY));
        assertEquals(-1, node.testCondition(Double.POSITIVE_INFINITY));
    }

    @Test
    public void constantContinuousRangeRetainsLegacyNullPointerFailure() throws Exception {
        final Data data = new Data("continuous");
        // Sorting puts equal x=0 values first.
        data.sort(data.getExplanatoryAttribute(0), 0, 5);
        TestSupport.expect(NullPointerException.class, () -> new ContinuousNode(data, 0, 1,
                (ContinuousAttribute) data.getExplanatoryAttribute(0)));
    }

    @Test
    public void splitComparisonCoversLessEqualAndGreater() throws Exception {
        Data firstData = new Data("mixed");
        DiscreteNode first = new DiscreteNode(firstData, 0, 4,
                (DiscreteAttribute) firstData.getExplanatoryAttribute(0));
        Data secondData = new Data("mixed");
        DiscreteNode second = new DiscreteNode(secondData, 0, 4,
                (DiscreteAttribute) secondData.getExplanatoryAttribute(0));

        assertEquals(0, first.compareTo(second));
        first.splitVariance = -Double.MAX_VALUE;
        second.splitVariance = 0.0;
        assertEquals(-1, first.compareTo(second));
        first.splitVariance = Double.MAX_VALUE;
        assertEquals(1, first.compareTo(second));
    }
}
