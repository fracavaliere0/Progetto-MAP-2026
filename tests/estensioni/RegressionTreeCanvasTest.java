package estensioni;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import data.Data;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import org.junit.Before;
import org.junit.Test;
import support.TestData;
import tree.RegressionTree;

/** Verifica disposizione, zoom e rendering del pannello dell'albero. */
public class RegressionTreeCanvasTest {

    private RegressionTreeCanvas canvas;

    @Before
    public void setUp() {
        canvas = new RegressionTreeCanvas();
    }

    @Test
    public void emptyCanvasUsesDefaultBoundsAndRendering() {
        assertFalse(canvas.hasTree());
        assertEquals(0.0, canvas.getRootCenterX(), 0.0);
        assertEquals(new Dimension(600, 460), canvas.getPreferredSize());

        paint(canvas);
    }

    @Test
    public void setScaleClampsLowerMiddleAndUpperValues() {
        canvas.setScale(Double.NEGATIVE_INFINITY);
        assertEquals(0.5, canvas.getScale(), 0.0);

        canvas.setScale(1.0);
        assertEquals(1.0, canvas.getScale(), 0.0);

        canvas.setScale(Double.POSITIVE_INFINITY);
        assertEquals(1.5, canvas.getScale(), 0.0);
    }

    @Test
    public void setTreeLaysOutAndPaintsLeafAndSplitTrees() throws Exception {
        VisualTree leaf = VisualTree.from(
            new RegressionTree(new Data(TestData.createSingleValueData()))
        );
        canvas.setTree(leaf);

        assertTrue(canvas.hasTree());
        assertTrue(canvas.getRootCenterX() > 0);
        assertEquals(new Dimension(272, 460), canvas.getPreferredSize());
        paint(canvas);

        VisualTree split = VisualTree.from(
            new RegressionTree(new Data(TestData.createTreeData()))
        );
        canvas.setTree(split);

        assertTrue(canvas.getPreferredSize().width > 600);
        assertTrue(canvas.getPreferredSize().height > 460);
        paint(canvas);

        canvas.setTree(null);
        assertFalse(canvas.hasTree());
        assertEquals(new Dimension(600, 460), canvas.getPreferredSize());
    }

    @Test
    public void paintShortensLongLowerMiddleAndUpperLabels() throws Exception {
        VisualTree.VisualNode root = new VisualTree.VisualNode(
            "an attribute name that is much wider than the node",
            "metadata that is also much wider than the available node",
            "",
            false
        );
        root.children.add(
            new VisualTree.VisualNode(
                "lower prediction with a very long title",
                "lower metadata with a very long description",
                "= a very long lower branch value",
                true
            )
        );
        root.children.add(
            new VisualTree.VisualNode("middle", "middle metadata", "= M", true)
        );
        root.children.add(
            new VisualTree.VisualNode(
                "upper prediction with a very long title",
                "upper metadata with a very long description",
                "= a very long upper branch value",
                true
            )
        );
        Constructor<VisualTree> constructor = VisualTree.class.getDeclaredConstructor(
            VisualTree.VisualNode.class
        );
        constructor.setAccessible(true);
        canvas.setTree(constructor.newInstance(root));

        paint(canvas);

        assertTrue(root.subtreeWidth > 0);
        assertTrue(root.children.get(0).x < root.children.get(1).x);
        assertTrue(root.children.get(1).x < root.children.get(2).x);
    }

    private void paint(RegressionTreeCanvas component) {
        Dimension size = component.getPreferredSize();
        component.setSize(Math.max(1, size.width), Math.max(1, size.height));
        BufferedImage image = new BufferedImage(
            Math.max(1, size.width),
            Math.max(1, size.height),
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = image.createGraphics();
        try {
            component.paint(graphics);
        } finally {
            graphics.dispose();
        }
    }
}
