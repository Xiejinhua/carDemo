package com.desaysv.psmap.base.business

import android.app.Application
import android.os.CountDownTimer
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.aosclient.model.GDriveReportSmsRequestParam
import com.autonavi.gbl.aosclient.model.GDriveReportUploadRequestParam
import com.autonavi.gbl.aosclient.model.GWsShieldNavigationRoutepathrestorationRequestParam
import com.autonavi.gbl.aosclient.model.RouteDisplayPoints
import com.autonavi.gbl.aosclient.model.RoutePathProjectPoints
import com.autonavi.gbl.aosclient.model.RouteViaProjInfo
import com.autonavi.gbl.aosclient.model.RoutepathrestorationPathsInfo
import com.autonavi.gbl.aosclient.model.RoutepathrestorationPointInfo
import com.autonavi.gbl.aosclient.model.RoutepathrestorationVehicleInfo
import com.autonavi.gbl.aosclient.observer.ICallBackDriveReportSms
import com.autonavi.gbl.aosclient.observer.ICallBackDriveReportUpload
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.path.model.POIInfo
import com.autonavi.gbl.common.path.model.PointType
import com.autonavi.gbl.common.path.option.RouteConstrainCode
import com.autonavi.gbl.common.path.option.RouteStrategy
import com.autonavi.gbl.common.path.option.SegmentInfo
import com.autonavi.gbl.route.RouteService
import com.autosdk.bussiness.aos.AosController
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.manager.SDKManager
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.navi.route.utils.RouteOptionUtil
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.bussiness.widget.setting.SettingComponent
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.common.NetWorkManager
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * @author 谢锦华
 * @time 2024/2/26
 * @description 分享路线二维码控制类
 */

@Singleton
class TripShareBusiness @Inject constructor(
    private val mSettingComponent: SettingComponent,
    private val gson: Gson,
    private val mAosController: AosController,
    private val mNetWorkManager: NetWorkManager,
    private val mLocationController: LocationController,
    private val application: Application,
    private val mNaviController: NaviController,
    private val mRouteRequestController: RouteRequestController,
) {

    private var mTripShareParam: GDriveReportUploadRequestParam? = null
    private var _naviTripUrlLd = MutableLiveData("")
    val naviTripUrlLd: LiveData<String> = _naviTripUrlLd

    //Toast内容
    val setToast = MutableLiveData<String>()

    @Volatile
    private var mFinished = false

    @Volatile
    private var isNaving = false

    @Volatile
    private var mCurDrivingRouteTime = 0

    @Volatile
    private var mCurDrivingRouteDist = 0

    @Volatile
    private var mCurRouteRemainDist = 0

    @Volatile
    private var mCurRouteRemainTime = 0

    @Volatile
    private var mSpeed = 0f

    private var isRequest = false


    fun setFinished(finished: Boolean) {
        Timber.i("setIsNaving() called with: finished = [$finished]")
        mFinished = finished
    }

    fun setCurDrivingRouteTime(curDrivingRouteTime: Int) {
        Timber.i("setCurDrivingRouteTime() called with: curDrivingRouteTime = [$curDrivingRouteTime]")
        mCurDrivingRouteTime = curDrivingRouteTime
    }

    fun setCurDrivingRouteDist(curDrivingRouteDist: Int) {
        Timber.i("setCurDrivingRouteDist() called with: curDrivingRouteDist = [$curDrivingRouteDist]")
        mCurDrivingRouteDist = curDrivingRouteDist
    }

    fun setCurRouteRemainDist(curRouteRemainDist: Int) {
        Timber.i("setCurRouteRemainDist() called with: curRouteRemainDist = [$curRouteRemainDist]")
        mCurRouteRemainDist = curRouteRemainDist
    }

    fun setCurRouteRemainTime(curRouteRemainTime: Int) {
        Timber.i("setCurRouteRemainTime() called with: curRouteRemainTime = [$curRouteRemainTime]")
        mCurRouteRemainTime = curRouteRemainTime
    }

    fun setSpeed(speed: Float) {
        Timber.i("setSpeed() called with: speed = [$speed]")
        mSpeed = speed
    }

    fun setNaving(naving: Boolean) {
        Timber.i("setNaving() called with: speed = [$naving]")
        isNaving = naving
    }


    private val tripShareRunnable = object : CountDownTimer(120000, 1000) {
        override fun onTick(millisUntilFinished: Long) = Unit

        override fun onFinish() {
            Timber.i(" tripShareRunnable onFinish is called ")
            startScheduleUpload()
        }
    }

    private fun removeTripShareRunnable() = tripShareRunnable.cancel()
    private fun startTripShareRunnable() = tripShareRunnable.start()

    fun initTripShareParam() {
        Timber.i("initTripShareParam() is called.")
        mTripShareParam = GDriveReportUploadRequestParam()
        mTripShareParam!!.option = calcTripShareOption()
        if (isNaving) {
            startScheduleUpload()
        } else {
            val routeCarResultData = mRouteRequestController.carRouteResult
            if (routeCarResultData == null) {
                Timber.i("initTripShareParam() routeCarResultData is null.")
                return
            }
            val location = SDKManager.getInstance().locController.lastLocation
            val startPoi = POIFactory.createPOI("我的位置", GeoPoint(location.longitude, location.latitude))
            updateNaviPointInfo(startPoi, routeCarResultData.toPOI, routeCarResultData.midPois)
        }
    }

    fun reqTripUrl() {
//        if (!mNetWorkManager.isNetworkConnected()) {
//            Timber.i("reqTripUrl() mTripShareParam is No internet.")
//            return
//        }
        if (mTripShareParam == null) {
            Timber.i("reqTripUrl() mTripShareParam is null.")
            return
        }
//        if (!TextUtils.isEmpty(mTripShareParam!!.id)) {
//            Timber.i("reqTripUrl() id = ${mTripShareParam!!.id}")
//            return
//        }
        Timber.i("reqTripUrl() called")
        uploadTripShareData()
    }

    fun startScheduleUpload() {
        if (mTripShareParam == null) {
            Timber.i("startScheduleUpload() mTripShareParam is null.")
            return
        }
//        if (TextUtils.isEmpty(mTripShareParam!!.id)) {
//            Timber.i("startScheduleUpload() id is null")
//            return
//        }
        Timber.i("startScheduleUpload() called")
        removeTripShareRunnable()
        startTripShareRunnable()

        val routeCarResultData = mRouteRequestController.carRouteResult
        if (routeCarResultData == null) {
            Timber.i("startScheduleUpload() routeCarResultData is null.")
            return
        }
        val location = SDKManager.getInstance().locController.lastLocation
        val startPoi = POIFactory.createPOI("我的位置", GeoPoint(location.longitude, location.latitude))
        updateNaviPointInfo(startPoi, routeCarResultData.toPOI, routeCarResultData.midPois)
    }

    fun stop() {
        Timber.i("stop() called")
        removeTripShareRunnable()
        mFinished = true
        clearData()
    }

    private fun clearData() {
        Timber.i("clearData() called")
        mTripShareParam = null
        _naviTripUrlLd.postValue("")
        mFinished = false
        isNaving = false
        mCurDrivingRouteTime = 0
        mCurDrivingRouteDist = 0
        mCurRouteRemainDist = 0
        mCurRouteRemainTime = 0
        mSpeed = 0f
        isRequest = false
    }


    fun updateNaviPointInfo(from: POI, to: POI, midPois: List<POI>?) {
        try {
            Timber.i("updateNaviPointInfo() called with: from = [${gson.toJson(from)}], to = [${gson.toJson(to)}], midPois = [${gson.toJson(midPois)}]")
            if (mTripShareParam == null) {
                mTripShareParam = GDriveReportUploadRequestParam()
            }
            val startInfo: POIInfo = RouteOptionUtil.poiToPOIInfo(from, PointType.PointTypeStart)
            val endInfo: POIInfo = RouteOptionUtil.poiToPOIInfo(to, PointType.PointTypeEnd)
            mTripShareParam!!.startPoiName = startInfo.name
            val startPos: Coord2DDouble = getNaviPos(startInfo)
            mTripShareParam!!.startX = startPos.lon.toFloat()
            mTripShareParam!!.startY = startPos.lat.toFloat()
            val viaList: MutableList<POIInfo> = ArrayList()
            if (!midPois.isNullOrEmpty()) {
                for (i in midPois.indices) {
                    viaList.add(RouteOptionUtil.poiToPOIInfo(midPois[i], PointType.PointTypeVia))
                }
            }
            val viaSize = viaList.size
            var viaItem: POIInfo
            val sb = StringBuilder()
            var midPos: Coord2DDouble
            for (i in 0 until viaSize) {
                viaItem = viaList[i]
                midPos = getNaviPos(viaItem)
                sb.append(midPos.lon)
                sb.append(",")
                sb.append(midPos.lat)
                sb.append(",")
                sb.append(viaItem.name)
                if (i != viaSize - 1) {
                    sb.append("|")
                }
            }
            mTripShareParam!!.viaPoints = sb.toString()
            mTripShareParam!!.endPoiName = endInfo.name
            val endPos: Coord2DDouble = getNaviPos(endInfo)
            mTripShareParam!!.endX = endPos.lon.toFloat()
            mTripShareParam!!.endY = endPos.lat.toFloat()
            uploadTripShareData()
        } catch (e: Exception) {
            Timber.e("updateNaviPointInfo Exception:${e.message}")
        }
    }

    private fun calcTripShareOption(): String {
        val sb = StringBuilder()
        val routePrefer = mSettingComponent.configKeyPlanPref
        val routeConstrainCode: Int = AutoRouteUtil.getRouteConstrainCode(routePrefer, true, mNetWorkManager.isNetworkConnected())
        val strategy: Int = AutoRouteUtil.getRouteStrategy(routePrefer, mNetWorkManager.isNetworkConnected())

        Timber.i("calcTripShareOption() strategy=$strategy, routeConstrainCode=$routeConstrainCode")
        if (strategy == RouteStrategy.RequestRouteTypeMostly) {
            sb.append("1")
        } else {
            if (strategy and RouteStrategy.RequestRouteTypeTMC == RouteStrategy.RequestRouteTypeTMC ||
                strategy and RouteStrategy.RequestRouteTypeTMCFree == RouteStrategy.RequestRouteTypeTMCFree
            ) {
                sb.append("2|")
            }
            if (strategy and RouteStrategy.RequestRouteTypeMoney == RouteStrategy.RequestRouteTypeMoney) {
                sb.append("4|")
            }
            if (routeConstrainCode and RouteConstrainCode.RouteAvoidFreeway == RouteConstrainCode.RouteAvoidFreeway) {
                sb.append("8|")
            }
            if (sb.isNotEmpty()) {
                sb.deleteCharAt(sb.length - 1)
            } else {
                sb.append("1")
            }
        }
        return sb.toString()
    }

    private fun getNaviPos(info: POIInfo): Coord2DDouble {
        return if (info.naviPos.lat > 0 && info.naviPos.lon > 0) info.naviPos else info.realPos
    }

    private fun uploadTripShareData() {
        if (isRequest) {
            Timber.i("uploadTripShareData() mTripShareParam is isRequest.")
            return
        }
        val routeCarResultData = mRouteRequestController.carRouteResult
        if (mTripShareParam == null || routeCarResultData == null || routeCarResultData.pathResult == null || routeCarResultData.pathResult.isEmpty()) {
            Timber.i("uploadTripShareData() mTripShareParam is null.")
            return
        }
        val pathInfo = routeCarResultData.pathResult[mRouteRequestController.carRouteResult.focusIndex]

        val tripShareParam: GDriveReportUploadRequestParam = mTripShareParam as GDriveReportUploadRequestParam
        tripShareParam.link_info.clear()
        val location = mLocationController.lastLocation
        tripShareParam.f32X = location.longitude.toFloat()
        tripShareParam.f32Y = location.latitude.toFloat()
        tripShareParam.finished = mFinished
        tripShareParam.duration = mCurDrivingRouteTime
        tripShareParam.distance = mCurDrivingRouteDist
        tripShareParam.residualDistance = mCurRouteRemainDist
        tripShareParam.totalDistance = mCurDrivingRouteDist + mCurRouteRemainDist
        tripShareParam.leftTime = mCurRouteRemainTime
        tripShareParam.speed = mSpeed

        val linkInfoParam = GWsShieldNavigationRoutepathrestorationRequestParam()
        linkInfoParam.type = "phone_car_share"
        linkInfoParam.encoder_version = "json"

        val httpProtocolParam = pathInfo.httpProtocolParam
        httpProtocolParam?.let {
            linkInfoParam.content_options = httpProtocolParam.contentOptions
        }

        linkInfoParam.sdk_vers = RouteService.getEngineVersion()
        linkInfoParam.brief = "0"
        linkInfoParam.navi_id = pathInfo.naviID

        val pathStartPoint = Coord2DDouble()
        val pathEndPoint = Coord2DDouble()
        val restorationPathInfo = RoutepathrestorationPathsInfo()
        restorationPathInfo.id_mode = 1
        var frontLinkId: Long = 0
        val totalSegmentCount: Long = pathInfo.segmentCount
        for (segmentIndex in 0 until totalSegmentCount) {
            val segmentInfo: SegmentInfo = pathInfo.getSegmentInfo(segmentIndex)
            if (segmentInfo != null) {
                val totalLinkCount = segmentInfo.linkCount
                for (linkIndex in 0 until totalLinkCount) {
                    val linkInfo = segmentInfo.getLinkInfo(linkIndex) ?: continue

                    // 获取起点、终点的投影点
                    val points = linkInfo.points
                    val linkPointCount = points.size
                    if (segmentIndex == 0L && linkIndex == 0L && linkPointCount > 0) {
                        pathStartPoint.lon = points[0].lon.toDouble() / 3600000.0
                        pathStartPoint.lat = points[0].lat.toDouble() / 3600000.0
                    } else if (segmentIndex == (totalSegmentCount - 1) && linkIndex == totalLinkCount - 1 && linkPointCount > 0) {
                        pathEndPoint.lon = points[linkPointCount - 1].lon.toDouble() / 3600000.0
                        pathEndPoint.lat = points[linkPointCount - 1].lat.toDouble() / 3600000.0
                    }

                    // 首个LinkID要完整，后续都传递LinkID差值
                    val curLinkId = linkInfo.get64TopoID().toLong()
                    if (restorationPathInfo.id.isEmpty()) {
                        restorationPathInfo.id.add(curLinkId.toString())
                    } else {
                        val diffVal = curLinkId - frontLinkId
                        restorationPathInfo.id.add(diffVal.toString())
                    }

                    frontLinkId = curLinkId
                }
            }
        }
        linkInfoParam.paths.add(restorationPathInfo)

        val vehicleInfo = RoutepathrestorationVehicleInfo()
        vehicleInfo.type = "0" // 客车
        vehicleInfo.size = "1" //  size 含义: 货车大小 1：微型车
        vehicleInfo.plate =
            if (mSettingComponent.getConfigKeyPlateNumber().isNullOrEmpty()) "" else mSettingComponent.getConfigKeyPlateNumber()  //判断是否有车牌号
        linkInfoParam.vehicle = vehicleInfo


        // Start Point
        val restorationStartPoint = RoutepathrestorationPointInfo()
        restorationStartPoint.type = 0
        restorationStartPoint.idx = 0
        restorationStartPoint.lon = location.longitude.toString()
        restorationStartPoint.lat = location.latitude.toString()
        linkInfoParam.start.points.add(restorationStartPoint)

        // 投影点（起点映射到路线上的点）
        val restorationPathStartPoint = RoutepathrestorationPointInfo()
        restorationPathStartPoint.type = 0
        restorationPathStartPoint.idx = 1
        restorationPathStartPoint.lon = pathStartPoint.lon.toString()
        restorationPathStartPoint.lat = pathStartPoint.lat.toString()
        linkInfoParam.start.points.add(restorationPathStartPoint)
        Timber.i("updateNaviPointInfo linkInfoParam.start.points = ${gson.toJson(linkInfoParam.start.points)}")


        // End Point
        val restorationEndPoint = RoutepathrestorationPointInfo()
        restorationEndPoint.type = 0
        restorationEndPoint.idx = 0
        restorationEndPoint.lon = routeCarResultData.toPOI.point.getLongitude().toString()
        restorationEndPoint.lat = routeCarResultData.toPOI.point.getLatitude().toString()
        linkInfoParam.end.points.add(restorationEndPoint)

        // 投影点（终点映射到路线上的点）
        val restorationPathEndPoint = RoutepathrestorationPointInfo()
        restorationPathEndPoint.type = 0
        restorationPathEndPoint.idx = 1
        restorationPathEndPoint.lon = pathEndPoint.lon.toString()
        restorationPathEndPoint.lat = pathEndPoint.lat.toString()
        linkInfoParam.end.points.add(restorationPathEndPoint)


        // Via Point
        val viaPointInfos = pathInfo.viaPointInfo
        viaPointInfos?.let {
            val totalViaCount = it.size
            if (totalViaCount > 0) {
                val viaProjectPoints = RoutePathProjectPoints()
                viaProjectPoints.path_idx = 1 // 路线编号：从1开始

                for (viaIndex in 0 until totalViaCount) {
                    val viaPoint = it[viaIndex]

                    val displayPoint = RouteDisplayPoints()
                    displayPoint.lon = viaPoint.show.lon.toString()
                    displayPoint.lat = viaPoint.show.lat.toString()
                    linkInfoParam.via_info.display_points.add(displayPoint)

                    // 投影点 （途经点映射到路线上的点）
                    val viaProjInfo = RouteViaProjInfo()
                    viaProjInfo.lon = viaPoint.projective.lon.toString()
                    viaProjInfo.lat = viaPoint.projective.lat.toString()
                    val segmentInfo: SegmentInfo = pathInfo.getSegmentInfo(viaPoint.segmentIdx.toLong())
                    val linkInfo = segmentInfo.getLinkInfo(0)
                    if (linkInfo != null) {
                        viaProjInfo.link_id = linkInfo.get64TopoID().toString()
                    }
                    viaProjectPoints.via_proj_info.add(viaProjInfo)
                }
                linkInfoParam.via_info.path_project_points.add(viaProjectPoints)
            }
        }
        tripShareParam.link_info.add(linkInfoParam)



        Timber.i("uploadTripShareData() tripShareParam = ${gson.toJson(tripShareParam)} ")
        isRequest = true
        val requestId: Long = mAosController.sendReqDriveReport(tripShareParam, onListener)
        Timber.i("uploadTripShareData() requestId: $requestId")
    }

    /**
     * 分享二维码请求回调
     */
    private val onListener: ICallBackDriveReportUpload = ICallBackDriveReportUpload {
        Timber.i("ICallBackDriveReportUpload onListener is called rsp = ${gson.toJson(it)} ")
        isRequest = false
        _naviTripUrlLd.postValue(if (TextUtils.isEmpty(it.url)) "" else it.url)
        mTripShareParam?.let { param ->
            if (it.id != param.id) {
                param.id = it.id
//                if (isNaving) {
//                    startScheduleUpload()
            }
        }
//        }


    }

    /**
     * 短信分享
     */
    fun sendSmsTripShare(phone: String) {
        if (!mNetWorkManager.isNetworkConnected()) {
            setToastTip(application.getString(R.string.sv_common_network_anomaly_please_try_again))
            return
        }
        if (TextUtils.isEmpty(mTripShareParam!!.id)) {
            Timber.i("sendSmsTripShare() is null ")
            reqTripUrl()
            return
        }
        Timber.i("sendSmsTripShare() is called")
        val javaRequest = GDriveReportSmsRequestParam()
        javaRequest.id = mTripShareParam!!.id
        javaRequest.phoneNumber = phone
        val requestId: Long = mAosController.sendReqDriveReportSms(javaRequest, onSmsListener)
        Timber.i("uploadTripShareData() requestId: $requestId")
    }

    private val onSmsListener: ICallBackDriveReportSms = ICallBackDriveReportSms {
        Timber.i("ICallBackDriveReportSms onListener is called rsp = ${gson.toJson(it)} ")
        if (it.code == 1) {
            setToastTip(application.getString(R.string.sv_route_the_itinerary_sharing_was_successful))
        }
    }


    /**
     * Toast信息
     */
    fun setToastTip(tip: String) = setToast.postValue(tip)
}