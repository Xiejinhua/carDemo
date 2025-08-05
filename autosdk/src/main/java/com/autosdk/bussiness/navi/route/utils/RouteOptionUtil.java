package com.autosdk.bussiness.navi.route.utils;

import android.text.TextUtils;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.ElecInfoConfig;
import com.autonavi.gbl.common.path.model.ChargingArgumentsInfo;
import com.autonavi.gbl.common.path.model.POIInfo;
import com.autonavi.gbl.common.path.model.PointType;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.POIFactory;
import com.autosdk.bussiness.common.utils.ElectricInfoConverter;
import com.autosdk.bussiness.search.utils.SearchPoiUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 路线参数工具类，负责算路参数类型转化
 */
public class RouteOptionUtil {

    /**
     * POI转POIInfo
     * @param poi
     * @param type 用于区分是起点还是转终点, 0：起点   1：途经点  2: 终点
     * @return
     */
    public static POIInfo poiToPOIInfo(POI poi, @PointType.PointType1 int type) {
        POIInfo poiInfo = new POIInfo();
        if (poi != null) {
            poiInfo.name = poi.getName();
            if ((type == PointType.PointTypeEnd || PointType.PointTypeVia == 1) && poi.getType() != 1) {
                // POI起终点位置类型为移图，导航到某个坐标点就不要传poiID
                poiInfo.poiID = poi.getId();
            }
            poiInfo.type = poi.getType();
            poiInfo.parentRel = String.valueOf(poi.getChildType());
            poiInfo.parentID = poi.getParent();
            poiInfo.typeCode = poi.getTypeCode();
            poiInfo.realPos = new Coord2DDouble(poi.getPoint().getLongitude(), poi.getPoint().getLatitude());
            if (type == PointType.PointTypeStart) {
                //起点需要判断是否有出口坐标，如果有优先使用出口坐标
                List<GeoPoint> exitList = poi.getExitList();
                if (exitList != null && exitList.size() > 0) {
                    // TODO: 2020/8/7 BL接口设计只支持一个导航坐标，先暂时取第一个坐标
                    poiInfo.naviPos = new Coord2DDouble(exitList.get(0).getLongitude(), exitList.get(0).getLatitude());
                }
            } else if (type == PointType.PointTypeEnd) {
                //到达点坐标
                List<GeoPoint> entranceList = poi.getEntranceList();
                if (entranceList != null && entranceList.size() > 0) {
                    // TODO: 2020/8/7 BL接口设计只支持一个导航坐标，先暂时取第一个坐标
                    poiInfo.naviPos = new Coord2DDouble(entranceList.get(0).getLongitude(), entranceList.get(0).getLatitude());
                }

            }else {
                ElecInfoConfig elecInfoConfig = ElectricInfoConverter.getElecInfoConfig();
                // 如果是接续算路的充电站,添加充电参数
                if (ElectricInfoConverter.isElectric()
                        && elecInfoConfig != null
                        && SearchPoiUtils.IsChargingStation(poi)) {
                    poiInfo.chargeInfo = new ChargingArgumentsInfo();
                    poiInfo.chargeInfo.type = 1;
                    if (poi.getChargeStationInfo() != null
                            && poi.getChargeStationInfo().plugsInfo != null
                            && !poi.getChargeStationInfo().plugsInfo.isEmpty()) {
                        poiInfo.chargeInfo.power = poi.getChargeStationInfo().plugsInfo.get(0).fastPower;
                        poiInfo.chargeInfo.voltage = poi.getChargeStationInfo().plugsInfo.get(0).fastVoltage;
                        poiInfo.chargeInfo.amperage = poi.getChargeStationInfo().plugsInfo.get(0).fastCurrent;
                    }
                    poiInfo.chargeInfo.minArrivalPercent = (short) elecInfoConfig.leavingPercent;
                }
                if (type == PointType.PointTypeVia) {
                    poiInfo.retainParam = poi.getPathRestorationInfo();
                }
            }
        }

        return poiInfo;
    }

    /**
     * POIInfo 转POI
     * @param poiInfo
     * @return
     */
    public static POI poiInfoToPOI(POIInfo poiInfo,@PointType.PointType1 int type) {
        POI poi = POIFactory.createPOI();
        if(poiInfo == null) {
            return poi;
        }
        poi.setName(poiInfo.name);
        poi.setId(poiInfo.poiID);
        poi.setType(poiInfo.type);
        if(!TextUtils.isEmpty(poiInfo.parentRel)){
            poi.setChildType(Integer.valueOf(poiInfo.parentRel));
        }
        poi.setParent(poiInfo.parentID);
        poi.setTypeCode(poiInfo.typeCode);
        GeoPoint point = new GeoPoint(poiInfo.realPos.lon, poiInfo.realPos.lat);
        poi.setPoint(point);
        Coord2DDouble naviPos =poiInfo.naviPos;
        ArrayList<GeoPoint> exitList = new ArrayList<>();
        GeoPoint exitPoint = new GeoPoint(naviPos.lon, naviPos.lat);
        exitList.add(exitPoint);
        if(type==PointType.PointTypeStart){
            poi.setExitList(exitList);
        }else if(type==PointType.PointTypeEnd) {
            poi.setEntranceList(exitList);
        }
        return poi;
    }
}
