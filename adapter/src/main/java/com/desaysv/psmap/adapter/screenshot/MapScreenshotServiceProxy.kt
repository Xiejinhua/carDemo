package com.desaysv.psmap.adapter.screenshot

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.CountDownTimer
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import android.view.Surface
import com.desaysv.psmap.adapter.IMapScreenshot
import com.desaysv.psmap.adapter.IMapScreenshotCallback
import com.desaysv.psmap.adapter.MapAPIManager

class MapScreenshotServiceProxy(context: Context) : ServiceConnection, IMapScreenshotCallback.Stub() {
    private val TAG: String = "MapScreenshotServiceProxy"
    private var mContext = context

    private var mAidl: IMapScreenshot? = null

    private var mCallback: MapAPIManager.ScreenshotServiceCallback? = null

    private var mIsReady: Boolean = false

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
        mCallback?.onServiceConnect(false)
        mAidl = null
        bindService()
    }

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        Log.i(TAG, "onServiceConnected")
        mAidl = IMapScreenshot.Stub.asInterface(binder)
        mAidl?.asBinder()?.linkToDeath(deathRecipient, 0)
        try {
            mAidl?.registerScreenshotCallback(this)
        } catch (e: DeadObjectException) {
            Log.w(TAG, "onServiceConnected error", e)
            bindService()
            return
        }
        mTimer.cancel()
        mCallback?.onServiceConnect(true)
        if (mIsReady) {
            mCallback?.onMapScreenshotReady()
        }

    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        Log.i(TAG, "onServiceDisconnected")
        mIsReady = false
        mAidl?.asBinder()?.unlinkToDeath(deathRecipient, 0)
        mAidl = null
        mCallback?.onServiceConnect(false)
        bindService()
    }

    override fun onBindingDied(name: ComponentName?) {
        super.onBindingDied(name)
        Log.i(TAG, "onBindingDied")
        mAidl?.asBinder()?.unlinkToDeath(deathRecipient, 0)
        mAidl?.unregisterScreenshotCallback(this)
        mAidl = null
        mIsReady = false
        mCallback?.onServiceConnect(false)
        bindService()
    }

    private fun bindService() {
        Log.i(TAG, "bindService")
        mTimer.cancel()
        mContext.bindService(
            Intent("com.desaysv.psmap.model.service.MapScreenshotService").apply {
                `package` = "com.desaysv.jetour.t1n.psmap"
            }, this, Context.BIND_AUTO_CREATE
        )
        mTimer.start()
    }

    fun unbindService() {
        try {
            mAidl?.unregisterScreenshotCallback(this)
            mContext.unbindService(this)
        } catch (e: Exception) {
            Log.w(TAG, e)
        }
        mAidl = null
        mCallback = null
        mTimer.cancel()
        Log.i(TAG, "unbindService")
    }

    fun registerScreenshotServiceCallback(callback: MapAPIManager.ScreenshotServiceCallback) {
        mCallback = callback
    }

    fun unregisterScreenshotServiceCallback() {
        mCallback = null
    }

    fun isConnected(): Boolean {
        return mAidl != null
    }

    override fun onMapScreenshotReady() {
        Log.i(TAG, "onMapScreenshotReady")
        mCallback?.onMapScreenshotReady()
        mIsReady = true
    }

    override fun onMapScreenshotpBitmapBuffer(pBitmapBuffer: ByteArray?) {
        mCallback?.onMapScreenshotpBitmapBuffer(pBitmapBuffer)
    }

    override fun onErrorInfo(code: Int, info: String?) {
        mCallback?.onErrorInfo(code, info)
    }

    fun addSurface(aSurfaceName: String, aSurface: Surface?, width: Int, height: Int, x: Int, y: Int) {
        Log.i(TAG, "addSurface aSurfaceName=$aSurfaceName aSurface=$aSurface width=$width height=$height x=$x y=$y")
        mAidl?.addSurface(aSurfaceName + mPackageName, aSurface, width, height, x, y)
    }

    fun removedSurface(aSurface: String) {
        Log.i(TAG, "removedSurface")
        mAidl?.removedSurface(aSurface + mPackageName)
    }

    fun addBitmapBufferCallback(key: String, width: Int, height: Int, x: Int, y: Int) {
        Log.i(TAG, "addBitmapBufferCallback key=$key width=$width height=$height x=$x y=$y")
        mAidl?.addBitmapBufferCallback(key + mPackageName, width, height, x, y)
    }

    fun removedBufferCallback(key: String) {
        Log.i(TAG, "removedBufferCallback")
        mAidl?.removedBufferCallback(key + mPackageName)
    }


}