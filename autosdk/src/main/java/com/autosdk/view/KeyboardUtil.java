package com.autosdk.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.autosdk.common.SdkApplicationUtils;

import timber.log.Timber;

/**
 * 软键盘相关工具
 */
public class KeyboardUtil {
    private static final int TAG_ON_GLOBAL_LAYOUT_LISTENER = -8;
    private static int sDecorViewDelta = 0;

    public static void showSoftInput() {
        InputMethodManager imm = (InputMethodManager) SdkApplicationUtils.getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 显示键盘
     */
    public static void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            view.requestFocus();
            imm.showSoftInput(view, 0);
        }
    }

    /**
     * 隐藏键盘
     */
    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // 系统键盘显示时才需要关闭
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 切换键盘的显隐状态
     */
    public static void toggleSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(0, 0);
        }
    }

//    /**
//     * 解决点击EditText点击外部区域软键盘隐藏
//     */
//    public boolean dispatchTouchEvent(Activity hostActivity, MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            View v = hostActivity.getCurrentFocus();
//            if (isShouldHideInput(v, ev)) { //需要隐藏软键盘
//                hideKeyboard(v);
////                InputMethodManager imm = (InputMethodManager) hostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
////                if (imm != null) {
////                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
////                }
////                mEditText.setCursorVisible(false);
//            }
//            return super.dispatchTouchEvent(ev);
//        }
//        // 必不可少，否则所有的组件都不会有TouchEvent了
//        if (getWindow().superDispatchTouchEvent(ev)) {
//            return true;
//        }
//        return onTouchEvent(ev);
//    }

    /**
     * 判断当前点击的位置是否为EditText
     */
    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if ((v instanceof EditText)) { //如果点击的view是EditText
                int[] leftTop = {0, 0};
                //获取输入框当前的location位置
                v.getLocationInWindow(leftTop);
                int left = leftTop[0];
                int top = leftTop[1];
                int bottom = top + v.getHeight();
                int right = left + v.getWidth();
                // 点击的是输入框区域，保留点击EditText的事件
                return !(event.getX() > left) || !(event.getX() < right)
                        || !(event.getY() > top) || !(event.getY() < bottom);
            }
        }
        return false;
    }

    /**
     * Register soft input changed listener.
     * @param window   The window.
     * @param listener The soft input changed listener.
     */
    public static void registerSoftInputChangedListener(@NonNull final Window window,
                                                        @NonNull final OnSoftInputChangedListener listener) {
        final int flags = window.getAttributes().flags;
        if ((flags & WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        final FrameLayout contentView = window.findViewById(android.R.id.content);
        final int[] decorViewInvisibleHeightPre = {getDecorViewInvisibleHeight(window)};
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = getDecorViewInvisibleHeight(window);
                if (decorViewInvisibleHeightPre[0] != height) {
                    listener.onSoftInputChanged(height);
                    decorViewInvisibleHeightPre[0] = height;
                }
            }
        };
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        contentView.setTag(TAG_ON_GLOBAL_LAYOUT_LISTENER, onGlobalLayoutListener);
    }

    /**
     * Unregister soft input changed listener.
     * @param window The window.
     */
    public static void unregisterSoftInputChangedListener(@NonNull final Window window) {
        final View contentView = window.findViewById(android.R.id.content);
        if (contentView == null) return;
        Object tag = contentView.getTag(TAG_ON_GLOBAL_LAYOUT_LISTENER);
        if (tag instanceof ViewTreeObserver.OnGlobalLayoutListener) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                contentView.getViewTreeObserver().removeOnGlobalLayoutListener((ViewTreeObserver.OnGlobalLayoutListener) tag);
            }
        }
    }

    private static int getDecorViewInvisibleHeight(@NonNull final Window window) {
        final View decorView = window.getDecorView();
        final Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);
        Timber.d("getDecorViewInvisibleHeight: %s", (decorView.getBottom() - outRect.bottom));
        int delta = Math.abs(decorView.getBottom() - outRect.bottom);
        if (delta <= getNavBarHeight() + getStatusBarHeight()) {
            sDecorViewDelta = delta;
            return 0;
        }
        return delta - sDecorViewDelta;
    }

    /**
     * Return the navigation bar's height.
     * @return the navigation bar's height
     */
    public static int getNavBarHeight() {
        Resources res = SdkApplicationUtils.getApplication().getResources();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    /**
     * Return the status bar's height.
     * @return the status bar's height
     */
    public static int getStatusBarHeight() {
        Resources resources = SdkApplicationUtils.getApplication().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public interface OnSoftInputChangedListener {
        void onSoftInputChanged(int height);
    }
}
