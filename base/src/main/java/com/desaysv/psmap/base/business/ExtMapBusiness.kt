package com.desaysv.psmap.base.business

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.layer.model.OpenLayerID
import com.autonavi.gbl.map.layer.model.ScaleInfo
import com.autonavi.gbl.map.model.MapBusinessDataType
import com.autonavi.gbl.map.model.MapParameter
import com.autonavi.gbl.map.model.MapPoiCustomOperateType
import com.autonavi.gbl.map.model.MapPoiCustomType
import com.autonavi.gbl.map.model.MapRenderMode
import com.autonavi.gbl.map.model.MapViewStateType
import com.autonavi.gbl.map.model.MapviewMode
import com.autonavi.gbl.map.model.MapviewModeParam
import com.autonavi.gbl.map.observer.IDeviceObserver
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.layer.DrivingLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.layer.RouteEndAreaLayer
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.MapController.EMapStyleStateType
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.widget.mapview.MapViewComponent
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.view.SDKMapSurfaceView
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.utils.BaseConstant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 扩展屏业务类
 */
@Singleton
class ExtMapBusiness @Inject constructor(
    private val mapController: MapController,
    private val layerController: LayerController,
    private val locationController: LocationController,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val engineerBusiness: EngineerBusiness,
    private val mapViewComponent: MapViewComponent,
) {

    lateinit var glMapSurfaces: SDKMapSurfaceView

    private val ext1MapView: MapView by lazy {
        mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_EX1)
    }

    private val ext1MapLayer: MapLayer by lazy {
        layerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1)
    }

    //导航引导图层
    val ex1DrivingLayer: DrivingLayer? by lazy {
        layerController.getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1)
    }

    //终点图层
    val ex1RouteEndAreaLayer: RouteEndAreaLayer? by lazy {
        layerController.getRouteEndAreaLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1)
    }

    private val _firstDeviceRender = MutableLiveData(false)
    val firstDeviceRender: LiveData<Boolean> = _firstDeviceRender

    private val deviceObserver = object : IDeviceObserver {
        override fun onDeviceRender(i: Int, i1: Int) {
            super.onDeviceRender(i, i1)
        }

        override fun onSurfaceChanged(deviceId: Int, width: Int, height: Int, colorBits: Int) {
            super.onSurfaceChanged(deviceId, width, height, colorBits)
            if (firstDeviceRender.value == false) {
                _firstDeviceRender.postValue(true)//地图图层准备好
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.EXSCREEN_FIRST_DRAW)
                AutoStatusAdapter.sendStatus(AutoStatus.FIRST_FRAME_DASHBOARD)
                Timber.i("ExtMapBusiness onSurfaceChanged")
            }
        }
    }

    fun initExt() {
        Timber.i("initExt")
        mapController.getMapDevice(ext1MapView).addDeviceObserver(deviceObserver)

        mapController.setMapStyle(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            NightModeGlobal.isNightMode(),
            EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NORMAL
        )
        mapController.setZoomLevel(SurfaceViewID.SURFACE_VIEW_ID_EX1, BaseConstant.Ex1_Zoom_Level_2D)
        mapController.setTmcVisible(SurfaceViewID.SURFACE_VIEW_ID_EX1, true)
        mapController.setBaseMapIconVisible(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            OpenLayerID.OpenLayerIDChargingStation,
            true
        )
        mapController.setBaseMapIconVisible(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            OpenLayerID.OpenLayerIDRouteTraffic,
            false
        )
        mapController.setBaseMapPOIVisible(SurfaceViewID.SURFACE_VIEW_ID_EX1, true)
        mapController.setBaseMapRoadNameVisible(SurfaceViewID.SURFACE_VIEW_ID_EX1, true)
        mapController.setBaseMapSample3DVisible(SurfaceViewID.SURFACE_VIEW_ID_EX1, false) //仪表3D建筑关

        ext1MapView.operatorBusiness.setMapViewState(MapViewStateType.MAP_VIEWSTATE_IS_SIMPLE3D_ON, false) //仪表简易三维关

        ext1MapView.operatorBusiness.setMapTextScale(1.0f) // 设置底图元素大小

        //设置纹理大小
        val mPointItemsScale = ScaleInfo()
        mPointItemsScale.bgScale = 1.0
        mPointItemsScale.bubbleScale = 1.0
        mPointItemsScale.poiScale = 1.0
        ext1MapView.layerMgr.setAllPointLayerItemsScale(mPointItemsScale)

        //高速行车中 隐藏 生活-生活服务 充电站 银行 生活-便利店 地点名-楼号 交通设施-地铁站 小区住宅 生活-餐饮 （包含酒店餐厅）区域名-商业场所
        //医疗卫生 汽车 汽车维修 区域名-公共常规
        val hideList: ArrayList<Int> = arrayListOf(
            MapPoiCustomType.POITYPE_LIFE_LIVING_SERVICE,
            MapPoiCustomType.POITYPE_PUBLIC_PUBLIC_FACILITIES,
            MapPoiCustomType.POITYPE_CHARGING_STATION,
            MapPoiCustomType.POITYPE_PUBLIC_BANK,
            MapPoiCustomType.POITYPE_LIFE_CVS,
            MapPoiCustomType.POITYPE_AREA_NAME_PUBLIC,
            MapPoiCustomType.POITYPE_PLACE_NAME_BUILDING,
            MapPoiCustomType.POITYPE_TRANSPORT_FACILITIES_SUBWAY_STATION,
            MapPoiCustomType.POITYPE_PUBLIC_HOUSE,
            MapPoiCustomType.POITYPE_LIFE_FOOD,
            MapPoiCustomType.POITYPE_TRANSPORT_FACILITIES_BUS_STATION,
            MapPoiCustomType.POITYPE_TRANSPORT_FACILITIES_OTHER_TRANSPORT,
            MapPoiCustomType.POITYPE_AREA_NAME_BUSINESS,
            MapPoiCustomType.POITYPE_BUSINESS_HOTEL,
            MapPoiCustomType.POITYPE_AUTO_SERVICE,
            MapPoiCustomType.POITYPE_AUTO_REPAIR
        )
        ext1MapView.operatorBusiness.setCustomLabelTypeVisable(
            hideList,
            MapPoiCustomOperateType.CUSTOM_POI_OPERATE_ONLY_LIST_HIDE
        )
    }

    fun initExtMapLayer() {
        Timber.i("initExtMapLayer")
        mapController.setMapStylePath(SurfaceViewID.SURFACE_VIEW_ID_EX1, sharePreferenceFactory.getThemePath())
        mapController.setMapStyle(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            NightModeGlobal.isNightMode(),
            EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NORMAL
        )
        val scaleValue = 0.8f
        val carScale =
            floatArrayOf(
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue,
                scaleValue
            )
        ext1MapLayer.setCarScaleByMapLevel(carScale)
        setOpenNaviLabel()
        setRenderFps()
        setFirstCarPosition()
        switchMapViewMode(MapModeType.VISUALMODE_2D_CAR)//设置第一次地图模式
        setMainMapCarMode()
        setMapProjectionCenter(0.5f, 0.78f)
        mapController.setTmcVisible(SurfaceViewID.SURFACE_VIEW_ID_EX1, true)//暂时默认打开路况
    }

    /**
     * 开启巡航、导航标注帧率15
     */
    private fun setOpenNaviLabel() {
        mapController.setMapBusinessDataPara(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            MapBusinessDataType.MAP_BUSINESSDATA_FORCE_NAVI_LABEL,
            MapParameter().apply {
                value1 = 1 //开启导航标注
                value2 = 15 //设置帧率
                value3 = 0 //按上层设置的帧率刷新
                value4 = 0 //保留
            })
    }

    /**
     * 设置车标模型 ，且为跟随状态
     */
    private fun setMainMapCarMode() {
        val path = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)
            .getStringValue(MapSharePreference.SharePreferenceKeyEnum.themePath, "")
        mapViewComponent.setMainMapCarMode(path, ext1MapLayer)
    }

    private fun setRenderFps() {
        // 修改正常场景下的帧率
        mapController.setRenderFpsByMode(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            MapRenderMode.MapRenderModeNormal,
            engineerBusiness.engineerConfig.backend_MapRenderModeNormal // SDK默认帧率：15帧
        )
        // 修改导航场景下的帧率
        mapController.setRenderFpsByMode(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            MapRenderMode.MapRenderModeNavi,
            engineerBusiness.engineerConfig.backend_MapRenderModeNavi // SDK默认帧率：15帧
        )
        // 修改动画场景下的帧率
        mapController.setRenderFpsByMode(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            MapRenderMode.MapRenderModeAnimation,
            engineerBusiness.engineerConfig.backend_MapRenderModeAnimation // SDK默认帧率：30帧
        )
        // 修改手势操作时帧率
        mapController.setRenderFpsByMode(
            SurfaceViewID.SURFACE_VIEW_ID_EX1,
            MapRenderMode.MapRenderModeGestureAction,
            engineerBusiness.engineerConfig.backend_MapRenderModeGestureAction// SDK默认帧率：40帧
        )
    }

    /**
     * 用于初次进入程序设置一次车标
     */
    private fun setFirstCarPosition() {
        val location = locationController.lastLocation
        ext1MapLayer.setPreviewMode(false)
        ext1MapLayer.setFollowMode(true)//地图中心是否跟GPS位置同步变化：true 跟随模式 false 自由模式
        ext1MapLayer.setCarPosition(location.longitude, location.latitude, location.bearing)
    }

    /**
     * 设置或者切换地图朝向模式
     */
    fun switchMapViewMode(@MapModeType mapMode: Int = MapModeType.VISUALMODE_UNKNOW) {
        val curMode =
            if (BaseConstant.currentMapviewModeEx1 == MapModeType.VISUALMODE_UNKNOW) MapModeType.VISUALMODE_2D_CAR else BaseConstant.currentMapviewMode
        Timber.i("switchMapViewMode curMode=$curMode mapMode=$mapMode")
        val mapviewMode = if (mapMode == MapModeType.VISUALMODE_UNKNOW) { //切换地图模式
            when (curMode) {
                MapModeType.VISUALMODE_2D_CAR -> {
                    BaseConstant.currentMapviewModeEx1 = MapModeType.VISUALMODE_2D_NORTH
                    MapviewMode.MapviewModeNorth
                }

                MapModeType.VISUALMODE_3D_CAR -> {
                    BaseConstant.currentMapviewModeEx1 = MapModeType.VISUALMODE_2D_CAR
                    MapviewMode.MapviewModeCar
                }

                else -> {
                    BaseConstant.currentMapviewModeEx1 = MapModeType.VISUALMODE_3D_CAR
                    MapviewMode.MapviewMode3D
                }
            }
        } else {//设置地图模式
            BaseConstant.currentMapviewModeEx1 = mapMode
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
                if (mapviewMode == MapviewMode.MapviewMode3D) BaseConstant.Ex1_Zoom_Level_3D else BaseConstant.Ex1_Zoom_Level_2D
        }
        mapController.setMapMode(SurfaceViewID.SURFACE_VIEW_ID_EX1, param, true)
    }

    /**
     *设置地图视口锚点在view中的比例
     */
    fun setMapProjectionCenter(percentX: Float, percentY: Float) {
        ext1MapView.setMapProjectionCenter(percentX, percentY)
    }

    fun renderPause() {
        mapController.renderPause(SurfaceViewID.SURFACE_VIEW_ID_EX1)
    }

    fun renderResume() {
        mapController.renderResume(SurfaceViewID.SURFACE_VIEW_ID_EX1)
    }

    fun resetTickCount() {
        ext1MapView.resetTickCount(1)
    }
}