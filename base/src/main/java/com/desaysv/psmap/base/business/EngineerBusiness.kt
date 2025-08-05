package com.desaysv.psmap.base.business

import android.os.Process
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.model.MapRenderMode
import com.autonavi.gbl.pos.observer.IPosSensorParaObserver
import com.autonavi.gbl.servicemanager.ServiceMgr
import com.autonavi.gbl.servicemanager.model.ALCGroup
import com.autonavi.gbl.servicemanager.model.ALCLogLevel
import com.autosdk.bussiness.common.utils.FileUtils
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.manager.SDKManager
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.common.AutoConstant
import com.autosdk.common.lane.LaneMockMode
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.config.AutoEggConfig
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EngineerBusiness @Inject constructor(
    private val locationController: LocationController,
    private val layerController: LayerController,
    private val mapController: MapController,
    private val sharePreferenceFactory: SharePreferenceFactory
) : IPosSensorParaObserver {

    @Inject
    lateinit var mapDataBusiness: MapDataBusiness

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var sdkManager: SDKManager

    private val sharePreference: MapSharePreference by lazy {
        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.eggSetting)
    }

    private val mainMapLayer: MapLayer by lazy {
        layerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }
    private val mainMapView: MapView by lazy {
        mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    val engineerConfig: AutoEggConfig by lazy {
        AutoEggConfig().apply {
            sharePreference.let {
                blLogLevel = it.getLongValue(
                    MapSharePreference.SharePreferenceKeyEnum.blLogcatLevel, ALCLogLevel.LogLevelNone
                )//BL日志等级默认关闭
                /*todo 目前没有控制
                isBlLogcat = it.getBooleanValue(
                    MapSharePreference.SharePreferenceKeyEnum.blLogcat, true
                )
                hmiLogLevel = it.getIntValue(
                    MapSharePreference.SharePreferenceKeyEnum.eggHmiLog, Log.DEBUG
                )
                laneMockMode = it.getIntValue(
                    MapSharePreference.SharePreferenceKeyEnum.eggLaneMode, LaneMockMode.LANE_MOCK_DISABLE
                )
                isDev = it.getBooleanValue(
                    MapSharePreference.SharePreferenceKeyEnum.eggServerType, false
                )*/
                openMapTestUuid = it.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.openMapTestUuid, false)
                mapTestUuid = it.getStringValue(MapSharePreference.SharePreferenceKeyEnum.mapTestUuid, "")
                isBlPosLog = it.getBooleanValue(
                    MapSharePreference.SharePreferenceKeyEnum.blPosLog, true
                )//定位日志默认开启

                //重置标志位，因为定位日志默认开启
                it.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.blPosLog, true)
                //重置标志位，BL日志等级默认关闭
                it.putLongValue(
                    MapSharePreference.SharePreferenceKeyEnum.blLogcatLevel, ALCLogLevel.LogLevelNone
                )
            }


        }
    }

    //后端融合是否关闭
    private val _offDRBack = MutableLiveData(engineerConfig.offDRBack)
    val offDRBack: LiveData<Boolean> = _offDRBack

    //gps调试是否开启
    private val _gpsTest = MutableLiveData(engineerConfig.gpsTest)
    val gpsTest: LiveData<Boolean> = _gpsTest

    //定位回放是否开启
    private val _replayPosTest = MutableLiveData(engineerConfig.replayPosTest)
    val replayPosTest: LiveData<Boolean> = _replayPosTest

    //接续算路是否开启
    private val _elecContinue = MutableLiveData(engineerConfig.elecContinue)
    val elecContinue: LiveData<Boolean> = _elecContinue

    //设置起始点是否开启
    private val _openStartPoint = MutableLiveData(locationController.openEngineerPosition)
    val openStartPoint: LiveData<Boolean> = _openStartPoint

    private val debugInfo = StringBuilder() //浮层信息信息

    private val sensorInfo = StringBuilder() //传感器信息

    private var drInfo = "unknown"//标定状态

    //定位日志开关状态
    private val _blPosLog = MutableLiveData(engineerConfig.isBlPosLog)
    val blPosLog: LiveData<Boolean> = _blPosLog

    //BL日志等级
    private val _blLogLevel = MutableLiveData(engineerConfig.blLogLevel)
    val blLogLevel: LiveData<Long> = _blLogLevel

    /**
     * 启动就执行初始化加载放这里
     */
    fun initEngineerData() {
        sharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.startingPoint, false).let {
            if (it) {
                saveStartCarPosition(
                    sharePreference.getStringValue(
                        MapSharePreference.SharePreferenceKeyEnum.startingPointLongitude,
                        ""
                    ),
                    sharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.startingPointLatitude, "")
                )
                sharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.startingPoint, false)
            }
        }
    }

    val sdkVersion: String
        get() {
            return sdkManager.version
        }

    val engineVersion: String
        get() {
            return sdkManager.engineVersion
        }

    val mapDataEngineVersion: String
        get() {
            return sdkManager.mapDataEngineVersion
        }

    fun openGpsTest(open: Boolean) {
        Timber.i("openGpsTest $open")
        if (open) locationController.addSensorParaObserver(this) else locationController.removeSensorParaObserver(this)
        _gpsTest.postValue(open)
    }

    fun openEngineerPosition(open: Boolean) {
        locationController.openEngineerPosition = open
        _openStartPoint.postValue(locationController.openEngineerPosition)
    }

    /**
     * 获取GPS浮层信息
     */
    fun getDebugInfo(): String {
        var location = locationController.gnssLocation
        debugInfo.setLength(0)
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        if (location == null) {
            debugInfo
                .append("系统时间:${formatter.format(Calendar.getInstance().time)}")
                .append("\nGPS时间:unknown")
                .append("\nGPS是否可用：${locationController.isLocationEnabled}")
                .append("\n经度:unknown 纬度:unknown")
                .append("\n高度:unknown 速度:unknown 方向:unknown")
                .append("\n地址:unknown")
                .append("\n定位精度:unknown")
                .append("\n收到卫星颗数:${locationController.gsvNum}")
        } else {
            debugInfo
                .append("系统时间:${formatter.format(Calendar.getInstance().time)}")
                .append("\nGPS时间:${formatter.format(location.time)}")
                .append("\nGPS是否可用：${locationController.isLocationEnabled}")
                .append("\n经度:${location.longitude} 纬度:${location.latitude}")
                .append("\n高度:${location.altitude} 速度:${location.speed} 方向:${location.bearing}")
                .append("\n地址:${mapDataBusiness.getCityInfo(location.longitude, location.latitude)?.cityName}")
                .append("\n定位精度:${location.accuracy}")
                .append("\n收到卫星颗数:${locationController.gsvNum}")
        }
        debugInfo.append("\n正常场景下的帧率：" + engineerConfig.foreground_MapRenderModeNormal);
        debugInfo.append("\n导航场景下的帧率：" + engineerConfig.foreground_MapRenderModeNavi);
        debugInfo.append("\n动画场景下的帧率：" + engineerConfig.foreground_MapRenderModeAnimation);
        debugInfo.append("\n手势操作下的帧率：" + engineerConfig.foreground_MapRenderModeGestureAction);
        debugInfo.append("\n网络是否连接：" + netWorkManager.isNetworkConnected() + "\n网络类型：" + netWorkManager.getNetWorkState().msg);
        return debugInfo.toString()
    }

    fun sensorInfo(): String {
        sensorInfo.setLength(0)
        sensorInfo
            .append("\n脉冲车速:${locationController.speed}")
            .append("\nGyroX：${locationController.locGyroInfo?.valueX}")
            .append("\nGyroY：${locationController.locGyroInfo?.valueY}")
            .append("\nGyroZ：${locationController.locGyroInfo?.valueZ}")
            .append("\nAccX：${locationController.locAcce3DInfo?.acceX}")
            .append("\nAccY：${locationController.locAcce3DInfo?.acceY}")
            .append("\nAccZ：${locationController.locAcce3DInfo?.acceZ}")
            .append("\n标定状态:")
            .append("\n${drInfo}")
        return sensorInfo.toString()
    }

    /**
     * 设置帧率
     * @param isForeground 前台/后台
     */
    fun setRenderFps(
        isForeground: Boolean,
        mapRenderModeNormal: Int,
        mapRenderModeNavi: Int,
        mapRenderModeAnimation: Int,
        mapRenderModeGestureAction: Int
    ) {
        Timber.i("setRenderFps isForeground=$isForeground mapRenderModeNormal=$mapRenderModeNormal mapRenderModeNavi=$mapRenderModeNavi mapRenderModeAnimation=$mapRenderModeAnimation mapRenderModeGestureAction=$mapRenderModeGestureAction")
        if (mapRenderModeNormal <= 0 || mapRenderModeNormal > 60) return
        if (mapRenderModeNavi <= 0 || mapRenderModeNavi > 60) return
        if (mapRenderModeAnimation <= 0 || mapRenderModeAnimation > 60) return
        if (mapRenderModeGestureAction <= 0 || mapRenderModeGestureAction > 60) return
        if (isForeground) {
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN, MapRenderMode.MapRenderModeNormal, mapRenderModeNormal
            )
            // 修改导航场景下的帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN, MapRenderMode.MapRenderModeNavi, mapRenderModeNavi
            )
            // 修改动画场景下的帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN, MapRenderMode.MapRenderModeAnimation, mapRenderModeAnimation
            )
            // 修改手势操作时帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN, MapRenderMode.MapRenderModeGestureAction, mapRenderModeGestureAction
            )
            /**
             * 前台帧率
             */
            engineerConfig.foreground_MapRenderModeNormal = mapRenderModeNormal// 正常场景下的帧率
            engineerConfig.foreground_MapRenderModeNavi = mapRenderModeNavi//导航场景下的帧率
            engineerConfig.foreground_MapRenderModeAnimation = mapRenderModeAnimation// 动画场景下的帧率
            engineerConfig.foreground_MapRenderModeGestureAction = mapRenderModeGestureAction//手势操作时帧率

        } else {
            /**
             * 后台帧率
             */
            engineerConfig.backend_MapRenderModeNormal = mapRenderModeNormal// 正常场景下的帧率
            engineerConfig.backend_MapRenderModeNavi = mapRenderModeNavi//导航场景下的帧率
            engineerConfig.backend_MapRenderModeAnimation = mapRenderModeAnimation// 动画场景下的帧率
            engineerConfig.backend_MapRenderModeGestureAction = mapRenderModeGestureAction//手势操作时帧率
        }
    }

    fun resetRenderFps() {
        Timber.i("resetRenderFps")
        mapController.setRenderFpsByMode(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            MapRenderMode.MapRenderModeNormal,
            AutoEggConfig.FOREGROUND_MapRenderModeNormal
        )
        // 修改导航场景下的帧率
        mapController.setRenderFpsByMode(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            MapRenderMode.MapRenderModeNavi,
            AutoEggConfig.FOREGROUND_MapRenderModeNavi
        )
        // 修改动画场景下的帧率
        mapController.setRenderFpsByMode(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            MapRenderMode.MapRenderModeAnimation,
            AutoEggConfig.FOREGROUND_MapRenderModeAnimation
        )
        // 修改手势操作时帧率
        mapController.setRenderFpsByMode(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            MapRenderMode.MapRenderModeGestureAction,
            AutoEggConfig.FOREGROUND_MapRenderModeGestureAction
        )
        //前台帧率
        engineerConfig.foreground_MapRenderModeNormal = AutoEggConfig.FOREGROUND_MapRenderModeNormal// 正常场景下的帧率
        engineerConfig.foreground_MapRenderModeNavi = AutoEggConfig.FOREGROUND_MapRenderModeNavi//导航场景下的帧率
        engineerConfig.foreground_MapRenderModeAnimation = AutoEggConfig.FOREGROUND_MapRenderModeAnimation// 动画场景下的帧率
        engineerConfig.foreground_MapRenderModeGestureAction =
            AutoEggConfig.FOREGROUND_MapRenderModeGestureAction//手势操作时帧率
        //后台帧率
        engineerConfig.backend_MapRenderModeNormal = AutoEggConfig.BACKEND_MapRenderModeNormal// 正常场景下的帧率
        engineerConfig.backend_MapRenderModeNavi = AutoEggConfig.BACKEND_MapRenderModeNavi//导航场景下的帧率
        engineerConfig.backend_MapRenderModeAnimation = AutoEggConfig.BACKEND_MapRenderModeAnimation// 动画场景下的帧率
        engineerConfig.backend_MapRenderModeGestureAction = AutoEggConfig.BACKEND_MapRenderModeGestureAction//手势操作时帧率
    }

    /**
     * 工程模式设置起始点
     */
    fun saveStartCarPosition(sLon: String, sLat: String, save: Boolean = false): Boolean {
        Timber.i("setDefaultCarPosition save = $save")
        if (!CommonUtils.checkLoLa(sLon, sLat)) {
            Timber.i("setStartCarPosition checkLoLa false")
            return false
        }
        var lon = 0.0
        var lat = 0.0
        //工程模式 设置起始点
        try {
            lon = sLon.toDouble()
            lat = sLat.toDouble()
        } catch (e: NumberFormatException) {
            Timber.e(e, "setDefaultCarPosition")
        }
        locationController.setEngineerPosition(lon, lat)
        mainMapLayer.setFollowMode(true)
        mainMapLayer.setCarPosition(
            locationController.lastLocation.longitude,
            locationController.lastLocation.latitude,
            locationController.lastLocation.bearing
        )
        mainMapView.operatorPosture.mapCenter = Coord3DDouble(
            locationController.lastLocation.longitude, locationController.lastLocation.latitude, 0.0
        )
        if (save) {
            sharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.startingPoint, true)
            sharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.startingPointLongitude, sLon)
            sharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.startingPointLatitude, sLat)
        } else {
            sharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.startingPoint, false)
        }
        return true
    }

    fun openTestUUID(open: Boolean) {
        Timber.i("openTestUUID open=$open")
        sharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.openMapTestUuid, open)
        engineerConfig.openMapTestUuid = open
    }

    fun setTestUUID(uuid: String) {
        Timber.i("setTestUUID uuid=$uuid")
        sharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.mapTestUuid, uuid)
        engineerConfig.mapTestUuid = uuid
    }

    /**
     * 是否关闭后端融合
     */
    fun offDRBack(isOff: Boolean) {
        Timber.i("offDRBack $isOff")
        locationController.pauseVelocityPulse(isOff)
        engineerConfig.offDRBack = isOff
        _offDRBack.postValue(engineerConfig.offDRBack)
    }

    fun switchReplayPosTest(open: Boolean) {
        _replayPosTest.postValue(open)
    }

    fun setElecContinue(open: Boolean) {
        _elecContinue.postValue(open)
        engineerConfig.elecContinue = open
    }

    /**
     * 定位日志默认打开
     * @param open 打开/关闭定位日志
     * @param save 是否保存到sp下次启动也立即生效
     */
    fun switchPosRecord(open: Boolean, save: Boolean = false) {
        Timber.i("switchPosRecord open=$open save=$save")
        sharePreference.putBooleanValue(
            MapSharePreference.SharePreferenceKeyEnum.blPosLog, if (save) open else false
        )
        engineerConfig.isBlPosLog = open
        locationController.switchPosRecord(engineerConfig.isBlPosLog)
        _blPosLog.postValue(engineerConfig.isBlPosLog)
    }

    /**
     * 创建网络log标志位
     */
    fun switchNetWorkLog(b: Boolean) {
        val file = File(BaseConstant.NETWORK_LOG_FLAG)
        if (b) {
            FileUtils.createFile(BaseConstant.NETWORK_LOG_FLAG)
            Timber.i("network flag is created:${FileUtils.checkFileExists(BaseConstant.NETWORK_LOG_FLAG)}")
        } else {
            if (file.exists()) {
                val deleteStatus = file.delete()
                Timber.i("network flag is deleted $deleteStatus")
            }
        }
    }

    /**
     * BL日志默认关闭
     * 设置BL日志等级
     * @param save 是否保存到sp下次启动也立即生效
     */
    fun setBLLogLevel(logLevel: Long, save: Boolean = false) {
        Timber.i("setBLLogLevel $logLevel isSave=$save")
        sharePreference.putLongValue(
            MapSharePreference.SharePreferenceKeyEnum.blLogcatLevel, if (save) logLevel else ALCLogLevel.LogLevelNone
        )

        when (logLevel) {
            ALCLogLevel.LogLevelError -> {
                ServiceMgr.getServiceMgrInstance().switchLog(ALCLogLevel.LogLevelError)
                ServiceMgr.getServiceMgrInstance().setGroupMask(ALCGroup.GROUP_MASK_ALL)
            }

            ALCLogLevel.LogLevelWarn -> {
                ServiceMgr.getServiceMgrInstance().switchLog(ALCLogLevel.LogLevelWarn)
                ServiceMgr.getServiceMgrInstance().setGroupMask(ALCGroup.GROUP_MASK_ALL)
            }

            ALCLogLevel.LogLevelInfo -> {
                ServiceMgr.getServiceMgrInstance().switchLog(ALCLogLevel.LogLevelInfo)
                ServiceMgr.getServiceMgrInstance().setGroupMask(ALCGroup.GROUP_MASK_ALL)
            }

            ALCLogLevel.LogLevelDebug -> {
                ServiceMgr.getServiceMgrInstance().switchLog(ALCLogLevel.LogLevelDebug)
                ServiceMgr.getServiceMgrInstance().setGroupMask(ALCGroup.GROUP_MASK_ALL)
            }

            ALCLogLevel.LogLevelVerbose -> {
                ServiceMgr.getServiceMgrInstance().switchLog(ALCLogLevel.LogLevelVerbose)
                ServiceMgr.getServiceMgrInstance().setGroupMask(ALCGroup.GROUP_MASK_ALL)
            }

            else -> {
                ServiceMgr.getServiceMgrInstance().switchLog(ALCLogLevel.LogLevelNone)
                ServiceMgr.getServiceMgrInstance().setGroupMask(ALCGroup.GROUP_MASK_ALL)
            }
        }
        engineerConfig.blLogLevel = logLevel
        _blLogLevel.postValue(engineerConfig.blLogLevel)
    }

    suspend fun resetMap() {
        Timber.i("resetMap")
        withContext(Dispatchers.IO) {
            FileUtils.listSubFiles(AutoConstant.PATH)?.map { file ->
                if (file.absolutePath.contains(AutoConstant.MAP_ACTIVE_DIR.dropLast(1))) {
                    Timber.i("resetMap ${file.name} not delete")
                } else {
                    if (file.exists()) {
                        if (file.isFile)
                            file.delete()
                        else FileUtils.deleteDir(file)
                    }
                }
            }
            Timber.i("resetMap killProcess")
            Thread.sleep(5000)
            try {
                Process.killProcess(Process.myPid())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 设置timber日志等级 todo 待实现
     */
    fun setHIMLogLevel(logLevel: Int, save: Boolean = false) {

    }

    /**
     * AutoSDK底层JNI层的日志是否打印到logcat一同输出
     * 默认打开,重启生效
     */
    fun setBLJNILog(isOpen: Boolean) {
        Timber.i("setBLJNILog isOpen $isOpen")
        sharePreference.putBooleanValue(
            MapSharePreference.SharePreferenceKeyEnum.blLogcat, isOpen
        )
        engineerConfig.isBlLogcat = isOpen
    }

    /**
     * 车道回放模式 算路模拟回放只能纯GPS定位，仿真回放只能后端融合
     * todo 暂不清楚用途，车道级别导航用到
     */
    fun setLaneMockMode(@LaneMockMode.LaneMockMode1 laneMockMode: Int) {
        Timber.i("setLaneMockMode $laneMockMode")
        sharePreference.putIntValue(
            MapSharePreference.SharePreferenceKeyEnum.eggLaneMode, laneMockMode
        )
        engineerConfig.laneMockMode = laneMockMode
    }

    override fun onSensorParaUpdate(info: String?) {
        info?.run { drInfo = this }
    }
}