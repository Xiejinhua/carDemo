package com.autosdk.bussiness.layer.observer;

import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.common.path.model.AvoidJamCloudControl;
import com.autonavi.gbl.common.path.model.ForbiddenCloudControl;
import com.autosdk.bussiness.common.POI;

public interface IBusinessLayerClickObserver {
    /**
     * 终点区域面子点点击事件
     */
    default void onNotifyEndAreaChildClick(POI poi) {}

    /**
     * 子点标签去这里点击事件
     */
    default void onNotifyPopEndAreaClick(POI poi) {}

    /**
     * 途径路点击事件
     */
    default void onNotifyViaRoadClick(int index) {}

    /**
     * 天气 扎点点击回调
     *
     * @param index 途经点下标
     */
    default void onNotifyWeatherPointClick(int index) {}

    /**
     * 路径点击回调
     */
    default void onRouteClick(int index) {}

    /**
     * 交通事件监听
     *
     * @param eventID 交通事件id
     */
    default void onTrafficEventClick(long eventID, Coord3DDouble position) {}

    /**
     * 拥堵事件图层
     * @param avoidJamCloudControl
     */
    default void onRouteJamPointClick(AvoidJamCloudControl avoidJamCloudControl){}

    /**
     * 禁行事件图层
     */
    default void onForbiddenDetailClick(ForbiddenCloudControl forbiddenCloudControl) {}


    /**
     * 途经点 扎点点击回调
     *
     * @param index 途经点下标
     */
    default void onWayPointClick(int index) {}

    /**
     * focus态途经点 扎点点击回调
     */
    default void onDeleteFocusClick(int index) {}

    /**
     * 沿途搜POI 扎点点击事件回调
     *  @param type POI的类型，分为默认和服务区，RouteResultLayer.POINT_TYPE_ALONG_WAY_DEFAULT 和 RouteResultLayer.POINT_TYPE_ALONG_WAY_REST_AREA
     * @param id   POI唯一识别码
     */
    default void onPointClick(int type, String id) {}
    default void onTipAddClick(String id) {}

    /**
     * 点击接续算路充电站气泡
     * @param index
     */
    default void onNotifyViaEtaClick(int index) {}

    /**
     * 点击接续算路充电站扎点
     */
    default void onNotifyViaChargeStationClick(int index) {}
}
