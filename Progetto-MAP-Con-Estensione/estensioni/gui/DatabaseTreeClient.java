package estensioni.gui;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Ottiene un albero dal server MAP. */
final class DatabaseTreeClient implements Closeable {

    private static final String OK = "OK";
    private static final String QUERY = "QUERY";
    private static final int LOAD_DATA = 0;
    private static final int LEARN_TREE = 1;
    private static final int PREDICT = 3;
    private static final int TIMEOUT_MILLIS = 30_000;
    private static final int MAX_NODES = 10_000;
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat(
        "0.####",
        DecimalFormatSymbols.getInstance(Locale.ROOT)
    );

    private final Socket socket;
    private final ObjectOutputStream output;
    private final ObjectInputStream input;
    private int visitedNodes;

    /**
     * Apre una sessione MAP.
     *
     * @param host indirizzo del server
     * @param port porta TCP
     * @throws IOException se la connessione fallisce
     */
    DatabaseTreeClient(String host, int port) throws IOException {
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_MILLIS);
            socket.setSoTimeout(TIMEOUT_MILLIS);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException exception) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            throw exception;
        }
    }

    /**
     * Esegue il training remoto.
     *
     * @param tableName tabella del training set
     * @return albero e dettagli testuali
     * @throws IOException per errori di rete o protocollo
     * @throws ClassNotFoundException per risposte sconosciute
     */
    DatabaseTreeResult train(String tableName) throws IOException, ClassNotFoundException {
        write(LOAD_DATA);
        write(tableName);
        String response = read().toString();
        if (!"Table found!".equals(response)) {
            throw new IOException("Errore durante caricamento della tabella: " + response);
        }
        requireOk("caricamento della tabella");
        write(LEARN_TREE);
        requireOk("training");

        visitedNodes = 0;
        VisualNode root = buildNode(new ArrayList<>(), "");
        return new DatabaseTreeResult(new VisualTree(root), createDetails(tableName, root));
    }

    /**
     * Ricostruisce via predizioni; alberi grandi richiedono un export lato server.
     *
     * @param path cammino del nodo
     * @param branch ramo entrante
     * @return nodo ricostruito
     * @throws IOException per errori di rete o protocollo
     * @throws ClassNotFoundException per risposte sconosciute
     */
    private VisualNode buildNode(List<Integer> path, String branch)
        throws IOException, ClassNotFoundException {
        if (++visitedNodes > MAX_NODES) {
            throw new IOException("L'albero remoto supera il limite di " + MAX_NODES + " nodi.");
        }

        DatabaseTreeSnapshot snapshot = inspect(path);
        if (snapshot.query == null) {
            return new VisualNode(
                NUMBER_FORMAT.format(snapshot.prediction),
                "predizione database",
                branch,
                true
            );
        }

        List<String> choices = conditions(snapshot.query);
        if (choices.isEmpty()) {
            throw new IOException("Il server ha restituito un nodo senza condizioni.");
        }

        VisualNode node = new VisualNode(
            attributeName(choices.get(0)),
            choices.size() + " RAMI  |  database",
            branch,
            false
        );
        for (int index = 0; index < choices.size(); index++) {
            List<Integer> childPath = new ArrayList<>(path);
            childPath.add(index);
            node.children.add(buildNode(childPath, branchName(choices.get(index))));
        }
        return node;
    }

    /**
     * Ispeziona un nodo remoto con il protocollo della base definitiva.
     * Ogni domanda e il messaggio finale {@code OK} sono preceduti da {@code QUERY}.
     *
     * @param path cammino del nodo
     * @return istantanea del nodo
     * @throws IOException per errori di rete o protocollo
     * @throws ClassNotFoundException per risposte sconosciute
     */
    private DatabaseTreeSnapshot inspect(List<Integer> path)
        throws IOException, ClassNotFoundException {
        write(PREDICT);

        int depth = 0;
        DatabaseTreeSnapshot target = null;
        while (true) {
            String response = read().toString();
            if (!QUERY.equals(response)) {
                throw new IOException("Errore del server: " + response);
            }
            response = read().toString();
            if (!OK.equals(response)) {
                String query = response;
                int choice;
                if (target != null) {
                    choice = 0;
                } else if (depth == path.size()) {
                    target = DatabaseTreeSnapshot.split(query);
                    choice = 0;
                } else {
                    choice = path.get(depth);
                    if (choice < 0 || choice >= conditions(query).size()) {
                        throw new IOException("Cammino non valido nell'albero remoto.");
                    }
                }
                write(choice);
                depth++;
                continue;
            }

            double prediction = prediction(read());
            if (target == null) {
                if (depth < path.size()) {
                    throw new IOException("Il cammino termina prima del nodo atteso.");
                }
                target = DatabaseTreeSnapshot.leaf(prediction);
            }
            return target;
        }
    }

    /**
     * Converte una predizione.
     *
     * @param value valore ricevuto
     * @return valore numerico
     * @throws IOException se il valore non è numerico
     */
    private static double prediction(Object value) throws IOException {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException exception) {
            throw new IOException("Predizione non numerica ricevuta dal server.", exception);
        }
    }

    /**
     * Richiede una risposta {@code OK}.
     *
     * @param operation operazione corrente
     * @throws IOException se la risposta segnala un errore
     * @throws ClassNotFoundException per risposte sconosciute
     */
    private void requireOk(String operation) throws IOException, ClassNotFoundException {
        String response = read().toString();
        if (!OK.equals(response)) {
            throw new IOException("Errore durante " + operation + ": " + response);
        }
    }

    /**
     * Invia un oggetto.
     *
     * @param value valore da inviare
     * @throws IOException se l'invio fallisce
     */
    private void write(Object value) throws IOException {
        output.writeObject(value);
        output.flush();
    }

    /**
     * Legge una risposta.
     *
     * @return risposta ricevuta
     * @throws IOException se la lettura fallisce
     * @throws ClassNotFoundException per risposte sconosciute
     */
    private Object read() throws IOException, ClassNotFoundException {
        Object value = input.readObject();
        if (value == null) {
            throw new IOException("Il server ha restituito una risposta vuota.");
        }
        return value;
    }

    /**
     * Divide una query nelle condizioni.
     *
     * @param query query del server
     * @return condizioni dei rami
     */
    private static List<String> conditions(String query) {
        List<String> result = new ArrayList<>();
        for (String line : query.split("\\r?\\n")) {
            String value = line.trim();
            if (!value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * Estrae il nome dell'attributo.
     *
     * @param query condizione
     * @return nome dell'attributo
     */
    private static String attributeName(String query) {
        String value = condition(query);
        int comparator = comparatorIndex(value);
        return comparator < 0 ? value : value.substring(0, comparator).trim();
    }

    /**
     * Estrae il testo del ramo.
     *
     * @param query condizione
     * @return testo del ramo
     */
    private static String branchName(String query) {
        String value = condition(query);
        int comparator = comparatorIndex(value);
        return comparator < 0 ? value : value.substring(comparator).trim();
    }

    /**
     * Rimuove l'indice iniziale.
     *
     * @param query condizione indicizzata
     * @return condizione pulita
     */
    private static String condition(String query) {
        int colon = query.indexOf(':');
        return (colon < 0 ? query : query.substring(colon + 1)).trim();
    }

    /**
     * Trova il primo comparatore.
     *
     * @param value condizione
     * @return indice del comparatore, oppure {@code -1}
     */
    private static int comparatorIndex(String value) {
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '=' || character == '<' || character == '>') {
                return index;
            }
        }
        return -1;
    }

    /**
     * Crea i dettagli testuali.
     *
     * @param tableName tabella elaborata
     * @param root radice dell'albero
     * @return dettagli creati
     */
    private static String createDetails(String tableName, VisualNode root) {
        StringBuilder details = new StringBuilder()
            .append("DATABASE TABLE: ").append(tableName).append('\n')
            .append("********* TREE **********\n");
        appendDetails(details, root, "");
        return details.append("*************************\n").toString();
    }

    /**
     * Aggiunge un sotto-albero ai dettagli.
     *
     * @param details testo in costruzione
     * @param node nodo corrente
     * @param indent rientro corrente
     */
    private static void appendDetails(
        StringBuilder details,
        VisualNode node,
        String indent
    ) {
        details.append(indent);
        if (!node.branch.isEmpty()) {
            details.append(node.branch).append(" -> ");
        }
        details.append(node.leaf ? "PREDIZIONE " : "SPLIT ")
            .append(node.title).append('\n');
        for (VisualNode child : node.children) {
            appendDetails(details, child, indent + "  ");
        }
    }

    /** Chiude il socket: la base termina la sessione alla disconnessione. */
    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
