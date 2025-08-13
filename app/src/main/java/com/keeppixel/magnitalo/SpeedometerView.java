package com.keeppixel.magnitalo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

public class SpeedometerView extends View {

    private Paint arcPaint;
    private Paint needlePaint;
    private Paint centerPaint;
    private Paint textPaint;
    private Paint backgroundPaint;

    private RectF arcRect;
    private Path needlePath;

    private float currentSpeed = 0f;
    private float targetSpeed = 65f;
    private float maxSpeed = 120f;

    private int centerX, centerY;
    private int radius;
    private float needleAngle = 0f;

    // Colors matching CarPlay theme
    private static final int COLOR_GREEN = 0xFF30D158;
    private static final int COLOR_YELLOW = 0xFFFFCC02;
    private static final int COLOR_ORANGE = 0xFFFF9500;
    private static final int COLOR_RED = 0xFFFF3B30;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GRAY = 0xFF8E8E93;
    private static final int COLOR_DARK = 0xFF1C1C1E;

    private ValueAnimator speedAnimator;

    public SpeedometerView(Context context) {
        super(context);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Initialize paints
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(COLOR_RED);
        needlePaint.setStyle(Paint.Style.FILL);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(COLOR_RED);
        centerPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(COLOR_WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(COLOR_DARK);
        backgroundPaint.setStyle(Paint.Style.FILL);

        arcRect = new RectF();
        needlePath = new Path();

        // Set initial speed
        setSpeed(targetSpeed, false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = w / 2;
        centerY = h / 2;
        radius = Math.min(w, h) / 2 - 20; // Leave some padding

        // Setup arc rectangle
        arcRect.set(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius);

        // Create gradient for the arc
        int[] colors = {COLOR_GREEN, COLOR_YELLOW, COLOR_ORANGE, COLOR_RED};
        float[] positions = {0f, 0.3f, 0.6f, 1f};

        SweepGradient gradient = new SweepGradient(centerX, centerY, colors, positions);
        arcPaint.setShader(gradient);
        arcPaint.setStrokeWidth(8f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (radius <= 0) return;

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius - 4, backgroundPaint);

        // Draw speed arc (270 degrees total, starting from 135 degrees)
        canvas.drawArc(arcRect, 135f, 270f, false, arcPaint);

        // Draw speed text
        drawSpeedText(canvas);

        // Draw needle
        drawNeedle(canvas);

        // Draw center circle
        canvas.drawCircle(centerX, centerY, 8f, centerPaint);
    }

    private void drawSpeedText(Canvas canvas) {
        // Main speed value
        textPaint.setTextSize(radius * 0.4f);
        textPaint.setColor(COLOR_WHITE);
        canvas.drawText(String.valueOf((int) currentSpeed),
                centerX, centerY + textPaint.getTextSize() * 0.3f, textPaint);

        // Speed unit
        textPaint.setTextSize(radius * 0.15f);
        textPaint.setColor(COLOR_GRAY);
        canvas.drawText("км/ч", centerX, centerY + radius * 0.6f, textPaint);
    }

    private void drawNeedle(Canvas canvas) {
        canvas.save();
        canvas.rotate(needleAngle, centerX, centerY);

        needlePath.reset();

        // Create needle shape
        float needleLength = radius * 0.7f;
        float needleWidth = 4f;

        needlePath.moveTo(centerX, centerY - needleWidth);
        needlePath.lineTo(centerX - needleWidth, centerY);
        needlePath.lineTo(centerX, centerY - needleLength);
        needlePath.lineTo(centerX + needleWidth, centerY);
        needlePath.close();

        canvas.drawPath(needlePath, needlePaint);
        canvas.restore();
    }

    public void setSpeed(float speed, boolean animate) {
        targetSpeed = Math.max(0, Math.min(speed, maxSpeed));

        if (animate) {
            animateToSpeed(targetSpeed);
        } else {
            currentSpeed = targetSpeed;
            needleAngle = speedToAngle(currentSpeed);
            invalidate();
        }
    }

    private void animateToSpeed(float newSpeed) {
        if (speedAnimator != null && speedAnimator.isRunning()) {
            speedAnimator.cancel();
        }

        speedAnimator = ValueAnimator.ofFloat(currentSpeed, newSpeed);
        speedAnimator.setDuration(1000);
        speedAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        speedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentSpeed = (Float) animation.getAnimatedValue();
                needleAngle = speedToAngle(currentSpeed);
                invalidate();
            }
        });

        speedAnimator.start();
    }

    private float speedToAngle(float speed) {
        // Map speed (0-120) to angle (135 to 405 degrees)
        // 135 degrees = start position, 270 degrees = total sweep
        float normalizedSpeed = speed / maxSpeed;
        return 135f + (normalizedSpeed * 270f);
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    // Simulate speed changes for demo
    public void startSpeedSimulation() {
        ValueAnimator simulator = ValueAnimator.ofFloat(0f, 120f, 60f, 90f, 30f, 65f);
        simulator.setDuration(15000); // 15 seconds cycle
        simulator.setRepeatCount(ValueAnimator.INFINITE);
        simulator.setRepeatMode(ValueAnimator.RESTART);

        simulator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float speed = (Float) animation.getAnimatedValue();
                setSpeed(speed, true);
            }
        });

        simulator.start();
    }
}
