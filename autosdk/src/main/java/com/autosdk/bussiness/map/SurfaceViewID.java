package com.autosdk.bussiness.map;

import androidx.annotation.IntDef;
import com.autonavi.gbl.map.model.MapEngineID;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Hashtable;

/**
 *  屏幕视图ID
 */
public final class SurfaceViewID {

    /** 无效屏幕视图ID */
    public static final int SURFACE_VIEW_ID_INVALID = 0;
    /** 主屏幕视图ID */
    public static final int SURFACE_VIEW_ID_MAIN = 1;
    /** 扩展屏幕1视图ID */
    public static final int SURFACE_VIEW_ID_EX1 = 2;
    /** 扩展屏幕2视图ID */
    public static final int SURFACE_VIEW_ID_EX2 = 3;
    /** 扩展屏幕3视图ID */
    public static final int SURFACE_VIEW_ID_EX3 = 4;

    /**
     * 是否是多屏一致性中的主屏，如果false，则另外转化为实际的多屏一致性的副屏
     */
    public static boolean isKldMaster = true;

    /**
     * 主副屏是否连接成功
     */
    public static boolean isChannelConnect = false;

    public static int kldSurfaceViewID;

    /**
     * 是否为包含副屏需求的多实例sdk
     */
    public static boolean isMultiPassenger = true;

    private static Hashtable<Integer,Integer> mDisplayIdMap = new Hashtable<>();
    private static Hashtable<Integer,Integer> mDisplaySurfaceViewMap = new Hashtable<>();

    public static void setDisplayId(@SurfaceViewID1 int surfaceId, int displayId){
        mDisplayIdMap.put(displayId, surfaceId);
        mDisplaySurfaceViewMap.put(surfaceId, displayId);
    }

    public static int getDisplayId(@SurfaceViewID1 int surfaceId){
        Integer id = mDisplaySurfaceViewMap.get(surfaceId);
        return id==null?0:id;
    }
    public static @SurfaceViewID1 int transformDisplayId2SurfaceId(int displayId){
        if(mDisplayIdMap.containsKey(displayId)){
            return mDisplayIdMap.get(displayId);
        }
        return SurfaceViewID.SURFACE_VIEW_ID_MAIN;
    }

    /**
     *  屏幕视图ID转换为SDK的EngineID
     */
    public static int transform2EngineID(@SurfaceViewID1 int surfaceViewID) {
        if(surfaceViewID == SURFACE_VIEW_ID_MAIN && !isKldMaster) {
            return kldSurfaceViewID*2-1;
        }
        return surfaceViewID*2-1;
    }

    /**
     *  屏幕视图ID转换为SDK的鹰眼EngineID
     */
    public static int transform2EyeEngineID(@SurfaceViewID1 int surfaceViewID) {
        int engineId = transform2EngineID(surfaceViewID);
        return engineId + 1;
    }

    /**
     * SDK的EngineID转换为屏幕视图ID
     */
    public static @SurfaceViewID1 int transform2SurfaceViewID(@MapEngineID.MapEngineID1 int engineID) {
        int surfaceViewId = (engineID+1)/2;
        if(!isKldMaster && surfaceViewId == SURFACE_VIEW_ID_MAIN) {
            return SURFACE_VIEW_ID_MAIN;
        }
        return surfaceViewId;
    }

    /** 屏幕视图ID枚举注解 */
    @IntDef({SURFACE_VIEW_ID_INVALID, SURFACE_VIEW_ID_MAIN, SURFACE_VIEW_ID_EX1, SURFACE_VIEW_ID_EX2, SURFACE_VIEW_ID_EX3})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SurfaceViewID1 {
    }
}
