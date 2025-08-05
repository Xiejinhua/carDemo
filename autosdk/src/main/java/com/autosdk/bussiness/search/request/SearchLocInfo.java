package com.autosdk.bussiness.search.request;

public class SearchLocInfo {
    // 用户(车标)所在位置经纬度
    public double lon;
    public double lat;

    // 地图中心经纬度
    public double mapCenterLon;
    public double mapCenterLat;

    // 当前城市名, 如: 北京
    public String cityName;

    // 城市adcode, 如: 110000
    public int adcode;
}
