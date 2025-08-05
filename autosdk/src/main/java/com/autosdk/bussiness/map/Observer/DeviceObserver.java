package com.autosdk.bussiness.map.Observer;

import androidx.lifecycle.MutableLiveData;

import com.autonavi.gbl.map.MapDevice;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.model.EGLColorBits;
import com.autonavi.gbl.map.model.MapViewPortParam;
import com.autonavi.gbl.map.observer.IDeviceObserver;

import timber.log.Timber;

public class DeviceObserver implements IDeviceObserver {

    private MapView mMapView;
    private MapDevice mMapDevice;
    public MutableLiveData<Boolean> isSurfaceChanged = new MutableLiveData<>(false);

    public DeviceObserver(){

    }

    public DeviceObserver(MapDevice mapDevice, MapView mapView)
    {
        mMapView = mapView;
        mMapDevice = mapDevice;
    }

    public MapDevice getMapDevice() {
        return mMapDevice;
    }

    public void setmMapView(MapView mapView) {
        this.mMapView = mapView;
    }

    public MapView getMapView()
    {
        return mMapView;
    }

    @Override
    public void onDeviceCreated(int i) {

    }

    @Override
    public void onDeviceDestroyed(int i) {

    }

    @Override
    public void onSurfaceCreated(int i, int i1, int i2, int i3) {
        Timber.i("onSurfaceCreated()");
    }

    @Override
    public void onSurfaceDestroyed(int i, int i1, int i2, int i3) {
        Timber.i("onSurfaceDestroyed()");
    }

    @Override
    public void onSurfaceChanged(int deviceId, final int width, final int height, @EGLColorBits.EGLColorBits1 int colorBits) {
        Timber.i("onSurfaceChanged() called with: deviceId = [" + deviceId + "], width = [" + width + "], height = [" + height + "], colorBits = [" + colorBits + "]");
        if (!Boolean.TRUE.equals(isSurfaceChanged.getValue())) {
            isSurfaceChanged.postValue(true);
        }
        if(null != mMapView)
        {
            MapViewPortParam mapViewPortParam = new MapViewPortParam(0, 0, width, height, width, height);
            mMapView.setMapviewPort(mapViewPortParam);
        }

    }

    @Override
    public void onDeviceRender(int i, int i1) {

    }

    @Override
    public void onEGLDoRender(int i) {

    }
}
