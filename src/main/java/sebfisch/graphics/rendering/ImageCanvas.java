package sebfisch.graphics.rendering;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import sebfisch.graphics.Image;
import sebfisch.graphics.ImageParams;
import sebfisch.graphics.Pixel;
import sebfisch.graphics.Box;

public class ImageCanvas extends Canvas {
    private static final long serialVersionUID = 1L;
    private static final int PERIOD_MILLIS = 500;

    private final ScheduledExecutorService service;
    private final Renderer renderer;

    private Future<?> repainting;
    private Future<?> rendering;
    private Box pixels;
    private boolean needsUpdate;

    public ImageCanvas(final Renderer renderer) {
        service = Executors.newScheduledThreadPool(2);
        ((ScheduledThreadPoolExecutor) service) //
            .setRemoveOnCancelPolicy(true);
        this.renderer = renderer;
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
    public void update(final Graphics g) {
        paint(g);
    }

    @Override
    public void paint(final Graphics g) {
        maybeRender();
        final Rectangle clip = g.getClipBounds();
        g.drawImage(renderer.getBuffer(), //
            clip.x, clip.y, clip.width, clip.height, null);
    }

    private void maybeRender() {
        updateBuffer();
        updateRaster();
        restartRendering();
        needsUpdate = false;
    }

    private void updateBuffer() {
        final Dimension my = getSize();
        final BufferedImage buffer = renderer.getBuffer();
        if (buffer == null || //
                buffer.getWidth() != my.width || //
                buffer.getHeight() != my.height) {
            renderer.setBuffer(new BufferedImage( //
                my.width, my.height, BufferedImage.TYPE_INT_RGB));
            needsUpdate = true;
        }
    }

    private void updateRaster() {
        if (needsUpdate) {
            final BufferedImage buffer = renderer.getBuffer();
            pixels = new Box( //
                new Pixel(0, 0), new Pixel(buffer.getWidth(), buffer.getHeight()));
        }
    }

    private void restartRendering() {
        if (needsUpdate && pixels != null) {
            stopRendering();
            rendering = service.schedule(this::render, 0, TimeUnit.SECONDS);
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
                this::repaint, 0, PERIOD_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private void pauseRepainting() {
        if (repainting != null && !repainting.isDone()) {
            repainting.cancel(false);
        }
    }
}
