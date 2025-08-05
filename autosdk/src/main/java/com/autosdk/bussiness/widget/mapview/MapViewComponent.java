package com.autosdk.bussiness.widget.mapview;

import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.autonavi.gbl.aosclient.model.GRangeSpiderAckLineGroup;
import com.autonavi.gbl.aosclient.model.GRangeSpiderAckLineGroupIsolineBbox;
import com.autonavi.gbl.aosclient.model.GRangeSpiderPoint;
import com.autonavi.gbl.aosclient.model.GRangeSpiderResponseParam;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.layer.BizLabelControl;
import com.autonavi.gbl.layer.model.BizCarType;
import com.autonavi.gbl.layer.model.BizCircleBusinessInfo;
import com.autonavi.gbl.layer.model.BizLabelType;
import com.autonavi.gbl.layer.model.BizLayerUtil;
import com.autonavi.gbl.layer.model.BizPointBusinessInfo;
import com.autonavi.gbl.layer.model.RangeOnMapPolygonInfo;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.model.CarMode;
import com.autonavi.gbl.map.model.PreviewParam;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.pos.model.LocMatchInfo;
import com.autosdk.bussiness.layer.LayerController;
import com.autosdk.bussiness.layer.MapLayer;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;
import com.autosdk.bussiness.widget.setting.SettingComponent;
import com.autosdk.common.AutoConstant;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by AutoSdk on 2021/7/12.
 **/
public class MapViewComponent {
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (timerCallBack != null) {
                timerCallBack.finish();
            }
            removeBackToCarTime();
        }
    };

    private Runnable updateRunnable;

    private MapViewComponent() {

    }

    private static MapViewComponent instance;

    public static MapViewComponent getInstance() {
        if (instance == null) {
            instance = new MapViewComponent();
        }
        return instance;
    }

    /**
     * 开始倒计时
     *
     * @param time
     * @param timerCallBack
     */
    public void backToCarTimer(long time, TimerCallBack timerCallBack) {
        this.timerCallBack = timerCallBack;
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, time);
    }

    public void rangeMapTimer(long time, Runnable updateRunnable) {
        this.updateRunnable = updateRunnable;
        handler.postDelayed(this.updateRunnable, time);
    }

    public void removeRangeMapTimer() {
        handler.removeCallbacks(updateRunnable);
    }

    /**
     * 移除计时
     */
    public void removeBackToCarTime() {
        handler.removeCallbacks(runnable);
    }

    TimerCallBack timerCallBack;

    public interface TimerCallBack {
        void finish();
    }

    /**
     * 设置车标模型 ，且为跟随状态
     */
    public void setMainMapCarMode(String path, MapLayer mMapLayer) {
        if (null != mMapLayer) {
            if (AutoConstant.SUPPORT_3D_CAR_LOGO) {
                int scale = SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getOperatorScale().getCurrentScale();
                if (scale >= 1000) {
                    mMapLayer.setCarMode(CarMode.CarMode2D, false);
                    mMapLayer.updateCarStyle(BizCarType.BizCarTypeCruise);
                    return;
                }
                //个性化车标
                int mCurrentCarLogos = SettingComponent.getInstance().getCarLogos();
                if (mCurrentCarLogos == 3) {
                    //车速车标
                    mMapLayer.setCarMode(CarMode.CarModeSpeed, false);
                } else {
                    if ((mCurrentCarLogos != 1 && mCurrentCarLogos != 0) || TextUtils.isEmpty(path)) {
                        mMapLayer.setSkeletonData(BusinessApplicationUtils.getApplication().getAssets(), mCurrentCarLogos);
                    } else {
                        mMapLayer.setSkeletonDataByTheme(path);
                    }
                    Timber.i("setMainMapCarMode CarModeSkeleton ");
                    mMapLayer.setCarMode(CarMode.CarModeSkeleton, false);
                }
                Timber.i("setMainMapCarMode updateCarStyle ");
                mMapLayer.updateCarStyle(BizCarType.BizCarTypeCruise);
            } else {
                mMapLayer.setCarMode(CarMode.CarMode2D, false);
                mMapLayer.updateCarStyle(BizCarType.BizCarTypeCruise);
            }
        }
    }

    public void showRangeMapPreview(RectDouble rectDouble) {
        PreviewParam previewParam = new PreviewParam();
        previewParam.mapBound = rectDouble;
        previewParam.bUseRect = true;
        SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).showPreview(previewParam, true, 500, -1);
    }

    /**
     * 失败 使用离线
     *
     * @param mMapLayer
     * @param isUpdateRange
     */
    public void showOffLineRangeMap(MapLayer mMapLayer, boolean isUpdateRange, double radius) {
        BizCircleBusinessInfo businessInfo = new BizCircleBusinessInfo();
        businessInfo.radius = radius;
        businessInfo.isDrawRim = true;
        businessInfo.isDrawPoint = true;
        Location location = LocationController.getInstance().getLastLocation();
        businessInfo.center = new Coord3DDouble(location.getLongitude(), location.getLatitude(), 0.0);
        RectDouble rectDouble = BizLayerUtil.getRect(businessInfo.center, businessInfo.radius + businessInfo.radius / 3);
        if (!isUpdateRange) {
            showRangeMapPreview(rectDouble);
        }
        mMapLayer.updateRangeOnMapCircle(businessInfo);
    }

    /**
     * 在线
     *
     * @param responseParam
     * @param mMapLayer
     * @param isUpdateRange
     */
    public void showRangeMap(GRangeSpiderResponseParam responseParam, MapLayer mMapLayer, boolean isUpdateRange) {
        if (responseParam.linegroup == null || responseParam.linegroup.size() == 0) {
            return;
        }
        GRangeSpiderAckLineGroup lineGroup = responseParam.linegroup.get(0);
        ArrayList<RangeOnMapPolygonInfo> polygonInfoList = new ArrayList<>();
        for (int i = 0; i < lineGroup.isoline.size(); i++) {
            if (lineGroup.isoline.get(i).component == null || lineGroup.isoline.get(i).component.size() == 0) {
                continue;
            }
            GRangeSpiderAckLineGroupIsolineBbox bbox = lineGroup.isoline.get(i).bbox;
            if (i == 0 && !isUpdateRange) {
                RectDouble rectDouble = new RectDouble();
                rectDouble.bottom = bbox.bottomright.lat;
                rectDouble.top = bbox.topleft.lat;
                rectDouble.right = bbox.bottomright.lon;
                rectDouble.left = bbox.topleft.lon;
                showRangeMapPreview(rectDouble);
            }
            ArrayList<GRangeSpiderPoint> shape = lineGroup.isoline.get(i).component.get(0).shape;
            RangeOnMapPolygonInfo rangeOnMapPolygonInfo = new RangeOnMapPolygonInfo();
            for (int j = 0; j < shape.size(); j++) {
                rangeOnMapPolygonInfo.mVecPoints.add(new Coord3DDouble(shape.get(j).lon, shape.get(j).lat, 0));
            }
            rangeOnMapPolygonInfo.id = "" + i;
            rangeOnMapPolygonInfo.mDrawPoint = true;
            rangeOnMapPolygonInfo.mDrawPolygonRim = true;
            rangeOnMapPolygonInfo.mEnergy = (float) lineGroup.isoline.get(i).energy;
            polygonInfoList.add(rangeOnMapPolygonInfo);
        }
        mMapLayer.updateRangeOnMapPolygon(polygonInfoList);
    }

    ArrayList<BizPointBusinessInfo> bizPointBusinessInfoArrayList = new ArrayList<>();
    long lastLocTime = 0;

    public void showRealGpsTrack(LocInfo locInfo) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - lastLocTime > 1000) { //1s处理一次
            lastLocTime = nowTime;
        } else {
            return;
        }
        MapView mapView = SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (mapView == null) {
            return;
        }
        BizLabelControl bizLabelControl = LayerController.getInstance().getBizControlService().getBizLabelControl(mapView);
        if (bizLabelControl == null) {
            return;
        }
        BizPointBusinessInfo gpsPoint = new BizPointBusinessInfo();
        ArrayList<LocMatchInfo> matchInfo = locInfo.matchInfo;
        int matchInfoSize = matchInfo == null ? 0 : matchInfo.size();
        if (matchInfoSize > 0) {
            LocMatchInfo locMatchInfo = matchInfo.get(0);
            gpsPoint.id = nowTime + "";
            gpsPoint.mPos3D.lat = locMatchInfo.stPos.lat;
            gpsPoint.mPos3D.lon = locMatchInfo.stPos.lon;
            if (bizPointBusinessInfoArrayList.size() > 10) {
                bizPointBusinessInfoArrayList.remove(0);
            }
            bizPointBusinessInfoArrayList.add(gpsPoint);
            bizLabelControl.updateGpsPointsInfo(bizPointBusinessInfoArrayList);
        }
    }

    public void clearGpsTrack() {
        MapView mapView = SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (mapView == null) {
            return;
        }
        BizLabelControl bizLabelControl = LayerController.getInstance().getBizControlService().getBizLabelControl(mapView);
        if (bizLabelControl == null) {
            return;
        }
        BaseLayer baseLayer = bizLabelControl.getLabelLayer(BizLabelType.BizLabelTypeGpsPoints);
        if (baseLayer == null) {
            return;
        }
        baseLayer.clearAllItems();
    }

    private CustomCountDownTimer mCountDownTimer;
    private TimerCallBack mRefreshTimerCallback;

    public void startRefreshTime(long millisInFuture, TimerCallBack timerCallBack) {
        this.mRefreshTimerCallback = timerCallBack;
        if (mCountDownTimer == null) {
            mCountDownTimer = new CustomCountDownTimer(millisInFuture, 1000);
        }
        mCountDownTimer.start();
    }

    public void stopRefreshTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private class CustomCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (mRefreshTimerCallback != null) {
                mRefreshTimerCallback.finish();
            }
            if (mCountDownTimer != null) {
                mCountDownTimer.start();
            }
        }
    }
}
