package com.autosdk.bussiness.navi.route.model;


import com.autosdk.bussiness.common.POI;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 路线类的结果集接口
 *
 */
public interface IRouteResultData extends Serializable {
    /**
     * 取起点poi
     *
     * @return 起点poi对象
     */
    public POI getFromPOI();

    /**
     * 设置起点poi
     *
     * @param fromPOI poi对象
     */
    public void setFromPOI(POI fromPOI);

    /**
     * 取终点poi
     *
     * @return
     */
    public POI getToPOI();

    /**
     * 设置终点poi
     *
     * @param toPOI poi对象
     */
    public void setToPOI(POI toPOI);

    /**
     * 多途经点支持（取途经点信息）
     *
     * @return
     */
    public ArrayList<POI> getMidPois();

    /**
     * 多途经点支持（设置途经点信息）
     *
     * @param pois
     */
    public void setMidPois(ArrayList<POI> pois);

    boolean hasMidPos();

    public int getRouteStrategy();

    public void setRouteStrategy(int m);

    public int getRouteConstrainCode();

    public void setRouteConstrainCode(int m);

    public boolean isOffline();

    public void setIsOffline(boolean isOffline);

    /**
     * 是否是场景化请求结果
     *
     * @return
     */
    boolean isSceneResult();

    /**
     * 设置场景化请求结果标识
     *
     * @param isCarScene
     */
    void setSceneResult(boolean isCarScene);

}
