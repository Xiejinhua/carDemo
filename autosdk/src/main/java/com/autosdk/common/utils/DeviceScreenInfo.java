package com.autosdk.common.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.autosdk.common.SdkApplicationUtils;

import java.lang.reflect.Method;

import timber.log.Timber;

/**
 * 屏幕硬件的宽高.
 */

public class DeviceScreenInfo {
    /**
     * 屏幕硬件的宽高
     */
    public final int deviceWidth;
    public final int deviceHeight;
    public final int deviceDensityDpi;

    public DeviceScreenInfo() {
        Context context = SdkApplicationUtils.getApplication();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int realWidth = metrics.widthPixels;
        int realHeight = metrics.heightPixels;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(metrics);
            realWidth = metrics.widthPixels;
            realHeight = metrics.heightPixels;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
            }
        }
        int dpi;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            dpi = config.densityDpi;
        } else {
            dpi = context.getResources().getDisplayMetrics().densityDpi;
        }
        this.deviceWidth = realWidth;
        this.deviceHeight = realHeight;
        this.deviceDensityDpi = dpi;
        Timber.d("deviceWidth = %s, deviceHeight = %s, deviceDensityDpi = %s", this.deviceWidth, this.deviceHeight, this.deviceDensityDpi);
    }

    @Override
    public String toString() {
        return "DeviceScreenInfo{" +
                "deviceWidth=" + deviceWidth +
                ", deviceHeight=" + deviceHeight +
                ", deviceDensityDpi=" + deviceDensityDpi +
                '}';
    }

    /**
     * 在根布局尚未加载的时候（比如后台自启），提供一个默认的显示区域宽度
     * TODO 适配层待处理LMQ
     */
    public int getDefaultDisplayWidth() {
        if (SdkApplicationUtils.getApplication().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return Math.max(deviceWidth, deviceHeight);
        } else {
            return Math.min(deviceWidth, deviceHeight);
        }
    }

    /**
     * 在根布局尚未加载的时候（比如后台自启），提供一个默认的显示区域高度
     * TODO 适配层待处理LMQ
     */
    public int getDefaultDisplayHeight() {
        if (SdkApplicationUtils.getApplication().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return Math.min(deviceWidth, deviceHeight);
        } else {
            return Math.max(deviceWidth, deviceHeight);
        }
    }
}
