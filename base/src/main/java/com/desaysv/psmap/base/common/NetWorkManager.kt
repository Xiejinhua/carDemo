package com.desaysv.psmap.base.common

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.text.TextUtils
import com.autosdk.common.utils.SdkNetworkUtil
import com.desaysv.psmap.base.utils.CommonUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetWorkManager @Inject constructor(@ApplicationContext context: Context) : BroadcastReceiver() {
    private val mContext: Context = context
    private val listeners: MutableList<NetWorkChangeListener> = ArrayList()
    private var mConnectivityManager: ConnectivityManager? = null
    private var mIsWifiConnected = false
    private var mIsMobileConnected = false

    /**
     * 初始化网络监听
     */
    fun initNetWorkListener() {
        registerNetworkListener()
    }

    /**
     * 注册网络状态的监听；
     */
    @SuppressLint("MissingPermission")
    fun registerNetworkListener() {
        if (CommonUtils.isVehicle()) {
            mConnectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            mIsWifiConnected = isConnected(ConnectivityManager.TYPE_WIFI)
            mIsMobileConnected = isConnected(ConnectivityManager.TYPE_MOBILE)
            SdkNetworkUtil.getInstance().isNetworkConnected =
                isNetworkConnected(mContext) || isWifiConnected() || isMobileConnected()

            // 通过广播的方式监听网络；
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            mContext.registerReceiver(this, intentFilter)
        } else {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                    mConnectivityManager =
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                    mConnectivityManager?.registerDefaultNetworkCallback(MyNetworkCallback(mContext, this))
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    mConnectivityManager =
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                    val builder: NetworkRequest.Builder = NetworkRequest.Builder()
                    builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    val networkRequest = builder.build()
                    mConnectivityManager?.registerNetworkCallback(
                        networkRequest,
                        MyNetworkCallback(mContext, this)
                    )
                }

                else -> {
                    mConnectivityManager =
                        mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                    mIsWifiConnected = isConnected(ConnectivityManager.TYPE_WIFI)
                    mIsMobileConnected = isConnected(ConnectivityManager.TYPE_MOBILE)
                    SdkNetworkUtil.getInstance().isNetworkConnected =
                        isNetworkConnected(mContext) || isWifiConnected() || isMobileConnected()

                    // 通过广播的方式监听网络；
                    val intentFilter = IntentFilter()
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                    intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                    intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                    mContext.registerReceiver(this, intentFilter)
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun isConnected(networkType: Int): Boolean {
        val networkInfo: NetworkInfo? = mConnectivityManager?.getNetworkInfo(networkType)
        return networkInfo?.isConnected ?: false
    }


    private class MyNetworkCallback(@ApplicationContext context: Context, private val networkManager: NetWorkManager) :
        ConnectivityManager.NetworkCallback() {
        private val mContext: Context = context;

        //当用户与网络连接（或断开连接）（可以是WiFi或蜂窝网络）时，这两个功能均作为默认回调;
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Timber.i("onAvailable#network=$network")
            // 需要同步获取一次网络状态；
            val netWorkState = getNetWorkState(mContext)
            Timber.i("onAvailable#netWorkState=$netWorkState")
            networkManager.postNetWorkChange(networkManager.isNetworkConnected())
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Timber.i("onLost#network=$network")
            // 需要同步获取一次网络状态；
            val netWorkState = getNetWorkState(mContext)
            Timber.i("onLost#netWorkState=$netWorkState")
            networkManager.postNetWorkChange(networkManager.isNetworkConnected())
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            Timber.i("onCapabilitiesChanged#network=$network")
            //            LogUtils.d("onCapabilitiesChanged#network=" + network + ", networkCapabilities=" + networkCapabilities);
            // 表示能够和互联网通信（这个为true表示能够上网）
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Timber.i("onCapabilitiesChanged TRANSPORT_WIFI")
                    }

                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Timber.i("onCapabilitiesChanged TRANSPORT_CELLULAR")
                    }

                    else -> {
                        Timber.i("onCapabilitiesChanged OTHER NETWORK")
                    }
                }
            } else {
                Timber.i("onCapabilitiesChanged# NO NETWORK")
            }
            networkManager.postNetWorkChange(networkManager.isNetworkConnected())
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        mIsWifiConnected = isConnected(ConnectivityManager.TYPE_WIFI)
        mIsMobileConnected = isConnected(ConnectivityManager.TYPE_MOBILE)
        SdkNetworkUtil.getInstance().isNetworkConnected =
            isNetworkConnected(mContext) || isWifiConnected() || isMobileConnected()
        Timber.i("mIsWifiConnected:$mIsWifiConnected, mIsMobileConnected:$mIsMobileConnected")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (CommonUtils.isVehicle()) {
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (null != networkCapabilities && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                )) && TextUtils.equals(intent.action, ConnectivityManager.CONNECTIVITY_ACTION)
            ) {
                postNetWorkChange(isNetworkConnected())
            } else {
                Timber.i("CommonUtils.isVehicle() postNetWorkChange false")
                postNetWorkChange(false)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val networkCapabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (null != networkCapabilities && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR
                    )) && TextUtils.equals(intent.action, ConnectivityManager.CONNECTIVITY_ACTION)
                ) {
                    postNetWorkChange(isNetworkConnected())
                }
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo != null && networkInfo.isAvailable) {
                    Timber.i("onReceive networkInfo isAvailable type = ${networkInfo.type}")
                    if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                        // WiFi is connected
                    } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                        // Mobile is connected
                    }
                    postNetWorkChange(isNetworkConnected())
                } else {
                    Timber.i("onReceive networkInfo not Available")
                    postNetWorkChange(false)
                }
            }
        }

    }

    fun isNetworkConnected(): Boolean {
        val isNetworkConnected = isNetworkConnected(mContext) || isWifiConnected() || isMobileConnected()
        SdkNetworkUtil.getInstance().isNetworkConnected = isNetworkConnected
        Timber.i("isNetworkConnected:$isNetworkConnected")
        return isNetworkConnected
    }

    fun isWifiConnected(): Boolean {
        val isWifiConnected = isWifiConnected(mContext) || mIsWifiConnected
        SdkNetworkUtil.getInstance().isWifiConnected = isWifiConnected
        return isWifiConnected
    }

    fun isMobileConnected(): Boolean {
        val isMobileConnected = isMobileConnected(mContext) || mIsMobileConnected
        SdkNetworkUtil.getInstance().isMobileConnected = isMobileConnected
        return isMobileConnected
    }

    fun getNetWorkState(): NetworkStatus {
        return getNetWorkState(mContext)
    }

    fun getAutoNetworkStatus(): Int {
        return when (getNetWorkState()) {
            NetworkStatus.WIFI -> {
                com.autonavi.gbl.util.model.NetworkStatus.NetworkStatusWiFi
            }

            NetworkStatus.MOBILE -> {
                com.autonavi.gbl.util.model.NetworkStatus.NetworkStatus4G
            }

            NetworkStatus.OTHER -> {
                com.autonavi.gbl.util.model.NetworkStatus.NetworkStatusOther
            }

            NetworkStatus.NONE -> {
                com.autonavi.gbl.util.model.NetworkStatus.NetworkStatusNotReachable
            }

        }
    }

    companion object {
        @SuppressLint("MissingPermission")
        fun getNetWorkState(context: Context): NetworkStatus {
            return if (CommonUtils.isVehicle()) {
                when {
                    isWifiConnected(context) -> {
                        NetworkStatus.WIFI
                    }

                    isMobileConnected(context) -> {
                        NetworkStatus.MOBILE
                    }

                    isNetworkConnected(context) -> {
                        NetworkStatus.OTHER
                    }

                    else -> {
                        NetworkStatus.NONE
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val mobileNetInfo =
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    val wifiNetInfo =
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    if (mobileNetInfo != null && mobileNetInfo.isAvailable) {
                        NetworkStatus.MOBILE
                    } else if (wifiNetInfo != null && wifiNetInfo.isAvailable) {
                        NetworkStatus.WIFI
                    } else if (isNetworkConnected(context)) {
                        NetworkStatus.OTHER
                    } else {
                        NetworkStatus.NONE
                    }
                } else {
                    when {
                        isWifiConnected(context) -> {
                            NetworkStatus.WIFI
                        }

                        isMobileConnected(context) -> {
                            NetworkStatus.MOBILE
                        }

                        isNetworkConnected(context) -> {
                            NetworkStatus.OTHER
                        }

                        else -> {
                            NetworkStatus.NONE
                        }
                    }
                }
            }
        }

        /**
         * 判断网络是否连接
         *
         * @param context
         * @return
         */
        @SuppressLint("MissingPermission")
        fun isNetworkConnected(context: Context): Boolean {
            if (CommonUtils.isVehicle()) {
                val mConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isConnected && mNetworkInfo.isAvailable
                }
                return false
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    val connMgr =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkInfo = connMgr.activeNetworkInfo
                    return networkInfo != null && networkInfo.isConnected
                } else {
                    val connectivityManager = context
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork = connectivityManager.activeNetwork ?: return false
                    val networkCapabilities =
                        connectivityManager.getNetworkCapabilities(activeNetwork)
                    if (networkCapabilities != null) {
                        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    }
                }
            }
            return false
        }

        @SuppressLint("MissingPermission")
        fun isWifiConnected(context: Context): Boolean {
            if (CommonUtils.isVehicle()) {
                if (context != null) {
                    val mConnectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    if (mWiFiNetworkInfo != null) {
                        return mWiFiNetworkInfo.isConnected
                    }
                }
                return false
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    val connectivityManager = context
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkInfo = connectivityManager
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    if (networkInfo != null) {
                        return networkInfo.isConnected
                    }
                } else {
                    val connectivityManager = context
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork = connectivityManager.activeNetwork ?: return false
                    val networkCapabilities =
                        connectivityManager.getNetworkCapabilities(activeNetwork)
                    if (networkCapabilities != null) {
                        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    }
                }
            }
            return false
        }

        @SuppressLint("MissingPermission")
        fun isMobileConnected(context: Context): Boolean {
            if (CommonUtils.isVehicle()) {
                if (context != null) {
                    val mConnectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    if (mMobileNetworkInfo != null) {
                        return mMobileNetworkInfo.isConnected
                    }
                }
                return false
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    val connectivityManager = context
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkInfo = connectivityManager
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    if (networkInfo != null) {
                        return networkInfo.isConnected
                    }
                } else {
                    val connectivityManager = context
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork = connectivityManager.activeNetwork ?: return false
                    val networkCapabilities =
                        connectivityManager.getNetworkCapabilities(activeNetwork)
                    if (networkCapabilities != null) {
                        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    }
                }
            }
            return false
        }
    }

    enum class NetworkStatus(val type: Int, val msg: String) {
        NONE(-1, "无网络连接"),
        MOBILE(0, "移动网络连接"),
        WIFI(1, "WIFI连接"),
        OTHER(3, "其它连接")
    }

    //网络通知
    private fun postNetWorkChange(isNetworkConnected: Boolean) {
        for (listener in listeners) {
            listener.onNetWorkChangeListener(isNetworkConnected)
        }
    }

    fun ping(host: String, callback: PingCallback) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec("ping $host")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // 解析输出结果，判断是否成功
                    if (line?.contains("Reply from") == true) {
                        callback.onPingResult(true)
                        return@launch
                    }
                }
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            callback.onPingResult(false)
        }
    }

    //用百度进行ping
    @OptIn(DelicateCoroutinesApi::class)
    fun pingBaidu(callback: PingCallback): Job {
        return GlobalScope.launch(Dispatchers.IO) {
            try {
                val process = Runtime.getRuntime().exec("ping www.baidu.com")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // 解析输出结果，判断是否成功
                    if (line?.contains("Reply from") == true) {
                        callback.onPingResult(true)
                        return@launch
                    }
                }
                reader.close()
                callback.onPingResult(false)
            } catch (e: Exception) {
                Timber.d("pingBaidu Exception: ${e.message}")
                callback.onPingResult(false)
            }
        }
    }

    //接口实现ping
    interface PingCallback {
        fun onPingResult(success: Boolean)
    }

    interface NetWorkChangeListener {
        fun onNetWorkChangeListener(isNetworkConnected: Boolean) //网络变化通知
    }

    fun addNetWorkChangeListener(mListener: NetWorkChangeListener?) {
        if (mListener != null) listeners.add(mListener)
    }

    fun removeNetWorkChangeListener(mListener: NetWorkChangeListener?) {
        if (mListener != null) listeners.remove(mListener)
    }

    fun destroyNetWorkChangeListener() {
        listeners.clear()
    }
}
