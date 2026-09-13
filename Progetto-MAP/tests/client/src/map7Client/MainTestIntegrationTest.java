package map7Client;

import org.junit.After;
import org.junit.Test;
import testsupport.TestSupport;
import utility.Keyboard;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MainTestIntegrationTest {
    @After
    public void restoreKeyboard() {
        Keyboard.setPrintErrors(true);
        Keyboard.resetErrorCount(0);
    }

    private String run(FakeServer server, String input) throws Exception {
        TestSupport.keyboardInput(input);
        String output = TestSupport.captureOutput(
                () -> MainTest.main(new String[] {"127.0.0.1", Integer.toString(server.port())}));
        server.await();
        return output;
    }

    @Test
    public void learnFlowUsesReferenceAcknowledgementsAndFinalQueryMarker() throws Exception {
        AtomicReference<String> table = new AtomicReference<String>();
        AtomicInteger path = new AtomicInteger(Integer.MIN_VALUE);
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(0, in.readObject());
            table.set(in.readObject().toString());
            send(out, "Table found!", "OK");
            assertEquals(1, in.readObject());
            send(out, "OK");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "0:x<=5.0\n1:x>5.0\n");
            path.set((Integer) in.readObject());
            send(out, "QUERY", "OK", 12.5);
        });

        String output = run(server, "bad\n0\n3\n1\ntraining_table\n1\nn\n");
        assertEquals("training_table", table.get());
        assertEquals(1, path.get());
        assertTrue(output.contains("Please type an integer"));
        assertTrue(output.contains("Starting data acquisition phase!"));
        assertTrue(output.contains("Starting learning phase!"));
        assertTrue(output.contains("0:x<=5.0"));
        assertTrue(output.contains("Predicted class:12.5"));
    }

    @Test
    public void loadFlowPredictsAtRootLeafWithoutRequestingAnIndex() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            assertEquals("archive", in.readObject());
            send(out, "Table found!", "OK");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "OK", -Double.MAX_VALUE);
        });

        String output = run(server, "2\narchive\nn\n");
        assertTrue(output.contains("Predicted class:" + (-Double.MAX_VALUE)));
        assertFalse(output.contains("Starting data acquisition phase!"));
    }

    @Test
    public void repeatPredictionValidatesYesOrNoAndRecoversFromServerError() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            in.readObject();
            send(out, "Table found!", "OK");
            assertEquals(3, in.readObject());
            send(out, "No tree available.");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "OK", Double.MAX_VALUE);
        });

        String output = run(server, "2\narchive\nx\nY\nN\n");
        assertTrue(output.contains("No tree available."));
        assertTrue(output.contains("Please type y or n"));
        assertTrue(output.contains("Predicted class:" + Double.MAX_VALUE));
        assertTrue(output.indexOf("Starting prediction phase!")
                != output.lastIndexOf("Starting prediction phase!"));
    }

    @Test
    public void loadFlowHandlesSeveralConsecutiveQueries() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            in.readObject();
            send(out, "Table found!", "OK");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "first branch");
            assertEquals(0, in.readObject());
            send(out, "QUERY", "second branch");
            assertEquals(1, in.readObject());
            send(out, "QUERY", "OK", 50.0);
        });

        String output = run(server, "2\ntree\n0\n1\nn\n");
        assertTrue(output.contains("first branch"));
        assertTrue(output.contains("second branch"));
        assertTrue(output.contains("Predicted class:50.0"));
    }

    @Test
    public void invalidBranchIsReportedAndPredictionCanRestart() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            in.readObject();
            send(out, "Table found!", "OK");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "first branch");
            assertEquals(Integer.MIN_VALUE, in.readObject());
            send(out, "The answer should be an integer between 0 and 1!");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "first branch");
            assertEquals(0, in.readObject());
            send(out, "QUERY", "OK", 2.0);
        });
        String output = run(server, "2\ntree\nbad\ny\n0\nn\n");
        assertTrue(output.contains("between 0 and 1"));
        assertTrue(output.contains("Predicted class:2.0"));
    }

    @Test
    public void acquisitionRetriesOnlyTheTableName() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(0, in.readObject());
            assertEquals("missing", in.readObject());
            send(out, "No such table!");
            assertEquals("also-missing", in.readObject());
            send(out, "No such table!");
            assertEquals("table", in.readObject());
            send(out, "Table found!", "OK");
            assertEquals(1, in.readObject());
            send(out, "OK");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "OK", 7.0);
        });
        String output = run(server, "1\nmissing\nalso-missing\ntable\nn\n");
        assertTrue(output.contains("Wrong table. Try again."));
        assertTrue(output.contains("Predicted class:7.0"));
    }

    @Test
    public void loadingRetriesOnlyTheArchiveName() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            assertEquals("missing", in.readObject());
            send(out, "The table missing doesn't exist!");
            assertEquals("archive", in.readObject());
            send(out, "Table found!", "OK");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "OK", 5.5);
        });
        String output = run(server, "2\nmissing\narchive\nn\n");
        assertTrue(output.contains("The table missing doesn't exist!"));
        assertTrue(output.contains("Predicted class:5.5"));
    }

    @Test
    public void learningFailureClosesConnectionBeforePrediction() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(0, in.readObject());
            in.readObject();
            send(out, "Table found!", "OK");
            assertEquals(1, in.readObject());
            send(out, "Error saving tree: denied");
        });
        String output = run(server, "1\ntable\n");
        assertTrue(output.contains("Error saving tree: denied"));
        assertFalse(output.contains("Starting prediction phase!"));
    }

    @Test
    public void unexpectedFinalLoadStatusClosesConnectionBeforePrediction() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            in.readObject();
            send(out, "Table found!", "Cannot load tree");
        });
        String output = run(server, "2\narchive\n");
        assertTrue(output.contains("Cannot load tree"));
        assertFalse(output.contains("Starting prediction phase!"));
    }

    @Test
    public void protocolIoFailureIsReported() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            in.readObject();
        }, false);
        String output = run(server, "2\narchive\n");
        assertTrue(output.contains("EOFException") || output.contains("SocketException"));
    }

    @Test(timeout = 10000)
    public void keyboardEofAtMenuClosesConnectionWithoutLooping() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {});
        assertTrue(run(server, "").contains("Input closed."));
    }

    @Test(timeout = 10000)
    public void keyboardEofDuringNameRetryClosesConnectionWithoutSendingNull() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(0, in.readObject());
            assertEquals("missing", in.readObject());
            send(out, "No such table!");
        });
        run(server, "1\nmissing\n");
    }

    @Test(timeout = 10000)
    public void keyboardEofDuringPredictionClosesConnectionWithoutSendingAnIndex() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            in.readObject();
            send(out, "Table found!", "OK");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "first branch");
        });
        assertTrue(run(server, "2\narchive\n").contains("Input closed."));
    }

    @Test(timeout = 10000)
    public void keyboardEofAtRepeatPromptClosesConnectionWithoutLooping() throws Exception {
        FakeServer server = new FakeServer((in, out) -> {
            assertEquals(2, in.readObject());
            in.readObject();
            send(out, "Table found!", "OK");
            assertEquals(3, in.readObject());
            send(out, "QUERY", "OK", 42.0);
        });
        assertTrue(run(server, "2\narchive\n").contains("Predicted class:42.0"));
    }

    @Test
    public void unknownHostAndRefusedPortsAreReported() throws Exception {
        String unknownHost = TestSupport.captureOutput(
                () -> MainTest.main(new String[] {"no-such-host.invalid", "8080"}));
        assertTrue(unknownHost.contains("UnknownHostException"));

        int closedPort;
        try (ServerSocket reservation = new ServerSocket(0)) {
            closedPort = reservation.getLocalPort();
        }
        String refused = TestSupport.captureOutput(
                () -> MainTest.main(new String[] {"127.0.0.1", Integer.toString(closedPort)}));
        assertTrue(refused.contains("ConnectException") || refused.contains("SocketException"));
    }

    @Test
    public void missingMalformedAndOutOfRangeArgumentsAreReportedWithoutThrowing() throws Exception {
        for (String[] arguments : new String[][] { {}, {"127.0.0.1"} }) {
            String output = TestSupport.captureOutput(() -> MainTest.main(arguments));
            assertTrue(output.contains("Usage:"));
        }
        for (String port : new String[] {"not-a-port", "-1", "0", "65536"}) {
            String output = TestSupport.captureOutput(() -> MainTest.main(new String[] {"127.0.0.1", port}));
            assertTrue(output.contains("Error: invalid server port"));
        }
    }

    private static void send(ObjectOutputStream output, Object... values) throws Exception {
        for (Object value : values) output.writeObject(value);
        output.flush();
    }

    private interface Protocol {
        void run(ObjectInputStream input, ObjectOutputStream output) throws Exception;
    }

    private static final class FakeServer {
        private final ServerSocket listener;
        private final Thread thread;
        private volatile Throwable failure;

        FakeServer(Protocol protocol) throws Exception {
            this(protocol, true);
        }

        FakeServer(Protocol protocol, boolean expectDisconnect) throws Exception {
            listener = new ServerSocket(0);
            thread = new Thread(() -> {
                try (Socket socket = listener.accept()) {
                    socket.setSoTimeout(3000);
                    try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                         ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
                        protocol.run(input, output);
                        if (expectDisconnect) {
                            TestSupport.expect(EOFException.class, input::readObject);
                        }
                    }
                } catch (Throwable thrown) {
                    failure = thrown;
                } finally {
                    try { listener.close(); } catch (Exception ignored) {}
                }
            }, "fake-map-server");
            thread.setDaemon(true);
            thread.start();
        }

        int port() {
            return listener.getLocalPort();
        }

        void await() throws Exception {
            thread.join(5000L);
            if (thread.isAlive()) {
                listener.close();
                throw new AssertionError("Fake server did not terminate");
            }
            if (failure != null) {
                if (failure instanceof Exception) throw (Exception) failure;
                if (failure instanceof Error) throw (Error) failure;
                throw new AssertionError(failure);
            }
        }
    }
}
