package sebfisch.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class ThreadPoolTests {

    @Test
    void testSubmittingToAThreadPool() {
        final int threadCount = Runtime.getRuntime().availableProcessors();
        final ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        final Future<Integer> future = pool.submit(() -> 42);

        try {
            assertEquals(42, future.get());
        } catch (InterruptedException | ExecutionException e) {
        }

        assertTrue(future.isDone());

        pool.shutdown();
    }

    @Test
    void testCancellingAFuture() {
        final ExecutorService pool = Executors.newSingleThreadExecutor();
        final List<Boolean> interruptions = new ArrayList<>();
        final Future<?> future = pool.submit(() -> {
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    interruptions.add(null);
                    return;
                }
            }
            interruptions.add(null);
        });

        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {}

        future.cancel(true);
        assertTrue(future.isCancelled());

        boolean cancelled = false;
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
        } catch (CancellationException e) {
            cancelled = true;
        }

        assertTrue(cancelled);
        assertEquals(1, interruptions.size());
    }
}
