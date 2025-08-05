package com.desaysv.psmap.model.impl

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.map.OperatorPosture
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.search.request.SearchPoiBizType
import com.autosdk.bussiness.search.request.SearchQueryType
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.model.bean.MapCommandBean
import com.desaysv.psmap.model.bean.MapCommandType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 对接IOV语音命令实现类,导航不显示搜索结果，语音显示搜索结果
 * 因为会通过语音API返回搜索数据，所以需要定制
 */
@Singleton
class IOVVoiceSearchMapCommandImpl @Inject constructor(
    @ApplicationContext private val mContext: Context,
    private val mSearchBusiness: SearchBusiness,
    private val mNaviRepository: INaviRepository,
    private val mRouteRequestController: RouteRequestController,
    private val userBusiness: UserBusiness,
    private val mLocationBusiness: LocationBusiness,
) : ICustomSearchCommand {

    private val _mapCommand = MutableLiveData<MapCommandBean>()
    val mapCommand: LiveData<MapCommandBean> = _mapCommand

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun keywordSearch(keyword: String) {
        ioScope.launch {
            val result = mSearchBusiness.keywordSearch(keyword)
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { data ->
                        val poiList = SearchDataConvertUtils.blPoiSearchResultToHmiResult(
                            data,
                            SearchPoiBizType.NORMAL
                        )?.searchInfo?.poiResults
                        Timber.i("poiList size ${poiList?.size}")
                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_KeyWordSearch,
                                searchPoiList = poiList
                            )
                        )
                    }
                }

                Status.ERROR -> {
                    Timber.i("keywordSearch Status.ERROR")
                    _mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.IOV_VOICE_KeyWordSearch))
                }

                Status.LOADING -> Timber.i("keywordSearch Loading")
            }
        }

    }

    override fun keywordSearch(keyword: String, lon: Double, lat: Double) {
        ioScope.launch {
            val result = mSearchBusiness.keywordSearch(
                keyword,
                curPoi = POIFactory.createPOI(keyword, GeoPoint(lon, lat))
            )
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { data ->
                        val poiList = SearchDataConvertUtils.blPoiSearchResultToHmiResult(
                            data,
                            SearchPoiBizType.NORMAL
                        ).searchInfo?.poiResults
                        Timber.i("poiList size ${poiList?.size}")
                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_KeyWordSearch,
                                searchPoiList = poiList
                            )
                        )
                    }
                }

                Status.ERROR -> {
                    Timber.i("keywordSearch Status.ERROR")
                    _mapCommand.postValue(MapCommandBean(mapCommandType = MapCommandType.IOV_VOICE_KeyWordSearch))
                }

                else -> {
                    Timber.i("keywordSearch Fail $result")
                }
            }
        }
    }

    override fun aroundSearch(keyword: String) {
        ioScope.launch {
            val result = mSearchBusiness.keywordSearch(
                keyword,
                searchPoiBizType = SearchPoiBizType.AROUND,
                searchQueryType = SearchQueryType.AROUND
            )
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { data ->
                        val list = SearchDataConvertUtils.blPoiSearchResultToHmiResult(
                            data,
                            SearchPoiBizType.NORMAL
                        ).searchInfo.poiResults
                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_AroundSearch,
                                searchPoiList = list
                            )
                        )
                    }
                }

                Status.ERROR -> {
                    Timber.i("aroundSearch Status.ERROR")
                    Timber.i(result.throwable.toString())
                }

                else -> {
                    Timber.i("aroundSearch Fail $result")
                }
            }
        }
    }

    override fun aroundSearch(keyword: String, lon: Double, lat: Double) {
        ioScope.launch {
            val result = mSearchBusiness.keywordSearch(
                keyword,
                searchPoiBizType = SearchPoiBizType.AROUND,
                searchQueryType = SearchQueryType.AROUND,
                curPoi = POIFactory.createPOI(keyword, GeoPoint(lon, lat))
            )
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { data ->
                        val list = SearchDataConvertUtils.blPoiSearchResultToHmiResult(
                            data,
                            SearchPoiBizType.NORMAL
                        ).searchInfo.poiResults
                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_AroundSearch,
                                searchPoiList = list
                            )
                        )
                    }
                }

                Status.ERROR -> {
                    Timber.i("aroundSearch Status.ERROR")
                    Timber.i(result.throwable.toString())
                }

                else -> {
                    Timber.i("aroundSearch Fail $result")
                }
            }
        }
    }

    override fun aroundSearchByLocationName(keyword: String, locationName: String) {
        when (locationName) {
            "家" -> {
                val home = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
                if (!home.isNullOrEmpty()) {
                    val poi = home[0]
                    val coord2DDouble =
                        OperatorPosture.mapToLonLat(poi.point_x.toDouble(), poi.point_y.toDouble())
                    aroundSearch(poi.name, coord2DDouble.lon, coord2DDouble.lat)
                } else {
                    Timber.i("家地址未设置")
                }
            }

            "公司" -> {
                val company =
                    userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
                if (!company.isNullOrEmpty()) {
                    val poi = company[0]
                    val coord2DDouble =
                        OperatorPosture.mapToLonLat(poi.point_x.toDouble(), poi.point_y.toDouble())
                    aroundSearch(poi.name, coord2DDouble.lon, coord2DDouble.lat)
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
                        aroundSearch(
                            endPoiInfo.name,
                            endPoiInfo.point.longitude,
                            endPoiInfo.point.latitude
                        )
                    } else {
                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_AroundSearch,
                                searchPoiList = emptyList()
                            )
                        )
                    }
                } else {
                    Timber.i("无目的地,无法搜索")
                }
            }

            else -> {
                ioScope.launch {
                    val result = mSearchBusiness.keywordSearch(
                        locationName,
                        searchPoiBizType = SearchPoiBizType.AROUND,
                        searchQueryType = SearchQueryType.AROUND
                    )
                    when (result.status) {
                        Status.SUCCESS -> {
                            result.data?.let { data ->
                                val list = SearchDataConvertUtils.blPoiSearchResultToHmiResult(
                                    data,
                                    SearchPoiBizType.NORMAL
                                ).searchInfo.poiResults
                                if (!list.isNullOrEmpty()) {
                                    val poi = list[0]
                                    aroundSearch(poi.name, poi.point.longitude, poi.point.latitude)
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
        }
    }

    override fun nearestSearch(lon: Double, lat: Double) {
        ioScope.launch {
            val result =
                mSearchBusiness.nearestSearch(poi = POIFactory.createPOI(null, GeoPoint(lon, lat)))
            when (result.status) {
                Status.SUCCESS -> {
                    result.data?.let { searchNearestResult ->
                        val poiList = SearchCommonUtils.invertOrderList(searchNearestResult.poi_list)
                        searchNearestResult.poi_list = poiList
                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_NearestSearch,
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
    }

    override fun alongRouteSearch(keyword: String) {
        if (mNaviRepository.isNavigating()) {
            ioScope.launch {
                val result = mSearchBusiness.searchAlongWay(keyword)
                when (result.status) {
                    Status.SUCCESS -> {

                        val alongWayPois = ArrayList<POI>()
                        result.data?.let { data ->
                            SearchDataConvertUtils.convertAlongWayToHmiResult(data)?.searchInfo?.poiResults?.forEach {
                                alongWayPois.add(it)
                            }
                        }

                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_AlongWaySearch,
                                searchPoiList = alongWayPois
                            )
                        )
                    }

                    else -> {
                        Timber.i("nearestSearch Fail $result")
                    }
                }
            }
            _mapCommand.postValue(
                MapCommandBean(
                    mapCommandType = MapCommandType.IOV_VOICE_AlongWaySearch,
                    data = keyword
                )
            )
        }
    }

    override fun requestHomeOrWorkAddress(type: String) {
        when (type) {
            "家" -> {
                val homeList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
                if (!homeList.isNullOrEmpty()) {
                    _mapCommand.postValue(
                        MapCommandBean(
                            mapCommandType = MapCommandType.IOV_VOICE_RequestHomeAddress,
                            poi = ConverUtils.converSimpleFavoriteItemToPoi(homeList[0])
                        )
                    )
                }
            }

            "公司" -> {
                val companyList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
                if (!companyList.isNullOrEmpty()) {
                    _mapCommand.postValue(
                        MapCommandBean(
                            mapCommandType = MapCommandType.IOV_VOICE_RequestCompanyAddress,
                            poi = ConverUtils.converSimpleFavoriteItemToPoi(companyList[0])
                        )
                    )
                }
            }
        }
    }

    override fun whereAmI() {
        ioScope.launch {
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
                    val searchNearestResult = result.data
                    if (!TextUtils.isEmpty(searchNearestResult?.desc)) {
                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_WhereAmI,
                                data = searchNearestResult?.desc
                            )
                        )
                    } else {
                        _mapCommand.postValue(
                            MapCommandBean(
                                mapCommandType = MapCommandType.IOV_VOICE_WhereAmI,
                                data = "未找到当前位置信息"
                            )
                        )
                    }
                }

                else -> {
                    _mapCommand.postValue(
                        MapCommandBean(
                            mapCommandType = MapCommandType.IOV_VOICE_WhereAmI,
                            data = "未找到当前位置信息"
                        )
                    )
                }
            }
        }
    }

}