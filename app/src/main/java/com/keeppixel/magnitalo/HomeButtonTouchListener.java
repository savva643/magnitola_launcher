package com.keeppixel.magnitalo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public class HomeButtonTouchListener implements View.OnTouchListener {
    private float dX;
    private float downRawX;
    private float downRawY;
    private boolean isDragging = false;

    private final ViewGroup multitaskLayout;
    private final ViewGroup menuLayout;
    private final View homeButton;

    public HomeButtonTouchListener(Context context, View homeButton, ViewGroup multitaskLayout, ViewGroup menuLayout) {
        this.homeButton = homeButton;
        this.multitaskLayout = multitaskLayout;
        this.menuLayout = menuLayout;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float rawX = event.getRawX();
        float rawY = event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragging = false;
                downRawX = rawX;
                downRawY = rawY;
                dX = v.getX() - rawX;
                return true;

            case MotionEvent.ACTION_MOVE:
                float moveX = rawX + dX;
                float deltaX = rawX - downRawX;
                float deltaY = rawY - downRawY;

                if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                    isDragging = true;
                }

                // Двигаем строго вместе с пальцем
                v.setX(moveX);
                v.setY(rawY - v.getHeight() / 2);  // по Y можно тоже плавно двигать, если нужно
                return true;

            case MotionEvent.ACTION_UP:
                v.animate()
                        .x(v.getRootView().getWidth() / 2f - v.getWidth() / 2f)
                        .y(v.getRootView().getHeight() - v.getHeight() - 50)
                        .setInterpolator(new DecelerateInterpolator())
                        .setDuration(200)
                        .start();

                float diffX = rawX - downRawX;
                float diffY = rawY - downRawY;

                if (!isDragging) {
                    // Одиночное нажатие — закрыть оба
                    hideLayouts();
                } else {
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (diffX > 100) {
                            toggleMultitask(); // вправо — открыть многозадачность
                        } else if (diffX < -100) {
                            closeMultitask(); // влево — закрыть многозадачность
                        }
                    } else {
                        if (diffY < -100) {
                            toggleMenu(); // вверх — открыть меню
                        } else if (diffY > 100) {
                            closeMenu(); // вниз — закрыть меню
                        }
                    }
                }
                return true;
        }
        return false;
    }

    private void toggleMultitask() {
        if (menuLayout.getVisibility() == View.VISIBLE) return;
        if (multitaskLayout.getVisibility() != View.VISIBLE) {
            showLayout(multitaskLayout);
        }
    }

    private void closeMultitask() {
        hideLayout(multitaskLayout);
    }

    private void toggleMenu() {
        if (multitaskLayout.getVisibility() == View.VISIBLE) return;
        if (menuLayout.getVisibility() != View.VISIBLE) {
            showLayout(menuLayout);
        }
    }

    private void closeMenu() {
        hideLayout(menuLayout);
    }

    private void hideLayouts() {
        hideLayout(multitaskLayout);
        hideLayout(menuLayout);
    }

    private void showLayout(ViewGroup layout) {
        layout.setAlpha(0f);
        layout.setVisibility(View.VISIBLE);
        layout.setTranslationY(50); // плавный выезд сверху или снизу

        layout.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(200)
                .start();
    }

    private void hideLayout(ViewGroup layout) {
        if (layout.getVisibility() == View.VISIBLE) {
            layout.animate()
                    .alpha(0f)
                    .translationY(50)
                    .setDuration(200)
                    .withEndAction(() -> layout.setVisibility(View.GONE))
                    .start();
        }
    }
}
