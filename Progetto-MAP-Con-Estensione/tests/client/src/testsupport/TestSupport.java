package testsupport;

import org.junit.Assert;
import utility.Keyboard;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public final class TestSupport {
    private TestSupport() {}

    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static <T extends Throwable> T expect(Class<T> type, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (type.isInstance(thrown)) return type.cast(thrown);
            throw new AssertionError("Expected " + type.getName() + " but got "
                    + thrown.getClass().getName(), thrown);
        }
        Assert.fail("Expected " + type.getName() + " to be thrown");
        return null;
    }

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
        return bytes.toString("UTF-8");
    }
}
