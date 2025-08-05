package com.desaysv.psmap.ui.search

import android.annotation.SuppressLint
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
import com.autonavi.gbl.search.model.SearchEnrouteScene
import com.autonavi.gbl.user.msgpush.model.AimPushMsg
import com.autosdk.bussiness.common.AlongWaySearchPoi
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.utils.GsonManager
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.common.utils.CommonUtil
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/29
 * @description
 */

@HiltViewModel
class SearchAimPoiPushModel @Inject constructor(
    private val pushMessageBusiness: PushMessageBusiness,
    private val mAosBusiness: AosBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val netWorkManager: NetWorkManager,
    private val customTeamBusiness: CustomTeamBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val mUserBusiness: UserBusiness,
    private var mINaviRepository: INaviRepository,
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val mSearchBusiness: SearchBusiness
) : ViewModel() {
    val hasAimData = MutableLiveData(false) //我的消息是否有数据
    val isLoading = MutableLiveData(true)
    val loginLoading = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val themeChange = skyBoxBusiness.themeChange()
    var send2carPushMessages: ArrayList<AimPushMsg> = ArrayList() //send2car消息列表
    val routeErrorMessage: LiveData<String> = mRouteBusiness.routeErrorMessage
    val naviErrorMessage: LiveData<String> = mNaviBusiness.naviErrorMessage//单个结果
    val singleResult: MutableLiveData<SearchResultBean?> = MutableLiveData()

    //结果列表
    val searchResultListLiveData: MutableLiveData<List<SearchResultBean>?> = MutableLiveData()

    fun isLogin(): Boolean {
        return settingAccountBusiness.isLogin()
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun setParentPoiSelect(poi: POI?, focus: Boolean, position: Int) {
        if (focus) {
            //更新地图中心点为选中的poi
            mSearchBusiness.updateMapCenter(poi)
        }
        //修改poi的焦点态
        mSearchBusiness.setFocus(BizSearchType.BizSearchTypePoiAlongRoute, position.toString(), focus)
    }

    //清除搜索图层上显示的数据
    fun clearAllSearchItems() {
        mSearchBusiness.clearAllSearchItems()
    }

    fun updateMapCenter(poi: POI?) {
        //更新地图中心点为选中的poi
        mSearchBusiness.updateMapCenter(poi)
    }

    fun setSearchLayerClickObserver(observer: ILayerClickObserver, removeOnly: Boolean) {
        mSearchBusiness.setSearchLayerClickObserver(observer, removeOnly)
    }

    //获取消息列表
    fun initMessageData() {
        hasAimData.postValue(false)
        isLoading.postValue(true)
        getMessageData()
    }

    //在搜索图层上显示当前父节点数据
    fun showSearchResultListData() {
        searchResultListLiveData.value?.let { list ->
            val showPoiList: ArrayList<BizSearchAlongWayPoint> =
                SearchDataConvertUtils.getBizSearchAlongPoints(list.map { it.poi }, mINaviRepository.isNavigating())
            mSearchBusiness.updateSearchAlongRoutePoi(showPoiList)
        }
    }

    //是否收藏
    fun isFavorited(poi: POI): Boolean {
        Timber.i("isFavorited poi = $poi")
        return mUserBusiness.isFavorited(poi)
    }

    fun showSinglePoiView(resultBean: SearchResultBean?) {
        Timber.i("showSinglePoiView() called with: resultBean = ${resultBean?.poi?.name}")
        singleResult.postValue(resultBean)
    }


    //获取本地保存的消息列表
    @SuppressLint("CheckResult")
    fun getMessageData() {
        Timber.i("getMessageData() called")
        if (isLogin().not()) {
            hasAimData.postValue(false)
            isLoading.postValue(false)
            return
        }
        viewModelScope.launch {
            send2carPushMessages.clear()
            val send2carMsgs = pushMessageBusiness.getSend2carPushMsg() //获取send2car消息列表
            Timber.i("getMessageData() called send2carMsgs $send2carMsgs")
            val uid = settingAccountBusiness.getAccountProfile()?.uid ?: ""
            if (send2carMsgs != null && send2carMsgs.size > 0) {
                for (i in 0 until send2carMsgs.size) {
                    if (pushMessageBusiness.isPoiMsg(send2carMsgs[i]) && send2carMsgs[i].aimPoiMsg.userId == uid) {
                        send2carPushMessages.add(send2carMsgs[i])
                    }
                }
            }

            val alongWayPois = send2carPushMessages.map { aimPushMsg ->
                Timber.i("getMessageData() called aimPushMsg = ${GsonManager.getInstance().toJson(aimPushMsg.aimPoiMsg)}")
                async {
                    getSearchBeanByPoiId(aimPushMsg)
                }
            }.map { it.await() }

            hasAimData.postValue(alongWayPois.isNotEmpty())
            searchResultListLiveData.postValue(alongWayPois)
            isLoading.postValue(false)
        }
    }

    private suspend fun getSearchBeanByPoiId(msg: AimPushMsg): SearchResultBean {
        val searchResultBean: SearchResultBean = SearchResultBean().apply {
            poi = AlongWaySearchPoi().apply {
                name = msg.aimPoiMsg.content.name
                point = GeoPoint(msg.aimPoiMsg.content.lon.toDouble() / 1000000, msg.aimPoiMsg.content.lat.toDouble() / 1000000)
                id = msg.aimPoiMsg.content.poiId
                addr = msg.aimPoiMsg.content.address
                val location = mLocationBusiness.getLastLocation()
                val startPoint = Coord2DDouble(location.longitude, location.latitude)
                val endPoint = Coord2DDouble(msg.aimPoiMsg.content.lon.toDouble() / 1000000, msg.aimPoiMsg.content.lat.toDouble() / 1000000)
                dis = CommonUtil.distanceUnitTransform(BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint).toLong())
            }
            createTime = switchDate(msg.aimPoiMsg.createTime)
        }


        if (msg.aimPoiMsg.content.poiId.isNullOrEmpty()) {
            return searchResultBean
        }
        val result = mSearchBusiness.searchAlongWayIdq(
            poiId = msg.aimPoiMsg.content.poiId,
            naviScene = if (mINaviRepository.isNavigating()) SearchEnrouteScene.Navi else SearchEnrouteScene.BeforeNavi
        )
        when (result.status) {
            Status.SUCCESS -> {
                result.data?.let { data ->
                    val list = SearchDataConvertUtils.convertSearchEnrouteResultToPoiList(data)
                    if (list.isNotEmpty()) {
                        val poi = list.first()
                        val poiBean = SearchResultBean()
                        poiBean.poi = poi
                        poiBean.createTime = switchDate(msg.aimPoiMsg.createTime)
                        return poiBean
                    } else {
                        return searchResultBean
                    }
                }
            }

            Status.ERROR -> {
                Timber.i("getSearchBeanByPoiId Status.ERROR")
                return searchResultBean
            }

            else -> {
                Timber.i("getSearchBeanByPoiId else")
                return searchResultBean
            }
        }
        return searchResultBean
    }

    private fun switchDate(date: String):String {
        var switchTime = ""
        try {
            val c = Calendar.getInstance()
            c.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date)
            val month = c[Calendar.MONTH] + 1
            val day = c[Calendar.DAY_OF_MONTH]
            switchTime = month.toString() + "月" + day + "日"
        } catch (e: ParseException) {
            e.printStackTrace();
        }
        return switchTime
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
    /**
     * 添加途经点
     */
    fun addWayPoint(poi: POI?) = mNaviBusiness.addWayPoint(poi)

    fun addDestination(poi: POI) {
        customTeamBusiness.reqUpdateDestination(poi)
    }
}