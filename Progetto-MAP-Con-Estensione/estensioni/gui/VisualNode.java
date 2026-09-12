package estensioni.gui;

import java.util.ArrayList;
import java.util.List;

/** Contiene i dati necessari al rendering di un nodo. */
final class VisualNode {

    final String title;
    final String meta;
    final String branch;
    final boolean leaf;
    final List<VisualNode> children = new ArrayList<>();
    int subtreeWidth;
    int x;
    int y;

    /**
     * Crea un nodo visuale.
     *
     * @param title testo principale
     * @param meta testo secondario
     * @param branch ramo entrante
     * @param leaf {@code true} per una foglia
     */
    VisualNode(String title, String meta, String branch, boolean leaf) {
        this.title = title;
        this.meta = meta;
        this.branch = branch;
        this.leaf = leaf;
    }
}
