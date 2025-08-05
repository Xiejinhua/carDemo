package com.autonavi.auto.common.util;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import com.autonavi.auto.skin.view.SkinConstraintLayout;

public class ViewTouchUtil {

    /**
     * 拦截事件分发，实现控件在disable情况下，可以响应事件（类似Toast提示）
     * @param callback 回调
     * @param views 点击控件
     */
    @SuppressLint("ClickableViewAccessibility")
    public static void handleEnable(@NonNull final Callback callback,View ...views){
        for (View view : views) {
            View tempView = null;
            if (view == null) {
                return;
            }
            if (view.getVisibility() != View.VISIBLE) {
                continue;
            }
            if (view instanceof ViewGroup) {
                ViewGroup targetViewGroup = (ViewGroup) view;
                for (int i = 0; i < targetViewGroup.getChildCount(); i++) {
                    targetViewGroup.getChildAt(i).setEnabled(!callback.condition());
                }
                tempView = targetViewGroup;
            }else {
                view.setEnabled(!callback.condition());
                ViewParent parent = view.getParent();
                if (parent != null) {
                    ViewGroup parentGroup = (ViewGroup) parent;
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    SkinConstraintLayout tmpLayout = new SkinConstraintLayout(view.getContext());
                    tmpLayout.setLayoutParams(layoutParams);
                    parentGroup.addView(tmpLayout);
                    tempView = tmpLayout;
                }
            }
            if (tempView != null) {
                tempView.setOnTouchListener(createOnTouchEnableListener(callback,view));
            }
        }

    }

    private static View.OnTouchListener createOnTouchEnableListener(final Callback callback,final View targetView){
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (checkActionUp(v, event)) {
                    callback.operate(targetView);
                    return true;
                }
                return callback.condition() && event.getAction() == MotionEvent.ACTION_DOWN;
            }
        };
    }

    private static boolean checkActionUp(View v,MotionEvent event){
        return event.getAction() == MotionEvent.ACTION_UP && isInside(v,event);
    }

    private static boolean isInside(View v,MotionEvent event){
        int [] location = new int[2];
        v.getLocationInWindow(location);
        float startX = location[0];
        float endX = startX + v.getWidth();
        float startY = location[1];
        float endY = startY + v.getHeight();
        return event.getRawX() > startX && event.getRawX() < endX && event.getRawY() > startY && event.getRawY() < endY;
    }

    public interface Callback{
        void operate(View targetView);
        boolean condition();
    }
}
