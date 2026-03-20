import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.*;

public class SimpleParticleFlow extends Activity {
    CanvasView canvasView;
    float[] particles = new float[8192*4];
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canvasView = new CanvasView(this);
        setContentView(canvasView);
        for(int i=0; i<8192; i++) {
            particles[i*4] = getResources().getDisplayMetrics().widthPixels * 0.5f;
            particles[i*4+1] = getResources().getDisplayMetrics().heightPixels * 0.5f;
            particles[i*4+2] = 0; particles[i*4+3] = 0;
        }
    }
    
    class CanvasView extends View {
        Paint paint = new Paint();
        float touchX=0, touchY=0;
        public CanvasView(Activity activity) { super(activity); }
        
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.rgb(10,10,20));
            for(int i=0; i<8192; i++) {
                float x = particles[i*4], y = particles[i*4+1];
                float dx = particles[i*4+2], dy = particles[i*4+3];
                float diffx = touchX-x, diffy = touchY-y;
                float distsq = diffx*diffx + diffy*diffy + 10;
                dx += 50000 * diffx / distsq;
                dy += 50000 * diffy / distsq;
                x += dx; y += dy; dx *= 0.96f; dy *= 0.96f;
                if(x<0) x = getWidth(); if(x>getWidth()) x=0;
                if(y<0) y = getHeight(); if(y>getHeight()) y=0;
                float speed = (float)Math.log(dx*dx+dy*dy+1)/4.5f;
                paint.setColor(Color.rgb((int)(speed*2*255),(int)(speed*255),(int)(speed/2*255)));
                canvas.drawCircle(x,y,2,paint);
                particles[i*4] = x; particles[i*4+1] = y;
                particles[i*4+2] = dx; particles[i*4+3] = dy;
            }
            invalidate();
        }
        
        @Override public boolean onTouchEvent(MotionEvent event) {
            touchX = event.getX(); touchY = event.getY();
            return true;
        }
    }
}
