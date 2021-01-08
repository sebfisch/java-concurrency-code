package sebfisch.graphics.rendering;

import java.util.List;
import java.util.stream.Collectors;
import sebfisch.graphics.Box;

public class MultiThreadedRenderer extends RendererAdapter<StreamRenderer> {
    private static final int WAITING_MILLIS = 100;

    public MultiThreadedRenderer() {
        super(new StreamRenderer());
    }

    @Override
    public boolean render(final Box pixels) {
        final List<Thread> threads = pixels.split() //
            .map(part -> (Runnable) () -> renderer.render(part)) //
            .map(Thread::new) //
            .collect(Collectors.toList());
        fork(threads);
        return join(threads);
    }

    private void fork(final List<Thread> threads) {
        threads.forEach(Thread::start);
    }

    private boolean join(final List<Thread> threads) {
        while (threads.stream().anyMatch(Thread::isAlive)) {
            if (Thread.interrupted()) {
                interruptAll(threads);
                return false;
            }
            try {
                Thread.sleep(WAITING_MILLIS);
            } catch (InterruptedException e) {
                interruptAll(threads);
                return false;
            }
        }
        return true;
    }

    private void interruptAll(final List<Thread> threads) {
        threads.forEach(Thread::interrupt);
    }
}
