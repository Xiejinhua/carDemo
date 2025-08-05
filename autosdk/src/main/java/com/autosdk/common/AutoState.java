package com.autosdk.common;

/**
 * auto运行状态对应的值
 * @author autoSDK
 */
public interface AutoState {
    /**
     * 开始运行，Application启动即为开始运行 - 0
     */
    int START = 0;

    /**
     * 初始化完成，每次创建地图完成通知 - 1
     */
    int START_FINISH = 1;

    /**
     * 运行结束，退出程序  - 2
     */
    int FINISH = 2;

    /**
     * 进入前台，OnStart函数中调用 - 3
     */
    int FOREGROUND = 3;

    /**
     * 进入后台，OnStop函数中调用 - 4
     */
    int BACKGROUND = 4;

    /**
     * 开始算路 - 5
     */
    int CALCULATE_ROUTE_START = 5;

    /**
     * 算路完成，成功 - 6
     */
    int CALCUATE_ROUTE_FINISH_SUCC = 6;

    /**
     * 算路完成，失败 - 7
     */
    int CALCUATE_ROUTE_FINISH_FAIL = 7;

    /**
     * 开始导航 - 8
     */
    int GUIDE_START = 8;

    /**
     * 结束导航 - 9
     */
    int GUIDE_STOP = 9;

    /**
     * 开始模拟导航 - 10
     */
    int SIMULATION_START = 10;

    /**
     * 暂停模拟导航 - 11
     */
    int SIMULATION_PAUSE = 11;

    /**
     * 停止模拟导航 - 12
     */
    int SIMULATION_STOP = 12;

    /**
     * 开始TTS播报 - 13
     */
    int TTS_PLAY_START = 13;

    /**
     * 停止TTS播报 - 14
     */
    int TTS_PLAY_FINISH = 14;

    /**
     * 比例尺放大- 15
     */
    int ZOOM_IN = 15;

    /**
     * 比例尺缩小- 16
     */
    int ZOOM_OUT = 16;

    /**
     * 2D车首上 - 17
     */
    int CAR_UP_2D = 17;

    /**
     * 2D北首上 - 18
     */
    int NORTH_UP_2D = 18;

    /**
     * 3D车首上 - 19
     */
    int CAR_UP_3D = 19;

    /**
     * TMC打开 - 20
     */
    int TMC_ON = 20;

    /**
     * TMC关闭 - 21
     */
    int TMC_OFF = 21;

    /**
     * 达到最大比例尺 - 22
     */
    int ZOOM_MAX = 22;

    /**
     * 达到最小比例尺 - 23
     */
    int ZOOM_MIN = 23;

    /**
     * 进入巡航播报状态- 24
     */
    int CRUISE_PLAY_START = 24;

    /**
     * 退出巡航播报状态- 25
     */
    int CRUISE_PLAY_END = 25;

    /**
     * 收藏夹家变化状态- 26
     */
    int FAV_HOME_CHANGE = 26;

    /**
     * 收藏夹公司变化状态- 27
     */
    int FAV_COMPANY_CHANGE = 27;

    /**
     * 点击查周边的状态- 28
     */
    int CLICK_SEARCH_NEAR = 28;

    /**
     * 点击搜地点的状态- 29
     */
    int CLICK_SEARCH_DEST = 29;

    /**
     * send to car消息消失-30
     */
    int SEND_TO_CAR_DISMISS = 30;

    /**
     * 停车场推送消息消失-31
     */
    int PARK_DISMISS = 31;

    /**
     * 续航通知消息消失-32
     */
    int CONTINUE_NAVIGATION_DISMISS = 32;

    /**
     * 算路失败消息消失-33
     */
    int CALCUATE_ROUTE_FINISH_FAIL_DISMISS = 33;

    /**
     * 导航中手动退出导航时通知-34
     */
    int MANUAL_EXIT_NAVIGATION = 34;

    /**
     * 导航中手动退出导航弹出框消失通知-35
     */
    int MANULA_EXIT_NAVIGATION_DISMISS = 35;

    /**
     * 普通收藏夹变化通知-36
     */
    int FAV_NORAML_CHANGE = 36;

    /**
     * 自动昼夜模式白天状态通知-37
     */
    int AUTO_MODE_DAY = 37;

    /**
     * 自动昼夜模式白天状态通知-38
     */
    int AUTO_MODE_NIGHT = 38;

    /**
     * 导航到达目的地-39
     */
    int AUTO_NAVI_ARRIVAE_DESTINATION = 39;

    /**
     * 导航处于活跃状态=心跳)
     */
    int AUTO_ALIVE = 40;

    /**
     * 进入导航沉浸播报状态- 43
     */
    int NAVI_IMMERSION_PLAY_START = 43;

    /**
     * 退出导航沉浸播报状态- 44
     */
    int NAVI_IMMERSION_PLAY_END = 44;
    //—————————————对外新增的类型往这里加吧———————————————

    //—————————————以下是未对外提供的状态，由于枚举定义的排序，影响到的整型值，所以关于新增类型的定义需要注意———————————————

    /**
     * 完全运行结束，退出程序-
     */
    int FINISHEND = 45;

    /**
     * 主界面-包括主图巡航界面和警告界面
     */
    int MAIN_PAGE = 46;

    /**
     * 子界面-除了包括主图巡航界面和警告界面的其他界面
     */
    int CHILD_PAGE = 47;

    /**
     * GPS已定位
     */
    int GPS_LOCATED = 48;

    /**
     * 主图activity oncreate状态
     */
    int ACTIVITY_ONCREATE = 49;

    /**
     * activity获得焦点
     */
    int ACTIVITY_ONRESUME = 50;

    /**
     * activity失去焦点
     */
    int ACTIVITY_ONPAUSE = 51;

    /**
     * 到达目的地卡片显示
     */
    int DESTINATION_MENU_VIEW_SHOW = 52;

    /**
     * 到达目的地卡片消失
     */
    int DESTINATION_MENU_VIEW_DISMISS = 53;

    /**
     * 进入敬告界面
     */
    int ENTER_WARN = 60;

    /**
     * 进入路况查询页面
     */
    int TRAFFIC_VIEW_IN = 61;

    /**
     * 退出路况查询页面
     */
    int TRAFFIC_VIEW_OUT = 62;

    /**
     * 永久静音
     */
    int PERMANENT_MUTE_ON = 70;
    /**
     * 取消静音
     */
    int MUTE_OFF = 71;

    /**
     * 临时静音
     */
    int TEMPORARY_MUTE_ON = 72;

    /**
     * 进入导航设置界面
     */
    int NAVI_SETTING_IN = 101;

    /**
     * 退出导航设置界面
     */
    int NAVI_SETTING_OUT = 102;

    /**
     * 进入路线规划界面
     */
    int ROUTE_START = 103;

    /**
     * 退出路线规划界面
     */
    int ROUTE_STOP = 104;

    /**
     * 路线规划选中第一条路线
     */
    int ROUTE_SELECTED_FIRST = 107;

    /**
     * 路线规划选中第二条路线
     */
    int ROUTE_SELECTED_SECOND = 108;

    /**
     * 路线规划选中第三条路线
     */
    int ROUTE_SELECTED_THIRD = 109;

    /**
     * 账号登录
     */
    int LOGIN_SUCCESS = 110;

    /**
     * 账号退出
     */
    int LOGOUT_SUCCESS = 111;

    /**
     * 进入路口
     */
    int ENTER_CROSS = 112;

    /**
     * 退出路口
     */
    int EXIT_CROSS = 113;

    /**
     * 巡航
     */
    int CURRENT_CRUISE = 114;

    /**
     * 导航
     */
    int CURRENT_NAVI = 115;

    /**
     * 主图绘制第一帧
     */
    int FIRST_DRAW = 116;

    /**
     * 仪表主题切换
     */
    int THEME_CHANGE = 117;

    /**
     * 开始发送DR信息通知
     */
    int START_SEND_DR = 118;


    /**
     * 仪表绘制第一帧
     */
    int EXSCREEN_FIRST_DRAW = 200;

    /**
     * 对外发送电子眼信息
     */
    int NAVI_MAP_DOG = 201;

    /**
     * 路口大图出现
     **/
    int CROSS_MAP_START = 202;
    /**
     * 路口大图关闭
     **/
    int CROSS_MAP_CLOSE = 203;

    /**
     * 路线规划结果页
     */
    int TOUCH_ROUTECARRESULT = 301;

    /**
     * 搜索结果列表页
     */
    int TOUCH_SEARCHRESULT = 302;
    /**
     * 导航中目的地停车场弹框界面
     */
    int TOUCH_NAVI_PARKING_DIALOG = 303;

    /**
     * 事件上报页面
     */
    int TOUCH_TRAFFIC_REPORT = 304;

    /**
     * 导航中路线更新成功
     */
    int NAVI_REROUTE_SUCCESS = 305;

    /**
     * 导航中路线更新失败
     */
    int NAVI_REROUTE_FAIL = 306;

    /**
     * 导航中偏航重算开始
     */
    int NAVI_YAW_REROUTE = 307;

    /**
     * 导航中途手动退出导航
     */
    int NAVI_GUIDE_EXIT = 308;

    /**
     * 进入隧道
     */
    int NAVI_ENTERTUNNEL = 309;

    /**
     * 退出隧道
     */
    int NAVI__EXITTUNNEL = 310;

    /**
     * GFrame第一帧
     */
    int GFRAME_FIRST_DRAW = 311;

    /**
     * VR场景使用：导航发起初始化主动对话|免唤醒功能
     */
    int WAKEUP_INIT = 312;

    /**
     * VR场景使用：导航通知停止主动对话|免唤醒功能
     */
    int WAKEUP_STOP = 313;

    /**
     * 疲劳驾驶场景使用：通知系统，导航在疲劳驾驶播报
     */
    int STATUS_FIGURE_DRIVING = 314;

    /**
     * Auto迁移全链路无法定位日志：开始
     */
    int STATUS_LOC_MONITOR_START = 315;

    /**
     * Auto迁移全链路无法定位日志：结束
     */
    int STATUS_LOC_MONITOR_STOP = 316;

    /**
     * 恢复上下文成功
     */
    int STATUS_LOC_RELATED_SUCCESS = 317;

    /**
     * 恢复上下文失败
     */
    int STATUS_LOC_RELATED_FAILED = 318;

    /**
     * 置前台后地图第一帧完成
     */
    int INFO_MAP_FOREGROUNDED = 319;

    /**
     * 巡航开始
     */
    int CRUISE_START = 401;

    /**
     * 巡航结束
     */
    int CRUISE_STOP = 402;

    /**
     * 车头方向
     */
    int HEAD_DIRECTION = 403;

    /**
     * 导航正在初始化
     */
    int APP_INITING = 404;

    /**
     * 导航正在激活
     */
    int APP_ACTIVATING = 405;

    /**
     * 导航可以输出图像到仪表盘
     */
    int DB_READY = 406;

    /**
     * 导航侧按钮点击声音播放
     */
    int KEY_SOUND_PLAY = 407;

    /**
     * 导航协议同意按钮被点击的通知
     */
    int AGREE_AUTONAVI_PROTOCOL = 408;

    /**
     * 点击躲避拥堵窗口避开按钮的通知
     */
    int AVOID_TRAFFIC_JAM_TRUE = 409;

    /**
     * 点击躲避拥堵窗口忽略按钮的通知
     */
    int AVOID_TRAFFIC_JAM_FALSE = 410;

    /**
     * 躲避拥堵弹窗消息的通知
     */
    int AVOID_TRAFFIC_JAM_HIDE = 411;

    /**
     * HMI可以响应外部切换
     */
    int AVOID_HMI_SWTICH_READY = 412;

    /**
     * HMI不能响应外部切换
     */
    int AVOID_HMI_SWTICH_NOT_READY = 413;

    /**
     * 导航中手动退出导航对话框弹出时通知
     */
    int MANUAL_QUIT_DLG_SHOW = 414;

    /**
     * GPS未定位
     */
    int GPS_UNLOCATED = 415;

    /**
     * 导航模式选择“地图导航”、“AR导航”或“AR在中控显示”时进入AR导航界面
     */
    int AR_ENTER_MAIN = 501;

    /**
     * 导航模式选择“地图导航”、“AR导航”或“AR在中控显示”时退出AR导航界面
     */
    int AR_EXIT_MAIN = 502;

    /**
     * 导航模式选择“AR在仪表显示”时进入AR导航界面
     */
    int AR_ENTER_EXSCREEN = 503;

    /**
     * 导航模式选择“AR在仪表显示”时退出AR导航界面
     */
    int AR_EXIT_EXSCREEN = 504;

    /**
     * 进入AR巡航
     */
    int AR_ENTER_CRUISE = 505;

    /**
     * 退出AR巡航
     */
    int AR_EXIT_CRUISE = 506;

    /**
     * mqtt初始化完成的状态
     */
    int MQTT_INIT_FINISH = 507;

    /**
     *新增协议类id统一从3001开始，广播和aidl保持一致
     */

    /**
     * 恢复默认设置成功
     */
    int RESTORE_SETTING_SUCCESS = 3001;

    /**
     * 恢复默认设置失败
     */
    int RESTORE_SETTING_FAIL = 3002;

    /**
     * 进入导航全览
     */
    int ROUTE_OVERVIEW_IN = 3003;

    /**
     * 退出导航全览
     */
    int ROUTE_OVERVIEW_OUT = 3004;

    /**
     * 开始退出程序
     */
    int START_QUIT_APP = 3005;

    /**
     * 【ar摄像头】未获取到第一帧图片
     */
    int AR_CAMERA_FIRST_IMAGE = 3006;
    /**
     * 开始组队录音
     */
    int AGROUP_START_RECORDING = 3007;
    /**
     * 结束组队录音
     */
    int AGROUP_STOP_RECORDING = 3008;

    /**
     * 超速中
     */
    int OVERSPEED = 3018;

    /**
     * 未超速
     */
    int UN_OVERSPEED = 3019;

    /**
     * 定位权限为关
     */
    int NAVI_INFO_LOC_AUTH_OFF = 3020;
    /**
     * 定位权限为开
     */
    int NAVI_INFO_LOC_AUTH_ON = 3021;
    /**
     * 定位服务为关
     */
    int NAVI_INFO_LOC_SERVICE_OFF = 3022;
    /**
     * 定位服务为开
     */
    int NAVI_INFO_LOC_SERVICE_ON = 3023;

    /**
     * 用户未同意开机敬告
     */
    int NAVI_INFO_AUTO_ATTENTION_DISAGREE = 3024;
    /**
     * 用户同意开机敬告
     */
    int NAVI_INFO_AUTO_ATTENTION_AGREE = 3025;

    /**
     * 导航Widget绘制第一帧
     */
    int WIDGET_FIRST_DRAW = 3026;

    /**
     * 重新算路通知
     */
    int REROUTING = 3027;

    /**
     * 仪表投屏结束通知
     */
    int EXSCREEN_STOP_RENDERING = 3028;

    /**
     * 手车更改目的地成功
     */
    int LINK_CAR_REROUTE_SUCCESS = 10001;

    /**
     * 手车更改目的地失败
     */
    int LINK_CAR_REROUTE_ERROR = 10002;

    /**
     * 手车路线偏好修改算路成功
     */
    int LINK_CAR_REROUTE_PREF_SUCCESS = 10003;

    /**
     * 手车路线偏好修改算路失败
     */
    int LINK_CAR_REROUTE_PREF_ERROR = 10004;

    /**
     * 手车添加途经点成功
     */
    int LINK_CAR_ADD_MID_SUCCESS = 10005;

    /**
     * 手车添加途经点失败
     */
    int LINK_CAR_ADD_MID_ERROR = 10006;

    /**
     * 手车删除途经点成功
     */
    int LINK_CAR_DELETE_MID_SUCCESS = 10007;

    /**
     * 手车删除途经点失败
     */
    int LINK_CAR_DELETE_MID_ERROR = 10008;

    /**
     * 进入了导航页面且显示
     */
    int LINK_CAR_IN_NAVI = 10009;
}

