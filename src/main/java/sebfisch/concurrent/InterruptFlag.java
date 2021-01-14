package sebfisch.concurrent;

public class InterruptFlag implements Interruptible {

    private volatile boolean wasInterrupted = false;

    @Override
    public boolean wasInterrupted() {
        return wasInterrupted;
    }
    
    public void wasInterrupted(final boolean wasInterrupted) {
        this.wasInterrupted = wasInterrupted;
    }
}
