package sebfisch.graphics.rendering;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import sebfisch.graphics.Box;
import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;
import sebfisch.graphics.Pixel;
import sebfisch.graphics.PixelRaster;

public class ImageCanvas extends Canvas implements PixelRaster {
    private static final long serialVersionUID = 1L;

    private final ScheduledExecutorService service;
    private final Renderer renderer;

    private Future<?> repainting;
    private Future<?> rendering;
    private BufferedImage buffer;
    private Box pixels;
    private boolean needsUpdate;

    public ImageCanvas(final Renderer renderer) {
        service = Executors.newScheduledThreadPool(2);
        ((ScheduledThreadPoolExecutor) service) //
            .setRemoveOnCancelPolicy(true);
        this.renderer = renderer;
        renderer.setRaster(this);
    }

    public void rerender() {
        this.needsUpdate = true;
        repaint();
    }

    public void setImage(final Image image) {
        renderer.setImage(image);
        this.needsUpdate = true;
    }

    public void setParams(final ImageParams params) {
        renderer.setParams(params);
        this.needsUpdate = true;
    }

    @Override
    public synchronized void setPixelColor(final Pixel pixel, final Color color) {
        buffer.setRGB(pixel.x, pixel.y, color.getRGB());
    }

    @Override
    public void update(final Graphics g) {
        paint(g);
    }

    @Override
    public void paint(final Graphics g) {
        maybeRender();
        final Rectangle clip = g.getClipBounds();
        synchronized (this) {
            g.drawImage(buffer, //
                clip.x, clip.y, clip.width, clip.height, null);
        }
    }

    private void maybeRender() {
        updateBuffer();
        updatePixels();
        restartRendering();
        needsUpdate = false;
    }

    private void updateBuffer() {
        if (buffer == null || //
                buffer.getWidth() != getWidth() || //
                buffer.getHeight() != getHeight()) {
            buffer = new BufferedImage( //
                getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            needsUpdate = true;
        }
    }

    private void updatePixels() {
        if (needsUpdate) {
            pixels = new Box( //
                new Pixel(0, 0), new Pixel(getWidth(), getHeight()));
        }
    }

    private void restartRendering() {
        if (needsUpdate && pixels != null) {
            stopRendering();
            rendering = service.submit(this::render);
        }
    }

    private void stopRendering() {
        if (rendering != null && !rendering.isDone()) {
            rendering.cancel(true);
        }
    }

    private void render() {
        resumeRepainting();
        if (renderer.render(pixels)) {
            pauseRepainting();
            repaint();
        }
    }

    private void resumeRepainting() {
        if (repainting == null || repainting.isDone()) {
            repainting = service.scheduleAtFixedRate( //
                this::repaint, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void pauseRepainting() {
        if (repainting != null && !repainting.isDone()) {
            repainting.cancel(false);
        }
    }
}
