package sebfisch.graphics.rendering;

import java.util.stream.IntStream;

import sebfisch.concurrent.Interruptible;
import sebfisch.graphics.Box;
import sebfisch.graphics.Pixel;

public class StreamRenderer extends AbstractRenderer {

    @Override
    public boolean render(final Box pixels) {
        final Thread origin = Thread.currentThread();
        return render(pixels, origin::isInterrupted);
    }

    public boolean render(final Box pixels, final Interruptible origin) {
        if (image == null || raster == null) {
            return false;
        }

        final int w = pixels.size.x;
        IntStream.range(0, w * pixels.size.y).forEach(index -> {
            if (!origin.wasInterrupted()) {
                final Pixel pixel = //
                    new Pixel(index % w, index / w).plus(pixels.min);
                raster.setPixelColor(pixel, image.colorAt(pixel));
            }
        });
        
        return !origin.wasInterrupted();
    }
}
