package com.desaysv.psmap.base.business

import android.app.Application
import android.text.TextUtils
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.layer.model.BizSearchAlongWayPoint
import com.autonavi.gbl.layer.model.BizSearchChildPoint
import com.autonavi.gbl.layer.model.BizSearchParentPoint
import com.autonavi.gbl.layer.model.BizSearchType
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.model.PreviewParam
import com.autonavi.gbl.search.model.KeywordSearchResultV2
import com.autonavi.gbl.search.model.SearchAlongWayResult
import com.autonavi.gbl.search.model.SearchDeepInfoResult
import com.autonavi.gbl.search.model.SearchEnrouteCategoryResult
import com.autonavi.gbl.search.model.SearchEnrouteResult
import com.autonavi.gbl.search.model.SearchEnrouteScene
import com.autonavi.gbl.search.model.SearchKeywordResult
import com.autonavi.gbl.search.model.SearchLineDeepInfoResult
import com.autonavi.gbl.search.model.SearchNearestResult
import com.autonavi.gbl.search.model.SearchSuggestResult
import com.autonavi.gbl.search.model.SuggestionSearchResult
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.layer.RouteEndAreaLayer
import com.autosdk.bussiness.layer.RouteResultLayer
import com.autosdk.bussiness.layer.SearchLayer
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.search.SearchCallback
import com.autosdk.bussiness.search.SearchController
import com.autosdk.bussiness.search.SearchControllerV2
import com.autosdk.bussiness.search.request.SearchAlongWayInfo
import com.autosdk.bussiness.search.request.SearchDeepInfo
import com.autosdk.bussiness.search.request.SearchLineDeepInfo
import com.autosdk.bussiness.search.request.SearchPoiBizType
import com.autosdk.bussiness.search.request.SearchQueryType
import com.autosdk.bussiness.search.request.SearchRequestInfo
import com.autosdk.bussiness.widget.search.util.SearchMapUtil
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.bean.SearchThrowable
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.Result
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * @author 张楠
 * @time 2024/1/16
 * @description 搜索相关业务类
 *
 */
@Singleton
open class SearchBusiness @Inject constructor(
    private val application: Application,
    private val mLayerController: LayerController,
    private val mRouteRequestController: RouteRequestController,
    private val mMapController: MapController,
    private val mNaviController: NaviController,
    private val mSearchController: SearchController,
    private val mSearchControllerV2: SearchControllerV2,
    private val mapDataBusiness: MapDataBusiness,
    private val netWorkManager: NetWorkManager,
    private val iCarInfoProxy: ICarInfoProxy,
    private val gson: Gson
) {
    // 主图层
    private val mMapLayer: MapLayer? by lazy {
        mLayerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //搜索图层
    private val mSearchLayer: SearchLayer? by lazy {
        mLayerController.getSearchLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //路线图层
    private val mRouteResultLayer: RouteResultLayer? by lazy {
        mLayerController.getRouteResultLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //终点图层
    private val mRouteEndAreaLayer: RouteEndAreaLayer? by lazy {
        mLayerController.getRouteEndAreaLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    private val mCustomLayer: CustomLayer by lazy {
        mLayerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    private val timeOut = 8 * 1000L;

    private var parentPoiList: ArrayList<BizSearchParentPoint>? = null

    private val tartgetOrder = listOf(
        application.getString(R.string.sv_custom_poi_category_1),
        application.getString(R.string.sv_custom_poi_category_2),
        application.getString(R.string.sv_custom_poi_category_3),
        application.getString(R.string.sv_custom_poi_category_4),
        application.getString(R.string.sv_custom_poi_category_5),
        application.getString(R.string.sv_custom_poi_category_6),
        application.getString(R.string.sv_custom_poi_category_7),
        application.getString(R.string.sv_custom_poi_category_8)
    )

    /**
     * 关键字搜索
     *
     * @param keyword 关键字
     * @param searchPoiBizType
     * @param curPoi 当前poi点
     * @param range 范围
     * @param size 搜索结果数量
     * @param page 搜索结果页码
     * @param isParallel 是否并行, 默认不并行
     * @return
     */
    suspend fun keywordSearch(
        keyword: String? = null,
        @SearchPoiBizType searchPoiBizType: Int = SearchPoiBizType.NORMAL,
        @SearchQueryType searchQueryType: String = SearchQueryType.NORMAL,
        curPoi: POI? = null,
        range: String? = null,
        filter: String? = null,
        size: Int = 30,
        page: Int = 1,
        isParallel: Boolean = false,
        cityCode: Int? = null
    ): Result<SearchKeywordResult> {
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                if (!isParallel) {
                    abortAll()
                }
                mSearchController.keywordSearch(
                    SearchRequestInfo.Builder()
                        .setKeyword(keyword)
                        .setPoi(curPoi)
                        .setBizType(searchPoiBizType)
                        .setQueryType(searchQueryType)
                        .setSize(size)
                        .setPage(page)
                        .setRange(range)
                        .setOfflineCity(cityCode ?: 0)
                        .setFilter(filter)
                        .build(),
                    isParallel,
                    object : SearchCallback<SearchKeywordResult>() {
                        override fun onSuccess(data: SearchKeywordResult?) {
                            Timber.i("keywordSearch v1 onSuccess $keyword")
                            continuation.resume(Result.success(data))
                        }

                        override fun onFailure(errCode: Int, msg: String?) {
                            Timber.i("onFailure() called with: errCode = $errCode, msg = $msg")
                            continuation.resume(
                                Result.error(
                                    SearchThrowable(
                                        message = "msg = $msg",
                                        errorCode = errCode
                                    )
                                )
                            )
                        }

                        override fun onComplete() {}

                        override fun onError() {
                            Timber.i("keywordSearch v1 onError keyword = $keyword")
                            continuation.resume(Result.error(SearchThrowable(message = "onError")))
                        }
                    })
            }
        } ?: Result.error(SearchThrowable(message = "Time Out!"))
    }

    /**
     * 关键字搜索
     *
     * @param keyword 关键字
     * @param searchPoiBizType
     * @param curPoi 当前poi点
     * @param range 范围
     * @param size 搜索结果数量
     * @param page 搜索结果页码
     * @param isParallel 是否并行, 默认不并行
     * @return
     */
    suspend fun keywordSearchV2(
        keyword: String? = null,
        @SearchPoiBizType searchPoiBizType: Int = SearchPoiBizType.NORMAL,
        @SearchQueryType searchQueryType: String = SearchQueryType.NORMAL,
        curPoi: POI? = null,
        range: String? = null,
        size: Int = 30,
        page: Int = 1,
        isParallel: Boolean = false,
        cityCode: Int? = null,
        classify: String? = null,
        retainState: String? = null,
        checkedLevel: String = "1",
    ): Result<KeywordSearchResultV2> {
        Timber.i("keywordSearchV2() called with: keyword = $keyword")
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                if (!isParallel) {
                    abortAllV2()
                }
                val callback = object : SearchCallback<KeywordSearchResultV2>() {
                    override fun onSuccess(data: KeywordSearchResultV2?) {
                        Timber.i("keywordSearch v2 onSuccess $keyword")
                        Timber.i("keywordSearch v2 _keywordSearchResultV2 .postValue($data)")
                        continuation.resume(Result.success(data))
                    }

                    override fun onFailure(errCode: Int, msg: String?) {
                        Timber.i("onFailure() called with: errCode = $errCode, msg = $msg")
                        continuation.resume(
                            Result.error(
                                SearchThrowable(
                                    message = "msg = $msg",
                                    errorCode = errCode
                                )
                            )
                        )
                    }

                    override fun onComplete() {}

                    override fun onError() {
                        Timber.i("keywordSearch onError keyword = $keyword")
                        continuation.resume(Result.error(SearchThrowable(message = "onError")))
                    }
                }
                when (searchQueryType) {
                    SearchQueryType.NORMAL -> {
                        mSearchControllerV2.keywordSearch(
                            SearchRequestInfo.Builder()
                                .setKeyword(keyword)
                                .setPoi(curPoi)
                                .setBizType(searchPoiBizType)
                                .setQueryType(searchQueryType)
                                .setSize(size)
                                .setPage(page)
                                .setRange(range)
                                .setOfflineCity(cityCode ?: 0)
                                .build().apply {
                                    setClassify(classify)
                                    setRetainState(retainState)
                                    setCheckedLevel(checkedLevel)
                                },
                            isParallel,
                            callback
                        )
                    }

                    SearchQueryType.AROUND -> {
                        mSearchControllerV2.nearbySearch(
                            SearchRequestInfo.Builder()
                                .setKeyword(keyword)
                                .setPoi(curPoi)
                                .setBizType(searchPoiBizType)
                                .setQueryType(searchQueryType)
                                .setSize(size)
                                .setPage(page)
                                .setRange(range)
                                .setOfflineCity(cityCode ?: 0)
                                .build().apply {
                                    setClassify(classify)
                                    setRetainState(retainState)
                                    setCheckedLevel(checkedLevel)
                                },
                            isParallel,
                            callback
                        )
                    }

                    SearchQueryType.ID -> {
                        mSearchControllerV2.poiIdSearch(
                            SearchRequestInfo.Builder()
                                .setKeyword(keyword)
                                .setPoi(curPoi)
                                .setBizType(searchPoiBizType)
                                .setQueryType(searchQueryType)
                                .setSize(
                                    size
                                ).setPage(page)
                                .setRange(range)
                                .setOfflineCity(cityCode ?: 0)
                                .build().apply {
                                    setClassify(classify)
                                    setRetainState(retainState)
                                    setCheckedLevel(checkedLevel)
                                },
                            true,
                            callback
                        )
                    }
                }
            }
        } ?: Result.error(SearchThrowable(message = "Time Out!"))
    }

    /**
     * 预搜索
     *
     * @param keyword 关键字
     * @param searchPoiBizType
     * @param curPoi
     * @param isParallel  是否并行, 默认不并行
     * @return
     */
    suspend fun suggestionSearch(
        keyword: String,
        @SearchPoiBizType searchPoiBizType: Int = SearchPoiBizType.NORMAL,
        curPoi: POI? = null,
        isParallel: Boolean = false,
        size: Int = 30,
        cityCode: Int? = null
    ): Result<SearchSuggestResult> {
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                if (!isParallel) {
                    abortAll()
                }
                mSearchController.suggestionSearch(SearchRequestInfo.Builder().setKeyword(keyword)
                    .setPoi(curPoi).setBizType(searchPoiBizType)
                    .setSize(size).setOfflineCity(cityCode ?: 0).build(),
                    object : SearchCallback<SearchSuggestResult>() {
                        override fun onSuccess(data: SearchSuggestResult?) {
                            continuation.resume(Result.success(data))
                        }

                        override fun onFailure(errCode: Int, msg: String?) {
                            val throwable =
                                SearchThrowable(message = "msg = $msg", errorCode = errCode)
                            continuation.resume(Result.error(throwable))
                            Timber.i("有执行")

                        }

                        override fun onComplete() {}

                        override fun onError() {
                            continuation.resume(Result.error(SearchThrowable(message = "onError")))
                        }
                    })
            }
        } ?: Result.error(SearchThrowable(message = "Time Out!"))
    }

    /**
     * 发起逆地理搜索 根据经纬度查询POI
     *
     * @param poi 需要查询的经纬度
     * @param isParallel  是否并行, 默认不并行
     * @return
     */
    suspend fun nearestSearch(poi: POI, isParallel: Boolean = false): Result<SearchNearestResult> {
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                if (!isParallel) {
                    abortAll()
                }
                mSearchController.nearestSearch(
                    poi.point,
                    object : SearchCallback<SearchNearestResult>() {
                        override fun onSuccess(data: SearchNearestResult?) {
                            continuation.resume(Result.success(data))
                        }

                        override fun onFailure(errCode: Int, msg: String?) {
                            continuation.resume(
                                Result.error(
                                    SearchThrowable(
                                        message = "msg = $msg",
                                        errorCode = errCode
                                    )
                                )
                            )
                        }

                        override fun onComplete() {}

                        override fun onError() {
                            continuation.resume(Result.error(SearchThrowable(message = "onError")))
                        }
                    })

            }
        } ?: Result.error(SearchThrowable(message = "Time Out!"))
    }

    /**
     * 深度搜索
     *
     * @param poi
     * @param isParallel  是否并行, 默认不并行
     * @return
     */
    suspend fun deepInfoSearch(
        poi: POI,
        isParallel: Boolean = false
    ): Result<SearchDeepInfoResult> {
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                if (!isParallel) {
                    abortAll()
                }
                mSearchController.deepInfoSearch(SearchDeepInfo().apply {
                    poiid = poi.id
                    geoPoint = poi.point
                }, object : SearchCallback<SearchDeepInfoResult>() {
                    override fun onSuccess(data: SearchDeepInfoResult?) {
                        continuation.resume(Result.success(data))
                    }

                    override fun onFailure(errCode: Int, msg: String?) {
                        continuation.resume(
                            Result.error(
                                SearchThrowable(
                                    message = "msg = $msg",
                                    errorCode = errCode
                                )
                            )
                        )
                    }

                    override fun onComplete() {}

                    override fun onError() {
                        continuation.resume(Result.error(SearchThrowable(message = "onError")))
                    }
                })

            }
        } ?: Result.error(SearchThrowable(message = "Time Out!"))
    }

    /**
     * 沿途搜索
     *
     * @param searchCategory 搜索分类
     * @param isParallel  是否并行, 默认不并行
     * @return
     */
    suspend fun searchAlongWay(
        searchCategory: String,
        isParallel: Boolean = false
    ): Result<SearchAlongWayResult> {
        val routeCarResultData = mRouteRequestController.carRouteResult
        val pathResult = routeCarResultData?.pathResult
        pathResult?.let {
            if (it.isNotEmpty()) {
                val pathInfo = routeCarResultData.pathResult[routeCarResultData.focusIndex]
                pathInfo?.let {
                    return withTimeoutOrNull(timeOut) {
                        suspendCancellableCoroutine { continuation ->
                            if (!isParallel) {
                                abortAll()
                            }
                            mSearchController.alongWaySearch(
                                SearchAlongWayInfo.Builder()
                                    .setKeyword(SearchCommonUtils.getAlongName(searchCategory))
                                    .setGeolinePointList(
                                        SearchCommonUtils.getAlongwaySearchGeoline(
                                            pathResult[routeCarResultData.focusIndex],
                                            routeCarResultData.fromPOI
                                        )
                                    ).setRoutePointsByPoi(
                                        routeCarResultData.fromPOI,
                                        routeCarResultData.toPOI,
                                        routeCarResultData.midPois
                                    )
                                    .setGuideRoadsIdList(
                                        SearchCommonUtils.getAlongwaySearchGuideRoads(
                                            pathInfo
                                        )
                                    ).setNaving(mNaviController.isNaving)
                                    .setIsNeedGasprice(true).build(),
                                object : SearchCallback<SearchAlongWayResult>() {
                                    override fun onSuccess(data: SearchAlongWayResult?) {
                                        continuation.resume(Result.success(data))
                                    }

                                    override fun onFailure(errCode: Int, msg: String?) {
                                        continuation.resume(
                                            Result.error(
                                                SearchThrowable(
                                                    message = "msg = $msg",
                                                    errorCode = errCode
                                                )
                                            )
                                        )
                                    }

                                    override fun onComplete() {}

                                    override fun onError() {
                                        continuation.resume(Result.error(SearchThrowable(message = "onError")))
                                    }
                                },
                                netWorkManager.isNetworkConnected()
                            )
                        }
                    } ?: Result.error(SearchThrowable(message = "Time Out!"))
                }
                return Result.error("RouteCarResultData.pathInfo is Empty!")
            }
        }
        return Result.error("RouteCarResultData.PathResult is Empty!")
    }


    // SearchServiceV2接口


    /**
     * 预搜索
     *
     * @param keyword 关键字
     * @param searchPoiBizType
     * @param curPoi
     * @param isParallel  是否并行, 默认不并行
     * @return
     */
    suspend fun suggestionSearchV2(
        keyword: String,
        @SearchPoiBizType searchPoiBizType: Int = SearchPoiBizType.NORMAL,
        curPoi: POI? = null,
        isParallel: Boolean = false,
        size: Int = 30,
        cityCode: Int? = null
    ): Result<SuggestionSearchResult> {
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                if (!isParallel) {
                    abortAll()
                }
                mSearchControllerV2.suggestionSearch(SearchRequestInfo.Builder().setKeyword(keyword)
                    .setPoi(curPoi).setBizType(searchPoiBizType)
                    .setSize(size).setOfflineCity(cityCode ?: 0).build(),
                    object : SearchCallback<SuggestionSearchResult>() {
                        override fun onSuccess(data: SuggestionSearchResult?) {
                            continuation.resume(Result.success(data))
                        }

                        override fun onFailure(errCode: Int, msg: String?) {
                            val throwable =
                                SearchThrowable(message = "msg = $msg", errorCode = errCode)
                            continuation.resume(Result.error(throwable))
                            Timber.i("有执行")

                        }

                        override fun onComplete() {}

                        override fun onError() {
                            continuation.resume(Result.error(SearchThrowable(message = "onError")))
                        }
                    })
            }
        } ?: Result.error(SearchThrowable(message = "Time Out!"))
    }

    /**
     * 路线规划界面 查找终点区域
     */
    suspend fun poiIdSearchV2(
        curPoi: POI, isParallel: Boolean = true, size: Int = 30
    ): Result<KeywordSearchResultV2> {
        Timber.i("poiIdSearchV2() called with: curPoi = $curPoi, isParallel = $isParallel, size = $size")
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                if (!isParallel) {
                    abortAllV2()
                }
                val resumed = AtomicBoolean(false)
                mSearchControllerV2.poiIdSearch(SearchRequestInfo.Builder().setPoi(curPoi)
                    .setQueryType(SearchQueryType.ID).setSize(size).build(),
                    isParallel,
                    object : SearchCallback<KeywordSearchResultV2>() {
                        override fun onSuccess(data: KeywordSearchResultV2?) {
                            if (resumed.compareAndSet(false, true)) {
                                continuation.resume(Result.success(data))
                            }
                        }

                        override fun onFailure(errCode: Int, msg: String?) {
                            if (resumed.compareAndSet(false, true)) {
                                continuation.resume(Result.error("errCode = $errCode ,msg = $msg"))
                            }
                        }

                        override fun onComplete() {}

                        override fun onError() {
                            if (resumed.compareAndSet(false, true)) {
                                continuation.resume(Result.error(SearchThrowable(message = "onError")))
                            }
                        }
                    })
            }
        } ?: Result.error(SearchThrowable(message = "Time Out!"))
    }


    /**
     * 沿途搜的品类列表搜索
     *
     * @param endCityAdcode 终点城市AdCode
     * @param isParallel  是否并行, 默认不并行
     * @return
     */
    suspend fun searchAlongWayCategoryList(
        isParallel: Boolean = false
    ): Result<SearchEnrouteCategoryResult> {
        Timber.d("enrouteCategoryListSearch() called with: isParallel = $isParallel")
        val mToPoi = mRouteRequestController.carRouteResult.toPOI
        mToPoi?.let {
            Timber.d("enrouteCategoryListSearch() called with: mToPoi.point.longitude = ${mToPoi.point.longitude}")
            Timber.d("enrouteCategoryListSearch() called with: mToPoi.point.latitude = ${mToPoi.point.latitude}")
            val endCityAdcode =
                mapDataBusiness.getAdCodeByLonLat(mToPoi.point.longitude, mToPoi.point.latitude)
            Timber.d("enrouteCategoryListSearch() called with: endCityAdcode = ${endCityAdcode}")
            return withTimeoutOrNull(timeOut) {
                suspendCancellableCoroutine { continuation ->
                    if (!isParallel) {
                        abortAllV2()
                    }
                    if (endCityAdcode == 0) {
                        continuation.resume(Result.error(SearchThrowable(message = "endCityAdcode = 0")))
                        return@suspendCancellableCoroutine
                    }
                    mSearchControllerV2.searchEnrouteCategoryList(
                        endCityAdcode.toString(),
                        object : SearchCallback<SearchEnrouteCategoryResult>() {
                            override fun onSuccess(data: SearchEnrouteCategoryResult?) {
                                continuation.resume(Result.success(data))
                            }

                            override fun onFailure(errCode: Int, msg: String?) {
                                continuation.resume(
                                    Result.error(
                                        SearchThrowable(
                                            message = "msg = $msg",
                                            errorCode = errCode
                                        )
                                    )
                                )
                            }

                            override fun onComplete() {}

                            override fun onError() {
                                continuation.resume(Result.error(SearchThrowable(message = "onError")))
                            }
                        })

                }
            } ?: Result.error(SearchThrowable(message = "Time Out!"))
        }
        return Result.error("RouteCarResultData.PathResult is Empty!")

    }

    /**
     * 顺路关键字搜索
     *
     * @param keyword 关键字
     * @param naviScene 是否导航中
     * @return
     */
    suspend fun searchAlongWayKeyword(
        keyword: String,
        naviScene: Int = SearchEnrouteScene.BeforeNavi
    ): Result<SearchEnrouteResult> {
        if (TextUtils.isEmpty(keyword)) {
            return Result.error("keywordis Empty!")
        }
        val routeCarResultData = mRouteRequestController.carRouteResult
        val pathResult = routeCarResultData?.pathResult
        pathResult?.let {
            if (it.isNotEmpty()) {
                val pathInfo = routeCarResultData.pathResult[routeCarResultData.focusIndex]
                pathInfo?.let {
                    return withTimeoutOrNull(timeOut) {
                        suspendCancellableCoroutine { continuation ->
                            abortAllV2()
                            mSearchControllerV2.searchEnrouteKeyword(
                                pathInfo, keyword, object : SearchCallback<SearchEnrouteResult>() {
                                    override fun onSuccess(data: SearchEnrouteResult?) {
                                        continuation.resume(Result.success(data))
                                    }

                                    override fun onFailure(errCode: Int, msg: String?) {
                                        continuation.resume(
                                            Result.error(
                                                SearchThrowable(
                                                    message = "msg = $msg",
                                                    errorCode = errCode
                                                )
                                            )
                                        )
                                    }

                                    override fun onComplete() {}

                                    override fun onError() {
                                        continuation.resume(Result.error(SearchThrowable(message = "onError")))
                                    }
                                }, naviScene
                            )
                        }
                    } ?: Result.error(SearchThrowable(message = "Time Out!"))
                }
                return Result.error("RouteCarResultData.pathInfo is Empty!")
            }
        }
        return Result.error("RouteCarResultData.PathResult is Empty!")

    }

    /**
     * 顺路IQDQ搜索
     *
     * @param poiId
     * @param naviScene 是否导航中
     * @return
     */
    suspend fun searchAlongWayIdq(
        poiId: String,
        naviScene: Int = SearchEnrouteScene.BeforeNavi
    ): Result<SearchEnrouteResult> {
        Timber.i("searchAlongWayIdq() called with: poiId = $poiId, naviScene = $naviScene")
        if (TextUtils.isEmpty(poiId)) {
            return Result.error("poiId Empty!")
        }
        val routeCarResultData = mRouteRequestController.carRouteResult
        val pathResult = routeCarResultData?.pathResult
        pathResult?.let {
            if (it.isNotEmpty()) {
                val pathInfo = routeCarResultData.pathResult[routeCarResultData.focusIndex]
                pathInfo?.let {
                    return withTimeoutOrNull(timeOut) {
                        suspendCancellableCoroutine { continuation ->
                            mSearchControllerV2.searchEnrouteIdq(
                                pathInfo, poiId, object : SearchCallback<SearchEnrouteResult>() {
                                    override fun onSuccess(data: SearchEnrouteResult?) {
                                        continuation.resume(Result.success(data))
                                    }

                                    override fun onFailure(errCode: Int, msg: String?) {
                                        continuation.resume(
                                            Result.error(
                                                SearchThrowable(
                                                    message = "msg = $msg",
                                                    errorCode = errCode
                                                )
                                            )
                                        )
                                    }

                                    override fun onComplete() {

                                    }

                                    override fun onError() {
                                        continuation.resume(Result.error(SearchThrowable(message = "onError")))
                                    }
                                }, naviScene
                            )
                        }
                    } ?: Result.error(SearchThrowable(message = "Time Out!"))
                }
                return Result.error("RouteCarResultData.pathInfo is Empty!")
            }
        }
        return Result.error("RouteCarResultData.PathResult is Empty!")

    }

    /**
     * 沿途批量获取poi点的深度信息
     *
     * @param poi
     * @param isParallel  是否并行, 默认不并行
     * @return
     */
    suspend fun lineDeepInfoSearch(
        searchLineDeepInfo: SearchLineDeepInfo,
        isParallel: Boolean = false
    ): Result<SearchLineDeepInfoResult> {
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                if (!isParallel) {
                    abortAll()
                }
                mSearchController.lineDeepInfoSearch(
                    searchLineDeepInfo,
                    object : SearchCallback<SearchLineDeepInfoResult>() {
                        override fun onSuccess(data: SearchLineDeepInfoResult?) {
                            continuation.resume(Result.success(data))
                        }

                        override fun onFailure(errCode: Int, msg: String?) {
                            continuation.resume(
                                Result.error(
                                    SearchThrowable(
                                        message = "msg = $msg",
                                        errorCode = errCode
                                    )
                                )
                            )
                        }

                        override fun onComplete() {}

                        override fun onError() {
                            continuation.resume(Result.error(SearchThrowable(message = "onError")))
                        }
                    })

            }
        } ?: Result.error(SearchThrowable(message = "Time Out!"))
    }

    // 搜索图层操作

    fun setSearchLayerHidden(hidden: Boolean) {
        Timber.i("setSearchLayerHidden hidden = $hidden")
        if (hidden) {
            //页面隐藏
            mSearchLayer?.run {
                setVisible(BizSearchType.BizSearchTypePoiChildPoint.toLong(), false)
                setVisible(BizSearchType.BizSearchTypePoiParentPoint.toLong(), false)
                setVisible(BizSearchType.BizSearchTypePoiLabel.toLong(), false)
                clearAllItems()
            }
        } else {
            mSearchLayer?.run {
                setVisible(BizSearchType.BizSearchTypePoiChildPoint.toLong(), true)
                setVisible(BizSearchType.BizSearchTypePoiParentPoint.toLong(), true)
                setVisible(BizSearchType.BizSearchTypePoiLabel.toLong(), true)
            }
        }
    }

    /**
     * 页面销毁后，清除图层中显示的POI
     */
    fun clearAllSearchItems() {
        Timber.i("clearAllSearchItems")
        mSearchLayer?.clearAllItems()
    }

    /**
     * 清除搜索图层中指定Type的元素
     */
    fun clearSearchItems(bizType: Int) {
        Timber.i("clearSearchItems")
        mSearchLayer?.clearAllItems(bizType)
    }

    /**
     * 搜索结果扎点（父POI）
     */
    fun updateSearchParentPoi(list: ArrayList<BizSearchParentPoint>): Boolean? {
        Timber.i("updateSearchParentPoi list.size = ${list.size}")
        //保存当前显示的poi点，计算全览画面需要用到
        parentPoiList = list
        return mSearchLayer?.updateSearchParentPoi(parentPoiList)
    }

    /**
     * 搜索结果扎点（子POI）
     */
    fun updateSearchChildPoi(pointList: ArrayList<BizSearchChildPoint?>?) =
        mSearchLayer?.updateSearchChildPoi(pointList)

    /**
     * 沿途搜索POI点信息
     */
    fun updateSearchAlongRoutePoi(list: ArrayList<BizSearchAlongWayPoint>): Boolean? {
        Timber.i("updateSearchAlongRoutePoi list.size = ${list.size}")
        return mSearchLayer?.updateSearchAlongRoutePoi(list)
    }

    /**
     * 清除绘制的poi点的区域
     */
    fun clearEndPointArea() {
        mSearchLayer?.clearAllItems(BizSearchType.BizSearchTypePoiEndAreaPolygon)
        mSearchLayer?.clearAllItems(BizSearchType.BizSearchTypePoiEndAreaPolyline)
        mRouteEndAreaLayer?.clearAllRouteEndAreaLayer()
    }

    /**
     * 显示全览
     *
     */
    fun showPreview(bAnimation: Boolean = true) {
        Timber.i("showPreview")
        SearchMapUtil.getSearchAlongBound(parentPoiList)?.let { mapRect ->
            val previewParam = PreviewParam().apply {
                leftOfMap = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1100 else R.dimen.sv_dimen_0
                )
                topOfMap = 0
                screenLeft = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1240 else R.dimen.sv_dimen_780
                )
                screenTop = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_200)
                screenRight = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_120)
                screenBottom = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_100)
                bUseRect = true
                mapBound = mapRect
            }
            mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                ?.showPreview(previewParam, bAnimation, 500, -1)
        }
    }

    /**
     * 退出全览
     *
     */
    fun exitPreview() {
        mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)?.exitPreview(true)
    }

    /**
     * 添加/移除指定的图层点击观测者
     *
     * @param observer   图层点击observer,若为null,则不作操作
     * @param removeOnly true-仅移除指定的observer, false-添加指定的observer
     */

    fun setSearchLayerClickObserver(observer: ILayerClickObserver, removeOnly: Boolean) =
        SearchMapUtil.setSearchLayerClickObserver(observer, removeOnly)

    /**
     * 切换主图中心点
     */
    fun updateMapCenter(poi: POI?) {
        poi?.let {
            SearchMapUtil.updateMapCenter(poi)
        }
    }

    /**
     * 设置某个图层的焦点态
     */
    fun setFocus(@BizSearchType.BizSearchType1 type: Int, id: String?, isFocus: Boolean) =
        SearchMapUtil.setFocus(type, id, isFocus)

    fun updateSearchChildPoiEnterExitPoi(tSelectPoi: POI?) =
        SearchMapUtil.updateSearchChildPoiEnterExitPoi(tSelectPoi)

    fun showCustomTypePoint1(coord3DDouble: Coord3DDouble) {
        mCustomLayer.showCustomTypePoint1(coord3DDouble)
    }

    fun hideCustomTypePoint1() {
        mCustomLayer.hideCustomTypePoint1()
    }

    /**
     * 取消请求
     */
    fun abortAll() {
        mSearchController.abortAll()
    }

    /**
     * 取消请求
     */
    fun abortAllV2() {
        mSearchControllerV2.abortAll()
    }
}
