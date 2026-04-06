package com.nfaralli.particleflow;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * SettingsView.java
 * Particle Flow — Vulkan Migration
 * NEXUS Engineering — Canyon (Kaneon)
 *
 * Complete settings dialog matching original app behavior.
 * Controls: particles, size, touch points, colors, drag, attraction.
 * Includes color gradient preview for slow→fast color transition.
 */
public class SettingsView extends Dialog {

    // ── STATE ─────────────────────────────────────────────────
    private VulkanParticlesRenderer renderer;
    private GradientPreviewView      gradientPreview;

    // Working copies — only applied on OK
    private int   numParticles;
    private int   particleSize;
    private int   numTouchPoints;
    private int   bgR, bgG, bgB;
    private int   slowR, slowG, slowB;
    private int   fastR, fastG, fastB;
    private int   hueDir;
    private int   attractionPct;
    private int   dragPct;

    // ── CONSTRUCTOR ───────────────────────────────────────────

    public SettingsView(Context context, VulkanParticlesRenderer renderer) {
        super(context);
        this.renderer = renderer;
        loadFromRenderer();
    }

    private void loadFromRenderer() {
        numParticles  = renderer.getNumParticles();
        particleSize  = renderer.getParticleSize();
        numTouchPoints= renderer.getNumTouchPoints();
        bgR           = renderer.getBgR();
        bgG           = renderer.getBgG();
        bgB           = renderer.getBgB();
        slowR         = renderer.getSlowR();
        slowG         = renderer.getSlowG();
        slowB         = renderer.getSlowB();
        fastR         = renderer.getFastR();
        fastG         = renderer.getFastG();
        fastB         = renderer.getFastB();
        hueDir        = renderer.getHueDir();
        attractionPct = renderer.getAttractionPct();
        dragPct       = renderer.getDragPct();
    }

    // ── DIALOG CREATION ───────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Context ctx = getContext();
        int pad     = dp(16);
        int padSm   = dp(8);
        int padXs   = dp(4);

        // Root scroll view
        ScrollView scroll = new ScrollView(ctx);
        scroll.setBackgroundColor(Color.parseColor("#1a1a2e"));

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        // ── TITLE ─────────────────────────────────────────────
        TextView title = new TextView(ctx);
        title.setText("PARTICLE FLOW SETTINGS");
        title.setTextColor(Color.parseColor("#00e5ff"));
        title.setTextSize(14);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setLetterSpacing(0.1f);
        title.setPadding(0, 0, 0, pad);
        root.addView(title);

        // ── NUMBER OF PARTICLES ───────────────────────────────
        root.addView(makeLabel(ctx, "Number of Particles (max 1,000,000)"));
        EditText etParticles = makeEditInt(ctx, numParticles);
        etParticles.addTextChangedListener(new IntWatcher(1, 1000000) {
            @Override void onValue(int v) { numParticles = v; }
        });
        root.addView(etParticles);

        // ── PARTICLE SIZE ─────────────────────────────────────
        root.addView(makeLabel(ctx, "Particle Size (px)"));
        EditText etSize = makeEditInt(ctx, particleSize);
        etSize.addTextChangedListener(new IntWatcher(1, 10) {
            @Override void onValue(int v) { particleSize = v; }
        });
        root.addView(etSize);

        // ── MAX TOUCH POINTS ──────────────────────────────────
        root.addView(makeLabel(ctx, "Max Attraction Points (max 16)"));
        EditText etTouch = makeEditInt(ctx, numTouchPoints);
        etTouch.addTextChangedListener(new IntWatcher(1, 16) {
            @Override void onValue(int v) { numTouchPoints = v; }
        });
        root.addView(etTouch);

        root.addView(makeDivider(ctx));

        // ── BACKGROUND COLOR ──────────────────────────────────
        root.addView(makeLabel(ctx, "Background Color (R / G / B)"));
        LinearLayout bgRow = makeColorRow(ctx);

        EditText etBgR = makeColorEditInt(ctx, bgR);
        EditText etBgG = makeColorEditInt(ctx, bgG);
        EditText etBgB = makeColorEditInt(ctx, bgB);

        etBgR.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){bgR=v;} });
        etBgG.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){bgG=v;} });
        etBgB.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){bgB=v;} });

        bgRow.addView(etBgR);
        bgRow.addView(etBgG);
        bgRow.addView(etBgB);
        root.addView(bgRow);

        root.addView(makeDivider(ctx));

        // ── SLOW COLOR ────────────────────────────────────────
        root.addView(makeLabel(ctx, "Slow Particle Color (R / G / B)"));
        LinearLayout slowRow = makeColorRow(ctx);

        EditText etSlowR = makeColorEditInt(ctx, slowR);
        EditText etSlowG = makeColorEditInt(ctx, slowG);
        EditText etSlowB = makeColorEditInt(ctx, slowB);

        etSlowR.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){ slowR=v; updateGradient(); } });
        etSlowG.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){ slowG=v; updateGradient(); } });
        etSlowB.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){ slowB=v; updateGradient(); } });

        slowRow.addView(etSlowR);
        slowRow.addView(etSlowG);
        slowRow.addView(etSlowB);
        root.addView(slowRow);

        // ── FAST COLOR ────────────────────────────────────────
        root.addView(makeLabel(ctx, "Fast Particle Color (R / G / B)"));
        LinearLayout fastRow = makeColorRow(ctx);

        EditText etFastR = makeColorEditInt(ctx, fastR);
        EditText etFastG = makeColorEditInt(ctx, fastG);
        EditText etFastB = makeColorEditInt(ctx, fastB);

        etFastR.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){ fastR=v; updateGradient(); } });
        etFastG.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){ fastG=v; updateGradient(); } });
        etFastB.addTextChangedListener(new IntWatcher(0,255){ @Override void onValue(int v){ fastB=v; updateGradient(); } });

        fastRow.addView(etFastR);
        fastRow.addView(etFastG);
        fastRow.addView(etFastB);
        root.addView(fastRow);

        // ── GRADIENT PREVIEW ──────────────────────────────────
        root.addView(makeLabel(ctx, "Color Gradient Preview (Slow → Fast)"));
        gradientPreview = new GradientPreviewView(ctx);
        LinearLayout.LayoutParams gpParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(32));
        gpParams.bottomMargin = padSm;
        gradientPreview.setLayoutParams(gpParams);
        updateGradient();
        root.addView(gradientPreview);

        // ── HUE DIRECTION ─────────────────────────────────────
        root.addView(makeLabel(ctx, "Hue Direction"));
        Spinner hueSpinner = new Spinner(ctx);
        ArrayAdapter<String> hueAdapter = new ArrayAdapter<>(ctx,
            android.R.layout.simple_spinner_item,
            new String[]{"Clockwise", "Counterclockwise"});
        hueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hueSpinner.setAdapter(hueAdapter);
        hueSpinner.setSelection(hueDir);
        hueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                hueDir = pos;
                updateGradient();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        spinnerParams.bottomMargin = padSm;
        hueSpinner.setLayoutParams(spinnerParams);
        root.addView(hueSpinner);

        root.addView(makeDivider(ctx));

        // ── ATTRACTION ────────────────────────────────────────
        root.addView(makeLabel(ctx, "Attraction Coefficient (0–500)"));
        EditText etAttract = makeEditInt(ctx, attractionPct);
        etAttract.addTextChangedListener(new IntWatcher(0, 500) {
            @Override void onValue(int v) { attractionPct = v; }
        });
        root.addView(etAttract);

        // ── DRAG ──────────────────────────────────────────────
        root.addView(makeLabel(ctx, "Drag (0=none, 999=max)"));
        EditText etDrag = makeEditInt(ctx, dragPct);
        etDrag.addTextChangedListener(new IntWatcher(0, 999) {
            @Override void onValue(int v) { dragPct = v; }
        });
        root.addView(etDrag);

        root.addView(makeDivider(ctx));

        // ── BUTTONS ROW ───────────────────────────────────────
        LinearLayout btnRow = new LinearLayout(ctx);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, padSm, 0, 0);

        Button resetBtn = makeButton(ctx, "RESET", Color.parseColor("#444455"));
        resetBtn.setOnClickListener(v -> resetToDefaults(
            etParticles, etSize, etTouch,
            etBgR, etBgG, etBgB,
            etSlowR, etSlowG, etSlowB,
            etFastR, etFastG, etFastB,
            etAttract, etDrag,
            hueSpinner
        ));
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        resetParams.rightMargin = padSm;
        resetBtn.setLayoutParams(resetParams);
        btnRow.addView(resetBtn);

        Button cancelBtn = makeButton(ctx, "CANCEL", Color.parseColor("#444455"));
        cancelBtn.setOnClickListener(v -> dismiss());
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cancelParams.rightMargin = padSm;
        cancelBtn.setLayoutParams(cancelParams);
        btnRow.addView(cancelBtn);

        Button okBtn = makeButton(ctx, "APPLY", Color.parseColor("#00e5ff"));
        okBtn.setOnClickListener(v -> applyAndDismiss());
        LinearLayout.LayoutParams okParams = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        okBtn.setLayoutParams(okParams);
        btnRow.addView(okBtn);

        root.addView(btnRow);

        scroll.addView(root);
        setContentView(scroll);
    }

    // ── ACTIONS ───────────────────────────────────────────────

    private void applyAndDismiss() {
        renderer.setNumParticles(numParticles);
        renderer.setParticleSize(particleSize);
        renderer.setNumTouchPoints(numTouchPoints);
        renderer.setBgColor(bgR, bgG, bgB);
        renderer.setSlowColor(slowR, slowG, slowB);
        renderer.setFastColor(fastR, fastG, fastB);
        renderer.setHueDir(hueDir);
        renderer.setAttractionPct(attractionPct);
        renderer.setDragPct(dragPct);
        renderer.savePreferences();
        renderer.applyParameters();
        renderer.resetParticles();
        dismiss();
    }

    private void resetToDefaults(
        EditText etParticles, EditText etSize, EditText etTouch,
        EditText etBgR, EditText etBgG, EditText etBgB,
        EditText etSlowR, EditText etSlowG, EditText etSlowB,
        EditText etFastR, EditText etFastG, EditText etFastB,
        EditText etAttract, EditText etDrag,
        Spinner hueSpinner)
    {
        numParticles   = VulkanParticlesRenderer.DEFAULT_NUM_PARTICLES;
        particleSize   = VulkanParticlesRenderer.DEFAULT_PARTICLE_SIZE;
        numTouchPoints = VulkanParticlesRenderer.DEFAULT_NUM_TOUCH;
        bgR  = VulkanParticlesRenderer.DEFAULT_BG_R;
        bgG  = VulkanParticlesRenderer.DEFAULT_BG_G;
        bgB  = VulkanParticlesRenderer.DEFAULT_BG_B;
        slowR= VulkanParticlesRenderer.DEFAULT_SLOW_R;
        slowG= VulkanParticlesRenderer.DEFAULT_SLOW_G;
        slowB= VulkanParticlesRenderer.DEFAULT_SLOW_B;
        fastR= VulkanParticlesRenderer.DEFAULT_FAST_R;
        fastG= VulkanParticlesRenderer.DEFAULT_FAST_G;
        fastB= VulkanParticlesRenderer.DEFAULT_FAST_B;
        hueDir       = VulkanParticlesRenderer.DEFAULT_HUE_DIR;
        attractionPct= VulkanParticlesRenderer.DEFAULT_ATTRACT;
        dragPct      = VulkanParticlesRenderer.DEFAULT_DRAG;

        etParticles.setText(String.valueOf(numParticles));
        etSize.setText(String.valueOf(particleSize));
        etTouch.setText(String.valueOf(numTouchPoints));
        etBgR.setText(String.valueOf(bgR));
        etBgG.setText(String.valueOf(bgG));
        etBgB.setText(String.valueOf(bgB));
        etSlowR.setText(String.valueOf(slowR));
        etSlowG.setText(String.valueOf(slowG));
        etSlowB.setText(String.valueOf(slowB));
        etFastR.setText(String.valueOf(fastR));
        etFastG.setText(String.valueOf(fastG));
        etFastB.setText(String.valueOf(fastB));
        etAttract.setText(String.valueOf(attractionPct));
        etDrag.setText(String.valueOf(dragPct));
        hueSpinner.setSelection(hueDir);
        updateGradient();
    }

    private void updateGradient() {
        if (gradientPreview == null) return;
        // Build gradient colors from current slow/fast settings
        int steps = 64;
        int[] colors = new int[steps];
        for (int i = 0; i < steps; i++) {
            float t = i / (float)(steps - 1);
            colors[i] = computeGradientColor(t);
        }
        gradientPreview.setColors(colors);
        gradientPreview.invalidate();
    }

    private int computeGradientColor(float t) {
        float[] slowHSV = VulkanParticlesRenderer.rgbToHSV(slowR, slowG, slowB);
        float[] fastHSV = VulkanParticlesRenderer.rgbToHSV(fastR, fastG, fastB);

        float h, s, v;
        if (hueDir == 0) {
            float dh = fastHSV[0] - slowHSV[0];
            if (dh < 0) dh += 1.0f;
            h = slowHSV[0] + dh * t;
        } else {
            float dh = slowHSV[0] - fastHSV[0];
            if (dh < 0) dh += 1.0f;
            h = slowHSV[0] - dh * t;
        }
        h = ((h % 1.0f) + 1.0f) % 1.0f;
        s = slowHSV[1] + (fastHSV[1] - slowHSV[1]) * t;
        v = slowHSV[2] + (fastHSV[2] - slowHSV[2]) * t;

        float[] rgb = VulkanParticlesRenderer.hsvToRGB(h, s, v);
        return Color.rgb(
            Math.min(255, Math.max(0, (int)(rgb[0] * 255))),
            Math.min(255, Math.max(0, (int)(rgb[1] * 255))),
            Math.min(255, Math.max(0, (int)(rgb[2] * 255)))
        );
    }

    // ── GRADIENT PREVIEW VIEW ─────────────────────────────────

    /**
     * Custom view that renders the slow→fast color gradient.
     * Matches original app color preview box behavior.
     */
    private static class GradientPreviewView extends View {

        private int[]  colors = new int[]{Color.BLUE, Color.RED};
        private Paint  paint  = new Paint();

        GradientPreviewView(Context ctx) {
            super(ctx);
        }

        void setColors(int[] colors) {
            this.colors = colors;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (getWidth() == 0 || colors == null || colors.length < 2) return;

            LinearGradient gradient = new LinearGradient(
                0, 0, getWidth(), 0,
                colors, null,
                Shader.TileMode.CLAMP
            );
            paint.setShader(gradient);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            paint.setShader(null);

            // Border
            paint.setColor(Color.parseColor("#00e5ff"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            canvas.drawRect(0, 0, getWidth() - 1, getHeight() - 1, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    // ── UI HELPERS ────────────────────────────────────────────

    private TextView makeLabel(Context ctx, String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#a0a0b0"));
        tv.setTextSize(11);
        tv.setPadding(0, dp(8), 0, dp(2));
        return tv;
    }

    private EditText makeEditInt(Context ctx, int value) {
        EditText et = new EditText(ctx);
        et.setText(String.valueOf(value));
        et.setTextColor(Color.WHITE);
        et.setHintTextColor(Color.GRAY);
        et.setBackgroundColor(Color.parseColor("#2a2a3e"));
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        et.setPadding(dp(8), dp(6), dp(8), dp(6));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = dp(4);
        et.setLayoutParams(p);
        return et;
    }

    /** EditText sized for 3-column color rows (equal weight, 4dp right gap). */
    private EditText makeColorEditInt(Context ctx, int value) {
        EditText et = makeEditInt(ctx, value);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.bottomMargin  = dp(4);
        p.setMarginEnd(dp(4));
        et.setLayoutParams(p);
        return et;
    }

    private LinearLayout makeColorRow(Context ctx) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = dp(4);
        row.setLayoutParams(p);
        return row;
    }

    private Button makeButton(Context ctx, String text, int bgColor) {
        Button btn = new Button(ctx);
        btn.setText(text);
        btn.setBackgroundColor(bgColor);
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(11);
        btn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        btn.setPadding(dp(8), dp(8), dp(8), dp(8));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = dp(4);
        btn.setLayoutParams(p);
        return btn;
    }

    private View makeDivider(Context ctx) {
        View v = new View(ctx);
        v.setBackgroundColor(Color.parseColor("#333355"));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        p.topMargin    = dp(8);
        p.bottomMargin = dp(8);
        v.setLayoutParams(p);
        return v;
    }

    private int dp(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int)(dp * density + 0.5f);
    }

    // ── INT WATCHER ───────────────────────────────────────────

    /**
     * TextWatcher that parses integer input and clamps to range.
     * Used for all numeric settings fields.
     */
    private abstract static class IntWatcher implements TextWatcher {
        private final int min, max;
        IntWatcher(int min, int max) { this.min = min; this.max = max; }

        abstract void onValue(int v);

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            try {
                int v = Integer.parseInt(s.toString());
                onValue(Math.min(max, Math.max(min, v)));
            } catch (NumberFormatException ignored) {}
        }
    }

    // Override color row children to be equal weight
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Ensure dialog fills most of screen width
        Window w = getWindow();
        if (w != null) {
            w.setLayout(
                (int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.92f),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
