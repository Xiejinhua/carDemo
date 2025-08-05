package com.autosdk.bussiness.layer;

import static com.autonavi.gbl.layer.model.BizCarType.BizCarType1;

import android.content.res.AssetManager;

import androidx.annotation.IntDef;

import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.layer.BizAreaControl;
import com.autonavi.gbl.layer.BizCarControl;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizFlyLineControl;
import com.autonavi.gbl.layer.model.BizCircleBusinessInfo;
import com.autonavi.gbl.layer.model.BizClickLabelType;
import com.autonavi.gbl.layer.model.FlylineDrawMode;
import com.autonavi.gbl.layer.model.RangeOnMapPolygonInfo;
import com.autonavi.gbl.layer.model.SkeletonCarStatus;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.model.CarLoc;
import com.autonavi.gbl.map.layer.model.CarMode;
import com.autonavi.gbl.map.layer.model.PathMatchInfo;
import com.autonavi.gbl.map.layer.observer.ICarObserver;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.location.constant.SdkLocStatus;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * @brief 定义类MapLayerImpl, 地图相关图层业务实现
 */
public class MapLayer extends HMIBaseLayer {

    public static String TAG = MapLayer.class.getSimpleName();
    private BizCarControl mCarControl;


    private BizFlyLineControl mBizFlyLineControl;

    private BizAreaControl mBizAreaControl;

    private Coord3DDouble mapCenter;


    /**
     * < 移图选点页的飞线纹理类型
     */
    public static final int FLYLINE_SCENE_TYPE_SELECT_POI = 0;
    /**
     * < 搜索结果页显示搜索结果列表时的飞线纹理类型
     */
    public static final int FLYLINE_SCENE_TYPE_RESULT_LIST = 1;
    /**
     * < 搜索结果页显示POI详情时的飞线纹理类型
     */
    public static final int FLYLINE_SCENE_TYPE_RESULT_DETAIL = 2;

    @IntDef({FLYLINE_SCENE_TYPE_RESULT_LIST, FLYLINE_SCENE_TYPE_RESULT_DETAIL, FLYLINE_SCENE_TYPE_SELECT_POI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlyLineSceneType {
    }

    private @FlyLineSceneType
    int mFlyLineType = FLYLINE_SCENE_TYPE_SELECT_POI;
    private boolean mIsFlyShow;

    private float mMapLevel;

    /**
     * @brief 初始化所有control
     */
    protected MapLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != mapView) {
            mCarControl = bizService.getBizCarControl(mapView);
            mBizFlyLineControl = bizService.getBizFlyLineControl(mapView);
            mBizAreaControl = bizService.getBizAreaControl(mapView);
        }
    }

    /**
     * @brief 获取车标图层
     */
    public BizCarControl getCarControl() {
        return mCarControl;
    }

    /**
     * @brief 设置车标观察者
     */
    public void addCarObserver(ICarObserver pObserver) {
        if (mCarControl != null) {
            mCarControl.addCarObserver(pObserver);
        }
    }

    /**
     * @brief 设置车标观察者
     */
    public void removeCarObserver(ICarObserver pObserver) {
        if (mCarControl != null) {
            mCarControl.removeCarObserver(pObserver);
        }
    }

    /**
     * @brief 设置车辆坐标方位
     * @param[in] cardir          方位
     */

    public void setCarPosition(double lon, double lat, float cardir) {
        if (mCarControl != null) {
            CarLoc carLoc = new CarLoc();
            PathMatchInfo info = new PathMatchInfo();
            info.longitude = lon;
            info.latitude = lat;
            info.carDir = cardir;
            carLoc.vecPathMatchInfo.add(info);
            mCarControl.setCarPosition(carLoc);
        }
    }

    public void updateCarStyle() {
        if (null != mCarControl) {
            mCarControl.updateStyle();
        }
    }

    /**
     * @brief 通知更新Style,"car_layer_style"
     * @param[in] nBusinessType   具体Auto图层业务类型,BizCarType
     * @note thread：multi
     */
    public void updateCarStyle(@BizCarType1 int nBusinessType) {
        if (null != mCarControl) {
            mCarControl.updateStyle(nBusinessType);
        }
    }

    public void setSkeletonBaseScale(float baseScale) {
        if (null != mCarControl) {
            mCarControl.setSkeletonBaseScale(baseScale);
        }
    }

    public void setSkeletonCarStatus(@SkeletonCarStatus.SkeletonCarStatus1 int status) {
        if (null != mCarControl) {
            mCarControl.setSkeletonCarStatus(status);
        }
    }

    /**
     * @return void         无返回值
     * @brief 控制车标图层显隐
     * @param[in] bVisible  true:显示 false:隐藏
     * @note thread：multi
     */
    public void setCarVisible(boolean bVisible) {
        if (null != mCarControl) {
            mCarControl.setVisible(bVisible);
        }
    }

    /**
     * @brief 设置跟随模式、自由模式
     * @param[in] bFollow   true 跟随模式   false 自由模式
     * @note 跟随模式是用于当GPS信号输入的时候, 地图中心是否跟GPS位置同步变化; true：地图中心和车标同步变化；false：地图中心不跟车标一起变化；
     * @note thread：multi
     */
    public void setFollowMode(boolean bFollow) {

        if (null != mCarControl) {
            mCarControl.setFollowMode(bFollow);
        }
    }

    public void setCarScaleByMapLevel(float[] vScales) {
        if (null != mCarControl) {
            mCarControl.setCarScaleByMapLevel(vScales);
        }
    }

    /**
     * @brief 设置车标模式，2D车标/3D车标/骨骼车标/车速车标
     * @param[in] carMode   车标模式
     * @param[in] bUpdateStyle  是否更新样式
     * @note thread：multi
     */
    public void setCarMode(@CarMode.CarMode1 int carMode, boolean bUpdateStyle) {
        if (null != mCarControl) {
            mCarControl.setCarMode(carMode, bUpdateStyle);
        }
    }

    /**
     * @brief 设置锁定地图角度模式
     * @param bLockAngle true：定位信号不影响地图旋转角    false：解除锁定控制
     */
    public void setLockMapRollAngle(boolean bLockAngle) {
        if (null != mCarControl) {
            mCarControl.setLockMapRollAngle(bLockAngle);
        }
    }


    /**
     * @brief 设置骨骼车标数据
     */
    public void setSkeletonData(AssetManager assetManager, int mCurrentCarLogos) {
        if (null != assetManager) {
            try {
                String str3DModelFile;
                SdkLocStatus locationStatus = LocationController.getInstance().getLocationStatus();
                Timber.i("setSkeletonData mCurrentCarLogos = %s", mCurrentCarLogos);
                String carNumber;
                if (mCurrentCarLogos == 0) {
                    carNumber = "1";
                } else {
                    carNumber = String.valueOf(mCurrentCarLogos);
                }
                if (locationStatus == SdkLocStatus.ON_LOCATION_GPS_FAIl || locationStatus == SdkLocStatus.ON_LOCATION_FAIL) {
                    //获取定位失败,车标置灰
                    str3DModelFile = "car_skeleton_logo/" + carNumber + "/carLogo.dat";
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
                        Timber.i("setSkeletonData buffer size %s", buffer.length);
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

    /**
     * @brief 设置预览模式
     * @param[in] bPreview  是否预览模式
     * @note thread：multi
     */
    public void setPreviewMode(boolean bPreview) {
        if (null != mCarControl) {
            mCarControl.setPreviewMode(bPreview);
        }
    }

    public BizFlyLineControl getBizFlyLineControl() {
        return mBizFlyLineControl;
    }

    /**
     * 控制飞线显示隐藏
     *
     * @param isShow true显示 false 隐藏
     */
    public void showFlyLine(boolean isShow) {
        Timber.i("showFlyLine = " + isShow);
        if (null != mBizFlyLineControl) {
            mIsFlyShow = isShow;
            mBizFlyLineControl.setVisible(isShow, isShow);
        }
    }

    public boolean getFlyLineVisible() {
        return mIsFlyShow;
    }


    public void setFlyClickLabelType(@BizClickLabelType.BizClickLabelType1 int labelType) {
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.setClickLabelType(labelType);
        }
    }

    public @BizClickLabelType.BizClickLabelType1
    int getFlyClickLabelType() {
        int labelType = 0;
        if (null != mBizFlyLineControl) {
            labelType = mBizFlyLineControl.getClickLabelType();
        }

        return labelType;
    }

    /**
     * 清除飞线
     */
    public void clearFlyLineOnce() {
        Timber.i("clearFlyLineOnce");
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.hideOnce();
        }
    }

    public boolean refreshFlyLineDrawing(@FlylineDrawMode.FlylineDrawMode1 int drawMode, boolean bAnim) {
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.updateDrawMode(drawMode, bAnim);
        }
        return true;
    }

    public void setFlyEndPointEnable(boolean endable) {
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.setClickable(endable);
        }
    }

    /**
     * 设置飞线绘制模式
     *
     * @param mode 飞线绘制模式
     */
    public void setFlyLineMode(@FlylineDrawMode.FlylineDrawMode1 int mode) {
        Timber.i("setFlyLineType");
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.updateDrawMode(mode, true);
        }
    }

    /**
     * 设置飞线类型
     *
     * @param type BizFlyLineTypeLine 线形飞线 BizFlyLineTypePoint 飞线点
     */
    public void setFlyLineSceneType(@FlyLineSceneType int type) {
        Timber.i("setFlyLineType");
        if (null != mBizFlyLineControl) {
            mFlyLineType = type;
            mBizFlyLineControl.updateStyle();
        }
    }

    /**
     * 丢弃地图中心，搜索切换城市需要
     */
    public void abandonMapCenter() {
        mapCenter = null;
    }

    /**
     * 缓存地图中心
     */
    public void saveMapCenter() {
        mapCenter = SDKManager.getInstance().getMapController().getMapView(getSurfaceViewID()).getOperatorPosture().getMapCenter();
    }

    /**
     * 恢复地图中心
     */
    public void restoreMapCenter() {
        if (null == mapCenter) {
            return;
        }
        SDKManager.getInstance().getMapController().getMapView(getSurfaceViewID()).getOperatorPosture().setMapCenter(mapCenter);
    }



    public int getFlyLineScenceType() {
        return mFlyLineType;
    }

    public void setClickLabelMoveMap(boolean moveMap) {
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.setClickLabelMoveMap(moveMap);
        }
    }

    public void updateFlyLineStyle() {
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.updateStyle();
        }
    }

    public int getFlyLineDrawMode() {
        int nMode = 0;
        if (null != mBizFlyLineControl) {
            nMode = mBizFlyLineControl.getDrawMode();
        }

        return nMode;
    }

    public void addFlyLineClickObserver(ILayerClickObserver pObserver) {
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.addClickObserver(pObserver);
        }
    }

    public void removeFlyLineClickObserver(ILayerClickObserver pObserver) {
        if (null != mBizFlyLineControl) {
            mBizFlyLineControl.removeClickObserver(pObserver);
        }
    }

    public void updateRangeOnMapPolygon(ArrayList<RangeOnMapPolygonInfo> polygonInfoList) {
        if (null != mBizAreaControl) {
            mBizAreaControl.updateRangeOnMapPolygon(polygonInfoList);
        }
    }

    public void clearRangeOnMapLayer() {
        if (null != mBizAreaControl) {
            mBizAreaControl.clearRangeOnMapLayer();
        }
    }

    public void updateRangeOnMapCircle(BizCircleBusinessInfo circleInfo) {
        if (null != mBizAreaControl) {
            mBizAreaControl.updateRangeOnMapCircle(circleInfo);
        }
    }

    public void saveMapLevel(){
        mMapLevel = SDKManager.getInstance().getMapController().getMapView(getSurfaceViewID()).getOperatorPosture().getZoomLevel();
    }

    public void restoreMapLevel(){
        if (mMapLevel <= 0){
            return;
        }
        SDKManager.getInstance().getMapController().getMapView(getSurfaceViewID()).getOperatorPosture().setZoomLevel(mMapLevel);
    }

}
