package com.nfaralli.particleflow;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

public class MainActivity extends Activity {
    private GLSurfaceView mGLView;
    private ParticlesRendererGL mRenderer;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(this);
        mRenderer = new ParticlesRendererGL();
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setContentView(mGLView);
        
        mGLView.setOnTouchListener((v, event) -> {
            float[] touches = {event.getX(), event.getY()};
            mRenderer.updateTouches(touches);
            return true;
        });
    }
    
    @Override protected void onPause() { super.onPause(); mGLView.onPause(); }
    @Override protected void onResume() { super.onResume(); mGLView.onResume(); }
}
