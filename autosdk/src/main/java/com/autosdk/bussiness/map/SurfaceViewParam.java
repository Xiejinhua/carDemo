package com.autosdk.bussiness.map;

import com.autonavi.gbl.map.adapter.MapSurfaceView;

/**
 * 屏幕视图view参数
 */
public class SurfaceViewParam {

    /** 屏幕左上角坐标x */
    public long x;
    /** 屏幕左上角坐标y */
    public long y;

    /** 视图宽 */
    public long width;
    /** 视图高 */
    public long height;

    /** 屏幕宽 */
    public long screenWidth;
    /** 屏幕高 */
    public long screenHeight;

    /** MapSurfaceView对象引用 */
    public MapSurfaceView glMapSurface = null;
}
