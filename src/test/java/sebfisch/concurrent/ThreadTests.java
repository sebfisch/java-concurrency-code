package sebfisch.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class ThreadTests {

    @Test
    void testHowToStartAndJoinThreads() {
        final List<Integer> results = new ArrayList<>();

        final Thread one = new Thread(() -> results.add(1));
        final Thread two = new Thread(() -> results.add(2));

        assertFalse(one.isAlive());
        assertFalse(two.isAlive());

        assertTrue(results.isEmpty());

        one.start();
        two.start();

        // assertTrue(one.isAlive());
        // assertTrue(two.isAlive());

        try {
            one.join();
            two.join();
        } catch (InterruptedException e) {}

        assertFalse(one.isAlive());
        assertFalse(two.isAlive());

        assertEquals(2, results.size());
    }

    @Test
    void testHowToClearInterruptStatus() {
        final Thread thread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        thread.start();
        thread.interrupt();
        
        try {
            thread.join();
        } catch (InterruptedException e) {}

        assertFalse(thread.isInterrupted());
    }
}
