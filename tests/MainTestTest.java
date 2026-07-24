import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import support.KeyboardInput;
import support.TestData;
import utility.Keyboard;

/** Verifica i flussi principali dell'applicazione a riga di comando. */
public class MainTestTest {

    private InputStream originalInput;
    private PrintStream originalOutput;

    @Before
    public void setUp() {
        originalInput = System.in;
        originalOutput = System.out;
        Keyboard.setPrintErrors(false);
    }

    @After
    public void tearDown() {
        KeyboardInput.set(originalInput);
        System.setOut(originalOutput);
        Keyboard.setPrintErrors(true);
    }

    @Test
    public void applicationCanBeInstantiated() {
        assertTrue(new MainTest() instanceof MainTest);
    }

    @Test
    public void mainReportsAnInvalidTrainingSet() {
        String missing = "missing-" + System.nanoTime() + ".dat";
        KeyboardInput.set(missing + "\n");

        String output = captureMain(new String[0]);

        assertTrue(output.contains("Starting data acquisition phase!"));
        assertTrue(output.contains("TrainingDataException"));
    }

    @Test
    public void mainLoadsTrainsAndPredictsTheMinimumTrainingSet() throws Exception {
        String file = TestData.createSingleExampleData();
        KeyboardInput.set(file + "\nn\n");

        String output = captureMain(null);

        assertTrue(output.contains("Starting learning phase!"));
        assertTrue(output.contains("Starting prediction phase!"));
        assertTrue(output.contains("5.0"));
        assertTrue(output.contains("Would you repeat ? (y/n)"));
    }

    @Test
    public void mainReportsAnUnknownPredictionValue() throws Exception {
        String file = TestData.createTreeData();
        KeyboardInput.set(file + "\n-1\nn\n");

        String output = captureMain(new String[] {"ignored"});

        assertTrue(output.contains("Starting prediction phase!"));
        assertTrue(output.contains("UnknownValueException"));
        assertTrue(output.contains("between 0 and 2"));
    }

    private String captureMain(String[] arguments) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        MainTest.main(arguments);
        return output.toString().replace("\r\n", "\n");
    }
}
