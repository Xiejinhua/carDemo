package com.desaysv.psmap.adapter.command

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.CountDownTimer
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import androidx.collection.ArraySet
import com.desaysv.psmap.adapter.IMapData
import com.desaysv.psmap.adapter.IMapDataCallback
import com.desaysv.psmap.adapter.MapAPIManager

class MapDataServiceProxy(context: Context) : ServiceConnection, IMapDataCallback.Stub() {
    private val TAG: String = "MapDataServiceProxy"
    private var mContext = context

    private var mAidl: IMapData? = null

    private var mCallback: ArraySet<MapAPIManager.MapDataServiceCallback> = ArraySet()

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

    init {
        mPackageName = mContext.packageName
        Log.i(TAG, "init packageName=${mPackageName}")
        bindService()
    }

    private val deathRecipient = IBinder.DeathRecipient {
        Log.i(TAG, "binderDied")
        for (callBack in mCallback) {
            callBack.onServiceConnect(false)
        }
        mAidl = null
        bindService()
    }

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        Log.i(TAG, "onServiceConnected ")
        mAidl = IMapData.Stub.asInterface(binder)
        mAidl?.asBinder()?.linkToDeath(deathRecipient, 0)
        try {
            mAidl?.registerMapDataCallback(mPackageName, this)
        } catch (e: DeadObjectException) {
            Log.w(TAG, "onServiceConnected error", e)
            bindService()
            return
        }
        mTimer.cancel()
        for (callBack in mCallback) {
            callBack.onServiceConnect(true)
        }

    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        Log.i(TAG, "onServiceDisconnected")
        mAidl?.asBinder()?.unlinkToDeath(deathRecipient, 0)
        mAidl = null
        for (callBack in mCallback) {
            callBack.onServiceConnect(false)
        }
        bindService()
    }

    override fun onBindingDied(name: ComponentName?) {
        super.onBindingDied(name)
        Log.i(TAG, "onBindingDied")
        mAidl?.asBinder()?.unlinkToDeath(deathRecipient, 0)
        mAidl = null
        for (callBack in mCallback) {
            callBack.onServiceConnect(false)
        }
        bindService()
    }

    private fun bindService() {
        Log.i(TAG, "bindService")
        mTimer.cancel()
        mContext.bindService(
            Intent("com.desaysv.psmap.model.service.MapDataOutputService").apply {
                `package` = "com.desaysv.jetour.t1n.psmap"
            }, this, Context.BIND_AUTO_CREATE
        )
        mTimer.start()
    }

    fun unbindService() {
        try {
            mAidl?.unregisterMapDataCallback(mPackageName)
            mContext.unbindService(this)
        } catch (e: Exception) {
            Log.w(TAG, e)
        }
        mAidl = null
        mCallback.clear()
        mTimer.cancel()
        Log.i(TAG, "unbindService")
    }

    fun registerMapDataServiceCallback(callback: MapAPIManager.MapDataServiceCallback) {
        Log.i(TAG, "registerMapDataServiceCallback callback=$callback")
        mCallback.add(callback)
    }

    fun unregisterMapDataServiceCallback(callback: MapAPIManager.MapDataServiceCallback) {
        Log.i(TAG, "unregisterMapDataServiceCallback")
        mCallback.remove(callback)
    }

    fun isConnected(): Boolean {
        return mAidl != null
    }

    fun sendMassage(msg: String) {
        Log.i(TAG, "sendMassage msg=$msg")
        mAidl?.sendMassage(mPackageName, msg)
    }

    fun getNaviStatus(): Int? {
        return mAidl?.naviStatus.also {
            Log.i(TAG, "getNaviStatus $it")
        }
    }

    override fun onMassage(pkg: String, msg: String?) {
        if (pkg == mPackageName) {
            for (callBack in mCallback) {
                callBack.onMassage(msg)
            }
        }
    }

    override fun onByteMassage(pkg: String?, msg: String?, byteArray: ByteArray?) {
        if (pkg == mPackageName) {
            for (callBack in mCallback) {
                callBack.onByteMassage(msg, byteArray)
            }
        }
    }


}