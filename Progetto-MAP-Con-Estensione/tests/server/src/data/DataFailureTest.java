package data;

import database.DatabaseConnectionException;
import database.EmptySetException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testsupport.TestSupport;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataFailureTest {
    @Before
    public void configureDatabase() {
        TestSupport.configureFixtures();
    }

    @After
    public void clearControls() {
        TestSupport.clearJdbcFailures();
    }

    @Test
    public void rejectsMinimumOneColumnTable() {
        TrainingDataException error = TestSupport.expect(
                TrainingDataException.class, () -> new Data("onecolumn"));
        assertEquals("La tabella non esiste o contiene meno di due colonne", error.getMessage());
    }

    @Test
    public void rejectsNonNumericTargetAtMaximumColumn() {
        TrainingDataException error = TestSupport.expect(
                TrainingDataException.class, () -> new Data("varchartarget"));
        assertEquals("L'ultima colonna della tabella deve essere numerica", error.getMessage());
    }

    @Test
    public void wrapsEmptyTable() {
        TrainingDataException error = TestSupport.expect(
                TrainingDataException.class, () -> new Data("empty"));
        assertTrue(error.getCause() instanceof EmptySetException);
    }

    @Test
    public void wrapsMissingTableAndMetadataFailures() {
        TrainingDataException missing = TestSupport.expect(
                TrainingDataException.class, () -> new Data("does-not-exist"));
        assertTrue(missing.getCause() instanceof SQLException);

        System.setProperty("map.metadataFailure", "true");
        TrainingDataException metadata = TestSupport.expect(
                TrainingDataException.class, () -> new Data("mixed"));
        assertTrue(metadata.getCause() instanceof SQLException);
    }

    @Test
    public void wrapsStatementQueryAndResultSetFailures() {
        String[] flags = {"map.statementFailure", "map.queryFailure", "map.resultSetFailure",
                "map.resultSetCloseFailure", "map.statementCloseFailure"};
        for (String flag : flags) {
            TestSupport.clearJdbcFailures();
            System.setProperty(flag, "true");
            TrainingDataException error = TestSupport.expect(
                    TrainingDataException.class, () -> new Data("mixed"));
            assertTrue(flag, error.getCause() instanceof SQLException);
        }
    }

    @Test
    public void wrapsUnavailableDatabase() {
        System.setProperty("map.offline", "true");
        TrainingDataException error = TestSupport.expect(
                TrainingDataException.class, () -> new Data("mixed"));
        assertTrue(error.getCause() instanceof DatabaseConnectionException);
        assertTrue(error.getCause().getCause() instanceof SQLException);
    }

    @Test
    public void closeFailureIsLoggedWithoutDiscardingLoadedData() throws Exception {
        System.setProperty("map.closeFailure", "true");
        final Data[] loaded = new Data[1];
        String output = TestSupport.captureOutput(() -> loaded[0] = new Data("minimum"));
        assertEquals(1, loaded[0].getNumberOfExamples());
        assertTrue(output.contains("Test close failure"));
    }

    @Test
    public void suppliedUnsupportedSqlTypesAreSkippedBeforeChoosingTarget() throws Exception {
        Data data = new Data("unsupportedtarget");
        assertEquals(1, data.getNumberOfExplanatoryAttributes());
        assertEquals("X", data.getExplanatoryAttribute(0).getName());
        assertEquals("Y", data.getClassAttribute().getName());
        assertEquals(2.0, data.getClassValue(0), 0.0);

        Data decimal = new Data("decimal");
        assertEquals("Y", decimal.getClassAttribute().getName());
        assertEquals(4.0, decimal.getClassValue(1), 0.0);
    }

    @Test
    public void trainingDataExceptionPreservesMessageAndCause() {
        assertEquals("bad", new TrainingDataException("bad").getMessage());
        SQLException cause = new SQLException("query");
        assertEquals(cause, new TrainingDataException(cause).getCause());
    }
}
