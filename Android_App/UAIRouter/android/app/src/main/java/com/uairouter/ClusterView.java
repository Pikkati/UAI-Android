package com.uairouter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class ClusterView extends View {
    private Cluster cluster;
    private Paint paint;
    private Paint shadowPaint;
    private Paint highlightPaint;
    private ValueAnimator pulseAnimator;
    private ValueAnimator sizeAnimator;
    private ScaleGestureDetector scaleGestureDetector;
    private float currentRadius;
    private float targetRadius;
    private float pulseScale = 1.0f;
    private float scaleFactor = 1.0f;
    private static final String TAG = "ClusterView";

    public ClusterView(Context context) {
        super(context);
        initPaints();
        initAnimations();
        initGestureDetector();
        Log.d(TAG, "Enhanced ClusterView created with 3D effects and pinch-to-zoom");
    }

    private void initPaints() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(60, 0, 0, 0));

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
    }

    private void initAnimations() {
        // Pulse animation for breathing effect
        pulseAnimator = ValueAnimator.ofFloat(0.8f, 1.2f);
        pulseAnimator.setDuration(2000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            pulseScale = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();

        // Size change animation
        sizeAnimator = ValueAnimator.ofFloat(0f, 1f);
        sizeAnimator.setDuration(500);
        sizeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        sizeAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            currentRadius = targetRadius * progress;
            invalidate();
        });
    }

    private void initGestureDetector() {
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                // Limit scale factor to reasonable bounds
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
                invalidate();
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
        Log.d(TAG, "Set cluster: " + (cluster != null ? cluster.name : "null"));

        if (cluster != null) {
            targetRadius = Math.min(getWidth(), getHeight()) / 2f * (cluster.size / 200f);
            if (sizeAnimator.isRunning()) {
                sizeAnimator.cancel();
            }
            sizeAnimator.start();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            Log.d(TAG, "onDraw called, cluster: " + (cluster != null ? cluster.name : "null"));
            if (cluster != null && currentRadius > 0) {
                float centerX = getWidth() / 2f;
                float centerY = getHeight() / 2f;
                float animatedRadius = currentRadius * pulseScale * scaleFactor;

                // Draw shadow for 3D effect
                canvas.drawCircle(centerX + 2, centerY + 2, animatedRadius, shadowPaint);

                // Create 3D gradient effect
                int baseColor = Color.rgb(cluster.color[0], cluster.color[1], cluster.color[2]);
                int darkerColor = Color.rgb(
                    Math.max(0, cluster.color[0] - 40),
                    Math.max(0, cluster.color[1] - 40),
                    Math.max(0, cluster.color[2] - 40)
                );
                int lighterColor = Color.rgb(
                    Math.min(255, cluster.color[0] + 60),
                    Math.min(255, cluster.color[1] + 60),
                    Math.min(255, cluster.color[2] + 60)
                );

                // Radial gradient for sphere effect
                RadialGradient gradient = new RadialGradient(
                    centerX - animatedRadius * 0.3f,
                    centerY - animatedRadius * 0.3f,
                    animatedRadius * 1.5f,
                    new int[]{lighterColor, baseColor, darkerColor},
                    new float[]{0f, 0.7f, 1f},
                    Shader.TileMode.CLAMP
                );
                paint.setShader(gradient);

                // Draw main circle
                canvas.drawCircle(centerX, centerY, animatedRadius, paint);

                // Add highlight for 3D effect
                highlightPaint.setShader(new RadialGradient(
                    centerX - animatedRadius * 0.4f,
                    centerY - animatedRadius * 0.4f,
                    animatedRadius * 0.6f,
                    Color.argb(100, 255, 255, 255),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                ));
                canvas.drawCircle(centerX, centerY, animatedRadius, highlightPaint);

                // Reset shader
                paint.setShader(null);
                highlightPaint.setShader(null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDraw", e);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = 120; // Slightly larger for 3D effects
        Log.d(TAG, "onMeasure: setting size to " + size);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        if (sizeAnimator != null) {
            sizeAnimator.cancel();
        }
    }
}
