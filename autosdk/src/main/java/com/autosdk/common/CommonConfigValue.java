package com.autosdk.common;

/**
 * Created by AutoSdk on 2020/12/2.
 **/
public class CommonConfigValue {
    public final static int KEY_ROAT_OPEN = 1;
    public final static int KEY_ROAT_CLOSE = 0;

    public final static int VISUALMODE_2D_CAR = 0;
    public final static int VISUALMODE_3D_CAR = 1;
    public final static int VISUALMODE_2D_NORTH = 2;

    //高速（V≥60km/h）
    public final static int SHEEPD_HIGH = 1;
    //中速（20＜V＜60km/h）
    public final static int SHEEPD_MEDIUM = 2;
    //低速（V≦20km/h）
    public final static int SHEEPD_LOW = 3;

    //比例尺
    public static final float Zoom_Level_2D = 15.0f;
    public static final float Zoom_Level_3D = 17.0f;

    public final static int KEY_MAP_FONT_NORMAL = 1;
    public final static int KEY_MAP_FONT_BIG = 2;
}
