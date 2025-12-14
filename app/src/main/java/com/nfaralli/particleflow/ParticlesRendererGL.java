package com.nfaralli.particleflow;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class ParticlesRendererGL implements GLSurfaceView.Renderer {
    private static final int NUM_PARTICLES = 16384;
    private static final int FLOAT_SIZE = 4;
    
    private FloatBuffer positionBuffer, deltaBuffer;
    private int program;
    private int positionHandle, deltaHandle;
    private float width = 1080, height = 1920;
    private float[] touches = {-999, -999};
    
    private float slowHue = 0f, fastHue = 0.5f, slowSat = 1f, fastSat = 1f;
    private float slowVal = 1f, fastVal = 1f, f01Attraction = 5000f, f01Drag = 0.96f;
    
    private final String vertexShader = 
        "#version 300 es\n" +
        "precision highp float;\n" +
        "layout(location=0) in vec2 a_Position;\n" +
        "layout(location=1) in vec2 a_Delta;\n" +
        "uniform vec2 u_Touch;\n" +
        "uniform vec2 u_Size;\n" +
        "uniform float u_Attraction;\n" +
        "uniform float u_Drag;\n" +
        "out vec2 v_Delta;\n" +
        "void main() {\n" +
        "    vec2 pos = a_Position;\n" +
        "    vec2 delta = a_Delta;\n" +
        "    vec2 diff = u_Touch - pos;\n" +
        "    float distSq = dot(diff, diff);\n" +
        "    if(distSq > 0.1) {\n" +
        "        delta += (u_Attraction / distSq) * diff;\n" +
        "    }\n" +
        "    pos += delta;\n" +
        "    delta *= u_Drag;\n" +
        "    if(pos.x < 0.0) pos.x = u_Size.x;\n" +
        "    if(pos.x > u_Size.x) pos.x = 0.0;\n" +
        "    if(pos.y < 0.0) pos.y = u_Size.y;\n" +
        "    if(pos.y > u_Size.y) pos.y = 0.0;\n" +
        "    gl_Position = vec4((pos / u_Size) * 2.0 - 1.0, 0.0, 1.0);\n" +
        "    gl_PointSize = 4.0;\n" +
        "    v_Delta = delta;\n" +
        "}";

    private final String fragmentShader = 
        "#version 300 es\n" +
        "precision highp float;\n" +
        "in vec2 v_Delta;\n" +
        "uniform float u_slowHue, u_fastHue;\n" +
        "uniform float u_slowSat, u_fastSat, u_slowVal, u_fastVal;\n" +
        "out vec4 fragColor;\n" +
        "float getSpeedCoef(vec2 v) {\n" +
        "    return clamp(log(dot(v,v) + 1.0) / 4.5, 0.0, 1.0);\n" +
        "}\n" +
        "vec4 hsv2rgba(float h, float s, float v) {\n" +
        "    float h6 = fract(h * 6.0);\n" +
        "    float r = 1.0 - abs(h6 - 3.0) + 1.0;\n" +
        "    float g = 2.0 - abs(h6 - 2.0);\n" +
        "    float b = 1.0 - abs(h6 - 1.0);\n" +
        "    float coef = v * s;\n" +
        "    return vec4(v - coef * r, v - coef * g, v - coef * b, 1.0);\n" +
        "}\n" +
        "void main() {\n" +
        "    float coef = getSpeedCoef(v_Delta);\n" +
        "    float hue = mix(u_slowHue, u_fastHue, coef);\n" +
        "    float sat = mix(u_slowSat, u_fastSat, coef);\n" +
        "    float val = mix(u_slowVal, u_fastVal, coef);\n" +
        "    fragColor = hsv2rgba(hue, sat, val);\n" +
        "}";

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.2f, 1.0f);
        program = createProgram(vertexShader, fragmentShader);
        initParticles();
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        return program;
    }

    private int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private void initParticles() {
        Random rand = new Random();
        float[] positions = new float[NUM_PARTICLES * 2];
        float[] deltas = new float[NUM_PARTICLES * 2];
        float radius = (float) Math.sqrt(width * width + height * height) / 2;

        for (int i = 0; i < NUM_PARTICLES; i++) {
            float r = radius * (float) Math.sqrt(rand.nextFloat());
            float theta = (float) (rand.nextFloat() * 6.28318530718f);
            positions[i*2] = (width/2) + r * (float)Math.cos(theta);
            positions[i*2+1] = (height/2) + r * (float)Math.sin(theta);
        }
        
        positionBuffer = ByteBuffer.allocateDirect(positions.length * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(positions);
        deltaBuffer = ByteBuffer.allocateDirect(deltas.length * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(deltas);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);
        
        positionHandle = GLES20.glGetAttribLocation(program, "a_Position");
        deltaHandle = GLES20.glGetAttribLocation(program, "a_Delta");
        
        GLES20.glEnableVertexAttribArray(positionHandle);
        positionBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, positionBuffer);
        
        GLES20.glEnableVertexAttribArray(deltaHandle);
        deltaBuffer.position(0);
        GLES20.glVertexAttribPointer(deltaHandle, 2, GLES20.GL_FLOAT, false, 0, deltaBuffer);
        
        GLES20.glUniform2f(GLES20.glGetUniformLocation(program, "u_Touch"), touches[0], touches[1]);
        GLES20.glUniform2f(GLES20.glGetUniformLocation(program, "u_Size"), width, height);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_Attraction"), f01Attraction);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_Drag"), f01Drag);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_slowHue"), slowHue);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_fastHue"), fastHue);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_slowSat"), slowSat);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_fastSat"), fastSat);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_slowVal"), slowVal);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "u_fastVal"), fastVal);
        
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, NUM_PARTICLES);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        GLES20.glViewport(0, 0, w, h);
        width = w; height = h;
    }

    public void updateTouches(float[] t) {
        touches[0] = t[0]; touches[1] = t[1];
    }
}
