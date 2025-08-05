package com.desaysv.psmap.model.car.dashboard

import android.content.Context
import com.autonavi.gbl.guide.model.LightInfo
import com.autonavi.gbl.guide.model.TrafficLightCountdown
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.desaysv.ivi.vdb.event.id.cabin.VDEventCabinLan
import com.desaysv.ivi.vdb.event.id.cabin.bean.VDCLCommonMessage
import com.desaysv.ivi.vdb.event.id.carlan.bean.VDNaviDigitalInfo
import com.desaysv.ivi.vdb.event.id.carlan.bean.VDNaviDisplayArea
import com.desaysv.ivi.vdb.event.id.carlan.bean.VDNaviLaneInfo
import com.desaysv.ivi.vdb.event.id.carlan.bean.VDNaviRoadInfo
import com.desaysv.psmap.base.business.ExtMapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.impl.CarDashboardStatus
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.VehicleInfoCallback
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.model.utils.OutputLaneInfo
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 仪表业务
 */
@Singleton
class CarDashboardBusiness @Inject constructor(
    @ApplicationContext private val context: Context,
    private val extMapBusiness: ExtMapBusiness,
    private val iCarInfoProxy: ICarInfoProxy,
    private val naviBusiness: NaviBusiness,
    private val gson: Gson,
) {

    //路线信息
    private val naviTotalInfo = VDNaviTotalInfoT1N().apply {
        DistanceUint = 0x01
        RemDistanceUint = 0x01
    }

    //TBT信息
    private val naviRoadInfo = VDNaviRoadInfo()

    //车道信息
    private val naviLaneInfo = VDNaviLaneInfo().apply {
        roadInfo = ArrayList()
    }

    //限速信息和导航状态
    private val naviDigitalInfo = VDNaviDigitalInfo()

    private var lightCountdownInfo: List<TrafficLightCountdown>? = null

    private var jobLoopRefreshTrafficLightInfo: Job? = null

    private var dashboardCallback: IDashboardCallback? = null

    private var dashboardReady = false

    private var lastCarDashboardTheme = 0

    fun init() {
        Timber.i("init")
        iCarInfoProxy.registerVehicleInfoCallback(vehicleInfoCallback)
        naviBusiness.naviStatus.unPeek().observeForever { naviStatus ->
            when (naviStatus) {
                BaseConstant.NAVI_STATE_REAL_NAVING, BaseConstant.NAVI_STATE_SIM_NAVING -> {
                    if (lastCarDashboardTheme == 0) {
                        iCarInfoProxy.getDashboardTheme().let {
                            if (it != BaseConstant.CARDASHBOARD_THEME_NAVI)
                                lastCarDashboardTheme = iCarInfoProxy.getDashboardTheme()
                        }
                    }
                    showMapViewToDashboard(true)
                    sendNaviStatus(true)
                }

                BaseConstant.NAVI_STATE_INIT_NAVI_STOP, BaseConstant.NAVI_STATE_STOP_REAL_NAVI, BaseConstant
                    .NAVI_STATE_STOP_SIM_NAVI -> {
                    sendNaviStatus(false)
                    lightCountdownInfo = null
                    jobLoopRefreshTrafficLightInfo?.cancel()
                    refreshTrafficLightInfo()
                }
            }
        }

        naviBusiness.naviInfo.unPeek().observeForever { naviInfo ->
            naviInfo?.run {
                naviTotalInfo.TotalDistance = naviBusiness.getTotalDistance().toInt()
                naviTotalInfo.RemDistance = naviInfo.routeRemain.dist
                naviTotalInfo.TimeLeft = (naviInfo.routeRemain.time + 30) / 60
                val times = AutoRouteUtil.getScheduledTimeArr(context, naviInfo.routeRemain.time.toLong(), false);
                naviTotalInfo.ArrivalTime = times[2]
                naviTotalInfo.Week = times[0]

                naviRoadInfo.apply {
                    roadType = naviInfo.curRoadClass
                    roadName = naviInfo.curRouteName
                    naviInfo.NaviInfoData.getOrNull(naviInfo.NaviInfoFlag)?.let {
                        roadIcon = it.maneuverID
                        segRemainDis = it.segmentRemain.dist
                        nextRoadName = it.nextRouteName
                    }
                }
                sendNaviTotalInfo()
                sendNaviRoadInfo()
                refreshTrafficLightInfo(naviInfo.curSegIdx, naviInfo.curLinkIdx)
            }
        }

        naviBusiness.naviLaneList.observeForever { laneList ->
            if (laneList.isEmpty()) {
                naviLaneInfo.roadInfo.clear()
            } else {
                naviLaneInfo.roadInfo.clear()
                for (i in 0 until laneList.size) {
                    naviLaneInfo.roadInfo.add(VDNaviLaneInfo.LaneInfo().apply {
                        laneInfo = if (laneList[i].isRecommend) 1 else 0 //1高亮，0不高亮
                        laneId = i
                        laneIconId = OutputLaneInfo.getLaneKanZiID(laneList[i].laneAction)
                    })
                }
            }
            sendLaneInfo()
        }

        iCarInfoProxy.getDashboardTheme().let {
            if (it != BaseConstant.CARDASHBOARD_THEME_NAVI)
                lastCarDashboardTheme = iCarInfoProxy.getDashboardTheme()
        }

        naviBusiness.trafficLightCountdownInfo.observeForever { lightCountdownInfo ->
            Timber.i("lightCountdownInfo ${gson.toJson(lightCountdownInfo)}")
            if (lightCountdownInfo.isNullOrEmpty()) {
                this@CarDashboardBusiness.lightCountdownInfo = null
                refreshTrafficLightInfo()
            } else {
                if (this@CarDashboardBusiness.lightCountdownInfo == null) {
                    jobLoopRefreshTrafficLightInfo?.cancel()
                    jobLoopRefreshTrafficLightInfo = MainScope().launch { loopRefreshTrafficLightInfo() }
                }
                this@CarDashboardBusiness.lightCountdownInfo = lightCountdownInfo
                refreshTrafficLightInfo()
            }

        }

        naviBusiness.showCrossView.observeForever { show ->
            Timber.i("showCrossView $show")
            naviRoadInfo.intersectionZoomStatus = if (show) 1 else 0
            sendNaviRoadInfo()
        }

    }

    fun unInit() {
        Timber.i("unInit")
        iCarInfoProxy.unregisterVehicleInfoCallback(vehicleInfoCallback)
    }

    fun dashboardReady(): Boolean {
        return dashboardReady
    }

    fun initMapInfo() {
        Timber.i("initMapInfo")
        dashboardReady = true
    }

    fun getDashboardStatus(): CarDashboardStatus {
        return iCarInfoProxy.carDashboardStatus
    }

    fun dashboardDisplayStatusListener(callback: IDashboardCallback) {
        dashboardCallback = callback
    }

    fun naviDisplayLoading(loading: Boolean) {
        Timber.i("naviDisplayLoading loading=$loading")
        val event = VDCLCommonMessage.createEvent(
            VDEventCabinLan.CABIN_LAN_MSG_COMMON,
            VDCLCommonMessage(9, gson.toJson(NaviDisplayLoading(if (loading) 1 else 0))).apply {
                msgType = 0x1004
            })
        iCarInfoProxy.sendMessageToDashboard(event)
    }

    /**
     * 默认发true，地图异常时候才发false
     */
    fun showMapViewToDashboard(show: Boolean) {
        Timber.i("showMapViewToDashboard show=$show")
        val data = VDNaviDisplayClusterT1N().apply {
            DisplayCluster = if (show) "true" else "false"
            NaviFrontDeskStatus = "true"
        }
        Timber.i("showMapViewToDashboard data=${gson.toJson(data)}")
        val event = VDCLCommonMessage.createEvent(
            VDEventCabinLan.CABIN_LAN_MSG_COMMON,
            VDCLCommonMessage(2, gson.toJson(data)).apply {
                msgType = 0x1004
            })
        iCarInfoProxy.sendMessageToDashboard(event)
    }

    private val vehicleInfoCallback = object : VehicleInfoCallback {
        override fun dashboardMapDisplayStatus(status: CarDashboardStatus) {
            Timber.i("dashboardMapDisplayStatus status=$status")
            if (iCarInfoProxy.getDashboardTheme() == BaseConstant.CARDASHBOARD_THEME_NAVI) {
                when (status) {
                    CarDashboardStatus.CLOSE -> {
                        openDashboardMapView(false, true)
                    }

                    CarDashboardStatus.FULL_MAP_THEME -> {
                        openDashboardMapView(true, true)
                    }

                    else -> {}
                }
            }
        }

        override fun onCarDashboardTheme(theme: Int) {
            Timber.i("onCarDashboardTheme $theme")
            if (theme == BaseConstant.CARDASHBOARD_THEME_NAVI) {
                openDashboardMapView(true)
            } else {
                lastCarDashboardTheme = iCarInfoProxy.getDashboardTheme()
                openDashboardMapView(false)
            }
        }

        override fun dashboardMapModeState(mode: Int) {
            MainScope().launch {
                extMapBusiness.switchMapViewMode(mode)
            }
        }
    }

    private fun sendNaviTotalInfo() {
        Timber.d("sendNaviTotalInfo")
        val event = VDCLCommonMessage.createEvent(
            VDEventCabinLan.CABIN_LAN_MSG_COMMON,
            VDCLCommonMessage(3, gson.toJson(naviTotalInfo)).apply {
                msgType = 0x1004
            })
        MainScope().launch {
            iCarInfoProxy.sendMessageToDashboard(event)
        }
    }

    private fun sendNaviRoadInfo() {
        //Timber.d("sendNaviRoadInfo" + gson.toJson(naviRoadInfo))
        val event = VDCLCommonMessage.createEvent(
            VDEventCabinLan.CABIN_LAN_MSG_COMMON,
            VDCLCommonMessage(1, gson.toJson(naviRoadInfo)).apply {
                msgType = 0x1004
            })
        MainScope().launch {
            iCarInfoProxy.sendMessageToDashboard(event)
        }
    }

    private fun sendLaneInfo() {
        //Timber.d("sendLaneInfo " + gson.toJson(naviLaneInfo))
        val event = VDCLCommonMessage.createEvent(
            VDEventCabinLan.CABIN_LAN_MSG_COMMON,
            VDCLCommonMessage(0, gson.toJson(naviLaneInfo)).apply {
                msgType = 0x1004
            })
        MainScope().launch {
            iCarInfoProxy.sendMessageToDashboard(event)
        }
    }

    private fun sendNaviStatus(inNavi: Boolean) {
        Timber.i("sendNaviStatus inNavi=$inNavi")
        naviDigitalInfo.naviStatus = if (inNavi) 3 else 0 //3导航中，4结束导航
        val event = VDCLCommonMessage.createEvent(
            VDEventCabinLan.CABIN_LAN_MSG_COMMON,
            VDCLCommonMessage(4, gson.toJson(naviDigitalInfo)).apply {
                msgType = 0x1004
            })
        MainScope().launch {
            iCarInfoProxy.sendMessageToDashboard(event)
        }

        //退出导航重置下数据
        if (!inNavi) {
            naviTotalInfo.apply {
                TotalDistance = 0
                RemDistance = 0
                TimeLeft = 0
                ArrivalTime = null
                Week = null
            }

            naviRoadInfo.apply {
                segRemainDis = 0
                roadType = 0
                roadIcon = 0
                roadName = null
                nextRoadName = null
                intersectionZoomStatus = 0
            }
            naviLaneInfo.roadInfo.clear()
            sendNaviTotalInfo()
            sendNaviRoadInfo()
        }
    }

    /**
     * 通知仪表设置导航主题
     * 左滑/右滑/开始/结束导航
     */
    fun setCarDashboardShowNavi(showNavi: Boolean) {
        Timber.i("setCarDashboardShowNavi showNavi=$showNavi lastCarDashboardTheme=$lastCarDashboardTheme")
        if (showNavi) {
            iCarInfoProxy.setDashboardTheme(BaseConstant.CARDASHBOARD_THEME_NAVI)
        } else {
            if (iCarInfoProxy.getDashboardTheme() == BaseConstant.CARDASHBOARD_THEME_NAVI) {
                if (lastCarDashboardTheme > 0 && lastCarDashboardTheme != BaseConstant.CARDASHBOARD_THEME_NAVI) {
                    iCarInfoProxy.setDashboardTheme(lastCarDashboardTheme)
                } else {
                    iCarInfoProxy.setDashboardTheme(BaseConstant.CARDASHBOARD_THEME_CLASSIC)
                }
            }

        }
    }

    /**
     * 通知仪表导航全屏显示
     */
    private fun openDashboardMapView(show: Boolean, onlyNotify: Boolean = false) {
        Timber.i("openDashboardMapView show=$show onlyNotify=$onlyNotify")
        val naviDisplayArea = VDNaviDisplayArea().apply {
            naviDisplayArea =
                if (show) 10 else 11
            naviDisplayAreaResult = "true"
        }
        val event = VDCLCommonMessage.createEvent(
            VDEventCabinLan.CABIN_LAN_MSG_COMMON,
            VDCLCommonMessage(5, gson.toJson(naviDisplayArea)).apply {
                msgType = 0x1004
            })
        MainScope().launch {
            iCarInfoProxy.sendMessageToDashboard(event)
            if (!onlyNotify)
                dashboardCallback?.onCarDashboardMapDisplay(show)
        }
    }

    private suspend fun loopRefreshTrafficLightInfo() {
        while (lightCountdownInfo != null) {
            delay(1000)
            refreshTrafficLightInfo()
        }
        Timber.i("loopRefreshTrafficLightInfo finish")
    }

    private val lightStateInfoT1N =  VDNaviLightStateInfoT1N()

    /**
     * 发送红绿灯信息
     */
    private fun refreshTrafficLightInfo(curSegIdx: Int? = null, curLinkIdx: Int? = null) {
        Timber.d("refreshTrafficLightInfo curSegIdx=$curSegIdx curLinkIdx=$curLinkIdx")
        var mCurSegIdx = curSegIdx
        var mCurLinkIdx = curLinkIdx
        if (mCurSegIdx == null || mCurLinkIdx == null) {
            mCurSegIdx = naviBusiness.naviInfo.value?.curSegIdx ?: 0
            mCurLinkIdx = naviBusiness.naviInfo.value?.curLinkIdx ?: 0
        }

        var lightInfo: LightInfo? = null
        var indexLinkId = -1
        lightCountdownInfo?.forEach {
            //取当前segment的最近一个link的红绿灯信息
            //Timber.i("lightCountdownInfo ${it.segmentIndex.toInt()} == $mCurSegIdx")
            //Timber.i("lightCountdownInfo ${it.linkIndex.toInt()} == $mCurLinkIdx == $indexLinkId")
            if (it.segmentIndex.toInt() == mCurSegIdx && mCurLinkIdx <= it.linkIndex.toInt() && (it.linkIndex.toInt() < indexLinkId ||
                        indexLinkId == -1)
            ) {
                Timber.d("lightCountdownInfo ${gson.toJson(it.lightInfo)}")
                indexLinkId = it.linkIndex.toInt()
                lightInfo = it.lightInfo
            }
        }

        lightStateInfoT1N.clear()
        lightInfo?.let {
            lightStateInfoT1N.TrafficLightRounds = it.waitNum.toInt()
            it.lightStates?.let { lightStates ->
                var endTime = 0L
                var lightType = 0
                val currentTime = System.currentTimeMillis() / 1000
                for (lightState in lightStates) {
                    //Timber.i("lightState $currentTime ${lightState.stime} ${lightState.etime}")
                    if (currentTime >= lightState.stime && currentTime < lightState.etime) {
                        endTime = lightState.etime
                        lightType = lightState.lightType
                        break
                    }
                }
                if (endTime > 0L) {
                    lightStateInfoT1N.TrafficLightIcon = it.dir * 4 + lightType
                    lightStateInfoT1N.TrafficLightSecond = (endTime - currentTime).toInt()
                }
            }
        }
        Timber.i("refreshTrafficLightInfo lightStateInfo=${lightStateInfoT1N}")
        val event = VDCLCommonMessage.createEvent(
            VDEventCabinLan.CABIN_LAN_MSG_COMMON,
            VDCLCommonMessage(11, gson.toJson(lightStateInfoT1N)).apply {
                msgType = 0x1004
            })
        MainScope().launch {
            iCarInfoProxy.sendMessageToDashboard(event)
        }
    }

    interface IDashboardCallback {
        fun onCarDashboardMapDisplay(show: Boolean)
    }

}