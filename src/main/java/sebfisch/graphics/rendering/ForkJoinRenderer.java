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
    public boolean render(final Box box) {
        // TODO [Task 4.1, Threads] use common fork join pool
        final ForkJoinPool pool = new ForkJoinPool();
        fork(pool, box);
        return join(pool);
    }

    // TODO [Task 4.2, Threads] return future
    private void fork(final ForkJoinPool pool, final Box box) {
        pool.execute(new Action(box));
    }

    // TODO [Task 4.3, Threads] accept future as argument instead of pool
    private boolean join(final ExecutorService pool) {
        pool.shutdown();
        try {
            return pool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            pool.shutdownNow();
            return false;
        }
    }

    // TODO [Task 4.4, Threads] wrap custom Interruptible instance
    private class Action extends RecursiveAction {
        private static final long serialVersionUID = 1L;
        
        private final Box box;

        Action(final Box box) {
            this.box = box;
        }

        @Override
        protected void compute() {
            if (box.size.x * box.size.y < THRESHOLD) {
                renderer.render(box);
            } else {
                final List<Action> actions = box.split() //
                    .map(Action::new) //
                    .collect(Collectors.toList());
                if (actions.size() > 1) {
                    invokeAll(actions);
                } else {
                    renderer.render(box);
                }
            }
        }
    }
}
