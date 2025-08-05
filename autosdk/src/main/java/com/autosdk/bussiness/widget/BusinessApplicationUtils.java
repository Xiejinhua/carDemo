package com.autosdk.bussiness.widget;

import android.app.Application;

import com.autosdk.bussiness.adapter.bean.AdapterCarAllInfo;
import com.autosdk.common.AutoConstant;

/**
 * 提供全局获取Application能力
 *
 * @author AutoSDK
 */
public class BusinessApplicationUtils {

    private static Application app;

    public static void setApplication(Application app) {
        BusinessApplicationUtils.app = app;
    }

    /**
     * 获取当前运用 application对象,任何地方，任何时间都可以直接获取到，不存在失败的问题。
     *
     * @return
     */
    public static Application getApplication() {
        return app;
    }

    public static int mScreenWidth = AutoConstant.mScreenWidth;
    public static int mScreenHeight = AutoConstant.mScreenHeight;

    /**
     * 性能源相关数据
     */
    private static AdapterCarAllInfo electricInfo = new AdapterCarAllInfo();

    public static void setElectricInfo(AdapterCarAllInfo electricInfo) {
        if (electricInfo != null) {
            BusinessApplicationUtils.electricInfo = electricInfo;
        }
    }

    public static AdapterCarAllInfo getElectricInfo() {
        return electricInfo;
    }
}
