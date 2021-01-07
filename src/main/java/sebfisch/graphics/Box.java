package sebfisch.graphics;

import java.util.stream.Stream;

public class Box {
    public final Pixel min;
    public final Pixel size;

    public Box(final Pixel min, final Pixel size) {
        this.min = min;
        this.size = size;
    }

    public Stream<Box> split(final int rows, final int cols) {
        if (size.x % cols != 0 || size.y % rows != 0) {
            throw new IllegalArgumentException("Cannot split PixelArea.");
        }
        final Pixel s = size.div(new Pixel(cols, rows));
        final Stream.Builder<Box> builder = Stream.builder();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final Pixel m = min.plus(s.times(new Pixel(col, row)));
                builder.accept(new Box(m, s));
            }
        }
        return builder.build();
    }

    public Stream<Box> split() {
        return split(maxEvenSplit(size.x), maxEvenSplit(size.y));
    }

    private int maxEvenSplit(final int size) {
        int parts = 1;
        for (int i = 2; i <= 5; i++) {
            if (size % i == 0) {
                parts = i;
            }
        }
        return parts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((min == null) ? 0 : min.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Box other = (Box) obj;
        if (min == null) {
            if (other.min != null)
                return false;
        } else if (!min.equals(other.min))
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PixelArea [min=" + min + ", size=" + size + "]";
    }
}
