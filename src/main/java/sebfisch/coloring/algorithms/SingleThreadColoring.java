package sebfisch.coloring.algorithms;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import sebfisch.coloring.EndlessGrid;
import sebfisch.coloring.ColoredLock;

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
        final Set<Float> neighborHues = grid.neighborIndices(row, col) //
            .map(grid::getCell) //
            .map(ColoredLock::getHue) //
            .collect(Collectors.toSet());
        final float newHue = grid.palette() //
            .filter(hue -> !neighborHues.contains(hue))
            .findFirst() //
            .orElseThrow();
        if (grid.getCell(row, col).getHue() != newHue) {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {}
            grid.getCell(row, col).setHue(newHue);
            runChangeActions();
            grid.neighborIndices(row, col).forEach(index -> {
                pickNewColor(grid.row(index), grid.col(index));
            });
        }
    }
}
