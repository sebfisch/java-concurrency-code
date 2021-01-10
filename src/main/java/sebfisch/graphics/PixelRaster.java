package sebfisch.graphics;

import java.awt.Color;

public interface PixelRaster {
    int getWidth();
    int getHeight();
    void setPixelColor(Pixel pixel, Color color);
}
