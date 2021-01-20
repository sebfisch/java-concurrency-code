package sebfisch.coloring;

import java.awt.Color;

import sebfisch.graphics.Image;
import sebfisch.graphics.Point;

public class GridImage implements Image<Point, Color> {

    private final EndlessGrid grid;

    public GridImage(final EndlessGrid grid) {
        this.grid = grid;
    }

    @Override
    public Color colorAt(final Point point) {
        final int row = (int) Math.floor(-point.y);
        final int col = (int) Math.floor(point.x);
        return grid.getCell(row, col).getColor();
    }
}
