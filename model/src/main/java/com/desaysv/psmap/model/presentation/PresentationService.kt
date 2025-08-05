package com.desaysv.psmap.model.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.autosdk.common.AutoStatus
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.ExtMapBusiness
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.def.InitSdkResultType
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.car.dashboard.CarDashboardBusiness
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 扩展屏投屏功能
 */
@AndroidEntryPoint
class PresentationService : Service() {
    @Inject
    lateinit var initSDKBusiness: InitSDKBusiness

    @Inject
    lateinit var extMapBusiness: ExtMapBusiness

    @Inject
    lateinit var carDashboardBusiness: CarDashboardBusiness

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    private var presentationDisplay: PresentationDisplay? = null

    private var mWindowManager: WindowManager? = null

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    private var loadingJob: Job? = null

    private var gaojieRemoveWindowJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startNotification()
        initPresentation()
        if (initSDKBusiness.isInitSuccess()) {
            initMapView()
        } else {
            initSDKBusiness.getInitResult().observeForever(object : Observer<InitSDKBusiness.InitSDKResult> {
                override fun onChanged(value: InitSDKBusiness.InitSDKResult) {
                    Timber.i("getInitResult observe ${value.code}")
                    if (value.code == InitSdkResultType.OK) {
                        initMapView()
                        initSDKBusiness.getInitResult().removeObserver(this)
                    }
                }
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startNotification() {
        Timber.i("Service startNotification()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Android 8.0 StartForegroundService
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    "map_PresentationService",
                    "map_PresentationService",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
            val notification: Notification = Notification.Builder(application, "map_PresentationService")
                .setSmallIcon(R.drawable.ic_start_up_night)
                .setContentTitle("Map Presentation Service")
                .setContentText("Running")
                .build()
            startForeground(1, notification)
        }
    }

    private fun initPresentation() {
        val displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.displays
        //是否存在可投屏的display
        for (display in displays) {
            Timber.i("display forEach displayId = ${display.displayId}")
            if (display.displayId == 2 && display.isValid) {
                Timber.i("displayId create")
                val context: Context = this.applicationContext.createDisplayContext(display)
                mWindowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
                carDashboardBusiness.init()
                presentationDisplay = PresentationDisplay(this@PresentationService.applicationContext)
                MainScope().launch {
                    carDashboardBusiness.naviDisplayLoading(true)
                    delay(300)
                    carDashboardBusiness.showMapViewToDashboard(true)
                }
                if (!iCarInfoProxy.isJetOurGaoJie())
                    addView()
                break
            }
        }

    }

    private fun addView() {
        removeView()
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSPARENT
        )
        Timber.i("Display mWindowManager.addView 111")
        mWindowManager!!.addView(presentationDisplay!!, params)
        Timber.i("Display mWindowManager.addView 222")
    }

    private fun removeView() {
        presentationDisplay?.run {
            if (this.isAttachedToWindow) {
                Timber.i("Display mWindowManager.removeView")
                mWindowManager?.removeView(this)
            }
        }
    }

    private fun initMapView() {
        Timber.i("111 initMapView")
        carDashboardBusiness.initMapInfo()
        presentationDisplay?.run {
            Timber.i("222 initMapView")
            this.initView(extMapBusiness.glMapSurfaces)
            val showPresentation = iCarInfoProxy.getDashboardTheme() == BaseConstant.CARDASHBOARD_THEME_NAVI
            Timber.i("init showPresentation $showPresentation")
            showPresentation(showPresentation)
            if (extMapBusiness.firstDeviceRender.value == true) {
                Timber.i("firstDeviceRender = true")
                MainScope().launch {
                    notifyCloseLoadingPage()
                }
            } else {
                extMapBusiness.firstDeviceRender.observeForever(object : Observer<Boolean> {
                    override fun onChanged(value: Boolean) {
                        Timber.i("firstDeviceRender observe $value")
                        if (value) {
                            MainScope().launch {
                                notifyCloseLoadingPage()
                            }
                            extMapBusiness.firstDeviceRender.removeObserver(this)
                        }
                    }
                })
            }
        }
        carDashboardBusiness.dashboardDisplayStatusListener(object : CarDashboardBusiness.IDashboardCallback {
            override fun onCarDashboardMapDisplay(show: Boolean) {
                Timber.i("onCarDashboardMapDisplay show=$show")
                showPresentation(show)
            }
        })

        skyBoxBusiness.themeChange().observeForever { isNight ->
            presentationDisplay?.updateView(isNight)
        }

        skyBoxBusiness.setDayNightChangeListener {
            Timber.i("setDayNightChangeListener true")
            carDashboardBusiness.naviDisplayLoading(true)
            loadingJob?.cancel()
            loadingJob = MainScope().launch {
                delay(1000)
                Timber.i("setDayNightChangeListener false")
                carDashboardBusiness.naviDisplayLoading(false)
            }
        }
    }

    private suspend fun notifyCloseLoadingPage() {
        extMapBusiness.resetTickCount()
        delay(2000)
        var count = 0
        while (iCarInfoProxy.getDashboardTheme() == -1 && count < 15) {
            Timber.i("notifyCloseLoadingPage DashboardTheme invalid, retry $count")
            count++
            delay(500)
        }
        Timber.i("notifyCloseLoadingPage DashboardTheme count = $count")
        if (iCarInfoProxy.getDashboardTheme() == BaseConstant.CARDASHBOARD_THEME_NAVI && showFlag == false) {
            Timber.i("notifyCloseLoadingPage DashboardTheme is valid, showPresentation")
            showPresentation(true)
            delay(500)
        }
        carDashboardBusiness.naviDisplayLoading(false)
    }

    private var showFlag: Boolean? = null
    private fun showPresentation(show: Boolean) {
        Timber.i("showPresentation showFlag=$showFlag show=$show")
        if (showFlag == show)
            return
        showFlag = show
        if (show) {
            if (iCarInfoProxy.isJetOurGaoJie()) {
                gaojieRemoveWindowJob?.cancel()
                addView()
                carDashboardBusiness.naviDisplayLoading(false)
            }
            extMapBusiness.renderResume()
            AutoStatusAdapter.sendStatus(AutoStatus.DASHBOARD_THEME_SWITCH)
        } else {
            extMapBusiness.renderPause()
            AutoStatusAdapter.sendStatus(AutoStatus.DASHBOARD_THEME_SWITCH)
            if (iCarInfoProxy.isJetOurGaoJie()) {
                gaojieRemoveWindowJob?.cancel()
                gaojieRemoveWindowJob = MainScope().launch {
                    delay(1000)
                    carDashboardBusiness.naviDisplayLoading(true)
                    removeView()
                }
            }
        }
    }

}