package support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import utility.Keyboard;

/** Configura in modo deterministico l'input statico usato da {@link Keyboard}. */
public final class KeyboardInput {

    private KeyboardInput() {}

    /**
     * Sostituisce l'input standard e azzera lo stato interno del lettore.
     *
     * @param input testo da rendere disponibile al lettore.
     */
    public static void set(String input) {
        set(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Ripristina lo stream indicato come input del lettore.
     *
     * @param input stream da usare.
     */
    public static void set(InputStream input) {
        try {
            System.setIn(input);
            setField("in", new BufferedReader(new InputStreamReader(input)));
            setField("reader", null);
            setField("current_token", null);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Impossibile configurare Keyboard", exception);
        }
    }

    private static void setField(String name, Object value)
        throws ReflectiveOperationException {
        Field field = Keyboard.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }
}
