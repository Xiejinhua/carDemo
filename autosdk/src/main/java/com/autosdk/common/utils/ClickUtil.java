package com.autosdk.common.utils;

import android.os.SystemClock;

/**
 * 重复点击事件判断
 */
public class ClickUtil {
    private static long mLastClickTime;

    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(500);
    }

    public static boolean isFastDoubleClick(long repeatTime) {
        /**
         * 采用 开机时间，避免用户手动调整时间，导致无法点击出错
         */
        long time = SystemClock.elapsedRealtime();
        if (Math.abs(time - mLastClickTime) < repeatTime) {
            return true;
        }
        mLastClickTime = time;
        return false;
    }
}
