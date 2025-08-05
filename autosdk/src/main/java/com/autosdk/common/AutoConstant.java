package com.autosdk.common;

import com.autosdk.common.utils.SdkPathUtils;

/**
 * Created by AutoSdk.
 */
public class AutoConstant {
    public static String SD_PATH = SdkPathUtils.getRootPath();
    public static final String PATH = SD_PATH + "amapauto9/";
    public static final String OLD_PATH = SD_PATH + "DsvAutoPsMap/";
    public static final String BLLOG_DIR = PATH + "bllog/"; // bl日志存储路径
    public static final String DEMOLOG_DIR = BLLOG_DIR + "demolog/"; // demo日志存储路径,统一放在 bllog/ 目录下,方便自动化脚本提取
    public static final String RES_DIR = AutoConstant.PATH + "res/"; // 热更新/tbt语音播报等相关配置文件
    public static final String OFFLINE_CONF_DIR = AutoConstant.PATH + "offline_conf/"; // 地图数据配置文件( all_city_compile.json 文件)所在目录
    public static final String OFFLINE_DOWNLOAD_DIR = AutoConstant.PATH + "data/navi/compile_v2/chn/";
    public static final String ACCOUNT_DIR = AutoConstant.PATH + "account/"; // 用户数据文件( all_city_compile.json 文件)所在目录
    public static final String SYNC_DIR = AutoConstant.PATH + "behavior/"; // 用户数据文件( all_city_compile.json 文件)所在目录
    public static final String NAVI_PATH_DATA = AutoConstant.PATH + "navidata/pathdata.bin"; //路线编码并保存到指定文件中
    public static final String NAVI_GUIDE_PATH_DATA = AutoConstant.PATH + "navidata/pathGuideData.bin"; //500后新增guide编码并保存到指定文件中
    public static final String PUSH_DIR = AutoConstant.PATH + "push/"; // push消息所在目录
    public static final String THEME_DATA_DIR = AutoConstant.PATH + "theme"; // 离线主题数据存储路径
    public static final String THEME_CONF_DIR = AutoConstant.PATH + "theme/theme_conf"; // 离线主题配置文件所存放的目录
    public static final String OFFLINE_AR_DIR = AutoConstant.PATH + "ar/cache"; // 离线主题配置文件所存放的目录
    public static final String MAP_ACTIVE_DIR = AutoConstant.PATH + "activation/";//激活文件存放路径
    public static final String AOS_DIR = AutoConstant.PATH + "aosinfo.db";
    public static final String BLLOG_CONFIG_DIR = PATH + "logConfig/";//日志配置文件路径
    public static final String BLLOG_CONFIG_NAME = BLLOG_CONFIG_DIR + "log_config";//日志配置文件
    public static final String MAP_CACHE_DIR = PATH + "data/mapcache/";   //地图缓存数据路径
    public static final String VOICE_CONF_DIR = PATH + "speech_conf/voice_conf/"; // 导航语音配置文件所存放的目录
    public static final String FLYTEK_STORED_PATH = PATH + "speech_conf/";//讯飞语音数据最终存储目录
    public static final String VOICE_DEFAULT_IRF = PATH + "speech_conf/xiaoyan.irf";//默认语音包

    public static final String FONT_DIR = AutoConstant.PATH + "font/"; //字体路径
    public static final String MAPASSET_DIR = "/android_assets/blRes/MapAsset/";
    public static final String DMAPASSET_DIR = AutoConstant.PATH + "DMapAsset/";
    public static final String LAYER_ASSET_DIR = "/android_assets/blRes/LayerAsset/";
    public static final String LAYER_ASSET_IMAGE_DIR = AutoConstant.PATH + "CardRes/images/";
    public static final String DYNAMIC_LAYER_ASSET_DIR = AutoConstant.PATH + "dynamic/layers/";
    public static final String GROUP_DIR = AutoConstant.PATH + "group/"; // 组队对讲数据文件所在目录
    public static final String GROUP_DOWNLOAD_DIR = AutoConstant.PATH + "group/download/"; // 组队下载目录
    public static final String GROUP_RECORD_DIR = AutoConstant.PATH + "group/record/";// 组队对讲录音文件目录

    public static final String REPLAY_RECORD_FILE_DIR = AutoConstant.PATH + "replay_record/";// 录制、回放文件存放路径

    public static final String routeCarResultData = "routeCarResultData";//路线数据保存

    /**
     * 多屏一致性路线数据目录
     */
    public static final String NAVI_CONSIS_PATH_DATA = AutoConstant.PATH + "navidata/consisPathData.bin";

    public static final String DEBUG_LIBS_DIR = AutoConstant.PATH + "libs";

    /**
     * 车道级导航
     */
    public static final String OFFLINE_LANERESOURCE_DIR = AutoConstant.PATH + "LaneCarSRResource/"; // 自车SR信息测试

    public static final String GPS_LANELOC_FOLDER = PATH + "loc_replay/"; // 车道级回放文件所在目录
    public static final String RECORDER_DATA_DIR = PATH + "recorder/"; //仿真回放路径

    public static final String CHANNEL_NAME = "";

    public static int mScreenWidth = 0;
    public static int mScreenHeight = 0;
    public static String compassMarkerId = "";
    public static String east_marker_id = "";
    public static String south_marker_id = "";
    public static String west_marker_id = "";
    public static String north_marker_id = "";
    public static String track_arc_marker_id = "";

    public static float enlargeViewX = 41f; //路口大图位置X方位
    public static float enlargeViewY = 290f;//路口大图位置Y方位

    public static String vehicleId = "";//全局车牌号

    public static final boolean SUPPORT_3D_CAR_LOGO = false;//是否支持3D车标

}
