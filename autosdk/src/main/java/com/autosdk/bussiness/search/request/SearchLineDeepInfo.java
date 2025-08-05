package com.autosdk.bussiness.search.request;

import com.autonavi.gbl.search.model.LineDeepQueryType;
import com.autosdk.bussiness.common.GeoPoint;

import java.util.ArrayList;

public class SearchLineDeepInfo {
    /**
     * 查询类型
     */
    private @LineDeepQueryType.LineDeepQueryType1
    int queryType;
    /**
     * 待查询poi列表，最多100个
     */
    private ArrayList<String> poiIds;

    public int getQueryType() {
        return queryType;
    }

    public void setQueryType(int queryType) {
        this.queryType = queryType;
    }

    public ArrayList<String> getPoiIds() {
        return poiIds;
    }

    public void setPoiIds(ArrayList<String> poiIds) {
        this.poiIds = poiIds;
    }
}
