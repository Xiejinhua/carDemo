package com.autosdk.bussiness.layer;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.common.model.RectInt;
import com.autonavi.gbl.layer.BizAreaControl;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizLabelControl;
import com.autonavi.gbl.layer.EndAreaPointLayerItem;
import com.autonavi.gbl.layer.model.BizAreaType;
import com.autonavi.gbl.layer.model.BizLabelType;
import com.autonavi.gbl.layer.model.BizPolygonBusinessInfo;
import com.autonavi.gbl.layer.model.BizPopPointBusinessInfo;
import com.autonavi.gbl.layer.model.BizRouteEndAreasInfo;
import com.autonavi.gbl.layer.model.RouteEndAreaPointInfo;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autonavi.gbl.search.model.SearchPoiBasicInfo;
import com.autonavi.gbl.search.model.SearchPoiChildInfo;
import com.autonavi.gbl.search.model.SearchPoiInfo;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.layer.observer.IBusinessLayerClickObserver;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils;
import com.autosdk.bussiness.widget.search.util.SearchMapUtil;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * @brief 路线终点区域相关图层类
 */
public class RouteEndAreaLayer extends HMIBaseLayer implements ILayerClickObserver {
    private static final String TAG = "RouteEndAreaLayer";

    private BizAreaControl mAreaControl;

    private BizLabelControl mLabelControl;

    private SearchPoiInfo mSearchEndPoi;

    private SearchPoiChildInfo mCurSelectChildPoi;

    private final ArrayList<IBusinessLayerClickObserver> mListeners = new ArrayList<>();

    protected RouteEndAreaLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != mapView) {
            mAreaControl = bizService.getBizAreaControl(mapView);
            mLabelControl = bizService.getBizLabelControl(mapView);
        }
    }

    public void addRouteEndAreaObserver(IBusinessLayerClickObserver l) {
        if (l != null && !mListeners.contains(l)) {
            if (mListeners.isEmpty()) {
                mAreaControl.addClickObserver(this);
                mLabelControl.addClickObserver(this);
            }
            mListeners.add(l);
        }
    }

    public void removeRouteEndAreaObserver(IBusinessLayerClickObserver l) {
        if (l != null) {
            mListeners.remove(l);
            if (mListeners.isEmpty()) {
                mAreaControl.removeClickObserver(this);
                mLabelControl.removeClickObserver(this);
            }
        }
    }

    /**
     * 非poi点终点信息绘制(由于非poi点无法发起poiid搜索,没有区域面、区域线及子点，因此只显示eta信息)
     * @param endPoi 终点poi
     * @param rectInt 多边区域
     */
    public void updateRouteEndParentPoint(POI endPoi, RectInt rectInt) {
        if (endPoi == null) return;
        if (rectInt == null) {
            rectInt = new RectInt(400, 900, 100, 600); // 由产品定义
        }
        clearAllRouteEndAreaLayer();

        BizRouteEndAreasInfo bizRouteEndAreasInfo = new BizRouteEndAreasInfo();

        // 父节点
        RouteEndAreaPointInfo majorPointData = new RouteEndAreaPointInfo();
        majorPointData.id = "parent";
        majorPointData.mPos3D.lon = endPoi.getPoint().getLongitude();
        majorPointData.mPos3D.lat = endPoi.getPoint().getLatitude();
        majorPointData.mPos3D.z = 0.0f;
        majorPointData.poiName = endPoi.getName();
        bizRouteEndAreasInfo.vecParentPointInfo.add(majorPointData);
        bizRouteEndAreasInfo.vecChildPointInfo = new ArrayList<>();
        bizRouteEndAreasInfo.vecAreaPolygonInfo = new ArrayList<>();
        setEndAreaOverlayVisible(true);
        updateRouteEndAreas(bizRouteEndAreasInfo, rectInt);
    }

    /**
     * 绘制终点区域边框及父子节点
     * @param searchPoiInfo 搜索终点区域数据
     * @param rectInt 多边区域
     */
    public void updateRouteEndAreaAndParentPoint(SearchPoiInfo searchPoiInfo, POI endPoi, RectInt rectInt) {
        if (searchPoiInfo == null) return;
        if (rectInt == null) {
            rectInt = new RectInt(400, 900, 100, 600);//由产品定义
        }

        clearAllRouteEndAreaLayer();
        mSearchEndPoi = searchPoiInfo;

        Timber.d("child poi size:%s, poiAoiBounds size:%s",
                searchPoiInfo.childInfoList.size(), searchPoiInfo.basicInfo.poiAoiBounds.size());

        BizRouteEndAreasInfo bizRouteEndAreasInfo = new BizRouteEndAreasInfo();

        // 父节点
//        SearchPoiBasicInfo poiBase = searchPoiInfo.basicInfo;
//        Coord2DDouble poiLoc = poiBase.location;
        RouteEndAreaPointInfo majorPointData = new RouteEndAreaPointInfo();
        majorPointData.id = "parent";
        majorPointData.mPos3D.lon = endPoi.getPoint().getLongitude();//终点要和规划路线终点一致，不然终点名称不在扎点下方
        majorPointData.mPos3D.lat = endPoi.getPoint().getLatitude();
        majorPointData.mPos3D.z = 0.0f;
        majorPointData.poiName = endPoi.getName();
        bizRouteEndAreasInfo.vecParentPointInfo.add(majorPointData);

        // 子节点
        ArrayList<SearchPoiChildInfo> childPois = searchPoiInfo.childInfoList;
        for (int i = 0; i < childPois.size(); i++) {
            RouteEndAreaPointInfo childPointData = new RouteEndAreaPointInfo();
            Coord3DDouble posEndArea = new Coord3DDouble();
            posEndArea.lon = childPois.get(i).location.lon;
            posEndArea.lat = childPois.get(i).location.lat;
            posEndArea.z = 0.0;
            childPointData.id = "child" + i;
            childPointData.mPos3D = posEndArea;
            childPointData.poiType = childPois.get(i).childType;
            childPointData.poiName = childPois.get(i).shortName;
            bizRouteEndAreasInfo.vecChildPointInfo.add(childPointData);
        }

        // 多个多边形区域
        ArrayList<ArrayList<Coord2DDouble>> poiAoiBounds = searchPoiInfo.basicInfo.poiAoiBounds;
        int index = 0;
        for (ArrayList<Coord2DDouble> poiAoiBound : poiAoiBounds) {
            BizPolygonBusinessInfo bizPolygonBusinessInfo = new BizPolygonBusinessInfo();
            bizPolygonBusinessInfo.id = "parent" + index;
            bizPolygonBusinessInfo.mDrawPolygonRim = true;
            for (Coord2DDouble poiAoi : poiAoiBound) {
                Coord3DDouble posEndArea = new Coord3DDouble();
                posEndArea.lon = poiAoi.lon;
                posEndArea.lat = poiAoi.lat;
                posEndArea.z = 0.0;
                bizPolygonBusinessInfo.mVecPoints.add(posEndArea);
            }
            index++;
            bizRouteEndAreasInfo.vecAreaPolygonInfo.add(bizPolygonBusinessInfo);
        }

        setEndAreaOverlayVisible(true);
        updateRouteEndAreas(bizRouteEndAreasInfo, rectInt);
    }

    /**
     * @brief 更新终点区域信息
     * @param endAreaBusinessInfo    终点区域信息
     * @note thread: main
     */
    public void updateRouteEndAreas(BizRouteEndAreasInfo endAreaBusinessInfo, RectInt rectInt) {
        if (mAreaControl != null) {
            mAreaControl.updateRouteEndAreas(endAreaBusinessInfo, rectInt);
        }
    }

    private void setEndAreaOverlayVisible(boolean status) {
        if (mAreaControl != null) {
            mAreaControl.getAreaLayer(BizAreaType.BizAreaTypeEndAreaPolygon).setVisible(status);
            mAreaControl.getAreaLayer(BizAreaType.BizAreaTypeEndAreaPolyline).setVisible(status);
            mAreaControl.getAreaLayer(BizAreaType.BizAreaTypeEndAreaParentPoint).setVisible(status);
            mAreaControl.getAreaLayer(BizAreaType.BizAreaTypeEndAreaChildPoint).setVisible(status);

            mAreaControl.getAreaLayer(BizAreaType.BizAreaTypeEndAreaPolygon).setClickable(false);
            mAreaControl.getAreaLayer(BizAreaType.BizAreaTypeEndAreaPolyline).setClickable(false);
            mAreaControl.getAreaLayer(BizAreaType.BizAreaTypeEndAreaParentPoint).setClickable(false);
            mAreaControl.getAreaLayer(BizAreaType.BizAreaTypeEndAreaChildPoint).setClickable(false);
        }
    }

    /**
     * 清空终点区域图层
     */
    public void clearAllRouteEndAreaLayer() {
        if (mAreaControl != null) {
            mAreaControl.clearAllItems(BizAreaType.BizAreaTypeEndAreaParentPoint);
            mAreaControl.clearAllItems(BizAreaType.BizAreaTypeEndAreaChildPoint);
            mAreaControl.clearAllItems(BizAreaType.BizAreaTypeEndAreaPolyline);
            mAreaControl.clearAllItems(BizAreaType.BizAreaTypeEndAreaPolygon);
        }
        mSearchEndPoi = null;
    }

    private void updatePopEndAreaPointBoxInfo(BizPopPointBusinessInfo popEnd) {
        ArrayList<BizPopPointBusinessInfo> popEnds = new ArrayList<>();
        popEnds.add(popEnd);
        mLabelControl.setVisible(BizLabelType.BizLabelTypeRoutePopEndArea, true);
        mLabelControl.updatePopEndAreaPointBoxInfo(popEnds);
        mLabelControl.setFocus(BizLabelType.BizLabelTypeRoutePopEndArea, popEnd.id, true);
    }

    public void clearBizLabelTypeRoutePopEndArea() {
        mLabelControl.clearAllItems(BizLabelType.BizLabelTypeRoutePopEndArea);
    }

    public void clearFocus() {
        mAreaControl.clearFocus(BizAreaType.BizAreaTypeEndAreaChildPoint);
        mAreaControl.clearFocus(BizAreaType.BizAreaTypeEndAreaParentPoint);
    }

    @Override
    public void onNotifyClick(BaseLayer baseLayer, LayerItem layerItem, ClickViewIdInfo clickViewIdInfo) {
        int businessType = layerItem.getBusinessType();

        switch (businessType) {
            case BizAreaType.BizAreaTypeEndAreaChildPoint:
                if (mSearchEndPoi == null || mSearchEndPoi.childInfoList.isEmpty()) break;
                EndAreaPointLayerItem endAreaPointLayerItem = (EndAreaPointLayerItem) layerItem;
                for (int i = 0; i < mSearchEndPoi.childInfoList.size(); i++) {
                    String childId = "child" + i;
                    if (childId.equals(endAreaPointLayerItem.getID())) {
                        SearchPoiChildInfo childPoi = mSearchEndPoi.childInfoList.get(i);
                        SearchMapUtil.updateMapCenter(childPoi.location.lon, childPoi.location.lat);
                        mCurSelectChildPoi = childPoi;
                        // 显示子点去这里pop框
                        BizPopPointBusinessInfo bizPopPointBusinessInfo = new BizPopPointBusinessInfo();
                        bizPopPointBusinessInfo.id = childId;
                        bizPopPointBusinessInfo.text = endAreaPointLayerItem.getMPoiName();
                        bizPopPointBusinessInfo.mPos3D = endAreaPointLayerItem.getPosition();
                        updatePopEndAreaPointBoxInfo(bizPopPointBusinessInfo);
                        Timber.d("set child point map center, id:" + childId);
                        break;
                    }
                }
                if (mCurSelectChildPoi != null) {
                    for (IBusinessLayerClickObserver listener : mListeners) {
                        listener.onNotifyEndAreaChildClick(SearchDataConvertUtils.convertNaviInfoToPoi(mCurSelectChildPoi));
                    }
                }
                break;
            case BizLabelType.BizLabelTypeRoutePopEndArea:
                clearBizLabelTypeRoutePopEndArea();
                if (mCurSelectChildPoi != null) {
                    for (IBusinessLayerClickObserver listener : mListeners) {
                        listener.onNotifyPopEndAreaClick(SearchDataConvertUtils.convertNaviInfoToPoi(mCurSelectChildPoi));
                    }
                } else {
                    Timber.d("Not find select child point.");
                }
                break;
            default:
                break;
        }
    }
}
