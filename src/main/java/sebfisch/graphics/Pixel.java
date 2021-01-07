package sebfisch.graphics;

public class Pixel {
    public final int x;
    public final int y;

    public Pixel(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public Pixel plus(final Pixel that) {
        return new Pixel(this.x + that.x, this.y + that.y);
    }

    public Pixel times(final Pixel that) {
        return new Pixel(this.x * that.x, this.y * that.y);
    }

    public Pixel times(final int z) {
        return new Pixel(this.x * z, this.y * z);
    }

    public Pixel minus(final Pixel that) {
        return this.plus(that.times(-1));
    }

    public Pixel div(final Pixel that) {
        return new Pixel(this.x / that.x, this.y / that.y);
    }

    public Pixel div(final int z) {
        return new Pixel(this.x / z, this.y / z);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
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
        Pixel other = (Pixel) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }
}
