package com.autosdk.bussiness.multidisplay;

import com.autosdk.bussiness.map.SurfaceViewID;

/**
 * KLD方案虚拟一致性相关配置参数
 */
public class KldDisplayParam {

    /**
     * 表示是否主屏
     */
    public boolean isMaster = true;

    /**
     * 是否使用内建能力，内部使用commonsocket方式创建
     */
    public boolean useInnerChannel;

    /**
     * IP
     */
    public String host;

    /**
     * 端口号
     */
    public int port;

    /**
     * 需要设置的屏幕ID，默认主屏
     */
    public @SurfaceViewID.SurfaceViewID1 int surfaceViewID = SurfaceViewID.SURFACE_VIEW_ID_MAIN;

    /**
     * 副屏是否发路线给主屏，发送过路线没改变就不再次发送
     */
    public static boolean isSendRouteToMain =true;

}
