package sebfisch.graphics;

import java.awt.Dimension;

public final class ImageParams {
    public final Point center;
    public final double pixelDist;

    public ImageParams(final Point center, final double pixelDist) {
        this.center = center;
        this.pixelDist = pixelDist;
    }

    public Point pointAt(final Pixel pixel, final Dimension size) {
        final Point topLeft = center.minus( //
            new Point(size.width, -size.height).times(pixelDist/2) //
        );
        return new Point(pixel.x+0.5, -pixel.y-0.5) //
            .times(pixelDist) //
            .plus(topLeft);
    }

    public <C> Image<Pixel, C> pixelImage(final Image<Point, C> image, //
            final Dimension size) {
        return image.mapP(pixel -> pointAt(pixel, size));
    }

    public ImageParams centeredOn(final Point point) {
        return new ImageParams(point, pixelDist);
    }

    public ImageParams centerdOn(final Pixel pixel, final Dimension size) {
        return centeredOn(pointAt(pixel, size));
    }

    public ImageParams zoomedBy(final double factor) {
        return new ImageParams(center, pixelDist * factor);
    }

    public ImageParams zoomedAround(final Point point, final double factor) {
        return this //
            .centeredOn(point.plus(center.minus(point).times(factor))) //
            .zoomedBy(factor);
    }

    @Override
    public String toString() {
        return "ImageParams [center=" + center + ", pixelDist=" + pixelDist + "]";
    }
}
