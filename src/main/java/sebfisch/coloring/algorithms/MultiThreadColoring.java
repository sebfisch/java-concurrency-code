package sebfisch.coloring.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sebfisch.coloring.ColoredLock;
import sebfisch.coloring.EndlessGrid;

public class MultiThreadColoring extends AbstractGridColoring {
    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    public MultiThreadColoring(final EndlessGrid grid) {
        super(grid);
    }

    @Override
    public void pickNewColors() {
        grid.indices().forEach(i -> {
            pickNewColor(grid.row(i), grid.col(i));
        });
    }

    @Override
    public void pickNewColor(int row, int col) {
        POOL.execute(() -> {
            final List<ColoredLock> neighbors = grid.neighbors(row, col);

            final List<ColoredLock> locked = new ArrayList<>(neighbors);
            locked.add(grid.getCell(row, col));
            Collections.sort(locked);
            locked.forEach(cell -> {
                cell.lock(cell.index() == grid.index(row, col));
            });
            try {
                randomSleep();
                final Set<Float> neighborHues = grid.neighborHues(row, col);
                final float newHue = grid.palette() //
                    .filter(hue -> !neighborHues.contains(hue)) //
                    .findFirst() //
                    .orElseThrow();
                
                if (grid.getCell(row, col).getHue() != newHue) {
                    grid.getCell(row, col).setHue(newHue);
                    grid.neighborIndices(row, col).forEach(index -> {
                        pickNewColor(grid.row(index), grid.col(index));
                    });
                }
            } finally {
                locked.forEach(cell -> {
                    cell.unlock(cell.index() == grid.index(row, col));
                });
                runChangeActions();
            }
        });
    }
}
