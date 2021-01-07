package sebfisch.graphics.rendering;

import java.awt.image.BufferedImage;
import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;

public abstract class AbstractRenderer implements Renderer {
    protected Image image;
    protected ImageParams params;
    protected BufferedImage buffer;

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

    public BufferedImage getBuffer() {
        return buffer;
    }

    public void setBuffer(final BufferedImage buffer) {
        this.buffer = buffer;
    }
}
