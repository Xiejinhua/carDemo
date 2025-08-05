package com.autosdk.bussiness.layer;

import com.autonavi.gbl.layer.BizAGroupControl;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.model.BizAGroupBusinessInfo;
import com.autonavi.gbl.layer.model.BizAGroupType;
import com.autonavi.gbl.layer.model.BizPointBusinessInfo;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.util.ArrayList;

import timber.log.Timber;


public class AGroupLayer extends HMIBaseLayer {
    private BizAGroupControl mBizAGroupControl;

    /**
     * @brief 初始化所有control
     */
    protected AGroupLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != mapView) {
            // 搜索图层
            mBizAGroupControl = bizService.getBizAGroupControl(mapView);
        }
    }

    public BizAGroupControl getUserControl() {
        return mBizAGroupControl;
    }

    /**
     * 获取对应图层的操作Layer，当前提供给搜索配置焦点态
     *
     * @param type
     * @return
     */
    public BaseLayer getBaseLayer(int type) {
        return mBizAGroupControl.getAGroupLayer(type);
    }

    public void addClickObserver(ILayerClickObserver observer) {
        if (mBizAGroupControl != null) {
            mBizAGroupControl.addClickObserver(observer);
        }
    }

    public void removeClickObserver(ILayerClickObserver observer) {
        if (mBizAGroupControl != null) {
            mBizAGroupControl.removeClickObserver(observer);
        }
    }


    public void removeItem(String id) {
        if (mBizAGroupControl != null) {
            BaseLayer baseLayer = getBaseLayer(BizAGroupType.BizAGroupTypeAGroup);
            baseLayer.removeItem(id);
        }
    }

    public void removeItems(String[] ids) {
        if (mBizAGroupControl != null) {
            BaseLayer baseLayer = getBaseLayer(BizAGroupType.BizAGroupTypeAGroup);
            baseLayer.removeItems(ids);
        }
    }


    /**
     * 添加车队
     */
    public void addAGroupMembers(ArrayList<BizAGroupBusinessInfo> memberList) {
        mBizAGroupControl.addAGroupMembers(memberList);
    }

    /**
     * 设置组队终点
     */
    public void setEndPoint(double lon, double lat) {
        BizPointBusinessInfo memPoint = new BizPointBusinessInfo();
        memPoint.mPos3D.lon = lon;
        memPoint.mPos3D.lat = lat;
        mBizAGroupControl.setEndPoint(memPoint);
    }


    /**
     * 更新组队信息
     */
    public void updateAGroupMember(BizAGroupBusinessInfo updateMember) {
        mBizAGroupControl.updateAGroupMember(updateMember);
    }

    /**
     * 清除所有结果
     */
    public void clearAllItems() {
        Timber.i("clearAllItems");
        if (mBizAGroupControl != null) {
            mBizAGroupControl.clearAllItems();
        }
    }

    /**
     * 清除所有结果
     */
    public void clearAllItems(int bizType) {
        if (mBizAGroupControl != null) {
            mBizAGroupControl.clearAllItems(bizType);
        }
    }

    /**
     * @brief 设置图元为焦点
     */
    public void setFocus(long bizType, String strID, boolean bFocus) {
        if (mBizAGroupControl != null) {
            mBizAGroupControl.setFocus(bizType, strID, bFocus);
        }
    }

    /**
     * 清除焦点
     *
     * @param bizType
     */
    public void clearFocus(long bizType) {
        if (mBizAGroupControl != null) {
            mBizAGroupControl.clearFocus(bizType);
        }
    }

    public void setVisible(boolean visible) {
        if (mBizAGroupControl != null) {
            mBizAGroupControl.setVisible(visible);
        }
    }

    public void setVisible(long bizType, boolean visible) {
        if (mBizAGroupControl != null) {
            mBizAGroupControl.setVisible(bizType, visible);
        }
    }

    public void updateStyle() {
        if (mBizAGroupControl != null) {
            mBizAGroupControl.updateStyle();
        }
    }
}
