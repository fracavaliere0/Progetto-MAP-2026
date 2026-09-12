package data;

import org.junit.Test;
import testsupport.TestSupport;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataTest {
    private Data load(String text) throws Exception {
        return new Data(TestSupport.dataFile(text).toString());
    }

    @Test
    public void loadsMinimumOneRowTrainingSet() throws Exception {
        Data data = load("@schema 1\n@desc x\n@target y\n@data 1\n-1.5,9.25\n");
        assertEquals(1, data.getNumberOfExamples());
        assertEquals(1, data.getNumberOfExplanatoryAttributes());
        assertEquals(-1.5, (Double) data.getExplanatoryValue(0, 0), 0.0);
        assertEquals(9.25, data.getClassValue(0), 0.0);
        assertEquals("x", data.getExplanatoryAttribute(0).getName());
        assertEquals("y", data.getClassAttribute().getName());
        assertEquals(1, data.getClassAttribute().getIndex());
        assertEquals("-1.5,9.25\n", data.toString());
    }

    @Test
    public void loadsMediumMixedTrainingSetAndAllBoundaryRows() throws Exception {
        Data data = load(TestSupport.mixedData());
        assertEquals(5, data.getNumberOfExamples());
        assertEquals(2, data.getNumberOfExplanatoryAttributes());
        assertTrue(data.getExplanatoryAttribute(0) instanceof DiscreteAttribute);
        assertTrue(data.getExplanatoryAttribute(1) instanceof ContinuousAttribute);
        assertEquals("medium", data.getExplanatoryValue(0, 0));
        assertEquals(5.0, (Double) data.getExplanatoryValue(0, 1), 0.0);
        assertEquals(50.0, data.getClassValue(0), 0.0);
        assertEquals("medium", data.getExplanatoryValue(4, 0));
        assertEquals(10.0, (Double) data.getExplanatoryValue(4, 1), 0.0);
        assertEquals(100.0, data.getClassValue(4), 0.0);
        assertEquals(3, ((DiscreteAttribute) data.getExplanatoryAttribute(0)).getNumberOfDistinctValues());
    }

    @Test
    public void loadsMaximumProjectFixture() throws Exception {
        Path servo = Paths.get("../../distribution/standalone/servo.dat").toAbsolutePath().normalize();
        assertTrue("servo.dat fixture should exist", Files.exists(servo));
        Data data = new Data(servo.toString());
        assertEquals(167, data.getNumberOfExamples());
        assertEquals(4, data.getNumberOfExplanatoryAttributes());
        assertEquals("E", data.getExplanatoryValue(0, 0));
        assertEquals(0.28125095, data.getClassValue(0), 0.0);
        assertEquals("A", data.getExplanatoryValue(166, 0));
        assertEquals(0.8062546, data.getClassValue(166), 0.0);
    }

    @Test
    public void sortsDiscreteValuesAscendingAndKeepsRowsTogether() throws Exception {
        Data data = load(TestSupport.mixedData());
        data.sort(data.getExplanatoryAttribute(0), 0, data.getNumberOfExamples() - 1);

        List<String> kinds = new ArrayList<String>();
        for (int i = 0; i < data.getNumberOfExamples(); i++) {
            kinds.add((String) data.getExplanatoryValue(i, 0));
            double amount = (Double) data.getExplanatoryValue(i, 1);
            double result = data.getClassValue(i);
            assertEquals(amount * 10.0, result, 0.0);
        }
        assertEquals(Arrays.asList("high", "low", "low", "medium", "medium"), kinds);
    }

    @Test
    public void sortsContinuousValuesAtMinimumMediumAndMaximumRanges() throws Exception {
        Data minimum = load(TestSupport.mixedData());
        String first = minimum.toString().split("\\n")[0];
        minimum.sort(minimum.getExplanatoryAttribute(1), 0, 0);
        assertEquals(first, minimum.toString().split("\\n")[0]);

        Data medium = load(TestSupport.mixedData());
        medium.sort(medium.getExplanatoryAttribute(1), 1, 3);
        assertEquals(5.0, (Double) medium.getExplanatoryValue(0, 1), 0.0);
        assertEquals(-10.0, (Double) medium.getExplanatoryValue(1, 1), 0.0);
        assertEquals(0.0, (Double) medium.getExplanatoryValue(2, 1), 0.0);
        assertEquals(100.0, (Double) medium.getExplanatoryValue(3, 1), 0.0);
        assertEquals(10.0, (Double) medium.getExplanatoryValue(4, 1), 0.0);

        Data maximum = load(TestSupport.mixedData());
        maximum.sort(maximum.getExplanatoryAttribute(1), 0, 4);
        double[] expected = {-10.0, 0.0, 5.0, 10.0, 100.0};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], (Double) maximum.getExplanatoryValue(i, 1), 0.0);
            assertEquals(expected[i] * 10.0, maximum.getClassValue(i), 0.0);
        }
    }

    @Test
    public void reversedSortRangeIsANoOp() throws Exception {
        Data data = load(TestSupport.mixedData());
        String before = data.toString();
        data.sort(data.getExplanatoryAttribute(0), 3, 2);
        assertEquals(before, data.toString());
    }

    @Test
    public void gettersRejectIndexesOutsideMinimumAndMaximumBoundaries() throws Exception {
        final Data data = load(TestSupport.mixedData());
        TestSupport.expect(ArrayIndexOutOfBoundsException.class, () -> data.getClassValue(-1));
        TestSupport.expect(ArrayIndexOutOfBoundsException.class, () -> data.getClassValue(5));
        TestSupport.expect(ArrayIndexOutOfBoundsException.class, () -> data.getExplanatoryValue(-1, 0));
        TestSupport.expect(ArrayIndexOutOfBoundsException.class, () -> data.getExplanatoryValue(0, -1));
        TestSupport.expect(ArrayIndexOutOfBoundsException.class, () -> data.getExplanatoryValue(5, 0));
        // The standalone implementation exposes the target column when passed the
        // first non-explanatory index; pin that legacy behavior explicitly.
        assertEquals(50.0, (Double) data.getExplanatoryValue(0, 2), 0.0);
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryValue(0, 3));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryAttribute(-1));
        TestSupport.expect(IndexOutOfBoundsException.class, () -> data.getExplanatoryAttribute(2));
    }

    @Test
    public void dataMainLoadsAndSortsTheMaximumFixture() throws Exception {
        Path localServo = Paths.get("servo.dat");
        Path source = Paths.get("../../distribution/standalone/servo.dat").toAbsolutePath().normalize();
        Files.copy(source, localServo);
        try {
            String output = TestSupport.captureOutput(() -> Data.main(new String[0]));
            assertTrue(output.contains("ORDER BY motor"));
            assertTrue(output.contains("ORDER BY vgain"));
            assertTrue(output.contains("A,A,3,2,5.7000422"));
        } finally {
            Files.deleteIfExists(localServo);
        }
    }

    @Test
    public void trainingDataExceptionConstructorsPreserveMessageAndCause() {
        TrainingDataException message = new TrainingDataException("bad training data");
        assertEquals("bad training data", message.getMessage());
        IllegalArgumentException cause = new IllegalArgumentException("bad number");
        TrainingDataException wrapped = new TrainingDataException(cause);
        assertEquals(cause, wrapped.getCause());
    }
}
