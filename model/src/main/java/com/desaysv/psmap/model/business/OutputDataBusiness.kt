package com.desaysv.psmap.model.business

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.Observer
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.guide.model.NaviInfo
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.common.AutoState
import com.autosdk.common.NaviStateListener
import com.desaysv.psmap.adapter.command.MassageType
import com.desaysv.psmap.base.app.ForegroundCallbacks
import com.desaysv.psmap.base.bean.MapLightBarItem
import com.desaysv.psmap.base.bean.TBTLaneInfoBean
import com.desaysv.psmap.base.bean.TmcModelInfoBean
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.data.NaviRepository
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.model.bean.InputCommonData
import com.desaysv.psmap.model.bean.OutPutPathInfo
import com.desaysv.psmap.model.bean.OutputCommonData
import com.desaysv.psmap.model.bean.OutputNaviPanelData
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import com.desaysv.psmap.model.impl.IMapDataOutputCallback
import com.desaysv.psmap.model.screenshot.CrossMapScreenShotManager
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 对外输出数据业务类
 */
@Singleton
class OutputDataBusiness @Inject constructor(
    private val naviBusiness: NaviBusiness,
    private val naviRepository: NaviRepository,
    private val mapBusiness: MapBusiness,
    @ProvidesHiltModule.DefaultMapCommand private val mapCommand: IMapCommand,
    @ApplicationContext private val context: Context,
    private val settingAccountBusiness: SettingAccountBusiness,
    protected val mNaviRepository: INaviRepository,
    private val cruiseBusiness: CruiseBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val mRouteRequestController: RouteRequestController,
    private val crossMapScreenShotManager: CrossMapScreenShotManager,
    private val iCanInfoProxy: ICarInfoProxy,
    private val gson: Gson,
    private val application: Application
) {
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    private var mCallback: IMapDataOutputCallback? = null

    private val panelInfo: OutputNaviPanelData = OutputNaviPanelData()

    companion object {
        private const val DELAYED_TIME = 30 * 1000
        private const val MSG_CHECK_GAS = 1
    }

    //如果已经发送过低油量提醒
    private var hascheckGas = false

    private var mGasCheckHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_CHECK_GAS -> {
                    checkGas()
                    sendEmptyMessageDelayed(MSG_CHECK_GAS, DELAYED_TIME.toLong())
                }

                else -> {}
            }
        }
    }

    //导航过程信息
    private val naviInfoOb = Observer<NaviInfo?> { naviInfo ->
        naviInfo?.let {
            outputNaviInfo(it)
            outputNaviPanelInfo()
            if (!mGasCheckHandler.hasMessages(MSG_CHECK_GAS)) {
                mGasCheckHandler.sendEmptyMessage(MSG_CHECK_GAS)
            }
        }

    }

    //光柱图信息
    private val tmcModelInfoOb = Observer<TmcModelInfoBean> {
        outputNaviBarInfo(it)
        outputNaviPanelInfo()
    }

    private val exitNumberOb = Observer<String> {
        panelInfo.exitNumber = it
        outputNaviPanelInfo()
    }

    private val exitDirectionInfoOb = Observer<String> {
        panelInfo.exitDirectionInfo = it
        outputNaviPanelInfo()
    }
    private val exitVisibleOb = Observer<Boolean> {
        panelInfo.exitVisible = it
        outputNaviPanelInfo()
    }

    private val naviLaneVisibleOb = Observer<Boolean> {
        panelInfo.naviLaneVisible = it
        outputNaviPanelInfo()
    }
    private val naviLaneInfoOb = Observer<ArrayList<TBTLaneInfoBean>> {
        panelInfo.naviLaneInfo = it
        outputNaviPanelInfo()
    }

    //重算路线
    private val routeRequestingOb = Observer<Boolean> {
        panelInfo.routeRequesting = it
    }

    private val distanceNextRouteOb = Observer<String> {
        panelInfo.distanceNextRoute = it
    }
    private val distanceNextRouteIntOb = Observer<Int> {
        panelInfo.distanceNextRouteInt = it
    }

    private val distanceNextRouteUnitOb = Observer<String> {
        panelInfo.distanceNextRouteUnit = it
    }

    private val nextRouteNameOb = Observer<String> {
        panelInfo.nextRouteName = it
    }
    private val distanceNextCrossOb = Observer<Int> {
        panelInfo.distanceNextCross = it
    }
    private val timeAndDistanceOb = Observer<String> {
        panelInfo.timeAndDistance = it
    }
    private val arriveTimeOb = Observer<String> {
        panelInfo.arriveTime = it
    }
    private val crossViewVisibleOb = Observer<Boolean> {
        panelInfo.crossViewVisible = it
        outputNaviPanelInfo()
        SdkAdapterManager.getInstance()
            .sendCrossMessage(if (it) AutoState.CROSS_MAP_START else AutoState.CROSS_MAP_CLOSE)
        if (!it) {
            outputBitMap(null)
        }
    }
    private val nearThumInfoVisibleOb = Observer<Boolean> {
        panelInfo.nearThumInfoVisible = it
    }
    private val nearRoadNameOb = Observer<String> {
        panelInfo.nearRoadName = it
    }
    private val turnIconIDOb = Observer<Int> {
        panelInfo.turnIconID = it
        outputNaviPanelInfo()
    }
    private val nearThumTurnIconIDOb = Observer<Int> {
        panelInfo.nearThumTurnIconID = it
    }
    private val cityInfoOb = Observer<CityItemInfo?> {
        it?.let {
            panelInfo.cityInfo = it
        }
    }

    private val naviStatusOb = Observer<Int> {
        outputNaviStatus(it)
        if (it != BaseConstant.NAVI_STATE_REAL_NAVING) {
            hascheckGas = false
            mGasCheckHandler.removeMessages(MSG_CHECK_GAS)
        }
    }

    private val mapModeOb = Observer<Int> { mapMode ->
        when (mapMode) {
            MapModeType.VISUALMODE_2D_CAR -> SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CAR_UP_2D)
            MapModeType.VISUALMODE_3D_CAR -> SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CAR_UP_3D)
            MapModeType.VISUALMODE_2D_NORTH -> SdkAdapterManager.getInstance().sendNormalMessage(AutoState.NORTH_UP_2D)
        }
    }

    private val zoomInOb = Observer<Boolean?> {
        it?.let {
            if (it) SdkAdapterManager.getInstance().sendNormalMessage(AutoState.ZOOM_IN) else
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.ZOOM_OUT)
        }
    }

    private val loginStateOb = Observer<Int> { state ->
        when (state) {
            BaseConstant.LOGIN_STATE_SUCCESS -> SdkAdapterManager.getInstance()
                .sendLoginMessage(AutoState.LOGIN_SUCCESS)

            BaseConstant.LOGIN_STATE_GUEST -> SdkAdapterManager.getInstance().sendLoginMessage(AutoState.LOGOUT_SUCCESS)
        }
    }

    private val cruiseStateOb = Observer<Boolean> { state ->
        if (state) {
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CRUISE_START)
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CURRENT_CRUISE)
        } else
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CRUISE_STOP)
    }

    private val dayNightOb = Observer<Boolean> { isNight ->
        SdkAdapterManager.getInstance()
            .sendDayNightMessage(if (isNight) AutoState.AUTO_MODE_NIGHT else AutoState.AUTO_MODE_DAY)
        val data = OutputCommonData(massageType = MassageType.ON_DAY_AND_NIGHT_MODE_STATUS, data = isNight)
        mCallback?.onMapDataToAllPackage(gson.toJson(data))

        if (naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_REAL_NAVING
            || naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_SIM_NAVING
            || naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_PAUSE_SIM_NAVI
        ) {
            outputNaviPanelInfo()
        }
    }

    //应用在前后台判断
    private val foregroundListener = object : ForegroundCallbacks.Listener {
        override fun onBecameForeground() {
            Timber.d("应用在前台 onBecameForeground")
            ioScope.launch {
                outputNaviAPPStatus(BaseConstant.NAVI_APP_STATE_FOREGROUND)
            }
        }

        override fun onBecameBackground() {
            Timber.i("应用在后台 onBecameBackground")
            ioScope.launch {
                outputNaviAPPStatus(BaseConstant.NAVI_APP_STATE_BACKGROUND)
                if (naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_REAL_NAVING
                    || naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_SIM_NAVING
                    || naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_PAUSE_SIM_NAVI
                ) {
                    Timber.i("outputNaviPanelInfo")
                    val data = OutputCommonData(massageType = MassageType.ON_NAVI_PANEL_INFO, data = panelInfo)
                    val json = gson.toJson(data)
                    //Timber.i("outputNaviPanelInfo: $json")
                    mCallback?.onMapDataToAllPackage(json)
                }
            }
        }
    }

    /**
     * 监听一些特殊导航状态
     */
    private val naviStateListener = NaviStateListener { state ->
        when (state) {
            //算路成功/刷新路线
            AutoState.CALCUATE_ROUTE_FINISH_SUCC, AutoState.NAVI_REROUTE_SUCCESS -> {
                mRouteRequestController.carRouteResult?.let {
                    it.pathResult?.get(it.focusIndex)?.let { pathInfo ->
                        val mapLightBarItems = pathInfo.lightBarItems?.map { lightBarItem ->
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
                        OutPutPathInfo(
                            PathID = pathInfo.pathID,
                            Length = pathInfo.length,//获取路线长度
                            Strategy = pathInfo.strategy,//获取路线计算策略
                            TravelTime = pathInfo.travelTime,//获取当前Path旅行时间
                            StaticTravelTime = pathInfo.staticTravelTime,//获取当前Path静态的旅行时间
                            TollCost = pathInfo.tollCost,//获取路线总收费金额
                            TrafficLightCount = pathInfo.trafficLightCount,//获取路线的总红绿灯个数
                            LightBarItems = mapLightBarItems as ArrayList<MapLightBarItem>,//获取路线的光柱信息
                            RestrictionInfo = pathInfo.restrictionInfo,
                            EndPoi = pathInfo.endPoi,//获取终点POI信息
                            ViaPointInfo = pathInfo.viaPointInfo//获取途经点信息
                        ).let { outputPathInfo ->
                            outputNaviPathInfo(outputPathInfo)
                        }
                    }
                }
            }
        }
    }


    suspend fun init() {
        Timber.i("init")
        withContext(Dispatchers.Main) {
            SdkAdapterManager.getInstance().addNaviStatusListener(naviStateListener)
            naviBusiness.naviInfo.unPeek().observeForever(naviInfoOb)
            naviBusiness.tmcModelInfo.unPeek().observeForever(tmcModelInfoOb)
            naviBusiness.exitTip.unPeek().observeForever(exitNumberOb)
            naviBusiness.exitDirectionInfo.unPeek().observeForever(exitDirectionInfoOb)
            naviBusiness.exitDirectionInfoVisible.unPeek().observeForever(exitVisibleOb)
            naviBusiness.naviLaneVisible.unPeek().observeForever(naviLaneVisibleOb)
            naviBusiness.naviLaneList.unPeek().observeForever(naviLaneInfoOb)
            naviBusiness.loadingRoute.unPeek().observeForever(routeRequestingOb)
            naviBusiness.topDistance.unPeek().observeForever(distanceNextRouteOb)
            naviBusiness.topDistanceInt.unPeek().observeForever(distanceNextRouteIntOb)
            naviBusiness.topDistanceUnit.unPeek().observeForever(distanceNextRouteUnitOb)
            naviBusiness.topRoadName.unPeek().observeForever(nextRouteNameOb)
            naviBusiness.crossImageProgress.unPeek().observeForever(distanceNextCrossOb)
            naviBusiness.remainTimeAndDistance.unPeek().observeForever(timeAndDistanceOb)
            naviBusiness.arriveTime.unPeek().observeForever(arriveTimeOb)
            naviBusiness.showCrossView.unPeek().observeForever(crossViewVisibleOb)
            naviBusiness.nearThumInfoVisibility.unPeek().observeForever(nearThumInfoVisibleOb)
            naviBusiness.nearRoadName.unPeek().observeForever(nearRoadNameOb)
            naviBusiness.turnManeuverID.unPeek().observeForever(turnIconIDOb)
            naviBusiness.nearThumTurnManeuverID.unPeek().observeForever(nearThumTurnIconIDOb)
            naviBusiness.cityInfo.unPeek().observeForever(cityInfoOb)
            naviBusiness.naviStatus.observeForever(naviStatusOb)
            mapBusiness.mapMode.observeForever(mapModeOb)
            mapBusiness.zoomIn.observeForever(zoomInOb)
            //监听登录状态
            settingAccountBusiness.loginLoading.observeForever(loginStateOb)
            cruiseBusiness.cruiseStatus.observeForever(cruiseStateOb)
            skyBoxBusiness.themeChange().observeForever(dayNightOb)
            crossMapScreenShotManager.setCrossMapCallback(object : CrossMapScreenShotManager.ICrossMapCallback {
                override fun onMapByteData(byteArray: ByteArray?) {
                    if (panelInfo.crossViewVisible) {
                        outputBitMap(byteArray)
                    }
                }

            })
            crossMapScreenShotManager.init()
            ForegroundCallbacks.getInstance(application).addListener(foregroundListener)
        }
    }

    fun unInit() {
        Timber.i("unInit")
        SdkAdapterManager.getInstance().removeNaviStatusListener(naviStateListener)
        naviBusiness.naviInfo.removeObserver(naviInfoOb)
        naviBusiness.tmcModelInfo.removeObserver(tmcModelInfoOb)
        naviBusiness.exitTip.removeObserver(exitNumberOb)
        naviBusiness.exitDirectionInfo.removeObserver(exitDirectionInfoOb)
        naviBusiness.exitDirectionInfoVisible.removeObserver(exitVisibleOb)
        naviBusiness.naviLaneVisible.removeObserver(naviLaneVisibleOb)
        naviBusiness.naviLaneList.removeObserver(naviLaneInfoOb)
        naviBusiness.loadingRoute.removeObserver(routeRequestingOb)
        naviBusiness.topDistance.removeObserver(distanceNextRouteOb)
        naviBusiness.topDistanceInt.removeObserver(distanceNextRouteIntOb)
        naviBusiness.topDistanceUnit.removeObserver(distanceNextRouteUnitOb)
        naviBusiness.topRoadName.removeObserver(nextRouteNameOb)
        naviBusiness.crossImageProgress.removeObserver(distanceNextCrossOb)
        naviBusiness.remainTimeAndDistance.removeObserver(timeAndDistanceOb)
        naviBusiness.arriveTime.removeObserver(arriveTimeOb)
        naviBusiness.showCrossView.removeObserver(crossViewVisibleOb)
        naviBusiness.nearThumInfoVisibility.removeObserver(nearThumInfoVisibleOb)
        naviBusiness.nearRoadName.removeObserver(nearRoadNameOb)
        naviBusiness.turnManeuverID.removeObserver(turnIconIDOb)
        naviBusiness.nearThumTurnManeuverID.removeObserver(nearThumTurnIconIDOb)
        naviBusiness.cityInfo.removeObserver(cityInfoOb)
        naviBusiness.naviStatus.removeObserver(naviStatusOb)
        mapBusiness.mapMode.removeObserver(mapModeOb)
        mapBusiness.zoomIn.removeObserver(zoomInOb)
        settingAccountBusiness.loginLoading.removeObserver(loginStateOb)
        cruiseBusiness.cruiseStatus.removeObserver(cruiseStateOb)
        skyBoxBusiness.themeChange().removeObserver(dayNightOb)
        crossMapScreenShotManager.unInit()
        ForegroundCallbacks.getInstance(application).removeListener(foregroundListener)
    }

    fun registerMapDataOutputCallback(callback: IMapDataOutputCallback) {
        mCallback = callback
    }

    fun unregisterMapDataOutputCallback(callback: IMapDataOutputCallback) {
        mCallback = null
    }

    private fun outputNaviInfo(info: NaviInfo) {
        ioScope.launch {
            val data = OutputCommonData(massageType = MassageType.ON_NAVIGATING_INFO, data = info)
            val json = gson.toJson(data)
//            Timber.i("outputNaviInfo: $json")
            mCallback?.onMapDataToAllPackage(json)
        }
    }

    private fun outputNaviBarInfo(info: TmcModelInfoBean) {
        val data = OutputCommonData(massageType = MassageType.ON_NAVI_TMC_LIGHT_BAR_INFO, data = info)
        ioScope.launch {
            val json = gson.toJson(data)
            mCallback?.onMapDataToAllPackage(json)
        }
    }

    private fun outputNaviPanelInfo() {
        Timber.i("outputNaviPanelInfo")
        val data = OutputCommonData(massageType = MassageType.ON_NAVI_PANEL_INFO, data = panelInfo)
        ioScope.launch {
            val json = gson.toJson(data)
            //Timber.i("outputNaviPanelInfo: $json")
            mCallback?.onMapDataToAllPackage(json)
        }
    }

    private fun outputNaviPathInfo(info: OutPutPathInfo) {
        val data = OutputCommonData(massageType = MassageType.ON_NAVI_PATH_INFO, data = info)
        ioScope.launch {
            val json = gson.toJson(data)
            Timber.i("outputNaviPathInfo: $json")
            mCallback?.onMapDataToAllPackage(json)
        }
    }

    private fun outputNaviStatus(naviState: Int) {
        Timber.i("outputNaviStatus naviStatus=$naviState")
        when (naviState) {
            BaseConstant.NAVI_STATE_REAL_NAVING -> {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.GUIDE_START)
            }

            BaseConstant.NAVI_STATE_SIM_NAVING -> {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.SIMULATION_START)
            }

            BaseConstant.NAVI_STATE_STOP_REAL_NAVI -> {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.GUIDE_STOP)
            }

            BaseConstant.NAVI_STATE_STOP_SIM_NAVI -> {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.SIMULATION_STOP)
            }

            BaseConstant.NAVI_STATE_PAUSE_SIM_NAVI -> {
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.SIMULATION_PAUSE)
            }
        }
        ioScope.launch {
            val data = OutputCommonData(massageType = MassageType.ON_NAVI_STATUS, data = naviState)
            mCallback?.onMapDataToAllPackage(gson.toJson(data))
        }
    }

    private fun outputNaviAPPStatus(naviAppState: Int) {
        Timber.i("outputNaviAPPStatus naviAppState=$naviAppState")
        val data = OutputCommonData(massageType = MassageType.ON_NAVI_APP_STATUS, data = naviAppState)
        mCallback?.onMapDataToAllPackage(gson.toJson(data))

    }

    private fun outputLowGas() {
        Timber.i("outputLowGas")
        ioScope.launch {
            val data = OutputCommonData(massageType = MassageType.ON_NAVI_LOW_GAS, data = null)
            mCallback?.onMapDataToAllPackage(gson.toJson(data))
        }
    }


    /**
     * todo 带上pkg区分哪个客户端的消息
     */
    fun parseCommandMassage(pkg: String, json: String) {
        Timber.i("parseCommandMassage pkg=$pkg msg=$json")
        ioScope.launch {
            try {
                val msg = gson.fromJson(json, InputCommonData::class.java)
                when (msg.massageType) {
                    MassageType.NAVI_STATUS.name -> {
                        ioScope.launch {
                            val data =
                                OutputCommonData(massageType = MassageType.ON_NAVI_STATUS, data = getNaviStatus())
                            mCallback?.onMapData(pkg, gson.toJson(data))

                            //如果导航状态是导航中，则输出导航面板信息
                            if (naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_REAL_NAVING
                                || naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_SIM_NAVING
                                || naviBusiness.naviStatus.value == BaseConstant.NAVI_STATE_PAUSE_SIM_NAVI
                            ) {
                                Timber.i("outputNaviPanelInfo NAVI_STATUS to output panelInfo")
                                val naviData = OutputCommonData(massageType = MassageType.ON_NAVI_PANEL_INFO, data = panelInfo)
                                val json = gson.toJson(naviData)
                                //Timber.i("outputNaviPanelInfo: $json")
                                mCallback?.onMapDataToAllPackage(json)
                            }
                        }
                    }

                    MassageType.NAVI_APP_STATUS.name -> {
                        ioScope.launch {
                            val data =
                                OutputCommonData(massageType = MassageType.ON_NAVI_APP_STATUS, data = getNaviAppStatus())
                            mCallback?.onMapData(pkg, gson.toJson(data))
                        }
                    }

                    MassageType.NAVI_TO_HOME.name -> {
                        mapCommand.naviToHome()
                    }

                    MassageType.NAVI_TO_WORK.name -> {
                        mapCommand.naviToWork()
                    }

                    MassageType.OPEN_SEARCH_PAGE.name -> {
                        mapCommand.openSearchPage()
                    }

                    MassageType.OPEN_NAVI_PAGE.name -> {
                        mapCommand.openNaviPage()
                    }

                    MassageType.OPEN_FAVORITE_PAGE.name -> {
                        mapCommand.openFavoritePage()
                    }

                    MassageType.OPEN_GROUP_PAGE.name -> {
                        //组队方控事件 data: "0"-默认，"1"-短按按下， "2"-短按释放，"3"-长按按下， "4"-长按释放
                        msg.data?.let { pressType ->
                            when (pressType) {
                                "1" -> mapCommand.openGroupPage(isLongClick = false, isPress = true)
                                "2" -> mapCommand.openGroupPage(isLongClick = false, isPress = false)
                                "3" -> mapCommand.openGroupPage(isLongClick = true, isPress = true)
                                "4" -> mapCommand.openGroupPage(isLongClick = true, isPress = false)
                                else -> Timber.i("parseCommandMassage: unknown map status pressType:$pressType ")
                            }
                        } ?: Timber.i("parseCommandMassage OPEN_GROUP_PAGE: set map status data is null")
                    }

                    MassageType.DAY_AND_NIGHT_MODE_STATUS.name -> {
                        val data = OutputCommonData(massageType = MassageType.ON_DAY_AND_NIGHT_MODE_STATUS, data = NightModeGlobal.isNightMode())
                        mCallback?.onMapDataToAllPackage(gson.toJson(data))
                    }

                    MassageType.OPEN_GAS_STATION_PAGE.name -> {
                        if (mNaviRepository.isNavigating()) {
                            mapCommand.alongRouteSearch("加油站")
                        } else {
                            mapCommand.keywordSearch("加油站")
                        }
                    }

                    MassageType.OPEN_CHARGE_STATION_PAGE.name -> {
                        if (mNaviRepository.isNavigating()) {
                            mapCommand.alongRouteSearch("充电站")
                        } else {
                            mapCommand.keywordSearch("充电站")
                        }
                    }

                    MassageType.SET_THE_MAP_STATUS.name -> {
                        //设置地图状态 data: "0"-回车位，"1"-放大地图， "2"-缩小地图
                        msg.data?.let { type ->
                            when (type) {
                                "0" -> mapBusiness.backCurrentCarPosition()
                                "1" -> mapBusiness.mapZoomIn()
                                "2" -> mapBusiness.mapZoomOut()
                                else -> Timber.i("parseCommandMassage: unknown map status $type")
                            }
                        } ?: run {
                            Timber.i("parseCommandMassage: set map status data is null")
                        }
                    }


                }
            } catch (e: Exception) {
                Timber.e(e, "parseCommandMassage")
            }
        }
    }

    fun getNaviStatus(): Int {
        return naviRepository.getNaviStatus().also {
            Timber.i("getNaviStatus $it")
        }
    }

    fun getNaviAppStatus(): Int {
        return if (ForegroundCallbacks.getInstance(application).isBackground) BaseConstant.NAVI_APP_STATE_BACKGROUND else BaseConstant.NAVI_APP_STATE_FOREGROUND
    }


    private fun checkGas() {
        Timber.i("checkGas naviBusiness.naviInfo.value?.routeRemain?.dist = ${naviBusiness.naviInfo.value?.routeRemain?.dist}, iCanInfoProxy.rangeDist = ${iCanInfoProxy.rangeDist}, hascheckGas = $hascheckGas")
        naviBusiness.naviInfo.value?.routeRemain?.dist?.let { dist ->
            if (dist > (iCanInfoProxy.rangeDist * 1000) && !hascheckGas) {
                outputLowGas()
                hascheckGas = true
            }

        }
    }

    private fun outputBitMap(byteArray: ByteArray?) {

        //暂时只将路口大图的数据发给SystemUI
        val systemUIpkg = "com.android.systemui"

        val jsonObject = JSONObject()
        jsonObject.put("NAVI_CROSS_BITMAP_WIDTH", 528)
        jsonObject.put("NAVI_CROSS_BITMAP_HEIGHT", 350)
        val data = OutputCommonData(massageType = MassageType.ON_NAVI_CROSS_MAP, data = jsonObject.toString())
        ioScope.launch {
            val json = gson.toJson(data)
            outputByteArray("", json, byteArray)
        }
    }

    /**
     * 透出消息给对应包名的客户端
     */
    private fun outputByteArray(pkg: String?, json: String, byteArray: ByteArray?) {
        ioScope.launch {
            mCallback?.onMapByteData(pkg, json, byteArray)
        }
    }
}