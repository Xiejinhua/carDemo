package com.autosdk.bussiness.layer;

import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizSearchControl;
import com.autonavi.gbl.layer.model.BizCustomTypePoint;
import com.autonavi.gbl.layer.model.BizLineBusinessInfo;
import com.autonavi.gbl.layer.model.BizPointBusinessInfo;
import com.autonavi.gbl.layer.model.BizPolygonBusinessInfo;
import com.autonavi.gbl.layer.model.BizSearchAlongWayPoint;
import com.autonavi.gbl.layer.model.BizSearchBeginEndPoint;
import com.autonavi.gbl.layer.model.BizSearchChargeStationInfo;
import com.autonavi.gbl.layer.model.BizSearchChildPoint;
import com.autonavi.gbl.layer.model.BizSearchParentPoint;
import com.autonavi.gbl.layer.model.BizSearchType;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.util.ArrayList;

import static com.autonavi.gbl.layer.model.BizSearchType.BizSearchTypePoiAlongRoute;

public class SearchLayer extends HMIBaseLayer{

    private BizSearchControl mSearchControl;
    private BaseLayer mBaseLayer;

    /**
     * @brief 初始化所有control
     */
    protected SearchLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != mapView) {
            // 搜索图层
            mSearchControl = bizService.getBizSearchControl(mapView);
        }
    }

    public BizSearchControl getSearchControl() {
        return mSearchControl;
    }

    /**
     * 获取对应图层的操作Layer，当前提供给搜索配置焦点态
     *
     * @param type
     * @return
     */
    public BaseLayer getBaseLayer(int type) {
        return mSearchControl.getSearchLayer(type);
    }

    public void addClickObserver(ILayerClickObserver observer) {
        if (mSearchControl != null) {
            mSearchControl.addClickObserver(observer);
        }
    }

    public void removeClickObserver(ILayerClickObserver observer) {
        if (mSearchControl != null) {
            mSearchControl.removeClickObserver(observer);
        }
    }

    public void updateSearchLine(ArrayList<BizLineBusinessInfo> vecLineInfo) {
        if (mSearchControl != null) {
            mSearchControl.updateSearchLine(vecLineInfo);
        }
    }

    public void updateSearchPolygon(BizPolygonBusinessInfo polygonInfo) {
        if (mSearchControl != null) {
            mSearchControl.updateSearchPolygon(polygonInfo);
        }
    }

    public void updateSearchPolygon(ArrayList<BizPolygonBusinessInfo> polygonInfoList) {
        /** 需要升级SDK版本 */
        //mSearchControl.updateSearchPolygon(polygonInfoList);
    }

    /**
     * @return bool            true:成功;false:失败
     * @brief 搜索结果扎点（父POI）
     * @param[in] pointList    沿途搜索POI点信息
     * @note thread：main
     */
    public boolean updateSearchParentPoi(ArrayList<BizSearchParentPoint> pointList) {
        clearAllItems(BizSearchType.BizSearchTypePoiParentPoint);
        return mSearchControl!= null && mSearchControl.updateSearchParentPoi(pointList);
    }

    /**
     * @return bool            true:成功;false:失败
     * @brief 搜索结果扎点（子POI）
     * @param[in] pointList    沿途搜索POI点信息
     * @note thread：main
     */
    public boolean updateSearchChildPoi(ArrayList<BizSearchChildPoint> pointList) {
        clearAllItems(BizSearchType.BizSearchTypePoiChildPoint);
        return mSearchControl!= null && mSearchControl.updateSearchChildPoi(pointList);
    }
    public boolean updateSearchCentralPoi(ArrayList<BizPointBusinessInfo> pointList) {
        return mSearchControl!= null && mSearchControl.updateSearchCentralPoi(pointList);
    }

    public boolean updateSearchBeginEndPoi(ArrayList<BizSearchBeginEndPoint> pointList) {
        return mSearchControl!= null && mSearchControl.updateSearchBeginEndPoi(pointList);
    }

    /**
     * @return bool            true:成功;false:失败
     * @brief 沿途搜索图层业务
     * @param[in] pointList    沿途搜索POI点信息
     * @note thread：main
     */
    public boolean updateSearchAlongRoutePoi(ArrayList<BizSearchAlongWayPoint> pointList) {
        return mSearchControl!= null && mSearchControl.updateSearchAlongRoutePoi(pointList);
    }

    /**
     * 沿途搜停车场POI
     *
     * @param pointList
     * @return
     */
    public boolean updateSearchParkPoi(ArrayList<BizPointBusinessInfo> pointList) {
        return mSearchControl!= null && mSearchControl.updateSearchParkPoi(pointList);
    }

    public boolean updateSearchPoiLabel(BizPointBusinessInfo labelInfo) {
        clearAllItems(BizSearchType.BizSearchTypePoiLabel);
        return mSearchControl!= null && mSearchControl.updateSearchPoiLabel(labelInfo);
    }

    public boolean updateSearchChargeStation(ArrayList<BizSearchChargeStationInfo> pointList) {
        return mSearchControl!= null && mSearchControl.updateSearchChargeStation(pointList);
    }

    /**
     * 清除所有搜索结果
     */
    public void clearAllItems() {
        if (mSearchControl != null) {
            mSearchControl.clearAllItems();
        }
    }

    /**
     * 清除所有搜索结果
     */
    public void clearAllItems(int bizType) {
        if (mSearchControl != null) {
            mSearchControl.clearAllItems(bizType);
        }
//        clearAlongRouteTip(bizType);
    }

    /**
     * @brief 设置图元为焦点
     */
    public void setFocus(long bizType, String strID, boolean bFocus) {
        if (mSearchControl != null) {
            mSearchControl.setFocus(bizType, strID, bFocus);
        }
    }

    /**
     * 清除焦点
     *
     * @param bizType
     */
    public void clearFocus(long bizType) {
        if (mSearchControl != null) {
            mSearchControl.clearFocus(bizType);
        }
    }

    public void setVisible(boolean visible) {
        if (mSearchControl != null) {
            mSearchControl.setVisible(visible);
        }
    }

    public void setVisible(long bizType, boolean visible) {
        if (mSearchControl != null) {
            mSearchControl.setVisible(bizType, visible);
        }
    }
}
