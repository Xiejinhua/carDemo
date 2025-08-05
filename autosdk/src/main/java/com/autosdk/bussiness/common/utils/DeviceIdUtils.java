package com.autosdk.bussiness.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.autosdk.bussiness.widget.BusinessApplicationUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class DeviceIdUtils {

    /**
     * 获取设备deviceID
     * @return
     */
    public static String getDeviceId() {
        String deviceId = getMD5(getDeviceInfo());
        return deviceId;
    }

    private static String getDeviceInfo() {
        StringBuilder deviceId = new StringBuilder();
        String imei = getIMEI();
        String androidId = getAndroidId();
        String serial = getSerial();
        String deviceUUID = getDeviceUUID();
        deviceId.append(imei).append(androidId).append(serial).append(deviceUUID);
        return deviceId.toString();
    }

    /**
     * 获取IMEI
     * @return
     */
    @SuppressLint("MissingPermission")
    private static String getIMEI() {
        try {
            TelephonyManager tm = (TelephonyManager) BusinessApplicationUtils.getApplication()
                    .getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取设备的AndroidId
     * @return
     */
    private static String getAndroidId() {
        try {
            String androidId = Settings.Secure.getString(
                    BusinessApplicationUtils.getApplication().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            return androidId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取设备序列号
     * @return
     */
    private static String getSerial() {
        try {
            return Build.SERIAL;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取硬件设备UUID
     * 拼接设备硬件uuid，计算出一个和硬件设备相关的随机数
     * @return
     */
    private static String getDeviceUUID() {
        try {

            String dev = "3883756" +
                    Build.BOARD.length() % 10 +
                    Build.BRAND.length() % 10 +
                    Build.DEVICE.length() % 10 +
                    Build.HARDWARE.length() % 10 +
                    Build.ID.length() % 10 +
                    Build.MODEL.length() % 10 +
                    Build.PRODUCT.length() % 10 +
                    Build.SERIAL.length() % 10;
            return new UUID(dev.hashCode(),Build.SERIAL.hashCode()).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UUID.randomUUID().toString();
    }

    /**
     * MD5加密
     * @param info
     * @return
     */
    private static String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] md5Array = md5.digest();
            return new BigInteger(1,md5Array).toString(16).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
