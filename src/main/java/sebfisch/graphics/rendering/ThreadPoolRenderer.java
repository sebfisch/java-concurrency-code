package sebfisch.graphics.rendering;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import sebfisch.fractals.FractalApp;
import sebfisch.graphics.Box;

public class ThreadPoolRenderer extends RendererAdapter {
    private static ExecutorService pool;

    public ThreadPoolRenderer() {
        super(new SingleThreadedRenderer());
    }

    @Override
    public boolean render(final Box pixels) {
        if (pool == null) {
            pool = Executors.newFixedThreadPool(FractalApp.THREADS);
        }
        return join(fork(pool, pixels));
    }

    private List<Future<?>> fork(final ExecutorService pool, final Box pixels) {
        return pixels.split() //
            .map(part -> (Runnable) () -> renderer.render(part)) //
            .map(pool::submit) //
            .collect(Collectors.toList());
    }

    private boolean join(final List<Future<?>> futures) {
        return futures.stream().map(future -> {
            try {
                future.get();
                return true;
            } catch (InterruptedException|ExecutionException e) {
                cancelAll(futures);
                return false;
            }
        }).allMatch(ok -> ok);
    }

    private void cancelAll(final List<Future<?>> futures) {
        futures.forEach(future -> future.cancel(true));
    }
}
