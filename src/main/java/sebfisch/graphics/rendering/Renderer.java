package sebfisch.graphics.rendering;

import java.awt.image.BufferedImage;
import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;
import sebfisch.graphics.Box;

public interface Renderer {
    Image getImage();
    void setImage(Image image);

    ImageParams getParams();
    void setParams(ImageParams params);

    BufferedImage getBuffer();
    void setBuffer(BufferedImage buffer);

    boolean render(Box pixels);
}
