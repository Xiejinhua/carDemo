package com.autosdk.bussiness.navi.route.model;

import com.autosdk.bussiness.common.POI;

import java.util.List;

/**
 * @author AutoSDK
 */
public class CarScenData {

    private String name;

    private int carscenType;

    private List<POI> childList;

    private POI poi;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCarscenType() {
        return carscenType;
    }

    public void setCarscenType(int carscenType) {
        this.carscenType = carscenType;
    }

    public List<POI> getChildList() {
        return childList;
    }

    public void setChildList(List<POI> childList) {
        this.childList = childList;
    }

    public POI getPoi() {
        return poi;
    }

    public void setPoi(POI poi) {
        this.poi = poi;
    }
}
