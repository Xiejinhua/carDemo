package com.desaysv.psmap.model.voice

import androidx.annotation.IntDef


// 标记限定的范围
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    START_RUN,
    IN_MAIN,
    OUT_MAIN,
    IN_FRONT,
    IN_BACK,
    START_PLAN_ROUTE,
    START_PLAN_ROUTE_SUCCESS,
    START_PLAN_ROUTE_FAIL,
    BEGIN_NAVI,
    STOP_NAVI,
    BEGIN_SIM_NAVI,
    PAUSE_SIM_NAVI,
    STOP_SIM_NAVI,
    BEGIN_TTS,
    STOP_TTS,
    ZOOM_OUT,
    ZOOM_IN,
    MODE_2D_CAR,
    MODE_2D_CAR_NORTH,
    MODE_3D_CAR,
    ARRIVE_END,
    GPS_LOCATION,
    MAIN_MAP_ON_CREATE,
    NAVI_APP_IN_FRONT,
    NAVI_APP_BACK_GROUND,
)
annotation class NaviState

const val START_RUN = 0 //导航正在运行

const val IN_MAIN = 1 //地图初始化完成

const val OUT_MAIN = 2 //运行结束，退出程序

const val IN_FRONT = 3 //应用在前台

const val IN_BACK = 4 //应用在后台

const val START_PLAN_ROUTE = 5 //开始算路5

const val START_PLAN_ROUTE_SUCCESS = 6 //算路完成功6

const val START_PLAN_ROUTE_FAIL = 7 // `算路完成，失败7

const val BEGIN_NAVI = 8 //开始导航

const val STOP_NAVI = 9 //结束导航

const val BEGIN_SIM_NAVI = 10 //开始模拟导航10

const val PAUSE_SIM_NAVI = 11 //暂停模拟导航11

const val STOP_SIM_NAVI = 12 //停止模拟导航12

const val BEGIN_TTS = 13 //开始TTS播报13

const val STOP_TTS = 14 //停止TTS播报14

const val ZOOM_IN = 15 //比例尺放大15

const val ZOOM_OUT = 16 //比例尺缩小16

const val MODE_2D_CAR = 17 //2D车首上17

const val MODE_2D_CAR_NORTH = 18 //2D北首上18

const val MODE_3D_CAR = 19 //3D车首上19

const val ARRIVE_END = 39 //到达目的地通知39

const val GPS_LOCATION = 48 //GPS已定位

const val MAIN_MAP_ON_CREATE = 49 //主图activity onCreate状态

const val NAVI_APP_IN_FRONT = 50 //activity获得焦点 导航进入前台

const val NAVI_APP_BACK_GROUND = 51 //activity失去焦点 包含退出，进后台，或者界面被其他应用部分盖住，如语音51
