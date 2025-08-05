package com.desaysv.psmap.adapter.tbt.bean;

import java.io.Serializable;

/**
 * @author 谢锦华
 * @time 2025/2/26
 * @description
 */

public class CityItemInfo implements Serializable {
    public int belongedProvince;
    public double cityX;
    public double cityY;
    public String cityName;
    public int cityLevel;
    public int cityAdcode;
    public String initial;
    public String pinyin;

    public CityItemInfo() {
        this.belongedProvince = 0;
        this.cityX = 0.0;
        this.cityY = 0.0;
        this.cityName = "";
        this.cityLevel = 0;
        this.cityAdcode = 0;
        this.initial = "";
        this.pinyin = "";
    }

    public CityItemInfo(int belongedProvinceLiteObj, double cityXLiteObj, double cityYLiteObj, String cityNameLiteObj, int cityLevelLiteObj, int cityAdcodeLiteObj, String initialLiteObj, String pinyinLiteObj) {
        this.belongedProvince = belongedProvinceLiteObj;
        this.cityX = cityXLiteObj;
        this.cityY = cityYLiteObj;
        this.cityName = cityNameLiteObj;
        this.cityLevel = cityLevelLiteObj;
        this.cityAdcode = cityAdcodeLiteObj;
        this.initial = initialLiteObj;
        this.pinyin = pinyinLiteObj;
    }
}