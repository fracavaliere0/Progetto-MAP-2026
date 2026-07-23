package estensioni;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
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

    static final Color PAPER = Color.WHITE;
    static final Color INK = Color.BLACK;
    static final Color MUTED = new Color(0x555555);
    static final Color RED = new Color(0xC00020);
    private static final BasicButtonUI BUTTON_UI = new BasicButtonUI() {

        /**
         * Disegna l'indicatore del pulsante quando viene premuto.
         *
         * @param graphics contesto grafico del pulsante.
         * @param button pulsante premuto.
         */
        @Override
        protected void paintButtonPressed(Graphics graphics, AbstractButton button) {
            paintIndicator(graphics, button, 2);
            paintIndicator(graphics, button, 3);
        }

        /**
         * Disegna l'indicatore di messa a fuoco del pulsante.
         *
         * @param graphics contesto grafico del pulsante.
         * @param button pulsante che ha ricevuto il focus.
         * @param view area disponibile per il contenuto.
         * @param text area occupata dal testo.
         * @param icon area occupata dall'icona.
         */
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

        /**
         * Disegna un bordo interno rosso sul pulsante.
         *
         * @param graphics contesto grafico del pulsante.
         * @param button pulsante sul quale disegnare il bordo.
         * @param inset distanza del bordo dai margini del pulsante.
         */
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

    private final JTextField fileField = new JTextField("prova.dat");
    private final JTextArea detailsArea = new JTextArea();
    private final JButton detailsButton = new JButton("Dettagli");
    private final JLabel statusLabel = new JLabel("Pronto");
    private final RegressionTreeCanvas treeCanvas = new RegressionTreeCanvas();
    private final JScrollPane graphScroll = new JScrollPane(treeCanvas);

    /** Costruisce e configura la finestra principale. */
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

    /**
     * Crea l'area superiore contenente intestazione e controlli.
     *
     * @return pannello superiore configurato.
     */
    private JPanel createTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.add(createHeader(), BorderLayout.NORTH);
        top.add(createControls(), BorderLayout.SOUTH);
        return top;
    }

    /**
     * Crea l'intestazione dell'applicazione.
     *
     * @return pannello di intestazione configurato.
     */
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

    /**
     * Crea i controlli per scegliere il file e avviare il training.
     *
     * @return pannello dei controlli configurato.
     */
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

    /**
     * Crea l'area centrale con albero, stato e controlli di visualizzazione.
     *
     * @return pannello centrale configurato.
     */
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

    /**
     * Modifica lo zoom dell'albero e aggiorna la percentuale mostrata.
     *
     * @param amount variazione da applicare al fattore di scala.
     * @param zoomValue etichetta che mostra la percentuale di zoom.
     */
    private void zoomBy(double amount, JLabel zoomValue) {
        treeCanvas.setScale(treeCanvas.getScale() + amount);
        zoomValue.setText(Math.round(treeCanvas.getScale() * 100) + "%");
    }

    /** Mostra la finestra per selezionare il training set. */
    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("Training set (*.dat)", "dat"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /** Mostra in una finestra separata i dettagli testuali dell'albero. */
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

    /** Carica il training set, costruisce l'albero e aggiorna l'interfaccia. */
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
            VisualTree visualTree = VisualTree.from(tree);
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
                visualTree.getStatistics()
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

    /** Centra la radice dell'albero nell'area visibile. */
    private void centerTree() {
        if (!treeCanvas.hasTree()) {
            return;
        }

        JViewport viewport = graphScroll.getViewport();
        int x = (int) Math.round(treeCanvas.getRootCenterX() * treeCanvas.getScale());
        viewport.setViewPosition(new Point(Math.max(0, x - viewport.getWidth() / 2), 0));
    }

    /**
     * Mostra un messaggio di errore.
     *
     * @param message testo dell'errore da mostrare.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Applica lo stile comune a un pulsante.
     *
     * @param button pulsante da configurare.
     * @param background colore di sfondo.
     * @param foreground colore del testo.
     */
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

    /**
     * Avvia l'interfaccia sul thread grafico di Swing.
     *
     * @param args Argomenti da linea di comando, non usati.
     */
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new RegressionTreeGUI().setVisible(true));
    }
}
