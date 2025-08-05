package com.desaysv.psmap.ui.search

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.layer.model.BizCustomPointInfo
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.layer.model.CustomPriorityMode
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.common.utils.CommonUtil
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.JetourPoi
import com.desaysv.psmap.model.business.ByteAutoBusiness
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */

@HiltViewModel
class JetourPoiListModel @Inject constructor(
    private val mUserBusiness: UserBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val mSearchBusiness: SearchBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val netWorkManager: NetWorkManager,
    private val mAosBusiness: AosBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val byteAutoBusiness: ByteAutoBusiness
) : ViewModel() {

    //loading界面
    val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData(true)

    val isNight = skyBoxBusiness.themeChange()

    val isByteDanceForeground = byteAutoBusiness.isByteDanceForeground

    //单个结果
    val singleResult: MutableLiveData<SearchResultBean?> = MutableLiveData()

    val jetourPoiListLiveData: MutableLiveData<ArrayList<JetourPoi>> = MutableLiveData()

    fun init() {
        Timber.i("JetourPoiListModel init")
        viewModelScope.launch (Dispatchers.IO){
            //只取前10个
            val list = byteAutoBusiness.fefreshJetourPoiListDistance().take(10) as ArrayList<JetourPoi>
            jetourPoiListLiveData.postValue(list)
        }
    }

    fun updateJetourPoiList(list: ArrayList<JetourPoi>) {
        Timber.i("JetourPoiListModel updateJetourPoiList")
        val customBottlePoints:ArrayList<BizCustomPointInfo>  = ArrayList()
        val size: Int = list.size
        for (i in 0 until size) {
            val point = BizCustomPointInfo()
            val position = Coord3DDouble(list[i].longitude, list[i].latitude, 0.0)
            point.mPos3D = position
            point.value = "${list[i].venue_name}|${CommonUtils.showDistance(list[i].distance!!.toDouble())}"
            point.id = list[i].id
            point.priorityMode = CustomPriorityMode.CustomPriorityModeAscend
            customBottlePoints.add(point)
        }
        byteAutoBusiness.updateJetourPoi(customBottlePoints)
    }

    fun showSinglePoiView(resultBean: SearchResultBean?) {
        Timber.i("showSinglePoiView() called with: resultBean = ${resultBean?.poi?.name}")
        singleResult.postValue(resultBean)
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

    fun updateMapCenter(poi: POI?) {
        //更新地图中心点为选中的poi
        mSearchBusiness.updateMapCenter(poi)
    }

    /**
     * 是否正在导航
     */
    fun isNavigating(): Boolean {
        return mNaviBusiness.isNavigating()
    }

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

    fun getSearchBeanByJetourPoi(jetourPoi: JetourPoi): POI {
        val poi = POIFactory.createPOI(
            jetourPoi.venue_name, jetourPoi.address_name,
            GeoPoint(
                jetourPoi.longitude,
                jetourPoi.latitude
            ),
            jetourPoi.id,
        ).apply {
            distance = jetourPoi.distance?.toString()
        }
        return poi
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
}