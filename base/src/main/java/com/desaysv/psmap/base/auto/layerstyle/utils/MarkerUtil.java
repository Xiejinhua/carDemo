package com.desaysv.psmap.base.auto.layerstyle.utils;

import com.autosdk.bussiness.map.SurfaceViewID;

public class MarkerUtil {

    /**
     * 获取线Overlay的宽度(目前线扎点分为两类：1. 任何分辨率下固定物理尺寸，比如飞线；2. 多分辨率下进行适配，比如引导线)
     *
     * @param width         ued标注的lineOverlay宽度
     * @param ratio         有些线Overlay的纹理带有透明区域，ued标注的是非透明区域，因此传给引擎的宽度要除以非透明区域的占比
     * @param needAdjust    是否需要适配
     * @param surfaceViewID 线Overlay所在的SurfaceViewID
     */
    public static int getAdapterLineOverlayWidth(int width, float ratio, boolean needAdjust, @SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        float result = width / ratio;
        /*
        todo 动态计算 暂时不用
        if (needAdjust){
            result = result * DisplayInfoManager.getInstance().getDisplayAdapter().getMarkerScaleRatio(surfaceViewID);
        }else {
            result = result * (DisplayInfoManager.getInstance().getDeviceScreenInfo().deviceDensityDpi / 160.0f);
        }*/
        result = result * (160 / 160.0f);
        return (int) Math.ceil(result);
    }
}
