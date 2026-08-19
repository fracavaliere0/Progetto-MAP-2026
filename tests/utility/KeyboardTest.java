package utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import support.KeyboardInput;

/** Verifica la lettura da tastiera con valori limite, intermedi ed errati. */
public class KeyboardTest {

    private InputStream originalInput;
    private PrintStream originalOutput;

    @Before
    public void setUp() {
        originalInput = System.in;
        originalOutput = System.out;
        Keyboard.resetErrorCount(0);
        Keyboard.setPrintErrors(false);
    }

    @After
    public void tearDown() {
        KeyboardInput.set(originalInput);
        System.setOut(originalOutput);
        Keyboard.setPrintErrors(true);
        Keyboard.resetErrorCount(0);
    }

    @Test
    public void errorConfigurationCanBeReadAndChanged() {
        Keyboard.setPrintErrors(false);
        assertFalse(Keyboard.getPrintErrors());

        Keyboard.setPrintErrors(true);
        assertTrue(Keyboard.getPrintErrors());

        Keyboard.setPrintErrors(false);
        assertFalse(Keyboard.getPrintErrors());
    }

    @Test
    public void resetErrorCountAcceptsLowerMiddleAndUpperArguments() {
        KeyboardInput.set("invalid\n");
        Keyboard.readInt();
        assertEquals(1, Keyboard.getErrorCount());

        Keyboard.resetErrorCount(Integer.MIN_VALUE);
        assertEquals(0, Keyboard.getErrorCount());
        Keyboard.resetErrorCount(0);
        assertEquals(0, Keyboard.getErrorCount());
        Keyboard.resetErrorCount(Integer.MAX_VALUE);
        assertEquals(0, Keyboard.getErrorCount());
    }

    @Test
    public void readStringPreservesTheCompleteLine() {
        KeyboardInput.set("lower middle upper\n");

        assertEquals("lower middle upper", Keyboard.readString());
        assertTrue(Keyboard.endOfLine());
    }

    @Test
    public void readWordReturnsLowerMiddleAndUpperTokens() {
        KeyboardInput.set("lower middle upper\n");

        assertEquals("lower", Keyboard.readWord());
        assertFalse(Keyboard.endOfLine());
        assertEquals("middle", Keyboard.readWord());
        assertEquals("upper", Keyboard.readWord());
        assertTrue(Keyboard.endOfLine());
    }

    @Test
    public void readBooleanHandlesFalseTrueAndInvalidValues() {
        KeyboardInput.set("false true unknown\n");

        assertFalse(Keyboard.readBoolean());
        assertTrue(Keyboard.readBoolean());
        assertFalse(Keyboard.readBoolean());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    public void readCharConsumesLowerMiddleAndUpperCharacters() {
        KeyboardInput.set("AMZ\n");

        assertEquals('A', Keyboard.readChar());
        assertEquals('M', Keyboard.readChar());
        assertEquals('Z', Keyboard.readChar());
    }

    @Test
    public void readIntHandlesLowerMiddleAndUpperValues() {
        KeyboardInput.set(
            Integer.MIN_VALUE + " 0 " + Integer.MAX_VALUE + "\n"
        );

        assertEquals(Integer.MIN_VALUE, Keyboard.readInt());
        assertEquals(0, Keyboard.readInt());
        assertEquals(Integer.MAX_VALUE, Keyboard.readInt());
    }

    @Test
    public void readLongHandlesLowerMiddleAndUpperValues() {
        KeyboardInput.set(Long.MIN_VALUE + " 0 " + Long.MAX_VALUE + "\n");

        assertEquals(Long.MIN_VALUE, Keyboard.readLong());
        assertEquals(0L, Keyboard.readLong());
        assertEquals(Long.MAX_VALUE, Keyboard.readLong());
    }

    @Test
    public void readFloatHandlesLowerMiddleAndUpperValues() {
        KeyboardInput.set(
            -Float.MAX_VALUE + " 0 " + Float.MAX_VALUE + "\n"
        );

        assertEquals(-Float.MAX_VALUE, Keyboard.readFloat(), 0.0f);
        assertEquals(0.0f, Keyboard.readFloat(), 0.0f);
        assertEquals(Float.MAX_VALUE, Keyboard.readFloat(), 0.0f);
    }

    @Test
    public void readDoubleHandlesLowerMiddleAndUpperValues() {
        KeyboardInput.set(
            -Double.MAX_VALUE + " 0 " + Double.MAX_VALUE + "\n"
        );

        assertEquals(-Double.MAX_VALUE, Keyboard.readDouble(), 0.0);
        assertEquals(0.0, Keyboard.readDouble(), 0.0);
        assertEquals(Double.MAX_VALUE, Keyboard.readDouble(), 0.0);
    }

    @Test
    public void invalidNumericValuesReturnDocumentedDefaults() {
        KeyboardInput.set("bad bad bad bad\n");

        assertEquals(Integer.MIN_VALUE, Keyboard.readInt());
        assertEquals(Long.MIN_VALUE, Keyboard.readLong());
        assertTrue(Float.isNaN(Keyboard.readFloat()));
        assertTrue(Double.isNaN(Keyboard.readDouble()));
        assertEquals(4, Keyboard.getErrorCount());
    }

    @Test
    public void missingCharacterReturnsMinimumValue() {
        KeyboardInput.set("");

        assertEquals(Character.MIN_VALUE, Keyboard.readChar());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    public void missingWordAndStringReturnNull() {
        KeyboardInput.set("");
        assertNull(Keyboard.readWord());

        KeyboardInput.set("");
        assertNull(Keyboard.readString());
    }

    @Test
    public void errorsArePrintedOnlyWhenEnabled() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Keyboard.setPrintErrors(false);
        KeyboardInput.set("bad\n");
        Keyboard.readInt();
        assertEquals("", output.toString());

        Keyboard.setPrintErrors(true);
        KeyboardInput.set("bad\n");
        Keyboard.readInt();
        assertTrue(output.toString().contains("Error reading int data"));
    }

    @Test
    public void keyboardCanBeInstantiated() {
        assertTrue(new Keyboard() instanceof Keyboard);
    }
}
