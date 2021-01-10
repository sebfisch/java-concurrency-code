package sebfisch.graphics.rendering;

import sebfisch.graphics.Box;
import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;
import sebfisch.graphics.PixelRaster;

public interface Renderer {
    Image getImage();
    void setImage(Image image);

    ImageParams getParams();
    void setParams(ImageParams params);

    PixelRaster getRaster();
    void setRaster(PixelRaster raster);

    boolean render(Box pixels);
}
