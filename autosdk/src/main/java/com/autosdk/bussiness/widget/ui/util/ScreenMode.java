package com.autosdk.bussiness.widget.ui.util;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 屏幕类型定义
 * Created by AutoSdk.
 */
@IntDef({
    ScreenMode.UNKNOWN,
    ScreenMode.LANDSCAPE,
    ScreenMode.PORTRAIT,
    ScreenMode.LANDSCAPE_WIDE,
    ScreenMode.SQUARE
})
@Retention(RetentionPolicy.SOURCE)
public @interface ScreenMode {
    /**
     * 未指定
     */
    int UNKNOWN = 0;
    /**
     * 横屏
     */
    int LANDSCAPE = 1;
    /**
     * 竖屏
     */
    int PORTRAIT = 2;
    /**
     * 横向宽屏
     */
    int LANDSCAPE_WIDE = 3;
    /**
     * 方屏
     */
    int SQUARE = 4;
}
