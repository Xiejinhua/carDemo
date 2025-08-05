package com.autosdk.bussiness.search.utils;

import android.location.Location;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.data.model.AdminCode;
import com.autonavi.gbl.data.model.AreaExtraInfo;
import com.autonavi.gbl.data.model.CityItemInfo;
import com.autonavi.gbl.data.model.RegionCode;
import com.autonavi.gbl.layer.model.BizLayerUtil;
import com.autonavi.gbl.search.model.NearestPoi;
import com.autonavi.gbl.search.model.SearchDistrict;
import com.autonavi.gbl.search.model.SearchNearestResult;
import com.autonavi.gbl.search.model.SearchParkInOutInfo;
import com.autonavi.gbl.search.model.SearchPoiBasicInfo;
import com.autonavi.gbl.search.model.SearchPoiChildInfo;
import com.autonavi.gbl.search.model.SearchPoiInfo;
import com.autonavi.gbl.search.model.SearchPoiLocRes;
import com.autonavi.gbl.search.model.SearchPoiParkingInfo;
import com.autonavi.gbl.search.model.SearchSuggestPoiBase;
import com.autonavi.gbl.search.model.SearchSuggestTip;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.POIFactory;
import com.autosdk.bussiness.data.MapDataController;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.search.request.SearchLocInfo;
import com.autosdk.bussiness.search.result.city.AdCity;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 搜索结果处理，数据转换
 */
public class SearchResultUtils {
    private static final String TAG = "SearchResultUtils";
    private SearchResultUtils(){

    }
    private static class SearchResultUtilsHolder {
        private static SearchResultUtils instance = new SearchResultUtils();
    }

    public static SearchResultUtils getInstance() {
        return SearchResultUtilsHolder.instance;
    }

    /**
     * 构建一个逆地理搜索结果的POI
     * @param data 逆地理结果
     * @param poi 上层已有的poi,传入赋值
     * @return
     */
    public POI createNearestResultToPoi(SearchNearestResult data,POI poi){
        if(null == poi || !isLegalPoint(poi.getPoint())){
            throw new NullPointerException("nearestSearch poi param invalid");
        }
        if(null == data){
            poi.setName("在地图选点附近");
            poi.setAddr("地图选点");
            Location location = LocationController.getInstance().getLastLocation();
            double dis = GeoPoint.calcDistanceBetweenPoints(poi.getPoint(),
                new GeoPoint(location.getLongitude(), location.getLatitude()));
            poi.setDis(routeResultDistance((long)dis));
        }else{
            NearestPoi nearestPoi = data.poi_list.get(0);
            poi.setTypeCode(nearestPoi.type);
            if (poi.getName().isEmpty()) {
                if (data.desc.isEmpty()) {
                    poi.setName(nearestPoi.name);
                } else {
                    poi.setName(data.desc);
                }
            }
            poi.setCityName(data.city);
            poi.setDistance(String.valueOf(nearestPoi.distance));
            poi.setCityCode("" + data.cityadcode);
            if (poi.getAddr().isEmpty()) {
                if (data.pos.isEmpty()) {
                    String address = nearestPoi.address;
                    if (address.isEmpty()) {
                        poi.setAddr("在" + poi.getName() + "附近");
                    } else {
                        poi.setAddr("在" + address + "附近");
                    }
                } else {
                    int index = data.pos.indexOf(',');
                    if (0 < index) {
                        poi.setAddr(data.pos.substring(0, index));
                    } else {
                        poi.setAddr(data.pos);
                    }
                }
            }
            poi.setAdCode("" + data.adcode);
            if(TextUtils.isEmpty(String.valueOf(nearestPoi.distance)) || nearestPoi.distance == 0){
                Location location = SDKManager.getInstance().getLocController().getLastLocation();
                Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
                double dis = BizLayerUtil.calcDistanceBetweenPoints(startPoint, nearestPoi.point);
                poi.setDis(routeResultDistance((long)dis));
            }else{
                poi.setDis(routeResultDistance(nearestPoi.distance));
            }
            //poi的出入口数据
            ArrayList<GeoPoint> geoEntrancesList = new ArrayList<>();
            Coord2DDouble entrancesList = nearestPoi.naviPoint;
            GeoPoint exitEntrancePoint = new GeoPoint();
            exitEntrancePoint.setLonLat(entrancesList.lon, entrancesList.lat);
            geoEntrancesList.add(exitEntrancePoint);
            poi.setEntranceList(geoEntrancesList);
        }
        return poi;
    }

    /**
     * 关键字搜索结果转换为List<POI>
     * @param searchPoiInfos 关键字搜索结果
     * @return
     */
    public List<POI> createKeyWordSearchResultToPoi(ArrayList<SearchPoiInfo> searchPoiInfos){
        List<POI> pois = new ArrayList<>();
        if (searchPoiInfos != null && !searchPoiInfos.isEmpty()) {
            Timber.d( "getSearchInfo: blResult.poiList.length = " + searchPoiInfos.size());
            for (SearchPoiInfo blSearchPoi : searchPoiInfos) {
                POI poi = getSearchPoiFromBlSearchPoi(blSearchPoi);
                pois.add(poi);
            }
        }
        return pois;
    }

    private POI getSearchPoiFromBlSearchPoi(SearchPoiInfo blSearchPoi) {
        if (blSearchPoi == null) {
            Timber.d( "getSearchPoiFromBlSearchPoi blSearchPoi == null");
            return null;
        }

        if (blSearchPoi.basicInfo == null) {
            Timber.d( "getSearchPoiFromBlSearchPoi blSearchPoi.basicInfo == null");
            return null;
        }

        POI poi = POIFactory.createPOI();
        SearchPoiBasicInfo poiBase = blSearchPoi.basicInfo;
        poi.setAdCode(String.valueOf(poiBase.adcode));
        poi.setIndustry(poiBase.industry);
        poi.setDistance(poiBase.distance);
        if (TextUtils.isEmpty(poiBase.address)) {
            AreaExtraInfo adareaInfo = MapDataController.getInstance().getAreaExtraInfo(new AdminCode(RegionCode.REGION_CODE_NULL,
                poiBase.adcode,
                poiBase.adcode));
            if (null != adareaInfo) {
                if (!TextUtils.isEmpty(adareaInfo.townName)) {
                    poi.setAddr(adareaInfo.townName);
                } else if (!TextUtils.isEmpty(adareaInfo.cityName)) {
                    poi.setAddr(adareaInfo.cityName);
                } else if (!TextUtils.isEmpty(adareaInfo.provName)) {
                    poi.setAddr(adareaInfo.provName);
                }
            }
        } else {
            poi.setAddr(poiBase.address);
        }
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
        poi.setChargeStationInfo(blSearchPoi.chargingStationInfo);
        poi.setParkingInfo(blSearchPoi.parkingInfo);
        poi.setRating(blSearchPoi.basicInfo.rating);
        poi.setAverageCost(blSearchPoi.basicInfo.averageCost);
        poi.setImageUrl(blSearchPoi.basicInfo.imageUrl);
        poi.setSearchProductInfoList(blSearchPoi.produceInfoList);

        poi.setDeepinfo(poiBase.openTime);
        if (TextUtils.isEmpty(poi.getDis())) {
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
            Coord2DDouble endPoint = new Coord2DDouble(poiBase.location.lon, poiBase.location.lat);
            poi.setDis("" + routeResultDistance((long) BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint)));
        }
        if (TextUtils.isEmpty(poi.getCityName())){
            // 获取城市adcode和城市名
            SearchLocInfo mSearchLocInfo = getLocationToSearchLocInfo();
            MapDataController mapDataController = SDKManager.getInstance().getMapDataController();
            mSearchLocInfo.adcode = mapDataController.getAdcodeByLonLat(poiBase.location.lon, poiBase.location.lat);
            CityItemInfo cityInfo = mapDataController.getCityInfo(mSearchLocInfo.adcode);
            mSearchLocInfo.cityName = cityInfo == null ? "" : cityInfo.cityName;
            poi.setCityName(mSearchLocInfo.cityName);
            poi.setCityCode(String.valueOf(mSearchLocInfo.adcode));
        }
        ArrayList<ArrayList<Coord2DDouble>> poiPolygonBounds = blSearchPoi.basicInfo.poiAoiBounds;
        if (poiPolygonBounds != null && !poiPolygonBounds.isEmpty()) {
            ArrayList<ArrayList<GeoPoint>> poiPolygonBoundList = new ArrayList<>();
            for (int i = 0; i < poiPolygonBounds.size(); i++) {
                ArrayList<GeoPoint> poiPoint = new ArrayList<>();
                for (int j = 0; j < poiPolygonBounds.get(i).size(); j++) {
                    poiPoint.add(new GeoPoint(poiPolygonBounds.get(i).get(j).lon, poiPolygonBounds.get(i).get(j).lat));
                }
                poiPolygonBoundList.add(poiPoint);
            }
            poi.setPoiRoadaoiBounds(poiPolygonBoundList);
        }


        ArrayList<ArrayList<Coord2DDouble>> poiRoadaoiBounds = blSearchPoi.basicInfo.roadPolygonBounds;
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
        for (int i = 0; i < blSearchPoi.childInfoList.size(); i++) {
            SearchPoiChildInfo childPoiBase = blSearchPoi.childInfoList.get(i);
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
            childEntranceList.size();
            childPoi.setEntranceList(childEntranceList);
            childPoi.setChildType(childPoiBase.childType);
            childPoiList.add(childPoi);
        }
        poi.setChildPois(childPoiList);

        poi.setGasInfo(blSearchPoi.gasInfo);

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

    public POI resultCityToPoi(SearchPoiLocRes searchPoiLocRes){
        POI poi = null;
        int citylistSize = searchPoiLocRes == null ? 0 : searchPoiLocRes.total;
        if (citylistSize > 0) {
            SearchDistrict searchDistrict = searchPoiLocRes.citylist.get(0);
            poi = searchDistrictToPoi(searchDistrict);
        }
        return poi;
    }

    public POI searchDistrictToPoi(SearchDistrict searchDistrict){
        POI poi = null;
        poi = POIFactory.createPOI();
        poi.setName(searchDistrict.name);
        poi.setAdCode(searchDistrict.adcode + "");
        if (TextUtils.isEmpty(searchDistrict.address)) {
            AreaExtraInfo adareaInfo = MapDataController.getInstance().getAreaExtraInfo(new AdminCode(RegionCode.REGION_CODE_NULL,
                searchDistrict.adcode,
                searchDistrict.adcode));
            if (null != adareaInfo) {
                if (!TextUtils.isEmpty(adareaInfo.townName)) {
                    poi.setAddr(adareaInfo.townName);
                } else if (!TextUtils.isEmpty(adareaInfo.cityName)) {
                    poi.setAddr(adareaInfo.cityName);
                } else if (!TextUtils.isEmpty(adareaInfo.provName)) {
                    poi.setAddr(adareaInfo.provName);
                }
            }
        } else {
            poi.setAddr(searchDistrict.address);
        }
        GeoPoint point = new GeoPoint();
        point.setLonLat(searchDistrict.poi_loc.lon, searchDistrict.poi_loc.lat);
        poi.setPoint(point);

        if (searchDistrict.polygonBounds != null && !searchDistrict.polygonBounds.isEmpty()) {
            ArrayList<ArrayList<GeoPoint>> poiPolygonBoundList = new ArrayList<>();
            for (int i = 0; i < searchDistrict.polygonBounds.size(); i++) {
                ArrayList<Coord2DDouble> poiPolygonBounds = searchDistrict.polygonBounds.get(i).points;
                if (poiPolygonBounds != null && !poiPolygonBounds.isEmpty()) {
                    ArrayList<GeoPoint> poiPolygonBoundPoints = new ArrayList<>();
                    for (int j = 0; j < poiPolygonBounds.size(); j++) {
                        poiPolygonBoundPoints.add(new GeoPoint(poiPolygonBounds.get(j).lon, poiPolygonBounds.get(j).lat));
                    }
                }
            }
            poi.setPoiPolygonBounds(poiPolygonBoundList);
        }
        return poi;
    }

    /**
     * 根据产品定义的策略，路线规划页面不做TBT策略取整处理，直接四舍五入取值
     *
     * @param dis
     * @return
     */
    private String routeResultDistance(long dis) {
        StringBuffer sb = new StringBuffer();
        int distance = (int) dis;
        if (distance >= 1000) {
            int kiloMeter = distance / 1000;
            int leftMeter = distance % 1000;
            leftMeter = leftMeter / 100;
            if (kiloMeter > 100) {
                sb.append(kiloMeter);
                sb.append("公里");
            } else if (leftMeter > 0) {
                sb.append(kiloMeter);
                sb.append(".");
                sb.append(leftMeter);
                sb.append("公里");
            } else {
                sb.append(kiloMeter);
                sb.append("公里");
            }
        } else {
            sb.append(distance);
            sb.append("米");
        }
        return sb.toString();
    }

    public boolean isLegalPoint(Coord2DDouble coord) {
        return coord != null && coord.lon > 0 && coord.lat > 0;
    }
    public boolean isLegalPoint(GeoPoint point) {
        return point != null && point.getLongitude() > 0 && point.getLatitude() > 0;
    }

    /**
     * 获取定位的数据，转化为搜索需求数据
     */
    public SearchLocInfo getLocationToSearchLocInfo() {
        SearchLocInfo  mSearchLocInfo = new SearchLocInfo();
        //来自定位的数据，一定不能为空
        SDKManager sdkManager = SDKManager.getInstance();
        MapDataController mapDataController = sdkManager.getMapDataController();

        // 用户(车标)位置经纬度
        Location location = sdkManager.getLocController().getLastLocation();
        mSearchLocInfo.lon = location.getLongitude();
        mSearchLocInfo.lat = location.getLatitude();
        mSearchLocInfo.adcode = mapDataController.getAdcodeByLonLat(location.getLongitude(), location.getLatitude());
        CityItemInfo cityInfo = mapDataController.getCityInfo(mSearchLocInfo.adcode);
        mSearchLocInfo.cityName = cityInfo == null ? "" : cityInfo.cityName;
        return mSearchLocInfo;
    }

    /**
     * 批量转换预搜索结果点为poi列表
     */
    @Nullable
    public List<POI> convertSuggestionTipToPoiList(List<SearchSuggestTip> src) {
        int size = src == null ? 0 : src.size();
        if (size == 0) {
            return null;
        }

        List<POI> result = new ArrayList<>();
        for (SearchSuggestTip tip : src) {
            POI poi = convertSuggestionTipToPoi(tip);
            if (poi != null) {
                result.add(poi);
            }
        }
        return result;
    }

    /**
     * 转换预搜索信息为poi对象
     */
    @Nullable
    private POI convertSuggestionTipToPoi(SearchSuggestTip src) {
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
            poi.setDis("" + routeResultDistance((long) BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint)));
        }
        return poi;
    }

    /**
     * 获取地图中心的城市编号和名称
     */
    public AdCity getMapCenterAdCity(int displayId) {
        AdCity adCity = new AdCity();
        //来自定位的数据，一定不能为空
        SDKManager sdkManager = SDKManager.getInstance();
        // 地图中心
        Coord3DDouble mapCenterLonLat = sdkManager.getMapController().getMapView(SurfaceViewID.transformDisplayId2SurfaceId(displayId)).getOperatorPosture().getMapCenter();
        // 获取城市adcode和城市名
        MapDataController mapDataController = sdkManager.getMapDataController();
        int adCode = mapDataController.getAdcodeByLonLat(mapCenterLonLat.lon, mapCenterLonLat.lat);
        CityItemInfo cityInfo = mapDataController.getCityInfo(adCode);
        String cityName = cityInfo == null ? "" : cityInfo.cityName;
        adCity.setCityName(cityName);
        adCity.setCityAdcode(adCode);
        return adCity;
    }

    public static ArrayList<ArrayList<GeoPoint>> coordList2GeoPointList(ArrayList<ArrayList<Coord2DDouble>> poiPolygonBounds) {
        if (poiPolygonBounds != null && !poiPolygonBounds.isEmpty()) {
            ArrayList<ArrayList<GeoPoint>> poiPolygonBoundList = new ArrayList<>();
            for (int i = 0; i < poiPolygonBounds.size(); i++) {
                ArrayList<Coord2DDouble> coord2DDoubles = poiPolygonBounds.get(i);
                if (coord2DDoubles != null && !coord2DDoubles.isEmpty()) {
                    ArrayList<GeoPoint> poiPolygonBoundPoints = new ArrayList<>();
                    for (int j = 0; j < coord2DDoubles.size(); j++) {
                        poiPolygonBoundPoints.add(new GeoPoint(coord2DDoubles.get(j).lon, coord2DDoubles.get(j).lat));
                    }
                    poiPolygonBoundList.add(poiPolygonBoundPoints);
                }
            }
            return poiPolygonBoundList;
        }
        return null;
    }
}
