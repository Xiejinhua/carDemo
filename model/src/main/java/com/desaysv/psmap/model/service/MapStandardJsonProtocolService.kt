package com.desaysv.psmap.model.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.common.PermissionReqController
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.business.IFlyTekVoiceCommandBusiness
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author ZZP
 * @time 2025/5/31
 * @description 接收讯飞语音Intent
 */
@AndroidEntryPoint
class MapStandardJsonProtocolService : Service() {

    @Inject
    lateinit var iFlyTekVoiceCommandBusiness: IFlyTekVoiceCommandBusiness

    @Inject
    lateinit var initSDKBusiness: InitSDKBusiness

    @Inject
    lateinit var permissionReqController: PermissionReqController

    override fun onCreate() {
        super.onCreate()
        startNotification()
        Timber.i("onCreate")
        iFlyTekVoiceCommandBusiness.init()
    }

    private fun startNotification() {
        Timber.i("Service startNotification()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Android 8.0 StartForegroundService
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    "map_MapStandardJsonProtocolService",
                    "map_MapStandardJsonProtocolService",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
            val notification: Notification = Notification.Builder(application, "map_MapStandardJsonProtocolService")
                .setSmallIcon(R.drawable.ic_start_up_night)
                .setContentTitle("MapStandardJsonProtocolService Service")
                .setContentText("Running")
                .build()
            startForeground(1, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Timber.i("onBind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand")
        intent?.let {
            val data = intent.getStringExtra("data")
            if (data != null && !TextUtils.isEmpty(data)) {
                if (initSDKBusiness.isInitSuccess()) {
                    iFlyTekVoiceCommandBusiness.parseCommand(data)
                } else {
                    if (permissionReqController.requiredPermissionsIsOk()) {
                        iFlyTekVoiceCommandBusiness.setTTSResponse("地图应用未准备好，请打开地图检查下")
                    } else {
                        iFlyTekVoiceCommandBusiness.setTTSResponse("地图应用缺少相关权限，请打开地图授予权限")
                    }
                    iFlyTekVoiceCommandBusiness.unableUseMap()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy")
        iFlyTekVoiceCommandBusiness.unInit()
    }

}

interface IStandardJsonCallback {
    fun onMassage(pkg: String, jsonData: String)
    fun onMassageToAllClient(jsonData: String)
    fun syncClientMessage(pkg: String, jsonData: String): String?
}