package com.desaysv.psmap.base.business

import android.app.Application
import android.graphics.Bitmap
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.aosclient.model.ENETWORKSTATUS
import com.autonavi.gbl.aosclient.model.GAimpoiMsg
import com.autonavi.gbl.aosclient.model.GSendToPhoneRequestParam
import com.autonavi.gbl.aosclient.model.GSendToPhoneResponseParam
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.model.RectInt
import com.autonavi.gbl.common.model.TbtCommonControl
import com.autonavi.gbl.common.model.UserConfig
import com.autonavi.gbl.common.model.WorkPath
import com.autonavi.gbl.common.path.model.ElecVehicleETAInfo
import com.autonavi.gbl.common.path.model.LightBarItem
import com.autonavi.gbl.common.path.model.RoadClass
import com.autonavi.gbl.common.path.model.TollGateInfo
import com.autonavi.gbl.common.path.option.RouteOption
import com.autonavi.gbl.common.path.option.RouteType
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.guide.model.CrossImageInfo
import com.autonavi.gbl.guide.model.CrossNaviInfo
import com.autonavi.gbl.guide.model.DriveEvent
import com.autonavi.gbl.guide.model.DriveEventTip
import com.autonavi.gbl.guide.model.DriveReport
import com.autonavi.gbl.guide.model.ExitDirectionInfo
import com.autonavi.gbl.guide.model.LaneAction
import com.autonavi.gbl.guide.model.LaneCategoryType
import com.autonavi.gbl.guide.model.LaneInfo
import com.autonavi.gbl.guide.model.LightBarDetail
import com.autonavi.gbl.guide.model.LightBarInfo
import com.autonavi.gbl.guide.model.LockScreenTip
import com.autonavi.gbl.guide.model.ManeuverConfig
import com.autonavi.gbl.guide.model.ManeuverIconResponseData
import com.autonavi.gbl.guide.model.ManeuverInfo
import com.autonavi.gbl.guide.model.MixForkInfo
import com.autonavi.gbl.guide.model.NaviCameraExt
import com.autonavi.gbl.guide.model.NaviCongestionInfo
import com.autonavi.gbl.guide.model.NaviFacility
import com.autonavi.gbl.guide.model.NaviGreenWaveCarSpeed
import com.autonavi.gbl.guide.model.NaviInfo
import com.autonavi.gbl.guide.model.NaviIntervalCameraDynamicInfo
import com.autonavi.gbl.guide.model.NaviRoadFacility
import com.autonavi.gbl.guide.model.NaviStatisticsInfo
import com.autonavi.gbl.guide.model.NaviType
import com.autonavi.gbl.guide.model.PathTrafficEventInfo
import com.autonavi.gbl.guide.model.RouteTrafficEventInfo
import com.autonavi.gbl.guide.model.SAPAInquireResponseData
import com.autonavi.gbl.guide.model.ServiceAreaInfo
import com.autonavi.gbl.guide.model.SocolEventInfo
import com.autonavi.gbl.guide.model.TMCIncidentReport
import com.autonavi.gbl.guide.model.TrafficLightCountdown
import com.autonavi.gbl.guide.model.TrafficSignal
import com.autonavi.gbl.guide.model.guidecontrol.EmulatorParam
import com.autonavi.gbl.guide.model.guidecontrol.Param
import com.autonavi.gbl.guide.model.guidecontrol.Type
import com.autonavi.gbl.guide.observer.IContinueGuideInfoObserver
import com.autonavi.gbl.guide.observer.INaviObserver
import com.autonavi.gbl.layer.CustomPointLayerItem
import com.autonavi.gbl.layer.GuideLabelLayerItem
import com.autonavi.gbl.layer.model.BizAreaType
import com.autonavi.gbl.layer.model.BizCarType
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.layer.model.BizRouteType
import com.autonavi.gbl.layer.model.DynamicLevelType
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.OperatorPosture
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.PointLayerItem
import com.autonavi.gbl.map.layer.model.CarLoc
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.model.LayerIconAnchor
import com.autonavi.gbl.map.layer.model.LayerIconType
import com.autonavi.gbl.map.layer.model.LayerTexture
import com.autonavi.gbl.map.layer.model.PathMatchInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.model.GestureAction
import com.autonavi.gbl.map.model.MapLabelItem
import com.autonavi.gbl.map.model.MapLabelType.LABEL_Type_OPENLAYER
import com.autonavi.gbl.map.model.PreviewParam
import com.autonavi.gbl.map.observer.IMapGestureObserver
import com.autonavi.gbl.pos.model.LocDataType
import com.autonavi.gbl.pos.model.LocMatchInfo
import com.autonavi.gbl.pos.model.LocParallelRoadInfo
import com.autonavi.gbl.pos.model.LocPulse
import com.autonavi.gbl.pos.model.LocSwitchRoadType
import com.autonavi.gbl.pos.observer.IPosLocInfoObserver
import com.autonavi.gbl.pos.observer.IPosParallelRoadObserver
import com.autonavi.gbl.pos.observer.IPosSwitchParallelRoadObserver
import com.autonavi.gbl.route.model.BLRerouteRequestInfo
import com.autonavi.gbl.route.model.RouteControlKey
import com.autonavi.gbl.route.observer.INaviRerouteObserver
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autonavi.gbl.user.msgpush.model.SceneSendType
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.user.usertrack.model.GpsTrackPoint
import com.autonavi.gbl.user.usertrack.observer.IGpsInfoGetter
import com.autonavi.gbl.util.errorcode.Route
import com.autonavi.gbl.util.model.BinaryStream
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.common.utils.ElectricInfoConverter
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.DrivingLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.RouteEndAreaLayer
import com.autosdk.bussiness.layer.SearchLayer
import com.autosdk.bussiness.layer.UserBehaviorLayer
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.location.utils.CustomTimer
import com.autosdk.bussiness.location.utils.CustomTimerTask
import com.autosdk.bussiness.location.utils.VelocityPulse
import com.autosdk.bussiness.manager.SDKManager
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.MapController.EMapStyleStateType
import com.autosdk.bussiness.map.Observer.MapViewObserver
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.navi.route.callback.IRouteResultCallBack
import com.autosdk.bussiness.navi.route.model.IRouteResultData
import com.autosdk.bussiness.navi.route.model.RouteCarResultData
import com.autosdk.bussiness.search.request.SearchPoiBizType
import com.autosdk.bussiness.search.request.SearchQueryType
import com.autosdk.bussiness.search.request.SearchRequestInfo
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.bussiness.widget.navi.NaviComponent
import com.autosdk.bussiness.widget.navi.utils.NaviUiUtil
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.bussiness.widget.search.util.SearchMapUtil
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.AutoConstant
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.autosdk.common.SdkApplicationUtils
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.tts.IAutoPlayer
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.FileUtils
import com.autosdk.common.utils.ResUtil
import com.autosdk.common.utils.StringUtils
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.bean.MapLightBarItem
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.bean.NaviViaDataBean
import com.desaysv.psmap.base.bean.TBTLaneInfoBean
import com.desaysv.psmap.base.bean.TmcModelInfoBean
import com.desaysv.psmap.base.bean.TrafficEventInfoBean
import com.desaysv.psmap.base.common.EVManager
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.data.IRouteRepository
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.LastRouteUtils
import com.desaysv.psmap.base.utils.NavigationUtil
import com.desaysv.psmap.base.utils.RouteErrorCodeUtils
import com.desaysv.psmap.base.utils.Status
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs


@Singleton
open class NaviBusiness @Inject constructor(
    private val routeRepository: IRouteRepository,
    private val settingComponent: ISettingComponent,
    private val mLayerController: LayerController,
    private val mNaviController: NaviController,
    private val mLocationController: LocationController,
    private val mUserBusiness: UserBusiness,
    private val mRouteRequestController: RouteRequestController,
    private val gson: Gson,
    private val mNaviComponent: NaviComponent,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val mNetWorkManager: NetWorkManager,
    private val iCarInfoProxy: ICarInfoProxy,
    private val mMapController: MapController,
    private val mMapBusiness: MapBusiness,
    private val mMapDataBusiness: MapDataBusiness,
    private val application: Application,
    private val mSearchBusiness: SearchBusiness,
    private val mLastRouteUtils: LastRouteUtils,
    private val mTripShareBusiness: TripShareBusiness,
    private val mEngineerBusiness: EngineerBusiness,
    private val mEVManager: EVManager,
    private val mAutoTTSPlayer: IAutoPlayer
) {
    @Inject
    lateinit var extMapBusiness: ExtMapBusiness

    //导航引导图层
    private val mDrivingLayer: DrivingLayer? by lazy {
        mLayerController.getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    private val mEx1DrivingLayer: DrivingLayer? by lazy {
        extMapBusiness.ex1DrivingLayer
    }


    //自定义图层
    private val mCustomLayer: CustomLayer? by lazy {
        mLayerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //主图
    private val mMapView: MapView? by lazy {
        mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //用户收藏点图层
    private val mUserBehaviorLayer: UserBehaviorLayer? by lazy {
        mLayerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //搜索图层
    private val mSearchLayer: SearchLayer? by lazy {
        mLayerController.getSearchLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //终点图层
    private val mRouteEndAreaLayer: RouteEndAreaLayer? by lazy {
        mLayerController.getRouteEndAreaLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //协程IO线程
    private var naviScopeIo = CoroutineScope(Dispatchers.IO + Job())
    private var naviScopeMain = CoroutineScope(Dispatchers.Main + Job())

    //是否从首页进入
    var mIsHome = false

    //Toast内容
    val setToast = MutableLiveData<String>()

    //退出导航界面
    private val _finishFragment = MutableLiveData(false)
    val finishFragment: LiveData<Boolean> = _finishFragment

    private val naviSharePreference =
        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.navi)

    // 鹰眼图途经点是否可见
    private val mShowEagleViaPoint = true

    // 鹰眼图是否是方形背景,默认为圆形
    private val isEagleBgSquare = false
    private var mNaviType: Int = NaviType.AUTO_UNKNOWN_ERROR

    //是否有导航信息透出
    private var isUpdateNaviInfo = false

    //前台是否在导航界面
    var isInNaviFragment = true

    //公路类型
    var roadClass = RoadClass.RoadClassNULL

    // ============================ IPosLocInfoObserver ================================
    private var mLastSpeed = 0f
    private val mGpsTrackPoint = GpsTrackPoint()

    /**
     * 比例尺缓存
     */
    private var mZoomLevelCache = 0f

    private var rerouteRequestInfo: BLRerouteRequestInfo? = null

    //导航状态
    private val _naviStatus = MutableLiveData<Int>(BaseConstant.NAVI_STATE_INIT_NAVI_STOP)
    val naviStatus: LiveData<Int> = _naviStatus

    //路线是否发生变化
    private val _routeChangedNotice = MutableLiveData(false)
    val routeChangedNotice: LiveData<Boolean> = _routeChangedNotice

    //偏航通知
    private val _routeYawNotice = MutableLiveData(false)
    val routeYawNotice: LiveData<Boolean> = _routeYawNotice

    //正在算路中
    var isRequestRoute = false

    //是否透出收费站车道
    var isTollBoothsLane = false

    private var mRerouteOption: RouteOption? = null

    //回车位状态
    private val _backCCPVisible = MutableLiveData(false)
    val backCCPVisible: LiveData<Boolean> = _backCCPVisible

    //鹰眼图点击View状态
    private val _eagleViewVisible = MutableLiveData(false)
    val eagleViewVisible: LiveData<Boolean> = _eagleViewVisible

    //光柱图View显隐状态
    private val _tmcBarVisible = MutableLiveData(false)
    val tmcBarVisible: LiveData<Boolean> = _tmcBarVisible

    //简易光柱图View显隐状态
    private val _simpleTmcBarVisible = MutableLiveData(false)
    val simpleTmcBarVisible: LiveData<Boolean> = _simpleTmcBarVisible

    //光柱图数据
    private val _tmcModelInfo = MutableLiveData<TmcModelInfoBean>()
    val tmcModelInfo: LiveData<TmcModelInfoBean> = _tmcModelInfo

    //是否处于全览状态
    private val _InFullView = MutableLiveData(false)
    val inFullView: LiveData<Boolean> = _InFullView

    //导航过程中信息
    private val _naviInfo = MutableLiveData<NaviInfo?>()
    val naviInfo: LiveData<NaviInfo?> = _naviInfo

    //导航loading 路线发生变化
    private val _loadingRoute = MutableLiveData<Boolean>(false)
    val loadingRoute: LiveData<Boolean> = _loadingRoute

    //导航loading
    private val _loadingView = MutableLiveData<Boolean>(false)
    val loadingView: LiveData<Boolean> = _loadingView

    //出口信息编号
    private val _exitTip = MutableLiveData<String>()
    val exitTip: LiveData<String> = _exitTip

    //出口信息 总
    private val _exitDirectionInfoAll = MutableLiveData<ExitDirectionInfo?>()
    val exitDirectionInfoAll: LiveData<ExitDirectionInfo?> = _exitDirectionInfoAll

    //出口信息
    private val _exitDirectionInfo = MutableLiveData<String>()
    val exitDirectionInfo: LiveData<String> = _exitDirectionInfo

    //出口信息显示隐藏状态
    private val _exitDirectionInfoVisible = MutableLiveData(false)
    val exitDirectionInfoVisible: LiveData<Boolean> = _exitDirectionInfoVisible

    //车道线显示隐藏状态
    private val _naviLaneVisible = MutableLiveData(false)
    val naviLaneVisible: LiveData<Boolean> = _naviLaneVisible

    //车道线数据
    private val _naviLaneList = MutableLiveData<ArrayList<TBTLaneInfoBean>>()
    val naviLaneList: LiveData<ArrayList<TBTLaneInfoBean>> = _naviLaneList

    //收费站车道线数据  对外透出
    private val _tollLaneList = MutableLiveData<ArrayList<Int>>()
    val tollLaneList: LiveData<ArrayList<Int>> = _tollLaneList

    //车道线数据 原始数据
    private val _naviLane = MutableLiveData<LaneInfo?>()
    val naviLane: LiveData<LaneInfo?> = _naviLane

    //删除途经点信息
    private val _showViaNaviViaDataDialog = MutableLiveData<NaviViaDataBean>()
    val showViaNaviViaDataDialog: LiveData<NaviViaDataBean> = _showViaNaviViaDataDialog

    //电量不足提示卡片
    private val _showBatteryLowCard = MutableLiveData(false)
    val showBatteryLowCard: LiveData<Boolean> = _showBatteryLowCard

    //主辅路显示隐藏 平行路
    private val _parallelRoadVisible = MutableLiveData(false)
    val parallelRoadVisible: LiveData<Boolean> = _parallelRoadVisible

    //主辅路状态 平行路
    private val _parallelRoadState = MutableLiveData(0)
    val parallelRoadState: LiveData<Int> = _parallelRoadState

    //桥上桥下显示隐藏 桥上桥下
    private val _parallelBridgeVisible = MutableLiveData(false)
    val parallelBridgeVisible: LiveData<Boolean> = _parallelBridgeVisible

    //桥上桥下状态 桥上桥下
    private val _parallelBridgeState = MutableLiveData(0)
    val parallelBridgeState: LiveData<Int> = _parallelBridgeState

    //当前是否有平行路，平行路状态 对外通知
    private val _parallelRoadStatus = MutableLiveData(0)
    val parallelRoadStatus: LiveData<Int> = _parallelRoadStatus

    //道路限速
    private val _mCurrentRoadSpeed = MutableLiveData(0)
    val mCurrentRoadSpeed: LiveData<Int> = _mCurrentRoadSpeed

    //显示隐藏道路限速View
    private val _roadSpeedVisible = MutableLiveData(false)
    val roadSpeedVisible: LiveData<Boolean> = _roadSpeedVisible

    //当前道路名称
    private val _mRoadName = MutableLiveData(application.getString(R.string.sv_navi_no_name_road))
    val mRoadName: LiveData<String> = _mRoadName

    //区间限速显示隐藏 view
    private val _limitSpeedVisible = MutableLiveData(false)
    val limitSpeedVisible: LiveData<Boolean> = _limitSpeedVisible

    //区间限速最大可行驶速度
    private val _limitSpeed = MutableLiveData<String>()
    val limitSpeed: LiveData<String> = _limitSpeed

    //区间限速当前平均速度
    private val _averageSpeed = MutableLiveData<String>()
    val averageSpeed: LiveData<String> = _averageSpeed

    //区间限速剩余距离
    private val _remainDist = MutableLiveData<String>()
    val remainDist: LiveData<String> = _remainDist

    //道路限行，道路措施
    private val _forbiddenInfo = MutableLiveData<TrafficEventInfoBean>()
    val forbiddenInfo: LiveData<TrafficEventInfoBean> = _forbiddenInfo

    //红绿灯绿波车速数据
    private val _naviGreenWaveCarSpeed = MutableLiveData<NaviGreenWaveCarSpeed>()
    val naviGreenWaveCarSpeed: LiveData<NaviGreenWaveCarSpeed> = _naviGreenWaveCarSpeed

    //红绿灯绿波车速数据显示隐藏 view
    private val _naviGreenWaveCarSpeedVisible = MutableLiveData(false)
    val naviGreenWaveCarSpeedVisible: LiveData<Boolean> = _naviGreenWaveCarSpeedVisible

    //速度
    private val _naviSpeed = MutableLiveData("0")
    val naviSpeed: MutableLiveData<String> = _naviSpeed

    //获取高速服务区信息显示隐藏 view
    private val _showGetSAPAInfoDialogVisible = MutableLiveData(false)
    val showGetSAPAInfoDialogVisible: LiveData<Boolean> = _showGetSAPAInfoDialogVisible

    //获取高速服务区信息
    private val _serviceAreaInfo = MutableLiveData<ServiceAreaInfo?>()
    val serviceAreaInfo: LiveData<ServiceAreaInfo?> = _serviceAreaInfo

    //获取高速服务区信息 显示隐藏 view
    private val _serviceAreaInfoVisible = MutableLiveData(false)
    val serviceAreaInfoVisible: LiveData<Boolean> = _serviceAreaInfoVisible

    //摄像头数据
    private val _naviCameraList = MutableLiveData<ArrayList<NaviCameraExt>?>()
    val naviCameraList: LiveData<ArrayList<NaviCameraExt>?> = _naviCameraList

    //进行终点周边搜  停车场推荐
    private val _parkingRecommend = MutableLiveData<List<POI>>()
    val parkingRecommend: LiveData<List<POI>> = _parkingRecommend

    //进行终点周边搜  停车场推荐 显隐
    private val _parkingRecommendVisible = MutableLiveData(false)
    val parkingRecommendVisible: LiveData<Boolean> = _parkingRecommendVisible


    //是否显示停车场卡片详情
    private val _showParkingRecommendDetail = MutableLiveData(false)
    val showParkingRecommendDetail: LiveData<Boolean> = _showParkingRecommendDetail

    // 图层选择停车场
    private val _parkingPosition = MutableLiveData(0)
    val parkingPosition: LiveData<Int> = _parkingPosition

    //道路设施
    private val _naviRoadFacilityList = MutableLiveData<ArrayList<NaviRoadFacility>?>()
    val naviRoadFacilityList: LiveData<ArrayList<NaviRoadFacility>?> = _naviRoadFacilityList

    //驾车导航过程 驾车事件
    private val _driverEventList = MutableLiveData<ArrayList<DriveEvent>?>()
    val driverEventList: LiveData<ArrayList<DriveEvent>?> = _driverEventList

    //驾车导航过程 统计信息
    private val _naviStatisticsInfo = MutableLiveData<NaviStatisticsInfo?>()
    val naviStatisticsInfo: LiveData<NaviStatisticsInfo?> = _naviStatisticsInfo

    //当前车辆道路信息
    private val _locMatchInfo = MutableLiveData<LocMatchInfo?>()
    val locMatchInfo: LiveData<LocMatchInfo?> = _locMatchInfo

    //算路错误信息
    private val _naviErrorMessage = MutableLiveData<String>()
    val naviErrorMessage: LiveData<String> = _naviErrorMessage

    //红绿灯信息
    private val _trafficLightCountdownInfo = MutableLiveData<List<TrafficLightCountdown>?>()
    val trafficLightCountdownInfo: LiveData<List<TrafficLightCountdown>?> = _trafficLightCountdownInfo

    private var activeShutdownSAPATip = false //主动关闭加载高速服务区提示

    private var mParallelRoadState = 0 //主辅路状态
    private var mParallelBridgeState = 0 //高架状态
    private var mLocParallelRoadInfo: LocParallelRoadInfo? = null //平行路、高架切换信息

    @Volatile
    private var mRoadId: BigInteger? = null //切换主辅路、高架的roadid
    private var mSwitchRoadType = 0;

    protected var mCurNaviInfo: NaviInfo? = null

    // 总路程（单位为m），重新规划路径后会更新
    var mTotalDistance: Long = 0

    // 路线ID
    var pathID: Long = 0
    private var isLessThran500Delete = false
    protected var mManeuverInfo = ManeuverInfo() //转向图标信息
    private var mSwitchParallelRoadEnable = true //是否主辅路切换

    //规划路线请求ID
    private var requestRouteTaskId: Long = 0

    //是否为添加途经点操作
    private var isShowAddPointToast = false

    private var lastMidpoi: POI? = null
    private var lastMidpoiList: ArrayList<POI> = arrayListOf()

    //是否为删除途经点操作
    private var isShowDeletePointToast = false

    //是否第一次进入导航模式
    private var isFirstGoToNavi = false

    private var commandRequestType = 0
    private var traceId: String? = null
    private var curSpeed = 0f

    // ================== 路口大图相关 ========================
    var mRoadCrossType = 0 //路口大图类型
    protected var isShowCrossImage = false //是否正在显示路口大图
    protected var isNeedShowCrossImage = true //是否需要显示路口大图
    private var linkCarState = 0
    private var mIsAlreadySearchParking = false//是否已经搜索了终点停车场
    private val CAR_PARKING_SEARCH_DISTANCE = 500
    private var isCloseNaviFragment = true//停止导航是否关闭导航页面
    private var isNaviSettingRestart: Boolean = false//是否导航设置点击的刷新路线

    var trackFileName = ""  //轨迹文件名

    //左右数值从左往右计算的  上下是从上往下计算的
    private val rectInt = RectInt().apply {
        left = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_680)
        right = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_1800)
        top = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_100)
        bottom = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_1000)
    }//由产品定义 终点字体扎点边距变化

    /**
     * 是否为真实导航
     */
    fun isRealNavi(): Boolean {
        val isNavi = mNaviController.isNaving && mNaviType == NaviType.NaviTypeGPS
        Timber.i("isRealNavi is called, isNavi: $isNavi, mNaviType: $mNaviType")
        return isNavi
    }

    /**
     * 是否在导航
     */
    fun isInitNavi(): Boolean {
        val isNavi = mNaviController.isNaving || mNaviType == NaviType.NaviTypeGPS || isInNaviFragment
        Timber.i("isInitNavi is called, isNavi: $isNavi, mNaviType: $mNaviType， isInNaviFragment: $isInNaviFragment")
        return isNavi
    }

    /**
     * 是否为模拟导航
     */
    fun isSimulationNavi(): Boolean {
        return mNaviType == NaviType.NaviTypeSimulation
    }

    /**
     * 判断是否正在导航中
     *
     * @return true false
     */
    fun isNavigating(): Boolean {
        return isRealNavi() || isSimulationNavi()
    }

    /**
     * 判断是否在高速上
     *
     * @return true false
     */
    fun isHighWay(): Boolean {
        return roadClass == RoadClass.RoadClassFreeway
    }

    fun getUpdateNaviInfoState(): Boolean {
        return isUpdateNaviInfo
    }

    fun getCurSpeed(): Float {
        return curSpeed
    }

    /**
     * 获取导航总长度
     */
    fun getTotalDistance(): Long {
        return mTotalDistance
    }

    /**
     * 获取导航路线ID
     */
    fun getPathIdValue(): Long {
        return pathID
    }

    /**
     * 设置是否从主页跳转-继续导航
     */
    fun setIsHome(mIsHome: Boolean) {
        this.mIsHome = mIsHome
    }

    /**
     * 设置导航模式
     */
    fun setNaviType(mNaviType: Int) {
        Timber.i("setNaviType is called")
        this.mNaviType = mNaviType
    }

    /**
     * 初始化
     */
    fun initTBT() {
        val cache = AutoConstant.PATH + "tbtCommonComponent/cache"
        val navi = AutoConstant.PATH + "data/navi/compile_v2/chn/"
        val res = AutoConstant.RES_DIR
        FileUtils.createDir(cache)
        FileUtils.createDir(navi)
        FileUtils.createDir(res)
        val workPath = WorkPath()
        workPath.cache = cache
        workPath.navi = navi
        //750开始可不赋值，表示使用SDK内置目录。如果HMI自行拷贝管理了blRes/res，则需要赋值成自己管理的res路径
        //workPath.res = res
        val userConfig = UserConfig()
        userConfig.deviceID = iCarInfoProxy.sNCode
        userConfig.userBatch = "0"
        val tbtCommonControl = TbtCommonControl.getInstance()
        if (tbtCommonControl != null) {
            // 需要设置, 否则tbt离线算路无法播报
            //50无需设置
            //tbtCommonControl.setTBTResReader(TBTResReaderImpl())
            tbtCommonControl.init(workPath, userConfig)
        }
        /*todo 多屏多SDK实现
        if (MultiDisplayManager.isMultiSDK()) {
            val externalService = ComponentManager.getInstance().getComponentService(ComponentConstant.EXTERNAL_SCREEN) as IComponentExScreenService
            mNaviController.isMultiConsistenceAvailable = externalService.isMultiConsistenceAvailable
        }*/
        mNaviController.init()
        configRouteControl()
    }

    /**
     * 配置算路参数   TODO 待优化
     */
    private fun configRouteControl() {
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
        //设置是否支持开启多备选路线 0，不开启多备选路线；1，开启
        mNaviController.routeControl(RouteControlKey.RouteControlKeySetMutilRoute, "1")
        //途径路聚合信息，需要(RouteControlKeySetLongDistInfo，"1") 开启长途信息透出才会透出
        mNaviController.routeControl(RouteControlKey.RouteControlKeySetLongDistInfo, "1")
        mNaviController.routeControl(
            RouteControlKey.RouteControlKeySetDiuInfo,
            CommonUtil.getDeviceID(SdkApplicationUtils.getApplication())
        )
    }

    //===============================================================下面是回调接口=============================================================================

    // ============================ IMapGestureObserver start ================================
    private val mapGestureObserver = object : IMapGestureObserver {
        override fun onMotionEvent(l: Long, i: Int, l1: Long, l2: Long) {
            if (i == GestureAction.ACTION_POINTER_DOWN) { //双指按下触发触碰态
                resetBackToCarTimer()
                if (serviceAreaInfoVisible.value == true) {
                    setServiceAreaInfoVisible(false)
                }
            }
        }

        override fun onMoveBegin(l: Long, l1: Long, l2: Long) {
            resetBackToCarTimer()
            if (serviceAreaInfoVisible.value == true) {
                setServiceAreaInfoVisible(false)
            }
        }

        override fun onMoveEnd(l: Long, l1: Long, l2: Long) {}

        override fun onMove(l: Long, l1: Long, l2: Long) {}

        override fun onMoveLocked(l: Long) {}

        override fun onPinchLocked(l: Long) {}

        override fun onLongPress(l: Long, l1: Long, l2: Long) {}

        override fun onDoublePress(l: Long, l1: Long, l2: Long): Boolean {
            resetBackToCarTimer()
            if (serviceAreaInfoVisible.value == true) {
                setServiceAreaInfoVisible(false)
            }
            return false
        }

        override fun onSinglePress(l: Long, l1: Long, l2: Long, b: Boolean): Boolean {
            resetBackToCarTimer()
            if (serviceAreaInfoVisible.value == true) {
                setServiceAreaInfoVisible(false)
            }
            return false
        }
    }
    // ============================ IMapGestureObserver end ================================

    /**
     * 比例尺监听，动态改变车标
     */
    private val mOnMapLevelChangedObserver = object : MapViewObserver() {
        override fun onMapLevelChanged(engineId: Long, bZoomIn: Boolean) {

            val zoomLevel = mMapBusiness.getZoomLevel()
            if (mZoomLevelCache != zoomLevel) {
                mDrivingLayer?.updateCarStyle(BizCarType.BizCarTypeGuide)
                if (BaseConstant.MULTI_MAP_VIEW)
                    mEx1DrivingLayer?.updateCarStyle(BizCarType.BizCarTypeGuide)
            }
            mZoomLevelCache = zoomLevel
        }

        override fun onClickLabel(l: Long, mapLabelItems: java.util.ArrayList<MapLabelItem>?) {
            super.onClickLabel(l, mapLabelItems)
            Timber.i("mOnMapLevelChangedObserver.onClickLabel() $l, ${mapLabelItems?.size}")

            mapLabelItems?.get(0)?.let {
                Timber.i("onClickLabel $it")
                val mapToLonLat = OperatorPosture.mapToLonLat(it.pixel20X.toDouble(), it.pixel20Y.toDouble())
                val poi = POIFactory.createPOI(it.name, GeoPoint(mapToLonLat.lon, mapToLonLat.lat), it.poiid)
                if (LABEL_Type_OPENLAYER == it.type) {
                    when ((it.sublayerId)) {
                        9000005 -> {
                            //交通事件点击
                            mMapBusiness.searchPoiCardInfo(MapPointCardData.PoiCardType.TYPE_TRAFFIC, poi)
                        }
                    }
                }
            }
        }
    }


    /**
     * DR轨迹回调
     */
    private val posLocInfoObserver = IPosLocInfoObserver { locInfoData ->
        //onLocInfoUpdate
//            Timber.i("onLocInfoUpdate")
        locInfoData?.let { locInfo ->
            //gps打点
            locInfo.matchInfo?.get(0)?.let { locMatchInfo ->
                mGpsTrackPoint.f32Course = locMatchInfo.course
                mGpsTrackPoint.f64Latitude = locMatchInfo.stPos.lat
                mGpsTrackPoint.f64Longitude = locMatchInfo.stPos.lon
                _locMatchInfo.postValue(locMatchInfo)
            }
            curSpeed = abs(locInfo.speed)
            var speed = abs(locInfo.speed)
            if (locInfo.speedometerIsValid) {
                //如果速度表有效，使用速度表的速度
                speed = locInfo.speedometer
            }
            _naviSpeed.postValue(speed.toInt().toString())
            mGpsTrackPoint.f32Accuracy = locInfo.posAcc
            mGpsTrackPoint.f32Speed = curSpeed
            mGpsTrackPoint.f64Altitude = locInfo.alt.toDouble()
            mGpsTrackPoint.n64TickTime = locInfo.gpsDatetime?.let { CommonUtil.parseGpsDateTime(it) } ?: 0
            if (abs(mLastSpeed - curSpeed) >= 1) {
                mLastSpeed = curSpeed
            }
        }
    }

    /**
     * 请求路线回调接口
     */
    private val routeResultCallBack = object : IRouteResultCallBack {

        /**
         * 路线请求成功回调
         */
        override fun callback(routeResult: IRouteResultData?, isLocal: Boolean) {
            Timber.i("routeResultCallBack callback is called isLocal = $isLocal")
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CALCUATE_ROUTE_FINISH_SUCC)
            isRequestRoute = false
            setParkingRecommendVisible(false)
            if (isNaviSettingRestart) {
                isNaviSettingRestart = false
                setToastTip(application.getString(R.string.sv_route_has_been_refreshed_successfully))
            }
            if (isShowAddPointToast) {
                isShowAddPointToast = false
                setToastTip(application.getString(R.string.sv_route_via_poi_add))
            }
            if (isShowDeletePointToast) {
                isShowDeletePointToast = false
                setToastTip(application.getString(R.string.sv_route_via_poi_deleted))
            }
            _routeChangedNotice.postValue(true)
            _loadingView.postValue(false)
            setRouteAndNavi(commandRequestType)
        }

        /**
         * 路线请求错误回调
         */
        override fun errorCallback(errorCode: Int, errorMessage: String?, isLocal: Boolean) {
            Timber.i("routeResultCallBack errorCallback is called errorCode = $errorCode , isLocal = $isLocal")
            AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_FAIL)
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CALCUATE_ROUTE_FINISH_FAIL)
            isRequestRoute = false
            isNaviSettingRestart = false
            //路线规划失败或取消时恢复途经点数据
            recoverViaPois()
//            setParkingRecommendVisible(false)
            //不在导航中规划路线失败，退出导航
//            if (!isRealNavi()) {
//                finishNavi()
//            }
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.NAVI_REROUTE_FAIL)
            if (linkCarState == AutoState.LINK_CAR_REROUTE_SUCCESS) {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.LINK_CAR_REROUTE_ERROR)
                linkCarState = 0
            } else if (linkCarState == AutoState.LINK_CAR_REROUTE_PREF_SUCCESS) {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.LINK_CAR_REROUTE_PREF_ERROR)
                linkCarState = 0
            } else if (linkCarState == AutoState.LINK_CAR_ADD_MID_SUCCESS) {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.LINK_CAR_ADD_MID_ERROR)
                linkCarState = 0
            } else if (linkCarState == AutoState.LINK_CAR_DELETE_MID_SUCCESS) {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.LINK_CAR_DELETE_MID_ERROR)
                linkCarState = 0
            }
            _naviErrorMessage.postValue(RouteErrorCodeUtils.activeErrorMessage(errorCode))
            _loadingView.postValue(false)
        }
    }

    /**
     * 重算，切换平行路，刷新路线回调处理
     */
    private val refreshRouteResultCallBack = object : IRouteResultCallBack {

        /**
         * 路线请求成功回调
         */
        override fun callback(routeResult: IRouteResultData?, isLocal: Boolean) {
            Timber.i("refreshRouteResultCallBack callback is called isLocal = $isLocal")
            mSwitchParallelRoadEnable = true
            isRequestRoute = false
            setParkingRecommendVisible(false)
            mDrivingLayer?.clearAllPaths()
            if (BaseConstant.MULTI_MAP_VIEW) mEx1DrivingLayer?.clearAllPaths()
            mCustomLayer?.removeAllLayerItems()
            //路线信息更新
            mRouteRequestController.carRouteResult.focusIndex = 0
            //绘制路线
            mNaviComponent.setRoute(
                mRouteRequestController.carRouteResult,
                mRouteRequestController.carRouteResult.pathResult,
                mIsHome,
                mNaviType,
                mNetWorkManager.isNetworkConnected(),
                0
            )
            mDrivingLayer?.showEaglePath()
            startSearchEndArea()
            setEagleVisible(!backCCPVisible.value!!)
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.NAVI_REROUTE_SUCCESS)
            AutoStatusAdapter.sendStatus(AutoStatus.NAVI_UPDATE_PATH_SUCCESS, statusDetails = mRerouteOption?.routeType ?: -1)
            _routeChangedNotice.postValue(true)
        }

        /**
         * 路线请求错误回调
         */
        override fun errorCallback(errorCode: Int, errorMessage: String?, isLocal: Boolean) {
            Timber.i("refreshRouteResultCallBack errorCallback is called errorCode = $errorCode , isLocal = $isLocal ， routeType =  ${mRerouteOption?.getRouteType()}")
            mSwitchParallelRoadEnable = true
            isRequestRoute = false
//            setParkingRecommendVisible(false)
            if (mRerouteOption?.routeType == RouteType.RouteTypeParallelRoad) {
                setToastTip("切换失败")
            }
            if (mRerouteOption?.routeType == RouteType.RouteTypeYaw) {
                if (errorCode == Route.ErrorCodeLackStartCityData || errorCode == Route.ErrorCodeLackWayCityData || errorCode == Route.ErrorCodeLackEndCityData || errorCode == Route.ErrorCodeLackViaCityData) {
                    setToastTip("算路失败，缺少离线地图数据")
                }
            }

            AutoStatusAdapter.sendStatus(AutoStatus.NAVI_UPDATE_PATH_FAIL)

        }
    }

    /** ============================================= 导航过程中相关信息回调 INaviObserver start =================================================== **/

    /**
     * 导航信息回调接口
     */
    private val naviObserver = object : INaviObserver {


        /**
         * 更新经过充电站索引
         *
         *
         * //@param[in] viaIndex 充电站索引
         * //@remark 索引从0开始
         */
        override fun onUpdateChargeStationPass(viaIndex: Long) {}

        /**
         * 传出当前导航信息
         *
         *
         * //@details 导航过程中传出当前导航信息
         * //@param[in] naviInfoList     当前导航信息数组
         * //@remark 在设置多条路线的情况下，会给出多条路线的信息
         * //@note thread mutil
         * //@note BL有内聚, HMI也可以使用这些信息做二次开发
         */
        override fun onUpdateNaviInfo(naviInfoList: ArrayList<NaviInfo>?) {
            Timber.i("onUpdateNaviInfo is called")
            mCurNaviInfo = null
            if (naviInfoList.isNullOrEmpty()) {
                return
            }
            isUpdateNaviInfo = true
            val naviInfoListSize = naviInfoList.size
            val carRouteResult = mRouteRequestController.carRouteResult
            carRouteResult?.let {
                if (it.pathResult.isNullOrEmpty()) {
                    Timber.i("onUpdateNaviInfo it.pathResult isNullOrEmpty is called")
                    return
                }
                Timber.i("onUpdateNaviInfo: naviInfoListSize = $naviInfoListSize;  getPathCount = ${it.pathResult.size}; focusIndex = ${it.focusIndex}")
                mCurNaviInfo = naviInfoList[0]
                // 获取路线总长度
                val newPathID = mNaviController.getPathId(it.pathResult[it.focusIndex])
                if (pathID != newPathID || mTotalDistance.toInt() == 0) {
                    pathID = newPathID
                    mTotalDistance = mNaviController.getTotalDistance(it.focusIndex)
                }

                _loadingRoute.postValue(false)
                //如果距离少于500的时候删除路线
                onDeletePathLessThan(mCurNaviInfo!!.routeRemain.dist)
                if (mCurNaviInfo!!.curSegIdx.toLong() != mManeuverInfo.segmentIndex) {
                    onUpdateExitDirectionInfo(null)
                    Timber.i("出口信息: 进入下一段路出口信息置为null，不显示出口信息")
                }
                if (null != mCurNaviInfo) {
                    roadClass = mCurNaviInfo!!.curRoadClass
                }
                if (mCurNaviInfo!!.innerRoad) {
                    Timber.i("updateNaviInfo2Out innerRoad：true") //是否偏航抑制
                }
                updateNaviInfo(mCurNaviInfo)
                _naviInfo.postValue(mCurNaviInfo)
            }
        }

        /**
         * 光柱信息数据更新
         *
         * @param lightBarInfo 光柱结构
         * @param passedIdx    为lightBar的索引，表示行驶过路段索引
         * @param dataStatus   TMC数据状态，是否有更新TMC路况数据
         */
        override fun onUpdateTMCLightBar(
            lightBarInfo: ArrayList<LightBarInfo>?,
            lightBarDetail: LightBarDetail?,
            passedIdx: Long,
            dataStatus: Boolean
        ) {
            Timber.i("onUpdateTMCLightBar called: passedIdx=$passedIdx, dataStatus=$dataStatus, lightBarInfo=$lightBarInfo")

            val carRouteResult = mRouteRequestController.carRouteResult
            if (carRouteResult == null) {
                Timber.i("onUpdateTMCLightBar: carRouteResult is null")
                return
            }

            val pathResult = carRouteResult.pathResult
            if (pathResult.isNullOrEmpty()) {
                Timber.i("onUpdateTMCLightBar: pathResult is empty or null")
                return
            }

            val focusIndex = carRouteResult.focusIndex
            if (focusIndex !in pathResult.indices) {
                Timber.i("onUpdateTMCLightBar: focusIndex $focusIndex out of bounds")
                return
            }

            val variantPathWrap = pathResult[focusIndex] ?: run {
                Timber.i("onUpdateTMCLightBar: variantPathWrap is null")
                return
            }

            // 高德通知状态 需要更新路线
            naviScopeIo.launch {
                if (dataStatus) {
                    Timber.i("onUpdateTMCLightBar: updating paths")
                    mDrivingLayer?.apply {
                        updatePaths()
                        updateEaglePaths()
                    }
                    if (BaseConstant.MULTI_MAP_VIEW) mEx1DrivingLayer?.updatePaths()
                }

                val curPathId = mNaviController.getPathId(variantPathWrap)

                // 路线总长度更新逻辑
                if (pathID != curPathId || mTotalDistance.toInt() == 0) {
                    pathID = curPathId
                    mTotalDistance = mNaviController.getTotalDistance(focusIndex)
                }

                // 光柱图数据计算
                lightBarInfo?.let { nonNullLightBar ->
                    val tmcItems = mNaviController.getTmcItemsInfo(
                        curPathId,
                        passedIdx.toInt(),
                        nonNullLightBar
                    )
                    tmcItems?.let { validTmcItems ->
                        val tmcModelInfo = sendTmsLauncher(validTmcItems, mTotalDistance)
                        _tmcModelInfo.postValue(tmcModelInfo)
                    }
                }
            }
        }

        /**
         * 切换路线
         * @param pathID 当前主导航路线id
         * @param result 是否成功原因（1:成功，2:失败,PathID无效,3:失败,因为id和当前主选路线一致）
         *               通知用户切换主导航路线状态，客户端主动SelectMainPathID切换的回调状态
         */
        override fun onSelectMainPathStatus(pathID: Long, result: Int) {
            Timber.i("onSelectMainPathStatus is called : $result  pathID : $pathID")
            mNaviComponent.selectMainPathStatus(result, mRouteRequestController.carRouteResult.focusIndex)
            setToastTip("路线切换成功")
        }

        /**
         * 传出出口编号和出口方向信息
         *
         *
         * //@details 导航过程中传出出口编号和出口方向信息
         * //@param[in] boardInfo        当前导航信息数组
         * //@remark 自车在高速和城市快速路并满足一定距离的情况下回调
         * //@note thread mutil
         */
        override fun onUpdateExitDirectionInfo(exitDirectionInfo: ExitDirectionInfo?) {
            if (exitDirectionInfo?.directionInfo == null || exitDirectionInfo.directionInfo.size == 0) {
                Timber.i("出口信息: : guideBoardInfo = null || guideBoardInfo.nDirectionNum = 0")
                _exitDirectionInfoVisible.postValue(false)
//                sendExitDirectionInfo(null)
                _exitDirectionInfoAll.postValue(null)
            } else {
//                sendExitDirectionInfo(exitDirectionInfo)
                updateExitDirectionInfo(exitDirectionInfo)
                _exitDirectionInfoVisible.postValue(true)
            }
        }

        /**
         * 显示路口大图
         *
         *
         * //@details 导航过程中传出路口大图数据
         * //@remark 1、根据CrossImageType区分回到的放大路口类型
         * //@remark 2、同一路口放大路口的优先级：CrossImageType3D > CrossImageTypeGrid > CrossImageTypeVector
         * //@remark 3、类型为CrossImageType3D的三维放大路口图需要有离线三维数据
         * //@param[in] info             路口大图信息
         * //@note thread mutil
         */
        override fun onShowCrossImage(info: CrossImageInfo?) {
            Timber.i("onShowCrossImage is called ")
            if (info == null || isShowCrossImage || serviceAreaInfoVisible.value == true || !isInNaviFragment || showParkingRecommendDetail.value == true) {
                Timber.i("onShowCrossImage return isShowCrossImage=$isShowCrossImage serviceAreaInfoVisible.value = ${serviceAreaInfoVisible.value} isInNaviFragment=$isInNaviFragment  showParkingRecommendDetail.value = ${showParkingRecommendDetail.value}")
                return
            }
            if (info.type == 4) {
                Timber.i("onShowCrossImage 3D return")
                return
            }
            //建议HMI使用SDK内聚策略，内聚策略默认不开启。
            //内聚策略开启方式：HMI收到OnShowCrossImage之后，调用setCrossImageInfo，将接收到的路口大图信息传给AutoSDK，则开启SDK内聚策略。
            mDrivingLayer?.setCrossImageInfo(info)
            mEx1DrivingLayer?.setCrossImageInfo(info)

            isShowCrossImage = true
            mRoadCrossType = info.type
            var isShowBitMapSuccess = false
            Timber.i("onShowCrossImage mRoadCrossType = $mRoadCrossType")
            if (info.type == 3 || info.type == 4) {   //矢量图或者三维图
                isShowBitMapSuccess = mDrivingLayer?.updateCross(info.dataBuf, info.type) == true
                mEx1DrivingLayer?.updateCross(info.dataBuf, info.type)
            } else if (info.type == 1) {  //栅格图
                val arrowImge = LayerTexture()
                val roadImage = LayerTexture()
                arrowImge.dataBuff = BinaryStream(info.arrowDataBuf)
                arrowImge.iconType = LayerIconType.LayerIconTypePNG //栅格图箭头png
                arrowImge.resID = info.crossImageID.toInt()
                arrowImge.isGenMipmaps = false
                arrowImge.isPreMulAlpha = true
                arrowImge.isRepeat = false
                arrowImge.anchorType = LayerIconAnchor.LayerIconAnchorLeftTop
                roadImage.dataBuff = BinaryStream(info.dataBuf)
                roadImage.iconType = LayerIconType.LayerIconTypeJPG //栅格图背景图jpg
                roadImage.resID = info.crossImageID.toInt()
                roadImage.isGenMipmaps = false
                roadImage.isPreMulAlpha = true
                roadImage.isRepeat = false
                roadImage.anchorType = LayerIconAnchor.LayerIconAnchorLeftTop
                isShowBitMapSuccess = mDrivingLayer?.setRasterImageData(arrowImge, roadImage) == true
                mEx1DrivingLayer?.setRasterImageData(arrowImge, roadImage)
            }
            var type = 0
            var bytes: ByteArray? = null
            var bytes1: ByteArray? = null
            if (info != null) {
                type = info.type
                bytes = info.dataBuf
                bytes1 = info.arrowDataBuf
            }
            //显示路口大图卡片
            if (isShowBitMapSuccess) {
                showCrossView(type)
            }

        }

        /**
         * 隐藏路口大图
         *
         *
         * //@details 导航过程中通知隐藏路口大图
         * //@param[in] type             路口大图类型
         * //@remark onShowCrossImage对应的消失通知
         * //@note thread mutil
         */
        override fun onHideCrossImage(type: Int) {
            Timber.i("onHideCrossImage is called ")
            hideCrossView(type)
        }

        /**
         * 输出三维路口放大图TMC数据
         *
         *
         * //@details 导航过程中输出三维路口放大图TMC数据
         * //@param[in] dataBuf          TMC数据流
         * ///@remark 输出类型为CrossImageType3D的三维放大路口图对应的道路路况状态
         * //@note thread mutil
         */
        override fun onShowNaviCrossTMC(binaryStream: BinaryStream?) {
            Timber.i("onShowNaviCrossTMC is called ")
        }


        /**
         * 三维路口大图通过最后一个导航段
         *
         *
         * //@details 导航过程中通知三维路口大图通过最后一个导航段
         * //@remark 精品三维场景中，进入后台不显示精品三维，但会缓存数据，恢复前台后会显示精品三维
         * //@remark 如果有onPassLast3DSegment这个通知收到，客户端就不会恢复精品三维，防止闪现
         * //@note thread mutil
         */
        override fun onPassLast3DSegment() {
            Timber.i("onPassLast3DSegment is called")
        }

        /**
         * 显示车道信息
         *
         *
         * //@details 导航过程中传出车道信息
         * //@param[in] info             行车引导线信息
         * //@remark 导航下，通知自车前方一定距离的行车引导线信息
         * //@note thread mutil
         */
        override fun onShowNaviLaneInfo(laneInfo: LaneInfo?) {
            Timber.i(
                "onShowNaviLaneInfo is called: backLane=${gson.toJson(laneInfo?.backLane)} ; frontLane:${
                    gson.toJson(
                        laneInfo?.frontLane
                    )
                }"
            )
            laneInfo?.let {
                Timber.i("onShowNaviLaneInfo isTollBoothsLane = $isTollBoothsLane")
                if (!isTollBoothsLane) {
                    showLaneInfo(laneInfo)
                }
            }
            //告诉其他模块 车道线数据
            _naviLane.postValue(laneInfo)
        }

        /**
         * 隐藏车道信息
         *
         *
         * ///@details 导航过程中通知隐藏车道信息
         * ///@remark 与onShowNaviLaneInfo配对回调
         * //@note thread mutil
         */
        override fun onHideNaviLaneInfo() {
            Timber.i("onHideNaviLaneInfo is called isTollBoothsLane = $isTollBoothsLane")
            if (!isTollBoothsLane) {
                _naviLaneVisible.postValue(false)
                _naviLaneList.postValue(ArrayList())
            }
            //告诉其他模块 车道线数据
            _naviLane.postValue(null)
        }


        /**
         * 导航过程中传出路径上转向图标
         *
         * @param info 转向图标信息
         */
        override fun onShowNaviManeuver(info: ManeuverInfo?) {
            Timber.i("onShowNaviManeuver is called")
            info?.let {
                mManeuverInfo.segmentIndex = info.segmentIndex
                mManeuverInfo.maneuverID = info.maneuverID
                mManeuverInfo.pathID = info.pathID
                //更新箭头
                val segmentCounts = ArrayList<Long>()
                segmentCounts.add(info.segmentIndex)
                mDrivingLayer?.setPathArrowSegment(segmentCounts) //设置转向箭头要显示导航段
                mDrivingLayer?.updatePathArrow() //更新路线上的箭头
                if (BaseConstant.MULTI_MAP_VIEW) {
                    mEx1DrivingLayer?.setPathArrowSegment(segmentCounts) //设置转向箭头要显示导航段
                    mEx1DrivingLayer?.updatePathArrow() //更新路线上的箭头
                }
                if (BaseConstant.MULTI_MAP_VIEW) {
                    mEx1DrivingLayer?.setPathArrowSegment(segmentCounts) //设置转向箭头要显示导航段
                    mEx1DrivingLayer?.updatePathArrow() //更新路线上的箭头
                }
            }
        }

        /**
         * 显示电子眼
         *
         *
         * //@details 显示电子眼，当naviCamera为NULL或者count为0时，清除界面展示的电子眼
         * //@param[in] naviCameraList   电子眼信息, 为0时，清除界面展示的电子眼
         * //@remark 包含区间电子眼，前端根据使用场景过滤使用
         * //@note thread mutil
         * //@note BL内聚在底图上 HMI无需关注
         */
        override fun onShowNaviCameraExt(naviCameraList: ArrayList<NaviCameraExt>?) {
            Timber.i("onShowNaviCameraExt is called")
            _naviCameraList.postValue(naviCameraList)
        }

        /**
         * 更新区间测试电子眼动态实时信息
         *
         *
         * //@details 更新区间测试电子眼动态实时信息
         * //@param[in] cameraDynamicList 区间电子眼动态信息数组
         */
        override fun onUpdateIntervalCameraDynamicInfo(arrayList: ArrayList<NaviIntervalCameraDynamicInfo>?) {
            Timber.i("onUpdateIntervalCameraDynamicInfo is called")
            //显示路口大图时不显示区间测速
            updateIntervalCameraDynamicInfo(arrayList)
        }

        /**
         * 更新服务区信息
         *
         *
         * //@details 更新服务区信息，当serviceArea为NULL或者count为0时，清除服务区信息
         * //@param[in] serviceAreaList  服务区信息数组，NaviFacility.sapaDetail 第六位增加充电站显示
         * //@remark 自车在高速上才有通知
         * //@note thread mutil
         */
        override fun onUpdateSAPA(arrayList: ArrayList<NaviFacility>?) {
            Timber.i("onUpdateSAPA is called")
            updateRestInfo(arrayList)

            /*
            todo 新能源
            if(ElectricInfoConverter.isElectric()){
                arrayList?.map {
                    it.energyConsume
                }
            }*/
        }

        /**
         * //@brief 导航结束
         * //@details 通知上层，引擎已完成结束导航动作
         * //@param[in] traceId 场景ID，表示驾车/公交/步导（ID值只要能区分不同即可）
         * //@param[in] naviType 0:GPS导航, 1:模拟导航
         * //@remark 1、前端使用者先调用StopNavi，收到这个回调表示引擎已经完成结束导航动作
         *             2、前端使用者在未接收到这个回调之前，不可移除guide观察者，否则可能接收不到onDriveReport回调
         * //@note thread mutil
         */
        override fun onNaviStop(id: Long, naviType: Int) {
            Timber.i(" NaviBusiness onNaviStop is called")
            onHideCrossImage(mRoadCrossType)
            if (isCloseNaviFragment) {
                finishNavi()
            }
        }


        /**
         * //@brief 导航到达终点
         * //@details 到达目的地的时候，通知到达终点事件
         * //@param[in] traceId 场景ID，表示驾车/公交/步导（ID值只要能区分不同即可）
         * //@param[in] naviType 0:GPS导航, 1:模拟导航
         * //@remark 1、(车到目的地小于50米时由引擎回调通知上层到达目的地)
         * 2、前端使用者收到这个回调需要调用GuideService::stopNavi结束导航；
         * //@note thread mutil
         */
        override fun onNaviArrive(id: Long, naviType: Int) {
            Timber.i(" NaviBusiness onNaviArrive is called naviType = $naviType")
            AutoStatusAdapter.sendStatus(AutoStatus.NAVI_ARRIVED)
            try {
                naviScopeIo.launch {
                    delay(if (naviType == NaviType.NaviTypeSimulation) 5000 else 3000)
                    stopNavi(naviType == NaviType.NaviTypeSimulation)
                    Timber.i("onNaviArrive:  stopNavi")
                }
            } catch (e: Exception) {
                Timber.e(" onNaviArrive e = ${e.message}")
            }
        }

        /**
         * 更新经过途经点索引
         *
         *
         * //@details 导航过程中经过途经点的时候通知途经点索引
         * //@param[in] viaIndex         途经点索引
         * //@note thread mutil
         */
        override fun onUpdateViaPass(viaIndex: Long) {
            Timber.i(" NaviBusiness onUpdateViaPass is called viaIndex = $viaIndex , mNaviType = $mNaviType")
            if (mNaviType == NaviType.NaviTypeGPS) {
                val midPois = mRouteRequestController.carRouteResult.midPois
                if (midPois != null && midPois.size > 0) {
                    mRouteRequestController.carRouteResult.midPois.removeAt(0)
                    mDrivingLayer?.updateViaPassStyle()
                }
            }
        }

        /**
         * 锁屏导航提示，锁屏状态导航远距离提示点亮屏幕
         *
         *
         * //@details 导航过程中通知锁屏导航提示
         * //@param[in] tip              锁屏提示信息
         * //@remark 1、使用场景：锁屏时在屏幕上显示的语音转向信息；
         * //@remark 2、导航段长度大于等于其远距离播报距离时，只在远距离位置进行一次锁屏通知
         * //@remark 3、导航段长度小于其远距离播报距离时，在其首次播报转向动作的位置进行一次锁屏通知
         * //@note thread mutil
         */
        override fun onShowLockScreenTip(lockScreenTip: LockScreenTip?) {
            Timber.i("onShowLockScreenTip: ")
        }

        /**
         * 驾驶行为报告
         *
         *
         * //@details 导航结束时，传出驾驶行为报告
         * //@param[in] driveReport      驾驶行为报告, json格式
         * //@param[in] naviStatisticsInfo  统计信息
         * //@remark 在导航结束的时候通知
         * //@note thread mutil
         */
        override fun onDriveReport(driveReport: DriveReport?) {
            Timber.i("onDriveReport:  isCloseNaviFragment = $isCloseNaviFragment , mNaviType = $mNaviType  driveReport: ${gson.toJson(driveReport)} ")
            if (mNaviType == NaviType.NaviTypeGPS) {
                unInit(mNaviType)
            }
            if (!isCloseNaviFragment) {
                Timber.i("onDriveReport: handleDriveReport BaseConstant.MAP_APP_FOREGROUND = ${BaseConstant.MAP_APP_FOREGROUND}")
                if (BaseConstant.MAP_APP_FOREGROUND) { //地图在前台
                    driveReport?.let {
                        handleDriveReport(driveReport)
                    }
                } else {
                    finishNavi()
                    mMapBusiness.backToMap.postValue(true) //回到地图主图
                }
            }
        }

        /**
         * 驾驶行为事件
         *
         *
         * //@details 导航结束时，传出驾驶行为事件
         * //@param[in] list             驾驶行为事件
         * //@remark 在导航结束的时候通知
         * //@note thread mutil
         */
        override fun onShowDriveEventTip(arrayList: ArrayList<DriveEventTip?>?) {
            Timber.i("onShowDriveEventTip: ")
        }

        /**
         * @param routeOption 重算信息
         * guide引擎通知重算
         * //@details 因偏航，道路限行，tmc路况拥堵等原因，guide引擎会通知外界进行路线重算
         * //@note thread mutil
         */
        override fun onReroute(routeOption: RouteOption?) {}


        /**
         * 偏航过程中或偏航失败车位重新匹配到引导道路
         *
         *
         * //@details 导航过程中因偏航过程中或偏航失败车位重新匹配到引导道路
         * //@remark 偏航后，如果在还没有算路成功，车回到原来路径的情况下
         * //@note thread mutil
         */
        override fun onCarOnRouteAgain() {
            Timber.i("onCarOnRouteAgain: ")
        }


        /**
         * 传出拥堵时长和原因
         *
         *
         * //@details 导航过程中传出拥堵时长和原因
         * //@param[in] info             传出拥堵时长
         * //@note thread mutil
         * //@note BL内聚在底图上 HMI无需关注
         */
        override fun onUpdateTMCCongestionInfo(naviCongestionInfo: NaviCongestionInfo?) {}

        /**
         * 传出交通事件信息
         *
         *
         * //@details 传出sdk获取的交通事件信息, 以及大数据挖据事件信息，用于终端众验
         * //@param[in] pathsTrafficEventInfo  事件数组
         * //@param[in] pathCount        事件个数
         * //@note thread mutil
         * //@note BL内聚在底图上 HMI无需关注
         */
        override fun onUpdateTREvent(arrayList: ArrayList<PathTrafficEventInfo>, l: Long) {
            Timber.i("onUpdateTREvent: arrayList.size = ${arrayList.size}")
//            if (showCrossView.value == false) {
//                naviScopeIo.launch {
//                    if (arrayList.isNotEmpty() && arrayList.first().eventInfoArray.isNotEmpty()) {
//                        val trafficEvent = arrayList.first().eventInfoArray.first()
//                        Timber.i("onUpdateTREvent: trafficEventInfo = ${gson.toJson(trafficEvent)}")
//                        val result = mUserBusiness.sendReqTrafficEventDetail(trafficEvent.id.toString())
//                        Timber.i("onUpdateTREvent: result = ${gson.toJson(result)}")
//                        when (result.status) {
//                            Status.SUCCESS -> {
//                                result.data?.takeIf { it.code == 1 && it.EventData != null }?.let { data ->
//                                    Timber.i("onUpdateTREvent sendReqTrafficEventDetail is  SUCCESS")
//                                    showForbiddenInfo(data.EventData.address, data.EventData.labelDesc, data.EventData.head)
//                                } ?: run {
//                                    _forbiddenInfoVisibility.postValue(false)
//                                }
//                            }
//
//                            else -> {
//                                Timber.i("onUpdateTREvent sendReqTrafficEventDetail is  Fail")
//                            }
//                        }
//                    }
//                }
//            }

        }

        /**
         * 传出交通事件信息，用于终端显示
         *
         *
         * //@details 路况播报与终端显示统一
         * //@param[in] info             事件信息
         * //@note thread mutil
         */
        override fun onUpdateTRPlayView(routeTrafficEventInfo: RouteTrafficEventInfo?) {
            Timber.i("onUpdateTRPlayView: ")
        }

        /**
         * 事件上报回调
         *
         *
         * //@details 显示常规拥堵或者非正常拥堵事件位置
         * //@param[in] incident         事件信息
         * //@note thread mutil
         */
        override fun onShowTMCIncidentReport(tmcIncidentReport: TMCIncidentReport?) {
            Timber.i("onShowTMCIncidentReport: ")
        }

        /**
         * 隐藏常规拥堵或者非正常拥堵事件
         *
         *
         * //@details 导航过程中通知常规拥堵或者非正常拥堵事件
         * //@param[in] type             拥堵事件类型
         * //@note thread mutil
         */
        override fun onHideTMCIncidentReport(i: Int) {
            Timber.i("onHideTMCIncidentReport: ")
        }

        /**
         * 对外输出socol采集时间段
         *
         *
         * //@details 导航过程中输出socol采集时间段
         * //@param[in] text             对外输出字符串信息
         * //@note thread mutil
         */
        override fun onUpdateSocolText(text: String) {
            Timber.i("onUpdateSocolText: text = $text")
        }

        /**
         * 更新是否支持简易三维导航
         *
         *
         * //@details guide根据ILink接口IsSupport3DNavigation，\n
         * 判断自车所在Link是否支持简易三维导航,当状态变换时，通知给客户端
         * //@param[in] support          false:不支持, true:支持
         * //@note thread mutil
         */
        override fun onUpdateIsSupportSimple3D(b: Boolean) {
            Timber.i("onUpdateIsSupportSimple3D: ")
        }

        /**
         * 删除对应id的路线, 如过分歧点，不需要重新设置naviPath给引擎
         *
         *
         * //@details guide根据情况，通知删除id为pathID的路线
         * //@param[in] pathIDList 路线id
         */
        override fun onDeletePath(pathIDList: ArrayList<Long>?) {
            Timber.i("onDeletePath is called pathIDList = ${pathIDList?.size}")
            if (mNaviType == NaviType.NaviTypeGPS) {
                if (pathIDList != null && pathIDList.size > 0) {
                    if (NaviController.getInstance().isNaving) {
                        mNaviComponent.setDeletePath(
                            pathIDList,
                            mRouteRequestController.carRouteResult,
                            mNetWorkManager.isNetworkConnected()
                        )
                    } else {
                        Timber.i("onDeletePath navi is false")
                    }
                } else {
                    Timber.i("onDeletePath pathIDList == null")
                }
            }
        }

        /**
         * @param pathID 主选路线id
         * 切换主选路线，引擎定位检测到走到备选路线，回调通知切换路线，主动选择不会有此通知
         * //@details guide根据情况，通知将id为pathID的备选路线切换为主选路线
         */
        override fun onChangeNaviPath(oldPathID: Long, pathID: Long) {
            Timber.i("onChangeNaviPath is called oldPathID = $oldPathID ; pathID = $pathID")
            mNaviController.setChangeNaviPath(mRouteRequestController.carRouteResult, pathID)
            _routeChangedNotice.postValue(true)
        }

        /**
         * 进阶动作 转向图标处理
         */
        override fun onObtainManeuverIconData(maneuverIconResponseData: ManeuverIconResponseData?) {
            Timber.i("onObtainManeuverIconData is called")
            naviScopeIo.launch {
                maneuverIconResponseData?.let {
                    //当前路口的主动作图标
                    val config = it.requestConfig
                    if (null != config && config.width == NaviUiUtil.nextTurnIconSize && config.height == NaviUiUtil.nextTurnIconSize) {
                        updateNextThumTurnIcon(maneuverIconResponseData.data, config, mNextThumRoundNum)
                    } else {
                        updateTurnIcon(maneuverIconResponseData.data, config, mRoundNum)
                    }
                }
            }
        }

        /**
         * 透出主动请求路线上所有服务区信息
         */
        override fun onObtainSAPAInfo(sapaInquireResponseData: SAPAInquireResponseData?) {
            Timber.i("onObtainSAPAInfo is called sapaInquireResponseData = ${gson.toJson(sapaInquireResponseData)}")
            Timber.i("onObtainSAPAInfo is called activeShutdownSAPATip = $activeShutdownSAPATip")
            _showGetSAPAInfoDialogVisible.postValue(false)
            if (activeShutdownSAPATip || sapaInquireResponseData == null || sapaInquireResponseData.serviceAreaInfo == null) {
                setToastTip("高速全程信息加载失败")
                return
            }
            _serviceAreaInfo.postValue(sapaInquireResponseData.serviceAreaInfo)
            setServiceAreaInfoVisible(true)
        }

        /**
         * 透出混淆路口信息
         *
         *
         * //@details 透出混淆路口信息
         * //@param[in] list             混淆路口信息列表
         * //@remark 通知距离导航路口最近的容易混淆的路口信息
         * //@note thread mutil
         * //@note BL内聚在底图上 HMI无需关注
         */
        override fun onShowSameDirectionMixForkInfo(list: ArrayList<MixForkInfo?>?) {}


        /**
         * //@return void 无返回值
         * 导航场景下道路设施信息等信息
         * //@details 显示设施，当list为空时，清除界面展示的设施
         * //@param[in] list 设施信息
         * //@note BL内聚在底图上 HMI无需关注
         */
        override fun onShowNaviFacility(list: ArrayList<NaviRoadFacility>?) {
            _naviRoadFacilityList.postValue(list)
        }

        /**
         * 透出一定距离范围内的收费站车道信息
         * //@details 距离默认高速1KM，快速500m，其他300米
         * //@param[out] tollGateInfo     收费站车道信息，为空代表关闭
         * //@remark 用于骑行步导场景下
         */
        override fun onShowTollGateLane(tollGateInfo: TollGateInfo?) {
            Timber.i(" onShowTollGateLane is called tollGateInfo = ${gson.toJson(tollGateInfo)}")
            if (tollGateInfo?.laneTypes?.isNotEmpty() == true) {
                // 显示收费车道信息
                isTollBoothsLane = true
                val size = tollGateInfo.laneTypes.size
                val laneInfos = ArrayList<TBTLaneInfoBean>(size)
                for (i in 0 until size) {
                    var mTBTLaneInfoBean = TBTLaneInfoBean()
                    mTBTLaneInfoBean.isTollBoothsLane = true
                    mTBTLaneInfoBean.isRecommend = false
                    mTBTLaneInfoBean.laneAction = tollGateInfo.laneTypes.get(i)
                    laneInfos.add(mTBTLaneInfoBean)
                }
                if (size > 0) {
                    _naviLaneVisible.postValue(true)
                    _naviLaneList.postValue(laneInfos)
                    _tollLaneList.postValue(tollGateInfo.laneTypes)
                }
            } else {
                // 隐藏收费车道信息
                isTollBoothsLane = false
                _naviLaneVisible.postValue(false)
                _naviLaneList.postValue(ArrayList())
                _tollLaneList.postValue(ArrayList())
            }
        }

        /**
         * 传出红路灯交通信号信息。
         *
         *
         * //@details 传出红路灯交通信号信息。包括红绿灯的状态及对应的预计结束时间，红绿灯经纬度坐标等。
         * //@param[in] list             红绿灯的数据
         * //@note BL内聚在底图上 HMI无需关注
         */
        override fun onUpdateTrafficSignalInfo(arrayList: ArrayList<TrafficSignal?>?) {}

        /**
         * 传出红路灯倒计时
         *
         * @param arrayList 红路灯倒计时的数据
         */
        override fun onUpdateTrafficLightCountdown(list: java.util.ArrayList<TrafficLightCountdown>?) {
            Timber.i("onUpdateTrafficLightCountdown is called ${list?.isNotEmpty()}")
            _trafficLightCountdownInfo.postValue(list)
        }

        /**
         * 新能源相关参数透出
         *
         *
         * 透出电动车ETA信息。
         *
         *
         * //@details 透出电动车ETA信息，仅在线支持。
         * //@param[in] elecVehicleETAInfo  新能源ETA信息
         */
        override fun onUpdateElecVehicleETAInfo(arrayList: ArrayList<ElecVehicleETAInfo?>?) {
            if (ElectricInfoConverter.isElectric()) {
                //todo 新能源
            }
        }

        /**
         * 限速路牌透出。
         *
         *
         * //@details 透出电动车ETA信息，仅在线支持。
         */
        override fun onCurrentRoadSpeed(speed: Int) {
            _mCurrentRoadSpeed.postValue(speed)
            _roadSpeedVisible.postValue(speed > 0)
        }


        /**
         * 传出导航状态下的拥堵事件信息
         *
         *
         * //@param[in] info 拥堵事件信息
         */
        override fun onUpdateNaviSocolEvent(socolEventInfo: SocolEventInfo) {
            Timber.i("拥堵2 onUpdateNaviSocolEvent: $socolEventInfo")
        }

        /**
         * 传出红绿灯绿波车速信息
         *
         *
         * //@param[in] arrayList 绿波车速信息
         */
        override fun onUpdateGreenWaveCarSpeed(arrayList: ArrayList<NaviGreenWaveCarSpeed>?) {
            Timber.i(" onUpdateGreenWaveCarSpeed is called NaviGreenWaveCarSpeed = ${gson.toJson(arrayList)}")
            if (arrayList.isNullOrEmpty()) {
                _naviGreenWaveCarSpeedVisible.postValue(false)
            } else {
                _naviGreenWaveCarSpeedVisible.postValue(true)
                _naviGreenWaveCarSpeed.postValue(arrayList[0])
            }
        }

    }

    /** ============================================== 导航过程中相关信息回调 INaviObserver end   ================================================== **/

    /**
     * 提供给用户服务 GPS轨迹
     */
    private val iGpsInfoGetter = IGpsInfoGetter { mGpsTrackPoint }

    /**
     * @return void
     * 更新主辅路信息
     * @details 启动主辅路信息更新的条件与更新位置信息相同。主辅路信息只有在位置信息更新的时候才会更新
     * 导航时，只输出导航路径的主辅路信息
     * @attention 此接口是在引擎线程内触发的，严禁做大规模运算或调用可能导致线程挂起的接口，如IO操作、同步类接口(同步DBUS)等。
     * @attention onParallelRoadUpdate接口由定位线程调用，如果有访问临界区需要做保护。但不建议使用过多的锁。
     */
    private val posParallelRoadObserver = IPosParallelRoadObserver { locParallelRoadInfo ->
        Timber.i("onParallelRoadUpdate is called 主辅路切换通知  ")
        mLocParallelRoadInfo = locParallelRoadInfo
        mLocParallelRoadInfo?.let { info ->
            Timber.i("onParallelRoadUpdate 主辅路切换状态status=${info.status} 主辅路标志flag=${info.flag}, 高架上下标志为hwFlag=${info.hwFlag}")
            //status: 主辅路切换状态:0 非平行路切换期间 1 平行路切换期间
            //flag:	  主辅路标识（默认0，离线数据计算/在线算路下发） 0：无主辅路（车标所在道路旁无主辅路） 1：车标在主路（车标所在道路旁有辅路） 2：车标在辅路（车标所在道路旁有主路）
            //hwFlag: 高架上下标识（默认0，在线算路下发） 0：无高架 1：车标在高架上（车标所在道路有对应高架下） 2：车标在高架下（车标所在道路有对应高架上）
            if (info.status != 1 && info.parallelRoadList.isNotEmpty()) {
                // 获取切换的roadid，后面重新算路时需要设置给起点
                mRoadId = info.parallelRoadList[0].roadId
            }
        }
        MainScope().launch {
            updateParallelRoad(mLocParallelRoadInfo)
        }

    }

    /**
     * @return void
     * 通知定位引擎完成主辅路切换
     * @details 定位引擎接收到客户端的切换通知后，关闭主辅路通知，然后调用此接口通知Guide完成主辅路切换
     * @attention 此接口是在引擎线程内触发的，严禁做大规模运算或调用可能导致线程挂起的接口，如IO操作、同步类接口(同步DBUS)等。
     * @attention onSwitchParallelRoadFinished接口由定位线程调用，如果有访问临界区需要做保护。但不建议使用过多的锁。
     */
    private val posSwitchParallelRoadObserver = IPosSwitchParallelRoadObserver {
        Timber.i("onSwitchParallelRoadFinished is called 主辅路切换完成通知  ")
        mRouteRequestController.registerRouteObserver(refreshRouteResultCallBack)
        if (mSwitchRoadType == LocSwitchRoadType.LocSwitchMainToSide) {
            //已为您切换至辅路
            setToastTip(application.getString(R.string.sv_navi_loc_switch_main_to_side))
        } else if (mSwitchRoadType == LocSwitchRoadType.LocSwitchSideToMain) {
            //已为您切换至主路
            setToastTip(application.getString(R.string.sv_navi_loc_switch_main_to_main))
        } else if (mSwitchRoadType == LocSwitchRoadType.LocSwitchUpBridgeToDownBridge) {
            //已为您切换至桥下
            setToastTip(application.getString(R.string.sv_navi_loc_switch_up_bridge_to_down_bridge))
        } else if (mSwitchRoadType == LocSwitchRoadType.LocSwitchDownBridgeToUpBridge) {
            //已为您切换至桥上
            setToastTip(application.getString(R.string.sv_navi_loc_switch_up_bridge_to_up_bridge))
        }
    }

    /**
     * 图层上POI点击监听
     */
    private var iLayerClickObserver: ILayerClickObserver = object : ILayerClickObserver {

        override fun onBeforeNotifyClick(
            baseLayer: BaseLayer?,
            layerItem: LayerItem?,
            clickViewIdInfo: ClickViewIdInfo?
        ) {
        }

        /**
         * 图层点击回调
         *
         * @param baseLayer
         * @param layerItem       图层回调信息,需根据不同业务场景转换
         * @param clickViewIdInfo
         */
        override fun onNotifyClick(baseLayer: BaseLayer?, layerItem: LayerItem?, clickViewIdInfo: ClickViewIdInfo?) {
            //需要特别注意：baseLayer和layerItem只能在这个函数中使用，不能抛到其它线程中使用（因为对象可能已经被释放）
            if (baseLayer == null || layerItem == null) {
                return
            }
            val businessType = layerItem.businessType
            val itemType = layerItem.itemType
            val id = layerItem.id
            Timber.i("NaviBusiness onNotifyClick businessType: $businessType  , id: ${layerItem.id}")
            when (businessType) {
                BizCustomTypePoint.BizCustomTypePoint4 -> { //停车场poi点击
                    if (layerItem is CustomPointLayerItem) {
                        val position = layerItem.mType
                        setParkingPosition(position)
                    }
                }

                BizRouteType.BizRouteTypeGuideLabel -> {//路径图层气泡
                    val mixForkItem: GuideLabelLayerItem = layerItem as GuideLabelLayerItem
                    val curSelectIndex = mixForkItem.mAlterPathIndx
                    val carRouteResult = mRouteRequestController.carRouteResult
                    if (carRouteResult?.pathResult == null) {
                        return
                    }
                    if (curSelectIndex !in 0 until carRouteResult.pathResult.size || curSelectIndex == carRouteResult.focusIndex) {
                        return
                    }
                    Timber.i("BizRouteTypeGuideLabel id= ${mixForkItem.id} ; index = $curSelectIndex")
                    mNaviController.setNaviPathChoose(carRouteResult, curSelectIndex)
                    carRouteResult.focusIndex = curSelectIndex
                    when (curSelectIndex) {
                        0 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_1)
                        1 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_2)
                        2 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_3)
                    }
                    _routeChangedNotice.postValue(true)
                }

                BizRouteType.BizRouteTypePath -> {//路径图层
                    MainScope().launch {
                        mRouteRequestController.carRouteResult?.pathResult?.let {
                            val tmpSelectIndex: Int =
                                mNaviController.getTmpCurSelectIndex(mRouteRequestController.carRouteResult, itemType, id)
                            Timber.i(" BizRouteTypePath tmpSelectIndex: $tmpSelectIndex")
                            //避免-1 无效情况
                            mRouteRequestController.carRouteResult.focusIndex = if (tmpSelectIndex == -1) 0 else tmpSelectIndex
                            _routeChangedNotice.postValue(true)
                            when (tmpSelectIndex) {
                                0 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_1)
                                1 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_2)
                                2 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_3)
                            }
                        }
                    }
                }

                BizRouteType.BizRouteTypeViaPoint -> {
                    val viaLayerItem = layerItem as PointLayerItem
                    val carRouteResult = mRouteRequestController.carRouteResult
                    if (carRouteResult != null && !carRouteResult.midPois.isNullOrEmpty()) {
                        //id= viaLayerItem.getID() 现在是0 1 2 可以对应途经点列表的下标 是这样设计的/。。
                        val index = StringUtils.str2Int(id, 10, -1)
                        if (index != -1) {
                            //不是-1代表成功
                            if (index < carRouteResult.midPois.size) {
                                val text: String = carRouteResult.midPois[index].name
                                val location = mLocationController.lastLocation
                                val startPoint = Coord2DDouble(location.longitude, location.latitude)
                                val midPOi = Coord2DDouble(viaLayerItem.position.lon, viaLayerItem.position.lat)
                                val disStr: String =
                                    CommonUtils.showDistance(BizLayerUtil.calcDistanceBetweenPoints(startPoint, midPOi))
                                val address: String = carRouteResult.midPois[index].addr
                                Timber.i("BizRouteTypeViaPoint disStr: $disStr")
                                _showViaNaviViaDataDialog.postValue(NaviViaDataBean(index, text, disStr, "", address))
                                setMapCenter(
                                    carRouteResult.midPois[index].point.longitude,
                                    carRouteResult.midPois[index].point.latitude
                                )
                            }
                        }
                    }
                    mDrivingLayer?.updateViaFocus()
                }

                BizRouteType.BizRouteTypeEndPoint -> {
                    mDrivingLayer?.updateEndFocus()
                    mRouteEndAreaLayer?.clearFocus()
                }

                BizAreaType.BizAreaTypeEndAreaParentPoint -> {
                    mRouteEndAreaLayer?.clearFocus()
                }

                else -> {}
            }
        }

        override fun onAfterNotifyClick(
            baseLayer: BaseLayer,
            layerItem: LayerItem,
            clickViewIdInfo: ClickViewIdInfo?
        ) {
        }
    }

    /**
     * 偏航事件回调接口
     */
    private val naviRerouteObserver = object : INaviRerouteObserver {
        override fun onModifyRerouteOption(routeOption: RouteOption?) {
            Timber.i(" onModifyRerouteOption  is called ")
        }

        override fun onRerouteInfo(blRerouteRequestInfo: BLRerouteRequestInfo?) {
            Timber.i(" onRerouteInfo  is called ")
            if (isRequestRoute) {
                Timber.i("onRerouteInfo isRequestRoute = $isRequestRoute")
                return
            }
            var routeType = RouteType.AUTO_UNKNOWN_ERROR
            try {
                if (blRerouteRequestInfo == null) {
                    Timber.i("onRerouteInfo blRerouteRequestInfo is null")
                    return
                }
                mRerouteOption = blRerouteRequestInfo.option
                if (mRerouteOption == null) {
                    Timber.i("onRerouteInfo RouteOption is null")
                    return
                }
                routeType = blRerouteRequestInfo.option.routeType
            } catch (e: Exception) { //防止快速多次回调导致空值针异常
                Timber.e("onRerouteInfo error ${e.message}")
                return
            }
            Timber.i("onRerouteInfo routeType = $routeType ")
            //mRerouteRequestInfo = blRerouteRequestInfo;
            if (routeType == RouteType.RouteTypeMutiRouteRequest && mRouteRequestController.routeRequesting()) {
                Timber.i("onRerouteInfo RouteTypeMutiRouteRequest, there are other route requesting, so give up")
            } else {
                rerouteRequestInfo = blRerouteRequestInfo
                mRouteRequestController.registerRouteObserver(refreshRouteResultCallBack)
            }
            if (routeType == RouteType.RouteTypeYaw) {
                _routeYawNotice.postValue(true)
                _loadingRoute.postValue(true)
                setToastTip(application.getString(R.string.sv_navi_reroute_reason_yaw))
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.NAVI_YAW_REROUTE)
                AutoStatusAdapter.sendStatus(AutoStatus.NAVI_YAW_UPDATE_PATH_START)
            }

        }

        override fun onSwitchParallelRoadRerouteInfo(blRerouteRequestInfo: BLRerouteRequestInfo?) {
            Timber.i("onSwitchParallelRoadRerouteInfo is called")
            if (blRerouteRequestInfo == null) {
                Timber.i("onSwitchParallelRoadRerouteInfo blRerouteRequestInfo is null")
                return
            }
            mRerouteOption = blRerouteRequestInfo.option
        }
    }
//===============================================================分割线===========================================================================

    /**
     * 初始化监听
     */
    fun init(naviType: Int) {
        Timber.i(" NaviBusiness init is called  naviType = $naviType")
        mNaviType = naviType
        isFirstGoToNavi = true
        initNaviScope()
        updaterArriveTime()
        mNaviController.registerNaviObserver(naviObserver)
        mLocationController.addLocInfoObserver(posLocInfoObserver)
        mMapView?.addGestureObserver(mapGestureObserver)
        mMapController.setMapStyle(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            NightModeGlobal.isNightMode(),
            EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NAVI
        )
        mMapController.addMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mOnMapLevelChangedObserver)
        mUserBusiness.registerGpsInfoGetter(iGpsInfoGetter)
        //设置播报类型,0无效 播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
        val mCurrentBroadcastMode = settingComponent.getConfigKeyBroadcastMode()
        Timber.d(" NaviBusiness init mCurrentBroadcastMode:$mCurrentBroadcastMode")
        /**
         * 播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
         */
        when (mCurrentBroadcastMode) {
            SettingConst.BROADCAST_EASY -> {
                mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "4");
            }

            SettingConst.BROADCAST_MINIMALISM -> {
                mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "6");
            }

            else -> {
                mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "2");
            }
        }
        //初始化引导参数
        mNaviComponent.initGuideParam(iCarInfoProxy.sNCode, settingComponent.getConfigKeyAvoidLimit())
        //隐藏收藏点
        mUserBehaviorLayer?.clearAllItems()
        mMapBusiness.setClickLabelMoveMap(false)
        when (naviType) {
            NaviType.NaviTypeGPS -> {
                mNaviComponent.setViaPassGreyMode(true)//设置走过的途经点置灰
                //添加主辅路信息观察者
                mLocationController.addPosParallelRoadObserver(posParallelRoadObserver)
                //添加主辅路切换完成通知
                mLocationController.addSwitchParallelRoadObserver(posSwitchParallelRoadObserver)
                //在画路线之前,添加图层点击事件监听
                mDrivingLayer?.addClickObserver(iLayerClickObserver)
                //停车场poi点击事件监听
                mSearchLayer?.addClickObserver(iLayerClickObserver)
                mCustomLayer?.addClickObserver(iLayerClickObserver)
                //偏航事件监听
                mNaviController.registerNaviRerouteObserver(naviRerouteObserver)
            }

            NaviType.NaviTypeSimulation -> {

            }
        }
        mNaviController.registerContinueGuideInfoObserver(mContinueGuideInfoObserver)
    }

    fun initNaviScope() {
        Timber.i(" initNaviScope is called ")
        if (!naviScopeIo.isActive) {
            Timber.i(" initNaviScope is called is new ")
            naviScopeIo = CoroutineScope(Dispatchers.IO + Job())
            naviScopeMain = CoroutineScope(Dispatchers.Main + Job())
        }
    }

    /**
     * 反初始化
     */
    fun unInit(naviType: Int) {
        Timber.i(" NaviBusiness unInit is called naviType = $naviType")
        mNaviType = NaviType.AUTO_UNKNOWN_ERROR
        mNaviController.clearAllMessages()
        abortRequestTaskId()
        mNaviController.unregisterNaviObserver(naviObserver)
        mMapView?.removeGestureObserver(mapGestureObserver)
        mMapController.removeMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mOnMapLevelChangedObserver)
        mUserBusiness.unregisterGpsInfoGetter(iGpsInfoGetter)
        mLocationController.removeLocInfoObserver(posLocInfoObserver)
        mDrivingLayer?.setRoadCrossVisible(mRoadCrossType, false)
        mNaviComponent.deleteNaviPath()
        mMapDataBusiness.startAllTask(DownLoadMode.DOWNLOAD_MODE_NET, null)
        mDrivingLayer?.clearAllItems()
        mDrivingLayer?.clearAllPaths()
        mDrivingLayer?.uninitEagleEye()
        if (BaseConstant.MULTI_MAP_VIEW) {
            mEx1DrivingLayer?.clearAllItems()
            mEx1DrivingLayer?.clearAllPaths()
            mEx1DrivingLayer?.uninitEagleEye()
        }
        when (naviType) {
            NaviType.NaviTypeGPS -> {
                mLocationController.removePosParallelRoadObserver(posParallelRoadObserver)
                mLocationController.removeSwitchParallelRoadObserver(posSwitchParallelRoadObserver)
                mDrivingLayer?.removeClickObserver(iLayerClickObserver)
                mSearchLayer?.removeClickObserver(iLayerClickObserver)
                mCustomLayer?.removeClickObserver(iLayerClickObserver)
                mNaviController.unregisterNaviRerouteObserver(naviRerouteObserver)
                mRouteRequestController.destroy()//删除路线数据
                mCustomLayer?.removeAllLayerItems()
                mRouteEndAreaLayer?.clearAllRouteEndAreaLayer() //清除终点区域高亮
                if (BaseConstant.MULTI_MAP_VIEW)
                    extMapBusiness.ex1RouteEndAreaLayer?.clearAllRouteEndAreaLayer()

                //获取最新的403文件
                if (CommonUtils.isVehicle()) {//是否是真实车辆
                    CustomFileUtils.getLatestFile(AutoConstant.SYNC_DIR + "403")?.let {
                        Timber.i("setRouteAndNavi() getTripReportTrack getLatestFile file is ${it.name}")
                        trackFileName = it.name
                    }
                }
                if (settingComponent.getAutoRecord() == 0) {
                    mUserBusiness.closeGpsTrack()
                }
                mUserBusiness.handleUnfinishTrace() //处理未完结或异常退出的打点轨迹文件
            }

            NaviType.NaviTypeSimulation -> {
                setCarPosition()
                mCustomLayer?.removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint4)
            }
        }
        mNaviController.unregisterContinueGuideInfoObserver(mContinueGuideInfoObserver)
        Timber.i(" NaviBusiness unInit end")
    }

    fun cancelNaviScope() {
        Timber.i(" cancelNaviScope is called ")
        naviScopeIo.cancel()
        naviScopeMain.cancel()
    }

    //回车位倒计时
    private val naviCcpTimer = object : CountDownTimer(BaseConstant.CCP_COUNT_DOWN_TOTAL_TIME, 10000) {
        override fun onTick(millisUntilFinished: Long) = Unit
        override fun onFinish() {
            Timber.i(" naviCcpTimer onFinish is called isInNaviFragment=$isInNaviFragment")
            if (isNavigating()) {
                if (isInNaviFragment) {
                    //导航中才执行该命令
                    naviBackCurrentCarPosition()//继续导航
                }
            }
        }
    }

    fun resetBackToCarTimer(bPreview: Boolean = false) {
        Timber.i("NaviBusiness resetBackToCarTimer is called")
        removeBackToCarTimer()
        startBackToCarTimer()
        setEagleVisible(false, isInNaviFragment)
        if (inFullView.value == false) {
            _backCCPVisible.postValue(true)
            mMapBusiness.setFollowMode(follow = false, bPreview = bPreview)
            setAutoZoom(false)//关闭动态比例尺
        }
        mMapBusiness.showMapControlButtons(true)
    }

    private fun removeBackToCarTimer() = naviCcpTimer.cancel()

    private fun startBackToCarTimer() = naviCcpTimer.start()

    /**
     * 继续导航
     */
    fun naviBackCurrentCarPosition() {
        Timber.i("naviBackCurrentCarPosition is called")
        val isDynamicLevel = getAutoScale() == 1//是否打开自动比例尺
        setAutoZoom(isDynamicLevel)
        _backCCPVisible.postValue(false)
        mMapBusiness.showMapControlButtons(false)
        mMapBusiness.backCurrentCarPosition(isFirstGoToNavi || inFullView.value == true)
        if (isInNaviFragment) {
            //初始化设置，根据用户设置显示光柱图或鹰眼图
            setEagleVisible(true)
        }
        _InFullView.postValue(false)
        removeBackToCarTimer()
        isFirstGoToNavi = false
        setServiceAreaInfoVisible(false)
        showParkingCardDetail(false)
    }

    /**
     * 退出全览
     */
    fun exitPreview() {
        Timber.i("exitPreview is called")
        val isDynamicLevel = getAutoScale() == 1//是否打开自动比例尺
        setAutoZoom(isDynamicLevel)
        mMapBusiness.backCurrentCarPosition()
        _InFullView.postValue(false)
    }

    /**
     * 打开全览
     *
     * type = 1：浏览未走过路线， 2：浏览全局路线
     */
    fun showPreview(type: Int = 1) {
        Timber.i("NaviBusiness showPreview is called ")
        resetBackToCarTimer(true)   //全览的时候要关闭自动比例尺功能 //关闭全览跟随模式  //设置预览模式
        if (type == 1) {
            //type = 1：浏览未走过路线
            mCurNaviInfo?.let {
                routeRepository.showPreview(it.curSegIdx.toLong(), it.curLinkIdx.toLong(), it.curPointIdx.toLong())
            }
        } else {
            //type = 2：浏览全局路线
            routeRepository.showPreview()
        }
        _InFullView.postValue(true)
    }

    //设置路线并开始导航
    @Synchronized
    fun setRouteAndNavi(type: Int) {
        Timber.i("setRouteAndNavi() is called mIsHome = $mIsHome， type = $type")
        isUpdateNaviInfo = false
        isLessThran500Delete = false
        //清除路线
        mDrivingLayer?.clearAllPaths()
        if (BaseConstant.MULTI_MAP_VIEW) mEx1DrivingLayer?.clearAllPaths()
        mCustomLayer?.removeAllLayerItems()
        val routeCarResultData = mRouteRequestController.carRouteResult
        Timber.i("setRouteAndNavi() FromPOI:   ${gson.toJson(routeCarResultData.fromPOI)}")
        Timber.i("setRouteAndNavi() MidPois():  ${gson.toJson(routeCarResultData.midPois)}")
        Timber.i("setRouteAndNavi() ToPOI:   ${gson.toJson(routeCarResultData.toPOI)}")
        if (linkCarState == AutoState.LINK_CAR_REROUTE_SUCCESS || linkCarState == AutoState.LINK_CAR_REROUTE_PREF_SUCCESS || linkCarState == AutoState.LINK_CAR_ADD_MID_SUCCESS || linkCarState == AutoState.LINK_CAR_DELETE_MID_SUCCESS) {
            SdkAdapterManager.getInstance().sendNormalMessage(linkCarState)
            linkCarState = 0
        }

        //清除删除所有自定义图层扎点
        //添加导航记录
        val toPoiName = routeCarResultData.toPOI.name
        if (toPoiName?.isNotEmpty() == true && toPoiName != "我的位置") {
            val historyRouteItem = ConverUtils.convertRouteResultToSearchHistoryRouteItem(routeCarResultData)
            Timber.i("setRouteAndNavi() historyRouteItem toPoi: ${gson.toJson(historyRouteItem.toPoi)}")
            mUserBusiness.delHistoryRoute(historyRouteItem, SyncMode.SyncModeNow)
            mUserBusiness.addRouteHistory(historyRouteItem)
        }

        if (mIsHome) { //是否从首页服务区续航进入
            try {
                if (routeCarResultData.pathResult != null) {
                    Timber.i("getLocationPos")
                    mLastRouteUtils.setContextPos()
                } else {
                    Timber.e("setRouteAndNavi() 获取数据错误,无法继续导航")
                    //获取数据错误无法继续导航
                    setToastTip("获取数据错误,无法继续导航")
                    mLastRouteUtils.deleteRouteFile()
                    finishNavi()
                }
            } catch (e: Exception) {
                Timber.e("setRouteAndNavi e: %s", e.message)
                setToastTip("获取数据错误,无法继续导航")
                mLastRouteUtils.deleteRouteFile()
                finishNavi()
            }
        } else {
            Timber.i("setRouteAndNavi() saveRouteToFile")
            mLastRouteUtils.saveRouteToFile(routeCarResultData)
        }

        //绘制路线
        mNaviComponent.setRoute(
            routeCarResultData,
            routeCarResultData.pathResult,
            mIsHome,
            mNaviType,
            mNetWorkManager.isNetworkConnected(),
            naviSharePreference.getBooleanValue(BaseConstant.KEY_CONTINUE_SAPA_NAVI, false),
            type
        )

        //初始化鹰眼图层
        mDrivingLayer?.initEagleEye(NavigationUtil.getEagleStyle(isEagleBgSquare, mShowEagleViaPoint))

        if (type == BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD_MISPOI) {
            //添加途经点需要全览路线
            naviScopeMain.launch {
                delay(500) // 延迟500毫秒启动
                Timber.i("NEED_REQUEST_RX_PLAN_ROAD_MISPOI showPreview")
                showPreview(2)
            }
        } else {
            naviBackCurrentCarPosition()
        }

        Timber.i("initTbt: init = ${mNaviController.isInitSuccess}")
        mMapDataBusiness.pauseAllTask(DownLoadMode.DOWNLOAD_MODE_NET, null)
        if (!mNaviController.isInitSuccess) {
            setToastTip("导航模块初始化失败，请重试")
            return
        } else {
            val isNavi = mNaviController.startNavi(mNaviType.toLong(), mNaviType) //开始导航
            Timber.i("isNavi=$isNavi , mNaviController.startNavi(mNaviType=$mNaviType)")
            if (isNavi) {
                when (mNaviType) {
                    //真实导航
                    NaviType.NaviTypeGPS -> _naviStatus.postValue(BaseConstant.NAVI_STATE_REAL_NAVING)

                    //模拟导航
                    NaviType.NaviTypeSimulation -> _naviStatus.postValue(BaseConstant.NAVI_STATE_SIM_NAVING)
                }
            }
            if (isRealNavi() && settingComponent.getAutoRecord() == 0) {
                val curTime = System.currentTimeMillis()
                val diu: String = iCarInfoProxy.sNCode
                val filename = curTime.toString() + "_" + 1 + "_" + diu
                mUserBusiness.startTrackAndhandleUnfinishTrace(filename)
            }
        }

        if (mIsHome) {
            naviScopeMain.launch {
                delay(5000) // 延迟5秒启动
                Timber.i("mIsHome refreshRoute")
                if (!isUpdateNaviInfo) {
                    refreshRoute()
                }
            }
            mLastRouteUtils.deleteRouteFile() //完成服务区继续导航，删除二进制数据
            mIsHome = false
        }
        //传递数据给launcher，暂时注释
//        val navigationInfo = NavigationInfo()
//        navigationInfo.setDataType(LauncherDataType.INIT_INFO)
//        navigationInfo.setRouteAllDis(
//            mNaviController.getTotalDistance(mNaviComponent.naviPath, mRouteRequestController.carRouteResult.focusIndex).toInt()
//        )
//        val variantPathWrap = mRouteRequestController.carRouteResult.pathResult[mRouteRequestController.carRouteResult.focusIndex]
//        var travelTime: Long = 0
//        if (variantPathWrap != null) {
//            travelTime = variantPathWrap.travelTime
//        }
//        val time = (System.currentTimeMillis() + travelTime * 1000).toString()
//        val toTime: String = DateUtil.timeStampToDate(time, "HH:mm")
//        navigationInfo.setRouteAllTime(travelTime)
//        navigationInfo.setEstimatedTime(toTime)
//        LauncherOutMapUtils.setNavigationInfo(navigationInfo)

        updateNaviPointInfo()
//        checkBatteryLowTips()
        if (type != BaseConstant.Type.NEED_RX_PLAN_HAVE_SUCCESS) {
            naviScopeIo.launch {
                Timber.i("startSearchEndArea go to search")
                delay(1000)
                //终点区域高亮,发起搜索
                startSearchEndArea()
            }
        }
        AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_SUCCESS)
    }

    /**
     * 结束导航
     */
    fun stopNavi(isCloseNaviFragment: Boolean = true, isNeedResumeNavi: Boolean = false) {
        Timber.i("NaviBusiness stopNavi is called isCloseNaviFragment = $isCloseNaviFragment isNeedResumeNavi=$isNeedResumeNavi")
        this.isCloseNaviFragment = isCloseNaviFragment
        when (mNaviType) {
            //真实导航
            NaviType.NaviTypeGPS -> {
                _naviStatus.postValue(BaseConstant.NAVI_STATE_STOP_REAL_NAVI)
                mTripShareBusiness.stop()
                mRouteRequestController.carRouteResult?.let {
                    EventTrackingUtils.trackEvent(
                        EventTrackingUtils.EventName.Nav_Finish,
                        mapOf(
                            Pair(EventTrackingUtils.EventValueName.EndTime, System.currentTimeMillis()),
                            Pair(EventTrackingUtils.EventValueName.Destination, JSONObject().apply {
                                put("lon", it.toPOI?.point?.longitude ?: 0.0)  // 默认值 0.0
                                put("lat", it.toPOI?.point?.latitude ?: 0.0)   // 默认值 0.0
                            }.toString())
                        )
                    )
                }
            }
            //模拟导航
            NaviType.NaviTypeSimulation -> _naviStatus.postValue(BaseConstant.NAVI_STATE_STOP_SIM_NAVI)
        }
        roadClass = RoadClass.RoadClassNULL
        mNaviController.stopNavi()
        naviScopeIo.launch {
            if (!isNeedResumeNavi) {
                mLastRouteUtils.deleteRouteFile()
            } else {
                if (!mIsHome) {
                    mLastRouteUtils.saveRouteToFile(mRouteRequestController.carRouteResult)
                }
            }
        }
        if (isCloseNaviFragment) {
            finishNavi()
        }
    }

    /**
     * 退出导航界面
     */
    fun finishNavi() {
        Timber.i(" NaviBusiness finishNavi is called")
        unInit(mNaviType)
        cleanParkingRecommend()
        _finishFragment.postValue(true)
    }

    fun cleanParkingRecommend() {
        _parkingRecommend.postValue(arrayListOf())
    }


    private fun refreshRoute() {
        Timber.i(" refreshRoute is called")
        val carRouteResult = mRouteRequestController.carRouteResult
        if (carRouteResult != null) {
            if (!mNetWorkManager.isNetworkConnected()) {
                setToastTip(application.getString(R.string.sv_route_network_unconnection_and_no_gps))
                return
            }
            Timber.i(" refreshRoute is start")
            reRoute(carRouteResult)
        }
    }

    /**
     * 网络刷新路线
     */
    fun networkRefreshRoute() {
        Timber.i(" networkRefreshRoute is called")
        val carRouteResult = mRouteRequestController.carRouteResult
        if (!mNetWorkManager.isNetworkConnected()) {
            setToastTip(application.getString(R.string.sv_route_network_unconnection))
            return
        }
        if (carRouteResult != null) {
            if (carRouteResult.isOffline) {
                Timber.i(" networkRefreshRoute is start")
                reRoute(carRouteResult)
            }
        }
    }

    private fun reRoute(routeCarResultData: RouteCarResultData) {
        abortRequestTaskId()
        isRequestRoute = true
        //请求路线
        requestRouteTaskId =
            routeRepository.planRoute(
                routeCarResultData.fromPOI,
                routeCarResultData.toPOI,
                routeCarResultData.midPois,
                refreshRouteResultCallBack
            )
        mRerouteOption = mRouteRequestController.getRouteOption(requestRouteTaskId)
        AutoStatusAdapter.sendStatus(AutoStatus.REPLAN_PATH)
    }

    /**
     * 设置离线路线样式
     */
    fun setSwitchOffline() {
        try {
            Timber.i(" setSwitchOffline is called")
            if (mDrivingLayer != null) {
                mDrivingLayer?.setSwitchOffline(isRealNavi(), true)
            }
            if (BaseConstant.MULTI_MAP_VIEW) {
                mEx1DrivingLayer?.setSwitchOffline(isRealNavi(), true)
            }
            mRouteRequestController.carRouteResult.setIsOffline(true)
            setEagleVisible(!backCCPVisible.value!!)
        } catch (e: Exception) {
            Timber.i(" setSwitchOffline Exception:${e.message}")
        }
    }


    /**
     * 导航页面场景子POI搜索 和 绘制终点文本
     */
    private fun startSearchEndArea() {
        Timber.i("startSearchEndArea is call")
        val carRouteResult = mRouteRequestController.carRouteResult
        if (carRouteResult?.toPOI == null) {
            return
        }
        val endPoi = carRouteResult.toPOI
        naviScopeIo.launch {
            val result = mSearchBusiness.poiIdSearchV2(endPoi)
            when (result.status) {
                Status.SUCCESS -> {
                    Timber.i("startSearchEndArea onSuccess")
//                    if (!isNavigating()) {
//                        return@launch
//                    }
                    result.data?.let { data ->
                        if (carRouteResult.isSceneResult) {
                            val list: List<POI>? =
                                SearchDataConvertUtils.blPoiSearchResultToHmiResult(
                                    data,
                                    SearchRequestInfo.Builder().build()
                                ).searchInfo.poiResults
                            list?.firstOrNull()?.childPois?.takeIf { it.isNotEmpty() }?.let {
                                endPoi.childPois = it
                            }
                        }
                        if (data.poiList.isEmpty()) {
                            mRouteEndAreaLayer?.updateRouteEndParentPoint(endPoi, rectInt)
                            if (BaseConstant.MULTI_MAP_VIEW)
                                extMapBusiness.ex1RouteEndAreaLayer?.updateRouteEndParentPoint(endPoi, rectInt)
                        } else {
                            mRouteEndAreaLayer?.updateRouteEndAreaAndParentPoint(data.poiList[0], endPoi, rectInt)
                            if (BaseConstant.MULTI_MAP_VIEW)
                                extMapBusiness.ex1RouteEndAreaLayer?.updateRouteEndAreaAndParentPoint(
                                    data.poiList[0], endPoi,
                                    rectInt
                                )

                        }
                    }
                }

                Status.ERROR -> {
                    Timber.i("startSearchEndArea ERROR = ${result.throwable.toString()}")
//                    if (!isNavigating()) {
//                        return@launch
//                    }
                    mRouteEndAreaLayer?.updateRouteEndParentPoint(endPoi, rectInt)
                    if (BaseConstant.MULTI_MAP_VIEW)
                        extMapBusiness.ex1RouteEndAreaLayer?.updateRouteEndParentPoint(endPoi, rectInt)
                }

                else -> {}
            }
        }
    }

    /**
     * 控制鹰眼图or光柱图显示隐藏
     */
    fun setEagleVisible(visible: Boolean = true, isTrue: Boolean = true) {
        Timber.i("setEagleVisible visible = $visible ; getOverviewRoads = ${getOverviewRoads()} ; isTrue = $isTrue")
        //本项目沉浸态和触碰态都显示所有 visible不使用，使用isTrue 默认是true
        val isEagleEye = getOverviewRoads() == 0
        val isSimpleTmc = getOverviewRoads() == 2
        mNaviComponent.setEagleEyeVisible(if (isSimpleTmc) false else if (isEagleEye) isTrue else false)
        _eagleViewVisible.postValue(if (isSimpleTmc) false else if (isEagleEye) isTrue else false)
        _tmcBarVisible.postValue(if (isSimpleTmc) false else if (isEagleEye) false else if (mNetWorkManager.isNetworkConnected()) isTrue else false)
        _simpleTmcBarVisible.postValue(isSimpleTmc)
    }

    /**
     * 自动比例尺
     * 0 关闭 1开启
     */
    fun getAutoScale(): Int = settingComponent.getAutoScale()

    /**
     * 初始化设置，根据用户设置显示光柱图或鹰眼图
     * 0 小地图  1 光柱图 2 极简
     */
    fun getOverviewRoads(): Int = settingComponent.getOverviewRoads()

    /**
     * 添加途经点
     */
    fun addWayPoint(poi: POI?) {
        Timber.i("addWayPoint is called")
        val carRouteResult = mRouteRequestController.carRouteResult
        carRouteResult?.let {
            if (it.midPois != null && it.midPois.size == 15) {
                setToastTip(application.getString(R.string.sv_route_result_addmid_has_15))
                return
            }
            //添加途经点进行算路
            var midPois = it.midPois
            if (midPois == null) {
                midPois = arrayListOf<POI>()
            }
            midPois.add(poi)
            isShowAddPointToast = true
            lastMidpoi = poi
            //请求路线
            planRoute(it.fromPOI, it.toPOI, midPois, BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD_MISPOI)
        }

    }

    /**
     * 删除途经点
     */
    fun deleteViaPoi(index: Int?) {
        Timber.i("deleteViaPoi is called index = $index")
        val carRouteResult = mRouteRequestController.carRouteResult
        lastMidpoiList.clear()
        carRouteResult?.let {
            //null 删除所有途经点
            if (index == null) {
                if (null != it.midPois && it.midPois.size > 0) {
                    isShowDeletePointToast = true
                    lastMidpoiList.addAll(it.midPois)
                    it.midPois.clear()
                    //重新开始路径规划
                    restartPlanRoute(type = BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD_MISPOI)
                }
            } else {
                if (null != it.midPois && index < it.midPois.size) {
                    isShowDeletePointToast = true
                    lastMidpoiList.addAll(it.midPois)
                    it.midPois.removeAt(index)
                    //重新开始路径规划
                    restartPlanRoute(type = BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD_MISPOI)
                }
            }

        }
    }

    /**
     * 路线规划失败或取消时恢复途经点数据
     */
    fun recoverViaPois() {
        Timber.i("recoverViaPois is called")
        if (isShowAddPointToast) {
            if (lastMidpoi != null) {
                val carRouteResult = mRouteRequestController.carRouteResult
                carRouteResult?.let {
                    it.midPois.remove(lastMidpoi)
                }
                lastMidpoi = null
            }
            isShowAddPointToast = false
        }
        if (isShowDeletePointToast) {
            if (lastMidpoiList.isNotEmpty()) {
                val carRouteResult = mRouteRequestController.carRouteResult
                carRouteResult?.let {
                    val midpoiList: ArrayList<POI> = arrayListOf()
                    midpoiList.addAll(lastMidpoiList)
                    it.midPois = midpoiList
                }
            }
            isShowDeletePointToast = false
        }
    }

    /**
     * 重新开始路径规划
     */
    fun restartPlanRoute(isNaviSettingRestart: Boolean = false, type: Int = BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD) {
        Timber.i("restartPlanRoute is called isNaviSettingRestart = $isNaviSettingRestart")
        this.isNaviSettingRestart = isNaviSettingRestart
        val carRouteResult = mRouteRequestController.carRouteResult
        //获取当前车位为起点
        val location = SDKManager.getInstance().locController.lastLocation
        val startPoi = POIFactory.createPOI("我的位置", GeoPoint(location.longitude, location.latitude))
        carRouteResult?.let {
            val to = it.toPOI
            val midPois = it.midPois
            //请求路线
            planRoute(startPoi, to, midPois, type)
        }
    }


    /**
     * 开始路径规划
     * startPoi：起点POI
     * endPoi：终点POI
     * midPois：途经点POI列表
     * type：请求type
     */
    fun planRoute(
        startPoi: POI,
        endPoi: POI,
        midPois: ArrayList<POI>?,
        type: Int
    ): Long {
        abortRequestTaskId()
        isRequestRoute = true
        commandRequestType = type
        _loadingRoute.postValue(true)
        _loadingView.postValue(true)
        _naviErrorMessage.postValue("")
        //请求路线
        AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_START)
        SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CALCULATE_ROUTE_START)
        requestRouteTaskId = routeRepository.planRoute(startPoi, endPoi, midPois, routeResultCallBack)
        Timber.i("planRoute requestId = $requestRouteTaskId")
        return requestRouteTaskId
    }

    /**
     * 更改路线偏好后重新算路
     * mPlanPref：路线偏好
     */
    fun onReRouteFromPlanPref(mPlanPref: String?) {
        Timber.i("onReRouteFromPlanPref is called  mPlanPref= $mPlanPref")
        val carRouteResult = mRouteRequestController.carRouteResult
        if (carRouteResult != null) {
            if (!mNetWorkManager.isNetworkConnected()) {
                setToastTip(application.getString(R.string.sv_common_network_anomaly_please_try_again))
                return
            }
            restartPlanRoute()
        }
    }

    /**
     * 更改行程点重新规划路线，支持终点、途经点
     */
    open fun onChangeDestination(poi: POI, @SceneSendType.SceneSendType1 sendType: Int, planPref: String?) {
        Timber.i("onChangeDestination is called sendType = $sendType , poi = ${gson.toJson(poi)}")
        val carRouteResult = mRouteRequestController.carRouteResult ?: return
        if (!mNetWorkManager.isNetworkConnected()) {
            setToastTip(application.getString(R.string.sv_common_network_anomaly_please_try_again))
            return
        }
        val startPoi = carRouteResult.fromPOI
        when (sendType) {
            SceneSendType.SceneTypeChangeDestination -> {
                linkCarState = AutoState.LINK_CAR_REROUTE_SUCCESS
                planRoute(startPoi, poi, carRouteResult.midPois, BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD)
            }

            SceneSendType.SceneTypeAddPathPoint -> {
                val midPOIs: ArrayList<POI> = carRouteResult.midPois ?: ArrayList()
                if (midPOIs.none { it.id == poi.id || it.point == poi.point }) {
                    midPOIs.add(poi)
                }
                linkCarState = AutoState.LINK_CAR_ADD_MID_SUCCESS
                planRoute(startPoi, carRouteResult.toPOI, midPOIs, BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD)
            }

            SceneSendType.SceneTypeDelPathPoint -> {
                val midPOIs: ArrayList<POI> = carRouteResult.midPois ?: ArrayList()
                midPOIs.removeAll { it.id == poi.id || it.point == poi.point }
                linkCarState = AutoState.LINK_CAR_DELETE_MID_SUCCESS
                planRoute(startPoi, carRouteResult.toPOI, midPOIs, BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD)
            }
        }
    }

    /**
     * 手车互联 开始路径规划
     * pushMsg：手机传递过来的信息
     * type：请求type
     */
    fun planAimRoutePushMsgRoute(
        pushMsg: AimRoutePushMsg?,
        type: Int
    ): Long {
        abortRequestTaskId()
        isRequestRoute = true
        commandRequestType = type
        traceId = pushMsg?.traceId
        _loadingRoute.postValue(true)
        _loadingView.postValue(true)
        _naviErrorMessage.postValue("")
        //请求路线
        requestRouteTaskId = routeRepository.planAimRoutePushMsgRoute(pushMsg, routeResultCallBack)
        Timber.i("planAimRoutePushMsgRoute requestId = $requestRouteTaskId")
        return requestRouteTaskId
    }

    /**
     * 取消请求
     */
    fun abortRequestTaskId() {
        if (requestRouteTaskId.toInt() != 0) {
            try {
                Timber.i("abortRequestTaskId requestId = $requestRouteTaskId")
                mRouteRequestController.abortRequest(requestRouteTaskId)
                requestRouteTaskId = 0
            } catch (e: Exception) {
                Timber.e(" cancleRoute e: ${e.message}")
            }
        }
    }

    /**
     * Toast信息
     */
    fun setToastTip(tip: String) = setToast.postValue(tip)


    //===================================更新导航卡片信息  start============================================================
    //距离下一个路口距离
    private val _topDistance = MutableLiveData<String>()
    val topDistance: LiveData<String> = _topDistance


    //距离下一个路口距离int 米
    private val _topDistanceInt = MutableLiveData(0)
    val topDistanceInt: LiveData<Int> = _topDistanceInt

    //距离下一个路口距离 单位
    private val _topDistanceUnit = MutableLiveData<String>()
    val topDistanceUnit: LiveData<String> = _topDistanceUnit

    //距离下一个路口距离 提示
    private val _topDistanceTip = MutableLiveData<String>()
    val topDistanceTip: LiveData<String> = _topDistanceTip

    //距离下一个路口名称
    private val _topRoadName = MutableLiveData<String>()
    val topRoadName: LiveData<String> = _topRoadName

    //显示到下一条道路的路口大图的距离
    private val _crossImageProgress = MutableLiveData(0)
    val crossImageProgress: LiveData<Int> = _crossImageProgress

    //剩余时间和距离
    private val _remainTimeAndDistance = MutableLiveData<String>()
    val remainTimeAndDistance: LiveData<String> = _remainTimeAndDistance

    //到达时间
    private val _arriveTime = MutableLiveData<String>()
    val arriveTime: LiveData<String> = _arriveTime

    //显示路口放大图
    private val _showCrossView = MutableLiveData<Boolean>(false)
    val showCrossView: LiveData<Boolean> = _showCrossView

    //更新(接近)进阶动作信息显示隐藏通知
    private val _nearThumInfoVisibility = MutableLiveData<Boolean>(false)
    val nearThumInfoVisibility: LiveData<Boolean> = _nearThumInfoVisibility

    //是否显示TBT卡片
    private val _tbtVisible = MutableLiveData<Boolean>(false)
    val tbtVisible: LiveData<Boolean> = _tbtVisible

    //更新(接近)进阶动作 路口名称
    private val _nearRoadName = MutableLiveData<String>()
    val nearRoadName: LiveData<String> = _nearRoadName

    //步行最后一公里View显示隐藏通知
    private val _naviSendToPhoneLd = MutableLiveData<Boolean>(false)
    val naviSendToPhoneLd: LiveData<Boolean> = _naviSendToPhoneLd

    //转向动作图标
    private val _turnIcon = MutableLiveData<Bitmap?>()
    val turnIcon: LiveData<Bitmap?> = _turnIcon

    //进阶动作图标 ManeuverID
    private val _turnManeuverID = MutableLiveData<Int>()
    val turnManeuverID: LiveData<Int> = _turnManeuverID

    //更新(接近)进阶动作图标
    private val _nearThumTurnIcon = MutableLiveData<Bitmap?>()
    val nearThumTurnIcon: LiveData<Bitmap?> = _nearThumTurnIcon

    //更新(接近)进阶动作图标 ManeuverID
    private val _nearThumTurnManeuverID = MutableLiveData<Int>()
    val nearThumTurnManeuverID: LiveData<Int> = _nearThumTurnManeuverID

    //城市信息
    private val _cityInfo = MutableLiveData<CityItemInfo?>()
    val cityInfo: LiveData<CityItemInfo?> = _cityInfo

    private val _cityVisibility = MutableLiveData<Boolean>(false)
    val cityVisibility: LiveData<Boolean> = _cityVisibility

    //服务区信息显示隐藏状态
    private val _sapaViewVisibility = MutableLiveData<Boolean>(false)
    val sapaViewVisibility: LiveData<Boolean> = _sapaViewVisibility

    //服务区信息
    private val _sapaInfoList = MutableLiveData<ArrayList<NaviFacility>?>()
    val sapaInfoList: LiveData<ArrayList<NaviFacility>?> = _sapaInfoList

    //限行信息显示隐藏状态
    private val _forbiddenInfoVisibility = MutableLiveData<Boolean>(false)
    val forbiddenInfoVisibility: LiveData<Boolean> = _forbiddenInfoVisibility

    //天气信息显示隐藏状态
    private val _weatherTipVisibility = MutableLiveData<Boolean>(false)
    val weatherTipVisibility: LiveData<Boolean> = _weatherTipVisibility


    //如果已经发送一次最后一公里的功能
    private var hasSendToPhone = false

    protected var mNextThumRoundNum = 0

    //近阶动作数
    protected var mRoundNum = 0

    private var mTimer: CustomTimer? = null
    private var mTimerTask: CustomTimerTask? = null

    /**
     * 重置导航卡片信息
     */
    fun resetNaviCardData() {
        Timber.i(" resetNaviCardData is called ")
        isFirstGoToNavi = false
        cancelNaviScope()
        setToastTip("")
        _finishFragment.postValue(false)
        _tbtVisible.postValue(false)
        _topDistance.postValue("")
        _topDistanceInt.postValue(0)
        _topDistanceUnit.postValue("")
        _topDistanceTip.postValue("")
        _topRoadName.postValue("")
        _crossImageProgress.postValue(0)
        _remainTimeAndDistance.postValue("")
        _arriveTime.postValue("")
        _showCrossView.postValue(false)
        _nearThumInfoVisibility.postValue(false)
        _nearRoadName.postValue("")
        _naviSendToPhoneLd.postValue(false)
        _turnIcon.postValue(null)
        _turnManeuverID.postValue(-1)
        _nearThumTurnIcon.postValue(null)
        _nearThumTurnManeuverID.postValue(-1)
        _cityInfo.postValue(null)
        _cityVisibility.postValue(false)
        _sapaViewVisibility.postValue(false)
        _sapaInfoList.postValue(null)
        _forbiddenInfoVisibility.postValue(false)
        _weatherTipVisibility.postValue(false)
        _limitSpeedVisible.postValue(false)
        _mCurrentRoadSpeed.postValue(0)
        _roadSpeedVisible.postValue(false)
        _naviGreenWaveCarSpeedVisible.postValue(false)
        _naviGreenWaveCarSpeed.postValue(NaviGreenWaveCarSpeed())
        _limitSpeed.postValue("")
        _averageSpeed.postValue("")
        _remainDist.postValue("")
        hideSendToPhone()
        hideCrossView(mRoadCrossType)
        _tmcModelInfo.postValue(TmcModelInfoBean())
        _forbiddenInfo.postValue(TrafficEventInfoBean())
        mCurNaviInfo = null
        _naviInfo.postValue(null)
        _naviErrorMessage.postValue("")
//        _loadingView.postValue(false)
        _loadingRoute.postValue(false)
        isLessThran500Delete = false
        _naviLaneVisible.postValue(false)
        isTollBoothsLane = false
        _exitDirectionInfoVisible.postValue(false)
        _naviLaneList.postValue(ArrayList())
        _tollLaneList.postValue(ArrayList())
        commandRequestType = 0
        _showGetSAPAInfoDialogVisible.postValue(false)
        setServiceAreaInfoVisible(false)
        isNaviSettingRestart = false
        isShowAddPointToast = false
        isShowDeletePointToast = false
        _serviceAreaInfo.postValue(null)
        activeShutdownSAPATip = false
        hasSendToPhone = false
        mMapBusiness.showMapControlButtons(true)
        _naviCameraList.postValue(arrayListOf())
        setParkingRecommendVisible(false)
        setParkingPosition(0)
        showParkingCardDetail(false)
        _routeChangedNotice.postValue(false)
        _routeYawNotice.postValue(false)
        curSpeed = 0f
        _locMatchInfo.postValue(null)
        _exitDirectionInfoAll.postValue(null)
        _naviSpeed.postValue("0")
        _exitTip.postValue("")
        _exitDirectionInfo.postValue("")
        hideSendToPhoneTimer.cancel()
        removeBackToCarTimer()
        removeForbiddenInfoTimer()
        mRoundNum = 0
        pathID = 0
        mTotalDistance = 0
        mIsAlreadySearchParking = false
        mRerouteOption = null
        _parallelRoadVisible.postValue(false)
        _parallelBridgeVisible.postValue(false)
        mParallelRoadState = 0
        mParallelBridgeState = 0
        _parallelRoadState.postValue(0)
        _parallelBridgeState.postValue(0)
        _parallelRoadStatus.postValue(0)
        cancelArriveTime()
    }

    //关闭步行最后一公里View倒计时
    private val hideSendToPhoneTimer = object : CountDownTimer(BaseConstant.CCP_COUNT_DOWN_TOTAL_TIME, 10000) {
        override fun onTick(millisUntilFinished: Long) = Unit
        override fun onFinish() {
            Timber.i(" hideSendToPhoneTimer onFinish is called ")
            hideSendToPhone()
        }
    }

    //显示路口放大图
    fun showCrossView(crossImageType: Int) {
        Timber.i("showCrossView is called crossImageType=$crossImageType")
        _crossImageProgress.postValue(0)
        // 需要主动触发显示（2D矢量路口大图第一次显示的时候默认隐藏）
        mDrivingLayer?.setRoadCrossVisible(crossImageType, true)
        _sapaViewVisibility.postValue(false)
        _forbiddenInfoVisibility.postValue(false)
        _weatherTipVisibility.postValue(false)
        _showCrossView.postValue(true)
    }

    //隐藏路口放大图
    fun hideCrossView() {
        Timber.i("hideCrossView is called")
        hideCrossView(mRoadCrossType)
    }

    //隐藏路口放大图
    fun hideCrossView(crossImageType: Int) {
        Timber.i("hideCrossView is called crossImageType=$crossImageType")
        isShowCrossImage = false
        _showCrossView.postValue(false)
        mDrivingLayer?.hideCross(crossImageType)
        mEx1DrivingLayer?.hideCross(crossImageType)
    }

    /**
     * 导航过程中信息处理
     */
    fun updateNaviInfo(naviInfo: NaviInfo?) {
        Timber.i("updateNaviInfo is called")
        if (naviInfo == null) {
            return
        }
        naviInfo.curRouteName?.let {
            _mRoadName.postValue(it)
        } ?: _mRoadName.postValue(application.getString(R.string.sv_navi_no_name_road))

        //这里是请求转向图标
        onUpdateDirectionInfo(naviInfo)
        // 更进阶 下个路口信息
        if (NavigationUtil.hasNextThumTip(naviInfo)) {
            val nextCrossInfo = naviInfo.nextCrossInfo[0]
            mNextThumRoundNum = nextCrossInfo.outCnt.toInt()
            val mNextThumManeuverInfo = ManeuverInfo()
            mNextThumManeuverInfo.maneuverID = nextCrossInfo.crossManeuverID.toLong()
            mNextThumManeuverInfo.segmentIndex = nextCrossInfo.segIdx.toLong()
            mNextThumManeuverInfo.pathID = naviInfo.pathID
            requestNextThumTurnIcon(mNextThumManeuverInfo)
        }
        naviScopeIo.launch {
            if (mUserBusiness.isLogin()) {
                //如果是最后一公里那么提示是否发送
                startSearchLastOneMile(naviInfo.routeRemain.dist)
            }
            //距离下一个路口距离 单位 和名称
            updateRoadRemainDistanceRoadName(naviInfo)
            //显示到下一条道路的路口大图的距离
            updateCrossImageProgress(naviInfo)
            //到达时间和距离
            updateRemainTimeAndDistance(naviInfo)
            //更新(接近)进阶动作信息
            updataNearThumInfo(naviInfo)
            //获取城市信息
            startGetCityItemInfo(naviInfo)
            //设置分享路线二维码数据
            setTripShareData(naviInfo)
        }
        //发起终点周边搜索 是否弹出停车场推荐  真实导航才搜索终点停车场
        if (mNaviType == NaviType.NaviTypeGPS) {
            startSearchEndRecommend(naviInfo.routeRemain.dist)
        }
        Timber.i("_tbtVisible is called")
        _tbtVisible.postValue(true)
    }

    /**
     * 设置分享路线二维码数据
     */
    private fun setTripShareData(naviInfo: NaviInfo) {
        mTripShareBusiness.setCurDrivingRouteDist(naviInfo.driveDist)
        mTripShareBusiness.setCurDrivingRouteTime(naviInfo.driveTime)
        mTripShareBusiness.setCurRouteRemainDist(naviInfo.routeRemain.dist)
        mTripShareBusiness.setCurRouteRemainTime(naviInfo.routeRemain.time)
        mTripShareBusiness.setSpeed(curSpeed)
    }


    /**
     * 如果是最后一公里那么提示是否发送
     */
    private fun startSearchLastOneMile(dist: Int) {

        if (mNaviType != NaviType.NaviTypeGPS) {
            Timber.i("startSearchLastOneMile no Real Navi, so return")
            return
        }
        if (hasSendToPhone) {
            Timber.i("startSearchLastOneMile last mile have send phone")
            return
        }
        if (!settingComponent.getKeyWalk()) {
            Timber.i("startSearchLastOneMile:mSettingComponent.getKeyWalk()=false")
            return
        }
        if (dist < 1000) {
            hasSendToPhone = true
            _naviSendToPhoneLd.postValue(true)
            hideSendToPhoneTimer.cancel()
            hideSendToPhoneTimer.start()
        }
    }

    /**
     * 隐藏步行最后一公里View
     */
    fun hideSendToPhone() {
        Timber.i("hideSendToPhone is called")
        hideSendToPhoneTimer.cancel()
        _naviSendToPhoneLd.postValue(false)
    }

    /**
     * 距离下一个路口距离 单位 和名称
     */
    private fun updateRoadRemainDistanceRoadName(naviInfo: NaviInfo) {
        Timber.i("updateRoadRemainDistance is called")
        //距离下一个路的距离
        val nDistanceNextRoad = naviInfo.NaviInfoData[naviInfo.NaviInfoFlag].segmentRemain.dist
        val mTVNextRoadNameStr = naviInfo.NaviInfoData[naviInfo.NaviInfoFlag].nextRouteName
        if (nDistanceNextRoad >= 0) {
            //得到数组两位  数组0 是距离  数组1 是 米 公里
            val mDistanceStrArray: Array<String> = NavigationUtil.formatDistanceArray(nDistanceNextRoad)
            _topDistance.postValue(mDistanceStrArray[0])
            _topDistanceInt.postValue(nDistanceNextRoad)
            _topDistanceUnit.postValue(mDistanceStrArray[1])
            _topRoadName.postValue(mTVNextRoadNameStr)
            _topDistanceTip.postValue("进入")
        }
    }

    /**
     * 显示到下一条道路的路口大图的距离
     */
    private fun updateCrossImageProgress(naviInfo: NaviInfo) {
        val mCurRoadclass = naviInfo.curRoadClass
        val nDistanceNextRoad = naviInfo.NaviInfoData[naviInfo.NaviInfoFlag].segmentRemain.dist
        if (nDistanceNextRoad >= 0) {
            val enlargeStartRemainDistance: Int = NavigationUtil.getCurRoadEnlargeDis(mCurRoadclass, nDistanceNextRoad)
            if (nDistanceNextRoad <= enlargeStartRemainDistance) {
                val progress = (enlargeStartRemainDistance - nDistanceNextRoad) * 100 / enlargeStartRemainDistance
                Timber.i("getEnlargeProgressBar progress:$progress,mEnlargeStartRemainDistance:$enlargeStartRemainDistance,nDistanceNextRoad:$nDistanceNextRoad")
                _crossImageProgress.postValue(progress)
            }
        }
    }

    /**
     * 到达时间和距离
     */
    private fun updateRemainTimeAndDistance(naviInfo: NaviInfo) {
        Timber.i("updateRemainTimeAndDistance is called")
        val builder = StringBuilder()
        val etaDistance = NavigationUtil.formatDistanceArray(naviInfo.routeRemain.dist)
        builder.append(etaDistance[0]).append(etaDistance[1]).append(" · ")
            .append(NavigationUtil.switchFromSecond(naviInfo.routeRemain.time))
        _remainTimeAndDistance.postValue(builder.toString())
        //预计到达时间
        val mArriveTime = AutoRouteUtil.getScheduledTime(application, naviInfo.routeRemain.time.toLong(), true)
        _arriveTime.postValue(mArriveTime)
    }

    /**
     * 开始1分钟 更新一次到达时间（防止车辆静止时naviInfo不更新问题）
     */
    private fun updaterArriveTime() {
        Timber.i("updaterArriveTime is called  ")
        cancelArriveTime()
        mTimer ?: run { mTimer = CustomTimer() }
        mTimerTask ?: run {
            mTimerTask = object : CustomTimerTask() {
                override fun run() {
                    try {
                        mCurNaviInfo?.let {
                            //预计到达时间
                            val mArriveTime = AutoRouteUtil.getScheduledTime(application, it.routeRemain.time.toLong(), true)
                            _arriveTime.postValue(mArriveTime)
                            Timber.i("updaterArriveTime mArriveTime = $mArriveTime ")
                        }
                    } catch (e: Exception) {
                        Timber.e("updaterArriveTime: Exception is %s", e.toString())
                    }
                }
            }
        }
        mTimer?.scheduleAtFixedRate(mTimerTask, 0, 60000)
    }

    /**
     * 取消定时器
     */
    private fun cancelArriveTime() {
        mTimer?.let {
            Timber.i("cancelArriveTime is called ")
            it.cancel()
            mTimerTask?.cancel()
            mTimerTask = null
            mTimer = null
        }
        Timber.i("cancelArriveTime mTimer.cancel() ")
    }

    /**
     * 更新(接近)进阶动作信息
     */
    private fun updataNearThumInfo(naviInfo: NaviInfo) {
        Timber.i("updataNearThumInfo is called")
        var crossNaviInfo: CrossNaviInfo? = null
        val isNextThumTip: Boolean = NavigationUtil.isNeedNextThumTip(naviInfo)
        if (!isNextThumTip) {
            //清空进阶动作信息
            Timber.i("cleanNextThumView")
            _nearThumInfoVisibility.postValue(false)
            _nearRoadName.postValue("")
            _nearThumTurnIcon.postValue(null)
            _nearThumTurnManeuverID.postValue(-1)
            return
        }
        crossNaviInfo = naviInfo.nextCrossInfo[0]
        if (null == crossNaviInfo) {
            return
        }
        _nearThumInfoVisibility.postValue(true)
        //是否为隧道内外分叉
        when (crossNaviInfo.tunnelFlag) {
            NavigationUtil.CROSS_NAV_TUNNEL_INNER -> {
                _nearRoadName.postValue("隧道内分叉")
                return
            }

            NavigationUtil.CROSS_NAV_TUNNEL_OUT -> {
                _nearRoadName.postValue("隧道外分叉")
                return
            }
        }
        _nearRoadName.postValue(NavigationUtil.getCrossNavNormalTip(application, crossNaviInfo.curToSegmentDist))
    }

    /**
     * 请求转向图标资源
     *
     * @param naviInfoList 请求转向图标资源
     */
    private fun onUpdateDirectionInfo(naviInfo: NaviInfo) {
        naviScopeIo.launch {
            // 当前标签板的内容
            val naviInfoPanel = naviInfo.NaviInfoData[naviInfo.NaviInfoFlag]
            // 转向ID
            val maneuverID = naviInfoPanel.maneuverID
            if (maneuverID > 0) {
                // 异步请求在线图片
                val maneuverConfig = ManeuverConfig()
                maneuverConfig.width = NaviUiUtil.turnIconSize
                maneuverConfig.height = NaviUiUtil.turnIconSize
                maneuverConfig.backColor = ResUtil.getColor(com.autosdk.R.color.auto_ui_direction_back_color)
                maneuverConfig.roadColor = ResUtil.getColor(com.autosdk.R.color.auto_ui_direction_road_color)
                maneuverConfig.arrowColor = ResUtil.getColor(com.autosdk.R.color.auto_ui_direction_arrow_color)
                maneuverConfig.pathID = naviInfo.pathID
                maneuverConfig.segmentIdx = naviInfo.curSegIdx.toLong()
                maneuverConfig.maneuverID = maneuverID.toLong()
                mRoundNum = naviInfo.ringOutCnt
                //发起请求
                mNaviController.obtainManeuverIconData(maneuverConfig)
            }
        }
    }

    /**
     * 请求进阶动作图标资源
     */
    private fun requestNextThumTurnIcon(mNextThumManeuverInfo: ManeuverInfo) {
        naviScopeIo.launch {
            val size = NaviUiUtil.nextTurnIconSize //自定义一个大小用于区分进阶动作和引导卡片图标
            val config = ManeuverConfig()
            config.width = size
            config.height = size
            config.backColor = ResUtil.getColor(com.autosdk.R.color.auto_ui_direction_back_color)
            config.roadColor = ResUtil.getColor(com.autosdk.R.color.auto_ui_direction_road_color)
            config.arrowColor = ResUtil.getColor(com.autosdk.R.color.auto_ui_direction_arrow_color)
            config.maneuverID = mNextThumManeuverInfo.maneuverID
            config.segmentIdx = mNextThumManeuverInfo.segmentIndex
            config.pathID = mNextThumManeuverInfo.pathID
            //发起请求
            mNaviController.obtainManeuverIconData(config)
        }
    }

    /**
     * 日夜模式切换 进阶动作 图标icon日夜模式UI
     */
    fun updateTurnIconTheme() {
        if (turnManeuverID.value != null && turnManeuverID.value != -1) {
            val roadSignBitmap = NaviUiUtil.getRoadSignBitmap(null, 0, 0, turnManeuverID.value!!, mRoundNum, false)
            _turnIcon.postValue(roadSignBitmap)
        }
    }

    /**
     * 进阶动作 图标icon
     */
    private fun updateTurnIcon(bytes: ByteArray?, config: ManeuverConfig?, aroundNum: Int) {
        //转向图标的更新
        if (null != config) {
            _turnManeuverID.postValue(config.maneuverID.toInt())
            val roadSignBitmap = NaviUiUtil.getRoadSignBitmap(
                bytes,
                config.width,
                config.height,
                config.maneuverID.toInt(),
                aroundNum,
                false
            )
            _turnIcon.postValue(roadSignBitmap)
        } else {
            Timber.i("updateTurnIcon config == null")
        }
    }

    /**
     * 日夜模式切换 更新(接近)进阶动作 日夜模式UI
     */
    fun updateNextThumTurnTheme() {
        if (nearThumTurnManeuverID.value != null && nearThumTurnManeuverID.value != -1) {
            val roadSignBitmap =
                NaviUiUtil.getRoadSignBitmap(null, 0, 0, nearThumTurnManeuverID.value!!, mNextThumRoundNum, true)
            _nearThumTurnIcon.postValue(roadSignBitmap)
        }
    }

    /**
     * 更新(接近)进阶动作 图标icon
     */
    private fun updateNextThumTurnIcon(bytes: ByteArray?, config: ManeuverConfig, aroundNum: Int) {
        val size = NaviUiUtil.nextTurnIconSize
        _nearThumTurnManeuverID.postValue(config.maneuverID.toInt())
        val roadSignBitmap = NaviUiUtil.getRoadSignBitmap(bytes, size, size, config.maneuverID.toInt(), aroundNum, true)
        _nearThumTurnIcon.postValue(roadSignBitmap)
    }

    /**
     *  开始获取城市信息
     */
    private fun startGetCityItemInfo(naviInfo: NaviInfo) {
        val currentCityItemInfo = mMapDataBusiness.getCityInfo(naviInfo.cityCode)
        currentCityItemInfo?.let {
            _cityInfo.postValue(it)
            _cityVisibility.postValue(true)
        }
    }

    /**
     * 显示出口信息
     */
    private fun updateExitDirectionInfo(exitDirectionInfo: ExitDirectionInfo) {
        naviScopeIo.launch {
            // 编号
            val exitNames = exitDirectionInfo.exitNameInfo
            // 路牌方向名字数组
            val directions = exitDirectionInfo.directionInfo
            val outting = exitDirectionInfo.entranceExit.takeIf { it.isNotEmpty() } ?: ResUtil.getString(R.string.sv_navi_entrance)
            Timber.i("xjj exitDirectionInfo.exitDirectionInfo = ${gson.toJson(exitDirectionInfo)}")
            val exitBuilder = StringBuilder()
            if (exitNames?.size == 1 && exitNames[0].length <= 2 && exitNames[0].isNotEmpty()) {
                // 只有一个出口编号时，出口编号小于等于5个字符时,展示出口编号以及出口字样
                exitBuilder.append(outting).append(exitNames[0])
            } else {
                exitBuilder.append(outting)
            }
            _exitTip.postValue(exitBuilder.toString())
            //显示路牌信息
            val exitDirectionBuilder = StringBuilder()
            if (!directions.isNullOrEmpty()) {
                for (direction in exitDirectionInfo.directionInfo) {
                    if (!TextUtils.isEmpty(direction)) {
                        exitDirectionBuilder.append(direction)
                        exitDirectionBuilder.append("  ")
                    }
                }
                _exitDirectionInfo.postValue(exitDirectionBuilder.toString())
            }
            _exitDirectionInfoAll.postValue(exitDirectionInfo)
        }
    }
//===================================更新导航卡片信息  end============================================================

    /**
     *发送目的地到高德手机APP下车后继续导航
     */
    fun sendToPhone() {
        //发送aos请求
        val poiMsg = GAimpoiMsg()

        poiMsg.lon = mRouteRequestController.carRouteResult.toPOI.point.longitude
        poiMsg.lat = mRouteRequestController.carRouteResult.toPOI.point.latitude
        poiMsg.name = mRouteRequestController.carRouteResult.toPOI.name // 目的地名称
        poiMsg.address = mRouteRequestController.carRouteResult.toPOI.addr // 目的地地址
        val param = GSendToPhoneRequestParam()
        param.sourceId = "autocpp"
        param.bizType = "aimpoi"
        param.isReliable = true
        param.expiration = 1800
        param.aimpoiMsg = poiMsg
        mUserBusiness.sendReqSendToPhone(param) { rsp: GSendToPhoneResponseParam ->
            Timber.i("sendToPhone rsp = ${gson.toJson(rsp)}")
            if (rsp.code == 1) {
                setToastTip("发送成功")
            } else if (rsp.mNetworkStatus == ENETWORKSTATUS.NETWORKSTATUS_FAILED) {
                setToastTip("网络异常，发送失败")
            } else {
                setToastTip("发送失败")
            }
            hideSendToPhone()
        }
    }

    /**
     * 如果距离少于500的时候删除路线
     */
    private fun onDeletePathLessThan(dist: Int) {
//        Timber.i("onDeletePathLessThan is called dist=$dist")
        naviScopeIo.launch {
            if (isLessThran500Delete) {
                return@launch
            }
            if (dist < 500) {
                isLessThran500Delete = true
                mLastRouteUtils.deleteRouteFile()
            }
        }
    }

    /**
     * 车道线数据处理
     */
    private fun showLaneInfo(laneInfo: LaneInfo) {
        Timber.d("showLaneInfo is called backLaneType =  ${laneInfo.backLaneType}")
        val size = laneInfo.backLane.size
        val laneInfos = ArrayList<TBTLaneInfoBean>(size)
        val sb = java.lang.StringBuilder(96)
        for (i in 0 until size) {
            var mTBTLaneInfoBean = TBTLaneInfoBean()
            mTBTLaneInfoBean.isTollBoothsLane = false
            var item = laneInfo.frontLane[i]
            sb.append(item).append(",")
            item = item or (laneInfo.frontExtenLane[i] shl 8)
            sb.append(laneInfo.frontExtenLane[i]).append(",")
            item = item or (laneInfo.backLane[i] shl 16)
            sb.append(laneInfo.backLane[i]).append(",")
            item = item or (laneInfo.backExtenLane[i] shl 24)
            sb.append(laneInfo.backExtenLane[i]).append("\n")
            // 设置推荐车道
            mTBTLaneInfoBean.isRecommend = laneInfo.optimalLane[i] != LaneAction.LaneActionNULL

            // 设置分时车道
            mTBTLaneInfoBean.laneAction = if (LaneCategoryType.LaneTypeBus == laneInfo.backLaneType[i]) {
                if (laneInfo.frontLane[i] == 0xFF) 0x001500ff else 0x00150015
            } else if (LaneCategoryType.LaneTypeOther == laneInfo.backLaneType[i]) {
                if (laneInfo.frontLane[i] == 0xFF) 0x001800ff else 0x00180018
            } else if (LaneCategoryType.LaneTypeTidal == laneInfo.backLaneType[i]) {
                if (laneInfo.frontLane[i] == 0xFF) 0x001900ff else 0x00190019
            } else if (LaneCategoryType.LaneTypeVariable == laneInfo.backLaneType[i]) {
                if (laneInfo.frontLane[i] == 0xFF) 0x001700ff else 0x00170017
            } else {
                //普通车道
                item
            }
            laneInfos.add(mTBTLaneInfoBean)
        }
        if (size > 0) {
            _naviLaneVisible.postValue(true)
            _naviLaneList.postValue(laneInfos)
        }
    }

    /**
     * 配置分享路线二维码数据
     */
    private fun updateNaviPointInfo() {
        Timber.i("updateNaviPointInfo is called")
        mTripShareBusiness.setNaving(true)
        mTripShareBusiness.initTripShareParam()
    }

    /**
     * 更新服务区信息
     */
    private fun updateRestInfo(infoArray: ArrayList<NaviFacility>?) {
        Timber.i("updateRestInfo is called")
        try {
            if (infoArray == null || infoArray.size == 0) {
                _sapaViewVisibility.postValue(false)
                _sapaInfoList.postValue(null)
            } else if (showCrossView.value == false) {
                Timber.i("updateRestInfo name:${infoArray[0].name} remainDist:${infoArray[0].remainDist}")
                _sapaViewVisibility.postValue(true)
                _sapaInfoList.postValue(infoArray)
            } else {
                Timber.i("updateRestInfo showCrossView is true 有路口大图不显示服务区信息")
                _sapaViewVisibility.postValue(false)
            }
            _weatherTipVisibility.postValue(false)
        } catch (e: Exception) {
            Timber.e(" updateRestInfo e:${e.message}");
        }
    }

    /**
     * 动态比例尺
     */
    fun setAutoZoom(autoZoom: Boolean) {
        mDrivingLayer?.openDynamicLevel(autoZoom, DynamicLevelType.DynamicLevelGuide)
    }

    //模拟导航后车标位置会被改变为模拟导航最后位置，需要将车标改为真实位置
    private fun setCarPosition() {
        val carLoc = CarLoc()
        val info = PathMatchInfo()
        val location = LocationController.getInstance().lastLocation
        info.longitude = location.longitude
        info.latitude = location.latitude
        info.carDir = location.bearing
        carLoc.vecPathMatchInfo.add(info)
        mDrivingLayer?.setCarPosition(carLoc)
        if (BaseConstant.MULTI_MAP_VIEW)
            mEx1DrivingLayer?.setCarPosition(carLoc)
    }

    fun setSimSpeed(speed: Int) {
        //模拟导航配置参数
        val mEmulatorParam = EmulatorParam()
        mEmulatorParam.speed = speed
        val param = Param()
        param.type = Type.GuideParamEmulator
        param.emulator = mEmulatorParam
        mNaviController.setGuideParam(param)
    }

    /**
     * 恢复导航
     */
    fun resumeNavi() {
        Timber.i("resumeNavi is called")
        mNaviController.resumeNavi()
    }

    /**
     * 暂停导航
     */
    fun pauseNavi() {
        Timber.i("pauseNavi is called")
        mNaviController.pauseNavi()
    }

    /**
     * 检查电量不足提示
     */
    fun checkBatteryLowTips() {
        Timber.i("checkBatteryLowTips is called")
        mRouteRequestController.carRouteResult?.pathResult?.let { pathResult ->
            val focusIndex = mRouteRequestController.carRouteResult.focusIndex
            if (focusIndex in pathResult.indices) {
                val pathInfo = pathResult[focusIndex]
                pathInfo.elecPathInfo?.mEnergyConsume?.energyEndFlag?.let {
                    _showBatteryLowCard.postValue(it)
                    Timber.i("_showBatteryLowCard $it")
                }
            } else {
                // 处理 focusIndex 超出范围的情况
                Timber.w("checkBatteryLowTips: Focus index out of bounds: $focusIndex, size: ${pathResult.size}")
            }
        } ?: run {
            // 处理 pathResult 为空的情况
            Timber.w("checkBatteryLowTips: Path result is empty")
        }
    }

    fun closeBatteryLowTipsCard() {
        Timber.i("closeBatteryLowTipsCard is called")
        _showBatteryLowCard.postValue(false)
    }

    /**
     * 更新主辅路信息 平行路 桥上桥下
     */
    private fun updateParallelRoad(mLocParallelRoadInfo: LocParallelRoadInfo?) {
        if (mLocParallelRoadInfo == null) {
            Timber.i("updateParallelRoad mLocParallelRoadInfo == null")
            _parallelRoadVisible.postValue(false)
            _parallelBridgeVisible.postValue(false)
            mParallelRoadState = 0
            mParallelBridgeState = 0
            _parallelRoadState.postValue(0)
            _parallelBridgeState.postValue(0)
            _parallelRoadStatus.postValue(0)
            return
        }
        Timber.i("updateParallelRoad is called")
        if ((mParallelRoadState == mLocParallelRoadInfo.flag) && (mParallelBridgeState == mLocParallelRoadInfo.hwFlag)) {
            return
        }

        mParallelRoadState = mLocParallelRoadInfo.flag
        mParallelBridgeState = mLocParallelRoadInfo.hwFlag
        //根据状态显示 主路和辅路的按钮

        val isSwitchingRoad = mLocParallelRoadInfo.status != 1
        if (isSwitchingRoad) {
            Timber.i("updateParallelRoad locParallelRoadInfo.flag=${mLocParallelRoadInfo.flag} __locParallelRoadInfo.hwFlag=${mLocParallelRoadInfo.hwFlag}")
            // 显示 "切到桥下" "切到桥上"按钮，在线才显示
            _parallelBridgeVisible.postValue(mLocParallelRoadInfo.hwFlag != 0)
            if (mLocParallelRoadInfo.hwFlag == 1 || mLocParallelRoadInfo.hwFlag == 2) {
                _parallelBridgeState.postValue(mLocParallelRoadInfo.hwFlag)
            }

            //  显示 "切到主路"  "切到辅路" 按钮，在线才显示
            _parallelRoadVisible.postValue(mLocParallelRoadInfo.flag != 0)
            if (mLocParallelRoadInfo.flag == 1 || mLocParallelRoadInfo.flag == 2) {
                _parallelRoadState.postValue(mLocParallelRoadInfo.flag)
            }
            _parallelRoadStatus.postValue(1)
        } else {
            _parallelRoadVisible.postValue(false)
            _parallelBridgeVisible.postValue(false)
            _parallelRoadStatus.postValue(0)
        }
    }

    /**
     * 点击切换主辅路
     * isMainSide： true=主辅路  false=桥上桥下
     */
    open fun onParallelWayClick(isMainSide: Boolean) {
        Timber.i("onParallelWayClick is called isMainSide:$isMainSide")
        if (isMainSide) {
            if (mLocParallelRoadInfo!!.flag == 1) {
                // 显示 "切到辅路" 按钮
                onParallelWayClick(LocSwitchRoadType.LocSwitchMainToSide, mRoadId!!)
            } else if (mLocParallelRoadInfo!!.flag == 2) {
                // 显示 "切到主路" 按钮
                onParallelWayClick(LocSwitchRoadType.LocSwitchSideToMain, mRoadId!!)
            }
        } else {
            if (mLocParallelRoadInfo!!.hwFlag == 1) {
                // 显示 "切到桥下" 按钮，在线才显示
                onParallelWayClick(LocSwitchRoadType.LocSwitchUpBridgeToDownBridge, mRoadId!!)
            } else if (mLocParallelRoadInfo!!.hwFlag == 2) {
                // 显示 "切到桥上" 按钮，在线才显示
                onParallelWayClick(LocSwitchRoadType.LocSwitchDownBridgeToUpBridge, mRoadId!!)
            }
        }
    }

    /**
     * 主辅路切换
     */
    open fun onParallelWayClick(@LocSwitchRoadType.LocSwitchRoadType1 switchRoadType: Int, roadid: BigInteger) {
        Timber.i("onParallelWayClick mSwitchParallelRoadEnable=$mSwitchParallelRoadEnable")
        mRoadId = roadid
        mSwitchRoadType = switchRoadType
        if (!mSwitchParallelRoadEnable) {
            return
        }
        _loadingRoute.postValue(true)
        mLocationController.switchParallelRoad(switchRoadType, roadid)
        mSwitchParallelRoadEnable = false
        Timber.i("onParallelWayClick execute")
    }

    /**
     * 更新区间限速信息
     */
    private fun updateIntervalCameraDynamicInfo(cameraDynamicList: ArrayList<NaviIntervalCameraDynamicInfo>?) {
        if (showCrossView.value == false) {
//            _forbiddenInfoVisibility.postValue(false)
            _weatherTipVisibility.postValue(false)

            if (cameraDynamicList?.isNotEmpty() == true) {
                var averageSpeed = -1
                var limitSpeed = 0
                var remainDist = 0
                val info = cameraDynamicList[0]
                averageSpeed = info.averageSpeed
                remainDist = info.remainDistance
                // get max limitSpeed
                val size = info.speed.size
                for (i in 0 until size) {
                    val speed = info.speed[i]
                    if (speed in (limitSpeed + 1)..254) {
                        limitSpeed = speed.toInt()
                    }
                }
                Timber.i("updateIntervalCameraDynamicInfo limitSpeed=$limitSpeed，averageSpeed=$averageSpeed，remainDist=$remainDist")
                _limitSpeed.postValue(limitSpeed.toString())//区间限速
                _averageSpeed.postValue(averageSpeed.toString())//区间当前平均速度
                _remainDist.postValue(remainDist.toString())//区间剩余距离
            }
            _limitSpeedVisible.postValue(cameraDynamicList?.isNotEmpty() == true)
        }
    }

    /**
     * 交通事件信息
     */
    fun showForbiddenInfo(address: String, labelDesc: String, mTitle: String) {
        if (TextUtils.isEmpty(labelDesc) && TextUtils.isEmpty(mTitle)) return
        var forbiddenTitle = ""
        var forbiddenContent = ""
        if (TextUtils.isEmpty(labelDesc)) {
            forbiddenTitle = if (TextUtils.isEmpty(mTitle)) {
                application.getString(R.string.poicard_traffic_11070)
            } else {
                mTitle
            }
            forbiddenContent = address
        } else {
            forbiddenTitle = labelDesc
            forbiddenContent = if (TextUtils.isEmpty(address)) {
                mTitle
            } else {
                "$mTitle($address)"
            }
        }
        val trafficEventInfoBean = TrafficEventInfoBean().apply {
            title = forbiddenTitle
            content = forbiddenContent
        }
        _forbiddenInfo.postValue(trafficEventInfoBean)
        _forbiddenInfoVisibility.postValue(true)
        removeForbiddenInfoTimer()
        startForbiddenInfoTimer()
    }

    //关闭交通信息倒计时
    private val forbiddenInfoTimer = object : CountDownTimer(BaseConstant.CCP_COUNT_DOWN_TOTAL_TIME, 10000) {
        override fun onTick(millisUntilFinished: Long) = Unit
        override fun onFinish() {
            Timber.i(" forbiddenInfoTimer onFinish is called ")
            _forbiddenInfoVisibility.postValue(false)
        }
    }

    private fun removeForbiddenInfoTimer() = forbiddenInfoTimer.cancel()

    private fun startForbiddenInfoTimer() = forbiddenInfoTimer.start()

    private val mContinueGuideInfoObserver = object : IContinueGuideInfoObserver {
        override fun continueGuideStartNotify() {
            Timber.i("continueGuideStartNotify")
            naviScopeIo.launch {
                mRouteRequestController.carRouteResult?.run {
                    mLastRouteUtils.saveRouteToFile(this)
                }
                naviSharePreference.putBooleanValue(BaseConstant.KEY_CONTINUE_SAPA_NAVI, true)
            }
        }

        override fun exitContinueGuideNotify() {
            Timber.i("exitContinueGuideNotify")
            naviSharePreference.putBooleanValue(BaseConstant.KEY_CONTINUE_SAPA_NAVI, false)
        }
    }


    /**
     * 设置显示中心
     */
    fun setMapCenter(longitude: Double, latitude: Double) {
        mMapBusiness.setMapCenter(longitude, latitude)
    }

    /**
     * 异步获取高速SAPA信息
     *
     * @return
     * @details 使用者需要主动获取转向图标信息的情况下，通过maneuverConfig获取对应的信息
     * @Param isFindRemainPath
     * true  : 查询剩余路线上的服务区和收费站;
     * false :  只查询当前车辆所在高速路段上的服务区和收费站(要求当前车辆需要在高速上);
     */
    fun obtainSAPAInfo() {
        activeShutdownSAPATip = false
        _showGetSAPAInfoDialogVisible.postValue(true)
        val result = mNaviController.obtainSAPAInfo(true)
        Timber.i("obtainSAPAInfo is called result = $result")
    }

    fun setShowGetSAPAInfoDialogVisible(isShow: Boolean) {
        _showGetSAPAInfoDialogVisible.postValue(isShow)
        activeShutdownSAPATip = true
    }

    /**
     * 开关高速服务区看板
     */
    fun setServiceAreaInfoVisible(isShow: Boolean) {
        _serviceAreaInfoVisible.postValue(isShow)
        removeServiceAreaInfoTimer()
        if (isShow) {
            startServiceAreaInfoTimer()
        }
    }

    //高速服务区看板倒计时
    private val serviceAreaInfoTimer = object : CountDownTimer(BaseConstant.CCP_COUNT_DOWN_TOTAL_TIME, 10000) {
        override fun onTick(millisUntilFinished: Long) = Unit
        override fun onFinish() {
            Timber.i(" serviceAreaInfoTimer onFinish is called ")
            setServiceAreaInfoVisible(false)
        }
    }

    private fun removeServiceAreaInfoTimer() = serviceAreaInfoTimer.cancel()

    private fun startServiceAreaInfoTimer() = serviceAreaInfoTimer.start()

    /**
     * 是否弹出停车场推荐
     */
    private fun startSearchEndRecommend(routeRemainDistance: Int) {
        if (mIsAlreadySearchParking) {
            return
        }
        Timber.i(" startSearchEndRecommend  is called routeRemainDistance = $routeRemainDistance")
        // 全程剩余500米时执行目的地周边停车场搜索
        if (routeRemainDistance > CAR_PARKING_SEARCH_DISTANCE) {
            return
        }
        mIsAlreadySearchParking = true
        val carRouteResult = mRouteRequestController.carRouteResult
        if (carRouteResult?.toPOI == null) {
            return
        }
        val endPoi = carRouteResult.toPOI
        naviScopeIo.launch {
            val result =
                mSearchBusiness.keywordSearchV2(
                    keyword = "停车场",
                    searchPoiBizType = SearchPoiBizType.AROUND,
                    searchQueryType = SearchQueryType.NORMAL,
                    curPoi = endPoi
                )
            when (result.status) {
                Status.SUCCESS -> {
                    Timber.i("startSearchEndRecommend onSuccess")
                    if (!isNavigating()) {
                        return@launch
                    }
                    result.data?.let { data ->
                        val list: List<POI>? =
                            SearchDataConvertUtils.blPoiSearchResultToHmiResult(
                                data,
                                SearchRequestInfo.Builder().build()
                            ).searchInfo.poiResults
                        list?.takeIf { it.isNotEmpty() }?.let {
                            // 处理非空且非空列表的情况
                            _parkingRecommend.postValue(it.take(3))
                            mCustomLayer?.showCustomDestinationParkPoint(data.poiList.take(3))
                            setParkingRecommendVisible(true)
                        }
                    }
                }

                Status.ERROR -> {
                    Timber.i("startSearchEndRecommend ERROR = ${result.throwable.toString()}")
                }

                else -> {}
            }
        }
    }

    /**
     * 自定义图层设置焦点
     */
    fun setCustomLayerItemFocus(type: Int, id: String, isFocus: Boolean) {
        mCustomLayer?.setCustomLayerItemFocus(type, id, isFocus)
    }

    fun showParkingCardDetail(isShowParkingRecommendDetail: Boolean) {
        Timber.i("showParkingCardDetail is called isShowParkingRecommendDetail = $isShowParkingRecommendDetail")
        _showParkingRecommendDetail.postValue(isShowParkingRecommendDetail)
        parkingRecommendVisible.value?.let { parkingRecommendVisible ->
            if (parkingRecommendVisible) {
                hideCrossView()
                parkingRecommend.value?.takeIf { it.isNotEmpty() }?.let {
                    setCustomLayerItemFocus(
                        BizCustomTypePoint.BizCustomTypePoint4,
                        it[parkingPosition.value!!].id,
                        isShowParkingRecommendDetail
                    )
                    if (isShowParkingRecommendDetail) {
                        parkingShowPreview()
                    }
                }
            }
        }

    }

    /**
     * 显示全览
     *
     */
    private fun parkingShowPreview(bAnimation: Boolean = true) {
        Timber.i("parkingRecommend showPreview")
        resetBackToCarTimer(true)   //全览的时候要关闭自动比例尺功能 //关闭全览跟随模式  //设置预览模式
        SearchMapUtil.getParkingBound(parkingRecommend.value)?.let { mapRect ->
            val previewParam = PreviewParam().apply {
                leftOfMap = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1100 else R.dimen.sv_dimen_0
                )
                topOfMap = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_370)
                screenLeft = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1240 else R.dimen.sv_dimen_680
                )
                screenTop = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_200)
                screenRight = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_180)
                screenBottom = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_100)
                bUseRect = true
                mapBound = mapRect
            }
            mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                ?.showPreview(previewParam, bAnimation, 500, -1)
        }
        _InFullView.postValue(true)
    }

    /**
     * 进行终点周边搜  停车场推荐 显隐
     */
    fun setParkingRecommendVisible(isVisible: Boolean) {
        _parkingRecommendVisible.postValue(isVisible)
        showParkingCardDetail(false)
    }

    /**
     * 同步选择了第几个停车场
     */
    fun setParkingPosition(position: Int) {
        _parkingPosition.postValue(position)
        if (isRealNavi()) {
            resetBackToCarTimer(true)
        }
    }

    /**
     * 驾驶行为报告 数据处理
     */
    private fun handleDriveReport(driveReport: DriveReport) {
        driveReport.blNaviStatisticsInfo?.let {
            _naviStatisticsInfo.postValue(it)
        }
        driveReport.driverEventList?.let {
            _driverEventList.postValue(it)
        }
    }

    fun cleanNaviStatisticsInfo() {
        Timber.i("cleanNaviStatisticsInfo is called")
        _naviStatisticsInfo.postValue(null)
        _driverEventList.postValue(null)
    }

    /**
     * 光柱图信息
     *
     * @param tmcItemsInTmcBar 光柱图信息
     * @param totalDistance    获取路线长度
     */
    fun sendTmsLauncher(
        tmcItemsInTmcBar: List<LightBarItem>?,
        totalDistance: Long,
    ): TmcModelInfoBean {
        val lightBarItems: ArrayList<MapLightBarItem> = ArrayList()
        if (tmcItemsInTmcBar != null) {
            var tmcRestDistance = 0L
            for (lightBarItem in tmcItemsInTmcBar) {
                tmcRestDistance += lightBarItem.length.toLong()
                val currentLightBarItem = MapLightBarItem(
                    lightBarItem.status,
                    lightBarItem.length,
                    lightBarItem.timeOfSeconds,
                    lightBarItem.startSegmentIdx,
                    lightBarItem.startLinkIdx,
                    lightBarItem.startLinkStatus,
                    lightBarItem.endSegmentIdx,
                    lightBarItem.endLinkIndex,
                    lightBarItem.endLinkStatus
                )
                lightBarItems.add(currentLightBarItem)
            }
            Timber.i("sendTmsLauncher totalDistance = $totalDistance")
            val tmcModelInfo = TmcModelInfoBean(totalDistance, tmcRestDistance, lightBarItems)
            return tmcModelInfo
        } else {
            val tmcModelInfo = TmcModelInfoBean(0, 0, lightBarItems)
            return tmcModelInfo
        }
    }

    /**
     * 更改导航路径
     *
     * @param pathID 要更改到的路径的ID
     */
    fun changeNaviPath(pathID: Long) {
        Timber.i("changeNaviPath pathID = $pathID")
        mNaviController.setVoiceChangeNaviPath(mRouteRequestController.carRouteResult, pathID)
        _routeChangedNotice.postValue(true)
    }
}

