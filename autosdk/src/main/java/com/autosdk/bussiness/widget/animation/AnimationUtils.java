package com.autosdk.bussiness.widget.animation;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.view.ViewPropertyAnimatorUpdateListener;

import com.autonavi.auto.skin.view.SkinConstraintLayout;

/**
 * 控件动画公共类
 * @author AutoSDK
 **/
public class AnimationUtils {
    /**
     * 图片旋转动画
     *
     * @param arrowView 动画控件
     * @param rotaValue 旋转角度
     * @param time      动画时长
     */
    public static void rotationArrowView(View arrowView, int rotaValue, int time) {
        AnimController.animate(arrowView).rotation(rotaValue).setDuration(time).setInterpolator(new LinearInterpolator()).setStartDelay(300).start();
    }

    /**
     * 图片缩放动画
     *
     * @param collectionView 缩放控件
     * @param time           动画时长
     */
    public static void scaleCollectionView(View collectionView, int time) {
        AnimController.animate(collectionView).fromScaleX(0).toScaleX(1).fromScaleY(0).toScaleY(1).setDuration(time).start();
    }

    /**
     * 控件从右往左弹出
     *
     * @param parent 弹出控件
     * @param time   动画时长
     */
    public static void translationEnter(final View parent, final View content, final int time, @Nullable final ViewPropertyAnimatorUpdateListener viewPropertyAnimatorUpdateListener) {
        AnimController.animate(parent).setSlideMode(AnimSlideMode.LEFT_IN).fromAlpha(0f).toAlpha(1f).setDuration(time).start();

    }

    /**
     * 缩放控件移动
     *
     * @param parent 弹出控件
     * @param time   动画时长
     */
    public static void translationX(SkinConstraintLayout parent, int translationX, int time, final ViewPropertyAnimatorUpdateListener viewPropertyAnimatorUpdateListener) {
        AnimController.animate(parent).toTranslationX(translationX).setUpdateListener(new ViewPropertyAnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(View view) {
                viewPropertyAnimatorUpdateListener.onAnimationUpdate(view);
            }
        }).setDuration(time).start();
    }

    public interface AnimationListener {
        void onAnimationEnd(View view);
    }

    /**
     * 关闭帧动画
     *
     * @param imageView
     */
    public static void stopAnimationDrawable(ImageView imageView) {
        if (imageView.getDrawable() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) imageView.getDrawable();
            if (animation.isRunning()) {
                animation.stop();
            }
        }
    }

    /**
     * 启动帧动画
     *
     * @param imageView
     */
    public static void startAnimationDrawable(ImageView imageView) {
        if (imageView.getDrawable() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) imageView.getDrawable();
            if (animation.isRunning()) {
                animation.stop();
            }
            animation.start();
        }
    }

    /**
     * 渐变闪烁
     * @param view
     */
    public static void startFlicker(View view){
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f,1.0f);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(alphaAnimation);
    }

    public static void cleanAnimation(View view){
        view.clearAnimation();
    }
}
