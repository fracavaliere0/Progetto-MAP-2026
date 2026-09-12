package data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Verifica gli attributi continui sui limiti e sul valore intermedio. */
public class AttributeTest {

    @Test
    public void constructorAndAccessorsSupportBoundaryValues() {
        String[] names = {"", "middle", "a-very-long-attribute-name"};
        int[] indexes = {Integer.MIN_VALUE, 0, Integer.MAX_VALUE};

        for (int i = 0; i < indexes.length; i++) {
            Attribute attribute = new ContinuousAttribute(names[i], indexes[i]);

            assertEquals(names[i], attribute.getName());
            assertEquals(indexes[i], attribute.getIndex());
            assertEquals(names[i], attribute.toString());
        }
    }
}
