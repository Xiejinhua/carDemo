package com.autosdk.bussiness.layer;

import com.autonavi.gbl.guide.model.CruiseCongestionInfo;
import com.autonavi.gbl.guide.model.CruiseEventInfo;
import com.autonavi.gbl.guide.model.CruiseFacilityInfo;
import com.autonavi.gbl.guide.model.LaneInfo;
import com.autonavi.gbl.guide.model.SocolEventInfo;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizLabelControl;
import com.autonavi.gbl.layer.BizRoadFacilityControl;
import com.autonavi.gbl.map.MapView;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.util.ArrayList;

public class CruiseLayer extends HMIBaseLayer {

    private BizLabelControl mLabelControl;
    private BizRoadFacilityControl mRoadFacilityControl;

    /**
     * @brief 初始化所有control
     */
    protected CruiseLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != mapView) {
            // 图层扎标类业务接口
            mLabelControl = bizService.getBizLabelControl(mapView);
            // 交通设施图层业务接口
            mRoadFacilityControl = bizService.getBizRoadFacilityControl(mapView);
        }
    }

    /**
     * @return void                 无返回值
     * @brief 更新巡航交通设施图层
     * @param[in] vecFacilityInfo   巡航交通设施信息
     * @note thread：main
     */
    public void updateCruiseFacility(ArrayList<CruiseFacilityInfo> vecFacilityInfo) {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.updateCruiseFacility(vecFacilityInfo);
        }
    }

    /**
     * @return void                 无返回值
     * @brief 更新巡航电子眼图层
     * @param[in] vecCameraInfo     巡航电子眼信息
     * @note thread：main
     */
    public void updateCruiseCamera(ArrayList<CruiseFacilityInfo> vecCameraInfo) {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.updateCruiseCamera(vecCameraInfo);
        }
    }

    /**
     * @return 无返回值
     * @brief 更新巡航拥堵信息，方便进行加粗显示
     * @param[in] iBoldDistance           距拥堵点的距离时加粗（推荐700）
     * @param[in] iClearDistance          距拥堵点的距离时清除(推荐500)
     * @param[in] iMaxLevel               显示加粗最大的比例尺(比如：100m 即 18 ,跟主图一致)
     * @note thread : main
     */
    public void updateCruiseCongestion(CruiseCongestionInfo congestInfo, int iBoldDistance, int iClearDistance, int iMaxLevel) {
        if (mLabelControl != null) {
            mLabelControl.updateCruiseCongestion(congestInfo, iBoldDistance, iClearDistance, iMaxLevel);
        }
    }

    /**
     * @brief 巡航车道线更新
     * @param[in] info      车道线信息
     * @note thread：ui
     */
    public void updateCruiseLane(LaneInfo info) {
        if (mLabelControl != null) {
            mLabelControl.updateCruiseLane(info);
        }
    }

    public void setRoadFacilityLayerVisible(boolean visiable) {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.setVisible(visiable);
        }
    }

    /**
     * @return void                 无返回值
     * @brief 更新巡航拥堵事件图层
     * @param[in] vecCongestInfo    巡航拥堵事件信息
     * @note thread：main
     */
    public void updateCruiseCongestionEvent(ArrayList<SocolEventInfo> vecCruiseEventInfo) {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.updateCruiseCongestionEvent(vecCruiseEventInfo);
        }
    }

    /**
     * @return void                 无返回值
     * @brief 更新巡航电子眼图层
     * @param[in] vecCameraInfo     巡航电子眼信息
     * @note thread：main
     */
    public void updateCruiseEvent(ArrayList<CruiseEventInfo> cruiseEventInfos) {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.updateCruiseEvent(cruiseEventInfos);
        }
    }

    public void clearAllItems() {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.clearAllItems();
        }
        if (mLabelControl != null) {
            mLabelControl.clearAllItems();
        }
    }
}
