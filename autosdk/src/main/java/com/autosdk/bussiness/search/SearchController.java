package com.autosdk.bussiness.search;

import android.text.TextUtils;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.OperatorPosture;
import com.autonavi.gbl.search.SearchService;
import com.autonavi.gbl.search.model.SearchAlongWayParam;
import com.autonavi.gbl.search.model.SearchAlongWayResult;
import com.autonavi.gbl.search.model.SearchDeepInfoParam;
import com.autonavi.gbl.search.model.SearchDeepInfoResult;
import com.autonavi.gbl.search.model.SearchKeywordParam;
import com.autonavi.gbl.search.model.SearchKeywordResult;
import com.autonavi.gbl.search.model.SearchLineDeepInfoParam;
import com.autonavi.gbl.search.model.SearchLineDeepInfoResult;
import com.autonavi.gbl.search.model.SearchMode;
import com.autonavi.gbl.search.model.SearchNaviInfoParam;
import com.autonavi.gbl.search.model.SearchNaviInfoResult;
import com.autonavi.gbl.search.model.SearchNearestParam;
import com.autonavi.gbl.search.model.SearchNearestResult;
import com.autonavi.gbl.search.model.SearchSuggestParam;
import com.autonavi.gbl.search.model.SearchSuggestResult;
import com.autonavi.gbl.search.observer.IGSearchDeepInfoObserver;
import com.autonavi.gbl.search.observer.IGSearchKeyWordObserver;
import com.autonavi.gbl.search.observer.IGSearchNaviInfoObserver;
import com.autonavi.gbl.search.observer.IGSearchNearestObserver;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.utils.GsonManager;
import com.autosdk.bussiness.map.MapController;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.search.request.SearchAlongWayInfo;
import com.autosdk.bussiness.search.request.SearchDeepInfo;
import com.autosdk.bussiness.search.request.SearchLineDeepInfo;
import com.autosdk.bussiness.search.request.SearchLocInfo;
import com.autosdk.bussiness.search.request.SearchPoiBizType;
import com.autosdk.bussiness.search.request.SearchRequestInfo;
import com.autosdk.bussiness.search.utils.NumberUtil;
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils;
import com.autosdk.common.utils.SdkNetworkUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import timber.log.Timber;

/**
 * 搜索模块M层
 * 注意:  a. 回调V层callback方法时,要切换到主线程
 * -      b. 各方法内部请使用 {@link SearchCallbackWrapper}, 内部自动进行了主线程切换,避免 V 层子线程更新崩溃
 * <p>
 * 通过 {@link #getInstance()} 来获取并自动进行 searchService 的创建和初始化
 * 通过 {@link #keywordSearch(SearchRequestInfo, SearchCallback)}    来发起在线优先的 关键字搜索/经纬度周边搜索 请求
 * <p>
 * 注意：搜索直接按BL原对象返回，因BL原实体类未做序列化，暂由客户端自行cover
 * 具体参考business模块下StringUtils.toJson/parseJson 方法
 * 如遇对象中需求参数使用不直观或隐藏太深，或未满足，请列设计优化项于语雀
 */
public class SearchController {
    private static final String TAG = "SearchController";
    private SearchService mSearchService;
    private int taskId = 0;

    private SearchController() {

    }

    private static class SearchControllerHolder {
        private static final SearchController instance = new SearchController();
    }

    public static SearchController getInstance() {
        return SearchControllerHolder.instance;
    }

    /**
     * 初始化搜索服务
     */
    public void initService() {
        if (mSearchService == null) {
            mSearchService = (SearchService) ServiceMgr.getServiceMgrInstance()
                    .getBLService(SingleServiceID.SearchSingleServiceID);
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
     * 在线关键字搜索
     *
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
    public int keywordSearch(@NonNull final SearchRequestInfo keywordInfo, boolean isParallel, SearchCallback<SearchKeywordResult> callback) {
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        SearchKeywordParam keywordParam = new SearchKeywordParam();
        keywordParam.query_type = keywordInfo.getQueryType();
        keywordParam.keywords = keywordInfo.getKeyword();
        keywordParam.pagenum = keywordInfo.getPage();
        keywordParam.pagesize = keywordInfo.getSize();
        keywordParam.classify_data = keywordInfo.getFilter();
        keywordParam.is_classify = true;
        //添加sort_rule以及geoobj的获取 , 优先返回排序后的车标周边搜索结果
//        keywordParam.sort_rule = 1;
        keywordParam.geoobj = getGeoObj(new Coord2DDouble(locationToSearchLocInfo.lon, locationToSearchLocInfo.lat));
//        UI模板场景ID , 默认复制101000 ,城市推荐页面发起搜索赋值400002 ,搜索结果页点击推荐词时赋值400001
        keywordParam.utd_sceneid = "101000";
        //用户所在位置经纬度
        keywordParam.user_loc.lon = locationToSearchLocInfo.lon;
        keywordParam.user_loc.lat = locationToSearchLocInfo.lat;

        // Poi经纬度; 在线 query_type!=TQUERY 时为必传
        // 如通过主图周边搜进入，应为以地图中心点坐标进行搜索
        POI poi = keywordInfo.getPoi();
        GeoPoint point = poi == null ? null : poi.getPoint();
        keywordParam.poi_loc.lon = point == null ? locationToSearchLocInfo.mapCenterLon : point.getLongitude();
        keywordParam.poi_loc.lat = point == null ? locationToSearchLocInfo.mapCenterLat : point.getLatitude();
        String city = poi == null ? null : poi.getAdCode();
        if (TextUtils.isEmpty(city)) {
            city = locationToSearchLocInfo.adcode + "";
        }
        if (!TextUtils.isEmpty(keywordInfo.getRange())) {
            keywordParam.range = keywordInfo.getRange();
        }
        //在线关键字和周边搜索在线参数
        keywordParam.city = city;
        if (keywordInfo.getCityCode() != 0) {
            // 区域编码，离线搜索
            keywordParam.adcode = keywordInfo.getCityCode();
        } else {
            // 区域编码，离线搜索
            keywordParam.adcode = NumberUtil.str2Int(city, 10, 0);
        }
        // PoiId搜索参数，poiid搜索时为必传
        keywordParam.id = poi == null ? null : poi.getId();

        // 源码注释: 分类; 在线关键字和周边搜索参数与keywords互斥，默认为空。如category存在，发起搜索时将优先使用category
        int bizType = keywordInfo.getBizType();
        if ((bizType & SearchPoiBizType.CATEGORY) > 0) {
            keywordParam.category = poi == null ? "" : poi.getCategory();
        }
        IGSearchKeyWordObserver observer;
        final SearchCallbackWrapper<SearchKeywordResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        if (isParallel) {
            observer = new ParallelSearchKeyWordCallBack(callbackWrapper);
        } else {
            igSearchKeyWordObserver.setSearchCallbackWrapper(callbackWrapper);
            observer = igSearchKeyWordObserver;
        }
        mSearchService.keyWordSearch(keywordParam, observer, SdkNetworkUtil.getInstance().isNetworkConnected() ? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, ++taskId);
        return taskId;
    }

    /* 获取对角线坐标字符串 */
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

    AbstractSearchKeyWordCallBack igSearchKeyWordObserver = new AbstractSearchKeyWordCallBack() {


        private SearchCallbackWrapper<SearchKeywordResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<SearchKeywordResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetKeyWordResult(int taskid, int euRet, SearchKeywordResult pstResult) {
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
            callbackWrapper = null;
        }
    };

    class ParallelSearchKeyWordCallBack extends AbstractSearchKeyWordCallBack {
        SearchCallbackWrapper<SearchKeywordResult> callbackWrapper;

        public ParallelSearchKeyWordCallBack(SearchCallbackWrapper<SearchKeywordResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<SearchKeywordResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetKeyWordResult(int taskid, int euRet, SearchKeywordResult pstResult) {
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
    public int suggestionSearch(@NonNull final SearchRequestInfo keywordInfo, SearchCallback<SearchSuggestResult> callback) {
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();
        SearchSuggestParam sugParam = new SearchSuggestParam();
        sugParam.keyword = keywordInfo.getKeyword();
        int bizType = keywordInfo.getBizType();
        int sugType;
        if (SearchPoiBizType.AROUND == bizType) {
            sugType = 1;
        } else {
            sugType = 0;
        }
        //默认为0 0:为框搜搜索提示 1: 周边搜索提示
        sugParam.sugType = sugType;

        POI poi = keywordInfo.getPoi();
        int cityCode = locationToSearchLocInfo.adcode;
        String adCodeStr = poi == null ? "" : poi.getAdCode();
        if (!TextUtils.isEmpty(adCodeStr)) {
            cityCode = NumberUtil.str2Int(adCodeStr, 10, 0);
        }

        //在线搜索城市
        sugParam.city = cityCode;

        //离线搜索城市
        if (keywordInfo.getCityCode() != 0) {
            sugParam.offlineAdminCode = keywordInfo.getCityCode();
        } else {
            sugParam.offlineAdminCode = cityCode;
        }
        sugParam.offlineResultMaxCount = 10;
        //是否返回virtualtip节点,缺省为空。在线参数 如true:返回 其它:不返回
        sugParam.need_vir = "true";
        //数据类型,缺省为“poi”。在线参数，如类别poi, bus, busline, keyword，可以组合使用，用“|”分隔，如：“poi|keyword” 表示获取POI和热词
        sugParam.datatype = "poi|bus";

        // 经纬度, 在线离线搜索均需要此参数
        GeoPoint point = poi == null ? null : poi.getPoint();
        sugParam.poi_loc.lon = point == null ? locationToSearchLocInfo.mapCenterLon : point.getLongitude();
        sugParam.poi_loc.lat = point == null ? locationToSearchLocInfo.mapCenterLat : point.getLatitude();

        // 用户位置经纬度,在线参数，必传
        sugParam.user_loc.lon = locationToSearchLocInfo.lon;
        sugParam.user_loc.lat = locationToSearchLocInfo.lat;

        // 是否返回行政区划和地址详情等,缺省为false
        sugParam.adcode = true;

        if (SearchPoiBizType.AROUND != bizType) {
            // 分类信息,缺省为空。在线参数 如类别05, 0501, 050101，多个类别同时查询时用"|"号分割(如050101|151101)
            sugParam.category = poi == null ? "" : poi.getCategory();
        }

        final SearchCallbackWrapper<SearchSuggestResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        abstractSuggestKeyWordCallBack.setSuggestCallbackWrapper(callbackWrapper);
        mSearchService.suggestionSearch(sugParam, abstractSuggestKeyWordCallBack, SdkNetworkUtil.getInstance().isNetworkConnected() ? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, ++taskId);
        return taskId;
    }

    AbstractSuggestKeyWordCallBack abstractSuggestKeyWordCallBack = new AbstractSuggestKeyWordCallBack() {
        private SearchCallbackWrapper<SearchSuggestResult> callbackWrapper;

        @Override
        public void setSuggestCallbackWrapper(SearchCallbackWrapper<SearchSuggestResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetSuggestionResult(int taskid, int euRet, SearchSuggestResult pstResult) {
            if (callbackWrapper == null) {
                return;
            }
            if (euRet == Service.ErrorCodeOK) {
//                    HmiSearchResult hmiResult = SearchDataConvertUtils.convertSuggestionResult(pstResult, keywordInfo);
                callbackWrapper.onSuccess(pstResult);
            } else {
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
            callbackWrapper.onComplete();
            callbackWrapper = null;
        }
    };

    /**
     * 发起逆地理搜索
     *
     * @param callback return taskId
     */
    public int nearestSearch(@NonNull GeoPoint geoPoint, SearchCallback<SearchNearestResult> callback) {
        SearchNearestParam nearestParam = new SearchNearestParam();
        nearestParam.poi_loc.lon = geoPoint.getLongitude();
        nearestParam.poi_loc.lat = geoPoint.getLatitude();
        if (mSearchService == null) {
            return Service.ErrorCodeFailed;
        }

        final IGSearchNearestObserver nearestSearchCallBack = new IGSearchNearestObserver() {

            @Override
            public void onGetNearestResult(int taskid, int euRet, SearchNearestResult result) {
                //POI数据为0时本质上也是失败，本处建议BL增加错误类型处理
                if (euRet == Service.ErrorCodeOK && result.poi_list.size() != 0) {
                    Timber.d("nearestSearch pstResult.poi_list.size:" + result.poi_list.size());
                    callback.onSuccess(result);
                } else {//失败时euRet也有可能为Service.ErrorCodeOK，因为返回pstResult.poi_list.size为0
                    callback.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
                }
                callback.onComplete();
            }
        };

        mSearchService.nearestSearch(nearestParam, nearestSearchCallBack, SdkNetworkUtil.getInstance().isNetworkConnected() ? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, ++taskId);
        return taskId;
    }

    private AbstractNearestSearchCallBack mAbstractNearestSearchCallBack = new AbstractNearestSearchCallBack() {

        private SearchCallbackWrapper<SearchNearestResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<SearchNearestResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetNearestResult(int taskid, int euRet, SearchNearestResult result) {
            //POI数据为0时本质上也是失败，本处建议BL增加错误类型处理
            if (euRet == Service.ErrorCodeOK && result.poi_list.size() != 0) {
                Timber.d("nearestSearch pstResult.poi_list.size:" + result.poi_list.size());
                callbackWrapper.onSuccess(result);
            } else {//失败时euRet也有可能为Service.ErrorCodeOK，因为返回pstResult.poi_list.size为0
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
            callbackWrapper.onComplete();
        }
    };

    /**
     * 深度信息搜索
     *
     * @param searchDeepInfo
     * @param callback
     * @return
     */
    public int deepInfoSearch(@NonNull SearchDeepInfo searchDeepInfo, SearchCallback<SearchDeepInfoResult> callback) {
        final SearchCallbackWrapper<SearchDeepInfoResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        SearchDeepInfoParam searchDeepInfoParam = new SearchDeepInfoParam();
        searchDeepInfoParam.poi_loc.lon = searchDeepInfo.getGeoPoint().getLongitude();
        searchDeepInfoParam.poi_loc.lat = searchDeepInfo.getGeoPoint().getLatitude();
        searchDeepInfoParam.poiid = searchDeepInfo.getPoiid();
        mSearchService.deepInfoSearch(searchDeepInfoParam, new IGSearchDeepInfoObserver() {
            @Override
            public void onGetDeepInfoResult(int taskid, int euRet, SearchDeepInfoResult pstResult) {
                //POI数据为0时本质上也是失败，本处建议BL增加错误类型处理
                if (euRet == Service.ErrorCodeOK && pstResult.deepinfoPoi != null) {
                    callbackWrapper.onSuccess(pstResult);
                } else {//失败时euRet也有可能为Service.ErrorCodeOK，因为返回pstResult.poi_list.size为0
                    callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
                }
            }


        }, SdkNetworkUtil.getInstance().isNetworkConnected() ? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, ++taskId);
        return taskId;
    }

    /**
     * 沿途批量获取poi点的深度信息
     *
     * @param searchLineDeepInfo
     * @param callback
     * @return
     */
    public int lineDeepInfoSearch(@NonNull SearchLineDeepInfo searchLineDeepInfo, SearchCallback<SearchLineDeepInfoResult> callback) {
        final SearchCallbackWrapper<SearchLineDeepInfoResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        SearchLineDeepInfoParam deepInfoParam = new SearchLineDeepInfoParam();
        deepInfoParam.poiIds = searchLineDeepInfo.getPoiIds();
        deepInfoParam.queryType = searchLineDeepInfo.getQueryType();
        mAbstractLineDeepInfoSearchCallBack.setSearchCallbackWrapper(callbackWrapper);
        mSearchService.lineDeepInfoSearch(deepInfoParam, mAbstractLineDeepInfoSearchCallBack, SdkNetworkUtil.getInstance().isNetworkConnected() ? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, ++taskId);
        return taskId;
    }

    AbstractLineDeepInfoSearchCallBack mAbstractLineDeepInfoSearchCallBack = new AbstractLineDeepInfoSearchCallBack() {
        private SearchCallbackWrapper<SearchLineDeepInfoResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<SearchLineDeepInfoResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetLineDeepInfoResult(int taskid, int euRet, SearchLineDeepInfoResult result) {
            //POI数据为0时本质上也是失败，本处建议BL增加错误类型处理
            if (euRet == Service.ErrorCodeOK && result.data.size() != 0) {
                Timber.d("pstResult.data.size = " + result.data.size());
                callbackWrapper.onSuccess(result);
            } else {//失败时euRet也有可能为Service.ErrorCodeOK，因为返回pstResult.data.size为0
                callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
            }
        }
    };

    /**
     * 沿途搜(在线优先)
     * <p>
     * 使用方法:
     * <pre>
     *      searchController.alongWaySearch(new SearchAlongWayInfo.Builder()
     *                  .setKeyword("充电站") // 关键字
     *                  .setGeolinePointList(geolinePointList) // 必传, 路线抽稀点列表,非空
     *                  .setFilterCondition("") // 可选, 过滤条件
     *                  .setStartPoint(startPoint) // 可选, 算路起点, 起点终点请一并设置
     *                  .setEndPoint(endPoint) // 可选, 算路终点, 起点终点请一并设置
     *                  .setViaPointList(viaPointList) // 可选, 算路途经点列表,可空, 默认为null
     *                  .build(),
     *          new SearchCallback<HmiSearchResult>());
     * </pre>
     *
     * @return taskId
     */
    public int alongWaySearch(@NonNull SearchAlongWayInfo alongWayInfo,
                              @NonNull SearchCallback<SearchAlongWayResult> callback, boolean hasNetWork) {

        SearchCallbackWrapper searchAlongWayResultCallback = new SearchCallbackWrapper<>(callback);
        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getLocationToSearchLocInfo();

        // 路线上坐标点(至少两个)使用;分隔，如: 经度;纬度;经度;纬度，在线必传参数
        String flag = ";";
        String geoline = SearchDataConvertUtils.convertPointList2String(alongWayInfo.getGeolinePointList(), flag);

        // 沿途搜起点、途经点坐标、终点，用分号间隔(最后不加分号)
        List<GeoPoint> routePointList = alongWayInfo.getViaPointList();
        if (routePointList == null) {
            routePointList = new ArrayList<>();
        }

        routePointList.add(alongWayInfo.getEndPoint());
        routePointList.add(0, alongWayInfo.getStartPoint());
        String routePoints = SearchDataConvertUtils.convertPointList2String(routePointList, flag);

        SearchAlongWayParam para = new SearchAlongWayParam();
        // 城市编码，离线搜索条件, 离线搜索必填
        para.adcode = locationToSearchLocInfo.adcode;
        // 用户位置，经度,纬度 离线搜索用
        para.user_loc.lon = locationToSearchLocInfo.lon;
        para.user_loc.lat = locationToSearchLocInfo.lat;

        // 在线沿路径道路点信息字符串 ，在线搜索条件，在线搜索必填
        para.geoline = geoline;
        // 在线搜索筛选条件
        para.auto_attr_filter = alongWayInfo.getFilterCondition();
        // 是否需要是否需要到eta，默认为false，在线参数必填
        para.need_eta = true;
        // 是否需要到达点信息，默认为false，在线参数
        para.need_naviinfo = true;
        // 起点、途经点坐标、终点，用分号间隔，必填字段，在线参数
        para.routepoints = routePoints;
        // 请求沿途搜接口的场景,1-行中，2-行前,必填，默认1
        para.navi_scene = alongWayInfo.isNaving() ? 1 : 2;
        // 导航类型,1-骑行,4-步行,2-驾车，5-货车，9-摩托车 必填 默认2
        para.navi_type = alongWayInfo.getNaviType();

        para.keyword = alongWayInfo.getKeyword();
        // 离线指定引导路径道路,离线搜索必填
        para.guideRoads = alongWayInfo.getGuideRoads();
        if (alongWayInfo.getCategory() != null && !alongWayInfo.getCategory().isEmpty()) {
            para.category = alongWayInfo.getCategory();
        }
        // 下发路线的能耗信息
        para.contentoptions = 0x2000;
        para.route_range = 1000;
        //不压缩
        para.linkid_format = 0;
        //64位ID
        para.linkid_type = 3;
        para.need_gasprice = alongWayInfo.getIsNeedGasprice();
        mSearchAlongWayResultCallback.setSearchCallbackWrapper(searchAlongWayResultCallback);
        mSearchService.alongWaySearch(para, mSearchAlongWayResultCallback, hasNetWork ? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, ++taskId);
        return taskId;
    }

    AbstractAlongWaySearchCallBack mSearchAlongWayResultCallback = new AbstractAlongWaySearchCallBack() {

        private SearchCallbackWrapper<SearchAlongWayResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<SearchAlongWayResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onGetAlongWayResult(int taskid, int euRet, SearchAlongWayResult pstResult) {
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
     * 子到达点搜索(在线优先)
     * <p>
     * 使用方法:
     *
     * @return taskId
     */
    public int naviInfoSearch(@NonNull SearchNaviInfoParam searchNaviInfoParam,
                              @NonNull SearchCallback<SearchNaviInfoResult> callback) {

        final SearchCallbackWrapper<SearchNaviInfoResult> callbackWrapper = new SearchCallbackWrapper<>(callback);
        mSearchService.naviInfoSearch(searchNaviInfoParam, new IGSearchNaviInfoObserver() {
            @Override
            public void onGetNaviInfoResult(int taskid, int euRet, SearchNaviInfoResult pstResult) {
                if (euRet == Service.ErrorCodeOK && pstResult.naviInfoItem.size() != 0) {
                    Timber.d("pstResult.data.size = " + pstResult.naviInfoItem.size());
                    callbackWrapper.onSuccess(pstResult);
                } else {//失败时euRet也有可能为Service.ErrorCodeOK，因为返回pstResult.data.size为0
                    callbackWrapper.onFailure(euRet, "taskId=" + taskid + ",euRet=" + euRet);
                }
            }
        }, SdkNetworkUtil.getInstance().isNetworkConnected() ? SearchMode.SEARCH_MODE_ONLINE_ADVANCED : SearchMode.SEARCH_MODE_OFFLINE_ADVANCED, ++taskId);

        return taskId;
    }


}
