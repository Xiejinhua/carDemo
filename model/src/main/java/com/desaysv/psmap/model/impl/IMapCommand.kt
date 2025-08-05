package com.desaysv.psmap.model.impl

import android.location.Location
import androidx.lifecycle.LiveData
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.search.model.SearchNearestResult
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.model.bean.MapCommandBean
import com.desaysv.psmap.model.bean.MapCommandParamType
import com.desaysv.psmap.model.bean.MapCommandType

interface IMapCommand {
    /**
     * 一般是需要页面处理的命令
     */
    fun getMapCommand(): LiveData<MapCommandBean>

    /**
     * 响应命令处理结果
     */
    fun notifyMapCommandResult(type: MapCommandType, result: Any?)

    fun addMapCommandResultWaitQueue(type: MapCommandType, request: Any)

    fun registerMapCommandResultCallback(callback: (type: MapCommandType, request: Any, result: Any?) -> Unit)

    fun checkMapCommandInQueue(type: MapCommandType): Boolean

    fun putMapCommandParams(type: MapCommandParamType, params: Map<String, Any>)

    fun getMapCommandParam(type: MapCommandParamType): Map<String, Any>?

    /**
     * 关键字搜索
     */
    fun keywordSearch(keyword: String, isVoice: Boolean = false)

    /**
     * 关键字搜索带经纬度
     */
    fun keywordSearch(keyword: String, lon: Double, lat: Double)

    /**
     * 搜索收藏点
     */
    fun keyWordSearchForCollect(keyword: String)

    /**
     * 周边搜
     */
    fun aroundSearch(keyword: String, isVoice: Boolean = false, range: String = "5000")

    /**
     * 周边搜带经纬度
     */
    fun aroundSearch(keyword: String, lon: Double, lat: Double, range: String? = null, isVoice: Boolean = false)

    /**
     * 逆地理搜索
     */
    suspend fun nearestSearch(lon: Double, lat: Double): SearchNearestResult?

    /**
     *在某个地点位置附近进行搜索
     *例如：成都孵化园附近的咖啡厅  咖啡厅放在keyword  成都孵化园放在locationNamen
     */
    suspend fun aroundSearchByLocationName(keyword: String, locationName: String, isVoice: Boolean = false): Boolean

    /**
     *播报前方路况
     */
    fun playRoadAheadTraffic()

    /**
     * 获取当前位置
     */
    fun getLastLocation(): Location

    /**
     * 放大地图
     */
    fun zoomIn(): Boolean

    /**
     * 缩小地图
     */
    fun zoomOut(): Boolean

    /**
     *显示路况路网
     */
    fun showTmc()

    /**
     *隐藏路况路网
     */
    fun hideTmc()

    /**
     * 导航回家
     */
    fun naviToHome(): Boolean

    /**
     * 导航去公司
     */
    fun naviToWork(): Boolean

    /**
     * 打开地图
     */
    fun openMap(): Boolean

    /**
     * 关闭地图
     */
    fun exitMap()

    /**
     * 退出导航
     */
    fun exitNavi()

    /**
     * 获取当前行政区域信息
     */
    suspend fun requestCityAreaInfo()

    /**
     * 获取对应经纬度的行政区域信息
     */
    suspend fun requestCityAreaInfo(lon: Double, lat: Double)

    fun switchMapMode(@MapModeType mapMode: Int)

    fun switchDayNightMode(status: SkyBoxBusiness.DAY_NIGHT_STATUS)

    /**
     * 收藏当前车标POI位置
     */
    suspend fun favoriteCurrentLocationPOI(type: String?): String?

    /**
     * 开始路线规划
     */
    fun startPlanRoute(name: String, lon: Double, lat: Double)

    /**
     * 开始路线规划
     * viaName  途经点名称
     * endName  终点
     */
    suspend fun startPlanRoute(viaName: String, endName: String)

    /**
     * 开始路线规划
     * 导航到XXX 途经XXX
     */
    fun startPlanRoute(name: String, lon: Double, lat: Double, viaName: String, viaLon: Double, viaLat: Double)

    /**
     * 开始路线规划
     */
    fun startPlanRoute(end: POI?, viaPois: List<POI>?)

    /**
     * 开始路线规划
     */
    fun startPlanRoute(from: POI?, end: POI?, viaPois: List<POI>?)

    /**
     * 直接开始导航
     */
    fun startNavi(name: String, lon: Double, lat: Double)

    /**
     * 直接开始导航
     */
    fun startNavi(end: POI?, viaPois: List<POI>?)

    /**
     * 直接开始导航
     */
    fun startNavi(from: POI?, end: POI?, viaPois: List<POI>?)

    /**
     * 如果在路线规划页面，开始导航
     */
    fun startNaviWhenHaveRoute()

    /**
     * 如果在路线规划页面，选择第几条路线
     */
    fun chooseRoute(routeIndex: Int)

    /**
     * 切换路线，默认切换到下一条路线
     */
    fun switchRoute(): Pair<Boolean, String>

    //添加途经点 这个必须在已经路径规划成功的情况下
    fun addWayToPointOnRoutePage(vaiPoint: POI)

    fun deleteWayToPointOnRoutePage(vaiPoint: POI)

    fun deleteWayToPointOnRoutePage()

    /**
     * 沿途搜索,需要在路线规划/导航中
     * 搜索沿途的加油站、4S店、服务区
     */
    fun alongRouteSearch(name: String): Boolean

    /**
     * 全览路线
     */
    fun previewRoute()

    /**
     * 退出全览路线
     */
    fun exitPreviewRoute()

    /**
     * 切换平行路
     */
    fun switchParallelRoute(type: Int)

    /**
     * 切换路线策略
     */
    fun switchRouteStrategy(routePreference: String)

    /**
     * 获取路线策略
     */
    fun requestRouteStrategy(): String

    /**
     * 请求家/公司地址
     */
    fun requestHomeOrWorkAddress(type: String)

    /**
     * 开启/关闭导航自动比例尺
     */
    fun setAutoZoom(autoZoom: Boolean)

    /**
     * 设置地图静音
     */
    fun setMapMute(mute: Boolean)

    /**
     * 设置播报模式
     */
    fun setVoiceBroadcastMode(mode: Int)

    fun naviToFavorite(): String

    /**
     * 打开收藏页面
     */
    fun openFavoritePage(): Boolean

    /**
     * 打开组队页面
     */
    fun openGroupPage(isLongClick: Boolean, isPress: Boolean): Boolean

    /**
     * 打开设置页面
     */
    fun openSettingsPage(): Boolean

    /**
     * 打开搜索页面
     */
    fun openSearchPage()

    /**
     * 打开导航界面
     */
    fun openNaviPage()

    /**
     * 回到首页，显示POI卡片
     */
    fun showPoiCard(poi: POI)

    /**
     * 显示POI详情
     */
    fun showPoiDetail(poi: POI)

    /**
     *打开修改家/公司地址页面
     */
    fun openModifyHomeCompanyAddressPage(type: String): Boolean

    /**
     *搜索家/公司地址结果页面
     */
    fun searchHomeCompanyAddressResultPage(type: String, keyword: String): Boolean

    /**
     * 确认操作
     */
    fun confirm(): Boolean

    /**
     * 关闭设置页面
     */
    fun closeSettingsPage(): Boolean

    /**
     * 选择
     */
    fun posRank(rank: Pair<String, Int>): Boolean

    /**
     * 选择页面
     */
    fun pageRank(rank: Pair<String, Int>): Boolean

    /**
     * 指定条件搜索路书
     */
    fun searchAhaTrip(cityItemInfo: CityItemInfo? = null, day: String? = null, keyword: String? = null): Boolean

    /**
     * 路书详情页收藏路书
     */
    fun ahaTripCollect(isCollect: Boolean): Boolean

    /**
     * 打开对应的路书详情页收藏路书
     */
    fun openAhaTripDetailPage(tab: String): Boolean
}