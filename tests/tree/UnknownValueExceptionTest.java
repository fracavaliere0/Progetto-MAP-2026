package tree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Verifica il messaggio restituito per i valori di predizione sconosciuti. */
public class UnknownValueExceptionTest {

    @Test
    public void constructorPreservesLowerMiddleAndUpperLengthMessages() {
        String[] messages = {"", "unknown", "unknown value outside the valid range"};

        for (String message : messages) {
            assertEquals(message, new UnknownValueException(message).getMessage());
        }
    }
}
