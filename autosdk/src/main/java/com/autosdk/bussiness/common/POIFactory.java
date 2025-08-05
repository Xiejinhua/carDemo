package com.autosdk.bussiness.common;

public class POIFactory {
    /**
     * 创建一个基础poi
     *
     * @param name  poi名称
     * @param point 坐标信息
     * @return
     */
    public static POI createPOI(String name, GeoPoint point) {
        POI poi = new POI();
        poi.setPoint(point);
        poi.setName(name);
        return poi;
    }

    /**
     * 创建一个基础poi
     *
     * @param name  poi名称
     * @param point 坐标信息
     * @return
     */
    public static POI createPOI(String name, String address, GeoPoint point) {
        POI poi = new POI();
        poi.setPoint(point);
        poi.setName(name);
        poi.setAddr(address);
        return poi;
    }

    /**
     * 创建一个基础poi
     *
     * @param name  poi名称
     * @param point 坐标信息
     * @param id    poiid
     * @return
     */
    public static POI createPOI(String name, GeoPoint point, String id) {
        POI poi = new POI();
        poi.setPoint(point);
        poi.setName(name);
        poi.setId(id);
        return poi;
    }

    /**
     * 创建一个基础poi
     *
     * @param name  poi名称
     * @param point 坐标信息
     * @param id    poiid
     * @return
     */
    public static POI createPOI(String name, String address, GeoPoint point, String id) {
        POI poi = new POI();
        poi.setPoint(point);
        poi.setName(name);
        poi.setAddr(address);
        poi.setId(id);
        return poi;
    }

    /**
     * 创建一个基础POI，不含名称和坐标信息
     *
     * @return
     */
    public static POI createPOI() {
        POI poi = new POI();
        GeoPoint point = new GeoPoint();
        poi.setPoint(point);
        return poi;
    }

}
