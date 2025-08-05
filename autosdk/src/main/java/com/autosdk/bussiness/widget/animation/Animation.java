package com.autosdk.bussiness.widget.animation;

import android.view.animation.Interpolator;

import androidx.annotation.FloatRange;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.core.view.ViewPropertyAnimatorUpdateListener;

/**
 * @author AutoSDK
 */
public interface Animation {

    Animation fromTranslationX(float fromTranslationX);

    Animation toTranslationX(float toTranslationX);

    Animation fromAlpha(@FloatRange(from=0.0, to=1.0) float alpha);

    Animation toAlpha(@FloatRange(from=0.0, to=1.0) float alpha);

    Animation rotation(float rotationValue);

    Animation fromScaleX(float fromScaleX);

    Animation toScaleX(float toScaleX);

    Animation fromScaleY(float fromScaleY);

    Animation toScaleY(float toScaleY);

    /**
     *
     * @param interpolator
     * @return
     */
    Animation setInterpolator(Interpolator interpolator);

    /**
     *
     * @param updateListener
     * @return
     */
    Animation setUpdateListener(final ViewPropertyAnimatorUpdateListener updateListener);

    /**
     *
     * @param animatorListener
     * @return
     */
    Animation setAnimatorListener(ViewPropertyAnimatorListener animatorListener);

    /**
     * @param time 默认400毫秒
     * @return
     */
    Animation setDuration(long time);

    Animation withEndAction(Runnable runnable);

    Animation setSlideMode(@AnimSlideMode int slideMode);

    Animation setStartDelay(long delay);

    void start();
}
