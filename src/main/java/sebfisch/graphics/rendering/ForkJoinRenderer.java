package sebfisch.graphics.rendering;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import sebfisch.graphics.Box;

public class ForkJoinRenderer extends RendererAdapter<StreamRenderer> {
    private static final int THRESHOLD = 10000;

    public ForkJoinRenderer() {
        super(new StreamRenderer());
    }

    @Override
    public boolean render(final Box pixels) {
        final ForkJoinPool pool = new ForkJoinPool();
        fork(pool, pixels);
        return join(pool);
    }

    private void fork(final ForkJoinPool pool, final Box pixels) {
        pool.execute(new Action(pixels));
    }

    private boolean join(final ExecutorService pool) {
        pool.shutdown();
        try {
            return pool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            pool.shutdownNow();
            return false;
        }
    }

    private class Action extends RecursiveAction {
        private static final long serialVersionUID = 1L;
        
        private final Box pixels;

        Action(final Box pixels) {
            this.pixels = pixels;
        }

        @Override
        protected void compute() {
            if (pixels.size.x * pixels.size.y < THRESHOLD) {
                renderer.render(pixels);
            } else {
                final List<Action> actions = pixels.split() //
                    .map(Action::new) //
                    .collect(Collectors.toList());
                if (actions.size() > 1) {
                    invokeAll(actions);
                } else {
                    renderer.render(pixels);
                }
            }
        }
    }
}
