package sebfisch.graphics.rendering;

import java.awt.image.BufferedImage;
import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;

public abstract class RendererAdapter implements Renderer {
    protected final Renderer renderer;

    public RendererAdapter(final Renderer renderer) {
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
    public BufferedImage getBuffer() {
        return renderer.getBuffer();
    }

    @Override
    public void setBuffer(final BufferedImage buffer) {
        renderer.setBuffer(buffer);
    }
}
