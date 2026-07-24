package data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import support.TestData;

/** Verifica caricamento, accesso e ordinamento dei training set. */
public class DataTest {

    private Data data;

    @Before
    public void setUp() throws Exception {
        data = new Data(TestData.createBoundaryData());
    }

    @Test
    public void gettersReturnSchemaInformation() {
        assertEquals(3, data.getNumberOfExamples());
        assertEquals(3, data.getNumberOfExplanatoryAttributes());
        assertEquals("target", data.getClassAttribute().getName());
        assertEquals(3, data.getClassAttribute().getIndex());
    }

    @Test
    public void gettersReturnLowerMiddleAndUpperExamples() {
        assertEquals("C", data.getExplanatoryValue(0, 0));
        assertEquals("middle", data.getExplanatoryValue(2, 1));
        assertEquals("X", data.getExplanatoryValue(1, 2));

        assertEquals(Double.valueOf(30.0), data.getClassValue(0));
        assertEquals(Double.valueOf(10.0), data.getClassValue(1));
        assertEquals(Double.valueOf(20.0), data.getClassValue(2));
    }

    @Test
    public void getExplanatoryAttributeReturnsLowerMiddleAndUpperAttributes() {
        assertEquals("first", data.getExplanatoryAttribute(0).getName());
        assertEquals("second", data.getExplanatoryAttribute(1).getName());
        assertEquals("third", data.getExplanatoryAttribute(2).getName());
    }

    @Test
    public void sortOrdersTheCompleteRange() {
        data.sort(data.getExplanatoryAttribute(0), 0, 2);

        assertEquals("A", data.getExplanatoryValue(0, 0));
        assertEquals("B", data.getExplanatoryValue(1, 0));
        assertEquals("C", data.getExplanatoryValue(2, 0));
        assertEquals(Double.valueOf(10.0), data.getClassValue(0));
        assertEquals(Double.valueOf(20.0), data.getClassValue(1));
        assertEquals(Double.valueOf(30.0), data.getClassValue(2));
    }

    @Test
    public void sortAcceptsLowerMiddleAndUpperSingleElementRanges() throws Exception {
        String original = data.toString();

        data.sort(data.getExplanatoryAttribute(0), 0, 0);
        data.sort(data.getExplanatoryAttribute(1), 1, 1);
        data.sort(data.getExplanatoryAttribute(2), 2, 2);

        assertEquals(original, data.toString());
    }

    @Test
    public void toStringReturnsEveryLoadedRow() {
        assertEquals(
            "C,high,Z,30.0\n" +
            "A,low,X,10.0\n" +
            "B,middle,Y,20.0\n",
            data.toString()
        );
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getClassValueRejectsIndexBelowLowerLimit() {
        data.getClassValue(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getClassValueRejectsIndexAboveUpperLimit() {
        data.getClassValue(3);
    }

    @Test
    public void constructorRejectsInvalidSchemas() throws IOException {
        assertInvalid("");
        assertInvalid("not-a-schema\n");
        assertInvalid("@schema invalid\n");
        assertInvalid("@schema 1\n@desc value A\n@data 1\nA,1\n");
        assertInvalid("@schema 1\n@desc value A\n@target target\n@data 0\n");
        assertInvalid("@schema 1\n@desc value A\n@target target\n");
    }

    @Test
    public void constructorRejectsInvalidRows() throws IOException {
        assertInvalid(
            "@schema 1\n@desc value A\n@target target\n@data 1\nA\n"
        );
        assertInvalid(
            "@schema 1\n@desc value A\n@target target\n@data 2\nA,1\n"
        );
        assertInvalid(
            "@schema 1\n@desc value A\n@target target\n@data 1\nA,invalid\n"
        );
    }

    @Test
    public void constructorReportsMissingFilesAsTrainingDataErrors() {
        try {
            new Data("missing-" + System.nanoTime() + ".dat");
            fail("Era attesa TrainingDataException");
        } catch (TrainingDataException exception) {
            assertTrue(exception.getCause() instanceof IOException);
        }
    }

    private void assertInvalid(String content) throws IOException {
        try {
            new Data(TestData.create(content));
            fail("Era attesa TrainingDataException per: " + content);
        } catch (TrainingDataException expected) {
            assertTrue(expected.getMessage().length() > 0);
        }
    }
}
