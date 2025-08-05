package com.autosdk.bussiness.widget.route.utils;

import com.autonavi.gbl.common.model.Coord2DFloat;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.map.model.PointD;
import com.autonavi.gbl.user.usertrack.model.GpsTrackPoint;
import com.autosdk.bussiness.common.POI;

import java.util.List;

/**
 * 矩形工具
 */
public class RectUtils {

    /**
     * @function  rectUnion
     * @brief 合并矩形框
     * @param rectOrig
     * @param rectOrig2
     * @return RectDouble
     * **/
    public static RectDouble rectUnion(RectDouble rectOrig, RectDouble rectOrig2) {
        if(rectOrig == null && rectOrig2 == null){
            return null;
        } else if(rectOrig == null){
            return rectOrig2;
        } else if(rectOrig2 == null){
            return rectOrig;
        }

        RectDouble destRect = new RectDouble();
        RectDouble srcRect1 = new RectDouble(rectOrig.left, rectOrig.top, rectOrig.right, rectOrig.bottom);
        RectDouble srcRect2 = new RectDouble(rectOrig2.left, rectOrig2.top, rectOrig2.right, rectOrig2.bottom);
        double tmp = 0.0d;
        if(srcRect1.left > srcRect1.right) {
            tmp = srcRect1.left;
            srcRect1.left = srcRect1.right;
            srcRect1.right = tmp;
        }

        if(srcRect1.top < srcRect1.bottom) {
            tmp = srcRect1.top;
            srcRect1.top = srcRect1.bottom;
            srcRect1.bottom = tmp;
        }

        if(srcRect2.left > srcRect2.right) {
            tmp = srcRect2.left;
            srcRect2.left = srcRect2.right;
            srcRect2.right = tmp;
        }

        if(srcRect2.top < srcRect2.bottom) {
            tmp = srcRect2.top;
            srcRect2.top = srcRect2.bottom;
            srcRect2.bottom = tmp;
        }

        destRect.left = rectOrig.left > rectOrig2.left ? rectOrig2.left : rectOrig.left;
        destRect.top = rectOrig.top < rectOrig2.top ? rectOrig2.top : rectOrig.top;
        destRect.right = rectOrig.right > rectOrig2.right ?  rectOrig.right : rectOrig2.right;
        destRect.bottom = rectOrig.bottom < rectOrig2.bottom ? rectOrig.bottom : rectOrig2.bottom;

        return destRect;
    }

    /**
     * 计算沿途搜索结果所有的Bound
     * @param pois
     * @return
     */
    public static RectDouble getSearchAlongBound(List<POI> pois){
        if (pois.size() == 0) {
            return null;
        }
        try {
            double x1 = Double.MAX_VALUE;
            double y1 = Double.MAX_VALUE;
            double x2 = Double.MIN_VALUE;
            double y2 = Double.MIN_VALUE;
            for (int i = 0; i < pois.size(); i++) {
                POI oItem = pois.get(i);
                x1 = Math.min(x1, oItem.getPoint().getLongitude());
                y1 = Math.min(y1, oItem.getPoint().getLatitude());
                x2 = Math.max(x2, oItem.getPoint().getLongitude());
                y2 = Math.max(y2, oItem.getPoint().getLatitude());
            }
            RectDouble rect = new RectDouble(x1,x2,y2,y1);
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算组队队员的Bound
     *
     * @param pointDs
     * @return
     */
    public static RectDouble getGroupMemberBound(List<PointD> pointDs) {
        if (pointDs.size() == 0) {
            return null;
        }
        try {
            double x1 = Double.MAX_VALUE;
            double y1 = Double.MAX_VALUE;
            double x2 = Double.MIN_VALUE;
            double y2 = Double.MIN_VALUE;
            for (int i = 0; i < pointDs.size(); i++) {
                PointD oItem = pointDs.get(i);
                x1 = Math.min(x1, oItem.x);
                y1 = Math.min(y1, oItem.y);
                x2 = Math.max(x2, oItem.x);
                y2 = Math.max(y2, oItem.y);
            }
            RectDouble rect = new RectDouble(x1, x2, y2, y1);
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据经纬度计算矩形框
     * @param geoPoints
     * @return
     */
    public static RectDouble getBound(List<Coord2DFloat> geoPoints){
        if (geoPoints.size() == 0) {
            return null;
        }
        try {
            double x1 = Double.MAX_VALUE;
            double y1 = Double.MAX_VALUE;
            double x2 = Double.MIN_VALUE;
            double y2 = Double.MIN_VALUE;
            for (int i = 0; i < geoPoints.size(); i++) {
                Coord2DFloat oItem = geoPoints.get(i);
                x1 = Math.min(x1, oItem.lon);
                y1 = Math.min(y1, oItem.lat);
                x2 = Math.max(x2, oItem.lon);
                y2 = Math.max(y2, oItem.lat);
            }
            RectDouble rect = new RectDouble(x1,x2,y2,y1);
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算沿途搜索结果所有的Bound
     * @param pois
     * @return
     */
    public static RectDouble getTrackBound(List<GpsTrackPoint> pois){
        if (pois.size() == 0) {
            return null;
        }
        try {
            double x1 = Double.MAX_VALUE;
            double y1 = Double.MAX_VALUE;
            double x2 = Double.MIN_VALUE;
            double y2 = Double.MIN_VALUE;
            for (int i = 0; i < pois.size(); i++) {
                GpsTrackPoint oItem = pois.get(i);
                x1 = Math.min(x1, oItem.f64Longitude);
                y1 = Math.min(y1, oItem.f64Latitude);
                x2 = Math.max(x2, oItem.f64Longitude);
                y2 = Math.max(y2,  oItem.f64Latitude);
            }
            RectDouble rect = new RectDouble(x1,x2,y2,y1);
            return rect;
        } catch (Exception e) {
            return null;
        }
    }
}
