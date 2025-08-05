package com.autosdk.bussiness.map.Observer;

import com.autonavi.gbl.map.MapDevice;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.adapter.MapHelper;
import com.autonavi.gbl.map.observer.ITextTextureObserver;

public class TextTextureObserver implements ITextTextureObserver {

    private MapView mMapView;
    private MapDevice mMapDevice;
    public TextTextureObserver(MapDevice mapDevice, MapView mapView)
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
    public byte[] getCharBitmap(long engineId, int oneChar, int fontSize) {
        byte[] txtPixelBuffer = MapHelper.getTextTextureHelper().getCharBitmap(oneChar, fontSize);

        if (null != mMapDevice) {
            mMapDevice.resetTickCount(6);
        }

        return txtPixelBuffer;
    }
    @Override
    public byte[] getCharsWidths(long engineId, short[] charBuffer, int fontSize) {
        return MapHelper.getTextTextureHelper().getCharsWidths(charBuffer,fontSize);
    }
}

