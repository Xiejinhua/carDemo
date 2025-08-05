package com.autosdk.bussiness.account.bean;

import com.autosdk.bussiness.common.GeoPoint;

/**
 * 停车上报需要的数据
 * @author AutoSDK
 */
public class LinkCarLocation {

    /**
     * 车位置
     */
    private GeoPoint carLoc;

    /**
     * 车牌号
     */
    private String plateNum = "";

    /**
     * 停车状态，1停车，0非停车
     */
    private int parkStatus;

    public GeoPoint getCarLoc() {
        return carLoc;
    }

    public void setCarLoc(GeoPoint carLoc) {
        this.carLoc = carLoc;
    }

    public String getPlateNum() {
        return plateNum;
    }

    public void setPlateNum(String plateNum) {
        this.plateNum = plateNum;
    }

    public int getParkStatus() {
        return parkStatus;
    }

    public void setParkStatus(int parkStatus) {
        this.parkStatus = parkStatus;
    }
}
