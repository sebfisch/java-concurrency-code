package sebfisch.graphics.rendering;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import sebfisch.concurrent.InterruptFlag;
import sebfisch.concurrent.Interruptible;
import sebfisch.graphics.Box;

public class ThreadPoolRenderer 
        extends RendererAdapter<StreamRenderer> {
    public ThreadPoolRenderer() {
        super(new StreamRenderer());
    }

    @Override
    public boolean render(final Box pixels) {
        final InterruptFlag self = new InterruptFlag();
        return join(fork(ForkJoinPool.commonPool(), pixels, self), self);
    }

    private List<Future<?>> fork(final ExecutorService pool, //
            final Box pixels, final Interruptible origin) {
        return pixels.split() //
            .map(part -> (Runnable) () -> renderer.render(part, origin)) //
            .map(pool::submit) //
            .collect(Collectors.toList());
    }

    private boolean join(final List<Future<?>> futures, //
            final InterruptFlag self) {
        return futures.stream().map(future -> {
            try {
                future.get();
                return true;
            } catch (InterruptedException | ExecutionException e) {
                self.wasInterrupted(true);
                return false;
            }
        }).allMatch(ok -> ok);
    }
}
