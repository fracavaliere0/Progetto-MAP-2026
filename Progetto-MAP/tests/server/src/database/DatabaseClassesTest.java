package database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testsupport.TestSupport;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DatabaseClassesTest {
    private DbAccess db;

    @Before
    public void connect() throws Exception {
        TestSupport.configureFixtures();
        db = new DbAccess();
        db.initConnection();
    }

    @After
    public void close() {
        TestSupport.clearJdbcFailures();
        if (db != null) db.closeConnection();
    }

    @Test
    public void columnCoversStringNumberAndUnknownTypes() {
        Column string = new Column("name", "string");
        assertEquals("name", string.getColumnName());
        assertFalse(string.isNumber());
        assertEquals("name:string", string.toString());

        Column number = new Column("age", "number");
        assertTrue(number.isNumber());
        assertEquals("age:number", number.toString());

        Column unknown = new Column("payload", "NUMBER");
        assertFalse(unknown.isNumber());
    }

    @Test
    public void exampleSupportsMinimumMediumAndMaximumPositions() {
        Example example = new Example();
        example.add("minimum");
        example.add(0.0);
        example.add("maximum");
        assertEquals("minimum", example.get(0));
        assertEquals(0.0, (Double) example.get(1), 0.0);
        assertEquals("maximum", example.get(2));
        assertEquals("minimum 0.0 maximum ", example.toString());
        assertNull("The supplied MAP implementation has no iterator yet", example.iterator());
        TestSupport.expect(IndexOutOfBoundsException.class, () -> example.get(-1));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> example.get(3));
    }

    @Test
    public void exampleComparisonChecksFirstMiddleLastAndEqualValues() {
        Example base = example(1.0, "b", 10.0);
        assertEquals(0, base.compareTo(example(1.0, "b", 10.0)));
        assertTrue(base.compareTo(example(2.0, "b", 10.0)) > 0);
        assertTrue(base.compareTo(example(0.0, "b", 10.0)) < 0);
        assertTrue(base.compareTo(example(1.0, "c", 10.0)) > 0);
        assertTrue(base.compareTo(example(1.0, "b", 20.0)) > 0);
        assertEquals(0, base.compareTo(example(1.0)));
    }

    private Example example(Object... values) {
        Example result = new Example();
        for (Object value : values) result.add(value);
        return result;
    }

    @Test
    public void dbAccessConnectsReturnsAndClosesConnection() throws Exception {
        Connection connection = db.getConnection();
        assertNotNull(connection);
        assertFalse(connection.isClosed());
        db.closeConnection();
        assertTrue(connection.isClosed());
        db.closeConnection();

        DbAccess neverOpened = new DbAccess();
        assertNull(neverOpened.getConnection());
        neverOpened.closeConnection();
    }

    @Test
    public void dbAccessWrapsSqlConnectionFailureAndReportsCloseFailure() throws Exception {
        db.closeConnection();
        System.setProperty("map.offline", "true");
        DbAccess offline = new DbAccess();
        DatabaseConnectionException error = TestSupport.expect(
                DatabaseConnectionException.class, offline::initConnection);
        assertTrue(error.getCause() instanceof SQLException);
        assertNull(offline.getConnection());

        TestSupport.clearJdbcFailures();
        DbAccess closeFailure = new DbAccess();
        closeFailure.initConnection();
        System.setProperty("map.closeFailure", "true");
        String output = TestSupport.captureOutput(closeFailure::closeConnection);
        assertTrue(output.contains("Test close failure"));
    }

    @Test
    public void tableSchemaMapsEverySupportedTypeAndSkipsUnknownTypes() throws Exception {
        TableSchema schema = new TableSchema(db, "alltypes");
        assertEquals(9, schema.getNumberOfAttributes());
        String[] expectedNames = {"C_CHAR", "C_VARCHAR", "C_LONGVARCHAR", "C_BIT",
                "C_SHORT", "C_INT", "C_LONG", "C_FLOAT", "C_DOUBLE"};
        int index = 0;
        for (Column column : schema) {
            assertEquals(expectedNames[index], column.getColumnName());
            assertEquals(index >= 4, column.isNumber());
            index++;
        }
        assertEquals(9, index);
        assertEquals("C_CHAR", schema.getColumn(0).getColumnName());
        assertEquals("C_DOUBLE", schema.getColumn(8).getColumnName());
        TestSupport.expect(IndexOutOfBoundsException.class, () -> schema.getColumn(-1));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> schema.getColumn(9));
    }

    @Test
    public void tableDataReadsRowsAndDistinctStringAndNumberValues() throws Exception {
        TableData tableData = new TableData(db);
        List<Example> rows = tableData.getTransazioni("mixed");
        assertEquals(5, rows.size());
        assertEquals("medium", rows.get(0).get(0));
        assertEquals(5.0, (Double) rows.get(0).get(1), 0.0);
        assertEquals(50.0, (Double) rows.get(0).get(2), 0.0);

        TableSchema schema = new TableSchema(db, "mixed");
        Set<Object> strings = tableData.getDistinctColumnValues("mixed", schema.getColumn(0));
        assertEquals(Arrays.<Object>asList("high", "low", "medium"), new ArrayList<Object>(strings));
        Set<Object> numbers = tableData.getDistinctColumnValues("mixed", schema.getColumn(1));
        assertEquals(Arrays.<Object>asList(-10.0, 0.0, 5.0, 10.0, 100.0),
                new ArrayList<Object>(numbers));
    }

    @Test
    public void tableDataHandlesMinimumEmptyAndZeroColumnResults() throws Exception {
        TableData tableData = new TableData(db);
        assertEquals(1, tableData.getTransazioni("minimum").size());
        TestSupport.expect(EmptySetException.class, () -> tableData.getTransazioni("empty"));
        TestSupport.expect(SQLException.class, () -> tableData.getTransazioni("zerocolumn"));

        TableSchema emptySchema = new TableSchema(db, "empty");
        assertTrue(tableData.getDistinctColumnValues("empty", emptySchema.getColumn(0)).isEmpty());
    }

    @Test
    public void exceptionConstructorsPreserveMessagesAndCauses() {
        EmptySetException empty = new EmptySetException();
        assertNull(empty.getMessage());
        assertEquals("none", new EmptySetException("none").getMessage());

        SQLException cause = new SQLException("offline");
        DatabaseConnectionException wrapped = new DatabaseConnectionException(cause);
        assertEquals(cause, wrapped.getCause());
        assertEquals("connection", new DatabaseConnectionException("connection").getMessage());
    }
}
