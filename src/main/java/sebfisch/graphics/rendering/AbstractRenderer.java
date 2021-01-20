package sebfisch.graphics.rendering;

import java.awt.Color;

import sebfisch.graphics.Image;
import sebfisch.graphics.Pixel;
import sebfisch.graphics.PixelRaster;

public abstract class AbstractRenderer implements Renderer {
    protected Image<Pixel, Color> image;
    protected PixelRaster raster;

    public Image<Pixel, Color> getImage() {
        return image;
    }

    public void setImage(final Image<Pixel, Color> image) {
        this.image = image;
    }

    public PixelRaster getRaster() {
        return raster;
    }

    public void setRaster(final PixelRaster raster) {
        this.raster = raster;
    }
}
