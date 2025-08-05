package com.dji.navigation

import android.content.Context
import androidx.lifecycle.Observer
import com.autonavi.gbl.common.path.model.SubCameraExtType
import com.autonavi.gbl.common.path.option.PathInfo
import com.autonavi.gbl.guide.model.LaneInfo
import com.autonavi.gbl.guide.model.NaviCameraExt
import com.autonavi.gbl.guide.model.NaviFacility
import com.autonavi.gbl.guide.model.NaviFacilityType
import com.autonavi.gbl.guide.model.NaviInfo
import com.autonavi.gbl.guide.model.NaviInfoPanel
import com.autonavi.gbl.guide.model.NaviRoadFacility
import com.autonavi.gbl.pos.model.LocMatchInfo
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.common.utils.FileUtils
import com.desaysv.psmap.adapter.command.MassageType
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.NavigationUtil
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.model.bean.InputCommonData
import com.desaysv.psmap.model.bean.OutputCommonData
import com.desaysv.psmap.model.impl.IMapDataOutputCallback
import com.dji.navigation.LaneType.Companion.get
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author 谢锦华
 * @time 2025/2/6
 * @description
 */

@Singleton
class AdasSupportBusiness @Inject constructor(
    private val mNaviBusiness: NaviBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val mCruiseBusiness: CruiseBusiness,
    private val mSearchBusiness: SearchBusiness,
    private val mLocationBusiness: LocationBusiness,
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val mRouteRequestController: RouteRequestController,
) {
    private val adasScope = CoroutineScope(Dispatchers.IO + Job())
    private var mNaviRealTimeData: NaviRealTimeData = NaviRealTimeData()
    private var mCurrentPathId: Long = -1

    private var tollRemainDist: Int = 0//收费站剩余距离[单位m]

    // 导航状态
    // 0：未导航
    // 1：GPS导航
    // 2：模拟导航
    // 3：重新规划
    // 4：巡航
    // 5：偏航
    // 6：规划中，还未点确认开始导航
    //private var mNaviStatus = 0
    private var lastSendTime = 0L
    private var mCallback: IMapDataOutputCallback? = null

    fun registerMapDataOutputCallback(callback: IMapDataOutputCallback) {
        mCallback = callback
    }

    fun unregisterMapDataOutputCallback(callback: IMapDataOutputCallback) {
        mCallback = null
    }

    /**
     * 初始化
     */
    fun init() {
        mNaviBusiness.naviStatus.unPeek().observeForever(naviStateOb)// 导航状态
        mNaviBusiness.routeYawNotice.unPeek().observeForever(routeYawNoticeOb)//偏航通知
        mNaviBusiness.routeChangedNotice.unPeek().observeForever(routeChangedNoticeOb)//路线发生变化
        mCruiseBusiness.cruiseStatus.unPeek().observeForever(cruiseStatusNoticeOb)//巡航通知
        mRouteBusiness.planRouteNotice.unPeek().observeForever(planRouteNoticeOb)//规划中，还未点确认开始导航
        mNaviBusiness.naviInfo.unPeek().observeForever(naviInfoOb)// TBT信息
        mNaviBusiness.mCurrentRoadSpeed.unPeek().observeForever(roadSpeedOb)// 道路限速
        mNaviBusiness.locMatchInfo.unPeek().observeForever(locMatchInfoOb)// 当前道路类型、道路等级、link类型、位置Gps
        mNaviBusiness.naviLane.unPeek().observeForever(naviLaneOb)// 车道信息的数据
        mNaviBusiness.parallelRoadStatus.unPeek().observeForever(parallelRoadStatusOb)// 平行路状态
        mNaviBusiness.naviCameraList.unPeek().observeForever(naviCameraOb)// 电子眼的数据
        mNaviBusiness.naviRoadFacilityList.unPeek().observeForever(naviRoadFacilityOb)// 道路设施
        mNaviBusiness.sapaInfoList.unPeek().observeForever(sapaInfoOb)// 服务区和收费站数据
        mNaviBusiness.tollLaneList.unPeek().observeForever(tollLaneListOb)// 收费站车道线数据
    }

    /**
     * 反初始化
     */
    fun unInit() {
        mNaviBusiness.naviStatus.unPeek().removeObserver(naviStateOb)
        mNaviBusiness.routeYawNotice.unPeek().removeObserver(routeYawNoticeOb)
        mNaviBusiness.routeChangedNotice.unPeek().removeObserver(routeChangedNoticeOb)
        mCruiseBusiness.cruiseStatus.unPeek().removeObserver(cruiseStatusNoticeOb)
        mRouteBusiness.planRouteNotice.unPeek().removeObserver(planRouteNoticeOb)
        mNaviBusiness.naviInfo.unPeek().removeObserver(naviInfoOb)
        mNaviBusiness.mCurrentRoadSpeed.unPeek().removeObserver(roadSpeedOb)
        mNaviBusiness.locMatchInfo.unPeek().removeObserver(locMatchInfoOb)
        mNaviBusiness.naviLane.unPeek().removeObserver(naviLaneOb)
        mNaviBusiness.parallelRoadStatus.unPeek().removeObserver(parallelRoadStatusOb)
        mNaviBusiness.naviCameraList.unPeek().removeObserver(naviCameraOb)
        mNaviBusiness.naviRoadFacilityList.unPeek().removeObserver(naviRoadFacilityOb)
        mNaviBusiness.sapaInfoList.unPeek().removeObserver(sapaInfoOb)
        mNaviBusiness.tollLaneList.unPeek().removeObserver(tollLaneListOb)
    }

    /**
     *  导航状态
     */
    private val naviStateOb = Observer<Int> { naviState ->
        adasScope.launch {
            val mNaviStatus = when (naviState) {
                BaseConstant.NAVI_STATE_REAL_NAVING -> 1
                BaseConstant.NAVI_STATE_SIM_NAVING -> 2
                BaseConstant.NAVI_STATE_STOP_REAL_NAVI, BaseConstant.NAVI_STATE_STOP_SIM_NAVI -> 0
                else -> 0  // 处理未知状态
            }
            Timber.i(" mNaviStatus = $mNaviStatus ; naviState = $naviState")
            mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_NAVI_STATUS
            mNaviRealTimeData.mNaviType = Status.get(mNaviStatus)
            when (mNaviStatus) {
                0 -> {
                    //结束导航
                    sendNaviRealTimeData(mNaviRealTimeData)
                    mNaviRealTimeData = NaviRealTimeData()//结束导航初始化数据
                }

                1, 2 -> {
                    saveAmapNaviRoute()
                    sendNaviRealTimeData(mNaviRealTimeData)
                }

                else -> return@launch
            }
        }
    }

    /**
     * 偏航监听
     */
    private val routeYawNoticeOb = Observer<Boolean> { isRouteYaw ->
        adasScope.launch {
            if (isRouteYaw) {
                mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_NAVI_STATUS
                mNaviRealTimeData.mNaviType = Status.get(5)//偏航
                sendNaviRealTimeData(mNaviRealTimeData)
            }
        }
    }

    /**
     * 巡航通知
     */
    private val cruiseStatusNoticeOb = Observer<Boolean> { isCruise ->
        adasScope.launch {
            mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_NAVI_STATUS
            val mNaviStatus = if (isCruise) 4 else 0
            mNaviRealTimeData.mNaviType = Status.get(mNaviStatus)//巡航
            sendNaviRealTimeData(mNaviRealTimeData)
        }
    }

    /**
     * 规划中，还未点确认开始导航
     */
    private val planRouteNoticeOb = Observer<Boolean> { isPlanRoute ->
        adasScope.launch {
            mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_NAVI_STATUS
            val mNaviStatus = if (isPlanRoute) 6 else 0
            mNaviRealTimeData.mNaviType = Status.get(mNaviStatus)//规划中
            sendNaviRealTimeData(mNaviRealTimeData)
        }
    }

    /**
     * 路线发生变化监听
     */
    private val routeChangedNoticeOb = Observer<Boolean> { isRouteChanged ->
        adasScope.launch {
            if (isRouteChanged) {
                saveAmapNaviRoute()
                /**
                 * 通知智驾路线发生变化
                 */
                mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_NAVI_STATUS
                mNaviRealTimeData.mNaviType = Status.get(3)//路线发生变化
                mNaviRealTimeData.mPathId = mCurrentPathId
                sendNaviRealTimeData(mNaviRealTimeData)
                mNaviRealTimeData.mNaviType = Status.get(1)
            }
        }
    }

    /**
     *  TBT信息
     */
    private val naviInfoOb = Observer<NaviInfo?> { data ->
        data?.let { adasData ->
            adasScope.launch {
                val pathInfo = getRealPathInfo()
                if (pathInfo == null) {
                    Timber.i("getRealPathId pathInfo is null")
                    return@launch
                }
                mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_TBT
                adasData.NaviInfoData?.takeIf { it.isNotEmpty() }?.let {
                    val naviInfoPanel: NaviInfoPanel? = adasData.NaviInfoData.getOrNull(adasData.NaviInfoFlag)
                    naviInfoPanel?.let {
                        mNaviRealTimeData.mDistanceToNextStep = it.segmentRemain.dist // 下一个路口距离 到下个Step的距离[m]
                        mNaviRealTimeData.mCurIconType = IconType.get(it.maneuverID) //导航推荐动作
                    }
                }
                // 更进阶 下个路口信息
                if (NavigationUtil.hasNextThumTip(adasData)) {
                    val nextCrossInfo = adasData.nextCrossInfo[0]
                    mNaviRealTimeData.mNextCloseIconType = IconType.get(nextCrossInfo.maneuverID) //下一个接近的导航推荐动作
                    mNaviRealTimeData.mNextCloseIconDistance = nextCrossInfo.curToSegmentDist//下一个接近的导航推荐动作增量距离
                } else {
                    mNaviRealTimeData.mNextCloseIconType = IconType.get(0) //下一个接近的导航推荐动作
                    mNaviRealTimeData.mNextCloseIconDistance = 0//下一个接近的导航推荐动作增量距离
                }
                adasData.curRouteName?.let {
                    mNaviRealTimeData.mCurRoadName = it //当前道路名
                } ?: run {
                    mNaviRealTimeData.mCurRoadName = context.getString(R.string.sv_navi_no_name_road) //当前道路名
                }
                //获取路线长度
                val totalDistance = pathInfo.length
                mNaviRealTimeData.mAllLength = totalDistance.toInt() //导航全程长度[m]
                mNaviRealTimeData.mAdCode = adasData.cityCode //城市编码
                mNaviRealTimeData.mPathRetainDistance = adasData.routeRemain.dist //路线剩余距离[m]
                mNaviRealTimeData.mCurStepId = adasData.curSegIdx //当前导航段Step Id
                mNaviRealTimeData.mCurLinkId = adasData.curLinkIdx //当前小路段Link Id
                mNaviRealTimeData.mDistanceToNextLink = adasData.linkRemainDist //到下个Link的距离[m]
                mNaviRealTimeData.mPathId = pathInfo.pathID
                sendNaviRealTimeData(mNaviRealTimeData)
            }
        }

    }

    /**
     * 当前道路限速
     */
    private val roadSpeedOb = Observer<Int> { roadSpeed ->
        adasScope.launch {
            if (mNaviBusiness.isNavigating()) {
                mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_ROAD_SPEED_LIMIT
                mNaviRealTimeData.mCurSpeedLimit = roadSpeed
                sendNaviRealTimeData(mNaviRealTimeData)
            }
        }
    }

    /**
     * 平行路状态
     */
    private val parallelRoadStatusOb = Observer<Int> { parallelRoadStatus ->
        adasScope.launch {
            mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_PARALLELROAD_STATUS
            mNaviRealTimeData.mParallelRoadStatus = ParallelRoadStatus.get(parallelRoadStatus)
            sendNaviRealTimeData(mNaviRealTimeData)
        }
    }

    /**
     * 当前道路类型、道路等级、link类型、位置Gps
     */
    private val locMatchInfoOb = Observer<LocMatchInfo?> { data ->
        try {
            adasScope.launch {
                data?.let { locMatchInfo ->
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastSendTime >= 1000) { // 检查是否超过1秒
                        mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_ROAD_TYPE_AND_CLASS_GPS
                        mNaviRealTimeData.mCurRoadClass = RoadClass.get(locMatchInfo.roadClass)
                        mNaviRealTimeData.mCurLinkType = LinkType.get(locMatchInfo.linkType)
                        mNaviRealTimeData.mCurRoadType = FormWay.get(locMatchInfo.formway)
                        mNaviRealTimeData.mCurPosition = locMatchInfo.stPos?.let { GpsPoint3D(it.lat, it.lon, it.z) }
                        sendNaviRealTimeData(mNaviRealTimeData)
                        lastSendTime = currentTime // 更新上次发送时间
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("locMatchInfoOb Exception:${e.message}")
        }
    }

    /**
     *  车道信息的数据
     */
    private val naviLaneOb = Observer<LaneInfo?> { data ->
        adasScope.launch {
            mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_LANE
            data?.let { laneInfo ->
                val frontTypes = ArrayList<LaneType>()
                val blaneTypes = ArrayList<LaneType>()
                if (laneInfo.frontLane != null && laneInfo.frontLane.size > 0) {
                    for (i in laneInfo.frontLane) {
                        try {
                            frontTypes.add(get(i))
                        } catch (e: Exception) {
                            Timber.e("frontLane_not_found", e)
                            frontTypes.add(get(0)) //找不到类型传直行
                        }
                    }
                }
                if (laneInfo.backLane != null && laneInfo.backLane.size > 0) {
                    for (i in laneInfo.backLane) {
                        try {
                            blaneTypes.add(get(i))
                        } catch (e: Exception) {
                            Timber.e("backLane_not_found", e)
                            blaneTypes.add(get(0)) //找不到类型传直行
                        }
                    }
                }
                mNaviRealTimeData.mLaneActions = LaneActions(0.0, frontTypes, blaneTypes)
            } ?: run {
                Timber.i("laneInfoCallBack data is null")
                mNaviRealTimeData.mLaneActions = null
            }
            sendNaviRealTimeData(mNaviRealTimeData)
        }
    }

    /**
     *  电子眼的数据
     */
    private val naviCameraOb = Observer<ArrayList<NaviCameraExt>?> { data ->
        adasScope.launch {
            mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_CAMERA_INFO
            if (data.isNullOrEmpty()) {
                mNaviRealTimeData.mCameraInfo = null
            } else {
                val naviCamera = data.firstOrNull() ?: return@launch
                val mCameraDistance = naviCamera.distance

                var smallSpeed = 0
                var mCameraSpeed = 0
                var mCameraType = 0
                naviCamera.subCameras?.forEach { subCamera ->
                    when (subCamera.subType) {
                        SubCameraExtType.SubCameraExtTypeUltrahighSpeed,
                        SubCameraExtType.SubCameraExtTypeVariableSpeed -> {
                            subCamera.speed?.forEach { sp ->
                                if (smallSpeed == 0 || sp < smallSpeed) {
                                    smallSpeed = sp.toInt()
                                    mCameraType = subCamera.subType
                                }
                            }
                            mCameraSpeed = smallSpeed
                        }
                    }
                }

                if (mCameraSpeed <= 0) {
                    mCameraType = naviCamera.subCameras?.firstOrNull()?.subType!!
                }

                mNaviRealTimeData.mCameraInfo = CameraInfo(
                    mCameraDistance,
                    CameraType.get(mCameraType),
                    mCameraSpeed,
                    0,
                    GpsPoint3D(naviCamera.coord3D.lat, naviCamera.coord3D.lon, naviCamera.coord3D.z)
                )
            }
            sendNaviRealTimeData(mNaviRealTimeData)
        }
    }

    /**
     *  道路设施
     */
    private val naviRoadFacilityOb = Observer<ArrayList<NaviRoadFacility>?> { data ->
        adasScope.launch {
            mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_ROAD_SITUATION
            mNaviRealTimeData.mFacilities = data?.firstOrNull()?.let { naviRoadFacility ->
                AmapFacility(
                    naviRoadFacility.distance,
                    FacilityType.get(naviRoadFacility.type),
                    0,
                    0,
                    GpsPoint3D(naviRoadFacility.coord2D.lat, naviRoadFacility.coord2D.lon, 0.0)
                )
            } ?: run {
                // 如果 data 为空或 firstOrNull 返回 null，设置 facilities 为 null
                null
            }
            sendNaviRealTimeData(mNaviRealTimeData)
        }
    }

    /**
     *  服务区和收费站数据
     */
    private val sapaInfoOb = Observer<ArrayList<NaviFacility>?> { infoArray ->
        infoArray?.takeIf { it.isNotEmpty() }?.let { sapaInfoList ->
            adasScope.launch {
                val list = sapaInfoList.take(1)
                Timber.i("sapaInfoOb is called sapaInfoList = ${gson.toJson(list)}")
                if (list[0].type == NaviFacilityType.NaviFacilityTypeTollGate) {
                    tollRemainDist = list[0].remainDist
                }
            }
        }
    }

    /**
     *  收费站车道线数据
     */
    private val tollLaneListOb = Observer<ArrayList<Int>?> { data ->
        adasScope.launch {
            mNaviRealTimeData.mMessageType = MsgType.MSG_TYPE_SAPA_INFO
            data?.takeIf { it.isNotEmpty() }?.let { tollLaneList ->
                Timber.i("tollLaneListOb is called tollLaneList = ${gson.toJson(tollLaneList)}")
                mNaviRealTimeData.mTollGateInfo = TollGateInfo(1, tollRemainDist, tollLaneList)
            } ?: run {
                mNaviRealTimeData.mTollGateInfo = null
            }
            sendNaviRealTimeData(mNaviRealTimeData)
        }
    }

    /**
     * 保存adas全局路径信息 发送
     */
    @Synchronized
    private fun saveAmapNaviRoute() {
        val pathResult = mRouteRequestController.carRouteResult
        if (pathResult == null) {
            Timber.w("saveAmapNaviRoute pathResult=null")
            return
        }
        val variantPathWrap = pathResult.pathResult[pathResult.focusIndex]
        if (variantPathWrap == null) {
            Timber.w("variantPathWrap = null")
            return
        }
        val pathID = variantPathWrap.pathID
        if (mCurrentPathId == pathID) {
            Timber.i("saveAmapNaviRoute same pathID $mCurrentPathId")
            return
        }
        Timber.i("saveAmapNaviRoute start")
        val segmentCount = variantPathWrap.segmentCount
        val pathJsonInfo = JsonObject()
        val amapNaviSteps = JsonArray()
        for (i in 0 until segmentCount) {
            val segmentAccessor = variantPathWrap.getSegmentInfo(i) ?: continue
            val amapNaviStep = JsonObject()
            val linkCount = segmentAccessor.linkCount
            amapNaviStep.addProperty("id", i)
            amapNaviStep.addProperty("length", segmentAccessor.length)
            amapNaviStep.addProperty("main_action", segmentAccessor.mainAction)
            amapNaviStep.addProperty("assistant_action", segmentAccessor.assistantAction)
            amapNaviStep.addProperty("link_num_all", linkCount)

            val amapNaviLinks = JsonArray()
            for (j in 0 until linkCount) {
                val amapNaviLink = JsonObject()
                val linkAccessor = segmentAccessor.getLinkInfo(j) ?: continue
                amapNaviLink.addProperty("id", j)
                amapNaviLink.addProperty("length", linkAccessor.length)
                amapNaviLink.addProperty("road_class", linkAccessor.roadClass)
                amapNaviLink.addProperty("road_type", linkAccessor.formway)
                amapNaviLink.addProperty("link_type", linkAccessor.linkType)
                amapNaviLink.addProperty("link_name", linkAccessor.roadName)
                amapNaviLink.addProperty("lane_num", linkAccessor.laneNum)
                amapNaviLink.addProperty("has_mix_fork", linkAccessor.hasMixFork())
                amapNaviLink.addProperty("has_multi_out", linkAccessor.hasMultiOut())

                val facilities = JsonObject()
                val roadFacility = linkAccessor.getRoadFacility(linkAccessor.linkIndex.toShort());
                facilities.addProperty("distance", roadFacility.distToEnd)
                facilities.addProperty("type", roadFacility.type)
                facilities.addProperty("mValue1", 0)
                facilities.addProperty("mValue2", 0)
                val coord3DJson = JsonObject()
                coord3DJson.addProperty("latitude", roadFacility.lat / 3600000.0)
                coord3DJson.addProperty("longitude", roadFacility.lon / 3600000.0)
                coord3DJson.addProperty("altitude", 0)
                facilities.add("mGpsPoint", coord3DJson)
                amapNaviLink.add("facilities", facilities)

                val dPoints = linkAccessor.points
                val dPointsJson = JsonArray()
                dPoints?.let {
                    for (coord3DInt32 in dPoints) {
                        val coord3DJson = JsonObject()
                        coord3DJson.addProperty("latitude", coord3DInt32.lat / 3600000.0)
                        coord3DJson.addProperty("longitude", coord3DInt32.lon / 3600000.0)
                        coord3DJson.addProperty("altitude", 0)
                        dPointsJson.add(coord3DJson)
                    }
                }
                amapNaviLink.add("gps_points", dPointsJson)

                amapNaviLinks.add(amapNaviLink)
            }
            amapNaviStep.add("links", amapNaviLinks)
            amapNaviSteps.add(amapNaviStep)
        }

        pathJsonInfo.addProperty("proto_id", 1)
        pathJsonInfo.addProperty("path_id", pathID)
        pathJsonInfo.addProperty("all_length", variantPathWrap.length)
        pathJsonInfo.addProperty("step_num_all", segmentCount)
        pathJsonInfo.add("steps", amapNaviSteps)

        //测试数据正确性代码 正式需注释
        val filePath = BaseConstant.NAV_MAP_ADAS + "NaviPlanningRoute.json"
        FileUtils.writeToFile(pathJsonInfo.toString(), filePath, false)
        mCurrentPathId = pathID
        Timber.i("saveAmapNaviRoute save pathID $mCurrentPathId")
    }

    /**
     * 获取路线
     */
    private fun getRealPathInfo(): PathInfo? {
        val pathInfo = mRouteRequestController.carRouteResult
            ?.let { it.pathResult.getOrNull(it.focusIndex) }
        Timber.i("getRealPath PathInfo pathID=${pathInfo?.pathID} length=${pathInfo?.length}")
        return pathInfo
    }

    /**
     * 监听智驾指令
     */
    fun parseCommandMassage(pkg: String, json: String) {
        adasScope.launch {
            try {
                val msg = gson.fromJson(json, InputCommonData::class.java)
                when (msg.massageType) {
                    MassageType.ACTION_REQUEST_MAP_POI_NAME.name -> {
                        val result = mSearchBusiness.nearestSearch(
                            poi = POIFactory.createPOI(
                                null,
                                GeoPoint(mLocationBusiness.getLastLocation().longitude, mLocationBusiness.getLastLocation().latitude)
                            )
                        )
                        when (result.status) {
                            com.desaysv.psmap.base.utils.Status.SUCCESS -> {
                                val poiList = SearchCommonUtils.invertOrderList(result.data?.poi_list)
                                poiList?.takeIf { it.isNotEmpty() }?.let { poiList ->
                                    Timber.i("poiList is called tollLaneList = ${gson.toJson(poiList)}")
                                    /**
                                     * 通知智驾记忆泊车POI名称
                                     */
                                    setAndSendParkingInfo(poiList[0].name)
                                } ?: run {
                                    // 如果 result.data 为 null，发送空 POI 名称
                                    setAndSendParkingInfo("")
                                }
                            }

                            else -> {
                                Timber.i("nearestSearch Fail $result")
                                // 如果 result.data 为 null，发送空 POI 名称
                                setAndSendParkingInfo("")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "parseCommandMassage")
                setAndSendParkingInfo("")
            }
        }
    }

    /**
     * 通知智驾记忆泊车POI名称
     * @param poiName POI 名称
     */
    private fun setAndSendParkingInfo(poiName: String) {
        mNaviRealTimeData.apply {
            mMessageType = MsgType.MSG_TYPE_PARKING_NAME
            mParkingPOIName = poiName
        }.also {
            sendNaviRealTimeData(it)
        }
    }

    /**
     * 通知智驾回放路线 起点和终点名称
     * @param startPoiName 起点POI 名称
     * @param endPoiName 终点POI 名称
     */
    fun setAndSendPOINameInfo(startPoiName: String, endPoiName: String) {
        mNaviRealTimeData.apply {
            mMessageType = MsgType.MSG_TYPE_POI_INFO
            mPOIInfo = AmapPOIInfo(startPoiName, endPoiName)
        }.also {
            sendNaviRealTimeData(it)
        }
    }

    private fun sendNaviRealTimeData(naviRealTimeData: NaviRealTimeData) {
        Timber.i(" sendNaviRealTimeData is called ${gson.toJson(naviRealTimeData)}")
        val data = OutputCommonData(massageType = MassageType.ON_NAVI_ADAS_MAP, data = naviRealTimeData)
        mCallback?.onMapDataToAllPackage(gson.toJson(data))
    }

}