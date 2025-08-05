package com.desaysv.psmap.ui.search

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.user.usertrack.model.HistoryRouteItem
import com.autonavi.gbl.user.usertrack.model.SearchHistoryItem
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.search.Offline.ErrorCodeNoData
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.search.request.SearchPoiBizType
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.bean.SearchThrowable
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.MyTeamBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/29
 * @description
 */

@HiltViewModel
class SearchAddHomeModel @Inject constructor(
    private val mSearchBusiness: SearchBusiness,
    private val mUserBusiness: UserBusiness,
    private val mMyTeamBusiness: MyTeamBusiness,
    private val customTeamBusiness: CustomTeamBusiness,
    private val mMapBusiness: MapBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val netWorkManager: NetWorkManager,
    private val mapDataBusiness: MapDataBusiness,
    private val mAosBusiness: AosBusiness,
    private val mSettingAccountBusiness: SettingAccountBusiness
) : ViewModel() {
    //历史搜索记录
    val historyPoiListLiveData: MutableLiveData<List<SearchHistoryBean>> = MutableLiveData()

    //预搜索记录
    val suggestPoiListLiveData: MutableLiveData<List<SearchHistoryBean>> = MutableLiveData()

    //是否预搜索界面
    var isSuggestionLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //无预搜索结果
    var noSuggestionVisibilityLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //无离线城市数据
    var noCityDataLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //无离线城市数据提示
    var noCityDataText: MutableLiveData<String> = MutableLiveData()//输入的关键字

    //无历史搜索记录
    var noHistoryVisibilityLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //searchBox的删除、loading状态
    var buttonType: MutableLiveData<Int> = MutableLiveData(0)//0隐藏 1显示删除按钮 2显示loading

    //输入的关键字
    var inputKeyWord: MutableLiveData<String> = MutableLiveData()//输入的关键字

    val gpsState = mMapBusiness.gpsState

    private var suggestionJob: Job? = null

    val isLoading = MutableLiveData(true) //是否加载中

    val themeChange = skyBoxBusiness.themeChange()
    //搜索框文字改变时发起预搜索
    fun onInputKeywordChanged(keyWord: String) {
        inputKeyWord.postValue(keyWord)
        if (keyWord.isNotEmpty()) {
            buttonType.postValue(2)
            searchSuggestionTip(keyWord)
        } else {
            suggestionJob?.cancel()
            buttonType.postValue(0)
            isSuggestionLiveData.postValue(false)
        }
    }

    private fun searchSuggestionTip(keyWord: String) {
        keyWord.let { key ->
            suggestionJob = viewModelScope.launch(Dispatchers.IO) {
                val result = mSearchBusiness.suggestionSearch(key, SearchPoiBizType.NORMAL)
                buttonType.postValue(1)
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { data ->
                            val suggestionList = ArrayList<SearchHistoryBean>()
                            SearchDataConvertUtils.convertSuggestionTipToPoiList(data.tipList)?.map {
                                suggestionList.add(SearchHistoryBean(type = 3, poi = it, updateTime = 0))
                            }
                            suggestPoiListLiveData.postValue(suggestionList)
                            noSuggestionVisibilityLiveData.postValue(suggestionList.isEmpty())
                            isSuggestionLiveData.postValue(true)
                        }
                        noCityDataLiveData.postValue(false)
                    }

                    Status.ERROR -> {
                        val throwable = result.throwable
                        if (throwable is SearchThrowable) {
                            when (throwable.errorCode) {
                                ErrorCodeNoData -> {
                                    noCityDataText.postValue(
                                        ResUtil.getString(
                                            R.string.sv_search_no_city_data_title,
                                            getCurrentCity()?.cityName
                                        )
                                    )
                                    isSuggestionLiveData.postValue(true)
                                    noCityDataLiveData.postValue(true)
                                }
                            }
                        }
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun refreshHistoryData() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
//            val searchHistoryItems: java.util.ArrayList<SearchHistoryItem>? = mUserBusiness.getSearchHistory()
            val historyRouteItems: ArrayList<HistoryRouteItem>? = mUserBusiness.getHistoryRouteItem()
            var historyPoiList = ArrayList<SearchHistoryBean>()

//            searchHistoryItems?.map { item ->
//                if (TextUtils.isEmpty(item.name) && TextUtils.isEmpty(item.address)) {
//                    mUserBusiness.delSearchHistory(item, SyncMode.SyncModeNow)
//                } else {
//                    val searchRouteHistoryBean = SearchHistoryBean(
//                        type = 2,
//                        updateTime = item.update_time,
//                        poi = SearchDataConvertUtils.convertSearchHistoryToPoi(item),
//                        isFavorite = isFavorited(SearchDataConvertUtils.convertSearchHistoryToPoi(item))
//                    )
//                    historyPoiList.add(searchRouteHistoryBean)
//                }
//            }

            historyRouteItems?.map { item ->
                if (TextUtils.isEmpty(item.toPoi.name) && TextUtils.isEmpty(item.toPoi.address)) {
                    mUserBusiness.delHistoryRoute(item, SyncMode.SyncModeNow)
                } else {
                    val searchRouteHistoryBean = SearchHistoryBean(
                        type = 6,
                        updateTime = item.updateTime,
                        poi = ConverUtils.convertHistoryRouteItemToPOI(item),
                        midPois = ConverUtils.convertHistoryRoutePoiToMidPois(item.midPoi),
                        routeId = item.id,
                        isFavorite = isFavorited(ConverUtils.convertHistoryRouteItemToPOI(item))
                    )
                    historyPoiList.add(searchRouteHistoryBean)
                }
            }
            invertOrderList(historyPoiList)
//            historyPoiList = removeDuplicate(historyPoiList)
            historyPoiListLiveData.postValue(historyPoiList)
            noHistoryVisibilityLiveData.postValue(historyPoiList.isEmpty())
            isLoading.postValue(false)
        }
    }

    fun addHome(poi: POI, isFavorite: Boolean): Boolean {
        val homeList = mUserBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
        val flag = if (isFavorite){
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
        if (flag) {
            viewModelScope.launch { mMapBusiness.checkCommutingScenariosFlag() }
        }
        return flag
    }

    fun addCompany(poi: POI, isFavorite: Boolean): Boolean {
        val companyList = mUserBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
        val flag = if (isFavorite){
            if (!companyList.isNullOrEmpty()) {
                mUserBusiness.removeFavorites(companyList)
            }
            val result = mUserBusiness.updateFavorite(ConverUtils.converPOIToFavoriteItem(poi, FavoriteType.FavoriteTypeCompany), SyncMode.SyncModeNow)
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
        if (flag) {
            viewModelScope.launch { mMapBusiness.checkCommutingScenariosFlag() }
        }
        return flag
    }

    //是否收藏
    fun isFavorited(poi: POI?): Boolean {
        return poi?.let {
            mUserBusiness.isFavorited(poi)
        } ?: false

    }

    fun addDestination(poi: POI) {
        customTeamBusiness.reqUpdateDestination(poi)
    }

    override fun onCleared() {
        Timber.i("SearchViewModel onCleared")
        super.onCleared()
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        return mSettingAccountBusiness.isLogin()
    }

    //将List按照时间倒序排列
    private fun invertOrderList(list: ArrayList<SearchHistoryBean>) {
        //按距离排序
        list.sortWith { bean1: SearchHistoryBean, bean2: SearchHistoryBean ->
            var date1 = Date(bean1.updateTime)
            var date2 = Date(bean2.updateTime)
            date2.compareTo(date1)
        }
    }

    private fun removeDuplicate(list: ArrayList<SearchHistoryBean>): ArrayList<SearchHistoryBean> {
        //去除重复name
        val distinctNameList = list.distinctBy { it.poi?.name }
        return ArrayList(distinctNameList)
    }

    fun startSync() {
        if (null != mSettingAccountBusiness.getAccountProfile()) {
            mUserBusiness.startSync()
        }
    }

    fun getLastLocation() = mLocationBusiness.getLastLocation()

    suspend fun getLastLocationPoi(): POI? {
        var resultPoi: POI? = null
        val result =
            mSearchBusiness.nearestSearch(
                poi = POIFactory.createPOI(
                    null,
                    GeoPoint(
                        mLocationBusiness.getLastLocation().longitude,
                        mLocationBusiness.getLastLocation().latitude
                    )
                )
            )
        when (result.status) {
            Status.SUCCESS -> {
                val poiList = SearchCommonUtils.invertOrderList(result.data?.poi_list)
                poiList?.let {
                    if (it.isNotEmpty()) {
//                        val list = SearchCommonUtils.invertOrderList(it)
                        it[0]?.let { searchInfo ->
                            resultPoi = POIFactory.createPOI()
                            resultPoi?.let { poi ->
                                poi.name = searchInfo.name
                                poi.id = searchInfo.poiid
                                poi.addr = searchInfo.address
                                poi.point = GeoPoint(mLocationBusiness.getLastLocation().longitude, mLocationBusiness.getLastLocation().latitude)
                                resultPoi = poi
                            }
                        }
                    }
                }
            }

            else -> {
                resultPoi = null
            }
        }
        return resultPoi
    }

    fun resetShowPoi(mMapPointCardData: MapPointCardData? = null) = mMapBusiness.resetShowPoi(mMapPointCardData)

    fun searchPoiCardInfo(@MapPointCardData.PoiCardType cardType: Int, poi: POI) =
        mMapBusiness.searchPoiCardInfo(cardType, poi)

    suspend fun getDisTime(poi: POI) {
        val location = mLocationBusiness.getLastLocation()
        val startPoint = Coord2DDouble(location.longitude, location.latitude)
        val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
        poi.distance = CommonUtils.showDistance(BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint))
        Timber.i("getDisTime disStr = $poi.distance")
        //网络尝试获取到达时间
        if (netWorkManager.isNetworkConnected()) {
            val result = mAosBusiness.getDisTime(endPoint = endPoint)
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.travel_time?.run {
                        if (!TextUtils.isEmpty(this))
                            poi.arriveTimes = CommonUtil.switchFromSecond(this.toInt())
                    }
                }

                Status.ERROR -> Timber.i(result.throwable.toString())
                else -> {}
            }
        }

    }

    fun getCurrentCity(): CityItemInfo? {
        val currentCityAdCode =
            mapDataBusiness.getAdCodeByLonLat(getLastLocation().longitude, getLastLocation().latitude)
        return mapDataBusiness.getCityInfo(currentCityAdCode)
    }

}