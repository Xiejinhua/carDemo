package com.autosdk.bussiness.widget.animation;

import android.view.View;
import android.view.animation.Interpolator;

import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.core.view.ViewPropertyAnimatorUpdateListener;

import java.lang.ref.WeakReference;

/**
 * 空实现、动画关闭时返回，HMI可以继续链式调用
 * @author AutoSDK
 */
public class EmptyAnimation implements Animation {
    public static final float TRANSLATION_DEFAULT = -9999f;
    public static final float ROTATION_DEFAULT = -9999f;
    public static final float SCALE_DEFAULT = -1;
    public static final double ALPHA_DEFAULT = -1;
    protected final WeakReference<View> view;
    protected @AnimSlideMode int slideMode = AnimSlideMode.DEFAULT;
    protected float fromTranslationX = TRANSLATION_DEFAULT;
    protected float toTranslationX = TRANSLATION_DEFAULT;
    protected float rotationValue = ROTATION_DEFAULT;
    protected float fromScaleX = SCALE_DEFAULT;
    protected float toScaleX = SCALE_DEFAULT;
    protected float fromScaleY = SCALE_DEFAULT;
    protected float toScaleY = SCALE_DEFAULT;
    protected double fromAlpha = ALPHA_DEFAULT;
    protected double toAlpha = ALPHA_DEFAULT;
    protected long delay = -1;
    protected Interpolator interpolator;
    protected ViewPropertyAnimatorUpdateListener updateListener;
    protected ViewPropertyAnimatorListener animatorListener;
    protected Runnable endRunnable;
    protected long time = 400;

    public EmptyAnimation(View view) {
        this.view = new WeakReference<>(view);
    }

    @Override
    public Animation fromTranslationX(float fromTranslationX) {
        this.fromTranslationX = fromTranslationX;
        return this;
    }

    @Override
    public Animation toTranslationX(float toTranslationX) {
        this.toTranslationX = toTranslationX;
        return this;
    }

    @Override
    public Animation fromAlpha(float fromAlpha) {
        this.fromAlpha = fromAlpha;
        return this;
    }

    @Override
    public Animation toAlpha(float toAlpha) {
        this.toAlpha = toAlpha;
        return this;
    }

    @Override
    public Animation rotation(float rotationValue) {
        this.rotationValue = rotationValue;
        return this;
    }

    @Override
    public Animation fromScaleX(float fromScaleX) {
        this.fromScaleX = fromScaleX;
        return this;
    }

    @Override
    public Animation toScaleX(float toScaleX) {
        this.toScaleX = toScaleX;
        return this;
    }

    @Override
    public Animation fromScaleY(float fromScaleY) {
        this.fromScaleY = fromScaleY;
        return this;
    }

    @Override
    public Animation toScaleY(float toScaleY) {
        this.toScaleY = toScaleY;
        return this;
    }

    @Override
    public Animation setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    @Override
    public Animation setUpdateListener(
            final ViewPropertyAnimatorUpdateListener updateListener) {
        this.updateListener = updateListener;
        return this;
    }

    @Override
    public Animation setAnimatorListener(ViewPropertyAnimatorListener animatorListener) {
        this.animatorListener = animatorListener;
        return this;
    }

    @Override
    public Animation setDuration(long time) {
        this.time = time;
        return this;
    }

    @Override
    public Animation withEndAction(Runnable endRunnable) {
        this.endRunnable = endRunnable;
        return this;
    }

    @Override
    public Animation setSlideMode(@AnimSlideMode int slideMode) {
        this.slideMode = slideMode;
        return this;
    }

    @Override
    public Animation setStartDelay(long delay) {
        this.delay = delay;
        return this;
    }

    @Override
    public void start() {

    }
}
