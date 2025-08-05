
package com.autosdk.bussiness.layer;


import android.content.res.AssetManager;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.common.path.model.RoutePoints;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.guide.model.CrossImageInfo;
import com.autonavi.gbl.guide.model.CrossType;
import com.autonavi.gbl.guide.model.CruiseFacilityInfo;
import com.autonavi.gbl.guide.model.LaneInfo;
import com.autonavi.gbl.guide.model.LightBarInfo;
import com.autonavi.gbl.guide.model.MixForkInfo;
import com.autonavi.gbl.guide.model.NaviCameraExt;
import com.autonavi.gbl.guide.model.NaviCongestionInfo;
import com.autonavi.gbl.guide.model.NaviInfo;
import com.autonavi.gbl.guide.model.NaviRoadFacility;
import com.autonavi.gbl.guide.model.TrafficEventInfo;
import com.autonavi.gbl.layer.BizAreaControl;
import com.autonavi.gbl.layer.BizCarControl;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizGuideEagleEyeControl;
import com.autonavi.gbl.layer.BizGuideRouteControl;
import com.autonavi.gbl.layer.BizLabelControl;
import com.autonavi.gbl.layer.BizRoadCrossControl;
import com.autonavi.gbl.layer.BizRoadFacilityControl;
import com.autonavi.gbl.layer.BizSearchControl;
import com.autonavi.gbl.layer.model.BizAreaType;
import com.autonavi.gbl.layer.model.BizCarType;
import com.autonavi.gbl.layer.model.BizLabelType;
import com.autonavi.gbl.layer.model.BizPathInfoAttrs;
import com.autonavi.gbl.layer.model.BizPopPointBusinessInfo;
import com.autonavi.gbl.layer.model.BizRoadFacilityType;
import com.autonavi.gbl.layer.model.BizRouteDrawCtrlAttrs;
import com.autonavi.gbl.layer.model.BizRouteMapMode;
import com.autonavi.gbl.layer.model.BizRouteType;
import com.autonavi.gbl.layer.model.DynamicLevelParam;
import com.autonavi.gbl.layer.model.DynamicLevelType;
import com.autonavi.gbl.layer.model.EagleEyeParam;
import com.autonavi.gbl.layer.model.EagleEyeStyle;
import com.autonavi.gbl.layer.model.RouteDrawStyle;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.model.CarLoc;
import com.autonavi.gbl.map.layer.model.CarMode;
import com.autonavi.gbl.map.layer.model.LayerTexture;
import com.autonavi.gbl.map.layer.model.RouteLayerScene;
import com.autonavi.gbl.map.layer.model.VectorCrossViewPostureEvent;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle;
import com.autonavi.gbl.map.model.MapViewPortParam;
import com.autosdk.bussiness.common.task.TaskManager;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.location.constant.SdkLocStatus;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.common.utils.SdkNetworkUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * @brief 定义类GuideLayerImpl, 导航相关图层
 */
public class DrivingLayer extends HMIBaseLayer {
    public static String TAG = DrivingLayer.class.getSimpleName();
    private BizGuideRouteControl mGuideRouteControl;
    private BizGuideEagleEyeControl mGuideEagleEyeControl;
    private IPrepareLayerStyle mEagleEyePrepareLayerStyle;
    private BizRoadCrossControl mRoadCrossControl;
    private BizLabelControl mLabelControl;
    private BizSearchControl mSearchControl;
    private BizAreaControl mAreaControl;
    private BizRoadFacilityControl mRoadFacilityControl;
    private BizCarControl mCarControl;

    private RoutePoints mPathPoints;
    private MapView mMapView = null;
    private ArrayList<PathInfo> mPathResult;
    private int mCurSelectIndex = 0;
    //路线总长度
    private long mTotalDistance = 0;
    private boolean mIsShowPreview;

    protected DrivingLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, IPrepareLayerStyle prepareLayerStyle, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != prepareLayerStyle && null != mapView) {
            mMapView = mapView;
            mEagleEyePrepareLayerStyle = prepareLayerStyle;
            mGuideRouteControl = bizService.getBizGuideRouteControl(mapView);
            mRoadCrossControl = bizService.getBizRoadCrossControl(mapView);
            mLabelControl = bizService.getBizLabelControl(mapView);
            mSearchControl = bizService.getBizSearchControl(mapView);
            mAreaControl = bizService.getBizAreaControl(mapView);
            mRoadFacilityControl = bizService.getBizRoadFacilityControl(mapView);
            mCarControl = bizService.getBizCarControl(mapView);
            mGuideEagleEyeControl = bizService.getBizGuideEagleEyeControl(mapView.getDeviceId());
            initDynamicLevel();
            //打开红绿灯倒计时功能
            mRoadFacilityControl.enableLayer(BizRoadFacilityType.BizRoadFacilityTypeGuideTrafficSignalLight, true);
        }
    }

    public void drawPaths(boolean isShowAll) {
        long pathCount = mPathResult == null ? 0 : mPathResult.size();
        // 准备线数据
        ArrayList<BizPathInfoAttrs> paths = new ArrayList<>();
        for (int i = 0; i < pathCount; i++) {
            addVariantPathWrap(isShowAll, paths, i);
        }
        // 路线绘制风格
        RouteDrawStyle mainRouteDrawStyle = new RouteDrawStyle();
        // 是否导航 路径规划中为false
        mainRouteDrawStyle.mIsNavi = true;
        // 是否离线
        mainRouteDrawStyle.mIsOffLine = !SdkNetworkUtil.getInstance().isNetworkConnected();
        // 图模式 主图 或 鹰眼
        mainRouteDrawStyle.mRouteMapMode = BizRouteMapMode.BizRouteMapModeMain;
        // 路线业务场景 单指线
        mainRouteDrawStyle.mRouteScene = RouteLayerScene.RouteLayerSceneNormal;
        // 是否是多备选模式
        mainRouteDrawStyle.mIsMultipleMode = true;

        if (mGuideRouteControl != null) {
            mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeTrafficEventTip).enableCollision(true);
            mGuideRouteControl.setPassGreyMode(true);
            mGuideRouteControl.setPathDrawStyle(mainRouteDrawStyle);
            mGuideRouteControl.setPathPoints(mPathPoints);
            if (!isShowAll) {
                // 更新引导路线数据
                mGuideRouteControl.setPathInfos(paths, 0);
            } else {
                mGuideRouteControl.setPathInfos(paths, mCurSelectIndex);
            }
            mGuideRouteControl.setVisible(BizRouteType.BizRouteTypeEnergyRemainPoint, true);
            // 绘制路线以及路线上的元素
            updatePathsAsync();
            updatePathArrow();
            setVisibleGuideLabel(true);
        }
    }

    private void addVariantPathWrap(boolean isShowAll, ArrayList<BizPathInfoAttrs> paths, int i) {
        BizRouteDrawCtrlAttrs mBizRouteDrawCtrlAttrs = new BizRouteDrawCtrlAttrs();
        //是否要绘制
        mBizRouteDrawCtrlAttrs.mIsDrawPath = true;
        //是否绘制电子眼
        mBizRouteDrawCtrlAttrs.mIsDrawPathCamera = false;
        //是否要绘制路线上的交通灯
        mBizRouteDrawCtrlAttrs.mIsDrawPathTrafficLight = true;
        //是否要绘制转向箭头
        mBizRouteDrawCtrlAttrs.mIsNewRouteForCompareRoute = true;
        //是否要显示
        mBizRouteDrawCtrlAttrs.mIsVisible = true;
        //是否要打开交通事件显示开关，默认为开
        mBizRouteDrawCtrlAttrs.mIsTrafficEventOpen = true;
        mBizRouteDrawCtrlAttrs.mIsHighLightRoadName = i == mCurSelectIndex;
        BizPathInfoAttrs mBizPathInfoAttrs = new BizPathInfoAttrs(mPathResult.get(i), mBizRouteDrawCtrlAttrs);
        paths.add(i, mBizPathInfoAttrs);
    }

    public void drawRoute(RoutePoints pathPoints, ArrayList<PathInfo> pathResult, int curSelectIndex, boolean isShowAll, long totalDistance) {
        Timber.i("drawRoute curSelectIndex=%s ", curSelectIndex);
        mPathResult = pathResult;
        mPathPoints = pathPoints;
        mCurSelectIndex = curSelectIndex;
        mTotalDistance = totalDistance;
        drawPaths(isShowAll);
    }

    public void switchSelectedPath(int index) {
        mCurSelectIndex = index;
        drawPaths(true);
    }

    public RoutePoints getPathPoints() {
        return mPathPoints;
    }

    public ArrayList<PathInfo> getPathResult() {
        return mPathResult;
    }

    public int getCurSelectIndex() {
        return mCurSelectIndex;
    }

    public long getTotalDistance() {
        return mTotalDistance;
    }

    public void setSelectedPathIndex(int index) {
        mCurSelectIndex = index;
        //mGuideRouteControl.setSelectedPathIndex(index);
        mGuideRouteControl.switchSelectedPath(index);
    }

    public void clearAllItems() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearAllItems();
        }
        TaskManager.runSingleThread(new Runnable() {
            @Override
            public void run() {
                if (mGuideRouteControl != null) {
                    mGuideRouteControl.clearPathsCacheData();
                }
            }
        });

        if (mSearchControl != null) {
            mSearchControl.clearAllItems();
        }

        if (mAreaControl != null) {
            mAreaControl.clearAllItems();
        }

        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.clearAllItems();
        }

        if (mLabelControl != null) {
            mLabelControl.clearAllItems();
        }
    }

    public void clearAllItems(long bizType) {
        if (mGuideRouteControl != null && mGuideRouteControl.matchBizControl(bizType)) {
            mGuideRouteControl.clearAllItems(bizType);
        }

        if (mSearchControl != null && mSearchControl.matchBizControl(bizType)) {
            mSearchControl.clearAllItems(bizType);
        }

        if (mAreaControl != null && mAreaControl.matchBizControl(bizType)) {
            mAreaControl.clearAllItems(bizType);
        }

        if (mRoadFacilityControl != null && mRoadFacilityControl.matchBizControl(bizType)) {
            mRoadFacilityControl.clearAllItems(bizType);
        }

        if (mLabelControl != null && mLabelControl.matchBizControl(bizType)) {
            mLabelControl.clearAllItems(bizType);
        }
    }

    /**
     * 取消焦点态
     *
     * @param bizType 类型
     */
    public void clearFocus(long bizType) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearFocus(bizType);
        }
    }

    public void updateViaPass(long viaIndex) {
        if (mGuideRouteControl != null) {
            BaseLayer layer = mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeViaPoint);
            layer.lockItems();
            if (layer != null) {
                LayerItem item = layer.getAllItems().get((int) viaIndex);
                if (item != null) {
                    item.setVisible(false);
//                    layer.removeItem(item.getID());
                }
            }
            layer.unLockItems();
        }
    }

    public void updateViaPassStyle() {
        if (mGuideRouteControl != null) {
            BaseLayer layer = mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeViaPoint);
            layer.lockItems();
            if (layer != null) {
                layer.updateStyle();
            }
            layer.unLockItems();
        }
    }

    public void updateViaFocus() {
        if (mGuideRouteControl != null) {
            BaseLayer layer = mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeViaPoint);
            layer.lockItems();
            if (layer != null) {
                layer.clearFocus();
            }
            layer.unLockItems();
        }
    }

    public void updateEndFocus() {
        if (mGuideRouteControl != null) {
            BaseLayer layer = mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeEndPoint);
            layer.lockItems();
            if (layer != null) {
                layer.clearFocus();
            }
            layer.unLockItems();
        }
    }

    public void updateNaviInfo(ArrayList<NaviInfo> vecNaviInfo) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.updateNaviInfo(vecNaviInfo);
        }
    }

    /**
     * @return void         无返回值
     * @brief 导航拥堵时长信息
     * @note thread：main
     */
    public void updateGuideCongestionBoard(NaviCongestionInfo naviCongestionInfo) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.updateGuideCongestionBoard(naviCongestionInfo);
        }
    }

    /**
     * @return void                 无返回值
     * @return void                 无返回值
     * @brief 设置转向箭头要显示导航段
     * @param[in] segmentsIndexs    要显示的导航段列表
     * @note thread: main
     */
    public void setPathArrowSegment(ArrayList<Long> segmentsIndexs) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.setPathArrowSegment(segmentsIndexs);
        }
    }

    /**
     * @brief 更新路线（自动抛入主线程执行）
     * @note thread: main
     */
    public void updatePathsAsync() {
        Timber.d("updatePaths, mGuideRouteControl=%s", mGuideRouteControl);
        if (mGuideRouteControl == null) {
            return;
        }
        TaskManager.runSingleThread(() -> mGuideRouteControl.updatePaths());
    }

    /**
     * @brief 更新路线上的箭头（自动抛入主线程执行）
     * @note thread: main
     */
    public void updatePathArrow() {
        Timber.d("updatePathArrow, mGuideRouteControl=%s", mGuideRouteControl);
        if (mGuideRouteControl == null) {
            return;
        }
        TaskManager.post(() -> mGuideRouteControl.updatePathArrow());
    }

    /**
     * @return void                     无返回值
     * @brief 更新导航电子眼图层
     * @param[in] vecGuideCamera        导航电子眼信息
     * @param[in] bChangeMapLevel       是否需要进行缩放以便电子眼更清晰
     * @note thread：main
     */
    public void updateGuideCamera(ArrayList<NaviCameraExt> vecGuideCamera) {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.updateGuideCamera(vecGuideCamera);
        }
    }


    /**
     * @return void 无返回值
     * @brief 更新巡航交通设施图层
     * @param[in] vecFacilityInfo        巡航交通设施信息
     * @note thread：main
     */
    public void updateCruiseFacility(ArrayList<CruiseFacilityInfo> vecFacilityInfo) {
        mRoadFacilityControl.updateCruiseFacility(vecFacilityInfo);
    }

    public void showRouteSegmentArrow(long segmentsId) {
        Timber.d("showRouteSegmentArrow() id:" + segmentsId);
        if (mGuideRouteControl == null) {
            return;
        }

        ArrayList<Long> data = new ArrayList<>();
        data.add(segmentsId);
        setPathArrowSegment(data);
        updatePathArrow();
    }

    /**
     * @return void 无返回值
     * @brief 更新巡航电子眼图层
     * @param[in] vecCameraInfo        巡航电子眼信息
     * @note thread：main
     */
    public void updateCruiseCamera(ArrayList<CruiseFacilityInfo> vecCameraInfo) {
        mRoadFacilityControl.updateCruiseCamera(vecCameraInfo);
    }

    public void updateTmcLightBar(ArrayList<LightBarInfo> lightBarInfos) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.updateTmcLightBar(lightBarInfos);
        }
    }

    public void updatePaths() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.updatePaths();
        }
    }

    /**
     * @return bool                成功返回true，失败返回false
     * @return bool                成功返回true，失败返回false
     * @brief 设置栅格图图片数据
     * @param[in] arrowImge        前景箭头
     * @param[in] roadImage        背景道路
     * @remark 使用arrowImge的ResId等参数添加一个OverTex
     */
    public boolean setRasterImageData(LayerTexture arrowImge, LayerTexture roadImage) {
        if (mRoadCrossControl == null) {
            return false;
        }
        return mRoadCrossControl.setRasterImageData(arrowImge, roadImage);
    }

    /**
     * @return bool                true：成功 false：失败
     * @brief 根据放大路口类型填充数据
     * @param[in] buff             路口大图二进制数据
     * @param[in] size             路口大图数据大小
     * @param[in] crossType        路口大图类型
     * @note thread：main
     */
    public boolean updateCross(byte[] buff, @CrossType.CrossType1 int crossType) {
        if (mRoadCrossControl == null) {
            return false;
        }
        Timber.d("====lane updateCross=========================");
        return mRoadCrossControl.updateCross(buff, crossType);
    }

    public void setCrossImageInfo(CrossImageInfo crossImageInfo) {
        if (mRoadCrossControl == null) {
            return;
        }
        Timber.d("====== setCrossImageInfo ======");
        mRoadCrossControl.setCrossImageInfo(crossImageInfo);
    }

    /**
     * @return void         无返回值
     * @brief 显示子点弹窗(去这里)
     */
    public void updatePopEndAreaPointBoxInfo(BizPopPointBusinessInfo popEnd) {
        ArrayList<BizPopPointBusinessInfo> popEnds = new ArrayList<>();
        popEnds.add(popEnd);
        mLabelControl.updatePopEndAreaPointBoxInfo(popEnds);
        mLabelControl.setFocus(BizLabelType.BizLabelTypeRoutePopEndArea, popEnd.id, true);
    }

    public void clearBizLabelTypeRoutePopEndArea() {
        mLabelControl.clearAllItems(BizLabelType.BizLabelTypeRoutePopEndArea);
    }

    /**
     * @return void         无返回值
     * @brief 根据放大路口类型隐藏对应的路口大图
     * @note thread：main
     */
    public void hideCross(@CrossType.CrossType1 int type) {
        if (mRoadCrossControl != null) {
            mRoadCrossControl.hideCross(type);
        }
    }

    /**
     * @return void   无返回值
     * @brief 根据放大路口类型进行显示隐藏控制
     * @note 只是显示或者隐藏控制，不涉及图片数据处理
     * @note thread：main
     */
    public void setRoadCrossVisible(@CrossType.CrossType1 int type, boolean bVisible) {
        Timber.d("====lane setRoadCrossVisible RoadCrossControl = %s, type = %s, visible = %s", mRoadCrossControl, type, bVisible);
        if (mRoadCrossControl != null) {
            mRoadCrossControl.setVisible(type, bVisible);
        }
    }

    /**
     * @brief 巡航车道线更新
     * @param[in] info            车道线信息
     * @note thread：ui
     */
    public void updateCruiseLane(LaneInfo info) {
        if (mLabelControl != null) {
            mLabelControl.updateCruiseLane(info);
        }
    }

    /**
     * @return void                     无返回值
     * @brief 导航分歧路口扎标
     * @param[in] vecMixForkInfo        导航分歧路口信息
     * @note thread：main
     */
    public void updateGuideMixForkInfo(ArrayList<MixForkInfo> vecMixForkInfo) {
        if (mLabelControl != null) {
            mLabelControl.updateGuideMixForkInfo(vecMixForkInfo);
        }
    }

    /**
     * @return void                      无返回值
     * @brief 更新导航交通设施图层
     * @param[in] vecNaviFacility        导航交通设施信息
     * @note thread：main
     */
    public void updateGuideRoadFacility(ArrayList<NaviRoadFacility> vecNaviFacility) {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.updateGuideRoadFacility(vecNaviFacility);
        }
    }

    /**
     * @return void                       无返回值
     * @brief 导航红绿灯倒计时图层显隐控制
     * @param[in] isVisible               红绿灯信号显隐状态
     */
    public void setVisibleTrafficSignalLight(boolean isVisible) {
        Timber.d("setVisibleTrafficSignalLight(), visible = %s", isVisible);
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.setVisible(BizRoadFacilityType.BizRoadFacilityTypeGuideTrafficSignalLight, isVisible);
            mRoadFacilityControl.enableTrafficSignalLight(isVisible);
        }
    }

    /**
     * @return void 无返回值
     * @brief 更新导航交通事件图层
     * @param[in] vecTrafficEventInfo        导航交通事件
     * @note thread：main
     */
    public void updateGuideTrafficEvent(ArrayList<TrafficEventInfo> vecTrafficEventInfo, boolean isPreview) {
        if (mRoadFacilityControl != null) {
            mRoadFacilityControl.updateGuideTrafficEvent(vecTrafficEventInfo, isPreview);
        }
    }

//    /**
//     * @brief                             巡航红绿灯倒计时图层显隐控制
//     * @param[in] isVisible               红绿灯信号显隐状态
//     * @return void                       无返回值
//     */
//    public void setVisibleCruiseSignalLight(boolean isVisible) {
//        Timber.d("setVisibleCruiseSignalLight(), visible = %s", isVisible);
//        if (mRoadFacilityControl != null) {
//            mRoadFacilityControl.enableLayer(BizRoadFacilityType.BizRoadFacilityTypeCruiseTrafficSignalLight, isVisible);
//            mRoadFacilityControl.setVisible(BizRoadFacilityType.BizRoadFacilityTypeCruiseTrafficSignalLight, isVisible);
//        }
//    }

    public boolean getVisible(int bizType) {
        if (mAreaControl != null && mAreaControl.matchBizControl(bizType)) {
            return mAreaControl.getVisible(bizType);
        }
        if (mGuideRouteControl != null && mGuideRouteControl.matchBizControl(bizType)) {
            return mGuideRouteControl.getVisible(bizType);
        }
        if (mRoadFacilityControl != null && mRoadFacilityControl.matchBizControl(bizType)) {
            return mRoadFacilityControl.getVisible(bizType);
        }
        if (mLabelControl != null && mLabelControl.matchBizControl(bizType)) {
            return mLabelControl.getVisible(bizType);
        }
        if (mSearchControl != null && mSearchControl.matchBizControl(bizType)) {
            return mSearchControl.getVisible(bizType);
        }
        return false;
    }

    public void setVisible(int bizType, boolean isVisible) {
        if (mAreaControl != null && mAreaControl.matchBizControl(bizType)) {
            mAreaControl.setVisible(bizType, isVisible);
        }
        if (mGuideRouteControl != null && mGuideRouteControl.matchBizControl(bizType)) {
            mGuideRouteControl.setVisible(bizType, isVisible);
        }
        if (mRoadFacilityControl != null && mRoadFacilityControl.matchBizControl(bizType)) {
            mRoadFacilityControl.setVisible(bizType, isVisible);
        }
        if (mLabelControl != null && mLabelControl.matchBizControl(bizType)) {
            mLabelControl.setVisible(bizType, isVisible);
        }
        if (mSearchControl != null && mSearchControl.matchBizControl(bizType)) {
            mSearchControl.setVisible(bizType, isVisible);
        }
    }

    /**
     * @brief 设置跟随模式、自由模式
     * @param[in] bFollow      true 跟随模式   false 自由模式
     * @note 跟随模式是用于当GPS信号输入的时候, 地图中心是否跟GPS位置同步变化; true：地图中心和车标同步变化；false：地图中心不跟车标一起变化；
     */
    public void setFollowMode(boolean bFollow) {
        mCarControl.setFollowMode(bFollow);
    }

    /**
     * @brief 设置预览模式
     * @param[in] bPreview  是否预览模式
     */
    public void setPreviewMode(boolean bPreview) {
        mCarControl.setPreviewMode(bPreview);
    }

    /**
     * @brief 设置车标模式，2D车标/3D车标/骨骼车标/车速车标
     * @param[in] carMode       车标模式
     * @param[in] bUpdateStyle  是否更新样式
     */
    public void setCarMode(@CarMode.CarMode1 int carMode, boolean bUpdateStyle) {
        mCarControl.setCarMode(carMode, bUpdateStyle);
    }

    /**
     * @brief 设置骨骼车标数据
     */
    public void setSkeletonData(AssetManager assetManager, int mCurrentCarLogos) {
        if (null != assetManager) {
            try {
                String str3DModelFile;
                SdkLocStatus locationStatus = LocationController.getInstance().getLocationStatus();
                String carNumber = "1";
                if (mCurrentCarLogos == 0) {
                    carNumber = "1";
                } else {
                    carNumber = String.valueOf(mCurrentCarLogos);
                }
                if (locationStatus == SdkLocStatus.ON_LOCATION_GPS_FAIl || locationStatus == SdkLocStatus.ON_LOCATION_FAIL) {
                    //获取定位失败,车标置灰
                    str3DModelFile = "car_skeleton_logo/" + carNumber + "/carLogoLow.dat";
                } else {
                    str3DModelFile = "car_skeleton_logo/" + carNumber + "/carLogo.dat";
                }
                InputStream input = null;
                try {
                    input = assetManager.open(str3DModelFile);
                    if (null != input) {
                        int len = input.available();
                        byte[] buffer = new byte[len];
                        input.read(buffer);
                        mCarControl.setSkeletonData(buffer);
                    }
                } finally {
                    if (input != null) {
                        input.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @brief 从下载主题中设置骨骼车标数据
     */
    public void setSkeletonDataByTheme(String path) {
        String str3DModelFile;
        SdkLocStatus locationStatus = LocationController.getInstance().getLocationStatus();
        if (locationStatus == SdkLocStatus.ON_LOCATION_GPS_FAIl || locationStatus == SdkLocStatus.ON_LOCATION_FAIL) {
            //获取定位失败,车标置灰
            str3DModelFile = "carLogoLow.dat";
        } else {
            str3DModelFile = "carLogo.dat";
        }
        byte[] skyboxData = FileUtils.file2Byte(new File(path, str3DModelFile));
        mCarControl.setSkeletonData(skyboxData);
    }

    public void initEagleEye(EagleEyeStyle eaglStyle) {
        if (null == mEagleEyePrepareLayerStyle) {
            Timber.e(new NullPointerException(), "initEagleEye  mEagleEyePrepareLayerStyle is null!");
            return;
        }
        mGuideEagleEyeControl.init(eaglStyle, mEagleEyePrepareLayerStyle);
        MapViewPortParam mapViewPortParam = new MapViewPortParam();
        mapViewPortParam.x = eaglStyle.mapViewParam.x;
        mapViewPortParam.y = eaglStyle.mapViewParam.y;
        mapViewPortParam.width = eaglStyle.mapViewParam.width;
        mapViewPortParam.height = eaglStyle.mapViewParam.height;
        mapViewPortParam.screenWidth = eaglStyle.mapViewParam.screenWidth;
        mapViewPortParam.screenHeight = eaglStyle.mapViewParam.screenHeight;
        updateEagleEyeParam(eaglStyle.eagleEyeParam);
        updateMapViewPort(mapViewPortParam);
        showEaglePath();
    }

    public void updateEaglePaths() {
        if (mGuideEagleEyeControl != null) {
            mGuideEagleEyeControl.updatePaths();
        }
    }

    public void uninitEagleEye() {
        if (mGuideEagleEyeControl != null) {
            mGuideEagleEyeControl.unInit();
        }
    }

    /**
     * @return bool     true：已初始化， false:未初始化
     * @brief 鹰眼地图是否已经被初始化
     */
    public boolean isGuideEagleEyeControlInitialized() {
        return mGuideEagleEyeControl != null && mGuideEagleEyeControl.isInitialized();
    }

    /**
     * @return void
     * @brief 更新鹰眼图相关展示参数，例如：当鹰眼图路线范围变更、纹理变更时都需要调用此接口来刷新
     * @param[in] param           鹰眼图展示参数
     */
    public void updateEagleEyeParam(EagleEyeParam param) {
        if (mGuideEagleEyeControl != null) {
            mGuideEagleEyeControl.updateEagleEyeParam(param);
        }
    }

    /**
     * @return 导航点偏移位置
     * @brief 计算导航点偏移位置
     * @param[in] isShowCrossImage     是否显示路口图
     */
    private int GetDynamicLevelTopOffsetValue(boolean isShowCrossImage) {
        long screenWidth = mMapView.getMapviewPort().screenWidth;
        long screenHeight = mMapView.getMapviewPort().screenHeight;
        float percent = 0.26f;
        if (screenWidth < screenHeight) {
            if (screenWidth < 930) {
                percent = isShowCrossImage ? 0.5f : 0.26f;
            } else {
                if (screenHeight > 930) {
                    percent = 0.05f;
                }
            }
        } else {
            if (screenHeight > 930) {
                percent = 0.05f;
            } else if (screenHeight >= 640) {
                percent = 0.1f;
            } else {
                percent = 0.26f;
            }
        }
        return (int) (screenHeight * percent);
    }

    /**
     * @brief 初始化动态比例尺, 调用动态比例尺方法之前需要初始化
     * @details 动态比例尺功能初始化
     * @note thread: main
     */
    public void initDynamicLevel() {
        if (mGuideRouteControl != null) {
            DynamicLevelParam param = new DynamicLevelParam();
            param.mLongDisToNaviPoint = 600;
            param.mSpeedWayMinLevel2DCarUp = 14.0f;
            param.mSpeedWayMinLevel3DCarUp = 14.0f;
            param.mPitchFar3DCarUp = 50.0f;
            param.mNaviPointOffsetToScreenTop = GetDynamicLevelTopOffsetValue(false);
            mGuideRouteControl.initDynamicLevel(param);
        }
    }

    /**
     * @brief 是否打开动态比例尺功能
     * @param[in] bOpen     打开或关闭动态比例尺
     * @param[in] type      自动比例尺类型(巡航/导航)，详见DynamicLevelType定义
     * @note thread: main
     */
    public void openDynamicLevel(boolean bOpen, @DynamicLevelType.DynamicLevelType1 int type) {
        Timber.d("openDynamicLevel %s", bOpen);
        if (mGuideRouteControl != null) {
            mGuideRouteControl.openDynamicLevel(bOpen, type);
            //设置地图中心点，不根据自动比例尺移动
            mGuideRouteControl.openDynamicCenter(false);
        }
    }

    public void openDynamicLevel(boolean bOpen) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.openDynamicLevel(bOpen);
        }
    }

    public void resetDynamicLevel(@DynamicLevelType.DynamicLevelType1 int type) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.resetDynamicLevel(type);
        }
    }

    public void setDynamicLevelLock(boolean isLock) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.setDynamicLevelLock(isLock);
        }
    }

    /**
     * @brief 显示鹰眼图路线（注意时序需要在绘制路线之后每次调用）
     */
    public void showEaglePath() {
        if (mGuideEagleEyeControl == null) {
            return;
        }

        RouteDrawStyle routeDrawStyle = new RouteDrawStyle();
        routeDrawStyle.mIsOffLine = !SdkNetworkUtil.getInstance().isNetworkConnected();
        routeDrawStyle.mIsNavi = true;
        routeDrawStyle.mRouteMapMode = BizRouteMapMode.BizRouteMapModeEagleEye;
        routeDrawStyle.mRouteScene = RouteLayerScene.RouteLayerSceneNormal;
        setEagleVisible(false);
        mGuideEagleEyeControl.setPassGreyMode(true);
        mGuideEagleEyeControl.drawPath(routeDrawStyle);
        mGuideEagleEyeControl.updateStyle(NightModeGlobal.isNightMode());
    }

    /**
     * @brief 设置车标当前位置信息
     * @param[in] carLoc      车标位置信息
     * @note thread：multi
     */
    public void setCarPosition(CarLoc carLoc) {
        if (mCarControl != null) {
            mCarControl.setCarPosition(carLoc);
        }
    }

    public void setCarVisible(boolean bVisible) {
        if (mCarControl != null) {
            mCarControl.setVisible(bVisible);
        }
    }

    /**
     * @brief 更新车标Style,"car_layer_style"
     * @param[in] nBusinessType   车标图层业务类型,BizCarType
     * @note thread：multi
     */
    public void updateCarStyle(@BizCarType.BizCarType1 int nBusinessType) {
        if (null != mCarControl) {
            mCarControl.updateStyle(nBusinessType);
        }
    }

    /**
     * @return void
     * @brief 更新鹰眼图展示位置
     * @param[in] portParam       地图视角位置参数
     */
    public void updateMapViewPort(MapViewPortParam portParam) {
        if (mGuideEagleEyeControl != null) {
            mGuideEagleEyeControl.updateMapViewPort(portParam);
        }
    }

    /**
     * @return void            无返回值
     * @brief 控制鹰眼显隐
     * @note thread：multi
     */
    public void setEagleVisible(boolean bVisible) {
        if (mGuideEagleEyeControl != null) {
            mGuideEagleEyeControl.setVisible(bVisible);
        }
    }

    public RectDouble getPathPointsBound(RoutePoints pathPoints) {
        if (mGuideRouteControl == null) {
            RectDouble ret = new RectDouble();
            return ret;
        }
        return BizGuideRouteControl.getPathPointsBound(pathPoints);
    }

    public RectDouble getPathResultBound(ArrayList<PathInfo> pathResult) {
        if (mGuideRouteControl == null) {
            RectDouble ret = new RectDouble();
            return ret;
        }
        return BizGuideRouteControl.getPathResultBound(pathResult);
    }


    public void addClickObserver(ILayerClickObserver observer) {
        if (null != mGuideRouteControl) {
            mGuideRouteControl.addClickObserver(observer);
        }
        if (null != mAreaControl) {
            mAreaControl.addClickObserver(observer);
        }
    }

    public void removeClickObserver(ILayerClickObserver observer) {
        if (null != mGuideRouteControl) {
            mGuideRouteControl.removeClickObserver(observer);
        }
        if (null != mAreaControl) {
            mAreaControl.removeClickObserver(observer);
        }
    }


    /**
     * 设置近接/混淆矢量大图的姿态事件
     *
     * @param type         路口大图类型
     * @param postureEvent 参数值参见VectorCrossViewPostureEvent类型定义
     */
    public void setViewPostureEvent(@CrossType.CrossType1 int type, @VectorCrossViewPostureEvent.VectorCrossViewPostureEvent1 int postureEvent) {
        if (mRoadCrossControl == null) {
            return;
        }
        mRoadCrossControl.setViewPostureEvent(type, postureEvent);
    }

    /**
     * 清除引导路线上的转向图标图层
     */
    public void clearBizRouteTypeArrowLayer() {
        if (mGuideRouteControl == null) {
            return;
        }
        mGuideRouteControl.clearAllItems(BizRouteType.BizRouteTypeArrow);
    }

    public void clearAllPaths() {
        clearPathsAsync();

//        if (mAreaControl != null) {
//            mAreaControl.clearAllItems();
//        }
    }

    public void clearGuidePaths() {
        clearPathsAsync();
        //更新样式，控制预计到达时间显隐
        mAreaControl.updateStyle(BizAreaType.BizAreaTypeEndAreaParentPoint);
    }

    public void clearAreaAllItems() {
        if (mAreaControl != null) {
            mAreaControl.clearAllItems();
        }
    }

    public boolean isShowPreview() {
        return mIsShowPreview;
    }

    public void setIsShowPreview(boolean mIsShowPreview) {
        this.mIsShowPreview = mIsShowPreview;
    }

    public void setMultiScreenOverlayType(int type) {
        if (getSurfaceViewID() == SurfaceViewID.SURFACE_VIEW_ID_MAIN) {
            return;
        }
        if (type == 1) {
            mGuideEagleEyeControl = null;
        }
    }

    /**
     * 设置走过的途经点置灰
     *
     * @param passGrey 是否走过置灰，默认不置灰：true:置灰， false:不置灰
     */
    public void setViaPassGreyMode(boolean passGrey) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.setViaPassGreyMode(passGrey);
        }
    }

    /**
     * 在线转离线导航页更新样式
     *
     * @param mIsOffLine true为离线，false为在线
     */
    public void setSwitchOffline(boolean mIsNavi, boolean mIsOffLine) {
        if (mGuideRouteControl != null) {
            // 路线绘制风格
            RouteDrawStyle mainRouteDrawStyle = new RouteDrawStyle();
            // 是否导航 路径规划中为false
            mainRouteDrawStyle.mIsNavi = mIsNavi;
            // 是否离线
            mainRouteDrawStyle.mIsOffLine = mIsOffLine;
            // 图模式 主图 或 鹰眼
            mainRouteDrawStyle.mRouteMapMode = BizRouteMapMode.BizRouteMapModeMain;
            // 路线业务场景 单指线
            mainRouteDrawStyle.mRouteScene = RouteLayerScene.RouteLayerSceneNormal;
            // 是否是多备选模式
            mainRouteDrawStyle.mIsMultipleMode = true;
            mGuideRouteControl.setPathDrawStyle(mainRouteDrawStyle);
            mGuideRouteControl.updateStyle();
            if (mIsNavi) {
                if (mGuideEagleEyeControl != null) {
                    RouteDrawStyle routeDrawStyle = new RouteDrawStyle();
                    routeDrawStyle.mIsOffLine = mIsOffLine;
                    routeDrawStyle.mIsNavi = true;
                    routeDrawStyle.mRouteMapMode = BizRouteMapMode.BizRouteMapModeEagleEye;
                    routeDrawStyle.mRouteScene = RouteLayerScene.RouteLayerSceneNormal;
                    mGuideEagleEyeControl.drawPath(routeDrawStyle);
                    mGuideEagleEyeControl.updateStyle(NightModeGlobal.isNightMode());
                }
            }
        }
    }

    public void clearPathsAsync() {
        TaskManager.runSingleThread(new Runnable() {
            @Override
            public void run() {
                if (mGuideRouteControl != null) {
                    mGuideRouteControl.clearPaths();
                }
            }
        });
    }

    public void clearAreaControl(long bizType) {
        if (mAreaControl != null) {
            mAreaControl.clearAllItems(bizType);
        }
    }

    /**
     * 设置导航多备选标签是否显示
     *
     * @param visible
     */
    public void setVisibleGuideLabel(boolean visible) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.setVisible(BizRouteType.BizRouteTypeGuideLabel, visible);
        }
    }

}
