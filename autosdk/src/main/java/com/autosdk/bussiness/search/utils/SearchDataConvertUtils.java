package com.autosdk.bussiness.search.utils;

import static com.autosdk.common.utils.CommonUtil.distanceUnitTransform;

import android.annotation.SuppressLint;
import android.location.Location;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.autonavi.gbl.aosclient.model.WsShieldSearchRanklistPortalRecommendData;
import com.autonavi.gbl.aosclient.model.WsShieldSearchRanklist_landingRecommendData;
import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.common.path.model.RestAreaInfo;
import com.autonavi.gbl.data.model.Area;
import com.autonavi.gbl.data.model.CityItemInfo;
import com.autonavi.gbl.data.model.DownLoadMode;
import com.autonavi.gbl.information.nearby.model.NearbyRecommendPoiInfo;
import com.autonavi.gbl.layer.model.AlongRouteMode;
import com.autonavi.gbl.layer.model.BizLayerUtil;
import com.autonavi.gbl.layer.model.BizRouteRestAreaInfo;
import com.autonavi.gbl.layer.model.BizSearchAlongWayPoint;
import com.autonavi.gbl.layer.model.BizSearchChildPoint;
import com.autonavi.gbl.layer.model.BizSearchParentPoint;
import com.autonavi.gbl.layer.model.ChargeStationType;
import com.autonavi.gbl.layer.model.PoiParentType;
import com.autonavi.gbl.layer.model.SearchAlongwayType;
import com.autonavi.gbl.search.model.AlongWayPoi;
import com.autonavi.gbl.search.model.ChargingStationInfo;
import com.autonavi.gbl.search.model.DeepinfoPoi;
import com.autonavi.gbl.search.model.KeywordSearchResultV2;
import com.autonavi.gbl.search.model.LinePoiBase;
import com.autonavi.gbl.search.model.LinePoiServiceAreaInfo;
import com.autonavi.gbl.search.model.NearestPoi;
import com.autonavi.gbl.search.model.PoiDetailProductInfo;
import com.autonavi.gbl.search.model.PoiDetailSearchResult;
import com.autonavi.gbl.search.model.PoiDetailShelfInfo;
import com.autonavi.gbl.search.model.PoiItemlateTypeId;
import com.autonavi.gbl.search.model.PoiTemplateConstant;
import com.autonavi.gbl.search.model.PricePoiInfo;
import com.autonavi.gbl.search.model.SearchAlongWayResult;
import com.autonavi.gbl.search.model.SearchCategoryInfo;
import com.autonavi.gbl.search.model.SearchChildCategoryInfo;
import com.autonavi.gbl.search.model.SearchClassifyInfo;
import com.autonavi.gbl.search.model.SearchCommonTemplate;
import com.autonavi.gbl.search.model.SearchDeepInfoResult;
import com.autonavi.gbl.search.model.SearchEnroutePoiInfo;
import com.autonavi.gbl.search.model.SearchEnrouteResult;
import com.autonavi.gbl.search.model.SearchKeywordResult;
import com.autonavi.gbl.search.model.SearchLabelInfo;
import com.autonavi.gbl.search.model.SearchLabelType;
import com.autonavi.gbl.search.model.SearchLineDeepInfoResult;
import com.autonavi.gbl.search.model.SearchNaviInfoBase;
import com.autonavi.gbl.search.model.SearchNearestResult;
import com.autonavi.gbl.search.model.SearchParkInOutInfo;
import com.autonavi.gbl.search.model.SearchParkInfo;
import com.autonavi.gbl.search.model.SearchPoi;
import com.autonavi.gbl.search.model.SearchPoiBase;
import com.autonavi.gbl.search.model.SearchPoiBasicInfo;
import com.autonavi.gbl.search.model.SearchPoiChildInfo;
import com.autonavi.gbl.search.model.SearchPoiGasInfo;
import com.autonavi.gbl.search.model.SearchPoiInfo;
import com.autonavi.gbl.search.model.SearchPoiParkingInfo;
import com.autonavi.gbl.search.model.SearchProductInfo;
import com.autonavi.gbl.search.model.SearchProductInfoBase;
import com.autonavi.gbl.search.model.SearchSuggestPoiBase;
import com.autonavi.gbl.search.model.SearchSuggestResult;
import com.autonavi.gbl.search.model.SearchSuggestTip;
import com.autonavi.gbl.search.model.SearchSuggestionPoiTip;
import com.autonavi.gbl.search.model.SearchTextTemplate;
import com.autonavi.gbl.user.usertrack.model.HistoryRouteItem;
import com.autonavi.gbl.user.usertrack.model.SearchHistoryItem;
import com.autosdk.R;
import com.autosdk.bussiness.account.UserTrackController;
import com.autosdk.bussiness.common.AlongWayPoiDeepInfo;
import com.autosdk.bussiness.common.AlongWaySearchPoi;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.POIFactory;
import com.autosdk.bussiness.common.utils.GsonManager;
import com.autosdk.bussiness.common.utils.InformationTypeUtil;
import com.autosdk.bussiness.data.MapDataController;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.search.bean.PoiDetailProductBean;
import com.autosdk.bussiness.search.request.SearchLocInfo;
import com.autosdk.bussiness.search.request.SearchPoiBizType;
import com.autosdk.bussiness.search.request.SearchRequestInfo;
import com.autosdk.bussiness.search.result.HmiSearchInfo;
import com.autosdk.bussiness.search.result.HmiSearchResult;
import com.autosdk.bussiness.search.result.ResponseHeaderModule;
import com.autosdk.bussiness.search.result.city.AdCity;
import com.autosdk.bussiness.widget.ui.util.ListUtil;
import com.autosdk.bussiness.widget.ui.util.StringUtils;
import com.autosdk.common.SdkApplicationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * 搜索结果转换工具类: 将BL类型转换解耦为 hmi 使用的类
 * 其他工具方法:
 * 1. 通过 {@link #getLocationToSearchLocInfo()} 获取定位相关数据,包括车标/地图中心经纬度,当前城市信息等
 */
public class SearchDataConvertUtils {
    private static final String TAG = "SearchDataConvertUtils";
    private static final int BL_RESULT_ONLINE = 1;
    private static final int BL_RESULT_OFFLINE = 0;
    private static SearchLocInfo mSearchLocInfo;
    private static Map<String, Integer> mLabelPriority = new HashMap<String, Integer>() {{
        put("最顺路", 100);
        put("最近", 90);
        put("熟悉", 80);
        put("近终点", 70);
        put("热门", 60);
        put("服务区", 50);
        put("下高速", 40);
    }};
    /**
     * 关键字搜索结果转化
     */
    @NonNull
    public static HmiSearchResult blPoiSearchResultToHmiResult(SearchKeywordResult blResult, @NonNull SearchRequestInfo keywordInfo) {
        HmiSearchResult hmiResult = new HmiSearchResult();
        if (blResult == null) {
            Timber.d("blPoiSearchResultToHmiResult: = null");
            return hmiResult;
        }
        hmiResult.mKeyword = blResult.keyword;
        hmiResult.responseHeader = new ResponseHeaderModule();
        //poi类型：0离线数据，1在线数据
        hmiResult.responseHeader.isOnLine = blResult.iPoiType == 1;
        hmiResult.responseHeader.version = blResult.version;
        hmiResult.responseHeader.errorCode = blResult.code;
        hmiResult.responseHeader.result = true;

        hmiResult.searchInfo = getSearchInfo(blResult);
        hmiResult.searchInfo.poiBizType = keywordInfo.getBizType();
        return hmiResult;
    }

    /**
     * 关键字搜索结果转化
     */
    @NonNull
    public static HmiSearchResult blPoiSearchResultToHmiResult(SearchKeywordResult blResult, @SearchPoiBizType int BizType) {
        HmiSearchResult hmiResult = new HmiSearchResult();
        if (blResult == null) {
            Timber.d("blPoiSearchResultToHmiResult: = null");
            return hmiResult;
        }
        hmiResult.mKeyword = blResult.keyword;
        hmiResult.responseHeader = new ResponseHeaderModule();
        //poi类型：0离线数据，1在线数据
        hmiResult.responseHeader.isOnLine = blResult.iPoiType == 1;
        hmiResult.responseHeader.version = blResult.version;
        hmiResult.responseHeader.errorCode = blResult.code;
        hmiResult.responseHeader.result = true;

        hmiResult.searchInfo = getSearchInfo(blResult);
        hmiResult.searchInfo.poiBizType = BizType;
        return hmiResult;
    }

    /**
     * 关键字搜索结果转化
     */
    @NonNull
    public static HmiSearchResult blPoiSearchResultToHmiResult(KeywordSearchResultV2 blResult, @NonNull SearchRequestInfo keywordInfo) {
        HmiSearchResult hmiResult = new HmiSearchResult();
        if (blResult == null) {
            Timber.d("blPoiSearchResultToHmiResult: = null");
            return hmiResult;
        }
        hmiResult.mKeyword = blResult.keyword;
        hmiResult.responseHeader = new ResponseHeaderModule();
        //poi类型：0离线数据，1在线数据
        hmiResult.responseHeader.isOnLine = blResult.poiType == 1;
        hmiResult.responseHeader.errorCode = blResult.code;
        hmiResult.responseHeader.result = true;

        hmiResult.searchInfo = getSearchInfo(blResult);
        hmiResult.searchInfo.poiBizType = keywordInfo.getBizType();
        return hmiResult;
    }

    /**
     * 关键字搜索结果转化
     */
    @NonNull
    public static HmiSearchResult blPoiSearchResultToHmiResult(KeywordSearchResultV2 blResult, @SearchPoiBizType int BizType) {
        HmiSearchResult hmiResult = new HmiSearchResult();
        if (blResult == null) {
            Timber.d("blPoiSearchResultToHmiResult: = null");
            return hmiResult;
        }
        hmiResult.mKeyword = blResult.keyword;
        hmiResult.responseHeader = new ResponseHeaderModule();
        //poi类型：0离线数据，1在线数据
        hmiResult.responseHeader.isOnLine = blResult.poiType == 1;
        hmiResult.responseHeader.errorCode = blResult.code;
        hmiResult.responseHeader.result = true;

        hmiResult.searchInfo = getSearchInfo(blResult);
        hmiResult.searchInfo.poiBizType = BizType;
        return hmiResult;
    }

    private static HmiSearchInfo getSearchInfo(KeywordSearchResultV2 blResult) {
        //返回SearchInfo不能为空
        HmiSearchInfo hmiSearchInfo = new HmiSearchInfo();
        hmiSearchInfo.poiTotalSize = blResult.total;
        if (blResult.poiList != null && blResult.poiList.size() > 0) {
            Timber.d("getSearchInfo: blResult.poiList.length = " + blResult.poiList.size());
            hmiSearchInfo.poiResults = new ArrayList<>();
            for (SearchPoiInfo blSearchPoi : blResult.poiList) {
                POI poi = getSearchPoiFromBlSearchPoi(blSearchPoi);
                hmiSearchInfo.poiResults.add(poi);
            }
        }
        return hmiSearchInfo;
    }

    private static HmiSearchInfo getSearchInfo(SearchKeywordResult blResult) {
        //返回SearchInfo不能为空
        HmiSearchInfo hmiSearchInfo = new HmiSearchInfo();
        hmiSearchInfo.isGeneralSearch = blResult.isGeneralSaearch;
        hmiSearchInfo.poiTotalSize = blResult.total;
        if (blResult.poiList != null && blResult.poiList.size() > 0) {
            Timber.d("getSearchInfo: blResult.poiList.length = " + blResult.poiList.size());
            hmiSearchInfo.poiResults = new ArrayList<>();
            for (SearchPoi blSearchPoi : blResult.poiList) {
                POI poi = getSearchPoiFromBlSearchPoi(blSearchPoi);
                hmiSearchInfo.poiResults.add(poi);
            }
        }
        return hmiSearchInfo;
    }

    public static POI getSearchPoiFromBlSearchPoi(SearchPoiInfo blSearchPoi) {
        if (blSearchPoi == null) {
            Timber.d("getSearchPoiFromBlSearchPoi blSearchPoi == null");
            return null;
        }

        if (blSearchPoi.basicInfo == null) {
            Timber.d("getSearchPoiFromBlSearchPoi blSearchPoi.basicInfo == null");
            return null;
        }

        POI poi = POIFactory.createPOI();
        convertSearchPoiBasicInfoToPoi(poi, blSearchPoi.basicInfo);
        poi.setChargeStationInfo(blSearchPoi.chargingStationInfo);
        poi.setParkingInfo(blSearchPoi.parkingInfo);
        poi.setSearchProductInfoList(blSearchPoi.produceInfoList);
        poi.setPoiPhoto(blSearchPoi.photoInfo);
        poi.setPoiRankInfo(blSearchPoi.rankInfo);
        poi.setChildPois(getChildPoiList(blSearchPoi.childInfoList));//子POI数据
        poi.setGasInfo(blSearchPoi.gasInfo);
        poi.setNearbyInfo(blSearchPoi.nearbyInfo);

        //停车场出入口
        SearchPoiParkingInfo searchParkInfo = blSearchPoi.parkingInfo;
        if (searchParkInfo != null) {
            ArrayList<GeoPoint> parkInfos = new ArrayList<>();
            ArrayList<SearchParkInOutInfo> searchParkInOutInfos = searchParkInfo.inoutInfoList;
            for (int i = 0; searchParkInOutInfos != null && i < searchParkInOutInfos.size(); i++) {
                parkInfos.add(new GeoPoint(searchParkInOutInfos.get(i).x, searchParkInOutInfos.get(i).y));
            }
            poi.setParkInfos(parkInfos);
        }
        return poi;
    }

    private static void convertSearchPoiBasicInfoToPoi(POI poi, SearchPoiBasicInfo poiBase) {
        poi.setAdCode(String.valueOf(poiBase.adcode));
        poi.setIndustry(poiBase.industry);
        poi.setDistance(poiBase.distance);
        poi.setAddr(poiBase.address);
        poi.setPoint(new GeoPoint(poiBase.location.lon, poiBase.location.lat));
        poi.setFloorNo(poiBase.floorNo);
        poi.setName(poiBase.name);
        poi.setPhone(poiBase.tel);
        poi.setId(poiBase.poiId);
        poi.setTypeCode(poiBase.typeCode);
        poi.setParent(poiBase.parentPoiId);
        poi.setChildType(poiBase.childType);
        poi.setCategory(poiBase.typeCode);
        poi.setPoiTag(poiBase.tag);
        poi.setSubIndustry(poiBase.subIndustry);
        poi.setHotInfo(poiBase.hotInfo);
        poi.setDistrict(poiBase.districtName);
        poi.setRating(poiBase.rating);
        poi.setReviewTotal(poiBase.reviewTotal);
        poi.setAverageCost(poiBase.averageCost);
        poi.setImageUrl(poiBase.imageUrl);
        poi.setDeepinfo(poiBase.openTime);
        poi.setHisMark(poiBase.mark);
        poi.setScenicMark(poiBase.scenicMark.featured);
        poi.setOpenStatus(poiBase.openStatus);
        poi.setFastestArrivalState(poiBase.isFastest);
        poi.setIsClosest(poiBase.isClosest);
        poi.setFeaturedLabel(poiBase.featuredLabel);
        poi.setChargeLeftPercentage(poiBase.chargeInfo.vehicleChargeLeft);
        if (poiBase.naviVisit != null) {
            poi.setNaviMonthUv(poiBase.naviVisit.monthUv);
        }
        if (TextUtils.isEmpty(poi.getDis())) {
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
            Coord2DDouble endPoint = new Coord2DDouble(poiBase.location.lon, poiBase.location.lat);
            poi.setDis("" + distanceUnitTransform((long) BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint)));
        }
        if (TextUtils.isEmpty(poi.getCityName())) {
            if (mSearchLocInfo == null) {
                mSearchLocInfo = new SearchLocInfo();
            }
            // 获取城市adcode和城市名
            MapDataController mapDataController = SDKManager.getInstance().getMapDataController();
            mSearchLocInfo.adcode = mapDataController.getAdcodeByLonLat(poiBase.location.lon, poiBase.location.lat);
            CityItemInfo cityInfo = mapDataController.getCityInfo(mSearchLocInfo.adcode);
            mSearchLocInfo.cityName = cityInfo == null ? "" : cityInfo.cityName;
            poi.setCityName(mSearchLocInfo.cityName);
            poi.setCityCode(String.valueOf(mSearchLocInfo.adcode));
        }
        //poi的出入口数据
        ArrayList<GeoPoint> geoEntrancesList = new ArrayList<>();
        ArrayList<Coord2DDouble> entrancesList = poiBase.entranceList;
        int entranceListSize = entrancesList == null ? 0 : entrancesList.size();

        if (entranceListSize > 0) {
            for (int i2 = 0; i2 < entranceListSize; i2++) {
                Coord2DDouble item = entrancesList.get(i2);
                GeoPoint exitEntrancePoint = new GeoPoint();
                exitEntrancePoint.setLonLat(item.lon, item.lat);
                geoEntrancesList.add(exitEntrancePoint);
            }
            poi.setEntranceList(geoEntrancesList);
        }

        ArrayList<ArrayList<Coord2DDouble>> poiPolygonBounds = poiBase.poiAoiBounds;
        if (poiPolygonBounds != null && !poiPolygonBounds.isEmpty()) {
            ArrayList<ArrayList<GeoPoint>> poiPolygonBoundList = SearchResultUtils.coordList2GeoPointList(poiPolygonBounds);
            poi.setPoiPolygonBounds(poiPolygonBoundList);
        }

        ArrayList<ArrayList<Coord2DDouble>> poiRoadaoiBounds = poiBase.roadPolygonBounds;
        if (poiRoadaoiBounds != null && !poiRoadaoiBounds.isEmpty()) {
            ArrayList<ArrayList<GeoPoint>> geoPointRoadAoiBound = new ArrayList<>();
            for (int i = 0; i < poiRoadaoiBounds.size(); i++) {
                ArrayList<GeoPoint> poiRoadaoi = new ArrayList<>();
                for (int j = 0; j < poiRoadaoiBounds.get(i).size(); j++) {
                    poiRoadaoi.add(new GeoPoint(poiRoadaoiBounds.get(i).get(j).lon, poiRoadaoiBounds.get(i).get(j).lat));
                }
                geoPointRoadAoiBound.add(poiRoadaoi);
            }
            poi.setPoiRoadaoiBounds(geoPointRoadAoiBound);
        }
    }

    public static ArrayList<POI> getChildPoiList(ArrayList<SearchPoiChildInfo> searchChildList) {

        if (searchChildList == null || searchChildList.isEmpty()) {
            return null;
        }

        ArrayList<POI> childPoiList = new ArrayList<>();
        for (int i = 0; i < searchChildList.size(); i++) {
            SearchPoiChildInfo childPoiBase = searchChildList.get(i);
            POI childPoi = POIFactory.createPOI();
            String childName = TextUtils.isEmpty(childPoiBase.shortName) ? childPoiBase.name : childPoiBase.shortName;
            childPoi.setId(childPoiBase.poiId);
            childPoi.setName(childName);
            childPoi.setAddr(childPoiBase.address);
            childPoi.setPoint(new GeoPoint(childPoiBase.location.lon, childPoiBase.location.lat));
            childPoi.setShortname(childPoiBase.shortName);
            childPoi.setRatio(childPoiBase.ratio);

            //入口经纬度
            ArrayList<GeoPoint> childEntranceList = new ArrayList<>();
            childEntranceList.add(new GeoPoint(childPoiBase.pointEnter.lon, childPoiBase.pointEnter.lat));
            childPoi.setEntranceList(childEntranceList);
            childPoi.setChildType(childPoiBase.childType);
            // 子点充电站信息
            if (childPoiBase.chargingStationList != null && !childPoiBase.chargingStationList.isEmpty()) {
                ChargingStationInfo chargeStationInfo = new ChargingStationInfo();
                chargeStationInfo.fast_total = childPoiBase.chargingStationList.get(0).numFast + "";
                chargeStationInfo.slow_total = childPoiBase.chargingStationList.get(0).numSlow + "";
                chargeStationInfo.current_ele_price = childPoiBase.chargingStationList.get(0).price + "";
                childPoi.setChargeStationInfo(chargeStationInfo);
            }
            childPoiList.add(childPoi);
        }
        return childPoiList;
    }

    public static POI getSearchPoiFromBlSearchPoi(SearchPoi blSearchPoi) {
        if (blSearchPoi == null) {
            Timber.d("getSearchPoiFromBlSearchPoi blSearchPoi == null");
            return null;
        }

        POI poi = POIFactory.createPOI();
        SearchPoiBase poiBase = blSearchPoi.poi;
        poi.setIndustry(poiBase.industry);
        poi.setDistance(poiBase.distance);
        poi.setAddr(poiBase.address);
        poi.setPoint(new GeoPoint(poiBase.poi_loc.lon, poiBase.poi_loc.lat));
        poi.setFloorNo(poiBase.floorNo);
        poi.setName(poiBase.name);
        poi.setPhone(poiBase.tel);
        poi.setId(poiBase.poiid);
        poi.setTypeCode(poiBase.typecode);
        poi.setParent(poiBase.parent);
        poi.setChildType(poiBase.childType);
        poi.setCategory(poiBase.typecode);
//        poi.setDis(getDis(blSearchPoi.mTempDataMap));
        poi.setPoiTag(poiBase.tag);
        poi.setChargeStationInfo(blSearchPoi.chargingStationInfo);
        poi.setDeepinfo(poiBase.deepinfo);
        if (TextUtils.isEmpty(poi.getDis())) {
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
            Coord2DDouble endPoint = new Coord2DDouble(poiBase.poi_loc.lon, poiBase.poi_loc.lat);
            poi.setDis(distanceUnitTransform((long) BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint)));
        }
        ArrayList<ArrayList<Coord2DDouble>> poiPolygonBounds = blSearchPoi.poiAoiBounds;
        if (poiPolygonBounds != null && !poiPolygonBounds.isEmpty()) {
            ArrayList<ArrayList<GeoPoint>> poiPolygonBoundList = SearchResultUtils.coordList2GeoPointList(poiPolygonBounds);
            poi.setPoiPolygonBounds(poiPolygonBoundList);
        }

        ArrayList<ArrayList<Coord2DDouble>> poiRoadaoiBounds = blSearchPoi.poiRoadaoiBounds;
        if (poiRoadaoiBounds != null && !poiRoadaoiBounds.isEmpty()) {
            ArrayList<ArrayList<GeoPoint>> geoPointRoadAoiBound = new ArrayList<>();
            for (int i = 0; i < poiRoadaoiBounds.size(); i++) {
                ArrayList<GeoPoint> poiRoadaoi = new ArrayList<>();
                for (int j = 0; j < poiRoadaoiBounds.get(i).size(); j++) {
                    poiRoadaoi.add(new GeoPoint(poiRoadaoiBounds.get(i).get(j).lon, poiRoadaoiBounds.get(i).get(j).lat));
                }
                geoPointRoadAoiBound.add(poiRoadaoi);
            }
            poi.setPoiRoadaoiBounds(geoPointRoadAoiBound);
        }
        //子POI数据
        ArrayList<POI> childPoiList = new ArrayList<>();
        for (int i = 0; i < blSearchPoi.childPois.size(); i++) {
            SearchPoiBase childPoiBase = blSearchPoi.childPois.get(i);
            POI childPoi = POIFactory.createPOI();
            String childName = TextUtils.isEmpty(childPoiBase.shortname) ? childPoiBase.name : childPoiBase.shortname;
            childPoi.setId(childPoiBase.poiid);
            childPoi.setName(childName);
            childPoi.setAddr(childPoiBase.address);
            childPoi.setPoint(new GeoPoint(childPoiBase.poi_loc.lon, childPoiBase.poi_loc.lat));
            childPoi.setShortname(childPoiBase.shortname);
            childPoi.setRatio(childPoiBase.ratio);

            //入口经纬度
            ArrayList<GeoPoint> childEntranceList = new ArrayList<>();
            ArrayList<Coord2DDouble> entrancesList = childPoiBase.entrances_list;
            for (int j = 0; j < entrancesList.size(); j++) {
                Coord2DDouble coord2DDouble = entrancesList.get(j);
                childEntranceList.add(new GeoPoint(coord2DDouble.lon, coord2DDouble.lat));
            }
            if (childEntranceList.size() > 0) {
                childPoi.setEntranceList(childEntranceList);
            }
            //出口经纬度
            ArrayList<Coord2DDouble> exitList = childPoiBase.exit_list;
            ArrayList<GeoPoint> childExitList = new ArrayList<>();
            for (int j = 0; j < exitList.size(); j++) {
                Coord2DDouble coord2DDouble = exitList.get(j);
                childExitList.add(new GeoPoint(coord2DDouble.lon, coord2DDouble.lat));
            }
            if (childExitList.size() > 0) {
                childPoi.setExitList(childExitList);
            }

            childPoi.setChildType(childPoiBase.childType);
            childPoiList.add(childPoi);
        }
        poi.setChildPois(childPoiList);

        //poi的出入口数据
        ArrayList<GeoPoint> geoEntrancesList = new ArrayList<>();
        ArrayList<Coord2DDouble> entrancesList = poiBase.entrances_list;
        int entranceListSize = entrancesList == null ? 0 : entrancesList.size();

        if (entranceListSize > 0) {
            for (int i2 = 0; i2 < entranceListSize; i2++) {
                Coord2DDouble item = entrancesList.get(i2);
                GeoPoint exitEntrancePoint = new GeoPoint();
                exitEntrancePoint.setLonLat(item.lon, item.lat);
                geoEntrancesList.add(exitEntrancePoint);
            }
            poi.setEntranceList(geoEntrancesList);
        }

        ArrayList<GeoPoint> geoExitList = new ArrayList<>();
        ArrayList<Coord2DDouble> exitList = poiBase.exit_list;
        int exitListSize = exitList == null ? 0 : exitList.size();

        if (exitListSize > 0) {
            for (int i2 = 0; i2 < exitListSize; i2++) {
                Coord2DDouble item = exitList.get(i2);
                GeoPoint exitEntrancePoint = new GeoPoint();
                exitEntrancePoint.setLonLat(item.lon, item.lat);
                geoExitList.add(exitEntrancePoint);
            }
            poi.setExitList(geoExitList);
        }
        //停车场出入口
        SearchParkInfo searchParkInfo = blSearchPoi.parkInfo;
        if (searchParkInfo != null) {
            ArrayList<GeoPoint> parkInfos = new ArrayList<>();
            ArrayList<SearchParkInOutInfo> searchParkInOutInfos = searchParkInfo.inoutInfo;
            for (int i = 0; searchParkInOutInfos != null && i < searchParkInOutInfos.size(); i++) {
                parkInfos.add(new GeoPoint(searchParkInOutInfos.get(i).x, searchParkInOutInfos.get(i).y));
            }
            poi.setParkInfos(parkInfos);
        }

        return poi;
    }



    private static String getDis(HashMap<Integer, SearchCommonTemplate> tempDataMap) {
        String dis = "";
        for (Integer key : tempDataMap.keySet()) {
            SearchCommonTemplate template = tempDataMap.get(key);
            if (template == null) {
                continue;
            }
            switch (template.typeId) {
                case PoiItemlateTypeId.POI_TEMPLATE_TYPE_TEXT:
                    SearchTextTemplate textTemplate = (SearchTextTemplate) template;
                    switch (template.id) {
                        // 距离
                        case PoiTemplateConstant.LIST_DISTANCE:
                            dis = textTemplate.value;
                            break;
                        default:
                    }
                    break;
                default:
            }
        }
        return dis;
    }

    /**
     * 预搜索结果类型转换
     */
    @NonNull
    public static HmiSearchResult convertSuggestionResult(SearchSuggestResult src, @NonNull SearchRequestInfo keywordInfo) {
        HmiSearchResult hmiResult = new HmiSearchResult();
        if (src == null) {
            return hmiResult;
        }
        //poi类型：0离线数据，1在线数据
        hmiResult.responseHeader.isOnLine = src.iPoiType == 1;
        hmiResult.responseHeader.version = src.version;
        hmiResult.responseHeader.errorCode = src.code;
        hmiResult.responseHeader.errorMessage = src.message;
        hmiResult.responseHeader.timeStamp = src.timestamp;
        hmiResult.responseHeader.result = true;

        ArrayList<SearchSuggestTip> srcTipList = src.tipList;
        int size = srcTipList == null ? 0 : srcTipList.size();
        for (int i = 0; i < size; i++) {
            hmiResult.searchInfo.poiResults.add(convertSuggestionTipToPoi(srcTipList.get(i)));
        }
        hmiResult.searchInfo.poiTotalSize = size;
        hmiResult.searchInfo.poiBizType = keywordInfo.getBizType();

        return hmiResult;
    }

    /**
     * 批量转换预搜索结果点为poi列表
     */
    @Nullable
    public static List<POI> convertSuggestionTipToPoiList(List<SearchSuggestTip> src) {
        int size = src == null ? 0 : src.size();
        if (size == 0) {
            return null;
        }

        List<POI> result = new ArrayList<>();
        for (SearchSuggestTip tip : src) {
            if (tip == null) {
                Timber.i("convertSuggestionTipToPoiList: tip is null");
                continue;
            }
            //高德原文：离线预搜索的结果有个u8CateCandiFlag字段，其中1表示联想结果，如果上层不想要这个联想结果，也针对u8CateCandiFlag进行过滤，展示u8CateCandiFlag=0的非联想结果。
            //联想结果没有地址经纬度等、不需要转为POI对象在HMI显示，在此过滤掉
            if (tip.u8CateCandiFlag == 1) {
                Timber.i("convertSuggestionTipToPoiList: skip u8CateCandiFlag = 1, name = %s", tip.name);
                continue;
            }
            POI poi = convertSuggestionTipToPoi(tip);
            if (poi != null) {
                result.add(poi);
            }
        }
        return result;
    }

    /**
     * 批量转换预搜索结果点为poi列表
     */
    @Nullable
    public static List<POI> convertSuggestionPoiTipToPoiList(List<SearchSuggestionPoiTip> src) {
        Timber.i("convertSuggestionPoiTipToPoiList src = " + src);
        int size = src == null ? 0 : src.size();
        if (size == 0) {
            return null;
        }
        Timber.i("convertSuggestionPoiTipToPoiList src.size() = " + src.size());

        List<POI> result = new ArrayList<>();
        for (SearchSuggestionPoiTip tip : src) {
            POI poi = convertSuggestionTipToPoi(tip);
            if (poi != null) {
                result.add(poi);
            }
        }
        return result;
    }

    /**
     * 转换V2预搜索信息为poi对象
     */
    @Nullable
    public static POI convertSuggestionTipToPoi(SearchSuggestionPoiTip src) {
        if (src == null || src.basicInfo == null) {
            return null;
        }
        Timber.i("convertSuggestionTipToPoi src = " + GsonManager.getInstance().toJson(src));
        POI poi = POIFactory.createPOI();
        poi.setAdCode(src.basicInfo.adcode + "");
        poi.setCategory(src.basicInfo.category);
        poi.setId(src.basicInfo.poiId);
        poi.setName(src.basicInfo.name);
        poi.setHisMark(src.basicInfo.mark);

        String address = src.basicInfo.address;
        poi.setAddr(address);
        poi.setPoint(new GeoPoint(src.basicInfo.location.lon, src.basicInfo.location.lat));
        return poi;
    }

    /**
     * 转换预搜索信息为poi对象
     */
    @Nullable
    public static POI convertSuggestionTipToPoi(SearchSuggestTip src) {
        if (src == null) {
            return null;
        }

        POI poi = POIFactory.createPOI();
        poi.setAdCode(src.adcode + "");
        poi.setCategory(src.category);
        poi.setTypeCode(src.category);
        poi.setCityCode(src.citycode + "");
        poi.setId(src.poiid);
        poi.setName(src.name);
        poi.setParent(src.parent);
        poi.setChildType(src.childType);
        poi.setHistoryType(2);

        //子poi
        ArrayList<POI> childPois = new ArrayList<>();
        for (SearchSuggestPoiBase suggestPoiBase : src.childPois) {
            POI childPoi = POIFactory.createPOI();
            childPoi.setId(suggestPoiBase.poiid);
            childPoi.setName(suggestPoiBase.name);
            childPoi.setAddr(suggestPoiBase.address);
            childPoi.setPoint(new GeoPoint(suggestPoiBase.point.lon, suggestPoiBase.point.lat));
            childPoi.setShortname(suggestPoiBase.short_name);
            childPoi.setAdCode(suggestPoiBase.adcode + "");
            childPoi.setCategory(suggestPoiBase.category);
            childPoi.setTypeCode(suggestPoiBase.category);
            childPoi.setRatio(suggestPoiBase.ratio);
            childPois.add(childPoi);
        }
        poi.setChildPois(childPois);

        String address = src.address;
        if (src.ignore_district != 1) {
            address = src.district + src.address;
        }
        poi.setAddr(address);
        poi.setPoint(new GeoPoint(src.point.lon, src.point.lat));
        poi.setHisMark(src.mark);
        if (TextUtils.isEmpty(poi.getDis())) {
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
            Coord2DDouble endPoint = new Coord2DDouble(src.point.lon, src.point.lat);
            poi.setDis("" + distanceUnitTransform((long) BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint)));
        }
        return poi;
    }


    /**
     * 转换搜索历史信息为poi对象
     */
    @Nullable
    public static POI convertSearchHistoryToPoi(SearchHistoryItem src) {
        if (src == null) {
            return null;
        }

        POI poi = POIFactory.createPOI();
        poi.setName(src.name);
        poi.setHistoryType(src.history_type);
        ArrayList<HistoryRouteItem> historyRouteItems = UserTrackController.getInstance().getHistoryRoute();
        if (historyRouteItems != null && historyRouteItems.size() > 0) {
            for (HistoryRouteItem historyRouteItem : historyRouteItems) {
                if (historyRouteItem.toPoi.name.equals(src.name)) {
                    poi.setHistoryType(3);
                }
            }
        }
        String address = src.address;
        poi.setAddr(address);
        poi.setId(src.poiid);
        poi.setParent(src.parent);
        poi.setChildType(src.childType);
        poi.setDis(src.func_text);
        poi.setCategory(src.category);
        poi.setDistrict(src.district);
        poi.setPoiTag(src.poi_tag);
        poi.setAdCode(src.adcode);
        if (src.x_entr != 0 && src.y_entr != 0) {
            poi.setPoint(new GeoPoint(src.x_entr, src.y_entr));
        } else {
            poi.setPoint(new GeoPoint(src.x, src.y));
        }
        return poi;
    }

    /**
     * 转换poi对象为搜索历史信息
     */
    @Nullable
    public static SearchHistoryItem convertPoiToSearchHistoryItem(POI src, String name, int historyType) {
        if (src == null) {
            return null;
        }
        SearchHistoryItem searchHistoryItem = new SearchHistoryItem();
        searchHistoryItem.name = name;
        searchHistoryItem.history_type = historyType;
        searchHistoryItem.poiid = src.getId();
        searchHistoryItem.parent = src.getParent();
        searchHistoryItem.x = src.getPoint().getLongitude();
        searchHistoryItem.x_entr = src.getPoint().getLongitude();
        searchHistoryItem.y = src.getPoint().getLatitude();
        searchHistoryItem.y_entr = src.getPoint().getLatitude();
        searchHistoryItem.address = src.getAddr();
        searchHistoryItem.func_text = src.getDis();
        searchHistoryItem.district = src.getDistrict();
        searchHistoryItem.poi_tag = src.getPoiTag();
        searchHistoryItem.category = src.getCategory();
        searchHistoryItem.adcode = src.getAdCode();
        return searchHistoryItem;
    }

    /**
     * 子到达结果转POI
     */
    @Nullable
    public static POI convertNaviInfoToPoi(SearchNaviInfoBase searchNaviInfoBase) {
        if (searchNaviInfoBase == null) {
            return null;
        }

        POI poi = POIFactory.createPOI();
        poi.setCategory(searchNaviInfoBase.shortname);
        poi.setId(searchNaviInfoBase.poiid);
        poi.setName(searchNaviInfoBase.name);

        String address = searchNaviInfoBase.address;
        poi.setAddr(address);
        poi.setPoint(new GeoPoint(Double.parseDouble(searchNaviInfoBase.x), Double.parseDouble(searchNaviInfoBase.y)));
        return poi;
    }

    /**
     * 子到达结果转POI
     */
    @Nullable
    public static POI convertNaviInfoToPoi(SearchPoiBase searchPoiBase) {
        if (searchPoiBase == null) {
            return null;
        }

        POI poi = POIFactory.createPOI();
        poi.setCategory(searchPoiBase.shortname);
        poi.setId(searchPoiBase.poiid);
        poi.setName(searchPoiBase.name);

        String address = searchPoiBase.address;
        poi.setAddr(address);
        poi.setPoint(new GeoPoint(searchPoiBase.poi_loc.lon, searchPoiBase.poi_loc.lat));
        return poi;
    }

    /**
     * 子到达结果转POI
     *
     * @param childInfo
     * @return
     */
    public static POI convertNaviInfoToPoi(SearchPoiChildInfo childInfo) {
        if (childInfo == null) {
            return null;
        }
        POI poi = POIFactory.createPOI();
        poi.setCategory(childInfo.shortName);
        poi.setId(childInfo.poiId);
        poi.setName(childInfo.name);
        poi.setAddr(childInfo.address);
        poi.setChildType(childInfo.childType);
        poi.setPoint(new GeoPoint(childInfo.location.lon, childInfo.location.lat));
        return poi;
    }

//    public static SearchKeywordResult convertPoiToSearchKeywordResult(POI poi){
//        Coord2DDouble coord2DDouble = new Coord2DDouble(poi.getPoint().getLongitude(),poi.getPoint().getLatitude());
//        SearchKeywordResult searchKeywordResult = new SearchKeywordResult();
//        SearchPoi searchPoi = new SearchPoi();
//        searchPoi.poi.poi_loc = coord2DDouble;
//        searchPoi.poi.name = poi.getName();
//        searchKeywordResult.poiList.add(searchPoi);
//        return searchKeywordResult;
//    }

    public static KeywordSearchResultV2 convertPoiToSearchKeywordResult(POI poi) {
        Coord2DDouble coord2DDouble = new Coord2DDouble(poi.getPoint().getLongitude(), poi.getPoint().getLatitude());
        KeywordSearchResultV2 searchKeywordResult = new KeywordSearchResultV2();
        SearchPoiInfo searchPoi = new SearchPoiInfo();
        searchPoi.basicInfo.location = coord2DDouble;
        searchPoi.basicInfo.name = poi.getName();
        searchKeywordResult.poiList.add(searchPoi);
        return searchKeywordResult;
    }

    public static ArrayList<Coord2DDouble> convertGeoPointToCoord2D(ArrayList<GeoPoint> arrayList) {
        ArrayList<Coord2DDouble> result = new ArrayList<>();
        if (arrayList == null || arrayList.size() == 0) {
            return result;
        }
        for (int i = 0; i < arrayList.size(); i++) {
            GeoPoint geoPoint = arrayList.get(i);
            Coord2DDouble coord2DDouble = new Coord2DDouble();
            coord2DDouble.lat = geoPoint.getLatitude();
            coord2DDouble.lon = geoPoint.getLongitude();
            result.add(coord2DDouble);
        }
        return result;
    }


    /**
     * 子到达结果转POI
     */
    @Nullable
    public static ArrayList<SearchPoiChildInfo> convertPoiToSearchPoiBase(POI filterPoi, ArrayList<POI> pois) {
        ArrayList<SearchPoiChildInfo> searchPoiBases = new ArrayList<>();
        if (pois == null || pois.size() == 0) {
            return searchPoiBases;
        }
        for (int i = 0; i < pois.size(); i++) {
            POI poi = pois.get(i);
            if (!filterPoi.getId().equals(poi.getId())) {
                SearchPoiChildInfo childInfo = new SearchPoiChildInfo();
                childInfo.location = new Coord2DDouble(poi.getPoint().getLongitude(), poi.getPoint().getLatitude());
                childInfo.poiId = poi.getId();
                childInfo.childType = poi.getChildType();
                childInfo.shortName = poi.getName();
                searchPoiBases.add(childInfo);
            }
        }
        return searchPoiBases;
    }


    @NonNull
    public static HmiSearchResult convertNearestToHmiResult(SearchNearestResult searchNearestResult) {
        HmiSearchResult hmiResult = new HmiSearchResult();
        if (searchNearestResult == null) {
            Timber.d("convertNearestToHmiResult: = null");
            return hmiResult;
        }
        hmiResult.responseHeader = new ResponseHeaderModule();
        //poi类型：0离线数据，1在线数据
        hmiResult.responseHeader.isOnLine = searchNearestResult.iPoiType == 1;
        hmiResult.responseHeader.version = searchNearestResult.version;
        hmiResult.responseHeader.errorCode = searchNearestResult.code;
        hmiResult.responseHeader.result = true;
        //只关注第一个POI信息
        NearestPoi nearestPoi = searchNearestResult.poi_list.get(0);
        POI poi = POIFactory.createPOI();
        poi.setId(nearestPoi.poiid);
        poi.setTypeCode(nearestPoi.type);
        poi.setName(nearestPoi.name);
        poi.setCityName(searchNearestResult.city);
        poi.setCityCode("" + searchNearestResult.cityadcode);
        poi.setAddr(nearestPoi.address);
        poi.setAdCode("" + searchNearestResult.adcode);
        poi.setDistance(String.valueOf(nearestPoi.distance));
        poi.setPoint(new GeoPoint(nearestPoi.point.lon, nearestPoi.point.lat));
        hmiResult.searchInfo = new HmiSearchInfo();
        hmiResult.searchInfo.poiResults.add(poi);
        return hmiResult;
    }

    /**
     * 深度搜索转客户端结果
     */
    @NonNull
    public static HmiSearchResult convertdeepInfoToSearchResult(SearchDeepInfoResult searchDeepInfoResult) {
        HmiSearchResult hmiResult = new HmiSearchResult();
        if (searchDeepInfoResult == null) {
            Timber.d("convertNearestToHmiResult: = null");
            return hmiResult;
        }
        hmiResult.responseHeader = new ResponseHeaderModule();
        //poi类型：0离线数据，1在线数据
        hmiResult.responseHeader.isOnLine = searchDeepInfoResult.iPoiType == 1;
        hmiResult.responseHeader.version = searchDeepInfoResult.version;
        hmiResult.responseHeader.errorCode = searchDeepInfoResult.code;
        hmiResult.responseHeader.result = true;
        //只关注第一个POI信息
        DeepinfoPoi deepinfoPoi = searchDeepInfoResult.deepinfoPoi;
        POI poi = POIFactory.createPOI();
        poi.setId(deepinfoPoi.poiid);
        poi.setTypeCode("" + searchDeepInfoResult.iPoiType);
        poi.setName(deepinfoPoi.name);
        poi.setCityCode("" + deepinfoPoi.city_adcode);
        poi.setAddr(deepinfoPoi.address);
        poi.setAdCode("" + deepinfoPoi.adcode);
        poi.setPoint(new GeoPoint(deepinfoPoi.poi_loc.lon, deepinfoPoi.poi_loc.lat));
        hmiResult.searchInfo = new HmiSearchInfo();
        hmiResult.searchInfo.poiResults.add(poi);
        return hmiResult;
    }

    /**
     * 获取定位的数据，转化为搜索需求数据
     */
    public static SearchLocInfo getLocationToSearchLocInfo() {
        if (mSearchLocInfo == null) {
            mSearchLocInfo = new SearchLocInfo();
        }

        //来自定位的数据，一定不能为空
        SDKManager sdkManager = SDKManager.getInstance();
        MapDataController mapDataController = sdkManager.getMapDataController();

        // 用户(车标)位置经纬度
        Location location = sdkManager.getLocController().getLastLocation();
        int adCodeCarLoc = mapDataController.getAdcodeByLonLat(location.getLongitude(), location.getLatitude());

        // 地图中心
        Coord3DDouble mapCenterLonLat = sdkManager.getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getOperatorPosture().getMapCenter();
//        int adCodeMapCenter = mapDataController.getAdcodeByLonLat(mapCenterLonLat.lon, mapCenterLonLat.lat);

//        if ((adCodeCarLoc == adCodeMapCenter) || (adCodeMapCenter < 0)) {
        mSearchLocInfo.lon = location.getLongitude();
        mSearchLocInfo.lat = location.getLatitude();
        mSearchLocInfo.adcode = adCodeCarLoc;
//        } else {
//            mSearchLocInfo.lon = mapCenterLonLat.lon;
//            mSearchLocInfo.lat = mapCenterLonLat.lat;
//            mSearchLocInfo.adcode = mapDataController.getAdcodeByLonLat(mapCenterLonLat.lon, mapCenterLonLat.lat);
//        }
        // 获取城市adcode和城市名
        CityItemInfo cityInfo = mapDataController.getCityInfo(mSearchLocInfo.adcode);
        mSearchLocInfo.cityName = cityInfo == null ? "" : cityInfo.cityName;
        mSearchLocInfo.mapCenterLon = mapCenterLonLat.lon;
        mSearchLocInfo.mapCenterLat = mapCenterLonLat.lat;
        return mSearchLocInfo;
    }

    /**
     * 获取当前车标位置数据
     *
     * @return
     */
    public static SearchLocInfo getCurrentLocationToLocInfo() {
        if (mSearchLocInfo == null) {
            mSearchLocInfo = new SearchLocInfo();
        }

        // 用户(车标)位置经纬度
        Location location = LocationController.getInstance().getLastLocation();

        mSearchLocInfo.lon = location.getLongitude();
        mSearchLocInfo.lat = location.getLatitude();
        mSearchLocInfo.adcode = MapDataController.getInstance().getAdcodeByLonLat(location.getLongitude(), location.getLatitude());
        // 获取城市adcode和城市名
        CityItemInfo cityInfo = MapDataController.getInstance().getCityInfo(mSearchLocInfo.adcode);
        mSearchLocInfo.cityName = cityInfo == null ? "" : cityInfo.cityName;
        return mSearchLocInfo;
    }

    /**
     * 获取地图中心的城市编号和名称
     */
    public static AdCity getMapCenterAdCity() {
        AdCity adCity = new AdCity();
        //来自定位的数据，一定不能为空
        SDKManager sdkManager = SDKManager.getInstance();
        // 地图中心
        Coord3DDouble mapCenterLonLat = sdkManager.getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getOperatorPosture().getMapCenter();
        // 获取城市adcode和城市名
        MapDataController mapDataController = sdkManager.getMapDataController();
        int adCode = mapDataController.getAdcodeByLonLat(mapCenterLonLat.lon, mapCenterLonLat.lat);
        CityItemInfo cityInfo = mapDataController.getCityInfo(adCode);
        String cityName = cityInfo == null ? "" : cityInfo.cityName;
        adCity.setCityName(cityName);
        adCity.setCityAdcode(adCode);
        return adCity;
    }

    /**
     * 根据经纬度获取城市编码
     */
    public static int getCityCode(double lon, double lat) {
        //来自定位的数据，一定不能为空
        // 获取城市adcode和城市名
        SDKManager sdkManager = SDKManager.getInstance();
        MapDataController mapDataController = sdkManager.getMapDataController();
        return mapDataController.getAdcodeByLonLat(lon, lat);
    }

    /**
     * 根据经纬度获取城市名称
     */
    public static String getCityName(int adCode) {
        //来自定位的数据，一定不能为空
        // 获取城市adcode和城市名
        SDKManager sdkManager = SDKManager.getInstance();
        MapDataController mapDataController = sdkManager.getMapDataController();
        CityItemInfo cityInfo = mapDataController.getCityInfo(adCode);
        String cityName = cityInfo == null ? "" : cityInfo.cityName;
        return cityName;
    }


    /**
     * 转换沿途搜结果
     */
    @Nullable
    public static HmiSearchResult convertAlongWayToHmiResult(SearchAlongWayResult alongWayResult) {
        if (alongWayResult == null) {
            Timber.d("convertAlongWayToHmiResult: = null");
            return null;
        }
        HmiSearchResult hmiResult = new HmiSearchResult();
        hmiResult.responseHeader = new ResponseHeaderModule();
        //poi类型：0离线数据，1在线数据
        hmiResult.responseHeader.isOnLine = alongWayResult.iPoiType == 1;
        hmiResult.responseHeader.version = alongWayResult.version;
        hmiResult.responseHeader.errorCode = alongWayResult.code;
        ArrayList<AlongWayPoi> pois = alongWayResult.pois;
        hmiResult.searchInfo = new HmiSearchInfo();
        // TODO: 2020/8/17 填满所有 POIBase 字段
        for (int i = 0; i < pois.size(); i++) {
            AlongWayPoi alongWayPoi = pois.get(i);
            POI poi = convertAlongWayPoiToPoi(alongWayPoi);
            hmiResult.searchInfo.poiResults.add(poi);
        }
        hmiResult.searchInfo.poiTotalSize = alongWayResult.total;
        return hmiResult;
    }

    private static POI convertAlongWayPoiToPoi(AlongWayPoi alongWayPoi) {
        AlongWaySearchPoi alongWaySearchPoi = new AlongWaySearchPoi();
        alongWaySearchPoi.setPoint(new GeoPoint(alongWayPoi.point.lon, alongWayPoi.point.lat));
        //导航入口坐标
        ArrayList<GeoPoint> entranceList = new ArrayList<>();
        entranceList.add(new GeoPoint(alongWayPoi.pointEnter.lon, alongWayPoi.pointEnter.lat));
        alongWaySearchPoi.setEntranceList(entranceList);
        //导航出口坐标
        ArrayList<GeoPoint> exitList = new ArrayList<>();
        exitList.add(new GeoPoint(alongWayPoi.pointExit.lon, alongWayPoi.pointExit.lat));
        alongWaySearchPoi.setExitList(exitList);

        alongWaySearchPoi.setName(alongWayPoi.name);
        alongWaySearchPoi.setAdCode("" + alongWayPoi.nCityAdCode);
        alongWaySearchPoi.setAddr(alongWayPoi.address);
        // 达沿途搜点的路线距离，单位:米
        alongWaySearchPoi.setDistance(String.valueOf(alongWayPoi.dist_to_via));
        alongWaySearchPoi.setId(alongWayPoi.id);
        alongWaySearchPoi.setTypeCode(alongWayPoi.typecode);
        alongWaySearchPoi.setLabelType(alongWayPoi.label_type);
        alongWaySearchPoi.setTravelTime(alongWayPoi.travel_time);
        alongWaySearchPoi.setTypeCode(alongWayPoi.typecode);
        alongWaySearchPoi.setPriceInfos(alongWayPoi.pricelist);
        alongWaySearchPoi.setDistToVia(alongWayPoi.dist_to_via);
        alongWaySearchPoi.setEtaToVia(alongWayPoi.eta_to_via);
//        alongWaySearchPoi.setVehiclechargeleft(alongWayPoi.vehiclechargeleft);
        alongWaySearchPoi.setToll(alongWayPoi.toll);
        alongWaySearchPoi.setViaLevel(alongWayPoi.via_level);
        alongWaySearchPoi.setBrandDesc(alongWayPoi.brand_desc);
        return alongWaySearchPoi;
    }

    /**
     * 将所给的point列表,按照 lon;lat;lon;lat (最后不以分号结尾)的形式拼接成字符串, 默认拼接连接符为分号
     *
     * @param flag 拼接符,若传空,则使用默认的分号
     */
    @NonNull
    public static String convertPointList2String(@Nullable List<GeoPoint> pointList, @Nullable String flag) {
        int size = pointList == null ? 0 : pointList.size();
        if (size == 0) {
            return "";
        }
        if (null == flag || flag.isEmpty()) {
            flag = ";";
        }

        StringBuilder sb = new StringBuilder(100);
        for (int i = 0; i < size; i++) {
            GeoPoint geoPoint = pointList.get(i);
            if (geoPoint == null) {
                continue;
            }

            sb.append(geoPoint.getLongitude()).append(flag).append(geoPoint.getLatitude());
            if (i != size - 1) {
                sb.append(flag);
            }
        }
        return sb.toString();
    }

    /**
     * POI列表转化为搜索POI父点图层业务所需数据
     */
    public static ArrayList<BizSearchParentPoint> getBizSearchParentPointList(List<POI> list) {
        ArrayList<BizSearchParentPoint> bizSearchParentPointArrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            POI poi = list.get(i);
            BizSearchParentPoint point = convertPoi2SearchParentPoint(poi);
            point.index = i + 1;
            if (list.get(i).getCategory().startsWith("05") && !ListUtil.isEmpty(list.get(i).getSearchProductInfoList())) {
                point.poiType = PoiParentType.PoiParentTypeDeliciousFood;
            }
            bizSearchParentPointArrayList.add(point);
        }
        return bizSearchParentPointArrayList;
    }

    @SuppressLint("WrongConstant")
    public static ArrayList<BizSearchAlongWayPoint> getBizSearchAlongPoints(List<POI> list, boolean isNaving) {
        ArrayList<BizSearchAlongWayPoint> pointList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            AlongWaySearchPoi poi = (AlongWaySearchPoi) list.get(i);
            BizSearchAlongWayPoint bizPoint = new BizSearchAlongWayPoint();
            bizPoint.name = poi.getName();
            bizPoint.mPos3D = new Coord3DDouble(poi.getPoint().getLongitude(), poi.getPoint().getLatitude(), 0);
            Timber.d("=======add getBizSearchAlongPoints  = id" + poi.getId());
            bizPoint.id = String.valueOf(i);
            String typeCode = poi.getTypeCode();
            String[] typeCodes = typeCode.split("\\|");
            if(typeCodes != null && typeCodes.length > 0) {
                typeCode = typeCodes[0];
            }
            bizPoint.typeCode = StringUtils.str2Int(typeCode, 10, 0);
            bizPoint.labelName = getAlongBizLabelName(poi.getSearchLabelInfos());
            bizPoint.searchType = getAlongBizSearchType(poi.getTypeCode());
            if (bizPoint.searchType == SearchAlongwayType.SearchAlongwayTypeCharge) {
                if ("国家电网".equals(poi.getBrandDesc()) || "Stategrid".equals(poi.getBrandDesc())) {
                    bizPoint.typeCode = ChargeStationType.ChargeStationTypeGuoJiaDianWang;
                } else if ("特来电".equals(poi.getBrandDesc()) || "Teld".equals(poi.getBrandDesc())) {
                    bizPoint.typeCode = ChargeStationType.ChargeStationTypeTeLaiDian;
                } else if ("星星充电".equals(poi.getBrandDesc()) || "Star Charge".equals(poi.getBrandDesc())) {
                    bizPoint.typeCode = ChargeStationType.ChargeStationTypeXingXing;
                } else if ("普天新能源".equals(poi.getBrandDesc()) || "Potevio".equals(poi.getBrandDesc())) {
                    bizPoint.typeCode =ChargeStationType.ChargeStationTypePuTian;
                } else {
                    bizPoint.typeCode = 0;
                }
            }
            if(poi.getSearchDriveInfo()!=null){
                bizPoint.travelTime = Math.max(poi.getSearchDriveInfo().toViaTime, 0);
                bizPoint.travelTime = Math.max(poi.getSearchDriveInfo().toViaTime, 0);
                bizPoint.mExtraData.extraTime = poi.getSearchDriveInfo().throughViaCostTime;
                bizPoint.mExtraData.extraDistance = poi.getSearchDriveInfo().throughViaCostDistance;
            }
            if (isNaving) {
                //导航中
                bizPoint.mExtraData.alongRouteMode = AlongRouteMode.AlongRouteModeGuide;
            } else{
                //路线规划
                bizPoint.mExtraData.alongRouteMode = AlongRouteMode.AlongRouteModeRoute;
            }
            bizPoint.mExtraData.isOnlineSearch = true;
            if(poi.getChargeStationInfo()!=null){
                bizPoint.mExtraData.chargeStationInfo.fastTotal = StringUtils.str2Int(poi.getChargeStationInfo().fast_total, 10, 0);
                bizPoint.mExtraData.chargeStationInfo.fastFree = StringUtils.str2Int(poi.getChargeStationInfo().fast_free, 10, 0);
                bizPoint.mExtraData.chargeStationInfo.slowTotal = StringUtils.str2Int(poi.getChargeStationInfo().slow_total, 10, 0);
                bizPoint.mExtraData.chargeStationInfo.slowFree = StringUtils.str2Int(poi.getChargeStationInfo().slow_free, 10, 0);
            }

            pointList.add(bizPoint);
        }
        return pointList;
    }

    private static String getAlongBizLabelName(ArrayList<SearchLabelInfo> labelInfoList) {
        String label = SearchDataConvertUtils.getLabelFromEnRouteResult(labelInfoList, 1);
        if (TextUtils.isEmpty(label)) {
            label = SearchDataConvertUtils.getLabelFromEnRouteResult(labelInfoList, 2);
        }
        return label;
    }

    private static int getAlongBizSearchType(String typeCode) {
        int splitIndex = typeCode.indexOf("|");
        int type = SearchAlongwayType.SearchAlongwayTypeNone;
        if (splitIndex > 0) {
            type = getAlongBizSearchTypeSingle(typeCode.substring(0, splitIndex));
            String subStr = typeCode.substring(splitIndex + 1);
            while (type == SearchAlongwayType.SearchAlongwayTypeNone) {
                splitIndex = subStr.indexOf("|");
                if (splitIndex > 0) {
                    type = getAlongBizSearchTypeSingle(subStr.substring(0, splitIndex));
                    subStr = subStr.substring(splitIndex + 1);
                } else {
                    type = getAlongBizSearchTypeSingle(subStr);
                    break;
                }
            }
        } else {
            type = getAlongBizSearchTypeSingle(typeCode);
        }
        return type;
    }

    private static int getAlongBizSearchTypeSingle(String typeCode) {
        int iTypeCode = StringUtils.str2Int(typeCode, 10, 0);
        if (InformationTypeUtil.isGasStationPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeGas;
        } else if (InformationTypeUtil.isChargeStationPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeCharge;
        } else if (InformationTypeUtil.isFoodPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeFood;
        } else if (InformationTypeUtil.isHotelPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeHotel;
        } else if (InformationTypeUtil.isScenicPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeScenicSpot;
        } else if (InformationTypeUtil.isCarRepairPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeMaintenance;
        } else if (InformationTypeUtil.isWashroomPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeToilet;
        } else if (InformationTypeUtil.isCngStationPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeCng;
        } else if (InformationTypeUtil.isCarWashingPoi(iTypeCode)) {
            return SearchAlongwayType.SearchAlongwayTypeCarWash;
        }
        return SearchAlongwayType.SearchAlongwayTypeNone;
    }


    /**
     * POI列表转化为搜索POI孙子节点图层业务所需数据
     */
    public static BizSearchChildPoint getBizSearchPoiLable(POI poi) {
        BizSearchChildPoint point = convertPoi2BizSearchPoiLable(poi);
        return point;
    }

    /**
     * POI转父节点对象
     *
     * @param poi
     * @return
     */
    public static BizSearchParentPoint convertPoi2SearchParentPoint(@NonNull POI poi) {
        BizSearchParentPoint point = new BizSearchParentPoint();
        GeoPoint geoPoint = poi.getPoint();
        point.id = poi.getId();
        if (!TextUtils.isEmpty(poi.getTypeCode())) {
            String[] typeCodes = poi.getTypeCode().split("\\|");
            boolean isGas = false;
            for (int i = 0; i < typeCodes.length; i++) {
                if (typeCodes[i].startsWith("0101")) {
                    isGas = true;
                }
                point.typeCode = point.typeCode | Integer.parseInt(typeCodes[i]);
            }

            if (isGas) {
                point.poiType = PoiParentType.PoiParentTypeGas;
            }
        }

        point.poiName = poi.getName();
        point.mTypeCode = poi.getTypeCode();
        if (geoPoint != null) {
            point.mPos3D.lon = geoPoint.getLongitude();
            point.mPos3D.lat = geoPoint.getLatitude();
        }
        return point;
    }

    /**
     * POI转子节点对象List
     */
    public static ArrayList<BizSearchChildPoint> convertPoi2BizSearchChildPoints(@Nullable POI poi) {
        ArrayList<BizSearchChildPoint> searchChildPointList = new ArrayList<>();
        if (poi == null || poi.getChildPois() == null) {
            return searchChildPointList;
        }

        for (int i = 0; i < poi.getChildPois().size(); i++) {
            POI childPoi = poi.getChildPois().get(i);
            BizSearchChildPoint point = new BizSearchChildPoint();
            GeoPoint geoPoint = childPoi.getPoint();
            point.id = childPoi.getId();
            point.mTypeCode = childPoi.getTypeCode();
            point.mPos3D.lon = geoPoint.getLongitude();
            point.mPos3D.lat = geoPoint.getLatitude();
            point.childType = childPoi.getChildType();
            point.shortName = childPoi.getShortname();
            searchChildPointList.add(point);
        }
        return searchChildPointList;
    }

    /**
     * POI转孙子节点对象
     */
    public static BizSearchChildPoint convertPoi2BizSearchPoiLable(@Nullable POI poi) {
        BizSearchChildPoint bizSearchChildPoint = new BizSearchChildPoint();
        if (poi == null) {
            return bizSearchChildPoint;
        }

        GeoPoint geoPoint = poi.getPoint();
        bizSearchChildPoint.id = poi.getId();
        bizSearchChildPoint.mTypeCode = poi.getTypeCode();
        bizSearchChildPoint.mPos3D.lon = geoPoint.getLongitude();
        bizSearchChildPoint.mPos3D.lat = geoPoint.getLatitude();
        return bizSearchChildPoint;
    }
//    /**
//     * POI转子节点对象
//     *
//     * @param poi
//     * @return
//     */
//    public static BizSearchChildPoint convertPoi2BizSearchChildPoint(@NonNull POI poi) {
//        BizSearchChildPoint point = new BizSearchChildPoint();
//        GeoPoint geoPoint = poi.getPoint();
//        point.id = poi.getId();
//        point.mTypeCode = poi.getType();
//        point.mPos3D.lon = geoPoint.getLongitude();
//        point.mPos3D.lat = geoPoint.getLatitude();
//        point.childType = poi.getChildType();
//        return point;
//    }

    /**
     * 获取附近城市数据
     *
     * @param adCity
     * @return
     */
    public static ArrayList<AdCity> getNearCityList(AdCity adCity) {
        ArrayList<AdCity> adCities = new ArrayList<>();
        Area area = SDKManager.getInstance().getMapDataController().getMapDataService()
                .getArea(DownLoadMode.DOWNLOAD_MODE_NET, adCity.getCityAdcode());
        if (area.vecNearAdcodeList != null) {
            for (int i = 0; i < area.vecNearAdcodeList.size(); i++) {
                AdCity nearCity = new AdCity();
                nearCity.setCityAdcode(area.vecNearAdcodeList.get(i));
                nearCity.setCityName(SDKManager.getInstance().getMapDataController().getMapDataService()
                        //获取城市名称
                        .getCityInfo(area.vecNearAdcodeList.get(i)).cityName);
                adCities.add(nearCity);
            }
        }
        return adCities;
    }

    /**
     * 小数点后两位四舍五入
     *
     * @param value
     * @return
     */
    public static double formatDouble2(double value, int newScale) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();

    }

    public static ArrayList<POI> convertAlongWayPoisToPoiList(ArrayList<AlongWayPoi> alongWayPois) {
        ArrayList<POI> pois = null;
        if (alongWayPois != null && alongWayPois.size() > 0) {
            pois = new ArrayList<>();
            for (AlongWayPoi alongWayPoi : alongWayPois) {
                AlongWaySearchPoi poi = new AlongWaySearchPoi();
                poi.setPoint(new GeoPoint(alongWayPoi.point.lon, alongWayPoi.point.lat));
                //导航入口坐标
                ArrayList<GeoPoint> entranceList = new ArrayList<>();
                entranceList.add(new GeoPoint(alongWayPoi.pointEnter.lon, alongWayPoi.pointEnter.lat));
                poi.setEntranceList(entranceList);
                //导航出口坐标
                ArrayList<GeoPoint> exitList = new ArrayList<>();
                exitList.add(new GeoPoint(alongWayPoi.pointExit.lon, alongWayPoi.pointExit.lat));
                poi.setExitList(exitList);

                poi.setName(alongWayPoi.name);
                poi.setAdCode("" + alongWayPoi.nCityAdCode);
                poi.setAddr(alongWayPoi.address);
                // 达沿途搜点的路线距离，单位:米
                poi.setDistance(String.valueOf(alongWayPoi.dist_to_via));
                if (TextUtils.isEmpty(poi.getDis())) {
                    Location location = SDKManager.getInstance().getLocController().getLastLocation();
                    Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
                    Coord2DDouble endPoint = new Coord2DDouble(alongWayPoi.point.lon, alongWayPoi.point.lat);
                    poi.setDis("" + distanceUnitTransform((long) BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint)));
                }
                poi.setId(alongWayPoi.id);
                poi.setLabelType(alongWayPoi.label_type);
                poi.setTypeCode(alongWayPoi.typecode);
                poi.setBrandDesc(alongWayPoi.brand_desc);
                poi.setChargeStationInfo(alongWayPoi.chargingStationInfo);
                SearchPoiGasInfo searchPoiGasInfo = new SearchPoiGasInfo();
                ArrayList<PricePoiInfo> pricePoiInfoArrayList = alongWayPoi.pricelist;
                //构造成搜索v2服务的加油站结果
                for (int i = 0; pricePoiInfoArrayList != null && i < pricePoiInfoArrayList.size(); i++) {
                    searchPoiGasInfo.priceList.add(pricePoiInfoArrayList.get(i).price);
                    searchPoiGasInfo.typeList.add(pricePoiInfoArrayList.get(i).type);
                }
                poi.setGasInfo(searchPoiGasInfo);
                poi.setAlongWaySearch(true);
                pois.add(poi);
            }
        }
        return pois;
    }

    /**
     * SearchLineDeepInfoResult转POI
     *
     * @param restAreaInfos
     * @param data
     * @return
     */
    public static ArrayList<POI> convertAlongWayDeepPoiToPoi(ArrayList<RestAreaInfo> restAreaInfos, SearchLineDeepInfoResult data) {
        ArrayList<LinePoiBase> datas = data.data;
        ArrayList<POI> pois = null;
        if (datas != null && datas.size() > 0) {
            pois = new ArrayList<>();
            for (int i = 0; i < datas.size(); i++) {
                AlongWayPoiDeepInfo alongWayPoiDeepInfo = new AlongWayPoiDeepInfo();
                LinePoiServiceAreaInfo linePoiDeepInfo = (LinePoiServiceAreaInfo) datas.get(i);
                RestAreaInfo restAreaInfo = restAreaInfos.get(i);
                alongWayPoiDeepInfo.setPoint(new GeoPoint(restAreaInfo.pos.lon, restAreaInfo.pos.lat));
                alongWayPoiDeepInfo.setId(restAreaInfo.servicePOIID);
                alongWayPoiDeepInfo.setDistance(String.valueOf(restAreaInfo.remainDist));
                alongWayPoiDeepInfo.setTravelTime(String.valueOf(restAreaInfo.remainTime));
                alongWayPoiDeepInfo.setName(restAreaInfo.serviceName);

                alongWayPoiDeepInfo.setAddr(linePoiDeepInfo.address);
                if (TextUtils.isEmpty(alongWayPoiDeepInfo.getDis())) {
                    alongWayPoiDeepInfo.setDis("" + distanceUnitTransform(restAreaInfo.remainDist));
                }
                alongWayPoiDeepInfo.setBuilding(linePoiDeepInfo.building);
                alongWayPoiDeepInfo.setServiceStar(linePoiDeepInfo.serviceStar);
                alongWayPoiDeepInfo.setBrand(linePoiDeepInfo.brand);

                alongWayPoiDeepInfo.setTypeCode(linePoiDeepInfo.typecode);
                alongWayPoiDeepInfo.setLineChildPois(linePoiDeepInfo.children);
                alongWayPoiDeepInfo.setAlongWaySearch(true);

                pois.add(alongWayPoiDeepInfo);
            }
        }
        return pois;
    }

    /**
     * 将路线的RestAreaInfo转换为图层需要的BizRouteRestAreaInfo
     *
     * @param restAreaInfos
     * @return
     */
    public static ArrayList<BizRouteRestAreaInfo> getBizAlongWayAreaInfo(ArrayList<RestAreaInfo> restAreaInfos) {
        if (restAreaInfos == null || restAreaInfos.size() == 0) {
            return null;
        }
        ArrayList<BizRouteRestAreaInfo> bizRouteRestAreaInfos = new ArrayList<>();
        for (int i = 0; i < restAreaInfos.size(); i++) {
            BizRouteRestAreaInfo bizRouteRestAreaInfo = new BizRouteRestAreaInfo();
            bizRouteRestAreaInfo.id = TextUtils.isEmpty(restAreaInfos.get(i).servicePOIID) ? restAreaInfos.get(i).serviceName.hashCode() + "" : restAreaInfos.get(i).servicePOIID;
            if (TextUtils.isEmpty(bizRouteRestAreaInfo.id)) {
                continue;
            }
            bizRouteRestAreaInfo.restAreaLabelInfo = restAreaInfos.get(i);
            bizRouteRestAreaInfos.add(bizRouteRestAreaInfo);
        }
        return bizRouteRestAreaInfos;
    }

    /**
     * poi详情数据转换为POI
     *
     * @param data
     * @param poi
     * @return
     */
    public static POI convertPoiDetailToPoi(PoiDetailSearchResult data, POI poi) {
        poi.setFoodCategory(data.baseInfo.poiInfo.categoryList);
        poi.setGalleryInfo(data.baseInfo.galleryInfo);
        poi.setRating(data.baseInfo.poiInfo.poiInfoBase.rating);
        poi.setAverageCost(data.baseInfo.poiInfo.poiInfoBase.averageCost);
        poi.setPhone(data.baseInfo.poiInfo.poiInfoBase.tel);
        poi.setDeepinfo(data.baseInfo.poiInfo.poiInfoBase.openTime);
        poi.setRankBarInfo(data.baseInfo.rankBarInfo);
        if (data.voucherList != null && data.voucherList.size() > 0) {
            List<PoiDetailProductBean> list = new ArrayList<>();
            for (PoiDetailShelfInfo info : data.voucherList) {
                for (PoiDetailProductInfo result : info.productInfoList) {
                    PoiDetailProductBean bean = new PoiDetailProductBean();
                    bean.imgUrl = result.mediaInfo.imageUrlList.get(0);
                    bean.currentPrice = result.currentPrice;
                    bean.spuName = result.spuName;
                    bean.originalPrice = result.originalPrice;
                    bean.discountRate = result.discountRate;
                    bean.tagInfo = result.tagInfo;
                    bean.qrCode = result.qrCode;
                    bean.spuId = result.spuId;
                    bean.skuId = result.skuId;
                    list.add(bean);
                    poi.setVoucherList(list);
                }
            }
        }
        return poi;
    }

    /**
     * poi详情数据转换成团购信息
     *
     * @param data
     * @return
     */
    public static List<PoiDetailProductBean> converPoiDetailToProductInfoList(PoiDetailSearchResult data) {
        List<PoiDetailProductBean> list = new ArrayList<>();
        if (data.groupBuyInfoList != null && data.groupBuyInfoList.size() > 0) {
            for (PoiDetailShelfInfo info : data.groupBuyInfoList) {
                for (PoiDetailProductInfo result : info.productInfoList) {
                    PoiDetailProductBean bean = new PoiDetailProductBean();
                    bean.imgUrl = result.mediaInfo.imageUrlList.get(0);
                    bean.currentPrice = result.currentPrice;
                    bean.spuName = result.spuName;
                    bean.originalPrice = result.originalPrice;
                    bean.discountRate = result.discountRate;
                    bean.tagInfo = result.tagInfo;
                    bean.qrCode = result.qrCode;
                    bean.spuId = result.spuId;
                    bean.skuId = result.skuId;
                    list.add(bean);
                }
            }
        }
        return list;
    }

    /**
     * poi详情数据转换成团购信息
     *
     * @return
     */
    public static List<SearchProductInfo> converPoiDetailToProductInfoListToSearchProductInfo(List<PoiDetailProductBean> poiDetailProductBeans) {
        List<SearchProductInfo> list = new ArrayList<>();
        if (poiDetailProductBeans != null && poiDetailProductBeans.size() > 0) {
            for (PoiDetailProductBean info : poiDetailProductBeans) {
                SearchProductInfo searchProductInfo = new SearchProductInfo();
                searchProductInfo.currentPrice = info.currentPrice;
                searchProductInfo.discountRate = info.discountRate;
                searchProductInfo.originalId = info.originalId;
                searchProductInfo.originalPrice = info.originalPrice;
                searchProductInfo.qrCode = info.qrCode;
                searchProductInfo.skuId = info.skuId;
                searchProductInfo.spuId = info.spuId;
                searchProductInfo.spuName = info.spuName;
                searchProductInfo.tagInfo = info.tagInfo;
                searchProductInfo.type = info.type;
                list.add(searchProductInfo);
            }
        }
        return list;
    }

    /**
     * 附近推荐数据转为POI
     *
     * @param info
     * @return
     */
    public static POI convertNearbyRecommendToPoi(NearbyRecommendPoiInfo info) {
        POI poi = POIFactory.createPOI();
        poi.setName(info.name);
        poi.setIndustry(info.retainParam.bizType);
        poi.setId(info.poiId);
        poi.setPoint(new GeoPoint(info.location.lon, info.location.lat));
        poi.setAdCode(info.adcode + "");
        poi.setTypeCode(info.typeCode);
        poi.setAddr(info.address);
        List<SearchProductInfo> productInfos = new ArrayList<>();
        for (SearchProductInfoBase productInfoBase : info.productList) {
            SearchProductInfo productInfo = new SearchProductInfo();
            productInfo.currentPrice = productInfoBase.currentPrice;
            productInfo.originalPrice = productInfoBase.originalPrice;
            productInfo.discountRate = productInfoBase.discountRate;
            productInfo.spuName = productInfoBase.spuName;
            productInfo.spuId = productInfoBase.spuId;
            productInfo.type = productInfoBase.type;
            productInfo.qrCode = productInfoBase.qrCode;
            productInfo.tagInfo = productInfoBase.tagInfo;
            productInfo.skuId = productInfoBase.skuId;
            productInfo.originalId = productInfoBase.originalId;
            productInfos.add(productInfo);
        }
        poi.setSearchProductInfoList(productInfos);
        return poi;
    }

    /**
     * 高德指南榜单页数据转为POI
     *
     * @param info
     * @return
     */
    public static POI convertRecommendToPoi(WsShieldSearchRanklistPortalRecommendData info) {
        POI poi = POIFactory.createPOI();
        poi.setName(info.data.name);
        poi.setId(info.data.poiid);
        if (!TextUtils.isEmpty(info.data.lon) && !TextUtils.isEmpty(info.data.lat)) {
            poi.setPoint(new GeoPoint(Double.parseDouble(info.data.lon), Double.parseDouble(info.data.lat)));
        }
        poi.setTypeCode(info.data.typecode);
        return poi;
    }

    /**
     * 高德指南落地页数据转为POI
     *
     * @param info
     * @return
     */
    public static POI convertSubRecommendToPoi(WsShieldSearchRanklist_landingRecommendData info) {
        POI poi = POIFactory.createPOI();
        poi.setName(info.data.name);
        poi.setId(info.data.poiid);
        if (!TextUtils.isEmpty(info.data.lon) && !TextUtils.isEmpty(info.data.lat)) {
            poi.setPoint(new GeoPoint(Double.parseDouble(info.data.lon), Double.parseDouble(info.data.lat)));
        }
        poi.setTypeCode(info.data.typecode);
        return poi;
    }

    /**
     * 关键字顺路搜索结果转化
     *
     * @param result
     * @return
     */
    public static List<POI> convertSearchEnrouteResultToPoiList(SearchEnrouteResult result) {
        List<POI> list = new ArrayList<>();
        if (result == null) {
            return list;
        }
        for (SearchEnroutePoiInfo poiInfo : result.poiInfos) {
            if (poiInfo.basicInfo == null) {
                continue;
            }
            SearchPoiBasicInfo basicInfo = poiInfo.basicInfo;
            AlongWaySearchPoi poi = new AlongWaySearchPoi();
            convertSearchPoiBasicInfoToPoi(poi, basicInfo);
            poi.setPoiRankInfo(poiInfo.rankInfo);
            poi.setSearchDriveInfo(poiInfo.driveInfo);
            poi.setSearchLabelInfos(poiInfo.labelInfo);
            poi.setBrandDesc(basicInfo.brand);
            if (poiInfo.driveInfo != null) {
                //经过途经点的绕行时间
                int throughViaCostTime = poiInfo.driveInfo.throughViaCostTime;
                if (throughViaCostTime != 0x80000000) { //过滤无效值
                    if (throughViaCostTime >= 0) {
                        poi.setAlongSearchTravelTime("多" + switchFromSecond(throughViaCostTime));
                    } else {
                        poi.setAlongSearchTravelTime("少" + switchFromSecond(Math.abs(throughViaCostTime)));
                    }
                }else{
                    poi.setAlongSearchTravelTime("多0分钟");
                }
                //经过途经点的绕行距离
                int throughViaCostDistance = poiInfo.driveInfo.throughViaCostDistance;
                if (throughViaCostDistance != 0x80000000) { //过滤无效值
                    if (throughViaCostDistance >= 0) {
                        poi.setAlongSearchDistance("多" + distanceUnitTransform(throughViaCostDistance));
                    } else {
                        poi.setAlongSearchDistance("少" + distanceUnitTransform(Math.abs(throughViaCostDistance)));
                    }
                }else{
                    poi.setAlongSearchDistance("多0米");
                }

            }

            List<SearchProductInfo> productInfos = new ArrayList<>();
            for (SearchProductInfoBase productInfoBase : poiInfo.productInfos) {
                SearchProductInfo productInfo = new SearchProductInfo();
                productInfo.currentPrice = productInfoBase.currentPrice;
                productInfo.originalPrice = productInfoBase.originalPrice;
                productInfo.discountRate = productInfoBase.discountRate;
                productInfo.spuName = productInfoBase.spuName;
                productInfo.spuId = productInfoBase.spuId;
                productInfo.type = productInfoBase.type;
                productInfo.qrCode = productInfoBase.qrCode;
                productInfo.tagInfo = productInfoBase.tagInfo;
                productInfo.skuId = productInfoBase.skuId;
                productInfo.originalId = productInfoBase.originalId;
                productInfos.add(productInfo);
            }
            poi.setSearchProductInfoList(productInfos);
            poi.setChargeStationInfo(poiInfo.chargingStationInfo);
            poi.setSearchPoiGasStationInfo(poi.getSearchPoiGasStationInfo());
            poi.setPathRestorationInfo(poiInfo.pathRestorationInfo);
            list.add(poi);
        }
        return list;
    }

    /**
     * 根据优先级从顺路搜数据中获取标签
     * @param labelInfos    顺路搜标签数据
     * @param type          根据优先级获取第几类标签, 1: 最顺路、最近、熟悉、近终点、热门 2:服务区、下高速
     */
    public static String getLabelFromEnRouteResult(List<SearchLabelInfo> labelInfos, int type) {
        if (labelInfos == null || labelInfos.isEmpty()) return null;

        int priority = 0;
        String label = null;
        for (SearchLabelInfo info : labelInfos) {
            if (info.type != SearchLabelType.Drive || info.content.isEmpty()) continue;
            if (mLabelPriority.containsKey(info.content)) {
                int curPriority = mLabelPriority.get(info.content);
                // 50~100区间的标签
                if (type == 1 && curPriority > 50 && curPriority <= 100) {
                    if (curPriority > priority) {
                        label = info.content;
                        priority = curPriority;
                    }
                } else if (type == 2 && curPriority <= 50) { // 50以下区间的标签
                    if (curPriority > priority) {
                        label = info.content;
                        priority = curPriority;
                    }
                }
            }
        }
        return label;
    }

    /**
     * 过滤掉筛选中的区域和地铁
     *
     * @param searchClassifyInfo
     */
    public static void getClassifyFilter(SearchClassifyInfo searchClassifyInfo) {
        if (searchClassifyInfo == null
                || searchClassifyInfo.classifyItemInfo == null
                || searchClassifyInfo.classifyItemInfo.categoryInfoList == null
                || searchClassifyInfo.classifyItemInfo.categoryInfoList.isEmpty()) {
            return ;
        }
        //处理搜索结果筛选器内容
        for (SearchCategoryInfo categoryInfo : searchClassifyInfo.classifyItemInfo.categoryInfoList) {
            if (categoryInfo.childCategoryInfo == null || categoryInfo.childCategoryInfo.isEmpty()) {
                continue;
            }
            //去掉 充电站-推荐电站-空闲较多（>3个） 中的（>3个）文案。太长显示不完全
            if (categoryInfo.baseInfo.name.startsWith("空闲较多")) {
                categoryInfo.baseInfo.name = "空闲较多";
            }
            List<SearchChildCategoryInfo> removeList = new ArrayList<>();
            for (SearchChildCategoryInfo childCategoryInfo : categoryInfo.childCategoryInfo) {
                if (TextUtils.isEmpty(childCategoryInfo.baseInfo.value)) {
                    if (childCategoryInfo.childCategoryInfoList == null || childCategoryInfo.childCategoryInfoList.isEmpty()) {
                        //去掉 位置区域-区域、地铁等 筛选项。
                        removeList.add(childCategoryInfo);
                    } else {
                        boolean isGrandChildValueEmpty = true;
                        for (SearchChildCategoryInfo grandChild : childCategoryInfo.childCategoryInfoList) {
                            if (!TextUtils.isEmpty(grandChild.baseInfo.value)) {
                                isGrandChildValueEmpty = false;
                                break;
                            }
                        }
                        if (isGrandChildValueEmpty) {
                            //去掉 酒店-更多筛选-品牌等 筛选项。
                            removeList.add(childCategoryInfo);
                        }
                    }
                }
                //去掉 酒店-星级价格-星级（可多选） 中的可多选文案。目前暂不支持多选。
                if ("星级(可多选)".equals(childCategoryInfo.baseInfo.name)) {
                    childCategoryInfo.baseInfo.name = "星级";
                }
                if (childCategoryInfo.childCategoryInfoList!=null) {
                    for (SearchChildCategoryInfo info : childCategoryInfo.childCategoryInfoList) {
                        //去掉 充电站-推荐电站-空闲较多（>3个） 中的（>3个）文案。太长显示不完全
                        if (info.baseInfo.name.startsWith("空闲较多")) {
                            info.baseInfo.name = "空闲较多";
                        }
                    }
                }
            }

            for (SearchChildCategoryInfo childCategoryInfo : removeList) {
                categoryInfo.childCategoryInfo.remove(childCategoryInfo);
            }
        }
    }

    public static String getClassifyValue(SearchClassifyInfo searchClassifyInfo, int selectFilterIndex, String value) {
        StringBuilder param = new StringBuilder();
        List<SearchCategoryInfo> categoryInfoList = searchClassifyInfo.classifyItemInfo.categoryInfoList;
        for (int i = 0; i < categoryInfoList.size(); i++) {
            if (i == selectFilterIndex) {
                param.append(value).append("+");
            } else {
                List<SearchChildCategoryInfo> childCategoryInfos = categoryInfoList.get(i).childCategoryInfo;
                for (SearchChildCategoryInfo searchChildCategoryInfo1 : childCategoryInfos) {
                    if (searchChildCategoryInfo1.baseInfo.checked == 1) {
                        if (searchChildCategoryInfo1.childCategoryInfoList != null && searchChildCategoryInfo1.childCategoryInfoList.size() > 0) {
                            for (SearchChildCategoryInfo childCategoryInfo2 : searchChildCategoryInfo1.childCategoryInfoList) {
                                if (childCategoryInfo2.baseInfo.checked == 1) {
                                    param.append(childCategoryInfo2.baseInfo.value).append("+");
                                }
                            }
                        } else {
                            param.append(searchChildCategoryInfo1.baseInfo.value).append("+");
                        }
                    }
                }
            }
        }
        if (param.length() > 0) {
            return param.substring(0, param.length() - 1);
        }
        return param.toString();
    }

    public static String switchFromSecond(int second) {
        String restTime = "";
        String minuteString = SdkApplicationUtils.getApplication().getString(R.string.minute);
        String hourString = SdkApplicationUtils.getApplication().getString(R.string.hour);

        int minute = (second + 30) / 60;

        if (minute < 60) { // 小于1小时
            if (minute == 0) {
                restTime = "1" + minuteString;
            } else {
                restTime = minute + minuteString;
            }
        } else { // 大于小于1小时，小于24小时
            int hour = minute / 60;
            minute = minute % 60;
            if (minute > 0) {
                restTime = hour + hourString + minute + minuteString;
            } else {
                restTime = hour + hourString;
            }
        }

        return restTime;
    }
}
