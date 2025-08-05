package com.autosdk.bussiness.search.bean;

import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.search.request.SearchPoiBizType;

import java.io.Serializable;
/**
 *  用于存放搜索商品详情以及商品详情相关功能所需数据的类
 */
public class SearchProductInfoRequestBean implements Serializable{

    private String skuId;
    private String spuId;
    private String poiId;
    private String adCode;
    private String industry;
    private GeoPoint geoPoint;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    /**
     * 搜索结果所表示的业务类型, 由调用方定义
     * 具体值参考 {@link SearchPoiBizType}, 可多种组合
     */
    private int bizType = SearchPoiBizType.NORMAL;

    @SearchPoiBizType
    public int getBizType() {
        return bizType;
    }


    public void setBizType(@SearchPoiBizType int bizType) {
        this.bizType = bizType;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }

    public String getSpuId() {
        return spuId;
    }

    public void setSpuId(String spuId) {
        this.spuId = spuId;
    }

    public String getPoiId() {
        return poiId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
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
