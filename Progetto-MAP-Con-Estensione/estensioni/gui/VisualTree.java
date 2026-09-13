package estensioni.gui;

import tree.LeafNode;
import tree.Node;
import tree.RegressionTree;
import tree.SplitNode;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Contiene i dati dell'albero necessari alla GUI. */
final class VisualTree {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat(
        "0.####",
        DecimalFormatSymbols.getInstance(Locale.ROOT)
    );

    private final VisualNode root;

    /**
     * Crea un albero visuale.
     *
     * @param root radice visuale
     */
    VisualTree(VisualNode root) {
        if (root == null) {
            throw new IllegalArgumentException("La radice visuale non può essere null.");
        }
        this.root = root;
    }

    /**
     * Converte un albero di regressione.
     *
     * @param tree albero da convertire
     * @return albero visuale
     * @throws IllegalStateException se l'albero è incompleto
     */
    static VisualTree from(RegressionTree tree) {
        return new VisualTree(createNode(tree, "", new RegressionTreeAccess()));
    }

    /**
     * Restituisce la radice.
     *
     * @return radice visuale
     */
    VisualNode getRoot() {
        return root;
    }

    /**
     * Restituisce le statistiche.
     *
     * @return numero di nodi, foglie e profondità
     */
    String getStatistics() {
        int[] values = statistics();
        return values[0] + " nodi  |  " + values[1] +
            " foglie  |  profondità " + (values[2] - 1);
    }

    /**
     * Conta i livelli.
     *
     * @return livelli, inclusa la radice
     */
    int getLevelCount() {
        return statistics()[2];
    }

    /**
     * Converte un sotto-albero.
     *
     * @param tree sotto-albero
     * @param branch ramo entrante
     * @param access adattatore in sola lettura della base
     * @return nodo visuale
     */
    private static VisualNode createNode(
        RegressionTree tree, String branch, RegressionTreeAccess access
    ) {
        if (tree == null) {
            throw new IllegalStateException("Sotto-albero mancante.");
        }
        Node node = access.rootOf(tree);
        if (node == null) {
            throw new IllegalStateException("Albero privo di radice.");
        }

        String examples =
            "esempi " + node.getBeginExampleIndex() + "-" + node.getEndExampleIndex();
        if (node instanceof LeafNode) {
            return new VisualNode(
                NUMBER_FORMAT.format(((LeafNode) node).getPredictedClassValue()),
                examples,
                branch,
                true
            );
        }

        String query = ((SplitNode) node).formulateQuery().trim();
        String[] conditions = query.isEmpty() ? new String[0] : query.split("\\r?\\n");
        VisualNode visual = new VisualNode(
            conditions.length == 0 ? "Split" : attributeName(conditions[0]),
            node.getNumberOfChildren() + " RAMI  |  " + examples,
            branch,
            false
        );

        RegressionTree[] children = access.childrenOf(tree);
        if (children.length == 0 || children.length != node.getNumberOfChildren()) {
            throw new IllegalStateException("Nodo di split con figli mancanti.");
        }
        for (int i = 0; i < children.length; i++) {
            String childBranch =
                i < conditions.length ? branchName(conditions[i]) : String.valueOf(i);
            visual.children.add(createNode(children[i], childBranch, access));
        }
        return visual;
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
     * Estrae il valore del ramo.
     *
     * @param query condizione
     * @return etichetta del ramo
     */
    private static String branchName(String query) {
        String value = condition(query);
        int comparator = comparatorIndex(value);
        if (comparator < 0) {
            return value;
        }
        String branch = value.substring(comparator).trim();
        return branch.startsWith("=") ? "= " + branch.substring(1).trim() : branch;
    }

    /**
     * Trova il comparatore di uno split discreto o continuo.
     *
     * @param value condizione senza indice
     * @return posizione del comparatore, oppure {@code -1}
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
     * Calcola le statistiche.
     *
     * @return nodi, foglie e livelli
     */
    private int[] statistics() {
        int[] values = new int[3];
        collectStatistics(root, 1, values);
        return values;
    }

    /**
     * Accumula le statistiche.
     *
     * @param node nodo corrente
     * @param level livello corrente
     * @param values valori accumulati
     */
    private static void collectStatistics(VisualNode node, int level, int[] values) {
        values[0]++;
        values[1] += node.children.isEmpty() ? 1 : 0;
        values[2] = Math.max(values[2], level);
        for (VisualNode child : node.children) {
            collectStatistics(child, level + 1, values);
        }
    }
}
