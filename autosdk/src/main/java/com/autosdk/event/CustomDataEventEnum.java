package com.autosdk.event;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author AutoSDK
 * 多屏自定义消息类型
 */
@IntDef({CustomDataEventEnum.EVENT_DEFAULT_MODE,CustomDataEventEnum.EVENT_DAY_NIGHT_MODE, CustomDataEventEnum.EVENT_POWER_TYPE,
        CustomDataEventEnum.EVENT_ADD_HISTORY_ROUTE, CustomDataEventEnum.EVENT_ADD_FAVORITE,
        CustomDataEventEnum.EVENT_DEL_FAVORITE,
        CustomDataEventEnum.EVENT_DEL_HISTORY_ROUTE,CustomDataEventEnum.EVENT_CLEAN_HISTORY_ROUTE,
        CustomDataEventEnum.EVENT_DEL_SEARCH_HISTORY,CustomDataEventEnum.EVENT_ADD_SEARCH_HISTORY,
        CustomDataEventEnum.EVENT_CLEAN_SEARCH_HISTORY,CustomDataEventEnum.EVENT_GET_HISTORY_ROUTE,
        CustomDataEventEnum.EVENT_GET_SEARCH_HISTORY,CustomDataEventEnum.EVENT_SYNC_PREF,
        CustomDataEventEnum.EVENT_SYNC_CAR_SETTING,CustomDataEventEnum.EVENT_SYNC_MID_POIS,
        CustomDataEventEnum.EVENT_GET_FAVORITE,CustomDataEventEnum.EVENT_RECEIVE_LOCATION_STATUS,
        CustomDataEventEnum.EVENT_REQUEST_LOCATION_STATUS,CustomDataEventEnum.EVENT_DYNAMIC_LEVEL_GUIDE,
        CustomDataEventEnum.EVENT_RECEIVE_ELECTRIC_INFO,CustomDataEventEnum.EVENT_REQUEST_ELECTRIC_INFO,
        CustomDataEventEnum.EVENT_SYNC_TO_POI_ADDRESS,CustomDataEventEnum.EVENT_REQUEST_CONSIS_ROUTE,
        CustomDataEventEnum.EVENT_LOGIN_STATUS,
        CustomDataEventEnum.EVENT_SIDE_SEND_ROUTE_TO_MAIN, CustomDataEventEnum.EVENT_DELETE_ROUTE,
        CustomDataEventEnum.EVENT_EXIT_NAVI,CustomDataEventEnum.EVENT_EXIT_APP,
        CustomDataEventEnum.EVENT_MULTIDISPLAY_CONNECT,CustomDataEventEnum.EVENT_MULTIDISPLAY_DISCONNECT,
        CustomDataEventEnum.EVENT_CHANGE_ROUTE_INDEX,CustomDataEventEnum.EVENT_REQUEST_LOCATION_POSIITON,
        CustomDataEventEnum.EVENT_RECEIVE_LOCATION_POSIITON, CustomDataEventEnum.EVENT_REQUEST_REST_INFO, CustomDataEventEnum.EVENT_REQUEST_STOP_CRUISE
})
@Retention(RetentionPolicy.SOURCE)
public @interface CustomDataEventEnum {
    int EVENT_DEFAULT_MODE = 1000;
    int EVENT_DAY_NIGHT_MODE = 1001;
    int EVENT_POWER_TYPE = 1002;
    int EVENT_ADD_HISTORY_ROUTE = 1003;
    int EVENT_DEL_HISTORY_ROUTE = 1004;
    int EVENT_CLEAN_HISTORY_ROUTE = 1005;
    int EVENT_ADD_SEARCH_HISTORY = 106;
    int EVENT_DEL_SEARCH_HISTORY = 1007;
    int EVENT_CLEAN_SEARCH_HISTORY = 1008;
    int EVENT_ADD_FAVORITE = 1009;
    int EVENT_DEL_FAVORITE = 1010;
    int EVENT_GET_HISTORY_ROUTE = 1012;
    int EVENT_GET_SEARCH_HISTORY = 1013;
    int EVENT_SYNC_PREF = 1014;
    int EVENT_SYNC_CAR_SETTING = 1015;
    int EVENT_SYNC_MID_POIS = 1016;
    int EVENT_GET_FAVORITE = 1017;
    int EVENT_RECEIVE_LOCATION_STATUS = 1018;
    int EVENT_REQUEST_LOCATION_STATUS = 1019;
    int EVENT_DYNAMIC_LEVEL_GUIDE = 1020;
    int EVENT_RECEIVE_ELECTRIC_INFO = 1021;
    int EVENT_REQUEST_ELECTRIC_INFO = 1022;
    int EVENT_SYNC_TO_POI_ADDRESS = 1023;
    /**
     * 主动请求路线数据，用于副屏进入导航
     */
    int EVENT_REQUEST_CONSIS_ROUTE = 1024;
    int EVENT_REQUEST_PREVIOUS_ROUTE = 1025;//主屏恢复路线
    int EVENT_REQUEST_PREVIOUS_MULTI_ROUTE = 1026;//副屏还原路线
    int EVENT_REQUEST_PREVIOUS_NEW_ROUTE = 1027;//主屏设置路线
    int EVENT_REQUEST_PREVIOUS_NEW_MULTI_ROUTE = 1028;//副屏设置路线

    /**
     * 副屏发送路线给主屏
     */
    int EVENT_SIDE_SEND_ROUTE_TO_MAIN = 1029;

    /**
     * 清除sdcard缓存的路线（根据参数决定清除主屏路线、非主屏路线或者全部路线）
     */
    int EVENT_DELETE_ROUTE = 1030;

    /**
     * 主屏退出app
     */
    int EVENT_EXIT_APP = 1031;

    /**
     * 多屏连接
     */
    int EVENT_MULTIDISPLAY_CONNECT = 1032;

    /**
     * 多屏断开连接
     */
    int EVENT_MULTIDISPLAY_DISCONNECT = 1033;

    /**
     * 退出实时导航
     */
    int EVENT_EXIT_NAVI = 1034;

    /**
     * 路线规划页面中切换路线
     */
    int EVENT_CHANGE_ROUTE_INDEX = 1035;

    /**
     * 请求主屏发送最新定位位置
     */
    int EVENT_REQUEST_LOCATION_POSIITON = 1036;

    /**
     * 收到主屏发送的最新定位位置
     */
    int EVENT_RECEIVE_LOCATION_POSIITON = 1037;

    /**
     * 副屏点击高速服务区展开详情由主屏请求
     */
    int EVENT_REQUEST_REST_INFO = 1038;

    /**
     * 停止巡航
     */
    int EVENT_REQUEST_STOP_CRUISE = 1039;

    int EVENT_LOGIN_STATUS = 1040;   //登录状态
}
