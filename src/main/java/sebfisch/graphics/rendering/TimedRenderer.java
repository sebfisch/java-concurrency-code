package sebfisch.graphics.rendering;

import java.time.Duration;
import java.time.Instant;
import sebfisch.graphics.Box;

public class TimedRenderer<R extends Renderer> extends RendererAdapter<R> {
    public TimedRenderer(final R renderer) {
        super(renderer);
    }

    @Override
    public boolean render(final Box pixels) {
        final Instant start = Instant.now();
        final boolean completed = renderer.render(pixels);
        final Duration duration = Duration.between(start, Instant.now());
        final String status = completed ? "Completed" : "Aborted";
        System.out.println(status + " after " + duration);
        return completed;
    }
}
