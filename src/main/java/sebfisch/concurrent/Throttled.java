package sebfisch.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Throttled implements Runnable {
    private ScheduledExecutorService service = //
        Executors.newSingleThreadScheduledExecutor();
    private volatile boolean delayed = false;
    
    private final Runnable command;
    private final int delay;
    private final TimeUnit unit;

    public Throttled(final Runnable command) {
        this(command, 1, TimeUnit.SECONDS);
    }

    public Throttled(final Runnable command, final int delay, final TimeUnit unit) {
        this.command = command;
        this.delay = delay;
        this.unit = unit;
    }

    @Override
    public void run() {
        if (!delayed) {
            delayed = true;
            service.schedule(() -> {
                command.run();
                delayed = false;
            }, delay, unit);
        }
    }
}
