package com.autosdk.bussiness.layer;

import static com.autosdk.bussiness.map.SurfaceViewID.transform2EngineID;

import android.os.HandlerThread;

import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.model.DynamicControlType;
import com.autonavi.gbl.layer.observer.PrepareLayerStyleInner;
import com.autonavi.gbl.map.MapService;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.layer.dynamic.BizDynamicType;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * 定位模块管理
 */
public class LayerController {

    public static String TAG = LayerController.class.getSimpleName();

    private MapService mMapService;
    private BizControlService mBizService;
    private ArrayList<LayerControllerInitParam> mControllerInitParams = new ArrayList<>();
    private ArrayList<MapLayer> mMapLayerArrayList = new ArrayList<>();
    private ArrayList<CruiseLayer> mCruiseLayerArrayList = new ArrayList<>();
    private ArrayList<RouteEndAreaLayer> mRouteEndAreaLayerArrayList = new ArrayList<>();
    private ArrayList<RouteResultLayer> mRouteResultLayerArrayList = new ArrayList<>();
    private ArrayList<DrivingLayer> mDrivingLayerArrayList = new ArrayList<>();
    private ArrayList<SearchLayer> mSearchLayerArrayList = new ArrayList<>();
    private ArrayList<UserBehaviorLayer> mUserBehaviorArrayList = new ArrayList<>();
    private ArrayList<AGroupLayer> mAGroupLayerArrayList = new ArrayList<>();
    private ArrayList<CustomLayer> mCustomLayerArrayList = new ArrayList<>();
    private ArrayList<LayerControllerInitParam> mKeepPrepareLayerStyles = new ArrayList<>();
    private ArrayList<DynamicLayer> mDynamicLayerArrayList = new ArrayList<>();

    private static class LayerCtrlHolder {
        private static LayerController mInstance = new LayerController();
    }

    private LayerController() {

    }

    public static LayerController getInstance() {
        return LayerCtrlHolder.mInstance;
    }


    /**
     * 初始化图层模块
     */
    public synchronized boolean init(ArrayList<LayerControllerInitParam> controllerInitParams) {
        int size = controllerInitParams.size();

        if (null == mMapService) {
            mMapService = (MapService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.MapSingleServiceID);
        }

        if (null == mBizService) {
            mBizService = (BizControlService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.BizControlSingleServiceID);
        }
        boolean bRet = false;

        if (null == controllerInitParams) {
            Timber.e(new NullPointerException(), "layerControllerInitParam is null!");
            return false;
        }

        if (controllerInitParams.isEmpty()) {
            Timber.e(new NullPointerException(), "layerControllerInitParam is empty!");
            return false;
        }

        for (int i = 0; i < size; i++) {
            int nSurfaceViewID = controllerInitParams.get(i).SurfaceViewID;

            if (0 < nSurfaceViewID && nSurfaceViewID < (SurfaceViewID.SURFACE_VIEW_ID_EX3 + 1)) {
                // SurfaceViewID 和 MapEngineID的对应关系
                int nEngineId = transform2EngineID(nSurfaceViewID);
                mBizService.init(nEngineId, controllerInitParams.get(i).mStyleBlFilePath);
                mBizService.setStyle(nEngineId, controllerInitParams.get(i).prepareLayerStyle);
                mBizService.initCollisionConfig(mMapService.getMapView(nEngineId), controllerInitParams.get(i).mStyleBlFilePath);
                int nEagleEyeEngineId = nEngineId + 1;
                mBizService.init(nEagleEyeEngineId, controllerInitParams.get(i).mStyleBlFilePath);
                mBizService.setStyle(nEagleEyeEngineId, controllerInitParams.get(i).prepareLayerStyle);
            }
        }

        mControllerInitParams = controllerInitParams;

        return true;
    }

    public synchronized MapLayer getMapLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        MapLayer mapLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mMapLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mMapLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        mapLayer = mMapLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == mapLayer) {
                    mapLayer = new MapLayer(nSurfaceViewID, mBizService, mapView);
                    mMapLayerArrayList.add(mapLayer);
                }
            }
        }
        return mapLayer;
    }

    public synchronized CruiseLayer getCruiseLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        CruiseLayer cruiseLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mCruiseLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mCruiseLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        cruiseLayer = mCruiseLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == cruiseLayer) {
                    cruiseLayer = new CruiseLayer(nSurfaceViewID, mBizService, mapView);
                    mCruiseLayerArrayList.add(cruiseLayer);
                }
            }
        }
        return cruiseLayer;
    }

    public synchronized RouteEndAreaLayer getRouteEndAreaLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        RouteEndAreaLayer routeEndAreaLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mRouteEndAreaLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mRouteEndAreaLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        routeEndAreaLayer = mRouteEndAreaLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == routeEndAreaLayer) {
                    routeEndAreaLayer = new RouteEndAreaLayer(nSurfaceViewID, mBizService, mapView);
                    mRouteEndAreaLayerArrayList.add(routeEndAreaLayer);
                }
            }
        }
        return routeEndAreaLayer;
    }

    public synchronized RouteResultLayer getRouteResultLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        RouteResultLayer routeResultLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mRouteResultLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mRouteResultLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        routeResultLayer = mRouteResultLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == routeResultLayer) {
                    routeResultLayer = new RouteResultLayer(nSurfaceViewID, mBizService, mapView);
                    mRouteResultLayerArrayList.add(routeResultLayer);
                }
            }
        }
        return routeResultLayer;
    }

    public synchronized DrivingLayer getDrivingLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        DrivingLayer drivingLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mDrivingLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mDrivingLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        drivingLayer = mDrivingLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == drivingLayer) {
                    IPrepareLayerStyle prepareLayerStyle = null;
                    for (int i = 0; i < mControllerInitParams.size(); i++) {
                        if (nSurfaceViewID == mControllerInitParams.get(i).SurfaceViewID) {
                            prepareLayerStyle = mControllerInitParams.get(i).prepareLayerStyle;

                            if (null != prepareLayerStyle) {
                                drivingLayer = new DrivingLayer(nSurfaceViewID, mBizService, prepareLayerStyle, mapView);
                                mDrivingLayerArrayList.add(drivingLayer);
                                break;
                            }
                        }
                    }

                }
            }
        }
        return drivingLayer;
    }

    public synchronized SearchLayer getSearchLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        SearchLayer searchLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mSearchLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mSearchLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        searchLayer = mSearchLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == searchLayer) {
                    searchLayer = new SearchLayer(nSurfaceViewID, mBizService, mapView);
                    mSearchLayerArrayList.add(searchLayer);
                }
            }
        }
        return searchLayer;
    }

    public synchronized UserBehaviorLayer getUserBehaviorLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        UserBehaviorLayer userBehaviorLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mUserBehaviorArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mUserBehaviorArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        userBehaviorLayer = mUserBehaviorArrayList.get(i);
                        break;
                    }
                }

                if (null == userBehaviorLayer) {
                    userBehaviorLayer = new UserBehaviorLayer(nSurfaceViewID, mBizService, mapView);
                    mUserBehaviorArrayList.add(userBehaviorLayer);
                }
            }
        }
        return userBehaviorLayer;
    }

    public synchronized CustomLayer getCustomLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        CustomLayer customLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mCustomLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mCustomLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        customLayer = mCustomLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == customLayer) {
                    customLayer = new CustomLayer(nSurfaceViewID, mBizService, mapView);
                    mCustomLayerArrayList.add(customLayer);
                }
            }
        }
        return customLayer;
    }

    public synchronized AGroupLayer getAGroupLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        AGroupLayer aGroupLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mAGroupLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mAGroupLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        aGroupLayer = mAGroupLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == aGroupLayer) {
                    aGroupLayer = new AGroupLayer(nSurfaceViewID, mBizService, mapView);
                    mAGroupLayerArrayList.add(aGroupLayer);
                }
            }
        }
        return aGroupLayer;
    }

    public synchronized DynamicLayer getDynamicLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        DynamicLayer dynamicLayer = null;
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                int size = mDynamicLayerArrayList.size();
                for (int i = 0; i < size; i++) {
                    if (mDynamicLayerArrayList.get(i).getSurfaceViewID() == nSurfaceViewID) {
                        dynamicLayer = mDynamicLayerArrayList.get(i);
                        break;
                    }
                }

                if (null == dynamicLayer) {
                    dynamicLayer = new DynamicLayer(nSurfaceViewID, mBizService, mapView);
                    mDynamicLayerArrayList.add(dynamicLayer);
                }
            }
        }
        return dynamicLayer;
    }

    public synchronized BizControlService getBizControlService() {
        return mBizService;
    }

    /**
     * @return void         无返回值
     * @brief 更新样式接口
     * @param[in] isNight   是否是夜间模式
     * @remark 在昼夜切换，中英文切换时，因为图层纹理大多数都需要相应变化，通过调用updateStyle来触发纹理刷新，
     * 然后在getLayerStyle和getMarkerId中返回新的样式配置信息以及创建新的纹理并返回
     */
    public void updateStyle(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, boolean isNight) {
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                mBizService.getBizCarControl(mapView).updateStyle();
                mBizService.getBizFlyLineControl(mapView).updateStyle();
                mBizService.getBizGuideRouteControl(mapView).updateStyle();
                mBizService.getBizRoadFacilityControl(mapView).updateStyle();
                /**< 路口大图通常不需要更新 */
                mBizService.getBizLabelControl(mapView).updateStyle();
                mBizService.getBizAreaControl(mapView).updateStyle();
                mBizService.getBizSearchControl(mapView).updateStyle();
                mBizService.getBizUserControl(mapView).updateStyle();
                mBizService.getBizAGroupControl(mapView).updateStyle();
                mBizService.getBizGuideEagleEyeControl(mapView.getDeviceId()).updateStyle(isNight);
                mBizService.getBizDynamicControl(mapView, DynamicControlType.Custom1).updateStyle();
                mBizService.getBizCustomControl(mapView).updateStyle();

            }
        }
    }

    public void addClickObserver(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, ILayerClickObserver observer) {
        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                mBizService.getBizFlyLineControl(mapView).addClickObserver(observer);
                mBizService.getBizGuideRouteControl(mapView).addClickObserver(observer);
                mBizService.getBizRoadFacilityControl(mapView).addClickObserver(observer);
                mBizService.getBizLabelControl(mapView).addClickObserver(observer);
                mBizService.getBizAreaControl(mapView).addClickObserver(observer);
                mBizService.getBizSearchControl(mapView).addClickObserver(observer);
                mBizService.getBizUserControl(mapView).addClickObserver(observer);
                mBizService.getBizAGroupControl(mapView).addClickObserver(observer);

            }
        }
    }

    public synchronized void removeClickObserver(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, ILayerClickObserver observer) {

        if (null != mMapService && null != mBizService) {
            MapView mapView = mMapService.getMapView(transform2EngineID(nSurfaceViewID));
            if (null != mapView) {
                mBizService.getBizFlyLineControl(mapView).removeClickObserver(observer);
                mBizService.getBizGuideRouteControl(mapView).removeClickObserver(observer);
                mBizService.getBizRoadFacilityControl(mapView).removeClickObserver(observer);
                mBizService.getBizLabelControl(mapView).removeClickObserver(observer);
                mBizService.getBizAreaControl(mapView).removeClickObserver(observer);
                mBizService.getBizSearchControl(mapView).removeClickObserver(observer);
                mBizService.getBizUserControl(mapView).removeClickObserver(observer);
                mBizService.getBizAGroupControl(mapView).removeClickObserver(observer);
            }
        }
    }

    /**
     * @brief 反初始化主图和biz
     */
    public synchronized void uninit() {
        if (null != mBizService) {
            getDynamicLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).destroyDynamicLayer(
                    BizDynamicType.BizRouteTypeRouteJamBubbles);//退出应用销毁动态图层
            DynamicLayer dynamicLayerEx1 = getDynamicLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1);
            if (dynamicLayerEx1 != null)
                dynamicLayerEx1.destroyDynamicLayer(BizDynamicType.BizRouteTypeRouteJamBubbles);
            int size = mControllerInitParams.size();
            for (int i = 0; i < size; i++) {
                int nSurfaceViewID = mControllerInitParams.get(i).SurfaceViewID;
                if (0 < nSurfaceViewID && nSurfaceViewID < (SurfaceViewID.SURFACE_VIEW_ID_EX3 + 1)) {
                    // SurfaceViewID 和 MapEngineID的对应关系
                    int nEngineId = transform2EngineID(nSurfaceViewID);
                    mBizService.setStyle(nEngineId, null);
                    int nEagleEyeEngineId = nEngineId + 1;
                    mBizService.setStyle(nEagleEyeEngineId, null);
                }
            }

            mBizService.unInit();
            mMapLayerArrayList.clear();
            mCruiseLayerArrayList.clear();
            mRouteResultLayerArrayList.clear();
            mDrivingLayerArrayList.clear();
            mSearchLayerArrayList.clear();
            mCustomLayerArrayList.clear();
            mUserBehaviorArrayList.clear();
            mAGroupLayerArrayList.clear();
            mDynamicLayerArrayList.clear();

            mBizService = null;
            mMapService = null;
        }
    }


    public IPrepareLayerStyle getPrepareLayerStyle(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        if (mKeepPrepareLayerStyles != null && mKeepPrepareLayerStyles.size() > 0) {
            for (int i = 0; i < mKeepPrepareLayerStyles.size(); i++) {
                LayerControllerInitParam layerControllerInitParam = mKeepPrepareLayerStyles.get(i);
                if (layerControllerInitParam.SurfaceViewID == nSurfaceViewID) {
                    return layerControllerInitParam.prepareLayerStyle;
                }
            }
        }
        return null;
    }

    public void keepCustomPrepareLayerStyle(LayerControllerInitParam layerControllerInitParam) {
        this.mKeepPrepareLayerStyles.add(layerControllerInitParam);
    }

    public synchronized void addControllerInitParam(LayerControllerInitParam layerControllerInitParam) {
        if (mControllerInitParams != null) {
            if (!mControllerInitParams.contains(layerControllerInitParam)) {
                this.mControllerInitParams.add(layerControllerInitParam);
            }
        }
    }
}

