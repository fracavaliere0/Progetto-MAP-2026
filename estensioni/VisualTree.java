package estensioni;

import tree.LeafNode;
import tree.Node;
import tree.RegressionTree;
import tree.SplitNode;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Rappresentazione dell'albero contenente i soli dati necessari alla GUI. */
final class VisualTree {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat(
        "0.####",
        DecimalFormatSymbols.getInstance(Locale.ROOT)
    );

    private final VisualNode root;

    /**
     * Costruisce una rappresentazione visuale con la radice indicata.
     *
     * @param root radice dell'albero visuale.
     */
    private VisualTree(VisualNode root) {
        this.root = root;
    }

    /**
     * Crea la rappresentazione visuale di un albero di regressione.
     *
     * @param tree albero di regressione da convertire.
     * @return rappresentazione usata dalla GUI.
     * @throws IllegalStateException se la struttura dell'albero non è completa.
     */
    static VisualTree from(RegressionTree tree) {
        return new VisualTree(createNode(tree, ""));
    }

    /**
     * Restituisce la radice dell'albero visuale.
     *
     * @return nodo visuale radice.
     */
    VisualNode getRoot() {
        return root;
    }

    /**
     * Restituisce le principali statistiche dell'albero.
     *
     * @return numero di nodi, foglie e profondità in forma testuale.
     */
    String getStatistics() {
        return countNodes(root) +
            " nodi  |  " +
            countLeaves(root) +
            " foglie  |  profondità " +
            (getLevelCount() - 1);
    }

    /**
     * Calcola il numero di livelli dell'albero.
     *
     * @return numero di livelli, inclusa la radice.
     */
    int getLevelCount() {
        return depth(root);
    }

    /**
     * Converte ricorsivamente un sotto-albero di regressione.
     *
     * @param tree sotto-albero da convertire.
     * @param branch etichetta del ramo che conduce al nodo.
     * @return nodo visuale corrispondente alla radice del sotto-albero.
     * @throws IllegalStateException se la struttura del sotto-albero non è completa.
     */
    private static VisualNode createNode(RegressionTree tree, String branch) {
        Node node = tree.getRoot();
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

        RegressionTree[] children = tree.getChildren();
        if (children.length == 0) {
            throw new IllegalStateException("Nodo di split privo di figli.");
        }

        for (int i = 0; i < children.length; i++) {
            String childBranch =
                i < conditions.length ? branchName(conditions[i]) : String.valueOf(i);
            visual.children.add(createNode(children[i], childBranch));
        }
        return visual;
    }

    /**
     * Estrae il nome dell'attributo da una condizione.
     *
     * @param query condizione testuale dello split.
     * @return nome dell'attributo.
     */
    private static String attributeName(String query) {
        String condition = condition(query);
        int equals = condition.indexOf('=');
        return equals < 0 ? condition : condition.substring(0, equals).trim();
    }

    /**
     * Estrae il valore mostrato sull'etichetta di un ramo.
     *
     * @param query condizione testuale dello split.
     * @return testo da mostrare sul ramo.
     */
    private static String branchName(String query) {
        String condition = condition(query);
        int equals = condition.indexOf('=');
        return equals < 0
            ? condition
            : "= " + condition.substring(equals + 1).trim();
    }

    /**
     * Rimuove l'indice numerico iniziale da una condizione.
     *
     * @param query condizione testuale completa.
     * @return condizione priva dell'indice iniziale.
     */
    private static String condition(String query) {
        int colon = query.indexOf(':');
        return (colon < 0 ? query : query.substring(colon + 1)).trim();
    }

    /**
     * Conta ricorsivamente i nodi di un sotto-albero.
     *
     * @param node radice del sotto-albero.
     * @return numero totale di nodi.
     */
    private static int countNodes(VisualNode node) {
        int count = 1;
        for (VisualNode child : node.children) {
            count += countNodes(child);
        }
        return count;
    }

    /**
     * Conta ricorsivamente le foglie di un sotto-albero.
     *
     * @param node radice del sotto-albero.
     * @return numero totale di foglie.
     */
    private static int countLeaves(VisualNode node) {
        if (node.children.isEmpty()) {
            return 1;
        }

        int count = 0;
        for (VisualNode child : node.children) {
            count += countLeaves(child);
        }
        return count;
    }

    /**
     * Calcola ricorsivamente il numero di livelli di un sotto-albero.
     *
     * @param node radice del sotto-albero.
     * @return numero di livelli, inclusa la radice.
     */
    private static int depth(VisualNode node) {
        int childDepth = 0;
        for (VisualNode child : node.children) {
            childDepth = Math.max(childDepth, depth(child));
        }
        return childDepth + 1;
    }

    /** Nodo con testo e coordinate necessari al rendering. */
    static final class VisualNode {

        final String title;
        final String meta;
        final String branch;
        final boolean leaf;
        final List<VisualNode> children = new ArrayList<VisualNode>();
        int subtreeWidth;
        int x;
        int y;

        /**
         * Costruisce un nodo contenente i dati necessari al rendering.
         *
         * @param title testo principale del nodo.
         * @param meta informazioni secondarie del nodo.
         * @param branch etichetta del ramo entrante.
         * @param leaf indica se il nodo rappresenta una foglia.
         */
        VisualNode(String title, String meta, String branch, boolean leaf) {
            this.title = title;
            this.meta = meta;
            this.branch = branch;
            this.leaf = leaf;
        }
    }
}
