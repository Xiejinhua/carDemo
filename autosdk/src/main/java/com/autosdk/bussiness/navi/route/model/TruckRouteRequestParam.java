package com.autosdk.bussiness.navi.route.model;

import com.autosdk.bussiness.common.POI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 货车算路类型（460 sdk不支持，490后开始支持）
 */
public class TruckRouteRequestParam extends RouteRequestParam{

    /**
     * 货车高度，浮点数，单位：米
     * 0~10（最多保留小数点后面1位）
     * 必填
     */
    public float vehicleHeight;

    /**
     * 货车最大载重，浮点数，单位：吨
     * 必填
     * 0~100（最多保留小数点后面3位）
     */
    public float vehicleLoad;

    /**
     * 货车宽度，浮点数，单位：米
     * 0~5（最多保留小数点后面1位）
     * 必填
     */
    public float vehicleWidth;

    /**
     * 货车长度，浮点数，单位：米
     * 0~25（最多保留小数点后面1位）
     * 必填
     */
    public float vehicleLength;

    /**
     * 核定货车载重，浮点数，单位：吨
     * 0~100（最多保留小数点后面3位）
     * 必填
     */
    public float vehicleWeight;

    /**
     * 货车大小
     * 1：微型车 2：轻型车(默认) 3：中型车 4：重型车
     * 必填
     */
    public int vehicleSize = 2;

    /**
     * 轴数
     * 1轴、2轴、3轴、4轴、5轴、6轴、6轴以上
     */
    public int vehicleAxis;

    /**
     * 重量是否参与算路，默认参与
     */
    public boolean vehicleLoadSwitch = true;

    public TruckRouteRequestParam(POI startPOI, POI endPOI) {
        super(startPOI, endPOI);
    }

    public TruckRouteRequestParam(POI startPOI, POI endPOI, ArrayList<POI> midPOIs) {
        super(startPOI, endPOI, midPOIs);
    }

    @Override
    public int getVehicleType() {
        return VEHICLE_TYPE_TRUCK;
    }

    /**
     * 货车信息(JSON) 例：{"height":"0.5","load":"0.5","width":"0.5","length":"0.5","weight":"0.5","size":"1","axis":"1","loadswitch":"1"}
     * @return
     */
    public String getRouteControlKeyTrukInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("height", vehicleHeight);
            jsonObject.put("load", vehicleLoad);
            jsonObject.put("width", vehicleWidth);
            jsonObject.put("length", vehicleLength);
            jsonObject.put("weight", vehicleWeight);
            jsonObject.put("size", vehicleSize);
            jsonObject.put("axis", vehicleAxis);
            jsonObject.put("loadswitch", 1);
            jsonObject.put("vehicleFlag", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
