package com.nfaralli.particleflow;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ParticlesSurfaceView extends SurfaceView
        implements SurfaceHolder.Callback {

    private static final String TAG = "ParticleFlow";
    private VulkanParticlesRenderer renderer;
    private RenderThread             renderThread;
    private volatile boolean         running = false;

    public ParticlesSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public ParticlesSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        renderer = new VulkanParticlesRenderer(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        running = true;
        renderThread = new RenderThread(holder);
        renderThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        renderer.onSurfaceChanged(w, h);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        try {
            if (renderThread != null) renderThread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        renderer.destroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action     = event.getActionMasked();
        int pointerIdx = event.getActionIndex();
        int pointerId  = event.getPointerId(pointerIdx);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerId < VulkanParticlesRenderer.MAX_TOUCH_POINTS)
                    renderer.setTouchPoint(pointerId, event.getX(pointerIdx), event.getY(pointerIdx));
                break;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    if (id < VulkanParticlesRenderer.MAX_TOUCH_POINTS)
                        renderer.setTouchPoint(id, event.getX(i), event.getY(i));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pointerId < VulkanParticlesRenderer.MAX_TOUCH_POINTS)
                    renderer.releaseTouchPoint(pointerId);
                break;
        }
        return true;
    }

    public VulkanParticlesRenderer getRenderer() { return renderer; }
    public void applySettings() { renderer.applyParameters(); renderer.resetParticles(); }
    public void initTesla369()  { renderer.initTesla369(); }

    private class RenderThread extends Thread {
        private final SurfaceHolder holder;
        private static final long TARGET_FRAME_MS = 16;

        RenderThread(SurfaceHolder holder) {
            this.holder = holder;
            setName("ParticleFlow-Render");
        }

        @Override
        public void run() {
            if (!renderer.initialize(holder.getSurface())) {
                Log.e(TAG, "Failed to initialize Vulkan renderer");
                return;
            }
            while (running) {
                long start = System.currentTimeMillis();
                renderer.drawFrame();
                long sleep = TARGET_FRAME_MS - (System.currentTimeMillis() - start);
                if (sleep > 0) {
                    try { Thread.sleep(sleep); }
                    catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }
        }
    }
}
