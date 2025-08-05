package com.autosdk.bussiness.location.listener;

import android.location.Location;

/**
 * 车标位置变化回调
 */
public interface LocationListener {
    void onLocationChange(Location location);
}