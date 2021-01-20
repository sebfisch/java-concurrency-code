package sebfisch.graphics.rendering;

import java.awt.Color;

import sebfisch.graphics.Image;
import sebfisch.graphics.Pixel;
import sebfisch.graphics.PixelRaster;

public abstract class RendererAdapter<R extends Renderer> implements Renderer {
    protected final R renderer;

    public RendererAdapter(final R renderer) {
        this.renderer = renderer;
    }

    @Override
    public Image<Pixel, Color> getImage() {
        return renderer.getImage();
    }

    @Override
    public void setImage(final Image<Pixel, Color> image) {
        renderer.setImage(image);
    }

    @Override
    public PixelRaster getRaster() {
        return renderer.getRaster();
    }

    @Override
    public void setRaster(final PixelRaster raster) {
        renderer.setRaster(raster);
    }
}
