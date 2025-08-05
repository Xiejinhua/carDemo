package com.desaysv.psmap.ui.route

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.widget.route.model.NaviStationItemData
import com.autosdk.bussiness.widget.route.model.RoutePathItemContent
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.bean.NaviViaDataBean
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.data.IRouteRepository
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.net.bean.CloudTipType
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import com.desaysv.psmap.utils.RouteSegmentUtil
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description 路线规划viewModel
 */
@HiltViewModel
class RouteViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val mNavigationSettingBusiness: NavigationSettingBusiness,
    private val gson: Gson,
    private val application: Application,
    mSkyBoxBusiness: SkyBoxBusiness,
    private val mRouteRequestController: RouteRequestController,
    private val mMapBusiness: MapBusiness,
    private val routeRepository: IRouteRepository,
    private val iCarInfoProxy: ICarInfoProxy,
    private val netWorkManager: NetWorkManager,
    @ProvidesHiltModule.DefaultMapCommand private val mapCommandImpl: IMapCommand
) : ViewModel() {
    private val UGC_CLOSE_START = 21// 起点所在道路上有避不开的封路事件
    private val UGC_CLOSE_VIA = 22// 途经点所在道路上有避不开的封路事件
    private val UGC_CLOSE_END = 23// 终点所在道路上有避不开的封路事件
    private val UGC_CLOSE = 24 // 非起点/途经点/终点所在道路上有避不开的封路事件
    private val UGC_CLOSE_AVOID = 25// 可以避开的封路事件，已避开
    private val UGC_DESCEND_SORT = 26// 路线干预
    private val UGC_TARGET_DISPATCHER = 27 // 终点干预
    private val K_AVOID_JAM = 41    // 已经避开的拥堵信息
    private val UN_AVOID_FORBIDDEN: Int = 61
    private val AVOID_FORBIDDEN: Int = 62
    private val HOLIDAY_FREE: Int = 81 // 高速路段节假日免费
    private val LANE: Int = 89// 小路提醒
    private val BUSINESS_ARRIVE_REST_SOON: Int = 83// 营业时间，到达时即将休息
    private val BUSINESS_NOT_OPEN: Int = 82// 营业时间，当日不营业

    val setToast: LiveData<String> = mRouteBusiness.setToast
    val focusPathIndex: LiveData<Int> = mRouteBusiness.focusPathIndex
    val isRequestRoute: LiveData<Boolean> = mRouteBusiness.isRequestRoute
    val deleteViaNotice: LiveData<Boolean> = mRouteBusiness.deleteViaNotice
    val routeErrorMessage: LiveData<String> = mRouteBusiness.routeErrorMessage
    val themeChange = mSkyBoxBusiness.themeChange()
    var pathListLiveData: LiveData<ArrayList<RoutePathItemContent>> = mRouteBusiness.pathListLiveData
    var showViaNaviViaDataDialog: LiveData<NaviViaDataBean> = mRouteBusiness.showViaNaviViaDataDialog

//    private var commandRequestRouteNaviBean: CommandRequestRouteNaviBean? = null

    //外部指令
    val mapCommand = mapCommandImpl.getMapCommand()

    //分屏状态监听
    val screenStatus = iCarInfoProxy.getScreenStatus()

    // 0:无路线数据 1:刷新路线数据结果页 2:loading 加载中界面
//    val mDataRefresh = MutableLiveData<Int>(-1)

    //路线偏好名称
    val preferenceName: LiveData<String> = mNavigationSettingBusiness.preferenceName

    //设置完路线偏好通知刷新路线
    val incrementalRouteNotice: LiveData<Boolean> = mNavigationSettingBusiness.incrementalRouteNotice

    //路线偏好设置相关
    val rsDefaultSelected = mNavigationSettingBusiness.rsDefaultSelected
    val rsTmcSelected = mNavigationSettingBusiness.rsTmcSelected
    val rsMoneySelected = mNavigationSettingBusiness.rsMoneySelected
    val rsFreewayNoSelected = mNavigationSettingBusiness.rsFreewayNoSelected
    val rsFreewayYesSelected = mNavigationSettingBusiness.rsFreewayYesSelected
    val rsFreewayQuickSelected = mNavigationSettingBusiness.rsFreewayQuickSelected
    val rsFreewayBigSelected = mNavigationSettingBusiness.rsFreewayBigSelected
    val saveStrategy = mNavigationSettingBusiness.saveStrategy

    private val _isShowPreferenceSetting = MutableLiveData<Boolean>(false)
    val isShowPreferenceSetting: LiveData<Boolean> = _isShowPreferenceSetting

    private val _startSettingCar = MutableLiveData<Boolean>(false)
    val startSettingCar: LiveData<Boolean> = _startSettingCar

    private val _isShowMore = MutableLiveData<Boolean>(false)
    val isShowMore: LiveData<Boolean> = _isShowMore

    private val _btStartNaviTip = MutableLiveData<String>()
    val btStartNaviTip: LiveData<String> = _btStartNaviTip

    val isNetworkConnected = MutableLiveData(netWorkManager.isNetworkConnected()) //是否有网络

    //显示添加途经点扎点
    val routeShowViaPoi = mMapBusiness.routeShowViaPoi

    //云控信息显示隐藏状态
    val hasPriorTipVisibility = mRouteBusiness.hasPriorTipVisibility

    //限行信息
    val restrictInfoLiveData = mRouteBusiness.restrictInfoLiveData

    //封路信息
    val eventCloudControl = mRouteBusiness.eventCloudControl

    //拥堵信息
    val avoidJamCloudControl = mRouteBusiness.avoidJamCloudControl

    //禁行信息
    val forbiddenCloudControl = mRouteBusiness.forbiddenCloudControl

    //节假日信息
    val holidayCloudControl = mRouteBusiness.holidayCloudControl

    //小路信息
    val tipsCloudControl = mRouteBusiness.tipsCloudControl

    //营业时间信息
    val bankingHoursCloudControl = mRouteBusiness.bankingHoursCloudControl

    //云控类型
    val cloudTipType = mRouteBusiness.cloudTipType

    //限行详情信息
    val restrictInfoDetails = mRouteBusiness.restrictInfoDetails

    //避开拥堵 禁行数据
    var avoidTrafficJamsBean = mRouteBusiness.avoidTrafficJamsBean

    //云控信息按钮是否显示
    private val _cloudShowInfoBtVisibility = MutableLiveData(true)
    val cloudShowInfoBtVisibility: LiveData<Boolean> = _cloudShowInfoBtVisibility

    //终点子POI
    val childPois = mRouteBusiness.childPois

    //终点子POI 显示隐藏状态
    val childPoisVisibility = mRouteBusiness.childPoisVisibility

    //沿途天气按钮 显示隐藏状态
    val weatherVisibility = mRouteBusiness.weatherVisibility

    //沿途途径路按钮 显示隐藏状态
    val pathwayVisibility = mRouteBusiness.pathwayVisibility

    //云控信息title
    val cloudShowInfoTip: LiveData<String> = hasPriorTipVisibility.map {
        _cloudShowInfoBtVisibility.postValue(true)
        if (it) {
            when (cloudTipType.value) {
                CloudTipType.NONE -> {
                    _cloudShowInfoBtVisibility.postValue(false)
                    ""
                }
                //限行
                CloudTipType.TYPE_RESTRICT -> restrictInfoLiveData.value?.title ?: ""
                //道路关闭 封路
                CloudTipType.TYPE_ROAD_CLOSE -> getEventCloudTitle()
                //避开拥堵
                CloudTipType.TYPE_AVOID_JAM -> getAvoidJamCloudTitle()
                //禁行
                CloudTipType.TYPE_FORBIDDEN -> getForbiddenCloudTitle()
                //节假日
                CloudTipType.TYPE_HOLIDAY -> getHolidayCloudTitle()
                //小路提醒
                CloudTipType.TYPE_NARROW -> getTipsCloudTitle()
                //营业时间提醒
                CloudTipType.TYPE_BUSINESS_HOURS -> getBankingHoursCloudTitle()
                null -> ""
            }
        } else {
            _cloudShowInfoBtVisibility.postValue(false)
            ""
        }
    }

    private val DEFAULT_BT_TYPE = 2

    //云控信息按钮类型
    val cloudShowInfoBtType: LiveData<Int> = hasPriorTipVisibility.map {
        if (it) {
            when (cloudTipType.value) {
                CloudTipType.NONE -> DEFAULT_BT_TYPE
                CloudTipType.TYPE_RESTRICT -> restrictInfoLiveData.value?.type ?: DEFAULT_BT_TYPE
                CloudTipType.TYPE_ROAD_CLOSE,
                CloudTipType.TYPE_AVOID_JAM,
                CloudTipType.TYPE_FORBIDDEN,
                CloudTipType.TYPE_HOLIDAY,
                CloudTipType.TYPE_NARROW,
                CloudTipType.TYPE_BUSINESS_HOURS -> DEFAULT_BT_TYPE

                null -> DEFAULT_BT_TYPE
            }
        } else {
            DEFAULT_BT_TYPE
        }
    }

    //隐藏云控信息
    fun closeHasPriorTip() = mRouteBusiness.closeHasPriorTip()

    fun setIncrementalRouteNotice() = mNavigationSettingBusiness.setIncrementalRouteNotice()


    //请求路线上的信息- 1:天气- 2:途径路- 3:服务区 4:请求完成 提示框
    val isRequestRouteInfoLoading = mRouteBusiness.isRequestRouteInfoLoading

    //清理服务区扎点
    fun clearRouteRestArea() = mRouteBusiness.clearRouteRestArea()

    //清理天气扎点
    fun clearWeatherOverlay() = mRouteBusiness.clearWeatherOverlay()

    //获取天气
    fun changedRouteWeather() = mRouteBusiness.changedRouteWeather()

    //获取沿途路线服务区
    fun changeAlongWayRestArea() = mRouteBusiness.changeAlongWayRestArea()

    //途径路点击
    fun onClickViaRoad(open: Boolean) = mRouteBusiness.onClickViaRoad(open)

    //单个沿途天气数据
    val weatherLabelItem = mRouteBusiness.weatherLabelItem

    //单个沿途服务区数据
    val alongWayPoiDeepInfo = mRouteBusiness.alongWayPoiDeepInfo

    //是否显示沿途天气
    fun getShowRouteWeather(): Boolean = mRouteBusiness.getShowRouteWeather()

    //是否显示沿途途径路
    fun getShowRoutePathWay(): Boolean = mRouteBusiness.getShowRoutePathWay()

    //是否显示沿途服务区
    fun getShowRouteService(): Boolean = mRouteBusiness.getShowRouteService()

    /**
     * 设置离线路线样式
     */
    fun setSwitchOffline() = mRouteBusiness.setSwitchOffline()

    val closePreferenceSetting = mRouteBusiness.closePreferenceSetting

    val licensePlateChange = iCarInfoProxy.getLicensePlateChange() //通知系统车牌号变化

    //监听网络变化
    private val mNetWorkChangeListener = object : NetWorkManager.NetWorkChangeListener {
        override fun onNetWorkChangeListener(isNetwork: Boolean) {
            Timber.i(" onNetWorkChangeListener is called isNetworkConnected = $isNetwork")
            isNetworkConnected.postValue(isNetwork)
            if (isNetwork) {
                if (mRouteRequestController.carRouteResult.isOffline) {
                    retryPlanRoute(isNetWork = true)
                }
            } else {
                setSwitchOffline()
            }
        }

    }

    init {
        Timber.i("RouteViewModel init")
        netWorkManager.addNetWorkChangeListener(mNetWorkChangeListener)
        mRouteBusiness.init()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("RouteViewModel onCleared")
        mRouteBusiness.unInit(mRouteBusiness.isNotDeleteRoute())
        netWorkManager.removeNetWorkChangeListener(mNetWorkChangeListener)
    }

    //设置导航路线偏好配置
    fun setNavigationData() {
        mNavigationSettingBusiness.getConfigKeyPlanPref() //获取云端的路线偏好配置
    }

    //设置导航路线偏好配置界面是否展示
    fun setPreferenceSetting() {
        if (isShowPreferenceSetting.value?.not() != true) {
            clearAllSelect()
        }
        _isShowPreferenceSetting.postValue(isShowPreferenceSetting.value?.not() ?: false)
        mRouteBusiness.closePreferenceSetting(false)
    }

    fun clearAllSelect() {
        clearRouteRestArea()
        clearWeatherOverlay()
        onClickViaRoad(false)
    }

    //偏好点击事件处理
    fun preferSelect(prefer: String, check: Boolean) {
        clearAllSelect()
        mNavigationSettingBusiness.preferSelect(prefer, check)
    }

    /**
     * 检测偏好设置，并判断是否触发返回重新算路
     */
    fun checkAndSavePrefer() {
        if (mRouteBusiness.isInRouteFragment) {
            //栈顶是路线规划界面才去更新偏好设置按钮状态
            mNavigationSettingBusiness.checkAndSavePrefer()
        }
    }

//    /**
//     * 保存传过来的 起点终点和途经点数据
//     */
//    @Synchronized
//    fun setRouteNaviBean(commandBean: CommandRequestRouteNaviBean?) {
//        if (commandBean != null) {
//            commandRequestRouteNaviBean = commandBean
//        }
//    }
//
//    /**
//     * 获取途经点和终点列表
//     */
//    @Synchronized
//    fun getViaAndEndPois(): ArrayList<POI> {
//        val viaAndEndPois = arrayListOf<POI>()
//        commandRequestRouteNaviBean?.let { commandBean ->
//            commandBean.midPois?.let {
//                viaAndEndPois.addAll(it)
//            }
//            commandBean.end?.let { viaAndEndPois.add(it.apply { poiType = 3 }) }
//        }
//        return viaAndEndPois
//    }
//
//    /**
//     * 获取 起点终点和途经点数据
//     */
//    @Synchronized
//    fun getRouteNaviBean(): CommandRequestRouteNaviBean? {
//        return commandRequestRouteNaviBean
//    }

    /**
     * 获取路线上的途经点
     */
    fun getWaypoints(): ArrayList<POI> {
        val viaList = arrayListOf<POI>()
        val carRouteResult = mRouteRequestController.carRouteResult
        if (carRouteResult?.hasMidPos() == true) {
            viaList.addAll(carRouteResult.midPois)
        }
        return viaList
    }

    /**
     * 规划路线
     */
    fun planRoute(endPoi: POI) {
        viewModelScope.launch {
            val carRouteResult = mRouteRequestController.carRouteResult
            carRouteResult?.let {
                val from = it.fromPOI
                val to = endPoi
                val midPois = it.midPois
                //请求路线
                planRoute(from, to, midPois)
            }
        }
    }

    /**
     * 规划路线
     */
    fun planRoute(
        startPoi: POI,
        endPoi: POI,
        midPois: ArrayList<POI>? = null,
        isNetWork: Boolean = false,
        isRouteRestart: Boolean = false
    ) {
        viewModelScope.launch {
            var requestId = mRouteBusiness.planRoute(startPoi, endPoi, midPois, isNetWork, isRouteRestart)
            Timber.i("RouteViewModel planRoute is called requestId = $requestId ，isNetWork = $isNetWork，isRouteRestart = $isRouteRestart")
        }
    }

    /**
     * 设置路线全览
     */
    fun showPreview() = routeRepository.showPreview()

    /**
     * 添加途经点
     */
    fun addWayPoint(poi: POI?) {
        Timber.i("RouteViewModel addWayPoint is called")
        viewModelScope.launch(Dispatchers.IO) {
            mRouteBusiness.addWayPoint(poi)
        }
    }

    /**
     * 删除途经点
     */
    fun delWayPoint(poi: POI?, index: Int) {
        Timber.i("RouteViewModel delWayPoint is called 111 index = $index")
        viewModelScope.launch {
            poi?.let { mRouteBusiness.deleteViaPoi(it) }
        }
    }

    /**
     * 删除途经点
     */
    fun delWayPoint(index: Int) {
        Timber.i("RouteViewModel delWayPoint is called 222 index = $index")
        viewModelScope.launch {
            mRouteBusiness.deleteViaPoi(index)
        }
    }


    /**
     * 列表点击选择路线
     * routeIndex: 第几条路线
     */
    fun selectPathByIndexOnMap(routeIndex: Int) {
        viewModelScope.launch {
            mRouteBusiness.selectPathByIndexOnMap(routeIndex)
        }
    }

    /**
     * 界面刷新 mDataRefresh = 0:无路线数据 1:刷新路线数据结果页 2:loading 加载中界面
     */
//    fun setRefreshView(isRefresh: Boolean) {
//        if (isRefresh) {
//            mDataRefresh.postValue(2)
//        } else if (pathListLiveData.value.isNullOrEmpty()) {
//            mDataRefresh.postValue(0)
//        } else {
//            mDataRefresh.postValue(1)
//        }
//    }

    /**
     * 重新规划路线
     */
    fun retryPlanRoute(
        isNetWork: Boolean = false,
        isRouteRestart: Boolean = false
    ) {
        Timber.i("retryPlanRoute is called")
        val carRouteResult = mRouteRequestController.carRouteResult ?: return
        val start = carRouteResult.fromPOI
        val end = carRouteResult.toPOI
        val midPois = carRouteResult.midPois
        if (start != null && end != null) {
            planRoute(start, end, midPois, isNetWork, isRouteRestart)
        }
    }

//    /**
//     * 当前规划路线的POI点是否相同
//     */
//    fun isPlanRoute(): Boolean {
//        val carRouteResult = mRouteRequestController.carRouteResult
//        val commandBean = commandRequestRouteNaviBean
//        val startMatches = carRouteResult.fromPOI == commandBean?.start
//        val endMatches = carRouteResult.toPOI == commandBean?.end
//        val midPoisMatches = carRouteResult.midPois == commandBean?.midPois || carRouteResult.midPois == null
//        return startMatches && endMatches && midPoisMatches
//    }

    /**
     * setNotDeleteRoute：notDeleteRoute=true 设置发起导航不删除路线
     */
    fun setNotDeleteRoute(notDeleteRoute: Boolean) = mRouteBusiness.setNotDeleteRoute(notDeleteRoute)

    /**
     * 设置按钮文字
     */
    fun setBtStartNaviTip(tip: String) {
        Timber.i("setBtStartNaviTip $tip")
        _btStartNaviTip.postValue(tip)
    }

    /**
     * 是否打开更多
     */
    fun setShowMore(showMore: Boolean) = _isShowMore.postValue(showMore)

    /**
     * 显示途经点扎点
     */
    fun showAddViaMap(mMapPointCardData: MapPointCardData) = mRouteBusiness.showAddViaMap(mMapPointCardData)

    /**
     * 获取路线详情页各路段信息
     */
    fun getPathNaviStationList(): ArrayList<NaviStationItemData>? {
        val carRouteResult = mRouteRequestController.carRouteResult ?: return null
        val pathInfo = carRouteResult.pathResult.getOrNull(carRouteResult.focusIndex) ?: return null
        val naviStationList = RouteSegmentUtil.getPathNaviStationList(pathInfo, carRouteResult)
        Timber.i("getPathNaviStationList naviStationList = ${gson.toJson(naviStationList)}")
        if (!naviStationList.isNullOrEmpty()) {
            mRouteBusiness.setPathNaviStationList(naviStationList)
        }
        return naviStationList
    }

    /** ---------------------------------------------下面是云控信息相关逻辑---------------------------------------------- **/


    /**
     * 处理云控按钮逻辑
     */
    fun disposeRestrictBt() {
        when (cloudTipType.value) {
            CloudTipType.NONE -> {}
            //限行
            CloudTipType.TYPE_RESTRICT -> {
                Timber.i("RouteViewModel TYPE_RESTRICT is called")
                restrictInfoLiveData.value?.let {
                    val type = it.type
                    //0：设置，1：开启， 其他：查看
                    when (type) {
                        0 -> _startSettingCar.postValue(true)
                        1 -> mRouteBusiness.openAvoidLimit()
                        else -> {
                            mRouteBusiness.getRestrictedDetail()
                        }
                    }
                }
            }
            //道路关闭 封路
            CloudTipType.TYPE_ROAD_CLOSE -> {
                Timber.i("RouteViewModel TYPE_ROAD_CLOSE is called")
                eventCloudControl.value?.let {
                    mRouteBusiness.getRoadCloseEvent(it.detail.eventID, it.pointDetail.pointControl.pos2D)
                }

            }
            //避开拥堵
            CloudTipType.TYPE_AVOID_JAM -> {
                Timber.i("RouteViewModel TYPE_AVOID_JAM is called")
                avoidJamCloudControl.value?.let { mRouteBusiness.onTipViewActionClick(it) }
            }
            //禁行
            CloudTipType.TYPE_FORBIDDEN -> {
                Timber.i("RouteViewModel TYPE_FORBIDDEN is called")
                forbiddenCloudControl.value?.let { mRouteBusiness.onTipViewActionClick(it) }
            }

            CloudTipType.TYPE_HOLIDAY -> {}
            CloudTipType.TYPE_NARROW -> {}
            CloudTipType.TYPE_BUSINESS_HOURS -> {}
            null -> {}

        }

    }


    /**
     * 道路关闭 封路Title
     */
    private fun getEventCloudTitle(): String {
        val eventCloudControl = eventCloudControl.value ?: return ""
        val tipType = eventCloudControl.tipsControl?.tipType?.toLong()

        return when (tipType) {
            UGC_CLOSE_START.toLong() -> application.getString(R.string.sv_route_navi_tip_start_in_closed_road)
            UGC_CLOSE_VIA.toLong() -> application.getString(R.string.sv_route_navi_tip_via_in_closed_road)
            UGC_CLOSE_END.toLong() -> application.getString(R.string.sv_route_navi_tip_end_in_closed_road)
            UGC_CLOSE.toLong() -> application.getString(R.string.sv_route_navi_fmt_tip_close_road, eventCloudControl.strContent)
            UGC_CLOSE_AVOID.toLong() -> application.getString(R.string.sv_route_navi_fmt_tip_avoid_close_road, eventCloudControl.strContent)
            UGC_DESCEND_SORT.toLong(), UGC_TARGET_DISPATCHER.toLong() -> {
                _cloudShowInfoBtVisibility.postValue(false)
                eventCloudControl.strContent
            }

            else -> {
                closeHasPriorTip()
                ""
            }
        }
    }


    /**
     * 避开拥堵 Title
     */
    private fun getAvoidJamCloudTitle(): String {
        val avoidJamCloudControl = avoidJamCloudControl.value ?: return ""
        val tipType = avoidJamCloudControl.tipsControl?.tipType ?: return ""
        var title = ""
        if (tipType == K_AVOID_JAM.toLong()) {
            title = application.getString(
                R.string.sv_route_navi_fmt_tip_avoid_jam,
                avoidJamCloudControl.strJamRoadName,
                avoidJamCloudControl.strJamDist,
                avoidJamCloudControl.strJamTime
            )
        } else {
            closeHasPriorTip()
        }
        return title
    }

    /**
     * 禁行 Title
     */
    private fun getForbiddenCloudTitle(): String {
        val forbiddenCloudControl = forbiddenCloudControl.value ?: return ""
        val tipType = forbiddenCloudControl.tipsControl?.tipType ?: return ""

        return when (tipType) {
            UN_AVOID_FORBIDDEN.toLong() -> application.getString(R.string.sv_route_navi_tip_unavoid_forbidden_road)
            AVOID_FORBIDDEN.toLong() -> application.getString(R.string.sv_route_navi_tip_avoid_forbidden_road)
            else -> {
                closeHasPriorTip()
                ""
            }
        }
    }


    /**
     * 节假日 Title
     */
    private fun getHolidayCloudTitle(): String {
        val holidayCloudControl = holidayCloudControl.value ?: return ""
        val tipType = holidayCloudControl.tipsControl?.tipType ?: return ""

        return when (tipType) {
            HOLIDAY_FREE.toLong() -> {
                _cloudShowInfoBtVisibility.postValue(false)
                application.getString(R.string.sv_route_navi_tip_holiday_free)
            }

            else -> {
                closeHasPriorTip()
                ""
            }
        }
    }


    /**
     * 小路 Title
     */
    private fun getTipsCloudTitle(): String {
        val tipsCloudControl = tipsCloudControl.value ?: return ""
        val tipType = tipsCloudControl.tipType

        return when (tipType) {
            LANE.toLong() -> {
                _cloudShowInfoBtVisibility.postValue(false)
                application.getString(R.string.sv_route_navi_tip_narrow_road)
            }

            else -> {
                closeHasPriorTip()
                ""
            }
        }
    }


    /**
     * 营业时间 Title
     */
    private fun getBankingHoursCloudTitle(): String {
        val bankingHoursCloudControl = bankingHoursCloudControl.value ?: return ""
        val tipType = bankingHoursCloudControl.tipsControl?.tipType ?: return ""
        _cloudShowInfoBtVisibility.postValue(false)
        return when (tipType) {
            BUSINESS_ARRIVE_REST_SOON.toLong() -> {
                application.getString(R.string.sv_route_navi_fmt_tip_business_arrive_rest_soon, bankingHoursCloudControl.strTime)
            }

            BUSINESS_NOT_OPEN.toLong() -> {
                application.getString(R.string.sv_route_navi_fmt_tip_business_not_open, bankingHoursCloudControl.strTime)
            }

            else -> {
                closeHasPriorTip()
                ""
            }
        }
    }

    fun notifyVoiceCommandResult(commandType: MapCommandType, result: Boolean, tips: String) {
        Timber.i("notifyPageRankCommandResult commandType=$commandType result = $result,tips = $tips")
        mapCommandImpl.notifyMapCommandResult(commandType, Pair(result, tips))
    }

}