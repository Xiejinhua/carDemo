package com.autosdk.bussiness.widget.route.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 路线页面卡片路线简单信息，用于卡片展示，包含红绿灯数等信息
 */
public class RoutePathItemContent {

    private String title;

    /**
     * 路线偏好
     */
    private String content;

    /**
     * 路线所需时间
     */
    private long travelTime;

    /**
     * 路线总长度
     */
    private long length;

    /**
     * 红绿灯个数
     */
    private long trafficLightCount;

    /**
     * 费用
     */
    private long tollCost;

    /**
     * 到达剩余充电量
     * 剩余电量【单位：0.01瓦时】-1代表不可到达。 按行程点排序。途经1剩余电量，途经地2剩余电量....目的地剩余电量
     */
    private ArrayList<Integer> vehiclechargeleft;

    //到达目的剩余电量百分比
    private String destRemainBatteryPercent;

    private int streetNamesSize;
    private List<String> streetNames;

    //是否是电动车导航
    private boolean isElecRoute;

    private boolean isSelected;

    private boolean showBottomSpline;

    public String getDestRemainBatteryPercent() {
        return destRemainBatteryPercent;
    }

    public void setDestRemainBatteryPercent(String destRemainBatteryPercent) {
        this.destRemainBatteryPercent = destRemainBatteryPercent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(long travelTime) {
        this.travelTime = travelTime;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long lenght) {
        this.length = lenght;
    }

    public long getTrafficLightCount() {
        return trafficLightCount;
    }

    public void setTrafficLightCount(long trafficLightCount) {
        this.trafficLightCount = trafficLightCount;
    }

    public long getTollCost() {
        return tollCost;
    }

    public void setTollCost(long tollCost) {
        this.tollCost = tollCost;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isShowBottomSpline() {
        return showBottomSpline;
    }

    public void setShowBottomSpline(boolean showBottomSpline) {
        this.showBottomSpline = showBottomSpline;
    }

    public ArrayList<Integer> getVehiclechargeleft() {
        return vehiclechargeleft;
    }

    public void setVehiclechargeleft(ArrayList<Integer> vehiclechargeleft) {
        this.vehiclechargeleft = vehiclechargeleft;
    }

    public boolean isElecRoute() {
        return isElecRoute;
    }

    public void setElecRoute(boolean elecRoute) {
        isElecRoute = elecRoute;
    }

    public int getStreetNamesSize() {
        return streetNamesSize;
    }

    public void setStreetNamesSize(int streetNamesSize) {
        this.streetNamesSize = streetNamesSize;
    }

    public List<String> getStreetNames() {
        return streetNames;
    }

    public void setStreetNames(List<String> streetNames) {
        this.streetNames = streetNames;
    }
}
