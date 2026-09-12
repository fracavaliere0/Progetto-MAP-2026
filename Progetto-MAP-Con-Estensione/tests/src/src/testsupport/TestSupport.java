package testsupport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import utility.Keyboard;

import static org.junit.Assert.fail;

/** Shared helpers for the standalone black-box and white-box tests. */
public final class TestSupport {
    private TestSupport() {}

    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static <T extends Throwable> T expect(Class<T> type, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (type.isInstance(thrown)) {
                return type.cast(thrown);
            }
            AssertionError error = new AssertionError(
                    "Expected " + type.getName() + " but got " + thrown.getClass().getName(), thrown);
            throw error;
        }
        fail("Expected " + type.getName() + " to be thrown");
        return null;
    }

    public static Path dataFile(String contents) throws Exception {
        Path file = Files.createTempFile("map-data-", ".dat");
        Files.write(file, contents.getBytes(StandardCharsets.UTF_8));
        file.toFile().deleteOnExit();
        return file;
    }

    public static String mixedData() {
        return "@schema 2\n"
                + "@desc kind low,medium,high\n"
                + "@desc amount\n"
                + "@target result\n"
                + "@data 5\n"
                + "medium,5,50\n"
                + "low,-10,-100\n"
                + "high,100,1000\n"
                + "low,0,0\n"
                + "medium,10,100\n";
    }

    public static String continuousData() {
        return "@schema 1\n"
                + "@desc x\n"
                + "@target y\n"
                + "@data 6\n"
                + "10,100\n"
                + "0,0\n"
                + "10,110\n"
                + "5,50\n"
                + "5,60\n"
                + "0,10\n";
    }

    public static String nestedDiscreteData() {
        StringBuilder value = new StringBuilder();
        value.append("@schema 2\n@desc first A,B\n@desc second u,v\n@target y\n@data 12\n");
        for (int i = 0; i < 3; i++) value.append("A,u,0\n");
        for (int i = 0; i < 3; i++) value.append("A,v,10\n");
        for (int i = 0; i < 3; i++) value.append("B,u,20\n");
        for (int i = 0; i < 3; i++) value.append("B,v,30\n");
        return value.toString();
    }

    /** Rebinds the legacy static keyboard reader after replacing System.in. */
    public static void keyboardInput(String input) throws Exception {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        setKeyboardField("in", new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
        setKeyboardField("reader", null);
        setKeyboardField("current_token", null);
    }

    private static void setKeyboardField(String name, Object value) throws Exception {
        Field field = Keyboard.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    public static String captureOutput(ThrowingRunnable action) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(bytes, true, "UTF-8"));
            action.run();
        } finally {
            System.setOut(original);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }
}
