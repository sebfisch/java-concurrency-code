package sebfisch.coloring;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EndlessGrid {
    private final int size;
    private ConcurrentHashMap<Integer,ColoredLock> cells;

    public EndlessGrid(final int size) {
        this.size = size;
        cells = new ConcurrentHashMap<>(size*size);
        resetColors();
    }

    public void resetColors() {
        List<Float> hueList = palette().collect(Collectors.toList());
        indices().forEach(index -> {
            cells.compute(index, (i, c) -> new ColoredLock(i, hueList.get(i)));
        });
    }

    public int size() {
        return size;
    }

    public IntStream indices() {
        return IntStream.range(0, size*size);
    }

    public ColoredLock getCell(final int row, final int col) {
        return getCell(index(row, col));
    }

    public ColoredLock getCell(final int index) {
        return cells.get(index);
    }

    public int index(final int row, final int col) {
        final int r = normalize(row);
        final int c = normalize(col);
        return r * size + c;
    }

    private int normalize(final int val) {
        return ((val % size) + size) % size;
    }

    public int row(final int index) {
        return index / size;
    }

    public int col(final int index) {
        return index % size;
    }

    public Stream<Integer> neighborIndices(final int row, final int col) {
        return Stream.of( //
            index(row-1, col),
            index(row, col-1),
            index(row, col+1),
            index(row+1, col)
        );
    }

    public Stream<Float> palette() {
        return IntStream //
            .rangeClosed(0, 32 - Integer.numberOfLeadingZeros(size*size)) //
            .boxed() //
            .flatMap(i -> {
                if (i == 0) {
                    return Stream.of(0f);
                } else {
                    final int power = (int) Math.pow(2, i-1);
                    return IntStream.range(0, power) //
                        .mapToObj(j -> (j + 0.5f) / power);
                }
            });
    }
}
