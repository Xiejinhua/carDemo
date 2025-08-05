package com.autosdk.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import com.autosdk.BuildConfig;
import com.autosdk.common.SdkApplicationUtils;

/**
 * 网络工具类
 */
public class SdkNetworkUtil {
    private volatile static SdkNetworkUtil mInstance = null;
    private boolean isNetworkConnected;
    private boolean isWifiConnected;
    private boolean isMobileConnected;

    public static SdkNetworkUtil getInstance() {
        if (null == mInstance) {
            synchronized (SdkNetworkUtil.class) {
                if (null == mInstance) {
                    mInstance = new SdkNetworkUtil();
                }
            }
        }
        return mInstance;
    }

    public void setNetworkConnected(boolean isNetworkConnected){
        this.isNetworkConnected = isNetworkConnected;
    }

    public boolean isNetworkConnected() {
        return isNetworkConnected || hasNetworkConnected();
    }

    public void setWifiConnected(boolean isWifiConnected){
        this.isWifiConnected = isWifiConnected;
    }

    public boolean isWifiConnected() {
        return isWifiConnected || hasWifiConnected();
    }

    public void setMobileConnected(boolean isMobileConnected){
        this.isMobileConnected = isMobileConnected;
    }

    public Boolean isMobileConnected() {
        return isMobileConnected || hasMobileConnected();
    }


    /**
     * 判断网络是否连接
     * @return
     */
    @SuppressLint("MissingPermission")
    public static boolean hasNetworkConnected() {
        if (BuildConfig.autoDevicesType == 1) {
            ConnectivityManager mConnectivityManager =
                    (ConnectivityManager) SdkApplicationUtils.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return mNetworkInfo != null && mNetworkInfo.isConnected() && mNetworkInfo.isAvailable();
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                ConnectivityManager connMgr =
                        (ConnectivityManager) SdkApplicationUtils.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            } else {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) SdkApplicationUtils.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager.getActiveNetwork() == null) {
                    return false;
                }
                NetworkCapabilities networkCapabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return networkCapabilities != null &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        }
    }

    /**
     * 判断 Wi-Fi 是否连接
     * @return
     */
    @SuppressLint("MissingPermission")
    public static boolean hasWifiConnected() {
        if (BuildConfig.autoDevicesType == 1) {
            if (SdkApplicationUtils.getApplication() != null) {
                ConnectivityManager mConnectivityManager =
                        (ConnectivityManager) SdkApplicationUtils.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return mWiFiNetworkInfo != null && mWiFiNetworkInfo.isConnected();
            }
            return false;
        } else {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) SdkApplicationUtils.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return networkInfo != null && networkInfo.isConnected();
            } else {
                if (connectivityManager.getActiveNetwork() == null) {
                    return false;
                }
                NetworkCapabilities networkCapabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return networkCapabilities != null &&
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        }
    }

    /**
     * 判断移动网络是否连接
     * @return
     */
    @SuppressLint("MissingPermission")
    public static boolean hasMobileConnected() {
        if (BuildConfig.autoDevicesType == 1) {
            if (SdkApplicationUtils.getApplication() != null) {
                ConnectivityManager mConnectivityManager =
                        (ConnectivityManager) SdkApplicationUtils.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                return mMobileNetworkInfo != null && mMobileNetworkInfo.isConnected();
            }
            return false;
        } else {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) SdkApplicationUtils.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                return networkInfo != null && networkInfo.isConnected();
            } else {
                if (connectivityManager.getActiveNetwork() == null) {
                    return false;
                }
                NetworkCapabilities networkCapabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return networkCapabilities != null &&
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            }
        }
    }
}
