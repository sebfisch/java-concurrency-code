package sebfisch.graphics.rendering;

import java.util.stream.IntStream;
import sebfisch.graphics.Box;
import sebfisch.graphics.Pixel;
import sebfisch.graphics.Point;

public class StreamRenderer extends AbstractRenderer {

    @Override
    public boolean render(final Box pixels) {
        return render(pixels, Thread.currentThread());
    }

    public boolean render(final Box pixels, final Thread origin) {
        if (image == null || params == null || buffer == null) {
            return false;
        }

        final int width = buffer.getWidth();
        final int height = buffer.getHeight();
        final int w = pixels.size.x;
        
        IntStream.range(0, w * pixels.size.y).forEach(index -> {
            if (!origin.isInterrupted()) {
                final Pixel pixel = //
                    new Pixel(index % w, index / w).plus(pixels.min);
                final Point point = params.pointAt(pixel, width, height);
                final int rgb = image.colorAt(point).getRGB();
                buffer.setRGB(pixel.x, pixel.y, rgb);
            }
        });
        
        return !origin.isInterrupted();
    }
}
