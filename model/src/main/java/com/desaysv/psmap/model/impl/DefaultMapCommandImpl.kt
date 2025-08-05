package com.desaysv.psmap.model.impl

import android.app.Application
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.map.OperatorPosture
import com.autonavi.gbl.search.model.NearestPoi
import com.autonavi.gbl.search.model.SearchNearestResult
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.search.request.SearchPoiBizType
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.bussiness.widget.navi.NaviComponent
import com.desaysv.psmap.base.business.ActivationMapBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.AppUtils
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.MapCommandBean
import com.desaysv.psmap.model.bean.MapCommandParamType
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.TtsPlayBusiness
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * 默认的命令实现类,导航显示搜索结果
 */
open class DefaultMapCommandImpl(
    @ApplicationContext protected val mContext: Context,
    protected val mMapBusiness: MapBusiness,
    protected val mSearchBusiness: SearchBusiness,
    protected val mNaviRepository: INaviRepository,
    protected val mRouteRequestController: RouteRequestController,
    protected val skyBoxBusiness: SkyBoxBusiness,
    protected val userBusiness: UserBusiness,
    protected val mLocationBusiness: LocationBusiness,
    protected val mTtsPlayBusiness: TtsPlayBusiness,
    protected val mNaviBusiness: NaviBusiness,
    protected val mNavigationSettingBusiness: NavigationSettingBusiness,
    protected val mRouteBusiness: RouteBusiness,
    protected val settingAccountBusiness: SettingAccountBusiness,
    protected val application: Application,
    protected val iCarInfoProxy: ICarInfoProxy,
    protected val activationMapBusiness: ActivationMapBusiness,
    protected val customTeamBusiness: CustomTeamBusiness
) : IMapCommand {
    protected val mapCommand = MutableLiveData<MapCommandBean>()

    private val mMapCommandResultWaitQueue = ConcurrentHashMap<MapCommandType, Any>()

    private val callbacks = mutableSetOf<(type: MapCommandType, request: Any, result: Any?) -> Unit>()

    private val mMapCommandParams = ConcurrentHashMap<MapCommandParamType, Map<String, Any>>()

    override fun getMapCommand(): LiveData<MapCommandBean> {
        return mapCommand
    }

    override fun notifyMapCommandResult(type: MapCommandType, result: Any?) {
        Timber.i("notifyMapCommandResult type = $type,result = $result")
        mMapCommandResultWaitQueue.remove(type)?.let { request ->
            callbacks.iterator().run {
                while (hasNext()) {
                    next().invoke(type, request, result)
                }
            }
        }
    }

    override fun addMapCommandResultWaitQueue(type: MapCommandType, request: Any) {
        Timber.i("notifyMapCommandResult type = $type,request = $request")
        mMapCommandResultWaitQueue[type] = request
    }

    override fun registerMapCommandResultCallback(callback: (type: MapCommandType, request: Any, result: Any?) -> Unit) {
        Timber.i("registerMapCommandResultCallback")
        callbacks.add(callback)
    }

    override fun checkMapCommandInQueue(type: MapCommandType): Boolean {
        return mMapCommandResultWaitQueue.containsKey(type)
    }

    override fun putMapCommandParams(type: MapCommandParamType, params: Map<String, Any>) {
        mMapCommandParams[type] = params
    }

    override fun getMapCommandParam(type: MapCommandParamType): Map<String, Any>? {
        return mMapCommandParams.remove(type)
    }

    override fun keywordSearch(keyword: String, isVoice: Boolean) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.KeyWordSearch, data = keyword, isVoice = isVoice))
    }

    override fun keywordSearch(keyword: String, lon: Double, lat: Double) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(
            MapCommandBean(
                mapCommandType = MapCommandType.KeyWordSearch,
                data = keyword,
                poi = POIFactory.createPOI(null, GeoPoint(lon, lat))
            )
        )
    }

    override fun keyWordSearchForCollect(keyword: String) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.KeyWordSearchForCollect, isVoice = true, data = keyword))
    }

    override fun aroundSearch(keyword: String, isVoice: Boolean, range: String) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.AroundSearch, data = keyword, isVoice = isVoice, range = range))
    }

    override fun aroundSearch(keyword: String, lon: Double, lat: Double, range: String?, isVoice: Boolean) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(
            MapCommandBean(
                mapCommandType = MapCommandType.AroundSearch,
                data = keyword,
                poi = POIFactory.createPOI(null, GeoPoint(lon, lat)),
                range = range,
                isVoice = isVoice
            )
        )
    }

    override suspend fun nearestSearch(lon: Double, lat: Double): SearchNearestResult? {
        val result = mSearchBusiness.nearestSearch(poi = POIFactory.createPOI(null, GeoPoint(lon, lat)))
        when (result.status) {
            Status.SUCCESS -> {
                return result.data
            }

            else -> {
                Timber.i("nearestSearch Fail $result")
                return null
            }
        }
    }

    override suspend fun aroundSearchByLocationName(keyword: String, locationName: String, isVoice: Boolean): Boolean {
        Timber.i("aroundSearchByLocationName keyword = $keyword locationName = $locationName")
        when (locationName) {
            "家" -> {
                val home = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
                if (!home.isNullOrEmpty()) {
                    val poi = home[0]
                    val coord2DDouble = OperatorPosture.mapToLonLat(poi.point_x.toDouble(), poi.point_y.toDouble())
                    aroundSearch(keyword, coord2DDouble.lon, coord2DDouble.lat, isVoice = isVoice)
                    return true
                } else {
                    Timber.i("家地址未设置")
                }
            }

            "公司" -> {
                val company = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
                if (!company.isNullOrEmpty()) {
                    val poi = company[0]
                    val coord2DDouble = OperatorPosture.mapToLonLat(poi.point_x.toDouble(), poi.point_y.toDouble())
                    aroundSearch(keyword, coord2DDouble.lon, coord2DDouble.lat, isVoice = isVoice)
                    return true
                } else {
                    Timber.i("公司地址未设置")
                }
            }
            //目的地周边的搜索
            "destination" -> {
                if (mNaviRepository.isNavigating()) {
                    val endPoiInfo: POI = mRouteRequestController.carRouteResult.toPOI
                    Timber.i("searchPoiNearLocation endPoiInfo:$endPoiInfo")
                    if (endPoiInfo != null) {
                        aroundSearch(endPoiInfo.name, endPoiInfo.point.longitude, endPoiInfo.point.latitude, isVoice = isVoice)
                    } else {
                        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.AroundSearch, searchPoiList = emptyList()))
                    }
                    return true
                } else {
                    Timber.i("无目的地,无法搜索")
                }
            }

            else -> {
                val result = mSearchBusiness.keywordSearch(locationName)
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { data ->
                            val list = SearchDataConvertUtils.blPoiSearchResultToHmiResult(data, SearchPoiBizType.NORMAL).searchInfo.poiResults
                            if (!list.isNullOrEmpty()) {
                                val poi = list[0]
                                Timber.i("aroundSearch  locationName = $locationName poi = $poi")
                                aroundSearch(keyword, poi.point.longitude, poi.point.latitude)
                                return true
                            }
                        }
                    }

                    Status.ERROR -> {
                        Timber.i("aroundSearch  locationName = $locationName Status.ERROR")
                        Timber.i(result.throwable.toString())
                    }

                    else -> {
                        Timber.i("aroundSearch Fail $result")
                    }
                }
            }
        }
        return false
    }

    override fun playRoadAheadTraffic() {
        if (mNaviRepository.isNavigating()) {
            if (mTtsPlayBusiness.playTrafficStatus()) {
                //在TtsPlayBusiness.onPlayTTS中会收到路况，在那里播报，这里不处理
            } else {
                mTtsPlayBusiness.playTextNavi("暂未获取到路况，请稍后再试试哦")
            }
        }
    }

    override fun getLastLocation(): Location = mLocationBusiness.getLastLocation()

    override fun zoomIn(): Boolean {
        Timber.i("zoomIn")
        mMapBusiness.mapZoomIn()
        return true
    }

    override fun zoomOut(): Boolean {
        Timber.i("zoomOut")
        mMapBusiness.mapZoomOut()
        return true
    }

    override fun showTmc() {
        mMapBusiness.setTmcVisible(true)
    }

    override fun hideTmc() {
        mMapBusiness.setTmcVisible(false)
    }

    override fun naviToHome(): Boolean {
        if (!openMap()) {
            return false
        }
        val homeList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
        val poi = homeList?.firstOrNull()?.let { ConverUtils.converSimpleFavoriteItemToPoi(it) }

        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.NaviToHome, poi = poi))
        return if (userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true).isNullOrEmpty()) {
            Timber.i("家地址未设置")
            false
        } else {
            true
        }
    }

    override fun naviToWork(): Boolean {
        if (!openMap()) {
            return false
        }
        val companyList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
        val poi = companyList?.firstOrNull()?.let { ConverUtils.converSimpleFavoriteItemToPoi(it) }

        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.NaviToWork, poi = poi))
        return if (userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true).isNullOrEmpty()) {
            Timber.i("公司地址未设置")
            false
        } else {
            true
        }
    }

    override fun openMap(): Boolean {
        AppUtils.startOrBringActivityToFront(mContext)
        //未激活，返回
        Timber.i(
            "openMap isActivate = ${activationMapBusiness.isActivate()}, isAgreement = ${
                activationMapBusiness
                    .isAgreement()
            }, isOpenAgreement = ${activationMapBusiness.isOpenAgreement}"
        )
        if (!activationMapBusiness.isActivate()) {
            return false
        }
        if (!activationMapBusiness.isAgreement() && !activationMapBusiness.isOpenAgreement) {
            return false
        }
        return true
    }

    override fun exitMap() {
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.MoveAppToBack))
    }

    override fun exitNavi() {
        Timber.i("exitNavi")
        mNaviRepository.stopNavi()
    }

    override suspend fun requestCityAreaInfo() {
        val result =
            mSearchBusiness.nearestSearch(poi = POIFactory.createPOI(null, GeoPoint(getLastLocation().longitude, getLastLocation().latitude)))
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.let { searchNearestResult ->
                    val poiList = SearchCommonUtils.invertOrderList(searchNearestResult.poi_list)
                    searchNearestResult.poi_list = poiList
                    mapCommand.postValue(
                        MapCommandBean(
                            mapCommandType = MapCommandType.RequestCityAreaInfo,
                            searchNearestResult = searchNearestResult
                        )
                    )
                }
            }

            else -> {
                Timber.i("nearestSearch Fail $result")
            }
        }
    }

    override suspend fun requestCityAreaInfo(lon: Double, lat: Double) {
        val result = mSearchBusiness.nearestSearch(poi = POIFactory.createPOI(null, GeoPoint(lon, lat)))
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.let { searchNearestResult ->
                    val poiList = SearchCommonUtils.invertOrderList(searchNearestResult.poi_list)
                    searchNearestResult.poi_list = poiList
                    mapCommand.postValue(
                        MapCommandBean(
                            mapCommandType = MapCommandType.RequestCityAreaInfo,
                            searchNearestResult = searchNearestResult
                        )
                    )
                }
            }

            else -> {
                Timber.i("nearestSearch Fail $result")
            }
        }
    }

    override fun switchMapMode(mapMode: Int) {
        mMapBusiness.switchMapViewMode(mapMode)
    }

    override fun switchDayNightMode(status: SkyBoxBusiness.DAY_NIGHT_STATUS) {
        MainScope().launch { skyBoxBusiness.updateDayNightStatus(status) }
    }

    override suspend fun favoriteCurrentLocationPOI(type: String?): String? {
        val point = GeoPoint(getLastLocation().longitude, getLastLocation().latitude)
        val result = mSearchBusiness.nearestSearch(poi = POIFactory.createPOI(null, point))
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.poi_list?.let { list ->
                    //将List按照距离倒序排列
                    list.sortWith { bean1: NearestPoi, bean2: NearestPoi ->
                        bean1.distance.compareTo(bean2.distance)
                    }
                    val nearestPoi: NearestPoi = list.get(0)
                    val poi = POI()
                    poi.point = point
                    poi.id = nearestPoi.poiid
                    poi.name = nearestPoi.name
                    poi.addr = nearestPoi.address
                    val fType = when (type) {
                        "家" -> {
                            FavoriteType.FavoriteTypeHome
                        }

                        "公司" -> {
                            FavoriteType.FavoriteTypeCompany
                        }

                        else -> {
                            FavoriteType.FavoriteTypePoi
                        }
                    }
                    Timber.i("addFavorite fType=$fType")

                    if (fType == FavoriteType.FavoriteTypePoi) {
                        return when (poi.id) {
                            userBusiness.getHomePoi()?.id -> "Home_Fail"
                            userBusiness.getCompanyPoi()?.id -> "Company_Fail"
                            else -> return if (userBusiness.addFavorite(poi, type = fType)) poi.name else null
                        }
                    } else {
                        return if (userBusiness.addFavorite(poi, type = fType)) poi.name else null
                    }
                }
                return null
            }

            else -> {
                Timber.i("nearestSearch Fail $result")
                return null
            }
        }
    }

    override fun startPlanRoute(name: String, lon: Double, lat: Double) {
        if (!openMap()) {
            return
        }
        val poi = POIFactory.createPOI(name, GeoPoint(lon, lat))
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartPlanRoute, poi = poi))
    }

    override suspend fun startPlanRoute(viaName: String, endName: String) {

        val viaKeywordResult = mSearchBusiness.keywordSearch(viaName, isParallel = true)
        var errorCode = ""
        var viaPOI = when (viaKeywordResult.status) {
            Status.SUCCESS -> {
                viaKeywordResult.data?.let { data ->
                    SearchDataConvertUtils.blPoiSearchResultToHmiResult(data, SearchPoiBizType.NORMAL).searchInfo?.poiResults?.get(0)
                }
            }

            else -> {
                errorCode = viaKeywordResult.throwable.toString()
                null
            }

        }
        if (viaPOI == null) {
            val viaSuggestionResult = mSearchBusiness.suggestionSearch(viaName, isParallel = true)
            viaPOI = when (viaSuggestionResult.status) {
                Status.SUCCESS -> {
                    viaSuggestionResult.data?.let { data ->
                        SearchDataConvertUtils.convertSuggestionTipToPoiList(data.tipList)?.get(0)
                    }
                }

                else -> {
                    errorCode = viaSuggestionResult.throwable.toString()
                    null
                }

            }
        }

        val endKeywordResult = mSearchBusiness.keywordSearch(endName, isParallel = true)
        var endPOI = when (endKeywordResult.status) {
            Status.SUCCESS -> {
                endKeywordResult.data?.let { data ->
                    SearchDataConvertUtils.blPoiSearchResultToHmiResult(data, SearchPoiBizType.NORMAL).searchInfo?.poiResults?.get(0)
                }
            }

            else -> {
                errorCode = endKeywordResult.throwable.toString()
                null
            }

        }
        if (endPOI == null) {
            val endSuggestionResult = mSearchBusiness.suggestionSearch(endName, isParallel = true)
            endPOI = when (endSuggestionResult.status) {
                Status.SUCCESS -> {
                    endSuggestionResult.data?.let { data ->
                        SearchDataConvertUtils.convertSuggestionTipToPoiList(data.tipList)?.get(0)
                    }
                }

                else -> {
                    errorCode = viaKeywordResult.throwable.toString()
                    null
                }

            }
        }
        if (viaPOI == null || endPOI == null) {
            Timber.i("viaPOI = $viaPOI,endPOI = $endPOI,errorCode = $errorCode")
        } else {
            mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartPlanRoute, poi = endPOI, viaPoi = arrayListOf(viaPOI)))
        }

    }

    override fun startPlanRoute(name: String, lon: Double, lat: Double, viaName: String, viaLon: Double, viaLat: Double) {
        val poi = POIFactory.createPOI(name, GeoPoint(lon, lat))
        val viaPoi = POIFactory.createPOI(viaName, GeoPoint(viaLon, viaLat))
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartPlanRoute, poi = poi, viaPoi = arrayListOf(viaPoi)))
    }

    override fun startPlanRoute(end: POI?, viaPois: List<POI>?) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartPlanRoute, poi = end, viaPoi = viaPois))
    }

    override fun startPlanRoute(from: POI?, end: POI?, viaPois: List<POI>?) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartPlanRoute, fromPoi = from, poi = end, viaPoi = viaPois))
    }

    override fun startNavi(name: String, lon: Double, lat: Double) {
        val poi = POIFactory.createPOI(name, GeoPoint(lon, lat))
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartNavi, poi = poi))
    }

    override fun startNavi(end: POI?, viaPois: List<POI>?) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartNavi, poi = end, viaPoi = viaPois))
    }

    override fun startNavi(from: POI?, end: POI?, viaPois: List<POI>?) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartNavi, fromPoi = from, poi = end, viaPoi = viaPois))
    }

    override fun startNaviWhenHaveRoute() {
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.StartNaviWhenHasRoute))
    }

    override fun chooseRoute(routeIndex: Int) {
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.ChooseRoute, data = routeIndex.toString()))
    }

    override fun switchRoute(): Pair<Boolean, String> {
        if (mNaviBusiness.isRealNavi()) {
            val pathSize = (NaviComponent.getInstance().naviPath?.vecPaths?.size ?: mRouteRequestController.carRouteResult?.pathResult?.size) ?: 1
            val pathIndex = mRouteRequestController.carRouteResult?.focusIndex ?: 0
            if (pathSize > 1) {
                val selectIndex = (pathIndex + 1) % pathSize
                Timber.i("switchRoute pathSize = $pathSize,pathIndex = $pathIndex,selectIndex = $selectIndex")
                mRouteRequestController.carRouteResult?.pathResult?.run {
                    mNaviBusiness.changeNaviPath(this[selectIndex].pathID)
                    return Pair(true, "已切换路线")
                }
            } else {
                return Pair(false, "当前只有一条路线哦")
            }
        } else {
            return Pair(false, "当前不在导航中哦")
        }
        return Pair(false, "路线切换失败")
    }

    override fun addWayToPointOnRoutePage(vaiPoint: POI) {
        if (!openMap()) {
            return
        }
        mRouteBusiness.addWayPoint(vaiPoint)
    }

    override fun deleteWayToPointOnRoutePage(vaiPoint: POI) {
        if (!openMap()) {
            return
        }
        mRouteBusiness.deleteViaPoi(vaiPoint)
    }

    override fun deleteWayToPointOnRoutePage() {
        if (!openMap()) {
            return
        }
        mRouteBusiness.deleteAllViaPoi()
    }

    override fun alongRouteSearch(name: String): Boolean {
        if (mRouteBusiness.isPlanRouteing() || mNaviRepository.isRealNavi()) {
            if (!openMap()) {
                return false
            }
            mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.AlongWaySearch, data = name))
            return true
        }
        return false
    }

    override fun previewRoute() {
        if (mNaviRepository.isNavigating()) {
            mNaviBusiness.showPreview(1)
        }
    }

    override fun exitPreviewRoute() {
        if (mNaviRepository.isNavigating()) {
            mNaviBusiness.exitPreview()
        }
    }

    override fun switchParallelRoute(type: Int) {
        TODO("Not yet implemented")
    }

    override fun switchRouteStrategy(routePreference: String) {
        TODO("Not yet implemented")
    }

    override fun requestRouteStrategy(): String {
        TODO("Not yet implemented")
    }

    override fun requestHomeOrWorkAddress(type: String) {
        when (type) {
            "家" -> {
                val homeList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
                if (!homeList.isNullOrEmpty()) {
                    mapCommand.postValue(
                        MapCommandBean(
                            mapCommandType = MapCommandType.RequestHomeAddress,
                            poi = ConverUtils.converSimpleFavoriteItemToPoi(homeList[0])
                        )
                    )
                }
            }

            "公司" -> {
                val companyList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
                if (!companyList.isNullOrEmpty()) {
                    mapCommand.postValue(
                        MapCommandBean(
                            mapCommandType = MapCommandType.RequestCompanyAddress,
                            poi = ConverUtils.converSimpleFavoriteItemToPoi(companyList[0])
                        )
                    )
                }
            }
        }
    }

    override fun setAutoZoom(autoZoom: Boolean) {
        mNaviBusiness.setAutoZoom(autoZoom)
    }

    override fun setMapMute(mute: Boolean) {
        mNavigationSettingBusiness.setConfigKeyMute(if (mute) 1 else 0)
    }

    override fun setVoiceBroadcastMode(mode: Int) {
        mNavigationSettingBusiness.setConfigKeyBroadcastMode(mode)
    }

    override fun naviToFavorite(): String {
        //导航到收藏地
        val pois = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypePoi, true)
        if (pois.isNullOrEmpty()) {
            return ""
        } else {
            if (pois.size > 1) {
                openFavoritePage()
                return "openFavoritePage"
            } else {
                startPlanRoute(ConverUtils.converSimpleFavoriteItemToPoi(pois[0]), null)
                return pois[0].name
            }
        }
    }

    override fun openFavoritePage(): Boolean {
        if (!openMap()) {
            return false
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.OpenFavoritePage))
        return true
    }

    override fun openGroupPage(isLongClick: Boolean, isPress: Boolean): Boolean {
        //未激活，返回
        Timber.i(
            "openGroupPage isLongClick = $isLongClick, isPress = $isPress, isActivate = ${activationMapBusiness.isActivate()}, isAgreement = ${
                activationMapBusiness
                    .isAgreement()
            }, isOpenAgreement = ${activationMapBusiness.isOpenAgreement}"
        )
        if (!activationMapBusiness.isActivate()) {
            return false
        }
        if (!activationMapBusiness.isAgreement() && !activationMapBusiness.isOpenAgreement) {
            return false
        }

        val isJoinCall = customTeamBusiness.isJoinCall.value
        Timber.i("openGroupPage isJoinCall = $isJoinCall")
        if (isLongClick) { //长按事件
            if (isJoinCall == true) {
                if (isPress) {
                    customTeamBusiness.addCallUser()
                } else {
                    customTeamBusiness.removeCallUser()
                }
            } else if (!isPress) {
                AppUtils.startOrBringActivityToFront(mContext)
                mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.OpenGroupPage))
            }
        } else if (!isPress) { //短按事件
            AppUtils.startOrBringActivityToFront(mContext)
            mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.OpenGroupPage))
        }
        return true
    }

    override fun openSettingsPage(): Boolean {
        if (!openMap()) {
            return false
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.OpenSettingPage))
        return true
    }

    override fun openSearchPage() {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.OpenSearchPage))
    }

    override fun openNaviPage() {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.OpenNaviPage))
    }

    override fun showPoiCard(poi: POI) {
        if (!openMap()) {
            return
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.ShowPoiCard, poi = poi))
    }

    override fun showPoiDetail(poi: POI) {
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.ShowPoiDetail, poi = poi))
    }

    /**
     * 跳转设置家/公司地址页面
     */
    override fun openModifyHomeCompanyAddressPage(type: String): Boolean {
        if (!openMap()) {
            return false
        }
        mapCommand.postValue(
            MapCommandBean(
                mapCommandType = MapCommandType.OpenModifyHomeCompanyAddressPage, data = type
            )
        )
        return true
    }

    override fun searchHomeCompanyAddressResultPage(type: String, keyword: String): Boolean {
        if (!openMap()) {
            return false
        }
        mapCommand.postValue(
            MapCommandBean(
                mapCommandType = MapCommandType.SearchHomeCompanyAddressResultPage, pair = Pair(
                    keyword, if (type == "家")
                        0 else 1
                )
            )
        )
        return true
    }

    override fun confirm(): Boolean {
        if (!openMap()) {
            return false
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.Confirm))
        return true
    }

    override fun closeSettingsPage(): Boolean {
        if (!openMap()) {
            return false
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.CloseSettingPage))
        return true
    }

    override fun posRank(rank: Pair<String, Int>): Boolean {
        if (!openMap()) {
            return false
        }
        Timber.i("posRank rank = $rank")
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.PosRank, pair = rank))
        return true
    }

    override fun pageRank(rank: Pair<String, Int>): Boolean {
        if (!openMap()) {
            return false
        }
        Timber.i("pageRank rank = $rank")
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.PageRank, pair = rank))
        return true
    }

    override fun searchAhaTrip(cityItemInfo: CityItemInfo?, day: String?, keyword: String?): Boolean {
        if (!openMap()) {
            return false
        }
        Timber.i("searchAhaTrip cityItemInfo = $cityItemInfo,day = $day,keyword = $keyword")
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.SearchAhaTrip, data = keyword, day = day, cityItemInfo = cityItemInfo))
        return true
    }

    override fun ahaTripCollect(isCollect: Boolean): Boolean {
        if (!openMap()) {
            return false
        }
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.AhaTripCollect, data = isCollect.toString()))
        return true
    }

    override fun openAhaTripDetailPage(tab: String): Boolean {
        if (!openMap()) {
            return false
        }
        Timber.i("openAhaTripDetailPage tab = $tab")
        mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.OpenAhaTripDetailPage, data = tab))
        return true
    }

}