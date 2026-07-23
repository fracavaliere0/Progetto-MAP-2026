package estensioni;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Componente Swing che dispone e disegna un {@link VisualTree}. */
final class RegressionTreeCanvas extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color PAPER = RegressionTreeGUI.PAPER;
    private static final Color INK = RegressionTreeGUI.INK;
    private static final Color MUTED = RegressionTreeGUI.MUTED;
    private static final Color RED = RegressionTreeGUI.RED;
    private static final int NODE_WIDTH = 200;
    private static final int NODE_HEIGHT = 72;
    private static final int HORIZONTAL_GAP = 48;
    private static final int LEVEL_GAP = 90;
    private static final int MARGIN = 36;

    private transient VisualTree tree;
    private double scale = 1.0;
    private int baseWidth = 600;
    private int baseHeight = 460;

    /** Costruisce il pannello usato per disegnare l'albero. */
    RegressionTreeCanvas() {
        setBackground(PAPER);
    }

    /**
     * Imposta l'albero da visualizzare e ne ricalcola la disposizione.
     *
     * @param tree albero visuale da mostrare, oppure {@code null} per svuotare il pannello.
     */
    void setTree(VisualTree tree) {
        this.tree = tree;
        layoutTree();
        revalidate();
        repaint();
    }

    /**
     * Imposta il fattore di scala entro l'intervallo supportato.
     *
     * @param scale fattore di scala richiesto.
     */
    void setScale(double scale) {
        this.scale = Math.max(0.5, Math.min(1.5, scale));
        revalidate();
        repaint();
    }

    /**
     * Verifica se è presente un albero da visualizzare.
     *
     * @return {@code true} se il pannello contiene un albero.
     */
    boolean hasTree() {
        return tree != null;
    }

    /**
     * Restituisce il fattore di scala corrente.
     *
     * @return fattore di scala applicato al disegno.
     */
    double getScale() {
        return scale;
    }

    /**
     * Restituisce la coordinata orizzontale del centro della radice.
     *
     * @return coordinata del centro della radice, oppure {@code 0} se l'albero è assente.
     */
    double getRootCenterX() {
        return tree == null ? 0 : tree.getRoot().x + NODE_WIDTH / 2.0;
    }

    /**
     * Calcola la dimensione preferita in base all'albero e allo zoom.
     *
     * @return dimensione necessaria per mostrare il contenuto.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
            (int) Math.ceil(baseWidth * scale),
            (int) Math.ceil(baseHeight * scale)
        );
    }

    /**
     * Disegna lo stato vuoto oppure l'albero corrente.
     *
     * @param graphics contesto grafico fornito da Swing.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        if (tree == null) {
            g.setColor(MUTED);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            String text = "In attesa del training";
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(
                text,
                (getWidth() - metrics.stringWidth(text)) / 2,
                getHeight() / 2
            );
            g.dispose();
            return;
        }

        g.scale(scale, scale);
        g.translate(Math.max(0, (getWidth() / scale - baseWidth) / 2), 0);
        drawEdges(g, tree.getRoot());
        drawNodes(g, tree.getRoot());
        g.dispose();
    }

    /** Calcola dimensioni e coordinate di tutti i nodi dell'albero. */
    private void layoutTree() {
        if (tree == null) {
            baseWidth = 600;
            baseHeight = 460;
            return;
        }

        VisualTree.VisualNode root = tree.getRoot();
        measure(root);
        baseWidth = root.subtreeWidth + MARGIN * 2;
        layout(root, MARGIN, MARGIN);
        int levels = tree.getLevelCount();
        baseHeight = Math.max(
            460,
            MARGIN * 2 + levels * NODE_HEIGHT + (levels - 1) * LEVEL_GAP
        );
    }

    /**
     * Calcola ricorsivamente la larghezza occupata da un sotto-albero.
     *
     * @param node radice del sotto-albero da misurare.
     * @return larghezza necessaria per il sotto-albero.
     */
    private int measure(VisualTree.VisualNode node) {
        if (node.children.isEmpty()) {
            node.subtreeWidth = NODE_WIDTH;
            return node.subtreeWidth;
        }

        int width = 0;
        for (VisualTree.VisualNode child : node.children) {
            width += measure(child);
        }
        width += HORIZONTAL_GAP * (node.children.size() - 1);
        node.subtreeWidth = Math.max(NODE_WIDTH, width);
        return node.subtreeWidth;
    }

    /**
     * Assegna ricorsivamente le coordinate ai nodi di un sotto-albero.
     *
     * @param node radice del sotto-albero da posizionare.
     * @param left coordinata sinistra disponibile.
     * @param top coordinata superiore del livello corrente.
     */
    private void layout(VisualTree.VisualNode node, int left, int top) {
        node.x = left + (node.subtreeWidth - NODE_WIDTH) / 2;
        node.y = top;

        int childLeft = left;
        for (VisualTree.VisualNode child : node.children) {
            layout(child, childLeft, top + NODE_HEIGHT + LEVEL_GAP);
            childLeft += child.subtreeWidth + HORIZONTAL_GAP;
        }
    }

    /**
     * Disegna ricorsivamente i collegamenti tra i nodi.
     *
     * @param g contesto grafico bidimensionale.
     * @param node nodo dal quale iniziare il disegno.
     */
    private void drawEdges(Graphics2D g, VisualTree.VisualNode node) {
        int fromX = node.x + NODE_WIDTH / 2;
        int fromY = node.y + NODE_HEIGHT;

        for (VisualTree.VisualNode child : node.children) {
            int toX = child.x + NODE_WIDTH / 2;
            int toY = child.y;
            int middleY = (fromY + toY) / 2;

            g.setColor(INK);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(fromX, fromY, fromX, middleY);
            g.drawLine(fromX, middleY, toX, middleY);
            g.drawLine(toX, middleY, toX, toY);
            drawBranch(g, child.branch, (fromX + toX) / 2, middleY);
            drawEdges(g, child);
        }
    }

    /**
     * Disegna l'etichetta associata a un ramo.
     *
     * @param g contesto grafico bidimensionale.
     * @param text testo del ramo.
     * @param centerX coordinata orizzontale del centro dell'etichetta.
     * @param centerY coordinata verticale del centro dell'etichetta.
     */
    private void drawBranch(Graphics2D g, String text, int centerX, int centerY) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text) + 14;
        int height = 21;
        int x = centerX - width / 2;
        int y = centerY - height / 2;

        g.setColor(PAPER);
        g.fillRect(x, y, width, height);
        g.setColor(INK);
        g.drawRect(x, y, width, height);
        g.setColor(RED);
        g.drawString(text, x + 7, y + 15);
    }

    /**
     * Disegna ricorsivamente tutti i nodi di un sotto-albero.
     *
     * @param g contesto grafico bidimensionale.
     * @param node radice del sotto-albero da disegnare.
     */
    private void drawNodes(Graphics2D g, VisualTree.VisualNode node) {
        drawNode(g, node);
        for (VisualTree.VisualNode child : node.children) {
            drawNodes(g, child);
        }
    }

    /**
     * Disegna un singolo nodo.
     *
     * @param g contesto grafico bidimensionale.
     * @param node nodo da disegnare.
     */
    private void drawNode(Graphics2D g, VisualTree.VisualNode node) {
        int x = node.x;
        int y = node.y;

        g.setColor(node.leaf ? PAPER : INK);
        g.fillRect(x, y, NODE_WIDTH, NODE_HEIGHT);
        g.setColor(INK);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(x, y, NODE_WIDTH, NODE_HEIGHT);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, node.leaf ? 19 : 17));
        g.setColor(node.leaf ? RED : PAPER);
        drawCentered(g, fit(node.title, g.getFontMetrics(), NODE_WIDTH - 22), x, y + 36);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(node.leaf ? INK : PAPER);
        drawCentered(g, fit(node.meta, g.getFontMetrics(), NODE_WIDTH - 18), x, y + 58);
    }

    /**
     * Disegna una stringa centrata rispetto alla larghezza del nodo.
     *
     * @param g contesto grafico bidimensionale.
     * @param text testo da disegnare.
     * @param x coordinata sinistra del nodo.
     * @param baseline coordinata verticale della linea di base.
     */
    private void drawCentered(Graphics2D g, String text, int x, int baseline) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x + (NODE_WIDTH - textWidth) / 2, baseline);
    }

    /**
     * Accorcia un testo con puntini di sospensione quando supera lo spazio disponibile.
     *
     * @param text testo originale.
     * @param metrics metriche del carattere usato.
     * @param maxWidth larghezza massima disponibile.
     * @return testo originale o versione accorciata.
     */
    private String fit(String text, FontMetrics metrics, int maxWidth) {
        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }

        String suffix = "...";
        int end = text.length();
        while (end > 0 && metrics.stringWidth(text.substring(0, end) + suffix) > maxWidth) {
            end--;
        }
        return text.substring(0, end) + suffix;
    }
}
