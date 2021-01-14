package sebfisch.graphics.rendering;

import java.util.stream.IntStream;

import sebfisch.concurrent.Interruptible;
import sebfisch.graphics.Box;
import sebfisch.graphics.Pixel;
import sebfisch.graphics.Point;

public class StreamRenderer extends AbstractRenderer {

    @Override
    public boolean render(final Box pixels) {
        final Thread origin = Thread.currentThread();
        return render(pixels, origin::isInterrupted);
    }

    public boolean render(final Box pixels, final Interruptible origin) {
        if (image == null || params == null || raster == null) {
            return false;
        }

        final int width = raster.getWidth();
        final int height = raster.getHeight();
        final int w = pixels.size.x;
        
        IntStream.range(0, w * pixels.size.y).forEach(index -> {
            if (!origin.wasInterrupted()) {
                final Pixel pixel = //
                    new Pixel(index % w, index / w).plus(pixels.min);
                final Point point = params.pointAt(pixel, width, height);
                raster.setPixelColor(pixel, image.colorAt(point));
            }
        });
        
        return !origin.wasInterrupted();
    }
}
