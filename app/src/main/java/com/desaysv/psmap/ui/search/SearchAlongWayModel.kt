package com.desaysv.psmap.ui.search

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.search.model.SearchEnrouteScene
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.user.usertrack.model.HistoryRouteItem
import com.autonavi.gbl.user.usertrack.model.SearchHistoryItem
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.search.Offline.ErrorCodeNoData
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.utils.GsonManager
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.bean.SearchThrowable
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
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
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */

@HiltViewModel
class SearchAlongWayModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val mSearchBusiness: SearchBusiness,
    private val mUserBusiness: UserBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val mMapBusiness: MapBusiness,
    private val mNaviBusiness: NaviBusiness,
    private var netWorkManager: NetWorkManager,
    private var mINaviRepository: INaviRepository,
    private val sharePreferenceFactory: SharePreferenceFactory
) : ViewModel() {
    //历史导航记录记录
    val historyPoiListLiveData: MutableLiveData<List<SearchHistoryBean>> = MutableLiveData()

    //预搜索记录
    val suggestPoiListLiveData: MutableLiveData<List<SearchHistoryBean>> = MutableLiveData()

    //无历史搜索记录
    var noHistoryVisibilityLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //无预搜索结果
    var noSuggestionVisibilityLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //无离线城市数据
    var noCityDataLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //无离线城市数据提示
    var noCityDataText: MutableLiveData<String> = MutableLiveData()//输入的关键字

    //是否预搜索界面
    var isSuggestionLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //searchBox的删除、loading状态
    var buttonType: MutableLiveData<Int> = MutableLiveData(0)//0隐藏 1显示删除按钮 2显示loading

    //输入的关键字
    var inputKeyWord: MutableLiveData<String> = MutableLiveData()//输入的关键字

    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)

    val routeErrorMessage: LiveData<String> = mRouteBusiness.routeErrorMessage
    val naviErrorMessage: LiveData<String> = mNaviBusiness.naviErrorMessage
    val setToast: LiveData<String> = mNaviBusiness.setToast

    private var currentCity: CityItemInfo? = null

    private var suggestionJob: Job? = null

    //搜索框文字改变时发起预搜索
    fun onInputKeywordChanged(keyWord: String) {
        inputKeyWord.postValue(keyWord)
        if (keyWord.isNotEmpty()) {
            buttonType.postValue(2)
            searchAlongKeyword(keyWord)
        } else {
            suggestionJob?.cancel()
            buttonType.postValue(0)
            isSuggestionLiveData.postValue(false)
        }
    }

    private fun searchAlongKeyword(keyWord: String) {
        keyWord.let { key ->
            suggestionJob = viewModelScope.launch(Dispatchers.IO) {
                val result = mSearchBusiness.searchAlongWayKeyword(
                    key,
                    naviScene = if (mINaviRepository.isNavigating()) SearchEnrouteScene.Navi else SearchEnrouteScene.BeforeNavi
                )
                buttonType.postValue(1)
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { data ->
                            val suggestionList = ArrayList<SearchHistoryBean>()
                            SearchDataConvertUtils.convertSearchEnrouteResultToPoiList(data)?.map {
                                suggestionList.add(SearchHistoryBean(type = 5, poi = it, updateTime = 0))
                            }
                            Timber.i("suggestionList.size ${suggestionList.size}")
                            for (item in suggestionList) {
                                Timber.i("result${GsonManager.getInstance().toJson(item)}")
                            }

                            suggestPoiListLiveData.postValue(suggestionList)
                            noSuggestionVisibilityLiveData.postValue(suggestionList.isEmpty())
                            isSuggestionLiveData.postValue(true)
                        }
                        noCityDataLiveData.postValue(false)
                    }

                    Status.ERROR -> {
                        Timber.i(result.throwable.toString())
                        val throwable = result.throwable
                        if (throwable is SearchThrowable) {
                            when (throwable.errorCode) {
                                ErrorCodeNoData -> {
                                    noCityDataText.postValue(ResUtil.getString(R.string.sv_search_no_city_data_title, currentCity?.cityName))
                                    noCityDataLiveData.postValue(true)
                                }
                            }
                        }
                        noSuggestionVisibilityLiveData.postValue(true)
                        isSuggestionLiveData.postValue(true)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun refreshHistoryData() {
        viewModelScope.launch(Dispatchers.IO) {
            val historyRouteItems: ArrayList<HistoryRouteItem>? = mUserBusiness.getHistoryRouteItem()
            var historyPoiList = ArrayList<SearchHistoryBean>()

            historyRouteItems?.map { item ->
                if (TextUtils.isEmpty(item.toPoi.name) && TextUtils.isEmpty(item.toPoi.address)) {
                    mUserBusiness.delHistoryRoute(item, SyncMode.SyncModeNow)
                } else {
                    val searchRouteHistoryBean = SearchHistoryBean(
                        type = 4,
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
        }
    }


    fun clearSearchHistory(): Boolean {

        //删除历史记录时，将常用城市重置为北上广深
        val key = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.commonCityList.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.commonCityList.toString()
        }
        mapSharePreference.putStringValue(key, "")
        return mUserBusiness.clearSearchHistory()
    }

    fun startSync() {
        if (null != settingAccountBusiness.getAccountProfile()) {
            mUserBusiness.startSync()
        }
    }

    //将List按照时间倒序排列
    private fun invertOrderList(list: ArrayList<SearchHistoryBean>) {
        //按距离排序
        list.sortWith { bean1: SearchHistoryBean, bean2: SearchHistoryBean ->
            val date1 = Date(bean1.updateTime)
            val date2 = Date(bean2.updateTime)
            date2.compareTo(date1)
        }
    }

    private fun removeDuplicate(list: ArrayList<SearchHistoryBean>): ArrayList<SearchHistoryBean> {
        //去除重复name
        val distinctNameList = list.distinctBy { it.poi?.name }
        return ArrayList(distinctNameList)
    }

    //是否收藏
    fun isFavorited(poi: POI?): Boolean {
        return poi?.let {
            mUserBusiness.isFavorited(poi)
        } ?: false

    }

    //添加收藏
    fun addFavorite(poi: POI): Boolean {
        Timber.i("poi = ${GsonManager.getInstance().toJson(poi)}")
        return mUserBusiness.addFavorite(poi)
    }

    //删除收藏
    fun delFavorite(poi: POI): Boolean {
        return mUserBusiness.delFavorite(poi)
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        return settingAccountBusiness.isLogin()
    }

    //删除搜索历史
    fun delSearchHistory(item: SearchHistoryBean, @SyncMode.SyncMode1 mode: Int = SyncMode.SyncModeNow): Boolean {
        val searchHistoryItem = SearchHistoryItem()
        searchHistoryItem.name = item.poi?.name
        return mUserBusiness.delSearchHistory(searchHistoryItem, mode) == Service.ErrorCodeOK
    }

    //删除历史路线
    fun delHistoryRoute(item: SearchHistoryBean, @SyncMode.SyncMode1 mode: Int = SyncMode.SyncModeNow): Boolean {
        val historyRouteItem = HistoryRouteItem()
        historyRouteItem.id = item.routeId
        return mUserBusiness.delHistoryRoute(historyRouteItem, mode) == Service.ErrorCodeOK
    }

    override fun onCleared() {
        Timber.i("SearchViewModel onCleared")
        super.onCleared()
    }

    fun getLastLocation() = mLocationBusiness.getLastLocation()

    fun isNetworkConnected() = netWorkManager.isNetworkConnected()

    fun getCurrentCity(): CityItemInfo? {
        val currentCityAdCode = mapDataBusiness.getAdCodeByLonLat(getLastLocation().longitude, getLastLocation().latitude)
        return mapDataBusiness.getCityInfo(currentCityAdCode)
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

    fun searchPoiCardInfo(@MapPointCardData.PoiCardType cardType: Int, poi: POI) = mMapBusiness.searchPoiCardInfo(cardType, poi)

    /**
     * 添加途经点
     */
    fun addWayPoint(poi: POI?) = mNaviBusiness.addWayPoint(poi)

    fun setFollowMode(follow: Boolean, bPreview: Boolean = false) = mMapBusiness.setFollowMode(follow, bPreview)
}