import data.Data;
import org.junit.After;
import org.junit.Test;
import testsupport.TestSupport;
import tree.RegressionTree;
import utility.Keyboard;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MainTestIntegrationTest {
    @After
    public void restoreKeyboard() {
        Keyboard.setPrintErrors(true);
        Keyboard.resetErrorCount(0);
    }

    private Path modelBase(String data) throws Exception {
        Path directory = Files.createTempDirectory("map-main-");
        Path base = directory.resolve("model");
        Files.write(Paths.get(base + ".dat"), data.getBytes(StandardCharsets.UTF_8));
        return base;
    }

    private String runMain(String input) throws Exception {
        TestSupport.keyboardInput(input);
        return TestSupport.captureOutput(() -> MainTest.main(new String[0]));
    }

    @Test
    public void learnsSavesAndPredictsARegressionTree() throws Exception {
        Path base = modelBase("@schema 1\n@desc x same\n@target y\n@data 3\nsame,1\nsame,2\nsame,3\n");
        String output = runMain("1\n" + base + "\nn\n");

        assertTrue(output.contains("Starting data acquisition phase!"));
        assertTrue(output.contains("Starting learning phase!"));
        assertTrue(output.contains("Class=2.0"));
        assertTrue(output.contains("Starting prediction phase!"));
        assertTrue(output.contains("2.0"));
        assertTrue(Files.isRegularFile(Paths.get(base + ".dmp")));
    }

    @Test
    public void menuRetriesThenLoadsAndRepeatsPrediction() throws Exception {
        Path base = modelBase("@schema 1\n@desc x same\n@target y\n@data 1\nsame,42\n");
        RegressionTree tree = new RegressionTree(new Data(base + ".dat"));
        tree.salva(base + ".dmp");

        String output = runMain("0\n3\n2\n" + base + "\ny\nn\n");
        assertTrue(output.contains("Class=42.0"));
        assertTrue(output.indexOf("Starting prediction phase!")
                != output.lastIndexOf("Starting prediction phase!"));
    }

    @Test
    public void reportsTrainingDataFailureAndReturns() throws Exception {
        Path missing = Files.createTempDirectory("map-main-missing-").resolve("absent");
        String output = runMain("1\n" + missing + "\n");
        assertTrue(output.contains("data.TrainingDataException"));
        assertFalse(output.contains("Starting learning phase!"));
    }

    @Test
    public void reportsSaveFailureButStillAllowsPrediction() throws Exception {
        Path base = modelBase("@schema 1\n@desc x same\n@target y\n@data 1\nsame,7\n");
        Files.createDirectory(Paths.get(base + ".dmp"));

        String output = runMain("1\n" + base + "\nn\n");
        assertTrue(output.contains("java.io.FileNotFoundException"));
        assertTrue(output.contains("Class=7.0"));
        assertTrue(output.contains("Starting prediction phase!"));
    }

    @Test
    public void reportsMissingArchiveAndReturns() throws Exception {
        Path missing = Files.createTempDirectory("map-main-archive-").resolve("missing");
        String output = runMain("2\n" + missing + "\n");
        assertTrue(output.contains("FileNotFoundException"));
        assertFalse(output.contains("********* RULES"));
    }

    @Test
    public void reportsUnknownPredictionPath() throws Exception {
        Path base = modelBase(TestSupport.nestedDiscreteData());
        new RegressionTree(new Data(base + ".dat")).salva(base + ".dmp");

        String output = runMain("2\n" + base + "\n-1\nn\n");
        assertTrue(output.contains("The answer should be an integer between 0 and 1"));
        assertTrue(output.contains("Would you repeat ?"));
    }

    @Test
    public void dataDemonstrationLoadsAndSortsTheMaximumFixture() throws Exception {
        Path source = Paths.get("../../distribution/standalone/servo.dat").toAbsolutePath().normalize();
        Path local = Paths.get("servo.dat").toAbsolutePath().normalize();
        boolean copied = !source.equals(local);
        if (copied) Files.copy(source, local, StandardCopyOption.REPLACE_EXISTING);
        try {
            String output = TestSupport.captureOutput(() -> Data.main(new String[0]));
            assertTrue(output.contains("ORDER BY motor"));
            assertTrue(output.contains("ORDER BY vgain"));
            assertTrue(output.contains("E,E,5,4,0.28125095"));
        } finally {
            if (copied) Files.deleteIfExists(local);
        }
    }
}
