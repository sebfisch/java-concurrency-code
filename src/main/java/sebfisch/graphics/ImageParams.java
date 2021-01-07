package sebfisch.graphics;

public class ImageParams {
    public final Point center;
    public final double pixelDist;

    public ImageParams(final Point center, final double pixelDist) {
        this.center = center;
        this.pixelDist = pixelDist;
    }

    public Point pointAt(final Pixel pixel, final int width, final int height) {
        final Point topLeft = center.minus( //
            new Point(width, -height).times(pixelDist/2) //
        );
        return new Point(pixel.x+0.5, -pixel.y-0.5) //
            .times(pixelDist) //
            .plus(topLeft);
    }

    public ImageParams centeredOn(final Point point) {
        return new ImageParams(point, pixelDist);
    }

    public ImageParams centerdOn(final Pixel pixel, final int width, final int height) {
        return centeredOn(pointAt(pixel, width, height));
    }

    public ImageParams zoomedBy(final double factor) {
        return new ImageParams(center, pixelDist * factor);
    }

    @Override
    public String toString() {
        return "ImageParams [center=" + center + ", pixelDist=" + pixelDist + "]";
    }
}
