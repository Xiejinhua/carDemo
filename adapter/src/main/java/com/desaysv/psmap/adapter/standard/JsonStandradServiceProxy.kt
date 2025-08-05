package com.desaysv.psmap.adapter.standard

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.CountDownTimer
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.text.TextUtils
import android.util.Log
import com.desaysv.psmap.adapter.IStandardJsonProtocol
import com.desaysv.psmap.adapter.IStandardJsonProtocolCallback
import org.json.JSONObject

class JsonStandardServiceProxy(
    context: Context, serviceConnectListener: IServiceConnectListener,
    callback: IJsonProtocolReceive
) : ServiceConnection, IStandardJsonProtocolCallback.Stub() {
    private val TAG: String = "JsonStandardServiceProxy"
    private var mContext = context

    private var mAidl: IStandardJsonProtocol? = null

    private var mServiceConnectListener: IServiceConnectListener = serviceConnectListener

    private var mCallback: IJsonProtocolReceive = callback

    private var mPackageName = ""

    private val mTimer = object : CountDownTimer(8000, 5000) {
        override fun onTick(millisUntilFinished: Long) = Unit
        override fun onFinish() {
            if (mAidl == null) {
                Log.i(TAG, "rebind aidl")
                bindService()
            }
        }
    }

    private val deathRecipient = DeathRecipient {
        Log.i(TAG, "binderDied")
        mServiceConnectListener.onServiceDied()
        mAidl = null
        bindService()
    }

    init {
        mPackageName = mContext.packageName
        Log.i(TAG, "init packageName=${mPackageName}")
        bindService()
    }

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        Log.i(TAG, "onServiceConnected ")
        mAidl = IStandardJsonProtocol.Stub.asInterface(binder)
        mAidl?.asBinder()?.linkToDeath(deathRecipient, 0)
        mAidl!!.registerJsonMessageCallback(mPackageName, this)
        mTimer.cancel()
        mServiceConnectListener.onServiceConnected()
        requestServerVersion()
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        Log.i(TAG, "onServiceDisconnected")
        mAidl?.asBinder()?.unlinkToDeath(deathRecipient, 0)
        mAidl = null
        mServiceConnectListener.onServiceDisconnected()
        bindService()
    }

    override fun onBindingDied(name: ComponentName?) {
        super.onBindingDied(name)
        Log.i(TAG, "onBindingDied")
        mAidl?.asBinder()?.unlinkToDeath(deathRecipient, 0)
        mAidl = null
        mServiceConnectListener.onServiceDied()
        bindService()
    }

    private fun bindService() {
        Log.i(TAG, "bindService")
        mTimer.cancel()
        mContext.bindService(
            Intent("com.desaysv.psmap.model.service.MapStandardJsonProtocolService").apply {
                `package` = "com.desaysv.jetour.t1n.psmap"
            }, this, Context.BIND_AUTO_CREATE
        )
        mTimer.start()
    }

    private fun requestServerVersion() {
        Log.i(TAG, "requestServerVersion")
        val requestData = JSONObject().apply {
            put("requestCode", "0")
            put("needResponse", true)
            put("protocolId", ProtocolID.PROTOCOL_GET_VERSION)
            put("versionName", VersionUtil.CLIENT_VERSION)
            put("requestAuthor", mPackageName)
            put("messageType", "request")
            put("data", JSONObject())
        }
        request(requestData.toString())
    }

    fun unbindService() {
        mAidl?.run {
            unregisterJsonMessageCallback(mPackageName)
            mContext.unbindService(this@JsonStandardServiceProxy)
        }
        mAidl = null
        mTimer.cancel()
        Log.i(TAG, "unbindService")
    }

    fun isConnected(): Boolean {
        return mAidl != null
    }

    fun request(msg: String) {
        Log.i(TAG, "sendMassage msg=$msg")
        mAidl?.request(mPackageName, msg)
    }

    override fun onMassage(msg: String?) {
        Log.d(TAG, "onMassage msg=$msg")
        if (TextUtils.isEmpty(VersionUtil.getServerVersion())) {
            try {
                msg?.run {
                    val versionResponse = JSONObject(msg)
                    if (ProtocolID.PROTOCOL_GET_VERSION == versionResponse.getInt("protocolId")) {
                        VersionUtil.dealVersionCallback(versionResponse)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "", e)
            }
        }
        mCallback.received(msg)
    }

    override fun onSyncMassage(msg: String?): String? {
        Log.d(TAG, "onSyncMassage msg=$msg")
        return mCallback.receivedSync(msg)
    }

}