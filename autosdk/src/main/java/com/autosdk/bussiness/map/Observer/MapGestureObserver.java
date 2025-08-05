package com.autosdk.bussiness.map.Observer;

import android.util.Log;

import com.autonavi.gbl.map.MapDevice;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.observer.IMapGestureObserver;

import timber.log.Timber;

public class MapGestureObserver implements IMapGestureObserver {

    private static String TAG = MapGestureObserver.class.getSimpleName();
    private MapView mMapView;
    private MapDevice mMapDevice;

    public MapGestureObserver()
    {

    }

    public MapGestureObserver(MapDevice mapDevice, MapView mapView)
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
    public void onMotionEvent(long l, int i, long l1, long l2) {
        Timber.i("onMotionEvent");
    }

    @Override
    public void onMoveBegin(long l, long l1, long l2) {
        Timber.i("onMoveBegin");
    }

    @Override
    public void onMoveEnd(long l, long l1, long l2) {
        Timber.i("onMoveEnd");
    }

    @Override
    public void onMove(long l, long l1, long l2) {
        Timber.i("onMove");
    }

    @Override
    public void onMoveLocked(long l) {
        Timber.i("onMoveLocked");
    }

    @Override
    public void onScaleRotateBegin(long l, long l1, long l2) {

    }

    @Override
    public void onScaleRotateEnd(long l, long l1, long l2) {

    }

    @Override
    public void onScaleRotate(long l, long l1, long l2) {

    }

    @Override
    public void onPinchLocked(long l) {
        Timber.i("onPinchLocked");
    }

    @Override
    public void onLongPress(long l, long l1, long l2) {
        Timber.i("onLongPress");
    }

    @Override
    public boolean onDoublePress(long l, long l1, long l2) {
        Timber.i("onDoublePress");
        return false;
    }

    @Override
    public boolean onSinglePress(long l, long l1, long l2, boolean b) {
        Timber.i("onSinglePress");
        return false;
    }

    @Override
    public void onSliding(long l, float v, float v1) {
        Timber.i("onSliding");
    }
}