package sebfisch.fractals;

import java.util.stream.Stream;
import sebfisch.graphics.Point;

public class JuliaSet extends MandelbrotSet {

    private final Point c;

    public JuliaSet(final Point c) {
        this.c = c;
    }

    @Override
    public Stream<Point> iterations(final Point p) {
        return Stream.iterate(p, q -> step(q, c));
    }
}
