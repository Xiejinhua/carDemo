package com.desaysv.psmap.ui.search

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.search.model.DeepinfoPoi
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.user.usertrack.model.HistoryRouteItem
import com.autonavi.gbl.user.usertrack.model.SearchHistoryItem
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.search.Offline.ErrorCodeNoData
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.utils.GsonManager
import com.autosdk.bussiness.search.request.SearchPoiBizType
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.SearchThrowable
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.business.BluetoothBusiness
import com.desaysv.psmap.model.business.ByteAutoBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import com.google.gson.Gson
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
class SearchViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val mSearchBusiness: SearchBusiness,
    private val mUserBusiness: UserBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val mAosBusiness: AosBusiness,
    private var netWorkManager: NetWorkManager,
    private val mBluetoothBusiness: BluetoothBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val byteAutoBusiness: ByteAutoBusiness,
    private val mMapBusiness: MapBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory
) : ViewModel() {
    //历史搜索记录
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

    //历史搜索记录的loading状态
    var isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData(true)

    //输入的关键字
    var inputKeyWord: MutableLiveData<String> = MutableLiveData()//输入的关键字

    //单个结果
    val singleResult: MutableLiveData<SearchResultBean?> = MutableLiveData()

    val isNight = skyBoxBusiness.themeChange()

    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)

    private var commandRequestSearchBean: CommandRequestSearchBean? = null

    private var currentCity: CityItemInfo? = null

    private var suggestionJob: Job? = null

    /**
     * 保存传过来的 搜索数据
     */
    @Synchronized
    fun setSearchBean(commandBean: CommandRequestSearchBean?) {
        commandBean?.let {
            commandRequestSearchBean = commandBean
            currentCity = commandRequestSearchBean?.city ?: getCurrentCity()
            commandRequestSearchBean?.city = currentCity
        }
    }

    fun init() {
        Timber.i("SearchViewModel init")
        viewModelScope.launch(Dispatchers.IO) {
            byteAutoBusiness.init()
        }
    }

    /**
     * 获取 搜索数据
     */
    @Synchronized
    fun getSearchBean(): CommandRequestSearchBean? {
        return commandRequestSearchBean
    }

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
                val result = mSearchBusiness.suggestionSearch(key, SearchPoiBizType.NORMAL, cityCode = currentCity?.cityAdcode)
                buttonType.postValue(1)
                when (result.status) {
                    Status.SUCCESS -> {
                        Timber.i("suggestionList = ${Gson().toJson(result.data)}")
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
                        Timber.i(result.throwable.toString())
                        val throwable = result.throwable
                        if (throwable is SearchThrowable) {
                            when (throwable.errorCode) {
                                ErrorCodeNoData -> {
                                    noCityDataText.postValue(ResUtil.getString(R.string.sv_search_no_city_data_title, currentCity?.cityName))
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
        isLoadingLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
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
//                        isFavorite = isFavorited(SearchDataConvertUtils.convertSearchHistoryToPoi(item)),
//                        isHomeorCompany = isHomeOrCompany(SearchDataConvertUtils.convertSearchHistoryToPoi(item))
//                    )
//                    historyPoiList.add(searchRouteHistoryBean)
//                }
//            }

            historyRouteItems?.map { item ->
                if (TextUtils.isEmpty(item.toPoi.name) && TextUtils.isEmpty(item.toPoi.address)) {
                    mUserBusiness.delHistoryRoute(item, SyncMode.SyncModeNow)
                } else {
                    val searchRouteHistoryBean = SearchHistoryBean(
                        type = 1,
                        updateTime = item.updateTime,
                        poi = ConverUtils.convertHistoryRouteItemToPOI(item),
                        midPois = ConverUtils.convertHistoryRoutePoiToMidPois(item.midPoi),
                        routeId = item.id,
                        isFavorite = isFavorited(ConverUtils.convertHistoryRouteItemToPOI(item)),
                        isHomeorCompany = isHomeOrCompany(ConverUtils.convertHistoryRouteItemToPOI(item))
                    )
                    historyPoiList.add(searchRouteHistoryBean)
                }
            }
            invertOrderList(historyPoiList)
//            historyPoiList = removeDuplicate(historyPoiList)
            historyPoiListLiveData.postValue(historyPoiList)
            noHistoryVisibilityLiveData.postValue(historyPoiList.isEmpty())
            isLoadingLiveData.postValue(false)
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

    fun addSearchHistory(keyword: String? = null, poi: POI? = null) = mUserBusiness.addSearchHistory(keyword, poi)

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

    //是否收藏
    fun isFavorited(poi: POI?): Boolean {
        return poi?.let {
            mUserBusiness.isFavorited(poi)
        } ?: false
    }

    private fun isHomeOrCompany(poi: POI?): Boolean {
        poi?.let {
            val homePoi = mUserBusiness.getHomePoi()
            val companyPoi = mUserBusiness.getCompanyPoi()
            if (poi.point.latitude != 0.0
                && isClose(poi.point.latitude, homePoi?.point?.latitude)
                && poi.point.longitude != 0.0
                && isClose(poi.point.longitude, homePoi?.point?.longitude)
            ) {
                return true
            }
            if (poi.point.latitude != 0.0
                && isClose(poi.point.latitude, companyPoi?.point?.latitude)
                && poi.point.longitude != 0.0
                && isClose(poi.point.longitude, companyPoi?.point?.longitude)
            ) {
                return true
            }
        }
        return false

    }

    private fun isClose(value: Double?, target: Double?, epsilon: Double = 0.00001): Boolean {
        if (value != null && target != null) {
            return kotlin.math.abs(value - target) <= epsilon
        }
        return false
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

    fun setFollowMode(follow: Boolean, bPreview: Boolean = false) = mMapBusiness.setFollowMode(follow, bPreview)

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        //路线规划监听初始化
        mRouteBusiness.outsideInit()
        viewModelScope.launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }

    fun showSinglePoiView(resultBean: SearchResultBean?) {
        Timber.i("showSinglePoiView() called with: resultBean = ${resultBean?.poi?.name}")
        singleResult.postValue(resultBean)
    }

    fun onPhoneCall(phone: String) = mBluetoothBusiness.callPhone(phone)

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

    fun doDouYinConfirm(flag: Boolean) {
        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.active)?.putBooleanValue(
            MapSharePreference.SharePreferenceKeyEnum.douYinConfirm, flag
        )
    }

    fun getDouYinConfirm(): Boolean {
        return sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.active).getBooleanValue(
            MapSharePreference.SharePreferenceKeyEnum.douYinConfirm, false
        )
    }
}