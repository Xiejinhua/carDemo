package com.autosdk.bussiness.navi.constant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * 路线页面场景子poi类型
 */
public class CarSceneType {

    /**
     * 101：门
     * 102：室内地图-建筑物设施出入口（门）
     * 103：进站
     * 104：出站
     * 105：出发
     * 106：到达
     * 107：地铁出入口
     * 201：室内地图-建筑物室内点
     * 301：景区内子景点
     * 302：景区内设施
     * 303：航站楼/候机楼
     * 304：火车站/机场/汽车站的候机室/候车室
     * 305：停车场
     * 306：售票处
     * 307：医院科室(如住院部、门诊楼、急诊楼)
     * 308：楼/栋/区/期
     */
    public static final int REL_TYPE_IN_103 = 103;
    public static final int REL_TYPE_OUT_104 = 104;
    public static final int REL_TYPE_DEPARTURE_105 = 105;
    public static final int REL_TYPE_ARRIVAL_106 = 106;
    public static final int REL_TYPE_TERMINAL_303 = 303;
    public static final int REL_TYPE_PARK_305 = 305;

    /**
     * 旧值
     * 34：航站楼/候机楼
     */
    public static final int REL_TYPE_DEPARTURE_34 = 34;

    /**
     * 旧值
     * 35：火车站/机场/汽车站的候机室/候车室
     */
    public static final int REL_TYPE_DEPARTURE_35 = 35;

    /**
     * 旧值
     * 41：停车场
     */
    public static final int REL_TYPE_DEPARTURE_41 = 41;

    /**
     * 旧值
     * 43：出发
     */
    public static final int REL_TYPE_DEPARTURE_43 = 43;
    /**
     * 旧值
     * 44：到达
     */
    public static final int REL_TYPE_ARRIVAL_44 = 44;
    /**
     * 旧值
     * 45：进站
     */
    public static final int REL_TYPE_IN_45 = 45;
    /**
     * 旧值
     * 46：出站
     */
    public static final int REL_TYPE_OUT_46 = 46;

    public CarSceneType() {
    }

    @IntDef(value = {REL_TYPE_DEPARTURE_43, REL_TYPE_ARRIVAL_44, REL_TYPE_IN_45, REL_TYPE_OUT_46, REL_TYPE_IN_103, REL_TYPE_OUT_104, REL_TYPE_DEPARTURE_105, REL_TYPE_ARRIVAL_106, REL_TYPE_TERMINAL_303, REL_TYPE_PARK_305})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CarSceneType1 {
    }
}
