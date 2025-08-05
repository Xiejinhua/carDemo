package com.autosdk.bussiness.map;

import com.autonavi.gbl.map.model.EGLDeviceID;
import com.autonavi.gbl.map.model.MapEngineID;

/**
 * 地图工具类
 * @author AutoSDK
 */
public class MapDeviceUtils {

    /**
     *  EGL设备ID转换为EngineID
     *  @param deviceId EGL设备ID
     */
    public static @MapEngineID.MapEngineID1 int transform2EngineID(@EGLDeviceID.EGLDeviceID1 int deviceId) {
        return (deviceId+1) * 2 - 1;
    }

    /**
     * 获取指定EGL设备鹰眼图EngineID
     * @param deviceId EGL设备ID
     * @return
     */
    public static @MapEngineID.MapEngineID1 int getEagleEyeEngineID(@EGLDeviceID.EGLDeviceID1 int deviceId) {
        int mainEngineID = transform2EngineID(deviceId);
        return mainEngineID + 1;
    }

    /**
     * SDK的EngineID转换为EGL设备ID
     * @param engineID SDK的EngineID
     */
    public static @EGLDeviceID.EGLDeviceID1 int transform2EGLDeviceID(@MapEngineID.MapEngineID1 int engineID) {
        return ((engineID + 1) / 2) - 1;
    }

}
