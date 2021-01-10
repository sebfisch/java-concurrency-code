package sebfisch.graphics.rendering;

import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;
import sebfisch.graphics.PixelRaster;

public abstract class AbstractRenderer implements Renderer {
    protected Image image;
    protected ImageParams params;
    protected PixelRaster raster;

    public Image getImage() {
        return image;
    }

    public void setImage(final Image image) {
        this.image = image;
    }

    public ImageParams getParams() {
        return params;
    }

    public void setParams(final ImageParams params) {
        this.params = params;
    }

    public PixelRaster getRaster() {
        return raster;
    }

    public void setRaster(final PixelRaster raster) {
        this.raster = raster;
    }
}
