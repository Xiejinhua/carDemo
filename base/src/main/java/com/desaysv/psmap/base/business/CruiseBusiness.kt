package com.desaysv.psmap.base.business

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.guide.model.CruiseEventInfo
import com.autonavi.gbl.guide.model.CruiseFacilityInfo
import com.autonavi.gbl.guide.model.CruiseInfo
import com.autonavi.gbl.guide.model.LaneInfo
import com.autonavi.gbl.guide.model.NaviCameraExt
import com.autonavi.gbl.guide.model.NaviType
import com.autonavi.gbl.guide.model.SocolEventInfo
import com.autonavi.gbl.guide.model.guidecontrol.Param
import com.autonavi.gbl.guide.model.guidecontrol.Type
import com.autonavi.gbl.guide.observer.ICruiseObserver
import com.autonavi.gbl.layer.model.DynamicLevelType
import com.autonavi.gbl.pos.model.LocInfo
import com.autonavi.gbl.pos.model.LocMatchInfo
import com.autonavi.gbl.pos.model.LocParallelRoadInfo
import com.autonavi.gbl.pos.observer.IPosLocInfoObserver
import com.autonavi.gbl.pos.observer.IPosParallelRoadObserver
import com.autonavi.gbl.user.behavior.model.ConfigKey
import com.autonavi.gbl.user.usertrack.model.GpsTrackPoint
import com.autonavi.gbl.user.usertrack.observer.IGpsInfoGetter
import com.autosdk.bussiness.account.BehaviorController
import com.autosdk.bussiness.layer.CruiseLayer
import com.autosdk.bussiness.layer.DrivingLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.CommonUtil.parseGpsDateTime
import com.desaysv.psmap.base.impl.ISettingComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * 巡航业务类
 */
@Singleton
class CruiseBusiness @Inject constructor(
    private val layerController: LayerController,
    private val settingComponent: ISettingComponent,
    private val userBusiness: UserBusiness,
    @ApplicationContext private val context: Context,
    private val speechSynthesizeBusiness: SpeechSynthesizeBusiness,
    private val locationController: LocationController
) : ICruiseObserver, IPosLocInfoObserver, IPosParallelRoadObserver {
    private val CRUISE_SPEED = 15//进入巡航车速 15 km/h
    private val CRUISE_SPEED_HOLD_TIME = 15000L//持续巡航车速15 km/h达到15秒，开始巡航

    //巡航当前路名
    private val _cruiseRouteName = MutableLiveData("无名道路")
    val cruiseRouteName: MutableLiveData<String> = _cruiseRouteName

    //速度
    private val _cruiseSpeed = MutableLiveData("0")
    val cruiseSpeed: MutableLiveData<String> = _cruiseSpeed

    //巡航车道线
    private val _cruiseLaneList = MutableLiveData<ArrayList<Int>>()
    val cruiseLaneList: LiveData<ArrayList<Int>> = _cruiseLaneList

    private val _showCruiseLane = MutableLiveData(false)
    val showCruiseLane: LiveData<Boolean> = _showCruiseLane

    //巡航状态
    private val _cruiseStatus = MutableLiveData(false)
    val cruiseStatus: MutableLiveData<Boolean> = _cruiseStatus

    //巡航平行路状态 0：不在平行路，1：在主路，2：在辅路，3：在高架上，4：在高架下
    private val _parallelRoadStatus = MutableLiveData(0)
    val parallelRoadStatus: LiveData<Int> = _parallelRoadStatus

    private val roadInfo = intArrayOf(-1, -1, -1)

    //巡航中道路信息，依次是道路等级，道路类型，Link类型, -1是无效值
    private val _cruiseRoadInfo = MutableLiveData(roadInfo)
    val cruiseRoadInfo: LiveData<IntArray> = _cruiseRoadInfo

    val tRTtsBroadcast = MutableLiveData<String>() //查询前⽅路况 tts播报

    //当前位置信息
    private val _stPos = MutableLiveData<Coord3DDouble?>(null)
    val stPos: LiveData<Coord3DDouble?> = _stPos

    private val _naviCameraExt = MutableLiveData<ArrayList<NaviCameraExt>?>()
    val naviCameraExt: LiveData<ArrayList<NaviCameraExt>?> = _naviCameraExt

    private val cruiseLayer: CruiseLayer by lazy {
        layerController.getCruiseLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //导航引导图层
    private val drivingLayer: DrivingLayer by lazy {
        layerController.getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    private val mainMapLayer: MapLayer by lazy {
        layerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    private val cruiseScope = CoroutineScope(Dispatchers.Default + Job())

    private val mHandler = Handler(Looper.getMainLooper())

    private var inHomePage = false
    private var curTime: Long = 0

    private val cruiseRunnable: Runnable = Runnable {
        Timber.i("cruiseRunnable")
        if (mSpeed >= CRUISE_SPEED)
            startCruise()
        hasRunnable = false
    }

    private var mSpeed = 0f

    private var hasRunnable = false

    private var mCruising = false

    private val mGpsTrackPoint = GpsTrackPoint()

    /**
     * 提供给用户服务 GPS轨迹
     */
    private val iGpsInfoGetter = IGpsInfoGetter { mGpsTrackPoint }

    fun init() {
        Timber.i("init")
        locationController.addLocInfoObserver(this)
        drivingLayer.openDynamicLevel(false, DynamicLevelType.DynamicLevelCruise)
        curTime = System.currentTimeMillis() // 当前打点文件时间戳
        if (settingComponent.getAutoRecord() == 0) { //处理未完成的轨迹文件先判断是否开启轨迹打点
            userBusiness.handleUnfinishTrace() //处理未完结或异常退出的打点轨迹文件
        }
    }

    fun unInit() {
        Timber.i("unInit")
        locationController.removeLocInfoObserver(this)
        cruiseScope.cancel()
    }

    fun setInHomePage(inHome: Boolean) {
        Timber.i("setInHomePage $inHome")
        inHomePage = inHome
        if (!inHomePage)
            stopCruise()
    }

    private fun startCruise() {
        if (mCruising) {
            Timber.d("startCruise already Running")
            return
        }
        val isCruise = NaviController.getInstance().startNavi(NaviType.NaviTypeCruise.toLong(), NaviType.NaviTypeCruise)
        Timber.i("startCruise isCruise=$isCruise")
        if (!isCruise)
            return
        drivingLayer.openDynamicLevel(autoScale(), DynamicLevelType.DynamicLevelCruise)
        NaviController.getInstance().registerCruiseObserver(this)
        locationController.addPosParallelRoadObserver(this)
        //初始化巡航参数
        initCruiseParam()
        mainMapLayer.setFollowMode(true)
        mainMapLayer.setPreviewMode(false)
        mCruising = isCruise
        _cruiseStatus.postValue(mCruising)
        if (settingComponent.getAutoRecord() == 0) {
            val diu = CommonUtil.getDeviceID(context)
            var filename: String = curTime.toString() + "_" + 0 + "_" + diu
            if (userBusiness.getCurrentFileName() != filename) {
                userBusiness.registerGpsInfoGetter(iGpsInfoGetter)
                curTime = System.currentTimeMillis()
                filename = curTime.toString() + "_" + 0 + "_" + diu
                userBusiness.startTrackAndhandleUnfinishTrace(filename)
            }
        }
    }

    fun stopCruise() {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mHandler.hasCallbacks(cruiseRunnable)
            } else {
                hasRunnable
            }
        ) {
            Timber.i("stopCruise removeCallbacks")
            mHandler.removeCallbacks(cruiseRunnable)
            hasRunnable = false
        }

        Timber.i("stopCruise mCruising=$mCruising")

        if (mCruising) {
            drivingLayer.openDynamicLevel(false, DynamicLevelType.DynamicLevelCruise)
            NaviController.getInstance().unregisterCruiseObserver(this)
            locationController.removePosParallelRoadObserver(this)
            NaviController.getInstance().stopNavi()//停止巡航
            cruiseLayer.clearAllItems()
            mCruising = false
            _cruiseStatus.postValue(false)
            speechSynthesizeBusiness.otherSetStop(false)
            for (i in roadInfo.indices) {
                roadInfo[i] = -1
            }
            _cruiseRoadInfo.postValue(roadInfo)
            _cruiseRouteName.postValue("无名道路")
            _stPos.postValue(null)
            _naviCameraExt.postValue(null)
            userBusiness.unregisterGpsInfoGetter(iGpsInfoGetter)
            if (settingComponent.getAutoRecord() == 0) {
                userBusiness.closeGpsTrack()
            }
            Timber.i("Cruise stopped")
        }
    }

    private fun initCruiseParam() {
        // 开关配置
        val cruiseParam = Param()
        cruiseParam.type = Type.GuideParamCruise
        //cruiseParam.cruise.mode = 0x7 //使用默认值
        cruiseParam.cruise.cameraNum = 5L //设置电子眼数
        var mode = 0x0 //默认不配置

        //前方路况 0：off； 1：on
        val configKeyRoadWarn = BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyRoadWarn)
        if (configKeyRoadWarn.intValue == 1) mode = mode or 0x1
        //电子眼播报 0：off； 1：on
        val configKeySafeBroadcast = BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeySafeBroadcast)
        if (configKeySafeBroadcast.intValue == 1) mode = mode or 0x2

        //安全提示播报 0：off； 1：on
        val configKeyDriveWarn = BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyDriveWarn)
        if (configKeyDriveWarn.intValue == 1) mode = mode or 0x4
        /**
         * 0x0：无前方巡航电子眼、巡航道路设施、巡航路况播报相关播报文本透出，回调OnUpdateCruiseFacility无相关详情信息透出;
         * 0x1：OnPlayTTS回调透出前方巡航电子眼相关播报文本，同时OnUpdateCruiseFacility回调透出电子眼详情信息;
         * 0x2：OnPlayTTS回调透出前方巡航道路设施相关播报文本，同时OnUpdateCruiseFacility回调透出巡航道路设施详情信息;
         * 0x4：OnPlayTTS回调透出前方巡航路况相关播报文本;
         * 特别说明：
         * 0x3：即0x3=0x1|0x2,为组合开关
         * 0x5：即0x5=0x1|0x4,为组合开关
         * 0x6：即0x6=0x2|0x4,为组合开关
         * 0x7：即0x7=0x1|0x2|0x4,为组合开关 (默认值)
         */
        cruiseParam.cruise.mode = mode
        Timber.i("configKeyRoadWarn=${configKeyRoadWarn.intValue} configKeySafeBroadcast=${configKeySafeBroadcast.intValue} configKeyDriveWarn=${configKeyDriveWarn.intValue} cruiseParam.cruise.mode=$mode")
        NaviController.getInstance().setGuideParam(cruiseParam)

        /*
        //默认是打开的不用配置
        val trParam = Param()
        trParam.type = Type.GuideParamTR
        trParam.tr.enable = true //打开TR开关
        NaviController.getInstance().setGuideParam(trParam)
        */
    }

    //是否在巡航
    fun isCruising(): Boolean {
        return mCruising
    }

    /**
     * 车道线数据处理
     */
    private fun showLaneInfo(laneInfo: LaneInfo) {
        Timber.d("showLaneInfo is called")
        val size = laneInfo.backLane.size
        val laneInfos = ArrayList<Int>(size)
        val sb = java.lang.StringBuilder(96)
        for (i in 0 until size) {
            var item = laneInfo.frontLane[i]
            sb.append(item).append(",")
            item = item or (laneInfo.frontExtenLane[i] shl 8)
            sb.append(laneInfo.frontExtenLane[i]).append(",")
            item = item or (laneInfo.backLane[i] shl 16)
            sb.append(laneInfo.backLane[i]).append(",")
            item = item or (laneInfo.backExtenLane[i] shl 24)
            sb.append(laneInfo.backExtenLane[i]).append("\n")
            laneInfos.add(item)
        }
        if (size > 0) {
            _cruiseLaneList.postValue(laneInfos)
            _showCruiseLane.postValue(true)
        }
    }

    override fun onLocInfoUpdate(p0: LocInfo?) {
        p0?.let { locInfo ->
            mSpeed = abs(locInfo.speed)
            if (mSpeed < CRUISE_SPEED)
                stopCruise()

            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mSpeed >= CRUISE_SPEED && !mHandler.hasCallbacks(cruiseRunnable) && inHomePage && !mCruising
                } else {
                    mSpeed >= CRUISE_SPEED && !hasRunnable && inHomePage && !mCruising
                }
            ) {
                Timber.i("cruiseRunnable start")
                hasRunnable = true
                mHandler.postDelayed(cruiseRunnable, CRUISE_SPEED_HOLD_TIME)
            }
            var speed = abs(locInfo.speed)
            if (locInfo.speedometerIsValid) {
                //如果速度表有效，使用速度表的速度
                speed = locInfo.speedometer
            }
//            Timber.d("onLocInfoUpdate mSpeed=$mSpeed speedometer=${locInfo.speedometer} speed=$speed LocInfo.speedometerIsValid=${locInfo.speedometerIsValid} ")
            if (mCruising)
                _cruiseSpeed.postValue(speed.toInt().toString())

            if (mCruising && locInfo.matchInfo.isNotEmpty()) {
                locInfo.matchInfo[0].run {
                    roadInfo[0] = this.roadClass
                    roadInfo[1] = this.formway
                    roadInfo[2] = this.linkType
                    //Timber.d("roadInfo=${roadInfo.contentToString()}")
                    _cruiseRoadInfo.postValue(roadInfo)
                    this.stPos?.run {
                        _stPos.postValue(this)
                    }
                }
            }

            val matchInfo = locInfo.matchInfo
            val matchInfoSize = matchInfo?.size ?: 0
            if (matchInfoSize > 0) {
                val locMatchInfo: LocMatchInfo = matchInfo.get(0)
                mGpsTrackPoint.f32Course = locMatchInfo.course
                mGpsTrackPoint.f64Latitude = locMatchInfo.stPos.lat
                mGpsTrackPoint.f64Longitude = locMatchInfo.stPos.lon
            }

            val curSpeed = locInfo.speed
            if (locInfo != null) {
                mGpsTrackPoint.f32Accuracy = locInfo.posAcc
                mGpsTrackPoint.f32Speed = curSpeed
                mGpsTrackPoint.f64Altitude = locInfo.alt.toDouble()
//                Timber.d("locInfo.gpsDatetime: " + parseGpsDateTime(locInfo.gpsDatetime))
                mGpsTrackPoint.n64TickTime = parseGpsDateTime(locInfo.gpsDatetime)
            }
        }
    }

    override fun onUpdateCruiseFacility(facilityInfo: ArrayList<CruiseFacilityInfo?>?) {
        //巡航模块的道路设施
        Timber.d("巡航onUpdateCruiseFacility 巡航情况下, 传出巡航探测到的电子眼、道路设施信息等信息 回调")
        /*if (facilityInfo != null && facilityInfo.size > 0) {
            cruiseScope.launch {
                cruiseLayer.updateCruiseFacility(facilityInfo)
            }
        }*/
    }

    /**
     * 710之后版本电子眼透出
     */
    override fun onShowCruiseCameraExt(cameraInfoList: java.util.ArrayList<NaviCameraExt>?) {
        super.onShowCruiseCameraExt(cameraInfoList)
        Timber.d("巡航onShowCruiseCameraExt")
        _naviCameraExt.postValue(cameraInfoList)
    }

    /**
     * 传出自车前方电子眼信息（仅特殊项目使用，不推荐使用，若对接需要经SDK开发人员确认）
     * 巡航情况下,传出自车前方电子眼信息
     */
    override fun onUpdateElecCameraInfo(cameraInfoList: ArrayList<CruiseFacilityInfo?>?) {
        Timber.d("巡航onUpdateElecCameraInfo回调 巡航情况下, 传出自车前方电子眼信息")
        /*if (cameraInfoList != null && cameraInfoList.size > 0) {
            cruiseScope.launch {
                cruiseLayer.updateCruiseCamera(cameraInfoList)
            }
        }*/
    }

    override fun onUpdateCruiseInfo(cruiseInfo: CruiseInfo?) {
        Timber.d("onUpdateCruiseInfo ")
        cruiseInfo?.let {
            if (cruiseInfo.roadName.isNotEmpty()) {
                _cruiseRouteName.postValue(cruiseInfo.roadName)
            } else {
                _cruiseRouteName.postValue("无名道路")
            }
        }
    }

    override fun onShowCruiseLaneInfo(laneInfo: LaneInfo?) {
        Timber.d("onShowCruiseLaneInfo")
        laneInfo?.let {
            showLaneInfo(it)
        }
    }

    override fun onHideCruiseLaneInfo() {
        Timber.d("onHideCruiseLaneInfo")
        _cruiseLaneList.postValue(ArrayList())
        _showCruiseLane.postValue(false)
    }

    override fun onUpdateCruiseEvent(eventInfo: CruiseEventInfo?) {
        Timber.d("巡航onUpdateCruiseEvent回调 巡航情况下, 传出巡航探测到的交通设施信息等")
        /*eventInfo?.let {
            cruiseScope.launch {
                cruiseLayer.updateCruiseEvent(arrayListOf(it))
            }
        }*/

    }

    override fun onUpdateCruiseSocolEvent(socolEventInfo: SocolEventInfo?) {
        Timber.d("巡航onUpdateCruiseSocolEvent回调 巡航情况下, 传出巡航状态下的拥堵事件信息")
        /*socolEventInfo?.let {
            cruiseScope.launch {
                cruiseLayer.updateCruiseCongestionEvent(arrayListOf(it))
            }
        }*/
    }

    override fun onParallelRoadUpdate(locParallelRoadInfo: LocParallelRoadInfo?) {
        locParallelRoadInfo?.run {
            if (this.flag > 0 || this.hwFlag > 0) {
                if (this.flag > 0) {
                    Timber.d("在主辅路")
                    // 0: 无主辅路（车标所在道路旁无主辅路）, 1: 车标在主路（车标所在道路旁有辅路）, 2: 车标在辅路（车标所在道路旁有主路）
                    _parallelRoadStatus.postValue(this.flag)
                } else {
                    // 0: 无高架, 1: 车标在高架上（车标所在道路有对应高架下）, 2: 车标在高架下（车标所在道路有对应高架上）
                    Timber.d("在高架")
                    _parallelRoadStatus.postValue(this.hwFlag + 2)
                }
            } else {
                _parallelRoadStatus.postValue(0)
            }
        } ?: _parallelRoadStatus.postValue(0)
    }

    private fun autoScale(): Boolean {
        return settingComponent.getAutoScale() == 1
    }

}