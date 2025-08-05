package com.autosdk.bussiness.widget.animation;
/**
 * @author AutoSDk
 */
public class AnimBuilder {
    //新的fragment进场动画
    private int enterAnim;
    //当前fragment出场动画
    private int exitAnim;
    //之前的fragment进场动画
    private int popEnterAnim;
    //新的fragment出场动画
    private int popExitAnim;

    public AnimBuilder() {
    }

    public int getEnterAnim() {
        return enterAnim;
    }

    public void setEnterAnim(int enterAnim) {
        this.enterAnim = enterAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public void setExitAnim(int exitAnim) {
        this.exitAnim = exitAnim;
    }

    public int getPopEnterAnim() {
        return popEnterAnim;
    }

    public void setPopEnterAnim(int popEnterAnim) {
        this.popEnterAnim = popEnterAnim;
    }

    public int getPopExitAnim() {
        return popExitAnim;
    }

    public void setPopExitAnim(int popExitAnim) {
        this.popExitAnim = popExitAnim;
    }
}
