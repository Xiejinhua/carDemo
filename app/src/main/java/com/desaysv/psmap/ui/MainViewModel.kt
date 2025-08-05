package com.desaysv.psmap.ui

import android.Manifest
import android.app.Application
import android.content.res.Configuration
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.guide.model.NaviType
import com.autonavi.gbl.user.msgpush.model.AimPoiPushMsg
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autosdk.BuildConfig
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.widget.route.model.RoutePathItemContent
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.autosdk.common.utils.DayStatusSystemUtil
import com.desaysv.psmap.base.bean.HomeCardTipsData
import com.desaysv.psmap.base.business.ActivationMapBusiness
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.ForecastBusiness
import com.desaysv.psmap.base.business.GroupObserverBusiness
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.PermissionReqController
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.LinkCarBusiness
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val initSDKBusiness: InitSDKBusiness,
    private val mapBusiness: MapBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val activationMapBusiness: ActivationMapBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val permissionReqController: PermissionReqController,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val userGroupBusiness: UserGroupBusiness,
    private val groupObserverBusiness: GroupObserverBusiness,
    private val pushMessageBusiness: PushMessageBusiness,
    private val cruiseBusiness: CruiseBusiness,
    private val linkCarBusiness: LinkCarBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val iCarInfoProxy: ICarInfoProxy,
    private val settingComponent: ISettingComponent,
    private val app: Application,
    @ProvidesHiltModule.DefaultMapCommand private val defaultMapCommand: IMapCommand,
    private val forecastBusiness: ForecastBusiness,
    private val userBusiness: UserBusiness,
    private val customTeamBusiness: CustomTeamBusiness
) : ViewModel() {
    val isShowActivateLayout = activationMapBusiness.isShowActivateLayout
    val isShowAgreementLayout = activationMapBusiness.isShowAgreementLayout
    val showActivateAgreement = activationMapBusiness.showActivateAgreement //在激活或者地图提示界面
    val themeChange = skyBoxBusiness.themeChange()

    val initResult = initSDKBusiness.getInitResult()

    val showRequestPermissionRationale = permissionReqController.showRequestPermissionRationale
    val notifyPoiPushMessage = linkCarBusiness.notifyPoiPushMessage //POI消息接收
    val notifyRoutePushMessage = linkCarBusiness.notifyRoutePushMessage //路线消息推送接收
    val linkCarStartNavi: LiveData<POI?> = linkCarBusiness.startNavi //开始导航
    val linkCarExitNavi: LiveData<Boolean> = linkCarBusiness.exitNavi //结束导航
    val linkRoutePushMessage = linkCarBusiness.linkRoutePushMessage //手车互联路线通知

    val glMapSurface by lazy {
        mapBusiness.glMapSurface
    }

    //监听登录状态
    val loginStatus: LiveData<Int> = settingAccountBusiness.loginLoading

    val zoomInEnable = mapBusiness.zoomInEnable
    val zoomOutEnable = mapBusiness.zoomOutEnable
    val backCCPVisible = mapBusiness.backCCPVisible
    val mapMode = mapBusiness.mapMode
    val scaleLineLength = mapBusiness.scaleLineLength
    val zoomLevel = mapBusiness.zoomLevel
    val gpsState = mapBusiness.gpsState
    val showMapControlButtons = mapBusiness.showMapControlButtons
    val showPushCardView = customTeamBusiness.showPushCardView //显示邀请组队弹条
    val teamHasQuitOrDismiss = userGroupBusiness.teamHasQuitOrDismiss //队伍状态，0正常，1无效，2解散，3移除或退出，4队伍信息变化

    val mapCommand = defaultMapCommand.getMapCommand()

    val showCruise = cruiseBusiness.cruiseStatus
    val showCruiseLane = cruiseBusiness.showCruiseLane
    val screenStatus = iCarInfoProxy.getScreenStatus()
    val launcherStatus = iCarInfoProxy.getLauncherStatus()
    val phoneLinkNaviStopLocalNavi = iCarInfoProxy.getPhoneLinkNaviStopLocalNavi() //通知结束本地导航

    val isMute: LiveData<Boolean> = navigationSettingBusiness.volumeMute.map { value ->
        value == 1
    }

    val groupToast = groupObserverBusiness.setToast
    val isPushMessageLiveData = groupObserverBusiness.isPushMessageLiveData //组队消息弹条
    val pushMessageCloseView = pushMessageBusiness.closeView

    //获取高速服务区信息显示隐藏 view
    val showGetSAPAInfoDialogVisible = mNaviBusiness.showGetSAPAInfoDialogVisible

    //摄像头数据
    val naviCameraList = mNaviBusiness.naviCameraList

    val mapPointCard = mapBusiness.mapPointCard
    val pointDetail = mapBusiness.pointDetail

    //是否请求路线中
    val isRequestRoute: LiveData<Boolean> = mRouteBusiness.isRequestRoute
    var pathListLiveData: LiveData<ArrayList<RoutePathItemContent>> = mRouteBusiness.pathListLiveData
    val routeErrorMessage: LiveData<String> = mRouteBusiness.routeErrorMessage


    //导航中 路线Loading提示图标
    val loadingView = mNaviBusiness.loadingView
    val naviErrorMessage: LiveData<String> = mNaviBusiness.naviErrorMessage

    //红绿灯绿波车速数据显示隐藏 View
    val naviGreenWaveCarSpeedVisible = mNaviBusiness.naviGreenWaveCarSpeedVisible

    //取消路线规划
    fun abortRequestTaskId() = mNaviBusiness.abortRequestTaskId()
    fun initNaviScope() = mNaviBusiness.initNaviScope()
    fun cancelNaviScope() = mNaviBusiness.cancelNaviScope()
    fun recoverViaPois() = mNaviBusiness.recoverViaPois()
    fun setNaviType() = mNaviBusiness.setNaviType(NaviType.NaviTypeGPS)

    /**
     * 当前车辆速度
     */
    fun getCurSpeed(): Int = mNaviBusiness.getCurSpeed().toInt()

    val openUserFavorite = userBusiness.openUserFavorite //打开收藏夹
    val backToMap = mapBusiness.backToMap //回到地图主图
    val backToNavi = mapBusiness.backToNavi //回到地图主图
    val backToMapHome = mapBusiness.backToMapHome //回到地图主图--在导航/模拟导航需要退出

    val isJetOurGaoJie = iCarInfoProxy.isJetOurGaoJie()

    val callDuration = customTeamBusiness.callDuration //通话时长
    val showFloatingView = customTeamBusiness.showFloatingView //通话弹窗
    val isJoinCall = customTeamBusiness.isJoinCall //是否加入对讲
    val isAllForbidden = customTeamBusiness.isAllForbidden //队伍禁言
    val isMineForbidden = customTeamBusiness.isMineForbidden //自己禁言
    val isLeader = customTeamBusiness.isLeader //队长
    val teamInfo = customTeamBusiness.teamInfo //组队信息
    val joinTeamResult = customTeamBusiness.joinTeamResult //加入队伍结果

    //驾车导航过程 统计信息
    val naviStatisticsInfo = mNaviBusiness.naviStatisticsInfo

    val surfaceViewRenderComplete = mapBusiness.surfaceViewRenderComplete

    val iovLicenseOK = initSDKBusiness.iovLicenseOK

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
    }

    fun setInHomeFragment(isHomeFragment: Boolean) {
        mapBusiness.setIsInHomePage(isHomeFragment)
        userGroupBusiness.setInHomeFragment(isHomeFragment)
        AutoStatusAdapter.sendStatus(if (isHomeFragment) AutoStatus.HOME_FRAGMENT else AutoStatus.OTHERS_FRAGMENT)
    }

    fun setIsInPOICardPage(isInPOICardPage: Boolean) {
        mapBusiness.setIsInPOICardPage(isInPOICardPage)
    }

    fun zoomIn() {
        mapBusiness.mapZoomIn()
    }

    fun zoomOut() {
        mapBusiness.mapZoomOut()
    }

    fun switchMapViewMode(): Int {
        mapBusiness.switchMapViewMode()
        return BaseConstant.currentMapviewMode
    }

    fun onlyBackToCarPosition() {
        mapBusiness.backCurrentCarPosition(false)
    }

    fun backToCar() {
        mapBusiness.backCurrentCarPosition()
    }

    fun getScaleLineLengthDesc(): String {
        return mapBusiness.getScaleLineLengthDesc()
    }

    fun getScaleLineLength(): Int {
        return mapBusiness.getScaleLineLength()
    }

    fun accountLoginStatusChange() {
        viewModelScope.launch {
            mapBusiness.accountStatusChange()
            if (!com.desaysv.psmap.base.BuildConfig.dayNightBySystemUI)
                skyBoxBusiness.refreshDayNightStatus()
        }
    }

    fun setScreenStatus(isScreenStatus: Boolean) = iCarInfoProxy.setScreenStatus(isScreenStatus)
    fun setMapCenterNavi(screenStatus: Boolean) = mapBusiness.setMapCenterNavi(screenStatus)

    fun isInitSuccess(): Boolean {
        return initSDKBusiness.isInitSuccess()
    }

    fun initMap() {
        viewModelScope.launch {
            initSDKBusiness.initMap()
        }
    }

    fun initMapSuccessfullyTask() {
        Timber.i("initMapSuccessfullyTask")
        navigationSettingBusiness.getShowCarCompass()
        navigationSettingBusiness.initCloseCruiseBroadcast()//预留接口，后续正式项目考虑是否使用
        navigationSettingBusiness.getConfigKeyMute()
        viewModelScope.launch {
            mapBusiness.checkCommutingScenariosFlag()
            mapBusiness.updateHomeFavorites()
            mapBusiness.initHomeCardTipsData()
        }
    }

    fun setInRouteFragment(isInRouteFragment: Boolean) {
        mRouteBusiness.isInRouteFragment = isInRouteFragment
        mapBusiness.isInRouteFragment = isInRouteFragment
        Timber.i("ccpTimer isInRouteFragment = $isInRouteFragment ,isRouteInit =  ${mapBusiness.isRouteInit}")
    }

    fun setInAddHomeFragment(isInAddHomeFragment: Boolean) {
        mapBusiness.isInAddHomeFragment = isInAddHomeFragment
        Timber.i("ccpTimer isInAddHomeFragment = $isInAddHomeFragment")
    }

    fun setInNaviFragment(isInNaviFragment: Boolean) {
        mNaviBusiness.isInNaviFragment = isInNaviFragment
        mapBusiness.isInNaviFragment = isInNaviFragment
        if (isInNaviFragment)
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.CURRENT_NAVI)
    }

    val showActivateAgreementOrMapButton = MediatorLiveData<Pair<Boolean, Boolean>>().apply {
        addSource(isShowActivateLayout) { isShowActivate ->
            val isShowAgreement = isShowAgreementLayout.value ?: false
            value = Pair(isShowActivate, isShowAgreement)
        }
        addSource(isShowAgreementLayout) { isShowAgreement ->
            val isShowActivate = isShowActivateLayout.value ?: false
            value = Pair(isShowActivate, isShowAgreement)
        }
    }

    fun updateAllSkinView(view: View, isNight: Boolean) {
        skyBoxBusiness.updateView(view, isNight, true)
    }

    fun requestPermissions(activity: MainActivity) {
        if (requiredPermissionsIsOk()) {
            viewModelScope.launch {
                Timber.i("requiredPermissionsIsOk")
                if (!initSDKBusiness.isInitSuccess()) {
                    initSDKBusiness.initMap()
                }
            }
            return
        }
        Timber.i("need to requestPermissions")
        permissionReqController.requestPermissions(activity)
    }

    fun requiredPermissionsIsOk(): Boolean {
        return permissionReqController.requiredPermissionsIsOk()
    }

    fun clearPermissions() {
        permissionReqController.clearPermissions()
    }

    fun isPermissionRequesting(): Boolean {
        return permissionReqController.isPermissionRequesting()
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionReqController.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //结束导航
    fun exitNavi() {
        Timber.i("exitNavi()")
        mNaviBusiness.stopNavi()
    }

    /**
     * 是否send2car poi消息
     * @param aimPoiPushMsg
     * @return
     */
    fun isSend2CarPoiMsg(aimPoiPushMsg: AimPoiPushMsg?): Boolean {
        return pushMessageBusiness.isSend2CarPoiMsg(aimPoiPushMsg)
    }

    fun isSend2CaRouteMsg(aimRoutePushMsg: AimRoutePushMsg?): Boolean {
        return pushMessageBusiness.isSend2CaRouteMsg(aimRoutePushMsg)
    }

    fun showPoiCard(poi: POI) {
        viewModelScope.launch {
            delay(50)
            Timber.i("showPoiCard")
            mapBusiness.showPoiCard(poi)
        }
    }

    fun showPoiDetail(poi: POI) {
        viewModelScope.launch {
            Timber.i("showPoiDetail")
            mapBusiness.showPoiDetail(poi)
        }
    }

    fun onHiddenChanged(hasFocus: Boolean) {
        if (initSDKBusiness.isInitSuccess())
            userGroupBusiness.onHiddenChanged(hasFocus)
    }

    fun doTeamMessageListener() {
        userGroupBusiness.doTeamMessageListener()
    }

    /**
     * 是否组队加入队伍
     */
    fun getTeamUserStatus(name: String) {
        userGroupBusiness.getTeamUserStatus(name)
    }

    /**
     * 是否进入组队界面
     */
    fun setInGroupModule(isInGroupModule: Boolean) {
        userGroupBusiness.setInGroupModule(isInGroupModule)
    }

    //上报自己位置
    fun doStartGroupPosition(delayDefault: Boolean = true) {
        userGroupBusiness.doStartGroupPosition(delayDefault)
    }

    fun setPlayTTsMute(isMute: Boolean) {
        Timber.i("setPlayTTsMute $isMute")
        navigationSettingBusiness.setConfigKeyMute(if (isMute) 1 else 0, true)
    }

    //防止重复触发
    private var mIsLoginFlag: Boolean? = null

    fun isLoginSuccessfully(isLogin: Boolean) {
        Timber.i("isLoginSuccessfully isLogin=$isLogin mIsLoginFlag=$mIsLoginFlag ")
        if (isLogin) {
            if (mIsLoginFlag != true) {
                mIsLoginFlag = true
                forecastBusiness.setLogin()
                viewModelScope.launch {
                    mapBusiness.checkCommutingScenariosFlag()
                    mapBusiness.checkAddressPredict()
                }
            }
        } else {
            if (mIsLoginFlag != false) {
                mIsLoginFlag = false
                viewModelScope.launch {
                    mapBusiness.hideCommutingScenariosCard()
                }
            }

        }

    }

    /**
     * 隐藏SAPA请求提示
     */
    fun setShowGetSAPAInfoDialogVisible(isShow: Boolean) = mNaviBusiness.setShowGetSAPAInfoDialogVisible(isShow)

    fun addHomeCardTipsData(cardData: HomeCardTipsData) {
        mapBusiness.addHomeCardTipsData(cardData)
    }

    /**
     * 是否为模拟导航
     */
    fun isSimulationNavi(): Boolean {
        return mNaviBusiness.isSimulationNavi()
    }

    fun isNavigating(): Boolean {
        return mNaviBusiness.isNavigating()
    }

    fun isRealNavi(): Boolean {
        return mNaviBusiness.isRealNavi()
    }

    fun stopNavi() {
        mNaviBusiness.stopNavi()
    }

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        //路线规划监听初始化
        mRouteBusiness.outsideInit()
        viewModelScope.launch {
            if (isNavigating()) {
                mNaviBusiness.stopNavi()
                delay(500)
            }
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }

    //路线规划监听反初始化
    fun outsideUnInit() = mRouteBusiness.outsideUnInit()
    fun isPlanRouteing() = mRouteBusiness.isPlanRouteing()

    fun abortRouteRequestTaskId() = mRouteBusiness.abortRequestTaskId()
    fun recoverRouteViaPois() = mRouteBusiness.recoverViaPois()

    /**
     * 发起导航
     */
    fun startNavi(commandBean: CommandRequestRouteNaviBean?) {
        Timber.i("startNavi is called")
        viewModelScope.launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mNaviBusiness.setParkingRecommendVisible(false)
                mNaviBusiness.planRoute(start, end, midPois, BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD)
            }
        }
    }

    /**
     * setNotDeleteRoute：notDeleteRoute=true 设置发起导航不删除路线
     */
    fun setNotDeleteRoute(notDeleteRoute: Boolean) = mRouteBusiness.setNotDeleteRoute(notDeleteRoute)

    /**
     * 发起导航
     */
    fun pushMsgStartNavi(commandBean: CommandRequestRouteNaviBean?) {
        Timber.i("pushMsgStartNavi is called")
        viewModelScope.launch {
            val pushMsg = commandBean?.aimRoutePushMsg
            mNaviBusiness.setParkingRecommendVisible(false)
            mNaviBusiness.planAimRoutePushMsgRoute(pushMsg, BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD)
        }
    }

    fun clearSomeCardTips() {
        viewModelScope.launch {
            Timber.i("clearSomeCardTips")
            mapBusiness.hideCommutingScenariosCard()
            mapBusiness.removeHomeCardTipsData()
        }

    }

    val restartMainActivity: Boolean get() = mapBusiness.restartMainActivity

    fun setRestartMainActivity(restart: Boolean) {
        mapBusiness.setRestartMainActivity(restart)
    }

    fun getFirstTimeNightMode(): Boolean {
        var isNightMode = false
        if (BuildConfig.dayNightBySystemUI) {
            isNightMode = getDayNightMode() == Configuration.UI_MODE_NIGHT_YES
        } else {
            when (settingComponent.getConfigKeyDayNightMode()) {
                SettingConst.MODE_DEFAULT -> isNightMode =
                    DayStatusSystemUtil.getInstance().firstTimeNightMode()

                SettingConst.MODE_DAY -> isNightMode = false
                SettingConst.MODE_NIGHT -> isNightMode = true
                else -> {}
            }
        }
        Timber.i("getFirstTimeNightMode isNightMode = %s", isNightMode)
        NightModeGlobal.setNightMode(isNightMode)
        return isNightMode
    }

    private fun getDayNightMode(): Int {
        if (BuildConfig.dayNightBySystemUI) {
            val currentNightMode: Int = app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            Timber.i("onConfigurationChanged currentNightMode = %s", currentNightMode)
            return currentNightMode
        }
        return Configuration.UI_MODE_NIGHT_NO
    }

    fun confirmInNavi(inSearchResultPage: Boolean) {
        Timber.i("confirmInNavi inSearchResultPage=$inSearchResultPage")
        //只处理回导航页面的情况,其他页面各自在页面监听处理
        if (mNaviBusiness.isRealNavi() && !inSearchResultPage) {
            mNaviBusiness.naviBackCurrentCarPosition()
            mapBusiness.backToNavi.postValue(true) //回到导航页面
            defaultMapCommand.notifyMapCommandResult(MapCommandType.Confirm, "好的，已为您继续导航")
        }
    }

    fun notifyModifyHomeCompanyAddress(data: String) {
        Timber.i("notifyModifyHomeCompanyAddress data=$data")
        defaultMapCommand.notifyMapCommandResult(MapCommandType.OpenModifyHomeCompanyAddressPage, "${data}在哪里？")
    }

    //保存巡航景点推荐开关
    fun setAhaScenicBroadcastSwitch(value: Boolean) {
        navigationSettingBusiness.setAhaScenicBroadcastSwitch(value)
    }

    fun tvJoinCall() {
        customTeamBusiness.tvJoinCall()
    }

    fun locationPermissionUse(isUsed: Boolean) {
        viewModelScope.launch {
            permissionReqController.notificationPermissionUse(app, Manifest.permission.ACCESS_FINE_LOCATION, isUsed)
        }
    }

    fun startRoute(poi: POI) {
        Timber.i("startRoute poi name:${poi.name} longitude:${poi.point.longitude} latitude:${poi.point.latitude}")
        defaultMapCommand.startPlanRoute(
            poi.name,
            poi.point.longitude,
            poi.point.latitude
        )
    }

    fun checkResetBackCppTimer() {
        if (mapBusiness.isInPOICardFragment()) {
            Timber.d("resetBackToCarTimer")
            mapBusiness.resetBackToCarTimer()
        }
    }

    /**
     * 尝试使用给定的组队口令加入队伍。
     * 此方法调用 [CustomTeamBusiness] 中的 `joinTeam` 方法，
     * 将传入的组队口令传递给业务层进行实际的加入队伍操作。
     *
     * @param code 用于加入队伍的组队口令。
     */
    fun joinTeam(code: String) {
        customTeamBusiness.joinTeam(code)
    }

    fun checkIOVLicenseIsOK() = initSDKBusiness.checkIOVLicenseIsOK()
}