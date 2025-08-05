package com.autosdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.autonavi.gbl.map.adapter.MapSurfaceView;
import com.autonavi.gbl.map.adapter.NetworkState;
import com.autosdk.bussiness.map.SurfaceViewID;

public class SDKMapSurfaceView extends MapSurfaceView {

    private Context mContext;
    private NetworkState mNetworkState = null;
    private @SurfaceViewID.SurfaceViewID1 int mSurfaceViewID;

    public SDKMapSurfaceView(Context context) {
        super(context);
        mContext = context;
    }

    public SDKMapSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    public void setSurfaceViewID(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        mSurfaceViewID = surfaceViewID;
    }

    /*private void initNetworkState() {
        if (this.mNetworkState == null) {
            this.mNetworkState = new NetworkState();
        }
        if (this.mNetworkState != null) {
            this.mNetworkState.setNetworkListener(this);
            this.mNetworkState.registerNetChangeReceiver(this.mContext.getApplicationContext(), true);
            boolean isConnected = NetworkState.isNetworkConnected(this.mContext.getApplicationContext());
            MapController.getInstance().setNetworkType(isConnected ? NetworkStatus.NetworkStatusWiFi : NetworkStatus.NetworkStatusNotReachable);
        }
    }*/
}
