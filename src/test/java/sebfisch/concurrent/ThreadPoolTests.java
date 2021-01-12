package sebfisch.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

public class ThreadPoolTests {

    @Test
    void testSubmittingToAThreadPool() {
        final int threadCount = Runtime.getRuntime().availableProcessors();
        final ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        final Future<Integer> future = pool.submit(() -> 42);

        try {
            assertEquals(42, future.get());
        } catch (InterruptedException | ExecutionException e) {}

        assertTrue(future.isDone());

        pool.shutdown();
    }
}
