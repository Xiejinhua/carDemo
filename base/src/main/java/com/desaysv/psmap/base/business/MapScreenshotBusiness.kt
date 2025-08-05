package com.desaysv.psmap.base.business

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.collection.arraySetOf
import androidx.lifecycle.Observer
import com.autonavi.gbl.map.model.EGLSurfaceAttr
import com.autonavi.gbl.map.model.ScreenShotCallbackMethod
import com.autonavi.gbl.map.model.ScreenShotDataInfo
import com.autonavi.gbl.map.model.ScreenShotMode
import com.autonavi.gbl.map.observer.IEGLScreenshotObserver
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.common.AutoConstant
import com.autosdk.common.AutoState
import timber.log.Timber
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapScreenshotBusiness @Inject constructor(
    val mapBusiness: MapBusiness,
    val naviBusiness: NaviBusiness,

    ) : IEGLScreenshotObserver {
    private val callbacks = arraySetOf<IScreenShortCallback>()

    private var screenBitmap: Bitmap? = null
    private val mScreenshotBmp_565 by lazy {
        Bitmap.createBitmap(
            AutoConstant.mScreenWidth,
            AutoConstant.mScreenHeight,
            Bitmap.Config.RGB_565
        )
    }
    private val mScreenshotBmp_8888 by lazy {
        Bitmap.createBitmap(
            AutoConstant.mScreenWidth,
            AutoConstant.mScreenHeight,
            Bitmap.Config.ARGB_8888
        )
    }

    fun init() {
        Timber.i("init")
        mapBusiness.mainMapDevice.let {
            Timber.i("initMapScreenShot attachSurfaceToDevice")
            it.attachSurfaceToDevice(EGLSurfaceAttr().apply {
                nativeWindow = -1
                isOnlyCreatePBSurface = true
                height = AutoConstant.mScreenHeight
                width = AutoConstant.mScreenWidth
            })
        }
        mapBusiness.firstDeviceRender.observeForever(object : Observer<Boolean> {
            override fun onChanged(value: Boolean) {
                Timber.i("firstDeviceRenderObserver $value")
                if (value) {
                    initMapScreenShot()
                    mapBusiness.firstDeviceRender.removeObserver(this)
                }
            }
        })
    }

    fun unInit() {
        Timber.i("unInit")
        callbacks.clear()
    }

    fun addMapScreenshotBitmapCallback(callback: IScreenShortCallback) {
        Timber.i("addMapScreenshotBitmapCallback")
        callbacks.add(callback)
        screenBitmap?.run {
            callback.onBitmap(this)
        }
    }

    fun removeMapScreenshotBitmapCallback(callback: IScreenShortCallback) {
        Timber.i("removeMapScreenshotBitmapCallback")
        callbacks.remove(callback)
    }


    private fun initMapScreenShot() {
        mapBusiness.mainMapDevice.let {
            it.setScreenshotMode(ScreenShotMode.ScreenShotModeBackGround, this)
            it.setScreenshotRect(0, 0, AutoConstant.mScreenWidth, AutoConstant.mScreenHeight)
            it.setScreenshotCallBackMethod(ScreenShotCallbackMethod.ScreenShotCallbackMethodBuffer)
            //刷帧一下
            mapBusiness.mainMapView.resetTickCount(1)
            Timber.i("initMapScreenShot finish ${AutoConstant.mScreenWidth} ${AutoConstant.mScreenHeight}")
        }
    }

    private var lastLogTime = 0L
    private var firstDraw = false
    override fun onEGLScreenshot(
        deviceId: Int,
        pBitmapBuffer: ByteArray?,
        bufferDataParams: ScreenShotDataInfo?,
        nMethod: Int,
        pParamEx: Long
    ) {
        val bufferSize = pBitmapBuffer?.size ?: 0
        val pixelByte = bufferDataParams?.pixelByte ?: 0
        val now = SystemClock.elapsedRealtime()
        if (now - lastLogTime > 5000) {
            Timber.i("onEGLScreenshot bufferSize=$bufferSize pixelByte=$pixelByte")
            lastLogTime = now
        }
        if (bufferSize == 0 || pixelByte == 0) {
            return
        }
        screenBitmap = if (pixelByte == 4) mScreenshotBmp_8888 else mScreenshotBmp_565
        screenBitmap?.run {
            copyPixelsFromBuffer(ByteBuffer.wrap(pBitmapBuffer!!))
            callbacks.forEach {
                it.onBitmap(this)
            }
            if (!firstDraw) {
                firstDraw = true
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.WIDGET_FIRST_DRAW)
            }
        }


    }

    interface IScreenShortCallback {
        fun onBitmap(bitMap: Bitmap)
    }

}