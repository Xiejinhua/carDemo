package com.autosdk.bussiness.account.bean;

import java.io.Serializable;

/**
 * Created by AutoSdk on 2021/1/7.
 **/
public class TrackItemBean implements Serializable {
    private String id;
    private int type;
    private String trackPointsURL;
    private String trackFileName;
    private int updateTime;
    private int timeInterval;
    private int runDistance;
    private int timeTotal;
    private String averageSpeed;
    private double maxSpeed;
    private String trackFileMd5;
    private String startLocation;
    private String endLocation;
    private String data;
    private String startPoiName;
    private String endPoiName;
    private int rideRunType;

    public int getTimeTotal() {
        return timeTotal;
    }

    public void setTimeTotal(int timeTotal) {
        this.timeTotal = timeTotal;
    }

    public int getRideRunType() {
        return rideRunType;
    }

    public void setRideRunType(int rideRunType) {
        this.rideRunType = rideRunType;
    }

    public String getStartPoiName() {
        return startPoiName;
    }

    public String getEndPoiName() {
        return endPoiName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTrackPointsURL() {
        return trackPointsURL;
    }

    public void setTrackPointsURL(String trackPointsURL) {
        this.trackPointsURL = trackPointsURL;
    }

    public String getTrackFileName() {
        return trackFileName;
    }

    public void setTrackFileName(String trackFileName) {
        this.trackFileName = trackFileName;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public int getRunDistance() {
        return runDistance;
    }

    public void setRunDistance(int runDistance) {
        this.runDistance = runDistance;
    }

    public String getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(String averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public String getTrackFileMd5() {
        return trackFileMd5;
    }

    public void setTrackFileMd5(String trackFileMd5) {
        this.trackFileMd5 = trackFileMd5;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
