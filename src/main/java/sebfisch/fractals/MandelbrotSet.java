package sebfisch.fractals;

import java.util.stream.Stream;
import sebfisch.graphics.Point;

public class MandelbrotSet extends FractalImage {

    @Override
    public Stream<Point> iterations(final Point p) {
        return Stream.iterate(new Point(0,0), q -> step(q, p));
    }

    @Override
    public boolean fairlyClose(final Point p) {
        return p.x*p.x + p.y*p.y < 4;
    }

    protected Point step(final Point z, final Point c) {
        return new Point(z.x*z.x - z.y*z.y + c.x, 2*z.x*z.y + c.y);
    }
}
