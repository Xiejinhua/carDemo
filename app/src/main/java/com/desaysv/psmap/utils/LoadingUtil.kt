package com.desaysv.psmap.utils

import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.DialogLoadingTipBinding
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author : Xiejinhua
 * Date : 2024-12-21
 * Description : Loading 通用工具类
 */
@Singleton
class LoadingUtil @Inject constructor(
    private val application: Application,
    private val skyBoxBusiness: SkyBoxBusiness
) {
    private var binding: DialogLoadingTipBinding? = null
    private var windowManager: WindowManager? = null


    private fun showLoading(message: String?, gravity: Int, onItemClick: () -> Unit = {}) {
        cancelLoading()
        val displayManager = application.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(0) // 根据 displayId 获取 Display 对象
        val displayContext: Context = application.createDisplayContext(display)
        if (windowManager == null) {
            windowManager = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        if (binding == null) {
            binding = DialogLoadingTipBinding.inflate(LayoutInflater.from(application.applicationContext))
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,  //设置层级
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSPARENT
        )
        params.gravity = gravity

        binding?.apply {
            tvTitle.text = message
            cancel.setDebouncedOnClickListener {
                cancelLoading()
                onItemClick()
            }
            ViewClickEffectUtils.addClickScale(cancel, CLICKED_SCALE_95)
            dialogMainCl.setDebouncedOnClickListener {
            }
            skyBoxBusiness.updateView(root, true)
            indeterminateDrawable(NightModeGlobal.isNightMode())
//            shadowLayout.setShadowColor(
//                if (NightModeGlobal.isNightMode()) shadowLayout.resources.getColor(R.color.customColorWXUnBindShadowColorNight) else shadowLayout.resources.getColor(
//                    R.color.customColorWXUnBindShadowColorDay
//                )
//            )
//            cancel.setShadowColor(
//                if (NightModeGlobal.isNightMode()) cancel.resources.getColor(R.color.customColorSettingBtnBgNormalNight) else cancel.resources.getColor(
//                    R.color.customColorSettingBtnBgNormalDay
//                )
//            )
        }
        skyBoxBusiness.themeChange().observeForever(observer)
        windowManager?.addView(binding?.root, params)
    }

    fun indeterminateDrawable(isNight: Boolean) {
        val drawable = if (isNight) binding?.clsLoading?.context?.getDrawable(R.drawable.rotate_loading_active_view_night) else
            binding?.clsLoading?.context?.getDrawable(R.drawable.rotate_loading_active_view_day)
        if (drawable != null) {
            drawable.setBounds(
                0, 0,
                binding?.clsLoading?.resources?.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_54) ?: 0,
                binding?.clsLoading?.resources?.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_54) ?: 0
            ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
            binding?.clsLoading?.indeterminateDrawable = drawable
        }
    }

    private val observer = Observer<Boolean?> {
        if (it != null && binding != null) {
            binding?.apply {
                indeterminateDrawable(it)
//                shadowLayout.setShadowColor(
//                    if (NightModeGlobal.isNightMode()) shadowLayout.resources.getColor(R.color.customColorWXUnBindShadowColorNight) else shadowLayout.resources.getColor(
//                        R.color.customColorWXUnBindShadowColorDay
//                    )
//                )
//                cancel.setShadowColor(
//                    if (NightModeGlobal.isNightMode()) cancel.resources.getColor(R.color.customColorSettingBtnBgNormalNight) else cancel.resources.getColor(
//                        R.color.customColorSettingBtnBgNormalDay
//                    )
//                )
            }
        }
    }

    /**
     * 取消显示最新的提示Toast
     */
    fun cancelLoading(onCancelClick: () -> Unit = {}) {
        if (binding != null) {
            windowManager?.removeView(binding?.root)
            binding = null
            onCancelClick()
            skyBoxBusiness.themeChange().removeObserver(observer)
        }
    }

    fun showLoading(message: String, onItemClick: () -> Unit = {}) {
        showLoading(message, Gravity.CENTER, onItemClick)
    }

    /**
     * type = 1:路线规划页面请求
     */
    fun showLoading(@StringRes resId: Int, onItemClick: () -> Unit = {}) {
        showLoading(application.applicationContext.getString(resId), onItemClick)
    }
}