package data;

import database.DatabaseConnectionException;
import database.EmptySetException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testsupport.TestSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataTest {
    @Before
    public void setUpDatabaseFixture() {
        TestSupport.configureFixtures();
    }

    @After
    public void clearDatabaseFailures() {
        TestSupport.clearJdbcFailures();
    }

    @Test
    public void loadsMinimumMediumAndMaximumTables() throws Exception {
        Data minimum = new Data("minimum");
        assertEquals(1, minimum.getNumberOfExamples());
        assertEquals(1, minimum.getNumberOfExplanatoryAttributes());
        assertEquals("only", minimum.getExplanatoryValue(0, 0));
        assertEquals(42.0, minimum.getClassValue(0), 0.0);
        assertEquals("x", minimum.getExplanatoryAttribute(0).getName());
        assertEquals("y", minimum.getClassAttribute().getName());
        assertEquals(1, minimum.getClassAttribute().getIndex());

        Data medium = new Data("mixed");
        assertEquals(5, medium.getNumberOfExamples());
        assertEquals(2, medium.getNumberOfExplanatoryAttributes());
        assertTrue(medium.getExplanatoryAttribute(0) instanceof DiscreteAttribute);
        assertTrue(medium.getExplanatoryAttribute(1) instanceof ContinuousAttribute);
        assertEquals(3, ((DiscreteAttribute) medium.getExplanatoryAttribute(0)).getNumberOfDistinctValues());
        assertEquals("medium", medium.getExplanatoryValue(0, 0));
        assertEquals(5.0, (Double) medium.getExplanatoryValue(0, 1), 0.0);
        assertEquals(50.0, medium.getClassValue(0), 0.0);
        assertEquals("medium,5.0,50.0\nlow,-10.0,-100.0\nhigh,100.0,1000.0\n"
                + "low,0.0,0.0\nmedium,10.0,100.0\n", medium.toString());

        Data maximum = new Data("servo");
        assertEquals(167, maximum.getNumberOfExamples());
        assertEquals(4, maximum.getNumberOfExplanatoryAttributes());
        assertEquals("E", maximum.getExplanatoryValue(0, 0));
        assertEquals(0.28125095, maximum.getClassValue(0), 0.0);
        assertEquals("A", maximum.getExplanatoryValue(166, 0));
        assertEquals(0.8062546, maximum.getClassValue(166), 0.0);
    }

    @Test
    public void provaTableUsesFloatColumnsAndSortedDiscreteDomain() throws Exception {
        Data data = new Data("provaC");
        assertEquals("X", data.getExplanatoryAttribute(0).getName());
        assertEquals("Y", data.getExplanatoryAttribute(1).getName());
        assertEquals("C", data.getClassAttribute().getName());
        assertTrue(data.getExplanatoryAttribute(1) instanceof ContinuousAttribute);
        DiscreteAttribute x = (DiscreteAttribute) data.getExplanatoryAttribute(0);
        List<String> values = new ArrayList<String>();
        for (String value : x) values.add(value);
        assertEquals(Arrays.asList("A", "B"), values);
    }

    @Test
    public void sortsDiscreteAndContinuousMinimumMediumAndMaximumRanges() throws Exception {
        Data minimum = new Data("mixed");
        String before = minimum.toString();
        minimum.sort(minimum.getExplanatoryAttribute(0), 3, 2);
        assertEquals(before, minimum.toString());
        minimum.sort(minimum.getExplanatoryAttribute(0), 0, 0);
        assertEquals(before, minimum.toString());

        Data medium = new Data("mixed");
        medium.sort(medium.getExplanatoryAttribute(1), 1, 3);
        assertEquals(5.0, (Double) medium.getExplanatoryValue(0, 1), 0.0);
        assertEquals(-10.0, (Double) medium.getExplanatoryValue(1, 1), 0.0);
        assertEquals(0.0, (Double) medium.getExplanatoryValue(2, 1), 0.0);
        assertEquals(100.0, (Double) medium.getExplanatoryValue(3, 1), 0.0);
        assertEquals(10.0, (Double) medium.getExplanatoryValue(4, 1), 0.0);

        Data maximum = new Data("mixed");
        maximum.sort(maximum.getExplanatoryAttribute(0), 0, 4);
        assertEquals("high", maximum.getExplanatoryValue(0, 0));
        assertEquals("low", maximum.getExplanatoryValue(1, 0));
        assertEquals("low", maximum.getExplanatoryValue(2, 0));
        assertEquals("medium", maximum.getExplanatoryValue(3, 0));
        assertEquals("medium", maximum.getExplanatoryValue(4, 0));
        for (int i = 0; i < 5; i++) {
            assertEquals((Double) maximum.getExplanatoryValue(i, 1) * 10.0,
                    maximum.getClassValue(i), 0.0);
        }
    }

    @Test
    public void gettersRejectEveryIndexJustOutsideTheValidRange() throws Exception {
        final Data data = new Data("mixed");
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getClassValue(-1));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getClassValue(5));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryValue(-1, 0));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryValue(5, 0));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryValue(0, -1));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryValue(0, 2));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryAttribute(-1));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryAttribute(2));
    }

    @Test
    public void wrapsConnectionFailure() {
        System.setProperty("map.offline", "true");
        TrainingDataException error = TestSupport.expect(TrainingDataException.class, () -> new Data("mixed"));
        assertTrue(error.getCause() instanceof DatabaseConnectionException);
        assertTrue(error.getCause().getCause() instanceof SQLException);
    }

    @Test
    public void rejectsMissingSmallNonNumericAndEmptyTables() {
        TrainingDataException missing = TestSupport.expect(
                TrainingDataException.class, () -> new Data("does_not_exist"));
        assertTrue(missing.getCause() instanceof SQLException);

        TrainingDataException small = TestSupport.expect(
                TrainingDataException.class, () -> new Data("onecolumn"));
        assertEquals("La tabella non esiste o contiene meno di due colonne", small.getMessage());

        TrainingDataException textTarget = TestSupport.expect(
                TrainingDataException.class, () -> new Data("varchartarget"));
        assertEquals("L'ultima colonna della tabella deve essere numerica", textTarget.getMessage());

        TrainingDataException empty = TestSupport.expect(
                TrainingDataException.class, () -> new Data("empty"));
        assertTrue(empty.getCause() instanceof EmptySetException);
    }

    @Test
    public void wrapsMetadataQueryAndResultSetSqlFailures() {
        String[] controls = {"map.metadataFailure", "map.statementFailure",
                "map.queryFailure", "map.resultSetFailure", "map.resultSetCloseFailure",
                "map.statementCloseFailure"};
        for (String control : controls) {
            TestSupport.clearJdbcFailures();
            System.setProperty(control, "true");
            TrainingDataException error = TestSupport.expect(
                    TrainingDataException.class, () -> new Data("mixed"));
            assertTrue(control, error.getCause() instanceof SQLException);
        }
    }

    @Test
    public void closeFailureIsReportedWithoutDiscardingLoadedData() throws Exception {
        System.setProperty("map.closeFailure", "true");
        final Data[] result = new Data[1];
        String output = TestSupport.captureOutput(() -> result[0] = new Data("minimum"));
        assertEquals(1, result[0].getNumberOfExamples());
        assertTrue(output.contains("Test close failure"));
    }

    @Test
    public void unsupportedSqlTypesAreIgnoredAsImplemented() throws Exception {
        Data data = new Data("unsupportedtarget");
        assertEquals(1, data.getNumberOfExplanatoryAttributes());
        assertEquals("Y", data.getClassAttribute().getName());
        assertEquals(2.0, data.getClassValue(0), 0.0);

        Data decimal = new Data("decimal");
        assertEquals(1, decimal.getNumberOfExplanatoryAttributes());
        assertEquals("Y", decimal.getClassAttribute().getName());
    }
}
