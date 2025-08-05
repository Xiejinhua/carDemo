package com.autosdk.bussiness.common;

import com.autonavi.gbl.search.model.LinePoiServiceAreaChild;

import java.util.ArrayList;
/**
 *@author AutoSDk
 */
public class AlongWayPoiDeepInfo extends POI {

    private ArrayList<LinePoiServiceAreaChild> childrens;

    private String travelTime;

    /**
     * 关闭状态，0-非建设中（默认值），1-建设中，2-未调查，3-装修中，4-暂停营业
     */
    private int building;

    /**
     * 星级,例如：“五星”、“四星”
     */
    private String serviceStar;

    /**
     * 品牌入驻，多品牌按英文半角“,”分隔。例：“肯德基,麦当劳”
     */
    private String 	brand;

    /**
     * 服务区包含的子Poi; 终点推荐Poi
     * @param childrens
     */
    public void setLineChildPois(ArrayList<LinePoiServiceAreaChild> childrens) {
        this.childrens = childrens;
    }

    /**
     * 服务区包含的子Poi; 终点推荐Poi
     * @return
     */
    public ArrayList<LinePoiServiceAreaChild> getLineChildPois() {
        return childrens;
    }

    /**
     * 经过沿途搜点到达终点的路线总旅行时间，单位：秒
     * @return
     */
    public String getTravelTime() {
        return travelTime;
    }

    /**
     * 经过沿途搜点到达终点的路线总旅行时间，单位：秒
     * @param travelTime
     */
    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }

    /**
     * 获取关闭状态，0-非建设中（默认值），1-建设中，2-未调查，3-装修中，4-暂停营业
     * @return
     */
    public int getBuilding() {
        return building;
    }

    /**
     * 设置关闭状态，0-非建设中（默认值），1-建设中，2-未调查，3-装修中，4-暂停营业
     * @param building
     */
    public void setBuilding(int building) {
        this.building = building;
    }

    /**
     * 获取星级,例如：“五星”、“四星”
     * @return
     */
    public String getServiceStar() {
        return serviceStar;
    }

    /**
     * 设置星级,例如：“五星”、“四星”
     * @param serviceStar
     */
    public void setServiceStar(String serviceStar) {
        this.serviceStar = serviceStar;
    }

    /**
     * 获取品牌入驻，多品牌按英文半角“,”分隔。例：“肯德基,麦当劳”
     * @return
     */
    public String getBrand() {
        return brand;
    }

    /**
     * 设置品牌入驻，多品牌按英文半角“,”分隔。例：“肯德基,麦当劳”
     * @param brand
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }
}
