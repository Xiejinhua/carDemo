package com.desaysv.psmap.base.business

import android.app.Application
import android.os.Process
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.aidl.ivi.yaca.interfaces.StatusCallbackYACA
import com.autonavi.gbl.activation.model.AuthenticationGoodsInfo
import com.autonavi.gbl.activation.model.AuthenticationResult
import com.autonavi.gbl.activation.model.AuthenticationStatus
import com.autonavi.gbl.activation.observer.IAuthenticationObserver
import com.autonavi.gbl.route.model.RouteControlKey
import com.autonavi.gbl.servicemanager.ServiceMgr
import com.autonavi.gbl.servicemanager.model.FileCopyCheckMode
import com.autonavi.gbl.servicemanager.model.ServiceManagerEnum
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.common.System.ErrorCodeReadfile
import com.autonavi.gbl.util.model.FactoryResetParam
import com.autonavi.gbl.util.model.FactoryResetResult
import com.autonavi.gbl.util.model.FactoryResetType
import com.autonavi.gbl.util.observer.IFactoryResetObserver
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.authentication.AuthenticationController
import com.autosdk.bussiness.common.utils.FileUtils
import com.autosdk.bussiness.manager.IPlatformDepends
import com.autosdk.bussiness.manager.SDKInitParams
import com.autosdk.bussiness.manager.SDKManager
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.scene.SceneModuleController
import com.autosdk.bussiness.search.SearchController
import com.autosdk.bussiness.search.SearchControllerV2
import com.autosdk.bussiness.widget.BusinessApplicationUtils
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.AutoConstant
import com.autosdk.common.AutoState
import com.autosdk.common.CommonConfigValue.KEY_ROAT_OPEN
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.utils.AssertUtils
import com.desaysv.psmap.base.BuildConfig
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.PermissionReqController
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.def.InitSdkResultType
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.IModelBusinessProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.unPeek
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess

@Singleton
class InitSDKBusiness @Inject constructor(
    private val mApp: Application,
    private val locationBusiness: LocationBusiness,
    private val naviBusiness: NaviBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val userBusiness: UserBusiness,
    private val pushMessageBusiness: PushMessageBusiness,
    private val mapBusiness: MapBusiness,
    private val sDKManager: SDKManager,
    private val activationMapBusiness: ActivationMapBusiness,
    private val searchController: SearchController,
    private val searchControllerV2: SearchControllerV2,
    private val sceneModuleController: SceneModuleController,
    private val authenticationController: AuthenticationController,
    private val permissionReqController: PermissionReqController,
    private val mNaviController: NaviController,
    private val carInfo: ICarInfoProxy,
    private val engineerBusiness: EngineerBusiness,
    private val modelBusiness: IModelBusinessProxy,
    private val settingComponent: ISettingComponent,
    private val voiceDataBusiness: VoiceDataBusiness,
    private val speechSynthesizeBusiness: SpeechSynthesizeBusiness,
    private val forecastBusiness: ForecastBusiness,
    private val desaysvLicensesBusiness: DesaysvLicensesBusiness
) {
    private var initResult: InitSDKResult = InitSDKResult(InitSdkResultType.INIT, "not init")
    private val mbInitResult: MutableLiveData<InitSDKResult> = MutableLiveData(initResult)
    private val activeInit = MutableLiveData(false) //激活模块是否已经init

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var sharePreferenceFactory: SharePreferenceFactory

    @Inject
    lateinit var extMapBusiness: ExtMapBusiness

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var screenshotBusiness: MapScreenshotBusiness

    private var mSDKInitParams: SDKInitParams? = null

    private var initing: Boolean = false

    private val _iovLicenseOK = MutableLiveData(true)
    val iovLicenseOK: LiveData<Boolean> = _iovLicenseOK

    init {
        System.loadLibrary("Gbl")
    }


    /**
     * 初始化
     */
    fun initMap() {
        if (initing || isInitSuccess()) {
            Timber.i("initMap, initing or InitSuccess")
            return
        }

        Timber.i("initMap")
        initing = true
        MainScope().launch { permissionReqController.notificationPermissionUse(mApp, reset = true) }

        if (!permissionReqController.requiredPermissionsIsOk()) {
            Timber.i("initMap need to requiredPermissions")
            initing = false
            return
        }
        //AAR过期检查
        if (isOverdue()) {
            initResult.code = InitSdkResultType.FAIL_AAR_OVERDUE
            initResult.msg = "initSDKBase Fail! auto aar is Overdue"
        } else {
            val initSDKBase = initSDKBase()
            val activationInit = activationMapBusiness.dataInit()
            if (initSDKBase == Service.ErrorCodeOK) {
                //激活检查
                activationMapBusiness.initActivation()
                if (activationInit == Service.ErrorCodeOK) {
                    val activateStatus = activationMapBusiness.isActivate()
                    Timber.i(" activateStatus: $activateStatus")
                    if (activateStatus) {
                        Timber.i("initMap Activate")
                        activatedInitMap(false)
                    } else {
                        initResult.apply {
                            code = InitSdkResultType.FAIL_ACTIVATE
                            msg = "not Activate"
                        }
                        activeInit.postValue(false)
                    }
                } else {
                    Timber.i("not activationInit")
                    initResult.code = InitSdkResultType.FAIL_INIT_ACTIVATE
                    initResult.msg = "initSDKBase Fail! for activationInit Fail"
                    activeInit.postValue(false)
                }
            } else {
                activeInit.postValue(false)
                if (initSDKBase == ErrorCodeReadfile && activationInit == Service.ErrorCodeFailed &&
                    !sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.normal)
                        .getBooleanValue(BaseConstant.IS_DATA_INIT, false)
                ) {
                    Timber.d("isDataInit 地图初始化失败，需要删除分区文件")
                    sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.normal)
                        .putBooleanValue(BaseConstant.IS_DATA_INIT, true)
                    FileUtils.deleteFile(AutoConstant.PATH)
                    MainScope().launch {
                        withContext(Dispatchers.IO) {
                            delay(1000)
                            Process.killProcess(Process.myPid())
                            exitProcess(0)
                        }
                    }
                }
                initResult.code = InitSdkResultType.FAIL
                initResult.msg = "initSDKBase Fail! ErrorCode:$initSDKBase"
            }
        }
        Timber.i("InitResult postValue ${initResult.code}")
        mbInitResult.postValue(initResult)
        initing = false


    }

    /**
     * 已经激活初始化
     */
    fun activatedInitMap(needPost: Boolean = true) {
        Timber.i("activatedInitMap $needPost")
        val initSDK = initSDK()
        if (initSDK == Service.ErrorCodeOK) {
            initSDKService()
            mapBusiness.initSDKMap()
            MainScope().launch {
                modelBusiness.init()
            }
            if (BaseConstant.MULTI_MAP_VIEW) {
                extMapBusiness.initExt()
            }
            MainScope().launch {
                screenshotBusiness.init()
            }
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.START_FINISH)
            initResult.code = InitSdkResultType.OK
            initResult.msg = "initSDK OK"
            activeInit.postValue(true)
        } else {
            initResult.code = InitSdkResultType.FAIL
            initResult.msg = "initSDK Fail! ErrorCode:$initSDK"
            activeInit.postValue(false)
        }
        if (needPost) {
            mbInitResult.postValue(initResult)
        }


    }

    /**
     * 检查德赛IOV网联软件权限
     */
    fun checkDesaySVIOVLicenses() {
        if (!CommonUtils.isVehicle() || BuildConfig.DEBUG) {
            Timber.i("No checkDesaySVIOVLicenses")
            return
        }
        Timber.i("checkDesaySVIOVLicenses")
        //初始化 直接返回连接状态
        desaysvLicensesBusiness.getLicensesManager().init(mApp.applicationContext, mApp.packageName,
            StatusCallbackYACA {
                Timber.i("checkDesaySVIOVLicenses desaysvLicensesInit LicenseCodeCallback:$it")
            }
        )
    }

    fun checkIOVLicenseIsOK() {
        Timber.i("checkIOVLicenseIsOK  = ${BaseConstant.DESAYSV_IOV_LICENSE_OK}")
        if (!BaseConstant.DESAYSV_IOV_LICENSE_OK) {
            BaseConstant.DESAYSV_IOV_LICENSE_OK = desaysvLicensesBusiness.isUsable()
            Timber.i("checkIOVLicenseIsOK isUsable = ${BaseConstant.DESAYSV_IOV_LICENSE_OK}")
        }
        if (_iovLicenseOK.value != BaseConstant.DESAYSV_IOV_LICENSE_OK) {
            Timber.i("checkIOVLicenseIsOK postValue = ${BaseConstant.DESAYSV_IOV_LICENSE_OK}")
            _iovLicenseOK.postValue(BaseConstant.DESAYSV_IOV_LICENSE_OK)
        }
    }

    /**
     * 初始化SDK基础
     */
    private fun initSDKBase(): Int {
        Timber.i("initSDKBase base path is ${AutoConstant.PATH}")
        copyAssetsFiles(AutoConstant.PATH)
        mSDKInitParams = getSDKInitParams()
        val initBaseLibsResult = sDKManager.initBaseLibs(mApp, object : IPlatformDepends {

            override fun getNetStatus(): Int {
                return netWorkManager.getAutoNetworkStatus()
            }

            override fun getDIU(): String {
                return carInfo.sNCode
            }

        }, mSDKInitParams)
        Timber.i("initSDKBase initBaseLibsResult is $initBaseLibsResult")
        return initBaseLibsResult
    }

    /**
     * 初始化SDK
     */
    private fun initSDK(): Int {
        val initBLResult = sDKManager.initBL(mApp, mSDKInitParams ?: getSDKInitParams())
        Timber.i("initSDK initBL is $initBLResult")
        initAuthentication()//鉴权服务
        return initBLResult
    }

    /**
     * 初始化导航SDK相关服务
     */
    fun initSDKService() {
        BusinessApplicationUtils.mScreenWidth = AutoConstant.mScreenWidth
        BusinessApplicationUtils.mScreenHeight = AutoConstant.mScreenHeight
        Timber.i("ScreenWidth=${AutoConstant.mScreenWidth} ScreenHeight=${AutoConstant.mScreenHeight}")

        mapBusiness.initSDKMapBase()

        locationBusiness.initPos(engineerBusiness.engineerConfig.laneMockMode)

        /** TBT初始化 */
        naviBusiness.initTBT()

        /** 搜索初始化 */
        searchController.initService()
        searchControllerV2.initService()


        /** 地图数据服务初始化 */
        mapDataBusiness.initMapData()

        /** 账号服务初始化*/
        userBusiness.initAccount()


        /** 同步服务初始化 */
        userBusiness.initSyncSdk()


        /** 轨迹服务初始化  */
        userBusiness.initUserTrack()


        /** 收藏服务初始化  */
        userBusiness.initBehavior()
        userBusiness.initUserGroup()
        initGroupChatService()


        /** Aos服务初始化  */
        userBusiness.initAos()


        /** push服务启动  */
        FileUtils.createDir(AutoConstant.PUSH_DIR)
        pushMessageBusiness.initPush()

        /** 高德附近场景模块初始化  */
        sceneModuleController.init()

        voiceDataBusiness.initService()


        /**预测服务初始化 */
        val ret = forecastBusiness.init(AutoConstant.ACCOUNT_DIR)
        Timber.i("forecastBusiness init $ret")

        getNavigationSettingConfig() //地图启动获取一些配置项
        todoSetVoice() //根据本地保存的角色音VoiceId或者恢复默认设置语音包
        Timber.i("initSDKService finish")
    }

    //地图启动获取一些配置项
    private fun getNavigationSettingConfig() {
        //第一次安装地图默认关闭全部巡航播报
        val roadConditionsAhead: String? = CustomFileUtils.getFile(BaseConstant.roadConditionsAhead)
        val electronicEyeBroadcast: String? =
            CustomFileUtils.getFile(BaseConstant.electronicEyeBroadcast)
        val safetyReminder: String? = CustomFileUtils.getFile(BaseConstant.safetyReminder)

        settingComponent.setConfigKeyRoadWarn(
            roadConditionsAhead?.toIntOrNull() ?: 0
        ) //巡航播报前方路况  0：off； 1：on
        settingComponent.setConfigKeySafeBroadcast(
            electronicEyeBroadcast?.toIntOrNull() ?: 0
        ) //巡航播报电子眼播报  0：off； 1：on
        settingComponent.setConfigKeyDriveWarn(
            safetyReminder?.toIntOrNull() ?: 0
        ) //巡航播报安全提醒  0：off； 1：on

        val configMediaType = if (isLogin()) {
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)
                .getIntValue(
                    BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.userMediaType.toString(),
                    1
                )
        } else {
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)
                .getIntValue(MapSharePreference.SharePreferenceKeyEnum.mediaType, 1)
        }
        CommonUtils.setSystemProperties("persist.map.media.type", configMediaType.toString())
        Timber.d(" mapMedia： $configMediaType")

        //配置算路参数，具体定义RouteControlKey，其他配置可查看开发指南
        //设置播报类型,0无效 播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
        val mCurrentBroadcastMode = settingComponent.getConfigKeyBroadcastMode()
        /**
         * 播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
         */
        if (mCurrentBroadcastMode == SettingConst.BROADCAST_EASY) {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "4");
        } else if (mCurrentBroadcastMode == SettingConst.BROADCAST_MINIMALISM) {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "6");
        } else {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "2");
        }

        //显示路况
        val configValue = settingComponent.getConfigKeyRoadEvent()
        Timber.d(" setTmcVisible ConfigKeyRoadEvent： $configValue")
        mapBusiness.setTmcVisible(configValue == KEY_ROAT_OPEN)
        mapBusiness.setRouteTrafficVisible(true)
        settingComponent.setConfigKeyRoadEvent(configValue)

        //设置是否支持开启多备选路线 0，不开启多备选路线；1，开启
        mNaviController.routeControl(RouteControlKey.RouteControlKeySetMutilRoute, "1")
        //途经路聚合信息，需要(RouteControlKeySetLongDistInfo，"1") 开启长途信息透出才会透出
        mNaviController.routeControl(RouteControlKey.RouteControlKeySetLongDistInfo, "1")
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        val accountProfile = userBusiness.accountInfo()
        val userName =
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                .getStringValue(MapSharePreference.SharePreferenceKeyEnum.userName, "")
        return !userName.isNullOrEmpty() || accountProfile != null
    }

    //根据本地保存的角色音VoiceId或者恢复默认设置语音包
    private fun todoSetVoice() {
        /**判断是否显示主图按钮或者显示激活界面或者鲸图界面
         *通过添加 distinctUntilChanged() 方法，只有当 isShowActivate 或 isShowAgreement 的值发生变化时，
         * 才会触发观察者并执行处理逻辑。这样可以避免重复执行相同的逻辑。
         */
        MainScope().launch {
            speechSynthesizeBusiness.voiceDataSpeechSynthesizeReady.unPeek().distinctUntilChanged()
                .observeForever { (voiceDataList, initSpeech) ->
                    // 在后台线程中执行其他操作
                    Timber.i("todoSetVoice voiceDataList:${voiceDataList.size} initSpeech:$initSpeech")
                    MainScope().launch {
                        withContext(Dispatchers.IO) {
                            if (voiceDataList.isEmpty() || !initSpeech)
                                return@withContext
                            val result = speechSynthesizeBusiness.todoSetVoice() //根据本地保存的角色音VoiceId或者根据新的voiceId设置角色音
                            Timber.i("setUseVoice result:$result")
                        }
                    }

                }
        }
    }

    fun unInit() {
        Timber.i("unInit")
        mapBusiness.unInit()
        modelBusiness.unInit()
        voiceDataBusiness.unInit()
        speechSynthesizeBusiness.unInit()
        forecastBusiness.unInit()
        activationMapBusiness.unInit()
    }

    private fun getSDKInitParams(): SDKInitParams {
        val initParam = SDKInitParams()
        initParam.logPath = AutoConstant.BLLOG_DIR
        initParam.logLevel = engineerBusiness.engineerConfig.blLogLevel
        initParam.cookieDBPath = AutoConstant.PATH
        initParam.restConfigPath = AutoConstant.PATH
        initParam.assetPath = "/android_assets/blRes/"
        initParam.cachePath = AutoConstant.PATH
        initParam.userDataPath = AutoConstant.PATH
        initParam.cfgFilePath = AutoConstant.PATH
        initParam.offlinePath = AutoConstant.OFFLINE_DOWNLOAD_DIR
        initParam.onlinePath = AutoConstant.PATH + "/online/"
        initParam.activateFilePath = AutoConstant.MAP_ACTIVE_DIR
        //配置车道级离线地图数据
        initParam.lndsOfflinePath = AutoConstant.PATH + "/data/navi/ld/chn"
        initParam.bSDKLogcat = engineerBusiness.engineerConfig.isBlLogcat


        //HMI的aar如果是单包多渠道，则需要指定渠道名称，用于单包多渠道资源拷贝。单包多渠道指的是assets/blRes/下存在channel目录，channel下有多个子目录，每一个目录的名称即为渠道名，例如C04XXXX
        initParam.channelName = AutoConstant.CHANNEL_NAME
        initParam.checkMode = FileCopyCheckMode.FileContent

        FileUtils.createDir(AutoConstant.BLLOG_DIR, false)
        FileUtils.createDir(initParam.offlinePath)
        FileUtils.createDir(initParam.onlinePath)
        Timber.i("getSDKInitParams SD_PATH = ${AutoConstant.SD_PATH}")
        initParam.serverType =
            if (engineerBusiness.engineerConfig.isDev) ServiceManagerEnum.AosDevelopmentEnv else ServiceManagerEnum.AosProductionEnv
        return initParam
    }

    private fun copyAssetsFiles(descDirPath: String) {
        // 若 amapauto20/ 目录创建失败, 可能导致 bl 初始化失败
        val success: Boolean = FileUtils.createDir(descDirPath)
        var filename: File?
        /* 750 SDK内部直接读取，HMI无需拷贝
        filename = File(descDirPath, "GNaviConfig.xml")
        AssertUtils.copyAssetsFileForCrc(
            mApp.applicationContext,
            "blRes/GNaviConfig.xml",
            filename.absolutePath
        )

        //内置纹理
        filename = File(descDirPath + "LayerAsset")
        if (!filename.exists()) {
            filename.mkdirs()
            AssertUtils.copyFilesAssets(
                mApp.applicationContext,
                "blRes/LayerAsset",
                filename.absolutePath
            )
            val file = File(filename.absolutePath + "/LayerImages.cmb")
            val fileTo = File(filename.absolutePath + "/libcmb_LayerImages.so")
            file.renameTo(fileTo)
            //因默认纹理副屏部分纹理不支持  copy style_1.json，去除飞线、导航电子眼、分岐路  覆盖aar中style_3.json
            filename = File(descDirPath + "LayerAsset/style_3.json")
            AssertUtils.copyFilesAssets(
                mApp.applicationContext,
                "style_3.json",
                filename.absolutePath
            )
        }
        filename = File(descDirPath + "LayerAsset/font")
        if (!filename.exists()) {
            AssertUtils.copyFilesAssets(mApp.applicationContext, "font", filename.absolutePath)
        }*/

        //卡片服务
        filename = File(descDirPath + "CardRes/images")
        if (!filename.exists()) {
            AssertUtils.copyFilesAssets(mApp.applicationContext, "images", filename.absolutePath)
        }
//        filename = File(descDirPath + "dynamic")
//        if (!filename.exists()) {
//            AssertUtils.copyFilesAssets(mApp.applicationContext, "dynamic", filename.absolutePath)
//        }
        //使用HMI 自己的字体，需要自行拷贝管理，如果是使用默认字体，则HMI无需自己拷贝
        filename = File(descDirPath + "font")
        if (!filename.exists()) {
            filename.mkdirs()
        }
//        filename = File(descDirPath + "font/SDK_Font_medium.ttf")
//        AssertUtils.copyAssetsFileForCrc(
//            mApp.applicationContext,
//            "SDK_Font_medium.ttf",
//            filename.absolutePath
//        )


        /*750 SDK内部直接读取，HMI无需拷贝
        //离线数据解压
        filename = File(descDirPath + "res") //sdcard/amapauto20/GNaviConfig.xml
        if (!filename.exists()) {
            filename.mkdirs()
            AssertUtils.copyFilesAssets(mApp.applicationContext, "blRes/res", filename.absolutePath)
        }
        filename = File(descDirPath, "GRestConfig.ini")
        AssertUtils.copyAssetsFileForCrc(
            mApp.applicationContext,
            "blRes/GRestConfig.ini",
            filename.absolutePath
        )
        filename = File(descDirPath, "GblConfig.json")
        if (!filename.exists()) { // 根据网络重新生成GblConfig.json
            AssertUtils.copyFilesAssets(
                mApp.applicationContext,
                "blRes/GblConfig.json",
                filename.absolutePath
            )
        }
        if (AssertUtils.isFileExist(mApp.applicationContext, "blRes/EhpConfig.dat")) {
            filename = File(descDirPath, "EhpConfig.dat")
            if (!filename.exists()) { // 根据网络重新生成EhpConfig.dat
                AssertUtils.copyFilesAssets(
                    mApp.applicationContext,
                    "blRes/EhpConfig.dat",
                    filename.absolutePath
                )
            }
        }*/

        // biz资源
        /*filename = File(descDirPath, "style_bl.json")
        AssertUtils.copyAssets(mApp.applicationContext, filename, "style_bl.json")*/
        filename = File(descDirPath, "style.json")
        AssertUtils.copyAssets(mApp.applicationContext, filename, "style.json")
        /*filename = File(descDirPath, "hmi_style_1.json")
        AssertUtils.copyAssets(mApp.applicationContext, filename, "hmi_style_1.json")*/

        /*750 SDK内部直接读取，HMI无需拷贝
         * 地图离线数据所需配置文件: all_city_compile.json 用于 MapDataService.init()
         * 使用 gbl.aar内置文件
         **//*filename = File(AutoConstant.OFFLINE_CONF_DIR)
        AssetUtils.copyAssetsFolder(
            mApp.applicationContext,
            "blRes/offline_conf",
            filename.absolutePath,
            false
        )

        // 复制 mapAsset
        filename = File(AutoConstant.MAPASSET_DIR)
        AssetUtils.copyAssetsFolder(
            mApp.applicationContext,
            "blRes/MapAsset",
            filename.absolutePath,
            false
        )

        //主题所需配置文件： themedata.json
        filename = File(AutoConstant.THEME_CONF_DIR)
        if (!filename.exists()) {
            filename.mkdirs()
        }
        filename = File(AutoConstant.THEME_CONF_DIR, "themedata2.json")
        if (!filename.exists()) {
            AssertUtils.copyFilesAssets(
                mApp.applicationContext,
                "blRes/theme_conf/themedata2.json",
                filename.absolutePath
            )
        }

        //520新增
        val localizationFileName = File(descDirPath, "localization/")
        if (!localizationFileName.exists()) {
            AssertUtils.copyFilesAssets(
                mApp.applicationContext,
                "blRes/localization",
                localizationFileName.absolutePath
            )
        }
        val lndsConfigFile: File = File(AutoConstant.PATH, "GLndsConfig.xml")
        if (!lndsConfigFile.exists()) {
            AssertUtils.copyFilesAssets(
                mApp.applicationContext,
                "blRes/GLndsConfig.xml",
                lndsConfigFile.absolutePath
            )
        }*/

        // 车道级导航回放文件
//        AssetUtils.copyAssetsFolder(
//            mApp.applicationContext,
//            "lanetest/loc_replay/",
//            AutoConstant.GPS_LANELOC_FOLDER,
//            false
//        )

        // 复制仿真回放数据recorder
//        AssetUtils.copyAssetsFolder(
//            mApp.applicationContext,
//            "lanetest/recorder/",
//            AutoConstant.RECORDER_DATA_DIR,
//            false
//        )

        // 复制车道级车标,引导线等资源文件(329之后需要hmi自行提供, aar包中不内置)
//        AssetUtils.copyAssetsFolder(
//            mApp.applicationContext,
//            "bl_lane_Res/LaneCarSRResource",
//            AutoConstant.OFFLINE_LANERESOURCE_DIR,
//            false
//        )
        //初始化审图组件
        copyReviewMapFile()
    }

    /**
     * todo 对讲模块初始化
     *
     */
    private fun initGroupChatService() {
        /*if (ComponentManager.getInstance().isComponentExist(mApp.applicationContext, ComponentConstant.GROUP_CHAT)) {
            mGroupChatService = ComponentManager.getInstance().getComponentGroupChatService()
            mGroupChatService.initGroupChatService()
        }*/
    }

    /**
     * 初始化审图组件
     */
    private fun copyReviewMapFile() {
        //todo 暂不清楚是否需要
    }

    /**
     * aar包有效期是否超期
     *
     * @return 是否超期
     */
    private fun isOverdue(): Boolean {
        //单位微秒
        val limitTime = ServiceMgr.getServiceMgrInstance().sdkLimitTimeUTC
        if (limitTime == 0L) {
            return false
        }
        val limitTimeMillis = limitTime / 1000
        val currentTime = System.currentTimeMillis()
        Timber.i("====limitTimeMillis = $limitTimeMillis， currentTime = $currentTime")
        return limitTimeMillis < currentTime
    }

    fun initAuthentication() {
        authenticationController.init()
        authenticationController.registerAuthenticationObserver(object : IAuthenticationObserver {
            override fun onStatusUpdated(reqId: Int, info: ArrayList<AuthenticationGoodsInfo>) {
                Timber.i("initAuthentication onStatusUpdated reqId $reqId")
            }

            override fun onError(result: AuthenticationResult) {
                Timber.i("initAuthentication onError result $result")
                var msg: String = ""
                authenticationController.getAuthenticationGoodsInfo(result.functionId)?.map {
                    msg = it.goodsName + ":"
                }
                when (result.status) {
                    AuthenticationStatus.AuthenticationStatusNotSync -> {
                        msg += "鉴权信息未同步服务"
                    }

                    AuthenticationStatus.AuthenticationStatusNotOpen -> {
                        msg += "功能未开通"
                    }

                    AuthenticationStatus.AuthenticationStatusExpired -> {
                        msg += "功能已过期"
                    }

                    else -> {
                        msg += "使用中"
                    }
                }
                Timber.i("initAuthentication $msg")
                /*CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(mApp.applicationContext, msg, Toast.LENGTH_LONG).show()
                }*/
            }
        })
        authenticationController.syncRequest()
    }

    /**
     * 监听SDK初始化结果
     */
    fun getInitResult(): LiveData<InitSDKResult> {
        return mbInitResult
    }

    /**
     * 监听激活模块是否已经init
     */
    fun getActiveInit(): LiveData<Boolean> {
        return activeInit
    }

    fun isInitSuccess(): Boolean {
        return (initResult.code == InitSdkResultType.OK).also {
            if (!it) {
                Timber.i("isInitFail: ${initResult.code} msg:${initResult.msg}")
            }
        }
    }

    fun factoryReset() {
        Timber.i("factoryReset")
        unInit()
        sDKManager.unInit()

        ServiceMgr.getServiceMgrInstance().factoryReset(FactoryResetParam().apply {
            canDelete = true
            cachePath = AutoConstant.PATH
            typeMask = FactoryResetType.All.toLong()
        }, object : IFactoryResetObserver {
            override fun onResult(info: FactoryResetResult?) {
                info?.run { Timber.i("onResult FactoryResetResult=${gson.toJson(this)}") }
            }

            override fun log(strLog: String?) {
                Timber.i("strLog:$strLog")
            }
        })
    }

    data class InitSDKResult(var code: Int, var msg: String)

}