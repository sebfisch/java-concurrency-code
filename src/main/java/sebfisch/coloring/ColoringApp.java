package sebfisch.coloring;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;
import sebfisch.coloring.algorithms.GridColoring;
import sebfisch.coloring.algorithms.MultiThreadColoring;
import sebfisch.concurrent.Throttled;
import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;
import sebfisch.graphics.Point;
import sebfisch.graphics.rendering.ImageCanvas;
import sebfisch.graphics.rendering.Renderer;
import sebfisch.graphics.rendering.StreamRenderer;

public class ColoringApp {
    public static void main(final String[] args) {
        new ColoringApp(4);
    }

    public ColoringApp(final int size) {
        final int boxSize = 50;
        final ImageParams params = //
            new ImageParams(new Point(0.5*(size+1), 0.5*(size-1)), 1f/boxSize);

        final EndlessGrid grid = new EndlessGrid(size);
        final Image image = new GridImage(grid);

        final Renderer renderer = new StreamRenderer();

        final ImageCanvas canvas = new ImageCanvas(renderer);
        final int dim = (size + 1) * boxSize;
        canvas.setPreferredSize(new Dimension(dim, dim));
        canvas.setImage(image);
        canvas.setParams(params);

        final GridColoring coloring = new MultiThreadColoring(grid);
        coloring.onChange(new Throttled(canvas::rerender, 200, TimeUnit.MILLISECONDS));
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                grid.resetColors();
                coloring.pickNewColors();
            }
        });

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
