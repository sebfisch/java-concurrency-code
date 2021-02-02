package sebfisch.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

public class CompletableFutureTests {

    @Test
    void testCreatingCompletedFuture() {
        final CompletableFuture<Integer> future = //
            CompletableFuture.completedFuture(42);
        assertEquals(42, future.join());
    }

    @Test
    void testCompletingFutureAsynchronously() {
        final CompletableFuture<Integer> future = //
            CompletableFuture.supplyAsync(() -> 42);
        assertEquals(42, future.join());
    } 
    
    @Test
    void testCompletingFutureManually() {
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        Executors.newSingleThreadExecutor().execute(() -> future.complete(42));
        assertEquals(42, future.join());
    }

    @Test
    void testThatThenApplyIsLikeMap() {
        final CompletableFuture<String> stringFuture = //
            CompletableFuture.completedFuture("hello");
        final CompletableFuture<Integer> intFuture = //
            stringFuture.thenApply(String::length);
        assertEquals(5, intFuture.join());
    }

    @Test
    void testThatThenComposeIsLikeFlatMap() {
        final CompletableFuture<String> stringFuture = //
            CompletableFuture.completedFuture("ha");
        final CompletableFuture<String> combinedFuture = //
            stringFuture.thenCompose(string -> //
            CompletableFuture.completedFuture(string+string));
        assertEquals("haha", combinedFuture.join());
    }

    // TODO [Task 6, Threads] write tests involving exceptions
}
