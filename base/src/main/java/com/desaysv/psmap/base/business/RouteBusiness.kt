package com.desaysv.psmap.base.business

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.aosclient.model.GReStrictedAreaDataRuleRes
import com.autonavi.gbl.aosclient.model.GRestrictRule
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.common.model.RectInt
import com.autonavi.gbl.common.path.model.AvoidJamCloudControl
import com.autonavi.gbl.common.path.model.BankingHoursCloudControl
import com.autonavi.gbl.common.path.model.EventCloudControl
import com.autonavi.gbl.common.path.model.ForbiddenCloudControl
import com.autonavi.gbl.common.path.model.HolidayCloudControl
import com.autonavi.gbl.common.path.model.LimitTipsType
import com.autonavi.gbl.common.path.model.RestAreaInfo
import com.autonavi.gbl.common.path.model.RestrictionInfo
import com.autonavi.gbl.common.path.model.RoutePoint
import com.autonavi.gbl.common.path.model.RoutePoints
import com.autonavi.gbl.common.path.model.TipsCloudControl
import com.autonavi.gbl.common.path.model.ViaRoadInfo
import com.autonavi.gbl.common.path.option.PathInfo
import com.autonavi.gbl.layer.CustomPointLayerItem
import com.autonavi.gbl.layer.model.BizAreaType
import com.autonavi.gbl.layer.model.BizCarType
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.layer.model.BizRouteRestrictInfo
import com.autonavi.gbl.layer.model.BizRouteType
import com.autonavi.gbl.layer.model.BizRouteViaRoadInfo
import com.autonavi.gbl.layer.model.BizRouteWeatherInfo
import com.autonavi.gbl.layer.model.BizSearchType.BizSearchTypePoiAlongRoute
import com.autonavi.gbl.layer.model.BizSearchType.BizSearchTypePoiLabel
import com.autonavi.gbl.layer.model.BizSearchType.BizSearchTypePoiParentPoint
import com.autonavi.gbl.layer.model.DynamicLevelType
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.OperatorPosture
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.PointLayerItem
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.model.MapLabelItem
import com.autonavi.gbl.map.model.MapLabelType.LABEL_Type_OPENLAYER
import com.autonavi.gbl.route.model.RouteServiceAreaInfo
import com.autonavi.gbl.route.model.WeatherLabelItem
import com.autonavi.gbl.route.observer.IRouteWeatherObserver
import com.autonavi.gbl.search.model.LineDeepQueryType
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.common.AlongWayPoiDeepInfo
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.common.utils.ElectricInfoConverter
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.DrivingLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.layer.RouteEndAreaLayer
import com.autosdk.bussiness.layer.RouteResultLayer
import com.autosdk.bussiness.layer.RouteResultLayer.OnAlongWayPointClickListener
import com.autosdk.bussiness.layer.RouteResultLayer.OnRouteClickListener
import com.autosdk.bussiness.layer.RouteResultLayer.OnTrafficEventClickListener
import com.autosdk.bussiness.layer.RouteResultLayer.OnWayPointClickListener
import com.autosdk.bussiness.layer.RouteResultLayer.OnWeatherClickListener
import com.autosdk.bussiness.layer.SearchLayer
import com.autosdk.bussiness.layer.UserBehaviorLayer
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.MapController.EMapStyleStateType
import com.autosdk.bussiness.map.Observer.MapGestureObserver
import com.autosdk.bussiness.map.Observer.MapViewObserver
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.navi.route.callback.IRouteResultCallBack
import com.autosdk.bussiness.navi.route.model.IRouteResultData
import com.autosdk.bussiness.search.request.SearchLineDeepInfo
import com.autosdk.bussiness.search.request.SearchRequestInfo
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.bussiness.widget.route.RouteComponent
import com.autosdk.bussiness.widget.route.model.NaviStationItemData
import com.autosdk.bussiness.widget.route.model.RoutePathItemContent
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.autosdk.common.utils.StringUtils
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.bean.AvoidTrafficJamsBean
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.bean.NaviViaDataBean
import com.desaysv.psmap.base.bean.RestrictInfoBean
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.data.IRouteRepository
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.net.bean.CloudTipType
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.RouteErrorCodeUtils
import com.desaysv.psmap.base.utils.Status
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @author 谢锦华
 * @time 2024/1/5
 * @description 路线相关操作类
 */
@Singleton
class RouteBusiness @Inject constructor(
    private val routeRepository: IRouteRepository,
    private val application: Application,
    private val mRouteRequestController: RouteRequestController,
    private val mMapController: MapController,
    private val mLayerController: LayerController,
    private val mLocationController: LocationController,
    private val mNaviController: NaviController,
    private val gson: Gson,
    private val mSearchBusiness: SearchBusiness,
    private val mTripShareBusiness: TripShareBusiness,
    private val mEngineerBusiness: EngineerBusiness,
    private val mMapBusiness: MapBusiness,
    private val mRouteComponent: RouteComponent,
    private val settingComponent: ISettingComponent,
    private val mUserBusiness: UserBusiness,
    private val locationController: LocationController,
    private val mNetWorkManager: NetWorkManager,
    private val mAosBusiness: AosBusiness
) :
    IRouteResultCallBack {

    @Inject
    lateinit var extMapBusiness: ExtMapBusiness

    // 主图层
    private val mMapLayer: MapLayer? by lazy {
        mLayerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //路线图层
    private val mRouteResultLayer: RouteResultLayer? by lazy {
        mLayerController.getRouteResultLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //终点图层
    private val mRouteEndAreaLayer: RouteEndAreaLayer? by lazy {
        mLayerController.getRouteEndAreaLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //导航引导图层
    private val mDrivingLayer: DrivingLayer? by lazy {
        mLayerController.getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //用户收藏点图层
    private val mUserBehaviorLayer: UserBehaviorLayer? by lazy {
        mLayerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //自定义图层
    private val mCustomLayer: CustomLayer? by lazy {
        mLayerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //搜索图层
    private val mSearchLayer: SearchLayer? by lazy {
        mLayerController.getSearchLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //主图
    private val mMapView: MapView? by lazy {
        mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }
    private val RouteScope = CoroutineScope(Dispatchers.IO + Job())


    //前台是否在路线界面
    var isInRouteFragment = true

    //Toast内容
    val setToast = MutableLiveData<String>()

    //正在算路中
    val isRequestRoute = MutableLiveData<Boolean>(false)

    //选择第几条路线通知adapter更新
    val focusPathIndex = MutableLiveData<Int>()

    val updateCarResultDataLiveData = MutableLiveData<Boolean>()

    val closePreferenceSetting = MutableLiveData<Boolean>()

    //限行信息
    private val _restrictInfoLiveData = MutableLiveData<RestrictInfoBean?>()
    val restrictInfoLiveData: LiveData<RestrictInfoBean?> = _restrictInfoLiveData

    //封路信息
    private val _eventCloudControl = MutableLiveData<EventCloudControl?>()
    val eventCloudControl: LiveData<EventCloudControl?> = _eventCloudControl

    //拥堵信息
    private val _avoidJamCloudControl = MutableLiveData<AvoidJamCloudControl?>()
    val avoidJamCloudControl: LiveData<AvoidJamCloudControl?> = _avoidJamCloudControl

    //禁行信息
    private val _forbiddenCloudControl = MutableLiveData<ForbiddenCloudControl?>()
    val forbiddenCloudControl: LiveData<ForbiddenCloudControl?> = _forbiddenCloudControl

    //节假日信息
    private val _holidayCloudControl = MutableLiveData<HolidayCloudControl?>()
    val holidayCloudControl: LiveData<HolidayCloudControl?> = _holidayCloudControl

    //小路信息
    private val _tipsCloudControl = MutableLiveData<TipsCloudControl?>()
    val tipsCloudControl: LiveData<TipsCloudControl?> = _tipsCloudControl

    //营业时间信息
    private val _bankingHoursCloudControl = MutableLiveData<BankingHoursCloudControl?>()
    val bankingHoursCloudControl: LiveData<BankingHoursCloudControl?> = _bankingHoursCloudControl

    //云控信息显示隐藏状态
    private val _hasPriorTipVisibility = MutableLiveData<Boolean>(false)
    val hasPriorTipVisibility: LiveData<Boolean> = _hasPriorTipVisibility

    //云控类型
    private val _cloudTipType = MutableLiveData(CloudTipType.NONE)
    val cloudTipType: LiveData<CloudTipType> = _cloudTipType

    //限行详情信息 页面
    private val _restrictInfoDetails = MutableLiveData<GReStrictedAreaDataRuleRes?>()
    val restrictInfoDetails: LiveData<GReStrictedAreaDataRuleRes?> = _restrictInfoDetails

    //躲避拥堵和禁行详情信息 页面
    private val _avoidTrafficJamsBean = MutableLiveData<AvoidTrafficJamsBean?>()
    val avoidTrafficJamsBean: LiveData<AvoidTrafficJamsBean?> = _avoidTrafficJamsBean

    //单个沿途天气数据 页面
    private val _weatherLabelItem = MutableLiveData<WeatherLabelItem?>()
    val weatherLabelItem: LiveData<WeatherLabelItem?> = _weatherLabelItem

    //单个沿途服务区数据 页面
    private val _alongWayPoiDeepInfo = MutableLiveData<AlongWayPoiDeepInfo?>()
    val alongWayPoiDeepInfo: LiveData<AlongWayPoiDeepInfo?> = _alongWayPoiDeepInfo

    //请求路线上的信息- 1:天气- 2:途径路- 3:服务区 4:请求完成 提示框
    private val _isRequestRouteInfoLoading = MutableLiveData(-1)
    val isRequestRouteInfoLoading: LiveData<Int> = _isRequestRouteInfoLoading

    //终点子POI
    private val _childPois = MutableLiveData<ArrayList<POI>>()
    val childPois: LiveData<ArrayList<POI>> = _childPois

    //终点子POI 显示隐藏状态
    private val _childPoisVisibility = MutableLiveData<Boolean>(false)
    val childPoisVisibility: LiveData<Boolean> = _childPoisVisibility

    //沿途天气按钮 显示隐藏状态
    private val _weatherVisibility = MutableLiveData<Boolean>(true)
    val weatherVisibility: LiveData<Boolean> = _weatherVisibility

    //沿途途径路按钮 显示隐藏状态
    private val _pathwayVisibility = MutableLiveData<Boolean>(true)
    val pathwayVisibility: LiveData<Boolean> = _pathwayVisibility

    //路线详情数据
    private val _naviStationFatalist = MutableLiveData<ArrayList<NaviStationItemData>>()
    val naviStationFatalist: LiveData<ArrayList<NaviStationItemData>> = _naviStationFatalist

    //删除途经点信息
    val showViaNaviViaDataDialog = MutableLiveData<NaviViaDataBean>()


    //天气信息
    private var mWeatherMap = HashMap<Int, ArrayList<WeatherLabelItem>>()

    //途径路信息
    private val mViaRoadInfoMap = HashMap<Int, ArrayList<ViaRoadInfo>>()

    /**
     * 沿途服务区信息
     */
    private var mRestAreaInfoList = ArrayList<RestAreaInfo>()

    /**
     * 用于存放沿途搜索返回且已被添加为途经点的服务区
     */
    private val mMidAlongWayPoiForServiceList = ArrayList<POI>()

    //路线数据
    var pathListLiveData = MutableLiveData<ArrayList<RoutePathItemContent>>()

    //是否删除途经点
    var deleteViaNotice = MutableLiveData<Boolean>()
    var routeErrorMessage = MutableLiveData<String>()

    //是否为添加途经点操作
    private var isShowAddPointToast = false

    private var lastMidpoi: POI? = null
    private var lastMidpoiList: ArrayList<POI> = arrayListOf()

    //是否为删除途经点操作
    private var isShowDeletePointToast = false

    private var isInit = false

    //规划路线通知
    private val _planRouteNotice = MutableLiveData(false)
    val planRouteNotice: LiveData<Boolean> = _planRouteNotice

    //规划路线请求ID
    private var requestRouteTaskId: Long = 0
    private var isNetWork: Boolean = false
    private var isRouteRestart: Boolean = false

    //是否显示沿途天气
    private var isShowRouteWeather = false

    //是否显示沿途途径路
    private var isShowRoutePathWay = false

    //是否显示沿途服务区
    private var isShowRouteService = false

    fun getShowRouteWeather(): Boolean {
        return isShowRouteWeather
    }

    fun getShowRoutePathWay(): Boolean {
        return isShowRoutePathWay
    }

    fun getShowRouteService(): Boolean {
        return isShowRouteService
    }

    //左右数值从左往右计算的  上下是从上往下计算的
    private val rectInt = RectInt().apply {
        left = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_680)
        right = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_1800)
        top = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_100)
        bottom = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_1000)
    }//由产品定义 终点字体扎点边距变化

    //是否不删除路线
    private var isNotDeleteRoute = false
    //限行图层常量
    /**
     * 不显示限行图层入口
     */
    val RESTRICTED_AREA_NO_INFO: Int = -1

    /**
     * 满足未躲避限行条件，显示限行图层提示，红色，可点击；
     */
    val RESTRICTED_AREA_NOT_AVOID: Int = 0

    /**
     * 满足已躲避限行条件，显示限行图层提示，黄色，可点击；
     */
    val RESTRICTED_AREA_AVOID: Int = 1

    /**
     * 满足已躲避限行条件，显示限行图层提示，黄色，不可点击；
     */
    val RESTRICTED_AREA_AVOID_CITY_2_MORE: Int = 2

    /**
     * setNotDeleteRoute：notDeleteRoute=true 设置发起导航不删除路线
     */
    fun setNotDeleteRoute(notDeleteRoute: Boolean) {
        isNotDeleteRoute = notDeleteRoute
    }

    fun isNotDeleteRoute(): Boolean {
        return isNotDeleteRoute
    }

    /**
     * 判断是否路线规划中
     *
     * @return true false
     */
    fun isPlanRouteing(): Boolean {
        Timber.i(" isPlanRouteing is called isInit = $isInit")
        return isInit
    }

    /**
     * 图层点击事件
     */
    private val mGestureObserver = object : MapGestureObserver() {
        override fun onLongPress(engineId: Long, px: Long, py: Long) {
            super.onLongPress(engineId, px, py)
            // 长按事件处理
            if (isInRouteFragment) {
                val geoPoint = mMapView?.operatorPosture?.screenToLonLat(px.toDouble(), py.toDouble())?.let {
                    GeoPoint(it.lon, it.lat)
                }
                mMapBusiness.searchPoiCardInfo(
                    MapPointCardData.PoiCardType.TYPE_LONG_CLICK,
                    POIFactory.createPOI("", geoPoint, "")
                )
            }
        }
    }

    /**
     * 沿途POI点击监听
     */
    private val mOnAlongWayPointClickListener: OnAlongWayPointClickListener =
        object : OnAlongWayPointClickListener {
            override fun onPointClick(type: Int, id: String) {
                Timber.i("xjh OnAlongWayPointClickListener is called type = $type，id = $id")
                var coord3DDouble: Coord3DDouble? = null
                if (type == RouteResultLayer.POINT_TYPE_ALONG_WAY_DEFAULT) {
                    //沿途搜结果POI默认类型，包含加油站、厕所、维修站、ATM
                } else if (type == RouteResultLayer.POINT_TYPE_ALONG_WAY_REST_AREA) {
                    //服务区类型，需要通过深度搜索才能获取到一些深度信息 沿途搜结果POI 服务区类型
                    var restAreaInfo: POI? = null
                    if (mMidAlongWayPoiForServiceList.isNotEmpty()) {
                        for (i in 0..mMidAlongWayPoiForServiceList.size) {
                            if (mMidAlongWayPoiForServiceList[i].id == id) {
                                restAreaInfo = mMidAlongWayPoiForServiceList[i]
                                break
                            }
                        }
                    }
                    restAreaInfo?.let {
                        mMapBusiness.setMapCenter(it.point.longitude, it.point.latitude)
                        _alongWayPoiDeepInfo.postValue(it as AlongWayPoiDeepInfo)
                    }
                }
            }

            override fun onTipAddClick(id: String?) {
                TODO("Not yet implemented")
            }
        }

    /**
     * 路线上途经点点击监听
     */
    private val mOnWayPointClickListener = OnWayPointClickListener { index ->
        Timber.i("xjh OnWayPointClickListener is called index = $index")
    }

    /**
     * 设置路径线上及路径线外点击交通事件监听
     */
    private val mOnTrafficEventClickListener: OnTrafficEventClickListener =
        object : OnTrafficEventClickListener {
            override fun onTrafficEventClick(eventID: Long, position: Coord3DDouble?) {
                Timber.i("xjh onTrafficEventClick is called eventID = $eventID")
                if (position != null) {
                    getRoadCloseEvent(eventID, Coord2DDouble(position.lon, position.lat))
                }
            }

            override fun onRouteJamPointClick(avoidJamCloudControl: AvoidJamCloudControl?) {
                Timber.i("xjh onRouteJamPointClick is called strJamRoadName = ${avoidJamCloudControl?.strJamRoadName}")
                if (avoidJamCloudControl != null) {
                    onTipViewActionClick(avoidJamCloudControl)
                }
            }

            override fun onForbiddenDetailClick(forbiddenCloudControl: ForbiddenCloudControl?) {
                Timber.i("xjh onForbiddenDetailClick is called strReason = ${forbiddenCloudControl?.strReason}")
                if (forbiddenCloudControl != null) {
                    onTipViewActionClick(forbiddenCloudControl)
                }
            }
        }

    /**
     * 沿途路线天气点击监听
     */
    private val mOnWeatherClickListener = OnWeatherClickListener { index ->
        Timber.i("xjh OnWeatherClickListener is called index = $index")
        val focusIndex = mRouteRequestController.carRouteResult.focusIndex
        val weatherList = mWeatherMap[focusIndex]

        if (!weatherList.isNullOrEmpty()) {
            val weatherData = weatherList.getOrNull(index)
            if (weatherData != null) {
                Timber.i("xjh OnWeatherClickListener weatherLabelItem = ${gson.toJson(weatherData)}")
                _weatherLabelItem.postValue(weatherData)
            }
        }
    }

    /**
     * 沿途路线途经路点击监听
     */
    private val mOnRouteViaRoadClickListener = RouteResultLayer.OnRouteViaRoadClickListener { index ->
        Timber.i("xjh OnRouteViaRoadClickListener is called index = $index")
    }

    /**
     * 图层上POI点击监听
     */
    private var iLayerClickObserver: ILayerClickObserver = object : ILayerClickObserver {
        override fun onBeforeNotifyClick(
            baseLayer: BaseLayer,
            layerItem: LayerItem,
            clickViewIdInfo: ClickViewIdInfo
        ) {
        }

        override fun onNotifyClick(
            baseLayer: BaseLayer,
            layerItem: LayerItem,
            clickViewIdInfo: ClickViewIdInfo
        ) {
            //需要特别注意：baseLayer和layerItem只能在这个函数中使用，不能抛到其它线程中使用（因为对象可能已经被释放）
            if (baseLayer == null || layerItem == null) {
                return
            }

            val businessType = layerItem.businessType
            val itemType = layerItem.itemType
            val id = layerItem.id
            Timber.i("ILayerClickObserver is called businessType: $businessType , id: ${layerItem.id}")
            when (businessType) {
                BizRouteType.BizRouteTypeViaPoint -> {
                    mDrivingLayer?.updateViaFocus()
                    if (!isInRouteFragment) {
                        return
                    }
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
                                showViaNaviViaDataDialog.postValue(NaviViaDataBean(index, text, disStr, "", address))
                                mMapBusiness.setMapCenter(
                                    carRouteResult.midPois[index].point.longitude,
                                    carRouteResult.midPois[index].point.latitude
                                )
                            }
                        }
                    }
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
            clickViewIdInfo: ClickViewIdInfo
        ) {

        }
    }

    /**
     * 点击图层上的路线切换路线监听
     */
    private val mOnRouteClickListener = OnRouteClickListener { index -> //这里是进入导航界面 导航界面不能点击这个效果
        Timber.i("RouteBusiness OnRouteClickListener is called index = $index")
        if (!isInRouteFragment) {
            return@OnRouteClickListener
        }
        val carRouteResult = mRouteRequestController.carRouteResult
        if (carRouteResult?.pathResult == null) {
            return@OnRouteClickListener
        }
        if (index !in 0 until carRouteResult.pathResult.size || index == carRouteResult.focusIndex) {
            return@OnRouteClickListener
        }
        selectPathByIndexOnMap(index)
    }

    /**
     * 比例尺监听，动态改变车标
     */
    private val mOnMapLevelChangedObserver = object : MapViewObserver() {

        override fun onClickLabel(l: Long, mapLabelItems: java.util.ArrayList<MapLabelItem>?) {
            super.onClickLabel(l, mapLabelItems)
            if (isInRouteFragment || mMapBusiness.isInPOICardFragment()) {
                Timber.i("xjh mOnMapLevelChangedObserver.onClickLabel() $l, ${mapLabelItems?.size}")
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
                    } else {
                        if (isInRouteFragment)
                            mMapBusiness.searchPoiCardInfo(MapPointCardData.PoiCardType.TYPE_LABEL, poi)
                    }
                }
            }
        }
    }

    /**
     * 路线请求成功回调
     */
    override fun callback(routeResult: IRouteResultData?, isLocal: Boolean) {
        Timber.i("RouteBusiness onRouteResultReceived is called isLocal = $isLocal")

        handleNewRoute(true)
        if (isShowAddPointToast) {
            isShowAddPointToast = false
            setToastTip(application.getString(R.string.sv_route_via_poi_add))
        }
        if (isShowDeletePointToast) {
            isShowDeletePointToast = false
            setToastTip(application.getString(R.string.sv_route_via_poi_deleted))
        }
        if (isRouteRestart) {
            isRouteRestart = false
            setToastTip(application.getString(R.string.sv_route_has_been_refreshed_successfully))
        }
        requestRouteTaskId = 0
        if (!isNetWork) {
            isRequestRoute.postValue(false)
        }
        mTripShareBusiness.initTripShareParam()
        showWeatherOrPathWay()
        AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_SUCCESS)
        SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CALCUATE_ROUTE_FINISH_SUCC)
    }

    /**
     * 路线请求错误回调
     */
    override fun errorCallback(errorCode: Int, errorMessage: String?, isLocal: Boolean) {
        Timber.i("RouteBusiness errorCallback is called errorCode = $errorCode , isLocal = $isLocal")
        AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_FAIL)
        SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CALCUATE_ROUTE_FINISH_FAIL)
        isRouteRestart = false
        //路线规划失败或取消时恢复途经点数据
        recoverViaPois()
//        focusPathIndex.postValue(-1)
//        pathListLiveData.postValue(ArrayList())//清楚历史路线数据
        routeErrorMessage.postValue(RouteErrorCodeUtils.activeErrorMessage(errorCode))
        requestRouteTaskId = 0
        if (!isNetWork) {
            isRequestRoute.postValue(false)
        }
    }

//===============================================================分割线=============================================================================

    /**
     * 初始化
     */
    fun init() {
        Timber.i("RouteBusiness init")
        isInit = true
        mMapBusiness.isRouteInit = isInit
        mMapController.setMapStyle(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            NightModeGlobal.isNightMode(),
            EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_PLAN
        )
        mUserBehaviorLayer?.clearAllItems() //隐藏收藏点
        _planRouteNotice.postValue(true)
        outsideInit()
    }

    /**
     * 外部模块初始化
     */
    fun outsideInit() {
        Timber.i("RouteBusiness outsideInit")
        mRouteResultLayer?.setOnAlongWayPointClickListener(mOnAlongWayPointClickListener)
        mRouteResultLayer?.setOnWayPointClickListener(mOnWayPointClickListener)
        mRouteResultLayer?.setOnTrafficEventClickListener(mOnTrafficEventClickListener)
        mRouteResultLayer?.setOnWeatherClickListener(mOnWeatherClickListener)
        mRouteResultLayer?.setOnRouteViaRoadClickListener(mOnRouteViaRoadClickListener)
        mRouteResultLayer?.setOnRouteClickListener(mOnRouteClickListener)
        mRouteResultLayer?.addClickObserver(iLayerClickObserver)
        mCustomLayer?.addClickObserver(iLayerClickObserver)
        mMapController.addMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mOnMapLevelChangedObserver)
        mMapView?.addGestureObserver(mGestureObserver)
        mNaviController.registerRouteWeatherObserver(mRouteWeatherClickListener)
    }

    /**
     * 反初始化
     */
    fun unInit(isRouteCleared: Boolean = false) {
        Timber.i("RouteBusiness unInit isNotDeleteRoute = $isRouteCleared")
        isInit = false
        mMapBusiness.isRouteInit = isInit
        outsideUnInit()//外部模块反初始化
        //清楚历史路线数据
        pathListLiveData.postValue(ArrayList())
        focusPathIndex.postValue(-1)
        //删除添加途经点扎点
        hideDeleteViaLayer()
        if (!isRouteCleared) {
            cleaEndChargePercentLayer()//删除电量不足图层
            mRouteResultLayer?.clearAllPaths()//清理路线
            mRouteRequestController.destroy()//删除路线数据
            mRouteEndAreaLayer?.clearAllRouteEndAreaLayer() //清除终点区域高亮
            if (BaseConstant.MULTI_MAP_VIEW)
                extMapBusiness.ex1RouteEndAreaLayer?.clearAllRouteEndAreaLayer()
            mTripShareBusiness.stop()
            _planRouteNotice.postValue(false)
        }
        isNotDeleteRoute = false
        _restrictInfoLiveData.postValue(null)
        _restrictInfoDetails.postValue(null)
        _eventCloudControl.postValue(null)
        _avoidJamCloudControl.postValue(null)
        _forbiddenCloudControl.postValue(null)
        _holidayCloudControl.postValue(null)
        _tipsCloudControl.postValue(null)
        _bankingHoursCloudControl.postValue(null)
        _avoidTrafficJamsBean.postValue(null)
        _weatherLabelItem.postValue(null)
        _alongWayPoiDeepInfo.postValue(null)
        closePreferenceSetting(false)
        _cloudTipType.postValue(CloudTipType.NONE)
        _childPois.postValue(arrayListOf())
        _naviStationFatalist.postValue(arrayListOf())
        _childPoisVisibility.postValue(false)
        closeHasPriorTip()
        mWeatherMap.clear()
        mViaRoadInfoMap.clear()
        mMidAlongWayPoiForServiceList.clear()
        mRestAreaInfoList.clear()
        mSearchBusiness.abortAll()
        mSearchBusiness.abortAllV2()
    }

    fun unitWeatherLabelItem() {
        _weatherLabelItem.postValue(null)
        mRouteResultLayer?.clearWeatherAllFocus()
    }

    fun unitAlongWayPoiDeepInfo() {
        _alongWayPoiDeepInfo.postValue(null)
        mRouteResultLayer?.clearRestAreaAllFocus()
    }

    /**
     * 外部模块反初始化
     */
    fun outsideUnInit() {
        Timber.i("RouteBusiness outsideUnInit")
        mRouteResultLayer?.setOnAlongWayPointClickListener(null)
        mRouteResultLayer?.setOnWayPointClickListener(null)
        mRouteResultLayer?.setOnTrafficEventClickListener(null)
        mRouteResultLayer?.setOnWeatherClickListener(null)
        mRouteResultLayer?.setOnRouteViaRoadClickListener(null)
        mRouteResultLayer?.setOnRouteClickListener(null)
        mRouteResultLayer?.removeClickObserver(iLayerClickObserver)
        mCustomLayer?.removeClickObserver(iLayerClickObserver)
        mMapController.removeMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mOnMapLevelChangedObserver)
        mMapView?.removeGestureObserver(mGestureObserver)
        mNaviController.unregisterRouteWeatherObserver(mRouteWeatherClickListener)
//        routeErrorMessage.postValue("")
        abortRequestTaskId()
    }

    /**
     * 开始路径规划
     * startPoi：起点POI
     * endPoi：终点POI
     * midPois：途经点POI列表
     */
    fun planRoute(
        startPoi: POI,
        endPoi: POI,
        midPois: ArrayList<POI>?,
        isNetWork: Boolean = false,//是否网络请求
        isRouteRestart: Boolean = false,//是否主动点击重新规划路线
        isViaPoiChange: Boolean = false//是否途经点变化
    ): Long {
        if (!isViaPoiChange) {
            //如果不是途经点变化，则清除之前的途经点标志位
            isShowAddPointToast = false
            isShowDeletePointToast = false
        }
        if (checkisSamePoi(startPoi, endPoi)) {
            routeErrorMessage.postValue(application.getString(R.string.sv_route_error_same_poi))
            isRequestRoute.postValue(false)
            return requestRouteTaskId
        }
        abortRequestTaskId()
        isNotDeleteRoute = false
        routeErrorMessage.postValue("")
        this.isNetWork = isNetWork
        this.isRouteRestart = isRouteRestart
        if (!isNetWork) {
            isRequestRoute.postValue(true)
        }

        //请求路线
        AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_START)
        SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CALCULATE_ROUTE_START)
        requestRouteTaskId = routeRepository.planRoute(startPoi, endPoi, midPois, this)
        checkChildPoi(endPoi)//检查是否有终点子POI
        Timber.i("planRoute requestId = $requestRouteTaskId")
        return requestRouteTaskId
    }

    /**
     * 设置离线路线样式
     */
    fun setSwitchOffline() {
        Timber.i(" setSwitchOffline is called")
        mDrivingLayer?.setSwitchOffline(false, true)
        if (BaseConstant.MULTI_MAP_VIEW) {
            extMapBusiness.ex1DrivingLayer?.setSwitchOffline(false, true)
        }
        mRouteRequestController.carRouteResult.setIsOffline(true)
    }

    // POI是否相同的距离阈值
    private val SAME_POI_DISTANCE_THRESHOLD = 20.0

    /**
     * 检查两个POI是否足够接近。
     * @param startPoi 起始POI
     * @param endPoi 结束POI
     * @return 如果两个POI的距离小于阈值，则返回true，否则返回false
     */
    fun checkisSamePoi(startPoi: POI, endPoi: POI): Boolean {
        val point1 = startPoi.point?.toCoord2DDouble() ?: return false
        val point2 = endPoi.point?.toCoord2DDouble() ?: return false
        return BizLayerUtil.calcDistanceBetweenPoints(point1, point2) < SAME_POI_DISTANCE_THRESHOLD
    }

    /**
     * 将POI点转换为Coord2DDouble对象。
     * @receiver POI点
     * @return Coord2DDouble对象
     */
    fun GeoPoint?.toCoord2DDouble(): Coord2DDouble? {
        this?.let {
            val point = Coord2DDouble()
            point.lon = it.longitude
            point.lat = it.latitude
            return point
        }
        return null
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
     * 根据routeIndex在地图上显示选中路线
     *
     * @param routeIndex 标记 routeIndex 选中第几条路线
     */
    fun selectPathByIndexOnMap(routeIndex: Int) {
        Timber.i("selectPathByIndexOnMap() called with: routeIndex = [$routeIndex]")
        setFollowMode()
        if (mRouteRequestController.carRouteResult?.pathResult.isNullOrEmpty()) {
            return
        }
        Timber.i("selectPathByIndexOnMap() pathCount = [${mRouteRequestController.carRouteResult.pathResult.size}]")
        if (routeIndex >= mRouteRequestController.carRouteResult.pathResult.size) {
            return
        }
        focusPathIndex.postValue(routeIndex)
        //设置选中的routeIndex;
        mRouteRequestController.carRouteResult.focusIndex = routeIndex
        when (routeIndex) {
            0 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_1)
            1 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_2)
            2 -> AutoStatusAdapter.sendStatus(AutoStatus.PATH_SWITCH_3)
        }
        // 切换焦点路径
        mRouteResultLayer?.switchFocusPath(routeIndex)
        //清空沿途搜结果，并全览
        mSearchLayer?.clearAllItems(BizSearchTypePoiAlongRoute)
        // 显示预览
        routeRepository.showPreview()
        // 显示到达时间
        showArrivalTime()
        //请求云控信息
        checkShowTipInfo()
        //刷新终点
        sceneSearchAndDrawEndText()
        //配置分享路线二维码数据
        updateNaviPointInfo()
        if (isShowRouteWeather) {
            clearWeatherOverlay()
            //切换路线是否请求沿途天气
            changedRouteWeather()
        }
        if (isShowRoutePathWay) {
            onClickViaRoad(false)
            //切换路线是否请求沿途途径路
            changedViaRoads()
        }
        if (isShowRouteService) {
            //清除服务区扎点
            clearRouteRestArea()
            //切换路线是否请求沿途途径路
            changeAlongWayRestArea()
        }
    }

    /**
     * 添加途经点
     */
    fun addWayPoint(poi: POI?) {
        Timber.i("addWayPoint is called")
        val carRouteResult = mRouteRequestController.carRouteResult
        carRouteResult?.let {
            if (it.midPois != null && it.midPois.size == 15) {
                setToastTip(application.getString(R.string.sv_route_result_addmid_has_15))
                //删除添加途经点扎点
                hideDeleteViaLayer()
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
            planRoute(it.fromPOI, it.toPOI, midPois, isViaPoiChange = true)
        }

    }

    /**
     * 清空途经点扎点
     */
    private fun hideDeleteViaLayer() {
        Timber.i("hideDeleteViaLayer is called")
    }

    /**
     * 清空途经点扎点
     */
    private fun cleaEndChargePercentLayer() {
        Timber.i("cleaEndChargePercentLayer is called")
    }

    /**
     * 删除途经点
     */
    fun deleteViaPoi(index: Int) {
        Timber.i("deleteViaPoi is called index = $index")
        val carRouteResult = mRouteRequestController.carRouteResult
        lastMidpoiList.clear()
        carRouteResult?.let {
            if (null != it.midPois && index < it.midPois.size) {
                isShowDeletePointToast = true
                lastMidpoiList.addAll(it.midPois)
                it.midPois.removeAt(index)
                deleteViaNotice.postValue(true)
                //重新开始路径规划
                restartPlanRoute(isViaPoiChange = true)
            }
        }
    }

    /**
     * 删除途经点
     */
    fun deleteViaPoi(poi: POI) {
        Timber.i("deleteViaPoi is called poi = $poi")
        val carRouteResult = mRouteRequestController.carRouteResult
        lastMidpoiList.clear()
        carRouteResult?.let {
            if (null != it.midPois && 0 < it.midPois.size) {
                isShowDeletePointToast = true
                lastMidpoiList.addAll(it.midPois)
                it.midPois.remove(poi)
                deleteViaNotice.postValue(true)
                //重新开始路径规划
                restartPlanRoute(isViaPoiChange = true)
            }
        }
    }

    /**
     * 删除所有途经点
     */
    fun deleteAllViaPoi() {
        Timber.i("deleteAllViaPoi is called")
        val carRouteResult = mRouteRequestController.carRouteResult
        lastMidpoiList.clear()
        carRouteResult?.let { value ->
            Timber.i("deleteAllViaPoi midPois = ${gson.toJson(value.midPois)}")
            value.midPois?.takeIf { it.isNotEmpty() }?.let {
                isShowDeletePointToast = true
                lastMidpoiList.addAll(it)
                it.clear()
                deleteViaNotice.postValue(true)
                //重新开始路径规划
                restartPlanRoute(isViaPoiChange = true)
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
    fun restartPlanRoute(isViaPoiChange: Boolean = false) {
        Timber.i("restartPlanRoute is called")
        //重新算路
        val carRouteResult = mRouteRequestController.carRouteResult
        carRouteResult?.let {
            val from = it.fromPOI
            val to = it.toPOI
            val midPois = it.midPois
            //请求路线
            planRoute(from, to, midPois, isViaPoiChange = isViaPoiChange)
        }
    }

    /**
     * 处理新路线数据
     */
    fun handleNewRoute(isFocusPathIndex: Boolean) {
        Timber.i("handleNewRoute is called")
        //删除添加途经点扎点
        hideDeleteViaLayer()
        //清空沿途搜结果、清空原有路线图层数据
        mRouteResultLayer?.let {
            it.cleanChargeStationOnRoute()
            it.clearAllPaths()
        }
        //清空搜索结果
        mSearchLayer?.let {
            it.clearAllItems(BizSearchTypePoiAlongRoute)
            it.clearAllItems(BizSearchTypePoiLabel)
            it.clearAllItems(BizSearchTypePoiParentPoint)
        }

        initData()
        drawRoute()
        mWeatherMap.clear()
        mViaRoadInfoMap.clear()
        mMidAlongWayPoiForServiceList.clear()
        mRestAreaInfoList.clear()
        clearWeatherOverlay()
        onClickViaRoad(false)
        clearRouteRestArea()
        if (isFocusPathIndex) {
            focusPathIndex.postValue(0)
            closePreferenceSetting(true)
        }
    }

    private fun initData() {
        updateCarResultDataLiveData.postValue(true)
        //请求云控信息
        checkShowTipInfo()
        setDataToRecycleView()

        //如果是场景化点击子POI进行算路后 绘制终点文本
        sceneSearchAndDrawEndText()
    }

    /**
     * 云控信息处理
     */
    private fun checkShowTipInfo() {
        RouteScope.launch {
            mRouteRequestController.carRouteResult?.pathResult?.takeIf { it.isNotEmpty() }?.let { pathResult ->
                pathResult.getOrNull(mRouteRequestController.carRouteResult.focusIndex)?.let { currentPathInfo ->
                    val hasPriorTip = findPriorTip(currentPathInfo)
                    Timber.i("checkShowTipInfo hasPriorTip = $hasPriorTip")
                    closeHasPriorTip(hasPriorTip)
                }
            }
        }
    }

    /**
     * 云控信息处理 查找存在云控的消息
     */
    private fun findPriorTip(pathInfo: PathInfo): Boolean {
        val cloudShowInfos = pathInfo.cloudShowInfo
        var mRestrictInfoBean: RestrictInfoBean? = null
        var mEventCloudControl: EventCloudControl? = null
        var mAvoidJamCloudControl: AvoidJamCloudControl? = null
        var mForbiddenCloudControl: ForbiddenCloudControl? = null
        var mHolidayCloudControl: HolidayCloudControl? = null
        var mTipsCloudControl: TipsCloudControl? = null
        var mBusinessHoursCloudControl: BankingHoursCloudControl? = null
        var mCloudTipType = CloudTipType.NONE
        var mPrior = -1
        cloudShowInfos?.forEach { cloudShowInfoList ->

            // 限行
            for (restrictCloudControl in cloudShowInfoList.vecRestrictCloudControl) {
                if (restrictCloudControl.tipsControl.prio > mPrior) {
                    mPrior = restrictCloudControl.tipsControl.prio.toInt()
                    mCloudTipType = CloudTipType.TYPE_RESTRICT
                    mRestrictInfoBean = getRestrictInfoBean(pathInfo)
                }
            }

            // 事件
            for (eventCloudControl in cloudShowInfoList.vecEventCloudControl) {
                // 封路21~40
                if (eventCloudControl.tipsControl.prio > mPrior) {
                    mPrior = eventCloudControl.tipsControl.prio.toInt()
                    mCloudTipType = CloudTipType.TYPE_ROAD_CLOSE
                    mEventCloudControl = eventCloudControl
                }
            }


            // 拥堵
            for (avoidJamCloudControl in cloudShowInfoList.vecAvoidJamCloudControl) {
                if (avoidJamCloudControl.tipsControl.prio > mPrior) {
                    mPrior = avoidJamCloudControl.tipsControl.prio.toInt()
                    mCloudTipType = CloudTipType.TYPE_AVOID_JAM
                    mAvoidJamCloudControl = avoidJamCloudControl
                }
            }

            // 禁行
            for (forbiddenCloudControl in cloudShowInfoList.vecForbiddenCloudControl) {
                if (forbiddenCloudControl.tipsControl.prio > mPrior) {
                    mPrior = forbiddenCloudControl.tipsControl.prio.toInt()
                    mCloudTipType = CloudTipType.TYPE_FORBIDDEN
                    mForbiddenCloudControl = forbiddenCloudControl
                }
            }

            // 节假日
            for (holidayCloudControl in cloudShowInfoList.vecHolidayCloudControl) {
                if (holidayCloudControl.tipsControl.prio > mPrior) {
                    mPrior = holidayCloudControl.tipsControl.prio.toInt()
                    mCloudTipType = CloudTipType.TYPE_HOLIDAY
                    mHolidayCloudControl = holidayCloudControl
                }
            }


            // 小路
            for (tipsCloudControl in cloudShowInfoList.vecTipsCloudControl) {
                if (tipsCloudControl.prio > mPrior) {
                    mPrior = tipsCloudControl.prio.toInt()
                    mCloudTipType = CloudTipType.TYPE_NARROW
                    mTipsCloudControl = tipsCloudControl
                }
            }


            // 营业时间
            for (bankingHoursCloudControl in cloudShowInfoList.vecBankingHoursCloudControl) {
                if (bankingHoursCloudControl.tipsControl.prio > mPrior) {
                    mPrior = bankingHoursCloudControl.tipsControl.prio.toInt()
                    mCloudTipType = CloudTipType.TYPE_BUSINESS_HOURS
                    mBusinessHoursCloudControl = bankingHoursCloudControl
                }
            }
        }
        _restrictInfoLiveData.postValue(mRestrictInfoBean)
        _eventCloudControl.postValue(mEventCloudControl)
        _avoidJamCloudControl.postValue(mAvoidJamCloudControl)
        _forbiddenCloudControl.postValue(mForbiddenCloudControl)
        _holidayCloudControl.postValue(mHolidayCloudControl)
        _tipsCloudControl.postValue(mTipsCloudControl)
        _bankingHoursCloudControl.postValue(mBusinessHoursCloudControl)
        _cloudTipType.postValue(mCloudTipType)
        Timber.i("checkShowTipInfo mCloudTipType = $mCloudTipType")
        Timber.i("checkShowTipInfo mRestrictInfoBean = ${gson.toJson(mRestrictInfoBean)}")
        Timber.i("checkShowTipInfo mEventCloudControl = ${gson.toJson(mEventCloudControl)}")
        Timber.i("checkShowTipInfo mAvoidJamCloudControl = ${gson.toJson(mAvoidJamCloudControl)}")
        Timber.i("checkShowTipInfo mForbiddenCloudControl = ${gson.toJson(mForbiddenCloudControl)}")
        Timber.i("checkShowTipInfo mHolidayCloudControl = ${gson.toJson(mHolidayCloudControl)}")
        Timber.i("checkShowTipInfo mTipsCloudControl = ${gson.toJson(mTipsCloudControl)}")
        Timber.i("checkShowTipInfo mBusinessHoursCloudControl = ${gson.toJson(mBusinessHoursCloudControl)}")
        return mPrior != -1
    }

    /**
     * 组装限行数据
     */
    private fun getRestrictInfoBean(pathInfo: PathInfo): RestrictInfoBean? {
        val restrictionInfo = pathInfo.restrictionInfo ?: return null
        //途经城市
        val codes = pathInfo.cityAdcodeList
//        if (codes.isNotEmpty()) {
//            for (i in codes.indices) {
//                val cityCode = codes[i].toInt()
//                var value = 0
//                if (cityCode > 100) {
//                    value = cityCode / 100 * 100
//                }
//            }
//        }
        Timber.i("handleAvoidLimitTip cityCode = ${gson.toJson(codes)}")
        Timber.i("handleAvoidLimitTip restrictionInfo =  ${gson.toJson(restrictionInfo)}")
        if (restrictionInfo.title.isEmpty()) {
            restrictionInfo.titleType = 2
        }
        mRouteRequestController.carRouteResult.setHasRestricted(true)
        mRouteRequestController.carRouteResult.cityCodes = codes
        val mTitle = getRestrictTipContent(restrictionInfo)
        val mTips = restrictionInfo.tips
        val mType: Int = restrictionInfo.titleType.toInt()
        return RestrictInfoBean().apply {
            title = mTitle
            tips = mTips
            type = mType
            isNaving = mNaviController.isNaving
        }
    }

    /**
     * 隐藏限行弹框
     */
    fun closeHasPriorTip(isClose: Boolean = false) {
        _hasPriorTipVisibility.postValue(isClose)
    }

    /**
     * 获取限行提示语
     */
    private fun getRestrictTipContent(mRestrictionInfo: RestrictionInfo?): String {
        if (mRestrictionInfo == null) {
            return ""
        }
        var restrictAreaType: Int = isRouteIsRestrict(mRestrictionInfo.titleType.toInt())
        val mCityCodes = mRouteRequestController.carRouteResult.cityCodes
        if (RESTRICTED_AREA_AVOID == restrictAreaType && mCityCodes != null && mCityCodes.size > 2) {
            restrictAreaType = RESTRICTED_AREA_AVOID_CITY_2_MORE
        }

        var tipContent: String = mRestrictionInfo.title
        when (restrictAreaType) {
            RESTRICTED_AREA_AVOID, RESTRICTED_AREA_AVOID_CITY_2_MORE ->                 // 绿色不可点
                tipContent = application.getString(R.string.route_restrict_titletype2_7_10_format)//已为您避开限行区域

            RESTRICTED_AREA_NOT_AVOID -> when (mRestrictionInfo.titleType.toInt()) {
                LimitTipsType.LimitTipsTypeRegionStart -> tipContent =
                    application.getString(R.string.route_restrict_titletype3_format)//"起点在限行区域，已无法避开"
                LimitTipsType.LimitTipsTypeRegionEnd -> tipContent =
                    application.getString(R.string.route_restrict_titletype4_format)//"终点在限行区域，已无法避开"
                LimitTipsType.LimitTipsTypeRegionVia -> tipContent =
                    application.getString(R.string.route_restrict_titletype5_format)//"途经点在限行区域，已无法避开"
                LimitTipsType.LimitTipsTypeRegionCross -> tipContent =
                    application.getString(R.string.route_restrict_titletype6_format)//"途径限行区域，已无法避开"
                LimitTipsType.LimitTipsTypeAvoidFutureSuccess -> tipContent =
                    application.getString(R.string.route_restrict_titletype7_format)//"限行即将开始，已无法避开"
                LimitTipsType.LimitTipsTypeExpiredImmediately -> tipContent =
                    application.getString(R.string.route_restrict_titletype8_format)//"限行即将结束，可正常通行"
                LimitTipsType.LimitTipsTypeWaitLimitOff, LimitTipsType.LimitTipsTypeWaitLimitOffShort -> {}
                else -> {}
            }

            else -> {}
        }
        return tipContent
    }

    /**
     * 获取路线数据提供给fragment显示路线列表
     */
    private fun setDataToRecycleView() {
        val variantPathWrapList: ArrayList<PathInfo> =
            routeRepository.getVariantPathWrapList(mRouteRequestController.carRouteResult)
        if (variantPathWrapList.size == 0) {
            return
        }
        val itemContentArrayList: ArrayList<RoutePathItemContent> =
            routeRepository.getItemContentList(variantPathWrapList)
//        VoiceOutMapUtils.setOutPutPathInfo(RoutePathUtils.createOutputPathInfo(mRouteRequestController.carRouteResult, null))
        // 将路线数据列表发布到LiveData 提供给fragment
        pathListLiveData.postValue(itemContentArrayList)
    }

    /**
     * 路线页面场景子POI搜索 和 绘制终点文本
     */
    private fun sceneSearchAndDrawEndText() {
        Timber.i("sceneSearchAndDrawEndText is call")
        val carRouteResult = mRouteRequestController.carRouteResult
        if (carRouteResult == null) {
            Timber.i("sceneSearchAndDrawEndText carRouteResult == null")
            return
        }
        val endPoi = carRouteResult.toPOI
        RouteScope.launch {
            var result = mSearchBusiness.poiIdSearchV2(endPoi, isParallel = false)
            when (result.status) {
                Status.SUCCESS -> {
                    Timber.i("sceneSearchAndDrawEndText onSuccess")
//                    if (!isInit) {
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
                                    data.poiList[0],
                                    endPoi,
                                    rectInt
                                )
                        }
                    }
                }

                Status.ERROR -> {
                    Timber.i("sceneSearchAndDrawEndText ERROR = ${result.throwable.toString()}")
//                    if (!isInit) {
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
     * 定义私有函数drawRoute，用于绘制路线
     */
    private fun drawRoute() {
        Timber.i("drawRoute is called")

        val routePoints = RoutePoints()
        routePoints.mStartPoints = ArrayList()
        val startCoord = Coord3DDouble()
        val carRouteResult = mRouteRequestController.carRouteResult ?: return
        startCoord.lon = carRouteResult.fromPOI.point.longitude
        startCoord.lat = carRouteResult.fromPOI.point.latitude
        routePoints.mStartPoints.add(RoutePoint(true, 0, 0, startCoord))
        val endCoord = Coord3DDouble()
        endCoord.lon = carRouteResult.toPOI.point.longitude
        endCoord.lat = carRouteResult.toPOI.point.latitude
        routePoints.mEndPoints.add(RoutePoint(true, 0, 1, endCoord))
        val mMidPois = carRouteResult.midPois
        mMidPois?.takeIf { it.isNotEmpty() }?.map {
            RoutePoint().apply {
                mPos = Coord3DDouble(it.point.longitude, it.point.latitude, 0.0)
            }
        }?.let { mViaPoints -> routePoints.mViaPoints = ArrayList(mViaPoints) }

        // 如果mRouteResultLayer不为空，则进行路线绘制和设置车辆模式、更新车辆样式、设置跟随模式和地图范围
        mRouteResultLayer?.let {
            it.drawRoute(
                carRouteResult.pathResult,
                routePoints,
                carRouteResult.focusIndex
            )
            it.updateCarStyle(BizCarType.BizCarTypeRoute)
        }
        setFollowMode()
        // 进行预览
        routeRepository.showPreview()
        // 显示到达时间
        showArrivalTime()
    }

    fun setFollowMode() {
        mRouteResultLayer?.let {
            it.setFollowMode(false)
            it.setPreviewMode(true) //设置预览模式
        }
    }

    /**
     * 显示预计到达时间Toast
     */
    private fun showArrivalTime() {
        Timber.i("showArrivalTime is called")
        val carRouteResult = mRouteRequestController.carRouteResult ?: return
        val pathInfo = carRouteResult.pathResult.getOrNull(carRouteResult.focusIndex)
//        val arrivalTime = pathInfo?.travelTime?.let { AutoRouteUtil.getScheduledTime(application, it, false) }
        pathInfo?.let {
            setTripShareData(it.travelTime, it.length)
        }

//        arrivalTime?.let { setToastTip(it) }

        if (ElectricInfoConverter.isElectric() && mEngineerBusiness.elecContinue.value!!) {
            setChargeStationInfo()
        }
    }

    /**
     * 设置分享路线二维码数据
     */
    private fun setTripShareData(travelTime: Long, remainDist: Long) {
        mTripShareBusiness.setCurDrivingRouteDist(0)
        mTripShareBusiness.setCurDrivingRouteTime(travelTime.toInt())
        mTripShareBusiness.setCurRouteRemainDist(remainDist.toInt())
        mTripShareBusiness.setCurRouteRemainTime(travelTime.toInt())
        mTripShareBusiness.setSpeed(0f)
    }

    /**
     * Toast信息
     */
    fun setToastTip(tip: String) = setToast.postValue(tip)

    /**
     * 配置分享路线二维码数据
     */
    private fun updateNaviPointInfo() {
        Timber.i("updateNaviPointInfo is called")
        val carRouteResult = mRouteRequestController.carRouteResult
        carRouteResult?.let { result ->
            val from = result.fromPOI
            val to = result.toPOI
            val midPois = result.midPois
            mTripShareBusiness.updateNaviPointInfo(from, to, midPois)
            showWeatherOrPathWay()
        }
    }

    /**
     * 胖屌是否显示沿途天气和沿途途经路
     */
    private fun showWeatherOrPathWay() {
        Timber.i("showWeatherOrPathWay is called")
        val carRouteResult = mRouteRequestController.carRouteResult
        carRouteResult?.let { result ->
            result.pathResult.getOrNull(result.focusIndex)?.let { path ->
                val isLongDistance = path.length > 50000
                val hasMultipleCities = (path.cityAdcodeList?.size ?: 0) > 1
                Timber.i("showWeatherOrPathWay cityAdcodeList =  ${gson.toJson(path.cityAdcodeList)} ，isLongDistance = $isLongDistance")
                //跨市且超过50公里才显示沿途天气
                _weatherVisibility.postValue(hasMultipleCities && isLongDistance)
                //跨市且超过50公里才显示沿途途经路
                _pathwayVisibility.postValue(hasMultipleCities && isLongDistance)
            }
        }
    }

    /**
     * 显示途经点扎点
     */
    fun showAddViaMap(mMapPointCardData: MapPointCardData) {
        showRoutePointCard(mMapPointCardData.poi.point.longitude, mMapPointCardData.poi.point.latitude, false)
    }

    /**
     * 接续算路，获取路线上的充电点
     */
    private fun setChargeStationInfo() {
        Timber.i("setChargeStationInfo is called")
        val carRouteResult = mRouteRequestController.carRouteResult ?: return
        carRouteResult.pathResult.getOrNull(carRouteResult.focusIndex)?.let { path ->
            if (path.isValid) {
                val chargeStationInfo = path.chargeStationInfo
                Timber.i("setChargeStationInfo chargeStationInfo = ${gson.toJson(chargeStationInfo)}")
                val elecPathInfo = path.elecPathInfo
                Timber.i("setChargeStationInfo elecPathInfo = ${gson.toJson(elecPathInfo)}")
            }
        }
    }

    /**
     * 路线详情部分展示 避开路线显示
     */
    fun onClickGroupItem(itemData: NaviStationItemData?) {
        itemData?.let {
            mRouteComponent.onClickGroupItem(it, routeRepository.getMapPreviewRect())
        }
    }

    /**
     * 请求路线详情避开路段算路
     */
    fun avoidRoute(naviStationItemDataList: ArrayList<NaviStationItemData>?) {
        naviStationItemDataList?.let {
            val carRouteResult = mRouteRequestController.carRouteResult ?: return
            abortRequestTaskId()
            routeErrorMessage.postValue("")
            isNetWork = false
            isRequestRoute.postValue(true)
            //请求路线
            requestRouteTaskId = mRouteComponent.avoidRoute(it, carRouteResult, this)
        }
    }

    /**
     * 获取路线详情页各路段信息
     */
    fun setPathNaviStationList(naviStationList: ArrayList<NaviStationItemData>) {
        _naviStationFatalist.postValue(naviStationList)
    }

    /**
     * 开启限行
     */
    fun openAvoidLimit() {
        settingComponent.setConfigKeyAvoidLimit(1)//避开限行 0关闭 1打开
        restartPlanRoute()
    }

    /**
     * 判断路径是否已避开限行
     *
     * @param titleType restrictionInfo.titleType
     * @return
     */
    private fun isRouteIsRestrict(titleType: Int): Int {
        var isAvoidRestrictedArea: Int = RESTRICTED_AREA_NO_INFO
        when (titleType) {
            LimitTipsType.LimitTipsTypeInvalid, LimitTipsType.LimitTipsTypeNoPlate, LimitTipsType.LimitTipsTypeNotOpen -> {}
            LimitTipsType.LimitTipsTypeAvoidSuccess, LimitTipsType.LimitTipsTypeAvoidFutureSuccess, LimitTipsType.LimitTipsTypeWaitLimitOffShort, LimitTipsType.LimitTipsTypeExpiredImmediately -> isAvoidRestrictedArea =
                RESTRICTED_AREA_AVOID

            LimitTipsType.LimitTipsTypeRegionStart, LimitTipsType.LimitTipsTypeRegionEnd, LimitTipsType.LimitTipsTypeRegionVia, LimitTipsType.LimitTipsTypeRegionCross, LimitTipsType.LimitTipsTypeWaitLimitOff -> isAvoidRestrictedArea =
                RESTRICTED_AREA_NOT_AVOID

            else -> {}
        }
        return isAvoidRestrictedArea
    }

    /**
     * 路线结果中解析限行图层请求参数.
     *
     *
     * 1.未避开限行时，上传的citycode为：起点，途经点1，途经点2，途经点3，终点所在城市；【最多五个】<br></br>
     * 2.已经避开限行时, 上传的citycode为：途经citycode；【最多两个】
     *
     *
     * @return
     */
    private fun getRequestAdcodes(restrictedType: Int, codes: ArrayList<Long>?): String {
        var cityCodes = ""
        if (restrictedType == RESTRICTED_AREA_AVOID) { //已避开限行
            if (codes != null && codes.size > 0) {
                val sb = StringBuilder()
                for (i in codes.indices) {
                    sb.append(codes[i])
                    sb.append("|")
                }
                cityCodes = sb.toString()
            }
        } else if (restrictedType == RESTRICTED_AREA_NOT_AVOID) { //未避开限行
            val sb = StringBuilder()
            var poi: POI = mRouteRequestController.carRouteResult.fromPOI
            if (poi.point != null) {
                sb.append(poi.point.adCode)
            }
            val pois: List<POI>? = mRouteRequestController.carRouteResult.midPois
            pois?.let {
                for (i in it.indices) {
                    poi = it[i]
                    if (poi.point != null) {
                        sb.append("|" + poi.point.adCode)
                    }
                }
            }
            poi = mRouteRequestController.carRouteResult.toPOI
            if (poi.point != null) {
                sb.append("|" + poi.point.adCode)
            }
            cityCodes = sb.toString()
        }
        return cityCodes
    }

    /**
     * 请求限行区域详情
     */
    fun getRestrictedDetail() {
        RouteScope.launch {
            var plate = ""
            var adcodes = ""
            //车牌信息通过用户模块获取
            val configKeyPlateNumber = settingComponent.getLicensePlateNumber()
            if (configKeyPlateNumber != null && !TextUtils.isEmpty(configKeyPlateNumber)) {
                plate = configKeyPlateNumber
            }
            adcodes = getRequestAdcodes(
                isRouteIsRestrict(restrictInfoLiveData.value!!.type),
                mRouteRequestController.carRouteResult.cityCodes
            )
            val result = mAosBusiness.getStrictedAreaInfo(plate, adcodes)
            Timber.i("getRestrictedDetail status = ${result.status} ，cityNum = ${gson.toJson(result.data?.citynums)}，plate =$plate")
            when (result.status) {
                Status.SUCCESS -> {
                    Timber.i("getRestrictedDetail onSuccess")
                    result.data?.let { data ->
                        _restrictInfoDetails.postValue(data)
                    }
                }

                Status.ERROR -> {
                    Timber.i("getRestrictedDetail ERROR = ${result.throwable.toString()}")
                    _restrictInfoDetails.postValue(null)
                    setToastTip("限行区域请求失败,请稍后重试！")
                }

                else -> {
                    Timber.i("getRestrictedDetail else is called")
                    _restrictInfoDetails.postValue(null)
                    setToastTip("限行区域请求失败,请稍后重试！")
                }
            }

        }
    }


    /**
     * 选择限行区域展示
     */
    fun onRuleSelected(rule: GRestrictRule?) {
        rule?.let {
            //绘制限行overlay
            drawRestrictOverlay(it)
            //地图中心移动到当前限行
            mMapBusiness.setMapCenter(it.centerpoint.lon, it.centerpoint.lat)
            //预览
            routeRepository.showRestrictPreview(it)
        }
    }

    /**
     * 绘制限行信息
     */
    private fun drawRestrictOverlay(restrictRule: GRestrictRule) {
        val bizPolygonDataLine = BizRouteRestrictInfo()
        clearRouteRestRestrict()
        val linePointsListNum = restrictRule.linepoints.size
        for (i in 0 until linePointsListNum) {
            val linePointsNum = restrictRule.linepoints[i].lstPoints.size
            val lineData = java.util.ArrayList<Coord3DDouble>()
            for (j in 0 until linePointsNum) {
                val point = restrictRule.linepoints[i].lstPoints[j]
                val linePoint = Coord3DDouble()
                linePoint.lon = point.lon
                linePoint.lat = point.lat
                linePoint.z = point.z
                lineData.add(linePoint)
            }
            bizPolygonDataLine.lineInfos.add(lineData)
        }
        if (0 < bizPolygonDataLine.lineInfos.size) {
            bizPolygonDataLine.isDrawPolygonRim = true
            mRouteResultLayer?.updateRouteRestrict(bizPolygonDataLine)
        }
        val areaPointsListNum = restrictRule.areapoints.size
        for (i in 0 until areaPointsListNum) {
            val bizPolygonDataPolygon = BizRouteRestrictInfo()
            val areaPointsNum = restrictRule.areapoints[i].lstPoints.size
            for (j in 0 until areaPointsNum) {
                val point = restrictRule.areapoints[i].lstPoints[j]
                val areaPoint = Coord3DDouble()
                areaPoint.lon = point.lon
                areaPoint.lat = point.lat
                areaPoint.z = point.z
                bizPolygonDataPolygon.polygonPoints.add(areaPoint)
                bizPolygonDataPolygon.isDrawPolygonRim = true
            }
            if (0 < bizPolygonDataPolygon.polygonPoints.size) {
                mRouteResultLayer?.updateRouteRestrict(bizPolygonDataPolygon)
            }
        }
    }

    /**
     * 清除限行相关图层
     */
    fun clearRouteRestRestrict() {
        mRouteResultLayer?.clearRouteRestRestrict()
    }

    /**
     * 主路线高亮并显示所有路径
     */
    fun switchFocusPath() {
        val carRouteResult = mRouteRequestController.carRouteResult ?: return
        mRouteResultLayer?.switchFocusPath(carRouteResult.focusIndex)

    }

    /**
     * 主路线高亮并隐藏备选路线
     */
    fun selectRoute() {
        val carRouteResult = mRouteRequestController.carRouteResult ?: return
        mRouteResultLayer?.selectRoute(carRouteResult.focusIndex)
    }

    /**
     * 请求封路事件
     */
    fun getRoadCloseEvent(trafficEventId: Long, pos2D: Coord2DDouble) {
        RouteScope.launch {
            val poiCardData = MapPointCardData(
                MapPointCardData.PoiCardType.TYPE_TRAFFIC,
                POIFactory.createPOI("", GeoPoint(pos2D.lon, pos2D.lat))
            )
            poiCardData.showLoading = true
            val result = mUserBusiness.sendReqTrafficEventDetail(trafficEventId.toString())
            Timber.i("getRoadCloseEvent: result = ${gson.toJson(result)}")
            poiCardData.showLoading = false
            val result1 = async {
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.takeIf { it.code == 1 && it.EventData != null }?.let { data ->
                            Timber.i("getRoadCloseEvent sendReqTrafficEventDetail is  SUCCESS")
                            poiCardData.showError = false
                            data.EventData?.let {
                                poiCardData.apply {
                                    locationController.lastLocation.let {
                                        poi.distance = "距离" + CommonUtils.calcDistanceBetweenPoints(
                                            application,
                                            Coord2DDouble(it.longitude, it.latitude),
                                            Coord2DDouble(poi.point.longitude, poi.point.latitude)
                                        )
                                    }
                                    poi.addr = it.address
                                    traffic_layertag = it.layertag
                                    traffic_head = it.head
                                    traffic_picurl = it.picurl
                                    traffic_desc = it.desc
                                    traffic_infotimeseg = it.infotimeseg
                                    traffic_infostartdate = it.infostartdate
                                    traffic_infoenddate = it.infoenddate
                                    traffic_nick = it.nick
                                    traffic_lastupdate = it.lastupdate
                                }
                            }
                        } ?: run {
                            poiCardData.showError = true
                        }
                    }

                    else -> {
                        poiCardData.showError = true
                        Timber.i("onUpdateTREvent sendReqTrafficEventDetail is  Fail")
                    }
                }
            }
            result1.await()
            if (!TextUtils.isEmpty(poiCardData.poi.name) || !TextUtils.isEmpty(poiCardData.traffic_head) || poiCardData.showError)
                mMapBusiness.showMapPointCard(poiCardData)
        }
    }

    fun onTipViewActionClick(data: Any) {
        if (data is AvoidJamCloudControl) {
            showAvoidJamDetail(data)
        } else if (data is ForbiddenCloudControl) {
            showForbiddenDetail(data)
        }
    }

    /**
     * 躲避拥堵数据处理
     */
    private fun showAvoidJamDetail(avoidJamCloudControl: AvoidJamCloudControl) {
        val trafficSource = application.getString(R.string.sv_route_traffic_source_update)
        val trafficLayertag = 11021
        val title = application.getString(
            R.string.sv_route_fmt_traffic_road_jam,
            avoidJamCloudControl.avoidJamDetail.strDetailRoadName
        )
        val tip = application.getString(
            R.string.sv_route_fmt_tip_avoid_jam,
            avoidJamCloudControl.strJamDist,
            avoidJamCloudControl.strJamTime
        )
        var distance = ""
        locationController.lastLocation.let {
            distance = "距离：" + CommonUtils.calcDistanceBetweenPoints(
                application,
                Coord2DDouble(it.longitude, it.latitude),
                Coord2DDouble(
                    avoidJamCloudControl.pointDetail.pointControl.pos2D.lon,
                    avoidJamCloudControl.pointDetail.pointControl.pos2D.lat
                )
            )
        }
        _avoidTrafficJamsBean.postValue(AvoidTrafficJamsBean().apply {
            traffic_layertag = trafficLayertag
            traffic_title = title
            traffic_tip = tip
            traffic_time_or_distance = distance
            traffic_source_update = trafficSource
        })

        val lon = avoidJamCloudControl.pointDetail.pointControl.pos2D.lon
        val lat = avoidJamCloudControl.pointDetail.pointControl.pos2D.lat
        showRoutePointCard(lon, lat)
    }

    /**
     * 禁行数据处理
     */
    private fun showForbiddenDetail(forbiddenCloudControl: ForbiddenCloudControl) {
        val trafficSource = application.getString(R.string.sv_route_traffic_source_update)
        val trafficLayertag = 11031


        val forbiddenDetail = forbiddenCloudControl.forbiddenDetail
        val title = when (forbiddenDetail.nForbSubType) {
            0 ->
                // 限高
                application.getString(R.string.sv_route_traffic_forbidden_limit_height)

            1 ->
                // 限宽
                application.getString(R.string.sv_route_traffic_forbidden_limit_width)

            2 ->
                // 限重
                application.getString(R.string.sv_route_traffic_forbidden_limit_weight)

            3 -> {
                // 禁左
                application.getString(R.string.sv_route_traffic_forbidden_turn_left)
            }

            4 -> {
                // 禁右
                application.getString(R.string.sv_route_traffic_forbidden_turn_right)
            }

            5 -> {
                // 禁左掉头
                application.getString(R.string.sv_route_traffic_forbidden_turn_left_back)
            }

            6 -> {
                // 禁右掉头
                application.getString(R.string.sv_route_traffic_forbidden_turn_right_back)
            }

            7 -> {
                // 禁直行
                application.getString(R.string.sv_route_traffic_forbidden_go)
            }

            else -> {
                ""
            }
        }
        val inRoad = forbiddenDetail.strInRoadName
        val nxtRoad = forbiddenDetail.strNxtRoadName
        val isInRoadUnknown = inRoad.isNullOrEmpty()
        val isNxtRoadUnknown = nxtRoad.isNullOrEmpty()
        val tip = when {
            isInRoadUnknown && isNxtRoadUnknown ->
                application.getString(R.string.sv_route_fmt_traffic_forbidden_road_section_unknown)

            isInRoadUnknown ->
                application.getString(R.string.sv_route_fmt_traffic_forbidden_road_section_in_roadName_unknown, nxtRoad ?: "")

            isNxtRoadUnknown ->
                application.getString(R.string.sv_route_fmt_traffic_forbidden_road_section_ext_roadName_unknown, inRoad ?: "")

            else ->
                application.getString(R.string.sv_route_fmt_traffic_forbidden_road_section, inRoad, nxtRoad)
        }


        val time = application.getString(
            R.string.sv_route_fmt_traffic_forbidden_time,
            forbiddenDetail.strForbTime
        )

        _avoidTrafficJamsBean.postValue(AvoidTrafficJamsBean().apply {
            traffic_layertag = trafficLayertag
            traffic_title = title
            traffic_tip = tip
            traffic_time_or_distance = time
            traffic_source_update = trafficSource
        })
        val lon = forbiddenCloudControl.pointDetail.pointControl.pos2D.lon
        val lat = forbiddenCloudControl.pointDetail.pointControl.pos2D.lat
        showRoutePointCard(lon, lat)
    }

    private fun showRoutePointCard(longitude: Double, latitude: Double, isSetZoomLevel: Boolean = true) {
        Timber.i("showRoutePointCard is called")
        mMapBusiness.setMapCenter(longitude, latitude)
        mCustomLayer?.showCustomTypePoint1(Coord3DDouble(longitude, latitude, 0.0))
        //算路显示扎点放大比例尺
        if (isSetZoomLevel)
            mMapBusiness.setZoomLevel(if (BaseConstant.currentMapviewMode == MapModeType.VISUALMODE_3D_CAR) BaseConstant.ZOOM_LEVEL_3D else BaseConstant.ZOOM_LEVEL_2D)
    }

    /**
     * 清理服务区扎点
     */
    fun clearRouteRestArea() {
        //清除服务区扎点
        mRouteResultLayer?.clearRouteRestArea()
        isShowRouteService = false
    }

    /**
     * 清理天气扎点
     */
    fun clearWeatherOverlay() {
        mRouteResultLayer?.clearRouteWeatherOverlay()
        isShowRouteWeather = false
    }

    /**
     * 途径路点击
     */
    fun onClickViaRoad(open: Boolean) {
        if (open) {
            // 先清空沿途搜的结果
            mSearchLayer?.clearAllItems(BizSearchTypePoiAlongRoute)
            changedViaRoads();
        } else {
            //清空途径路图层
            mRouteResultLayer?.clearFocus(BizRouteType.BizRouteTypeViaRoad.toLong())
            mRouteResultLayer?.clearViaRoadOverlay()
            isShowRoutePathWay = false
        }
    }

    /**
     * 沿途途径路
     */
    private fun changedViaRoads() {
        val mRouteCarResult = mRouteRequestController.carRouteResult ?: return
        var viaRoadInfos = mViaRoadInfoMap[mRouteCarResult.focusIndex]
        if (viaRoadInfos != null) {
            //如果缓存里面已经有，则无需重新请求
            Timber.i("xjh changedViaRoads is viaRoadInfos is data")
            handleViaRoads(viaRoadInfos)
            return
        }
        _isRequestRouteInfoLoading.postValue(2)
        val pathInfo = mRouteCarResult.pathResult[mRouteCarResult.focusIndex]
        viaRoadInfos = pathInfo.pathViaRoadInfo
        if (viaRoadInfos == null) {
            return
        }
        val viaRoadInfoList: ArrayList<ViaRoadInfo> = filterViaRoads(viaRoadInfos)
        handleViaRoads(viaRoadInfoList)
    }

    /**
     * 过滤连续重复名称的途径路
     *
     * @param viaRoadInfos
     * @return
     */
    private fun filterViaRoads(viaRoadInfos: ArrayList<ViaRoadInfo>): ArrayList<ViaRoadInfo> {
        val viaRoadInfoArrayList = java.util.ArrayList<ViaRoadInfo>()
        for (i in viaRoadInfos.indices) {
            val viaRoadInfo = viaRoadInfos[i]
            if (viaRoadInfoArrayList.isNotEmpty() && viaRoadInfos[i].roadName == viaRoadInfoArrayList[viaRoadInfoArrayList.size - 1].roadName) {
                continue
            }
            viaRoadInfoArrayList.add(viaRoadInfo)
        }
        return viaRoadInfoArrayList
    }

    /**
     * 沿途途径路 数据处理
     */
    private fun handleViaRoads(arrayList: ArrayList<ViaRoadInfo>?) {
        _isRequestRouteInfoLoading.postValue(4)

        arrayList?.takeIf { it.isNotEmpty() }?.let { viaRoadInfos ->
            isShowRoutePathWay = true
            val bizRouteViaRoadInfos = java.util.ArrayList<BizRouteViaRoadInfo>()
            for (i in viaRoadInfos.indices) {
                val bizRouteViaRoadInfo = BizRouteViaRoadInfo()
                bizRouteViaRoadInfo.viaRoadLabelInfo = viaRoadInfos[i]
                bizRouteViaRoadInfos.add(bizRouteViaRoadInfo)
            }
            val mRouteCarResult = mRouteRequestController.carRouteResult
            if (!mViaRoadInfoMap.containsKey(mRouteCarResult.focusIndex)) {
                mViaRoadInfoMap[mRouteCarResult.focusIndex] = viaRoadInfos
            }
            mRouteResultLayer?.updateViaRoadInfo(bizRouteViaRoadInfos);
//            // 显示预览
//            routeRepository.showPreview()
            setDefaultFocusViaRoad()
        } ?: run {
            setToastTip(application.getString(R.string.sv_route_no_routes_along_the_way))
        }
    }

    /**
     * 设置默认的途径路焦点，根据等级最好来设置焦点
     */
    private fun setDefaultFocusViaRoad() {
        val layerItems = mRouteResultLayer?.viaRoadAllItems
        if (layerItems == null || layerItems.size <= 0) {
            return
        }
        var maxPriority = 0
        var focusId = ""
        for (i in layerItems.indices) {
            val layerItem = layerItems[i]
            if (null != layerItem) {
                val priority = layerItem.priority
                if (priority > maxPriority) {
                    maxPriority = priority
                    focusId = layerItem.id
                }
            }
        }
        mRouteResultLayer?.setViaRoadLayerItemFocus(focusId)
    }

    /**
     * 沿途路线天气 处理
     */
    fun changedRouteWeather() {
        val mRouteCarResult = mRouteRequestController.carRouteResult ?: return
        // 先清空沿途搜的结果
        mSearchLayer?.clearAllItems(BizSearchTypePoiAlongRoute)
        val weatherLabelItems = mWeatherMap[mRouteCarResult.focusIndex]
        if (weatherLabelItems != null) {
            //如果缓存里面已经有，则无需重新请求
            Timber.i("xjh changedRouteWeather is weatherLabelItems is data")
            handleWeather(weatherLabelItems)
            return
        }
        if (!mNetWorkManager.isNetworkConnected()) {
            setToastTip(application.getString(R.string.sv_route_network_unconnection))
            return
        }
        _isRequestRouteInfoLoading.postValue(1)
        abortRequestTaskId()
        val pathInfo = mRouteCarResult.pathResult[mRouteCarResult.focusIndex]
        requestRouteTaskId = mNaviController.requestPathWeather(pathInfo)
        Timber.i("xjh changedRouteWeather is requestRouteTaskId = $requestRouteTaskId")
        if (requestRouteTaskId.toInt() == -1) {
            setToastTip(application.getString(R.string.sv_route_failed_to_request_weather))
            _isRequestRouteInfoLoading.postValue(4)
        }
    }

    /**
     * 沿途路线天气请求
     */
    private val mRouteWeatherClickListener = IRouteWeatherObserver { requestId, weatherLabelItems ->
        if (requestId != requestRouteTaskId) {
            Timber.i("xjh mRouteWeatherClickListener requestId != requestRouteTaskId return")
            return@IRouteWeatherObserver
        }
        handleWeather(weatherLabelItems)
    }

    /**
     * 沿途路线天气 数据处理
     */
    private fun handleWeather(arrayList: ArrayList<WeatherLabelItem>?) {
        _isRequestRouteInfoLoading.postValue(4)
        arrayList?.takeIf { it.isNotEmpty() }?.let { weatherLabelItems ->
            isShowRouteWeather = true
            val weatherInfos = ArrayList<BizRouteWeatherInfo>()
            for (i in weatherLabelItems.indices) {
                val bizRouteWeatherInfo = BizRouteWeatherInfo()
                bizRouteWeatherInfo.weatherLabelInfo = weatherLabelItems[i]
                weatherInfos.add(bizRouteWeatherInfo)
            }
            val mRouteCarResult = mRouteRequestController.carRouteResult
            if (!mWeatherMap.containsKey(mRouteCarResult.focusIndex)) {
                mWeatherMap[mRouteCarResult.focusIndex] = weatherLabelItems
            }
            mRouteResultLayer?.updateRouteWeatherInfo(weatherInfos)
            // 显示预览
            routeRepository.showPreview()
        } ?: run {
            setToastTip(application.getString(R.string.sv_route_no_weather_along_the_way))
        }
    }

    /**
     * 沿途路线服务区 处理
     */
    fun changeAlongWayRestArea() {
        // 先清空沿途搜的结果
        mSearchLayer?.clearAllItems(BizSearchTypePoiAlongRoute)
        //服务区处理
        if (mNetWorkManager.isNetworkConnected()) {
            handleAlongWayRestAreaSearch()
        } else {
            handleAlongWayRestAreaSearchOffline()
        }
    }

    /**
     * 沿途路线服务区 在线数据处理
     */
    private fun handleAlongWayRestAreaSearch() {
        RouteScope.launch {
            val mRouteCarResult = mRouteRequestController.carRouteResult ?: return@launch
            val pathInfo = mRouteCarResult.pathResult[mRouteCarResult.focusIndex]
            val mRouteServiceAreaInfoList = mRouteRequestController.getRouteAlongServiceArea(pathInfo.pathID)
            if (mRouteServiceAreaInfoList != null && mRouteServiceAreaInfoList.isNotEmpty()) {
                Timber.i("xjh handleAlongWayRestAreaSearch mRouteServiceAreaInfoList != null")
                mRestAreaInfoList = convertRouteAreaToRestAreaInfo(mRouteServiceAreaInfoList)
            } else {
                Timber.i("xjh handleAlongWayRestAreaSearch mRouteServiceAreaInfoList == null")
                val restAreaInfoList = pathInfo.getRestAreas(0, 100)
                if (restAreaInfoList != null) {
                    mRestAreaInfoList = getHasIdRestAreaInfos(restAreaInfoList)
                }
            }
            Timber.i("xjh handleAlongWayRestAreaSearch = ${gson.toJson(mRestAreaInfoList)}")
            if (mRestAreaInfoList.isNotEmpty()) {
                for (restAreaInfo in mRestAreaInfoList) {
                    restAreaInfo.remainDist = pathInfo.length - restAreaInfo.remainDist
                    restAreaInfo.remainTime = pathInfo.travelTime - restAreaInfo.remainTime
                }
                _isRequestRouteInfoLoading.postValue(3)
                val searchLineDeepInfo = SearchLineDeepInfo()
                searchLineDeepInfo.poiIds = getPoiIdList(mRestAreaInfoList)
                searchLineDeepInfo.queryType = LineDeepQueryType.eServiceArea
                val result = mSearchBusiness.lineDeepInfoSearch(searchLineDeepInfo)
                _isRequestRouteInfoLoading.postValue(4)
                when (result.status) {
                    Status.SUCCESS -> {
                        Timber.i("handleAlongWayRestAreaSearch onSuccess")
                        result.data?.let { data ->
                            Timber.i("handleAlongWayRestAreaSearch onSuccess result.data != null")
                            isShowRouteService = true
                            mRouteResultLayer?.updateRouteRestAreaInfo(
                                SearchDataConvertUtils.getBizAlongWayAreaInfo(
                                    mRestAreaInfoList
                                )
                            );
                            // 显示预览
                            routeRepository.showPreview()
                            mMidAlongWayPoiForServiceList.clear()
                            mMidAlongWayPoiForServiceList.addAll(
                                SearchDataConvertUtils.convertAlongWayDeepPoiToPoi(
                                    mRestAreaInfoList,
                                    data
                                )
                            )
                        } ?: run {
                            setToastTip(application.getString(R.string.sv_route_no_service_along_the_way))
                        }
                    }

                    Status.ERROR -> {
                        Timber.i("handleAlongWayRestAreaSearch ERROR = ${result.throwable.toString()}")
                        setToastTip(application.getString(R.string.sv_route_no_service_along_the_way))
                    }

                    else -> {
                        Timber.i("handleAlongWayRestAreaSearch else is called ")
                        setToastTip(application.getString(R.string.sv_route_no_service_along_the_way))
                    }
                }

            } else {
                setToastTip(application.getString(R.string.sv_route_no_service_along_the_way))
            }
        }
    }

    /**
     * 沿途路线服务区 离线数据处理
     */
    private fun handleAlongWayRestAreaSearchOffline() {
        val mRouteCarResult = mRouteRequestController.carRouteResult ?: return
        _isRequestRouteInfoLoading.postValue(3)
        val pathInfo = mRouteCarResult.pathResult[mRouteCarResult.focusIndex]
        val restAreaInfoList = pathInfo.getRestAreas(0, 100)
        Timber.i("xjh handleAlongWayRestAreaSearchOffline = ${gson.toJson(restAreaInfoList)}")
        if (restAreaInfoList != null && restAreaInfoList.isNotEmpty()) {
            isShowRouteService = true
            mRouteResultLayer?.updateRouteRestAreaInfo(SearchDataConvertUtils.getBizAlongWayAreaInfo(restAreaInfoList))
            // 显示预览
            routeRepository.showPreview()
            val pois = ArrayList<POI>()
            for (i in restAreaInfoList.indices) {
                val poi = AlongWayPoiDeepInfo()
                val restAreaInfo = restAreaInfoList[i]
                poi.name = restAreaInfo.serviceName
                poi.addr = restAreaInfo.serviceName
                poi.id = if (TextUtils.isEmpty(restAreaInfo.servicePOIID)) restAreaInfo.serviceName.hashCode()
                    .toString() + "" else restAreaInfo.servicePOIID

                poi.distance = (pathInfo.length - restAreaInfo.remainDist).toString()
                poi.dis = "" + AutoRouteUtil.routeResultDistance(pathInfo.length - restAreaInfo.remainDist)
                poi.travelTime = (pathInfo.travelTime - restAreaInfo.remainTime).toString()
                poi.point = GeoPoint(restAreaInfo.pos.lon, restAreaInfo.pos.lat)
                pois.add(poi)
            }
            mMidAlongWayPoiForServiceList.clear()
            mMidAlongWayPoiForServiceList.addAll(pois)
            _isRequestRouteInfoLoading.postValue(4)
        } else {
            setToastTip(application.getString(R.string.sv_route_no_service_along_the_way))
            _isRequestRouteInfoLoading.postValue(4)
        }
    }

    private fun getPoiIdList(restAreaInfos: ArrayList<RestAreaInfo>): ArrayList<String> {
        val poiIdList = java.util.ArrayList<String>()
        for (i in restAreaInfos.indices) {
            poiIdList.add(restAreaInfos[i].servicePOIID)
        }
        return poiIdList
    }

    /**
     * 将动态运营获取的服务区信息对象转成原有服务区对象
     * @param routeServiceAreaInfos
     * @return restAreaInfos
     */
    private fun convertRouteAreaToRestAreaInfo(routeServiceAreaInfos: ArrayList<RouteServiceAreaInfo>): ArrayList<RestAreaInfo> {
        val restAreaInfoArrayList = ArrayList<RestAreaInfo>()
        for (areaInfo in routeServiceAreaInfos) {
            val restAreaInfo = RestAreaInfo()
            restAreaInfo.serviceName = areaInfo.name
            restAreaInfo.servicePOIID = areaInfo.poiID
            restAreaInfo.pos = areaInfo.pos
            restAreaInfo.remainDist = areaInfo.remainDist
            restAreaInfo.remainTime = areaInfo.remainTime
            restAreaInfoArrayList.add(restAreaInfo)
        }
        return restAreaInfoArrayList
    }

    /**
     * 过滤掉服务区ID获取是空的情况
     * @param restAreaInfos
     */
    private fun getHasIdRestAreaInfos(restAreaInfos: ArrayList<RestAreaInfo>): ArrayList<RestAreaInfo> {
        val restAreaInfoList = ArrayList<RestAreaInfo>()
        for (i in restAreaInfos.indices) {
            if (TextUtils.isEmpty(restAreaInfos[i].servicePOIID)) {
                continue
            }
            restAreaInfoList.add(restAreaInfos[i])
        }
        return restAreaInfoList
    }

    fun closePreferenceSetting(isShow: Boolean) {
        closePreferenceSetting.postValue(isShow)
    }

    /**
     * 检查是否有终点子POI
     */
    private fun checkChildPoi(endPoi: POI) {
        Timber.i("checkChildPoi endPoi = ${gson.toJson(endPoi)}")
        endPoi.childPois?.takeIf { it.isNotEmpty() }?.let {
            _childPois.postValue(it)
            _childPoisVisibility.postValue(true)
        } ?: run {
            _childPois.postValue(arrayListOf())
            _childPoisVisibility.postValue(false)
        }
    }
}