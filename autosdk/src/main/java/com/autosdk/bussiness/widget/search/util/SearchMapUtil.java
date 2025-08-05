package com.autosdk.bussiness.widget.search.util;

import androidx.annotation.Nullable;

import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.layer.BizAreaControl;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizSearchControl;
import com.autonavi.gbl.layer.model.BizLineBusinessInfo;
import com.autonavi.gbl.layer.model.BizPointBusinessInfo;
import com.autonavi.gbl.layer.model.BizPolygonBusinessInfo;
import com.autonavi.gbl.layer.model.BizSearchChildPoint;
import com.autonavi.gbl.layer.model.BizSearchExitEntrancePoint;
import com.autonavi.gbl.layer.model.BizSearchParentPoint;
import com.autonavi.gbl.layer.model.BizSearchType;
import com.autonavi.gbl.layer.model.BizUserType;
import com.autonavi.gbl.layer.model.RouteEndAreaType;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.layer.LayerController;
import com.autosdk.bussiness.layer.MapLayer;
import com.autosdk.bussiness.layer.RouteResultLayer;
import com.autosdk.bussiness.layer.SearchLayer;
import com.autosdk.bussiness.layer.UserBehaviorLayer;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.MapController;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.widget.ui.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * description: 主图操作工具类
 */
public class SearchMapUtil {
    private static final String TAG = "SearchMapUtil";
    private static final int SURFACE_VIEW_ID = SurfaceViewID.SURFACE_VIEW_ID_MAIN;

    @Nullable
    private static MapController getMapController() {
        return SDKManager.getInstance().getMapController();
    }

    /**
     * 切换主图中心点
     */
    public static void updateMapCenter(@Nullable POI poi) {
        GeoPoint point = poi == null ? null : poi.getPoint();
        double lon = point == null ? 0 : point.getLongitude();
        double lat = point == null ? 0 : point.getLatitude();
        updateMapCenter(lon, lat);
    }

    /**
     * 切换主图中心点
     * // TODO: 2020/8/25 后续改为使用 mapController 中的方法, 尽量不直接操作
     * 另外, 当前主图移动位置后, 会自动切换会车标位置
     */
    public static void updateMapCenter(double lon, double lat) {
        MapView mapView = getMapView();
        if (lon != 0 && lat != 0 && mapView != null) {
            mapView.getOperatorPosture().setMapCenter(lon, lat, 0, true, true);
        }
    }

    /**
     * 获取默认的主图mapview
     */
    @Nullable
    public static MapView getMapView() {
        MapController mapController = getMapController();
        return mapController == null ? null : mapController.getMapView(SURFACE_VIEW_ID);
    }

    /**
     * 获取默认主图图层
     */
    @Nullable
    public static MapLayer getMapLayer() {
        return SDKManager.getInstance().getLayerController().getMapLayer(SURFACE_VIEW_ID);
    }

    /**
     * 获取指定的主图图层
     */
    @Nullable
    public static MapLayer getMapLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        return SDKManager.getInstance().getLayerController().getMapLayer(nSurfaceViewID);
    }

    /**
     * 获取默认RouteResultLayer图层
     */
    @Nullable
    public static RouteResultLayer getRouteResultLayer() {
        return SDKManager.getInstance().getLayerController().getRouteResultLayer(SURFACE_VIEW_ID);
    }

    /**
     * 获取指定的RouteResultLayer图层
     */
    @Nullable
    public static RouteResultLayer getRouteResultLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        return SDKManager.getInstance().getLayerController().getRouteResultLayer(nSurfaceViewID);
    }

    /**
     * 获取默认主图的搜索图层
     */
    @Nullable
    public static SearchLayer getSearchLayer() {
        return LayerController.getInstance().getSearchLayer(SURFACE_VIEW_ID);
    }

    /**
     * 获取指定的搜索图层
     */
    @Nullable
    public static SearchLayer getSearchLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        return LayerController.getInstance().getSearchLayer(nSurfaceViewID);
    }


    /**
     * 添加/移除指定的图层点击观测者
     *
     * @param observer   图层点击observer,若为null,则不作操作
     * @param removeOnly true-仅移除指定的observer, false-添加指定的observer
     */
    public static void setSearchLayerClickObserver(ILayerClickObserver observer, boolean removeOnly) {
        SearchLayer searchLayer = getSearchLayer();
        if (observer == null || searchLayer == null) {
            return;
        }

        searchLayer.removeClickObserver(observer);
        if (!removeOnly) {
            searchLayer.addClickObserver(observer);
        }
    }

    public static void clearFocus(@BizSearchType.BizSearchType1 int type) {
        SearchLayer searchLayer = getSearchLayer();
        if (searchLayer != null) {
            searchLayer.clearFocus(type);
        }
    }

    /**
     * 设置某个图层的焦点态
     */
    public static void setFocus(@BizSearchType.BizSearchType1 int type, String id, boolean isFocus) {
        if (StringUtils.isEmpty(id)) {
            return;
        }

        SearchLayer searchLayer = getSearchLayer();
        BaseLayer baseLayer = searchLayer == null ? null : searchLayer.getBaseLayer(type);
        if (baseLayer != null) {
            baseLayer.setFocus(id, isFocus);
        }
    }

    public static void clearAllSearchItems(@BizSearchType.BizSearchType1 int type) {
        SearchLayer searchLayer = getSearchLayer();
        BaseLayer baseLayer = searchLayer == null ? null : searchLayer.getBaseLayer(type);
        if (baseLayer != null) {
            baseLayer.clearAllItems();
        }
    }


    public static void clearAllUserItems(@BizUserType.BizUserType1 int type) {
        UserBehaviorLayer userBehaviorLayer = SDKManager.getInstance().getLayerController().getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        BaseLayer baseLayer = userBehaviorLayer == null ? null : userBehaviorLayer.getBaseLayer(type);
        if (baseLayer != null) {
            baseLayer.clearAllItems();
        }
    }


    /**
     * 搜索绘制终点区域
     *
     * @param endPoi         终点poi
     * @param drawPolygonRim 是否绘制多边形边框
     */
    public static void drawSearchEndPointArea(@Nullable POI endPoi, boolean drawPolygonRim) {
        /*暂时屏蔽
        GeoPoint endPoiPoint = endPoi == null ? null : endPoi.getPoint();
        if (endPoiPoint == null) {
            return;
        }
        RouteResultLayer routeResultLayer = getRouteResultLayer();
        //绘制搜索线
        ArrayList<BizLineBusinessInfo> vecLineInfo = new ArrayList<>();
        for (int i2 = 0; endPoi.getPoiRoadaoiBounds() != null && i2 < endPoi.getPoiRoadaoiBounds().size(); i2++) {
            BizLineBusinessInfo info = new BizLineBusinessInfo();
            info.id = i2 + "";
            for (int i3 = 0; i3 < endPoi.getPoiRoadaoiBounds().get(i2).size(); i3++) {
                Coord3DDouble coord3DDouble = new Coord3DDouble();
                coord3DDouble.lon = endPoi.getPoiRoadaoiBounds().get(i2).get(i3).getLongitude();
                coord3DDouble.lat = endPoi.getPoiRoadaoiBounds().get(i2).get(i3).getLatitude();
                info.mVecPoints.add(coord3DDouble);
            }
            vecLineInfo.add(info);
        }
        if (routeResultLayer != null) {
            if (vecLineInfo.size() > 0) {
                routeResultLayer.updateSearchLine(vecLineInfo);
            } else {
                routeResultLayer.clearSearchItems(BizSearchType.BizSearchTypeLine);
            }
        }

        //绘制多边形图层
        BizPolygonBusinessInfo polygonInfo = new BizPolygonBusinessInfo();
        ArrayList<GeoPoint> poiPolygonBounds1 = endPoi.getPoiPolygonBounds();
        int boundsSize = poiPolygonBounds1 == null ? 0 : poiPolygonBounds1.size();
        ArrayList<Coord3DDouble> mVecPoints = polygonInfo.mVecPoints;
        if (boundsSize > 0) {
            for (int i2 = 0; i2 < boundsSize; i2++) {
                GeoPoint geoPoint = poiPolygonBounds1.get(i2);
                Coord3DDouble posEndArea = new Coord3DDouble();
                posEndArea.lon = geoPoint.getLongitude();
                posEndArea.lat = geoPoint.getLatitude();
                mVecPoints.add(posEndArea);
            }
            polygonInfo.mDrawPolygonRim = true;
            if (routeResultLayer != null) {
                routeResultLayer.updateSearchPolygon(polygonInfo);
            }
        }*/
    }

    /**
     * 清除终点区域信息
     */
    public static void clearRouteEndArea() {
        MapView mapView = getMapView();
        if (mapView == null) {
            return;
        }

        BizControlService bizService = SDKManager.getInstance().getLayerController().getBizControlService();
        BizAreaControl bizAreaControl = bizService == null ? null : bizService.getBizAreaControl(mapView);
        BizSearchControl searchControl = bizService == null ? null : bizService.getBizSearchControl(mapView);

        if (bizAreaControl != null) {
            bizAreaControl.clearRouteEndArea(RouteEndAreaType.RouteEndAreaTypeAll);
        }
        if (searchControl != null) {
            searchControl.clearAllItems();
        }
    }

    /**
     * 搜索结果扎点（父POI）
     */
    public static boolean updateSearchParentPoi(ArrayList<BizSearchParentPoint> pointList) {
        SearchLayer searchLayer = getSearchLayer();
        return searchLayer != null && searchLayer.updateSearchParentPoi(pointList);
    }

    /**
     * 搜索结果扎点（子POI）
     */
    public static boolean updateSearchChildPoi(ArrayList<BizSearchChildPoint> pointList) {
        SearchLayer searchLayer = getSearchLayer();
        return searchLayer != null && searchLayer.updateSearchChildPoi(pointList);
    }

    /**
     * 搜索结果扎点（孙子POI）
     */
    public static boolean updateSearchPoiLabel(BizPointBusinessInfo pointList) {
        SearchLayer searchLayer = getSearchLayer();
        return searchLayer != null && searchLayer.updateSearchPoiLabel(pointList);
    }


    /**
     * 蓝色大头扎标
     */
    public static void updateBluePoi(POI poi, int offsetx, int offsety) {
        BizPointBusinessInfo sendToCarInfo = new BizPointBusinessInfo();
        sendToCarInfo.mPos3D.lon = poi.getPoint().getLongitude();
        sendToCarInfo.mPos3D.lat = poi.getPoint().getLatitude();
        SDKManager.getInstance().getLayerController().getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getUserControl().updateSendToCar(sendToCarInfo, offsetx, offsety);
    }

    public static void updateSearchChildPoiEnterExitPoi(POI tSelectPoi) {
        ArrayList<BizSearchExitEntrancePoint> exitEntrancePoints = new ArrayList<>();
        ArrayList<GeoPoint> parkiInfos = tSelectPoi == null ? null : tSelectPoi.getParkInfos();
        int entranceListSize = parkiInfos == null ? 0 : parkiInfos.size();

        ArrayList<BizLineBusinessInfo> vecLineInfo = new ArrayList<>();

        if (entranceListSize > 0) {
            for (int i2 = 0; i2 < entranceListSize; i2++) {
                GeoPoint item = parkiInfos.get(i2);
                BizSearchExitEntrancePoint parkinfo = new BizSearchExitEntrancePoint();
                parkinfo.type = 0;
                parkinfo.mPos3D.lon = item.getLongitude();
                parkinfo.mPos3D.lat = item.getLatitude();
                exitEntrancePoints.add(parkinfo);
                BizLineBusinessInfo info = new BizLineBusinessInfo();
                info.type = 3;
                info.id = "Park";
                info.mVecPoints = new ArrayList<>();
                Coord3DDouble start = new Coord3DDouble(tSelectPoi.getPoint().getLongitude(), tSelectPoi.getPoint().getLatitude(), 0);
                Coord3DDouble end = new Coord3DDouble(item.getLongitude(), item.getLatitude(), 0);
                info.mVecPoints.add(start);
                info.mVecPoints.add(end);
                vecLineInfo.add(info);
            }
        }
        RouteResultLayer routeResultLayer = getRouteResultLayer();
        if (routeResultLayer == null) {
            return;
        }
        if (exitEntrancePoints.size() > 0) {
            routeResultLayer.updateSearchExitEntrancePoi(exitEntrancePoints);
        } else {
            routeResultLayer.clearSearchItems(BizSearchType.BizSearchTypePoiExitEntrance);
        }
        if (vecLineInfo.size() > 0) {
            routeResultLayer.updateSearchLine(vecLineInfo);
        }
    }

    /**
     * 计算搜索结果所有的Bound
     *
     * @param pois
     * @return
     */
    public static RectDouble getSearchAlongBound(List<BizSearchParentPoint> pois) {
        if (pois==null || pois.isEmpty()) {
            return null;
        }
        try {
            double x1 = Double.MAX_VALUE;
            double y1 = Double.MAX_VALUE;
            double x2 = Double.MIN_VALUE;
            double y2 = Double.MIN_VALUE;
            for (int i = 0; i < pois.size(); i++) {
                BizSearchParentPoint oItem = pois.get(i);
                x1 = Math.min(x1, oItem.mPos3D.lon);
                y1 = Math.min(y1, oItem.mPos3D.lat);
                x2 = Math.max(x2, oItem.mPos3D.lon);
                y2 = Math.max(y2, oItem.mPos3D.lat);
            }
            RectDouble rect = new RectDouble(x1, x2, y2, y1);
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算记忆行车的Bound
     *
     * @param pois
     * @return
     */
    public static RectDouble getSmartDriveBound(List<Coord3DDouble> pois) {
        if (pois == null && pois.size() == 0) {
            return null;
        }
        try {
            double x1 = Double.MAX_VALUE;
            double y1 = Double.MAX_VALUE;
            double x2 = Double.MIN_VALUE;
            double y2 = Double.MIN_VALUE;
            for (int i = 0; i < pois.size(); i++) {
                Coord3DDouble oItem = pois.get(i);
                x1 = Math.min(x1, oItem.lon);
                y1 = Math.min(y1, oItem.lat);
                x2 = Math.max(x2, oItem.lon);
                y2 = Math.max(y2, oItem.lat);
            }
            RectDouble rect = new RectDouble(x1, x2, y2, y1);
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算搜索结果所有的Bound
     *
     * @param pois
     * @return
     */
    public static RectDouble getParkingBound(List<POI> pois) {
        if (pois.size() == 0) {
            return null;
        }
        try {
            double x1 = Double.MAX_VALUE;
            double y1 = Double.MAX_VALUE;
            double x2 = Double.MIN_VALUE;
            double y2 = Double.MIN_VALUE;
            for (int i = 0; i < pois.size(); i++) {
                POI oItem = pois.get(i);
                x1 = Math.min(x1, oItem.getPoint().getLongitude());
                y1 = Math.min(y1, oItem.getPoint().getLatitude());
                x2 = Math.max(x2, oItem.getPoint().getLongitude());
                y2 = Math.max(y2, oItem.getPoint().getLatitude());
            }
            RectDouble rect = new RectDouble(x1, x2, y2, y1);
            return rect;
        } catch (Exception e) {
            return null;
        }
    }
}
