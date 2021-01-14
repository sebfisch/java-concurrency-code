package sebfisch.concurrent;

@FunctionalInterface
public interface Interruptible {
    boolean wasInterrupted();
}
