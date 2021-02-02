package sebfisch.graphics.rendering;

import java.awt.Color;

import sebfisch.graphics.Box;
import sebfisch.graphics.Image;
import sebfisch.graphics.Pixel;
import sebfisch.graphics.PixelRaster;

public interface Renderer {
    Image<Pixel, Color> getImage();
    void setImage(Image<Pixel, Color> image);

    PixelRaster getRaster();
    void setRaster(PixelRaster raster);

    boolean render(Box box);
}
