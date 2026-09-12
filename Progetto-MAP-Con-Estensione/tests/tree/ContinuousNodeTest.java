package tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import data.ContinuousAttribute;
import data.Data;
import java.io.File;
import org.junit.Test;
import support.TestData;

/** Verifica i requisiti MAP5 per attributi continui e serializzazione. */
public class ContinuousNodeTest {

    @Test
    public void dataParsesAndSortsContinuousValuesAsDoubles() throws Exception {
        Data data = new Data(continuousData());

        assertTrue(data.getExplanatoryAttribute(0) instanceof ContinuousAttribute);
        assertTrue(data.getExplanatoryValue(0, 0) instanceof Double);

        data.sort(data.getExplanatoryAttribute(0), 0, 3);
        assertEquals(Double.valueOf(1.0), data.getExplanatoryValue(0, 0));
        assertEquals(Double.valueOf(4.0), data.getExplanatoryValue(3, 0));
    }

    @Test
    public void continuousNodeFindsAndTestsTheBestThreshold() throws Exception {
        Data data = new Data(continuousData());
        ContinuousNode node = new ContinuousNode(data, 0, 3,
                (ContinuousAttribute) data.getExplanatoryAttribute(0));

        assertEquals(2, node.getNumberOfChildren());
        assertTrue(node.formulateQuery().contains("value<=2.0"));
        assertEquals(0, node.testCondition(Double.valueOf(2.0)));
        assertEquals(1, node.testCondition(Double.valueOf(3.0)));
        assertEquals(-1, node.testCondition("invalid"));
    }

    @Test
    public void constantContinuousAttributeProducesOneBranch() throws Exception {
        Data data = new Data(TestData.create(
            "@schema 1\n" +
            "@desc value\n" +
            "@target target\n" +
            "@data 3\n" +
            "1,1\n1,2\n1,3\n"
        ));

        ContinuousNode node = new ContinuousNode(data, 0, 2,
                (ContinuousAttribute) data.getExplanatoryAttribute(0));
        assertEquals(1, node.getNumberOfChildren());
    }

    @Test
    public void regressionTreeCanBeSavedAndLoaded() throws Exception {
        Data data = new Data(continuousData());
        RegressionTree original = new RegressionTree(data);
        File archive = File.createTempFile("map-tree-", ".dmp");
        archive.deleteOnExit();

        original.salva(archive.getAbsolutePath());
        RegressionTree loaded = RegressionTree.carica(archive.getAbsolutePath());

        assertEquals(original.toString(), loaded.toString());
        assertTrue(loaded.getRoot() instanceof ContinuousNode);
    }

    private String continuousData() throws Exception {
        return TestData.create(
            "@schema 1\n" +
            "@desc value\n" +
            "@target target\n" +
            "@data 4\n" +
            "4,10\n" +
            "1,1\n" +
            "3,10\n" +
            "2,1\n"
        );
    }
}
