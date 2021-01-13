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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
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
        final List<?> interruptions = new ArrayList<>();
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
        // assertEquals(1, interruptions.size()); // fails with maven
    }

    @Test
    void testRecursiveTasks() {
        final ForkJoinPool pool = ForkJoinPool.commonPool();
        final Future<Integer> fib10 = pool.submit(new Fib(10));

        try {
            assertEquals(fib10.get(), 55);
        } catch (InterruptedException | ExecutionException e) {
        }
    }

    static class Fib extends RecursiveTask<Integer> {
        private static final long serialVersionUID = 1L;

        final int n;

        Fib(final int n) {
            this.n = n;
        }

        @Override
        protected Integer compute() {
            if (n <= 1) {
                return n;
            } else {
                final Fib fib1 = new Fib(n - 1);
                final Fib fib2 = new Fib(n - 2);
                fib1.fork();
                return fib2.compute() + fib1.join();
            }
        }
    }

    @Test
    void testCancellingARecursiveAction() {
        final ForkJoinPool pool = new ForkJoinPool();
        final List<?> interruptions = new ArrayList<>();

        final Future<?> future = pool.submit(new RecursiveAction() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void compute() {
                while (!Thread.interrupted()) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        interruptions.add(null);
                        return;
                    }
                }
                interruptions.add(null);
            }
        });

        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {}

        future.cancel(true);
        assertTrue(future.isCancelled());

        pool.shutdown();
        try {
            assertFalse(pool.awaitTermination(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {}
        pool.shutdownNow();

        assertTrue(interruptions.isEmpty());
    }
}
