package com.desaysv.psmap

import android.content.Intent
import android.os.Build
import com.desaysv.psmap.base.app.AutoApplication
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.service.MapService
import com.desaysv.psmap.model.tracking.JetourEventTracking
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber


/**
 * @description App的初始化入口
 */
@HiltAndroidApp
class PsMapApplication : AutoApplication() {

    override fun onCreate() {
        super.onCreate()
        startService() //判断服务是否已经起来
        MainScope().launch {
            EventTrackingUtils.init(this@PsMapApplication, JetourEventTracking(), true)
        }
        Timber.i("PsMapApplication onCreate finished VERSION_NAME: ${BuildConfig.VERSION_NAME}")
        BaseConstant.APP_VERSION = BuildConfig.VERSION_NAME
    }

    //判断服务是否已经起来
    private fun startService() {
        if (MapService.instance == null) {
            val service = Intent(this, MapService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(service)
            } else {
                startService(service)
            }
        }
    }
}
