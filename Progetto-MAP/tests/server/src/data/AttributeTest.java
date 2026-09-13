package data;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AttributeTest {
    @Test
    public void continuousAttributeCoversMinimumMediumAndMaximumIndexes() {
        int[] indexes = {Integer.MIN_VALUE, 0, Integer.MAX_VALUE};
        String[] names = {"", "amount", "maximum"};
        for (int i = 0; i < indexes.length; i++) {
            ContinuousAttribute attribute = new ContinuousAttribute(names[i], indexes[i]);
            assertEquals(names[i], attribute.getName());
            assertEquals(indexes[i], attribute.getIndex());
            assertEquals(names[i], attribute.toString());
        }
    }

    @Test
    public void discreteAttributeCoversMinimumMediumAndMaximumDomains() {
        DiscreteAttribute empty = new DiscreteAttribute("empty", 0, Collections.<String>emptySet());
        assertEquals(0, empty.getNumberOfDistinctValues());
        assertFalse(empty.iterator().hasNext());

        Set<String> values = new TreeSet<String>(Arrays.asList("medium", "maximum", "minimum"));
        DiscreteAttribute attribute = new DiscreteAttribute("size", 1, values);
        assertEquals(3, attribute.getNumberOfDistinctValues());
        Iterator<String> iterator = attribute.iterator();
        assertEquals("maximum", iterator.next());
        assertEquals("medium", iterator.next());
        assertEquals("minimum", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void trainingExceptionConstructorsPreserveDetails() {
        TrainingDataException message = new TrainingDataException("bad data");
        assertEquals("bad data", message.getMessage());
        Exception cause = new IllegalStateException("database failed");
        TrainingDataException wrapped = new TrainingDataException(cause);
        assertEquals(cause, wrapped.getCause());
    }
}
