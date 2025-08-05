package com.autosdk.bussiness.navi.constant;

public class NaviConstant {

    public static final int DEFAULT_ERR_CODE = -9527;
    // ============================ TBT ================================
    //算路成功
    public static final int HANDLER_ON_NEW_ROUTE = 100000;
    //算路失败
    public static final int HANDLER_ON_ERROR_ROUTE = 100001;
    //POS回调信息
    public static final int HANDLER_ON_LOCINFO = 100002;
    //引导电子眼
    public static final int HANDLER_ON_SHOWCAMERA = 100003;
    //语音播报
    public static final int HANDLER_ON_ONPLAYTTS = 100004;
    //更新导航信息
    public static final int HANDLER_ON_UPDATENAVIINFO = 100005;
    //更新出口信息
    public static final int HANDLER_ON_UPDATEEXITINFO = 100006;
    //显示路口大图
    public static final int HANDLER_ON_SHOWCROSSPIC = 100007;
    //显示socol文本
    public static final int HANDLER_ON_UPDATESOCOL = 100008;
    //更新导航中的交通事件
    public static final int HANDLER_ON_UPDATEEVENT = 100009;
    //巡航中的道路设施
    public static final int HANDLER_ON_CRUISE_FAC = 100010;
    //隐藏放大路口
    public static final int HANDLER_ON_HIDE_CROSS = 100011;
    //显示放大路口
    public static final int HANDLER_ON_SHOW_MANEUVER = 100012;
    //主辅路更新
    public static final int HANDLER_ON_UPDATE_PARA = 100013;
    //主辅路切换
    public static final int HANDLER_ON_SWITCH_PARA = 100014;
    //巡航拥堵信息
    public static final int HANDLER_ON_CRUISE_CONGESTION = 100015;
    //巡航拥堵信息
    public static final int HANDLER_ON_UPDATE_BAR = 100016;
    //区间测速电子眼信息
    public static final int HANDLER_ON_SHOWINTERVALCAMERA = 100017;
    //区间测速电子眼动态信息
    public static final int HANDLER_ON_INTERVALCAMERADYNAMICINFO = 100018;
    //重新算路
    public static final int HANDLER_ON_REROTE = 100019;
    //onUpdateTMCCongestionInfo
    public static final int HANDLER_ON_CONGESTION = 100020;
    //地理位置改变
    public static final int HANDLER_ON_LOCATION = 100021;
    //分歧路口
    public static final int HANDLER_ON_SHOWMIXINFO = 100022;
    //隐藏车道线
    public static final int HANDLER_ON_HIDE_CRUISE_LANE_INFO = 100023;
    //更新SAPA
    public static final int HANDLER_ON_UPDATESAPA = 100024;
    //停止导航
    public static final int HANDLER_ON_STOPNAVI = 100025;
    //天气信息更新
    public static final int HANDLER_ON_WEATHER_UPDATE = 100026;
    //导航过程更新天气
    public static final int HANDLER_ON_NAVIWEATHER = 100027;
    //导航中的道路设施
    public static final int HANDLER_ON_NAVIFACILITY = 100028;
    //ETC车道线
    public static final int HANDLER_ON_GATELANE = 100029;
    //车道线
    public static final int HANDLER_ON_LANEINFO = 100030;
    //隐藏车道线
    public static final int HANDLER_ON_HIDELANE = 100031;
    //隐藏车道线
    public static final int HANDLER_ON_OBTAININFO = 100032;
    //基础限速功能
    public static final int HANDLER_ON_SPEED = 100033;
    //定位DR信息
    public static final int HANDLER_ON_DRINFO = 100034;
    //传出巡航状态下的交通事件信息
    public static final int HANDLER_ON_CRUISEEVENT = 100035;
    //传出巡航状态下的拥堵事件信息
    public static final int HANDLER_ON_CRUISESOCOL = 100036;
    //传出导航状态下的拥堵事件信息
    public static final int HANDLER_ON_NAVISOCOL = 100037;
    //传出导航状态下的拥堵事件信息
    public static final int HANDLER_ON_VEHICLEETAINFO = 100038;
    //传出红路灯交通信号信息
    public static final int HANDLER_ON_TRAFFICSIGNALINFO = 100039;
    // 精品三维大图路线支持路况显示
    public static final int HANDLER_ON_SHOW_NAVI_CROSS_TMC = 100040;
    // 更新经过充电站索引
    public static final int HANDLER_ON_UPDATE_CHARGE_STATION_PASS = 100041;
    // 点击路线或路线上的点(途经点/终点/起点)
    public static final int HANDLER_ON_NOTIFY_CLICK = 100042;
    public static final int HANDLER_ON_SIGNINFO_UPDATE = 100043;
    //显示巡航车道线
    public static final int HANDLER_ON_SHOW_CRUISE_LANE_INFO = 100044;
    //通知用户切换主导航路线状态
    public static final int HANDLER_ON_SELECT_MAIN_PATH_STATUS_INFO = 100045;
    //巡航引导信息
    public static final int HANDLER_ON_UPDATE_CRUISE_INFO = 100046;
    //巡航电子眼
    public static final int HANDLER_ON_UPDATE_ELEC_CAMERA_INFO = 100047;
    //连续巡航时间巡航
    public static final int HANDLER_ON_UPDATE_CRUISE_TIME_AND_DIST = 100048;
    //更新经过途经点索引
    public static final int HANDLER_ON_UPDATE_VIA_PASS = 100051;
    //删除对应id的路线
    public static final int HANDLER_ON_DELETE_PATH = 100052;
    //删除对应id的路线
    public static final int HANDLER_ON_CHANGE_NAVIPATH = 100053;
    //导航过程中通知建议用户切备选路线
    public static final int HANDLER_ON_SUGGEST_CHANGE_PATH = 100054;

    /**
     * tts播报
     */
    public static final int HANDLER_TTS_PLAYING = 100049;

    /**
     * 警告音播报
     */
    public static final int HANDLER_RING_PLAYING = 100050;

    /**
     * 沿途天气
     */
    public static final int HANDLER_WEATHER = 100055;
    /**
     * 导航到达终点
     */
    public static final int HANDLER_ARRIVE = 100056;
    //在线转向图标
    public static final int HANDLER_ON_MANEUVERICON = 100057;
    //出口编号和路牌方向信息
    public static final int HANDLER_ON_EXIT_DIRECTION = 100058;
    //服务区收费站信息
    public static final int HANDLER_ON_SAPA_INQUIRE = 100059;
    //更新重算设置，在发起算路之前
    public static final int ON_MODIFY_REROUTE_OPTION = 100063;
    public static final int ON_REROUTE_INFO = 100064;
    public static final int ON_SWITCHPARALLELROADREROUTE_INFO = 100065;
    /**
     * 路线沿途服务区
     */
    public static final int HANDLER_ALONG_SERVICE_AREA = 100066;
    //传出红路灯倒计时信息
    public static final int HANDLER_ON_TRAFFIC_LIGHT_COUNTDOWN = 100067;
    //传出红绿灯绿波车速
    public static final int HANDLER_GREEN_WAVE = 100068;

    public static final int HANDLER_ON_NAVI_REPORT = 100069;

    public static final int HANDLER_ON_NAVI_DRIVEEVENT = 100070;
    /**
     * {网络路径平行路的主辅路标识 有路可以切到：0--无主辅路 1--辅路  2--主路}
     */
    public static final int PARALLELROAD_FLAG_NONE = 0;
    public static final int PARALLELROAD_FLAG_SIDE = 1;
    public static final int PARALLELROAD_FLAG_MAIN = 2;

    /**
     * {网络路径平行路的主辅路标识 有路可以切到：0--无高架  1--高架下 2--高架上}
     */

    public static final int HW_FLAG_NONE = 0;
    public static final int HW_FLAG_DOWN = 1;
    public static final int HW_FLAG_UP = 2;

    /*******************  天气常量定义  start ******************************/
    /**  预警天气定义  **/
    /**
     * 台风预警
     */
    public static final int WEATHER_TYPHOON_WARNING = 1;
    /**
     * 暴雪预警
     */
    public static final int WEATHER_BLIZZARD_WARNING = 3;
    /**
     * 冰雹预警
     */
    public static final int WEATHER_HALE_WARNING = 10;
    /**
     * 道路结冰预警
     */
    public static final int WEATHER_ICY_ROAD_WARNING = 14;
    /**
     * 道路冰雪预警
     */
    public static final int WEATHER_ROAD_SNOW_ICE_WARNING = 21;
    /**
     * 暴雨预警
     */
    public static final int WEATHER_STORM_WARNING = 2;
    /**
     * 雷雨大风预警
     */
    public static final int WEATHER_THUNDERSTORM_WARNING = 17;
    /**
     * 雷电预警
     */
    public static final int WEATHER_THUNDER_WARNING = 9;
    /**
     * 大雾预警
     */
    public static final int WEATHER_FOGGY_WARNING = 12;
    /**
     * 霾预警
     */
    public static final int WEATHER_HAZE_WARNING = 13;
    /**
     * 灰霾预警
     */
    public static final int WEATHER_DUSTHAZE_WARNING = 16;
    /**
     * 沙尘暴预警
     */
    public static final int WEATHER_DUSTSTORM_WARNING = 6;
    /**
     * 大风预警
     */
    public static final int WEATHER_GALE_WARNING = 5;
    /**
     * 霜冻预警
     */
    public static final int WEATHER_FROST_WARNING = 11;
    /**
     * 晴
     */
    public static final int WEATHER_SUNNY = 100;
    /**
     * 多云
     */
    public static final int WEATHER_CLOUDY = 101;
    /**
     * 少云
     */
    public static final int WEATHER_FEW_CLOUDY = 102;
    /**
     * 晴间多云
     */
    public static final int WEATHER_PARTLY_CLOUDY = 103;
    /**
     * 阴
     */
    public static final int WEATHER_OVERCAST = 104;
    /**
     * 有风
     */
    public static final int WEATHER_WINDY = 200;
    /**
     * 平静
     */
    public static final int WEATHER_CALM = 201;
    /**
     * 微风
     */
    public static final int WEATHER_LIGHT_BREEZE = 202;
    /**
     * 和风
     */
    public static final int WEATHER_GENTLE_BREEZE = 203;
    /**
     * 清风
     */
    public static final int WEATHER_FRESH_BREEZE = 204;
    /**
     * 强风/劲风
     */
    public static final int WEATHER_STRONG_BREEZE = 205;
    /**
     * 疾风
     */
    public static final int WEATHER_HIGH_WIND = 206;
    /**
     * 大风
     */
    public static final int WEATHER_GALE = 207;
    /**
     * 烈风
     */
    public static final int WEATHER_STRONG_GALE = 208;
    /**
     * 风暴
     */
    public static final int WEATHER_WIND_STORM = 209;
    /**
     * 狂暴风
     */
    public static final int WEATHER_VIOLENT_STORM = 210;
    /**
     * 飓风
     */
    public static final int WEATHER_HURRICANE = 211;
    /**
     * 龙卷风
     */
    public static final int WEATHER_TORNADO = 212;
    /**
     * 热带风暴
     */
    public static final int WEATHER_TROPICAL_STROM = 213;

    /**
     * 阵雨
     */
    public static final int WEATHER_SHOWER_RAIN = 300;
    /**
     * 强阵雨
     */
    public static final int WEATHER_HEAVY_SHOWER_RAIN = 301;
    /**
     * 雷阵雨
     */
    public static final int WEATHER_THUNDER_SHOWER = 302;
    /**
     * 强雷阵雨
     */
    public static final int WEATHER_HEAVY_THUNDER_SHOWER = 303;
    /**
     * 雷阵雨伴有冰雹
     */
    public static final int WEATHER_HAIL = 304;
    /**
     * 小雨
     */
    public static final int WEATHER_LIGHT_RAIN = 305;
    /**
     * 中雨
     */
    public static final int WEATHER_MODERATE_RAIN = 306;
    /**
     * 大雨
     */
    public static final int WEATHER_HEAVY_RAIN = 307;
    /**
     * 极端降雨
     */
    public static final int WEATHER_EXTREME_RAIN = 308;
    /**
     * 毛毛雨/细雨
     */
    public static final int WEATHER_DRIZZLE_RAIN = 309;
    /**
     * 暴雨
     */
    public static final int WEATHER_STORM = 310;
    /**
     * 大暴雨
     */
    public static final int WEATHER_HEAVY_STORM = 311;
    /**
     * 特大暴雨
     */
    public static final int WEATHER_SEVERE_STORM = 312;
    /**
     * 冻雨
     */
    public static final int WEATHER_FREEZING_RAIN = 313;
    /**
     * 小雪
     */
    public static final int WEATHER_LIGHT_SNOW = 400;
    /**
     * 中雪
     */
    public static final int WEATHER_MODERATE_SNOW = 401;
    /**
     * 大雪
     */
    public static final int WEATHER_HEAVY_SNOW = 402;
    /**
     * 暴雪
     */
    public static final int WEATHER_SNOW_STORM = 403;
    /**
     * 雨夹雪
     */
    public static final int WEATHER_SLEET = 404;
    /**
     * 雨雪天气
     */
    public static final int WEATHER_RAIN_AND_SNOW = 405;
    /**
     * 阵雨夹雪
     */
    public static final int WEATHER_SHOWER_STORM = 406;
    /**
     * 阵雪
     */
    public static final int WEATHER_SNOW_FLURRY = 407;
    /**
     * 薄雾
     */
    public static final int WEATHER_MIST = 500;
    /**
     * 大雾
     */
    public static final int WEATHER_FOGGY = 501;
    /**
     * 雾霾
     */
    public static final int WEATHER_HAZE = 502;
    /**
     * 扬沙
     */
    public static final int WEATHER_SAND = 503;
    /**
     * 浮尘
     */
    public static final int WEATHER_DUST = 504;
    /**
     * 沙尘暴
     */
    public static final int WEATHER_DUSTSTORM = 507;
    /**
     * 强沙尘暴
     */
    public static final int WEATHER_SANDSTORM = 508;
    /**
     * 热
     */
    public static final int WEATHER_HOT = 900;
    /**
     * 冷
     */
    public static final int WEATHER_COLD = 901;
    /**
     * 冰粒
     */
    public static final int WEATHER_ICE1 = 1001;
    /**
     * 冰针
     */
    public static final int WEATHER_ICE2 = 1002;
    /**
     * 冰雹
     */
    public static final int WEATHER_HAIL_STONE = 1003;
    /**
     * 雷暴
     */
    public static final int WEATHER_THUNDER_STORM = 1004;
    /**
     * 雷电
     */
    public static final int WEATHER_THUNDER = 1005;
    /**
     * 寒潮
     */
    public static final int WEATHER_COLD_WAVE_WARNING = 4;
    /**
     * 高温
     */
    public static final int WEATHER_HIGH_TEMPERATURE_WARNING = 7;
    /**
     * 干旱
     */
    public static final int WEATHER_DROUGHT_WARNING = 8;
    /**
     * 寒冷
     */
    public static final int WEATHER_COLD_WARING = 15;
    /**
     * 森林火险
     */
    public static final int WEATHER_FOREST_FIRE_WARNING = 18;
    /**
     * 降温
     */
    public static final int WEATHER_COOLING_WARNING = 19;
    /**
     * 低温
     */
    public static final int WEATHER_LOW_TEMPERATURE_WARNING = 20;
    /**
     * 空气重污染
     */
    public static final int WEATHER_HEAVY_AIR_POLLUTION_WARNING = 22;
    /**
     * 干热风
     */
    public static final int WEATHER_DRY_HOT_WIND_WARNING = 23;
    /**
     * 火险
     */
    public static final int WEATHER_FIRE_WANING = 26;
    /**
     * 持续高温
     */
    public static final int WEATHER_CONTINUOUS_HIGH_TEMPERATURE_WARNING = 25;
    /**
     * 持续低温
     */
    public static final int WEATHER_CONTINUOUS_LOW_TEMPERATURE_WARNING = 24;
    /*******************  天气常量定义  end ******************************/
}
