package estensioni.gui;

import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Interfaccia grafica di Progetto Map 2026. */
public final class RegressionTreeGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    static final Color PAPER = Color.WHITE;
    static final Color INK = Color.BLACK;
    static final Color MUTED = new Color(0x555555);
    static final Color RED = new Color(0xC00020);
    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    private static final String LOCAL_SOURCE = "locale";
    private static final String DATABASE_SOURCE = "database";

    private final JTextField fileField = new JTextField("distribution/standalone/prova.dat");
    private final JTextField hostField = new JTextField("localhost", 10);
    private final JTextField portField = new JTextField("8080", 5);
    private final JTextField tableField = new JTextField("provaC", 12);
    private final JRadioButton localSourceButton = new JRadioButton("File locale", true);
    private final JRadioButton databaseSourceButton = new JRadioButton("Database");
    private final JPanel sourceCards = new JPanel(new CardLayout());
    private final JTextArea detailsArea = new JTextArea();
    private final JButton detailsButton = new JButton("Dettagli");
    private final JLabel statusLabel = new JLabel("Pronto");
    private final JLabel zoomLabel = new JLabel("100%");
    private final RegressionTreeCanvas treeCanvas = new RegressionTreeCanvas();
    private final JScrollPane graphScroll = new JScrollPane(treeCanvas);

    /** Crea la finestra principale. */
    public RegressionTreeGUI() {
        super("Progetto Map 2026");

        JPanel top = new JPanel(new BorderLayout());
        top.add(createHeader(), BorderLayout.NORTH);
        top.add(createControls(), BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(PAPER);
        content.add(top, BorderLayout.NORTH);
        content.add(createCenter(), BorderLayout.CENTER);
        setContentPane(content);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setSize(1180, 760);
        setLocationRelativeTo(null);
    }

    /**
     * Crea l'intestazione.
     *
     * @return pannello creato
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
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, INK),
            BorderFactory.createEmptyBorder(12, 20, 11, 20)
        ));
        header.add(labels, BorderLayout.WEST);
        return header;
    }

    /**
     * Crea i controlli del training.
     *
     * @return pannello creato
     */
    private JPanel createControls() {
        configureInputField(fileField);
        configureInputField(hostField);
        configureInputField(portField);
        configureInputField(tableField);

        sourceCards.setOpaque(false);
        sourceCards.add(createLocalSourcePanel(), LOCAL_SOURCE);
        sourceCards.add(createDatabaseSourcePanel(), DATABASE_SOURCE);

        styleSourceButton(localSourceButton);
        styleSourceButton(databaseSourceButton);
        localSourceButton.setMnemonic('L');
        databaseSourceButton.setMnemonic('B');

        ButtonGroup sourceGroup = new ButtonGroup();
        sourceGroup.add(localSourceButton);
        sourceGroup.add(databaseSourceButton);
        localSourceButton.addActionListener(event -> showSource(LOCAL_SOURCE));
        databaseSourceButton.addActionListener(event -> showSource(DATABASE_SOURCE));

        JLabel sourceLabel = new JLabel("Origine dati");
        sourceLabel.setFont(LABEL_FONT);
        sourceLabel.setForeground(INK);

        JPanel sourceSelector = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        sourceSelector.setOpaque(false);
        sourceSelector.add(sourceLabel);
        sourceSelector.add(localSourceButton);
        sourceSelector.add(databaseSourceButton);

        JPanel sourcePanel = new JPanel(new BorderLayout(0, 7));
        sourcePanel.setOpaque(false);
        sourcePanel.add(sourceSelector, BorderLayout.NORTH);
        sourcePanel.add(sourceCards, BorderLayout.CENTER);

        JButton trainButton = new JButton("Avvia training");
        styleButton(trainButton, INK, PAPER);
        trainButton.setMnemonic('T');
        trainButton.addActionListener(event -> train());
        getRootPane().setDefaultButton(trainButton);

        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setBackground(PAPER);
        controls.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, INK),
            BorderFactory.createEmptyBorder(9, 20, 10, 20)
        ));
        controls.add(sourcePanel, BorderLayout.CENTER);
        controls.add(trainButton, BorderLayout.EAST);
        return controls;
    }

    /**
     * Crea i controlli locali.
     *
     * @return pannello creato
     */
    private JPanel createLocalSourcePanel() {
        JButton openButton = new JButton("Scegli file");
        styleButton(openButton, PAPER, INK);
        openButton.setMnemonic('S');
        openButton.addActionListener(event -> chooseFile());

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.add(inputLabel("Training set", fileField), BorderLayout.WEST);
        panel.add(fileField, BorderLayout.CENTER);
        panel.add(openButton, BorderLayout.EAST);
        return panel;
    }

    /**
     * Crea i controlli del database.
     *
     * @return pannello creato
     */
    private JPanel createDatabaseSourcePanel() {
        JLabel separator = new JLabel(":");
        separator.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        separator.setForeground(INK);
        portField.setToolTipText("Porta TCP del server MAP");

        JPanel serverPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        serverPanel.setOpaque(false);
        serverPanel.add(inputLabel("Server MAP", hostField));
        serverPanel.add(hostField);
        serverPanel.add(separator);
        serverPanel.add(portField);

        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setToolTipText("Il server MAP deve essere collegato al database");
        panel.add(inputLabel("Tabella", tableField), BorderLayout.WEST);
        panel.add(tableField, BorderLayout.CENTER);
        panel.add(serverPanel, BorderLayout.EAST);
        return panel;
    }

    /**
     * Applica lo stile a un campo.
     *
     * @param field campo da configurare
     */
    private static void configureInputField(JTextField field) {
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        field.setForeground(INK);
        field.setBackground(PAPER);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INK),
            BorderFactory.createEmptyBorder(7, 9, 7, 9)
        ));
    }

    /**
     * Crea l'etichetta di un campo.
     *
     * @param text testo dell'etichetta
     * @param field campo associato
     * @return etichetta creata
     */
    private static JLabel inputLabel(String text, JTextField field) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(INK);
        label.setLabelFor(field);
        return label;
    }

    /**
     * Applica lo stile a un selettore.
     *
     * @param button selettore da configurare
     */
    private static void styleSourceButton(JRadioButton button) {
        button.setOpaque(false);
        button.setForeground(INK);
        button.setFont(LABEL_FONT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Mostra i campi dell'origine.
     *
     * @param source origine selezionata
     */
    private void showSource(String source) {
        ((CardLayout) sourceCards.getLayout()).show(sourceCards, source);
    }

    /**
     * Crea l'area centrale.
     *
     * @return pannello creato
     */
    private JPanel createCenter() {
        graphScroll.setBorder(BorderFactory.createLineBorder(INK));
        graphScroll.getViewport().setBackground(PAPER);

        zoomLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        zoomLabel.setForeground(MUTED);

        JButton zoomOutButton = new JButton("−");
        styleButton(zoomOutButton, PAPER, INK);
        zoomOutButton.setToolTipText("Riduci zoom");
        zoomOutButton.addActionListener(event -> zoomBy(-0.1));

        JButton zoomInButton = new JButton("+");
        styleButton(zoomInButton, PAPER, INK);
        zoomInButton.setToolTipText("Aumenta zoom");
        zoomInButton.addActionListener(event -> zoomBy(0.1));

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

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 7));
        actions.setBackground(PAPER);
        actions.add(detailsButton);
        actions.add(new JLabel("Zoom"));
        actions.add(zoomOutButton);
        actions.add(zoomLabel);
        actions.add(zoomInButton);
        actions.add(centerButton);

        JPanel tools = new JPanel(new BorderLayout());
        tools.setBackground(PAPER);
        tools.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, INK));
        tools.add(statusLabel, BorderLayout.CENTER);
        tools.add(actions, BorderLayout.EAST);

        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.setBackground(PAPER);
        graphPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        graphPanel.add(graphScroll, BorderLayout.CENTER);
        graphPanel.add(tools, BorderLayout.SOUTH);

        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        detailsArea.setForeground(INK);
        detailsArea.setBackground(PAPER);
        detailsArea.setMargin(new Insets(12, 12, 12, 12));
        return graphPanel;
    }

    /**
     * Modifica lo zoom.
     *
     * @param amount variazione richiesta
     */
    private void zoomBy(double amount) {
        treeCanvas.setScale(treeCanvas.getScale() + amount);
        zoomLabel.setText(Math.round(treeCanvas.getScale() * 100) + "%");
    }

    /** Seleziona un training set locale. */
    private void chooseFile() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("Training set (*.dat)", "dat"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /** Mostra i dettagli dell'albero. */
    private void showDetails() {
        JScrollPane scroll = new JScrollPane(detailsArea);
        scroll.setBorder(BorderFactory.createLineBorder(INK));
        scroll.setPreferredSize(new Dimension(640, 420));
        JOptionPane.showMessageDialog(this, scroll, "Dettagli albero", JOptionPane.PLAIN_MESSAGE);
    }

    /** Avvia il training dall'origine selezionata. */
    private void train() {
        if (databaseSourceButton.isSelected()) {
            trainFromDatabase();
        } else {
            trainFromLocalFile();
        }
    }

    /** Esegue il training locale. */
    private void trainFromLocalFile() {
        String fileName = fileField.getText().trim();
        if (fileName.isEmpty()) {
            showError("Seleziona un training set.");
            return;
        }

        beginTraining("Training locale in corso...");
        long start = System.nanoTime();
        try {
            Data data = new Data(fileName);
            RegressionTree tree = new RegressionTree(data);
            VisualTree visualTree = VisualTree.from(tree);
            showTrainingResult(visualTree, tree.toString());

            long elapsed = (System.nanoTime() - start) / 1_000_000;
            statusLabel.setText(
                "<html>" + data.getNumberOfExamples() +
                " esempi &nbsp;•&nbsp; " + data.getNumberOfExplanatoryAttributes() +
                " attributi &nbsp;•&nbsp; " + statisticsForStatus(visualTree) +
                " &nbsp;•&nbsp; " + elapsed + " ms</html>"
            );
            finishTraining();
        } catch (TrainingDataException | RuntimeException exception) {
            failTraining(exception);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /** Esegue il training sul server MAP. */
    private void trainFromDatabase() {
        String host = hostField.getText().trim();
        String tableName = tableField.getText().trim();
        if (host.isEmpty()) {
            showError("Inserisci l'indirizzo del server MAP.");
            return;
        }
        if (!tableName.matches("[A-Za-z][A-Za-z0-9_$]*")) {
            showError("Inserisci un nome di tabella valido.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            showError("La porta deve essere un numero compreso tra 1 e 65535.");
            return;
        }

        beginTraining("Caricamento dal database in corso...");
        long start = System.nanoTime();
        try (DatabaseTreeClient client = new DatabaseTreeClient(host, port)) {
            DatabaseTreeResult result = client.train(tableName);
            VisualTree visualTree = result.tree;
            showTrainingResult(visualTree, result.details);

            long elapsed = (System.nanoTime() - start) / 1_000_000;
            statusLabel.setText(
                "<html>Tabella " + escapeHtml(tableName) +
                " &nbsp;•&nbsp; " + statisticsForStatus(visualTree) +
                " &nbsp;•&nbsp; " + elapsed + " ms<br>Server " +
                escapeHtml(host) + ":" + port + "</html>"
            );
            finishTraining();
        } catch (Exception exception) {
            failTraining(exception);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Imposta lo stato di training.
     *
     * @param message messaggio di stato
     */
    private void beginTraining(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(INK);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Mostra il risultato del training.
     *
     * @param tree albero ottenuto
     * @param details dettagli testuali
     */
    private void showTrainingResult(VisualTree tree, String details) {
        treeCanvas.setTree(tree);
        detailsArea.setText(details);
        detailsArea.setCaretPosition(0);
        detailsButton.setEnabled(true);
    }

    /** Completa il training. */
    private void finishTraining() {
        statusLabel.setForeground(RED);
        SwingUtilities.invokeLater(this::centerTree);
    }

    /**
     * Azzera il risultato e mostra un errore.
     *
     * @param exception errore ricevuto
     */
    private void failTraining(Exception exception) {
        treeCanvas.setTree(null);
        detailsArea.setText("");
        detailsButton.setEnabled(false);
        statusLabel.setText("Training non completato");
        statusLabel.setForeground(RED);
        showError(exception.toString());
    }

    /**
     * Adatta le statistiche alla barra.
     *
     * @param tree albero visualizzato
     * @return statistiche formattate
     */
    private static String statisticsForStatus(VisualTree tree) {
        return tree.getStatistics()
            .replace(" nodi  |  ", " nodi<br>")
            .replace("  |  ", " &nbsp;•&nbsp; ");
    }

    /**
     * Protegge un valore per HTML Swing.
     *
     * @param value valore originale
     * @return valore protetto
     */
    private static String escapeHtml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    /** Centra la radice nell'area visibile. */
    private void centerTree() {
        if (!treeCanvas.hasTree()) {
            return;
        }
        JViewport viewport = graphScroll.getViewport();
        int x = (int) Math.round(treeCanvas.getRootCenterX() * treeCanvas.getScale());
        viewport.setViewPosition(new Point(Math.max(0, x - viewport.getWidth() / 2), 0));
    }

    /**
     * Mostra un errore.
     *
     * @param message messaggio da mostrare
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Applica lo stile a un pulsante.
     *
     * @param button pulsante da configurare
     * @param background colore di sfondo
     * @param foreground colore del testo
     */
    private static void styleButton(JButton button, Color background, Color foreground) {
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(LABEL_FONT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(INK),
            BorderFactory.createEmptyBorder(7, 13, 7, 13)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    /**
     * Avvia la GUI sul thread Swing.
     *
     * @param args argomenti non usati
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
