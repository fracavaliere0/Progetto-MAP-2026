package estensioni.gui;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/** Verifica la GUI contro ServerOneClient della base con il solo JDBC simulato. */
public final class DatabaseTreeIntegrationTest {

    private DatabaseTreeIntegrationTest() {}

    /**
     * Esegue training, predizioni e gestione degli errori sul server definitivo.
     * Il class loader separato evita conflitti fra i package data/tree dei moduli.
     *
     * @param args directory delle classi server e directory delle fixture JDBC
     * @throws Exception se una verifica fallisce
     */
    public static void main(String[] args) throws Exception {
        Path work = Files.createTempDirectory("map-gui-server-");
        String previousFixtures = System.getProperty("map.fixtures");
        try {
            for (String name : new String[] {"nested", "constant", "savefail"}) {
                Files.copy(Paths.get(args[1], name + ".dat"), work.resolve(name + ".dat"));
            }
            Files.createDirectory(work.resolve("savefail.dmp"));
            System.setProperty("map.fixtures", work.toString());
            try (URLClassLoader serverClasses = new URLClassLoader(
                    new URL[] {Paths.get(args[0]).toUri().toURL()},
                    ClassLoader.getSystemClassLoader().getParent());
                 ServerSocket listener = new ServerSocket(0)) {
                Class<?> workerClass = serverClasses.loadClass("Server.ServerOneClient");
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Thread server = new Thread(() -> {
                    try (Socket socket = listener.accept()) {
                        socket.setSoTimeout(5_000);
                        Thread worker = (Thread) workerClass.getConstructor(Socket.class).newInstance(socket);
                        worker.join(10_000);
                        if (worker.isAlive()) {
                            throw new AssertionError("La sessione base non si è chiusa.");
                        }
                    } catch (Throwable exception) {
                        failure.set(exception);
                    }
                }, "map-base-integration");
                server.start();
                try (DatabaseTreeClient client = new DatabaseTreeClient("localhost", listener.getLocalPort())) {
                    DatabaseTreeResult nested = client.train(work.resolve("nested").toString());
                    assert nested.tree.getStatistics().equals("7 nodi  |  4 foglie  |  profondità 2");
                    assert nested.tree.getRoot().title.equals("first");
                    assert nested.tree.getRoot().children.get(0).branch.equals("=A");
                    assert nested.details.contains("PREDIZIONE 30");
                    assert Files.isRegularFile(work.resolve("nested.dmp"));

                    DatabaseTreeResult leaf = client.train(work.resolve("constant").toString());
                    assert leaf.tree.getRoot().leaf;
                    assert leaf.tree.getRoot().title.equals("5.5");
                    assert leaf.tree.getStatistics().equals("1 nodi  |  1 foglie  |  profondità 0");

                    expectTrainingError(client, work.resolve("savefail").toString(), "Error saving tree:");
                    expectTrainingError(client, work.resolve("missing").toString(), "No such table!");
                } finally {
                    server.join(12_000);
                    if (server.isAlive()) {
                        throw new AssertionError("Il server di test non si è arrestato.");
                    }
                    if (failure.get() != null) {
                        throw new AssertionError("Errore del server base.", failure.get());
                    }
                }
            }
        } finally {
            if (previousFixtures == null) {
                System.clearProperty("map.fixtures");
            } else {
                System.setProperty("map.fixtures", previousFixtures);
            }
            try (Stream<Path> files = Files.walk(work)) {
                for (Path path : (Iterable<Path>) files.sorted(Comparator.reverseOrder())::iterator) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    /**
     * Verifica che gli errori del server vengano riportati senza bloccare il client.
     *
     * @param client sessione da verificare
     * @param table tabella richiesta
     * @param message messaggio atteso
     * @throws Exception se la verifica fallisce
     */
    private static void expectTrainingError(DatabaseTreeClient client, String table, String message)
        throws Exception {
        try {
            client.train(table);
            throw new AssertionError("Era atteso un errore di training.");
        } catch (IOException expected) {
            assert expected.getMessage().contains(message) : expected.getMessage();
        }
    }
}
