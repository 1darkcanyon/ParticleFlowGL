package com.nfaralli.particleflow;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {
    private GLSurfaceView mGLView;
    private ParticlesRendererGL mRenderer;
    private int[] mCount = new int[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(this);
        mRenderer = new ParticlesRendererGL();
        mGLView.setEGLContextClientVersion(3);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(mGLView);
        mGLView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            |View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        mGLView.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_POINTER_DOWN) {
                int ids = 0;
                for (int i = 0; i < event.getPointerCount(); i++) {
                    int id = event.getPointerId(i);
                    if (id < mCount.length) {
                        ids |= (1 << id);
                        mCount[id] = 0;
                        mRenderer.setTouch(id, event.getX(i), event.getY(i));
                    }
                }
                for (int id = 0; id < mCount.length; id++, ids >>= 1) {
                    if ((ids & 1) == 0 && mCount[id]++ >= 3) {
                        mRenderer.setTouch(id, -1f, -1f);
                    }
                }
                mGLView.requestRender();
            }
            return true;
        });
    }

    @Override protected void onPause() { super.onPause(); mGLView.onPause(); }
    @Override protected void onResume() { super.onResume(); mGLView.onResume(); }
}
