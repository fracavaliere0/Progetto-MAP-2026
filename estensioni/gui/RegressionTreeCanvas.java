package estensioni.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** Disegna un {@link VisualTree}. */
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

    /** Crea il pannello dell'albero. */
    RegressionTreeCanvas() {
        setBackground(PAPER);
    }

    /**
     * Imposta l'albero.
     *
     * @param tree albero, oppure {@code null}
     */
    void setTree(VisualTree tree) {
        this.tree = tree;
        layoutTree();
        revalidate();
        repaint();
    }

    /**
     * Imposta lo zoom tra 0,5 e 1,5.
     *
     * @param scale zoom richiesto
     */
    void setScale(double scale) {
        this.scale = Double.isNaN(scale) ? 1.0 : Math.max(0.5, Math.min(1.5, scale));
        revalidate();
        repaint();
    }

    /**
     * Verifica la presenza dell'albero.
     *
     * @return {@code true} se presente
     */
    boolean hasTree() {
        return tree != null;
    }

    /**
     * Restituisce lo zoom.
     *
     * @return zoom corrente
     */
    double getScale() {
        return scale;
    }

    /**
     * Restituisce il centro della radice.
     *
     * @return coordinata orizzontale, oppure {@code 0}
     */
    double getRootCenterX() {
        return tree == null ? 0 : tree.getRoot().x + NODE_WIDTH / 2.0;
    }

    /**
     * Calcola la dimensione richiesta.
     *
     * @return dimensione dell'albero
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
            (int) Math.ceil(baseWidth * scale),
            (int) Math.ceil(baseHeight * scale)
        );
    }

    /**
     * Disegna il contenuto.
     *
     * @param graphics contesto grafico
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
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
                return;
            }

            g.scale(scale, scale);
            g.translate(Math.max(0, (getWidth() / scale - baseWidth) / 2), 0);
            g.setStroke(new BasicStroke(2f));
            drawEdges(g, tree.getRoot());
            drawNodes(g, tree.getRoot());
        } finally {
            g.dispose();
        }
    }

    /** Calcola dimensioni e coordinate dell'albero. */
    private void layoutTree() {
        if (tree == null) {
            baseWidth = 600;
            baseHeight = 460;
            return;
        }

        VisualNode root = tree.getRoot();
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
     * Calcola la larghezza di un sotto-albero.
     *
     * @param node radice del sotto-albero
     * @return larghezza richiesta
     */
    private int measure(VisualNode node) {
        if (node.children.isEmpty()) {
            node.subtreeWidth = NODE_WIDTH;
            return NODE_WIDTH;
        }

        int width = HORIZONTAL_GAP * (node.children.size() - 1);
        for (VisualNode child : node.children) {
            width += measure(child);
        }
        node.subtreeWidth = Math.max(NODE_WIDTH, width);
        return node.subtreeWidth;
    }

    /**
     * Posiziona un sotto-albero.
     *
     * @param node radice del sotto-albero
     * @param left margine sinistro
     * @param top margine superiore
     */
    private void layout(VisualNode node, int left, int top) {
        node.x = left + (node.subtreeWidth - NODE_WIDTH) / 2;
        node.y = top;

        int childLeft = left;
        for (VisualNode child : node.children) {
            layout(child, childLeft, top + NODE_HEIGHT + LEVEL_GAP);
            childLeft += child.subtreeWidth + HORIZONTAL_GAP;
        }
    }

    /**
     * Disegna i collegamenti.
     *
     * @param g contesto grafico
     * @param node radice del sotto-albero
     */
    private void drawEdges(Graphics2D g, VisualNode node) {
        int fromX = node.x + NODE_WIDTH / 2;
        int fromY = node.y + NODE_HEIGHT;

        for (VisualNode child : node.children) {
            int toX = child.x + NODE_WIDTH / 2;
            int toY = child.y;
            int middleY = (fromY + toY) / 2;

            g.setColor(INK);
            g.drawLine(fromX, fromY, fromX, middleY);
            g.drawLine(fromX, middleY, toX, middleY);
            g.drawLine(toX, middleY, toX, toY);
            drawBranch(g, child.branch, (fromX + toX) / 2, middleY);
            drawEdges(g, child);
        }
    }

    /**
     * Disegna l'etichetta di un ramo.
     *
     * @param g contesto grafico
     * @param text etichetta
     * @param centerX centro orizzontale
     * @param centerY centro verticale
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
     * Disegna i nodi.
     *
     * @param g contesto grafico
     * @param node radice del sotto-albero
     */
    private void drawNodes(Graphics2D g, VisualNode node) {
        drawNode(g, node);
        for (VisualNode child : node.children) {
            drawNodes(g, child);
        }
    }

    /**
     * Disegna un nodo.
     *
     * @param g contesto grafico
     * @param node nodo da disegnare
     */
    private void drawNode(Graphics2D g, VisualNode node) {
        int x = node.x;
        int y = node.y;

        g.setColor(node.leaf ? PAPER : INK);
        g.fillRect(x, y, NODE_WIDTH, NODE_HEIGHT);
        g.setColor(INK);
        g.drawRect(x, y, NODE_WIDTH, NODE_HEIGHT);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, node.leaf ? 19 : 17));
        g.setColor(node.leaf ? RED : PAPER);
        drawCentered(g, fit(node.title, g.getFontMetrics(), NODE_WIDTH - 22), x, y + 36);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(node.leaf ? INK : PAPER);
        drawCentered(g, fit(node.meta, g.getFontMetrics(), NODE_WIDTH - 18), x, y + 58);
    }

    /**
     * Centra un testo nel nodo.
     *
     * @param g contesto grafico
     * @param text testo
     * @param x coordinata del nodo
     * @param baseline linea di base
     */
    private void drawCentered(Graphics2D g, String text, int x, int baseline) {
        int width = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x + (NODE_WIDTH - width) / 2, baseline);
    }

    /**
     * Accorcia un testo troppo largo.
     *
     * @param text testo
     * @param metrics metriche del font
     * @param maxWidth larghezza massima
     * @return testo adattato
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
