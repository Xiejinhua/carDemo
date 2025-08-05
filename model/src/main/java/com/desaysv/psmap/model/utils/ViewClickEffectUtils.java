package com.desaysv.psmap.model.utils;

import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;


public class ViewClickEffectUtils {

    private static final float CLICKED_ALPHA = 0.8f;
    private static final float UNCLICKED_ALPHA = 1.0f;
    public static final float CLICKED_SCALE = 0.97f;
    public static final float CLICKED_SCALE_90 = 0.90f;
    public static final float CLICKED_SCALE_93 = 0.93f;
    public static final float CLICKED_SCALE_95 = 0.95f;
    public static final float CLICKED_SCALE_97 = 0.97f;
    public static final float CLICKED_SCALE_98 = 0.98f;
    public static final float UNCLICKED_SCALE = 1.0f;
    private static final int ANIMATION_DURATION = 150;

    public static void addClickEffect(final View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 按下时设置透明度和放大动画
                    animateAlpha(view, CLICKED_ALPHA);
                    animateScale(view, CLICKED_SCALE);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 抬起或取消时恢复透明度和缩小动画
                    animateAlpha(view, UNCLICKED_ALPHA);
                    animateScale(view, UNCLICKED_SCALE);
                    break;
            }
            return false;
        });
    }

    public static void addClickScaleAlpha(final View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 按下时设置透明度和放大动画
                    animateAlpha(view, CLICKED_ALPHA);
                    animateScale(view, CLICKED_SCALE);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 抬起或取消时恢复透明度和缩小动画
                    animateAlpha(view, UNCLICKED_ALPHA);
                    animateScale(view, UNCLICKED_SCALE);
                    break;
            }
            return false;
        });
    }

    public static void addClickScale97Alpha(final View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 按下时设置透明度和放大动画
                    animateAlpha(view, CLICKED_ALPHA);
                    animateScale(view, CLICKED_SCALE_97);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 抬起或取消时恢复透明度和缩小动画
                    animateAlpha(view, UNCLICKED_ALPHA);
                    animateScale(view, UNCLICKED_SCALE);
                    break;
            }
            return false;
        });
    }

    public static void addClickScale97Alpha(final View view, View.OnTouchListener listener) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 按下时设置透明度和放大动画
                    animateAlpha(view, CLICKED_ALPHA);
                    animateScale(view, CLICKED_SCALE_97);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 抬起或取消时恢复透明度和缩小动画
                    animateAlpha(view, UNCLICKED_ALPHA);
                    animateScale(view, UNCLICKED_SCALE);
                    break;
            }
            listener.onTouch(v, event);
            return false;
        });
    }


    public static void addClickScale(final View view) {
        addClickScale(view, CLICKED_SCALE_97); // 设置默认缩放值
    }

    public static void addClickScale(final View view, float scale) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 按下时设置透明度和放大动画
                    animateScale(view, scale);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 抬起或取消时恢复透明度和缩小动画
                    animateScale(view, UNCLICKED_SCALE);
                    break;
            }
            return v.onTouchEvent(event);
        });
    }

    private static void animateAlpha(final View view, float targetAlpha) {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", view.getAlpha(), targetAlpha);
        alphaAnimator.setDuration(ANIMATION_DURATION);
        alphaAnimator.setInterpolator(new DecelerateInterpolator());
        alphaAnimator.start();
    }

    public static void animateScaleTouchStart(final View view) {
        animateScale(view, CLICKED_SCALE);
    }

    public static void animateScaleTouchEnd(final View view) {
        animateScale(view, UNCLICKED_SCALE);
    }

    public static void animateScale(final View view, float targetScale) {
        if (view.getScaleX() == targetScale) {
            return;
        }
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), targetScale);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), targetScale);
        scaleXAnimator.setDuration(ANIMATION_DURATION);
        scaleYAnimator.setDuration(ANIMATION_DURATION);

        scaleXAnimator.setInterpolator(new DecelerateInterpolator());
        scaleYAnimator.setInterpolator(new DecelerateInterpolator());

        scaleXAnimator.start();
        scaleYAnimator.start();
    }

    public static void dialogShow(final View view) {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);

        scaleXAnimator.setDuration(250);
        scaleYAnimator.setDuration(250);

        scaleXAnimator.setInterpolator(new DecelerateInterpolator());
        scaleYAnimator.setInterpolator(new DecelerateInterpolator());

        scaleXAnimator.start();
        scaleYAnimator.start();

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        alphaAnimator.setDuration(250);
        alphaAnimator.setInterpolator(new DecelerateInterpolator());
        alphaAnimator.start();
    }

    public static ObjectAnimator dialogDismiss(final View view) {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f);

        scaleXAnimator.setDuration(250);
        scaleYAnimator.setDuration(250);

        scaleXAnimator.setInterpolator(new DecelerateInterpolator());
        scaleYAnimator.setInterpolator(new DecelerateInterpolator());
        scaleXAnimator.start();
        scaleYAnimator.start();

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        alphaAnimator.setDuration(250);
        alphaAnimator.setInterpolator(new DecelerateInterpolator());
        alphaAnimator.start();
        return alphaAnimator;
    }
}