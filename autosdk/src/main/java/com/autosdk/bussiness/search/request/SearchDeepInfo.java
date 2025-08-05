package com.autosdk.bussiness.search.request;

import com.autosdk.bussiness.common.GeoPoint;

public class SearchDeepInfo {
    private GeoPoint mGeoPoint;
    /**
     * poi点的唯一标识
     */
    private String poiid;

    public GeoPoint getGeoPoint() {
        return mGeoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        mGeoPoint = geoPoint;
    }

    public String getPoiid() {
        return poiid;
    }

    public void setPoiid(String poiid) {
        this.poiid = poiid;
    }
}
