package com.autosdk.bussiness.account.utils;


import static com.autosdk.common.utils.CommonUtil.distanceUnitTransform;

import android.location.Location;
import android.text.TextUtils;

import com.autonavi.gbl.aosclient.model.GPredictInfo;
import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.layer.model.BizLayerUtil;
import com.autonavi.gbl.map.OperatorPosture;
import com.autonavi.gbl.map.model.PointD;
import com.autonavi.gbl.user.behavior.model.FavoriteBaseItem;
import com.autonavi.gbl.user.behavior.model.FavoriteItem;
import com.autonavi.gbl.user.behavior.model.FavoriteType;
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem;
import com.autonavi.gbl.user.usertrack.model.HistoryRouteItem;
import com.autonavi.gbl.user.usertrack.model.HistoryRoutePoiItem;
import com.autonavi.gbl.user.usertrack.model.SearchHistoryItem;
import com.autosdk.R;
import com.autosdk.bussiness.account.BehaviorController;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.POIFactory;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;
import com.autosdk.bussiness.search.utils.NumberUtil;

import java.util.ArrayList;

/**
 * Created by AutoSdk on 2020/10/27.
 **/
public class ConverUtils {

    /**
     * POI是否有效
     */
    public static boolean isPoiValid(POI poi) {
        if (poi == null || poi.getPoint() == null) {
            return false;
        }
        return true;

    }

    /**
     * 保存开始导航数据未历史导航记录
     *
     * @param routeCarResultData //naviFragment接收的数据
     * @return
     */
    public static HistoryRouteItem convertRouteResultToSearchHistoryRouteItem(RouteCarResultData routeCarResultData) {
        if (routeCarResultData == null || !isPoiValid(routeCarResultData.getFromPOI()) || !isPoiValid(routeCarResultData.getToPOI())) {
            return null;
        }

        HistoryRouteItem historyRouteItem = new HistoryRouteItem();
        historyRouteItem.startLoc = new Coord2DDouble(routeCarResultData.getFromPOI().getPoint().getLongitude(), routeCarResultData.getFromPOI().getPoint().getLatitude());
        historyRouteItem.endLoc = new Coord2DDouble(routeCarResultData.getToPOI().getPoint().getLongitude(), routeCarResultData.getToPOI().getPoint().getLatitude());
        historyRouteItem.fromPoi = converPoiToHistoryRoutePoiItem(routeCarResultData.getFromPOI());
        historyRouteItem.toPoi = converPoiToHistoryRoutePoiItem(routeCarResultData.getToPOI());
        historyRouteItem.midPoi = new ArrayList<>();
        if (null != routeCarResultData.getMidPois()) {
            for (POI poi : routeCarResultData.getMidPois()) {
                historyRouteItem.midPoi.add(converPoiToHistoryRoutePoiItem(poi));
            }
        }
        return historyRouteItem;
    }

    public static HistoryRoutePoiItem converPoiToHistoryRoutePoiItem(POI poi) {
        if (poi == null) {
            return null;
        }

        HistoryRoutePoiItem historyRoutePoiItem = new HistoryRoutePoiItem();
        historyRoutePoiItem.address = poi.getAddr();
        historyRoutePoiItem.childType = poi.getChildType();
        historyRoutePoiItem.cityCode = NumberUtil.str2Int(poi.getCityCode(), 0);
        historyRoutePoiItem.cityName = poi.getCityName();
        historyRoutePoiItem.entranceList = new ArrayList<>();
        if (null != poi.getEntranceList()) {
            for (GeoPoint geoPoint : poi.getEntranceList()) {
                historyRoutePoiItem.entranceList.add(new Coord2DDouble(geoPoint.getLongitude(), geoPoint.getLatitude()));
            }
        }
        historyRoutePoiItem.floorNo = poi.getFloorNo();
        historyRoutePoiItem.name = poi.getName();
        historyRoutePoiItem.parent = poi.getParent();
        historyRoutePoiItem.poiId = poi.getId();
        if (poi.getPoint() != null) {
            historyRoutePoiItem.poiLoc = new Coord2DDouble(poi.getPoint().getLongitude(), poi.getPoint().getLatitude());
        }
        return historyRoutePoiItem;
    }

    /**
     * 将导航历史记录途经点转换成POI
     *
     * @param midPoiList
     * @return
     */
    public static ArrayList<POI> convertHistoryRoutePoiToMidPois(ArrayList<HistoryRoutePoiItem> midPoiList) {
        ArrayList<POI> poiList = new ArrayList<>();
        if (midPoiList != null && midPoiList.size() > 0) {
            for (HistoryRoutePoiItem midItem : midPoiList) {
                POI poi = new POI();
                poi.setName(midItem.name);
                poi.setAddr(midItem.address);
                poi.setId(midItem.poiId);
                poi.setPoint(new GeoPoint(midItem.poiLoc.lon, midItem.poiLoc.lat));
                poiList.add(poi);
            }
        }
        return poiList;
    }

    /**
     * 将导航历史记录转换未POI ，供开启路线规划使用
     *
     * @param historyRouteItem
     * @return
     */
    public static POI convertHistoryRouteItemToPOI(HistoryRouteItem historyRouteItem) {
        POI poi = new POI();
        poi.setCategory(historyRouteItem.toPoi.address);
        poi.setAddr(historyRouteItem.toPoi.address);
        poi.setId(historyRouteItem.toPoi.poiId);
        poi.setName(historyRouteItem.toPoi.name);
        poi.setCityCode(historyRouteItem.toPoi.cityCode + "");
        poi.setCityName(historyRouteItem.toPoi.cityName);
        poi.setFloorNo(historyRouteItem.toPoi.floorNo);
        poi.setTypeCode(historyRouteItem.toPoi.typeCode);
        poi.setPoint(new GeoPoint(historyRouteItem.toPoi.poiLoc.lon, historyRouteItem.toPoi.poiLoc.lat));
        if (TextUtils.isEmpty(poi.getDis())) {
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
            Coord2DDouble endPoint = new Coord2DDouble(historyRouteItem.endLoc.lon, historyRouteItem.endLoc.lat);
            poi.setDis(distanceUnitTransform((long) BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint)));
        }
        return poi;
    }

    public static FavoriteBaseItem converPOIToFavoriteBaseItem(POI poi) {
        FavoriteBaseItem favoriteBaseItem = new FavoriteBaseItem();
        favoriteBaseItem.name = poi.getName();
        favoriteBaseItem.poiid = poi.getId();
        PointD coord2DDouble = OperatorPosture.lonLatToMap(poi.getPoint().getLongitude(), poi.getPoint().getLatitude());
        int mX = new Double(coord2DDouble.x).intValue();
        int mY = new Double(coord2DDouble.y).intValue();
        favoriteBaseItem.point_x = mX;
        favoriteBaseItem.point_y = mY;
        return favoriteBaseItem;
    }

    public static FavoriteItem converPOIToFavoriteItem(POI poi, @FavoriteType.FavoriteType1 int type) {
        FavoriteItem favoriteItem = new FavoriteItem();
        favoriteItem.name = poi.getName();
        favoriteItem.poiid = poi.getId();
        favoriteItem.address = poi.getAddr();
        PointD coord2DDouble = OperatorPosture.lonLatToMap(poi.getPoint().getLongitude(), poi.getPoint().getLatitude());
        int mX = new Double(coord2DDouble.x).intValue();
        int mY = new Double(coord2DDouble.y).intValue();
        favoriteItem.point_x = mX;
        favoriteItem.point_y = mY;
        favoriteItem.common_name = type;
        return favoriteItem;
    }

    public static POI converFavoriteItemToPOI(FavoriteItem favoriteItem) {
        POI poi = POIFactory.createPOI();
        poi.setName(favoriteItem.name);
        poi.setId(favoriteItem.poiid);
        poi.setAddr(favoriteItem.address);
        Coord2DDouble coord2DDouble = OperatorPosture.mapToLonLat(favoriteItem.point_x, favoriteItem.point_y);
        poi.setPoint(new GeoPoint(coord2DDouble.lon, coord2DDouble.lat));
        return poi;
    }

    public static POI converSimpleFavoriteItemToPoi(SimpleFavoriteItem simpleFavoriteItem) {
        POI poi = POIFactory.createPOI();
        poi.setName(simpleFavoriteItem.name);
        poi.setAddr(simpleFavoriteItem.address);
        poi.setCityCode(simpleFavoriteItem.city_code);
        FavoriteBaseItem favoriteBaseItem = new FavoriteBaseItem();
        favoriteBaseItem.item_id = simpleFavoriteItem.item_id;
        FavoriteItem favoriteItem = BehaviorController.getInstance().getFavorite(favoriteBaseItem);
        if (favoriteItem == null) {
            return poi;
        }
        poi.setId(favoriteItem.poiid);
        Coord2DDouble coord2DDouble = OperatorPosture.mapToLonLat(favoriteItem.point_x, favoriteItem.point_y);
        poi.setPoint(new GeoPoint(coord2DDouble.lon, coord2DDouble.lat));
        return poi;
    }

    public static POI converSimpleFavoriteToPoi(SimpleFavoriteItem simpleFavoriteItem) {
        POI poi = POIFactory.createPOI();
        poi.setName(simpleFavoriteItem.name);
        poi.setAddr(simpleFavoriteItem.address);
        poi.setCityCode(simpleFavoriteItem.city_code);
        FavoriteBaseItem favoriteBaseItem = new FavoriteBaseItem();
        favoriteBaseItem.item_id = simpleFavoriteItem.item_id;
        FavoriteItem favoriteItem = BehaviorController.getInstance().getFavorite(favoriteBaseItem);
        if (favoriteItem == null) {
            return poi;
        }
        if (TextUtils.isEmpty(favoriteItem.poiid)){
            poi.setAddr(simpleFavoriteItem.address + "附近");
        }
        poi.setId(favoriteItem.poiid);
        Coord2DDouble coord2DDouble = OperatorPosture.mapToLonLat(favoriteItem.point_x, favoriteItem.point_y);
        poi.setPoint(new GeoPoint(coord2DDouble.lon, coord2DDouble.lat));
        return poi;
    }

    public static POI converPredictInfoToPoi(GPredictInfo predictInfo) {
        POI poi = POIFactory.createPOI();
        poi.setName(predictInfo.poi_name);
        poi.setAddr(predictInfo.poi_address);
        poi.setId(predictInfo.poi_id);
        poi.setPoint(new GeoPoint(predictInfo.poi_x, predictInfo.poi_y));
        return poi;
    }

    /**
     * 保存开始导航数据搜索历史记录
     *
     * @param routeCarResultData
     * @return
     */
    public static SearchHistoryItem convertRouteResultToSearchHistoryItem(RouteCarResultData routeCarResultData) {
        if (routeCarResultData == null) {
            return null;
        }

        SearchHistoryItem searchHistoryItem = new SearchHistoryItem();
        searchHistoryItem.history_type = 3;
        //searchHistoryItem.iconinfo = R.drawable.ic_location_day;

        POI toPoi = routeCarResultData.getToPOI();
        if (toPoi != null) {
            searchHistoryItem.name = toPoi.getName();
            searchHistoryItem.address = toPoi.getAddr();
            searchHistoryItem.poiid = toPoi.getId();
            searchHistoryItem.parent = toPoi.getParent();
            searchHistoryItem.x_entr = toPoi.getPoint().getLongitude();
            searchHistoryItem.x = toPoi.getPoint().getLongitude();
            searchHistoryItem.y_entr = toPoi.getPoint().getLatitude();
            searchHistoryItem.y = toPoi.getPoint().getLatitude();
            searchHistoryItem.func_text = toPoi.getDis();
            searchHistoryItem.district = toPoi.getDistrict();
            searchHistoryItem.poi_tag = toPoi.getPoiTag();
            searchHistoryItem.category = toPoi.getCategory();
        }
        return searchHistoryItem;
    }
}
