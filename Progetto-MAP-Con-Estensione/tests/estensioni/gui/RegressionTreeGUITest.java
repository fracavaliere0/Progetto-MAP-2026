package estensioni.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import support.TestData;

/** Verifica i flussi e i valori limite dell'interfaccia Swing. */
public class RegressionTreeGUITest {

    private RegressionTreeGUI gui;

    /** Crea il test della GUI. */
    public RegressionTreeGUITest() {}

    /**
     * Crea la finestra da verificare.
     *
     * @throws Exception se Swing non risponde
     */
    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        gui = onEdt(RegressionTreeGUI::new);
    }

    /**
     * Chiude le finestre aperte.
     *
     * @throws Exception se Swing non risponde
     */
    @After
    public void tearDown() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        onEdt(() -> {
            for (Window window : Window.getWindows()) {
                window.dispose();
            }
            return null;
        });
    }

    /**
     * Verifica lo stato iniziale.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void constructorCreatesTheInitialState() throws Exception {
        JTextField file = field("fileField", JTextField.class);
        JButton details = field("detailsButton", JButton.class);
        JLabel status = field("statusLabel", JLabel.class);
        RegressionTreeCanvas canvas = field(
            "treeCanvas",
            RegressionTreeCanvas.class
        );

        assertEquals("prova.dat", file.getText());
        assertFalse(details.isEnabled());
        assertEquals("Pronto", status.getText());
        assertFalse(canvas.hasTree());
        assertEquals(1.0, canvas.getScale(), 0.0);
    }

    /**
     * Verifica i limiti dello zoom.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void zoomChecksLowerMiddleAndUpperLimits() throws Exception {
        final JLabel value = field("zoomLabel", JLabel.class);
        final RegressionTreeCanvas canvas = field(
            "treeCanvas",
            RegressionTreeCanvas.class
        );

        onEdt(() -> {
            invoke("zoomBy", new Class<?>[] {double.class}, -10.0);
            assertEquals(0.5, canvas.getScale(), 0.0);
            assertEquals("50%", value.getText());

            invoke("zoomBy", new Class<?>[] {double.class}, 0.5);
            assertEquals(1.0, canvas.getScale(), 0.0);
            assertEquals("100%", value.getText());

            invoke("zoomBy", new Class<?>[] {double.class}, 10.0);
            assertEquals(1.5, canvas.getScale(), 0.0);
            assertEquals("150%", value.getText());

            button("−").doClick();
            assertEquals(1.4, canvas.getScale(), 0.000001);
            button("+").doClick();
            assertEquals(1.5, canvas.getScale(), 0.000001);
            return null;
        });
    }

    /**
     * Verifica un training locale valido.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void trainLoadsAValidTrainingSetAndShowsDetails() throws Exception {
        final JTextField file = field("fileField", JTextField.class);
        final JButton details = field("detailsButton", JButton.class);
        final JLabel status = field("statusLabel", JLabel.class);
        final JTextArea area = field("detailsArea", JTextArea.class);
        final RegressionTreeCanvas canvas = field(
            "treeCanvas",
            RegressionTreeCanvas.class
        );
        file.setText(TestData.createTreeData());

        onEdt(() -> {
            button("Avvia training").doClick();
            button("Centra").doClick();
            return null;
        });

        assertTrue(canvas.hasTree());
        assertTrue(details.isEnabled());
        assertTrue(area.getText().contains("DISCRETE SPLIT"));
        assertTrue(status.getText().contains("6 esempi"));
        assertEquals(RegressionTreeGUI.RED, status.getForeground());

        clickWithAutoClosingDialog("Dettagli");
    }

    /**
     * Verifica il rifiuto di un percorso vuoto.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void trainRejectsTheLowerEmptyValue() throws Exception {
        field("fileField", JTextField.class).setText("");

        invokeWithAutoClosingDialog("train");

        assertFalse(field("treeCanvas", RegressionTreeCanvas.class).hasTree());
        assertEquals("Pronto", field("statusLabel", JLabel.class).getText());
    }

    /**
     * Verifica il rifiuto di un file inesistente.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void trainRejectsAnInvalidUpperValue() throws Exception {
        field("fileField", JTextField.class).setText(
            "missing-" + System.nanoTime() + ".dat"
        );

        invokeWithAutoClosingDialog("train");

        assertFalse(field("treeCanvas", RegressionTreeCanvas.class).hasTree());
        assertFalse(field("detailsButton", JButton.class).isEnabled());
        assertEquals(
            "Training non completato",
            field("statusLabel", JLabel.class).getText()
        );
        assertEquals(
            RegressionTreeGUI.RED,
            field("statusLabel", JLabel.class).getForeground()
        );
    }

    /**
     * Verifica il centraggio senza albero.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void centerTreeReturnsWhenNoTreeIsAvailable() throws Exception {
        onEdt(() -> {
            invoke("centerTree", new Class<?>[0]);
            return null;
        });

        assertFalse(field("treeCanvas", RegressionTreeCanvas.class).hasTree());
    }

    /**
     * Verifica la chiusura del selettore file.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void chooseFileHandlesAClosedDialog() throws Exception {
        clickWithAutoClosingDialog("Scegli file");

        assertEquals("prova.dat", field("fileField", JTextField.class).getText());
    }

    /**
     * Verifica lo stile di un pulsante premuto.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void buttonStylePaintsPressedState() throws Exception {
        final JButton button = new JButton("Boundary button");

        onEdt(() -> {
            Method method = RegressionTreeGUI.class.getDeclaredMethod(
                "styleButton",
                JButton.class,
                Color.class,
                Color.class
            );
            method.setAccessible(true);
            method.invoke(null, button, Color.WHITE, Color.BLACK);
            button.setSize(180, 50);
            button.getModel().setArmed(true);
            button.getModel().setPressed(true);

            BufferedImage image = new BufferedImage(180, 50, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            try {
                button.paint(graphics);
            } finally {
                graphics.dispose();
            }
            return null;
        });

        assertEquals(Color.WHITE, button.getBackground());
        assertEquals(Color.BLACK, button.getForeground());
        assertTrue(button.isOpaque());
        assertTrue(button.isContentAreaFilled());
    }

    /**
     * Verifica l'avvio della finestra.
     *
     * @throws Exception se la verifica fallisce
     */
    @Test
    public void mainCreatesAVisibleApplicationWindow() throws Exception {
        RegressionTreeGUI.main(new String[] {"lower", "middle", "upper"});
        onEdt(() -> null);

        boolean found = false;
        for (Window window : Window.getWindows()) {
            if (window instanceof RegressionTreeGUI && window.isVisible()) {
                found = true;
            }
        }
        assertTrue(found);
        assertEquals("on", System.getProperty("awt.useSystemAAFontSettings"));
        assertEquals("true", System.getProperty("swing.aatext"));
    }

    /**
     * Invoca un metodo chiudendo il dialogo.
     *
     * @param methodName metodo da invocare
     * @throws Exception se l'invocazione fallisce
     */
    private void invokeWithAutoClosingDialog(final String methodName)
        throws Exception {
        withAutoClosingDialog(() -> {
            invoke(methodName, new Class<?>[0]);
            return null;
        });
    }

    /**
     * Preme un pulsante chiudendo il dialogo.
     *
     * @param buttonText testo del pulsante
     * @throws Exception se l'azione fallisce
     */
    private void clickWithAutoClosingDialog(final String buttonText)
        throws Exception {
        withAutoClosingDialog(() -> {
            button(buttonText).doClick();
            return null;
        });
    }

    /**
     * Esegue un'azione e chiude i dialoghi.
     *
     * @param action azione da eseguire
     * @throws Exception se l'azione fallisce
     */
    private void withAutoClosingDialog(final Callable<Void> action)
        throws Exception {
        onEdt(() -> {
            Timer timer = new Timer(50, event -> {
                for (Window window : Window.getWindows()) {
                    if (window instanceof Dialog && window.isVisible()) {
                        window.dispose();
                    }
                }
            });
            timer.start();
            try {
                action.call();
            } finally {
                timer.stop();
            }
            return null;
        });
    }

    /**
     * Cerca un pulsante nella finestra.
     *
     * @param text testo del pulsante
     * @return pulsante trovato
     */
    private JButton button(String text) {
        return button(gui.getContentPane(), text);
    }

    /**
     * Cerca ricorsivamente un pulsante.
     *
     * @param container contenitore da visitare
     * @param text testo del pulsante
     * @return pulsante trovato, oppure {@code null}
     */
    private JButton button(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (text.equals(button.getText())) {
                    return button;
                }
            }
            if (component instanceof Container) {
                JButton match = button((Container) component, text);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    /**
     * Invoca un metodo privato della GUI.
     *
     * @param name nome del metodo
     * @param parameterTypes tipi dei parametri
     * @param arguments argomenti
     * @return risultato del metodo
     * @throws Exception se l'invocazione fallisce
     */
    private Object invoke(String name, Class<?>[] parameterTypes, Object... arguments)
        throws Exception {
        Method method = RegressionTreeGUI.class.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(gui, arguments);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw exception;
        }
    }

    /**
     * Legge un campo privato della GUI.
     *
     * @param <T> tipo del campo
     * @param name nome del campo
     * @param type classe del campo
     * @return valore del campo
     * @throws Exception se la lettura fallisce
     */
    private <T> T field(String name, Class<T> type) throws Exception {
        Field field = RegressionTreeGUI.class.getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(gui));
    }

    /**
     * Esegue un'azione sul thread Swing.
     *
     * @param <T> tipo del risultato
     * @param action azione da eseguire
     * @return risultato dell'azione
     * @throws Exception se l'azione fallisce
     */
    private <T> T onEdt(Callable<T> action) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            return action.call();
        }

        FutureTask<T> task = new FutureTask<T>(action);
        SwingUtilities.invokeAndWait(task);
        try {
            return task.get();
        } catch (java.util.concurrent.ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw exception;
        }
    }
}
