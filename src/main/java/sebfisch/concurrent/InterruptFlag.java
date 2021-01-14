package sebfisch.concurrent;

public class InterruptFlag implements Interruptible {

    private boolean wasInterrupted = false;

    @Override
    public boolean wasInterrupted() {
        return wasInterrupted;
    }
    
    public void wasInterrupted(final boolean wasInterrupted) {
        this.wasInterrupted = wasInterrupted;
    }
}
