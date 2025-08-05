package com.desaysv.psmap.model.service

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.IBinder
import android.os.SystemClock
import android.text.TextUtils
import android.view.Surface
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.Observer
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.adapter.IMapScreenshot
import com.desaysv.psmap.adapter.IMapScreenshotCallback
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapScreenshotBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * 后台截图功能服务
 */
@AndroidEntryPoint
class MapScreenshotService : Service() {
    @Inject
    lateinit var mapBusiness: MapBusiness

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    @Inject
    lateinit var mapScreenshotBusiness: MapScreenshotBusiness

    private val mWidgetSurfaces = ConcurrentHashMap<String, AtomicReference<Surface>>()
    private val mBitmapBufferKeys: MutableSet<String> = mutableSetOf()

    private val mMapScreenshotCallbacks: MutableSet<IMapScreenshotCallback> = mutableSetOf()

    private val mMirrorMatrix = ConcurrentHashMap<String, Matrix>()//镜像处理

    private val mY = ConcurrentHashMap<String, Int>()
    private val mX = ConcurrentHashMap<String, Int>()

    private var mWidgetWidth = ConcurrentHashMap<String, Int>()
    private var mWidgetHeight = ConcurrentHashMap<String, Int>()

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder {
        Timber.i("onBind")
        mapScreenshotBusiness.addMapScreenshotBitmapCallback(bitmapCallback)
        return mapScreenshot
    }

    override fun onDestroy() {
        Timber.i("onDestroy")
        super.onDestroy()
        mapScreenshotBusiness.removeMapScreenshotBitmapCallback(bitmapCallback)
    }

    private val mapScreenshot: IMapScreenshot.Stub = object : IMapScreenshot.Stub() {

        override fun addSurface(surfaceName: String, aSurface: Surface?, width: Int, height: Int, x1: Int, y1: Int) {
            synchronized(this) {
                Timber.i("addSurface surfaceName=$surfaceName aSurface=$aSurface width=$width height=$height x=$x1 y=$y1")
                if (aSurface == null || TextUtils.isEmpty(surfaceName)) {
                    onErrorInfo(-1, "Surface is null or surfaceName null")
                    return
                }
                if (width <= 0 || height <= 0) {
                    onErrorInfo(-2, "surfaceName=$surfaceName, width <= 0 || height <= 0")
                    return
                }

                //val x = x1
                //val y = （AutoConstant.mScreenHeight - height）/ 2 + y1
                val offset = getCarPositionOffset(iCarInfoProxy.getScreenStatus().value!!, width, height)
                val x = offset.x.toInt()
                val y = offset.y.toInt()

                if (height + y > AutoConstant.mScreenHeight || y < 0) {
                    Timber.w("y Incorrect")
                    onErrorInfo(-3, "surfaceName=$surfaceName, offset y Incorrect")
                    return
                }
                if ((AutoConstant.mScreenWidth - width) / 2 + x + width > AutoConstant.mScreenWidth || (AutoConstant.mScreenWidth - width) / 2 + x < 0) {
                    Timber.w("x Incorrect")
                    onErrorInfo(
                        -4,
                        "surfaceName=$surfaceName, offset x Incorrect, (ScreenWidth - width) / 2 + x + width > " +
                                "ScreenWidth || (ScreenWidth - width) / 2 + x < 0)"
                    )
                    return
                }
                if (mapBusiness.firstDeviceRender.value != true) {
                    Timber.i("MapScreenshot not Ready")
                    onErrorInfo(-5, "surfaceName=$surfaceName, MapScreenshot not Ready")
                    return
                }
                mWidgetWidth[surfaceName] = width
                mWidgetHeight[surfaceName] = height
                mX[surfaceName] = x
                mY[surfaceName] = y
                mMirrorMatrix[surfaceName] = Matrix().apply {
                    setScale(1f, -1f)
                    postTranslate(0f, height.toFloat())
                }
                mWidgetSurfaces[surfaceName] = AtomicReference<Surface>().apply { set(aSurface) }
                //刷帧一下
                mapBusiness.mainMapView.resetTickCount(1)
            }
        }

        override fun removedSurface(surfaceName: String) {
            Timber.i("removedSurface surfaceName=$surfaceName")
            synchronized(this) {
                mWidgetSurfaces.remove(surfaceName)
                mWidgetWidth.remove(surfaceName)
                mWidgetHeight.remove(surfaceName)
                mX.remove(surfaceName)
                mY.remove(surfaceName)
                mMirrorMatrix.remove(surfaceName)
            }
        }

        override fun addBitmapBufferCallback(key: String, width: Int, height: Int, x1: Int, y1: Int) {
            Timber.i("addBitmapBufferCallback key=$key width=$width height=$height x=$x1 y=$y1")
            synchronized(this) {
                if (TextUtils.isEmpty(key)) {
                    onErrorInfo(-1, "key is Empty")
                    return
                }
                if (width <= 0 || height <= 0) {
                    onErrorInfo(-2, "key=$key, width <= 0 || height <= 0")
                    return
                }
                val x = x1
                val y = (AutoConstant.mScreenHeight - height) / 2 + y1

                if (height + y > AutoConstant.mScreenHeight || y < 0) {
                    Timber.w("y Incorrect")
                    onErrorInfo(-3, "key=$key, offset y Incorrect")
                    return
                }
                if ((AutoConstant.mScreenWidth - width) / 2 + x + width > AutoConstant.mScreenWidth || (AutoConstant.mScreenWidth - width) / 2 + x < 0) {
                    Timber.w("x Incorrect")
                    onErrorInfo(
                        -4,
                        "key=$key, offset x Incorrect, (ScreenWidth - width) / 2 + x + width > ScreenWidth || " +
                                "(ScreenWidth - width) / 2 + x < 0)"
                    )
                    return
                }
                if (mapBusiness.firstDeviceRender.value != true) {
                    Timber.i("MapScreenshot not Ready")
                    onErrorInfo(-5, "key=$key, MapScreenshot not Ready")
                    return
                }
                mBitmapBufferKeys.add(key)
                mWidgetWidth[key] = width
                mWidgetHeight[key] = height
                mX[key] = x
                mY[key] = y
                mMirrorMatrix[key] = Matrix().apply {
                    setScale(1f, -1f)
                    postTranslate(0f, height.toFloat())
                }
                //刷帧一下
                mapBusiness.mainMapView.resetTickCount(1)
            }
        }

        override fun removedBufferCallback(key: String?) {
            Timber.i("removedBufferCallback key=$key")
            synchronized(this) {
                mWidgetWidth.remove(key)
                mWidgetHeight.remove(key)
                mX.remove(key)
                mY.remove(key)
                mMirrorMatrix.remove(key)
            }
        }

        override fun registerScreenshotCallback(callback: IMapScreenshotCallback?) {
            Timber.i("registerScreenshotCallback $callback")
            callback?.let { newCallback ->
                mMapScreenshotCallbacks.add(newCallback)
                if (mapBusiness.firstDeviceRender.value == true) {
                    Timber.i("onMapScreenshotReady ${mMapScreenshotCallbacks.size}")
                    mMapScreenshotCallbacks.forEach {
                        it.onMapScreenshotReady()
                    }
                } else {
                    MainScope().launch {
                        mapBusiness.firstDeviceRender.observeForever(object : Observer<Boolean> {
                            override fun onChanged(value: Boolean) {
                                Timber.i("firstDeviceRenderObserver $value")
                                if (value) {
                                    Timber.i("onMapScreenshotReady ${mMapScreenshotCallbacks.size}")
                                    mMapScreenshotCallbacks.forEach {
                                        it.onMapScreenshotReady()
                                    }
                                    mapBusiness.firstDeviceRender.removeObserver(this)
                                }
                            }
                        })
                    }
                }
            }

        }

        override fun unregisterScreenshotCallback(callback: IMapScreenshotCallback?) {
            Timber.i("unregisterScreenshotCallback $callback")
            mMapScreenshotCallbacks.remove(callback)
        }
    }
    private var lastLogTime = 0L
    private val bitmapCallback = object : MapScreenshotBusiness.IScreenShortCallback {
        override fun onBitmap(bitmap: Bitmap) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastLogTime > 5000) {
                Timber.i("onBitmap is called, bitmap size: ${bitmap.width}x${bitmap.height}")
                lastLogTime = now
            }
            mWidgetSurfaces.forEach { surfaceBean ->
                val myWidth = mWidgetWidth[surfaceBean.key]!!
                val myHeight = mWidgetHeight[surfaceBean.key]!!
                val myX = mX[surfaceBean.key]!!
                val myY = mY[surfaceBean.key]!!
                val x = (AutoConstant.mScreenWidth - myWidth) / 2 + myX
                val widgetBitmap = Bitmap.createBitmap(bitmap, x, myY, myWidth, myHeight)
                try {
                    val surface = surfaceBean.value.get()
                    if (surface.isValid) {
                        surface.lockCanvas(null)?.let {
                            it.drawBitmap(widgetBitmap, mMirrorMatrix[surfaceBean.key]!!, null)
                            surface.unlockCanvasAndPost(it)
                        }
                    } else {
                        Timber.i("surface ${surfaceBean.key} not Valid")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "lockCanvas fail")
                }
                if (!widgetBitmap.isRecycled)
                    widgetBitmap.recycle()
            }

            mBitmapBufferKeys.forEach { key ->
                val myWidth = mWidgetWidth[key]!!
                val myHeight = mWidgetHeight[key]!!
                val myX = mX[key]!!
                val myY = mY[key]!!
                val x = (AutoConstant.mScreenWidth - myWidth) / 2 + myX
                val widgetBitmap = Bitmap.createBitmap(bitmap, x, myY, myWidth, myHeight, mMirrorMatrix[key], false)
                val stream = ByteArrayOutputStream()
                widgetBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                ioScope.launch {
                    mMapScreenshotCallbacks.forEach {
                        it.onMapScreenshotpBitmapBuffer(stream.toByteArray())
                    }
                    if (!widgetBitmap.isRecycled) widgetBitmap.recycle()
                }
            }
        }

    }

    private fun onErrorInfo(code: Int, info: String?) {
        Timber.i("onErrorInfo code=$code info=$info")
        mMapScreenshotCallbacks.iterator().run {
            while (hasNext()) {
                val callback = next()
                try {
                    if (callback.asBinder().isBinderAlive) {
                        callback.onErrorInfo(code, info)
                    } else {
                        remove()
                    }
                } catch (e: Exception) {
                    remove()
                    Timber.w(e, "onErrorInfo error")
                }
            }
        }
    }

    /**
     * @param screenStatus true 为2/3屛， false 为全屏
     * @param width surface width
     * @param height surface height
     */
    private fun getCarPositionOffset(screenStatus: Boolean, width: Int, height: Int): Offset {
        val x = if (screenStatus) (552 * ((0.67 - 0.5) / 2) + 552 * 0.5).toFloat() else 0f
        //AutoConstant.mScreenHeight不是1080，所以要加上104 dock栏高度
        val y = (AutoConstant.mScreenHeight + 104 - height).toFloat() / 2//默认截中间部分
        Timber.i("getCarPositionOffset  x=$x  y=$y")
        return Offset(x, y)
    }
}