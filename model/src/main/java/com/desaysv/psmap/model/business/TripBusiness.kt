package com.desaysv.psmap.model.business

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.layer.model.BizCustomPointInfo
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autonavi.gbl.layer.model.BizGpsPointType
import com.autonavi.gbl.layer.model.ColorSpeedPair
import com.autonavi.gbl.layer.model.RainbowLinePoint
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.model.PreviewParam
import com.autonavi.gbl.user.model.BehaviorDataType
import com.autonavi.gbl.user.syncsdk.model.SyncEventType
import com.autonavi.gbl.user.syncsdk.model.SyncEventType.SyncSdkEventSyncEnd
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.user.syncsdk.observer.ISyncSDKServiceObserver
import com.autonavi.gbl.user.usertrack.model.BehaviorFileType
import com.autonavi.gbl.user.usertrack.model.FootprintDeleteRecordResult
import com.autonavi.gbl.user.usertrack.model.FootprintNaviRecordResult
import com.autonavi.gbl.user.usertrack.model.FootprintSummaryResult
import com.autonavi.gbl.user.usertrack.model.FootprintSwitchResult
import com.autonavi.gbl.user.usertrack.model.GpsTrackDepthInfo
import com.autonavi.gbl.user.usertrack.model.GpsTrackPoint
import com.autonavi.gbl.user.usertrack.observer.IUserTrackObserver
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.account.UserTrackController
import com.autosdk.bussiness.account.bean.TrackItemBean
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.layer.UserBehaviorLayer
import com.autosdk.bussiness.manager.SDKManager
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.Observer.MapViewObserver
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.widget.route.utils.RectUtils
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.NavigationUtil
import com.desaysv.psmap.model.R
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

/**
 * 行程模块
 */
@Singleton
class TripBusiness @Inject constructor(
    private val userBusiness: UserBusiness,
    private val mapBusiness: MapBusiness,
    private val mapController: MapController,
    private val layerController: LayerController,
    private val mNaviBusiness: NaviBusiness,
    private val userTrackController: UserTrackController,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) {
    private val customLayer: CustomLayer by lazy {
        layerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }
    private val tripScope = CoroutineScope(Dispatchers.Default + Job())
    var trackItem: TrackItemBean? = null
    var naviTrackItem: TrackItemBean? = null
    private var mapLayer: MapLayer? = null
    private var mapRect: Rect? = null
    private val mPoiLabelClickedObserver: PoiLabelClickedObserver = PoiLabelClickedObserver()

    private class PoiLabelClickedObserver : MapViewObserver()

    val showToast = MutableLiveData<String>()
    val finishPage = MutableLiveData<Boolean>()
    val time = MutableLiveData<String>() //时间 比如：2023/07/17  15:33·
    val type = MutableLiveData<String>() //类型 比如：导航
    val startName = MutableLiveData<String>() //起点名称 比：钱氏饭店
    val endName = MutableLiveData<String>() //终点名称 比：万达广场
    val averageSpeed = MutableLiveData<String>() //平均速度 比：69km/h
    val drivingDuration = MutableLiveData<String>() //驾驶时长 比：00:04:50
    val allTrip = MutableLiveData<String>() //驾驶里程 比：13.8公里
    val maximumSpeed = MutableLiveData<String>() //最快速度 比：13.8公里
    val addNewData = MutableLiveData<ArrayList<TrackItemBean>>()
    val isNotEmptyData = MutableLiveData<Boolean>()
    val stopSyncRefresh = MutableLiveData<Boolean>()
    val vecColorSpeedPairList = ArrayList<ColorSpeedPair>().apply {
        //设定彩虹线速度区间到颜色值的映射关系：[0,20)对应颜色"FFAF0805"
        val mColorSpeedPair = ColorSpeedPair()
        mColorSpeedPair.mMinspeed = 0
        mColorSpeedPair.mMaxspeed = 20
        mColorSpeedPair.mStrColorvalue = "FFAF0805"
        add(mColorSpeedPair)

        //设定彩虹线速度区间到颜色值的映射关系：[20,40)对应颜色"FFE76215"
        val mColorSpeedPair1 = ColorSpeedPair()
        mColorSpeedPair1.mMinspeed = 20
        mColorSpeedPair1.mMaxspeed = 40
        mColorSpeedPair1.mStrColorvalue = "FFE76215"
        add(mColorSpeedPair1)

        //设定彩虹线速度区间到颜色值的映射关系：[40,60)对应颜色"FFD3DA3D"
        val mColorSpeedPair2 = ColorSpeedPair()
        mColorSpeedPair2.mMinspeed = 40 //单位：公里/小时
        mColorSpeedPair2.mMaxspeed = 60 //单位：公里/小时
        mColorSpeedPair2.mStrColorvalue = "FFD3DA3D" //格式：ARGB
        add(mColorSpeedPair2)

        //设定彩虹线速度区间到颜色值的映射关系：[60,100)对应颜色"FF69EECF"
        val mColorSpeedPair3 = ColorSpeedPair()
        mColorSpeedPair3.mMinspeed = 60 //单位：公里/小时
        mColorSpeedPair3.mMaxspeed = 100 //单位：公里/小时
        mColorSpeedPair3.mStrColorvalue = "FF69EECF" //格式：ARGB
        add(mColorSpeedPair3)

        //设定彩虹线速度区间到颜色值的映射关系：[100,1000000000)对应颜色"FF068364"
        val mColorSpeedPair4 = ColorSpeedPair()
        mColorSpeedPair4.mMinspeed = 100 //单位：公里/小时
        mColorSpeedPair4.mMaxspeed = 1000000000 //单位：公里/小时
        mColorSpeedPair4.mStrColorvalue = "FF068364" //格式：ARGB
        add(mColorSpeedPair4)
    }
    var customPoints: ArrayList<BizCustomPointInfo> = ArrayList()


    fun registerISyncSDKServiceObserver() {
        userBusiness.registerISyncSDKServiceObserver(iSyncSdkServiceObserver)
    }

    fun unregisterISyncSDKServiceObserver() {
        userBusiness.unregisterISyncSDKServiceObserver(iSyncSdkServiceObserver)
    }

    fun refreshTripData() {
        val ids = userBusiness.getBehaviorDataIds(BehaviorDataType.BehaviorTypeTrailDriveForAuto)
        val trackItemBeans = ArrayList<TrackItemBean>()
        var i = 0
        while (ids != null && i < ids.size) {
            val trackItemBean =
                gson.fromJson(userBusiness.getBehaviorDataById(BehaviorDataType.BehaviorTypeTrailDriveForAuto, ids[i]), TrackItemBean::class.java)
            trackItemBeans.add(trackItemBean)
            i++
        }
        val isNotEmpty = trackItemBeans.size > 0
        Timber.d("MyTripFragment refreshTripData size:${trackItemBeans.size}")
        if (isNotEmpty) {
            addNewData.postValue(trackItemBeans)
        }
        isNotEmptyData.postValue(isNotEmpty)
    }

    private var iSyncSdkServiceObserver = ISyncSDKServiceObserver { i: Int, _: Int ->
        tripScope.launch(Dispatchers.IO) {
            Timber.d(" observer SyncSdkEventSyncEnd")
            if (i == SyncSdkEventSyncEnd || i == SyncEventType.SyncSdkEventBackupEnd) {
                refreshTripData()
                stopSyncRefresh.postValue(true)
            }
        }
    }

    fun getTrackItemBean(): TrackItemBean? {
        return trackItem
    }

    fun setTrackItemBean(trackItemBean: TrackItemBean?) {
        this.trackItem = trackItemBean
        setLayoutData()
    }

    fun setRect(mapRect: Rect?) {
        this.mapRect = mapRect
    }

    //主图
    private val mainMapView: MapView? by lazy {
        mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //用户行为图层UserBehavior
    private val userBehaviorLayer: UserBehaviorLayer? by lazy {
        layerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    @SuppressLint("SimpleDateFormat")
    private fun setLayoutData() {
        trackItem?.let {
            time.postValue(
                SimpleDateFormat("yyyy/MM/dd HH:mm").format(Date(it.updateTime * 1000L)) + "·"
            )
            type.postValue(
                if (it.rideRunType == 0) context.resources.getString(R.string.sv_setting_cruise_trip_txt) else context.resources.getString(
                    R.string.sv_setting_navi_trip
                )
            )
            val startPoiName = it.startPoiName
            val endPoiName = it.endPoiName
            startName.postValue(
                if (TextUtils.isEmpty(startPoiName) || TextUtils.equals(
                        startPoiName,
                        context.resources.getString(R.string.sv_setting_map_select_point)
                    )
                )
                    context.resources.getString(R.string.sv_setting_map_point) else startPoiName
            )
            endName.postValue(
                if (TextUtils.isEmpty(endPoiName) || TextUtils.equals(endPoiName, context.resources.getString(R.string.sv_setting_map_select_point)))
                    context.resources.getString(R.string.sv_setting_map_point) else endPoiName
            )

            val maxSpeed: Double = it.maxSpeed
            val hour: Float = it.timeInterval.toFloat() / 3600f
            val distance: Float = it.runDistance.toFloat() / 1000f
            val b = BigDecimal((distance / hour).toDouble())
            var result = b.setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()
            if (result > maxSpeed) {
                result = maxSpeed
            }
            averageSpeed.postValue(result.toInt().toString() + "km/h")
            drivingDuration.postValue(NavigationUtil.formatTime(it.timeInterval))
            val totalDistance: Int = it.runDistance
            allTrip.postValue(
                if (totalDistance == 0) {
                    "0" + context.resources.getString(com.desaysv.psmap.base.R.string.sv_common_km_english)
                } else {
                    val km = floor(totalDistance / 1000.0 * 10) / 10
                    km.toString() + context.resources.getString(com.desaysv.psmap.base.R.string.sv_common_km_english)
                }
            )
            maximumSpeed.postValue(maxSpeed.toInt().toString() + "km/h")
        }
    }

    fun onDestroyView() {
        mapLayer?.setFollowMode(true)
        mapLayer?.setCarVisible(true)
    }

    fun onCleared(isBackCurrentCarPosition: Boolean = true) {
        Timber.d("TripBusiness onCleared is called")
        layerController.removeClickObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, iLayerClickObserver)
        mapController.addMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mPoiLabelClickedObserver)
        layerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).clearGpsTrack()
        mapLayer?.setCarVisible(true)
        mainMapView?.exitPreview(true)
        if (isBackCurrentCarPosition) {
            mapBusiness.backCurrentCarPosition()
        }
        userBehaviorLayer?.clearAllItems()
        customLayer.bizCustomControl.clearAllItems(BizCustomTypePoint.BizCustomTypePoint10.toLong())
        trackItem = null
        naviTrackItem = null
    }

    fun initData(isObtainGpsTrackInfo: Boolean = true) {
        Timber.d("TripBusiness initData is called")
        layerController.addClickObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, iLayerClickObserver)
        mapController.addMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mPoiLabelClickedObserver)
        mapLayer = SDKManager.getInstance().layerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
        mapBusiness.setFollowMode(follow = false, bPreview = true)
        mapLayer?.setCarVisible(false)
        userBehaviorLayer?.clearAllItems()
        if (isObtainGpsTrackInfo) {
            obtainGpsTrackInfo()
        }
    }

    fun setUserTrackListener() {
        userTrackController.setUserTrackListener(iUserTrackObserver)
    }

    fun removeUserTrackListener() {
        userTrackController.removeUserTrackListener()
    }

    private fun obtainGpsTrackInfo() {
        trackItem?.let {
            userTrackController.getFilePath(BehaviorDataType.BehaviorTypeTrailDriveForAuto, it.id, BehaviorFileType.BehaviorFileTrail)
            userTrackController.obtainGpsTrackInfo(AutoConstant.SYNC_DIR + "403", it.trackFileName)
        }
    }

    fun deleteById(id: String?): Int {
        val code = userTrackController.delBehaviorData(BehaviorDataType.BehaviorTypeTrailDriveForAuto, id, SyncMode.SyncModeNow)
        if (code == Service.ErrorCodeOK) {
            showToast.postValue("行程已删除")
            finishPage.postValue(true)
        } else {
            showToast.postValue("行程删除失败")
        }
        return code
    }

    //更新彩虹线
    fun updateRainbowLine(trackPoints: ArrayList<GpsTrackPoint>?) {
        trackPoints?.apply {
            val vecRainbowLinePoint = ArrayList<RainbowLinePoint>()

            for (gpsTrackPoint in trackPoints) {
                val rainbowLinePoint = RainbowLinePoint()
                rainbowLinePoint.f64Longitude = gpsTrackPoint.f64Longitude // 经度
                rainbowLinePoint.f64Latitude = gpsTrackPoint.f64Latitude // 纬度
                rainbowLinePoint.f32Speed = gpsTrackPoint.f32Speed // 速度（公里/小时）
                vecRainbowLinePoint.add(rainbowLinePoint)
            }

            layerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).updateRainbowLine(vecColorSpeedPairList, vecRainbowLinePoint)
        }
    }

    fun createCustomPoint(longitude: Double, latitude: Double, type: Int): BizCustomPointInfo {
        return BizCustomPointInfo().apply {
            mPos3D = Coord3DDouble(longitude, latitude, 0.0)
            this.type = type
        }
    }

    private var iUserTrackObserver: IUserTrackObserver = object : IUserTrackObserver {
        override fun onStartGpsTrack(i: Int, s: String, s1: String) {

        }

        override fun onCloseGpsTrack(i: Int, s: String, s1: String, gpsTrackDepthInfo: GpsTrackDepthInfo) {

        }

        override fun onGpsTrackDepInfo(i: Int, s: String, s1: String, gpsTrackDepthInfo: GpsTrackDepthInfo) {
            if (i == Service.ErrorCodeOK) {
                tripScope.launch {
                    naviTrackItem?.let {
                        Timber.d("onGpsTrackDepInfo naviTrackItem is called")
                        if (!gpsTrackDepthInfo.trackPoints.isNullOrEmpty()) {
                            Timber.i("getTripReportTrack trackPoints size:${gpsTrackDepthInfo.trackPoints.size}")
                            updateRainbowLine(gpsTrackDepthInfo.trackPoints)
                            customPoints.clear()
                            val pointStart = createCustomPoint(
                                gpsTrackDepthInfo.trackPoints[0].f64Longitude,
                                gpsTrackDepthInfo.trackPoints[0].f64Latitude,
                                BizGpsPointType.GPS_POINT_START
                            )
                            customPoints.add(pointStart)

                            val pointEnd = createCustomPoint(
                                gpsTrackDepthInfo.trackPoints.last().f64Longitude,
                                gpsTrackDepthInfo.trackPoints.last().f64Latitude,
                                BizGpsPointType.GPS_POINT_END
                            )
                            customPoints.add(pointEnd)

                            customLayer.bizCustomControl.updateCustomPoint(customPoints, BizCustomTypePoint.BizCustomTypePoint10)
                            showPreview(gpsTrackDepthInfo)
                        }
                    }

                    trackItem?.let {
                        Timber.d("onGpsTrackDepInfo trackItem is called")
                        layerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).updateGpsTrack(gpsTrackDepthInfo)
                        showPreview(gpsTrackDepthInfo)
                    }
                }
            }
        }

        override fun notify(i: Int, i1: Int) {
        }

        override fun onFootprintSwitch(footprintSwitchResult: FootprintSwitchResult) {
        }

        override fun onFootprintSummary(footprintSummaryResult: FootprintSummaryResult) {
        }

        override fun onFootprintNaviRecordList(footprintNaviRecordResult: FootprintNaviRecordResult) {
        }

        override fun onFootprintDeleteRecord(footprintDeleteRecordResult: FootprintDeleteRecordResult) {
        }
    }

    private var iLayerClickObserver: ILayerClickObserver = object : ILayerClickObserver {
        override fun onBeforeNotifyClick(baseLayer: BaseLayer, layerItem: LayerItem, clickViewIdInfo: ClickViewIdInfo) {
        }

        override fun onNotifyClick(baseLayer: BaseLayer, layerItem: LayerItem, clickViewIdInfo: ClickViewIdInfo) {
        }

        override fun onAfterNotifyClick(baseLayer: BaseLayer, layerItem: LayerItem, clickViewIdInfo: ClickViewIdInfo) {
        }
    }

    /**
     * 显示预览
     */
    fun showPreview(gpsTrackDepthInfo: GpsTrackDepthInfo) {
        val mPreviewParam = PreviewParam()
        mPreviewParam.mapBound = RectUtils.getTrackBound(gpsTrackDepthInfo.trackPoints)
        mPreviewParam.leftOfMap = mapRect!!.left
        mPreviewParam.topOfMap = mapRect!!.top
        mPreviewParam.screenLeft = mapRect!!.left
        mPreviewParam.screenTop = mapRect!!.top
        mPreviewParam.screenRight = mapRect!!.right
        mPreviewParam.screenBottom = mapRect!!.bottom
        mPreviewParam.bUseRect = true
        mainMapView?.showPreview(mPreviewParam, true, 500, -1)
    }


    /*----------------------------导航模块 行程报告轨迹路线获取 start-----------------------------------*/

    /**
     * 获取行程报告轨迹路线
     */
    fun getTripReportTrack(): Boolean {
        Timber.i("getTripReportTrack is called")
        initData(isObtainGpsTrackInfo = false) //初始化数据
        val ids = userBusiness.getBehaviorDataIds(BehaviorDataType.BehaviorTypeTrailDriveForAuto)
        ids?.takeIf { it.isNotEmpty() }?.let { id ->
            // 通过id获取行程数据Json串
            val dataId = id[0] // 获取第一条行程数据
            naviTrackItem =
                gson.fromJson(userBusiness.getBehaviorDataById(BehaviorDataType.BehaviorTypeTrailDriveForAuto, dataId), TrackItemBean::class.java)
            naviTrackItem?.let {
                Timber.i("getTripReportTrack  trackFileName = ${it.trackFileName} , mNaviBusiness.trackFileName = ${mNaviBusiness.trackFileName}")
                if (TextUtils.equals(it.trackFileName, mNaviBusiness.trackFileName)) {
                    userTrackController.obtainGpsTrackInfo(AutoConstant.SYNC_DIR + "403", it.trackFileName)
                    return true
                }
                return false
            } ?: run {
                Timber.e("getTripReportTrack naviTrackItem is null")
                return false
            }
        } ?: run {
            Timber.e("getTripReportTrack ids is null or empty")
            return false
        }


    }

    /*----------------------------导航模块 行程报告轨迹路线获取 end-----------------------------------*/


}