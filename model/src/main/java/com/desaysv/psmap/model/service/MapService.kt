package com.desaysv.psmap.model.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.autonavi.gbl.servicemanager.ServiceMgr
import com.autonavi.gbl.user.msgpush.model.AimPoiPushMsg
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autonavi.gbl.util.JniUtil
import com.autosdk.adapter.AdapterConstants
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.common.utils.FileUtils
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.push.listener.AimPushMessageListener
import com.autosdk.common.AutoConstant
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.autosdk.common.CommonConfigValue.KEY_ROAT_OPEN
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.storage.MapSharePreference.SharePreferenceName
import com.desaysv.psmap.base.business.ActivationMapBusiness
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.ExtMapBusiness
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.def.InitSdkResultType
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.impl.VehicleInfoCallback
import com.desaysv.psmap.base.utils.AppUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.HmiExceptionUtils
import com.desaysv.psmap.base.utils.LastRouteUtils
import com.desaysv.psmap.model.business.AccountSdkBusiness
import com.desaysv.psmap.model.business.BroadcastCommandBusiness
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.LinkCarBusiness
import com.desaysv.psmap.model.business.OfflineDataBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.SmartDriveBusiness
import com.desaysv.psmap.model.business.TripBusiness
import com.desaysv.psmap.model.car.dashboard.CarDashboardBusiness
import com.desaysv.psmap.model.presentation.PresentationService
import com.google.gson.Gson
import com.ivi.licenses_online.manager.LicensesOnline
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sv.account.sdk.common.AccountLifecycle
import timber.log.Timber
import javax.inject.Inject


/**
 * 地图前台服务
 */
@AndroidEntryPoint
class MapService : Service(), NetWorkManager.NetWorkChangeListener {
    private var mAutoNaviBroadcastReceiver: AutoNaviBroadcastReceiver? = null
    private var usbReceiver: UsbBroadcastReceiver? = null
    private val serviceJob = Job()
    private lateinit var jobState: Job
    private lateinit var jobLinkageEvent: Job
    private lateinit var jobPushEvent: Job
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var hmiException = HmiExceptionUtils() //崩溃堆栈回调通知
    private var isInit = true
    private var isFirst = true
    private var lastLineTime = 0L

    companion object {
        var instance: MapService? = null
    }

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var activationMapBusiness: ActivationMapBusiness

    @Inject
    lateinit var initSDKBusiness: InitSDKBusiness

    @Inject
    lateinit var mapBusiness: MapBusiness

    @Inject
    lateinit var pushMessageBusiness: PushMessageBusiness

    @Inject
    lateinit var linkCarBusiness: LinkCarBusiness

    @Inject
    lateinit var settingComponent: ISettingComponent

    @Inject
    lateinit var userBusiness: UserBusiness

    @Inject
    lateinit var naviBusiness: NaviBusiness

    @Inject
    lateinit var locationBusiness: LocationBusiness

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    @Inject
    lateinit var offlineDataBusiness: OfflineDataBusiness

    @Inject
    lateinit var sharePreferenceFactory: SharePreferenceFactory

    @Inject
    lateinit var broadcastCommandBusiness: BroadcastCommandBusiness

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var mRouteBusiness: RouteBusiness

    @Inject
    lateinit var dashboardBusiness: CarDashboardBusiness

    @Inject
    lateinit var mLastRouteUtils: LastRouteUtils

    @Inject
    lateinit var smartDriveBusiness: SmartDriveBusiness

    @Inject
    lateinit var tripBusiness: TripBusiness

    @Inject
    lateinit var customTeamBusiness: CustomTeamBusiness

    @Inject
    lateinit var ahaTripImpl: AhaTripImpl

    @Inject
    lateinit var cruiseBusiness: CruiseBusiness

    @Inject
    lateinit var extMapBusiness: ExtMapBusiness

    override fun onCreate() {
        super.onCreate()
        Timber.d(" ---------> onCreate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            //数字是随便写的“40”，
            nm.createNotificationChannel(
                NotificationChannel(
                    MapService::class.java.simpleName,
                    "MapService",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            val builder = NotificationCompat.Builder(this, MapService::class.java.simpleName)
            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2, builder.build())
        }
        instance = this
        usbChange //U盘监听广播设置
        autoNaviCommandBroadcastReceiver //
        initService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
            jobState.cancel()
            jobLinkageEvent.cancel()
            jobPushEvent.cancel()
        }
        serviceJob.cancel()
        if (mAutoNaviBroadcastReceiver != null) {
            unregisterReceiver(mAutoNaviBroadcastReceiver)
            mAutoNaviBroadcastReceiver = null
        }
        if (usbReceiver != null) {
            unregisterReceiver(usbReceiver)
            usbReceiver = null
        }
        netWorkManager.destroyNetWorkChangeListener()
        JniUtil.getInstance().removeObserver(hmiException) //崩溃堆栈回调 取消监听
        activationMapBusiness.unregisterActivateResultListener()
        initSDKBusiness.unInit()
        linkCarBusiness.removeLinkPhoneNaviStatus()
        if (pushMessageBusiness.isInitSuccess()) {
            pushMessageBusiness.removeSend2carPushMsgListener(aimPushMessageListener) //移除send2car 推送监听
        }
        smartDriveBusiness.unregisterAutoDataCallback()
        SdkAdapterManager.getInstance().sendNormalMessage(AutoState.FINISH)
        AutoStatusAdapter.sendStatus(AutoStatus.APP_EXIT)
        tripBusiness.removeUserTrackListener()
        Timber.w("onDestroy")
    }

    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        val state = netWorkManager.getAutoNetworkStatus()
        Timber.d(" onNetWorkChangeListener state=$state")
        ServiceMgr.getServiceMgrInstance().networkChange(state)
        updateTmcStatusOnNetWorkChange()
    }

    private fun updateTmcStatusOnNetWorkChange() {
        val configValue = settingComponent.getConfigKeyRoadEvent()
        val isNetworkConnected = netWorkManager.isNetworkConnected()
        Timber.d(" updateTmcStatusOnNetWorkChange ConfigKeyRoadEvent： $configValue , isNetworkConnected: $isNetworkConnected")
        if (configValue == KEY_ROAT_OPEN) {
            mapBusiness.setTmcVisible(isNetworkConnected)
        }
        if (isNetworkConnected)
            mapBusiness.setRouteTrafficVisible(true)
    }

    private fun initService() {
        serviceScope.launch {
            FileUtils.copy(AutoConstant.OLD_PATH + "activation/", AutoConstant.MAP_ACTIVE_DIR)
            FileUtils.deleteFile(AutoConstant.OLD_PATH) //清空旧目录DsvAutoPsMap，后续放在amapauto9目录
            FileUtils.createDir(BaseConstant.SETTING_DATA_PATH)
            netWorkManager.initNetWorkListener()

            withContext(Dispatchers.Main) {
                registerAutoActivateBroadcast()
                initSDKBusiness.getActiveInit().observeForever(object : Observer<Boolean> {
                    override fun onChanged(result: Boolean) {
                        Timber.i("getActiveInit observe isActivate: $result")
                        if (result) { //已激活
                            Settings.Global.putInt(contentResolver, BaseConstant.KEY_GLOBAL_ACTIVATE_FLAG, BaseConstant.GLOBAL_ACTIVATE_YES) //已激活
                            initSDKBusiness.getActiveInit().removeObserver(this)
                        } else {
                            //未激活，广播自动激活功能
                            Settings.Global.putInt(contentResolver, BaseConstant.KEY_GLOBAL_ACTIVATE_FLAG, BaseConstant.GLOBAL_ACTIVATE_NO) //未激活
                        }
                    }
                })

                activationMapBusiness.postActivateResult.observeForever {
                    serviceScope.launch {
                        if (it) {
                            activationMapBusiness.toSetPostActivateResult(false)
                            if (!CommonUtils.isVehicle()) {
                                Timber.d("do not postActivateResult")
                                return@launch
                            }
                            try {
                                LicensesOnline.statistics(
                                    "sv-898333330676775840",
                                    "T1J",
                                    iCarInfoProxy.uuid,
                                    "navigation_gaode"
                                ) { code: Int, msg: String ->
                                    Timber.i("statistics: code:$code, msg:$msg")
                                }
                            } catch (e: Exception) {
                                Timber.d("Exception is ${e.message}")
                            }
                        }
                    }
                }
            }
        }
        if (initSDKBusiness.isInitSuccess()) {
            if (isInit) {
                isInit = false
                Timber.i("getInitResult isInitSuccess")
                hasMapInit() //地图已经初始化
            } else {
                Timber.i("getInitResult has isInit")
            }
        } else {
            initSDKBusiness.getInitResult().observeForever(object : Observer<InitSDKBusiness.InitSDKResult> {
                override fun onChanged(result: InitSDKBusiness.InitSDKResult) {
                    Timber.i("getInitResult observe ${result.code}")
                    if (result.code == InitSdkResultType.OK) {
                        if (isInit) {
                            isInit = false
                            Timber.i("getInitResult observeForever isInitSuccess")
                            hasMapInit() //地图已经初始化
                        } else {
                            Timber.i("getInitResult observeForever has isInit")
                        }
                        initSDKBusiness.getInitResult().removeObserver(this)
                        JniUtil.getInstance().addObserver(hmiException)//崩溃堆栈回调 注册监听
                    }
                }
            })
        }
        if (BaseConstant.MULTI_MAP_VIEW)
            startPresentationService()
        startMapStandardJsonProtocolService()

        val restart = sharePreferenceFactory.getMapSharePreference(SharePreferenceName.normal)
            .getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.usbReStart, false)
        Timber.i("restart:$restart")
        if (restart) {
            Timber.i("usbReStart 罐装地图后重启地图，需要拉起地图界面")
            sharePreferenceFactory.getMapSharePreference(SharePreferenceName.normal)
                .putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.usbReStart, false)
            openMap() //打开地图
        }
    }

    //打开地图
    private fun openMap() {
        AppUtils.startOrBringActivityToFront(application)
    }

    //地图已经初始化
    private fun hasMapInit() {
        try {
            netWorkManager.addNetWorkChangeListener(this@MapService)
            mapBusiness.initMainMapLayer()
            settingAccountBusiness.addObserver()
            addLinkPhoneNaviStatus()
            if (pushMessageBusiness.isInitSuccess()) {
                pushMessageBusiness.addSend2carPushMsgListener(aimPushMessageListener)//注册send2car 推送监听
            }
            registerCarInfoCallback()
            smartDriveBusiness.registerAutoDataCallback()
            tripBusiness.removeUserTrackListener()
            tripBusiness.setUserTrackListener()
            //组队功能初始化
            customTeamBusiness.initSDK(application)

            //路书初始化
            ahaTripImpl.ahaInit() //个人中心账号绑定初始化--放这里

            if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
//                settingAccountBusiness.accountSdkInit() //个人中心账号绑定初始化
                settingAccountBusiness.setOnGetLinkageIdListener() //提供CP账号id(注意：CP必要设置)
                settingAccountBusiness.setAccountStateLinkageEvent(object : AccountSdkBusiness.AccountStateLinkageEvent {
                    override fun accountObserver(state: AccountLifecycle.State) {
                        serviceScope.launch {
                            Timber.i("onAccountStateChanged: $state")
                            if (state != AccountLifecycle.State.Unknown) {
                                if (settingAccountBusiness.isLoggedIn()) { //个人中心账号已经登录
                                    val linkageDto = settingAccountBusiness.linkageDto() //获取当前cp绑定账号的绑定信息
                                    val accountInfo = settingAccountBusiness.getAccount()
                                    Timber.i("onAccountStateChanged: linkageDto${linkageDto?.toString()}")
                                    Timber.i("onAccountStateChanged: accountInfo${accountInfo?.toString()}")
                                    if (null == accountInfo) {
                                        settingAccountBusiness.jetourAccountState.postValue(false)
                                        settingAccountBusiness.deleteUserData(false) //删除高德用户账号数据
                                    } else if (null == linkageDto) {
                                        settingAccountBusiness.jetourAccountState.postValue(true)
                                        settingAccountBusiness.deleteUserData(false) //删除高德用户账号数据
                                    } else if (TextUtils.equals(linkageDto.status, "1")) { //已经绑定，快速登录
                                        settingAccountBusiness.jetourAccountState.postValue(true)
                                        val uid = settingAccountBusiness.getAccountProfile()?.uid ?: ""
                                        if (TextUtils.isEmpty(uid) || uid != linkageDto.linkageId || isFirst) {//登录用户不同时切换用户
                                            isFirst = false
                                            settingAccountBusiness.requestQuickLogin(accountInfo.id, linkageDto.linkageId)
                                        }
                                    } else {//未绑定
                                        settingAccountBusiness.jetourAccountState.postValue(true)
                                        settingAccountBusiness.deleteUserData(false) //删除高德用户账号数据
                                    }

                                    if (accountInfo != null) {
                                        Timber.i("onAccountStateChanged userId:${customTeamBusiness.userId.value} accountInfo.id:${accountInfo.id} jtUserId${customTeamBusiness.jtUserId.value}")
                                        if ((customTeamBusiness.userId.value ?: 0) == 0
                                            || accountInfo.id != customTeamBusiness.jtUserId.value.toString()
                                            || accountInfo.nickname != customTeamBusiness.userInfo.value?.nick_name
                                            || accountInfo.avatar != customTeamBusiness.userInfo.value?.head_img
                                        ) {
                                            customTeamBusiness.disconnectServe()
                                            customTeamBusiness.leaveTeam(false)
                                            customTeamBusiness.loginUser(accountInfo.id, accountInfo.nickname ?: "", accountInfo.avatar ?: "")
                                        }
                                    }
                                } else { //个人中心账号未登录
                                    Timber.i("onAccountStateChanged 个人中心账号未登录")
                                    settingAccountBusiness.jetourAccountState.postValue(false)
                                    settingAccountBusiness.deleteUserData(false) //删除高德用户账号数据
                                    customTeamBusiness.disconnectServe()
                                    customTeamBusiness.leaveTeam()
                                    customTeamBusiness.resetUserId()
                                }
                            } else {
                                Timber.i("onAccountStateChanged: state == null || state == Unknown")
                            }
                        }
                    }

                    override fun linkageEventObserver(isSuccessLinkage: Boolean, isBind: Boolean) {
                        serviceScope.launch {
                            Timber.i("onLinkageEventChanged isSuccessLinkage:$isSuccessLinkage  isBind:$isBind")
                            if (isBind) { //绑定操作
                                if (isSuccessLinkage) { //成功
                                    val currentTime = System.currentTimeMillis()
                                    val dif = currentTime - lastLineTime
                                    Timber.i(" onLinkageEventChanged dif:$dif")
                                    if (dif > 10) {
                                        lastLineTime = currentTime
                                        settingAccountBusiness.getUserData() //成功用户数据
                                    }
                                } else { //失败
                                    settingAccountBusiness.bindFail() //绑定失败了，需要重新刷新二维码
                                }
                            } else { //解绑操作
                                settingAccountBusiness.deleteUserData(false) //删除高德用户账号数据
                            }
                        }
                    }
                })
                jobState = settingAccountBusiness.jobState()
                jobLinkageEvent = settingAccountBusiness.jobLinkageEvent()
                jobPushEvent = settingAccountBusiness.jobPushEvent(BaseConstant.JETOUR_PUSH_MESSAGE_TYPE)
            }
        } catch (e: Exception) {
            Timber.d("hasMapInit Exception:${e.message}")
        }
    }

    private fun addLinkPhoneNaviStatus() {
        // 找用户模块要uid，如果为空，则表示未登录
        val accountInfo = userBusiness.accountInfo()
        linkCarBusiness.addLinkPhoneNaviStatus()
        if (accountInfo != null) {
            BaseConstant.uid = accountInfo.uid
            Timber.d("addLinkPhoneNaviStatus uid:${accountInfo.uid}")
            linkCarBusiness.startLinkPhone()
            pushMessageBusiness.stopListener()
            pushMessageBusiness.startListen(BaseConstant.uid)
        }
    }

    private fun registerCarInfoCallback() {
        iCarInfoProxy.registerVehicleInfoCallback(object : VehicleInfoCallback {
            override fun powerStatusChange(accOn: Boolean) {
                Timber.i("accStateChange $accOn")//true 上电，false 下电/STR
                if (accOn) {
                    if (!locationBusiness.timerIsRunning())
                        locationBusiness.doStartTimerWithAccOn()
                    mapBusiness.renderResume(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                    extMapBusiness.renderResume()
                    //提示卡片
                    MainScope().launch {
                        mapBusiness.checkCommutingScenariosFlag()
                        mapBusiness.initHomeCardTipsData()
                        delay(1000)
                        dashboardBusiness.showMapViewToDashboard(true)
                    }
                } else {
                    dashboardBusiness.showMapViewToDashboard(false)
                    if (locationBusiness.timerIsRunning())
                        locationBusiness.doStopTimerWithAccOff()
                    mapBusiness.renderPause(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                    extMapBusiness.renderPause()
                    locationBusiness.saveCurPos()
                    sharePreferenceFactory.getMapSharePreference(SharePreferenceName.normal).putLongValue(
                        MapSharePreference.SharePreferenceKeyEnum.accOffTime, System.currentTimeMillis()
                    )
                    if (naviBusiness.isNavigating())
                        naviBusiness.stopNavi(isNeedResumeNavi = true)
                    cruiseBusiness.stopCruise()
                }
            }

            override fun factoryResetNotify(status: Boolean) {
                if (status) {
                    dashboardBusiness.showMapViewToDashboard(false)
                    Timber.d(" MASTER_CLEAR_NOTIFICATION ")
                    FileUtils.deleteFile(AutoConstant.ACCOUNT_DIR)
                    FileUtils.deleteFile(AutoConstant.AOS_DIR)
                    FileUtils.deleteFile(AutoConstant.SYNC_DIR)
                    mLastRouteUtils.deleteRouteFile()
                    initSDKBusiness.factoryReset()
                }
            }
        })
    }

    private fun startPresentationService() {
        Timber.i("startPresentationService")
        val service = Intent(this, PresentationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service)
        } else {
            startService(service)
        }
    }

    private fun startMapStandardJsonProtocolService() {
        Timber.i("startPresentationService")
        val service = Intent(this, MapStandardJsonProtocolService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service)
        } else {
            startService(service)
        }
    }

    //注册广播自动激活功能
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerAutoActivateBroadcast() {
        activationMapBusiness.registerActivateResultListener(object : ActivationMapBusiness.IActivateResultListener {
            override fun activateResult(type: Int, flag: Boolean, msg: String?) {
                var b = Settings.Global.putInt(
                    contentResolver,
                    BaseConstant.KEY_GLOBAL_ACTIVATE_FLAG,
                    if (flag) BaseConstant.GLOBAL_ACTIVATE_YES else BaseConstant.GLOBAL_ACTIVATE_NO
                )
                if (!b) { //再次尝试
                    b = Settings.Global.putInt(
                        contentResolver,
                        BaseConstant.KEY_GLOBAL_ACTIVATE_FLAG,
                        if (flag) BaseConstant.GLOBAL_ACTIVATE_YES else BaseConstant.GLOBAL_ACTIVATE_NO
                    )
                }
                val intent = Intent(BaseConstant.ACTION_AUTO_ACTIVATE_RESULT)
                intent.putExtra(BaseConstant.KEY_AUTO_ACTIVATE_TYPE, type)
                intent.putExtra(BaseConstant.KEY_AUTO_ACTIVATE_RESULT_MSG, msg)
                sendBroadcast(intent)
                Timber.d(
                    "sendBroadcast AUTO_ACTIVATE_RESULT Global.putInt $b , type:$type , flag:$flag , KEY_GLOBAL_ACTIVATE_FLAG: %s",
                    Settings.Global.getInt(
                        contentResolver,
                        BaseConstant.KEY_GLOBAL_ACTIVATE_FLAG,
                        BaseConstant.GLOBAL_ACTIVATE_NO
                    )
                )
                serviceScope.launch {
                    if (flag) { //自动激活成功，初始化地图SDK
                        initSDKBusiness.activatedInitMap(true)
                    }
                }
            }
        })
    }

    //U盘监听广播设置
    private val usbChange: Unit
        get() {
            usbReceiver = UsbBroadcastReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED)
            intentFilter.addAction(Intent.ACTION_MEDIA_EJECT)
            intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
            intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED)
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            intentFilter.addDataScheme("file")
            registerReceiver(usbReceiver, intentFilter)
        }

    //U盘监听
    inner class UsbBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("UsbBroadcastReceiver action:${intent.action}")
            //项目有用于行车记录采集数据的usb1盘符，不能当成普通U盘去监听！
            if (intent.data?.path != BaseConstant.USB_ROOT_PATH) {
                Timber.i("usb path isn't '/storage/usb0',is ${intent.data?.path} ; return usbStateReceiver.onReceive")
                return
            }
            when (intent.action) {
                Intent.ACTION_MEDIA_MOUNTED -> { //USB挂载
                    val rootPathStart = intent.data?.path
                    Timber.w("U盘挂载 $rootPathStart")
                    if (CustomFileUtils.isExistFile(BaseConstant.USB_MAP_DATA_PATH)) {//U盘存在amapauto9路径的离线数据
                        offlineDataBusiness.usbMount.postValue(true)
                    } else {
                        Timber.w("U盘挂载 不存在amapauto9路径的离线数据")
                    }
                }

                Intent.ACTION_MEDIA_EJECT,
                Intent.ACTION_MEDIA_UNMOUNTED,
                Intent.ACTION_MEDIA_BAD_REMOVAL -> { //USB移除
                    val rootPathEnd = intent.data?.path
                    Timber.w("U盘移除 $rootPathEnd")
                    offlineDataBusiness.usbMount.postValue(false)
                }

                else -> {
                    Timber.w("U盘状态 ${intent.action}")
                }
            }
        }
    }


    //U盘监听广播设置
    private val autoNaviCommandBroadcastReceiver: Unit
        get() {
            mAutoNaviBroadcastReceiver = AutoNaviBroadcastReceiver(broadcastCommandBusiness)
            Timber.d("  registerAutoNaviBroadcastReceiver")
            registerReceiver(mAutoNaviBroadcastReceiver, IntentFilter(AdapterConstants.ACTION))
        }

    //消息接收监听器，包含了POI和路线
    private val aimPushMessageListener = object : AimPushMessageListener {
        override fun notifyPoiPushMessage(aimPoiPushMsg: AimPoiPushMsg?) {//接收POI点信息
            Timber.i("notifyPoiPushMessage")
        }

        override fun notifyRoutePushMessage(aimRoutePushMsg: AimRoutePushMsg?) {//接收到的路线
            Timber.i("notifyRoutePushMessage")
        }

        override fun isSend2Car(): Boolean {
            return true
        }
    }
}