package data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;

/** Verifica l'iterazione sui valori di un attributo discreto. */
public class DiscreteAttributeTest {

    @Test
    public void iteratorReturnsValuesInAscendingOrder() {
        Set<String> values = new TreeSet<String>(
            Arrays.asList("low", "middle", "high")
        );
        DiscreteAttribute attribute = new DiscreteAttribute(
            "level",
            1,
            values
        );

        assertEquals(3, attribute.getNumberOfDistinctValues());

        Iterator<String> iterator = attribute.iterator();
        assertEquals("high", iterator.next());
        assertEquals("low", iterator.next());
        assertEquals("middle", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void emptyAttributeHasNoDistinctValues() {
        DiscreteAttribute attribute = new DiscreteAttribute(
            "empty",
            0,
            new TreeSet<String>()
        );

        assertEquals(0, attribute.getNumberOfDistinctValues());
        assertTrue(attribute instanceof Iterable<?>);
        assertFalse(attribute.iterator().hasNext());
    }
}
