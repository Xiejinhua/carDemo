package com.desaysv.psmap.model.screenshot

import android.R.attr.height
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.lifecycle.Observer
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapScreenshotBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.utils.BaseConstant.NAVI_STATE_INIT_NAVI_STOP
import com.desaysv.psmap.base.utils.BaseConstant.NAVI_STATE_REAL_NAVING
import com.desaysv.psmap.base.utils.BaseConstant.NAVI_STATE_SIM_NAVING
import com.desaysv.psmap.base.utils.BaseConstant.NAVI_STATE_STOP_REAL_NAVI
import com.desaysv.psmap.base.utils.BaseConstant.NAVI_STATE_STOP_SIM_NAVI
import com.desaysv.psmap.base.utils.EnlargeInfo
import com.desaysv.psmap.base.utils.unPeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton


//路口大图截图回调
@Singleton
class CrossMapScreenShotManager @Inject constructor(
    val mapBusiness: MapBusiness,
    val naviBusiness: NaviBusiness,
    val mapScreenshotBusiness: MapScreenshotBusiness,
) {

    private var screenBitmap: Bitmap? = null
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())
    private var showCross: Boolean = false
    private val crossViewVisibleOb = Observer<Boolean> {
        //刷帧一下
        mapBusiness.mainMapView.resetTickCount(1)
        ioScope.launch {
            if (it) {
                //在高德显示路口放大图的回调触发时，可能在图层上还没有显示路口大图，所有添加了200ms的延迟
                delay(200)
            }
            showCross = it
        }
    }

    //在导航中才启动截图回调
    private val naviStatusOb = Observer<Int> {
        when (it) {
            NAVI_STATE_INIT_NAVI_STOP,
            NAVI_STATE_STOP_REAL_NAVI,
            NAVI_STATE_STOP_SIM_NAVI -> {
                initScreenShot(false)
            }

            NAVI_STATE_REAL_NAVING,
            NAVI_STATE_SIM_NAVING -> {
                initScreenShot(true)
            }
        }
    }

    private val mMirrorMatrix = Matrix().apply {
        setScale(1f, -1f)
        postTranslate(0f, height.toFloat())
    }

    fun init() {
        Timber.i("initCrossMapScreenShot() called with ")
        ioScope.launch {
            withContext(Dispatchers.Main) {
                naviBusiness.showCrossView.unPeek().observeForever(crossViewVisibleOb)
                naviBusiness.naviStatus.observeForever(naviStatusOb)
            }
        }
    }

    private fun initScreenShot(open: Boolean) {
        Timber.i("initScreenshot() called with: open = $open")
        if (open) {
            mapScreenshotBusiness.addMapScreenshotBitmapCallback(bitmapCallback)
        } else {
            mapScreenshotBusiness.removeMapScreenshotBitmapCallback(bitmapCallback)
        }

    }

    fun unInit() {
        Timber.i("unInit")
        naviBusiness.showCrossView.unPeek().removeObserver(crossViewVisibleOb)
        naviBusiness.naviStatus.removeObserver(naviStatusOb)
        mapScreenshotBusiness.removeMapScreenshotBitmapCallback(bitmapCallback)
    }

    private val bitmapCallback = object : MapScreenshotBusiness.IScreenShortCallback {
        override fun onBitmap(bitmap: Bitmap) {
            val rect = EnlargeInfo.getInstance().rect
            //路口大图位置未初始化
            if (showCross && rect.width() != 0) {
                ioScope.launch {
                    val widgetBitmap = Bitmap.createBitmap(
                        bitmap,
                        rect.left,
                        1440 - rect.top - rect.height(),
                        rect.width(),
                        rect.height(),
                        mMirrorMatrix,
                        false
                    )
                    val stream = ByteArrayOutputStream()
                    widgetBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    crossMapCallback?.onMapByteData(stream.toByteArray())
                }
            }
        }

    }

    private var crossMapCallback: ICrossMapCallback? = null

    fun setCrossMapCallback(callback: ICrossMapCallback) {
        this.crossMapCallback = callback
    }

    interface ICrossMapCallback {
        fun onMapByteData(byteArray: ByteArray?)
    }


    private val mBitmapCachePool = HashMap<String, Bitmap>()

    private fun getCacheKey(w: Int, h: Int, pixelByte: Int): String {
        return String.format("%dx%d_%d", w, h, pixelByte)
    }

    private fun getCachedBitmap(w: Int, h: Int, pixelByte: Int): Bitmap {
        val key = getCacheKey(w, h, pixelByte)
        var bitmap: Bitmap? = mBitmapCachePool[key]
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(w, h, if (pixelByte == 4) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
            mBitmapCachePool[key] = bitmap
        }
        return bitmap
    }
}