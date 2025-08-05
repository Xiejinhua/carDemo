package com.autosdk.bussiness.search;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.OperatorPosture;
import com.autonavi.gbl.search.SearchServiceV2;
import com.autonavi.gbl.search.model.KeywordSearchIdqParam;
import com.autonavi.gbl.search.model.KeywordSearchResultV2;
import com.autonavi.gbl.search.model.KeywordSearchRqbxyParam;
import com.autonavi.gbl.search.model.KeywordSearchSpqParam;
import com.autonavi.gbl.search.model.KeywordSearchTQueryParam;
import com.autonavi.gbl.search.model.PoiCmallDetailSearchResult;
import com.autonavi.gbl.search.model.PoiDetailSearchResult;
import com.autonavi.gbl.search.model.PoiShopListSearchResult;
import com.autonavi.gbl.search.model.SceneSearchParam;
import com.autonavi.gbl.search.model.SceneSearchResult;
import com.autonavi.gbl.search.model.SearchDataType;
import com.autonavi.gbl.search.model.SearchEnrouteCategoryListParam;
import com.autonavi.gbl.search.model.SearchEnrouteCategoryResult;
import com.autonavi.gbl.search.model.SearchEnrouteIdqParam;
import com.autonavi.gbl.search.model.SearchEnrouteKeywordParam;
import com.autonavi.gbl.search.model.SearchEnrouteResult;
import com.autonavi.gbl.search.model.SearchKeywordParamV2;
import com.autonavi.gbl.search.model.SearchMode;
import com.autonavi.gbl.search.model.SearchPageParam;
import com.autonavi.gbl.search.model.SearchPoiCmallDetailParam;
import com.autonavi.gbl.search.model.SearchPoiDetailParam;
import com.autonavi.gbl.search.model.SearchPoiShopListParam;
import com.autonavi.gbl.search.model.SearchResult;
import com.autonavi.gbl.search.model.SearchRetainParam;
import com.autonavi.gbl.search.model.SearchSuggestionParam;
import com.autonavi.gbl.search.model.SuggestionSearchResult;
import com.autonavi.gbl.search.observer.IKeyWordSearchObserverV2;
import com.autonavi.gbl.search.observer.ISearchEnrouteCategoryObserver;
import com.autonavi.gbl.search.observer.ISearchEnrouteObserver;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autonavi.gbl.util.model.TaskResult;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.MapController;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.search.request.SearchLocInfo;
import com.autosdk.bussiness.search.request.SearchPoiBizType;
import com.autosdk.bussiness.search.request.SearchQueryType;
import com.autosdk.bussiness.search.request.SearchRequestInfo;
import com.autosdk.bussiness.search.utils.NumberUtil;
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils;
import com.autosdk.common.utils.SdkNetworkUtil;

import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

/**
 * 搜索模块M层
 * 注意:  a. 回调V层callback方法时,要切换到主线程
 * -      b. 各方法内部请使用 {@link SearchCallbackWrapper}, 内部自动进行了主线程切换,避免 V 层子线程更新崩溃
 * <p>
 * 通过 {@link #getInstance()} 来获取并自动进行 searchService 的创建和初始化
 * 通过 {@link #keywordSearch(SearchRequestInfo, SearchCallback)}  来发起在线优先的 关键字搜索/经纬度周边搜索 请求
 * <p>
 * 注意：搜索直接按BL原对象返回，因BL原实体类未做序列化，暂由客户端自行cover
 * 具体参考business模块下StringUtils.toJson/parseJson 方法
 * 如遇对象中需求参数使用不直观或隐藏太深，或未满足，请列设计优化项于语雀
 *
 * @author AutoSDK
 */
public class SearchControllerV2 {
    private static final String TAG = "SearchControllerV2";
    private SearchServiceV2 mSearchService;
    private final int PAGE_SIZE = 10;
    private AtomicInteger taskId = new AtomicInteger(0);;

    private SearchControllerV2() {

    }

    private static class SearchControllerHolder {
        private static SearchControllerV2 instance = new SearchControllerV2();
    }

    public static SearchControllerV2 getInstance() {
        return SearchControllerHolder.instance;
    }

    /**
     * 初始化搜索服务
     */
    public void initService() {
        if (mSearchService == null) {
            mSearchService = (SearchServiceV2) ServiceMgr.getServiceMgrInstance()
                    .getBLService(SingleServiceID.SearchV2SingleServiceID);
            mSearchService.init();
            Timber.d("init searchService");
        }
    }


    public void uninit() {
        // TODO: 2020/8/4   释放资源,销毁service
        if (mSearchService != null) {
            abortAll();
            mSearchService = null;
        }
    }

    /**
     * 取消所有搜索请求
     */
    public int abortAll() {
        return mSearchService.abortAll();
    }

    // TODO: 2020/8/17 返回值注解取消, 考虑改成 boolean

    /**
     * 取消单个搜索请求
     */
    public int abort(int taskId) {
        if (mSearchService == null) {
            return Service.ErrorCodeFailed;
        }
        return mSearchService.abort(taskId);
    }

    /**
     * 获取默认搜索Size
     *
     * @return
     */
    public int getPageSize() {
        return PAGE_SIZE;
    }

    /**
     * 关键字搜索
     * <pre>
     *  searchController.keywordSearchInner(new SearchRequestInfo.Builder()
     *      .setKeyword(keyword) // 必传
     *      .setPoi(curPoi) // 可空
     *      .setQueryType(SearchQueryType.NORMAL) // 搜索类型,默认为关键字搜索
     *      .setBizType(SearchPoiBizType.NORMAL) // 搜索点表示的业务类型, 请求结束后原样返回给调用方
     *      .build(),
     *    new SearchCallback<HmiSearchResult>())
     * </pre>
     *
     * @param callback   搜索观察者
     * @param isParallel 是否并行, 默认不并行
     * @return taskId
     */
    public int keywordSearch(@NonNull final SearchRequestInfo keywordInfo, boolean isParallel, SearchCallback<KeywordSearchResultV2> callback) {
        Timber.i("keywordSearch() called with: keywordInfo = [" + keywordInfo + "], isParallel = [" + isParallel + "], callback = [" + callback + "]");
        KeywordSearchTQueryParam tQueryParam = (KeywordSearchTQueryParam) getSearchKeywordParams(keywordInfo, true);
        tQueryParam.pageParam.pageNum = keywordInfo.getPage();
        tQueryParam.offlineParam.resultMaxCount = 20;
        if (!TextUtils.isEmpty(keywordInfo.getFilter())) {
            tQueryParam.customParam.autoAttrFilter = keywordInfo.getFilter();
        }
        if (!TextUtils.isEmpty(keywordInfo.getCategory())) {
            tQueryParam.category = keywordInfo.getCategory();
        }
        IKeyWordSearchObserverV2 observer;
        final SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper = new SearchCallbackWrapper<>(callback);
        if (isParallel) {
            observer = new ParallelSearchKeyWordCallBack(callbackWrapper);
        } else {
            igSearchKeyWordObserver.setSearchCallbackWrapper(callbackWrapper);
            observer = igSearchKeyWordObserver;
        }
        igSearchKeyWordObserver.setSearchCallbackWrapper(callbackWrapper);
        int id = taskId.addAndGet(1);
        mSearchService.keyWordSearchTQuery(tQueryParam, observer, SdkNetworkUtil.getInstance().isNetworkConnected()? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, id);
        return id;
    }

    /**
     * PoiId搜索
     *
     * @param keywordInfo
     * @param callback
     * @return
     */
    public int poiIdSearch(@NonNull final SearchRequestInfo keywordInfo, boolean isParallel, SearchCallback<KeywordSearchResultV2> callback) {
        KeywordSearchIdqParam idqParam = (KeywordSearchIdqParam) getSearchKeywordParams(keywordInfo);
        //搜索参考点经纬度，必传参数
        POI poi = keywordInfo.getPoi();
        //PoiId: PoiId搜索参数，poiid搜索时为必传
        idqParam.id = poi == null ? null : poi.getId();

        IKeyWordSearchObserverV2 observer;
        final SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper = new SearchCallbackWrapper<>(callback);
        if (isParallel) {
            observer = new ParallelSearchKeyWordCallBack(callbackWrapper);
        } else {
            igSearchKeyWordObserver.setSearchCallbackWrapper(callbackWrapper);
            observer = igSearchKeyWordObserver;
        }
        igSearchKeyWordObserver.setSearchCallbackWrapper(callbackWrapper);
        int id = taskId.addAndGet(1);
        mSearchService.keyWordSearchIdq(idqParam, observer, SdkNetworkUtil.getInstance().isNetworkConnected()? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, id);
        return id;
    }

    /**
     * 周边搜
     *
     * @param keywordInfo
     * @param callback
     * @return
     */
    public int nearbySearch(@NonNull final SearchRequestInfo keywordInfo, boolean isParallel, SearchCallback<KeywordSearchResultV2> callback) {
        Timber.i("nearbySearch() called with: keywordInfo = [" + keywordInfo + "], isParallel = [" + isParallel + "], callback = [" + callback + "]");
        KeywordSearchRqbxyParam rqbxyParam = (KeywordSearchRqbxyParam) getSearchKeywordParams(keywordInfo, true);
        if (keywordInfo.isEndPoint()) {
            rqbxyParam.pageParam.pageNum = keywordInfo.getEndpointPage();
        } else {
            rqbxyParam.pageParam.pageNum = keywordInfo.getPage();
        }
        IKeyWordSearchObserverV2 observer;
        final SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper = new SearchCallbackWrapper<>(callback);
        if (isParallel) {
            observer = new ParallelSearchKeyWordCallBack(callbackWrapper);
        } else {
            igSearchKeyWordObserver.setSearchCallbackWrapper(callbackWrapper);
            observer = igSearchKeyWordObserver;
        }
        igSearchKeyWordObserver.setSearchCallbackWrapper(callbackWrapper);
        int id = taskId.addAndGet(1);
        mSearchService.keyWordSearchRqbxy(rqbxyParam, observer, SdkNetworkUtil.getInstance().isNetworkConnected()? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, id);
        return id;
    }

    /**
     * 框选搜索
     *
     * @param keywordInfo
     * @param callback
     * @return
     */
    public int polygonAreaSearch(@NonNull final SearchRequestInfo keywordInfo, SearchCallback<KeywordSearchResultV2> callback) {
        KeywordSearchSpqParam spqParam = (KeywordSearchSpqParam) getSearchKeywordParams(keywordInfo);
        final SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper = new SearchCallbackWrapper<>(callback);
        igSearchKeyWordObserver.setSearchCallbackWrapper(callbackWrapper);
        int id = taskId.addAndGet(1);
        mSearchService.keyWordSearchSpq(spqParam, igSearchKeyWordObserver, SdkNetworkUtil.getInstance().isNetworkConnected()? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, id);
        return id;
    }

    /**
     * 品类列表搜索
     *
     * @param endCityAdcode
     * @param callback
     * @return
     */
    public TaskResult searchEnrouteCategoryList(@NonNull final String endCityAdcode, SearchCallback<SearchEnrouteCategoryResult> callback) {
        SearchEnrouteCategoryListParam searchEnrouteCategoryListParam = new SearchEnrouteCategoryListParam();
        searchEnrouteCategoryListParam.endCityAdcode = endCityAdcode;

        final SearchCallbackWrapper<SearchEnrouteCategoryResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        ISearchEnrouteCategoryObserver observer = new SearchEnrouteCategoryCallBack(callbackWrapper);
        return mSearchService.search(searchEnrouteCategoryListParam, observer);
    }

    //顺路搜

    /**
     * 顺路—关键词搜索
     *
     * @param pathInfo
     * @param callback
     * @return
     */
    public TaskResult searchEnrouteKeyword(@NonNull final PathInfo pathInfo, String keyword, SearchCallback<SearchEnrouteResult> callback, int naviScene) {
        Log.i(TAG,"searchEnrouteKeyword() called with: pathInfo = [" + pathInfo + "], keyword = [" + keyword + "], callback = [" + callback + "]");
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        SearchEnrouteKeywordParam searchParam = new SearchEnrouteKeywordParam();
        searchParam.keyword = keyword;
        searchParam.userLoc.lon = locationToSearchLocInfo.lon;
        searchParam.userLoc.lat = locationToSearchLocInfo.lat;
        searchParam.naviScene = naviScene;
        final SearchCallbackWrapper<SearchEnrouteResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        ISearchEnrouteObserver observer = new SearchEnrouteCallBack(callbackWrapper);
        return mSearchService.search(pathInfo,searchParam, observer);
    }

    /**
     * Idq搜索
     *
     * @param pathInfo
     * @param callback
     * @return
     */
    public TaskResult searchEnrouteIdq(@NonNull final PathInfo pathInfo, String poiId, SearchCallback<SearchEnrouteResult> callback, int naviScene) {
        Log.i(TAG,"searchEnrouteKeyword() called with: pathInfo = [" + pathInfo + "], poiId = [" + poiId + "], callback = [" + callback + "]");
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        SearchEnrouteIdqParam searchParam = new SearchEnrouteIdqParam();
        searchParam.poiId = poiId;
        searchParam.userLoc.lon = locationToSearchLocInfo.lon;
        searchParam.userLoc.lat = locationToSearchLocInfo.lat;
        searchParam.naviScene = naviScene;
        final SearchCallbackWrapper<SearchEnrouteResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        ISearchEnrouteObserver observer = new SearchEnrouteCallBack(callbackWrapper);
        return mSearchService.search(pathInfo,searchParam, observer);
    }



    private SearchKeywordParamV2 getSearchKeywordParams(SearchRequestInfo keywordInfo) {
        return getSearchKeywordParams(keywordInfo, false);
    }

    private SearchKeywordParamV2 getSearchKeywordParams(SearchRequestInfo keywordInfo, boolean isNeedBuildClassifyData) {
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        String queryType = keywordInfo.getQueryType();
        SearchKeywordParamV2 paramV2;
        if (SearchQueryType.NORMAL.equals(queryType)) {
            paramV2 = new KeywordSearchTQueryParam();
        } else if (SearchQueryType.ID.equals(queryType)) {
            paramV2 = new KeywordSearchIdqParam();
        } else if (SearchQueryType.AROUND.equals(queryType)) {
            paramV2 = new KeywordSearchRqbxyParam();
        } else {
            paramV2 = new KeywordSearchSpqParam();
        }

        //搜索关键字; 关键字和周边搜索参数，关键字搜索时为必传
        paramV2.keywords = keywordInfo.getKeyword();
        paramV2.pageParam.pageSize = PAGE_SIZE;
        paramV2.geoObj = getGeoObj(new Coord2DDouble(locationToSearchLocInfo.lon, locationToSearchLocInfo.lat));
        //用户位置,在线搜索必传。
        paramV2.userLoc.lon = locationToSearchLocInfo.lon;
        paramV2.userLoc.lat = locationToSearchLocInfo.lat;

        if (keywordInfo.getCategory() != null && !keywordInfo.getCategory().isEmpty()) {
            paramV2.category = keywordInfo.getCategory();
        }

        if (keywordInfo.getRange() != null && !keywordInfo.getRange().isEmpty()) {
            paramV2.range = keywordInfo.getRange(); //周边搜range和geoObj冲突
        }

        //poiLoc Poi经纬度; 必传
        POI poi = keywordInfo.getPoi();
        GeoPoint point = poi == null ? null : poi.getPoint();
        /* 搜索中心点坐标 */
        //如果是沿途搜的情况下，则以车标为中心搜索
        String city;
        if (((keywordInfo.getBizType() & SearchPoiBizType.VIA_POINT) > 0)) {
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            Coord2DDouble userPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
            paramV2.poiLoc.lon = userPoint.lon;
            paramV2.poiLoc.lat = userPoint.lat;
            city = String.valueOf(SearchDataConvertUtils.getCityCode(location.getLongitude(), location.getLatitude()));
        } else {
            if (point != null && point.getLongitude() != 0) {
                paramV2.poiLoc.lon = point.getLongitude();
            } else {
                paramV2.poiLoc.lon = locationToSearchLocInfo.lon;
            }
            if (point != null && point.getLatitude() != 0) {
                paramV2.poiLoc.lat = point.getLatitude();
            } else {
                paramV2.poiLoc.lat = locationToSearchLocInfo.lat;
            }
            //通过经纬度获取城市编码
            city = poi == null ? null : poi.getAdCode();
            if (TextUtils.isEmpty(city)) {
                city = locationToSearchLocInfo.adcode + "";
            }
        }
        //在线关键字和周边搜索在线参数
        paramV2.city = city;

        if(keywordInfo.getCityCode() !=0) {
            paramV2.offlineParam.adcode = keywordInfo.getCityCode();
        }else{
            paramV2.offlineParam.adcode = NumberUtil.str2Int(city, 10, 0);
        }
        paramV2.switchParam.needParkInfo = true;

        if (isNeedBuildClassifyData) {
            if (!TextUtils.isEmpty(keywordInfo.getClassify())) {
                paramV2.customParam.classifyParam.classifyV2Data = keywordInfo.getClassify();
                paramV2.customParam.classifyParam.retainState = keywordInfo.getRetainState();
                paramV2.customParam.classifyParam.checkedLevel = keywordInfo.getCheckedLevel();
            }
            if (!TextUtils.isEmpty(keywordInfo.getClassifyLevel2())) {
                paramV2.customParam.classifyParam.classifyV2Level2Data = keywordInfo.getClassifyLevel2();
                paramV2.customParam.classifyParam.retainState = keywordInfo.getRetainState();
                paramV2.customParam.classifyParam.checkedLevel = keywordInfo.getCheckedLevel();
            }
            if (!TextUtils.isEmpty(keywordInfo.getClassifyLevel3())) {
                paramV2.customParam.classifyParam.claissfyV2Level3Data = keywordInfo.getClassifyLevel3();
                paramV2.customParam.classifyParam.retainState = keywordInfo.getRetainState();
                paramV2.customParam.classifyParam.checkedLevel = keywordInfo.getCheckedLevel();
            }
        }
        return paramV2;
    }

    /**
     * 获取对角线坐标字符串
     *
     * @param carPos
     * @return
     */
    public String getGeoObj(Coord2DDouble carPos) {
        // 1. 获取POI点的搜索区域范围
        /* 自车位经纬度转地图P20坐标 */
        MapView mapView = MapController.getInstance().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        // 获取地图姿态操作接口, com.autonavi.gbl.map.OperatorPosture
        OperatorPosture operatorPosture = mapView.getOperatorPosture();

        /* 获取当前显示的地图范围 */
        RectDouble rectDouble = operatorPosture.getMapBound();

        String geoobj = "";
        geoobj += rectDouble.left + "|" + rectDouble.top + "|" + rectDouble.right + "|" + rectDouble.bottom;

        return geoobj;
    }

    /**
     * 关键字搜索回调接口
     */
    AbstractSearchKeyWordCallBackV2 igSearchKeyWordObserver = new AbstractSearchKeyWordCallBackV2() {
        private SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetKeyWordResult(int taskid, int euRet, KeywordSearchResultV2 pstResult) {
            if (callbackWrapper == null) {
                return;
            }
            if (euRet == Service.ErrorCodeOK) {
                callbackWrapper.onSuccess(pstResult);
            } else {
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
            callbackWrapper.onComplete();
            callbackWrapper = null;
        }
    };

    class ParallelSearchKeyWordCallBack extends AbstractSearchKeyWordCallBackV2 {
        SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper;

        public ParallelSearchKeyWordCallBack(SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetKeyWordResult(int taskid, int euRet, KeywordSearchResultV2 pstResult) {
            if (callbackWrapper == null) {
                return;
            }
            if (euRet == Service.ErrorCodeOK) {
//                    HmiSearchResult hmiResult = SearchDataConvertUtils.blPoiSearchResultToHmiResult(pstResult, keywordInfo);
                callbackWrapper.onSuccess(pstResult);
            } else {
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
            callbackWrapper.onComplete();
        }
    }

    class SearchEnrouteCategoryCallBack extends SearchEnrouteCategoryCallBackV2 {
        SearchCallbackWrapper<SearchEnrouteCategoryResult> callbackWrapper;

        public SearchEnrouteCategoryCallBack(SearchCallbackWrapper<SearchEnrouteCategoryResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<SearchEnrouteCategoryResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onResult(SearchEnrouteCategoryResult result) {
            if (callbackWrapper == null) {
                return;
            }
            if (result.errorCode == Service.ErrorCodeOK) {
//                    HmiSearchResult hmiResult = SearchDataConvertUtils.blPoiSearchResultToHmiResult(pstResult, keywordInfo);
                callbackWrapper.onSuccess(result);
            } else {
                callbackWrapper.onFailure(result.errorCode, "taskId=" + result.taskId + ",euRet=" + result.errorCode + ",errorMessage=" + result.errorMessage);
            }

            callbackWrapper.onComplete();
        }

    }


    //顺路搜CallBack
    class SearchEnrouteCallBack implements ISearchEnrouteObserver {
        SearchCallbackWrapper<SearchEnrouteResult> callbackWrapper;

        public SearchEnrouteCallBack(SearchCallbackWrapper<SearchEnrouteResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        public void setSearchCallbackWrapper(SearchCallbackWrapper<SearchEnrouteResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onResult(SearchEnrouteResult result) {
            Log.i(TAG,"onResult() called with: result = [" + result + "]");
            if (callbackWrapper == null) {
                return;
            }
            if (result.errorCode == Service.ErrorCodeOK) {
//                    HmiSearchResult hmiResult = SearchDataConvertUtils.blPoiSearchResultToHmiResult(pstResult, keywordInfo);
                callbackWrapper.onSuccess(result);
            } else {
                callbackWrapper.onFailure(result.errorCode, "taskId=" + result.taskId + ",euRet=" + result.errorCode + ",errorMessage=" + result.errorMessage);
            }

            callbackWrapper.onComplete();
        }
    }

    /**
     * 场景搜索回调接口
     */
    AbstractSearchSceneCallBack mSearchSceneObserver = new AbstractSearchSceneCallBack() {

        private SearchCallbackWrapper<SceneSearchResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<SceneSearchResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetSceneResult(int taskid, int euRet, SceneSearchResult sceneSearchResult) {
            if (callbackWrapper == null) {
                return;
            }
            if (euRet == Service.ErrorCodeOK) {
                callbackWrapper.onSuccess(sceneSearchResult);
            } else {
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
            callbackWrapper.onComplete();
            callbackWrapper = null;
        }
    };

    /**
     * POI详情搜索
     *
     * @param requestInfo
     * @param callback
     * @return
     */
    public int poiDetailSearch(@NonNull final SearchRequestInfo requestInfo, SearchCallback<PoiDetailSearchResult> callback) {
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        SearchPoiDetailParam poiDetailParam = new SearchPoiDetailParam();
        SearchRetainParam retainParam = new SearchRetainParam();
        POI poi = requestInfo.getPoi();
        if (poi != null) {
            String adCode = poi.getAdCode();
            if (!TextUtils.isEmpty(adCode)) {
                poiDetailParam.adcode = Integer.parseInt(adCode);
            }
            poiDetailParam.poiId = poi.getId();
            retainParam.keywordBizType = poi.getIndustry();
        }
        poiDetailParam.userLoc.lat = locationToSearchLocInfo.lat;
        poiDetailParam.userLoc.lon = locationToSearchLocInfo.lon;
        poiDetailParam.retainParam = retainParam;
        final SearchCallbackWrapper<PoiDetailSearchResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        mSearchPoiObserver.setSearchCallbackWrapper(callbackWrapper);
        int id = taskId.addAndGet(1);
        mSearchService.poiDetailSearch(poiDetailParam, mSearchPoiObserver, SdkNetworkUtil.getInstance().isNetworkConnected()? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, id);
        return id;
    }

    AbstractSearchPoiDetailCallBack mSearchPoiObserver = new AbstractSearchPoiDetailCallBack() {

        private SearchCallbackWrapper<PoiDetailSearchResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<PoiDetailSearchResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetPoiDetailResult(int taskid, int euRet, PoiDetailSearchResult pstResult) {
            if (callbackWrapper == null) {
                return;
            }
            if (euRet == Service.ErrorCodeOK) {
                callbackWrapper.onSuccess(pstResult);
            } else {
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
            callbackWrapper.onComplete();
            callbackWrapper = null;
        }
    };

    /**
     * 商品详情搜索
     *
     * @param requestInfo
     * @param callback
     * @return
     */
    public int productInfoDetailSearch(@NonNull final SearchRequestInfo requestInfo, SearchCallback<PoiCmallDetailSearchResult> callback) {
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        SearchPoiCmallDetailParam searchPoiCmallDetailParam = new SearchPoiCmallDetailParam();
        POI poi = requestInfo.getPoi();
        if (poi != null) {
            searchPoiCmallDetailParam.poiId = poi.getId();
        }
        searchPoiCmallDetailParam.userLoc.lat = locationToSearchLocInfo.lat;
        searchPoiCmallDetailParam.userLoc.lon = locationToSearchLocInfo.lon;
        searchPoiCmallDetailParam.skuId = requestInfo.getSkuId();
        searchPoiCmallDetailParam.spuId = requestInfo.getSpuId();
        final SearchCallbackWrapper<PoiCmallDetailSearchResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        mSearchProductInfoObserver.setSearchCallbackWrapper(callbackWrapper);
        int id = taskId.addAndGet(1);
        mSearchService.poiCmallDetailSearch(searchPoiCmallDetailParam, mSearchProductInfoObserver, SdkNetworkUtil.getInstance().isNetworkConnected()? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, id);
        return id;
    }

    AbstractSearchProductInfoDetaillCallBack mSearchProductInfoObserver = new AbstractSearchProductInfoDetaillCallBack() {

        private SearchCallbackWrapper<PoiCmallDetailSearchResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<PoiCmallDetailSearchResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetPoiCmallDetailResult(int taskid, int euRet, PoiCmallDetailSearchResult pstResult) {
            if (callbackWrapper == null) {
                return;
            }
            if (euRet == Service.ErrorCodeOK) {
                callbackWrapper.onSuccess(pstResult);
            } else {
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
            callbackWrapper.onComplete();
            callbackWrapper = null;
        }
    };


    /**
     * 适用门店搜索
     *
     * @param requestInfo
     * @param callback
     * @return
     */
    public int shopListInfoSearch(@NonNull final SearchRequestInfo requestInfo, int page, SearchCallback<PoiShopListSearchResult> callback) {
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        SearchPoiCmallDetailParam searchPoiCmallDetailParam = new SearchPoiCmallDetailParam();
        POI poi = requestInfo.getPoi();
        if (poi != null) {
            searchPoiCmallDetailParam.poiId = poi.getId();
        }
        SearchPageParam pageParam = new SearchPageParam();
        pageParam.pageNum = page;
        pageParam.pageSize = 10;
        SearchPoiShopListParam param = new SearchPoiShopListParam();
        searchPoiCmallDetailParam.userLoc.lat = locationToSearchLocInfo.lat;
        searchPoiCmallDetailParam.userLoc.lon = locationToSearchLocInfo.lon;
        searchPoiCmallDetailParam.skuId = requestInfo.getSkuId();
        searchPoiCmallDetailParam.spuId = requestInfo.getSpuId();
        param.page = pageParam;
        param.poiInfo = searchPoiCmallDetailParam;
        final SearchCallbackWrapper<PoiShopListSearchResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        mSearchIPoiShopListObserver.setSearchCallbackWrapper(callbackWrapper);
        int id = taskId.addAndGet(1);
        mSearchService.poiShopListSearch(param, mSearchIPoiShopListObserver, SdkNetworkUtil.getInstance().isNetworkConnected()? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, id);
        return id;
    }

    AbstractIPoiShopListSearchCallBack mSearchIPoiShopListObserver = new AbstractIPoiShopListSearchCallBack() {

        private SearchCallbackWrapper<PoiShopListSearchResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<PoiShopListSearchResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetPoiShopListResult(int taskid, int euRet, PoiShopListSearchResult pstResult) {
            if (callbackWrapper == null) {
                return;
            }
            if (euRet == Service.ErrorCodeOK) {
                callbackWrapper.onSuccess(pstResult);
            } else {
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
            callbackWrapper.onComplete();
            callbackWrapper = null;
        }
    };

    /**
     * 预搜索
     * <pre>
     *  searchController.suggestionSearch(new SearchKeywordInfo.Builder()
     *      .setKeyword(keyword) // 必传
     *      .setPoi(curPoi) // 可空
     *      .setBizType(SearchPoiBizType.NORMAL) // 搜索点表示的业务类型, 请求结束后原样返回给调用方
     *      .build(),
     *    new SearchCallback<HmiSearchResult>())
     * </pre>
     * return taskId
     */
    public int suggestionSearch(@NonNull final SearchRequestInfo keywordInfo, SearchCallback<SuggestionSearchResult> callback) {
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        SearchSuggestionParam sugParam = new SearchSuggestionParam();
        sugParam.keyword = keywordInfo.getKeyword();
        //默认为0 0:为框搜搜索提示 1: 周边搜索提示
        sugParam.type = 1;

        POI poi = keywordInfo.getPoi();
        int cityCode = locationToSearchLocInfo.adcode;
        String adCodeStr = poi == null ? "" : poi.getAdCode();
        if (!TextUtils.isEmpty(adCodeStr)) {
            cityCode = NumberUtil.str2Int(adCodeStr, 10, 0);
        }

        //在线、离线搜索城市
        if(keywordInfo.getCityCode() !=0) {
            sugParam.city = keywordInfo.getCityCode();
        }else{
            sugParam.city = cityCode;
        }
        //离线搜最大结果数,缺省为20。离线参数
        sugParam.offlineParam.resultMaxCount = 10;
        //在线参数。是否返回virtualtip节点。true:返回，其它:不返回。缺省为false。
        sugParam.switchParam.needVirtualTip = true;
        //数据类型,缺省为SearchSuggestionDataType.POI。在线参数，如类别poi, bus, busline, 可以组合使用
        sugParam.dataType = SearchDataType.Poi | SearchDataType.Bus;

        // 经纬度, 在线离线搜索均需要此参数
        GeoPoint point = poi == null ? null : poi.getPoint();
        sugParam.poiLoc.lon = point == null ? locationToSearchLocInfo.mapCenterLon : point.getLongitude();
        sugParam.poiLoc.lat = point == null ? locationToSearchLocInfo.mapCenterLat : point.getLatitude();

        // 用户位置经纬度,在线参数，必传
        sugParam.userLoc.lon = locationToSearchLocInfo.lon;
        sugParam.userLoc.lat = locationToSearchLocInfo.lat;

        // 是否返回行政区划和地址详情等,缺省为false
        sugParam.switchParam.needAdcode = false;

        final SearchCallbackWrapper<SuggestionSearchResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        abstractSuggestKeyWordCallBack.setSuggestCallbackWrapper(callbackWrapper);
        SearchResult result = mSearchService.search(sugParam, abstractSuggestKeyWordCallBack);
        Timber.d( "suggestionSearch, errorCode:" + result.errorCode + ", taskId:" + result.taskId);
        return result.taskId;
    }

    AbstractSuggestKeyWordCallBackV2 abstractSuggestKeyWordCallBack = new AbstractSuggestKeyWordCallBackV2() {
        private SearchCallbackWrapper<SuggestionSearchResult> callbackWrapper;

        @Override
        public void setSuggestCallbackWrapper(SearchCallbackWrapper<SuggestionSearchResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }


        @Override
        public void onGetSuggestionResult(int taskid, int errorCode, SuggestionSearchResult pstResult) {
            Timber.d( "onGetSuggestionResult, taskid:" + taskid + ", errorCode:" + errorCode);
            if (callbackWrapper == null) {
                return;
            }
            if (errorCode == Service.ErrorCodeOK) {
                callbackWrapper.onSuccess(pstResult);
            } else {
                callbackWrapper.onFailure(errorCode, "taskId=" + taskid + ",euRet=" + errorCode);
            }
            callbackWrapper.onComplete();
            callbackWrapper = null;
        }
    };

}
