package com.desaysv.psmap.adapter

import android.content.Context
import android.util.Log
import com.desaysv.psmap.adapter.standard.IJsonProtocolReceive
import com.desaysv.psmap.adapter.standard.IServiceConnectListener
import com.desaysv.psmap.adapter.standard.JsonStandardServiceProxy
import com.desaysv.psmap.adapter.standard.VersionUtil

object JsonProtocolManager {
    private const val TAG = "SVJsonProtocolManager"
    private var mServiceConnectListener: IServiceConnectListener? = null

    private var mCallback: IJsonProtocolReceive? = null

    private var mProxy: JsonStandardServiceProxy? = null

    fun init(context: Context, listener: OnInitListener? = null) {
        if (mProxy == null) {
            mProxy = JsonStandardServiceProxy(context, object : IServiceConnectListener {
                override fun onServiceConnected() {
                    listener?.onInit()
                    mServiceConnectListener?.onServiceConnected()
                }

                override fun onServiceDisconnected() {
                    mServiceConnectListener?.onServiceDisconnected()

                }

                override fun onServiceDied() {
                    mServiceConnectListener?.onServiceDied()
                }

            }, object : IJsonProtocolReceive {
                override fun received(jsonString: String?) {
                    mCallback?.received(jsonString)
                }

                override fun receivedSync(jsonString: String?): String? {
                    return mCallback?.receivedSync(jsonString)
                }

            })
        }
    }

    fun destroy() {
        Log.i(TAG, "destroy")
        mProxy?.unbindService()
        mProxy = null
    }

    fun setServiceConnectListener(serviceConnectListener: IServiceConnectListener) {
        mServiceConnectListener = serviceConnectListener
    }

    fun getSDKVersion(): String {
        return VersionUtil.CLIENT_VERSION
    }

    fun request(jsonString: String) {
        Log.i(TAG, "request = $jsonString")
        mProxy?.request(jsonString)
    }

    fun setJsonProtocolReceive(jsonProtocolReceive: IJsonProtocolReceive) {
        Log.i(TAG, "setJsonProtocolReceive")
        mCallback = jsonProtocolReceive
    }

    interface OnInitListener {
        fun onInit()
    }
}