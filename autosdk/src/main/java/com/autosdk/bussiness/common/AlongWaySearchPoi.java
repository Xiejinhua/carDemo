package com.autosdk.bussiness.common;

import com.autonavi.gbl.search.model.PricePoiInfo;

import java.util.ArrayList;
/**
 *@author AutoSDk
 */
public class AlongWaySearchPoi extends POI {

    private int labelType;

    private String travelTime;

    private ArrayList<PricePoiInfo> priceList;

    private int distToVia;

    private int etaToVia;

    private String vehicleChargeLeft;

    private int toll;

    private int viaLevel;

    private String brandDesc;

    /**
     * 设置标签类型
     * @return
     */
    public int getLabelType() {
        return labelType;
    }

    /**
     * 设置标签
     * @param labelType
     */
    public void setLabelType(int labelType) {
        this.labelType = labelType;
    }

    /**
     * 经过沿途搜点到达终点的路线总旅行时间，单位：秒
     * @return
     */
    public String getTravelTime() {
        return travelTime;
    }

    /**
     * 经过沿途搜点到达终点的路线总旅行时间，单位：秒
     * @param travelTime
     */
    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }

    /**
     * 设置加油站价格信息
     * @param priceList
     */
    public void setPriceInfos(ArrayList<PricePoiInfo> priceList) {
        this.priceList = priceList;
    }

    /**
     * 获取加油站价格信息
     * @return
     */
    public ArrayList<PricePoiInfo> getPriceInfos() {
        return priceList;
    }

    /**
     * 设置到达沿途搜点的路线距离，单位:米
     * @param distToVia 单位:米
     */
    public void setDistToVia(int distToVia) {
        this.distToVia = distToVia;
    }

    /**
     * 获取到达沿途搜点的旅行时间，单位：秒
     * @return 单位:秒
     */
    public int getDistToVia() {
        return distToVia;
    }

    /**
     * 获取到达沿途搜点的路线距离
     * @return 单位:米
     */
    public int getEtaToVia() {
        return etaToVia;
    }

    /**
     * 设置到达沿途搜点的旅行时间，单位：秒
     * @param etaToVia 单位:秒
     */
    public void setEtaToVia(int etaToVia) {
        this.etaToVia = etaToVia;
    }

    /**
     * 针对新能源车辆，返回到达第一个via的剩余电量，单位：百分之一wh；当剩余电量计算结果小于0时，返回-1
     * @param vehicleChargeLeft
     */
    public void setVehiclechargeleft(String vehicleChargeLeft) {
        this.vehicleChargeLeft = vehicleChargeLeft;
    }

    /**
     * 针对新能源车辆，返回到达第一个via的剩余电量，单位：百分之一wh；当剩余电量计算结果小于0时，返回-1
     * @return
     */
    public String getVehiclechargeleft() {
        return vehicleChargeLeft;
    }

    /**
     * 经过沿途搜点的总花费，单位：元
     * @param toll
     */
    public void setToll(int toll) {
        this.toll = toll;
    }

    /**
     * 经过沿途搜点的总花费，单位：元
     * @return
     */
    public int getToll() {
        return toll;
    }

    /**
     * 沿途搜点的等级标记, 0:无等级, 1:优质点, 2:普通点, 3:垃圾点
     * @param viaLevel
     */
    public void setViaLevel(int viaLevel) {
        this.viaLevel = viaLevel;
    }

    /**
     * 沿途搜点的等级标记, 0:无等级, 1:优质点, 2:普通点, 3:垃圾点
     * @return
     */
    public int getViaLevel() {
        return viaLevel;
    }

    /**
     * 充电站品牌
     * @param brandDesc
     */
    public void setBrandDesc(String brandDesc) {
        this.brandDesc = brandDesc;
    }

    /**
     * 充电站品牌
     * @return
     */
    public String getBrandDesc() {
        return brandDesc;
    }
}
