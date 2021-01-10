package sebfisch.coloring;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;
import sebfisch.graphics.Point;
import sebfisch.graphics.rendering.ImageCanvas;
import sebfisch.graphics.rendering.Renderer;
import sebfisch.graphics.rendering.StreamRenderer;

public class ColoringApp {
    public static void main(final String[] args) {
        new ColoringApp(3);
    }

    public ColoringApp(final int size) {
        final ImageParams params = //
            new ImageParams(new Point(0, 0), 0.01);

        final Image image = new GridImage(new Grid(size));

        final Renderer renderer = new StreamRenderer();

        final ImageCanvas canvas = new ImageCanvas(renderer);
        canvas.setPreferredSize(new Dimension(500, 500));
        canvas.setImage(image);
        canvas.setParams(params);

        final Frame frame = new Frame("ColoringApp");
        frame.addWindowListener(EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
    }

    private static final WindowAdapter EXIT_ON_CLOSE = new WindowAdapter() {
        @Override
        public void windowClosing(final WindowEvent event) {
            System.exit(0);
        }
    };
}
