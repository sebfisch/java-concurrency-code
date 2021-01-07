package sebfisch.fractals;

import java.awt.Color;
import java.util.stream.Stream;
import sebfisch.graphics.Image;
import sebfisch.graphics.Point;

public abstract class FractalImage implements Image {
    private long maxIter = 100;

    public void setMaxIter(final long maxIter) {
        this.maxIter = maxIter;
    }

    public abstract Stream<Point> iterations(Point p);
    public abstract boolean fairlyClose(Point p);

    @Override
    public Color colorAt(final Point p) {
        return color(closeRatio(p));
    }

    public Color color(final float ratio) {
        final float scaled = 0.6f*ratio + 0.2f;
        return Color.getHSBColor(1 - ratio, 1 - scaled, scaled);
    }

    private float closeRatio(final Point p) {
        final long closeIter = iterations(p) //
            .takeWhile(this::fairlyClose) //
            .limit(maxIter) //
            .count();
        return (float) closeIter / maxIter;
    }
}
