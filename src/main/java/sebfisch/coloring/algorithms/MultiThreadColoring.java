package sebfisch.coloring.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import sebfisch.coloring.EndlessGrid;
import sebfisch.coloring.ColoredLock;

public class MultiThreadColoring extends AbstractGridColoring {
    private static ExecutorService POOL = Executors.newCachedThreadPool();

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
        POOL.submit(() -> {
            final List<ColoredLock> neighbors = grid.neighborIndices(row, col) //
                .map(grid::getCell) //
                .collect(Collectors.toList());
            final List<ColoredLock> locked = new ArrayList<>(neighbors);
            locked.add(grid.getCell(row, col));
            Collections.sort(locked);
            locked.forEach(cell -> {
                cell.lock(cell.index() == grid.index(row, col));
            });
            runChangeActions();
            try {
                final long ms = 100 + ThreadLocalRandom.current().nextLong(100);
                TimeUnit.MILLISECONDS.sleep(ms);
            } catch (InterruptedException e) {}
            final Set<Float> neighborHues = neighbors.stream() //
                .map(ColoredLock::getHue) //
                .collect(Collectors.toSet());
            final float newHue = grid.palette() //
                .filter(hue -> !neighborHues.contains(hue))
                .findFirst() //
                .orElseThrow();
            if (grid.getCell(row, col).getHue() != newHue) {
                grid.getCell(row, col).setHue(newHue);
                locked.forEach(cell -> {
                    cell.unlock(cell.index() == grid.index(row, col));
                });
                runChangeActions();
                grid.neighborIndices(row, col).forEach(index -> {
                    pickNewColor(grid.row(index), grid.col(index));
                });
            } else {
                locked.forEach(cell -> {
                    cell.unlock(cell.index() == grid.index(row, col));
                });    
                runChangeActions();
            }
        });
    }
}
