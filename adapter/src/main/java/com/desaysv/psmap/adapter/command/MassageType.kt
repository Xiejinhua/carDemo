package com.desaysv.psmap.adapter.command

enum class MassageType {
    //请求指令
    NAVI_STATUS,//请求导航状态
    NAVI_APP_STATUS,//请求导航APP前后台状态
    NAVI_TO_HOME,//导航回家
    NAVI_TO_WORK,//导航去公司
    OPEN_SEARCH_PAGE,//打开搜索页面
    OPEN_NAVI_PAGE,//打开导航页面
    OPEN_GAS_STATION_PAGE,//打开加油站搜索页面
    OPEN_CHARGE_STATION_PAGE,//打开充电站搜索页面
    OPEN_FAVORITE_PAGE,//打开搜索页面
    OPEN_GROUP_PAGE,//打开组队页面
    ACTION_REQUEST_MAP_POI_NAME,//记忆泊车获取POI名称
    DAY_AND_NIGHT_MODE_STATUS,//日夜模式状态获取
    SET_THE_MAP_STATUS,//设置地图状态 data: "0"-回车位，"1"-放大地图， "2"-缩小地图

    //回调指令
    ON_NAVI_STATUS,//导航状态回调
    ON_NAVI_APP_STATUS,//导航APP前后台状态
    ON_NAVI_PATH_INFO,//导航路线信息
    ON_NAVIGATING_INFO,//导航中信息回调
    ON_NAVI_TMC_LIGHT_BAR_INFO,//导航中光柱图信息回调
    ON_NAVI_PANEL_INFO,//导航中TBT面板信息回调
    ON_NAVI_CROSS_MAP,//导航中路口大图回调
    ON_NAVI_ADAS_MAP,//导航ADAS消息发送
    ON_DAY_AND_NIGHT_MODE_STATUS,//日夜模式状态回调
    ON_NAVI_LOW_GAS,//低油量提醒
}