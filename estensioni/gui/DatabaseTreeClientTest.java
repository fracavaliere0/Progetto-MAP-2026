package estensioni.gui;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/** Verifica il client con un server MAP simulato. */
public final class DatabaseTreeClientTest {

    /** Impedisce l'istanziazione. */
    private DatabaseTreeClientTest() {}

    /**
     * Esegue la verifica del protocollo.
     *
     * @param args argomenti non usati
     * @throws Exception se la verifica fallisce
     */
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        Thread server = new Thread(() -> serve(serverSocket), "map-test-server");
        server.start();

        try (DatabaseTreeClient client = new DatabaseTreeClient(
            "localhost",
            serverSocket.getLocalPort()
        )) {
            DatabaseTreeResult result = client.train("provaC");
            VisualTree tree = result.tree;
            assert tree.getStatistics().equals("5 nodi  |  3 foglie  |  profondità 2");
            assert tree.getRoot().title.equals("X");
            assert tree.getRoot().children.get(0).children.get(0).branch.equals("<=2");
            assert result.details.contains("PREDIZIONE 10");
        } finally {
            serverSocket.close();
        }

        server.join(5_000);
        assert !server.isAlive() : "Il server simulato non si è arrestato.";
    }

    /**
     * Serve i comandi della verifica.
     *
     * @param serverSocket socket del server simulato
     */
    private static void serve(ServerSocket serverSocket) {
        try (
            Socket socket = serverSocket.accept();
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream())
        ) {
            while (true) {
                int command = (Integer) input.readObject();
                if (command == 0) {
                    input.readObject();
                    send(output, "OK");
                } else if (command == 1) {
                    send(output, "OK");
                } else if (command == 3) {
                    predict(input, output);
                } else {
                    return;
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Simula un albero con due split.
     *
     * @param input richieste del client
     * @param output risposte del server
     * @throws Exception se il protocollo fallisce
     */
    private static void predict(
        ObjectInputStream input,
        ObjectOutputStream output
    ) throws Exception {
        send(output, "QUERY");
        send(output, "0:X=A\n1:X=B\n");
        int rootChoice = (Integer) input.readObject();

        double prediction = 10.0;
        if (rootChoice == 0) {
            send(output, "QUERY");
            send(output, "0:Y<=2\n1:Y>2\n");
            prediction = (Integer) input.readObject() == 0 ? 1.0 : 2.0;
        }
        send(output, "OK");
        send(output, prediction);
    }

    /**
     * Invia una risposta.
     *
     * @param output stream del server
     * @param value risposta
     * @throws Exception se l'invio fallisce
     */
    private static void send(ObjectOutputStream output, Object value) throws Exception {
        output.writeObject(value);
        output.flush();
    }
}
