package com.autosdk.common.utils;

import static com.autosdk.common.storage.MapSharePreference.SharePreferenceKeyEnum.storagePath;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.text.TextUtils;

import com.autosdk.BuildConfig;
import com.autosdk.common.SdkApplicationUtils;
import com.autosdk.common.storage.MapSharePreference;

import timber.log.Timber;

/**
 * 获取auto 文件保存基础路径
 */

public class ReqStorageController {
    private final static String TAG = "PermissionReqController";
    private static final int ANDROID_VERSION_30 = VERSION_CODES.R;
    private MapSharePreference mMapSharePreference;
    public static final String sandBox = SdkApplicationUtils.getApplication().getExternalFilesDir(null) + "/"; //沙箱环境
    public static final String sdcardPath = Environment.getExternalStorageDirectory().getPath() + "/";

    private static class ControllerInstance {
        private static ReqStorageController instance = new ReqStorageController();
    }

    private ReqStorageController() {
        mMapSharePreference = new MapSharePreference(MapSharePreference.SharePreferenceName.permission);
    }

    public static ReqStorageController getInstance() {
        return ControllerInstance.instance;
    }

    /**
     * 获取当前可用路径
     *
     * @return sdk>=30 直接走沙箱 , <30 走授权和判断权限
     */
    public String reqStoragePath() {
        String result = BuildConfig.specifyAutoBasePath;
        if (!BuildConfig.isEnableSpecifyAutoPath) {//是否启用指定目录
            if (Build.VERSION.SDK_INT >= ANDROID_VERSION_30) {
                result = SdkApplicationUtils.getApplication().getExternalFilesDir(null) + "/";
            } else {
                result = reqUsedPath();
            }
        }
        savedPath(result);
        Timber.i("reqStoragePath %s", result);
        return result;
    }

    /**
     * sdk<30
     *
     * @return 获取当前的存储路径
     */
    private String reqUsedPath() {
        if (null == mMapSharePreference) {
            return sandBox;
        }
        String result = mMapSharePreference.getStringValue(storagePath, "");
        boolean permissions = checkPermissions(SdkApplicationUtils.getApplication(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (TextUtils.isEmpty(result) && permissions) {
            return sdcardPath;
        } else if (result.equals(sdcardPath) && permissions) {
            return sdcardPath;
        } else {
            return sandBox;
        }
    }

    /**
     * 保存存储路径
     *
     * @param result
     */
    private void savedPath(String result) {
        if (null != mMapSharePreference) {
            mMapSharePreference.putStringValue(storagePath, result);
        }
    }

    /**
     * 检测是否有缺少的权限
     *
     * @param appContext  applicationContext
     * @param permissions 需要的权限
     * @return
     */
    public boolean checkPermissions(Context appContext, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (appContext.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
