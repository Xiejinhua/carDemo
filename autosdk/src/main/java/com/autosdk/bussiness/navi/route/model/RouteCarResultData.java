package com.autosdk.bussiness.navi.route.model;

import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.common.path.option.RouteStrategy;
import com.autonavi.gbl.guide.model.SceneFlagType;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.navi.route.utils.RouteLifecycleMonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

/**
 * 用于存放路线结果（包含了行程点信息）
 */
public class RouteCarResultData implements IRouteResultData, Serializable {

    /**
     * 途经点
     */
    private ArrayList<POI> mMidPois;
    /**
     * 车牌
     */
    private String mCarPlate;
    /**
     * 是否避开限行
     */
    private boolean mOpenAvoidLimit = false;
    /**
     * 起点
     */
    private POI mFromPoi = null;
    /**
     * 终点
     */
    private POI mToPoi = null;
    /**
     * BL 透传上来的路线结果
     */
    private PathResultDataInfo mPathResultDataInfo;

    /**
     * 路线偏好
     * {@link com.autonavi.gbl.common.path.option.RouteStrategy}
     */
    @RouteStrategy.RouteStrategy1
    private int mRouteStrategy;

    /**
     * 算路附加要求
     * {@link com.autonavi.gbl.common.path.option.RouteConstrainCode}
     */
    private int mRouteConstrainCode;

    private boolean mIsLocal;

    /**
     * 当前选中的路径
     */
    private final AtomicInteger mFocusIndex  = new AtomicInteger(0);

    private boolean mIsCarSceneResult;

    /**
     * 是否包含限行信息
     */
    private boolean mHasRestricted;

    /**
     * 用于存放路线途径的城市adcode
     */
    private ArrayList<Long> mCityCodes;

    /**
     * 多屏路线数据标识码，以当前包含路线的id生成
     */
    private ArrayList<String> mArrayPathId;

    /**
     * 避让路段
     */
    private ArrayList<Long> mAvoidLinks;

    private ArrayList<PathInfo> mPreviousPathInfo;

    private int mSceneFlagType = SceneFlagType.SceneFlagTypeNormal;

    private boolean mIsSend2car = false;

    public static boolean isValid(RouteCarResultData routeResult) {
        return null != routeResult && null != routeResult.getPathResult() && !routeResult.getPathResult().isEmpty();
    }

    public static PathInfo getFocusPathInfo(RouteCarResultData routeResult) {
        if (!isValid(routeResult)) {
            return null;
        }
        return routeResult.getPathResult().get(routeResult.getFocusIndex());
    }

    @Override
    public POI getFromPOI() {
        return mFromPoi;
    }

    @Override
    public void setFromPOI(POI fromPOI) {
        this.mFromPoi = fromPOI;
    }

    @Override
    public POI getToPOI() {
        return mToPoi;
    }

    @Override
    public void setToPOI(POI toPOI) {
        mToPoi = toPOI;
    }

    @Override
    public ArrayList<POI> getMidPois() {
        return mMidPois;
    }

    @Override
    public void setMidPois(ArrayList<POI> pois) {
        mMidPois = pois;
    }

    @Override
    public int getRouteStrategy() {
        return mRouteStrategy;
    }

    @Override
    public void setRouteStrategy(int m) {
        mRouteStrategy = m;
    }

    @Override
    public int getRouteConstrainCode() {
        return mRouteConstrainCode;
    }

    @Override
    public void setRouteConstrainCode(int m) {
        mRouteConstrainCode = m;
    }

    @Override
    public boolean isOffline() {
        return mIsLocal;
    }

    @Override
    public void setIsOffline(boolean isOffline) {
        Timber.i("isOffline = %s", isOffline);
        this.mIsLocal = isOffline;
    }

    @Override
    public boolean hasMidPos() {
        if (mMidPois != null && mMidPois.size() > 0) {
            return true;
        }
        return false;
    }

    public PathResultDataInfo getPathResultDataInfo() {
        return mPathResultDataInfo;
    }

    public void setPathResultDataInfo(PathResultDataInfo pathResultDataInfo) {
        this.mPathResultDataInfo = pathResultDataInfo;
    }

    public ArrayList<String> getArrayPathId() {
        return mArrayPathId;
    }

    public void setArrayPathId(ArrayList<String> mArrayPathId) {
        this.mArrayPathId = mArrayPathId;
    }

    public ArrayList<PathInfo> getPathResult() {
        return RouteLifecycleMonitor.getInstance().getPathResult();
    }

    public PathInfo getPathResultById(long pathId) {
        if (pathId <= 0 || getPathResult().isEmpty()) return null;

        for (PathInfo info : getPathResult()) {
            if (info.getPathID() == pathId) return info;
        }

        return null;
    }

    public void setPathResult(ArrayList<PathInfo> pathResult) {
        RouteLifecycleMonitor.getInstance().setPathResult(pathResult);
    }

    public void setFocusIndex(int index) {
        Timber.i("setFocusIndex index = %s", index);
        mFocusIndex.set(index);
    }

    public int getFocusIndex() {
        return mFocusIndex.get();
    }

    @Override
    public boolean isSceneResult() {
        return mIsCarSceneResult;
    }

    @Override
    public void setSceneResult(boolean isCarScene) {
        mIsCarSceneResult = isCarScene;
    }

    public String getCarPlate() {
        return mCarPlate;
    }

    public void setCarPlate(String carPlate) {
        this.mCarPlate = carPlate;
    }

    public int getSceneFlagType() {
        return mSceneFlagType;
    }

    public void setSceneFlagType(int mSceneFlagType) {
        this.mSceneFlagType = mSceneFlagType;
    }

    /**
     * 设置是否有限行信息
     */
    public void setHasRestricted(boolean hasRestricted) {
        mHasRestricted = hasRestricted;
    }

    /**
     * 获取是否有限行信息
     *
     * @return
     */
    public boolean hasRestricted() {
        return mHasRestricted;
    }

    /**
     * 设置路线途径城市
     *
     * @param cityCodes
     */
    public void setCityCodes(ArrayList<Long> cityCodes) {
        mCityCodes = cityCodes;
    }

    /**
     * 获取路线途径城市
     *
     * @return
     */
    public ArrayList<Long> getCityCodes() {
        return mCityCodes;
    }

    /**
     * 是否开启避让
     *
     * @return
     */
    public boolean isOpenAvoidLimit() {
        return mOpenAvoidLimit;
    }

    /**
     * 设置避让
     *
     * @param openAvoidLimit
     */
    public void setOpenAvoidLimit(boolean openAvoidLimit) {
        this.mOpenAvoidLimit = openAvoidLimit;
    }

    /**
     * 获取规避路段
     *
     * @return
     */
    public ArrayList<Long> getAvoidLinks() {
        return mAvoidLinks;
    }

    /**
     * 设置规避路段
     *
     * @param avoidLinks
     */
    public void setAvoidLinks(ArrayList<Long> avoidLinks) {
        this.mAvoidLinks = avoidLinks;
    }

    /**
     * 设置send2car标识
     *
     * @param isSend2car
     */
    public void setSend2car(boolean isSend2car) {
        mIsSend2car = isSend2car;
    }

    public boolean isSend2car() {
        return mIsSend2car;
    }
}
