package com.desaysv.psmap.model.business

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.location.Location
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.layer.model.BizCustomLineInfo
import com.autonavi.gbl.layer.model.BizCustomPointInfo
import com.autonavi.gbl.layer.model.BizCustomTypeLine
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autonavi.gbl.layer.model.BizGpsPointType
import com.autonavi.gbl.map.adapter.MapSurfaceView
import com.autonavi.gbl.map.model.PreviewParam
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.DrivingLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.location.listener.LocationListener
import com.autosdk.bussiness.manager.SDKManager
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.widget.search.util.SearchMapUtil
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.CecuPushDrivingAppInfo.CECUPushDrivingAppInfo
import com.desaysv.psmap.model.bean.CecuPushDrivingAppInfo.EnumModMapperState
import com.desaysv.psmap.model.bean.CecuPushDrivingAppInfo.EnumModState
import com.dji.navigation.AdasSupportBusiness
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.zyt.autoivi.sdk.AutoDataManager
import com.zyt.autoivi.sdk.IAutoDataCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 智驾模块业务类
 */
@Singleton
class SmartDriveBusiness @Inject constructor(
    private val application: Application,
    private val layerController: LayerController,
    private val mMapController: MapController,
    private val mapBusiness: MapBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val mSearchBusiness: SearchBusiness,
    private val mAdasSupportBusiness: AdasSupportBusiness,
    private val iCarInfoProxy: ICarInfoProxy,
    private val gson: Gson
) : LocationListener {
    private val smartDriveBusinessScope = CoroutineScope(Dispatchers.IO + Job())
    private val customLayer: CustomLayer by lazy {
        layerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //导航引导图层
    private val mDrivingLayer: DrivingLayer? by lazy {
        layerController.getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    private var isUpdateGpsPointsInfo = false
    private var isShowGpsPoints = false
    private var mLastCustomPoint: Coord3DDouble? = null
    var customLines: ArrayList<BizCustomLineInfo> = ArrayList()
    var customPoints: ArrayList<BizCustomPointInfo> = ArrayList()
    var pointAll: ArrayList<Coord3DDouble> = ArrayList()
    var points: ArrayList<Coord3DDouble> = ArrayList()
    var line: BizCustomLineInfo? = null
    var customPoint: Coord3DDouble? = null
    var startPoiName: String = ""
    var startPoiDistance: Int = 0
    var endPoiName: String = ""
    var endPoiDistance: Int = 0

    private var thread: HandlerThread? = null
    private var mScreenBitmap: Bitmap? = null
    private var mResizedBitmap: Bitmap? = null


    private var isModMapperMapTraining = false //记忆行车绘制路线状态 true：modMappingStateValue = 0x3
    private var isMolOper = false //记忆行车回放路线状态 true: mod_state = 0x2: 定位就绪
    private var modMapperMapId = 0  //建图完成地图id
    private var molOperMapId = 0  //回放地图id

    //GPS状态
    /*private val _modMappingStateValue = MutableLiveData(EnumModMapperState.MOD_MAPPER_OFF_VALUE)
    val modMappingStateValue: LiveData<Boolean> = _gpsState*/

    fun isModMapperMapTraining(): Boolean {
        Timber.i("isModMapperMapTraining : $isModMapperMapTraining")
        return isModMapperMapTraining
    }

    /**
     * 初始智驾业务
     */
    fun init() {
        Timber.i(" init is called")
    }

    override fun onLocationChange(location: Location) {
        if (isUpdateGpsPointsInfo) {
            smartDriveBusinessScope.launch {
                customLines.clear()
                points.clear()
                line = BizCustomLineInfo()
                customPoint = Coord3DDouble(location.longitude, location.latitude, 0.0)
                mLastCustomPoint?.also {
                    points.add(it)
                }
//                if (mLastCustomPoint != null) {
//                    points.add(mLastCustomPoint)
//                }
                customPoint?.also {
                    points.add(it)
                    pointAll.add(it)
                }
//                points.add(customPoint!!)
//                pointAll.add(customPoint!!)
                if (isShowGpsPoints) {
                    val isUpdate: Boolean
                    if (pointAll.size % 100 == 0) {
                        isUpdate = true
                        line!!.mVecPoints.addAll(pointAll)
                    } else {
                        isUpdate = false
                        line!!.mVecPoints.addAll(points)
                    }
                    //                    LogUtils.d("xxxxxxxxxxx " + isUpdate);
                    customLines.add(line!!)
                    if (isUpdate) {
                        customLayer.bizCustomControl.updateCustomLine(
                            customLines,
                            BizCustomTypeLine.BizCustomTypeLine2
                        )
                    } else {
                        customLayer.bizCustomControl.addCustomLine(
                            customLines,
                            BizCustomTypeLine.BizCustomTypeLine2
                        )
                    }
                }
                mLastCustomPoint = customPoint
            }
        }
    }

    fun addLocInfoUpdate() {
        Timber.i("addLocInfoUpdate")
        pointAll.clear()
        customLines.clear()
        customPoints.clear()
        startPoiName = ""
        startPoiDistance = 0
        endPoiName = ""
        endPoiDistance = 0

        LocationController.getInstance().addLocInfoUpdate(this)
    }

    fun removeLocInfoUpdate() {
        Timber.i("removeLocInfoUpdate")
        LocationController.getInstance().removeLocInfoUpdate()
    }

    suspend fun removeCustomSmartDriveLayerItems(isSave: Boolean) {
        Timber.i("removeCustomSmartDriveLayerItems isSave = $isSave")
        setUpdateGpsPointsInfo(false)
        isShowGpsPoints = false
        mLastCustomPoint = null
        if (isSave) {
            showGpsPointsCustomLayer()
        } else {
            pointAll.clear()
            customPoints.clear()
            customLines.clear()
            removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine2.toLong())
            removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint10.toLong())
        }
    }

    fun removeGpsPointsAndBackToCar() {
        Timber.i("removeGpsPointsAndBackToCar ")
        removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine2.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint10.toLong())
        //        MapCenterUtils.getInstance().setMapModeNavi();//切换车标视图中心
        if (!mapBusiness.isInNaviFragment) {
            mapBusiness.backCurrentCarPosition()
        }
    }

    @SuppressLint("CheckResult")
    suspend fun setUpdateGpsPointsInfo(isUpdate: Boolean) {
        Timber.i("setUpdateGpsPointsInfo isUpdate = $isUpdate")

        isUpdateGpsPointsInfo = isUpdate
        val location = SDKManager.getInstance().locController.lastLocation
        //逆地理搜索查询当前位置
        val result =
            mSearchBusiness.nearestSearch(
                poi = POIFactory.createPOI(
                    null,
                    GeoPoint(
                        location.longitude,
                        location.latitude
                    )
                )
            )
        if (result.status == Status.SUCCESS) {
            val poiList = SearchCommonUtils.invertOrderList(result.data?.poi_list)
            poiList?.takeIf { it.isNotEmpty() }?.let { poiList ->
                Timber.i("poiList is called tollLaneList = $poiList")
                /**
                 * 通知智驾记忆泊车POI名称
                 */
                if (isUpdate) {
                    startPoiName = poiList[0].name
                } else {
                    endPoiName = poiList[0].name
                }
            } ?: run {
                // 如果 result.data 为 null，发送空 POI 名称
                if (isUpdate) {
                    startPoiName = ""
                } else {
                    endPoiName = ""
                }
            }
        } else {
            Timber.i("setUpdateGpsPointsInfo nearestSearch result is null")
            if (isUpdate) {
                startPoiName = ""
            } else {
                endPoiName = ""
            }
        }

        if (isUpdate) {
            customPoints.clear()
            val pointStart = BizCustomPointInfo()
            pointStart.mPos3D = Coord3DDouble(location.longitude, location.latitude, 0.0)
            pointStart.type = BizGpsPointType.GPS_POINT_START
            customPoints.add(pointStart)
            Timber.i("setUpdateGpsPointsInfo pointStart = ${gson.toJson(pointStart)}")
        } else {
            val pointEnd = BizCustomPointInfo()
            pointEnd.mPos3D = Coord3DDouble(location.longitude, location.latitude, 0.0)
            pointEnd.type = BizGpsPointType.GPS_POINT_END
            customPoints.add(pointEnd)
            Timber.i("setUpdateGpsPointsInfo pointEnd = ${gson.toJson(pointEnd)}")
        }
        Timber.i("setUpdateGpsPointsInfo customPoints = ${gson.toJson(customPoints)}")

    }

    fun setShowGpsPoints(isShow: Boolean) {
        Timber.i("setShowGpsPoints isShow = $isShow isUpdateGpsPointsInfo = $isUpdateGpsPointsInfo")
        if (mapBusiness.isInNaviFragment && isShow) {
            return
        }
        if (isUpdateGpsPointsInfo) {
            removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine2.toLong())
            removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint10.toLong())
            if (isShow) {
                showGpsPointsCustomLayer()
            }
            isShowGpsPoints = isShow
        }
    }

    private fun showGpsPointsCustomLayer() {
        Timber.i("showGpsPointsCustomLayer isUpdateGpsPointsInfo = $isUpdateGpsPointsInfo customPoints:${gson.toJson(customPoints)}")
        customLines.clear()
        line = BizCustomLineInfo()
        line!!.mVecPoints.addAll(pointAll)
        customLines.add(line!!)

        customLayer.bizCustomControl.updateCustomLine(customLines, BizCustomTypeLine.BizCustomTypeLine2)
        if (!isUpdateGpsPointsInfo) {
            mapBusiness.setFollowMode(false, true)
            customLayer.bizCustomControl.updateCustomPoint(
                customPoints,
                BizCustomTypePoint.BizCustomTypePoint10
            )
            showPreview(pointAll)
            /*if (mapBusiness.isInNaviFragment) {
                mNaviBusiness.setEagleVisible(isTrue = false)
                //全览的时候要关闭自动比例尺功能
                mDrivingLayer?.openDynamicLevel(false, DynamicLevelType.DynamicLevelGuide) //动态比例尺
            }*/

            val mapSurfaceView: MapSurfaceView = mMapController.mapSurfaceViewArrayList[0]

            smartDriveBusinessScope.launch {
                thread = HandlerThread("SaveBitmap")
                thread?.start()
                mScreenBitmap = Bitmap.createBitmap(
                    mapSurfaceView.width,
                    mapSurfaceView.height,
                    Bitmap.Config.ARGB_8888
                )
                mResizedBitmap = Bitmap.createBitmap(
                    mapSurfaceView.height,
                    mapSurfaceView.height,
                    Bitmap.Config.ARGB_8888
                )

                delay(1000)
                Timber.i("showGpsPointsCustomLayer surface isValid : " + mapSurfaceView.holder.surface.isValid)
                if (mapSurfaceView.holder.surface.isValid) {
                    PixelCopy.request(
                        mapSurfaceView, mScreenBitmap!!,
                        { copyResult ->
                            if (PixelCopy.SUCCESS == copyResult) {
                                onSuccessCallback(
                                    mapSurfaceView,
                                    mScreenBitmap!!, mResizedBitmap!!,
                                    modMapperMapId.toString()
                                )
                            } else {
                                Timber.i("FAILED = $copyResult")
                                // onErrorCallback()
                            }
                        }, Handler(thread!!.getLooper())
                    )
                }

            }
        }
    }

    fun showGpsPoints() {
//        setUpdateGpsPointsInfo(true)
        if (mapBusiness.isInNaviFragment) {
            setShowGpsPoints(false)
            /*TaskManager.post(() -> iMapRouteNaviProvider.stopNavi());
            CustomToast.showCustomToast("记忆行车已开启，路线引导功能已退出");*/
        } else {
            setShowGpsPoints(true)
        }
    }

    fun removeAllLayerItems() {
        customLayer.bizCustomControl.clearAllItems()
    }

    fun removeAllLayerItems(bizType: Long) {
        customLayer.bizCustomControl.clearAllItems(bizType)
    }

    fun registerAutoDataCallback() {
        AutoDataManager.registerCallback(autoDataCallback)
    }

    fun unregisterAutoDataCallback() {
        AutoDataManager.unregisterCallback(autoDataCallback)
    }

    private val autoDataCallback = object : IAutoDataCallback {
        override fun onDrivingAppInfoUpdate(data: ByteArray?) {
            try {
                val parser = CECUPushDrivingAppInfo.parseFrom(data)
                modMapperMapId = parser.modMapperMapId
                molOperMapId = parser.modUserSelectMapId
                Timber.i("autoDataCallback onDrivingAppInfoUpdate modState = ${parser.modStateValue} isMolOper = $isMolOper molOperMapId = $molOperMapId")
                Timber.i("autoDataCallback onDrivingAppInfoUpdate modMappingState = ${parser.modMappingStateValue} isModMapperMapTraining = $isModMapperMapTraining modMapperMapId = $modMapperMapId parser = $parser")
                if (!isModMapperMapTraining) {
                    if (parser.modMappingStateValue == EnumModMapperState.MOD_MAPPER_MAP_TRAINING_VALUE) {
                        isModMapperMapTraining = true
                        smartDriveBusinessScope.launch {
                            addLocInfoUpdate()
                            setUpdateGpsPointsInfo(true)
                        }
                    }
                } else {
                    if (parser.modMappingStateValue == EnumModMapperState.MOD_MAPPER_FINISH_VALUE) { //结束绘制
                        isModMapperMapTraining = false
                        smartDriveBusinessScope.launch {
                            removeLocInfoUpdate()
                            removeCustomSmartDriveLayerItems(parser.modMappingStateValue == EnumModMapperState.MOD_MAPPER_MAP_SAVING_VALUE) //保存路线 并发送给智驾
                        }
                    }
                }

                if (!isMolOper) {
                    if (parser.modStateValue == EnumModState.MOD_LOCATION_VALUE || parser.modStateValue == EnumModState.MOD_WAIT_VALUE || parser.modStateValue == EnumModState.MOD_ACTIVE_VALUE) {
                        isMolOper = true
                        replayGpsPoints()
                    }
                } else {
                    if (parser.modStateValue == EnumModState.MOD_OFF_VALUE || parser.modStateValue == EnumModState.MOD_NOT_READY_VALUE || parser.modStateValue == EnumModState.MOD_ERROR_VALUE) {
                        isMolOper = false
                        removeGpsPointsAndBackToCar()
                    }
                }


            } catch (e: Exception) {
                Timber.e("onDrivingAppInfoUpdate Exception:${e.message}")
            }
        }

        override fun onRouteDelete(mapID: Int) {
            Timber.i("autoDataCallback onRouteDelete mapID = $mapID")
        }

    }

    /**
     * 显示全览
     *
     */
    fun showPreview(pointList: List<Coord3DDouble>) {
        Timber.i("showPreview getScreenStatus = ${iCarInfoProxy.getScreenStatus().value}")
        SearchMapUtil.getSmartDriveBound(pointList)?.let { mapRect ->
            val previewParam = PreviewParam().apply {
                leftOfMap = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1100 else R.dimen.sv_dimen_0
                )
                topOfMap = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_370)
                screenLeft = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1240 else R.dimen.sv_dimen_680
                )
                screenTop = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_200)
                screenRight = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_180)
                screenBottom = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_100)
                bUseRect = true
                mapBound = mapRect
            }
            mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                ?.showPreview(previewParam, true, 500, -1)
        }
    }

    private fun onSuccessCallback(mapSurfaceView: MapSurfaceView, screenBitmap: Bitmap, resizedBitmap: Bitmap, name: String) {
        Timber.i("onSuccessCallback mapSurfaceView height = ${mapSurfaceView.height} width = ${mapSurfaceView.width}")
        val screenCanvas = Canvas(resizedBitmap)
        val srcRect = Rect(
            CommonUtils.getAutoDimenValue(
                application,
                if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1240 else R.dimen.sv_dimen_680
            ),
            0,
            mapSurfaceView.width,
            mapSurfaceView.height
        ) // 指定原始Bitmap中要裁剪的矩形区域
        val dstRect = Rect(
            0,
            0,
            mapSurfaceView.height,
            mapSurfaceView.height
        ) // 目标矩形区域
        screenCanvas.drawBitmap(screenBitmap, srcRect, dstRect, Paint())

        val path: String = BaseConstant.NAV_MAP_ADAS + name + "/"
        Timber.i("bitmap = $screenBitmap name = $name path = $path")

        val director = File(path)
        if (!director.exists()) {
            director.mkdirs()
        }
        val f = File(path + "thumbnail.jpg")
        val pos = File(path + "pose.txt")
        Timber.i("absolutePath ${f.absolutePath}")
        var fOut: FileOutputStream? = null
        var fOutPos: FileOutputStream? = null
        try {
            fOut = FileOutputStream(f)
            fOutPos = FileOutputStream(pos)

            if (fOut == null || fOutPos == null) {
                Timber.i("file path is not exist")
                return
            }
            mResizedBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            //        String jsonString = GsonUtils.toJson(pointAll);
            fOutPos.write(gson.toJson(pointAll).toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            fOut?.flush()
            fOutPos?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {

            fOut?.close()
            fOutPos?.close()
            thread?.quitSafely()
            thread = null
            mScreenBitmap?.recycle()
            mScreenBitmap = null
            mResizedBitmap?.recycle()
            mResizedBitmap = null
            pointAll.clear()
            customPoints.clear()
            customLines.clear()
            //保存图片之后发送广播
            /*val intent = Intent("com.dji.autoivi.mod_thumbnail_save_success")
            intent.setPackage("com.dji.autoivi")
            getContext().sendBroadcast(intent)*/
            mAdasSupportBusiness.setAndSendPOINameInfo(startPoiName, endPoiName)

            smartDriveBusinessScope.launch {
                delay(3000)
                removeGpsPointsAndBackToCar()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun replayGpsPoints() {
        replayLinePoint(true)
    }

    private fun replayLinePoint(isShowPreview: Boolean) {
        Timber.i("replayGpsPoints molOperMapId = $molOperMapId  isShowPreview =  $isShowPreview")
        val pos = CustomFileUtils.getGpsPointsFile(molOperMapId.toString())
        if (pos == null) {
            Timber.i("replayGpsPoints pos is null ")
        }
        try {
            pointAll = Gson().fromJson(pos, object : TypeToken<List<Coord3DDouble?>?>() {
            }.type)
//            Timber.i("replayGpsPoints  pointAll = ${gson.toJson(pointAll)}")
            if (pointAll == null || pointAll.size == 0) {
                return
            }

            mapBusiness.backToMapHome.postValue(true) //回到地图主图--在导航/模拟导航需要退出

            smartDriveBusinessScope.launch {
                delay(500)
                if (isMolOper) {
                    Timber.i("replayLinePoint is called ")
                    customLines.clear()
                    customPoints.clear()
                    line = BizCustomLineInfo()
                    line!!.mVecPoints.addAll(pointAll)
                    customLines.add(line!!)

                    val pointStart = BizCustomPointInfo()
                    pointStart.mPos3D = pointAll[0]
                    pointStart.type = BizGpsPointType.GPS_POINT_START
                    customPoints.add(pointStart)

                    val pointEnd = BizCustomPointInfo()
                    pointEnd.mPos3D = pointAll[pointAll.size - 1]
                    pointEnd.type = BizGpsPointType.GPS_POINT_END
                    customPoints.add(pointEnd)

                    customLayer.bizCustomControl.updateCustomLine(customLines, BizCustomTypeLine.BizCustomTypeLine2)
                    customLayer.bizCustomControl.updateCustomPoint(customPoints, BizCustomTypePoint.BizCustomTypePoint10)

                    if (isShowPreview) {
                        mapBusiness.setFollowMode(false, true)
                        showPreview(pointAll)
                        mNaviBusiness.resetBackToCarTimer()
                    }
                    pointAll.clear()
                    customLines.clear()
                    customPoints.clear()
                }
            }

        } catch (e: JsonSyntaxException) {
            Timber.e("Exception = $e")
        }
    }

}