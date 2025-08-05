package com.desaysv.psmap.model.utils

import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.databinding.CustomBaseToastBinding
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author : wangmansheng
 * Date : 2024-1-10
 * Description : Toast工具类
 */
@Singleton
class ToastUtil @Inject constructor(
    private val application: Application,
    private val skyBoxBusiness: SkyBoxBusiness
) {

    private var binding: CustomBaseToastBinding? = null
    private var windowManager: WindowManager? = null
    private var mHandler: Handler = Handler()


    fun showToast(message: String?, completeImage: Int = -1, gravity: Int, ySet: Int) {
        cancelToast()
        if (windowManager == null) {
            val displayManager = application.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = displayManager.getDisplay(0) // 根据 displayId 获取 Display 对象
            val displayContext: Context = application.createDisplayContext(display)
            windowManager = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        if (binding == null) {
            binding = CustomBaseToastBinding.inflate(LayoutInflater.from(application.applicationContext))
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,  //设置层级
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = gravity
        //设置y轴方向的偏移量
        params.y = ySet

        binding?.apply {
            try {
                if (completeImage != -1) {
                    complete.setImageResource(completeImage)
                }
                complete.isVisible = completeImage != -1
                val params = msg.layoutParams as ConstraintLayout.LayoutParams
                params.leftMargin =
                    if (completeImage != -1) msg.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_16) else msg.resources.getDimensionPixelSize(
                        com.desaysv.psmap.base.R.dimen.sv_dimen_0
                    )
                msg.layoutParams = params
                msg.text = message
                skyBoxBusiness.updateView(root, true)
                shadowLayout.setShadowColor(
                    if (NightModeGlobal.isNightMode()) shadowLayout.resources.getColor(com.desaysv.psmap.model.R.color.customColorBlack10BgNight) else shadowLayout.resources.getColor(
                        com.desaysv.psmap.model.R.color.customColorShadowColorBlack10Day
                    )
                )
            } catch (e: Exception) {
                Timber.i("ToastUtil Exception: ${e.message}")
            }
        }
        skyBoxBusiness.themeChange().observeForever(observer)
        windowManager?.addView(binding?.root, params)

        //提示3秒
        mHandler.postDelayed({
            cancelToast()
        }, 3000)
    }

    private val observer = Observer<Boolean?> {
        if (it != null && binding != null)
            skyBoxBusiness.updateView(binding!!.root, true)
    }

    /**
     * 取消显示最新的提示Toast
     */
    fun cancelToast() {
        if (binding != null) {
            windowManager?.removeView(binding?.root)
            binding = null
            skyBoxBusiness.themeChange().removeObserver(observer)
        }
        mHandler.removeCallbacksAndMessages(null)
    }

    fun showToast(@StringRes resId: Int, completeImage: Int = -1, gravity: Int) {
        showToast(application.applicationContext.getString(resId), completeImage, gravity)
    }

    fun showToast(@StringRes resId: Int, completeImage: Int = -1) {
        showToast(application.applicationContext.getString(resId), completeImage)
    }

    fun showToast(@StringRes resId: Int) {
        showToast(application.applicationContext.getString(resId))
    }

    fun showToast(message: String?, completeImage: Int = -1, gravity: Int) {
        showToast(message, completeImage, gravity, application.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_10))
    }

    fun showToast(message: String?, completeImage: Int = -1) {
        showToast(
            message,
            completeImage,
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
            application.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_10)
        )
    }

    fun showToast(message: String?) {
        showToast(
            message,
            -1,
            Gravity.TOP or Gravity.CENTER_HORIZONTAL,
            application.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_10)
        )
    }
}