package com.autosdk.common.utils;

import timber.log.Timber;

/**
 * 资源管理类
 * 包含了地图数据路径获取
 *
 * @author AutoSDK
 */
public class SdkPathUtils {

    /**
     * 获取应用所在根目录，一般为sdcard
     *
     * @return
     */
    public static String getRootPath() {
        String basePath = ReqStorageController.getInstance().reqStoragePath();
        Timber.i("getRootPath %s", basePath);
        return basePath;
    }

}
