package tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import data.Data;
import org.junit.Before;
import org.junit.Test;
import support.TestData;

/** Verifica i nodi foglia sui limiti del sottoinsieme coperto. */
public class LeafNodeTest {

    private static final double DELTA = 0.000001;

    private Data data;

    @Before
    public void setUp() throws Exception {
        data = new Data(TestData.createTreeData());
    }

    @Test
    public void constructorCalculatesLowerMiddleAndUpperPredictions() {
        LeafNode lower = new LeafNode(data, 0, 0);
        LeafNode middle = new LeafNode(data, 1, 4);
        LeafNode upper = new LeafNode(data, 5, 5);

        assertEquals(1.0, lower.getPredictedClassValue(), DELTA);
        assertEquals(11.25, middle.getPredictedClassValue(), DELTA);
        assertEquals(22.0, upper.getPredictedClassValue(), DELTA);

        assertEquals(0.0, lower.getVariance(), DELTA);
        assertEquals(146.75, middle.getVariance(), DELTA);
        assertEquals(0.0, upper.getVariance(), DELTA);
    }

    @Test
    public void nodeAccessorsReturnRangeAndSequentialIdentifiers() {
        LeafNode first = new LeafNode(data, 0, 1);
        LeafNode second = new LeafNode(data, 4, 5);

        assertEquals(0, first.getBeginExampleIndex());
        assertEquals(1, first.getEndExampleIndex());
        assertEquals(first.getIdNode() + 1, second.getIdNode());
        assertEquals(0, first.getNumberOfChildren());
    }

    @Test
    public void emptyRangeUsesNeutralValues() {
        LeafNode node = new LeafNode(data, 1, 0);

        assertEquals(0.0, node.getVariance(), DELTA);
        assertEquals(0.0, node.getPredictedClassValue(), DELTA);
    }

    @Test
    public void toStringContainsPredictionRangeAndVariance() {
        LeafNode node = new LeafNode(data, 0, 1);
        String value = node.toString();

        assertEquals(
            "LEAF : class=2.0 Nodo: [Examples:0-1] variance:2.0",
            value
        );
    }
}
