package testsupport;

import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static void configureFixtures() {
        Path fixtures = Paths.get("fixtures").toAbsolutePath().normalize();
        System.setProperty("map.fixtures", fixtures.toString());
        clearJdbcFailures();
    }

    public static void clearJdbcFailures() {
        String[] names = {"map.offline", "map.metadataFailure", "map.statementFailure",
                "map.queryFailure", "map.resultSetFailure", "map.resultSetCloseFailure",
                "map.statementCloseFailure", "map.closeFailure"};
        for (String name : names) System.clearProperty(name);
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
