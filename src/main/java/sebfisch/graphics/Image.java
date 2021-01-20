package sebfisch.graphics;

import java.util.function.Function;

@FunctionalInterface
public interface Image<P, C> {
    C colorAt(P point);

    default <D> Image<P, D> mapC(final Function<C, D> function) {
        return point -> function.apply(colorAt(point));
    }

    default <Q> Image<Q, C> mapP(final Function<Q, P> function) {
        return point -> colorAt(function.apply(point));
    }
}
