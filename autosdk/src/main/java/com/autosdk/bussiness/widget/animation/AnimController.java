package com.autosdk.bussiness.widget.animation;

import android.view.View;
import android.view.Window;

import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.annotation.StyleRes;

/**
 * @author AutoSDK
 */
public class AnimController {
    /**
     * 是否开启动画
     */
    private static boolean isAnim = true;

    private AnimController() {
    }

    /**
     * 属性动画
     *
     * @param view
     * @return
     */
    public static Animation animate(View view) {
        if (isAnim) {
            return new CustomAnimation(view);
        } else {
            return new EmptyAnimation(view);
        }
    }

    /**
     * dialog设置动画
     *
     * @param window dialog的windows
     * @param resId
     */
    public static void setWindowAnimations(Window window, @StyleRes int resId) {
        if (!isAnim) {
            return;
        }
        if (null != window) {
            window.setWindowAnimations(resId);
        }

    }

    /**
     * Fragment 进入退出动画,需要在commit之前添加
     *
     * @param clazz
     * @param enter
     * @param popExit
     */
    public static void setCustomAnimations(Class<?> clazz,
                                           @AnimatorRes @AnimRes int enter,
                                           @AnimatorRes @AnimRes int popExit) {
        setCustomAnimations(clazz, enter, popExit);
    }

    public static void setCustomAnimations(Class<?> clazz,
                                           @AnimatorRes @AnimRes int enter,
                                           @AnimatorRes @AnimRes int exit,
                                           @AnimatorRes @AnimRes int popEnter,
                                           @AnimatorRes @AnimRes int popExit) {
        if (!isAnim) {
            return;
        }
        // TODO
//        AnimBuilder animBuilder = new AnimBuilder();
//        animBuilder.newEnterAnim(enter).newExitAnim(exit).oldEnterAnim(popEnter).oldExitAnim(popExit);
//        AutoContext autoContext = (AutoContext) SdkApplicationUtils.getApplication();
//        FragmentContainerManagerService fcm = (FragmentContainerManagerService) autoContext.getAutoService(AutoContext.FRAGMENT_MANAGER_SERVICE);
//        fcm.setCustomAnimations(clazz, animBuilder);
    }


}
