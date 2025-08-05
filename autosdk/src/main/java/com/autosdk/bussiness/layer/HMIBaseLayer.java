package com.autosdk.bussiness.layer;

import com.autosdk.bussiness.map.SurfaceViewID;

public abstract class HMIBaseLayer {
    @SurfaceViewID.SurfaceViewID1
    protected int mSurfaceViewID;

    HMIBaseLayer(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        mSurfaceViewID = surfaceViewID;
    }

    @SurfaceViewID.SurfaceViewID1
    public int getSurfaceViewID(){
        return mSurfaceViewID;
    }
}
