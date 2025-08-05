package com.autosdk.bussiness.common.utils;

import android.os.Looper;

public class CommonUtil {

    private static final String TAG = "CommonUtil";
    /**
     * 获取当前线程信息, 线程名/id/是否主线程等
     */
    public static String getThreadInfo() {
        Thread thread = Thread.currentThread();
        return "name:" + thread.getName() + ",id:" + thread.getId() + ",isMain:" + isMainThread();
    }
    /**
     * 判断当前是否在主线程
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
    /**
     * 获取主线程id
     */
    public static long getMainThreadId() {
        return Looper.getMainLooper().getThread().getId();
    }

    /**
     * 获取当前线程id
     */
    public static long getcurrentThreadId() {
        return Thread.currentThread().getId();
    }
}
