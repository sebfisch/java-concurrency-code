package sebfisch.graphics;

public class Point {
    public final double x;
    public final double y;

    public Point(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public Point plus(final Point that) {
        return new Point(this.x + that.x, this.y + that.y);
    }

    public Point times(final Point that) {
        return new Point(this.x * that.x, this.y * that.y);
    }

    public Point times(final double z) {
        return new Point(this.x * z, this.y * z);
    }

    public Point minus(final Point that) {
        return this.plus(that.times(-1));
    }

    public Point div(final Point that) {
        return new Point(this.x / that.x, this.y / that.y);
    }

    public Point div(final double z) {
        return new Point(this.x / z, this.y / z);
    }
}
