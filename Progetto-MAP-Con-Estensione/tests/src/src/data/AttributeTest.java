package data;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AttributeTest {
    @Test
    public void continuousAttributeExposesMinimumMediumAndMaximumIndexes() {
        int[] indexes = {Integer.MIN_VALUE, 0, Integer.MAX_VALUE};
        String[] names = {"", "temperature", "z"};
        for (int i = 0; i < indexes.length; i++) {
            ContinuousAttribute attribute = new ContinuousAttribute(names[i], indexes[i]);
            assertEquals(names[i], attribute.getName());
            assertEquals(indexes[i], attribute.getIndex());
            assertEquals(names[i], attribute.toString());
        }
    }

    @Test
    public void discreteAttributeSupportsMinimumMediumAndMaximumDomainSizes() {
        Set<String> empty = new TreeSet<String>();
        DiscreteAttribute minimum = new DiscreteAttribute("empty", 0, empty);
        assertEquals(0, minimum.getNumberOfDistinctValues());
        assertFalse(minimum.iterator().hasNext());

        Set<String> mediumValues = new TreeSet<String>(Arrays.asList("middle", "minimum", "maximum"));
        DiscreteAttribute medium = new DiscreteAttribute("level", 7, mediumValues);
        assertEquals(3, medium.getNumberOfDistinctValues());
        Iterator<String> iterator = medium.iterator();
        assertEquals("maximum", iterator.next());
        assertEquals("middle", iterator.next());
        assertEquals("minimum", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> maximumValues = new TreeSet<String>();
        for (int i = 0; i < 1_000; i++) maximumValues.add(String.format("v%04d", i));
        DiscreteAttribute maximum = new DiscreteAttribute("large", Integer.MAX_VALUE, maximumValues);
        assertEquals(1_000, maximum.getNumberOfDistinctValues());
        assertTrue(maximum.iterator().hasNext());
    }

    @Test
    public void discreteAttributeUsesTheProvidedLiveSet() {
        Set<String> values = new TreeSet<String>();
        DiscreteAttribute attribute = new DiscreteAttribute("kind", 1, values);
        values.add("new");
        assertEquals(1, attribute.getNumberOfDistinctValues());
        assertSame(values.iterator().next(), attribute.iterator().next());
    }
}
