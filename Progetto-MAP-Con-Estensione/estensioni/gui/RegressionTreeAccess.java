package estensioni.gui;

import java.lang.reflect.Field;
import tree.Node;
import tree.RegressionTree;

/**
 * Legge la struttura dell'albero senza modificare l'API della base definitiva.
 *
 * <p>La base non espone radice e sotto-alberi: l'accesso riflessivo ai campi
 * {@code root} e {@code childTree} rimane confinato a questo adattatore della
 * GUI. I campi vengono soltanto letti e l'array dei figli viene copiato.</p>
 */
final class RegressionTreeAccess {

    private final Field root;
    private final Field children;

    /**
     * Prepara l'accesso ai campi della base.
     *
     * @throws IllegalStateException se la struttura non è accessibile
     */
    RegressionTreeAccess() {
        try {
            root = RegressionTree.class.getDeclaredField("root");
            children = RegressionTree.class.getDeclaredField("childTree");
            root.setAccessible(true);
            children.setAccessible(true);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            throw new IllegalStateException("Impossibile leggere la struttura dell'albero base.", exception);
        }
    }

    /**
     * Legge la radice di un sotto-albero.
     *
     * @param tree sotto-albero da leggere
     * @return radice, oppure {@code null} per un albero vuoto
     */
    Node rootOf(RegressionTree tree) {
        return (Node) read(root, tree);
    }

    /**
     * Legge i figli senza esporre l'array originale.
     *
     * @param tree sotto-albero da leggere
     * @return copia dei figli, oppure un array vuoto
     */
    RegressionTree[] childrenOf(RegressionTree tree) {
        RegressionTree[] value = (RegressionTree[]) read(children, tree);
        return value == null ? new RegressionTree[0] : value.clone();
    }

    /**
     * Legge un campo privato senza modificarlo.
     *
     * @param field campo da leggere
     * @param tree istanza proprietaria
     * @return valore del campo
     * @throws IllegalStateException se l'accesso fallisce
     */
    private Object read(Field field, RegressionTree tree) {
        try {
            return field.get(tree);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Impossibile leggere la struttura dell'albero base.", exception);
        }
    }
}
