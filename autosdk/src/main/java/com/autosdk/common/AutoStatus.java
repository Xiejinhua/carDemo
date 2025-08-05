package com.autosdk.common;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 地图状态定义，AIDL透出 protocolID 30200
 *
 * @author ZZP
 */
@IntDef({
        AutoStatus.PLAN_ROUTE_FRAGMENT_START,
        AutoStatus.PLAN_ROUTE_FRAGMENT_EXIT0,
        AutoStatus.ACCOUNT_LOGIN,
        AutoStatus.ACCOUNT_EXIT,
        AutoStatus.APP_START,
        AutoStatus.SDK_INIT_FINISHED,
        AutoStatus.APP_EXIT,
        AutoStatus.APP_FOREGROUND,
        AutoStatus.APP_BACKGROUND,
        AutoStatus.PLAN_ROUTE_START,
        AutoStatus.PLAN_ROUTE_SUCCESS,
        AutoStatus.PLAN_ROUTE_FAIL,
        AutoStatus.NAVI_START,
        AutoStatus.NAVI_STOP,
        AutoStatus.NAVI_ARRIVED,
        AutoStatus.MAP_ZOOM_OUT,
        AutoStatus.MAP_ZOOM_IN,
        AutoStatus.MAP_SCALE_MAX,
        AutoStatus.MAP_SCALE_MIN,
        AutoStatus.TMC_OPEN,
        AutoStatus.TMC_CLOSE,
        AutoStatus.VISUAL_3D_CAR,
        AutoStatus.VISUAL_NORTH,
        AutoStatus.VISUAL_2D_CAR,
        AutoStatus.CRUISE_START,
        AutoStatus.CRUISE_STOP,
        AutoStatus.THEME_DAY,
        AutoStatus.THEME_NIGHT,
        AutoStatus.SIM_NAVI_START,
        AutoStatus.SIM_NAVI_PAUSE,
        AutoStatus.SIM_NAVI_STOP,
        AutoStatus.PERMANENTLY_MUTED,
        AutoStatus.CANCEL_MUTE,
        AutoStatus.MUTED,
        AutoStatus.CROSS_IMAGE_SHOW,
        AutoStatus.CROSS_IMAGE_HIDE,
        AutoStatus.SETTING_FRAGMENT_START,
        AutoStatus.SETTING_FRAGMENT_EXIT,
        AutoStatus.PLAN_ROUTE_FRAGMENT_EXIT,
        AutoStatus.PATH_SWITCH_1,
        AutoStatus.PATH_SWITCH_2,
        AutoStatus.PATH_SWITCH_3,
        AutoStatus.FIRST_FRAME_DASHBOARD,
        AutoStatus.FIRST_FRAME_MAIN,
        AutoStatus.TTS_PLAY_START,
        AutoStatus.TTS_PLAY_STOP,
        AutoStatus.HOME_ADDR_CHANGED,
        AutoStatus.COMPANY_ADDR_CHANGED,
        AutoStatus.NAVI_MANUAL_STOP,
        AutoStatus.FAVORITE_ADDR_CHANGED,
        AutoStatus.NAVI_IMMERSE_TTS_START,
        AutoStatus.NAVI_IMMERSE_TTS_STOP,
        AutoStatus.APP_FULL_EXIT,
        AutoStatus.HOME_FRAGMENT,
        AutoStatus.OTHERS_FRAGMENT,
        AutoStatus.GPS_OK,
        AutoStatus.MAIN_ACTIVITY_CREATE,
        AutoStatus.DRIVING_REPORT_FRAGMENT_START,
        AutoStatus.DRIVING_REPORT_FRAGMENT_EXIT,
        AutoStatus.CROSS_START,
        AutoStatus.CROSS_EXIT,
        AutoStatus.DASHBOARD_THEME_SWITCH,
        AutoStatus.DR_LOCATION_START,
        AutoStatus.CAMERA_INFO_OUTPUT,
        AutoStatus.NAVI_UPDATE_PATH_SUCCESS,
        AutoStatus.NAVI_UPDATE_PATH_FAIL,
        AutoStatus.NAVI_YAW_UPDATE_PATH_START,
        AutoStatus.NAVI_MANUAL_STOP1,
        AutoStatus.NAVI_TUNNEL_START,
        AutoStatus.NAVI_TUNNEL_EXIT,
        AutoStatus.FIRST_FRAME_SURFACE,
        AutoStatus.SETTING_RESET_OK,
        AutoStatus.SETTING_RESET_FAIL,
        AutoStatus.NAVI_PREVIEW_START,
        AutoStatus.NAVI_PREVIEW_EXIT,
        AutoStatus.TEAM_RECORD_SOUND_START,
        AutoStatus.TEAM_RECORD_SOUND_EXIT,
        AutoStatus.WARNING_TONE_START,
        AutoStatus.WARNING_TONE_EXIT,
        AutoStatus.TEAM_CHANGE_DEST,
        AutoStatus.ON_MAIN_ROAD,
        AutoStatus.ON_AUXILIARY_ROAD,
        AutoStatus.ON_BRIDGE,
        AutoStatus.UNDER_BRIDGE,
        AutoStatus.SPEEDING,
        AutoStatus.NOT_SPEEDING,
        AutoStatus.NOT_AGREE_AGREEMENT,
        AutoStatus.AGREE_AGREEMENT,
        AutoStatus.FIRST_FRAME_Widget,
        AutoStatus.REPLAN_PATH,
        AutoStatus.DASHBOARD_MAP_EXIT,
        AutoStatus.SEARCH_RESULT_FRAGMENT_START,
        AutoStatus.SEARCH_RESULT_FRAGMENT_EXIT,
        AutoStatus.SEARCH_RESULT_SINGLE_POI_FRAGMENT_START,
        AutoStatus.SEARCH_RESULT_SINGLE_POI_FRAGMENT_EXIT
}
)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoStatus {
    int PLAN_ROUTE_FRAGMENT_START = 0;// 0:进⼊路线规划结果⻚（路线规划完成⾸次进⼊路线结果⻚⾯）
    int PLAN_ROUTE_FRAGMENT_EXIT0 = 1; //1:进⼊路线结果⻚不可选态（当⽆法通过图⾯信息进⾏路线选择时；4.x版本不使⽤该类型，改为104）
    // 2:进⼊路况查询⻚⾯（调⽤路况查询接⼝进⼊路况查询结果⻚）
    // 3:退出路况查询⻚⾯（路况查询结果⻚关闭）
    int ACCOUNT_LOGIN = 5;// 5:账号登录成功（⽤户登录成功后，透出状态）
    int ACCOUNT_EXIT = 6;// 6:账号登出成功（⽤户主动退出登录时，透出状态）
    int APP_START = 7;// 7:开始运⾏，Application启动即为开始运⾏（不⽀持，需要导航初始化状态的话建议使⽤8）
    int SDK_INIT_FINISHED = 8;// 8:初始化完成
    int APP_EXIT = 9;// 9:运⾏结束，退出程序
    int APP_FOREGROUND = 10;// 10:进⼊前台
    int APP_BACKGROUND = 11;// 11:进⼊后台
    // 12:⼼跳通知
    int PLAN_ROUTE_START = 13;// 13:开始算路
    int PLAN_ROUTE_SUCCESS = 14;// 14:算路完成，成功
    int PLAN_ROUTE_FAIL = 15;// 15:算路完成，失败
    int NAVI_START = 16;// 16:开始导航
    int NAVI_STOP = 17;// 17:结束导航
    int NAVI_ARRIVED = 18;// 18:到达⽬的地通知（此消息在“结束导航”消息前透出）
    int MAP_ZOOM_OUT = 19;// 19:⽐例尺缩⼩
    int MAP_ZOOM_IN = 20;// 20:⽐例尺放⼤
    int MAP_SCALE_MAX = 21;// 21:⽐例尺达到最⼤⽐例尺
    int MAP_SCALE_MIN = 22;// 22:⽐例尺达到最⼩⽐例尺
    int TMC_OPEN = 23;// 23:路况开启
    int TMC_CLOSE = 24;// 24:路况关闭
    int VISUAL_3D_CAR = 25;// 25:3D视⻆
    int VISUAL_NORTH = 26;// 26:2D北⾸上
    int VISUAL_2D_CAR = 27;// 27:2D⻋⾸上
    int CRUISE_START = 28;// 28:进⼊巡航通知
    int CRUISE_STOP = 29;// 29:退出巡航通知
    int THEME_DAY = 30;// 30:昼夜模式（⽩天）
    int THEME_NIGHT = 32;// 32:昼夜模式（⿊夜）
    int SIM_NAVI_START = 33;// 33:开始模拟导航
    int SIM_NAVI_PAUSE = 34;// 34:暂停模拟导航
    int SIM_NAVI_STOP = 35;// 35:结束模拟导航
    // 36:⽬的地停⻋场弹框进⼊不可选态
    // 37:导航中避开拥堵弹框进⼊不可选态
    // 38:油量提醒弹框进⼊不可选态
    // 39:继续导航提醒进⼊不可选态
    int PERMANENTLY_MUTED = 40;// 40:永久静⾳(417及以上版本⽀持)
    int CANCEL_MUTE = 41;// 41:⽆静⾳（请求）、取消静⾳（透出）(417及以上版本⽀持)
    int MUTED = 42;// 42:临时静⾳(417及以上版本⽀持)
    int CROSS_IMAGE_SHOW = 43;// 43:路⼝⼤图出现(430及以上版本⽀持)
    int CROSS_IMAGE_HIDE = 44;// 44:路⼝⼤图关闭(430及以上版本⽀持)
    int SETTING_FRAGMENT_START = 101;// 101:进⼊导航设置界⾯(460及以上版本⽀持)
    int SETTING_FRAGMENT_EXIT = 102;// 102:退出导航设置界⾯(460及以上版本⽀持)
    int PLAN_ROUTE_FRAGMENT_EXIT = 104; // 104:退出路线规划界⾯(460及以上版本⽀持)
    int PATH_SWITCH_1 = 107;// 107:路线选择路线1(460及以上版本⽀持)
    int PATH_SWITCH_2 = 108;// 108:路线选择路线2(460及以上版本⽀持)
    int PATH_SWITCH_3 = 109;// 109:路线选择路线3(460及以上版本⽀持)
    int FIRST_FRAME_DASHBOARD = 116;// 116:仪表地图第⼀帧(460及以上版本⽀持)
    int FIRST_FRAME_MAIN = 200;// 200:主图绘制第⼀帧（暂不⽀持）
    int TTS_PLAY_START = 1013;// 1013:开始TTS播报（460及以上版本⽀持）
    int TTS_PLAY_STOP = 1014;// 1014:停⽌TTS播报（460及以上版本⽀持）
    int HOME_ADDR_CHANGED = 1026;// 1026:收藏夹家变化状态
    int COMPANY_ADDR_CHANGED = 1027;// 1027:收藏夹公司变化状态
    // 1028:点击查周边的状态
    // 1029:点击搜地点的状态
    // 1030:sendtocar消息消失
    // 1031:停⻋场推送消息消失
    // 1032:续航通知消息消失
    // 1033:算路失败消息消失
    int NAVI_MANUAL_STOP = 1034;// 1034:导航中⼿动退出导航时通知
    // 1035:导航中⼿动退出导航弹出框消失通知
    int FAVORITE_ADDR_CHANGED = 1036;// 1036:普通收藏夹变化通知
    int NAVI_IMMERSE_TTS_START = 1043;// 1043:进⼊导航沉浸播报状态
    int NAVI_IMMERSE_TTS_STOP = 1044;// 1044:退出导航沉浸播报状态
    int APP_FULL_EXIT = 1045;// 1045:完全运⾏结束，退出程序
    int HOME_FRAGMENT = 1046;// 1046:主界⾯（包括主图巡航界⾯和警告界⾯）
    int OTHERS_FRAGMENT = 1047;// 1047:⼦界⾯（除了包括主图巡航界⾯和警告界⾯的其他界⾯）
    int GPS_OK = 1048;// 1048:GPS已定位
    int MAIN_ACTIVITY_CREATE = 1049;// 1049:主图activity oncreate状态
    // 1050:activity获得焦点
    // 1051:activity失去焦点
    int DRIVING_REPORT_FRAGMENT_START = 1052;// 1052:到达⽬的地卡⽚显示
    int DRIVING_REPORT_FRAGMENT_EXIT = 1053;// 1053:到达⽬的地卡⽚消失
    // 1060:进⼊敬告界⾯
    int CROSS_START = 1112;// 1112:进⼊路⼝
    int CROSS_EXIT = 1113;// 1113:退出路⼝
    // 1114:巡航（⻓安多屏使⽤）
    // 1115:导航（⻓安多屏使⽤）
    int DASHBOARD_THEME_SWITCH = 1117;// 1117:仪表主题切换
    int DR_LOCATION_START = 1118;// 1118:开始发送DR信息通
    int CAMERA_INFO_OUTPUT = 1201;// 1201:对外发送电⼦眼信息
    // 1301:触摸事件：路线规划结果
    // 1302:触摸事件：搜索结果列表
    // 1303:触摸事件：导航中⽬的地停⻋场弹框界⾯
    // 1304:触摸事件：事件上报⻚⾯
    int NAVI_UPDATE_PATH_SUCCESS = 1305;// 1305:导航中路线更新成功
    int NAVI_UPDATE_PATH_FAIL = 1306;// 1306:导航中路线更新失败
    int NAVI_YAW_UPDATE_PATH_START = 1307;// 1307:导航中偏航重算开始
    int NAVI_MANUAL_STOP1 = 1308;// 1308:导航中途⼿动退出导航
    int NAVI_TUNNEL_START = 1309;// 1309:进⼊隧道
    int NAVI_TUNNEL_EXIT = 1310;// 1310:退出隧道
    int FIRST_FRAME_SURFACE = 1311;// 1311:GFrame第⼀帧
    // 1312:VR场景使⽤：导航发起初始化主动对话|免唤醒功能
    // 1313:VR场景使⽤：导航通知停⽌主动对话|免唤醒功能
    // 1314:疲劳驾驶场景使⽤：通知系统，导航在疲劳驾驶播报
    // 1315:Auto迁移全链路⽆法定位⽇志：开始
    // 1316:Auto迁移全链路⽆法定位⽇志：结束
    // 1317:恢复上下⽂成功
    // 1318:恢复上下⽂失败
    // 1319:置前台后地图第⼀帧完成
    // 1501:进⼊AR导航（460及以上版本⽀持）
    // 1502:退出AR导航（460及以上版本⽀持）
    // 1503:进⼊AR导航仪表显示（460及以上版本⽀持）
    // 1504:退出AR导航仪表显示（460及以上版本⽀持）
    // 1505:进⼊AR巡航（460及以上版本⽀持）
    // 1506:退出AR巡航（460及以上版本⽀持）
    // 1507:mqtt初始化完成（460及以上版本⽀持）
    int SETTING_RESET_OK = 3001;// 3001:恢复默认设置成功（460及以上版本⽀持）
    int SETTING_RESET_FAIL = 3002;// 3002:恢复默认设置失败（460及以上版本⽀持）
    int NAVI_PREVIEW_START = 3003;// 3003:进⼊导航全览（460及以上版本⽀持）
    int NAVI_PREVIEW_EXIT = 3004;// 3004:退出导航全览（460及以上版本⽀持）
    // 3005:开始退出程序（即开始执⾏退出进程时就对外通知）460及以上⽀持
    // 3006:【ar摄像头】未获取到第⼀帧图⽚（460及以上⽀持）
    int TEAM_RECORD_SOUND_START = 3007;// 3007:开始组队录音（500及以上版本⽀持）
    int TEAM_RECORD_SOUND_EXIT = 3008;// 3008:结束组队录音（500及以上版本⽀持）
    // 3009: ⽤户点击了中卡的家（仅针对AD项⽬）
    // 3010:⽤户点击了中卡的公司（仅针对AD项⽬）
    int WARNING_TONE_START = 3011;// 3011: 开始“警告⾳”播放
    int WARNING_TONE_EXIT = 3012;// 3012: 停⽌“警告⾳”播放
    int TEAM_CHANGE_DEST = 3013;// 3013:组队队⻓修改⽬的地（500及以上版本⽀持）
    int ON_MAIN_ROAD = 3014;// 3014:在主路（460以上版本⽀持，表示⽀持切换到主路）
    int ON_AUXILIARY_ROAD = 3015;// 3015:在辅路（460以上版本⽀持，表示⽀持切换到辅路）
    int ON_BRIDGE = 3016;// 3016:在桥上（460以上版本⽀持，表示⽀持切换到桥上）
    int UNDER_BRIDGE = 3017;// 3017:在桥下（460以上版本⽀持，表示⽀持切换到桥下）
    int SPEEDING = 3018;// 3018:超速中（480以上版本⽀持）
    int NOT_SPEEDING = 3019;// 3019:未超速（480以上版本⽀持）
    // 3020:定位权限为关（仅针对AD项⽬）
    // 3021:定位权限为开（仅针对AD项⽬）
    // 3022:定位服务开关为关（仅针对AD项⽬）
    // 3023:定位服务开关为开（仅针对AD项⽬）
    int NOT_AGREE_AGREEMENT = 3024;// 3024:⽤户未同意开机敬告（仅针对AD项⽬）
    int AGREE_AGREEMENT = 3025;// 3025:⽤户已同意开机敬告（仅针对AD项⽬）
    int FIRST_FRAME_Widget = 3026;// 3026:导航Widget绘制第⼀帧（500及以上版本⽀持）
    int REPLAN_PATH = 3027;// 3027:重新算路通知（500及以上版本⽀持）
    int DASHBOARD_MAP_EXIT = 3028;// 3028:仪表投屏结束通知（500及以上版本⽀持）
    int SEARCH_RESULT_FRAGMENT_START = 3029; //3029:进⼊搜索结果界⾯
    int SEARCH_RESULT_FRAGMENT_EXIT = 3030; //3030:退出搜索结果界⾯
    int SEARCH_RESULT_SINGLE_POI_FRAGMENT_START = 3021; //3021:进⼊搜索结果子界⾯
    int SEARCH_RESULT_SINGLE_POI_FRAGMENT_EXIT = 3032;//3021:退出搜索结果子界⾯
}