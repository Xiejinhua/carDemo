package com.desaysv.psmap.ui.settings.view

import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.LayoutUnBindPopBinding
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Description : 微信解绑提示文言
 */
@Singleton
class UnBindPopWindow @Inject constructor(
    private val application: Application,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val settingAccountBusiness: SettingAccountBusiness
) {
    private var binding: LayoutUnBindPopBinding? = null
    private var windowManager: WindowManager? = null

    private fun showUnBindPop(moreInfo: MoreInfoBean, gravity: Int, ySet: Int) {
        cancelUnBindPop()
        val displayManager = application.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(0) // 根据 displayId 获取 Display 对象
        val displayContext: Context = application.createDisplayContext(display)
        if (windowManager == null) {
            windowManager = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        if (binding == null) {
            binding = LayoutUnBindPopBinding.inflate(LayoutInflater.from(displayContext))
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,  //设置层级
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = gravity
        //设置y轴方向的偏移量
        params.y = ySet

        binding?.apply {
            skyBoxBusiness.updateView(root, true)
            info = moreInfo
            //取消显示提示文言
            root.setDebouncedOnClickListener {
                cancelUnBindPop()
            }
        }

        windowManager?.addView(binding?.root, params)
        skyBoxBusiness.themeChange().observeForever(observer)
    }

    private val observer = Observer<Boolean?> {
        if (it != null && binding != null)
            skyBoxBusiness.updateView(binding!!.root, true)
    }

    /**
     * 取消显示最新的提示文言
     */
    fun cancelUnBindPop() {
        try {
            if (binding != null) {
                windowManager?.removeView(binding?.root)
                binding = null
                settingAccountBusiness.setShowMoreInfo(MoreInfoBean())
                skyBoxBusiness.themeChange().removeObserver(observer)
            }
        } catch (e: Exception) {
            Timber.i("cancelPromptLanguagePop Exception:${e.message}")
        }
    }

    fun showUnBindPop(moreInfo: MoreInfoBean, gravity: Int) {
        showUnBindPop(moreInfo, gravity, application.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_0))
    }

    fun showUnBindPop(moreInfo: MoreInfoBean) {
        showUnBindPop(moreInfo, Gravity.CENTER)
    }
}