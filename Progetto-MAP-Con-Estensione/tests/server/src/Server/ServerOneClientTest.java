package Server;

import data.Data;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testsupport.TestSupport;
import tree.RegressionTree;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerOneClientTest {
    private final List<Session> sessions = new ArrayList<Session>();
    private Path directory;

    @Before
    public void configureDatabase() throws Exception {
        TestSupport.configureFixtures();
        directory = Files.createTempDirectory("map-protocol-");
        System.setProperty("map.fixtures", directory.toString());
        for (String name : new String[] {"protocol", "savefail", "constant", "nested", "continuous"}) {
            Files.copy(Paths.get("fixtures", name + ".dat"), directory.resolve(name + ".dat"));
        }
        Files.createDirectory(directory.resolve("savefail.dmp"));
    }

    @After
    public void cleanUp() throws Exception {
        for (Session session : sessions) session.close();
        sessions.clear();
        try (java.util.stream.Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(java.util.Comparator.reverseOrder())::iterator) {
                Files.deleteIfExists(path);
            }
        }
        TestSupport.configureFixtures();
    }

    private String name(String stem) {
        return directory.resolve(stem).toString();
    }

    private Session openSession() throws Exception {
        ServerSocket listener = new ServerSocket(0);
        Socket clientSocket = new Socket("127.0.0.1", listener.getLocalPort());
        clientSocket.setSoTimeout(3000);
        Socket serverSocket = listener.accept();
        listener.close();

        ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
        ServerOneClient worker = new ServerOneClient(serverSocket);
        ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
        Session session = new Session(clientSocket, output, input, worker, serverSocket);
        sessions.add(session);
        return session;
    }

    private String archive(String stem, String table) throws Exception {
        String name = name(stem);
        new RegressionTree(new Data(table)).salva(name + ".dmp");
        return name;
    }

    @Test
    public void missingTrainingDataAndTreeUseReferenceMessages() throws Exception {
        Session session = openSession();
        session.send(1);
        assertEquals("No training data loaded.", session.read());
        session.send(3);
        assertEquals("No tree available.", session.read());
    }

    @Test
    public void loadDataRetriesNamesWithoutResendingActionAndThenLearns() throws Exception {
        Session session = openSession();
        session.send(0, "does-not-exist");
        assertEquals("No such table!", session.read());
        session.send("another-missing-table");
        assertEquals("No such table!", session.read());
        session.send(name("protocol"));
        session.found();
        session.send(1);
        assertEquals("OK", session.read());
        assertTrue(Files.isRegularFile(directory.resolve("protocol.dmp")));
        assertFalse(Files.exists(directory.resolve("protocol")));
    }

    @Test
    public void databaseFailureCanBeRetriedInTheSameSession() throws Exception {
        Session session = openSession();
        System.setProperty("map.offline", "true");
        session.send(0, name("protocol"));
        assertEquals("No such table!", session.read());
        TestSupport.clearJdbcFailures();
        session.send(name("protocol"));
        session.found();
    }

    @Test
    public void learningReportsArchiveWriteFailureAndCanRetry() throws Exception {
        Session session = openSession();
        session.send(0, name("savefail"));
        session.found();
        session.send(1);
        String error = session.read().toString();
        assertTrue(error.startsWith("Error saving tree: "));
        assertTrue(error.contains("FileNotFoundException"));
        Files.delete(directory.resolve("savefail.dmp"));
        session.send(1);
        assertEquals("OK", session.read());
    }

    @Test
    public void runtimeTrainingFailureIsReportedAndSessionRemainsUsable() throws Exception {
        Session session = openSession();
        session.send(0, name("continuous"));
        session.found();
        session.send(1);
        assertTrue(session.read().toString().startsWith("Error building tree: "));
        assertTrue(session.worker.isAlive());
        session.send(0, name("constant"));
        session.found();
        session.send(1);
        assertEquals("OK", session.read());
        session.send(3);
        session.prediction(5.5);
    }

    @Test
    public void loadTreeRetriesMissingCorruptWrongTypeAndValidArchives() throws Exception {
        Session session = openSession();
        String missing = name("missing");
        session.send(2, missing);
        assertEquals("The table " + missing + " doesn't exist!", session.read());

        Files.write(directory.resolve("corrupt.dmp"), new byte[] {1, 2, 3});
        session.send(name("corrupt"));
        assertEquals("The table " + name("corrupt") + " doesn't exist!", session.read());

        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(directory.resolve("wrong.dmp")))) {
            output.writeObject("not a tree");
        }
        session.send(name("wrong"));
        assertEquals("The table " + name("wrong") + " doesn't exist!", session.read());

        new RegressionTree().salva(name("empty") + ".dmp");
        session.send(name("empty"));
        assertEquals("The table " + name("empty") + " doesn't exist!", session.read());

        session.send(archive("valid", "constant"));
        session.found();
        session.send(3);
        session.prediction(5.5);
    }

    @Test
    public void predictsMinimumAndMaximumRecursivePathsWithFinalQueryMarker() throws Exception {
        Session session = openSession();
        session.send(2, archive("nested-tree", "nested"));
        session.found();

        for (int path : new int[] {0, 1}) {
            session.send(3);
            session.query("0:first=A\n1:first=B\n");
            session.send(path);
            session.query("0:second=u\n1:second=v\n");
            session.send(path);
            session.prediction(path == 0 ? 0.0 : 30.0);
        }
    }

    @Test
    public void invalidPredictionAnswersHaveNoExceptionPrefixAndAllowRetry() throws Exception {
        Session session = openSession();
        session.send(2, archive("nested-tree", "nested"));
        session.found();
        for (Object answer : new Object[] {-2, -1, 2, Integer.MAX_VALUE, "bad", null}) {
            session.send(3);
            session.query("0:first=A\n1:first=B\n");
            session.send(answer);
            assertEquals("The answer should be an integer between 0 and 1!", session.read());
        }
        session.send(3);
        session.query("0:first=A\n1:first=B\n");
        session.send(0);
        session.query("0:second=u\n1:second=v\n");
        session.send(1);
        session.prediction(10.0);
    }

    @Test
    public void loadingAnotherArchiveDoesNotChangeTrainingSaveName() throws Exception {
        Session session = openSession();
        session.send(0, name("constant"));
        session.found();
        String other = archive("other", "nested");
        byte[] previous = Files.readAllBytes(Paths.get(other + ".dmp"));
        session.send(2, other);
        session.found();
        session.send(1);
        assertEquals("OK", session.read());
        org.junit.Assert.assertArrayEquals(previous, Files.readAllBytes(Paths.get(other + ".dmp")));
        assertTrue(Files.isRegularFile(directory.resolve("constant.dmp")));
    }

    @Test
    public void independentClientsDoNotShareTrainingDataOrTrees() throws Exception {
        Session first = openSession();
        Session second = openSession();
        first.send(0, name("constant"));
        first.found();
        first.send(1);
        assertEquals("OK", first.read());
        second.send(1);
        assertEquals("No training data loaded.", second.read());
        second.send(3);
        assertEquals("No tree available.", second.read());
        first.send(3);
        first.prediction(5.5);
    }

    @Test
    public void unknownActionIsIgnoredLikeReferenceAndDoesNotCloseSession() throws Exception {
        Session session = openSession();
        session.send(99, 3);
        assertEquals("No tree available.", session.read());
        assertTrue(session.worker.isAlive());
    }

    @Test
    public void malformedActionOrNameClosesOnlyItsSession() throws Exception {
        for (Object[] requests : new Object[][] {{"not an integer"}, {null}, {0, 12}, {2, null}}) {
            Session malformed = openSession();
            malformed.send(requests);
            TestSupport.expect(EOFException.class, malformed::read);
            malformed.worker.join(3000);
            assertFalse(malformed.worker.isAlive());
            assertTrue(malformed.serverSocket.isClosed());
        }
    }

    @Test
    public void disconnectsWhileWaitingForActionOrRetryReleaseResources() throws Exception {
        Session idle = openSession();
        Session loading = openSession();
        loading.send(0, "missing");
        assertEquals("No such table!", loading.read());
        Session archive = openSession();
        archive.send(2, name("missing"));
        archive.read();
        Session predicting = openSession();
        predicting.send(2, archive("nested-tree", "nested"));
        predicting.found();
        predicting.send(3);
        predicting.query("0:first=A\n1:first=B\n");
        for (Session session : new Session[] {idle, loading, archive, predicting}) {
            session.close();
            assertFalse(session.worker.isAlive());
            assertTrue(session.serverSocket.isClosed());
        }
    }

    @Test
    public void unknownValueExceptionPreservesMessage() {
        UnknownValueException exception = new UnknownValueException("invalid path");
        assertEquals("invalid path", exception.getMessage());
    }

    private static final class Session {
        final Socket socket;
        final ObjectOutputStream output;
        final ObjectInputStream input;
        final ServerOneClient worker;
        final Socket serverSocket;
        boolean closed;

        Session(Socket socket, ObjectOutputStream output, ObjectInputStream input,
                ServerOneClient worker, Socket serverSocket) {
            this.socket = socket;
            this.output = output;
            this.input = input;
            this.worker = worker;
            this.serverSocket = serverSocket;
        }

        void send(Object... values) throws Exception {
            for (Object value : values) output.writeObject(value);
            output.flush();
        }

        Object read() throws Exception {
            return input.readObject();
        }

        void found() throws Exception {
            assertEquals("Table found!", read());
            assertEquals("OK", read());
        }

        void query(String text) throws Exception {
            assertEquals("QUERY", read());
            assertEquals(text, read());
        }

        void prediction(double value) throws Exception {
            assertEquals("QUERY", read());
            assertEquals("OK", read());
            assertEquals(value, (Double) read(), 0.0);
        }

        void close() throws Exception {
            if (closed) return;
            closed = true;
            socket.close();
            worker.join(3000);
        }
    }
}
