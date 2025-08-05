package com.autosdk.adapter;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by AutoSdk on 2021/5/13.
 **/
public class AdapterConstants {
    public static final String ACTION = "AUTONAVI_STANDARD_BROADCAST_RECV";
    public static final String ACTION_SEND = "AUTONAVI_STANDARD_BROADCAST_SEND";
    public static final String KEY_TYPE = "KEY_TYPE";
    public static final String SOURCE_APP = "SOURCE_APP";
    public static final String KEYWORDS = "KEYWORDS";
    public static final String EXTRA_DLAT = "EXTRA_DLAT";
    public static final String EXTRA_DLON = "EXTRA_DLON";
    public static final String EXTRA_DNAME = "EXTRA_DNAME";
    public static final String EXTRA_STATE = "EXTRA_STATE";
    public static final String EXTRA_CROSS_MAP = "EXTRA_CROSS_MAP";
    public static final String EXTRA_DAY_NIGHT_MODE = "EXTRA_DAY_NIGHT_MODE";
    public static final String EXTRA_LOGIN_ACCOUNT = "EXTRA_LOGIN_ACCOUNT";
    public static final int EXTRA_DAY_NIGHT_KEY_VALUE = 10048;
    public static final int EXTRA_AUTO_LOGIN_KEY_VALUE = 10049;
    public static final int EXTRA_SEND_KEY_VALUE = 10019;

    public static final int ROUTE_ELECTRIC_REQUEST = 0x4000;


    public static final String SUGGESTION = "suggestion";   //预搜索
    public static final String KEYWORD_SEARCH = "keywordsearch"; //关键字搜索
    public static final String AROUND_SEARCH = "aroundsearch"; //周边搜索
    public static final String ALONG_SEARCH = "alongsearch";    //沿途搜索
    public static final String ROUTE = "route";          //路线规划
    public static final String RESULT_TO_NAVI = "result_to_navi"; //路线规划到导航页面
    public static final String DRAW_PATH = "draw_path";  //路线绘制
    public static final String INIT = "init";  //启动到渲染第一帧时间
    public static final String RENDER_COMPLETE = "render_complete";  //启动到完全渲染时间


    /**
     * 统计类型
     */
    @StringDef({SUGGESTION, KEYWORD_SEARCH, AROUND_SEARCH, ALONG_SEARCH, ROUTE,RESULT_TO_NAVI,DRAW_PATH,INIT,RENDER_COMPLETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CountType {
    }

    /**
     * 车辆信息
     */
    public static final int EXTRA_AUTO_CAR_INFO_KEY_VALUE = 13021;//导航查询车辆资料  系统通知车辆资料
    public static final int EXTRA_AUTO_CAR_ENERGY_INFO_KEY_VALUE = 13022;//导航查询当前能量信息 系统通知车辆能量信息
    public static final int EXTRA_AUTO_CAR_CHARGE_STATUS_KEY_VALUE = 13023;//车辆充电的状态通知
    public static final int EXTRA_AUTO_CAR_DRIVE_MODEL_KEY_VALUE = 13024;//驾驶模式变更通知
    public static final int EXTRA_AUTO_CAR_ADDITION_KEY_VALUE = 13025;// 附加能耗通知
    public static final int EXTRA_AUTO_CAR_OUTSIDE_TEMPERATURE_KEY_VALUE = 13026;// 室外温度变更通知
    public static final int EXTRA_AUTO_CAR_REALTIME_POWER_KEY_VALUE = 13027;// 充电功率变更通知

    @IntDef({EXTRA_AUTO_CAR_INFO_KEY_VALUE, EXTRA_AUTO_CAR_ENERGY_INFO_KEY_VALUE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestCarInfoType {
    }


    @IntDef({EXTRA_AUTO_CAR_INFO_KEY_VALUE, EXTRA_AUTO_CAR_ENERGY_INFO_KEY_VALUE,
            EXTRA_AUTO_CAR_CHARGE_STATUS_KEY_VALUE, EXTRA_AUTO_CAR_DRIVE_MODEL_KEY_VALUE,
            EXTRA_AUTO_CAR_ADDITION_KEY_VALUE, EXTRA_AUTO_CAR_OUTSIDE_TEMPERATURE_KEY_VALUE,
            EXTRA_AUTO_CAR_REALTIME_POWER_KEY_VALUE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CarInfoType {
    }

    /**
     * dispatch协议类型
     */
    public static final int PROTOCOL_DISPATCH_MAIN_MAP_STATUS = 1;//透出主图事件：0启动，1关闭
    public static final int PROTOCOL_DISPATCH_MAP_MOVE_END = 2;//透出移图结束事件
    public static final int PROTOCOL_DISPATCH_MAP_CLICK_LABEL = 3;//透出底图poi点击事件-主图
    public static final int PROTOCOL_DISPATCH_SEARCH_CLICK_LABEL = 4;//透出底图poi点击事件-搜索结果页
    public static final int PROTOCOL_DISPATCH_LAYER_CLICK_ITEM = 5;//透出图层元素点击事件
    public static final int PROTOCOL_DISPATCH_TTS_PLAYER = 6;//透出TTS语音播报内容

    @IntDef({PROTOCOL_DISPATCH_MAIN_MAP_STATUS,
            PROTOCOL_DISPATCH_MAP_MOVE_END,
            PROTOCOL_DISPATCH_MAP_CLICK_LABEL,
            PROTOCOL_DISPATCH_SEARCH_CLICK_LABEL,
            PROTOCOL_DISPATCH_LAYER_CLICK_ITEM,
            PROTOCOL_DISPATCH_TTS_PLAYER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ProtocolDispatchType {
    }
}
