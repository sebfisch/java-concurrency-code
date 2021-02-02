package sebfisch.graphics.rendering;

import sebfisch.concurrent.Interruptible;
import sebfisch.graphics.Box;

public class StreamRenderer extends AbstractRenderer {

    @Override
    public boolean render(final Box box) {
        final Thread origin = Thread.currentThread();
        return render(box, origin::isInterrupted);
    }

    public boolean render(final Box box, final Interruptible origin) {
        if (image == null || raster == null) {
            return false;
        }

        box.pixels().forEach(pixel -> {
            if (!origin.wasInterrupted()) {
                raster.setPixelColor(pixel, image.colorAt(pixel));
            }
        });
        
        return !origin.wasInterrupted();
    }
}
