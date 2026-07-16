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
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Interfaccia grafica del Regression Tree Miner. */
public final class RegressionTreeGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color PAGE = new Color(0xF3F6F7);
    private static final Color SURFACE = Color.WHITE;
    private static final Color HEADER = new Color(0x1D3038);
    private static final Color TEXT = new Color(0x1F2930);
    private static final Color MUTED = new Color(0x66747C);
    private static final Color PRIMARY = new Color(0x147D82);
    private static final Color ACCENT = new Color(0xE36F51);
    private static final Color SPLIT = new Color(0x243E49);
    private static final Color LEAF = new Color(0x2E8B57);
    private static final Color LEAF_BACKGROUND = new Color(0xEAF7EF);
    private static final Color LINE = new Color(0xAAB5BA);
    private static final Color CANVAS = new Color(0xF8FAFB);

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat(
        "0.####",
        DecimalFormatSymbols.getInstance(Locale.ROOT)
    );

    private final JTextField fileField = new JTextField("prova.dat");
    private final JTextArea detailsArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Pronto");
    private final TreeCanvas treeCanvas = new TreeCanvas();
    private final JScrollPane graphScroll = new JScrollPane(treeCanvas);

    public RegressionTreeGUI() {
        super("Regression Tree Miner");

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(PAGE);
        content.add(createTop(), BorderLayout.NORTH);
        content.add(createCenter(), BorderLayout.CENTER);
        content.add(createStatusBar(), BorderLayout.SOUTH);

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
        JLabel title = new JLabel("Regression Tree Miner");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Training e visualizzazione del modello");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xC7D4D8));

        JPanel labels = new JPanel(new BorderLayout(0, 3));
        labels.setOpaque(false);
        labels.add(title, BorderLayout.NORTH);
        labels.add(subtitle, BorderLayout.SOUTH);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        header.add(labels, BorderLayout.WEST);
        return header;
    }

    private JPanel createControls() {
        JLabel fileLabel = new JLabel("Training set");
        fileLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        fileLabel.setForeground(TEXT);
        fileLabel.setLabelFor(fileField);

        fileField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        fileField.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCBD4D8)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            )
        );

        JButton openButton = new JButton("Scegli file");
        styleButton(openButton, new Color(0xE7ECEE), TEXT);
        openButton.setMnemonic('S');
        openButton.addActionListener(event -> chooseFile());

        JButton trainButton = new JButton("Avvia training");
        styleButton(trainButton, PRIMARY, Color.WHITE);
        trainButton.setMnemonic('T');
        trainButton.addActionListener(event -> train());
        getRootPane().setDefaultButton(trainButton);

        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setOpaque(false);
        filePanel.add(fileLabel, BorderLayout.WEST);
        filePanel.add(fileField, BorderLayout.CENTER);
        filePanel.add(openButton, BorderLayout.EAST);

        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setBackground(SURFACE);
        controls.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        controls.add(filePanel, BorderLayout.CENTER);
        controls.add(trainButton, BorderLayout.EAST);
        return controls;
    }

    private JPanel createCenter() {
        treeCanvas.setScale(0.8);
        graphScroll.setBorder(BorderFactory.createEmptyBorder());
        graphScroll.getViewport().setBackground(CANVAS);

        JSlider zoomSlider = new JSlider(45, 125, 80);
        zoomSlider.setOpaque(false);
        zoomSlider.setPreferredSize(new Dimension(170, 32));

        JLabel zoomValue = new JLabel("80%");
        zoomValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        zoomValue.setForeground(MUTED);
        zoomSlider.addChangeListener(event -> {
            treeCanvas.setScale(zoomSlider.getValue() / 100.0);
            zoomValue.setText(zoomSlider.getValue() + "%");
        });

        JButton centerButton = new JButton("Centra");
        styleButton(centerButton, new Color(0xE7ECEE), TEXT);
        centerButton.addActionListener(event -> centerTree());

        JPanel graphTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 7));
        graphTools.setBackground(SURFACE);
        graphTools.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDE4E7)));
        graphTools.add(new JLabel("Zoom"));
        graphTools.add(zoomSlider);
        graphTools.add(zoomValue);
        graphTools.add(centerButton);

        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.add(graphScroll, BorderLayout.CENTER);
        graphPanel.add(graphTools, BorderLayout.SOUTH);

        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        detailsArea.setForeground(TEXT);
        detailsArea.setBackground(SURFACE);
        detailsArea.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createEmptyBorder());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        tabs.addTab("Albero grafico", graphPanel);
        tabs.addTab("Dettagli", detailsScroll);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(PAGE);
        center.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
        center.add(tabs, BorderLayout.CENTER);
        return center;
    }

    private JPanel createStatusBar() {
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statusLabel.setForeground(MUTED);

        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(SURFACE);
        status.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDDE4E7)),
                BorderFactory.createEmptyBorder(9, 22, 9, 22)
            )
        );
        status.add(statusLabel, BorderLayout.WEST);
        return status;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("Training set (*.dat)", "dat"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void train() {
        String fileName = fileField.getText().trim();
        if (fileName.isEmpty()) {
            showError("Seleziona un training set.");
            return;
        }

        statusLabel.setText("Training in corso...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        long start = System.nanoTime();

        try {
            Data trainingSet = new Data(fileName);
            RegressionTree tree = new RegressionTree(trainingSet);
            treeCanvas.setTree(createVisualTree(tree));
            detailsArea.setText(tree.toString());
            detailsArea.setCaretPosition(0);

            long elapsed = (System.nanoTime() - start) / 1_000_000;
            statusLabel.setText(
                trainingSet.getNumberOfExamples() +
                " esempi  |  " +
                trainingSet.getNumberOfExplanatoryAttributes() +
                " attributi  |  " +
                elapsed +
                " ms"
            );
            SwingUtilities.invokeLater(() -> centerTree());
        } catch (TrainingDataException | RuntimeException exception) {
            treeCanvas.setTree(null);
            detailsArea.setText("");
            statusLabel.setText("Training non completato");
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
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        button.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
            )
        );
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
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

    /** Avvia l'interfaccia sul thread grafico di Swing. */
    public static void main(String[] args) {
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
        private static final int NODE_WIDTH = 174;
        private static final int NODE_HEIGHT = 68;
        private static final int HORIZONTAL_GAP = 38;
        private static final int LEVEL_GAP = 86;
        private static final int MARGIN = 56;

        private transient VisualNode root;
        private double scale = 1.0;
        private int baseWidth = 1300;
        private int baseHeight = 500;

        private TreeCanvas() {
            setBackground(CANVAS);
        }

        private void setTree(VisualNode root) {
            this.root = root;
            layoutTree();
            revalidate();
            repaint();
        }

        private void setScale(double scale) {
            this.scale = Math.max(0.45, Math.min(1.25, scale));
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
            drawEdges(g, root);
            drawNodes(g, root);
            g.dispose();
        }

        private void layoutTree() {
            if (root == null) {
                baseWidth = 1300;
                baseHeight = 500;
                return;
            }

            measure(root);
            baseWidth = Math.max(1300, root.subtreeWidth + MARGIN * 2);
            layout(root, (baseWidth - root.subtreeWidth) / 2, MARGIN);
            int levels = depth(root);
            baseHeight = Math.max(
                500,
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

        private int depth(VisualNode node) {
            int childDepth = 0;
            for (VisualNode child : node.children) {
                childDepth = Math.max(childDepth, depth(child));
            }
            return childDepth + 1;
        }

        private void drawEdges(Graphics2D g, VisualNode node) {
            int fromX = node.x + NODE_WIDTH / 2;
            int fromY = node.y + NODE_HEIGHT;

            for (VisualNode child : node.children) {
                int toX = child.x + NODE_WIDTH / 2;
                int toY = child.y;
                int middleY = (fromY + toY) / 2;

                Path2D path = new Path2D.Double();
                path.moveTo(fromX, fromY);
                path.curveTo(fromX, middleY, toX, middleY, toX, toY);

                g.setColor(LINE);
                g.setStroke(new BasicStroke(2f));
                g.draw(path);
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

            g.setColor(new Color(255, 255, 255, 235));
            g.fillRoundRect(x, y, width, height, 8, 8);
            g.setColor(new Color(0xD7DEE1));
            g.drawRoundRect(x, y, width, height, 8, 8);
            g.setColor(ACCENT);
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

            g.setColor(new Color(0, 0, 0, 24));
            g.fillRoundRect(x + 3, y + 4, NODE_WIDTH, NODE_HEIGHT, 8, 8);

            g.setColor(node.leaf ? LEAF_BACKGROUND : SPLIT);
            g.fillRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 8, 8);
            g.setColor(node.leaf ? LEAF : PRIMARY);
            g.setStroke(new BasicStroke(1.6f));
            g.drawRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 8, 8);

            if (node.leaf) {
                g.setColor(LEAF);
                g.fillOval(x + 13, y + 12, 8, 8);
            } else {
                g.setColor(ACCENT);
                g.fillRoundRect(x + 13, y + 12, 30, 4, 4, 4);
            }

            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g.setColor(node.leaf ? TEXT : Color.WHITE);
            drawCentered(g, fit(node.title, g.getFontMetrics(), NODE_WIDTH - 22), x, y + 35);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g.setColor(node.leaf ? new Color(0x3B7654) : new Color(0xC6D4D8));
            drawCentered(g, fit(node.meta, g.getFontMetrics(), NODE_WIDTH - 18), x, y + 55);
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
