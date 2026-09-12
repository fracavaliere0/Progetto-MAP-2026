package data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import org.junit.Test;

/** Verifica entrambe le modalità di costruzione dell'eccezione. */
public class TrainingDataExceptionTest {

    @Test
    public void constructorsPreserveMessageAndCause() {
        TrainingDataException withMessage = new TrainingDataException("invalid data");
        IOException cause = new IOException("missing file");
        TrainingDataException withCause = new TrainingDataException(cause);

        assertEquals("invalid data", withMessage.getMessage());
        assertSame(cause, withCause.getCause());
    }
}
