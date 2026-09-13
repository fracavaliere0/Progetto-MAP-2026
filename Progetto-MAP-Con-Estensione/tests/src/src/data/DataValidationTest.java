package data;

import org.junit.Test;
import testsupport.TestSupport;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DataValidationTest {
    private TrainingDataException invalid(String text) throws Exception {
        Path file = TestSupport.dataFile(text);
        return TestSupport.expect(TrainingDataException.class, () -> new Data(file.toString()));
    }

    @Test
    public void rejectsMissingAndEmptyFiles() throws Exception {
        Path missing = Files.createTempDirectory("missing-map-file-").resolve("not-there.dat");
        TrainingDataException notFound = TestSupport.expect(
                TrainingDataException.class, () -> new Data(missing.toString()));
        assertTrue(notFound.getCause() instanceof java.io.FileNotFoundException);

        TrainingDataException empty = invalid("");
        assertTrue(empty.getCause() instanceof java.util.NoSuchElementException);
    }

    @Test
    public void rejectsMinimumMediumAndMaximumInvalidSchemaCounts() throws Exception {
        assertEquals("Errore nello schema", invalid("not-a-schema 1\n").getMessage());
        assertEquals("Errore nello schema", invalid("@schema -1\n").getMessage());

        TrainingDataException maximum = invalid("@schema 2147483648\n");
        assertTrue(maximum.getCause() instanceof NumberFormatException);
        TrainingDataException malformed = invalid("@schema many\n");
        assertTrue(malformed.getCause() instanceof NumberFormatException);
    }

    @Test
    public void rejectsMalformedAttributeDescriptions() throws Exception {
        String[] invalidSchemas = {
            "@schema 1\n@desc\n@target y\n@data 1\n1,2\n",
            "@schema 1\n@desc x a,b extra\n@target y\n@data 1\na,2\n",
            "@schema 1\n@unknown x\n@target y\n@data 1\n1,2\n",
            "@schema 1\n@desc x\n@desc extra\n@target y\n@data 1\n1,2\n",
            "@schema 2\n@target y\n@data 1\n1\n",
            "@schema 1\n@desc x\n@target y extra\n@data 1\n1,2\n",
            "@schema 1\n@desc x\n@target y\n@target z\n@data 1\n1,2\n",
            "@schema 2\n@desc x\n@target y\n@data 1\n1,2\n",
            "@schema 1\n@desc x\n@data 1\n1,2\n",
            "@schema 1\n@desc x\n"
        };

        for (String schema : invalidSchemas) {
            assertNotNull(invalid(schema));
        }
    }

    @Test
    public void rejectsEmptyMalformedAndOverflowingDeclaredRowCounts() throws Exception {
        String prefix = "@schema 1\n@desc x\n@target y\n";
        assertEquals("Training set vuoto", invalid(prefix + "@data 0\n").getMessage());
        assertEquals("Training set vuoto", invalid(prefix + "@data -1\n").getMessage());
        assertTrue(invalid(prefix + "@data many\n").getCause() instanceof NumberFormatException);
        assertNotNull(invalid(prefix + "@data\n"));
        assertEquals("Errore nei dati", invalid(prefix + "@data 1\n1,2\n2,3\n").getMessage());
    }

    @Test
    public void rejectsMissingShortWideAndNonNumericRows() throws Exception {
        String prefix = "@schema 2\n@desc category A,B\n@desc amount\n@target y\n";
        assertEquals("Errore nei dati", invalid(prefix + "@data 2\nA,1,2\n").getMessage());
        assertEquals("Errore nei dati", invalid(prefix + "@data 1\nA,1\n").getMessage());
        assertEquals("Errore nei dati", invalid(prefix + "@data 1\nA,1,2,3\n").getMessage());
        assertTrue(invalid(prefix + "@data 1\nA,nope,2\n").getCause() instanceof NumberFormatException);
        assertTrue(invalid(prefix + "@data 1\nA,1,nope\n").getCause() instanceof NumberFormatException);
    }

    @Test
    public void acceptsZeroExplanatoryAttributesAndUndeclaredDiscreteValuesAsImplemented() throws Exception {
        Data targetOnly = new Data(TestSupport.dataFile(
                "@schema 0\n@target y\n@data 1\n42\n").toString());
        assertEquals(0, targetOnly.getNumberOfExplanatoryAttributes());
        assertEquals(42.0, targetOnly.getClassValue(0), 0.0);

        Data openDomain = new Data(TestSupport.dataFile(
                "@schema 1\n@desc category A,B\n@target y\n@data 1\nC,3\n").toString());
        assertEquals("C", openDomain.getExplanatoryValue(0, 0));
    }
}
