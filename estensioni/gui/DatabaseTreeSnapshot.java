package estensioni.gui;

/** Rappresenta un nodo remoto ispezionato. */
final class DatabaseTreeSnapshot {

    final String query;
    final double prediction;

    /**
     * Crea un'istantanea remota.
     *
     * @param query query del nodo, oppure {@code null}
     * @param prediction predizione della foglia
     */
    private DatabaseTreeSnapshot(String query, double prediction) {
        this.query = query;
        this.prediction = prediction;
    }

    /**
     * Crea l'istantanea di uno split.
     *
     * @param query query dello split
     * @return istantanea creata
     */
    static DatabaseTreeSnapshot split(String query) {
        return new DatabaseTreeSnapshot(query, Double.NaN);
    }

    /**
     * Crea l'istantanea di una foglia.
     *
     * @param prediction valore predetto
     * @return istantanea creata
     */
    static DatabaseTreeSnapshot leaf(double prediction) {
        return new DatabaseTreeSnapshot(null, prediction);
    }
}
