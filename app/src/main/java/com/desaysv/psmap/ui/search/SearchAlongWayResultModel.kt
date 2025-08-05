package com.desaysv.psmap.ui.search

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.layer.model.BizSearchAlongWayPoint
import com.autonavi.gbl.layer.model.BizSearchType
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.search.model.DeepinfoPoi
import com.autonavi.gbl.search.model.SearchEnrouteResult
import com.autonavi.gbl.search.model.SearchEnrouteScene
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.common.utils.CommonUtil
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.data.IRouteRepository
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.business.JsonStandardProtocolManager
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/1/29
 * @description
 */

@HiltViewModel
class SearchAlongWayResultModel @Inject constructor(
    private val mSearchBusiness: SearchBusiness,
    private val mAosBusiness: AosBusiness,
    private val mUserBusiness: UserBusiness,
    private val netWorkManager: NetWorkManager,
    private val mLocationBusiness: LocationBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val mMapBusiness: MapBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private var mINaviRepository: INaviRepository,
    private val jsonStandardProtocolManager: JsonStandardProtocolManager,
    private val routeRepository: IRouteRepository,
    private val mapBusiness: MapBusiness,
    @ProvidesHiltModule.DefaultMapCommand private val defaultMapCommand: IMapCommand
) : ViewModel() {

    private var commandRequestSearchCategoryBean: CommandRequestSearchCategoryBean? = null

    //搜索结果列表
    val searchResultListLiveData: MutableLiveData<List<SearchResultBean>?> = MutableLiveData()

    //loading界面
    val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData(true)

    //搜索是否成功
    val isSearchSuccess: MutableLiveData<Boolean> = MutableLiveData(true)

    //是否是单个结果
    val isSingleResult = MutableLiveData(false)

    //单个结果
    val singleResult: MutableLiveData<SearchResultBean?> = MutableLiveData()

    //设为途经点按钮是否可点击
    var clickEnable = MutableLiveData(false)

    val routeErrorMessage: LiveData<String> = mRouteBusiness.routeErrorMessage
    val naviErrorMessage: LiveData<String> = mNaviBusiness.naviErrorMessage

    val isNight = skyBoxBusiness.themeChange()
    val setToast: LiveData<String> = mNaviBusiness.setToast

    val searchListOperaLiveData = jsonStandardProtocolManager.searchListOperaLiveData

    var totalPage: MutableLiveData<Int> = MutableLiveData(1) //总页码

    var currentPage: MutableLiveData<Int> = MutableLiveData(1) //当前页码

    var isListTop: MutableLiveData<Boolean> = MutableLiveData(true) //是否处于列表顶部即第⼀条 true:处于列表顶部 false:未处于列表顶部

    var isListBottom: MutableLiveData<Boolean> = MutableLiveData(false) //是否处于列表底部即最后⼀条 true:处于列表底部 false:未处于列表底部

    val result: SearchEnrouteResult? = null

    val mapCommand = defaultMapCommand.getMapCommand()

    /**
     * 保存传过来的 搜索数据
     */
    @Synchronized
    fun setSearchBean(commandBean: CommandRequestSearchCategoryBean?) {
        if (commandBean != null) {
            commandRequestSearchCategoryBean = commandBean
        }
    }

    /**
     * 获取 搜索数据
     */
    @Synchronized
    fun getSearchBean(): CommandRequestSearchCategoryBean? {
        return commandRequestSearchCategoryBean
    }

    fun startAlongWaySearch(searchCategory: String) {
        isLoadingLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val result = mSearchBusiness.searchAlongWayKeyword(
                searchCategory,
                naviScene = if (mINaviRepository.isNavigating()) SearchEnrouteScene.Navi else SearchEnrouteScene.BeforeNavi
            )
            when (result.status) {
                Status.SUCCESS -> {
                    val alongWayPois = ArrayList<SearchResultBean>()
                    val pois = ArrayList<POI>()
                    result.data?.let { data ->
                        SearchDataConvertUtils.convertSearchEnrouteResultToPoiList(data).forEach {
                            val searchResultBean = SearchResultBean()
                            searchResultBean.poi = it
                            alongWayPois.add(searchResultBean)
                            pois.add(it)
                        }
                    }
                    searchResultListLiveData.postValue(alongWayPois)
                    if (alongWayPois.size == 1) {
                        alongWayPois[0].let { poi ->
//                            if (TextUtils.isEmpty(resultBean.distance)) {
//                                resultBean.distance = getDisTime(poi)
//                            }
//                            resultBean.isFavorite = isFavorited(poi)
//                            Timber.i("isFavorite poi = $poi,isFavorite = ${resultBean.isFavorite}")
                            singleResult.postValue(poi)
                            setParentPoiSelect(poi.poi, true, 0)
//                            mUserBusiness.addSearchHistory(poi = resultBean.poi)
                        }
                        isSingleResult.postValue(true)
                    } else {
//                        mUserBusiness.addSearchHistory(keyWord)
                        isSingleResult.postValue(false)
                        singleResult.postValue(null)
                    }
                    isSearchSuccess.postValue(alongWayPois.isNotEmpty())
                    jsonStandardProtocolManager.protocolAlongWaySearchResponse(result.data)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.AlongWaySearch, pois.toList())
                }

                Status.ERROR -> {
                    Timber.i("startAlongWaySearch Status.ERROR")
                    isSearchSuccess.postValue(false)
                    jsonStandardProtocolManager.protocolAlongWaySearchResponse(null)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.AlongWaySearch, null)
                }

                Status.LOADING -> Timber.i("search Loading")
            }
            isLoadingLiveData.postValue(false)
        }
    }

    fun getDistance(poi: POI?): String {
        var disStr = ""
        poi?.let {
            val location = mLocationBusiness.getLastLocation()
            val startPoint = Coord2DDouble(location.longitude, location.latitude)
            val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
            var dis: Double = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint)
            if (dis < 1000.00) {
                var distanceStr = java.lang.Double.toString(dis)
                distanceStr = distanceStr.substring(0, distanceStr.indexOf('.'))
                disStr = distanceStr + "m"
            }
            var distanceStr = java.lang.Double.toString(dis / 1000)
            distanceStr = distanceStr.substring(0, distanceStr.indexOf('.') + 2)
            disStr = distanceStr + "km"
        }
        return disStr
    }

    //是否收藏
    fun isFavorited(poi: POI): Boolean {
        Timber.i("isFavorited poi = $poi")
        return mUserBusiness.isFavorited(poi)
    }

    suspend fun getDisTime(poi: POI): String {
        val location = mLocationBusiness.getLastLocation()
        val startPoint = Coord2DDouble(location.longitude, location.latitude)
        val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
        //先本地计算距离
        var disStr: String = CommonUtils.showDistance(BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint))
        Timber.i("getDisTime disStr = $disStr")
        if (netWorkManager.isNetworkConnected()) {
            //网络尝试获取到达时间
            val result = mAosBusiness.getDisTime(endPoint = endPoint)
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let {
                        if (TextUtils.isEmpty(it.distance)) {//网络计算距离没有
                            if (TextUtils.isEmpty(it.travel_time)) { //距离没有 时间没有
                                //直接使用上面的距离文字 disStr
                            } else { //距离没有 时间有
                                disStr = "距您$disStr | 约${CommonUtil.switchFromSecond(it.travel_time.toInt())}"
                            }
                        } else if (TextUtils.isEmpty(it.travel_time)) {//距离有，时间没有
                            disStr = "距您${(if (TextUtils.isEmpty(poi.dis)) CommonUtil.distanceUnitTransform(it.distance.toLong()) else poi.dis)}"
                        } else {//有距离 有时间
                            disStr =
                                "距您${(if (TextUtils.isEmpty(poi.dis)) CommonUtil.distanceUnitTransform(it.distance.toLong()) else poi.dis)} | 约" +
                                        "${CommonUtil.switchFromSecond(it.travel_time.toInt())}"
                        }
                    }
                }

                Status.ERROR -> Timber.i(result.throwable.toString())
                else -> {}
            }
        }

        return disStr
    }

    suspend fun getDeepInfo(resultBean: SearchResultBean): SearchResultBean {
        resultBean.poi?.let { poi ->
            if (resultBean.deepinfoPoi == null) {
                getDeepInfo(poi).let { deepInfo ->
                    resultBean.deepinfoPoi = deepInfo
                }
            }
            if (TextUtils.isEmpty(resultBean.disAndTime)) {
                resultBean.disAndTime = getDisTime(poi)
            }
            val str = StringBuilder()
            if (!TextUtils.isEmpty(poi.alongSearchDistance)) {
                str.append(poi.alongSearchDistance)
            }
            if (!TextUtils.isEmpty(poi.alongSearchTravelTime)) {
                str.append("·${poi.alongSearchTravelTime}")
            }
            resultBean.disAndTime = str.toString();

        }
        return resultBean
    }

    //获取深度信息
    suspend fun getDeepInfo(poi: POI): DeepinfoPoi? {
        Timber.i("getDeepInfo poi = $poi")
        var deepInfo: DeepinfoPoi? = null
        val result = mSearchBusiness.deepInfoSearch(poi)
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.let {
                    deepInfo = it.deepinfoPoi
                }
            }

            Status.ERROR -> {
                Timber.i(result.throwable.toString())
            }

            else -> {}
        }
        return deepInfo
    }

    //清除搜索图层上显示的数据
    fun clearAllSearchItems() {
        mSearchBusiness.clearAllSearchItems()
    }

    //设置搜索图层的显示与隐藏
    fun setSearchLayer(hidden: Boolean) = mSearchBusiness.setSearchLayerHidden(hidden)

    //显示全览
    fun showPreview() = routeRepository.showPreview()

    //退出全览
    fun exitPreview() = mSearchBusiness.exitPreview()

    fun setSearchLayerClickObserver(observer: ILayerClickObserver, removeOnly: Boolean) {
        mSearchBusiness.setSearchLayerClickObserver(observer, removeOnly)
    }

    //在搜索图层上显示当前父节点数据
    fun showSearchResultListData() {
        searchResultListLiveData.value?.let { list ->
            val showPoiList: ArrayList<BizSearchAlongWayPoint> =
                SearchDataConvertUtils.getBizSearchAlongPoints(list.map { it.poi }, mINaviRepository.isNavigating())
            mSearchBusiness.updateSearchAlongRoutePoi(showPoiList)
        }
    }

    fun setParentPoiSelect(poi: POI?, focus: Boolean, position: Int) {
        if (focus) {
            //更新地图中心点为选中的poi
            mSearchBusiness.updateMapCenter(poi)
        }
        //修改poi的焦点态
        mSearchBusiness.setFocus(BizSearchType.BizSearchTypePoiAlongRoute, position.toString(), focus)
    }


    fun updateMapCenter(poi: POI?) {
        //更新地图中心点为选中的poi
        mSearchBusiness.updateMapCenter(poi)
    }

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        viewModelScope.launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }

    /**
     * 添加途经点
     */
    fun addWayPointPlan(poi: POI?) = mRouteBusiness.addWayPoint(poi)

    fun showSinglePoiView(resultBean: SearchResultBean?) {
        Timber.i("showSinglePoiView() called with: resultBean = ${resultBean?.poi?.name}")
        singleResult.postValue(resultBean)
    }

    /**
     * 添加途经点
     */
    fun addWayPoint(poi: POI?) = mNaviBusiness.addWayPoint(poi)

    /**
     * 语音搜索结果列表状态透出
     */
    fun protocolAlongWaySearchResultListChangeExecute(choice: Int = -1, isPlanRoute: Boolean = false, isback: Boolean = false) {
        viewModelScope.launch {
            jsonStandardProtocolManager.protocolAlongWaySearchResultListChangeExecute(
                poinum = searchResultListLiveData.value?.size ?: 0,
                isFirstPage = currentPage.value == 1,
                isLastPage = currentPage.value == totalPage.value,
                choice = choice,
                planRoute = isPlanRoute,
                back = isback,
                isListTop = isListTop.value ?: true,
                isListBottom = isListBottom.value ?: true,
                curPage = currentPage.value ?: 1,
                totalPage = totalPage.value ?: 1,
                searchResult = result
            )
        }
    }

    fun notifyPosRankCommandResult(result: Boolean, tips: String) {
        Timber.i("notifyPosRankCommandResult result = $result,tips = $tips")
        defaultMapCommand.notifyMapCommandResult(MapCommandType.PosRank, Pair(result, tips))
    }

    fun notifyConfirmCommandResult(result: Boolean, tips: String) {
        Timber.i("notifyConfirmCommandResult result = $result,tips = $tips")
        defaultMapCommand.notifyMapCommandResult(MapCommandType.Confirm, Pair(result, tips))
    }

    fun notifyPageRankCommandResult(result: Boolean, tips: String) {
        Timber.i("notifyPageRankCommandResult result = $result,tips = $tips")
        defaultMapCommand.notifyMapCommandResult(MapCommandType.PageRank, Pair(result, tips))
    }

    fun backToNavi() {
        Timber.i("backToNavi() called")
        mNaviBusiness.naviBackCurrentCarPosition()
        mapBusiness.backToNavi.postValue(true) //回到导航页面
        defaultMapCommand.notifyMapCommandResult(MapCommandType.Confirm, "好的，已为您继续导航")
    }
    fun setFollowMode(follow: Boolean, bPreview: Boolean = false) = mMapBusiness.setFollowMode(follow, bPreview)

    fun setAutoZoom(autoZoom: Boolean) = mNaviBusiness.setAutoZoom(autoZoom)
}