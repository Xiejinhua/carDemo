package com.desaysv.psmap.ui.search

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.layer.model.BizSearchParentPoint
import com.autonavi.gbl.layer.model.BizSearchType
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.search.model.DeepinfoPoi
import com.autonavi.gbl.search.model.KeywordSearchResultV2
import com.autonavi.gbl.search.model.SearchClassifyInfo
import com.autonavi.gbl.search.model.SearchPoiLocRes
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.search.Offline.ErrorCodeNoData
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.search.request.SearchPoiBizType
import com.autosdk.bussiness.search.request.SearchQueryType
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils.getClassifyValue
import com.autosdk.bussiness.search.utils.SearchResultUtils
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.SearchThrowable
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.Result
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.MapCommandParamType
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.business.BluetoothBusiness
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.JsonStandardProtocolManager
import com.desaysv.psmap.model.business.MyTeamBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/1/29
 * @description
 */

@HiltViewModel
class SearchResultModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val mSearchBusiness: SearchBusiness,
    private val mUserBusiness: UserBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val mAosBusiness: AosBusiness,
    private val mMyTeamBusiness: MyTeamBusiness,
    private val customTeamBusiness: CustomTeamBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val netWorkManager: NetWorkManager,
    private val mRouteBusiness: RouteBusiness,
    private val mMapBusiness: MapBusiness,
    private val iCarInfoProxy: ICarInfoProxy,
    private val mBluetoothBusiness: BluetoothBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val jsonStandardProtocolManager: JsonStandardProtocolManager,
    private val sharePreferenceFactory: SharePreferenceFactory,
    @ProvidesHiltModule.DefaultMapCommand private val defaultMapCommandImpl: IMapCommand
) : ViewModel() {
    var totalPage: MutableLiveData<Int> = MutableLiveData(0) //总页码

    var currentPage: MutableLiveData<Int> = MutableLiveData(0) //当前页码

    var isListTop: MutableLiveData<Boolean> = MutableLiveData(true) //是否处于列表顶部即第⼀条 true:处于列表顶部 false:未处于列表顶部

    var isListBottom: MutableLiveData<Boolean> = MutableLiveData(false) //是否处于列表底部即最后⼀条 true:处于列表底部 false:未处于列表底部

    var isRefresh = -1  //0 上拉加载上一页 1下拉加载上一页

    private var commandRequestSearchBean: CommandRequestSearchBean? = null

    private var currentCity: CityItemInfo? = null

    //搜索结果列表
    val searchResultListLiveData: MutableLiveData<List<SearchResultBean>> = MutableLiveData()

    //loading界面
    val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData(true)

    //搜索是否成功
    val isSearchSuccess: MutableLiveData<Boolean> = MutableLiveData(true)

    //失败分类
    val failType: MutableLiveData<Int> = MutableLiveData(0) // 1 其他 , 2该城市无离线数据

    //无离线城市数据提示
    var noCityDataText: MutableLiveData<String> = MutableLiveData()//输入的关键字

    //是否是单个结果
    val isSingleResult = MutableLiveData(false)

    //单个结果
    val singleResult: MutableLiveData<SearchResultBean?> = MutableLiveData()

    val classifyInfo: MutableLiveData<SearchClassifyInfo> = MutableLiveData()

    val showClassifyInfo: MutableLiveData<Boolean> = MutableLiveData()

    //捷途专属分类咖啡店
    val isShowCustomPoi: MutableLiveData<Boolean> = MutableLiveData(false)

    val setToast = MutableLiveData<String>()

    //是否一键补能界面
    val isSearchCharge: MutableLiveData<Boolean> = MutableLiveData()

    val isNight = skyBoxBusiness.themeChange()

    val isFirst = MutableLiveData(true)

    val isListLoadOrRefreash = MutableLiveData(false)

    val searchListOperaLiveData = jsonStandardProtocolManager.searchListOperaLiveData

    val tabSelect = MutableLiveData(0) //0：加油站 1：充电站

    val mapCommand = defaultMapCommandImpl.getMapCommand()


    val isFuel = MutableLiveData(iCarInfoProxy.isT1JFL2ICE()) // 是否是燃油版车型

    //增加上一页的缓存，防止出现切换为上一页后结果不同的情况
    private var keywordSearchResultPageMap: HashMap<Int, Result<KeywordSearchResultV2>> = HashMap()
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
    private var classify: String? = null
    private var retainState: String? = null

    /**
     * 保存传过来的 搜索数据
     */
    @Synchronized
    fun setSearchBean(commandBean: CommandRequestSearchBean?) {
        if (commandBean != null) {
            commandRequestSearchBean = commandBean
            currentCity = commandRequestSearchBean?.city ?: getCurrentCity()
            commandRequestSearchBean?.city = currentCity
            isSearchCharge.postValue(commandBean.type == CommandRequestSearchBean.Type.SEARCH_CHARGE)
        }
    }

    /**
     * 获取 搜索数据
     */
    @Synchronized
    fun getSearchBean(): CommandRequestSearchBean? {
        return commandRequestSearchBean
    }

    private fun startKeywordSearch(
        keyWord: String?,
        curPoi: POI? = null,
        page: Int = 1,
        withLoading: Boolean = true,
        classify: String? = null,
        retainState: String? = null
    ) {
        if (withLoading) {
            isLoadingLiveData.postValue(true)
        }
        this.classify = classify
        this.retainState = retainState
        if (page == 1) {
            keywordSearchResultPageMap.clear()
        }
        Timber.i("startKeywordSearchviewModelScope is isActive: ${viewModelScope.coroutineContext[Job]?.isActive}")
        Timber.i("startKeywordSearch Dispatchers.IO is valid: ${Dispatchers.IO}")
        viewModelScope.launch(Dispatchers.IO) {
            val result = mSearchBusiness.keywordSearchV2(
                keyword = keyWord,
                curPoi = curPoi,
                size = 10,
                page = page,
                cityCode = currentCity?.cityAdcode,
                classify = classify,
                retainState = retainState
            )
            Timber.i("startKeywordSearch result = $result")
            result.data?.let { data ->
                if (data.keyword.isNullOrEmpty()) {
                    data.keyword = keyWord
                }
            }
            parseSearchResult(result, page = page)
            if (withLoading) {
                isLoadingLiveData.postValue(false)
            }
        }
    }

    private fun startSearchAround(
        keyWord: String?,
        poi: POI?,
        page: Int = 1,
        withLoading: Boolean = true,
        classify: String? = null,
        retainState: String? = null,
        range: String? = null,
    ) {
        if (withLoading) {
            isLoadingLiveData.postValue(true)
        }
        this.classify = classify
        this.retainState = retainState

        if (page == 1) {
            keywordSearchResultPageMap.clear()
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = mSearchBusiness.keywordSearchV2(
                keyWord,
                searchPoiBizType = SearchPoiBizType.AROUND,
                searchQueryType = SearchQueryType.AROUND,
                curPoi = poi,
                size = 10,
                page = page,
                cityCode = currentCity?.cityAdcode,
                range = range,
                classify = classify,
                retainState = retainState
            )
            parseSearchResult(result, page = page)
            if (withLoading) {
                isLoadingLiveData.postValue(false)
            }
        }
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
                                disStr = "$disStr • 约${CommonUtil.switchFromSecond(it.travel_time.toInt())}"
                            }
                        } else if (TextUtils.isEmpty(it.travel_time)) {//距离有，时间没有
                            disStr = (if (TextUtils.isEmpty(poi.dis)) CommonUtil.distanceUnitTransform(it.distance.toLong()) else poi.dis)
                        } else {//有距离 有时间
                            disStr = "${(if (TextUtils.isEmpty(poi.dis)) CommonUtil.distanceUnitTransform(it.distance.toLong()) else poi.dis)} • 约" +
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

    //获取深度信息
    suspend fun getDeepInfo(poi: POI): DeepinfoPoi? {
        Timber.i("getDeepInfo poi = $poi")
        var deepInfo: DeepinfoPoi? = null
        val result = mSearchBusiness.deepInfoSearch(poi)
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.let {
                    deepInfo = it.deepinfoPoi

                    Timber.i("getDeepInfo ParkSpace = ${it.deepinfoPoi.parkinfo.sumSpace}")

                }
            }

            Status.ERROR -> {
                Timber.i(result.throwable.toString())
            }

            else -> {}
        }
        return deepInfo
    }

    //是否收藏
    fun isFavorited(poi: POI): Boolean {
        return mUserBusiness.isFavorited(poi)
    }

    fun addFavorite(poi: POI): Boolean {
        return mUserBusiness.addFavorite(poi)
    }

    fun delFavorite(poi: POI): Boolean {
        return mUserBusiness.delFavorite(poi)
    }

    //在搜索图层上显示当前父节点数据
    fun showSearchResultListData() {
        mSearchBusiness.clearAllSearchItems()
        searchResultListLiveData.value?.let { list ->
            val showPoiList: ArrayList<BizSearchParentPoint> = ArrayList()
            for ((index, searchResultBean) in list.withIndex()) {
                val point = SearchDataConvertUtils.convertPoi2SearchParentPoint(searchResultBean.poi!!)
                point.index = index
                showPoiList.add(point)
            }
            mSearchBusiness.updateSearchParentPoi(showPoiList)
        }
    }

    //在搜索图层上显示单个数据
    fun showSearchSingleResultData() {
        Timber.i("showSearchSingleResultData() called")
        singleResult.value?.let { bean ->
            val showPoiList: ArrayList<BizSearchParentPoint> = ArrayList()
            val point = SearchDataConvertUtils.convertPoi2SearchParentPoint(bean.poi!!)
            point.index = 0
            showPoiList.add(point)
            mSearchBusiness.updateSearchParentPoi(showPoiList)
            //修改poi的焦点态
            mSearchBusiness.setFocus(BizSearchType.BizSearchTypePoiParentPoint, bean.poi?.id, true)
        }
//        showPreview()
    }

    //清除搜索图层上显示的数据
    fun clearAllSearchItems() {
        mSearchBusiness.clearAllSearchItems()
    }

    //设置搜索图层的显示与隐藏
    fun setSearchLayer(hidden: Boolean) = mSearchBusiness.setSearchLayerHidden(hidden)

    //显示全览
    fun showPreview() = mSearchBusiness.showPreview()

    //退出全览
    fun exitPreview() = mSearchBusiness.exitPreview()

    fun setFollowMode(follow: Boolean, bPreview: Boolean = false) = mMapBusiness.setFollowMode(follow, bPreview)

    fun setAutoZoom(autoZoom: Boolean) = mNaviBusiness.setAutoZoom(autoZoom)

    fun setSearchLayerClickObserver(observer: ILayerClickObserver, removeOnly: Boolean) {
        mSearchBusiness.setSearchLayerClickObserver(observer, removeOnly)
    }

    fun setParentPoiSelect(poi: POI?, focus: Boolean) {
        if (focus) {
            //更新地图中心点为选中的poi
            mSearchBusiness.updateMapCenter(poi)
            //绘制子poi
            mSearchBusiness.updateSearchChildPoi(SearchDataConvertUtils.convertPoi2BizSearchChildPoints(poi)) // 更新子点
        } else {
            //清除绘制的区域
            mSearchBusiness.clearEndPointArea()
            mSearchBusiness.updateSearchChildPoi(null) // 更新子点
        }
        //修改poi的焦点态
        mSearchBusiness.setFocus(BizSearchType.BizSearchTypePoiParentPoint, poi?.id, focus)
    }

    fun setChildPoiSelect(poi: POI?, focus: Boolean) {
        mSearchBusiness.updateMapCenter(if (focus) poi else null)
        mSearchBusiness.setFocus(BizSearchType.BizSearchTypePoiChildPoint, poi?.id, focus)
    }

    fun addSearchHistory(keyword: String? = null, poi: POI? = null) = mUserBusiness.addSearchHistory(keyword, poi)

    fun updateMapCenter(poi: POI?) {
        //更新地图中心点为选中的poi
        mSearchBusiness.updateMapCenter(poi)
    }

    fun showSinglePoiView(resultBean: SearchResultBean?) {
        Timber.i("showSinglePoiView() called with: resultBean = ${resultBean?.poi?.name}")
        singleResult.postValue(resultBean)
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

        }
        return resultBean
    }

    fun isFavorite(poi: POI) {
        mUserBusiness.isFavorited(poi)
    }

    fun addHome(poi: POI, isFavorite: Boolean): Boolean {
        val homeList = mUserBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
        val flag = if (isFavorite) {
            if (!homeList.isNullOrEmpty()) {
                mUserBusiness.removeFavorites(homeList)
            }
            val result = mUserBusiness.updateFavorite(ConverUtils.converPOIToFavoriteItem(poi, FavoriteType.FavoriteTypeHome), SyncMode.SyncModeNow)
            result == Service.ErrorCodeOK
        } else {
            if (!homeList.isNullOrEmpty()) {
                mUserBusiness.removeFavorites(homeList)
                mUserBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeHome)
            } else {
                mUserBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeHome)
            }
        }
        Timber.i("addHome $flag")
        return flag
    }

    fun addCompany(poi: POI, isFavorite: Boolean): Boolean {
        val companyList = mUserBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
        val flag = if (isFavorite) {
            if (!companyList.isNullOrEmpty()) {
                mUserBusiness.removeFavorites(companyList)
            }
            val result =
                mUserBusiness.updateFavorite(ConverUtils.converPOIToFavoriteItem(poi, FavoriteType.FavoriteTypeCompany), SyncMode.SyncModeNow)
            result == Service.ErrorCodeOK
        } else {
            if (!companyList.isNullOrEmpty()) {
                mUserBusiness.removeFavorites(companyList)
                mUserBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeCompany)
            } else {
                mUserBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeCompany)
            }
        }
        Timber.i("addCompany $flag")
        return flag
    }

    fun addDestination(poi: POI) {
        customTeamBusiness.reqUpdateDestination(poi)
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        return settingAccountBusiness.isLogin()
    }

    fun doSearch(
        commandRequestSearchBean: CommandRequestSearchBean?,
        page: Int = 1,
        withLoading: Boolean = true,
        classify: String? = null,
        retainState: String? = null,
        chargeKeyword: String? = null
    ) {
        Timber.d(
            "doSearch() called with: commandRequestSearchBean = $commandRequestSearchBean, page = $page, withLoading = $withLoading, classify = $classify, retainState = $retainState, chargeKeyword = $chargeKeyword"
        )
        commandRequestSearchBean?.let {
            when (it.type) {
                CommandRequestSearchBean.Type.SEARCH_KEYWORD,
                CommandRequestSearchBean.Type.SEARCH_HOME,
                CommandRequestSearchBean.Type.SEARCH_COMPANY,
                CommandRequestSearchBean.Type.SEARCH_TEAM_DESTINATION,
                CommandRequestSearchBean.Type.SEARCH_CUSTOM_POI,
                CommandRequestSearchBean.Type.SEARCH_KEYWORD_COLLECT,
                -> {
                    startKeywordSearch(it.keyword, it.poi, page = page, withLoading = withLoading, classify = classify, retainState = retainState)
                }

                CommandRequestSearchBean.Type.SEARCH_AROUND -> {
                    startSearchAround(
                        it.keyword,
                        it.poi,
                        page = page,
                        withLoading = withLoading,
                        classify = classify,
                        retainState = retainState,
                        range = it.range
                    )
                }

                CommandRequestSearchBean.Type.SEARCH_CHARGE -> {
                    val word = chargeKeyword ?: getChargeKeyword()
                    saveChargeKeyword(word)
                    startSearchAround(word, it.poi, page = page, withLoading = withLoading, classify = classify, retainState = retainState)
                }
            }
        }
    }

    fun getChargeKeyword(): String {
        //如果是燃油版，直接返回加油站
        if (iCarInfoProxy.isT1JFL2ICE()) {
            return "加油站"
        }
        val key = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.chargeType.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.chargeType.toString()
        }
        return mapSharePreference.getStringValue(key, "加油站")
    }

    fun saveChargeKeyword(keyword: String) {
        val key = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.chargeType.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.chargeType.toString()
        }
        mapSharePreference.putStringValue(key, keyword)
    }

    fun onListRefresh() {
        isListLoadOrRefreash.postValue(true)
        isLoadingLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            isRefresh = 0
            currentPage.value?.let { currentPage ->
                if (currentPage > 1) {
                    val cacheResult = keywordSearchResultPageMap.get(currentPage - 1)
                    if (cacheResult != null) {
                        Timber.i("onListRefresh() called with: cacheResult = $cacheResult, keywordSearchResultPageMap.size = ${keywordSearchResultPageMap.size}")
                        parseSearchResult(cacheResult, page = currentPage - 1)
                        isLoadingLiveData.postValue(false)
                    } else {
                        doSearch(commandRequestSearchBean, page = currentPage - 1, true, classify = classify, retainState = retainState)
                    }
                }
            }
        }
    }

    fun onListLoadMore() {
        isListLoadOrRefreash.postValue(true)
        isLoadingLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            isRefresh = 1
            currentPage.value?.let { currentPage ->
                totalPage.value?.let { totalPage ->
                    if (currentPage < totalPage) {
                        val cacheResult = keywordSearchResultPageMap.get(currentPage + 1)
                        if (cacheResult != null) {
                            Timber.i("onListLoadMore() called with: cacheResult = $cacheResult, keywordSearchResultPageMap.size = ${keywordSearchResultPageMap}")
                            parseSearchResult(cacheResult, page = currentPage + 1)
                            isLoadingLiveData.postValue(false)
                        } else {
                            doSearch(commandRequestSearchBean, page = currentPage + 1, true, classify = classify, retainState = retainState)
                        }
                    }
                }
            }
        }
    }

    fun getLastLocation() = mLocationBusiness.getLastLocation()

    fun getCurrentCity(): CityItemInfo? {
        val currentCityAdCode = mapDataBusiness.getAdCodeByLonLat(getLastLocation().longitude, getLastLocation().latitude)
        return mapDataBusiness.getCityInfo(currentCityAdCode)
    }


    fun onPhoneCall(phone: String) = mBluetoothBusiness.callPhone(phone)

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        //路线规划监听初始化
        mRouteBusiness.outsideInit()
        viewModelScope.launch {
            if (isNavigating()) {
                mNaviBusiness.stopNavi()
                delay(500)
            }
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }

    /**
     * 是否正在导航
     */
    fun isNavigating(): Boolean {
        return mNaviBusiness.isNavigating()
    }

    /**
     * 取消请求
     */
    fun abortAll() {
        mSearchBusiness.abortAll()
        mSearchBusiness.abortAllV2()
    }

    /**
     * 用户点击了分类搜索
     */
    fun onClassifySearch(value: String, retainState: String, searchClassifyInfo: SearchClassifyInfo) {
        if (TextUtils.isEmpty(value)) {
            return
        }
        classifyInfo.postValue(searchClassifyInfo)
        doSearch(commandRequestSearchBean, page = 1, classify = value, retainState = retainState)
    }


    /**
     * 语音搜索结果列表状态透出
     */
    fun protocolSearchResultListChangeExecute(choice: Int = -1, isPlanRoute: Boolean = false, isback: Boolean = false) {
        Timber.i("protocolSearchResultListChangeExecute() called with: choice = $choice, isPlanRoute = $isPlanRoute,isback = $isback")
        viewModelScope.launch {
            jsonStandardProtocolManager.protocolSearchResultListChangeExecute(
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
                searchResult = keywordSearchResultPageMap[currentPage.value]?.data
            )
        }
    }

    private suspend fun parseSearchResult(result: Result<KeywordSearchResultV2>, page: Int = 1) {
        when (result.status) {
            Status.SUCCESS -> {
                val searchResultPoiList = ArrayList<SearchResultBean>()
                var cityResultBeanVoice: SearchResultBean? = null
                keywordSearchResultPageMap[page] = result
                result.data?.let { data ->
                    if (classify != null) {
                        SearchDataConvertUtils.getClassifyFilter(data.classify)
                        if (data.classify?.classifyItemInfo?.categoryInfoList?.isNotEmpty() == true) {
                            classifyInfo.postValue(data.classify)
                        }
                        showClassifyInfo.postValue(
                            if (data.classify == null)
                                data.classify?.classifyItemInfo?.categoryInfoList?.isNotEmpty() == true
                            else
                                classifyInfo.value?.classifyItemInfo?.categoryInfoList?.isNotEmpty() == true
                        )
                    } else {
                        SearchDataConvertUtils.getClassifyFilter(data.classify)
                        classifyInfo.postValue(data.classify)
                        showClassifyInfo.postValue(data.classify?.classifyItemInfo?.categoryInfoList?.isNotEmpty() == true)
                    }
                    SearchDataConvertUtils.blPoiSearchResultToHmiResult(data, SearchPoiBizType.NORMAL).searchInfo.poiResults.map {
                        searchResultPoiList.add(SearchResultBean(it))
                    }
                    isSearchSuccess.postValue(searchResultPoiList.isNotEmpty())
                    if (searchResultPoiList.isNotEmpty()) {
                        currentPage.postValue(page)
                        if (page != totalPage.value || totalPage.value == 0) {
                            totalPage.postValue(if (searchResultPoiList.isNotEmpty()) CommonUtil.getPagerCount(data.total, 10) else 0)
                        }
                    }
                }
                if (searchResultPoiList.size == 1 && page == 1) {
                    isSearchSuccess.postValue(true)
                    isSingleResult.postValue(true)
                    searchResultPoiList[0].let { resultBean ->
                        resultBean.poi?.let { poi ->
                            if (TextUtils.isEmpty(resultBean.disAndTime)) {
                                resultBean.disAndTime = getDisTime(poi)
                            }
                            resultBean.isFavorite = isFavorited(poi)
                            Timber.i("isFavorite poi = $poi,isFavorite = ${resultBean.isFavorite}")
                            singleResult.postValue(resultBean)

                        }
                        mUserBusiness.addSearchHistory(poi = resultBean.poi)
                    }
                } else if (searchResultPoiList.size == 0) {
                    // 无poi数据,但有城市定位列表数据,也直接跳转详情页
                    val poiLocres: SearchPoiLocRes? = result.data?.poiLocres
                    val citylistSize = poiLocres?.total ?: 0
                    if (citylistSize > 0) {
                        isSearchSuccess.postValue(true)
                        val cityResultBean = SearchResultBean()
                        cityResultBean.poi = SearchResultUtils.getInstance().resultCityToPoi(poiLocres)
                        cityResultBean.poi?.let { poi ->
                            if (TextUtils.isEmpty(cityResultBean.disAndTime)) {
                                cityResultBean.disAndTime = getDisTime(poi)
                            }
                            cityResultBean.isFavorite = isFavorited(poi)
                            Timber.i("isFavorite poi = $poi,isFavorite = ${cityResultBean.isFavorite}")
                            singleResult.postValue(cityResultBean)
                            mUserBusiness.addSearchHistory(poi = poi)
                        }
                        isSingleResult.postValue(true)
                        cityResultBeanVoice = cityResultBean
                    } else {
                        isSearchSuccess.postValue(false)
                        mUserBusiness.addSearchHistory(result.data?.keyword)
                        singleResult.postValue(null)
                        isSingleResult.postValue(false)
                    }
                } else {
                    searchResultListLiveData.postValue(searchResultPoiList)
                    mUserBusiness.addSearchHistory(result.data?.keyword)
                    singleResult.postValue(null)
                    isSingleResult.postValue(false)
                }
                jsonStandardProtocolManager.protocolSearchResponse(result.data)
                notifyIflytekVoiceSearchResult(searchResultPoiList, cityResultBeanVoice)
            }

            Status.ERROR -> {
                Timber.i("Status.ERROR")
                Timber.i(result.throwable.toString())
                isSearchSuccess.postValue(false)
                val throwable = result.throwable
                if (throwable is SearchThrowable) {
                    Timber.i("throwable.errorCode = " + throwable.errorCode)
                    when (throwable.errorCode) {
                        ErrorCodeNoData -> {
                            noCityDataText.postValue(ResUtil.getString(R.string.sv_search_no_city_data_title, currentCity?.cityName))
                            failType.postValue(2)
                        }

                        else -> {
                            failType.postValue(1)
                        }
                    }
                }
                jsonStandardProtocolManager.protocolSearchResponse(null)
                notifyIflytekVoiceSearchResult(null)
            }

            Status.LOADING -> Timber.i("search Loading")
        }
    }

    fun doSearchByPage(page: Int) {
        Timber.i("doSearchByPage() called page = $page")
        isListLoadOrRefreash.postValue(true)
        isLoadingLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            isRefresh = 0
            currentPage.value?.let { currentPage ->
                val cacheResult = keywordSearchResultPageMap[page]
                if (cacheResult != null) {
                    Timber.i("doSearchByPage() called with: cacheResult = $cacheResult, keywordSearchResultPageMap.size = ${keywordSearchResultPageMap.size}")
                    parseSearchResult(cacheResult, page = page)
                    isLoadingLiveData.postValue(false)
                } else {
                    doSearch(commandRequestSearchBean, page = page, true, classify = classify, retainState = retainState)
                }
            }
        }
    }

    private fun notifyIflytekVoiceSearchResult(searchResultPoiList: List<SearchResultBean>?, singelPOI: SearchResultBean? = null) {
        Timber.i("notifyIflytekVoiceSearchResult() called size = ${searchResultPoiList?.size}")
        //通知讯飞搜索结果，关键字搜索，周边搜索，家/公司地址搜索 MapCommandType.KeyWordSearch MapCommandType.AroundSearch MapCommandType.SearchHomeCompanyAddressResultPage
        val poiList = if (singelPOI != null) {
            listOf(singelPOI.poi)
        } else {
            searchResultPoiList?.map {
                it.poi
            }
        }
        defaultMapCommandImpl.notifyMapCommandResult(MapCommandType.KeyWordSearch, poiList)
        defaultMapCommandImpl.notifyMapCommandResult(MapCommandType.AroundSearch, poiList)
        defaultMapCommandImpl.notifyMapCommandResult(MapCommandType.KeyWordSearchViaEnd, poiList)
        defaultMapCommandImpl.notifyMapCommandResult(MapCommandType.SearchHomeCompanyAddressResultPage, poiList)
        defaultMapCommandImpl.notifyMapCommandResult(MapCommandType.PageRank, poiList)
        defaultMapCommandImpl.notifyMapCommandResult(MapCommandType.KeyWordSearchForCollect, poiList)
        //todo 筛选酒店星级
        defaultMapCommandImpl.getMapCommandParam(MapCommandParamType.Search)?.let { params ->
            val hotelLvl = params["hotelLvl"] as? String
            Timber.i("hotelLvl = $hotelLvl")
            hotelLvl?.let {
                doHotelLevelClassifySearch(hotelLvl)
            }
        }
    }


    fun notifyPosRankCommandResult(type: MapCommandType, result: Boolean, param: Any) {
        Timber.i("notifyPageRankCommandResult type=$type result = $result,param = $param")
        defaultMapCommandImpl.notifyMapCommandResult(type, Pair(result, param))
    }

    private fun doHotelLevelClassifySearch(hotelLvl: String) {
        Timber.i("doHotelLevelClassifySearch() called with: hotelLvl = $hotelLvl")
        val classifyInfoValue = classifyInfo.value ?: return
        val categoryList = classifyInfoValue.classifyItemInfo.categoryInfoList
        val childCategoryList = categoryList.getOrNull(1)?.childCategoryInfo?.getOrNull(0)?.childCategoryInfoList ?: return

        val hotelTypeMap = mapOf(
            "1" to "经济型",
            "2" to "经济型",
            "3" to "三星",
            "4" to "四星",
            "5" to "五星"
        )

        val typeName = hotelTypeMap[hotelLvl] ?: return

        childCategoryList.forEach { item ->
            val name = item?.baseInfo?.name ?: return@forEach
            if (name.contains(typeName)) {
                Timber.i("doHotelLevelClassifySearch() called $typeName")
                onClassifySearch(
                    getClassifyValue(classifyInfoValue, 1, item.baseInfo.value),
                    classifyInfoValue.retainState,
                    classifyInfoValue
                )
            }
        }
    }

}