package com.desaysv.psmap.model.business

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.autonavi.gbl.aosclient.model.GTrafficRestrictRequestParam
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.common.path.model.SubCameraExtType
import com.autonavi.gbl.common.path.option.RouteType
import com.autonavi.gbl.guide.model.NaviCameraExt
import com.autonavi.gbl.guide.model.NaviFacilityType
import com.autonavi.gbl.guide.model.NaviInfo
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.search.model.KeywordSearchResultV2
import com.autonavi.gbl.search.model.SearchEnrouteResult
import com.autonavi.gbl.search.model.SearchEnrouteScene
import com.autonavi.gbl.user.behavior.model.ConfigKey
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.search.request.SearchQueryType
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.bussiness.search.utils.SearchResultUtils
import com.autosdk.bussiness.widget.navi.NaviComponent
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.desaysv.psmap.adapter.standard.ProtocolID
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.business.ActivationMapBusiness
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.def.InitSdkResultType
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.NavigationUtil
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.bean.standard.ALONG_SEARCH_TYPE_ATM
import com.desaysv.psmap.model.bean.standard.ALONG_SEARCH_TYPE_CHARGING_PILE
import com.desaysv.psmap.model.bean.standard.ALONG_SEARCH_TYPE_GAS_STATION
import com.desaysv.psmap.model.bean.standard.ALONG_SEARCH_TYPE_NATURAL_GAS_STATION
import com.desaysv.psmap.model.bean.standard.ALONG_SEARCH_TYPE_REPAIR
import com.desaysv.psmap.model.bean.standard.ALONG_SEARCH_TYPE_RESTAURANT
import com.desaysv.psmap.model.bean.standard.ALONG_SEARCH_TYPE_RESTROOM
import com.desaysv.psmap.model.bean.standard.ALONG_SEARCH_TYPE_SERVICE_AREA
import com.desaysv.psmap.model.bean.standard.AlongWaySearchData
import com.desaysv.psmap.model.bean.standard.AroundSearchData
import com.desaysv.psmap.model.bean.standard.BackToMapHomeOperaData
import com.desaysv.psmap.model.bean.standard.BackToMapOperaData
import com.desaysv.psmap.model.bean.standard.BizExt
import com.desaysv.psmap.model.bean.standard.Category
import com.desaysv.psmap.model.bean.standard.CategoryItem
import com.desaysv.psmap.model.bean.standard.CategoryItemX
import com.desaysv.psmap.model.bean.standard.ChildPoi
import com.desaysv.psmap.model.bean.standard.Citysuggestion
import com.desaysv.psmap.model.bean.standard.CruiseBroadcastOperaData
import com.desaysv.psmap.model.bean.standard.CurrentLocationOperaData
import com.desaysv.psmap.model.bean.standard.EnteryX
import com.desaysv.psmap.model.bean.standard.FavoriteAnyPointOperaData
import com.desaysv.psmap.model.bean.standard.FavoriteChangeData
import com.desaysv.psmap.model.bean.standard.FavoriteCurrentData
import com.desaysv.psmap.model.bean.standard.FavoriteData
import com.desaysv.psmap.model.bean.standard.FavoriteDetailData
import com.desaysv.psmap.model.bean.standard.FavoriteGetData
import com.desaysv.psmap.model.bean.standard.FavoriteHomeCompanyChangeData
import com.desaysv.psmap.model.bean.standard.FavoriteHomeCompanyData
import com.desaysv.psmap.model.bean.standard.FavoriteInformationInquiryData
import com.desaysv.psmap.model.bean.standard.HomeCompanyData
import com.desaysv.psmap.model.bean.standard.KeywordSearchData
import com.desaysv.psmap.model.bean.standard.MapOperaData
import com.desaysv.psmap.model.bean.standard.MapOperaResultData
import com.desaysv.psmap.model.bean.standard.MapStateInfoData
import com.desaysv.psmap.model.bean.standard.NaviInfoResponseData
import com.desaysv.psmap.model.bean.standard.NaviInfoSearchData
import com.desaysv.psmap.model.bean.standard.NaviInfoSearchResponse
import com.desaysv.psmap.model.bean.standard.NaviOperaBroadcastData
import com.desaysv.psmap.model.bean.standard.NaviRoutePreferOperaData
import com.desaysv.psmap.model.bean.standard.NewFavoriteData
import com.desaysv.psmap.model.bean.standard.OnNaviRoutePreferOperaData
import com.desaysv.psmap.model.bean.standard.PlanRouteData
import com.desaysv.psmap.model.bean.standard.PlateNumberData
import com.desaysv.psmap.model.bean.standard.Poi
import com.desaysv.psmap.model.bean.standard.PoiResult
import com.desaysv.psmap.model.bean.standard.ProtocolCityInfo
import com.desaysv.psmap.model.bean.standard.ProtocolRouteInfo
import com.desaysv.psmap.model.bean.standard.ProtocolRouteInfoData
import com.desaysv.psmap.model.bean.standard.ProtocolViaPOIInfo
import com.desaysv.psmap.model.bean.standard.QueryRestrictedInfoOperaData
import com.desaysv.psmap.model.bean.standard.ResponseDataData
import com.desaysv.psmap.model.bean.standard.ResponsePlanRouteData
import com.desaysv.psmap.model.bean.standard.RouteChangeOperaData
import com.desaysv.psmap.model.bean.standard.RouteInfoDataRequest
import com.desaysv.psmap.model.bean.standard.RouteInfoDataResponse
import com.desaysv.psmap.model.bean.standard.RouteOverviewOperaData
import com.desaysv.psmap.model.bean.standard.RouteSelectOperaData
import com.desaysv.psmap.model.bean.standard.SearchListOperaData
import com.desaysv.psmap.model.bean.standard.SearchResponseData
import com.desaysv.psmap.model.bean.standard.SearchResultListChangeData
import com.desaysv.psmap.model.bean.standard.StandardJsonConstant
import com.desaysv.psmap.model.bean.standard.StandardJsonConstant.SERVER_VERSION
import com.desaysv.psmap.model.bean.standard.StandardJsonProtocolData
import com.desaysv.psmap.model.bean.standard.StandardResponseData
import com.desaysv.psmap.model.bean.standard.SuggestionCityDetail
import com.desaysv.psmap.model.bean.standard.TRManualOperaData
import com.desaysv.psmap.model.bean.standard.TripContrastRoute
import com.desaysv.psmap.model.bean.standard.TripContrastRouteInfo
import com.desaysv.psmap.model.bean.standard.VehicleLimitOperaData
import com.desaysv.psmap.model.bean.standard.VersionData
import com.desaysv.psmap.model.bean.standard.ViaPoiOperaData
import com.desaysv.psmap.model.bean.standard.VoiceMuteOperaData
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import com.desaysv.psmap.model.service.IStandardJsonCallback
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

/**
 * 高德公版json协议处理类
 */
@Singleton
class JsonStandardProtocolManager @Inject constructor(
    private val application: Application,
    private val gson: Gson,
    private val mapBusiness: MapBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val mapController: MapController,
    private val routeBusiness: RouteBusiness,
    private val initSDKBusiness: InitSDKBusiness,
    private val naviBusiness: NaviBusiness,
    private val userBusiness: UserBusiness,
    private val cruiseBusiness: CruiseBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val locationBusiness: LocationBusiness,
    private val searchBusiness: SearchBusiness,
    private val netWorkManager: NetWorkManager,
    private val mRouteRequestController: RouteRequestController,
    private val mSettingComponent: ISettingComponent,
    private val activationMapBusiness: ActivationMapBusiness,
    private val aosBusiness: AosBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val mTtsPlayBusiness: TtsPlayBusiness,
    @ProvidesHiltModule.DefaultMapCommand private val defaultMapCommand: IMapCommand,
) {
    private var mCallback: IStandardJsonCallback? = null

    private var mIsInit = false

    //搜索结果列表操作
    private val _searchListOperaLiveData = MutableLiveData<SearchListOperaData?>()
    val searchListOperaLiveData: LiveData<SearchListOperaData?> = _searchListOperaLiveData

    private var naviInfoResponse: NaviInfoResponseData = NaviInfoResponseData()

    private var mCruiseTimer = false
    private var mSapaETA = 0//到达最近的服务区的预计⽤时，单位：秒
    private var mTollDist = 0//距离最近收费站的距离，对应的值为int类型，单位：⽶
    private var mTollETA = 0//到达最近的收费站的预计⽤时，单位：秒
    private var mTollName = ""//距离最近的收费站名称

    private var standardResponseList = ConcurrentHashMap.newKeySet<StandardResponseData>()

    //如果在收藏界面，则使用这个变量，刷新收藏列表
    val favoritesUpdate = MutableLiveData(true)

    fun init(callback: IStandardJsonCallback) {
        Timber.i("init")
        mCallback = callback
        if (initSDKBusiness.isInitSuccess()) {
            initData()
        } else {
            initSDKBusiness.getInitResult().observeForever(object : Observer<InitSDKBusiness.InitSDKResult> {
                override fun onChanged(value: InitSDKBusiness.InitSDKResult) {
                    Timber.i("getInitResult observe ${value.code}")
                    if (value.code == InitSdkResultType.OK) {
                        initData()
                        initSDKBusiness.getInitResult().removeObserver(this)
                    }
                }
            })
        }
    }

    /**
     * 初始化相关
     */
    private fun initData() {
        Timber.i("initTask")
        if (mIsInit) {
            Timber.i("already initData")
            return
        }
        mIsInit = true
        AutoStatusAdapter.setStatusCallback(object : AutoStatusAdapter.IStatusCallback {
            override fun onStatus(autoStatus: Int, statusDetails: Int) {
                MainScope().launch {
                    Timber.i("autoStatus=$autoStatus statusDetails=$statusDetails")
                    dispatchMessage(ProtocolID.PROTOCOL_AUTO_STATUS, JsonObject().apply {
                        addProperty("autoStatus", autoStatus)
                        addProperty("statusDetails", statusDetails)
                    })
                    when (autoStatus) {
                        AutoStatus.PLAN_ROUTE_SUCCESS, AutoStatus.NAVI_UPDATE_PATH_SUCCESS -> {
                            protocolGetRouteInfoExecute("", null, 0)
//                            if (autoStatus == AutoStatus.NAVI_UPDATE_PATH_SUCCESS)
                            protocolTripRouteInfoContrastExecute(if (statusDetails == RouteType.RouteTypeMutiRouteRequest) 0 else 1)
                        }
                    }
                }
            }
        })
        //一些写在autoSDK模块的状态透出，需要用这个监听，再转换为Json状态透出
        SdkAdapterManager.getInstance().addNaviStatusListener { state ->
            when (state) {
                AutoState.NAVI_ENTERTUNNEL -> AutoStatusAdapter.sendStatus(AutoStatus.NAVI_TUNNEL_START)
                AutoState.NAVI__EXITTUNNEL -> AutoStatusAdapter.sendStatus(AutoStatus.NAVI_TUNNEL_EXIT)
                AutoState.TMC_ON -> {
                    AutoStatusAdapter.sendStatus(AutoStatus.TMC_OPEN)
                    MainScope().launch {
                        dispatchMessage(ProtocolID.PROTOCOL_INFO_NOTIFY_MAPOPERATERESULT, MapOperaResultData(operateType = 1))
                    }
                }

                AutoState.TMC_OFF -> {
                    AutoStatusAdapter.sendStatus(AutoStatus.TMC_CLOSE)
                    MainScope().launch {
                        dispatchMessage(ProtocolID.PROTOCOL_INFO_NOTIFY_MAPOPERATERESULT, MapOperaResultData(operateType = 2))
                    }
                }

                else -> {}
            }
        }

        naviBusiness.naviStatus.observeForever { naviState ->
            Timber.i("naviStatus=$naviState")
            when (naviState) {
                BaseConstant.NAVI_STATE_REAL_NAVING -> {
                    AutoStatusAdapter.sendStatus(AutoStatus.NAVI_START)
                }

                BaseConstant.NAVI_STATE_SIM_NAVING -> {
                    AutoStatusAdapter.sendStatus(AutoStatus.SIM_NAVI_START)
                }

                BaseConstant.NAVI_STATE_STOP_REAL_NAVI -> {
                    AutoStatusAdapter.sendStatus(AutoStatus.NAVI_STOP)
                    naviInfoResponse = NaviInfoResponseData()
                    MainScope().launch {
                        dispatchMessage(ProtocolID.PROTOCOL_GUIDE_INFO, naviInfoResponse)
                    }
                }

                BaseConstant.NAVI_STATE_STOP_SIM_NAVI -> {
                    AutoStatusAdapter.sendStatus(AutoStatus.SIM_NAVI_STOP)
                    naviInfoResponse = NaviInfoResponseData()
                    MainScope().launch {
                        dispatchMessage(ProtocolID.PROTOCOL_GUIDE_INFO, naviInfoResponse)
                    }
                }

                BaseConstant.NAVI_STATE_PAUSE_SIM_NAVI -> {
                    AutoStatusAdapter.sendStatus(AutoStatus.SIM_NAVI_PAUSE)
                }
            }
        }
        mapBusiness.zoomIn.observeForever { zoomIn ->
            zoomIn?.let {
                val data = MapOperaResultData()
                if (it) {
                    data.operateType = 3
                    data.isCanZoom = mapBusiness.zoomInEnable.value!!
                    AutoStatusAdapter.sendStatus(AutoStatus.MAP_ZOOM_IN)
                } else {
                    data.operateType = 4
                    data.isCanZoom = mapBusiness.zoomOutEnable.value!!
                    AutoStatusAdapter.sendStatus(AutoStatus.MAP_ZOOM_OUT)
                }
                MainScope().launch {
                    dispatchMessage(ProtocolID.PROTOCOL_INFO_NOTIFY_MAPOPERATERESULT, data)
                }
            }
        }
        mapBusiness.zoomInEnable.observeForever {
            if (it == false)
                AutoStatusAdapter.sendStatus(AutoStatus.MAP_SCALE_MAX)
        }
        mapBusiness.zoomOutEnable.observeForever {
            if (it == false)
                AutoStatusAdapter.sendStatus(AutoStatus.MAP_SCALE_MIN)
        }
        mapBusiness.mapMode.observeForever { mapMode ->
            when (mapMode) {
                MapModeType.VISUALMODE_2D_CAR -> AutoStatusAdapter.sendStatus(AutoStatus.VISUAL_2D_CAR)
                MapModeType.VISUALMODE_3D_CAR -> AutoStatusAdapter.sendStatus(AutoStatus.VISUAL_3D_CAR)
                MapModeType.VISUALMODE_2D_NORTH -> AutoStatusAdapter.sendStatus(AutoStatus.VISUAL_NORTH)
            }
        }

        cruiseBusiness.cruiseStatus.observeForever {
            Timber.i("cruiseStatus = $it")
            AutoStatusAdapter.sendStatus(if (it) AutoStatus.CRUISE_START else AutoStatus.CRUISE_STOP)
            MainScope().launch {
                if (it) {
                    if (!mCruiseTimer) {
                        mCruiseTimer = true
                        loopSendCruiseInfo()
                        mCruiseTimer = false
                    }
                } else {
                    naviInfoResponse = NaviInfoResponseData()
                    dispatchMessage(ProtocolID.PROTOCOL_GUIDE_INFO, naviInfoResponse)
                }
            }
        }
        skyBoxBusiness.themeChange().observeForever { isNight ->
            AutoStatusAdapter.sendStatus(if (isNight) AutoStatus.THEME_NIGHT else AutoStatus.THEME_DAY)
        }
        naviBusiness.showCrossView.observeForever { show ->
            AutoStatusAdapter.sendStatus(if (show) AutoStatus.CROSS_IMAGE_SHOW else AutoStatus.CROSS_IMAGE_HIDE)
        }
        naviBusiness.inFullView.observeForever { preview ->
            AutoStatusAdapter.sendStatus(if (preview) AutoStatus.NAVI_PREVIEW_START else AutoStatus.NAVI_PREVIEW_EXIT)
        }

        naviBusiness.parallelBridgeState.observeForever {
            when (it) {
                1 -> AutoStatusAdapter.sendStatus(AutoStatus.ON_BRIDGE)
                2 -> AutoStatusAdapter.sendStatus(AutoStatus.UNDER_BRIDGE)
            }
        }

        naviBusiness.parallelRoadState.observeForever {
            when (it) {
                1 -> AutoStatusAdapter.sendStatus(AutoStatus.ON_MAIN_ROAD)
                2 -> AutoStatusAdapter.sendStatus(AutoStatus.ON_AUXILIARY_ROAD)
            }
        }
        //导航信息
        naviBusiness.naviInfo.observeForever { naviInfo ->
            MainScope().launch {
                naviInfo?.let {
                    protocolNaviInfoExecute(it)
                }
            }
        }
        naviBusiness.loadingView.observeForever { loadingView ->
            MainScope().launch {
                if (!loadingView) {
                    if (naviBusiness.naviErrorMessage.value?.isNotEmpty() == true) {
                        standardAsyncResponseMessage(ProtocolID.PROTOCOL_REQUEST_ROUTE_EX, StandardJsonConstant.ResponseResult.FAIL_10032)
                        standardAsyncResponseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, StandardJsonConstant.ResponseResult.FAIL_10032)
                    } else {
                        protocolPlanRouteResponse()
                        standardAsyncResponseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, StandardJsonConstant.ResponseResult.OK)
                    }
                }
            }
        }
        val updateCameraInfo = fun(data: ArrayList<NaviCameraExt>?) {
            naviInfoResponse.cameraType = -1
            naviInfoResponse.cameraIndex = -1
            naviInfoResponse.cameraSpeed = 0
            naviInfoResponse.cameraPenalty = false
            naviInfoResponse.newCamera = false
            naviInfoResponse.cameraID = -1
            if (!data.isNullOrEmpty()) {
                val naviCamera = data.firstOrNull() ?: return
                naviInfoResponse.cameraDist = naviCamera.distance
                naviInfoResponse.cameraID = naviCamera.cameraId.toInt()
                var smallSpeed = 0
                val naviCameraSecond = data.getOrNull(1)
                naviCameraSecond?.let { naviInfoResponse.cameraIndex = it.cameraId.toInt() }
                naviCamera.subCameras?.forEach { subCamera ->
                    if (subCamera.penalty == 2) {
                        naviInfoResponse.cameraPenalty = true
                    }
                    if (subCamera.isNew) {
                        naviInfoResponse.newCamera = true
                    }
                    when (subCamera.subType) {
                        SubCameraExtType.SubCameraExtTypeUltrahighSpeed,
                        SubCameraExtType.SubCameraExtTypeVariableSpeed -> {
                            subCamera.speed?.forEach { sp ->
                                if (smallSpeed == 0 || sp < smallSpeed) {
                                    smallSpeed = sp.toInt()
                                    naviInfoResponse.cameraType = subCamera.subType
                                }
                            }
                            naviInfoResponse.cameraSpeed = smallSpeed
                        }
                    }
                }

                if (naviInfoResponse.cameraSpeed <= 0) {
                    naviInfoResponse.cameraType = naviCamera.subCameras?.firstOrNull()?.subType!!
                }
            }
        }

        //电子眼信息
        naviBusiness.naviCameraList.observeForever { data ->
            updateCameraInfo(data)
        }
        //出口信息
        naviBusiness.exitDirectionInfoAll.observeForever { exitDirectionInfo ->
            naviInfoResponse.exitNameInfo = exitDirectionInfo?.exitNameInfo?.firstOrNull() ?: ""
        }
        //道路限速
        naviBusiness.mCurrentRoadSpeed.observeForever {
            naviInfoResponse.limitedSpeed = it
        }

        //显示到下一条道路的路口大图的距离
        naviBusiness.crossImageProgress.observeForever {
            naviInfoResponse.nextRoadProgressPrecent = it
        }

        naviBusiness.sapaInfoList.observeForever { sapaInfo ->
            if (!sapaInfo.isNullOrEmpty()) {
                naviInfoResponse.sapaDist = sapaInfo[0].remainDist//距离最近服务区的距离，对应的值为int类型，单位：⽶
                mSapaETA = sapaInfo[0].remainTime.toInt()//当前车位距离到sapa的剩余时间（单位秒） 默认0
                naviInfoResponse.sapaDistAuto = CommonUtils.getDistanceStr(
                    application.applicationContext,
                    sapaInfo[0].remainDist.toDouble()
                )//转换后距离最近服务区的距离，对应的值为String类型，由距离和单位组成
                naviInfoResponse.sapaName = sapaInfo[0].name//距离最近的服务区名称
                naviInfoResponse.sapaNum = sapaInfo.size//服务区个数
                naviInfoResponse.sapaType =
                    if (sapaInfo[0].type == NaviFacilityType.NaviFacilityTypeServiceArea) 0 else 1//距离最近的服务区类型： 0：⾼速服务区 1：其他服务设施（收费站、停⻋区等）
                if (sapaInfo.size > 1) {
                    naviInfoResponse.nextSapaDist = sapaInfo[1].remainDist//距离前⽅第⼆个服务区的距离，对应的值为int类型，单位：⽶
                    naviInfoResponse.nextSapaDistAuto = CommonUtils.getDistanceStr(
                        application.applicationContext,
                        sapaInfo[1].remainDist.toDouble()
                    )//转换后距离前⽅第⼆个服务区的距离，对应的值为String类型，由距离和单位组成
                    naviInfoResponse.nextSapaName = sapaInfo[1].name//前⽅第⼆个服务区名称
                    naviInfoResponse.nextSapaType = if (sapaInfo[1].type == 0) 0 else 1//前⽅第⼆个服务区类型
                }
                val firstTollGate = sapaInfo.find { it.type == NaviFacilityType.NaviFacilityTypeTollGate }
                firstTollGate?.let {
                    mTollDist = it.remainDist//距离最近收费站的距离，对应的值为int类型，单位：⽶
                    mTollETA = it.remainTime.toInt()//到达最近的收费站的预计⽤时，单位：秒
                    mTollName = it.name//距离最近的收费站名称
                } ?: run {
                    mTollDist = 0//距离最近收费站的距离，对应的值为int类型，单位：⽶
                    mTollETA = 0//到达最近的收费站的预计⽤时，单位：秒
                    mTollName = ""//距离最近的收费站名称
                }
            } else {
                naviInfoResponse.sapaDist = 0//距离最近服务区的距离，对应的值为int类型，单位：⽶
                naviInfoResponse.sapaDistAuto = ""//转换后距离最近服务区的距离，对应的值为String类型，由距离和单位组成
                naviInfoResponse.sapaName = ""//距离最近的服务区名称
                naviInfoResponse.sapaNum = 0//服务区个数
                naviInfoResponse.sapaType = -1//距离最近的服务区类型： 0：⾼速服务区 1：其他服务设施（收费站、停⻋区等）
                naviInfoResponse.nextSapaDist = 0//距离前⽅第⼆个服务区的距离，对应的值为int类型，单位：⽶
                naviInfoResponse.nextSapaDistAuto = ""//转换后距离前⽅第⼆个服务区的距离，对应的值为String类型，由距离和单位组成
                naviInfoResponse.nextSapaName = ""//前⽅第⼆个服务区名称
                naviInfoResponse.nextSapaType = -1//前⽅第⼆个服务区类型
                mSapaETA = 0//当前车位距离到sapa的剩余时间（单位秒） 默认0
                mTollDist = 0//距离最近收费站的距离，对应的值为int类型，单位：⽶
                mTollETA = 0//到达最近的收费站的预计⽤时，单位：秒
                mTollName = ""//距离最近的收费站名称
            }
        }

        cruiseBusiness.cruiseRouteName.observeForever {
            if (cruiseBusiness.isCruising())
                naviInfoResponse.curRoadName = it
        }

        cruiseBusiness.cruiseRoadInfo.observeForever {
            if (cruiseBusiness.isCruising())
                naviInfoResponse.roadType = it[0]
        }

        cruiseBusiness.cruiseSpeed.observeForever {
            if (cruiseBusiness.isCruising())
                naviInfoResponse.curSpeed = it.toInt()
        }

        cruiseBusiness.naviCameraExt.observeForever {
            if (cruiseBusiness.isCruising())
                updateCameraInfo(it)
        }

        //查询前⽅路况 tts播报
        cruiseBusiness.tRTtsBroadcast.observeForever {
            MainScope().launch {
                responseMessage(
                    ProtocolID.PROTOCOL_FRONT_TRAFFIC_RADIO,
                    TRManualOperaData().apply {
                        resultCode = StandardJsonConstant.ResponseResult.OK.code
                        errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                        frontTrafficInfo = it
                    },
                    pkg = BaseConstant.TR_PKG,
                    responseCode = BaseConstant.TR_REQUEST_CODE
                )
            }
        }

        //通知语音家和公司变更通知--增加
        userBusiness.addFavoriteResultVoice.unPeek().observeForever {
            MainScope().launch {
                protocolFavoriteHomeCompanyChangeExecute(it.keys.first(), 1, "增加", it.values.first())
            }
        }

        //通知语音家和公司变更通知--刪除
        userBusiness.delFavoriteResultVoice.unPeek().observeForever {
            MainScope().launch {
                protocolFavoriteHomeCompanyChangeExecute(it.keys.first(), 2, "刪除", it.values.first())
            }
        }

        //通知语音家和公司变更通知--更新
        userBusiness.updateFavoriteResultVoice.unPeek().observeForever {
            MainScope().launch {
                protocolFavoriteHomeCompanyChangeExecute(it.keys.first(), 1, "更新", it.values.first())
            }
        }

        routeBusiness.isRequestRoute.unPeek().observeForever { value ->
            Timber.i("isRequestRoute isRequestRoute is called")
            if (!value) {
                MainScope().launch {
                    if (routeBusiness.routeErrorMessage.value?.isNotEmpty() == true) {
                        Timber.i("isRequestRoute routeErrorMessage is called")
                        standardAsyncResponseMessage(ProtocolID.PROTOCOL_REQUEST_ROUTE_EX, StandardJsonConstant.ResponseResult.FAIL_10032)
                        standardAsyncResponseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, StandardJsonConstant.ResponseResult.FAIL_10032)

                    } else if (!routeBusiness.pathListLiveData.value.isNullOrEmpty()) {
                        protocolPlanRouteResponse()
                        standardAsyncResponseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, StandardJsonConstant.ResponseResult.OK)
                    }
                }
            }
        }
    }

    fun unInit() {
        Timber.i("unInit")
        mIsInit = false
        mCallback = null
        AutoStatusAdapter.setStatusCallback(null)
    }

    fun receivedRequestMessage(pkg: String, jsonString: String) {
        Timber.i("pkg= $pkg jsonString= $jsonString")
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        val protocolId = jsonObject.get("protocolId").asInt
        val needResponse = jsonObject.get("needResponse").asBoolean
        val requestCode = jsonObject.get("requestCode")?.asString

        if (!initSDKBusiness.isInitSuccess()) {
            Timber.w("Amap SDK not init")
            MainScope().launch {
                responseMessage(
                    protocolId, VersionData(versionName = SERVER_VERSION).apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10018.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10018.msg
                    }, pkg = pkg, responseCode = requestCode
                )
            }
            return
        }

        when (protocolId) {
            ProtocolID.PROTOCOL_GET_VERSION -> {
                val data = VersionData(versionName = SERVER_VERSION)
                MainScope().launch {
                    responseMessage(ProtocolID.PROTOCOL_GET_VERSION, data, pkg = pkg, responseCode = requestCode)
                }
            }

            ProtocolID.PROTOCOL_MAP_OPERA -> {
                val mapOperaData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), MapOperaData::class.java)
                MainScope().launch {
                    protocolMapOperaExecute(pkg, requestCode, mapOperaData)
                }
            }

            ProtocolID.PROTOCOL_REPORT_NAVI_MODEL -> {
                val naviOperaBroadcastData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), NaviOperaBroadcastData::class.java)
                MainScope().launch {
                    protocolNaviOperaBroadcastExecute(pkg, requestCode, naviOperaBroadcastData)
                }
            }

            ProtocolID.PROTOCOL_CRUISE_PLAY_TYPE -> {
                val cruiseBroadcastOperaData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), CruiseBroadcastOperaData::class.java)
                MainScope().launch {
                    protocolCruiseBroadcastOperaExecute(pkg, requestCode, cruiseBroadcastOperaData)
                }
            }

            ProtocolID.PROTOCOL_INFO_NOTIFY_MAPSTATEINFOQUERY -> {
                val data = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), MapStateInfoData::class.java)
                MainScope().launch {
                    protocolMapStateInfoQueryExecute(pkg, requestCode, data)
                }
            }

            ProtocolID.PROTOCOL_SET_MUTE -> {
                val voiceMuteOperaData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), VoiceMuteOperaData::class.java)
                MainScope().launch {
                    protocolVoiceMuteOperaExecute(pkg, requestCode, voiceMuteOperaData)
                }
            }

            ProtocolID.PROTOCOL_ON_NAVI_ROUTE_PREFER -> {
                val onNaviRoutePreferOperaData =
                    gson.fromJson(
                        jsonObject.get("data").asJsonObject.toString(),
                        OnNaviRoutePreferOperaData::class.java
                    )
                MainScope().launch {
                    protocolOnNaviRoutePreferOperaExecute(pkg, requestCode, onNaviRoutePreferOperaData, protocolId)
                }
            }

            ProtocolID.PROTOCOL_NAVI_ROUTE_PREFER -> {
                val naviRoutePreferOperaData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), NaviRoutePreferOperaData::class.java)
                MainScope().launch {
                    protocolNaviRoutePreferOperaExecute(pkg, requestCode, naviRoutePreferOperaData, protocolId)
                }
            }

            ProtocolID.PROTOCOL_FAVORITE_ANY_POI -> {
                val favoriteAnyPointOperaData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), FavoriteAnyPointOperaData::class.java)
                MainScope().launch {
                    protocolFavoriteAnyPointOperaExecute(pkg, requestCode, favoriteAnyPointOperaData)
                }
            }

            ProtocolID.PROTOCOL_FAVORITE_CURRENT_POI -> {
                val favoriteCurrentData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), FavoriteCurrentData::class.java)
                MainScope().launch {
                    protocolFavoriteCurrentExecute(pkg, requestCode, favoriteCurrentData)
                }
            }

            ProtocolID.PROTOCOL_OTHER_NEWQUERYSAVES -> {
                val favoriteInformationInquiryData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), FavoriteInformationInquiryData::class.java)
                MainScope().launch {
                    protocolFavoriteInformationInquiryExecute(pkg, requestCode, favoriteInformationInquiryData)
                }
            }

            ProtocolID.PROTOCOL_REQUEST_ROUTE_EX -> {
                val planRouteData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), PlanRouteData::class.java)
                MainScope().launch {
                    protocolPlanRouteExecute(pkg, requestCode, planRouteData)
                }
            }

            ProtocolID.PROTOCOL_USER_OPENSAVES -> {
                MainScope().launch {
                    protocolUserFavoriteExecute(pkg, requestCode)
                }
            }

            ProtocolID.PROTOCOL_OTHER_QUERYSAVES -> {
                val favoriteGetData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), FavoriteGetData::class.java)
                MainScope().launch {
                    protocolFavoriteGetExecute(pkg, requestCode, favoriteGetData)
                }
            }

            ProtocolID.PROTOCOL_GET_FAVORITE_POI -> {
                val favoriteHomeCompanyData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), FavoriteHomeCompanyData::class.java)
                MainScope().launch {
                    protocolFavoriteHomeCompanyDataExecute(pkg, requestCode, favoriteHomeCompanyData)
                }
            }

            ProtocolID.PROTOCOL_CAR_GETPLATENUMBERFROMAUTO -> {
                MainScope().launch {
                    protocolPlateNumberExecute(pkg, requestCode)
                }
            }

            ProtocolID.PROTOCOL_BACK_TO_MAP -> {
                val backToMapOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), BackToMapOperaData::class.java)
                MainScope().launch {
                    protocolBackToMapExecute(pkg, requestCode, backToMapOperaData)
                }
            }

            ProtocolID.PROTOCOL_SHOW_MY_LOCATION -> {
                val currentLocationOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), CurrentLocationOperaData::class.java)
                MainScope().launch {
                    protocolCurrentLocationExecute(pkg, requestCode, currentLocationOperaData)
                }
            }

            ProtocolID.PROTOCOL_TRIP_ENDNAVIGATION -> { //三⽅通知auto结束引导，退出导航状态，回到主图界⾯
                val responseDataData = ResponseDataData()
                MainScope().launch {
                    protocolEndNavigationExecute(pkg, requestCode, responseDataData)
                }
            }

            ProtocolID.PROTOCOL_TRIP_ROUTEOVERVIEW -> { //在且仅在导航场景下，通过第三⽅控制进⼊或退出全览状态
                val routeOverviewOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), RouteOverviewOperaData::class.java)
                MainScope().launch {
                    protocolRouteOverviewExecute(pkg, requestCode, routeOverviewOperaData)
                }
            }

            ProtocolID.PROTOCOL_ROUTE_SELECT -> { //路径规划完成后在路线规划结果⻚⾯，第三⽅可选择路线并开始导航
                val routeSelectOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), RouteSelectOperaData::class.java)
                MainScope().launch {
                    protocolRouteSelectExecute(pkg, requestCode, routeSelectOperaData)
                }
            }

            ProtocolID.PROTOCOL_MODIFY_NAVI_VIA -> {
                val viaPoiOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), ViaPoiOperaData::class.java)
                MainScope().launch {
                    protocolViaPoiOperaDataExecute(pkg, requestCode, viaPoiOperaData)
                }
            }

            ProtocolID.PROTOCOL_INFO_NOTIFY_TRAFFICRESTRICTQUERY -> {
                val vehicleLimitOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), VehicleLimitOperaData::class.java)
                MainScope().launch {
                    protocolVehicleLimitExecute(pkg, requestCode, vehicleLimitOperaData)
                }
            }

            ProtocolID.PROTOCOL_TRIP_ROUTECHANGE -> { //当导航过程中图⾯出现备选路或动态算路时，通过此协议⽀持系统控制切换备选路线，切换后返回对应状态码
                val routeChangeOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), RouteChangeOperaData::class.java)
                MainScope().launch {
                    protocolRouteChangeExecute(pkg, requestCode, routeChangeOperaData)
                }
            }

            ProtocolID.PROTOCOL_FRONT_TRAFFIC_RADIO -> { //查询前⽅路况
                val tRManualOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), TRManualOperaData::class.java)
                MainScope().launch {
                    protocolTRManualExecute(pkg, requestCode, tRManualOperaData)
                }
            }

            ProtocolID.PROTOCOL_KEYWORD_SEARCH -> {
                val keywordSearchData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), KeywordSearchData::class.java)
                MainScope().launch {
                    protocolKeywordSearchExecute(pkg, requestCode, keywordSearchData)
                }
            }

            ProtocolID.PROTOCOL_AROUNDSEARCH_SEARCH -> {
                val aroundSearchData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), AroundSearchData::class.java)
                MainScope().launch {
                    protocolAroundSearchExecute(pkg, requestCode, aroundSearchData)
                }
            }

            ProtocolID.PROTOCOL_ALONG_THE_WAY_SEARCH -> {
                val aroundSearchData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), AlongWaySearchData::class.java)
                MainScope().launch {
                    protocolAlongWaySearchExecute(pkg, requestCode, aroundSearchData)
                }
            }

            ProtocolID.PROTOCOL_BACK_CURRENT_CAR_POSITION -> { //地图返回⻋位视⻆的接⼝. (⽤于导航过程中⽤户⼿动操作地图缩放,平移等操作后的复位)
                val responseDataData = ResponseDataData()
                MainScope().launch {
                    protocolBackCurrentCarPositionExecute(pkg, requestCode, responseDataData)
                }
            }

            ProtocolID.PROTOCOL_QUERY_RESTRICTED_INFO -> { //第三⽅传⼊指定城市/区县名称进⾏限⾏尾号/限⾏政策查询
                val queryRestrictedInfoOperaData =
                    gson.fromJson(jsonObject.get("data").asJsonObject.toString(), QueryRestrictedInfoOperaData::class.java)
                MainScope().launch {
                    protocolQueryRestrictedInfoExecute(pkg, requestCode, queryRestrictedInfoOperaData)
                }
            }

            ProtocolID.PROTOCOL_SEARCH_SEARCHRESULTOPERATE -> {
                val searchListOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), SearchListOperaData::class.java)
                _searchListOperaLiveData.postValue(searchListOperaData)
            }

            ProtocolID.PROTOCOL_MAP_ENTERAUTO -> { //进⼊主图
                val backToMapHomeOperaData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), BackToMapHomeOperaData::class.java)
                MainScope().launch {
                    protocolBackToMapHomeExecute(pkg, requestCode, backToMapHomeOperaData)
                }
            }

            ProtocolID.PROTOCOL_GET_ROUTE_INFO -> {
                val requestData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), RouteInfoDataRequest::class.java)
                MainScope().launch {
                    protocolGetRouteInfoExecute(pkg, requestCode, requestData.type)
                }
            }

            ProtocolID.PROTOCOL_INFO_NOTIFY_SEARCHNAVINFO -> {
                val requestData = gson.fromJson(jsonObject.get("data").asJsonObject.toString(), NaviInfoSearchData::class.java)
                MainScope().launch {
                    protocolGetNaviInfoSearchExecute(pkg, requestCode, requestData.requestType)
                }
            }

            ProtocolID.PROTOCOL_GUIDE_INFO -> {
                MainScope().launch {
                    naviInfoResponse.apply {
                        resultCode = StandardJsonConstant.ResponseResult.OK.code
                        errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                    }
                    responseMessage(ProtocolID.PROTOCOL_GUIDE_INFO, naviInfoResponse, pkg = pkg, responseCode = requestCode)
                }
            }

            ProtocolID.PROTOCOL_TRIP_ROUTEINFOCONTRAST -> {
                MainScope().launch {
                    protocolTripRouteInfoContrastExecute(isQuery = true, pkg = pkg, requestCode = requestCode)
                }
            }

            else -> {

            }
        }
    }

    /**
     * response 响应的消息
     * @param responseCode 如果有与requestCode一致
     */
    private suspend fun responseMessage(
        protocolId: Int, data: ResponseDataData, pkg: String = "", responseCode: String? = null, statusCode:
        Int = 0
    ) {
        Timber.i("responseMessage is called ${gson.toJson(data)}")
        withContext(Dispatchers.IO) {
            val jsonData = StandardJsonProtocolData(
                protocolId = protocolId,
                responseCode = responseCode,
                messageType = "response",
                statusCode = statusCode,
                needResponse = false,
                data = data
            )
            if (TextUtils.isEmpty(pkg)) {
                mCallback?.onMassageToAllClient(gson.toJson(jsonData))
            } else {
                mCallback?.onMassage(pkg, gson.toJson(jsonData))
            }
        }

    }

    /**
     * 需要异步处理后带responseCode的响应消息
     */
    private fun standardAsyncResponseMessage(protocolId: Int, result: StandardJsonConstant.ResponseResult? = null, data: ResponseDataData? = null) {
        synchronized(this) {
            standardResponseList.forEach {
                if (it.protocolId == protocolId) {
                    Timber.i("standardResponseMessage is called protocolId = $protocolId")
                    MainScope().launch {
                        data?.run {
                            it.data = this
                        }
                        result?.run {
                            it.data.resultCode = result.code
                            it.data.errorMessage = result.msg
                        }
                        withContext(Dispatchers.IO) {
                            mCallback?.onMassage(it.pkg, gson.toJson(it))
                        }
                    }
                }
            }
            standardResponseList.removeIf { it.protocolId == protocolId }
        }
    }

    /**
     * dispatch 主动透出的消息
     */
    private suspend fun <T> dispatchMessage(protocolId: Int, data: T) {
        withContext(Dispatchers.IO) {
            val jsonData = StandardJsonProtocolData(
                protocolId = protocolId,
                messageType = "dispatch",
                data = data
            )
            Timber.i("dispatchMessage jsonData:${gson.toJson(jsonData)}")
            mCallback?.onMassageToAllClient(gson.toJson(jsonData))
        }
    }

    /**
     * 同步接口，获取客户端信息
     * @param pkg 必须指定客户端
     */
    private fun <T> requestClientMessage(pkg: String, protocolId: Int, data: T): String {
        val jsonData = StandardJsonProtocolData(
            protocolId = protocolId,
            messageType = "request",
            needResponse = true,
            data = data
        )
        return mCallback?.syncClientMessage(pkg, gson.toJson(jsonData)) ?: JsonObject().toString()
    }


    /**
     * =======================================================下面是指令处理代码===================================================================
     */


    /**
     * PROTOCOL_MAP_OPERA: Int = 30000 指令处理
     */
    private suspend fun protocolMapOperaExecute(pkg: String, requestCode: String?, mapOperaData: MapOperaData) {
        //0：实时路况 1：缩放地图 2：视图设置
        Timber.i(
            "actionType=${mapOperaData.actionType} operaType=${mapOperaData.operaType} operaValue=${
                mapOperaData
                    .operaValue
            }"
        )

        if (mapOperaData.operaValue in 3..19) {
            mapBusiness.setZoomLevel(mapOperaData.operaValue.toFloat())
        }

        when (mapOperaData.actionType) {
            0 -> {
                //0：开启路况 1：关闭路况
                when (mapOperaData.operaType) {
                    0 -> {
                        Timber.i("open tmc")
                        navigationSettingBusiness.setConfigKeyRoadEvent(1)
                    }

                    1 -> {
                        Timber.i("close tmc")
                        navigationSettingBusiness.setConfigKeyRoadEvent(0)
                    }
                }
            }

            1 -> {
                //0：放⼤地图 1：缩⼩地图 2：最⼤⽐例尺 3：最⼩⽐例尺
                when (mapOperaData.operaType) {
                    0 -> {
                        mapBusiness.mapZoomIn()
                    }

                    1 -> {
                        mapBusiness.mapZoomOut()
                    }

                    2 -> {
                        mapController.getMaxScale(SurfaceViewID.SURFACE_VIEW_ID_MAIN).run {
                            if (this != 0)
                                mapBusiness.setZoomLevel(this.toFloat())
                        }
                    }

                    3 -> {
                        mapController.getMinScale(SurfaceViewID.SURFACE_VIEW_ID_MAIN).run {
                            if (this != 0)
                                mapBusiness.setZoomLevel(this.toFloat())
                        }
                    }
                }
            }
            //0：2D⻋头向上 1：2D正北朝上 2：3D⻋头向上 3：视图轮流切换
            2 -> {
                when (mapOperaData.operaType) {
                    0 -> {
                        mapBusiness.switchMapViewMode(MapModeType.VISUALMODE_2D_CAR)
                    }

                    1 -> {
                        mapBusiness.switchMapViewMode(MapModeType.VISUALMODE_2D_NORTH)
                    }

                    2 -> {
                        mapBusiness.switchMapViewMode(MapModeType.VISUALMODE_3D_CAR)
                    }

                    3 -> {
                        mapBusiness.switchMapViewMode()
                    }
                }
            }
        }

        responseMessage(
            ProtocolID.PROTOCOL_MAP_OPERA,
            mapOperaData.apply {
                resultCode = StandardJsonConstant.ResponseResult.OK.code
                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
            },
            pkg = pkg,
            responseCode = requestCode
        )

    }

    /**
     * PROTOCOL_INFO_NOTIFY_MAPSTATEINFOQUERY: Int = 80008 查询地图状态指令处理
     */
    private suspend fun protocolMapStateInfoQueryExecute(
        pkg: String,
        requestCode: String?,
        mapStateInfoData: MapStateInfoData
    ) {
        Timber.i("stateType = ${mapStateInfoData.stateType}")
        mapStateInfoData.errorMessage = StandardJsonConstant.ResponseResult.OK.msg
        mapStateInfoData.resultCode = StandardJsonConstant.ResponseResult.OK.code
        when (mapStateInfoData.stateType) {
            //当stateType=0地图视⻆⻆时；stateValue的值 0：3D视⻆，1：2D北⾸上，2：2D⻋⾸上
            0 -> {
                when (mapBusiness.mapMode.value) {
                    MapModeType.VISUALMODE_3D_CAR -> {
                        mapStateInfoData.stateValue = 0
                    }

                    MapModeType.VISUALMODE_2D_CAR -> {
                        mapStateInfoData.stateValue = 2
                    }

                    MapModeType.VISUALMODE_2D_NORTH -> {
                        mapStateInfoData.stateValue = 1
                    }
                }
            }
            //当stateType=1导航所在⻚⾯时 stateValue的值 0:其它页面 1:主页，2：导航页 3：路线规划⻚
            1 -> {
                mapStateInfoData.stateValue = if (mapBusiness.isInHomeFragment()) {
                    1
                } else if (mapBusiness.isInNaviFragment) {
                    2
                } else if (mapBusiness.isInRouteFragment) {
                    3
                } else 0
            }
            //当stateType=2导航前后台查询 stateValue的值 0：导航在后台，1：导航在前台
            2 -> {
                mapStateInfoData.stateValue = if (BaseConstant.MAP_APP_FOREGROUND) 1 else 0
            }
            //当stateType=3mqtt初始化状态查询时，stateValue的值 0：未完成，1：已完成
            3 -> {
                Timber.i("not support query mqtt")
                mapStateInfoData.errorMessage = StandardJsonConstant.ResponseResult.FAIL_10028.msg
                mapStateInfoData.resultCode = StandardJsonConstant.ResponseResult.FAIL_10028.code
            }
            //当stateType=4 AR中控状态查询时，stateValue的值 0：⾮AR状态，1：AR巡航（功能暂未⽀持，先预留字段）2：AR导航
            4 -> {
                Timber.i("not support Ar central control state")
                mapStateInfoData.errorMessage = StandardJsonConstant.ResponseResult.FAIL_10028.msg
                mapStateInfoData.resultCode = StandardJsonConstant.ResponseResult.FAIL_10028.code
            }
            //stateType=5 AR仪表状态查询时，stateValue的值 0：⾮AR状态，1：AR巡航 2：AR导航
            5 -> {
                Timber.i("not support Ar dashboard state")
                mapStateInfoData.errorMessage = StandardJsonConstant.ResponseResult.FAIL_10028.msg
                mapStateInfoData.resultCode = StandardJsonConstant.ResponseResult.FAIL_10028.code
            }
            //当stateType=6 导航状态查询时，stateValue的值 0：导航，1：⾮导航 2：模拟导航
            6 -> {
                mapStateInfoData.stateValue = when (naviBusiness.naviStatus.value) {
                    BaseConstant.NAVI_STATE_INIT_NAVI_STOP, BaseConstant.NAVI_STATE_STOP_REAL_NAVI, BaseConstant
                        .NAVI_STATE_STOP_SIM_NAVI -> 1

                    BaseConstant.NAVI_STATE_REAL_NAVING -> 0
                    BaseConstant.NAVI_STATE_SIM_NAVING, BaseConstant.NAVI_STATE_PAUSE_SIM_NAVI -> 2
                    else -> 1
                }
            }
            //当stateType=7 运⾏状态查询时，stateValue的值 1：⾃启 2：启动
            7 -> {
                mapStateInfoData.stateValue = 2 //默认启动
            }
            //当stateType=8 运⾏状态查询时, stateValue的值,奥迪项⽬为1-19(2000km-10m),AUTO项⽬为3-19(1000km-10m) (500及以上版本⽀持)
            //应该是比例尺等级查询
            8 -> {
                val fLevel = mapBusiness.getZoomLevel()
                val level = when {
                    fLevel > 3 && fLevel < 5 -> ceil(fLevel.toDouble()).toInt()
                    else -> fLevel.toInt()
                }
                mapStateInfoData.stateValue = level
            }
        }

        responseMessage(
            ProtocolID.PROTOCOL_INFO_NOTIFY_MAPSTATEINFOQUERY,
            mapStateInfoData,
            pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_REPORT_NAVI_MODEL: Int = 30011 指令处理 导航播报模式设置
     */
    private suspend fun protocolNaviOperaBroadcastExecute(
        pkg: String,
        requestCode: String?,
        naviOperaBroadcastData: NaviOperaBroadcastData?
    ) {
        if (naviOperaBroadcastData == null) {
            Timber.i("protocolNaviOperaBroadcastExecute naviOperaBroadcastData == null")
            return
        }
        navigationSettingBusiness.setConfigKeyBroadcastMode(naviOperaBroadcastData.naviBroadcastType) //1：简洁播报 2：详细播报 3：极简播报（V650开始⽀持）
        responseMessage(
            ProtocolID.PROTOCOL_REPORT_NAVI_MODEL,
            naviOperaBroadcastData.apply {
                resultCode = StandardJsonConstant.ResponseResult.OK.code
                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_CRUISE_PLAY_TYPE: Int = 30016 指令处理 巡航播报模式设置
     */
    private suspend fun protocolCruiseBroadcastOperaExecute(
        pkg: String,
        requestCode: String?,
        cruiseBroadcastOperaData: CruiseBroadcastOperaData?
    ) {
        if (cruiseBroadcastOperaData == null) {
            Timber.i("protocolCruiseBroadcastOperaExecute cruiseBroadcastOperaData == null")
            return
        }
        navigationSettingBusiness.cruiseBroadcastSelect(ConfigKey.ConfigKeyRoadWarn, false)
        navigationSettingBusiness.cruiseBroadcastSelect(ConfigKey.ConfigKeySafeBroadcast, false)
        navigationSettingBusiness.cruiseBroadcastSelect(ConfigKey.ConfigKeyDriveWarn, false)
        when (cruiseBroadcastOperaData.naviCruiseType) {
            0 -> { //全部
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeyRoadWarn,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeySafeBroadcast,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeyDriveWarn,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
            }

            1 -> { //路况播报
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeyRoadWarn,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
            }

            2 -> { //电⼦眼播报
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeySafeBroadcast,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
            }

            3 -> { //安全警示
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeyDriveWarn,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
            }

            4 -> { //前⽅路况+电⼦眼播报
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeyRoadWarn,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeySafeBroadcast,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
            }

            5 -> { //前⽅路况+安全提醒
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeyRoadWarn,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeyDriveWarn,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
            }

            6 -> { //电⼦眼播报+安全提醒
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeySafeBroadcast,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
                navigationSettingBusiness.cruiseBroadcastSelect(
                    ConfigKey.ConfigKeyDriveWarn,
                    cruiseBroadcastOperaData.operaType == 1 //设置需要反过来
                ) //0：打开1：关闭
            }
        }

        responseMessage(
            ProtocolID.PROTOCOL_CRUISE_PLAY_TYPE,
            cruiseBroadcastOperaData.apply {
                resultCode = StandardJsonConstant.ResponseResult.OK.code
                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                isSuccess = true
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_SET_MUTE: Int = 30006 指令处理 语⾳设置--静音操作
     */
    private suspend fun protocolVoiceMuteOperaExecute(
        pkg: String,
        requestCode: String?,
        voiceMuteOperaData: VoiceMuteOperaData?
    ) {
        if (voiceMuteOperaData == null) {
            Timber.i("protocolVoiceMuteOperaExecute voiceMuteOperaData == null")
            return
        }

        navigationSettingBusiness.setConfigKeyMute(if (voiceMuteOperaData.operaType == 1 || voiceMuteOperaData.operaType == 2) 1 else 0) //1永久静⾳ 2临时静⾳ 3取消永久静⾳ 4取消临时静⾳
        responseMessage(
            ProtocolID.PROTOCOL_SET_MUTE,
            voiceMuteOperaData.apply {
                resultCode = StandardJsonConstant.ResponseResult.OK.code
                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_ON_NAVI_ROUTE_PREFER: Int = 30405 指令处理 导航中路线偏好设置
     */
    private suspend fun protocolOnNaviRoutePreferOperaExecute(
        pkg: String,
        requestCode: String?,
        onNaviRoutePreferOperaData: OnNaviRoutePreferOperaData?,
        protocolId: Int
    ) {
        if (onNaviRoutePreferOperaData == null) {
            Timber.i("protocolOnNaviRoutePreferOperaExecute onNaviRoutePreferOperaData == null")
            return
        }
        var naviRoutePreferOperaData = NaviRoutePreferOperaData()
        if (onNaviRoutePreferOperaData.strategy != -1) {
            naviRoutePreferOperaData.routePreferSet = onNaviRoutePreferOperaData.strategy
        } else if (onNaviRoutePreferOperaData.newStrategy != -100) {
            naviRoutePreferOperaData.routePreferSet = onNaviRoutePreferOperaData.newStrategy
        }
        protocolNaviRoutePreferOperaExecute(pkg, requestCode, naviRoutePreferOperaData, protocolId)
    }

    /**
     * PROTOCOL_NAVI_ROUTE_PREFER: Int = 80179 指令处理 导航偏好设置
     */
    private suspend fun protocolNaviRoutePreferOperaExecute(
        pkg: String,
        requestCode: String?,
        naviRoutePreferOperaData: NaviRoutePreferOperaData?,
        protocolId: Int
    ) {
        if (naviRoutePreferOperaData == null) {
            Timber.i("protocolNaviRoutePreferOperaExecute naviRoutePreferOperaData == null")
            return
        }
        if (naviRoutePreferOperaData.routePreferSet != -1) {
            val configRoutePreferenceValue =
                CommonUtils.getConfigRoutePreferenceValue(naviRoutePreferOperaData.routePreferSet)
            navigationSettingBusiness.checkAndSavePrefer(configRoutePreferenceValue)
            if (naviBusiness.isNavigating()) {
                naviBusiness.onReRouteFromPlanPref(navigationSettingBusiness.routePreference)
            }
        }
        responseMessage(
            protocolId,
            naviRoutePreferOperaData.apply {
                resultCode =
                    if (netWorkManager.isNetworkConnected()) StandardJsonConstant.ResponseResult.OK.code else StandardJsonConstant.ResponseResult.FAIL_10009.code
                errorMessage =
                    if (netWorkManager.isNetworkConnected()) StandardJsonConstant.ResponseResult.OK.msg else CommonUtils.getConfigRoutePreferenceInfo(
                        routePreferSet
                    )
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_FAVORITE_ANY_POI: Int = 30509 指令处理 收藏任意点
     */
    private suspend fun protocolFavoriteAnyPointOperaExecute(
        pkg: String,
        requestCode: String?,
        favoriteAnyPointOperaData: FavoriteAnyPointOperaData?
    ) {
        if (favoriteAnyPointOperaData == null) {
            Timber.i("protocolFavoriteAnyPointOperaExecute favoriteAnyPointOperaData == null")
            return
        }
        Timber.i("protocolFavoriteAnyPointOperaExecute favoriteAnyPointOperaData:${gson.toJson(favoriteAnyPointOperaData)}")
        try {
            val coord3DDouble = if (favoriteAnyPointOperaData.isDev) {
                locationBusiness.encryptLonLat(Coord3DDouble(favoriteAnyPointOperaData.longitude, favoriteAnyPointOperaData.latitude, 0.0))
            } else {
                Coord3DDouble(favoriteAnyPointOperaData.longitude, favoriteAnyPointOperaData.latitude, 0.0)
            }

            if (TextUtils.isEmpty(favoriteAnyPointOperaData.poiName) || TextUtils.isEmpty(favoriteAnyPointOperaData.poiId)) {
                withContext(Dispatchers.IO) {
                    val result =
                        searchBusiness.nearestSearch(poi = POIFactory.createPOI(null, GeoPoint(coord3DDouble!!.lon, coord3DDouble!!.lat)))
                    when (result.status) {
                        Status.SUCCESS -> {
                            result.data?.let { searchNearestResult ->
                                val poiList = SearchCommonUtils.invertOrderList(searchNearestResult.poi_list)
                                if (TextUtils.isEmpty(favoriteAnyPointOperaData.poiName)) {
                                    favoriteAnyPointOperaData.poiName = poiList?.firstOrNull()?.name ?: "语音收藏的点"
                                }
                                if (TextUtils.isEmpty(favoriteAnyPointOperaData.poiId)) {
                                    favoriteAnyPointOperaData.poiId = poiList?.firstOrNull()?.poiid ?: ""
                                }
                            }
                        }

                        else -> {
                            Timber.i("nearestSearch Fail $result")
                            responseMessage(
                                ProtocolID.PROTOCOL_FAVORITE_ANY_POI,
                                favoriteAnyPointOperaData.apply {
                                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                                    type = favoriteType
                                },
                                pkg = pkg,
                                responseCode = requestCode
                            )
                            return@withContext
                        }
                    }
                }
            }

            val poi = POIFactory.createPOI(
                favoriteAnyPointOperaData.poiName,
                favoriteAnyPointOperaData.poiAddress,
                GeoPoint(coord3DDouble!!.lon, coord3DDouble!!.lat),
                favoriteAnyPointOperaData.poiId
            )
            val isFavorited = userBusiness.isFavorited(poi)
            val homePoi = userBusiness.getHomePoi()
            val companyPoi = userBusiness.getCompanyPoi()
            val isSuccess = handleFavorited(isFavorited, poi, homePoi, companyPoi, favoriteAnyPointOperaData.favoriteType)
            responseMessage(
                ProtocolID.PROTOCOL_FAVORITE_ANY_POI,
                favoriteAnyPointOperaData.apply {
                    resultCode = getResponseCode(isSuccess, favoriteAnyPointOperaData.favoriteType)
                    errorMessage = getErrorMessage(isSuccess, favoriteAnyPointOperaData.favoriteType)
                    type = favoriteType
                },
                pkg = pkg,
                responseCode = requestCode
            )
            favoritesUpdate.postValue(true)
        } catch (e: Exception) {
            Timber.i("protocolFavoriteAnyPointOperaExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_FAVORITE_ANY_POI,
                favoriteAnyPointOperaData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                    type = favoriteType
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    /**
     * PROTOCOL_FAVORITE_CURRENT_POI: Int = 30004 指令处理 收藏当前点
     */
    private suspend fun protocolFavoriteCurrentExecute(pkg: String, requestCode: String?, favoriteCurrentData: FavoriteCurrentData?) {
        if (favoriteCurrentData == null) {
            Timber.i("protocolFavoriteCurrentExecute favoriteCurrentData == null")
            return
        }

        try {
            val lastLocation = locationBusiness.getLastLocation()

            withContext(Dispatchers.IO) {
                val result =
                    searchBusiness.nearestSearch(poi = POIFactory.createPOI(null, GeoPoint(lastLocation.longitude, lastLocation.latitude)))
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { searchNearestResult ->
                            val poiList = SearchCommonUtils.invertOrderList(searchNearestResult.poi_list)
                            val poi = POIFactory.createPOI(
                                poiList?.firstOrNull()?.name,
                                poiList?.firstOrNull()?.address,
                                GeoPoint(
                                    lastLocation.longitude,
                                    lastLocation.latitude
                                ),
                                poiList?.firstOrNull()?.poiid
                            )
                            val isFavorited = userBusiness.isFavorited(poi)
                            val homePoi = userBusiness.getHomePoi()
                            val companyPoi = userBusiness.getCompanyPoi()

                            val isSuccess = if (isFavorited) {
                                handleFavorited(true, poi, homePoi, companyPoi, 0)
                            } else {
                                handleNotFavorited(poi, 0)
                            }

                            responseMessage(
                                ProtocolID.PROTOCOL_FAVORITE_CURRENT_POI,
                                favoriteCurrentData.apply {
                                    resultCode =
                                        if (isSuccess) StandardJsonConstant.ResponseResult.OK.code else StandardJsonConstant.ResponseResult.FAIL_10032.code
                                    errorMessage =
                                        if (isSuccess) StandardJsonConstant.ResponseResult.OK.msg else StandardJsonConstant.ResponseResult.FAIL_10032.msg
                                    isFavoriteSuccess = if (isSuccess) 1 else 2
                                    poiId = if (isSuccess) poiList?.firstOrNull()?.poiid.toString() else ""
                                    poiName = if (isSuccess) poiList?.firstOrNull()?.name.toString() else ""
                                    addr = if (isSuccess) poiList?.firstOrNull()?.address.toString() else ""
                                    phone = if (isSuccess) poiList?.firstOrNull()?.tel.toString() else ""
                                    lat = if (isSuccess) lastLocation.latitude else 0.0
                                    lon = if (isSuccess) lastLocation.longitude else 0.0
                                },
                                pkg = pkg,
                                responseCode = requestCode
                            )

                        }
                    }

                    else -> {
                        Timber.i("nearestSearch Fail $result")
                        responseMessage(
                            ProtocolID.PROTOCOL_FAVORITE_CURRENT_POI,
                            favoriteCurrentData.apply {
                                resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                            },
                            pkg = pkg,
                            responseCode = requestCode
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.i("protocolFavoriteCurrentExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_FAVORITE_CURRENT_POI,
                favoriteCurrentData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    private fun handleFavorited(isFavorited: Boolean, poi: POI, homePoi: POI?, companyPoi: POI?, favoriteType: Int): Boolean {
        Timber.i("protocolFavoriteCurrentExecute handleFavorited poi.id:${poi.id} homePoi?.id:${homePoi?.id}, companyPoi?.id:${companyPoi?.id}")

        val result = when (favoriteType) {
            0 -> { //普通收藏
                if (isFavorited) {
                    userBusiness.updateFavorite(
                        ConverUtils.converPOIToFavoriteItem(poi, FavoriteType.FavoriteTypePoi),
                        SyncMode.SyncModeNow
                    ) == Service.ErrorCodeOK
                } else {
                    userBusiness.addFavorite(poi, SyncMode.SyncModeNow, FavoriteType.FavoriteTypePoi)
                }
            }

            1 -> { //家
                val homeList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
                val flag = if (isFavorited) {
                    if (!homeList.isNullOrEmpty()) {
                        userBusiness.removeFavorites(homeList)
                    }
                    val result =
                        userBusiness.updateFavorite(ConverUtils.converPOIToFavoriteItem(poi, FavoriteType.FavoriteTypeHome), SyncMode.SyncModeNow)
                    result == Service.ErrorCodeOK
                } else {
                    if (!homeList.isNullOrEmpty()) {
                        userBusiness.removeFavorites(homeList)
                        userBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeHome)
                    } else {
                        userBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeHome)
                    }
                }
                Timber.i("addHome $flag")
                flag
            }

            2 -> { //公司
                val companyList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
                val flag = if (isFavorited) {
                    if (!companyList.isNullOrEmpty()) {
                        userBusiness.removeFavorites(companyList)
                    }

                    Timber.i(
                        "updateFavorite FavoriteItem = ${
                            gson.toJson(
                                ConverUtils.converPOIToFavoriteItem(
                                    poi,
                                    FavoriteType.FavoriteTypeCompany
                                )
                            )
                        }"
                    )

                    val result =
                        userBusiness.updateFavorite(ConverUtils.converPOIToFavoriteItem(poi, FavoriteType.FavoriteTypeCompany), SyncMode.SyncModeNow)
                    result == Service.ErrorCodeOK
                } else {
                    if (!companyList.isNullOrEmpty()) {
                        userBusiness.removeFavorites(companyList)
                        userBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeCompany)
                    } else {
                        userBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeCompany)
                    }
                }
                Timber.i("addCompany $flag")
                flag
            }

            else -> {
                true
            }
        }
        return result
    }

    private fun handleNotFavorited(poi: POI, favoriteType: Int): Boolean {
        return userBusiness.addFavorite(
            poi, type = when (favoriteType) {
                0 -> FavoriteType.FavoriteTypePoi
                1 -> FavoriteType.FavoriteTypeHome
                2 -> FavoriteType.FavoriteTypeCompany
                else -> return false
            }
        )
    }

    private fun isSamePoi(poi1: POI?, poi2: POI?): Boolean {
        if (poi1 == null || poi2 == null) {
            return false
        }
        if (TextUtils.equals(poi1.id, poi2.id)) {
            return true
        }
        val poi1Point = Coord2DDouble(poi1.point.longitude, poi1.point.latitude)
        val poi2Point = Coord2DDouble(poi2.point.longitude, poi2.point.latitude)
        //小于10米也认为是同一个点
        return BizLayerUtil.calcDistanceBetweenPoints(poi1Point, poi2Point).toInt() < 10
    }

    private fun getResponseCode(isSuccess: Boolean, favoriteType: Int): Int {
        return when (favoriteType) {
            0 -> if (isSuccess) StandardJsonConstant.ResponseResult.FAIL_10037.code else StandardJsonConstant.ResponseResult.FAIL_10032.code
            1 -> if (isSuccess) StandardJsonConstant.ResponseResult.FAIL_10035.code else StandardJsonConstant.ResponseResult.FAIL_10032.code
            2 -> if (isSuccess) StandardJsonConstant.ResponseResult.FAIL_10036.code else StandardJsonConstant.ResponseResult.FAIL_10032.code
            else -> StandardJsonConstant.ResponseResult.FAIL_10032.code
        }
    }

    private fun getErrorMessage(isSuccess: Boolean, favoriteType: Int): String {
        return when (favoriteType) {
            0 -> if (isSuccess) StandardJsonConstant.ResponseResult.FAIL_10037.msg else StandardJsonConstant.ResponseResult.FAIL_10032.msg
            1 -> if (isSuccess) StandardJsonConstant.ResponseResult.FAIL_10035.msg else StandardJsonConstant.ResponseResult.FAIL_10032.msg
            2 -> if (isSuccess) StandardJsonConstant.ResponseResult.FAIL_10036.msg else StandardJsonConstant.ResponseResult.FAIL_10032.msg
            else -> StandardJsonConstant.ResponseResult.FAIL_10032.msg
        }
    }

    /**
     * PROTOCOL_OTHER_QUERYSAVES: Int = 80072 指令处理 收藏点信息查询
     */
    private suspend fun protocolFavoriteGetExecute(
        pkg: String,
        requestCode: String?,
        favoriteGetData: FavoriteGetData?
    ) {
        if (favoriteGetData == null) {
            Timber.i("protocolFavoriteGetExecute favoriteGetData == null")
            return
        }
        try {
            val favoriteData: MutableList<FavoriteDetailData> = arrayListOf()
            val lastLocation = locationBusiness.getLastLocation()
            val startPoint = Coord2DDouble(lastLocation.longitude, lastLocation.latitude)

            fun addPoiToFavorites(poi: POI) {
                val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
                favoriteData.add(
                    FavoriteDetailData(
                        poi.id, poi.name, poi.addr, poi.point.latitude, poi.point.longitude,
                        BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint).toInt(),
                        poi.phone, 0
                    )
                )
            }

            val favoritePoi = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypePoi, true) ?: ArrayList()
            val home = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)?.first()
            val company = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)?.first()
            if (favoritePoi.isNotEmpty()) {
                favoritePoi.forEach { simpleFavoriteItem ->
                    val poi = ConverUtils.converSimpleFavoriteToPoi(simpleFavoriteItem)
                    addPoiToFavorites(poi)
                }
            }
            val homePoi = home?.let { ConverUtils.converSimpleFavoriteToPoi(it) }
            val companyPoi = company?.let { ConverUtils.converSimpleFavoriteToPoi(it) }
            when {
                homePoi != null && companyPoi == null -> {
                    addPoiToFavorites(homePoi)
                }

                companyPoi != null && homePoi == null -> {
                    addPoiToFavorites(companyPoi)
                }

                homePoi != null && companyPoi != null -> {
                    addPoiToFavorites(homePoi)
                    addPoiToFavorites(companyPoi)
                }
            }
            responseMessage(
                ProtocolID.PROTOCOL_OTHER_QUERYSAVES,
                favoriteGetData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.OK.code
                    errorMessage = if (favoriteData.size > 0) StandardJsonConstant.ResponseResult.OK.msg else "收藏夹没有收藏点"
                    this.favoriteData = favoriteData
                },
                pkg = pkg,
                responseCode = requestCode
            )
        } catch (e: Exception) {
            Timber.i("protocolFavoriteHomeCompanyExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_OTHER_QUERYSAVES,
                favoriteGetData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    /**
     * PROTOCOL_OTHER_NEWQUERYSAVES: Int = 80139 指令处理 收藏点信息查询
     */
    private suspend fun protocolFavoriteInformationInquiryExecute(
        pkg: String,
        requestCode: String?,
        favoriteInformationInquiryData: FavoriteInformationInquiryData?
    ) {
        if (favoriteInformationInquiryData == null) {
            Timber.i("protocolFavoriteInformationInquiryExecute favoriteInformationInquiryData == null")
            return
        }
        try {
            val newFavoriteData: MutableList<NewFavoriteData> = arrayListOf()
            val lastLocation = locationBusiness.getLastLocation()
            val startPoint = Coord2DDouble(lastLocation.longitude, lastLocation.latitude)

            fun addPoiToFavorites(poi: POI, isHome: Int) {
                val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
                newFavoriteData.add(
                    NewFavoriteData(
                        poi.id, poi.name, poi.addr, poi.point.latitude, poi.point.longitude,
                        poi.point.latitude, poi.point.longitude,
                        BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint).toInt(),
                        poi.phone, 0, isHome
                    )
                )
            }

            when (favoriteInformationInquiryData.requestType) {
                0 -> { //请求收藏夹内容（不包括家和公司）
                    val favoritePoi = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypePoi, true) ?: ArrayList()
                    if (favoritePoi.isNotEmpty()) {
                        val limitedPoi = favoritePoi.take(favoriteInformationInquiryData.maxCount)
                        limitedPoi.forEach { simpleFavoriteItem ->
                            val poi = ConverUtils.converSimpleFavoriteToPoi(simpleFavoriteItem)
                            addPoiToFavorites(poi, 0)
                        }
                    }
                }

                1 -> { //请求收藏夹内容（包括家和公司）
                    val favoritePoi = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypePoi, true) ?: ArrayList()
                    val home = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)?.first()
                    val company = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)?.first()
                    if (favoritePoi.isNotEmpty()) {
                        favoritePoi.forEach { simpleFavoriteItem ->
                            val poi = ConverUtils.converSimpleFavoriteToPoi(simpleFavoriteItem)
                            addPoiToFavorites(poi, 0)
                        }
                    }
                    val homePoi = home?.let { ConverUtils.converSimpleFavoriteToPoi(it) }
                    val companyPoi = company?.let { ConverUtils.converSimpleFavoriteToPoi(it) }
                    when {
                        homePoi != null && companyPoi == null -> {
                            if (newFavoriteData.size > favoriteInformationInquiryData.maxCount - 1) {
                                newFavoriteData.subList(favoriteInformationInquiryData.maxCount - 1, favoritePoi.size).clear()
                            }
                            addPoiToFavorites(homePoi, 1)
                        }

                        companyPoi != null && homePoi == null -> {
                            if (newFavoriteData.size > favoriteInformationInquiryData.maxCount - 1) {
                                newFavoriteData.subList(favoriteInformationInquiryData.maxCount - 1, favoritePoi.size).clear()
                            }
                            addPoiToFavorites(companyPoi, 2)
                        }

                        homePoi != null && companyPoi != null -> {
                            if (newFavoriteData.size > favoriteInformationInquiryData.maxCount - 2) {
                                newFavoriteData.subList(favoriteInformationInquiryData.maxCount - 2, favoritePoi.size).clear()
                            }
                            addPoiToFavorites(homePoi, 1)
                            addPoiToFavorites(companyPoi, 2)
                        }

                        else -> {
                            if (newFavoriteData.size > favoriteInformationInquiryData.maxCount) {
                                newFavoriteData.subList(favoriteInformationInquiryData.maxCount, favoritePoi.size).clear()
                            }
                        }
                    }
                }

                2 -> { //请求家
                    val home = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)?.first()
                    if (home != null) {
                        val homePoi = ConverUtils.converSimpleFavoriteToPoi(home)
                        addPoiToFavorites(homePoi, 1)
                    }
                }

                3 -> { //请求公司
                    val company = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)?.first()
                    if (company != null) {
                        val companyPoi = ConverUtils.converSimpleFavoriteToPoi(company)
                        addPoiToFavorites(companyPoi, 2)
                    }
                }

                4 -> { //请求家和公司
                    val home = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)?.first()
                    val company = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)?.first()
                    if (home != null) {
                        val homePoi = ConverUtils.converSimpleFavoriteToPoi(home)
                        addPoiToFavorites(homePoi, 1)
                    }
                    if (company != null) {
                        val companyPoi = ConverUtils.converSimpleFavoriteToPoi(company)
                        addPoiToFavorites(companyPoi, 2)
                    }
                }
            }
            responseMessage(
                ProtocolID.PROTOCOL_OTHER_NEWQUERYSAVES,
                favoriteInformationInquiryData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.OK.code
                    errorMessage = if (newFavoriteData.size > 0) StandardJsonConstant.ResponseResult.OK.msg else "收藏夹没有收藏点"
                    this.newFavoriteData = newFavoriteData
                },
                pkg = pkg,
                responseCode = requestCode
            )
        } catch (e: Exception) {
            Timber.i("protocolFavoriteInformationInquiryExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_OTHER_NEWQUERYSAVES,
                favoriteInformationInquiryData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    /**
     * PROTOCOL_GET_FAVORITE_POI: Int = 30507 指令处理 查询家和公司信息
     */
    private suspend fun protocolFavoriteHomeCompanyDataExecute(
        pkg: String,
        requestCode: String?,
        favoriteHomeCompanyData: FavoriteHomeCompanyData?
    ) {
        if (favoriteHomeCompanyData == null) {
            Timber.i("protocolFavoriteHomeCompanyDataExecute favoriteHomeCompanyData == null")
            return
        }
        try {
            val homeCompanyData: MutableList<HomeCompanyData> = arrayListOf()
            val lastLocation = locationBusiness.getLastLocation()
            val startPoint = Coord2DDouble(lastLocation.longitude, lastLocation.latitude)

            fun addPoiToFavorites(poi: POI, isHome: Int) {
                val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
                homeCompanyData.add(
                    HomeCompanyData(
                        isHome,
                        poi.id,
                        poi.name,
                        poi.addr,
                        BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint).toInt(),
                        poi.point.latitude,
                        poi.point.longitude,
                        poi.point.latitude,
                        poi.point.longitude,
                        favoriteHomeCompanyData.requestFavoriteType,
                        0
                    )
                )
            }

            when (favoriteHomeCompanyData.requestFavoriteType) {
                1 -> { //请求家
                    val home = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)?.first()
                    if (home != null) {
                        val homePoi = ConverUtils.converSimpleFavoriteToPoi(home)
                        addPoiToFavorites(homePoi, 1)
                    }
                }

                2 -> { //请求公司
                    val company = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)?.first()
                    if (company != null) {
                        val companyPoi = ConverUtils.converSimpleFavoriteToPoi(company)
                        addPoiToFavorites(companyPoi, 2)
                    }
                }

                3 -> { //请求家和公司
                    val home = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)?.first()
                    val company = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)?.first()
                    if (home != null) {
                        val homePoi = ConverUtils.converSimpleFavoriteToPoi(home)
                        addPoiToFavorites(homePoi, 1)
                    }
                    if (company != null) {
                        val companyPoi = ConverUtils.converSimpleFavoriteToPoi(company)
                        addPoiToFavorites(companyPoi, 2)
                    }
                }
            }
            responseMessage(
                ProtocolID.PROTOCOL_GET_FAVORITE_POI,
                favoriteHomeCompanyData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.OK.code
                    errorMessage = if (homeCompanyData.size > 0) StandardJsonConstant.ResponseResult.OK.msg else "收藏夹没有家或者公司"
                    this.protocolFavPoiInfos = homeCompanyData
                },
                pkg = pkg,
                responseCode = requestCode
            )
        } catch (e: Exception) {
            Timber.i("protocolFavoriteHomeCompanyDataExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_GET_FAVORITE_POI,
                favoriteHomeCompanyData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    /**
     * PROTOCOL_FAVORITE_CHANGE_NOTY: Int = 80172 指令处理 收藏点信息透出
     */
    suspend fun protocolFavoriteChangeExecute(
        favoritePoi: ArrayList<SimpleFavoriteItem> = arrayListOf(),
        home: SimpleFavoriteItem?,
        company: SimpleFavoriteItem?
    ) {
        try {
            val favoriteData: MutableList<FavoriteData> = arrayListOf()
            val lastLocation = locationBusiness.getLastLocation()
            val startPoint = Coord2DDouble(lastLocation.longitude, lastLocation.latitude)

            fun addPoiToFavorites(poi: POI, isHome: Int) {
                val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
                favoriteData.add(
                    FavoriteData(
                        poi.id, poi.name, poi.addr, poi.point.latitude, poi.point.longitude,
                        poi.point.latitude, poi.point.longitude,
                        BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint).toInt(),
                        poi.phone, 0, isHome
                    )
                )
            }

            if (favoritePoi.isNotEmpty()) {
                favoritePoi.forEach { simpleFavoriteItem ->
                    val poi = ConverUtils.converSimpleFavoriteToPoi(simpleFavoriteItem)
                    addPoiToFavorites(poi, 0)
                }
            }
            val homePoi = home?.let { ConverUtils.converSimpleFavoriteToPoi(it) }
            val companyPoi = company?.let { ConverUtils.converSimpleFavoriteToPoi(it) }
            when {
                homePoi != null && companyPoi == null -> {
                    if (favoriteData.size > 19) {
                        favoriteData.subList(19, favoritePoi.size).clear()
                    }
                    addPoiToFavorites(homePoi, 1)
                }

                companyPoi != null && homePoi == null -> {
                    if (favoriteData.size > 19) {
                        favoriteData.subList(19, favoritePoi.size).clear()
                    }
                    addPoiToFavorites(companyPoi, 2)
                }

                homePoi != null && companyPoi != null -> {
                    if (favoriteData.size > 18) {
                        favoriteData.subList(18, favoritePoi.size).clear()
                    }
                    addPoiToFavorites(homePoi, 1)
                    addPoiToFavorites(companyPoi, 2)
                }

                else -> {
                    if (favoriteData.size > 20) {
                        favoriteData.subList(20, favoritePoi.size).clear()
                    }
                }
            }
            dispatchMessage(
                ProtocolID.PROTOCOL_FAVORITE_CHANGE_NOTY,
                FavoriteChangeData().apply {
                    resultCode = StandardJsonConstant.ResponseResult.OK.code
                    errorMessage = if (favoriteData.size > 0) StandardJsonConstant.ResponseResult.OK.msg else "收藏夹没有收藏点"
                    message = if (favoriteData.size > 0) StandardJsonConstant.ResponseResult.OK.msg else "收藏夹没有收藏点"
                    this.favoriteData = favoriteData
                }
            )
        } catch (e: Exception) {
            Timber.i("protocolFavoriteChangeExecute Exception:${e.message}")
            dispatchMessage(
                ProtocolID.PROTOCOL_FAVORITE_CHANGE_NOTY,
                FavoriteChangeData().apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                    message = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                }
            )
        }
    }

    /**
     * PROTOCOL_HOME_COMPANY_FAVORITE_CHANGE_NOTY: Int = 30508 指令处理 家和公司变更通知
     */
    suspend fun protocolFavoriteHomeCompanyChangeExecute(favoriteType: Int, editType: Int, editTypeText: String, poi: POI?) {
        try {
            val lastLocation = locationBusiness.getLastLocation()
            val startPoint = Coord2DDouble(lastLocation.longitude, lastLocation.latitude)

            if (poi != null && (favoriteType == FavoriteType.FavoriteTypeHome || favoriteType == FavoriteType.FavoriteTypeCompany)) {
                val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
                val favoriteHomeCompanyChangeData = FavoriteHomeCompanyChangeData(
                    editType, favoriteType, poi.name, poi.id, poi.addr, BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint).toInt()
                )
                dispatchMessage(
                    ProtocolID.PROTOCOL_HOME_COMPANY_FAVORITE_CHANGE_NOTY,
                    favoriteHomeCompanyChangeData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.OK.code
                        errorMessage = editTypeText
                        message = editTypeText
                    }
                )
            }
        } catch (e: Exception) {
            Timber.i("protocolFavoriteHomeCompanyChangeExecute Exception:${e.message}")
            dispatchMessage(
                ProtocolID.PROTOCOL_HOME_COMPANY_FAVORITE_CHANGE_NOTY,
                FavoriteHomeCompanyChangeData().apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                    message = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                }
            )
        }
    }

    /**
     * PROTOCOL_REQUEST_ROUTE_EX: Int = 30402 指令处理 发起路线规划
     */
    private suspend fun protocolPlanRouteExecute(
        pkg: String,
        requestCode: String?,
        planRouteData: PlanRouteData?
    ) {
        if (planRouteData == null) {
            Timber.i("protocolPlanRouteExecute planRouteData == null")
            return
        }
        try {
            val actionType = planRouteData.actionType
            Timber.i("protocolPlanRouteExecute is called actionType =  ${actionType} ")
            when (actionType) {
                0, 1 -> {
                    if (!defaultMapCommand.openMap()) {
                        responseMessage(
                            ProtocolID.PROTOCOL_REQUEST_ROUTE_EX,
                            ResponsePlanRouteData().apply {
                                resultCode = StandardJsonConstant.ResponseResult.FAIL_10003.code
                                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10003.msg
                            },
                            pkg = pkg,
                            responseCode = requestCode
                        )
                        return
                    }
                    //起点POI
                    val startPoi: POI? = planRouteData.startProtocolPoi?.let { poi ->
                        val coord3DDouble = if (planRouteData.dev == 1) {
                            locationBusiness.encryptLonLat(Coord3DDouble(poi.longitude, poi.latitude, 0.0))
                        } else {
                            Coord3DDouble(poi.longitude, poi.latitude, 0.0)
                        }
                        POIFactory.createPOI(poi.poiName, poi.address, coord3DDouble?.let { GeoPoint(it.lon, coord3DDouble.lat) })
                    }

                    //终点
                    val endPoi: POI? = planRouteData.endProtocolPoi?.let { poi ->
                        val coord3DDouble = if (planRouteData.dev == 1) {
                            locationBusiness.encryptLonLat(Coord3DDouble(poi.longitude, poi.latitude, 0.0))
                        } else {
                            Coord3DDouble(poi.longitude, poi.latitude, 0.0)
                        }
                        POIFactory.createPOI(poi.poiName, poi.address, coord3DDouble?.let { GeoPoint(it.lon, coord3DDouble.lat) })
                    }

                    //途经点
                    val midPois: List<POI> = planRouteData.midProtocolPois?.takeIf { it.isNotEmpty() }?.map { midPoi ->
                        val coord3DDouble = if (planRouteData.dev == 1) {
                            locationBusiness.encryptLonLat(Coord3DDouble(midPoi.longitude, midPoi.latitude, 0.0))
                        } else {
                            Coord3DDouble(midPoi.longitude, midPoi.latitude, 0.0)
                        }
                        POIFactory.createPOI(midPoi.poiName, midPoi.address, coord3DDouble?.let { GeoPoint(it.lon, coord3DDouble.lat) })
                    } ?: emptyList()


                    //路线偏好
                    var naviRoutePreferOperaData = -1
                    if (planRouteData.strategy != -1) {
                        naviRoutePreferOperaData = planRouteData.strategy
                    } else if (planRouteData.newStrategy != -100) {
                        naviRoutePreferOperaData = planRouteData.newStrategy
                    }
                    if (naviRoutePreferOperaData != -1) {
                        val configRoutePreferenceValue = CommonUtils.getConfigRoutePreferenceValue(naviRoutePreferOperaData)
                        navigationSettingBusiness.checkAndSavePrefer(configRoutePreferenceValue, isRouteNotice = false)
                    }

                    if (endPoi == null) {
                        Timber.i("protocolPlanRouteExecute endPoi == null")
                        responseMessage(
                            ProtocolID.PROTOCOL_REQUEST_ROUTE_EX,
                            ResponsePlanRouteData().apply {
                                resultCode = StandardJsonConstant.ResponseResult.FAIL_10001.code
                                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10001.msg
                            },
                            pkg = pkg,
                            responseCode = requestCode
                        )
                    } else {
                        val responseData = StandardResponseData(
                            pkg = pkg,
                            protocolId = ProtocolID.PROTOCOL_REQUEST_ROUTE_EX,
                            responseCode = requestCode,
                            data = ResponsePlanRouteData()
                        )
                        standardResponseList.add(responseData)
                        //导航不传起点
                        if (actionType == 1) {
                            //导航
                            defaultMapCommand.startNavi(endPoi, midPois)
                        } else {
                            //路线规划
                            defaultMapCommand.startPlanRoute(startPoi, endPoi, midPois)
                        }

                    }

                }

                else -> {
                    Timber.i("protocolPlanRouteExecute else is called")
                    responseMessage(
                        ProtocolID.PROTOCOL_REQUEST_ROUTE_EX,
                        ResponsePlanRouteData().apply {
                            resultCode = StandardJsonConstant.ResponseResult.FAIL_10028.code
                            errorMessage = StandardJsonConstant.ResponseResult.FAIL_10028.msg
                        },
                        pkg = pkg,
                        responseCode = requestCode
                    )
                }
            }

        } catch (e: Exception) {
            Timber.i("protocolPlanRouteExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_REQUEST_ROUTE_EX,
                ResponsePlanRouteData().apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    /**
     * PROTOCOL_REQUEST_ROUTE_EX: Int = 30402 应答 发起路线规划
     */
    private fun protocolPlanRouteResponse() {
        Timber.i("protocolPlanRouteResponse  is called")
        val mRouteCarResult = mRouteRequestController.carRouteResult
        mRouteCarResult?.let { carResult ->
            val variantPathWrapList = mRouteRequestController.carRouteResult.pathResult
            val mFromPoiAddr = carResult.fromPOI.addr
            val mFromPoiLatitude = carResult.fromPOI.point.latitude
            val mFromPoiLongitude = carResult.fromPOI.point.longitude
            val mFromPoiName = carResult.fromPOI.name
            val midPoiArray = if (carResult.midPois.isNullOrEmpty()) "" else gson.toJson(carResult.midPois)
            val midPoisNum = if (carResult.midPois.isNullOrEmpty()) 0 else carResult.midPois.size
            val mToPoiAddr = carResult.toPOI.addr
            val mToPoiLatitude = carResult.toPOI.point.latitude
            val mToPoiLongitude = carResult.toPOI.point.longitude
            val mToPoiName = carResult.toPOI.name
            val mProtocolRouteInfos: ArrayList<ProtocolRouteInfo> = arrayListOf()
            var mCount = 0
            variantPathWrapList?.let {
                val routePrefer = mSettingComponent.getConfigKeyPlanPref()
                mCount = it.size
                for (i in it.indices) {
                    val variantPathWrap = it[i]
                    val index: Short = 0
                    val labelInfo = variantPathWrap.getLabelInfo(index)
                    val content = labelInfo?.content ?: ""
                    val cityInfos: ArrayList<ProtocolCityInfo> = arrayListOf()
                    if (!variantPathWrap.cityAdcodeList.isNullOrEmpty()) {
                        for (adcode in variantPathWrap.cityAdcodeList) {
                            cityInfos.add(ProtocolCityInfo(SearchDataConvertUtils.getCityName(adcode.toInt())))
                        }
                    }
                    mProtocolRouteInfos.add(
                        ProtocolRouteInfo(
                            distance = variantPathWrap.length.toDouble(),
                            method = when (i) {
                                0 -> if (content.isEmpty()) "推荐" else content.replace("备选一", "推荐")
                                    .replace("备选方案一", "推荐")

                                1 -> if (content.isEmpty()) "备选一" else content.replace("备选二", "备选一")
                                    .replace("备选方案二", "备选一")

                                2 -> if (content.isEmpty()) "备选二" else content.replace("备选三", "备选二")
                                    .replace("备选方案三", "备选二")

                                else -> ""
                            },
                            newStrategy = CommonUtils.getConfigRoutePreferenceKey(routePrefer),
                            time = variantPathWrap.travelTime.toDouble(),
                            tolls = variantPathWrap.tollCost,
                            totalOddDistance = variantPathWrap.length.toString(),
                            trafficLights = variantPathWrap.trafficLightCount.toInt(),
                            viaCityNumbers = variantPathWrap.cityAdcodeList.size,
                            protocolCityInfos = cityInfos
                        )
                    )
                }
            }

            standardAsyncResponseMessage(ProtocolID.PROTOCOL_REQUEST_ROUTE_EX, data = ResponsePlanRouteData().apply {
                resultCode = StandardJsonConstant.ResponseResult.OK.code
                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                count = mCount
                fromPoiAddr = mFromPoiAddr
                fromPoiLatitude = mFromPoiLatitude
                fromPoiLongitude = mFromPoiLongitude
                fromPoiName = mFromPoiName
                this.midPoiArray = midPoiArray
                this.midPoisNum = midPoisNum
                protocolRouteInfos = mProtocolRouteInfos
                toPoiAddr = mToPoiAddr
                toPoiLatitude = mToPoiLatitude
                toPoiLongitude = mToPoiLongitude
                toPoiName = mToPoiName
            })
        }
    }

    /**
     * PROTOCOL_USER_OPENSAVES: Int = 80047 应答 打开收藏夹
     */
    private suspend fun protocolUserFavoriteExecute(pkg: String, requestCode: String?) {
        userBusiness.openUserFavorite.postValue(true)
        if (!defaultMapCommand.openMap()) {
            responseMessage(
                ProtocolID.PROTOCOL_USER_OPENSAVES,
                ResponseDataData().apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10003.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10003.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
            return
        }

        responseMessage(
            ProtocolID.PROTOCOL_USER_OPENSAVES,
            ResponseDataData().apply {
                resultCode = StandardJsonConstant.ResponseResult.OK.code
                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_CAR_GETPLATENUMBERFROMAUTO: Int = 80150 应答 第三⽅主动查询⻋牌号信息
     */
    private suspend fun protocolPlateNumberExecute(pkg: String, requestCode: String?) {
        val number = navigationSettingBusiness.getLicensePlateNumber()
        val hasNumber = TextUtils.isEmpty(number)
        Timber.d(" getVehicleNumber vehicleNum:$number")
        responseMessage(
            ProtocolID.PROTOCOL_CAR_GETPLATENUMBERFROMAUTO,
            PlateNumberData().apply {
                resultCode = if (hasNumber) StandardJsonConstant.ResponseResult.FAIL_10032.code else StandardJsonConstant.ResponseResult.OK.code
                errorMessage = if (hasNumber) StandardJsonConstant.ResponseResult.FAIL_10032.msg else StandardJsonConstant.ResponseResult.OK.msg
                this.plateNumber = number
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_BACK_TO_MAP: Int = 30001 应答 回地图
     */
    private suspend fun protocolBackToMapExecute(
        pkg: String,
        requestCode: String?,
        backToMapOperaData: BackToMapOperaData?
    ) {
        if (backToMapOperaData == null) {
            Timber.i("protocolBackToMapExecute backToMapOperaData == null")
            return
        }
        if (BaseConstant.MAP_APP_FOREGROUND) { //地图在前台
            if (naviBusiness.isRealNavi()) { //导航中
                naviBusiness.naviBackCurrentCarPosition()
                mapBusiness.backToNavi.postValue(true) //回到地图主图
            } else {
                val isShowActivateLayout = activationMapBusiness.isShowActivateLayout
                val isShowAgreementLayout = activationMapBusiness.isShowAgreementLayout
                if (isShowActivateLayout.value == false || isShowAgreementLayout.value == false) {
                    mapBusiness.backToMap.postValue(true) //回到地图主图
                }
            }
        } else { //地图在后台
            if (!defaultMapCommand.openMap()) { //打开地图失败
                responseMessage(
                    ProtocolID.PROTOCOL_BACK_TO_MAP,
                    backToMapOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10003.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10003.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
                return
            }
        }
        responseMessage(
            ProtocolID.PROTOCOL_BACK_TO_MAP,
            backToMapOperaData.apply {
                resultCode = StandardJsonConstant.ResponseResult.OK.code
                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_SHOW_MY_LOCATION: Int = 30002 应答 当前位置查询
     */
    private suspend fun protocolCurrentLocationExecute(
        pkg: String,
        requestCode: String?,
        currentLocationOperaData: CurrentLocationOperaData?
    ) {
        if (currentLocationOperaData == null) {
            Timber.i("protocolCurrentLocationExecute currentLocationOperaData == null")
            return
        }

        try {
            val lastLocation = locationBusiness.getLastLocation()
            withContext(Dispatchers.IO) {
                val result =
                    searchBusiness.nearestSearch(poi = POIFactory.createPOI(null, GeoPoint(lastLocation.longitude, lastLocation.latitude)))
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { searchNearestResult ->
                            val poiList = SearchCommonUtils.invertOrderList(searchNearestResult.poi_list)
                            if (poiList != null && poiList.size > 0) {

                                if (currentLocationOperaData.type == 0) {
                                    mapBusiness.hideMapPointCard()
                                    val poi = POIFactory.createPOI().apply {
                                        name = if (TextUtils.isEmpty(poiList[0].name)) searchNearestResult.desc else poiList[0].name
                                        addr =
                                            if (TextUtils.isEmpty(poiList[0].address)) "在" + poiList[0].name + "附近" else "在" + poiList[0].address + "附近"
                                        id = poiList[0].poiid
                                        typeCode = poiList[0].typecode
                                        phone = poiList[0].tel
                                        point = GeoPoint(
                                            lastLocation.longitude,
                                            lastLocation.latitude
                                        )
                                    }
                                    val poiCardData = MapPointCardData(MapPointCardData.PoiCardType.TYPE_CAR_LOC, poi)
                                    mapBusiness.showMapPointCard(poiCardData)
                                }
                                val currentLocationAddress = if (TextUtils.isEmpty(poiList[0].address)) poiList[0].name else poiList[0].address

                                currentLocationOperaData.apply {
                                    myLocationName = "在${currentLocationAddress}附近" //我的位置信息描述例如：“在厦⻔市软件园⼆期望海路59号附近”
                                    poiName =
                                        if (TextUtils.isEmpty(poiList[0].name)) searchNearestResult.desc else poiList[0].name //位置名称(兼容Android⼴播协议)
                                    longitude = lastLocation.longitude //当前位置的坐标，经度
                                    latitude = lastLocation.latitude //当前位置的坐标，纬度
                                    provinceName = searchNearestResult.province //当前位置的省份名
                                    provinceCode = searchNearestResult.provinceadcode.toString() //当前位置的省份编码
                                    cityCode = searchNearestResult.cityadcode.toString() //当前位置的城市编码
                                    districtCode = searchNearestResult.districtadcode.toString() //当前位置的地区编码
                                    fullAddress =
                                        searchNearestResult.province + searchNearestResult.city + searchNearestResult.district +
                                                if (searchNearestResult.district?.equals(currentLocationAddress) == true) "" else currentLocationAddress
                                    //当前位置的详细地址（包含省份城市信息） 暂不⽀持
                                    address = currentLocationAddress //当前位置的简要地址（不包含省份城市信息）
                                    cityName = searchNearestResult.city //城市名（520及以上⽀持）
                                    districtName = searchNearestResult.district //区县名（520及以上⽀持）
                                }
                                responseMessage(
                                    ProtocolID.PROTOCOL_SHOW_MY_LOCATION,
                                    currentLocationOperaData.apply {
                                        resultCode = StandardJsonConstant.ResponseResult.OK.code
                                        errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                                    },
                                    pkg = pkg,
                                    responseCode = requestCode
                                )
                            } else {
                                Timber.i("nearestSearch poiList 没有数据")
                                currentLocationFail(
                                    pkg,
                                    requestCode,
                                    currentLocationOperaData
                                ) // PROTOCOL_SHOW_MY_LOCATION: Int = 30002 应答 当前位置查询 查询失败
                            }
                        }
                    }

                    else -> {
                        Timber.i("nearestSearch Fail $result")
                        currentLocationFail(pkg, requestCode, currentLocationOperaData) // PROTOCOL_SHOW_MY_LOCATION: Int = 30002 应答 当前位置查询 查询失败
                    }
                }
            }
        } catch (e: Exception) {
            Timber.i("protocolCurrentLocationExecute Exception:${e.message}")
            currentLocationFail(pkg, requestCode, currentLocationOperaData) // PROTOCOL_SHOW_MY_LOCATION: Int = 30002 应答 当前位置查询 查询失败
        }
    }

    // PROTOCOL_SHOW_MY_LOCATION: Int = 30002 应答 当前位置查询 查询失败
    private suspend fun currentLocationFail(
        pkg: String,
        requestCode: String?,
        currentLocationOperaData: CurrentLocationOperaData
    ) {
        responseMessage(
            ProtocolID.PROTOCOL_SHOW_MY_LOCATION,
            currentLocationOperaData.apply {
                resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_TRIP_ENDNAVIGATION: Int = 80078
     * 三⽅通知auto结束引导，退出导航状态，回到主图界⾯
     */
    private suspend fun protocolEndNavigationExecute(pkg: String, requestCode: String?, responseDataData: ResponseDataData?) {
        if (responseDataData == null) {
            Timber.i("protocolEndNavigationExecute responseDataData == null")
            return
        }
        try {
            if (naviBusiness.isNavigating()) {
                naviBusiness.stopNavi()
                responseMessage(
                    ProtocolID.PROTOCOL_TRIP_ENDNAVIGATION,
                    responseDataData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.OK.code
                        errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            } else {
                responseMessage(
                    ProtocolID.PROTOCOL_TRIP_ENDNAVIGATION,
                    responseDataData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            }
        } catch (e: Exception) {
            Timber.i("protocolEndNavigationExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_TRIP_ENDNAVIGATION,
                responseDataData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    /**
     * PROTOCOL_TRIP_ROUTEOVERVIEW: Int = 80076
     * 在且仅在导航场景下，通过第三⽅控制进⼊或退出全览状态
     */
    private suspend fun protocolRouteOverviewExecute(pkg: String, requestCode: String?, routeOverviewOperaData: RouteOverviewOperaData?) {
        if (routeOverviewOperaData == null) {
            Timber.i("protocolRouteOverviewExecute routeOverviewOperaData == null")
            return
        }
        try {
            val isShow = routeOverviewOperaData.isShow
            when (isShow) {
                0 -> { //0：进⼊全览
                    naviBusiness.showPreview(1)
                    responseMessage(
                        ProtocolID.PROTOCOL_TRIP_ROUTEOVERVIEW,
                        routeOverviewOperaData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.OK.code
                            errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                        },
                        pkg = pkg,
                        responseCode = requestCode
                    )
                }

                1 -> { //1：退出全览
                    naviBusiness.exitPreview()
                    responseMessage(
                        ProtocolID.PROTOCOL_TRIP_ROUTEOVERVIEW,
                        routeOverviewOperaData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.OK.code
                            errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                        },
                        pkg = pkg,
                        responseCode = requestCode
                    )
                }

                2 -> { //2：如果全览则切换⾮全览，如果⾮全览则切换全览
                    if (naviBusiness.inFullView.value == true) {
                        naviBusiness.exitPreview()
                    } else {
                        naviBusiness.showPreview(1)
                    }
                    responseMessage(
                        ProtocolID.PROTOCOL_TRIP_ROUTEOVERVIEW,
                        routeOverviewOperaData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.OK.code
                            errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                        },
                        pkg = pkg,
                        responseCode = requestCode
                    )
                }

                else -> {
                    Timber.i("protocolRouteOverviewExecute else is called")
                    responseMessage(
                        ProtocolID.PROTOCOL_TRIP_ROUTEOVERVIEW,
                        routeOverviewOperaData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                            errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.msg
                        },
                        pkg = pkg,
                        responseCode = requestCode
                    )
                }
            }
        } catch (e: Exception) {
            Timber.i("protocolRouteOverviewExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_TRIP_ROUTEOVERVIEW,
                routeOverviewOperaData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }

    }

    /**
     * PROTOCOL_ROUTE_SELECT: Int = 30404
     * 路径规划完成后在路线规划结果⻚⾯，第三⽅可选择路线并开始导航
     */
    private suspend fun protocolRouteSelectExecute(pkg: String, requestCode: String?, routeSelectOperaData: RouteSelectOperaData?) {
        if (routeSelectOperaData == null) {
            Timber.i("protocolRouteSelectExecute routeSelectOperaData == null")
            return
        }
        try {
            val successResponse = suspend {
                responseMessage(
                    ProtocolID.PROTOCOL_ROUTE_SELECT,
                    routeSelectOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.OK.code
                        errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            }

            val failure10001Response = suspend {
                responseMessage(
                    ProtocolID.PROTOCOL_ROUTE_SELECT,
                    routeSelectOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10001.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10001.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            }

            val failure10004Response = suspend {
                responseMessage(
                    ProtocolID.PROTOCOL_ROUTE_SELECT,
                    routeSelectOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            }

            val selectType = routeSelectOperaData.selectType
            val isStartNavi = routeSelectOperaData.isStartNavi
            val pathResultSize = mRouteRequestController.carRouteResult?.pathResult?.size ?: 0
            Timber.i("protocolRouteSelectExecute selectType = $selectType isStartNavi = $isStartNavi pathResultSize = $pathResultSize")
            if (routeBusiness.isPlanRouteing()) { //规划路线中
                when (selectType) {
                    0 -> { //0: 当前选中路线的“开始导航”操作
                        defaultMapCommand.startNaviWhenHaveRoute()
                        successResponse()
                    }

                    in 1..3 -> { //1：选中第1条线路 2：选中第2条路线 3：选中第3条路线
                        if (selectType > pathResultSize) {
                            failure10001Response()
                        } else {
                            defaultMapCommand.chooseRoute(selectType - 1)
                            delay(300)
                            if (isStartNavi) {
                                defaultMapCommand.startNaviWhenHaveRoute()
                            }
                            successResponse()
                        }
                    }

                    else -> {
                        failure10001Response()
                    }
                }
            } else { //未规划路线
                failure10004Response()
            }
        } catch (e: Exception) {
            Timber.i("protocolRouteSelectExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_ROUTE_SELECT,
                routeSelectOperaData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    /**
     * ProtocolID.PROTOCOL_MODIFY_NAVI_VIA 30409 操作途经点
     */
    private suspend fun protocolViaPoiOperaDataExecute(pkg: String, requestCode: String?, viaPoiOperaData: ViaPoiOperaData) {
        Timber.i("protocolViaPoiOperaDataExecute ${viaPoiOperaData.action} ${viaPoiOperaData.viaProtocolPoi}")
        val viaPoi = viaPoiOperaData.viaProtocolPoi?.let { protocolPoi ->
            POIFactory.createPOI().apply {
                id = protocolPoi.poiId
                point = if (viaPoiOperaData.dev == 1) {
                    locationBusiness.encryptLonLat(Coord3DDouble(protocolPoi.longitude, protocolPoi.latitude, 0.0))?.run {
                        GeoPoint(lon, lat)
                    }
                } else {
                    GeoPoint(protocolPoi.longitude, protocolPoi.latitude)
                }
                name = protocolPoi.poiName
                addr = protocolPoi.address
            }
        }

        val responseData = ViaPoiOperaData(action = viaPoiOperaData.action)
        val standardResponseData = StandardResponseData(
            pkg = pkg,
            protocolId = ProtocolID.PROTOCOL_MODIFY_NAVI_VIA,
            responseCode = requestCode,
            data = responseData
        )

        when (viaPoiOperaData.action) {
            //-1：删除所有途经点
            -1 -> {
                if (mRouteRequestController.carRouteResult != null) {
                    if ((mRouteRequestController.carRouteResult.midPois?.size ?: 0) > 0) {
                        if (routeBusiness.isPlanRouteing()) {
                            standardResponseList.add(standardResponseData)
                            routeBusiness.deleteAllViaPoi()
                        } else if (naviBusiness.isRealNavi()) {
                            standardResponseList.add(standardResponseData)
                            naviBusiness.deleteViaPoi(null)
                        } else {
                            responseData.apply {
                                resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.name
                            }
                            responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                        }
                    } else {
                        //没有途经点
                        responseData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.FAIL_10063.code
                            errorMessage = StandardJsonConstant.ResponseResult.FAIL_10063.name
                        }
                        responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                    }

                } else {
                    responseData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.name
                    }
                    responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                }

            }
            // 0：增加途经点（需要传⼊要增加的途经点的经纬度）
            0 -> {
                if (viaPoi == null) {
                    responseData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10015.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10015.name
                    }
                    responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                    return
                }

                if (mRouteRequestController.carRouteResult != null) {
                    if ((mRouteRequestController.carRouteResult.midPois?.size ?: 0) < 15) {
                        if (AutoRouteUtil.isSamePoi(mRouteRequestController.carRouteResult.toPOI, viaPoi)) {
                            responseData.apply {
                                resultCode = StandardJsonConstant.ResponseResult.FAIL_10011.code
                                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10011.name
                            }
                            responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                            return
                        }
                        mRouteRequestController.carRouteResult?.midPois?.forEach {
                            if (AutoRouteUtil.isSamePoi(it, viaPoi)) {
                                responseData.apply {
                                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10011.code
                                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10011.name
                                }
                                responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                                return
                            }
                        }
                        if (routeBusiness.isPlanRouteing()) {
                            standardResponseList.add(standardResponseData)
                            routeBusiness.addWayPoint(viaPoi)
                        } else if (naviBusiness.isRealNavi()) {
                            standardResponseList.add(standardResponseData)
                            naviBusiness.addWayPoint(viaPoi)
                        } else {
                            responseData.apply {
                                resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.name
                            }
                            responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                        }
                    } else {
                        //途经点已满
                        responseData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.FAIL_10012.code
                            errorMessage = StandardJsonConstant.ResponseResult.FAIL_10012.name
                        }
                        responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                        return
                    }

                } else {
                    responseData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.name
                    }
                    responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                }
            }

            //1：删除途经点（需要传⼊要删除的途经点的经纬度）
            1 -> {
                if (viaPoi == null) {
                    responseData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10015.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10015.name
                    }
                    responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                    return
                }

                if (mRouteRequestController.carRouteResult != null) {
                    if ((mRouteRequestController.carRouteResult.midPois?.size ?: 0) > 0) {
                        var index = -1
                        mRouteRequestController.carRouteResult.midPois?.let {
                            for (i in it.indices) {
                                if (AutoRouteUtil.isSamePoi(it[i], viaPoi)) {
                                    index = i
                                    break
                                }
                            }
                        }

                        if (index != -1) {
                            if (routeBusiness.isPlanRouteing()) {
                                standardResponseList.add(standardResponseData)
                                routeBusiness.deleteViaPoi(index)
                            } else if (naviBusiness.isNavigating()) {
                                standardResponseList.add(standardResponseData)
                                naviBusiness.deleteViaPoi(index)
                            } else {
                                responseData.apply {
                                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.name
                                }
                                responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                            }
                        } else {
                            responseData.apply {
                                resultCode = StandardJsonConstant.ResponseResult.FAIL_10015.code
                                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10015.name
                            }
                            responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                        }
                    } else {
                        //没有途经点
                        responseData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.FAIL_10063.code
                            errorMessage = StandardJsonConstant.ResponseResult.FAIL_10063.name
                        }
                        responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                    }
                } else {
                    responseData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.name
                    }
                    responseMessage(ProtocolID.PROTOCOL_MODIFY_NAVI_VIA, responseData, responseCode = requestCode)
                }

            }

            else -> {}
        }
    }

    /**
     * PROTOCOL_INFO_NOTIFY_TRAFFICRESTRICTQUERY: Int = 80097 应答 发起限⾏信息查询请求
     */
    private suspend fun protocolVehicleLimitExecute(
        pkg: String,
        requestCode: String?,
        vehicleLimitOperaData: VehicleLimitOperaData?
    ) {
        if (vehicleLimitOperaData == null) {
            Timber.i("protocolVehicleLimitExecute vehicleLimitOperaData == null")
            return
        }

        var adCode: Int = -1
        if (vehicleLimitOperaData.lon != 0.0 && vehicleLimitOperaData.lat != 0.0) {
            adCode = mapDataBusiness.getAdCodeByLonLat(vehicleLimitOperaData.lon, vehicleLimitOperaData.lat)
        }
        aosBusiness.sendReqTrafficRestrict(GTrafficRestrictRequestParam().apply {
            if (adCode != -1) {
                Adcode = adCode.toLong()
            }
            Lon = vehicleLimitOperaData.lon
            Lat = vehicleLimitOperaData.lat
            Date = vehicleLimitOperaData.date
            CarPlate = vehicleLimitOperaData.carPlateNumber
        }) { gTrafficRestrictResponseParam ->
            if (gTrafficRestrictResponseParam?.Restrict != null) {
                MainScope().launch {
                    responseMessage(
                        ProtocolID.PROTOCOL_INFO_NOTIFY_TRAFFICRESTRICTQUERY,
                        vehicleLimitOperaData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.OK.code
                            errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                            this.trafficRestrictInfoResult = gson.toJson(gTrafficRestrictResponseParam.Restrict)
                        },
                        pkg = pkg,
                        responseCode = requestCode
                    )
                }
            } else {
                MainScope().launch {
                    responseMessage(
                        ProtocolID.PROTOCOL_INFO_NOTIFY_TRAFFICRESTRICTQUERY,
                        vehicleLimitOperaData.apply {
                            resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                            errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                        },
                        pkg = pkg,
                        responseCode = requestCode
                    )
                }
            }
        }


    }

    /**
     * PROTOCOL_TRIP_ROUTECHANGE: Int = 80156
     * 当导航过程中图⾯出现备选路或动态算路时，通过此协议⽀持系统控制切换备选路线，切换后返回对应状态码
     */
    private suspend fun protocolRouteChangeExecute(pkg: String, requestCode: String?, routeChangeOperaData: RouteChangeOperaData?) {
        if (routeChangeOperaData == null) {
            Timber.i("protocolRouteChangeExecute routeChangeOperaData == null")
            return
        }
        try {
            val num = routeChangeOperaData.num
            val carRouteResult = mRouteRequestController.carRouteResult
            val pathResult = carRouteResult.pathResult
            val pathIDList = pathResult.map { pathInfo ->
                pathInfo.pathID
            }
            val curPathID = pathResult[carRouteResult.focusIndex].pathID

            Timber.i("protocolRouteChangeExecute pathIDList：${gson.toJson(pathIDList)} curPathID：$curPathID num：$num")

            if (pathIDList.contains(curPathID) && pathIDList.contains(num) && curPathID != num) {
                naviBusiness.changeNaviPath(num)
                responseMessage(
                    ProtocolID.PROTOCOL_TRIP_ROUTECHANGE,
                    routeChangeOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.OK.code
                        errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            } else {
                responseMessage(
                    ProtocolID.PROTOCOL_TRIP_ROUTECHANGE,
                    routeChangeOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10001.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10001.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            }
        } catch (e: Exception) {
            Timber.i("protocolRouteChangeExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_TRIP_ROUTECHANGE,
                routeChangeOperaData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }

    }

    /**
     * PROTOCOL_FRONT_TRAFFIC_RADIO: Int = 30408 应答 查询前⽅路况
     */
    private suspend fun protocolTRManualExecute(
        pkg: String,
        requestCode: String?,
        tRManualOperaData: TRManualOperaData?
    ) {
        if (tRManualOperaData == null) {
            Timber.i("protocolTRManualExecute tRManualOperaData == null")
            return
        }
        if (naviBusiness.isRealNavi() || cruiseBusiness.isCruising()) {
            BaseConstant.TR_TTS_BROADCAST = tRManualOperaData.ttsBroadcast
            BaseConstant.TR_PKG = pkg
            BaseConstant.TR_REQUEST_CODE = requestCode
            val result = mTtsPlayBusiness.playTrafficStatus()
            if (!result) {
                responseMessage(
                    ProtocolID.PROTOCOL_FRONT_TRAFFIC_RADIO,
                    tRManualOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            }
        } else {
            responseMessage(
                ProtocolID.PROTOCOL_FRONT_TRAFFIC_RADIO,
                tRManualOperaData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = "当前地图不在导航或者巡航中"
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }
    }

    /**
     * PROTOCOL_BACK_CURRENT_CAR_POSITION: Int = 90000
     * 地图返回⻋位视⻆的接⼝. (⽤于导航过程中⽤户⼿动操作地图缩放,平移等操作后的复位)
     */
    private suspend fun protocolBackCurrentCarPositionExecute(pkg: String, requestCode: String?, responseDataData: ResponseDataData?) {
        if (responseDataData == null) {
            Timber.i("protocolBackCurrentCarPositionExecute responseDataData == null")
            return
        }
        try {
            if (naviBusiness.isNavigating()) {
                mapBusiness.backCurrentCarPosition(false)
                responseMessage(
                    ProtocolID.PROTOCOL_BACK_CURRENT_CAR_POSITION,
                    responseDataData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.OK.code
                        errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            } else {
                responseMessage(
                    ProtocolID.PROTOCOL_BACK_CURRENT_CAR_POSITION,
                    responseDataData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                        errorMessage = "当前地图不在导航中"
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            }
        } catch (e: Exception) {
            Timber.i("protocolBackCurrentCarPositionExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_BACK_CURRENT_CAR_POSITION,
                responseDataData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }

    }

    suspend fun protocolKeywordSearchExecute(
        pkg: String,
        requestCode: String?,
        keywordSearchData: KeywordSearchData?
    ) {
        if (keywordSearchData == null) {
            Timber.i("protocolKeywordSearchExecute keywordSearchData == null")
            return
        }
        if (keywordSearchData.searchType != 0) {
            Timber.i("protocolKeywordSearchExecute keywordSearchData.searchType != 0, return")
            return
        }
        var poi: POI? = null
        if (keywordSearchData.mylocLon != null && keywordSearchData.mylocLat != null) {
            poi = POIFactory.createPOI(
                "当前位置",
                GeoPoint(
                    keywordSearchData.mylocLon,
                    keywordSearchData.mylocLat
                )
            )
        }

        val cityCode = keywordSearchData.city?.let {
            mapDataBusiness.searchAdCode(keywordSearchData.city)?.firstOrNull()
        }

        standardResponseList.add(
            StandardResponseData(
                pkg = pkg, protocolId = ProtocolID.PROTOCOL_KEYWORD_SEARCH, responseCode = requestCode, data =
                SearchResponseData()
            )
        )
        if (keywordSearchData.requestType == 0) { //0:应⽤外搜索
            withContext(Dispatchers.IO) {
                val result = searchBusiness.keywordSearchV2(
                    keyword = keywordSearchData.keywords,
                    curPoi = poi,
                    size = keywordSearchData.maxCount?.takeIf { it > 0 } ?: 10,
                    cityCode = cityCode,
                    range = if (keywordSearchData.needRange == 1) keywordSearchData.range else null
                )
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { data ->
                            protocolKeywordSearchResponse(data)
                        }
                    }

                    Status.ERROR -> {
                        Timber.i("startKeywordSearch Status.ERROR throwable:${result.throwable.toString()}")
                        standardAsyncResponseMessage(ProtocolID.PROTOCOL_KEYWORD_SEARCH, StandardJsonConstant.ResponseResult.FAIL_10032)
                    }

                    else -> {}
                }
            }

        } else if (keywordSearchData.requestType == 1) {
            defaultMapCommand.keywordSearch(keywordSearchData.keywords,true)
        }
    }

    /**
     * PROTOCOL_KEYWORD_SEARCH: Int = 30300 应答 发起关键字搜索
     */
    private fun protocolKeywordSearchResponse(result: KeywordSearchResultV2?) {
        Timber.i("protocolKeywordSearchResponse result = $result")
        if (result == null) {
            standardAsyncResponseMessage(ProtocolID.PROTOCOL_KEYWORD_SEARCH, StandardJsonConstant.ResponseResult.FAIL_10032)
        } else {
            standardAsyncResponseMessage(
                ProtocolID.PROTOCOL_KEYWORD_SEARCH,
                data = SearchResponseData(
                    getPoiResultFromSearchResult(result)
                ).apply {
                    resultCode = StandardJsonConstant.ResponseResult.OK.code
                    errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                }
            )
        }
    }

    private fun getPoiResultFromSearchEnrouteResult(result: SearchEnrouteResult?): PoiResult {
        val Citysuggestion = Citysuggestion()
        val Count = result?.poiInfos?.size ?: 0

        val Pois = result?.poiInfos?.map { poiInfo ->
            val startPoint = Coord2DDouble(locationBusiness.getLastLocation().longitude, locationBusiness.getLastLocation().latitude)
            val endPOIPoint = Coord2DDouble(poiInfo.basicInfo.location.lon, poiInfo.basicInfo.location.lat)
            Poi(
                Address = poiInfo.basicInfo.address,
                Latitude = poiInfo.basicInfo.location.lat,
                Name = poiInfo.basicInfo.name,
                Poiid = poiInfo.basicInfo.poiId,
                Tel = poiInfo.basicInfo.tel,
                Typecode = poiInfo.basicInfo.typeCode,
                biz_ext = BizExt(),
                childPoiList = poiInfo.childInfos?.map { childPoi ->
                    val endChildPoint = Coord2DDouble(childPoi.location.lon, childPoi.location.lat)
                    ChildPoi(
                        Address = childPoi.address,
                        Latitude = childPoi.location.lat,
                        Name = childPoi.name,
                        Poiid = childPoi.poiId,
                        Tel = null,
                        Typecode = null,
                        biz_ext = BizExt(),
                        distance = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endChildPoint).toInt(),
                        enteryList = null,
                        homecopType = 0,
                        longitude = childPoi.location.lat,
                    )
                },
                distaceToSearchLocation = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPOIPoint).toInt(),
                distance = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPOIPoint).toInt(),
                enteryList = poiInfo.basicInfo.entranceList.map {
                    EnteryX(
                        entry_latitude = it.lat,
                        entry_longitude = it.lon
                    )
                },
                homecopType = 0,
                longitude = poiInfo.basicInfo.location.lon
            )
        }

        val categories = result?.classify?.classifyItemInfo?.categoryInfoList?.map { categoryInfoList ->
            Category(
                name = categoryInfoList.baseInfo.name,
                checkedvalue = categoryInfoList.baseInfo.name,
                ctype = null,
                categoryItems = categoryInfoList?.childCategoryInfo?.map { childCategoryInfo ->
                    CategoryItem(
                        name = childCategoryInfo.baseInfo.name,
                        value = childCategoryInfo.baseInfo.value,
                        categoryItems = childCategoryInfo?.childCategoryInfoList?.map { childCategoryInfoList ->
                            CategoryItemX(
                                name = childCategoryInfoList.baseInfo.name,
                                value = childCategoryInfoList.baseInfo.value,
                            )
                        }
                    )
                }
            )
        }
        return PoiResult(
            Citysuggestion = Citysuggestion,
            Count = Count,
            Pois = Pois,
            categories = categories
        )


    }

    private fun getPoiResultFromSearchResult(searchResultV2: KeywordSearchResultV2?): PoiResult {
        val Citysuggestion = Citysuggestion(
            Citycount = searchResultV2?.poiSuggestion?.citySuggestion?.size ?: 0,
            SuggestionCityDetail = searchResultV2?.poiSuggestion?.citySuggestion?.map {
                SuggestionCityDetail(
                    Cityname = it.name,
                    Citynum = it.total
                )
            }
        )
        var Count = searchResultV2?.poiList?.size ?: 0

        var Pois = searchResultV2?.poiList?.map { poiInfo ->
            val startPoint = Coord2DDouble(locationBusiness.getLastLocation().longitude, locationBusiness.getLastLocation().latitude)
            val endPOIPoint = Coord2DDouble(poiInfo.basicInfo.location.lon, poiInfo.basicInfo.location.lat)
            Poi(
                Address = poiInfo.basicInfo.address,
                Latitude = poiInfo.basicInfo.location.lat,
                Name = poiInfo.basicInfo.name,
                Poiid = poiInfo.basicInfo.poiId,
                Tel = poiInfo.basicInfo.tel,
                Typecode = poiInfo.basicInfo.typeCode,
                biz_ext = BizExt(),
                childPoiList = poiInfo.childInfoList?.map { childPoi ->
                    val endChildPoint = Coord2DDouble(childPoi.location.lon, childPoi.location.lat)
                    ChildPoi(
                        Address = childPoi.address,
                        Latitude = childPoi.location.lat,
                        Name = childPoi.name,
                        Poiid = childPoi.poiId,
                        Tel = null,
                        Typecode = null,
                        biz_ext = BizExt(),
                        distance = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endChildPoint).toInt(),
                        enteryList = null,
                        homecopType = 0,
                        longitude = childPoi.location.lon,
                    )
                },
                distaceToSearchLocation = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPOIPoint).toInt(),
                distance = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPOIPoint).toInt(),
                enteryList = poiInfo.basicInfo.entranceList.map {
                    EnteryX(
                        entry_latitude = it.lat,
                        entry_longitude = it.lon
                    )
                },
                homecopType = 0,
                longitude = poiInfo.basicInfo.location.lon
            )
        }
        if (Count == 0 && (searchResultV2?.poiLocres?.total ?: 0) > 0) {
            val poi = SearchResultUtils.getInstance().resultCityToPoi(searchResultV2?.poiLocres)
            val startPoint = Coord2DDouble(locationBusiness.getLastLocation().longitude, locationBusiness.getLastLocation().latitude)
            val endPOIPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
            Pois = mutableListOf<Poi>().also {
                it.add(
                    Poi(
                        Address = poi.addr,
                        Latitude = poi.point.latitude,
                        Name = poi.name,
                        Poiid = poi.id,
                        Tel = poi.phone,
                        Typecode = poi.typeCode,
                        biz_ext = BizExt(),
                        childPoiList = poi.childPois?.map { childPoi ->
                            val endChildPoint = Coord2DDouble(childPoi.point.longitude, childPoi.point.latitude)
                            ChildPoi(
                                Address = childPoi.addr,
                                Latitude = childPoi.point.latitude,
                                Name = childPoi.name,
                                Poiid = childPoi.id,
                                Tel = null,
                                Typecode = null,
                                biz_ext = BizExt(),
                                distance = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endChildPoint).toInt(),
                                enteryList = null,
                                homecopType = 0,
                                longitude = childPoi.point.longitude,
                            )
                        },
                        distaceToSearchLocation = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPOIPoint).toInt(),
                        distance = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPOIPoint).toInt(),
                        enteryList = poi.entranceList?.map { geoPoint ->
                            EnteryX(
                                entry_latitude = geoPoint.latitude,
                                entry_longitude = geoPoint.longitude
                            )
                        },
                        homecopType = 0,
                        longitude = poi.point.longitude
                    )
                )
            }
            Count = 1
        }

        val categories = searchResultV2?.classify?.classifyItemInfo?.categoryInfoList?.map { categoryInfoList ->
            Category(
                name = categoryInfoList.baseInfo.name,
                checkedvalue = categoryInfoList.baseInfo.name,
                ctype = null,
                categoryItems = categoryInfoList?.childCategoryInfo?.map { childCategoryInfo ->
                    CategoryItem(
                        name = childCategoryInfo.baseInfo.name,
                        value = childCategoryInfo.baseInfo.value,
                        categoryItems = childCategoryInfo?.childCategoryInfoList?.map { childCategoryInfoList ->
                            CategoryItemX(
                                name = childCategoryInfoList.baseInfo.name,
                                value = childCategoryInfoList.baseInfo.value,
                            )
                        }
                    )
                }
            )
        }
        return PoiResult(
            Citysuggestion = Citysuggestion,
            Count = Count,
            Pois = Pois,
            categories = categories
        )


    }

    private suspend fun protocolAroundSearchExecute(
        pkg: String,
        requestCode: String?,
        aroundSearchData: AroundSearchData?
    ) {
        if (aroundSearchData == null) {
            Timber.i("protocolAroundSearchExecute keywordSearchData == null")
            return
        }
        if (aroundSearchData.searchType != 1) {
            Timber.i("protocolAroundSearchExecute keywordSearchData.searchType != 0, return")
            return
        }
        var curPoi: POI? = null

        if (aroundSearchData.mylocLon != null && aroundSearchData.mylocLat != null) {
            curPoi = POIFactory.createPOI(
                "当前位置",
                GeoPoint(
                    aroundSearchData.mylocLon,
                    aroundSearchData.mylocLat
                )
            )
        }
        // 如果有传入位置信息，使用传入的位置信息
        if (!TextUtils.isEmpty(aroundSearchData.location)) {
            val location = aroundSearchData.location?.split(",")
            if (location?.size == 2) {
                curPoi = POIFactory.createPOI(
                    "当前位置",
                    GeoPoint(
                        location[0].toDouble(),
                        location[1].toDouble()
                    )
                )
            }
        }
        standardResponseList.add(
            StandardResponseData(
                pkg = pkg, protocolId = ProtocolID.PROTOCOL_AROUNDSEARCH_SEARCH, responseCode = requestCode, data =
                SearchResponseData()
            )
        )
        if (aroundSearchData.requestType == 0) { //0:应⽤外搜索
            withContext(Dispatchers.IO) {
                val result = searchBusiness.keywordSearchV2(
                    searchQueryType = SearchQueryType.AROUND,
                    keyword = aroundSearchData.keywords,
                    curPoi = curPoi,
                    size = aroundSearchData.maxCount?.takeIf { it > 0 } ?: 10,
                    range = aroundSearchData.radius.takeIf { it in 1..50000 }?.toString() ?: "3000"
                )
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { data ->
                            protocolAroundSearchResponse(data)
                        }
                    }

                    Status.ERROR -> {
                        Timber.i("startKeywordSearch Status.ERROR throwable:${result.throwable.toString()}")
                        standardAsyncResponseMessage(ProtocolID.PROTOCOL_AROUNDSEARCH_SEARCH, StandardJsonConstant.ResponseResult.FAIL_10032)
                    }

                    else -> {}
                }
            }

        } else if (aroundSearchData.requestType == 1) {
            if (curPoi != null) {
                defaultMapCommand.aroundSearch(
                    aroundSearchData.keywords,
                    curPoi.point.longitude,
                    curPoi.point.latitude,
                    range = aroundSearchData.radius.takeIf { it in 1..50000 }?.toString() ?: "3000"
                )
            } else {
                defaultMapCommand.aroundSearch(aroundSearchData.keywords)
            }
        }
    }

    private suspend fun protocolAlongWaySearchExecute(
        pkg: String,
        requestCode: String?,
        alongWaySearchData: AlongWaySearchData?
    ) {
        Timber.i("protocolAlongWaySearchExecute alongWaySearchData = $alongWaySearchData")
        if (alongWaySearchData == null) {
            Timber.i("protocolAlongWaySearchExecute alongWaySearchData == null")
            return
        }
        if (routeBusiness.isPlanRouteing() || naviBusiness.isNavigating()) {

            val keyword = when (alongWaySearchData.alongSearchType) {
                ALONG_SEARCH_TYPE_RESTROOM -> "厕所"
                ALONG_SEARCH_TYPE_ATM -> "ATM"
                ALONG_SEARCH_TYPE_REPAIR -> "维修站"
                ALONG_SEARCH_TYPE_GAS_STATION -> "加油站"
                ALONG_SEARCH_TYPE_CHARGING_PILE -> "充电站"
                ALONG_SEARCH_TYPE_NATURAL_GAS_STATION -> "加气站"
                ALONG_SEARCH_TYPE_RESTAURANT -> "美食"
                ALONG_SEARCH_TYPE_SERVICE_AREA -> "服务区"
                else -> ""
            }

            if (TextUtils.isEmpty(keyword)) { //未输入参数
                responseMessage(
                    ProtocolID.PROTOCOL_ALONG_THE_WAY_SEARCH,
                    SearchResponseData().apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10001.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10001.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
                return
            }
            standardResponseList.add(
                StandardResponseData(
                    pkg = pkg, protocolId = ProtocolID.PROTOCOL_ALONG_THE_WAY_SEARCH, responseCode = requestCode, data =
                    SearchResponseData()
                )
            )
            if (alongWaySearchData.requestType == 0) { //0:应⽤外搜索
                withContext(Dispatchers.IO) {
                    val result = searchBusiness.searchAlongWayKeyword(
                        keyword = keyword,
                        naviScene = if (naviBusiness.isNavigating()) SearchEnrouteScene.Navi else SearchEnrouteScene.BeforeNavi
                    )
                    when (result.status) {
                        Status.SUCCESS -> {
                            result.data?.let { data ->
                                protocolAlongWaySearchResponse(data)
                            }
                        }

                        Status.ERROR -> {
                            Timber.i("searchAlongWayKeyword Status.ERROR throwable:${result.throwable.toString()}")
                            standardAsyncResponseMessage(ProtocolID.PROTOCOL_ALONG_THE_WAY_SEARCH, StandardJsonConstant.ResponseResult.FAIL_10032)
                        }

                        else -> {}
                    }
                }

            } else if (alongWaySearchData.requestType == 1) { //0:应⽤内搜索
                defaultMapCommand.alongRouteSearch(keyword)
            }
        } else {
            responseMessage(
                ProtocolID.PROTOCOL_ALONG_THE_WAY_SEARCH,
                SearchResponseData().apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }


    }

    /**
     * PROTOCOL_AROUNDSEARCH_SEARCH: Int = 30301 应答 发起周边搜索
     */
    private fun protocolAroundSearchResponse(result: KeywordSearchResultV2?) {
        if (result == null) {
            standardAsyncResponseMessage(
                ProtocolID.PROTOCOL_AROUNDSEARCH_SEARCH,
                StandardJsonConstant.ResponseResult.FAIL_10032
            )
        } else {
            standardAsyncResponseMessage(
                ProtocolID.PROTOCOL_AROUNDSEARCH_SEARCH,
                data = SearchResponseData(
                    poiResult = getPoiResultFromSearchResult(result)
                ).apply {
                    resultCode = StandardJsonConstant.ResponseResult.OK.code
                    errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                }
            )
        }
    }

    /**
     * PROTOCOL_ALONG_THE_WAY_SEARCH: Int = 30302 应答 沿途搜索
     */
    fun protocolAlongWaySearchResponse(result: SearchEnrouteResult?) {
        Timber.i("protocolprotocolAlongWaySearchResponseAlongWaySearchResponse result = $result")
        standardResponseList.iterator().run {
            while (this.hasNext()) {
                if (this.next().protocolId == ProtocolID.PROTOCOL_ALONG_THE_WAY_SEARCH) {
                    if (result == null) {
                        standardAsyncResponseMessage(
                            ProtocolID.PROTOCOL_ALONG_THE_WAY_SEARCH,
                            StandardJsonConstant.ResponseResult.FAIL_10032
                        )
                    } else {
                        standardAsyncResponseMessage(
                            ProtocolID.PROTOCOL_ALONG_THE_WAY_SEARCH,
                            data = SearchResponseData(
                                poiResult = getPoiResultFromSearchEnrouteResult(result)
                            ).apply {
                                resultCode = StandardJsonConstant.ResponseResult.OK.code
                                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                            }
                        )
                    }
                }
            }
        }
    }


    /**
     * PROTOCOL_MAP_ENTERAUTO: Int = 80044 应答 进⼊主图
     */
    private suspend fun protocolBackToMapHomeExecute(
        pkg: String,
        requestCode: String?,
        backToMapHomeOperaData: BackToMapHomeOperaData?
    ) {
        if (backToMapHomeOperaData == null) {
            Timber.i("protocolBackToMapHomeExecute backToMapHomeOperaData == null")
            return
        }
        if (!BaseConstant.MAP_APP_FOREGROUND) { //地图在后台
            if (!defaultMapCommand.openMap()) {
                responseMessage(
                    ProtocolID.PROTOCOL_MAP_ENTERAUTO,
                    backToMapHomeOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10003.code
                        errorMessage = StandardJsonConstant.ResponseResult.FAIL_10003.msg
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
                return
            }

        }
        val isShowActivateLayout = activationMapBusiness.isShowActivateLayout
        val isShowAgreementLayout = activationMapBusiness.isShowAgreementLayout
        if (isShowActivateLayout.value == false || isShowAgreementLayout.value == false) {
            mapBusiness.backToMapHome.postValue(true) //回到地图主图--在导航/模拟导航需要退出
        }
        responseMessage(
            ProtocolID.PROTOCOL_MAP_ENTERAUTO,
            backToMapHomeOperaData.apply {
                resultCode = StandardJsonConstant.ResponseResult.OK.code
                errorMessage = StandardJsonConstant.ResponseResult.OK.msg
            },
            pkg = pkg,
            responseCode = requestCode
        )
    }

    /**
     * PROTOCOL_QUERY_RESTRICTED_INFO: Int = 80182
     * 第三⽅传⼊指定城市/区县名称进⾏限⾏尾号/限⾏政策查询
     */
    private suspend fun protocolQueryRestrictedInfoExecute(
        pkg: String,
        requestCode: String?,
        queryRestrictedInfoOperaData: QueryRestrictedInfoOperaData?
    ) {
        if (queryRestrictedInfoOperaData == null) {
            Timber.i("protocolQueryRestrictedInfoExecute queryRestrictedInfoOperaData == null")
            return
        }
        try {
            val requestType = queryRestrictedInfoOperaData.requestType
            val cityName = queryRestrictedInfoOperaData.cityName
            val adCode = queryRestrictedInfoOperaData.adCode
            val code = mapDataBusiness.searchAdCode(cityName)?.firstOrNull()

            val number = navigationSettingBusiness.getLicensePlateNumber()
            val hasNumber = TextUtils.isEmpty(number)
            Timber.d(" getVehicleNumber vehicleNum:$number")
            Timber.i("protocolQueryRestrictedInfoExecute queryRestrictedInfoOperaData : $queryRestrictedInfoOperaData  code : $code number : $number hasNumber : $hasNumber")
            if (adCode != 0) {
                aosBusiness.sendReqTrafficRestrict(GTrafficRestrictRequestParam().apply {
                    Adcode = adCode.toLong()
                }) { gTrafficRestrictResponseParam ->
                    Timber.i("protocolQueryRestrictedInfoExecute gTrafficRestrictResponseParam : ${gson.toJson(gTrafficRestrictResponseParam)}")
                    if (gTrafficRestrictResponseParam?.Restrict != null) {
                        MainScope().launch {
                            responseMessage(
                                ProtocolID.PROTOCOL_QUERY_RESTRICTED_INFO,
                                queryRestrictedInfoOperaData.apply {
                                    resultCode = StandardJsonConstant.ResponseResult.OK.code
                                    errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                                    restrictedNumber = gTrafficRestrictResponseParam.Restrict.m_plateNo
                                },
                                pkg = pkg,
                                responseCode = requestCode
                            )
                        }
                    } else {
                        MainScope().launch {
                            responseMessage(
                                ProtocolID.PROTOCOL_QUERY_RESTRICTED_INFO,
                                queryRestrictedInfoOperaData.apply {
                                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                                    errorMessage = application.getString(R.string.sv_voice_no_traffic_restriction_policy, cityName)
                                },
                                pkg = pkg,
                                responseCode = requestCode
                            )
                        }
                    }
                }
            } else {
                responseMessage(
                    ProtocolID.PROTOCOL_QUERY_RESTRICTED_INFO,
                    queryRestrictedInfoOperaData.apply {
                        resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                        errorMessage = "未获取到对应城市adcode"
                    },
                    pkg = pkg,
                    responseCode = requestCode
                )
            }
        } catch (e: Exception) {
            Timber.i("protocolQueryRestrictedInfoExecute Exception:${e.message}")
            responseMessage(
                ProtocolID.PROTOCOL_QUERY_RESTRICTED_INFO,
                queryRestrictedInfoOperaData.apply {
                    resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                    errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
                },
                pkg = pkg,
                responseCode = requestCode
            )
        }

    }

    fun protocolSearchResponse(result: KeywordSearchResultV2?) {
        Timber.i("protocolSearchResponse result = ${result?.keyword}")
        standardResponseList.iterator().run {
            while (this.hasNext()) {
                val responseData = this.next()
                Timber.i("protocolSearchResponse standardResponseList protocolId is ${responseData.protocolId}")
                when (responseData.protocolId) {
                    ProtocolID.PROTOCOL_KEYWORD_SEARCH -> {
                        protocolKeywordSearchResponse(result)
                    }

                    ProtocolID.PROTOCOL_AROUNDSEARCH_SEARCH -> {
                        protocolAroundSearchResponse(result)
                    }
                }
            }
        }
    }


    /**
     * PROTOCOL_SEARCH_SENDSEARCHRESULTLISTSTATE: Int = 80034 搜索结果列表状态透出 （
     */
    suspend fun protocolSearchResultListChangeExecute(
        poinum: Int = 10,
        isFirstPage: Boolean,
        isLastPage: Boolean,
        choice: Int = -1,
        planRoute: Boolean = false,
        back: Boolean = false,
        isListTop: Boolean = false,
        isListBottom: Boolean = false,
        curPage: Int = 1,
        totalPage: Int = 1,
        searchResult: KeywordSearchResultV2? = null
    ) {
        responseMessage(
            ProtocolID.PROTOCOL_SEARCH_SENDSEARCHRESULTLISTSTATE,
            SearchResultListChangeData(
                poinum = poinum,
                isFirstPage = isFirstPage,
                isLastPage = isLastPage,
                choice = choice,
                planRoute = planRoute,
                back = back,
                isListTop = isListTop,
                isListBottom = isListBottom,
                curPage = curPage,
                totalPage = totalPage,
                poiResult = getPoiResultFromSearchResult(searchResult)
            )
        )
    }

    /**
     * PROTOCOL_SEARCH_SENDSEARCHRESULTLISTSTATE: Int = 80034 搜索结果列表状态透出 （
     */
    suspend fun protocolAlongWaySearchResultListChangeExecute(
        poinum: Int = 10,
        isFirstPage: Boolean,
        isLastPage: Boolean,
        choice: Int = -1,
        planRoute: Boolean = false,
        back: Boolean = false,
        isListTop: Boolean = false,
        isListBottom: Boolean = false,
        curPage: Int = 1,
        totalPage: Int = 1,
        searchResult: SearchEnrouteResult? = null
    ) {
        responseMessage(
            ProtocolID.PROTOCOL_SEARCH_SENDSEARCHRESULTLISTSTATE,
            SearchResultListChangeData(
                poinum = poinum,
                isFirstPage = isFirstPage,
                isLastPage = isLastPage,
                choice = choice,
                planRoute = planRoute,
                back = back,
                isListTop = isListTop,
                isListBottom = isListBottom,
                curPage = curPage,
                totalPage = totalPage,
                poiResult = getPoiResultFromSearchEnrouteResult(searchResult)
            )
        )
    }

    /**
     * 获取路线信息 PROTOCOL_GET_ROUTE_INFO: Int = 30005
     */
    private suspend fun protocolGetRouteInfoExecute(pkg: String = "", requestCode: String? = null, type: Int) {
        Timber.i("protocolGetRouteInfoExecute type = $type")
        mRouteRequestController.carRouteResult?.run {
            val responseData = RouteInfoDataResponse(
                viaPOITotal = 3,
                count = this.pathResult.size,
                startPOIName = this.fromPOI.name,
                startPOIAddr = this.fromPOI.addr,
                startPOILongitude = this.fromPOI.point.longitude,
                startPOILatitude = this.fromPOI.point.latitude,
                startPOIType = this.fromPOI.typeCode,
                endPOIName = this.toPOI.name,
                endPoiid = this.toPOI.id,
                endPOIAddr = this.toPOI.addr,
                endPOILongitude = this.toPOI.point.longitude,
                endPOILatitude = this.toPOI.point.latitude,
                endPOIType = this.toPOI.typeCode,
                arrivePOILongitude = this.toPOI.point.longitude,
                arrivePOILatitude = this.toPOI.point.latitude,
                arrivePOIType = "",
                arrivePOIDistance = locationBusiness.getLastLocation().let {
                    CommonUtils.calcDistance(
                        Coord2DDouble(it.longitude, it.latitude),
                        Coord2DDouble(this.toPOI.point.longitude, this.toPOI.point.latitude)
                    ).toInt()
                },
                arrivePOIPhone = this.toPOI.phone,
                viaNumbers = this.midPois?.size ?: 0,
                routePreference = this.routeStrategy,
                newStrategy = CommonUtils.getConfigRoutePreferenceKey(mSettingComponent.getConfigKeyPlanPref()),
                protocolViaPOIInfos = this.midPois?.run {
                    val viaPois = mutableListOf<ProtocolViaPOIInfo>()
                    this.forEach { poi ->
                        viaPois.add(
                            ProtocolViaPOIInfo(
                                viaPOIName = poi.name,
                                viaPoiid = poi.id,
                                viaPOIAddr = poi.addr,
                                viaPOILongitude = poi.point.longitude,
                                viaPOILatitude = poi.point.latitude,
                                viaPOIType = poi.typeCode,
                                viaEntryLongitude = poi.point.longitude,
                                viaEntryLatitude = poi.point.latitude,
                                viaPOIDistance = locationBusiness.getLastLocation().let {
                                    CommonUtils.calcDistance(
                                        Coord2DDouble(it.longitude, it.latitude),
                                        Coord2DDouble(poi.point.longitude, poi.point.latitude)
                                    ).toInt()
                                },
                                viaPOIPhone = poi.phone,
                            )
                        )
                    }
                    viaPois
                },

                protocolRouteInfos = this.pathResult?.let { pathResult0 ->
                    val pathResult = if (type == 0) pathResult0 else arrayListOf(pathResult0[this.focusIndex])

                    val routeInfo = mutableListOf<ProtocolRouteInfoData>()
                    for (i in pathResult.indices) {
                        val variantPathWrap = pathResult[i]
                        val labelInfo = variantPathWrap.getLabelInfo(0)
                        val content = labelInfo?.content ?: ""
                        val cityInfos: ArrayList<ProtocolCityInfo> = arrayListOf()
                        if (!variantPathWrap.cityAdcodeList.isNullOrEmpty()) {
                            for (adcode in variantPathWrap.cityAdcodeList) {
                                cityInfos.add(ProtocolCityInfo(SearchDataConvertUtils.getCityName(adcode.toInt())))
                            }
                        }

                        routeInfo.add(
                            ProtocolRouteInfoData(
                                method = when (i) {
                                    0 -> if (content.isEmpty()) "推荐" else content.replace("备选一", "推荐")
                                        .replace("备选方案一", "推荐")

                                    1 -> if (content.isEmpty()) "备选一" else content.replace("备选二", "备选一")
                                        .replace("备选方案二", "备选一")

                                    2 -> if (content.isEmpty()) "备选二" else content.replace("备选三", "备选二")
                                        .replace("备选方案三", "备选二")

                                    else -> ""
                                },
                                routePreference = this.routeStrategy,
                                newStrategy = CommonUtils.getConfigRoutePreferenceKey(mSettingComponent.getConfigKeyPlanPref()),
                                distance = variantPathWrap.length.toDouble(),
                                distanceAuto = "",
                                time = variantPathWrap.travelTime.toDouble(),
                                timeAuto = "",
                                tolls = variantPathWrap.tollCost,
                                totalOddDistance = variantPathWrap.length.toString(),
                                viaPOItime = 0,
                                viaPOIdistance = 0,
                                oddNum = 0,
                                tmcSize = variantPathWrap.lightBarItems?.size ?: 0,
                                tmcSegments = "",
                                trafficLights = variantPathWrap.trafficLightCount.toInt(),
                                viaCityNumbers = variantPathWrap.cityAdcodeList.size,
                                protocolCityInfos = cityInfos
                            )
                        )
                    }
                    routeInfo
                },
            )
            if (TextUtils.isEmpty(pkg)) {
                dispatchMessage(ProtocolID.PROTOCOL_GET_ROUTE_INFO, responseData)
            } else {
                responseMessage(ProtocolID.PROTOCOL_GET_ROUTE_INFO, responseData, pkg, requestCode)
            }
        }
        if (mRouteRequestController.carRouteResult == null && !TextUtils.isEmpty(pkg)) {
            responseMessage(ProtocolID.PROTOCOL_GET_ROUTE_INFO, RouteInfoDataRequest(type = type).apply {
                resultCode = StandardJsonConstant.ResponseResult.FAIL_10004.code
                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10004.msg
            }, pkg, requestCode)
        }
    }

    /**
     * 80155 PROTOCOL_TRIP_ROUTEINFOCONTRAST
     * ⽤于监听⾼德导航过程中透出的备选路线信息
     */
    private suspend fun protocolTripRouteInfoContrastExecute(type: Int = 0, isQuery: Boolean = false, pkg: String = "", requestCode: String? = null) {
        Timber.i("protocolTripRouteInfoContrastExecute type=$type")
        mRouteRequestController.carRouteResult?.run {
            val routeInfo = arrayListOf<TripContrastRoute>()
            if ((NaviComponent.getInstance().naviPath?.vecPaths?.size ?: this.pathResult.size) > 1) {
                val currentPath = this.pathResult[this.focusIndex]
                for (i in this.pathResult.indices) {
                    if (i != this.focusIndex) {
                        val pathInfo = this.pathResult[i]
                        routeInfo.add(
                            TripContrastRoute(
                                num = pathInfo.pathID,
                                type = type,
                                label = if (pathInfo.labelInfoCount > 0) pathInfo.getLabelInfo(0).content else "",
                                time = (pathInfo.travelTime - currentPath.travelTime).toInt(),
                                trafficLight = (pathInfo.trafficLightCount - currentPath.trafficLightCount).toInt(),
                                cost = pathInfo.tollCost - currentPath.tollCost,
                                distance = (pathInfo.length - currentPath.length).toInt()
                            )
                        )
                    }
                }
            }
            if (isQuery) {
                responseMessage(ProtocolID.PROTOCOL_TRIP_ROUTEINFOCONTRAST, TripContrastRouteInfo(route = routeInfo).apply {
                    resultCode = StandardJsonConstant.ResponseResult.OK.code
                    errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                }, pkg, requestCode)
            } else {
                dispatchMessage(ProtocolID.PROTOCOL_TRIP_ROUTEINFOCONTRAST, TripContrastRouteInfo(route = routeInfo))
            }
        }
        if (isQuery && mRouteRequestController.carRouteResult == null) {
            responseMessage(ProtocolID.PROTOCOL_TRIP_ROUTEINFOCONTRAST, TripContrastRouteInfo(route = null).apply {
                resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
                errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
            }, pkg, requestCode)
        }
    }


    /**
     * 30407 PROTOCOL_GUIDE_INFO
     * 引导信息主动透出 （30407）
     */
    private suspend fun protocolNaviInfoExecute(naviInfo: NaviInfo) {
        Timber.i("protocolNaviInfoExecute is called")
        val result = mRouteRequestController.carRouteResult
        val errorResponse = NaviInfoResponseData().apply {
            resultCode = StandardJsonConstant.ResponseResult.FAIL_10032.code
            errorMessage = StandardJsonConstant.ResponseResult.FAIL_10032.msg
        }

        result?.let { carRouteResult ->
            carRouteResult.pathResult.getOrNull(carRouteResult.focusIndex)?.let { pathInfo ->
                Timber.i("protocolNaviInfoExecute is send")
                naviInfoResponse.arrivePOILatitude = carRouteResult.toPOI.point.latitude
                naviInfoResponse.arrivePOILongitude = carRouteResult.toPOI.point.longitude
                naviInfoResponse.arrivePOIType = pathInfo.endPoi.typeCode
                naviInfoResponse.endPOIAddr = carRouteResult.toPOI.addr
                val mCityInfo = mapDataBusiness.getCityInfo(carRouteResult.toPOI.point.longitude, carRouteResult.toPOI.point.latitude)
                naviInfoResponse.endPOICityName = mCityInfo?.cityName ?: ""
                naviInfoResponse.endPOIDistrictName = ""
                naviInfoResponse.endPOILatitude = carRouteResult.toPOI.point.latitude
                naviInfoResponse.endPOILongitude = carRouteResult.toPOI.point.longitude
                naviInfoResponse.endPOIName = carRouteResult.toPOI.name
                naviInfoResponse.endPOIType = pathInfo.endPoi.typeCode
                //获取当前车位为起点
                val location = locationBusiness.getLastLocation()
                naviInfoResponse.carDirection = location.bearing.toInt()
                naviInfoResponse.carLatitude = location.latitude
                naviInfoResponse.carLongitude = location.longitude
                naviInfoResponse.curPointNum = naviInfo.curPointIdx
                naviInfoResponse.curRoadName = naviInfo.curRouteName
                naviInfoResponse.curSegNum = naviInfo.curSegIdx
                naviInfoResponse.curSpeed = naviBusiness.getCurSpeed().toInt()
                val mCurrentRoadTotalDis = pathInfo.getSegmentInfo(naviInfo.curSegIdx.toLong())?.length ?: 0
                var mIcon = 0
                var mNextRoadName = ""
                var segRemainDis = 0
                var segRemainDisAuto = ""
                var segRemainTime = 0

                naviInfo.NaviInfoData?.firstOrNull()?.let { naviPanel ->
                    mIcon = naviPanel.maneuverID
                    mNextRoadName = naviPanel.nextRouteName
                    segRemainDis = naviPanel.segmentRemain.dist//当前导航段剩余距离，对应的值为int类型，单位：⽶
                    segRemainDisAuto = CommonUtils.getDistanceStr(application, naviPanel.segmentRemain.dist.toDouble())
                    //转换后当前导航段剩余距离，对应的值为String类型，由距离和单位组成
                    segRemainTime = naviPanel.segmentRemain.time//当前导航段剩余时间，对应的值为int类型，单位：秒
                }
                naviInfoResponse.currentRoadTotalDis = mCurrentRoadTotalDis.toInt()
                naviInfoResponse.icon = mIcon
                naviInfoResponse.nextRoadName = mNextRoadName
                naviInfoResponse.segRemainDis = segRemainDis//当前导航段剩余距离，对应的值为int类型，单位：⽶
                naviInfoResponse.segRemainDisAuto = segRemainDisAuto //转换后当前导航段剩余距离，对应的值为String类型，由距离和单位组成
                naviInfoResponse.segRemainTime = segRemainTime//当前导航段剩余时间，对应的值为int类型，单位：秒

                naviInfoResponse.etaText = AutoRouteUtil.getScheduledTime(application, naviInfo.routeRemain.time.toLong(), true)
                naviInfoResponse.exitDirectionInfo = ""
                // 更进阶 下个路口信息
                if (NavigationUtil.hasNextThumTip(naviInfo)) {
                    val nextCrossInfo = naviInfo.nextCrossInfo[0]
                    naviInfoResponse.nextNextTurnIcon = nextCrossInfo.maneuverID//下下个路名名称
                    naviInfoResponse.nextNextRoadName = nextCrossInfo.nextRoadName//下下个路⼝转向图标
                    naviInfoResponse.nextSegRemainDis = nextCrossInfo.curToSegmentDist//距离下下个路⼝剩余距离,对应的值为int类型，单位：⽶
                    naviInfoResponse.nextSegRemainDisAuto = CommonUtils.getDistanceStr(application, nextCrossInfo.curToSegmentDist.toDouble())
                    //转换后下下个路⼝剩余距离,（带单位）（仅sdk项⽬⽀持）
                    naviInfoResponse.nextSegRemainTime = nextCrossInfo.curToSegmentTime//距离下下个路⼝剩余时间，对应的值为int类型，单位：秒
                } else {
                    naviInfoResponse.nextNextTurnIcon = 0//下下个路名名称
                    naviInfoResponse.nextNextRoadName = ""//下下个路⼝转向图标
                    naviInfoResponse.nextSegRemainDis = 0//距离下下个路⼝剩余距离,对应的值为int类型，单位：⽶
                    naviInfoResponse.nextSegRemainDisAuto = ""
                    //转换后下下个路⼝剩余距离,（带单位）（仅sdk项⽬⽀持）
                    naviInfoResponse.nextSegRemainTime = 0//距离下下个路⼝剩余时间，对应的值为int类型，单位：秒
                }

                naviInfoResponse.roadType = naviInfo.curRoadClass//当前道路类型
                naviInfoResponse.roundAboutNum = naviInfo.ringOutCnt//环岛出⼝序号，对应的值为int类型，从0开始，只有在icon为11和12时有效，其余为⽆效值0
                naviInfoResponse.roundAllNum = 0//环岛出⼝个数，对应的值为int类型，只有在icon为11和12时有效，其余为⽆效值0
                naviInfoResponse.roundaboutOutAngle = naviInfo.roundaboutOutAngle//环岛出⼝度数，只有在icon类型为环岛的时候有效
                naviInfoResponse.routeAllDis = pathInfo.length.toInt()//路径总距离，对应的值为int类型，单位：⽶
                naviInfoResponse.routeAllTime = pathInfo.travelTime.toInt()//路径总时间，对应的值为int类型，单位：秒
                naviInfoResponse.routeRemainDis = naviInfo.routeRemain.dist//路径剩余距离，对应的值为int类型，单位：⽶
                naviInfoResponse.routeRemainDistanceAuto =
                    CommonUtils.getDistanceStr(application, naviInfo.routeRemain.dist.toDouble())//转换后的路径剩余距离（带单位）
                naviInfoResponse.routeRemainTime = naviInfo.routeRemain.time//路径剩余时间，对应的值为int类型，单位：秒
                naviInfoResponse.routeRemainTimeAuto = AutoRouteUtil.getTimeStr(naviInfo.routeRemain.time.toLong())//转换后的路径剩余时间（带单位）
                naviInfoResponse.routeRemainTrafficLightNum = naviInfo.routeRemainLightCount//路径剩余红绿灯个数，对应的值为int类型（610及以上⽀持）
                naviInfoResponse.segAssistantAction = pathInfo.getSegmentInfo(naviInfo.curSegIdx.toLong())?.assistantAction ?: 0//当前导航段的辅助动作
                naviInfoResponse.trafficLightNum = pathInfo.trafficLightCount.toInt()//红绿灯个数，对应的值为int类型
                naviInfoResponse.type = if (naviBusiness.isRealNavi()) 0 else 1//当导航类型参数type值为0（真实导航），1（模拟导航），2（巡航）时，此参数取值有效；当type参数值为-1时，此参数取值⽆效
                naviInfo.viaRemain?.firstOrNull()?.run {
                    naviInfoResponse.viaPOIArrivalTime =
                        AutoRouteUtil.getScheduledTime(application, this.time.toLong(), true)//到达最近⼀个途经点的时间 (530及以上⽀持）⽐如11点20分到达
                    naviInfoResponse.viaPOIdistance = this.dist//到达最近⼀个途经点的距离，单位：米 (520及以上⽀持）
                    naviInfoResponse.viaPOItime = this.time//到达最近⼀个途经点的时间，单位：秒 (520及以上⽀持）
                }?.run {
                    naviInfoResponse.viaPOIArrivalTime = ""
                    naviInfoResponse.viaPOIdistance = 0
                    naviInfoResponse.viaPOItime = 0
                }
                naviInfoResponse.resultCode = StandardJsonConstant.ResponseResult.OK.code
                naviInfoResponse.errorMessage = StandardJsonConstant.ResponseResult.OK.msg
                dispatchMessage(ProtocolID.PROTOCOL_GUIDE_INFO, naviInfoResponse)

            } ?: dispatchMessage(ProtocolID.PROTOCOL_GUIDE_INFO, errorResponse)
        } ?: dispatchMessage(ProtocolID.PROTOCOL_GUIDE_INFO, errorResponse)
    }

    /**
     * 巡航中1秒发送一次巡航信息
     */
    private suspend fun loopSendCruiseInfo() {
        while (cruiseBusiness.isCruising()) {
            Timber.i("loopSendCruiseInfo")
            protocolCruiseInfoExecute()
            delay(1000)
        }
    }

    /**
     * 30407 PROTOCOL_GUIDE_INFO
     * 巡航信息主动透出 （30407）
     */
    private suspend fun protocolCruiseInfoExecute() {
        Timber.i("protocolCruiseInfoExecute")
        naviInfoResponse.type = 2
        val location = locationBusiness.getLastLocation()
        naviInfoResponse.carDirection = location.bearing.toInt()
        naviInfoResponse.carLatitude = location.latitude
        naviInfoResponse.carLongitude = location.longitude
        dispatchMessage(ProtocolID.PROTOCOL_GUIDE_INFO, naviInfoResponse)
    }

    /**
     * 查询引导信息  PROTOCOL_INFO_NOTIFY_SEARCHNAVINFO: Int = 80163
     */
    private suspend fun protocolGetNaviInfoSearchExecute(pkg: String = "", requestCode: String? = null, requestType: Int) {
        Timber.i("protocolGetNaviInfoSearchExecute is called requestType=$requestType")
        responseMessage(ProtocolID.PROTOCOL_INFO_NOTIFY_SEARCHNAVINFO, NaviInfoSearchResponse().apply {
            cameraType = naviInfoResponse.cameraType
            cameraID = naviInfoResponse.cameraID
            cameraPenalty = naviInfoResponse.cameraPenalty
            cameraSpeed = naviInfoResponse.cameraSpeed
            cameraDist = naviInfoResponse.cameraDist
            isOverspeed = if (naviInfoResponse.cameraSpeed > 0) naviBusiness.getCurSpeed() > naviInfoResponse.cameraSpeed else false
            sapaDist = naviInfoResponse.sapaDist
            sapaETA = mSapaETA
            sapaName = naviInfoResponse.sapaName
            sapaNum = naviInfoResponse.sapaNum
            sapaType = naviInfoResponse.sapaType
            tollDist = mTollDist
            tollETA = mTollETA
            tollName = mTollName
            resultCode = StandardJsonConstant.ResponseResult.OK.code
            errorMessage = StandardJsonConstant.ResponseResult.OK.msg
        }, pkg, requestCode)
    }

}



