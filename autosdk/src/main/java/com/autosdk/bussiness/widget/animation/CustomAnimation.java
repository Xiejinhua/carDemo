package com.autosdk.bussiness.widget.animation;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;

import com.autosdk.bussiness.widget.BusinessApplicationUtils;

/**
 * @author AutoSDK
 */
public class CustomAnimation extends EmptyAnimation {

    public CustomAnimation(View view) {
        super(view);
    }

    @Override
    public void start() {
        final View targetView = view.get();
        if (targetView == null) {
            return;
        }
        //进入时隐藏
        switch (slideMode) {
            case AnimSlideMode.LEFT_IN:
                targetView.setTranslationX(-BusinessApplicationUtils.mScreenWidth);
                break;
            case AnimSlideMode.RIGHT_IN:
                targetView.setTranslationX(BusinessApplicationUtils.mScreenWidth);
                break;
            case AnimSlideMode.UP_IN:
                targetView.setTranslationY(-BusinessApplicationUtils.mScreenHeight);
                break;
            case AnimSlideMode.DOWN_IN:
                targetView.setTranslationY(BusinessApplicationUtils.mScreenHeight);
                break;
            default:
                break;
        }

        //解决宽高不能获取问题
        targetView.post(new Runnable() {
            @Override
            public void run() {
                ViewPropertyAnimatorCompat animatorCompat = ViewCompat.animate(targetView);
                setSlide(animatorCompat, slideMode, targetView);

                setScale(animatorCompat, targetView);

                if (rotationValue != ROTATION_DEFAULT) {
                    animatorCompat.rotation(rotationValue);
                }

                if (interpolator != null) {
                    animatorCompat.setInterpolator(interpolator);
                } else {
                    animatorCompat.setInterpolator(new AccelerateDecelerateInterpolator());
                }
                if (updateListener != null) {
                    animatorCompat.setUpdateListener(updateListener);
                }
                if (animatorListener != null) {
                    animatorCompat.setListener(animatorListener);
                }

                animatorCompat.setDuration(time);
                if (delay != -1) {
                    animatorCompat.setStartDelay(delay);
                }
                if (null != endRunnable) {
                    animatorCompat.withEndAction(endRunnable);
                }
                animatorCompat.start();
            }
        });
    }

    private void setScale(ViewPropertyAnimatorCompat animatorCompat, View targetView) {
        if (fromScaleX != SCALE_DEFAULT) {
            targetView.setScaleX(fromScaleX);
        }
        if (fromScaleY != SCALE_DEFAULT) {
            targetView.setScaleY(fromScaleY);
        }

        if (toScaleX != SCALE_DEFAULT) {
            animatorCompat.scaleX(toScaleX);
        }
        if (toScaleY != SCALE_DEFAULT) {
            animatorCompat.scaleY(toScaleY);
        }
    }

    private void setSlide(ViewPropertyAnimatorCompat animatorCompat,@AnimSlideMode int slideMode, View
            targetView) {
        int screenHeight = BusinessApplicationUtils.mScreenHeight;
        int screenWidth = BusinessApplicationUtils.mScreenWidth;
        float offset;
        switch (slideMode) {
            case AnimSlideMode.LEFT_IN:
                offset = -(targetView.getLeft() + targetView.getWidth());
                targetView.setTranslationX(offset);
                animatorCompat.translationX(0);
                break;
            case AnimSlideMode.LEFT_OUT:
                offset = -(targetView.getLeft() + targetView.getWidth());
                targetView.setTranslationX(0);
                animatorCompat.translationX(offset);
                break;
            case AnimSlideMode.RIGHT_IN:
                offset = (screenWidth - targetView.getLeft());
                targetView.setTranslationX(offset);
                animatorCompat.translationX(0);
                break;
            case AnimSlideMode.RIGHT_OUT:
                offset = (screenWidth - targetView.getLeft());
                targetView.setTranslationX(0);
                animatorCompat.translationX(offset);
                break;
            case AnimSlideMode.UP_IN:
                offset = -(targetView.getTop() + targetView.getHeight());
                targetView.setTranslationY(offset);
                animatorCompat.translationY(0);
                break;
            case AnimSlideMode.UP_OUT:
                offset = -(targetView.getTop() + targetView.getHeight());
                targetView.setTranslationY(0);
                animatorCompat.translationY(offset);
                break;
            case AnimSlideMode.DOWN_IN:
                offset = (screenHeight - targetView.getTop());
                targetView.setTranslationY(offset);
                animatorCompat.translationY(0);
                break;
            case AnimSlideMode.DOWN_OUT:
                offset = (screenHeight - targetView.getTop());
                targetView.setTranslationY(0);
                animatorCompat.translationY(offset);
                break;
            default:
                if (fromTranslationX != TRANSLATION_DEFAULT) {
                    targetView.setTranslationX(fromTranslationX);
                }
                if (toTranslationX != TRANSLATION_DEFAULT) {
                    animatorCompat.translationX(toTranslationX);
                }
                break;
        }
    }
}
