package com.autosdk.bussiness.common;

import java.util.ArrayList;

import com.autosdk.bussiness.navi.route.model.PathResultDataInfo;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;
import com.google.gson.Gson;

/**
 * 自定义多屏数据传输
 * 只需要坐标数据
 * @author AutoSDK
 */
public class ConsisUserData {

    private POI mStartPoi;

    private POI mEndPoi;

    private ArrayList<POI> mViaPois;

    private PathResultDataInfo mPathResultDataInfo;


    public byte[] buildData(RouteCarResultData resultData){
        if (resultData == null || resultData.getToPOI() == null || resultData.getFromPOI() == null) {
            return null;
        }
        POI fromPoi = resultData.getFromPOI();
        mStartPoi = new POI();
        mStartPoi.setId(fromPoi.getId());
        mStartPoi.setPoint(fromPoi.getPoint());
        mStartPoi.setName(fromPoi.getName());

        POI toPoi = resultData.getToPOI();
        mEndPoi = new POI();
        mEndPoi.setId(toPoi.getId());
        mEndPoi.setPoint(toPoi.getPoint());
        mEndPoi.setName(toPoi.getName());
        mEndPoi.setAddr(toPoi.getAddr());
        mViaPois = new ArrayList<>();
        ArrayList<POI> midPois = resultData.getMidPois();
        if (midPois != null) {
            for (POI next : midPois) {
                POI via = new POI();
                via.setPoint(next.getPoint());
                via.setId(next.getId());
                via.setName(next.getName());
                mViaPois.add(via);
            }
        }
        mPathResultDataInfo=resultData.getPathResultDataInfo();
        return new Gson().toJson(this).getBytes();
    }

    public RouteCarResultData restore(byte[] userData){
        if (userData == null || userData.length == 0) {
            return null;
        }
        RouteCarResultData routeCarResultData = new RouteCarResultData();
        Gson gson = new Gson();
        ConsisUserData consisUserData = gson.fromJson(new String(userData), this.getClass());
        routeCarResultData.setToPOI(consisUserData.mEndPoi);
        routeCarResultData.setFromPOI(consisUserData.mStartPoi);
        routeCarResultData.setMidPois(consisUserData.mViaPois);
        routeCarResultData.setPathResultDataInfo(consisUserData.mPathResultDataInfo);
        return routeCarResultData;
    }
}
