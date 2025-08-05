package com.autosdk.bussiness.navi.route.model;


import com.autonavi.gbl.common.path.option.RouteConstrainCode;
import com.autonavi.gbl.common.path.option.RouteStrategy;
import com.autosdk.bussiness.common.POI;

import java.util.ArrayList;

/**
 * 路线规划请求参数
 */
public class RouteRequestParam {

    /**
     * 车辆类型，默认0，燃油小客车
     */
    public static final int VEHICLE_TYPE_DEFAULT = 0;

    /**
     * 车辆类型，燃油货车
     */
    public static final int VEHICLE_TYPE_TRUCK = 1;
    /**
     * 车辆类型，电动客车
     */
    public static final int VEHICLE_TYPE_ELC_BUS = 2;
    //车辆类型
    public int type = 0;

    /**
     * 起点POI【必选】
     */
    public POI startPOI;
    /**
     * 终点POI【必选】
     */
    public POI endPOI;

    /**
     * 途经点（支持多个，可选）
     */
    public ArrayList<POI> midPois;

    /**
     * 路线偏好
     * {@link com.autonavi.gbl.common.path.option.RouteStrategy}
     */
    public int routeStrategy = RouteStrategy.RequestRouteTypeMostly;

    /**
     * 算路附加要求
     * {@link com.autonavi.gbl.common.path.option.RouteConstrainCode}
     */
    public int routeConstrainCode = RouteConstrainCode.RouteCalcMulti | RouteConstrainCode.RouteNetWorking;;

    /**
     * 计算路线条数，默认3条
     */
    public int routeCalcNumber = 3;

    /**
     * 车牌号（可选）
     */
    public String carPlate;

    /**
     * 是否多备选算路请求
     */
    public boolean isAlternative;

    /**
     * 车牌开关标识(配合车牌号使用)
     */
    public int contentoptions;

    /**
     * 用于标识是否是驾车场景化请求
     */
    public boolean isCarSceneRequest = false;

    /**
     * 是否避开限行
     */
    public boolean openAvoidLimit = false;

    /**
     * 避让路段
     */
    public ArrayList<Long> avoidLinks;

    /**
     * 路线规划类别 导航 navi 算路plan
     */
    public String invokerType;
    /**
     * 构造方法
     * @param startPOI 起点（必填）
     * @param endPOI   终点（必填）
     * @param midPOIs  途经点（选填）
     */
    public RouteRequestParam(POI startPOI, POI endPOI, ArrayList<POI> midPOIs) {
        this.startPOI = startPOI;
        this.endPOI = endPOI;
        this.midPois = midPOIs;
    }

    public RouteRequestParam(POI startPOI, POI endPOI) {
        this.startPOI = startPOI;
        this.endPOI = endPOI;
    }

    /**
     * 车辆类型，0客车，1货车, 2电动客车，3电动货车，4插电式混动客车，5插电式混动货车
     * @return
     */
    public int getVehicleType() {
        return type;
    }

    public int setVehicleType(int mType) {
        return type = mType;
    }
}
