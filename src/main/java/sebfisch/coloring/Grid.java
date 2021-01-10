package sebfisch.coloring;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class Grid {
    private static final int THRESHOLD = 10000;

    private final int size;
    private ConcurrentHashMap<Integer,Integer> values;

    public Grid(final int size) {
        this.size = size;
        values = new ConcurrentHashMap<>(size*size);
        IntStream.range(0, size*size).forEach(key -> {
            values.computeIfAbsent(key, k -> k+1);
        });
    }

    public int size() {
        return size;
    }

    public int valueAt(final int row, final int col) {
        final int r = normalize(row);
        final int c = normalize(col);
        return values.get(r * size + c);
    }

    private int normalize(final int val) {
        return ((val % size) + size) % size;
    }

    public int maxValue() {
        return values.reduce(THRESHOLD, (k,v) -> v, Math::max);
    }
}
