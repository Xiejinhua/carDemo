package com.desaysv.psmap.model.layerstyle;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.gbl.layer.observer.IBizDynamicAdapter;

public class DynamicLayerParamImpl implements IBizDynamicAdapter {
    private int surfaceViewId;

    public DynamicLayerParamImpl(int surfaceViewId) {
        this.surfaceViewId = surfaceViewId;
    }

    @Override
    public float getPointMarkerScaleFactor() {
        return 1.0f;
    }

    @Override
    public boolean isNightMode() {
        return NightModeGlobal.isNightMode();
    }
}
