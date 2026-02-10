package com.uairouter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import java.util.List;

public class MultiClusterView extends View {
    private List<Cluster> clusters;
    private Paint paint;
    private Paint textPaint;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private float offsetX = 0f;
    private float offsetY = 0f;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging = false;
    private static final String TAG = "MultiClusterView";
    private static final float CLUSTER_SPACING = 200f;

    public MultiClusterView(Context context) {
        super(context);
        initPaints();
        initGestureDetector();
        Log.d(TAG, "MultiClusterView created with pinch-to-zoom and pan support");
    }

    private void initPaints() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void initGestureDetector() {
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                // Limit scale factor to reasonable bounds
                scaleFactor = Math.max(0.3f, Math.min(scaleFactor, 5.0f));
                invalidate();
                return true;
            }
        });
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
        Log.d(TAG, "Set clusters: " + (clusters != null ? clusters.size() : 0) + " clusters");
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scaleGestureDetector.isInProgress()) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isDragging = true;
                    }
                    if (isDragging) {
                        offsetX += dx;
                        offsetY += dy;
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isDragging = false;
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if (clusters != null && !clusters.isEmpty()) {
                float centerX = getWidth() / 2f;
                float centerY = getHeight() / 2f;

                // Calculate grid layout
                int cols = (int) Math.ceil(Math.sqrt(clusters.size()));
                int rows = (int) Math.ceil((double) clusters.size() / cols);

                for (int i = 0; i < clusters.size(); i++) {
                    Cluster cluster = clusters.get(i);
                    int row = i / cols;
                    int col = i % cols;

                    // Calculate position in grid
                    float baseX = centerX + offsetX + (col - (cols - 1) / 2f) * CLUSTER_SPACING * scaleFactor;
                    float baseY = centerY + offsetY + (row - (rows - 1) / 2f) * CLUSTER_SPACING * scaleFactor;

                    drawCluster(canvas, cluster, baseX, baseY);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDraw", e);
        }
    }

    private void drawCluster(Canvas canvas, Cluster cluster, float centerX, float centerY) {
        float radius = 40f * scaleFactor * (cluster.size / 200f);

        if (radius <= 0) return;

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
            centerX - radius * 0.3f,
            centerY - radius * 0.3f,
            radius * 1.5f,
            new int[]{lighterColor, baseColor, darkerColor},
            new float[]{0f, 0.7f, 1f},
            Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);

        // Draw shadow
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(60, 0, 0, 0));
        canvas.drawCircle(centerX + 2, centerY + 2, radius, shadowPaint);

        // Draw main circle
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Add highlight for 3D effect
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setShader(new RadialGradient(
            centerX - radius * 0.4f,
            centerY - radius * 0.4f,
            radius * 0.6f,
            Color.argb(100, 255, 255, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(centerX, centerY, radius, highlightPaint);

        // Draw cluster name
        if (scaleFactor > 0.8f) {
            canvas.drawText(cluster.name, centerX, centerY + radius + 30, textPaint);
        }

        // Reset shaders
        paint.setShader(null);
        highlightPaint.setShader(null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = 400; // Larger size for multi-cluster view
        Log.d(TAG, "onMeasure: setting size to " + size);
        setMeasuredDimension(size, size);
    }
}
