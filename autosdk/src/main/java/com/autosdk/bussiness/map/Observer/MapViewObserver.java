package com.autosdk.bussiness.map.Observer;

import android.util.Log;

import com.autonavi.gbl.map.MapDevice;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.model.GuideRoadNameBoardParam;
import com.autonavi.gbl.map.model.IndoorBuilding;
import com.autonavi.gbl.map.model.MapLabelItem;
import com.autonavi.gbl.map.model.MapRoadTip;
import com.autonavi.gbl.map.model.ScenicInfo;
import com.autonavi.gbl.map.observer.IMapviewObserver;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.util.ArrayList;

import timber.log.Timber;

public class MapViewObserver implements IMapviewObserver {

    public static String TAG = MapViewObserver.class.getSimpleName();
    private MapView mMapView;
    private MapDevice mMapDevice;

    public MapViewObserver()
    {
    }

    public MapViewObserver(MapDevice mapDevice, MapView mapView)
    {
        mMapDevice = mapDevice;
        mMapView = mapView;
    }

    public MapDevice getMapDevice() {
        return mMapDevice;
    }

    public MapView getMapView()
    {
        return mMapView;
    }

    @Override
    public void onMapCenterChanged(long l, double v, double v1) {

    }

    @Override
    public void onMapSizeChanged(long l) {

    }

    @Override
    public void onMapLevelChanged(long l, boolean b) {
        Timber.i("onMapLevelChanged");
    }

    @Override
    public void onMapModeChanged(long l, int i) {

    }

    @Override
    public void onMapPreviewEnter(long l) {

    }

    @Override
    public void onMapPreviewExit(long l) {

    }

    @Override
    public void onClickLabel(long l, ArrayList<MapLabelItem> mapLabelItems) {
    }

    @Override
    public void onClickBlank(long l, float v, float v1) {
        //取消充电站焦点态
        //SDKManager.getInstance().getLayerController().getRouteResultLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).cancelChargeStationFocus();
    }

    @Override
    public void onRenderMap(long l, int i) {

    }

    @Override
    public void onRealCityAnimationFinished(long l) {

    }

    @Override
    public void onMapAnimationFinished(long l, long l1) {

    }

    @Override
    public void onRouteBoardData(long l, ArrayList<MapRoadTip> arrayList) {

    }

    @Override
    public void onMapHeatActive(long l, boolean b) {

    }

    @Override
    public void onScenicActive(long l, ScenicInfo scenicInfo) {

    }

    @Override
    public void onIndoorBuildingActivity(long l, IndoorBuilding indoorBuilding) {

    }

    @Override
    public void onSelectSubWayActive(long l, long[] longs) {

    }

    @Override
    public void onMotionFinished(long l, int i) {

    }

    @Override
    public void onPreDrawFrame(long l) {

    }

    @Override
    public void onRenderEnter(long l) {

    }

    @Override
    public void onMapViewDestory(long l) {

    }

    @Override
    public void onRollAngle(long l, float v) {

    }

    @Override
    public void onPitchAngle(long l, float v) {

    }

    @Override
    public void onCheckIngDataRenderComplete(long l, long l1) {

    }

    @Override
    public void onGuideRoadBoardNameProcessed(long l, GuideRoadNameBoardParam guideRoadNameBoardParam) {

    }

    @Override
    public void onMapVisibleIndoor(long l, ArrayList<IndoorBuilding> arrayList, ArrayList<IndoorBuilding> arrayList1) {

    }
}
