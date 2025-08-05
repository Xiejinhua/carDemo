package com.autosdk.bussiness.multidisplay;

import java.io.Serializable;

/**
 * 多屏间同步的定位位置信息
 * @author autoSDK
 */
public class MultidisplayPositionInfo implements Serializable {

    public String provider;

    public double longitude;

    public double latitude;

    public double altitude;

    public float speed;

    public float bearing;

    public float accuracy;

    public long time;

}
