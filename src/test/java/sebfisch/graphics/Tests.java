package sebfisch.graphics;

import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class Tests {

    private static final Dimension SIZE = new Dimension(800, 600);

    @Test
    void testPixelToPointConversionCloseToZeroCenter() {
        final ImageParams params = new ImageParams(new Point(0, 0), 0.1);
        final Pixel pixel = new Pixel(400, 300);
        final Point point = params.pointAt(pixel, SIZE);
        assertTrue(Math.abs(+0.05 - point.x) < 1e-5, "x = " + point.x);
        assertTrue(Math.abs(-0.05 - point.y) < 1e-5, "y = " + point.y);
    }

    @Test
    void testPixelToPointConversionElsewhereWithZeroCenter() {
        final ImageParams params = new ImageParams(new Point(0, 0), 0.1);
        final Pixel pixel = new Pixel(600, 100);
        final Point point = params.pointAt(pixel, SIZE);
        assertTrue(Math.abs(+20.05 - point.x) < 1e-5, "x = " + point.x);
        assertTrue(Math.abs(+19.95 - point.y) < 1e-5, "y = " + point.y);
    }

    @Test
    void testPixelToPointConversionWithTopLeftAtZero() {
        final ImageParams params = new ImageParams(new Point(40, -30), 0.1);
        final Pixel pixel = new Pixel(0, 0);
        final Point point = params.pointAt(pixel, SIZE);
        assertTrue(Math.abs(+0.05 - point.x) < 1e-5, "x = " + point.x);
        assertTrue(Math.abs(-0.05 - point.y) < 1e-5, "y = " + point.y);
    }

    @Test
    void testPixelToPointConversionWithTopLeftAtZeroElsewhere() {
        final ImageParams params = new ImageParams(new Point(40, -30), 0.1);
        final Pixel pixel = new Pixel(600, 100);
        final Point point = params.pointAt(pixel, SIZE);
        assertTrue(Math.abs(+60.05 - point.x) < 1e-5, "x = " + point.x);
        assertTrue(Math.abs(-10.05 - point.y) < 1e-5, "y = " + point.y);
    }
    
    @Test
    void testThatBoxCanBeSplitEvenlyFromTopLeftToBottomRight() {
        String actual = new Box(new Pixel(100,100), new Pixel(200,200)) //
            .split(2, 2) //
            .map(Box::toString) //
            .collect(Collectors.joining());
        String expected =
            "PixelArea [min=(100,100), size=(100,100)]" +
            "PixelArea [min=(200,100), size=(100,100)]" +
            "PixelArea [min=(100,200), size=(100,100)]" +
            "PixelArea [min=(200,200), size=(100,100)]";
        assertEquals(expected, actual);
    }
}
