package sebfisch.coloring;

import java.awt.Color;
import sebfisch.graphics.Image;
import sebfisch.graphics.Point;

public class GridImage implements Image {

    private final Grid grid;

    public GridImage(final Grid grid) {
        this.grid = grid;
    }

    @Override
    public Color colorAt(final Point point) {
        final int row = (int) Math.round(point.y);
        final int col = (int) Math.round(point.x);

        final float hue = 1f * grid.valueAt(row, col) / (grid.maxValue() + 1);
        return Color.getHSBColor(hue, 1, 1);
    }
}
