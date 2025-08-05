package com.autosdk.common.display;


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
    ScreenMode.SQUARE,
    ScreenMode.SMALL_PORTRAIT,
    ScreenMode.SMALL_LANDSCAPE,
    ScreenMode.SPECIAL_LANDSCAPE,
    ScreenMode.SPECIAL_LANDSCAPE_WIDE
})
@Retention(RetentionPolicy.SOURCE)
public @interface ScreenMode {
    /**
     * 未指定
     */
    int UNKNOWN = -1;
    /**
     * 横屏
     */
    int LANDSCAPE = 0;
    /**
     * 长横屏
     */
    int SPECIAL_LANDSCAPE = 6;
    /**
     * 竖屏
     */
    int PORTRAIT = 1;
    /**
     * 横向宽屏(大屏)
     */
    int LANDSCAPE_WIDE = 2;

    /**
     * 大屏 sw1080
     */
    int SPECIAL_LANDSCAPE_WIDE = 7;

    /**
     * 小竖屏
     */
    int SMALL_PORTRAIT = 4;
    /**
     * 小横屏
     */
    int SMALL_LANDSCAPE = 3;
    /**
     * 方屏
     */
    int SQUARE = 5;
}
