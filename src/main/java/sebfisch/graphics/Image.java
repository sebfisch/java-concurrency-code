package sebfisch.graphics;

import java.awt.Color;
import java.util.function.Function;

@FunctionalInterface
public interface Image {
    Color colorAt(Point point);

    default Image map(final Function<Color, Color> function) {
        return point -> function.apply(colorAt(point));
    }
}
