package com.keeppixel.magnitalo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.widget.AppCompatImageButton;

public class DraggableHomeButton extends AppCompatImageButton {
    private static final int DRAG_THRESHOLD = 30;
    private static final int MIN_TRIGGER_DISTANCE = 100;
    private static final int ANIMATION_DURATION = 300;

    // Paint objects for custom drawing
    private Paint backgroundPaint;
    private Paint borderPaint;
    private Paint iconPaint;
    private Paint glowPaint;

    // Animation values
    private float pressedScale = 1.0f;
    private float glowAlpha = 0.0f;
    private ValueAnimator pressAnimator;
    private ValueAnimator glowAnimator;

    // Colors matching CarPlay theme
    private static final int COLOR_BACKGROUND_START = 0x1AFFFFFF; // 10% white
    private static final int COLOR_BACKGROUND_END = 0x0DFFFFFF;   // 5% white
    private static final int COLOR_BORDER = 0x33FFFFFF;          // 20% white
    private static final int COLOR_ICON = 0xFFFFFFFF;            // White
    private static final int COLOR_GLOW = 0x4D007AFF;            // Blue glow

    // Координаты
    private float startX, startY;
    private float originalX, originalY;
    private float currentX, currentY;

    // Максимальные расстояния движения
    private float maxUpDistance;
    private float maxRightDistance;
    private float maxDownDistance;
    private float maxLeftDistance;

    // Состояния
    private boolean isDragging = false;
    private DragDirection currentDirection = DragDirection.NONE;
    private ViewState currentState = ViewState.HOME;

    // Layouts
    private View menuLayout;
    private View multitaskLayout;
    private ViewGroup parentContainer;

    // Listener
    private OnDragListener dragListener;

    // Enums
    public enum DragDirection {
        NONE, UP, DOWN, LEFT, RIGHT
    }

    public enum ViewState {
        HOME, MENU_OPEN, MULTITASK_OPEN
    }

    public interface OnDragListener {
        void onMenuShow();
        void onMenuHide();
        void onMultitaskShow();
        void onMultitaskHide();
        void onReturnToHome();
    }

    public DraggableHomeButton(Context context) {
        super(context);
        init();
    }

    public DraggableHomeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableHomeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Remove default background and image
        setBackground(null);
        setImageDrawable(null);

        // Initialize paints
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(COLOR_BORDER);

        iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconPaint.setStyle(Paint.Style.FILL);
        iconPaint.setColor(COLOR_ICON);
        iconPaint.setStrokeWidth(3f);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(COLOR_GLOW);

        // Enable hardware acceleration for better performance
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Create gradient for background
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, h,
                COLOR_BACKGROUND_START, COLOR_BACKGROUND_END,
                Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) / 2f - 4f; // Leave space for border

        // Apply scale transformation
        canvas.save();
        canvas.scale(pressedScale, pressedScale, centerX, centerY);

        // Draw glow effect
        if (glowAlpha > 0) {
            glowPaint.setAlpha((int) (glowAlpha * 255));
            canvas.drawCircle(centerX, centerY, radius + 8f, glowPaint);
        }

        // Draw background circle with gradient
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Draw border
        canvas.drawCircle(centerX, centerY, radius, borderPaint);

        // Draw home icon (house shape)
        drawHomeIcon(canvas, centerX, centerY, radius * 0.4f);

        canvas.restore();
    }

    private void drawHomeIcon(Canvas canvas, float centerX, float centerY, float size) {
        Path iconPath = new Path();

        // House roof (triangle)
        iconPath.moveTo(centerX, centerY - size * 0.8f); // Top point
        iconPath.lineTo(centerX - size * 0.8f, centerY - size * 0.2f); // Left point
        iconPath.lineTo(centerX + size * 0.8f, centerY - size * 0.2f); // Right point
        iconPath.close();

        // House base (rectangle)
        RectF baseRect = new RectF(
                centerX - size * 0.6f,
                centerY - size * 0.2f,
                centerX + size * 0.6f,
                centerY + size * 0.8f
        );
        iconPath.addRect(baseRect, Path.Direction.CW);

        // Door (small rectangle)
        RectF doorRect = new RectF(
                centerX - size * 0.15f,
                centerY + size * 0.2f,
                centerX + size * 0.15f,
                centerY + size * 0.8f
        );

        // Draw filled icon
        iconPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(iconPath, iconPaint);

        // Draw door (cut out)
        iconPaint.setStyle(Paint.Style.FILL);
        iconPaint.setColor(Color.TRANSPARENT);
        iconPaint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
        canvas.drawRect(doorRect, iconPaint);

        // Reset paint
        iconPaint.setXfermode(null);
        iconPaint.setColor(COLOR_ICON);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startPressAnimation();
                return handleTouchDown(event);
            case MotionEvent.ACTION_MOVE:
                return handleTouchMove(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                endPressAnimation();
                return handleTouchUp(event);
        }
        return super.onTouchEvent(event);
    }

    private void startPressAnimation() {
        if (pressAnimator != null) {
            pressAnimator.cancel();
        }

        pressAnimator = ValueAnimator.ofFloat(pressedScale, 0.9f);
        pressAnimator.setDuration(150);
        pressAnimator.setInterpolator(new DecelerateInterpolator());
        pressAnimator.addUpdateListener(animation -> {
            pressedScale = (Float) animation.getAnimatedValue();
            invalidate();
        });
        pressAnimator.start();

        // Start glow animation
        if (glowAnimator != null) {
            glowAnimator.cancel();
        }

        glowAnimator = ValueAnimator.ofFloat(glowAlpha, 1.0f);
        glowAnimator.setDuration(200);
        glowAnimator.addUpdateListener(animation -> {
            glowAlpha = (Float) animation.getAnimatedValue();
            invalidate();
        });
        glowAnimator.start();
    }

    private void endPressAnimation() {
        if (pressAnimator != null) {
            pressAnimator.cancel();
        }

        pressAnimator = ValueAnimator.ofFloat(pressedScale, 1.0f);
        pressAnimator.setDuration(200);
        pressAnimator.setInterpolator(new DecelerateInterpolator());
        pressAnimator.addUpdateListener(animation -> {
            pressedScale = (Float) animation.getAnimatedValue();
            invalidate();
        });
        pressAnimator.start();

        // End glow animation
        if (glowAnimator != null) {
            glowAnimator.cancel();
        }

        glowAnimator = ValueAnimator.ofFloat(glowAlpha, 0.0f);
        glowAnimator.setDuration(300);
        glowAnimator.addUpdateListener(animation -> {
            glowAlpha = (Float) animation.getAnimatedValue();
            invalidate();
        });
        glowAnimator.start();
    }

    // Добавляем метод для создания эффекта "ripple" при клике
    public void createRippleEffect() {
        ValueAnimator rippleAnimator = ValueAnimator.ofFloat(0f, 1f);
        rippleAnimator.setDuration(600);
        rippleAnimator.addUpdateListener(animation -> {
            float progress = (Float) animation.getAnimatedValue();
            // Создаем эффект расширяющегося круга
            glowAlpha = 1f - progress;
            pressedScale = 1f + (progress * 0.2f);
            invalidate();
        });
        rippleAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                pressedScale = 1f;
                glowAlpha = 0f;
                invalidate();
            }
        });
        rippleAnimator.start();
    }

    // Остальные методы остаются без изменений...
    public void setDragListener(OnDragListener listener) {
        this.dragListener = listener;
    }

    public void setMenuLayout(View menuLayout) {
        this.menuLayout = menuLayout;
        if (menuLayout != null) {
            menuLayout.setVisibility(View.GONE);
            menuLayout.setAlpha(0f);
            menuLayout.setTranslationY(menuLayout.getHeight());
        }
    }

    public void setMultitaskingLayout(View multitaskingLayout) {
        this.multitaskLayout = multitaskingLayout;
        if (multitaskingLayout != null) {
            multitaskingLayout.setVisibility(View.GONE);
            multitaskingLayout.setAlpha(0f);
            multitaskingLayout.setTranslationX(-multitaskingLayout.getWidth());
        }
    }

    public void setParentContainer(ViewGroup container) {
        this.parentContainer = container;
        post(() -> {
            originalX = getX();
            originalY = getY();
            currentX = originalX;
            currentY = originalY;

            maxUpDistance = originalY;
            maxRightDistance = container.getWidth() - originalX - getWidth();
            maxDownDistance = container.getHeight() - originalY - getHeight();
            maxLeftDistance = originalX;
        });
    }

    private boolean handleTouchDown(MotionEvent event) {
        startX = event.getRawX();
        startY = event.getRawY();
        isDragging = false;
        currentDirection = DragDirection.NONE;
        return true;
    }

    private boolean handleTouchMove(MotionEvent event) {
        float currentTouchX = event.getRawX();
        float currentTouchY = event.getRawY();
        float deltaX = currentTouchX - startX;
        float deltaY = currentTouchY - startY;

        if (currentDirection == DragDirection.NONE) {
            if (Math.abs(deltaX) > DRAG_THRESHOLD || Math.abs(deltaY) > DRAG_THRESHOLD) {
                currentDirection = determineDirection(deltaX, deltaY);
                isDragging = true;
            }
        }

        if (isDragging && currentDirection != DragDirection.NONE) {
            handleDragInDirection(deltaX, deltaY);
        }

        return true;
    }

    private DragDirection determineDirection(float deltaX, float deltaY) {
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX > 0) {
                return canMoveRight() ? DragDirection.RIGHT : DragDirection.NONE;
            } else {
                return canMoveLeft() ? DragDirection.LEFT : DragDirection.NONE;
            }
        } else {
            if (deltaY < 0) {
                return canMoveUp() ? DragDirection.UP : DragDirection.NONE;
            } else {
                return canMoveDown() ? DragDirection.DOWN : DragDirection.NONE;
            }
        }
    }

    private boolean canMoveUp() {
        return currentState == ViewState.HOME;
    }

    private boolean canMoveDown() {
        return currentState == ViewState.MENU_OPEN;
    }

    private boolean canMoveRight() {
        return currentState == ViewState.HOME;
    }

    private boolean canMoveLeft() {
        return currentState == ViewState.MULTITASK_OPEN;
    }

    private void handleDragInDirection(float deltaX, float deltaY) {
        switch (currentDirection) {
            case UP:
                handleUpDrag(-deltaY);
                break;
            case DOWN:
                handleDownDrag(deltaY);
                break;
            case RIGHT:
                handleRightDrag(deltaX);
                break;
            case LEFT:
                handleLeftDrag(-deltaX);
                break;
        }
    }

    private void handleUpDrag(float distance) {
        distance = Math.max(0, Math.min(distance, maxUpDistance));
        float layoutProgress = distance / maxUpDistance;

        currentY = originalY - distance;
        setY(currentY);

        if (menuLayout != null) {
            if (menuLayout.getVisibility() != View.VISIBLE) {
                menuLayout.setVisibility(View.VISIBLE);
            }
            float menuTranslation = menuLayout.getHeight() * (1 - layoutProgress);
            menuLayout.setTranslationY(menuTranslation);
            menuLayout.setAlpha(layoutProgress);
        }
    }

    private void handleDownDrag(float distance) {
        if (currentState != ViewState.MENU_OPEN) return;

        distance = Math.max(0, Math.min(distance, maxUpDistance));
        float layoutProgress = distance / maxUpDistance;

        currentY = originalY - maxUpDistance + distance;
        setY(currentY);

        if (menuLayout != null) {
            float menuTranslation = menuLayout.getHeight() * layoutProgress;
            menuLayout.setTranslationY(menuTranslation);
            menuLayout.setAlpha(1 - layoutProgress);
        }
    }

    private void handleRightDrag(float distance) {
        distance = Math.max(0, Math.min(distance, maxRightDistance));
        float layoutProgress = distance / maxRightDistance;

        currentX = originalX + distance;
        setX(currentX);

        if (multitaskLayout != null) {
            if (multitaskLayout.getVisibility() != View.VISIBLE) {
                multitaskLayout.setVisibility(View.VISIBLE);
            }
            float multitaskTranslation = -multitaskLayout.getWidth() * (1 - layoutProgress);
            multitaskLayout.setTranslationX(multitaskTranslation);
            multitaskLayout.setAlpha(layoutProgress);
        }
    }

    private void handleLeftDrag(float distance) {
        if (currentState != ViewState.MULTITASK_OPEN) return;

        distance = Math.max(0, Math.min(distance, maxRightDistance));
        float layoutProgress = distance / maxRightDistance;

        currentX = originalX + maxRightDistance - distance;
        setX(currentX);

        if (multitaskLayout != null) {
            float multitaskTranslation = -multitaskLayout.getWidth() * layoutProgress;
            multitaskLayout.setTranslationX(multitaskTranslation);
            multitaskLayout.setAlpha(1 - layoutProgress);
        }
    }

    private boolean handleTouchUp(MotionEvent event) {
        if (!isDragging) {
            performClick();
            return true;
        }

        float currentTouchX = event.getRawX();
        float currentTouchY = event.getRawY();
        float deltaX = currentTouchX - startX;
        float deltaY = currentTouchY - startY;

        switch (currentDirection) {
            case UP:
                finishUpDrag(-deltaY);
                break;
            case DOWN:
                finishDownDrag(deltaY);
                break;
            case RIGHT:
                finishRightDrag(deltaX);
                break;
            case LEFT:
                finishLeftDrag(-deltaX);
                break;
        }

        isDragging = false;
        currentDirection = DragDirection.NONE;
        return true;
    }

    private void finishUpDrag(float distance) {
        float threshold = Math.max(MIN_TRIGGER_DISTANCE, maxUpDistance / 4);
        if (distance > threshold) {
            animateToMenuOpen();
        } else {
            animateToHome();
        }
    }

    private void finishDownDrag(float distance) {
        float threshold = Math.max(MIN_TRIGGER_DISTANCE, maxUpDistance / 4);
        if (distance > threshold) {
            animateToHome();
        } else {
            animateToMenuOpen();
        }
    }

    private void finishRightDrag(float distance) {
        float threshold = Math.max(MIN_TRIGGER_DISTANCE, maxRightDistance / 4);
        if (distance > threshold) {
            animateToMultitaskOpen();
        } else {
            animateToHome();
        }
    }

    private void finishLeftDrag(float distance) {
        float threshold = Math.max(MIN_TRIGGER_DISTANCE, maxRightDistance / 4);
        if (distance > threshold) {
            animateToHome();
        } else {
            animateToMultitaskOpen();
        }
    }

    private void animateToHome() {
        animateButtonTo(originalX, originalY);

        if (currentState == ViewState.MENU_OPEN && menuLayout != null) {
            animateMenuClose();
        }

        if (currentState == ViewState.MULTITASK_OPEN && multitaskLayout != null) {
            animateMultitaskClose();
        }

        currentState = ViewState.HOME;
        if (dragListener != null) {
            dragListener.onReturnToHome();
        }
    }

    private void animateToMenuOpen() {
        animateButtonTo(originalX, 0);

        if (menuLayout != null) {
            animateMenuOpen();
        }

        currentState = ViewState.MENU_OPEN;
        if (dragListener != null) {
            dragListener.onMenuShow();
        }
    }

    private void animateToMultitaskOpen() {
        float rightEdgeX = parentContainer.getWidth() - getWidth();
        animateButtonTo(rightEdgeX, originalY);

        if (multitaskLayout != null) {
            animateMultitaskOpen();
        }

        currentState = ViewState.MULTITASK_OPEN;
        if (dragListener != null) {
            dragListener.onMultitaskShow();
        }
    }

    private void animateButtonTo(float targetX, float targetY) {
        ValueAnimator animatorX = ValueAnimator.ofFloat(getX(), targetX);
        ValueAnimator animatorY = ValueAnimator.ofFloat(getY(), targetY);

        animatorX.setDuration(ANIMATION_DURATION);
        animatorY.setDuration(ANIMATION_DURATION);
        animatorX.setInterpolator(new DecelerateInterpolator());
        animatorY.setInterpolator(new DecelerateInterpolator());

        animatorX.addUpdateListener(animation -> {
            currentX = (Float) animation.getAnimatedValue();
            setX(currentX);
        });

        animatorY.addUpdateListener(animation -> {
            currentY = (Float) animation.getAnimatedValue();
            setY(currentY);
        });

        animatorX.start();
        animatorY.start();
    }

    private void animateMenuOpen() {
        ValueAnimator animator = ValueAnimator.ofFloat(menuLayout.getTranslationY(), 0);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            menuLayout.setTranslationY(value);
            menuLayout.setAlpha(1f - (value / menuLayout.getHeight()));
        });
        animator.start();
    }

    private void animateMenuClose() {
        ValueAnimator animator = ValueAnimator.ofFloat(menuLayout.getTranslationY(), menuLayout.getHeight());
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            menuLayout.setTranslationY(value);
            menuLayout.setAlpha(1f - (value / menuLayout.getHeight()));
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                menuLayout.setVisibility(View.GONE);
                if (dragListener != null) {
                    dragListener.onMenuHide();
                }
            }
        });
        animator.start();
    }

    private void animateMultitaskOpen() {
        ValueAnimator animator = ValueAnimator.ofFloat(multitaskLayout.getTranslationX(), 0);
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            multitaskLayout.setTranslationX(value);
            multitaskLayout.setAlpha(1f - Math.abs(value / multitaskLayout.getWidth()));
        });
        animator.start();
    }

    private void animateMultitaskClose() {
        ValueAnimator animator = ValueAnimator.ofFloat(multitaskLayout.getTranslationX(), -multitaskLayout.getWidth());
        animator.setDuration(ANIMATION_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (Float) animation.getAnimatedValue();
            multitaskLayout.setTranslationX(value);
            multitaskLayout.setAlpha(1f - Math.abs(value / multitaskLayout.getWidth()));
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                multitaskLayout.setVisibility(View.GONE);
                if (dragListener != null) {
                    dragListener.onMultitaskHide();
                }
            }
        });
        animator.start();
    }

    public void hideAllMenus() {
        animateToHome();
    }

    public ViewState getCurrentState() {
        return currentState;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        createRippleEffect(); // Добавляем эффект при клике
        hideAllMenus();
        return true;
    }
}
