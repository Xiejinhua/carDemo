package com.desaysv.psmap.base.business

import android.annotation.SuppressLint
import android.app.Application
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.aosclient.model.GReStrictedAreaDataRuleRes
import com.autonavi.gbl.aosclient.model.GTrafficEventDetailResponseParam
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.TaskStatusCode
import com.autonavi.gbl.layer.FavoritePointLayerItem
import com.autonavi.gbl.layer.model.BizAGroupType
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.layer.model.BizUserType
import com.autonavi.gbl.map.MapDevice
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.OperatorPosture
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.model.CarLoc
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.model.OpenLayerID.OpenLayerIDRouteTraffic
import com.autonavi.gbl.map.layer.observer.ICarObserver
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle
import com.autonavi.gbl.map.model.EGLColorBits
import com.autonavi.gbl.map.model.MapBusinessDataType
import com.autonavi.gbl.map.model.MapLabelItem
import com.autonavi.gbl.map.model.MapLabelType.LABEL_Type_OPENLAYER
import com.autonavi.gbl.map.model.MapParameter
import com.autonavi.gbl.map.model.MapRenderMode
import com.autonavi.gbl.map.model.MapViewPortParam
import com.autonavi.gbl.map.model.MapviewMode
import com.autonavi.gbl.map.model.MapviewModeParam
import com.autonavi.gbl.map.observer.IDeviceObserver
import com.autonavi.gbl.search.model.KeywordSearchResultV2
import com.autonavi.gbl.search.model.SearchNearestResult
import com.autonavi.gbl.user.behavior.model.FavoriteBaseItem
import com.autonavi.gbl.util.BlToolPoiID
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.account.UserGroupController
import com.autosdk.bussiness.account.observer.BehaviorServiceObserver
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.common.utils.FileUtils
import com.autosdk.bussiness.layer.AGroupLayer
import com.autosdk.bussiness.layer.CardController
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.DynamicLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.LayerControllerInitParam
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.layer.UserBehaviorLayer
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.location.constant.SdkLocStatus
import com.autosdk.bussiness.location.listener.OriginalLocationCallback
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.MapController.EMapStyleStateType
import com.autosdk.bussiness.map.MapControllerInitParam
import com.autosdk.bussiness.map.Observer.MapEventObserver
import com.autosdk.bussiness.map.Observer.MapGestureObserver
import com.autosdk.bussiness.map.Observer.MapViewObserver
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.map.SurfaceViewID.SurfaceViewID1
import com.autosdk.bussiness.map.SurfaceViewParam
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.navi.route.callback.IRouteResultCallBack
import com.autosdk.bussiness.navi.route.model.IRouteResultData
import com.autosdk.bussiness.navi.route.model.RouteCarResultData
import com.autosdk.bussiness.navi.route.utils.RouteLifecycleMonitor
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.bussiness.widget.mapview.MapViewComponent
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.common.AutoConstant
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.autosdk.common.SdkApplicationUtils
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.storage.MapSharePreference.SharePreferenceName
import com.autosdk.common.utils.CommonUtil
import com.autosdk.view.SDKMapSurfaceView
import com.desaysv.psmap.base.BuildConfig
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.auto.layerstyle.IPrepareStyleFactory
import com.desaysv.psmap.base.bean.CommutingScenariosData
import com.desaysv.psmap.base.bean.HomeCardTipsData
import com.desaysv.psmap.base.bean.HomeCardTipsType
import com.desaysv.psmap.base.bean.HomeFavoriteItem
import com.desaysv.psmap.base.bean.MapLightBarItem
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.bean.MapPointCardData.PoiCardType
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.component.UserComponent
import com.desaysv.psmap.base.data.IRouteRepository
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.LastRouteUtils
import com.desaysv.psmap.base.utils.Result
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.Status
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 主图模块业务类
 */
@Singleton
class MapBusiness @Inject constructor(
    private val app: Application,
    private val prepareStyleManager: IPrepareStyleFactory,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val mapController: MapController,
    private val locationController: LocationController,
    private val layerController: LayerController,
    private val mapDataBusiness: MapDataBusiness,
    private val mapViewComponent: MapViewComponent,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val settingComponent: ISettingComponent,
    private val searchBusiness: SearchBusiness,
    private val userBusiness: UserBusiness,
    private val engineerBusiness: EngineerBusiness,
    private val cruiseBusiness: CruiseBusiness,
    private val routeRepository: IRouteRepository,
    private val netWorkManager: NetWorkManager,
    private val iCarInfoProxy: ICarInfoProxy,
    private val userGroupController: UserGroupController,
    private val naviController: NaviController,
    private val gson: Gson
) : IDeviceObserver {
    private val mapBusinessScope = CoroutineScope(Dispatchers.IO + Job())

    @Inject
    lateinit var extMapBusiness: ExtMapBusiness

    @Inject
    lateinit var mLastRouteUtils: LastRouteUtils

    @Inject
    lateinit var mAosBusiness: AosBusiness

    @Inject
    lateinit var mForecastBusiness: ForecastBusiness

    @Inject
    lateinit var mRouteLifecycleMonitor: RouteLifecycleMonitor

    //0-21地图比例尺像素长度
    val MAIN_MAP_SCALE_LINE_LENGTH =
        intArrayOf(
            171,
            171,
            171,
            171,
            171,
            137,
            137,
            137,
            165,
            220,
            220,
            220,
            176,
            176,
            176,
            140,
            140,
            140,
            140,
            112,
            112,
            112
        )

    val glMapSurfaces = mutableListOf<SDKMapSurfaceView>()

    val glMapSurface: SDKMapSurfaceView
        get() {
            return glMapSurfaces[0]
        }

    val mainMapView: MapView by lazy {
        mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    val mainMapDevice: MapDevice
        get() {
            return mapController.getMapDevice(mainMapView)
        }

    private val mainMapLayer: MapLayer by lazy {
        layerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    private val userBehaviorLayer: UserBehaviorLayer by lazy {
        layerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    private val customLayer: CustomLayer by lazy {
        layerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //组队图层Layer
    private val aGroupLayer: AGroupLayer? by lazy {
        layerController.getAGroupLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    var mDynamicLayer: DynamicLayer? = null

    //GPS状态
    private val _gpsState = MutableLiveData(false)
    val gpsState: LiveData<Boolean> = _gpsState

    //地图等级
    private val _zoomLevel = MutableLiveData(BaseConstant.ZOOM_LEVEL_3D)
    val zoomLevel: LiveData<Float> = _zoomLevel

    //地图比例尺长度
    private val _scaleLineLength = MutableLiveData(0)
    val scaleLineLength: LiveData<Int> = _scaleLineLength

    //地图是否可以继续放大
    private val _zoomInEnable = MutableLiveData(true)
    val zoomInEnable: LiveData<Boolean> = _zoomInEnable

    //地图是否可以继续缩小
    private val _zoomOutEnable = MutableLiveData(true)
    val zoomOutEnable: LiveData<Boolean> = _zoomOutEnable

    private val _mapMode = MutableLiveData(MapModeType.VISUALMODE_3D_CAR)
    val mapMode: LiveData<Int> = _mapMode

    //回车位状态
    private val _backCCPVisible = MutableLiveData(false)
    val backCCPVisible: LiveData<Boolean> = _backCCPVisible

    //POI卡片信息
    private val _mapPointCard = MutableLiveData<MapPointCardData?>(null)
    val mapPointCard: LiveData<MapPointCardData?> = _mapPointCard

    //POI详情信息
    private val _pointDetail = MutableLiveData<MapPointCardData?>(null)
    val pointDetail: LiveData<MapPointCardData?> = _pointDetail

    //是否显示地图控制按钮
    private val _showMapControlButtons = MutableLiveData(true)
    val showMapControlButtons: LiveData<Boolean> = _showMapControlButtons

    //路线界面点击图层添加途经点监听
    private val _routeShowViaPoi = MutableLiveData<MapPointCardData?>(null)
    val routeShowViaPoi: LiveData<MapPointCardData?> = _routeShowViaPoi

    //放大缩小动作
    private val _zoomIn = MutableLiveData<Boolean?>()
    val zoomIn: LiveData<Boolean?> = _zoomIn

    //主页收藏夹
    private val _favoritesFlow: MutableStateFlow<List<HomeFavoriteItem>> =
        MutableStateFlow(emptyList())

    // 将 MutableStateFlow 转换为只读的 StateFlow
    val favoritesFlow = _favoritesFlow.asStateFlow()

    //消息卡片
    private val _tipsCardList = MutableLiveData<ArrayList<HomeCardTipsData>>(arrayListOf())
    val tipsCardList: LiveData<ArrayList<HomeCardTipsData>> = _tipsCardList

    //通勤消息
    private val _commutingScenariosData = MutableLiveData<CommutingScenariosData?>(null)
    val commutingScenariosData: LiveData<CommutingScenariosData?> = _commutingScenariosData

    //toast
    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    val backToMap = MutableLiveData<Boolean>() //回到地图主图
    val backToMapHome = MutableLiveData<Boolean>() //回到地图主图--在导航/模拟导航需要退出
    val backToNavi = MutableLiveData<Boolean>() //回到导航界面

    private var onMapAnimationFinish = true
    private var mIsMoveMap = false

    private var notInHomePage = false

    //前台是否在路线界面
    var isInRouteFragment = false

    //前台是否在添加家、公司、组队目的地地图选点界面
    var isInAddHomeFragment = false

    //前台是否在导航界面
    var isInNaviFragment = false

    private var mGroupIsFocus = false //组队是否为焦点态

    //是否处于路线中
    var isRouteInit = false

    private var searchPoiCardJob: Job? = null

    private var setHomeNeedBackCar = true

    private var checkCommutingScenariosFlag = false //检查通勤标志位

    private var loopCommutingScenariosCheck = true //定时检查

    private var mRestartMainActivity = false

    private var isInPOICardPage = false

    private val _surfaceViewRenderComplete = MutableLiveData(false)
    val surfaceViewRenderComplete: LiveData<Boolean> = _surfaceViewRenderComplete
    private var isSurfaceCreate = false

    //回车位倒计时
    private val ccpTimer = object : CountDownTimer(BaseConstant.CCP_COUNT_DOWN_TOTAL_TIME, 10000) {
        override fun onTick(millisUntilFinished: Long) = Unit
        override fun onFinish() {
            Timber.d("ccpTimer.onFinish()")
            //路线规划界面不会车位
            if (isRouteInit) {
                Timber.d("isRouteInit return")
                return
            }
            backCurrentCarPosition(!cruiseBusiness.cruiseStatus.value!!)
        }
    }

    private val naviSharePreference =
        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.navi)

    /** 内部监听对象=====================================================开始============================================================ **/

    private val mBehaviorServiceObserver: BehaviorServiceObserver =
        object : BehaviorServiceObserver {
            override fun notifyFavorite(baseItem: FavoriteBaseItem, isDelete: Boolean) {
                if (isDelete) {
                    userBehaviorLayer.removeItem(baseItem.item_id)
                } else {
                    userBehaviorLayer.removeItem(baseItem.item_id) //修复把收藏点变为家或者公司 ， 主图poi不更新问题
                    userBehaviorLayer.addFavoriteMainByFavoriteItem(baseItem)
                }
                mapBusinessScope.launch { updateHomeFavorites() }

            }
        }

    /**
     * 车标点击
     */
    private val mMapCarObserver = object : ICarObserver {
        override fun onCarClick(carLoc: CarLoc?) {
            super.onCarClick(carLoc)
            if (mIsMoveMap || (notInHomePage && _mapPointCard.value?.showStatus != true)) return
            val descPoint = carLoc?.vecPathMatchInfo?.get(0)?.let {
                GeoPoint(it.longitude, it.latitude)
            }
            searchPoiCardInfo(
                MapPointCardData.PoiCardType.TYPE_CAR_LOC,
                POIFactory.createPOI().apply { point = descPoint })
            cruiseBusiness.stopCruise()
            removeHomeCardTipsData(null)
        }

        override fun onCarLocChange(carLoc: CarLoc?) {
            super.onCarLocChange(carLoc)
        }
    }

    /**
     * 视图控制：缩放、点击
     */
    private val mMapViewObserver = object : MapViewObserver() {
        override fun onCheckIngDataRenderComplete(engineId: Long, dataCompleteStatus: Long) {
            if (isSurfaceCreate && _surfaceViewRenderComplete.value == false && dataCompleteStatus != 0L) {
                _surfaceViewRenderComplete.postValue(true)
            }
        }

        override fun onMapAnimationFinished(l: Long, l1: Long) {
            super.onMapAnimationFinished(l, l1)
            Timber.d("MapViewObserver.onMapAnimationFinished() $l, $l1")
            onMapAnimationFinish = true
        }

        override fun onMapLevelChanged(engineId: Long, bZoomIn: Boolean) {
            super.onMapLevelChanged(engineId, bZoomIn)
            val mapLevel = getZoomLevel()
            Timber.d("MapViewObserver.onMapLevelChanged() $engineId, $bZoomIn $mapLevel")
            //通知更新UI，按钮可点击状态
            if (mapLevel > zoomLevel.value!!) {
                _zoomIn.postValue(true)
            } else if (mapLevel < zoomLevel.value!!) {
                _zoomIn.postValue(false)
            }
            _zoomInEnable.postValue(isZoomInEnable())
            _zoomOutEnable.postValue(isZoomOutEnable())
            _zoomLevel.postValue(mapLevel)
            _scaleLineLength.postValue(MAIN_MAP_SCALE_LINE_LENGTH[mapLevel.toInt()])
        }

        override fun onClickLabel(l: Long, mapLabelItems: ArrayList<MapLabelItem>?) {
            super.onClickLabel(l, mapLabelItems)
            Timber.i("MapViewObserver.onClickLabel() $l, ${mapLabelItems?.size}")

            mapLabelItems?.get(0)?.let {
                Timber.i("onClickLabel $it $isRouteInit")
                if (mIsMoveMap || ((notInHomePage) && _mapPointCard.value?.showStatus != true) || isRouteInit) return
                val mapToLonLat = OperatorPosture.mapToLonLat(it.pixel20X.toDouble(), it.pixel20Y.toDouble())
                val poi = POIFactory.createPOI(it.name, GeoPoint(mapToLonLat.lon, mapToLonLat.lat), it.poiid)
                resetBackToCarTimer()
                if (LABEL_Type_OPENLAYER == it.type) {
                    when ((it.sublayerId)) {
                        2001 -> {//开放图层中的sublayerId充电桩标识为2001
                            //todo 发起充电桩深度信息搜索
                        }

                        9000005 -> {
                            //交通事件点击
                            searchPoiCardInfo(MapPointCardData.PoiCardType.TYPE_TRAFFIC, poi)
                        }
                    }
                } else {
                    searchPoiCardInfo(MapPointCardData.PoiCardType.TYPE_LABEL, poi)
                }

                if (mGroupIsFocus) {
                    UserComponent.getInstance().setGroupFocus(aGroupLayer, true, "-1")
                    mGroupIsFocus = false
                }

                cruiseBusiness.stopCruise()
                removeHomeCardTipsData(null)
            }

        }

        override fun onClickBlank(l: Long, v: Float, v1: Float) {
            if (mGroupIsFocus) {
                UserComponent.getInstance().setGroupFocus(aGroupLayer, true, "-1")
                mGroupIsFocus = false
            }
        }
    }

    /**
     * 手势识别
     */
    private val mMapGestureObserver = object : MapGestureObserver() {
        override fun onMotionEvent(engineId: Long, action: Int, px: Long, py: Long) {
            super.onMotionEvent(engineId, action, px, py)
            Timber.d("MapGestureObserver.onMotionEvent() engineId: $engineId, action: $action, px: $px, py:$py")

            if (MotionEvent.ACTION_DOWN == action) {
                removeBackToCarTimer()
                if (mGroupIsFocus) {
                    UserComponent.getInstance().setGroupFocus(aGroupLayer, true, "-1")
                    mGroupIsFocus = false
                }
                cruiseBusiness.stopCruise()
                removeHomeCardTipsData(null)
            } else if (MotionEvent.ACTION_UP == action) {
                if ((notInHomePage && _mapPointCard.value?.showStatus != true))
                    return
                resetBackToCarTimer()
            }
        }

        override fun onMoveBegin(l: Long, l1: Long, l2: Long) {
            super.onMoveBegin(l, l1, l2)
            Timber.d("MapGestureObserver.onMoveBegin() $l, $l1, $l2")
        }

        override fun onSinglePress(l: Long, l1: Long, l2: Long, b: Boolean): Boolean {
            Timber.d("MapGestureObserver.onSinglePress() $l, $l1, $l2 b=$b")
            if ((notInHomePage && _mapPointCard.value?.showStatus != true) || isRouteInit)
                return true
            //单击地图，关闭主图POI卡片显示
            if (_mapPointCard.value?.showStatus == true && !b) {
                setHomeNeedBackCar = false
                hideMapPointCard(true)
                resetBackToCarTimer()
            } else {
                setHomeNeedBackCar = true
            }

            return super.onSinglePress(l, l1, l2, b)
        }

        override fun onLongPress(engineId: Long, px: Long, py: Long) {
            super.onLongPress(engineId, px, py)
            Timber.d("MapGestureObserver.onLongPress() $engineId, $px, $py")
            if ((notInHomePage && _mapPointCard.value?.showStatus != true) || isRouteInit)
                return
            if (SurfaceViewID.transform2SurfaceViewID(engineId.toInt()) == SurfaceViewID.SURFACE_VIEW_ID_MAIN) {
                val geoPoint =
                    mainMapView.operatorPosture?.screenToLonLat(px.toDouble(), py.toDouble())?.let {
                        GeoPoint(it.lon, it.lat)
                    }
                searchPoiCardInfo(
                    MapPointCardData.PoiCardType.TYPE_LONG_CLICK,
                    POIFactory.createPOI("", geoPoint, "")
                )
            }
        }
    }

    /**
     * 图面事件：移图
     */
    private val mMapEvenObserver = object : MapEventObserver() {
        override fun onMapMoveStart(): Boolean {
            Timber.d("MapEventObserver.onMapMoveStart() notInHomePage=$notInHomePage")
            if ((notInHomePage && _mapPointCard.value?.showStatus != true) || isRouteInit)
                return true
            mIsMoveMap = true
            setFollowMode(false)
            if (_mapPointCard.value?.showStatus == true) {
                setHomeNeedBackCar = false
                hideMapPointCard(true)
            } else {
                setHomeNeedBackCar = true
            }

            return true
        }

        override fun onMapMoveEnd(): Boolean {
            Timber.d("MapEventObserver.onMapMoveEnd() notInHomePage=$notInHomePage")
            if ((notInHomePage && _mapPointCard.value?.showStatus != true))
                return true
            mIsMoveMap = false
            resetBackToCarTimer()
            return true
        }
    }

    /**
     * 用户图层点击
     */
    private val mLayerClickObserver = object : ILayerClickObserver {
        override fun onBeforeNotifyClick(
            layer: BaseLayer?,
            pItem: LayerItem?,
            clickViewIds: ClickViewIdInfo?
        ) {
            super.onBeforeNotifyClick(layer, pItem, clickViewIds)
        }

        override fun onNotifyClick(
            layer: BaseLayer?,
            pItem: LayerItem?,
            clickViewIds: ClickViewIdInfo?
        ) {
            super.onNotifyClick(layer, pItem, clickViewIds)
            if (mIsMoveMap || (notInHomePage && _mapPointCard.value?.showStatus != true)) return
            resetBackToCarTimer()
            pItem?.let {
                when (pItem.businessType) {
                    BizUserType.BizUserTypeFavoriteMain -> {
                        if (pItem is FavoritePointLayerItem) {
                            val poi = POIFactory.createPOI().apply {
                                id = userBusiness.getFavorite(FavoriteBaseItem().apply {
                                    item_id = pItem.id
                                })?.poiid
                                point = GeoPoint(pItem.position.lon, pItem.position.lat)
                            }
                            setMapCenter(pItem.position.lon, pItem.position.lat)
                            searchPoiCardInfo(MapPointCardData.PoiCardType.TYPE_FAVORITE, poi)
                        }
                    }
                }
            }
        }

        override fun onAfterNotifyClick(
            layer: BaseLayer?,
            pItem: LayerItem?,
            clickViewIds: ClickViewIdInfo?
        ) {
            super.onAfterNotifyClick(layer, pItem, clickViewIds)
        }
    }

    //组队图层点击
    private val iGroupLayerClickObserver = object : ILayerClickObserver {
        override fun onNotifyClick(
            layer: BaseLayer?,
            pItem: LayerItem?,
            clickViewIds: ClickViewIdInfo?
        ) {
            try {
                if (layer == null || pItem == null) {
                    return
                }
                val businessType = pItem.businessType
                Timber.d(
                    "iGroupLayerClickObserver businessType: %s , id: %s , mIsFocus: %s",
                    businessType,
                    pItem.id,
                    mGroupIsFocus
                )
                when (businessType) {
                    BizAGroupType.BizAGroupTypeAGroup -> {
                        resetBackToCarTimer()
                        val id: String = pItem.id
                        mGroupIsFocus = if (mGroupIsFocus) {
                            UserComponent.getInstance().setGroupFocus(aGroupLayer, mGroupIsFocus, id)
                            false
                        } else {
                            UserComponent.getInstance().setGroupFocus(aGroupLayer, mGroupIsFocus, id)
                            mapBusinessScope.launch {
                                delay(100)
                                val groupMember = userGroupController.getMemberInfo(id)
                                setMapCenter(groupMember.locInfo.lon, groupMember.locInfo.lat)
                            }
                            true
                        }
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                Timber.i("iGroupLayerClickObserver Exception:${e.message}")
            }
        }
    }

    private val gpsStateCallback = OriginalLocationCallback<SdkLocStatus> {
        when (it) {
            SdkLocStatus.ON_LOCATION_GPS_OK -> {
                AutoStatusAdapter.sendStatus(AutoStatus.GPS_OK)
                _gpsState.postValue(true)
            }

            SdkLocStatus.ON_LOCATION_OK -> {
                //AutoStatusAdapter.sendStatus(AutoStatus.DR_LOCATION_START)
            }

            SdkLocStatus.ON_LOCATION_GPS_FAIl -> {
                _gpsState.postValue(false)
            }

            SdkLocStatus.ON_LOCATION_FAIL -> {}

            else -> {}
        }
    }
    /** 内部监听对象=====================================================结束============================================================ **/


    /** 初始化和反初始化=====================================================开始============================================================ **/
    @SuppressLint("WrongConstant")
    fun initMainMapLayer() {
        Timber.i("initMainMapLayer")
        //设置主图监听
        mapController.setMaxZoomLevel(mainMapView, 19f)
        mapController.addGestureObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mMapGestureObserver)
        mapController.addMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mMapViewObserver)
        mapController.addMapEventObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mMapEvenObserver)
        mainMapLayer.addCarObserver(mMapCarObserver)
        userBehaviorLayer.addClickObserver(mLayerClickObserver)
//        aGroupLayer?.addClickObserver(iGroupLayerClickObserver)
        userBusiness.registerBehaviorServiceObserver(mBehaviorServiceObserver)

        mapController.setMapStylePath(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            sharePreferenceFactory.getThemePath()
        )
        mapController.setMapStyle(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            NightModeGlobal.isNightMode(),
            EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NORMAL
        )
        setOpenNaviLabel()
        setRenderFps(true)
        setFirstCarPosition()
        switchMapViewMode(settingComponent.getConfigKeyMapviewMode())//设置第一次地图模式
        userBusiness.showAllFavoritesItem(isShowMyFavorite())
        showEarthView(true)
        setMainMapCarMode()
        setTmcVisible(true)//暂时默认打开路况
        if (glMapSurfaces.size > 1)
            extMapBusiness.initExtMapLayer()
        engineerBusiness.initEngineerData()
        cruiseBusiness.init()
        mForecastBusiness.setLogin()

        //检查是否长时间未网络状态
        if (!netWorkManager.isNetworkConnected()) {
            sharePreferenceFactory.getMapSharePreference(SharePreferenceName.normal).putLongValue(
                MapSharePreference.SharePreferenceKeyEnum.disconnectNetworkTime,
                System.currentTimeMillis()
            )
        }
        netWorkManager.addNetWorkChangeListener(object : NetWorkManager.NetWorkChangeListener {
            override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
                val spf = sharePreferenceFactory.getMapSharePreference(SharePreferenceName.normal)
                if (isNetworkConnected) {
                    spf.putLongValue(MapSharePreference.SharePreferenceKeyEnum.disconnectNetworkTime, 0)
                    Timber.i("NetworkConnected notInHomePage=$notInHomePage")
                    MainScope().launch {
                        checkCommutingScenariosFlag()
                    }
                } else {
                    spf.getLongValue(MapSharePreference.SharePreferenceKeyEnum.disconnectNetworkTime, 0).run {
                        if (this <= 0) {
                            spf.putLongValue(
                                MapSharePreference.SharePreferenceKeyEnum.disconnectNetworkTime,
                                System.currentTimeMillis()
                            )
                        }
                    }
                }
            }
        })

        mapBusinessScope.launch {
            while (true) {
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                Timber.i("loop checkCommutingScenariosFlag currentHour=$currentHour")
                if (currentHour in 6..9 || currentHour in 17..23) {
                    //只检查一次
                    Timber.i("loop checkCommutingScenariosFlag $loopCommutingScenariosCheck")
                    if (!loopCommutingScenariosCheck) {
                        loopCommutingScenariosCheck = true
                        checkCommutingScenariosFlag()
                    }
                } else {
                    loopCommutingScenariosCheck = false
                }
                delay(120_000L)
            }
        }
    }

    /**
     * 设置车标模型 ，且为跟随状态k
     */
    private fun setMainMapCarMode() {
        val path =
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)
                .getStringValue(MapSharePreference.SharePreferenceKeyEnum.themePath, "")
        mapViewComponent.setMainMapCarMode(path, mainMapLayer)
    }

    /**
     * 开启巡航、导航标注帧率15
     */
    private fun setOpenNaviLabel() {
        mapController.setMapBusinessDataPara(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            MapBusinessDataType.MAP_BUSINESSDATA_FORCE_NAVI_LABEL,
            MapParameter().apply {
                value1 = 1 //开启导航标注
                value2 = 15 //设置帧率
                value3 = 0 //按上层设置的帧率刷新
                value4 = 0 //保留
            })
    }

    /**
     * 用于初次进入程序设置一次车标
     */
    private fun setFirstCarPosition() {
        val location = locationController.lastLocation
        setFollowMode(true)
        mainMapLayer.setCarPosition(location.longitude, location.latitude, location.bearing)
    }

    //当日夜模式修改时调用
    fun updateMapSurfaceViewInitColor(isNight: Boolean) {
        mapController.updateMapSurfaceViewInitColor(isNight)
    }

    /**
     * 初始化主图基础服务
     * @return
     */
    fun initSDKMapBase() {
        mapController.init(app.applicationContext, AutoConstant.PATH)
    }

    /**
     * 初始化主图
     */
    fun initSDKMap() {
        /** 主图初始化  */
        val surfaceViewIDList: ArrayList<Int>? = initDefaultMap()
        setupMapFont() //获取地图字体大小
        /** 注册地图设备观察者  */
        mainMapDevice.addDeviceObserver(this)

        /** 初始化图层  */
        initLayer(surfaceViewIDList)

        /** 主题数据初始化  */
        initThemeData()

        /** 车道级模块初始化  */
        if (BuildConfig.isSupportLaneMode) {
            //todo initLaneService(mockMode)
        }

        skyBoxBusiness.init()
        locationController.addOriginalGpsLocation(gpsStateCallback)
        updateMapSurfaceViewInitColor(NightModeGlobal.isNightMode())
        BaseConstant.currentMapviewMode = settingComponent.getConfigKeyMapviewMode()
        Timber.i("initSDKMap finish")
    }

    fun unInit() {
        Timber.i("unInit")
        mapController.removeGestureObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mMapGestureObserver)
        mapController.removeMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mMapViewObserver)
        mapController.removeMapEventObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mMapEvenObserver)
        mainMapLayer.removeCarObserver(mMapCarObserver)
        userBehaviorLayer.removeClickObserver(mLayerClickObserver)
        userBusiness.unregisterBehaviorServiceObserver(mBehaviorServiceObserver)
        mapController.uninit()
        skyBoxBusiness.unInit()
        locationController.removeOriginalGpsLocation(gpsStateCallback)
//        aGroupLayer?.removeClickObserver(iGroupLayerClickObserver)
        cruiseBusiness.unInit()
        mDynamicLayer?.unInit()
        if (BaseConstant.MULTI_MAP_VIEW) {
            val ex1Layer = layerController.getDynamicLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1)
            ex1Layer.unInit()
        }
        CardController.unInitCardController()
    }

    private fun initDefaultMap(): ArrayList<Int>? {
        val surfaceViewParamsArrayList = java.util.ArrayList<SurfaceViewParam>()
        for (i in 0 until BuildConfig.multiMapViewNumber) {//创建了宽高都是一样surfaceView
            val surfaceViewParam = SurfaceViewParam()
            surfaceViewParam.x = 0
            surfaceViewParam.y = 0
            surfaceViewParam.width = AutoConstant.mScreenWidth.toLong()
            surfaceViewParam.height = AutoConstant.mScreenHeight.toLong()
            surfaceViewParam.screenWidth = AutoConstant.mScreenWidth.toLong()
            surfaceViewParam.screenHeight = AutoConstant.mScreenHeight.toLong()
            glMapSurfaces.add(SDKMapSurfaceView(app.applicationContext))
            //扩展屏
            if (i == 1) {
                extMapBusiness.glMapSurfaces = glMapSurfaces[i]
                surfaceViewParam.width = BaseConstant.EX1_ZOOM_SCREEN_WIDTH
                surfaceViewParam.height = BaseConstant.EX1_ZOOM_SCREEN_HEIGHT
                surfaceViewParam.screenWidth = BaseConstant.EX1_ZOOM_SCREEN_WIDTH
                surfaceViewParam.screenHeight = BaseConstant.EX1_ZOOM_SCREEN_HEIGHT
            }
            surfaceViewParam.glMapSurface = glMapSurfaces[i]
            surfaceViewParamsArrayList.add(surfaceViewParam)
        }
        val mapControllerInitParam = MapControllerInitParam()
        mapControllerInitParam.mContext = SdkApplicationUtils.getApplication().applicationContext
        mapControllerInitParam.mStrDataPath = AutoConstant.PATH
        mapControllerInitParam.mSurfaceViewParamArrayList = surfaceViewParamsArrayList
        return mapController.init(
            mapControllerInitParam,
            settingComponent.getConfigKeyDayNightMode()
        )
    }

    //获取地图字体大小
    fun setupMapFont(
        type: Int = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)
            .getIntValue(MapSharePreference.SharePreferenceKeyEnum.mapFont, 1)
    ) {
        if (type == 1) { // 1标准 2大
            mainMapView.operatorBusiness.setMapTextScale(1.3f) // 设置底图元素大小
        } else if (type == 2) {
            mainMapView.operatorBusiness.setMapTextScale(1.6f) // 设置底图元素大小
        }
    }

    /**
     * 初始化图层
     * @param surfaceViewIDList
     */
    private fun initLayer(pSurfaceViewIDList: ArrayList<Int>?) {
        val surfaceViewIDList: ArrayList<Int> =
            if (pSurfaceViewIDList == null || pSurfaceViewIDList.size == 0) {
                ArrayList<Int>().apply { add(SurfaceViewID.SURFACE_VIEW_ID_MAIN) }
            } else {
                pSurfaceViewIDList
            }

        CardController.getInstance(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
            .init(AutoConstant.LAYER_ASSET_IMAGE_DIR, AutoConstant.FONT_DIR)
        //图层相关
        val controllerInitParams = ArrayList<LayerControllerInitParam>()
        surfaceViewIDList.map { surfaceViewID ->
            val layerControllerInitParam = LayerControllerInitParam()
            val prepareLayerStyle: IPrepareLayerStyle?
            layerController.keepCustomPrepareLayerStyle(customPrepareLayerStyle(surfaceViewID))
            val isInnerStyle =
                MapSharePreference(MapSharePreference.SharePreferenceName.eggSetting).getBooleanValue(
                    MapSharePreference.SharePreferenceKeyEnum.innerStyle, true
                )
            if (!isInnerStyle) {
                prepareLayerStyle = prepareStyleManager.getPrepareLayerStyleImpl(surfaceViewID)
            } else {
                prepareLayerStyle = prepareStyleManager.getPrepareLayerStyleInnerImpl(
                    mapController.getMapView(
                        surfaceViewID
                    )
                )
            }
            // 多屏且每屏样式配置不同，样式文件也就可以不同。为单屏或多屏同样式场景用一个配置文件style.json即可
            layerControllerInitParam.mStyleBlFilePath =
                AutoConstant.LAYER_ASSET_DIR + "style_bl.json"
            layerControllerInitParam.SurfaceViewID = surfaceViewID
            layerControllerInitParam.prepareLayerStyle = prepareLayerStyle
            controllerInitParams.add(layerControllerInitParam)
        }
        layerController.init(controllerInitParams)
        //动态图层初始化
        mDynamicLayer = layerController.getDynamicLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
        //mDynamicLayer.setPointMarkerScaleFactor()
        mDynamicLayer?.init(prepareStyleManager.getDynamicInitParam(SurfaceViewID.SURFACE_VIEW_ID_MAIN))
        if (BaseConstant.MULTI_MAP_VIEW) {
            val ex1Layer = layerController.getDynamicLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1)
            ex1Layer.init(prepareStyleManager.getDynamicInitParam(SurfaceViewID.SURFACE_VIEW_ID_EX1))
        }
    }

    private fun customPrepareLayerStyle(@SurfaceViewID.SurfaceViewID1 surfaceViewID: Int): LayerControllerInitParam {
        val layerControllerInitParam = LayerControllerInitParam()
        layerControllerInitParam.SurfaceViewID = surfaceViewID
        layerControllerInitParam.prepareLayerStyle = prepareStyleManager.getCustomPrepareStyleImpl()
        return layerControllerInitParam
    }

    /**
     * 主题数据初始化
     */
    private fun initThemeData() {
        val dataVersion = mainMapView.operatorStyle.mapAssetStyleVersion.toString()
        mapDataBusiness.initThemeDataService(
            AutoConstant.THEME_CONF_DIR,
            AutoConstant.THEME_DATA_DIR,
            dataVersion
        )
        val mSettingMsp =
            sharePreferenceFactory.getMapSharePreference(SharePreferenceName.userSetting)
        val path =
            mSettingMsp.getStringValue(MapSharePreference.SharePreferenceKeyEnum.themePath, "")
        if (TextUtils.isEmpty(path) || !FileUtils.checkFileExists(path)) {
            mSettingMsp.putStringValue(MapSharePreference.SharePreferenceKeyEnum.themePath, "")
            mSettingMsp.putIntValue(MapSharePreference.SharePreferenceKeyEnum.themeId, 0)
        }
        mapDataBusiness.initHotUpdateService(AutoConstant.RES_DIR)
    }
    /** 初始化和反初始化=====================================================结束============================================================ **/

    /** 下面是重写方法=====================================================开始============================================================ **/

    private val _firstDeviceRender = MutableLiveData(false)
    val firstDeviceRender: LiveData<Boolean> = _firstDeviceRender
    override fun onSurfaceDestroyed(deviceId: Int, width: Int, height: Int, colorBits: Int) {
        Timber.i("onSurfaceDestroyed")
        isSurfaceCreate = false
        if (_surfaceViewRenderComplete.value == true) {
            _surfaceViewRenderComplete.postValue(false)
        }
    }

    override fun onSurfaceCreated(deviceId: Int, width: Int, height: Int, colorBits: Int) {
        Timber.i("onSurfaceCreated")
        isSurfaceCreate = true
    }

    override fun onSurfaceChanged(
        deviceId: Int,
        width: Int,
        height: Int,
        @EGLColorBits.EGLColorBits1 colorBits: Int
    ) {
        val mapViewPortParam =
            MapViewPortParam(0, 0, width.toLong(), height.toLong(), width.toLong(), height.toLong())
        mainMapView.mapviewPort = mapViewPortParam
        if (firstDeviceRender.value == false) {
            _firstDeviceRender.postValue(true)//地图图层准备好
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.FIRST_DRAW)
            AutoStatusAdapter.sendStatus(AutoStatus.FIRST_FRAME_MAIN)
            AutoStatusAdapter.sendStatus(AutoStatus.FIRST_FRAME_SURFACE)
            Timber.i("onDeviceRender onSurfaceChanged")
        }
    }
    /** 重写方法=====================================================结束============================================================ **/


    /** 内部调用方法=====================================================开始============================================================ **/

    private fun isShowMyFavorite(): Boolean {
        val configKeyMyFavorite = settingComponent.getConfigKeyMyFavorite()
        Timber.i("isShowMyFavorite configKeyMyFavorite=${configKeyMyFavorite}")
        return configKeyMyFavorite == 1
    }

    fun resetBackToCarTimer(showBackCCP: Boolean = true) {
        Timber.i("resetBackToCarTimer")
        removeBackToCarTimer()
        startBackToCarTimer()
        if (showBackCCP)
            _backCCPVisible.postValue(true)
    }

    private fun removeBackToCarTimer() = ccpTimer.cancel()

    private fun startBackToCarTimer() = ccpTimer.start()

    fun setFollowMode(follow: Boolean, bPreview: Boolean = false) {
        Timber.i("setFollowMode follow = $follow ，bPreview = $bPreview")
        mainMapLayer.setPreviewMode(bPreview)
        mainMapLayer.setFollowMode(follow)//地图中心是否跟GPS位置同步变化：true 跟随模式 false 自由模式
    }

    /**
     * 刷新卡片数据状态
     */
    suspend fun poiCardAddOrDelFavorite() {
        withContext(Dispatchers.IO) {
            _mapPointCard.value?.run {
                var fPoi = poi
                if (!poi.childPois.isNullOrEmpty() && this.poi.childIndex != -1) {
                    fPoi = poi.childPois[this.poi.childIndex]
                }
                Timber.i("addOrDelFavorite ${fPoi.name}")
                if (userBusiness.isFavorited(fPoi)) {
                    if (userBusiness.delFavorite(fPoi)) {
                        _toast.postValue(app.getString(R.string.sv_common_unfavorited))
                        /*customLayer.showCustomTypePoint1(
                            Coord3DDouble(
                                fPoi.point.longitude,
                                fPoi.point.latitude,
                                0.0
                            )
                        )*/
                    }
                } else {
                    if (userBusiness.addFavorite(fPoi)) {
                        _toast.postValue(app.getString(R.string.sv_common_favorited))
                        //customLayer.hideCustomTypePoint1()
                    }
                }
                _mapPointCard.postValue(_mapPointCard.value)
            }
        }
    }

    fun updatePointCardChildPoiIndex(index: Int) {
        Timber.i("updatePointCardChildPoiIndex $index")
        _mapPointCard.value?.run {
            this.poi.childIndex = index
            _mapPointCard.postValue(_mapPointCard.value)
        }
    }

    fun retrySearchPoiCardInfo() {
        Timber.i("retrySearchPoiCardInfo")
        mapPointCard.value?.run {
            searchPoiCardInfo(this.cardType, this.poi)
        }
    }

    fun searchPoiCardInfo(@MapPointCardData.PoiCardType cardType: Int, poi: POI) {
        Timber.i("searchPoiCardInfo cardType = $cardType poi = ${gson.toJson(poi)}")
        searchPoiCardJob?.cancel()
        searchPoiCardJob = mapBusinessScope.launch {
            hideMapPointCard()
            var poiCardData = if (_mapPointCard.value?.showStatus == true) _mapPointCard.value!! else MapPointCardData(cardType, poi)
            poiCardData.showLoading = true
            if (poiCardData.showStatus) {
                _mapPointCard.postValue(poiCardData)
                if (cardType != PoiCardType.TYPE_CAR_LOC) {
                    Timber.i("searchPoiCardInfo setMapCenter")
                    setMapCenter(poi.point.longitude, poi.point.latitude)
                    customLayer.showCustomTypePoint1(Coord3DDouble(poi.point.longitude, poi.point.latitude, 0.0))
                }
                poiCardData = MapPointCardData(cardType, poi)
            } else {
                showMapPointCard(poiCardData)
            }

            val result1 = async {
                val result = when (cardType) {
                    MapPointCardData.PoiCardType.TYPE_TRAFFIC -> {
                        userBusiness.sendReqTrafficEventDetail(
                            BlToolPoiID.poiIDToEventID(
                                poi.id,
                                0
                            )
                        )
                    }

                    else -> {
                        Timber.i("showLoading22 ${poi.id}")
                        if (TextUtils.isEmpty(poi.id)) {
                            searchBusiness.nearestSearch(poi)
                        } else {
                            searchBusiness.poiIdSearchV2(poi)
                        }
                    }
                }

                poiCardData.showLoading = false
                Timber.i("showLoading false")
                when (result.status) {
                    Status.SUCCESS -> {
                        if (result.data is GTrafficEventDetailResponseParam?) {
                            poiCardData.showError = result.data?.code != 1
                            result.data?.EventData?.let {
                                Timber.i(
                                    "searchPoiCardInfo GTrafficEventDetailResponseParam ${
                                        gson.toJson(
                                            it
                                        )
                                    }"
                                )
                                poiCardData.apply {
                                    locationController.lastLocation.let {
                                        poi.distance = "距离" + CommonUtils.calcDistanceBetweenPoints(
                                            app,
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
                        } else {
                            Timber.i("searchPoiCardInfo ${poi.id}")
                            if (TextUtils.isEmpty(poi.id)) {
                                result.data as SearchNearestResult?
                                //poiCardData.showError = result.data?.code != 1
                                result.data?.poi_list?.let {
                                    if (it.isNotEmpty()) {
                                        val list = SearchCommonUtils.invertOrderList(it)
                                        list?.get(0)?.let { searchInfo ->
                                            poiCardData.apply {
                                                if (TextUtils.isEmpty(poi.name)) {
                                                    poi.name =
                                                        if (TextUtils.isEmpty(searchInfo.name)) result.data.desc else searchInfo.name
                                                }

                                                if (TextUtils.isEmpty(poi.addr)) {
                                                    poi.addr =
                                                        if (TextUtils.isEmpty(searchInfo.address)) "在" + searchInfo.name + "附近" else "在" + searchInfo.address + "附近"
                                                }

                                                locationController.lastLocation.let {
                                                    poi.distance =
                                                        CommonUtils.calcDistanceBetweenPoints(
                                                            app,
                                                            Coord2DDouble(
                                                                it.longitude,
                                                                it.latitude
                                                            ),
                                                            Coord2DDouble(
                                                                poi.point.longitude,
                                                                poi.point.latitude
                                                            )
                                                        )
                                                }
                                                poi.id = searchInfo.poiid
                                                poi.typeCode = searchInfo.typecode
                                                poi.phone = searchInfo.tel
                                                //arriveTimes = "XX分钟"
                                            }
                                            //showMapPointCard(poiCardData)
                                        }
                                    }
                                } ?: { poiCardData.showError = true }
                            } else {
                                result.data as KeywordSearchResultV2?
                                //poiCardData.showError = result.data?.code != 1
                                result.data?.poiList?.let {
                                    if (it.isNotEmpty()) {
                                        it[0]?.basicInfo?.let { searchInfo ->
                                            poiCardData.apply {
                                                poi.name = searchInfo.name
                                                poi.addr = searchInfo.address
                                                if (!TextUtils.isEmpty(searchInfo.tag))
                                                    poi.addr = searchInfo.tag + " • " + searchInfo.address
                                                locationController.lastLocation.let {
                                                    poi.distance =
                                                        CommonUtils.calcDistanceBetweenPoints(
                                                            app,
                                                            Coord2DDouble(
                                                                it.longitude,
                                                                it.latitude
                                                            ),
                                                            Coord2DDouble(
                                                                poi.point.longitude,
                                                                poi.point.latitude
                                                            )
                                                        )
                                                }
                                                poi.phone = searchInfo.tel
                                                poi.deepinfo = searchInfo.openTime
                                                //arriveTimes = "XX分钟"
                                            }
                                            //showMapPointCard(poiCardData)
                                        }
                                    }
                                    poi.childPois = ArrayList()
                                    it[0]?.childInfoList?.run {
                                        poi.childPois = SearchDataConvertUtils.getChildPoiList(this)
                                    }
                                } ?: { poiCardData.showError = true }
                            }
                        }

                    }

                    Status.ERROR -> {
                        poiCardData.showError = true
                        Timber.i(result.throwable.toString())
                    }

                    else -> {
                        Timber.i("searchPoiCardInfo $result")
                    }
                }
            }

            /* 不需要电量
            if (MapPointCardData.PoiCardType.TYPE_TRAFFIC != cardType && MapPointCardData.PoiCardType.TYPE_CAR_LOC != cardType) {
                val result2 = async {
                    val batteryValue = evManager.reqVehicleCharge(poi)
                    poiCardData.remindBatteryValue = batteryValue
                }
                result2.await()
            }*/

            if (netWorkManager.isNetworkConnected() && MapPointCardData.PoiCardType.TYPE_TRAFFIC != cardType && MapPointCardData.PoiCardType.TYPE_CAR_LOC != cardType) {
                val result2 = async {
                    val result = mAosBusiness.getDisTime(
                        endPoint = Coord2DDouble(
                            poi.point.longitude,
                            poi.point.latitude
                        )
                    )
                    when (result.status) {
                        Status.SUCCESS -> {
                            result.data?.travel_time?.run {
                                if (!TextUtils.isEmpty(this))
                                    poiCardData.poi.arriveTimes =
                                        CommonUtil.switchFromSecond(this.toInt())
                            }
                        }

                        Status.ERROR -> Timber.i(result.throwable.toString())
                        else -> {}
                    }
                }
                result2.await()
            }
            result1.await()

            if (!TextUtils.isEmpty(poiCardData.poi.name) || !TextUtils.isEmpty(poiCardData.traffic_head) || poiCardData.showError) {
                showMapPointCard(poiCardData)
            }

        }
    }

    suspend fun updateHomeFavorites() {
        withContext(Dispatchers.Default) {
            Timber.i("updateHomeFavorites")
            _favoritesFlow.tryEmit(userBusiness.getHomeFavorites())
        }
    }
    /** 内部调用方法=====================================================结束============================================================ **/


    /** 外部调用方法=====================================================开始============================================================ **/
    fun setIsInHomePage(isHomeFragment: Boolean) {
        notInHomePage = !isHomeFragment
        cruiseBusiness.setInHomePage(isHomeFragment)
        Timber.i("notInHomePage $notInHomePage")
        if (notInHomePage) {
            removeHomeCardTipsData(null)
            if (_mapPointCard.value?.showStatus != true)
                removeBackToCarTimer()
        } else {
            //回到主图设置回主图样式
            mapController.setMapStyleNotForce(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                NightModeGlobal.isNightMode(),
                EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NORMAL
            )
        }
        MainScope().launch {
            showEarthView(isHomeFragment)
            if (isHomeFragment && setHomeNeedBackCar) {
                backCurrentCarPosition()
            } else {
                setHomeNeedBackCar = true
            }
            if (isHomeFragment) {
                userBusiness.showAllFavoritesItem(isShowMyFavorite())
                if (checkCommutingScenariosFlag) {
                    checkCommutingScenariosFlag = false
                    checkCommutingScenarios()
                }
            }
        }
    }

    fun isInHomeFragment(): Boolean {
        return !notInHomePage
    }

    fun setIsInPOICardPage(isPOICardPage: Boolean) {
        Timber.i("setIsInPOICardPage isPOICardPage:$isPOICardPage")
        isInPOICardPage = isPOICardPage
    }

    fun isInPOICardFragment(): Boolean {
        return isInPOICardPage
    }

    /**
     * 控制主图按钮显隐(放大/缩小/切换视角)
     * @param isShow 显示/隐藏
     */
    fun showMapControlButtons(isShow: Boolean) {
        Timber.i("showMapControlButtons isShow:$isShow")
        _showMapControlButtons.postValue(isShow)
    }

    fun isZoomInEnable(): Boolean {
        return mapController.isZoomInEnable(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    fun isZoomOutEnable(): Boolean {
        return mapController.isZoomOutEnable(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    /**
     * 放大地图
     */
    fun mapZoomIn() {
        Timber.i("setMapZoomIn()")
        mapController.mapZoomIn(SurfaceViewID.SURFACE_VIEW_ID_MAIN, true, true)
    }

    /**
     * 缩小地图
     */
    fun mapZoomOut() {
        Timber.i("setMapZoomOut()")
        mapController.mapZoomOut(SurfaceViewID.SURFACE_VIEW_ID_MAIN, true, true)
    }

    fun setZoomLevel(level: Float) {
        mapController.setZoomLevel(SurfaceViewID.SURFACE_VIEW_ID_MAIN, level)
    }

    fun getZoomLevel(): Float {
        return mapController.getZoomLevel(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    fun getScaleLineLengthDesc(): String {
        val length = mapController.getScale(SurfaceViewID.SURFACE_VIEW_ID_MAIN, getZoomLevel())
        Timber.d("getScaleLineLengthDesc length=$length")
        return CommonUtils.getScaleLineLengthDesc(app, length)
    }

    fun getScaleLineLength(): Int {
        return MAIN_MAP_SCALE_LINE_LENGTH[getZoomLevel().toInt()]
    }

    fun setMapCenter(lon: Double, lat: Double) {
        Timber.d("setMapCenter lon=$lon lat=$lat")
        mainMapView.operatorPosture?.setMapCenter(lon, lat, 0.0, false, true)
    }

    fun resetShowPoi(mMapPointCardData: MapPointCardData? = null) {
        _routeShowViaPoi.postValue(mMapPointCardData)
    }


    suspend fun accountStatusChange() {
        Timber.i("accountStatusChange()")
        updateHomeFavorites()
        //switchMapViewMode(settingComponent.getConfigKeyMapviewMode())//设置第一次地图模式
        userBusiness.showAllFavoritesItem(isShowMyFavorite())
    }

    fun setClickLabelMoveMap(moveMap: Boolean) {
        mainMapLayer.setClickLabelMoveMap(moveMap)
    }

    fun renderPause(@SurfaceViewID1 nSurfaceViewID: Int) {
        mapController.renderPause(nSurfaceViewID)
    }

    fun renderResume(@SurfaceViewID1 nSurfaceViewID: Int) {
        mapController.renderResume(nSurfaceViewID)
    }

    fun showMapPointCard(mapPointCardData: MapPointCardData) {
        Timber.i("showMapPointCard ${gson.toJson(mapPointCardData)}")
        if ((isRouteInit && mapPointCardData.cardType != PoiCardType.TYPE_TRAFFIC) || isInAddHomeFragment) {
            Timber.i("showMapPointCard isInRouteFragment")
            _routeShowViaPoi.postValue(mapPointCardData)
        } else {
            setFollowMode(false)
            mapPointCardData.showStatus = true
            mapPointCardData.poi.point?.let {
                if (mapPointCardData.cardType != MapPointCardData.PoiCardType.TYPE_CAR_LOC) {
                    Timber.i("showMapPointCard showCustomTypePoint1")
                    setMapCenter(it.longitude, it.latitude)
                    customLayer.showCustomTypePoint1(Coord3DDouble(it.longitude, it.latitude, 0.0))
                }
            }
            _mapPointCard.postValue(mapPointCardData)
            _backCCPVisible.postValue(true)
            if (isRouteInit) {
                //算路显示扎点放大比例尺
                setZoomLevel(if (BaseConstant.currentMapviewMode == MapModeType.VISUALMODE_3D_CAR) BaseConstant.ZOOM_LEVEL_3D else BaseConstant.ZOOM_LEVEL_2D)
            }
        }
    }

    fun hideMapPointCard(hideFragment: Boolean = false) {
        Timber.i("hideMapPointCard hideFragment=$hideFragment")
        customLayer.hideCustomTypePoint1()
        _mapPointCard.value?.showLoading = false
        _mapPointCard.value?.showError = false
        if (hideFragment) {
            _mapPointCard.value?.let {
                it.showStatus = false
                _mapPointCard.postValue(it)
            }
        }
    }

    private fun showPointDetail(mapPointCardData: MapPointCardData) {
        Timber.i("showPointDetail ${gson.toJson(mapPointCardData)}")
        setFollowMode(false)
        mapPointCardData.showStatus = true
        mapPointCardData.poi.point?.let {
            if (mapPointCardData.cardType != MapPointCardData.PoiCardType.TYPE_CAR_LOC) {
                Timber.i("showPointDetail showCustomTypePoint1")
                setMapCenter(it.longitude, it.latitude)
                customLayer.showCustomTypePoint1(Coord3DDouble(it.longitude, it.latitude, 0.0))
            }
        }
        removeBackToCarTimer()
        _pointDetail.postValue(mapPointCardData)
        _backCCPVisible.postValue(false)
    }

    fun hidePointDetail(hideFragment: Boolean = false) {
        Timber.i("hidePointDetail hideFragment=$hideFragment")
        customLayer.hideCustomTypePoint1()
        _pointDetail.value?.showLoading = false
        _pointDetail.value?.showError = false
        if (hideFragment) {
            _pointDetail.value?.let {
                it.showStatus = false
                _pointDetail.postValue(it)
            }
        }
    }

    /**
     * 回车位
     * @param backMapViewMode false 只回车位车标姿态不变化
     */
    @SuppressLint("WrongConstant")
    fun backCurrentCarPosition(backMapViewMode: Boolean = true) {
        Timber.i("backCurrentCarPosition() backMapViewMode $backMapViewMode")
        setFollowMode(true)
        if (backMapViewMode) {
            switchMapViewMode(settingComponent.getConfigKeyMapviewMode())
        } else {
            mapController.goToDefaultPosition(SurfaceViewID.SURFACE_VIEW_ID_MAIN, false)
            setZoomLevel(if (BaseConstant.currentMapviewMode == MapModeType.VISUALMODE_3D_CAR) BaseConstant.ZOOM_LEVEL_3D else BaseConstant.ZOOM_LEVEL_2D)
        }
        removeBackToCarTimer()
        _backCCPVisible.postValue(false)
        hideMapPointCard(true)
        searchPoiCardJob?.cancel()
        iCarInfoProxy.getScreenStatus().value?.let { setMapCenterNavi(it) }
    }

    /**
     * 回车位--针对个人中心&设置
     */
    @SuppressLint("WrongConstant")
    fun backCurrentCarPositionOther() {
        Timber.i("backCurrentCarPositionOther() ")
        setFollowMode(true)
        mapController.goToDefaultPosition(SurfaceViewID.SURFACE_VIEW_ID_MAIN, false)
        setZoomLevel(if (BaseConstant.currentMapviewMode == MapModeType.VISUALMODE_3D_CAR) BaseConstant.ZOOM_LEVEL_3D else BaseConstant.ZOOM_LEVEL_2D)
        removeBackToCarTimer()
        _backCCPVisible.postValue(false)
        hidePointDetail(true)
        searchPoiCardJob?.cancel()
        iCarInfoProxy.getScreenStatus().value?.let { setMapCenterNavi(it) }
    }

    /**
     * 返回自车位置
     * @param surfaceViewID 屏幕视图ID
     * @param bAnimation    是否携带动画
     */
    fun goToDefaultPosition(bAnimation: Boolean) {
        mapController.goToDefaultPosition(SurfaceViewID.SURFACE_VIEW_ID_MAIN, bAnimation)
    }

    /**
     * 设置或者切换地图朝向模式
     */
    fun switchMapViewMode(@MapModeType mapMode: Int = MapModeType.VISUALMODE_UNKNOW) {
        onMapAnimationFinish = false
        val curMode =
            if (BaseConstant.currentMapviewMode == MapModeType.VISUALMODE_UNKNOW) settingComponent.getConfigKeyMapviewMode() else BaseConstant.currentMapviewMode
        Timber.i("switchMapViewMode curMode=$curMode mapMode=$mapMode")
        val mapviewMode = if (mapMode == MapModeType.VISUALMODE_UNKNOW) { //切换地图模式
            when (curMode) {
                MapModeType.VISUALMODE_2D_CAR -> {
                    BaseConstant.currentMapviewMode = MapModeType.VISUALMODE_3D_CAR
                    MapviewMode.MapviewMode3D
                }

                MapModeType.VISUALMODE_3D_CAR -> {
                    BaseConstant.currentMapviewMode = MapModeType.VISUALMODE_2D_NORTH
                    MapviewMode.MapviewModeNorth
                }

                else -> {
                    BaseConstant.currentMapviewMode = MapModeType.VISUALMODE_2D_CAR
                    MapviewMode.MapviewModeCar
                }
            }
        } else {//设置地图模式
            BaseConstant.currentMapviewMode = mapMode
            when (mapMode) {
                MapModeType.VISUALMODE_2D_CAR -> {
                    MapviewMode.MapviewModeCar
                }

                MapModeType.VISUALMODE_3D_CAR -> {
                    MapviewMode.MapviewMode3D
                }

                else -> {
                    MapviewMode.MapviewModeNorth
                }
            }
        }

        //设置视图朝向
        val param = MapviewModeParam().apply {
            bChangeCenter = true
            mode = mapviewMode
            mapZoomLevel =
                if (mapviewMode == MapviewMode.MapviewMode3D) BaseConstant.ZOOM_LEVEL_3D else BaseConstant.ZOOM_LEVEL_2D
        }
        setMapMode(SurfaceViewID.SURFACE_VIEW_ID_MAIN, param, true)
        _mapMode.postValue(BaseConstant.currentMapviewMode)
        settingComponent.setConfigKeyMapviewMode(BaseConstant.currentMapviewMode)
    }

    /**
     * 设置导航的时中心的位置
     * screenStatus = true：分屏中  false：全屏
     */
    fun setMapCenterNavi(screenStatus: Boolean) {
        Timber.i("setMapCenterNavi is called screenStatus = $screenStatus")//分屏状态
        val curMode =
            if (BaseConstant.currentMapviewMode == MapModeType.VISUALMODE_UNKNOW) settingComponent.getConfigKeyMapviewMode() else BaseConstant.currentMapviewMode
        var percentX = 0f
        percentX = if (screenStatus) {
            0.67f
        } else {
            0.5f
        }
        val percentY: Float = if (curMode === MapModeType.VISUALMODE_2D_NORTH) 0.5f else 0.67f
        //导航时  默认是水平
        mainMapView.setMapProjectionCenter(percentX, percentY)
    }

    /**
     * 设置地图朝向模式
     * @param surfaceViewID 屏幕视图ID
     * @param modeParam     模式参数
     * @param bAnimation    是否携带动画
     */
    fun setMapMode(
        @SurfaceViewID1 surfaceViewID: Int,
        modeParam: MapviewModeParam?,
        bAnimation: Boolean
    ) {
        mapController.setMapMode(surfaceViewID, modeParam, bAnimation)
    }

    fun showEarthView(isShow: Boolean) {
        if (BuildConfig.isSupportEarthMap) mapController.showEarthView(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            isShow,
            4f,
            1f
        )
    }

    /**
     * APP前后台设置帧率
     */
    fun setRenderFps(isForeground: Boolean) {
        if (isForeground) {
            // 修改正常场景下的帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                MapRenderMode.MapRenderModeNormal,
                engineerBusiness.engineerConfig.foreground_MapRenderModeNormal // SDK默认帧率：15帧
            )
            // 修改导航场景下的帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                MapRenderMode.MapRenderModeNavi,
                engineerBusiness.engineerConfig.foreground_MapRenderModeNavi // SDK默认帧率：15帧
            )
            // 修改动画场景下的帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                MapRenderMode.MapRenderModeAnimation,
                engineerBusiness.engineerConfig.foreground_MapRenderModeAnimation // SDK默认帧率：30帧
            )
            // 修改手势操作时帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                MapRenderMode.MapRenderModeGestureAction,
                engineerBusiness.engineerConfig.foreground_MapRenderModeGestureAction // SDK默认帧率：40帧
            )
        } else {
            // 修改正常场景下的帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                MapRenderMode.MapRenderModeNormal,
                engineerBusiness.engineerConfig.backend_MapRenderModeNormal // SDK默认帧率：15帧
            )
            // 修改导航场景下的帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                MapRenderMode.MapRenderModeNavi,
                engineerBusiness.engineerConfig.backend_MapRenderModeNavi // SDK默认帧率：15帧
            )
            // 修改动画场景下的帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                MapRenderMode.MapRenderModeAnimation,
                engineerBusiness.engineerConfig.backend_MapRenderModeAnimation // SDK默认帧率：30帧
            )
            // 修改手势操作时帧率
            mapController.setRenderFpsByMode(
                SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                MapRenderMode.MapRenderModeGestureAction,
                engineerBusiness.engineerConfig.backend_MapRenderModeGestureAction// SDK默认帧率：40帧
            )
        }
    }

    //打开关闭路况
    fun setTmcVisible(flag: Boolean) {
        Timber.i("setTmcVisible $flag")
        mapController.setTmcVisible(SurfaceViewID.SURFACE_VIEW_ID_MAIN, flag)//打开路况
        SdkAdapterManager.getInstance()
            .sendNormalMessage(if (flag) AutoState.TMC_ON else AutoState.TMC_OFF)
    }

    fun setRouteTrafficVisible(flag: Boolean) {
        Timber.i("setBaseMapIconVisible $flag")
        mapController.setBaseMapIconVisible(
            SurfaceViewID.SURFACE_VIEW_ID_MAIN,
            OpenLayerIDRouteTraffic,
            flag
        )//打开路况
    }

    /**
     * 外部显示POI卡片
     */
    fun showPoiCard(poi: POI) {
        Timber.i("showPoiCard")
        val cardData = MapPointCardData(MapPointCardData.PoiCardType.TYPE_LABEL, poi).apply {
            locationController.lastLocation.let {
                poi.distance = CommonUtils.calcDistanceBetweenPoints(
                    app,
                    Coord2DDouble(it.longitude, it.latitude),
                    Coord2DDouble(poi.point.longitude, poi.point.latitude)
                )
            }
        }
        mapBusinessScope.launch {
            //val batteryValue = evManager.reqVehicleCharge(poi)
            //cardData.remindBatteryValue = batteryValue
            if (netWorkManager.isNetworkConnected()) {
                val result = async {
                    val result = mAosBusiness.getDisTime(
                        endPoint = Coord2DDouble(
                            poi.point.longitude,
                            poi.point.latitude
                        )
                    )
                    when (result.status) {
                        Status.SUCCESS -> {
                            result.data?.travel_time?.run {
                                if (!TextUtils.isEmpty(this))
                                    cardData.poi.arriveTimes =
                                        CommonUtil.switchFromSecond(this.toInt())
                            }
                        }

                        Status.ERROR -> Timber.i(result.throwable.toString())
                        else -> {}
                    }
                }
                result.await()
            }
            showMapPointCard(cardData)
        }
    }

    /**
     * 显示POI详情，不回主图
     */
    fun showPoiDetail(poi: POI) {
        Timber.i("showPoiDetail")
        val cardData = MapPointCardData(MapPointCardData.PoiCardType.TYPE_LABEL, poi).apply {
            locationController.lastLocation.let {
                poi.distance = CommonUtils.calcDistanceBetweenPoints(
                    app,
                    Coord2DDouble(it.longitude, it.latitude),
                    Coord2DDouble(poi.point.longitude, poi.point.latitude)
                )
            }
        }
        mapBusinessScope.launch {
            if (netWorkManager.isNetworkConnected()) {
                val result = async {
                    val result = mAosBusiness.getDisTime(
                        endPoint = Coord2DDouble(
                            poi.point.longitude,
                            poi.point.latitude
                        )
                    )
                    when (result.status) {
                        Status.SUCCESS -> {
                            result.data?.travel_time?.run {
                                if (!TextUtils.isEmpty(this))
                                    cardData.poi.arriveTimes =
                                        CommonUtil.switchFromSecond(this.toInt())
                            }
                        }

                        Status.ERROR -> Timber.i(result.throwable.toString())
                        else -> {}
                    }
                }
                result.await()
            }
            showPointDetail(cardData)
        }
    }

    /**
     * 是否显示所有收藏扎点
     */
    fun showAllFavoritesItem() {
        userBusiness.showAllFavoritesItem(isShowMyFavorite())
    }

    /**
     * 暂停渲染帧率
     */
    fun renderPause() {
        try {
            val renderPaused: Boolean =
                mapController.isRenderPaused(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
            Timber.d(" renderPaused :$renderPaused")
            if (!renderPaused) {
                mapController.renderPause(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                Timber.d(" renderPaused to")
            }
        } catch (e: java.lang.Exception) {
            Timber.e("renderPause e:%s", e.message)
        }
    }

    /**
     * 恢复渲染帧率
     */
    fun renderResume() {
        try {
            val renderPaused: Boolean =
                mapController.isRenderPaused(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
            Timber.d(" renderResume :$renderPaused")
            if (renderPaused) {
                mapController.renderResume(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                Timber.d(" renderResume to")
            }
        } catch (e: java.lang.Exception) {
            Timber.e("renderResume e:%s", e.message)
        }
    }

    //限行详情信息 页面
    private val _restrictInfoDetails = MutableLiveData<GReStrictedAreaDataRuleRes?>()
    val restrictInfoDetails: LiveData<GReStrictedAreaDataRuleRes?> = _restrictInfoDetails

    suspend fun initHomeCardTipsData() {
        Timber.i("initHomeCardTipsData")
        val cardData = arrayListOf<HomeCardTipsData>()
        //恢复上次导航
        if (!continueSapaNavi()) {
            val accOffTime = sharePreferenceFactory.getMapSharePreference(SharePreferenceName.normal).getLongValue(
                MapSharePreference.SharePreferenceKeyEnum.accOffTime, System.currentTimeMillis()
            )
            if (System.currentTimeMillis() - accOffTime >= 24 * 3600 * 1000L) {
                Timber.i("accOffTime to long")
                if (!naviController.isNaving) {
                    //删除存储的导航文件
                    mLastRouteUtils.deleteRouteFile()
                }
            } else {
                Timber.i("CONTINUE_NAVI")
                mLastRouteUtils.getLastRouteCarResultData()?.run {
                    cardData.add(
                        HomeCardTipsData(
                            HomeCardTipsType.CONTINUE_NAVI,
                            "继续导航",
                            this.first.toPOI.name,
                            "导航"
                        )
                    )
                }
            }
        }

        if (!netWorkManager.isNetworkConnected()) {
            val disconnectTime = sharePreferenceFactory.getMapSharePreference(SharePreferenceName.normal).getLongValue(
                MapSharePreference.SharePreferenceKeyEnum.disconnectNetworkTime,
                0
            )
            Timber.i("network disconnect time is ${System.currentTimeMillis() - disconnectTime}")
            if (disconnectTime > 0 && (System.currentTimeMillis() - disconnectTime) >= (45 * 24 * 3600 * 1000L)) {
                cardData.add(
                    HomeCardTipsData(
                        HomeCardTipsType.LONG_TERM_OFFLINE,
                        "长期未连网提醒",
                        "连接网络更新离线地图",
                        "去更新"
                    )
                )
            }
        }

        if (iCarInfoProxy.lowFuelWarning) {
            cardData.add(
                HomeCardTipsData(
                    HomeCardTipsType.LOW_FUEL,
                    "油量提醒",
                    "当前油量过低",
                    "去加油"
                )
            )
        }

        if (netWorkManager.isNetworkConnected()) {
            locationController.lastLocation?.let { location ->
                mapDataBusiness.getCityDownLoadItem(
                    DownLoadMode.DOWNLOAD_MODE_NET,
                    mapDataBusiness.getAdCodeByLonLat(location.longitude, location.latitude)
                )?.run {
                    if (this.bUpdate || this.taskState == TaskStatusCode.TASK_STATUS_CODE_READY) {
                        cardData.add(
                            HomeCardTipsData(
                                HomeCardTipsType.DOWNLOAD_OFFLINE_DATA,
                                "离线数据下载提醒",
                                "当前城市未下载/更新离线数据",
                                "去更新"
                            )
                        )
                    }
                }
            }
        }

        val plateNumber = settingComponent.getLicensePlateNumber()
        //打开限行提示
        if (!settingComponent.getConfigKeyAvoidLimit()) {
            cardData.add(
                HomeCardTipsData(
                    HomeCardTipsType.OPEN_TRAFFIC_RESTRICTION,
                    "开启限行",
                    "建议开启避开限行",
                    "去设置"
                )
            )
        }

        //限行提醒
        if (netWorkManager.isNetworkConnected() && !TextUtils.isEmpty(plateNumber)) {
            val result = mAosBusiness.getStrictedAreaInfo(plateNumber)
            when (result.status) {
                Status.SUCCESS -> {
                    Timber.i("getRestrictedDetail onSuccess")
                    result.data?.let { data ->
                        _restrictInfoDetails.postValue(data)
                        if (!data.cities.isNullOrEmpty()) {
                            cardData.add(
                                HomeCardTipsData(
                                    HomeCardTipsType.TRAFFIC_RESTRICTION,
                                    "限行提醒",
                                    data.cities[0].title,
                                    "查看详情"
                                )
                            )
                        }

                    }
                }

                Status.ERROR -> {
                    Timber.i("getRestrictedDetail ERROR = ${result.throwable.toString()}")
                }

                else -> {
                    Timber.i("getRestrictedDetail else is called")
                }
            }
        }

        _tipsCardList.postValue(cardData)
        Timber.i("initHomeCardTips")
    }

    fun removeHomeCardTipsData(type: HomeCardTipsType? = null) {
        Timber.i("removeHomeCardTipsData type=$type")
        if (type == null) {
            val checkHomeCardTipsByType = checkHomeCardTipsByType(HomeCardTipsType.CONTINUE_NAVI)
            _tipsCardList.postValue(arrayListOf())
            hideCommutingScenariosCard()//通勤卡片也移除
            if (!naviController.isNaving && checkHomeCardTipsByType) {
                //删除存储的导航文件
                mLastRouteUtils.deleteRouteFile()
            }
        } else {
            _tipsCardList.value?.removeIf {
                it.type == type
            }
        }
    }

    fun removeRestrictInfoDetails() {
        Timber.i("removeRestrictInfoDetails is called")
        _restrictInfoDetails.postValue(null)
    }

    private fun checkHomeCardTipsByType(type: HomeCardTipsType): Boolean {
        _tipsCardList.value?.forEach {
            if (it.type == type) {
                return true
            }
        }
        return false
    }

    suspend fun checkAddressPredict() {
        Timber.i("checkAddressPredict")
        if (netWorkManager.isNetworkConnected()) {
            if (settingComponent.getIntentionNavigation()) {
                //快速出发
                mForecastBusiness.requestTravelRecommendBeforeNavi()?.run {
                    addHomeCardTipsData(
                        HomeCardTipsData(
                            HomeCardTipsType.TRAVEL_RECOMMEND_POI,
                            "为你推荐",
                            this.name,
                            "去这里",
                            poi = this
                        )
                    )
                }

                //猜你想去
                val result = mForecastBusiness.getOnlineForecastArrivedData()
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.run {
                            if (this.isNotEmpty()) {
                                addHomeCardTipsData(
                                    HomeCardTipsData(
                                        HomeCardTipsType.FORECAST_POI,
                                        "猜你想去",
                                        this[0].name,
                                        "去这里",
                                        poi = result.data[0]
                                    )
                                )
                            }
                        }
                    }

                    else -> {
                        Timber.i("getOnlineForecastArrivedData $result")
                    }
                }
            }

            //预测家/公司地址
            if (userBusiness.getHomePoi() == null || userBusiness.getCompanyPoi() == null) {
                mForecastBusiness.requestAddressPredict().run {
                    if (userBusiness.getCompanyPoi() == null) {
                        this[BaseConstant.REQ_ADDRESS_LABEL_COMPANY]?.run {
                            addHomeCardTipsData(
                                HomeCardTipsData(
                                    HomeCardTipsType.PREDICT_COMPANY,
                                    "您公司的位置是？",
                                    this.name,
                                    "确定",
                                    poi = this
                                )
                            )
                        }
                    }

                    if (userBusiness.getHomePoi() == null) {
                        this[BaseConstant.REQ_ADDRESS_LABEL_HOME]?.run {
                            addHomeCardTipsData(
                                HomeCardTipsData(
                                    HomeCardTipsType.PREDICT_HOME,
                                    "您家的位置是？",
                                    this.name,
                                    "确定",
                                    poi = this
                                )
                            )
                        }
                    }

                }
            }
        }
    }

    fun addHomeCardTipsData(cardData: HomeCardTipsData) {
        Timber.i("addHomeCardTipsData cardData=$cardData")
        _tipsCardList.value?.removeIf {
            it.type == cardData.type
        }
        _tipsCardList.value?.run {
            if (this.size > 0 && this[0].type == HomeCardTipsType.CONTINUE_NAVI) {
                this.add(1, cardData)
            } else {
                this.add(0, cardData)
            }
            _tipsCardList.postValue(this)
        }
    }

    fun continueSapaNavi(): Boolean {
        return naviSharePreference.getBooleanValue(BaseConstant.KEY_CONTINUE_SAPA_NAVI, false).run {
            Timber.i("continueSapaNavi $this")
            this
        }
    }

    fun clearContinueSapaNavi() {
        Timber.i("clearContinueSapaNavi")
        naviSharePreference.putBooleanValue(BaseConstant.KEY_CONTINUE_SAPA_NAVI, false)
    }

    suspend fun checkCommutingScenariosFlag() {
        Timber.i("checkCommutingScenariosFlag notInHomePage=$notInHomePage")
        if (notInHomePage) {
            checkCommutingScenariosFlag = true
        } else {
            checkCommutingScenarios()
        }
    }

    private var requestingCommutingScenariosRoute = false

    /**
     * 检查通勤信息
     */
    private suspend fun checkCommutingScenarios() {
        if (_commutingScenariosData.value != null) {
            Timber.i("checkCommutingScenarios commutingScenario showing")
            return
        }
        if (!settingComponent.getIntentionNavigation()) {
            Timber.i("checkCommutingScenarios getIntentionNavigation false")
            return
        }
        if (requestingCommutingScenariosRoute) {
            Timber.i("checkCommutingScenarios requestingCommutingScenariosRoute")
        }
        if (!netWorkManager.isNetworkConnected()) {
            Timber.i("checkCommutingScenarios network disconnect")
            return
        }
        val company = userBusiness.getCompanyPoi()
        val home = userBusiness.getHomePoi()
        if (home == null || company == null) {
            Timber.i("checkCommutingScenarios no company or home")
            return
        }
        locationController.lastLocation?.run {
            val distToHome = BizLayerUtil.calcDistanceBetweenPoints(
                Coord2DDouble(this.longitude, this.latitude), Coord2DDouble(
                    home.point.longitude, home
                        .point.latitude
                )
            )

            val distToCompany = BizLayerUtil.calcDistanceBetweenPoints(
                Coord2DDouble(this.longitude, this.latitude), Coord2DDouble(
                    company.point.longitude, company
                        .point.latitude
                )
            )
            Timber.i("checkCommutingScenarios distToHome=$distToHome distToCompany=$distToCompany")
            //已设置家和公司（自车位到家/公司的直线距离1.5≤X≤100km）
            if ((distToHome in 1500.0..100000.0) || (distToCompany in 1500.0..100000.0)) {
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (currentHour in 6..9) {
                    //定位点在家500米范围内，且系统时间为6:00-10:00，去公司场景
                    locationController.lastLocation.run {
                        Timber.i("checkCommutingScenarios distToHome=$distToHome")
                        if (distToHome < 500) {
                            val result = commutingScenariosPlanRoute(
                                1,
                                POIFactory.createPOI("我的位置", GeoPoint(this.longitude, this.latitude)),
                                company
                            )
                            when (result.status) {
                                Status.SUCCESS -> _commutingScenariosData.postValue(result.data)
                                else -> {
                                    _commutingScenariosData.postValue(null)
                                    Timber.i("${result.throwable?.toString()}")
                                }
                            }
                        }
                    }
                } else if (currentHour in 17..23) {
                    //定位点在公司500米范围内，且系统时间为17:00-24:00，回家场景
                    locationController.lastLocation.run {
                        Timber.i("checkCommutingScenarios distToCompany=$distToCompany")
                        if (distToCompany < 500) {
                            val result = commutingScenariosPlanRoute(
                                0,
                                POIFactory.createPOI("我的位置", GeoPoint(this.longitude, this.latitude)),
                                home
                            )
                            when (result.status) {
                                Status.SUCCESS -> {
                                    Timber.d(gson.toJson(result.data))
                                    _commutingScenariosData.postValue(result.data)
                                    requestingCommutingScenariosRoute = false
                                }

                                else -> {
                                    requestingCommutingScenariosRoute = false
                                    _commutingScenariosData.postValue(null)
                                    Timber.i("${result.throwable?.toString()}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun hideCommutingScenariosCard() {
        Timber.i("hideCommutingScenariosCard")
        _commutingScenariosData.postValue(null)
    }

    private suspend fun commutingScenariosPlanRoute(type: Int, startPoi: POI, endPoi: POI):
            Result<CommutingScenariosData> {
        return withContext(Dispatchers.IO) {
            withTimeoutOrNull(10000) {
                suspendCancellableCoroutine { continuation ->
                    requestingCommutingScenariosRoute = true
                    routeRepository.planRoute(startPoi,
                        endPoi, null, object : IRouteResultCallBack {
                            override fun callback(result: IRouteResultData?, isLocal: Boolean) {
                                val data = result as RouteCarResultData
                                data.pathResult?.get(0)?.run {
                                    val title = CommonUtil.switchFromSecond(this.travelTime.toInt())
                                    val content = AutoRouteUtil.getScheduledTime(app, this.travelTime, false)
                                    val mapLightBarItems = this.lightBarItems?.map { lightBarItem ->
                                        MapLightBarItem(
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
                                    } ?: emptyList()
                                    continuation.resume(
                                        Result.success(
                                            CommutingScenariosData(
                                                type,
                                                title,
                                                content,
                                                mapLightBarItems,
                                                this.length
                                            )
                                        )
                                    )
                                }
                            }

                            override fun errorCallback(errorCode: Int, errorMessage: String?, isLocal: Boolean) {
                                continuation.resume(Result.error("errorCallback errorCode = $errorCode errorMessage=$errorMessage"))
                            }

                        })
                }
            } ?: Result.error("time out")
        }
    }

    fun setRestartMainActivity(restart: Boolean) {
        Timber.i("setRestartMainActivity $restart")
        mRestartMainActivity = restart
    }

    val restartMainActivity: Boolean get() = mRestartMainActivity

    fun refreshMapPointCard() {
        _mapPointCard.value?.run {
            if (this.cardType == MapPointCardData.PoiCardType.TYPE_CAR_LOC)
                _mapPointCard.postValue(_mapPointCard.value)
        }
    }

    /** 外部调用方法=====================================================结束============================================================ **/

}