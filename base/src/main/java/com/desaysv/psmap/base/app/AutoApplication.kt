package com.desaysv.psmap.base.app

import android.app.Application
import android.content.res.Configuration
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.widget.BusinessApplicationUtils
import com.autosdk.common.AutoConstant
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.autosdk.common.SdkApplicationUtils
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.BaseConstant
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

open class AutoApplication : Application() {

    @Inject
    lateinit var initSDKBusiness: InitSDKBusiness

    @Inject
    lateinit var carInfoProxy: ICarInfoProxy

    @Inject
    lateinit var mapBusiness: MapBusiness

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Timber.i("PsMapApplication onCreate start")
        SdkApplicationUtils.setApplication(this)//提供给Auto Java代码使用
        BusinessApplicationUtils.setApplication(this)
        ForegroundCallbacks.getInstance(this).addListener(foregroundListener)
        AutoConstant.mScreenWidth = this.resources.displayMetrics.widthPixels
        AutoConstant.mScreenHeight = this.resources.displayMetrics.heightPixels
        MainScope().launch {
            SdkAdapterManager.getInstance().startup()
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.START)
            AutoStatusAdapter.sendStatus(AutoStatus.APP_START)
        }
        carInfoProxy.init()
        initSDKBusiness.initMap()
        Timber.i("AutoConstant.mScreenWidth=${AutoConstant.mScreenWidth} AutoConstant.mScreenHeight=${AutoConstant.mScreenHeight}")
    }

    //应用在前后台判断
    private val foregroundListener = object : ForegroundCallbacks.Listener {
        override fun onBecameForeground() {
            Timber.i("应用在前台 onBecameForeground ${BaseConstant.APP_VERSION}")
            BaseConstant.MAP_APP_FOREGROUND = true
            if (initSDKBusiness.isInitSuccess()) {
                mapBusiness.setRenderFps(true) //刷帧控制
            }
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.FOREGROUND)
            AutoStatusAdapter.sendStatus(AutoStatus.APP_FOREGROUND)
        }

        override fun onBecameBackground() {
            Timber.i("应用在后台 onBecameBackground ${BaseConstant.APP_VERSION}")
            BaseConstant.MAP_APP_FOREGROUND = false
            if (initSDKBusiness.isInitSuccess()) {
                mapBusiness.setRenderFps(false) //刷帧控制
            }
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.BACKGROUND)
            AutoStatusAdapter.sendStatus(AutoStatus.APP_BACKGROUND)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (com.desaysv.psmap.base.BuildConfig.dayNightBySystemUI) {
            MainScope().launch {
                val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
                Timber.i("onConfigurationChanged currentNightMode = $currentNightMode")
                if (initSDKBusiness.isInitSuccess()) {
                    skyBoxBusiness.updateDayNightStatus(
                        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES)
                            SkyBoxBusiness.DAY_NIGHT_STATUS.NIGHT else SkyBoxBusiness.DAY_NIGHT_STATUS.DAY
                    )
                } else {
                    NightModeGlobal.setNightMode(currentNightMode == Configuration.UI_MODE_NIGHT_YES)
                    skyBoxBusiness.setThemeChange(NightModeGlobal.isNightMode())
                }
            }
        }
    }
}