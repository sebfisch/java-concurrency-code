package sebfisch.coloring.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import sebfisch.coloring.EndlessGrid;

public abstract class AbstractGridColoring implements GridColoring {
    protected EndlessGrid grid;
    protected List<Runnable> changeActions;

    public AbstractGridColoring(final EndlessGrid grid) {
        this.grid = grid;
        changeActions = new ArrayList<>();
    }

    public void onChange(final Runnable action) {
        changeActions.add(action);
    }

    protected void runChangeActions() {
        changeActions.forEach(Runnable::run);
    }

    protected static void randomSleep() {
        try {
            final long ms = 100 + ThreadLocalRandom.current().nextLong(100);
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {}
    }
}
