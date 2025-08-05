package com.autosdk.bussiness.layer;

import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizDynamicControl;
import com.autonavi.gbl.layer.model.DynamicControlType;
import com.autonavi.gbl.layer.model.DynamicInitParam;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autosdk.bussiness.map.SurfaceViewID;

import timber.log.Timber;

public class DynamicLayer extends HMIBaseLayer {
    private static final String TAG = DynamicLayer.class.getSimpleName();
    private BizDynamicControl bizDynamicControl;

    private BizDynamicControl bizDynamicControlScene;

    private DynamicInitParam mDynamicInitParam;

    private float mMarkerScaleFactor = 1;

    private BizControlService mBizControlService;

    private MapView  mMapView;

    public DynamicLayer(@SurfaceViewID.SurfaceViewID1 int mSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(mSurfaceViewID);
        if (null != bizService && null != mapView) {
            mBizControlService = bizService;
            mMapView = mapView;
            bizDynamicControl = bizService.getBizDynamicControl(mapView, DynamicControlType.Custom1);
            bizDynamicControlScene = bizService.getBizDynamicControl(mapView, DynamicControlType.Scene);
        }
    }

    /**
     * 初始化
     * @param param 动态图层初始化参数
     */
    public void init(DynamicInitParam param) {
        if (null != bizDynamicControl && param != null) {
            mDynamicInitParam = param;
            bizDynamicControlScene.init(mDynamicInitParam);
            bizDynamicControl.init(mDynamicInitParam);
        }
    }

    public void unInit() {
        if (null != bizDynamicControl) {
            mDynamicInitParam.setDynamicObserver(null);
            bizDynamicControl.unInit();
        }
        if( null != bizDynamicControlScene){
            bizDynamicControlScene.unInit();
        }
    }

    public void updateDynamicBizStyle(){
        if(mBizControlService != null){
            mBizControlService.updateAllStyle(mMapView);
        }
    }

    /**
     * 通过type创建动态图层
     * @param dynamicLayerJson  动态图层DSL，不带源数据
     */
    public void createDynamicLayer(String dynamicLayerJson) {
        if (null != bizDynamicControl) {
            bizDynamicControl.createDynamicLayer(dynamicLayerJson);
        }
    }

    /**
     * 更新动态图层
     * @param bizType        动态图层type
     * @param sourceDataJson 图层动态数据
     */
    public int updateSourceData(long bizType, String sourceDataJson) {
        if (null != bizDynamicControl) {
            return bizDynamicControl.updateSourceData(bizType, sourceDataJson);
        }
        return -1;
    }

    /**
     * 添加图层事件观察者，未来会下架
     * @param observer       观察者对象
     */
    @Deprecated
    public void addClickObserver(ILayerClickObserver observer) {
        if (null != bizDynamicControl) {
            bizDynamicControl.addClickObserver(observer);
        }
    }

    /**
     * 移除图层事件观察者, 未来会下架
     * @param observer       观察者对象
     */
    @Deprecated
    public void removeClickObserver(ILayerClickObserver observer) {
        if (null != bizDynamicControl) {
            bizDynamicControl.removeClickObserver(observer);
        }
    }

    /**
     * 设置图层显隐
     *
     * @param bizType  动态图层type
     * @param bVisible 显隐状态
     */
    public void setVisible(long bizType, boolean bVisible) {
        if (null != bizDynamicControl) {
            bizDynamicControl.setVisible(bizType, bVisible);
        }
    }

    /**
     * 更新动态图层
     * @param bizType        动态图层type
     * @param strID          图元id
     * @param bFocus         是否是焦点态
     */
    public void setFocus(long bizType, String strID, boolean bFocus) {
        if (null != bizDynamicControl && bizDynamicControl.matchBizControl(bizType)) {
            bizDynamicControl.setFocus(bizType, strID, bFocus);
        }
    }

    /**
     * 清除图层
     * @param bizType 动态图层type
     */
    public void clearAllItems(long bizType) {
        if (null != bizDynamicControl && bizDynamicControl.matchBizControl(bizType)) {
            bizDynamicControl.clearAllItems(bizType);
        }
    }

    /**
     * 清除焦点
     * @param bizType 动态图层type
     */
    public void clearFocus(long bizType) {
        if (null != bizDynamicControl && bizDynamicControl.matchBizControl(bizType)) {
            bizDynamicControl.clearFocus(bizType);
        }
    }

    /**
     * 销毁图层
     * @param bizType 动态图层type
     */
    public void destroyDynamicLayer(long bizType){
        if (null!=bizDynamicControl){
            bizDynamicControl.destroyDynamicLayer(bizType);
        }
    }

    public void setPointMarkerScaleFactor(float factor){
        Timber.d("setPointMarkerScaleFactor: %s", factor);
        mMarkerScaleFactor = factor;
    }
}
