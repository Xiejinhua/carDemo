package com.autosdk.common.location;

/**
 * 卫星类型
 * @author autoSDK
 */
public class SatelliteType {
    /**
     * GPS单模
     */
    public static int GPS = 0;

    /**
     * 北斗单模
     */
    public static int BEIDOU = 1;

    /**
     * 格洛纳斯单模
     */
    public static int GLONASS = 2;

    /**
     * 伽利略单模
     */
    public static int GALILEO = 3;

    /**
     * 其他
     */
    public static int OTHER = 4;

//    /**
//     * GPS+北斗双模
//     */
//    public static int GPSANDBEIDOU = 5;

    /**
     * 需要被过滤掉的值
     */
    public static int INVALID = -1000;

}
