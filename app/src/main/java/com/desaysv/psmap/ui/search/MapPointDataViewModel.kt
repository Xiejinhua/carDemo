package com.desaysv.psmap.ui.search

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.OperatorPosture
import com.autonavi.gbl.map.model.MapLabelItem
import com.autonavi.gbl.map.model.MapLabelType.LABEL_Type_OPENLAYER
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.Observer.MapGestureObserver
import com.autosdk.bussiness.map.Observer.MapViewObserver
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.common.utils.CommonUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.MyTeamBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/29
 * @description
 */

@HiltViewModel
class MapPointDataViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val mSearchBusiness: SearchBusiness,
    private val mUserBusiness: UserBusiness,
    private val mMyTeamBusiness: MyTeamBusiness,
    private val customTeamBusiness: CustomTeamBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val netWorkManager: NetWorkManager,
    private val mMapController: MapController,
    private val mAosBusiness: AosBusiness,
    private val mMapBusiness: MapBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {

    //显示扎点
    val showViaPoi = mMapBusiness.routeShowViaPoi

    val poiCardDistanceAndTime: LiveData<String> = showViaPoi.map { value ->
        value?.let {
            if ((!TextUtils.isEmpty(it.poi.arriveTimes))) {
                it.poi.distance + " • " + it.poi.arriveTimes
            } else {
                it.poi.distance
            }
        }.toString()
    }
    val poiName: LiveData<String> = showViaPoi.map { value ->
        value?.let {
            if (it.cardType == MapPointCardData.PoiCardType.TYPE_CAR_LOC)
                context.getString(R.string.sv_map_my_position)
            else it.poi.name
        } ?: ""
    }

    val phoneNumber: LiveData<String> = showViaPoi.map { value ->
        value?.let {
            if (it.cardType == MapPointCardData.PoiCardType.TYPE_CAR_LOC)
                ""
            else it.poi.phone
        } ?: ""
    }

    val poiCardIsFavorite: LiveData<Boolean> = showViaPoi.map { value ->
        value?.run {
            var fPoi = poi
            if (!poi.childPois.isNullOrEmpty() && this.poi.childIndex != -1) {
                fPoi = poi.childPois[this.poi.childIndex]
            }
            mUserBusiness.isFavorited(fPoi)
        } == true
    }

    val showError: LiveData<Boolean> = showViaPoi.map { value ->
        value?.showError == true
    }

    val showLoading: LiveData<Boolean> = showViaPoi.map { value ->
        value?.showLoading == true
    }

    private val _showChild = MutableLiveData(false)
    val showChild: LiveData<Boolean> = _showChild

    private val _moreChild = MutableLiveData(false)
    val moreChild: LiveData<Boolean> = _moreChild

    //主图
    private val mMapView: MapView? by lazy {
        mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    val routeErrorMessage: LiveData<String> = mRouteBusiness.routeErrorMessage
    val naviErrorMessage: LiveData<String> = mNaviBusiness.naviErrorMessage
    val setToast: LiveData<String> = mNaviBusiness.setToast
    val isNight = skyBoxBusiness.themeChange()

    /**
     * 比例尺监听，动态改变车标
     */
    private val mOnMapLevelChangedObserver = object : MapViewObserver() {
        override fun onClickLabel(l: Long, mapLabelItems: java.util.ArrayList<MapLabelItem>?) {
            super.onClickLabel(l, mapLabelItems)
            Timber.i("mOnMapLevelChangedObserver.onClickLabel() $l, ${mapLabelItems?.size}")
            mapLabelItems?.get(0)?.let {
                Timber.i("onClickLabel $it")
                val mapToLonLat = OperatorPosture.mapToLonLat(it.pixel20X.toDouble(), it.pixel20Y.toDouble())
                val poi = POIFactory.createPOI(it.name, GeoPoint(mapToLonLat.lon, mapToLonLat.lat), it.poiid)
                if (LABEL_Type_OPENLAYER != it.type) {
                    mMapBusiness.searchPoiCardInfo(MapPointCardData.PoiCardType.TYPE_LABEL, poi)
                }
            }
        }
    }

    /**
     * 图层点击事件
     */
    private val mGestureObserver = object : MapGestureObserver() {
        override fun onLongPress(engineId: Long, px: Long, py: Long) {
            super.onLongPress(engineId, px, py)
            val geoPoint = mMapView?.operatorPosture?.screenToLonLat(px.toDouble(), py.toDouble())?.let {
                GeoPoint(it.lon, it.lat)
            }
            mMapBusiness.searchPoiCardInfo(
                MapPointCardData.PoiCardType.TYPE_LONG_CLICK,
                POIFactory.createPOI("", geoPoint, "")
            )
        }
    }

    init {
        mMapView?.addGestureObserver(mGestureObserver)
        mMapController.addMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mOnMapLevelChangedObserver)
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
        Timber.i("isFavorited poi = $poi")
        return poi?.let {
            mUserBusiness.isFavorited(poi)
        } ?: false

    }

    fun addDestination(poi: POI) {
        customTeamBusiness.reqUpdateDestination(poi)
    }

    override fun onCleared() {
        Timber.i("SearchViewModel onCleared")
        mMapView?.removeGestureObserver(mGestureObserver)
        mMapController.removeMapViewObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mOnMapLevelChangedObserver)
        mMapBusiness.resetShowPoi()
        super.onCleared()
    }

    suspend fun getDisTime(poi: POI) {
        val location = mLocationBusiness.getLastLocation()
        val startPoint = Coord2DDouble(location.longitude, location.latitude)
        val endPoint = Coord2DDouble(poi.point.longitude, poi.point.latitude)
        poi.distance = CommonUtils.showDistance(BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint))
        Timber.i("getDisTime disStr = $poi.distance")

        if (netWorkManager.isNetworkConnected()) {
            //网络尝试获取到达时间
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

    fun updateMapCenter(poi: POI?) {
        //更新地图中心点为选中的poi
        mSearchBusiness.updateMapCenter(poi)
    }

    fun setFollowMode(follow: Boolean, bPreview: Boolean = false) = mMapBusiness.setFollowMode(follow, bPreview)

    fun showCustomTypePoint1(coord3DDouble: Coord3DDouble) = mSearchBusiness.showCustomTypePoint1(coord3DDouble)

    fun hideCustomTypePoint1() = mSearchBusiness.hideCustomTypePoint1()

    fun updatePointCardChildPoiIndex(index: Int) {
        showViaPoi.value?.run {
            this.poi.childIndex = index
            mMapBusiness.resetShowPoi(showViaPoi.value)
        }
    }

    fun showChild(show: Boolean, moreChild: Boolean) {
        _showChild.value = show
        _moreChild.value = moreChild
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
}