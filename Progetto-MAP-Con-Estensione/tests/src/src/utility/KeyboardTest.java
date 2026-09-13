package utility;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testsupport.TestSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class KeyboardTest {
    @Before
    public void silenceDiagnostics() {
        Keyboard.setPrintErrors(false);
        Keyboard.resetErrorCount(123);
    }

    @After
    public void restoreDiagnostics() {
        Keyboard.setPrintErrors(true);
        Keyboard.resetErrorCount(0);
    }

    @Test
    public void exposesAndResetsErrorConfiguration() {
        new Keyboard();
        assertFalse(Keyboard.getPrintErrors());
        assertEquals(0, Keyboard.getErrorCount());
        Keyboard.setPrintErrors(true);
        assertTrue(Keyboard.getPrintErrors());
        Keyboard.resetErrorCount(Integer.MAX_VALUE);
        assertEquals(0, Keyboard.getErrorCount());
    }

    @Test
    public void readsWordsAcrossMinimumMediumAndMaximumWhitespace() throws Exception {
        TestSupport.keyboardInput("first   middle\nlast\n");
        assertEquals("first", Keyboard.readWord());
        assertEquals("middle", Keyboard.readWord());
        assertEquals("last", Keyboard.readWord());
        assertNull(Keyboard.readWord());
    }

    @Test
    public void readsWholeStringIncludingItsInternalSpacing() throws Exception {
        TestSupport.keyboardInput("hello  wide world\n");
        assertEquals("hello  wide world", Keyboard.readString());
        assertTrue(Keyboard.endOfLine());
    }

    @Test
    public void emptyStringInputReturnsNullAndCountsError() throws Exception {
        TestSupport.keyboardInput("");
        assertNull(Keyboard.readString());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    public void readsTrueFalseAndInvalidBooleans() throws Exception {
        TestSupport.keyboardInput("TrUe false perhaps\n");
        assertTrue(Keyboard.readBoolean());
        assertFalse(Keyboard.readBoolean());
        assertFalse(Keyboard.readBoolean());
        assertEquals(1, Keyboard.getErrorCount());

        TestSupport.keyboardInput("");
        assertFalse(Keyboard.readBoolean());
        assertEquals(2, Keyboard.getErrorCount());
    }

    @Test
    public void readsCharactersFromStartMiddleAndEndOfToken() throws Exception {
        TestSupport.keyboardInput("abc\nZ\n");
        assertEquals('a', Keyboard.readChar());
        assertEquals('b', Keyboard.readChar());
        assertEquals('c', Keyboard.readChar());
        assertEquals('Z', Keyboard.readChar());

        TestSupport.keyboardInput("");
        assertEquals(Character.MIN_VALUE, Keyboard.readChar());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    public void readsMinimumMediumAndMaximumIntegers() throws Exception {
        TestSupport.keyboardInput(Integer.MIN_VALUE + " 0 " + Integer.MAX_VALUE + "\n");
        assertEquals(Integer.MIN_VALUE, Keyboard.readInt());
        assertEquals(0, Keyboard.readInt());
        assertEquals(Integer.MAX_VALUE, Keyboard.readInt());
    }

    @Test
    public void invalidIntegerReturnsMinimumAndCountsError() throws Exception {
        TestSupport.keyboardInput("12.5\n");
        assertEquals(Integer.MIN_VALUE, Keyboard.readInt());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    public void readsMinimumMediumAndMaximumLongs() throws Exception {
        TestSupport.keyboardInput(Long.MIN_VALUE + " 0 " + Long.MAX_VALUE + "\n");
        assertEquals(Long.MIN_VALUE, Keyboard.readLong());
        assertEquals(0L, Keyboard.readLong());
        assertEquals(Long.MAX_VALUE, Keyboard.readLong());
    }

    @Test
    public void invalidLongReturnsMinimumAndCountsError() throws Exception {
        TestSupport.keyboardInput("huge\n");
        assertEquals(Long.MIN_VALUE, Keyboard.readLong());
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    public void readsMinimumMediumAndMaximumFloats() throws Exception {
        TestSupport.keyboardInput("-" + Float.MAX_VALUE + " 0 " + Float.MAX_VALUE + "\n");
        assertEquals(-Float.MAX_VALUE, Keyboard.readFloat(), 0.0f);
        assertEquals(0.0f, Keyboard.readFloat(), 0.0f);
        assertEquals(Float.MAX_VALUE, Keyboard.readFloat(), 0.0f);
    }

    @Test
    public void invalidFloatReturnsNaNAndCountsError() throws Exception {
        TestSupport.keyboardInput("floating\n");
        assertTrue(Float.isNaN(Keyboard.readFloat()));
        assertEquals(1, Keyboard.getErrorCount());
    }

    @Test
    public void readsMinimumMediumAndMaximumDoubles() throws Exception {
        TestSupport.keyboardInput("-" + Double.MAX_VALUE + " 0 " + Double.MAX_VALUE + "\n");
        assertEquals(-Double.MAX_VALUE, Keyboard.readDouble(), 0.0);
        assertEquals(0.0, Keyboard.readDouble(), 0.0);
        assertEquals(Double.MAX_VALUE, Keyboard.readDouble(), 0.0);
    }

    @Test
    public void invalidDoubleReturnsNaNAndCanPrintDiagnostic() throws Exception {
        Keyboard.setPrintErrors(true);
        TestSupport.keyboardInput("double?\n");
        String output = TestSupport.captureOutput(() -> assertTrue(Double.isNaN(Keyboard.readDouble())));
        assertTrue(output.contains("Error reading double data"));
        assertEquals(1, Keyboard.getErrorCount());
    }
}
