package estensioni;

import data.Data;
import data.TrainingDataException;
import tree.LeafNode;
import tree.Node;
import tree.RegressionTree;
import tree.SplitNode;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;

/** Interfaccia grafica di Progetto Map 2026. */
public final class RegressionTreeGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color PAPER = Color.WHITE;
    private static final Color INK = Color.BLACK;
    private static final Color MUTED = new Color(0x555555);
    private static final Color RED = new Color(0xC00020);
    private static final BasicButtonUI BUTTON_UI = new BasicButtonUI() {

        @Override
        protected void paintButtonPressed(Graphics graphics, AbstractButton button) {
            paintIndicator(graphics, button, 2);
            paintIndicator(graphics, button, 3);
        }

        @Override
        protected void paintFocus(
            Graphics graphics,
            AbstractButton button,
            Rectangle view,
            Rectangle text,
            Rectangle icon
        ) {
            paintIndicator(graphics, button, 4);
        }

        private void paintIndicator(Graphics graphics, AbstractButton button, int inset) {
            graphics.setColor(RED);
            graphics.drawRect(
                inset,
                inset,
                button.getWidth() - inset * 2 - 1,
                button.getHeight() - inset * 2 - 1
            );
        }
    };

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat(
        "0.####",
        DecimalFormatSymbols.getInstance(Locale.ROOT)
    );

    private final JTextField fileField = new JTextField("prova.dat");
    private final JTextArea detailsArea = new JTextArea();
    private final JButton detailsButton = new JButton("Dettagli");
    private final JLabel statusLabel = new JLabel("Pronto");
    private final TreeCanvas treeCanvas = new TreeCanvas();
    private final JScrollPane graphScroll = new JScrollPane(treeCanvas);

    public RegressionTreeGUI() {
        super("Progetto Map 2026");

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(PAPER);
        content.add(createTop(), BorderLayout.NORTH);
        content.add(createCenter(), BorderLayout.CENTER);

        setContentPane(content);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setSize(1180, 760);
        setLocationRelativeTo(null);
    }

    private JPanel createTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.add(createHeader(), BorderLayout.NORTH);
        top.add(createControls(), BorderLayout.SOUTH);
        return top;
    }

    private JPanel createHeader() {
        JLabel title = new JLabel("Progetto Map 2026");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        title.setForeground(INK);

        JLabel subtitle = new JLabel("REGRESSION TREE");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        subtitle.setForeground(RED);

        JPanel labels = new JPanel(new BorderLayout(0, 2));
        labels.setOpaque(false);
        labels.add(title, BorderLayout.NORTH);
        labels.add(subtitle, BorderLayout.SOUTH);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PAPER);
        header.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, INK),
                BorderFactory.createEmptyBorder(12, 20, 11, 20)
            )
        );
        header.add(labels, BorderLayout.WEST);
        return header;
    }

    private JPanel createControls() {
        JLabel fileLabel = new JLabel("Training set");
        fileLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        fileLabel.setForeground(INK);
        fileLabel.setLabelFor(fileField);

        fileField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        fileField.setForeground(INK);
        fileField.setBackground(PAPER);
        fileField.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INK),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)
            )
        );

        JButton openButton = new JButton("Scegli file");
        styleButton(openButton, PAPER, INK);
        openButton.setMnemonic('S');
        openButton.addActionListener(event -> chooseFile());

        JButton trainButton = new JButton("Avvia training");
        styleButton(trainButton, INK, PAPER);
        trainButton.setMnemonic('T');
        trainButton.addActionListener(event -> train());
        getRootPane().setDefaultButton(trainButton);

        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setOpaque(false);
        filePanel.add(fileLabel, BorderLayout.WEST);
        filePanel.add(fileField, BorderLayout.CENTER);
        filePanel.add(openButton, BorderLayout.EAST);

        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setBackground(PAPER);
        controls.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, INK),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
            )
        );
        controls.add(filePanel, BorderLayout.CENTER);
        controls.add(trainButton, BorderLayout.EAST);
        return controls;
    }

    private JPanel createCenter() {
        treeCanvas.setScale(1.0);
        graphScroll.setBorder(BorderFactory.createLineBorder(INK));
        graphScroll.getViewport().setBackground(PAPER);

        JLabel zoomValue = new JLabel("100%");
        zoomValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        zoomValue.setForeground(MUTED);

        JButton zoomOutButton = new JButton("−");
        styleButton(zoomOutButton, PAPER, INK);
        zoomOutButton.setToolTipText("Riduci zoom");
        zoomOutButton.addActionListener(event -> zoomBy(-0.1, zoomValue));

        JButton zoomInButton = new JButton("+");
        styleButton(zoomInButton, PAPER, INK);
        zoomInButton.setToolTipText("Aumenta zoom");
        zoomInButton.addActionListener(event -> zoomBy(0.1, zoomValue));

        JButton centerButton = new JButton("Centra");
        styleButton(centerButton, PAPER, INK);
        centerButton.addActionListener(event -> centerTree());

        styleButton(detailsButton, PAPER, INK);
        detailsButton.setMnemonic('D');
        detailsButton.setEnabled(false);
        detailsButton.addActionListener(event -> showDetails());

        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statusLabel.setForeground(MUTED);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));

        JPanel graphActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 7));
        graphActions.setBackground(PAPER);
        graphActions.add(detailsButton);
        graphActions.add(new JLabel("Zoom"));
        graphActions.add(zoomOutButton);
        graphActions.add(zoomValue);
        graphActions.add(zoomInButton);
        graphActions.add(centerButton);

        JPanel graphTools = new JPanel(new BorderLayout());
        graphTools.setBackground(PAPER);
        graphTools.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, INK));
        graphTools.add(statusLabel, BorderLayout.CENTER);
        graphTools.add(graphActions, BorderLayout.EAST);

        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBackground(PAPER);
        graphPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        graphPanel.add(graphScroll, BorderLayout.CENTER);
        graphPanel.add(graphTools, BorderLayout.SOUTH);

        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        detailsArea.setForeground(INK);
        detailsArea.setBackground(PAPER);
        detailsArea.setMargin(new Insets(12, 12, 12, 12));

        return graphPanel;
    }

    private void zoomBy(double amount, JLabel zoomValue) {
        treeCanvas.setScale(treeCanvas.getScale() + amount);
        zoomValue.setText(Math.round(treeCanvas.getScale() * 100) + "%");
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("Training set (*.dat)", "dat"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void showDetails() {
        JScrollPane scroll = new JScrollPane(detailsArea);
        scroll.setBorder(BorderFactory.createLineBorder(INK));
        scroll.setPreferredSize(new Dimension(640, 420));
        JOptionPane.showMessageDialog(
            this,
            scroll,
            "Dettagli albero",
            JOptionPane.PLAIN_MESSAGE
        );
    }

    private void train() {
        String fileName = fileField.getText().trim();
        if (fileName.isEmpty()) {
            showError("Seleziona un training set.");
            return;
        }

        statusLabel.setText("Training in corso...");
        statusLabel.setForeground(INK);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        long start = System.nanoTime();

        try {
            Data trainingSet = new Data(fileName);
            RegressionTree tree = new RegressionTree(trainingSet);
            VisualNode visualTree = createVisualTree(tree);
            treeCanvas.setTree(visualTree);
            detailsArea.setText(tree.toString());
            detailsArea.setCaretPosition(0);
            detailsButton.setEnabled(true);

            long elapsed = (System.nanoTime() - start) / 1_000_000;
            statusLabel.setText(
                "<html>" +
                trainingSet.getNumberOfExamples() +
                " esempi &nbsp;•&nbsp; " +
                trainingSet.getNumberOfExplanatoryAttributes() +
                " attributi &nbsp;•&nbsp; " +
                treeStatistics(visualTree)
                    .replace(" nodi  |  ", " nodi<br>")
                    .replace("  |  ", " &nbsp;•&nbsp; ") +
                " &nbsp;•&nbsp; " +
                elapsed +
                " ms</html>"
            );
            statusLabel.setForeground(RED);
            SwingUtilities.invokeLater(() -> centerTree());
        } catch (TrainingDataException | RuntimeException exception) {
            treeCanvas.setTree(null);
            detailsArea.setText("");
            detailsButton.setEnabled(false);
            statusLabel.setText("Training non completato");
            statusLabel.setForeground(RED);
            showError(exception.toString());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void centerTree() {
        if (!treeCanvas.hasTree()) {
            return;
        }

        JViewport viewport = graphScroll.getViewport();
        int x = (int) Math.round(treeCanvas.getRootCenterX() * treeCanvas.getScale());
        viewport.setViewPosition(new Point(Math.max(0, x - viewport.getWidth() / 2), 0));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private static void styleButton(JButton button, Color background, Color foreground) {
        button.setUI(BUTTON_UI);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INK),
                BorderFactory.createEmptyBorder(7, 13, 7, 13)
            )
        );
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    private static VisualNode createVisualTree(RegressionTree tree) {
        try {
            Field rootField = RegressionTree.class.getDeclaredField("root");
            Field childrenField = RegressionTree.class.getDeclaredField("childTree");
            rootField.setAccessible(true);
            childrenField.setAccessible(true);
            return createVisualTree(tree, "", rootField, childrenField);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Impossibile leggere la struttura dell'albero.", exception);
        }
    }

    private static VisualNode createVisualTree(
        RegressionTree tree,
        String branch,
        Field rootField,
        Field childrenField
    ) throws IllegalAccessException {
        Node node = (Node) rootField.get(tree);
        if (node == null) {
            throw new IllegalStateException("Albero privo di radice.");
        }

        String examples =
            "esempi " + node.getBeginExampleIndex() + "-" + node.getEndExampleIndex();

        if (node instanceof LeafNode) {
            return new VisualNode(
                NUMBER_FORMAT.format(((LeafNode) node).getPredictedClassValue()),
                "PREDIZIONE  |  " + examples,
                branch,
                true
            );
        }

        String query = ((SplitNode) node).formulateQuery().trim();
        String[] conditions = query.isEmpty() ? new String[0] : query.split("\\r?\\n");
        String title = conditions.length == 0 ? "Split" : attributeName(conditions[0]);
        VisualNode visual = new VisualNode(
            title,
            node.getNumberOfChildren() + " RAMI  |  " + examples,
            branch,
            false
        );

        RegressionTree[] children = (RegressionTree[]) childrenField.get(tree);
        if (children == null) {
            throw new IllegalStateException("Nodo di split privo di figli.");
        }

        for (int i = 0; i < children.length; i++) {
            String childBranch = i < conditions.length ? branchName(conditions[i]) : String.valueOf(i);
            visual.children.add(
                createVisualTree(children[i], childBranch, rootField, childrenField)
            );
        }
        return visual;
    }

    private static String attributeName(String query) {
        String condition = condition(query);
        int equals = condition.indexOf('=');
        return equals < 0 ? condition : condition.substring(0, equals).trim();
    }

    private static String branchName(String query) {
        String condition = condition(query);
        int equals = condition.indexOf('=');
        return equals < 0
            ? condition
            : "= " + condition.substring(equals + 1).trim();
    }

    private static String condition(String query) {
        int colon = query.indexOf(':');
        return (colon < 0 ? query : query.substring(colon + 1)).trim();
    }

    private static String treeStatistics(VisualNode root) {
        return countNodes(root) +
            " nodi  |  " +
            countLeaves(root) +
            " foglie  |  profondità " +
            (depth(root) - 1);
    }

    static String treeStatistics(RegressionTree tree) {
        return treeStatistics(createVisualTree(tree));
    }

    private static int countNodes(VisualNode node) {
        int count = 1;
        for (VisualNode child : node.children) {
            count += countNodes(child);
        }
        return count;
    }

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

    private static int depth(VisualNode node) {
        int childDepth = 0;
        for (VisualNode child : node.children) {
            childDepth = Math.max(childDepth, depth(child));
        }
        return childDepth + 1;
    }

    /** Avvia l'interfaccia sul thread grafico di Swing. */
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new RegressionTreeGUI().setVisible(true));
    }

    private static final class VisualNode {

        private final String title;
        private final String meta;
        private final String branch;
        private final boolean leaf;
        private final List<VisualNode> children = new ArrayList<VisualNode>();
        private int subtreeWidth;
        private int x;
        private int y;

        private VisualNode(String title, String meta, String branch, boolean leaf) {
            this.title = title;
            this.meta = meta;
            this.branch = branch;
            this.leaf = leaf;
        }
    }

    private static final class TreeCanvas extends JPanel {

        private static final long serialVersionUID = 1L;
        private static final int NODE_WIDTH = 200;
        private static final int NODE_HEIGHT = 72;
        private static final int HORIZONTAL_GAP = 48;
        private static final int LEVEL_GAP = 90;
        private static final int MARGIN = 36;

        private transient VisualNode root;
        private double scale = 1.0;
        private int baseWidth = 600;
        private int baseHeight = 460;

        private TreeCanvas() {
            setBackground(PAPER);
        }

        private void setTree(VisualNode root) {
            this.root = root;
            layoutTree();
            revalidate();
            repaint();
        }

        private void setScale(double scale) {
            this.scale = Math.max(0.5, Math.min(1.5, scale));
            revalidate();
            repaint();
        }

        private boolean hasTree() {
            return root != null;
        }

        private double getScale() {
            return scale;
        }

        private double getRootCenterX() {
            return root == null ? 0 : root.x + NODE_WIDTH / 2.0;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(
                (int) Math.ceil(baseWidth * scale),
                (int) Math.ceil(baseHeight * scale)
            );
        }

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

            if (root == null) {
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
            drawEdges(g, root);
            drawNodes(g, root);
            g.dispose();
        }

        private void layoutTree() {
            if (root == null) {
                baseWidth = 600;
                baseHeight = 460;
                return;
            }

            measure(root);
            baseWidth = root.subtreeWidth + MARGIN * 2;
            layout(root, MARGIN, MARGIN);
            int levels = depth(root);
            baseHeight = Math.max(
                460,
                MARGIN * 2 + levels * NODE_HEIGHT + (levels - 1) * LEVEL_GAP
            );
        }

        private int measure(VisualNode node) {
            if (node.children.isEmpty()) {
                node.subtreeWidth = NODE_WIDTH;
                return node.subtreeWidth;
            }

            int width = 0;
            for (VisualNode child : node.children) {
                width += measure(child);
            }
            width += HORIZONTAL_GAP * (node.children.size() - 1);
            node.subtreeWidth = Math.max(NODE_WIDTH, width);
            return node.subtreeWidth;
        }

        private void layout(VisualNode node, int left, int top) {
            node.x = left + (node.subtreeWidth - NODE_WIDTH) / 2;
            node.y = top;

            int childLeft = left;
            for (VisualNode child : node.children) {
                layout(child, childLeft, top + NODE_HEIGHT + LEVEL_GAP);
                childLeft += child.subtreeWidth + HORIZONTAL_GAP;
            }
        }

        private void drawEdges(Graphics2D g, VisualNode node) {
            int fromX = node.x + NODE_WIDTH / 2;
            int fromY = node.y + NODE_HEIGHT;

            for (VisualNode child : node.children) {
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

        private void drawNodes(Graphics2D g, VisualNode node) {
            drawNode(g, node);
            for (VisualNode child : node.children) {
                drawNodes(g, child);
            }
        }

        private void drawNode(Graphics2D g, VisualNode node) {
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

        private void drawCentered(Graphics2D g, String text, int x, int baseline) {
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, x + (NODE_WIDTH - textWidth) / 2, baseline);
        }

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
}
