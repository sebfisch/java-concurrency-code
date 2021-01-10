package sebfisch.graphics.rendering;

import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;
import sebfisch.graphics.PixelRaster;

public abstract class RendererAdapter<R extends Renderer> implements Renderer {
    protected final R renderer;

    public RendererAdapter(final R renderer) {
        this.renderer = renderer;
    }

    @Override
    public Image getImage() {
        return renderer.getImage();
    }

    @Override
    public void setImage(final Image image) {
        renderer.setImage(image);
    }

    @Override
    public ImageParams getParams() {
        return renderer.getParams();
    }

    @Override
    public void setParams(final ImageParams params) {
        renderer.setParams(params);
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
