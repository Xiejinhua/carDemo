package com.autosdk.bussiness.search.bean;

import com.autonavi.gbl.search.model.PoiDetailProductInfo;

import java.io.Serializable;

public class SearchProductInfoDetailBean implements Serializable {

    public String poiId;
    public PoiDetailProductBean poiDetailProductInfo;
    public String adCode;
    public String industry;

    public SearchProductInfoDetailBean(String poiId, PoiDetailProductBean poiDetailProductInfo) {
        this.poiId = poiId;
        this.poiDetailProductInfo = poiDetailProductInfo;
    }

    public String getPoiId() {
        return poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public PoiDetailProductBean getPoiDetailProductInfo() {
        return poiDetailProductInfo;
    }

    public void setPoiDetailProductInfo(PoiDetailProductBean poiDetailProductInfo) {
        this.poiDetailProductInfo = poiDetailProductInfo;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }
}
