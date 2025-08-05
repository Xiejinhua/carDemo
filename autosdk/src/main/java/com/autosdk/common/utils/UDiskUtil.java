package com.autosdk.common.utils;

import android.content.Context;
import android.os.storage.StorageManager;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import timber.log.Timber;

public class UDiskUtil {

    public static String getUDiskPath(Context context) {
        String path = "";
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> volumeInfoClazz;
        Class<?> diskInfoClaszz;
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            diskInfoClaszz = Class.forName("android.os.storage.DiskInfo");
            Method StorageManager_getVolumes = Class.forName("android.os.storage.StorageManager").getMethod(
                    "getVolumes");
            Method VolumeInfo_GetDisk = volumeInfoClazz.getMethod("getDisk");
            Method VolumeInfo_GetPath = volumeInfoClazz.getMethod("getPath");
            Method DiskInfo_IsUsb = diskInfoClaszz.getMethod("isUsb");
            List<Object> listVolumeInfo = (List<Object>) StorageManager_getVolumes.invoke(mStorageManager);
            assert listVolumeInfo != null;
            for (int i = 0; i < listVolumeInfo.size(); i++) {
                Object volumeInfo = listVolumeInfo.get(i);
                Object diskInfo = VolumeInfo_GetDisk.invoke(volumeInfo);
                if (diskInfo == null) {
                    continue;
                }
                boolean usb = (boolean) DiskInfo_IsUsb.invoke(diskInfo);
                File file = (File) VolumeInfo_GetPath.invoke(volumeInfo);
                if (usb && file != null) {//usb
                    path = file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e(e, "检测异常");
        }
        return path;
    }
}
