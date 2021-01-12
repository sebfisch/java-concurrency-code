package sebfisch.coloring;

import java.awt.Color;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ColoredLock implements Comparable<ColoredLock> {
    private final ReadWriteLock lock;
    private final int index;
    private float hue;
    private volatile boolean isReadLocked;
    private volatile boolean isWriteLocked;

    public ColoredLock(final int index, final float hue) {
        lock = new ReentrantReadWriteLock();
        this.index = index;
        setHue(hue);
    }

    public int index() {
        return index;
    }

    public float getHue() {
        return hue;
    }

    public void setHue(final float hue) {
        this.hue = hue;
    }

    public void lock(final boolean writing) {
        (writing ? lock.writeLock() : lock.readLock()).lock();
        isReadLocked = true;
        isWriteLocked = writing;
    }

    public void unlock(final boolean writing) {
        isReadLocked = false;
        isWriteLocked = false;
        (writing ? lock.writeLock() : lock.readLock()).unlock();
    }

    public Color getColor() {
        return Color.getHSBColor(getHue(), getSaturation(), getBrightness());
    }

    public float getSaturation() {
        return isReadLocked ? 0.5f : 1;
    }

    public float getBrightness() {
        return isWriteLocked ? 0 : 1;
    }

    @Override
    public int compareTo(final ColoredLock that) {
        return Integer.compare(this.index, that.index);
    }
}
