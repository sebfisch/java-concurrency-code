package sebfisch.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class ClassicTests {

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
        } catch (InterruptedException e) {
        }

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
        } catch (InterruptedException e) {
        }

        assertFalse(thread.isInterrupted());
    }

    private static class Count {
        public int value = 0;
    }

    private void randomSleep() {
        try {
            TimeUnit.MILLISECONDS.sleep( //
                    ThreadLocalRandom.current().nextInt(10) //
            );
        } catch (InterruptedException e) {
        }
    }

    @Test
    void testThreadInterference() {
        final Count count = new Count();

        final Runnable inc = () -> {
            final int val = count.value;
            randomSleep();
            count.value = val + 1;
        };

        final Runnable dec = () -> {
            final int val = count.value;
            randomSleep();
            count.value = val - 1;
        };

        final IntSupplier race = () -> {
            final Thread incThread = new Thread(inc);
            final Thread decThread = new Thread(dec);
            incThread.start();
            decThread.start();
            try {
                incThread.join();
                decThread.join();
            } catch (InterruptedException e) {
            }
            return count.value;
        };

        assertTrue(IntStream.generate(race).limit(100).anyMatch(n -> n != 0));
    }

    @Test
    void testIntrinsicLock() {
        final Count count = new Count();

        final Runnable inc = () -> {
            synchronized (count) {
                final int val = count.value;
                randomSleep();
                count.value = val + 1;
            }
        };

        final Runnable dec = () -> {
            synchronized (count) {
                final int val = count.value;
                randomSleep();
                count.value = val - 1;
            }
        };

        final IntSupplier race = () -> {
            final Thread incThread = new Thread(inc);
            final Thread decThread = new Thread(dec);
            incThread.start();
            decThread.start();
            try {
                incThread.join();
                decThread.join();
            } catch (InterruptedException e) {}
            return count.value;
        };

        assertTrue(IntStream.generate(race).limit(100).allMatch(n -> n == 0));
    }

    @Test
    void testIntrinsicLockRelease() {
        final Count count = new Count();

        final Runnable consume = () -> {
            synchronized (count) {
                while (count.value == 0) {
                    try {
                        count.wait();
                    } catch (InterruptedException e) {}
                }
                count.value = 0;
                count.notifyAll();
            }
        };

        final Runnable produce = () -> {
            synchronized (count) {
                while (count.value != 0) {
                    try {
                        count.wait();
                    } catch (InterruptedException e) {}
                }
                count.value = 10;
                count.notifyAll();
            }
        };

        final IntSupplier handshake = () -> {
            final Thread consumer = new Thread(consume);
            final Thread producer = new Thread(produce);
            consumer.start();
            producer.start();
            try {
                consumer.join();
                producer.join();
            } catch (InterruptedException e) {}
            return count.value;
        };

        assertTrue(IntStream.generate(handshake).limit(100).allMatch(n -> n == 0));
    }

    // TODO [Task 5, Threads] write new test suite for Lock and Condition
}
