package estensioni.gui;

/** Contiene il risultato di un training remoto. */
final class DatabaseTreeResult {

    final VisualTree tree;
    final String details;

    /**
     * Crea un risultato.
     *
     * @param tree albero ottenuto
     * @param details dettagli testuali
     */
    DatabaseTreeResult(VisualTree tree, String details) {
        this.tree = tree;
        this.details = details;
    }
}
