package com.desaysv.psmap.adapter.command

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.collection.ArraySet
import com.desaysv.psmap.adapter.MapAPIManager
import com.zyt.autoivi.sdk.AutoDataManager
import com.zyt.autoivi.sdk.IAutoDataCallback
import com.zyt.autoivi.sdk.IAutoiviMapInterface

class MapNavigationServiceProxy(context: Context) : ServiceConnection, IAutoDataCallback {
    private val TAG: String = "MapNavigationServiceProxy"
    private var mContext = context

    private var mAidl: IAutoiviMapInterface? = null

    private var mCallback: ArraySet<MapAPIManager.MapNavigationServiceCallback> = ArraySet()

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
        mAidl = IAutoiviMapInterface.Stub.asInterface(binder)
        mAidl?.asBinder()?.linkToDeath(deathRecipient, 0)
        AutoDataManager.registerCallback(this)
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
        AutoDataManager.unregisterCallback(this)
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
            Intent("com.zyt.autoivi.sdk.src.main.aidl.IAutoiviMapInterface").apply {
                `package` = "com.zyt.autoivi.map"
            }, this, Context.BIND_AUTO_CREATE
        )
        mTimer.start()
    }

    fun unbindService() {
        try {
            mAidl?.unregisterCallback()
            mContext.unbindService(this)
        } catch (e: Exception) {
            Log.w(TAG, e)
        }
        mAidl = null
        mCallback.clear()
        mTimer.cancel()
        Log.i(TAG, "unbindService")
    }

    fun registerMapNavigationServiceCallback(callback: MapAPIManager.MapNavigationServiceCallback) {
        Log.i(TAG, "registerMapNavigationServiceCallback callback=$callback")
        mCallback.add(callback)
    }

    fun unregisterMapNavigationServiceCallback(callback: MapAPIManager.MapNavigationServiceCallback) {
        Log.i(TAG, "unregisterMapNavigationServiceCallback")
        mCallback.remove(callback)
    }

    fun isConnected(): Boolean {
        return mAidl != null
    }

    override fun onDrivingAppInfoUpdate(data: ByteArray?) {
        for (callBack in mCallback) {
            callBack.onDrivingAppInfoUpdate(data)
        }
    }

    override fun onRouteDelete(mapID: Int) {
        for (callBack in mCallback) {
            callBack.onRouteDelete(mapID)
        }
    }

}