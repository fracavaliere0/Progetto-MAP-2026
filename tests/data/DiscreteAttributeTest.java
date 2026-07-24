package data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Verifica l'accesso ai valori di un attributo discreto. */
public class DiscreteAttributeTest {

    @Test
    public void getValueReturnsLowerMiddleAndUpperValues() {
        DiscreteAttribute attribute = new DiscreteAttribute(
            "level",
            1,
            new String[] {"low", "middle", "high"}
        );

        assertEquals(3, attribute.getNumberOfDistinctValues());
        assertEquals("low", attribute.getValue(0));
        assertEquals("middle", attribute.getValue(1));
        assertEquals("high", attribute.getValue(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getValueRejectsIndexBelowLowerLimit() {
        DiscreteAttribute attribute = new DiscreteAttribute(
            "level",
            0,
            new String[] {"low", "middle", "high"}
        );

        attribute.getValue(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void getValueRejectsIndexAboveUpperLimit() {
        DiscreteAttribute attribute = new DiscreteAttribute(
            "level",
            0,
            new String[] {"low", "middle", "high"}
        );

        attribute.getValue(3);
    }

    @Test
    public void emptyAttributeHasNoDistinctValues() {
        DiscreteAttribute attribute = new DiscreteAttribute(
            "empty",
            0,
            new String[0]
        );

        assertEquals(0, attribute.getNumberOfDistinctValues());
    }
}
