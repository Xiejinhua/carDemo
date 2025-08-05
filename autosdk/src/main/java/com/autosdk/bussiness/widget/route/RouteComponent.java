package com.autosdk.bussiness.widget.route;

import android.graphics.Rect;
import android.location.Location;

import androidx.annotation.NonNull;

import com.autonavi.gbl.common.model.Coord2DFloat;
import com.autonavi.gbl.common.model.Coord2DInt32;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.common.model.RectInt;
import com.autonavi.gbl.common.path.model.PointType;
import com.autonavi.gbl.common.path.option.RouteConstrainCode;
import com.autonavi.gbl.map.model.PreviewParam;
import com.autonavi.gbl.search.model.KeywordSearchResultV2;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.POIFactory;
import com.autosdk.bussiness.common.task.TaskManager;
import com.autosdk.bussiness.layer.LayerController;
import com.autosdk.bussiness.layer.RouteResultLayer;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.navi.route.RouteRequestController;
import com.autosdk.bussiness.navi.route.callback.IRouteResultCallBack;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;
import com.autosdk.bussiness.navi.route.model.RouteRequestParam;
import com.autosdk.bussiness.search.SearchCallback;
import com.autosdk.bussiness.search.SearchControllerV2;
import com.autosdk.bussiness.search.request.SearchQueryType;
import com.autosdk.bussiness.search.request.SearchRequestInfo;
import com.autosdk.bussiness.widget.route.model.NaviStationItemData;
import com.autosdk.bussiness.widget.route.utils.RectUtils;
import com.autosdk.bussiness.widget.setting.SettingComponent;
import com.autosdk.common.AutoConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AutoSdk on 2021/7/12.
 **/
public class RouteComponent {

    public static RouteComponent mInstance;

    private RouteCarResultData mRouteCarResultData;

    public static RouteComponent getInstance() {
        if (mInstance == null) {
            mInstance = new RouteComponent();
        }
        return mInstance;
    }

    /**
     * 规划页获取算路请求参数
     *
     * @param startPoi           起点poi
     * @param endPoi             终点poi
     * @param routeStrategy      路线偏好
     * @param routeConstrainCode 算路附加要求
     * @return
     */
    public static RouteRequestParam getRouteRequestParam(POI startPoi, POI endPoi, int routeStrategy, int routeConstrainCode, boolean isNetworkConnected) {
        if (startPoi == null) {
            //获取当前车位为起点
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            startPoi = POIFactory.createPOI("我的位置", new GeoPoint(location.getLongitude(), location.getLatitude()));
        }
        RouteRequestParam routeRequestParam = new RouteRequestParam(startPoi, endPoi);
        routeRequestParam.invokerType = "plan";
        routeRequestParam.routeStrategy = routeStrategy;
        if (isNetworkConnected) {
            routeRequestParam.routeConstrainCode = routeConstrainCode;
        } else {//如果刚开始本地没有网络那么直接走离线规划
            routeRequestParam.routeConstrainCode = routeConstrainCode | RouteConstrainCode.RouteCalcLocal;
        }
        //车牌信息通过用户模块获取
        routeRequestParam.carPlate = AutoConstant.vehicleId;
        //设置车牌号
        int configAvoidLimit = SettingComponent.getInstance().getConfigKeyAvoidLimit();
        if (configAvoidLimit == 0) {
            //默认态
            routeRequestParam.openAvoidLimit = false;
        } else {//避开限行
            routeRequestParam.openAvoidLimit = true;
        }
        return routeRequestParam;
    }

    /**
     * 带有途经点规划页获取算路请求参数
     *
     * @param startPoi
     * @param endPoi
     * @param midPois
     * @param routeStrategy
     * @param routeConstrainCode
     * @return
     */
    public static RouteRequestParam getRouteRequestParam(POI startPoi, POI endPoi, ArrayList<POI> midPois, int routeStrategy, int routeConstrainCode, boolean isNetworkConnected) {
        if (startPoi == null) {
            //获取当前车位为起点
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            startPoi = POIFactory.createPOI("我的位置", new GeoPoint(location.getLongitude(), location.getLatitude()));
        }
        RouteRequestParam routeRequestParam = new RouteRequestParam(startPoi, endPoi, midPois);
        routeRequestParam.invokerType = "plan";
        routeRequestParam.routeStrategy = routeStrategy;
        if (isNetworkConnected) {
            routeRequestParam.routeConstrainCode = routeConstrainCode;
        } else {
            //如果刚开始本地没有网络那么直接走离线规划
            routeRequestParam.routeConstrainCode = routeConstrainCode | RouteConstrainCode.RouteCalcLocal;
        }
        //车牌信息通过用户模块获取
        routeRequestParam.carPlate = AutoConstant.vehicleId;
        //设置车牌号
        int configAvoidLimit = SettingComponent.getInstance().getConfigKeyAvoidLimit();
        if (configAvoidLimit == 0) {
            //默认态
            routeRequestParam.openAvoidLimit = false;
        } else {
            //避开限行
            routeRequestParam.openAvoidLimit = true;
        }
        return routeRequestParam;
    }


    public static void clearElectricInfo() {
//        LayerController.getInstance().getDrivingLayer(SurfaceViewID.SurfaceViewIDMain).clearBizLabelTypeRoutePopEndArea();
    }


    /**
     * 绘制终点区域
     * endPoi 终点poi
     */
    public void drawEndPointArea(final POI endPoi, final RectInt rectInt) {
        //发起搜索终点区域数据，用于画终点区域
        /*todo
        TaskManager.post(new Runnable() {
            @Override
            public void run() {
                SearchRequestInfo mSearchRequestInfo = new SearchRequestInfo.Builder()
                        .setQueryType(SearchQueryType.ID)
                        .setPoi(POIFactory.createPOI("", endPoi.getPoint(), endPoi.getId()))
                        .build();
//                SearchController.getInstance().keywordSearch(mSearchRequestInfo, new SearchCallback<SearchKeywordResult>() {
//                    @Override
//                    public void onSuccess(SearchKeywordResult data) {
//                        super.onSuccess(data);
//                        LayerController.getInstance().getRouteResultLayer(SurfaceViewID.SurfaceViewIDMain).updateRouteEndAreaAndParentPoint(data,endPoi);
//                    }
//
//                    @Override
//                    public void onFailure(int errCode, String msg) {
//                        super.onFailure(errCode, msg);
//                    }
//                });
                SearchControllerV2.getInstance().poiIdSearch(mSearchRequestInfo, new SearchCallback<KeywordSearchResultV2>() {
                    @Override
                    public void onSuccess(KeywordSearchResultV2 data) {
                        super.onSuccess(data);
                        RouteResultLayer routeResultLayer = LayerController.getInstance().getRouteResultLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
                        if (routeResultLayer != null) {
                            routeResultLayer.updateRouteEndAreaAndParentPoint(data, endPoi,rectInt);
                        }
                    }

                    @Override
                    public void onFailure(int errCode, String msg) {
                        super.onFailure(errCode, msg);
                    }
                });
            }
        });*/
    }


    /**
     * 路线详情避开路段算路
     *
     * @param datas               避开路段数据
     * @param routeCarResult      缓存的算路结果
     * @param routeResultCallBack 算路监听回调
     */
    public long avoidRoute(ArrayList<NaviStationItemData> datas, RouteCarResultData routeCarResult, IRouteResultCallBack routeResultCallBack) {
        ArrayList<Long> links = getLongs(datas);
        POI startPoi = routeCarResult.getFromPOI();
        POI endPoi = routeCarResult.getToPOI();
        startPoi.setType(PointType.PointTypeStart);
        endPoi.setType(PointType.PointTypeEnd);
        RouteRequestParam routeRequestParam = new RouteRequestParam(startPoi, endPoi);
        routeRequestParam.invokerType = "plan";
        ArrayList<POI> midPois = routeCarResult.getMidPois();
        if (midPois != null && !midPois.isEmpty()) {
            for (POI misPoi : midPois) {
                misPoi.setType(PointType.PointTypeVia);
            }
            routeRequestParam.midPois = midPois;
        }
        routeRequestParam.routeStrategy = routeCarResult.getRouteStrategy();
        routeRequestParam.routeConstrainCode = routeCarResult.getRouteConstrainCode();
        routeRequestParam.openAvoidLimit = routeCarResult.isOpenAvoidLimit();
        routeRequestParam.carPlate = routeCarResult.getCarPlate();
        routeRequestParam.avoidLinks = links;
        return RouteRequestController.getInstance().requestRoute(routeRequestParam, routeResultCallBack);
    }

    private static @NonNull ArrayList<Long> getLongs(ArrayList<NaviStationItemData> datas) {
        ArrayList<Long> links = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            NaviStationItemData data = datas.get(i);
            List<NaviStationItemData.SubItem> subList = data.getSubList();
            if (subList != null && subList.size() > 0) {
                for (int j = 0; j < subList.size(); j++) {
                    ArrayList<Long> routeLinks = subList.get(j).getRouteLinks();
                    if (routeLinks == null) {
                        continue;
                    }
                    links.addAll(routeLinks);
                }
            }
        }
        return links;
    }

    /**
     * 将所有的link经纬度获取后进行矩形框预览
     *
     * @param itemData 当前选中路段信息
     * @param mapRect  矩形区域
     */
    public void onClickGroupItem(NaviStationItemData itemData, Rect mapRect) {
        if (itemData == null) {
            return;
        }
        LayerController.getInstance().getRouteResultLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).updateRouteDodgeLine(itemData.getIndex() - 1);
        ArrayList<Coord2DFloat> floatPoints = getGroupLinkPoints(itemData);
        if (floatPoints != null) {
            RectDouble rect = RectUtils.getBound(floatPoints);
            if (rect != null) {
                PreviewParam previewParam = new PreviewParam();
                previewParam.mapBound = rect;
                previewParam.leftOfMap = mapRect.left;
                previewParam.topOfMap = mapRect.top;
                previewParam.screenLeft = mapRect.left;
                previewParam.screenTop = mapRect.top;
                previewParam.screenRight = mapRect.right;
                previewParam.screenBottom = mapRect.bottom;
                SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).showPreview(previewParam, true, 500, -1);
            }
        }
    }

    /**
     * 获取link段数据
     *
     * @param itemData 当前选中路段信息
     * @return
     */
    private ArrayList<Coord2DFloat> getGroupLinkPoints(NaviStationItemData itemData) {
        List<NaviStationItemData.SubItem> subList = itemData.getSubList();
        if (subList != null && subList.size() > 0) {
            ArrayList<Coord2DFloat> points = new ArrayList<>();
            for (int i = 0; i < subList.size(); i++) {
                NaviStationItemData.SubItem subItem = subList.get(i);
                if (subItem == null) {
                    continue;
                }
                ArrayList<Coord2DInt32> coord2DInt32s = subItem.getRoutelinkPoints();
                if (coord2DInt32s == null) {
                    return points;
                }
                for (int j = 0; j < coord2DInt32s.size(); j++) {
                    Coord2DFloat coord2DFloat = new Coord2DFloat();
                    coord2DFloat.lon = (float) (coord2DInt32s.get(j).lon / 3600000.0);
                    coord2DFloat.lat = (float) (coord2DInt32s.get(j).lat / 3600000.0);
                    points.add(coord2DFloat);
                }
            }
            return points;
        }
        return null;
    }

    public RouteCarResultData getmRouteCarResultData() {
        return mRouteCarResultData;
    }

    public void setmRouteCarResultData(RouteCarResultData mRouteCarResultData) {
        this.mRouteCarResultData = mRouteCarResultData;
    }

    public void clearRouteCarResultData() {
        mRouteCarResultData = null;
    }
}
