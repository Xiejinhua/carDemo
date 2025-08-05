package com.desaysv.psmap.model.voice

import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.autosdk.adapter.AdapterConstants
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.common.AutoState
import com.autosdk.common.tts.IAutoPlayer
import com.autosdk.common.tts.ITTSListener
import com.desay_svautomotive.voicemanager.SdkManager
import com.desay_svautomotive.voicemanager.VrAsrManager
import com.desay_svautomotive.voicemanager.VrNaviManager
import com.desay_svautomotive.voicemanager.model.PoiInfo
import com.desaysv.psmap.base.app.ForegroundCallbacks
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.def.MapModeType.Companion.VISUALMODE_2D_CAR
import com.desaysv.psmap.base.def.MapModeType.Companion.VISUALMODE_2D_NORTH
import com.desaysv.psmap.base.def.MapModeType.Companion.VISUALMODE_3D_CAR
import com.desaysv.psmap.base.utils.AppUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IOVVoiceSearchMapCommandImpl
import com.desaysv.psmap.model.impl.IMapCommand
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton


/**
 * IOV语音对接管理类，对接IOV语音提供的jar包
 * 后续项目建议语音适配导航aar，减少工作量
 */
@Singleton
class IOVVoiceBusiness @Inject constructor(
    @ApplicationContext private val context: Context,
    private val iovVoiceSearchMapCommand: IOVVoiceSearchMapCommandImpl,
    @ProvidesHiltModule.DefaultMapCommand private val defaultMapCommand: IMapCommand,
    private val mNaviRepository: INaviRepository,
    private val mMapBusiness: MapBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val mRouteBusiness: RouteBusiness,
    private var ttsPlayer: IAutoPlayer,
    private val application: Application
) : VrNaviManager.INavigationListener, VrAsrManager.IAsrListener {

    var tempPoiList = ArrayList<PoiInfo>()

    fun init() {
        Timber.i("init")
        VrNaviManager.getInstance().setINaviClient(this)
        VrAsrManager.getInstance().setIAsrClient(this)
        SdkManager.getInstance().init(context)

        iovVoiceSearchMapCommand.mapCommand.observeForever {
            Timber.i("voiceCommandType ${it.mapCommandType}")
            when (it.mapCommandType) {
                MapCommandType.IOV_VOICE_KeyWordSearch -> {
                    val voiceList = mutableListOf<PoiInfo>()
                    it.searchPoiList?.map { poi ->
                        voiceList.add(converseToVoicePoiInfo(poi))
                    }
                    Timber.i("setSearchedNaviSource() called with: voiceList = $voiceList")
                    tempPoiList.clear()
                    tempPoiList.addAll(voiceList)
                    VrNaviManager.getInstance().setSearchedNaviSource(voiceList)
                }

                MapCommandType.IOV_VOICE_AroundSearch -> {
                    val voiceList = mutableListOf<PoiInfo>()
                    it.searchPoiList?.map { poi ->
                        voiceList.add(converseToVoicePoiInfo(poi))
                    }
                    Timber.i("setSearchedNaviSource() called with: voiceList = $voiceList")
                    tempPoiList.clear()
                    tempPoiList.addAll(voiceList)
                    VrNaviManager.getInstance().setSearchedNaviSource(voiceList)
                }

                MapCommandType.IOV_VOICE_NearestSearch -> {
                    it.searchNearestResult?.let { searchNearestResult ->
                        val jsonObject = JSONObject()
                        jsonObject.put("PROVINCE_NAME", searchNearestResult.province)
                        jsonObject.put("CITY_NAME", searchNearestResult.city)
                        jsonObject.put("AREA_NAME", searchNearestResult.district)
                        jsonObject.put("AREA_CODE", searchNearestResult.districtadcode)
                        val jsonRet = jsonObject.toString()
                        updateNavigationJson(10030, value = jsonRet)
                    }
                }

                //目前语音周边搜索都是拉起导航界面的，以下是没有拉起导航界面只传信息的逻辑，没有用到，
                MapCommandType.IOV_VOICE_AlongWaySearch -> {
                    val voiceList = mutableListOf<PoiInfo>()
                    it.searchPoiList?.map { poi ->
                        voiceList.add(converseToVoicePoiInfo(poi))
                    }
                    if (voiceList.size > 30) {
                        VrNaviManager.getInstance().setSearchedAlongRoute(voiceList.subList(0, 30))
                    } else {
                        VrNaviManager.getInstance().setSearchedAlongRoute(voiceList)
                    }
                    Timber.i("setSearchedAlongRoute() called with: voiceList = $voiceList")
                }

                MapCommandType.IOV_VOICE_WhereAmI -> {
                    Timber.i("WhereAmI() called with: playTextNavi data = $it.data")
                    ttsPlayer.playTextNavi(it.data)
                }

                MapCommandType.IOV_VOICE_RequestHomeAddress -> {
                    Timber.i("ResponseHomeAddress called with")
                    it.poi?.let { poi ->
                        val jsonArray = JSONArray()
                        val jsonObject = JSONObject()
                        try {
                            jsonObject.put("POINAME", poi.name)
                            jsonObject.put("POIID", poi.id)
                            jsonObject.put("LON", poi.point.longitude)
                            jsonObject.put("LAT", poi.point.latitude)
                            jsonObject.put("DISTANCE", poi.distance)
                            jsonObject.put("CATEGORY", "家")
                            jsonObject.put("ADDRESS", poi.addr)
                            jsonObject.put("COMMON_NAME", "家")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        jsonArray.put(jsonObject)
                        val retJson = jsonArray.toString()
                        Timber.i("ResponseHomeAddress retJson:$retJson")
                        VrNaviManager.getInstance().returnHomeWorkAddress(retJson)
                    }
                }

                MapCommandType.IOV_VOICE_RequestCompanyAddress -> {
                    Timber.i("ResponseHomeAddress called with")
                    it.poi?.let { poi ->
                        val jsonArray = JSONArray()
                        val jsonObject = JSONObject()
                        try {
                            jsonObject.put("POINAME", poi.name)
                            jsonObject.put("POIID", poi.id)
                            jsonObject.put("LON", poi.point.longitude)
                            jsonObject.put("LAT", poi.point.latitude)
                            jsonObject.put("DISTANCE", poi.distance)
                            jsonObject.put("CATEGORY", "公司")
                            jsonObject.put("ADDRESS", poi.addr)
                            jsonObject.put("COMMON_NAME", "公司")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        jsonArray.put(jsonObject)
                        val retJson = jsonArray.toString()
                        Timber.i("ResponseCompanyAddress retJson:$retJson")
                        VrNaviManager.getInstance().returnHomeWorkAddress(retJson)
                    }
                }

                else -> {}
            }
        }
        initNaviStateSender()
    }

    fun unInit() {
        Timber.i("unInit")
        VrNaviManager.getInstance().setINaviClient(null)
        VrAsrManager.getInstance().setIAsrClient(null)
    }

    private fun converseToVoicePoiInfo(poi: POI): PoiInfo {
        val poiInfo = PoiInfo()
        poiInfo.id = poi.id
        poiInfo.poiName = poi.name
        poiInfo.address = poi.addr
        //注意，这里可能是7.8米，导致程序奔溃，暂为空
        poiInfo.distance = poi.distance?.toIntOrNull() ?: 0
        poiInfo.longitude = poi.point.longitude
        poiInfo.latitude = poi.point.latitude
        poiInfo.phoneNumber = poi.phone
        return poiInfo
    }

    override fun searchPoiDest(name: String?) {
        Timber.i("searchPoiDest keyword=$name")
        name?.let {
            if (!defaultMapCommand.openMap()) {
                return
            }
            iovVoiceSearchMapCommand.keywordSearch(name)
        }
    }

    override fun searchPoiDest(name: String?, lon: Double, lat: Double) {
        name?.let {
            if (!defaultMapCommand.openMap()) {
                return
            }
            iovVoiceSearchMapCommand.keywordSearch(name, lon, lat)
        }
    }

    override fun searchPoiNear(name: String?) {
        name?.let {
            if (!defaultMapCommand.openMap()) {
                return
            }
            iovVoiceSearchMapCommand.aroundSearch(name)
        }
    }

    override fun searchPoiNear(name: String?, lon: Double, lat: Double) {
        name?.let {
            if (!defaultMapCommand.openMap()) {
                return
            }
            iovVoiceSearchMapCommand.aroundSearch(name, lon, lat)
        }
    }

    //    参数name：地点名称
    //    参数 location：某个地点名称附近
    //    例如：华贸新天地附近的加油站
    //    在某个地点位置附近进行搜索
    //成都孵化园附近的咖啡厅  咖啡厅放在name  成都孵化园放在location
    override fun searchPoiNearLocation(name: String?, location: String?) {
        name?.let { name ->
            location?.let { location ->
                if (!defaultMapCommand.openMap()) {
                    return
                }
                iovVoiceSearchMapCommand.aroundSearchByLocationName(name, location)
            }
        }
    }

    override fun searchTrafficInfo() {
        if (!defaultMapCommand.openMap()) {
            return
        }
        defaultMapCommand.playRoadAheadTraffic()
    }

    //  参数location：目的地地点名称
    //  查询当前地点到目的地地点沿途/附近的路况
    //  暂不支持，返回固定值
    override fun searchTrafficInfo(location: String?) {
        updateNavigationJson(12402, "EXTRA_TRAFFIC_CONDITION_RESULT", "5")
    }

    //  xxx市xxx路的路况
    //  查询开始地点到目的地沿途/附近的路况
    //  目前暂不支持，返回固定值
    override fun searchTrafficInfo(city: String?, road: String?) {
        updateNavigationJson(12402, "EXTRA_TRAFFIC_CONDITION_RESULT", "5")
    }

    //搜索沿途的XX
    //搜索沿途的dest 例如：搜索沿途的加油站、4S店、服务区
    override fun searchAlongRoute(searchName: String?) {
        searchName?.let {
            iovVoiceSearchMapCommand.alongRouteSearch(it)
        }
    }

    override fun whereAmI(): PoiInfo {
        Timber.i("whereAmI")
        iovVoiceSearchMapCommand.whereAmI()
        return PoiInfo()
    }

    override fun zoomIn(): Boolean {
        Timber.i("zoomIn")
        val canZoomIn: Boolean = mMapBusiness.isZoomInEnable()
        if (canZoomIn) {
            defaultMapCommand.zoomIn()
            ttsPlayer.playTextNavi("地图已放大")
        } else {
            ttsPlayer.playTextNavi("地图已放大")
        }
        return canZoomIn
    }

    override fun zoomOut(): Boolean {
        Timber.i("zoomOut")
        val canZoomOut: Boolean = mMapBusiness.isZoomOutEnable()
        if (canZoomOut) {
            defaultMapCommand.zoomOut()
            ttsPlayer.playTextNavi("地图已缩小")
        } else {
            ttsPlayer.playTextNavi("地图已缩至最小")
        }
        return canZoomOut
    }

    override fun openTraffic(): Boolean {
        if (!defaultMapCommand.openMap()) {
            return false
        }
        defaultMapCommand.showTmc()
        return false
    }

    override fun closeTraffic(): Boolean {
        if (!defaultMapCommand.openMap()) {
            return false
        }
        defaultMapCommand.hideTmc()
        return false
    }

    override fun naviToHome() {
        if (!defaultMapCommand.openMap()) {
            return
        }
        if (defaultMapCommand.naviToHome()) {

        } else {
            ttsPlayer.playTextNavi("请先确认是否登录账号，并已设置家的地址")
        }
    }

    override fun naviToWork() {
        if (!defaultMapCommand.openMap()) {
            return
        }
        if (defaultMapCommand.naviToWork()) {

        } else {
            ttsPlayer.playTextNavi("请先确认是否登录账号，并已设置公司的地址")
        }
    }

    override fun openNavi() {
        if (!defaultMapCommand.openMap()) {
            return
        }
    }

    override fun closeNavi() {
        defaultMapCommand.exitMap()
    }

    override fun cancelNavi() {
        defaultMapCommand.exitNavi()
    }

    //打开电子狗
    override fun openElectronicDog() {

    }

    //关闭电子狗
    override fun closeElectronicDog() {

    }

    override fun ensureCity() {
        iovVoiceSearchMapCommand.nearestSearch(
            defaultMapCommand.getLastLocation().longitude,
            defaultMapCommand.getLastLocation().latitude
        )
    }

    override fun switchMapMode(mapMode: String?) {
        Timber.i("switchMapMode() called with: mapMode = $mapMode")
        // TODO: 2021-9-10 此处需要注意,语音那边给我们的2D/2D模式,我们需要展示2D车头朝北,语音给我们返回2D车头朝北,我们展示2D模式
        when (mapMode) {
            "2D", "2D模式" -> {
                if (!defaultMapCommand.openMap()) {
                    return
                }
                defaultMapCommand.switchMapMode(VISUALMODE_2D_NORTH)
            }

            "3D", "3D模式" -> {
                if (!defaultMapCommand.openMap()) {
                    return
                }
                defaultMapCommand.switchMapMode(VISUALMODE_3D_CAR)
            }

            "2D车头朝北" -> {
                if (!defaultMapCommand.openMap()) {
                    return
                }
                defaultMapCommand.switchMapMode(VISUALMODE_2D_CAR)
            }
        }
    }

    override fun collectNavi() {
        if (mNaviRepository.isNavigating()) {
            ttsPlayer.playTextNavi("导航过程中不能收藏")
        } else {
            if (!defaultMapCommand.openMap()) {
                return
            }
            MainScope().launch {
                defaultMapCommand.favoriteCurrentLocationPOI(null)
            }
        }
    }

    override fun startNaviDest(name: String?, lon: Double, lat: Double) {
        var poiName = name ?: "终点"
        if (!defaultMapCommand.openMap()) {
            return
        }
        defaultMapCommand.startPlanRoute(poiName, lon, lat)
    }

    //传入终点 地名+经纬度 直接导航。 strategy = "默认选择"
    override fun startNaviDestStrategy(name: String?, lon: Double, lat: Double, p3: String?) {
        var poiName = name ?: "终点"
        if (!defaultMapCommand.openMap()) {
            return
        }
        defaultMapCommand.startPlanRoute(poiName, lon, lat)
    }

    override fun startNaviRoute(
        name: String,
        startLon: Double,
        startLat: Double,
        endLon: Double,
        endLat: Double
    ) {
        if (!defaultMapCommand.openMap()) {
            return
        }
        var isExistValue = false
        val poiName = if (TextUtils.isEmpty(name)) "终点" else name
        for (poiInfo in tempPoiList) {
            if (poiInfo.longitude == endLon && poiInfo.latitude == endLat && TextUtils.equals(
                    poiInfo.poiName,
                    name
                )
            ) {
                isExistValue = true
                defaultMapCommand.startPlanRoute(poiName, endLon, endLat)
                break
            }
        }
        if (!isExistValue) {
            defaultMapCommand.startPlanRoute(poiName, endLon, endLat)
        }
    }

    override fun viaNaviDest(name: String, lon: Double, lat: Double, viaStatus: String?) {
        if (!defaultMapCommand.openMap()) {
            return
        }
        if (mNaviRepository.isNavigating()) {
            when (viaStatus) {
                "删除全部" -> {
                    defaultMapCommand.deleteWayToPointOnRoutePage()
                }

                "增加" -> {
                    val poi = POIFactory.createPOI(name, GeoPoint(lon, lat))
                    defaultMapCommand.addWayToPointOnRoutePage(poi)
                }
            }
        }

    }

    //传入途经点pass_name， 终点dest_name， 直接发起导航
    override fun passbyDestSearch(viaName: String, endName: String) {
        if (!defaultMapCommand.openMap()) {
            return
        }
        //defaultMapCommand.startPlanRoute(viaName, endName)
    }

    //导航到XXX 途经XXX
    override fun passbyDestSearchLonLat(
        name: String,
        lon: Double,
        lat: Double,
        viaName: String,
        viaLon: Double,
        viaLat: Double
    ) {
        if (!defaultMapCommand.openMap()) {
            return
        }
        defaultMapCommand.startPlanRoute(name, lon, lat, viaName, viaLon, viaLat)
    }

    //路线方案选择,选择第select个
    // 语音传过来的下标需要-1
    override fun naviRouteSelect(index: Int) {
        if (!defaultMapCommand.openMap()) {
            return
        }
        defaultMapCommand.chooseRoute(index - 1)
    }

    override fun startNavi() {
        defaultMapCommand.startNaviWhenHaveRoute()
    }

    override fun routeIsSHow(routeIsSHow: Boolean) {
        if (routeIsSHow) {
            defaultMapCommand.previewRoute()
        } else {
            defaultMapCommand.exitPreviewRoute()
        }
    }

    //planning=高速优先/不走高速/避免收费躲避拥堵
    override fun routePlanning(s: String?) {

    }

    //切换主路/辅路， MainOn=true 为主路
    override fun routeChangeMain(isMain: Boolean) {
//        defaultMapCommand.switchParallelRoute()
    }

    override fun routeChangeHighWay(p0: Boolean) {

    }

    override fun setHomeWorkAddress(name: String, lon: Double, lat: Double, isHome: Boolean) {
        if (!defaultMapCommand.openMap()) {
            return
        }
        if (isHome) {
            //defaultMapCommand.setHomeOrCompanyAddress(name, lon, lat, "家")
        } else {
            //defaultMapCommand.setHomeOrCompanyAddress(name, lon, lat, "公司")
        }
    }

    override fun requestHomeWorkAddress() {
        iovVoiceSearchMapCommand.requestHomeOrWorkAddress("家")
        iovVoiceSearchMapCommand.requestHomeOrWorkAddress("公司")
    }

    override fun requestNaviStates() {
        VrNaviManager.getInstance().updateNavigationAppStartState(true)
        VrNaviManager.getInstance().updateNavigationInFrontState(AppUtils.mapIsTopAPP(context))
        VrNaviManager.getInstance().updateNavigationState(mNaviRepository.isNavigating())
    }

    override fun openTeamMode() {
    }

    override fun exitTeamMode() {
    }

    override fun showTeammatesLocation() {
    }

    override fun CreatTeam() {
    }

    override fun ExitTeam() {
    }

    override fun showTeammatesList() {
    }

    override fun showRoadBookList() {
    }

    override fun showActivityList() {
    }

    override fun throwMessage() {
    }

    override fun getMessage() {
    }

    override fun clockIN() {
    }

    override fun takePicture() {
    }

    override fun startVideo() {
    }

    override fun stopVideo() {
    }

    override fun doneVideo() {
    }

    override fun restartVideo() {
    }

    override fun syncPageNum(p0: Int) {
    }

    override fun voiceUpInLeft() {
        Timber.i("voiceUpInLeft is called")
    }

    override fun voiceUpInRight() {
        Timber.i("voiceUpInRight is called")
    }

    override fun closeAutoZoom() {
        defaultMapCommand.setAutoZoom(false)
    }

    override fun openAutoZoom() {
        defaultMapCommand.setAutoZoom(true)
    }

    override fun closeMapMute() {
        defaultMapCommand.setMapMute(false)
    }

    override fun openMapMute() {
        defaultMapCommand.setMapMute(true)
    }

    override fun setBoradCastDetal() {
        defaultMapCommand.setVoiceBroadcastMode(2)
    }

    override fun setBoradCastSimple() {
        defaultMapCommand.setVoiceBroadcastMode(1)
    }

    override fun openCollectList() {
        defaultMapCommand.openFavoritePage()
    }

    override fun openSettings() {
        Timber.i("openSettings")
        defaultMapCommand.openSettingsPage()
    }

    override fun openSearch() {
        Timber.i("openSearch")
        defaultMapCommand.openSearchPage()
    }

    override fun voiceNaviUiConnect(p0: String?, p1: String?) {

    }

    override fun naviContinue(p0: Boolean) {

    }

    override fun startAlongRoute() {

    }

    override fun nearHomeOrWorkPlace() {

    }

    override fun trafficStatus() {

    }

    override fun openNaviCenter(p0: String?) {

    }

    /*=======================================asr 分割线==========================================*/

    override fun AsrWakeUp() {
        Timber.d("AsrWakeUp")
    }

    override fun AsrSrStart() {
        Timber.d("AsrSrStart")
    }

    override fun AsrSrStop() {
        Timber.d("AsrSrStop")
    }

    override fun AsrTtsStart() {
        Timber.d("AsrTtsStart")
    }

    override fun AsrTtsStop() {
        Timber.d("AsrTtsStop")
    }

    override fun AsrTopStateOn() {
        Timber.d("AsrTopStateOn")
    }

    override fun AsrTopStateOff() {
        Timber.d("AsrTopStateOff")
    }

    override fun AsrPgsText(p0: String?) {
        Timber.d("AsrPgsText $p0")
    }

    override fun requestFocus() {
        Timber.d("requestFocus")
    }

    override fun abandonFocus() {
        Timber.d("abandonFocus")
    }

    override fun isRecognizing(p0: Boolean, p1: Int) {
        Timber.d("isRecognizing $p0 $p1")
    }

    override fun AsrTipsText(p0: String?) {
        Timber.d("AsrTipsText $p0")
    }

    //将文本转成语音播报格式,传给语音
    private fun updateNavigationJson(type: Int, name: String? = null, value: String?) {
        var result = name?.let {
            val jsonObject = JSONObject()
            jsonObject.put(name, value)
            jsonObject.toString()
        } ?: value

        Timber.i("updateNavigationJson() called with: type = $type, result = $result")
        VrNaviManager.getInstance().updateNavigationJson(type, result)
    }

    private fun sendNaviState(@NaviState state: Int) {
        // 语音反馈10019信息太频繁导致语音卡顿，所以目前仅向语音发送需要的状态
        val needStateArr = intArrayOf(1, 2, 3, 4, 8, 9, 10, 12, 17, 18, 19)
        val idx = Arrays.binarySearch(needStateArr, state)
        if (idx >= 0) {
            val jsonObject = JSONObject()
            jsonObject.put("EXTRA_STATE", state)
            jsonObject.put("EXTRA_CROSS_MAP", 0)
            val jsonRet = jsonObject.toString()
            VrNaviManager.getInstance()
                .updateNavigationJson(AdapterConstants.EXTRA_SEND_KEY_VALUE, jsonRet)
        }
    }

    private fun initNaviStateSender() {
        //Application启动即为开始运行
        sendNaviState(START_RUN)

        //前后台判断
        ForegroundCallbacks.getInstance(application)
            .addListener(object : ForegroundCallbacks.Listener {
                override fun onBecameForeground() {
                    sendNaviState(IN_FRONT)
                }

                override fun onBecameBackground() {
                    sendNaviState(IN_BACK)
                }

            })

        //算路开始与完成
        mRouteBusiness.isRequestRoute.observeForever {
            if (it == true) {
                sendNaviState(START_PLAN_ROUTE)
            } else {
                sendNaviState(START_PLAN_ROUTE_SUCCESS)
            }
        }

        //导航状态
        mNaviBusiness.naviStatus.observeForever { naviState ->
            when (naviState) {
                BaseConstant.NAVI_STATE_REAL_NAVING -> sendNaviState(BEGIN_NAVI)
                BaseConstant.NAVI_STATE_SIM_NAVING -> sendNaviState(BEGIN_SIM_NAVI)
                BaseConstant.NAVI_STATE_STOP_REAL_NAVI -> sendNaviState(STOP_NAVI)
                BaseConstant.NAVI_STATE_STOP_SIM_NAVI -> sendNaviState(STOP_SIM_NAVI)
            }
        }
        mMapBusiness.mapMode.observeForever { mapMode ->
            when (mapMode) {
                VISUALMODE_2D_CAR -> sendNaviState(MODE_2D_CAR)
                VISUALMODE_3D_CAR -> sendNaviState(MODE_3D_CAR)
                VISUALMODE_2D_NORTH -> sendNaviState(MODE_2D_CAR_NORTH)
            }
        }

        ttsPlayer.registerITTSListener(object : ITTSListener {
            override fun onTTSPlayBegin() {
                sendNaviState(BEGIN_TTS)
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.TTS_PLAY_START)
            }

            override fun onTTSPlayComplete() {
                sendNaviState(STOP_TTS)
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.TTS_PLAY_FINISH)
            }

            override fun onTTSPlayInterrupted() {
                sendNaviState(STOP_TTS)
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.TTS_PLAY_FINISH)
            }

            override fun onTTSPlayError() {
                sendNaviState(STOP_TTS)
                SdkAdapterManager.getInstance().sendNormalMessage(AutoState.TTS_PLAY_FINISH)
            }

            override fun onTTSIsPlaying() {
            }
        })
        sendNaviState(IN_MAIN)

    }

}