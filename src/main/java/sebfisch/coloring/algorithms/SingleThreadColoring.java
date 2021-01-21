package sebfisch.coloring.algorithms;

import java.util.Set;

import sebfisch.coloring.EndlessGrid;

public class SingleThreadColoring extends AbstractGridColoring {

    public SingleThreadColoring(final EndlessGrid grid) {
        super(grid);
    }

    @Override
    public void pickNewColors() {
        new Thread(() -> {
            grid.indices().forEach(i -> {
                pickNewColor(grid.row(i), grid.col(i));
            });
        }).start();
    }

    @Override
    public void pickNewColor(int row, int col) {
        final Set<Float> neighborHues = grid.neighborHues(row, col);
        final float newHue = grid.palette() //
            .filter(hue -> !neighborHues.contains(hue)) //
            .findFirst() //
            .orElseThrow();
        
        if (grid.getCell(row, col).getHue() != newHue) {
            randomSleep();

            grid.getCell(row, col).setHue(newHue);
            runChangeActions();
            
            grid.neighborIndices(row, col).forEach(index -> {
                pickNewColor(grid.row(index), grid.col(index));
            });
        }
    }
}
