package com.autosdk.bussiness.layer;

import static com.autonavi.gbl.layer.model.CustomPriorityMode.CustomPriorityModeAscend;

import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizCustomControl;
import com.autonavi.gbl.layer.model.BizCustomArrowInfo;
import com.autonavi.gbl.layer.model.BizCustomCircleInfo;
import com.autonavi.gbl.layer.model.BizCustomLineInfo;
import com.autonavi.gbl.layer.model.BizCustomPointInfo;
import com.autonavi.gbl.layer.model.BizCustomPolygonInfo;
import com.autonavi.gbl.layer.model.BizCustomTypeArrow;
import com.autonavi.gbl.layer.model.BizCustomTypeCircle;
import com.autonavi.gbl.layer.model.BizCustomTypeLine;
import com.autonavi.gbl.layer.model.BizCustomTypePoint;
import com.autonavi.gbl.layer.model.BizCustomTypePolygon;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.model.LayerPriority;
import com.autonavi.gbl.map.layer.model.SectorAngles;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autonavi.gbl.search.model.SearchPoi;
import com.autonavi.gbl.search.model.SearchPoiInfo;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.util.ArrayList;
import java.util.List;

public class CustomLayer extends HMIBaseLayer {

    private BizCustomControl mBizCustomControl;

    /**
     * @brief 初始化所有control
     */
    protected CustomLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != mapView) {
            mBizCustomControl = bizService.getBizCustomControl(mapView);
        }
    }

    /**
     * @brief 获取车标图层
     */
    public BizCustomControl getBizCustomControl() {
        return mBizCustomControl;
    }

    public void addClickObserver(ILayerClickObserver layerClickObserver) {
        if (mBizCustomControl != null) {
            mBizCustomControl.addClickObserver(layerClickObserver);
        }
    }

    public void addClickObserver(long bizCustomType, ILayerClickObserver layerClickObserver) {
        if (mBizCustomControl != null) {
            mBizCustomControl.getCustomLayer(bizCustomType);
            mBizCustomControl.setClickable(bizCustomType, true);
            mBizCustomControl.addClickObserver(layerClickObserver);
        }
    }

    public void removeClickObserver(ILayerClickObserver observer) {
        if (mBizCustomControl != null) {
            mBizCustomControl.removeClickObserver(observer);
        }
    }

    /*** 自定义图层控制器 ***/
    public void showCustomTypePoint1(Coord3DDouble position) {
        if (mBizCustomControl != null) {
            ArrayList<BizCustomPointInfo> customPoints = new ArrayList<>();
            BizCustomPointInfo point = new BizCustomPointInfo();
            point.mPos3D = position;
            point.priorityMode = CustomPriorityModeAscend;
            customPoints.add(point);
            mBizCustomControl.updateCustomPoint(customPoints, BizCustomTypePoint.BizCustomTypePoint1);
        }
    }

    public void hideCustomTypePoint1() {
        if (mBizCustomControl != null) {
            mBizCustomControl.clearAllItems(BizCustomTypePoint.BizCustomTypePoint1);
        }
    }

    //显示路书详情扎标画线
    public void showAhaCustomLineLayer(ArrayList<BizCustomLineInfo> customPolygons) {
        if (mBizCustomControl != null) {
            mBizCustomControl.updateCustomLine(customPolygons, BizCustomTypeLine.BizCustomTypeLine3);
        }
    }

    public void showCustomPolygonLayer() {
        if (mBizCustomControl != null) {
            ArrayList<BizCustomPolygonInfo> customPolygons = new ArrayList<>();
            BizCustomPolygonInfo customPolygon = new BizCustomPolygonInfo();
            ArrayList<Coord3DDouble> points = new ArrayList<Coord3DDouble>();
            Coord3DDouble start = new Coord3DDouble(116.475536, 39.992828, 0.0);
            Coord3DDouble mid = new Coord3DDouble(116.478883, 39.994776, 0.0);
            Coord3DDouble end = new Coord3DDouble(116.47884, 39.992737, 0.0);
            points.add(start);
            points.add(mid);
            points.add(end);
            customPolygon.vecPoints = points;
            customPolygons.add(customPolygon);
            mBizCustomControl.updateCustomPolygon(customPolygons, BizCustomTypePolygon.BizCustomTypePolygon1);
        }
    }

    public void showCustomCircleLayer() {
        if (mBizCustomControl != null) {
            ArrayList<BizCustomCircleInfo> customCircles = new ArrayList<>();
            BizCustomCircleInfo customCircle = new BizCustomCircleInfo();
            Coord3DDouble center = new Coord3DDouble(116.475064, 39.995137, 0.0);
            customCircle.radius = 500.0;
            customCircle.center = center;
            SectorAngles angles = new SectorAngles();
            angles.startAngle = 0.0;
            angles.endAngle = 360.0;
            angles.stepAngle = 1.0;
            customCircle.sectorAngles = angles;
            customCircles.add(customCircle);
            mBizCustomControl.updateCustomCircle(customCircles, BizCustomTypeCircle.BizCustomTypeCircle1);
        }
    }

    public void showCustomArrowLayer() {
        if (mBizCustomControl != null) {
            ArrayList<BizCustomArrowInfo> customArrows = new ArrayList<>();
            BizCustomArrowInfo customArrow = new BizCustomArrowInfo();
            ArrayList<Coord3DDouble> points = new ArrayList<Coord3DDouble>();
            Coord3DDouble start = new Coord3DDouble(116.470735, 39.992745, 0.0);
            Coord3DDouble mid = new Coord3DDouble(116.47236, 39.993411, 0.0);
            points.add(start);
            points.add(mid);
            customArrow.mVecPoints = points;
            customArrows.add(customArrow);
            mBizCustomControl.updateCustomArrow(customArrows, BizCustomTypeArrow.BizCustomTypeArrow1);
        }
    }

    //终点停车场推荐
    public void showCustomDestinationParkPoint(List<SearchPoiInfo> searchKeywordResult) {
        ArrayList<BizCustomPointInfo> customBottlePoints = new ArrayList<>();
        if (mBizCustomControl != null) {
            int size = searchKeywordResult.size();
            for (int i = 0; i < size; i++) {
                BizCustomPointInfo point = new BizCustomPointInfo();
                Coord3DDouble position = new Coord3DDouble(searchKeywordResult.get(i).basicInfo.location.lon, searchKeywordResult.get(i).basicInfo.location.lat, 0.0);
                point.mPos3D = position;
                point.id = searchKeywordResult.get(i).basicInfo.poiId;
                point.value = searchKeywordResult.get(i).basicInfo.name;
                point.type = i;
                point.priorityMode = CustomPriorityModeAscend;
                customBottlePoints.add(point);
            }
            mBizCustomControl.updateCustomPoint(customBottlePoints, BizCustomTypePoint.BizCustomTypePoint4);
            mBizCustomControl.setClickable(BizCustomTypePoint.BizCustomTypePoint4, true);
        }
    }

    //路书详情简介图层扎标
    public void showAhaLineDayPoint(List<SearchPoiInfo> searchKeywordResult) {
        if (mBizCustomControl != null) {
            ArrayList<BizCustomPointInfo> customBottlePoints = new ArrayList<>();
            for (int i = 0; i < searchKeywordResult.size(); i++) {
                BizCustomPointInfo point = new BizCustomPointInfo();
                Coord3DDouble position = new Coord3DDouble(searchKeywordResult.get(i).basicInfo.location.lon, searchKeywordResult.get(i).basicInfo.location.lat, 0.0);
                point.mPos3D = position;
                point.id = searchKeywordResult.get(i).basicInfo.poiId;
                point.value = searchKeywordResult.get(i).basicInfo.name;
                point.type = searchKeywordResult.get(i).basicInfo.adcode;
                point.priorityMode = CustomPriorityModeAscend;
                customBottlePoints.add(point);
            }
            mBizCustomControl.updateCustomPoint(customBottlePoints, BizCustomTypePoint.BizCustomTypePoint3);
            mBizCustomControl.setClickable(BizCustomTypePoint.BizCustomTypePoint3, true);
        }
    }

    //捷途探趣POI扎点
    public void showJetourListPoint(ArrayList<BizCustomPointInfo> customBottlePoints) {
        if (mBizCustomControl != null) {
            mBizCustomControl.updateCustomPoint(customBottlePoints, BizCustomTypePoint.BizCustomTypePoint5);
            mBizCustomControl.setClickable(BizCustomTypePoint.BizCustomTypePoint5, true);
        }
    }

    //路书详情DAY行程图层扎标
    public void showAhaLineDayNodePoint(List<SearchPoiInfo> searchKeywordResult) {
        if (mBizCustomControl != null) {
            ArrayList<BizCustomPointInfo> customBottlePoints = new ArrayList<>();
            for (int i = 0; i < searchKeywordResult.size(); i++) {
                BizCustomPointInfo point = new BizCustomPointInfo();
                Coord3DDouble position = new Coord3DDouble(searchKeywordResult.get(i).basicInfo.location.lon, searchKeywordResult.get(i).basicInfo.location.lat, 0.0);
                point.mPos3D = position;
                point.id = searchKeywordResult.get(i).basicInfo.poiId;
                point.value = searchKeywordResult.get(i).basicInfo.name;
                point.type = searchKeywordResult.get(i).basicInfo.adcode;
                point.priorityMode = CustomPriorityModeAscend;
                customBottlePoints.add(point);
            }
            mBizCustomControl.updateCustomPoint(customBottlePoints, BizCustomTypePoint.BizCustomTypePoint2);
            mBizCustomControl.setClickable(BizCustomTypePoint.BizCustomTypePoint2, true);
        }
    }

    public void setCustomLayerItemFocus(int type, String id, boolean isFocus) {
        if (mBizCustomControl != null) {
            mBizCustomControl.getCustomLayer(type).setFocus(id, isFocus);
        }
    }

    public void removeLayerItems(long bizType) {
        if (mBizCustomControl != null) {
            mBizCustomControl.clearAllItems(bizType);
        }
    }

    public void removeAllLayerItems() {
        if (mBizCustomControl != null) {
            mBizCustomControl.clearAllItems();
        }
    }

    public void removeAllLayerItems(int type) {
        if (mBizCustomControl != null) {
            mBizCustomControl.clearAllItems(type);
        }
    }
}
