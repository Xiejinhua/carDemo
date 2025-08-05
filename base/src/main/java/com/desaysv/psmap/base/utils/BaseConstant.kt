package com.desaysv.psmap.base.utils

import android.os.Environment
import androidx.annotation.IntDef
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.base.BuildConfig
import com.desaysv.psmap.base.def.MapModeType

/**
 * 常量或者全局变量
 */
object BaseConstant {
    var uid = ""
    const val CHANNEL_NUMBER = "C12589794702" //渠道号
    const val PID = "213086" //鉴权时用
    const val JT_VERSION = "JT_Maps_AL_750.1.1.0001" //捷途软件版本号， JT_Maps：捷途汽车地图，AL:AutoLink， 750：高德SDK版本，1：基线项目，1：第一款车，000X本项目软件发布次数
    var APP_VERSION = ""

    const val KEY_GLOBAL_ACTIVATE_FLAG = "desayMapAutoActivateFlag" //全局激活标志,仅供外部使用
    const val GLOBAL_ACTIVATE_NO = 0 //未激活标志
    const val GLOBAL_ACTIVATE_YES = 1 //已激活标志
    const val ACTION_AUTO_ACTIVATE = "desayMapAutoActivate" //自动激活广播action
    const val ACTION_AUTO_ACTIVATE_RESULT = "desayMapAutoActivateResult" //自动激活结果广播
    const val KEY_AUTO_ACTIVATE_TYPE = "activateType" //激活类型， 0 网络激活，1 usb激活
    const val AUTO_ACTIVATE_TYPE_NET = 0 //0 网络激活
    const val AUTO_ACTIVATE_TYPE_USB = 1 //1 usb激活
    const val AUTO_ACTIVATE_TYPE_MANUAL = 2 //2 手动激活
    const val KEY_AUTO_ACTIVATE_RESULT_MSG = "activateResultMessage" //激活失败理由

    const val TERMS_LINK = "https://cache.gaode.com/activity/auto_legal_instrument/index.html" //服务条款--链接
    const val POLICY_LINK = "https://cache.gaode.com/activity/auto_legal_instrument/privacyPolicy.html" //隐私权政策--链接
    const val TERMS_LINK_NIGHT = "https://cache.gaode.com/activity/auto_legal_instrument/index.html?theme=dark" //服务条款--链接--黑夜
    const val POLICY_LINK_NIGHT = "https://cache.gaode.com/activity/auto_legal_instrument/privacyPolicy.html?theme=dark" //隐私权政策--链接--黑夜
    const val ACCOUNT_LINK = "https://cache.gaode.com/activity/auto_legal_instrument/personalInfo.html" //备案号国家网站
    const val ICP_LINK = "https://beian.miit.gov.cn/" //备案号国家网站

    var UUID_EMPTY = false //uuid是否为默认值000000000000000 或者 空
    const val LOCATION_UUID = "locationUuid" //用于保存uuid，在获取系统uuid为空时使用

    val SETTING_DATA_PATH = AutoConstant.PATH + "settingData/"
    val NETWORK_LOG_FLAG = AutoConstant.BLLOG_DIR + "gnet_stat_a.txt" //高德网络日志标志位文件，需要与log_GNet.log同级，创建完成后程序全部网络请求数据在gnet_stats/目录下采集生成

    //不同朝向模式下的比例尺等级
    const val ZOOM_LEVEL_2D = 15.0f
    const val ZOOM_LEVEL_3D = 18.0f

    const val EX1_ZOOM_SCREEN_WIDTH = 1920L
    const val EX1_ZOOM_SCREEN_HEIGHT = 720L

    const val Ex1_Zoom_Level_2D = 16.0f
    const val Ex1_Zoom_Level_3D = 18.0f

    const val CCP_COUNT_DOWN_TOTAL_TIME = 10000L//回车位倒计时

    const val PERMISSION_REQUEST_CODE = 0x13//权限请求ID

    var roadConditionsAhead = "roadConditionsAhead" //巡航播报前方路况  0：off； 1：on
    var electronicEyeBroadcast = "electronicEyeBroadcast" //巡航播报电子眼播报  0：off； 1：on
    var safetyReminder = "safetyReminder" //巡航播报安全提醒  0：off； 1：on

    @MapModeType
    var currentMapviewMode = MapModeType.VISUALMODE_UNKNOW//当前车标模式

    @MapModeType
    var currentMapviewModeEx1 = MapModeType.VISUALMODE_UNKNOW//扩展屏，当前车标模式

    /**
     * 是否使用卡片实现纹理
     */
    const val USE_CARD_TEXTURE = true

    /**
     * 是否支持单SDK多屏
     */
    const val MULTI_MAP_VIEW = BuildConfig.multiMapViewNumber > 1

    /**
     * 登录状态
     */
    const val LOGIN_STATE_LOADING = 100 //登录中
    const val LOGIN_STATE_SUCCESS = 200 //登录成功
    const val LOGIN_STATE_GUEST = 300 //未登录-游客模式
    const val LOGIN_STATE_FAILED = 400 //登录失败
    const val LOGOUT_STATE_LOADING = 500 //退出登录中

    const val SIZE_TYPE_B = 1 //获取文件大小单位为B的double值
    const val SIZE_TYPE_KB = 2 //获取文件大小单位为KB的double值
    const val SIZE_TYPE_MB = 3 //获取文件大小单位为MB的double值
    const val SIZE_TYPE_GB = 4 //获取文件大小单位为GB的double值

    /***
     * 导航相关
     */
    const val NAVI_ID: Long = 0 //真实导航ID
    const val NAVI_SIM_ID: Long = 1 //模拟导航ID

    const val NAVI_STATE_INIT_NAVI_STOP: Int = 0 //初始化未导航的状态
    const val NAVI_STATE_REAL_NAVING: Int = 1 //真实导航中
    const val NAVI_STATE_SIM_NAVING: Int = 2 //模拟导航中
    const val NAVI_STATE_STOP_REAL_NAVI: Int = 3 //真实导航停止
    const val NAVI_STATE_STOP_SIM_NAVI: Int = 4 //模拟导航停止
    const val NAVI_STATE_PAUSE_SIM_NAVI: Int = 5 //模拟导航暂停

    const val NAVI_APP_STATE_FOREGROUND: Int = 6 //导航应用前台
    const val NAVI_APP_STATE_BACKGROUND: Int = 7 //导航应用后台
    const val REQUEST_LOADING_TIP = "requestLoadingTip" //请求loading提示语
    const val REQUEST_LOADING_CLOSE_TIME = "requestLoadingCloseTime" //请求loading 自动关闭时间

    @IntDef(
        *[Type.NEED_NULL, Type.NEED_REQUEST_RX_PLAN_ROAD, Type.NEED_RX_PLAN_HAVE_SUCCESS,
            Type.NEED_FILE_DATA_HAVE_SUCCESS, Type.NEED_PHONE_SEND_ROUTE_DATA,
            Type.NEED_REQUEST_RX_PLAN_ROAD_MISPOI, Type.NEED_REQUEST_RX_PLAN_MANUAL_REFRESH]
    )
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class Type {
        companion object {
            const val NEED_NULL = 0 //不存在路线或不需要发起路径规划
            const val NEED_REQUEST_RX_PLAN_ROAD = 1 //需要发起路径规划
            const val NEED_RX_PLAN_HAVE_SUCCESS = 2 //路径规划已经成功
            const val NEED_FILE_DATA_HAVE_SUCCESS = 3 //从本地文件获取保存的路径规划的数据
            const val NEED_PHONE_SEND_ROUTE_DATA = 4 //手机推送发送的数据
            const val NEED_REQUEST_RX_PLAN_ROAD_MISPOI = 5 //需要发起路径规划 添加途经点 删除途经点
            const val NEED_REQUEST_RX_PLAN_MANUAL_REFRESH = 6 //行中手动刷新路线 RouteTypeManualRefresh = 12
        }
    }

    /**
     * 离线地图dialog处理状态
     */
    const val OFFLINE_DIALOG_STATE_CONFIRM = 100 //确定并关闭dialog
    const val OFFLINE_DIALOG_STATE_CANCEL = 200 //取消并关闭dialog
    const val OFFLINE_DIALOG_STATE_OTHER = 300 //关闭dialog

    /**
     * 离线地图自定义下载状态
     */
    const val OFFLINE_STATE_UPDATE = 100 //更新状态
    const val OFFLINE_STATE_T0_DOWNLOAD = 200 //全新未下载状态
    const val OFFLINE_STATE_DOWNLOAD = 300 //下载状态
    const val OFFLINE_STATE_PAUSE = 400 //暂停状态
    const val OFFLINE_STATE_COMPLETE = 500 //完成状态

    /**
     * 离线地图自定义dialog参数
     */
    const val OFFLINE_IS_BATCH = "isBatch"
    const val OFFLINE_CITY_NAME = "cityName"
    const val OFFLINE_AD_CODE = "adCode"
    const val OFFLINE_AD_CODES = "adCodes"

    /**
     * 组队卡片类型
     */
    const val GROUP_DEFAULT_TYPE = 0 //default 默认 进来组队页面UI
    const val GROUP_SETTING_TYPE = 1 //点击了设置
    const val GROUP_CHANGE_USERNAME_TYPE = 2 //点击了 修改昵称
    const val GROUP_REMOVE_MEMBERS_TYPE = 3 //点击了移除组员
    const val GROUP_START_CALL_TYPE = 4 //加入对讲
    const val GROUP_START_CALL_SETTING_TYPE = 5 //聊天界面点击了设置
    const val GROUP_START_CALL_CHANGE_USERNAME_TYPE = 6 //聊天界面点击了修改昵称

    /**
     * 用户退出状态 0无 1被踢出队伍 2解散队伍
     */
    const val GROUP_EXIT_DEFAULT_TYPE = 0 //default 默认
    const val GROUP_EXIT_USER_KICK_TYPE = 1 //被踢出队伍
    const val GROUP_EXIT_TEAM_DISBAND_TYPE = 2 //解散队伍

    const val PLAY_TRAFFIC_STATUS_ID = 2207091 //是否查询路况ID

    const val MAP_OUTPUT_DATA_VERSION = "V1.0.0_240228"

    const val SETTING_TAB = "settingTab" //设置fragment标签
    const val ACCOUNT_SETTING_TAB = "accountSettingTab" //个人中心&设置fragment标签

    /**
     * 进入组队界面类型
     */
    const val TO_TEAM_MAIN_TYPE = 1 //主图按钮进入
    const val TO_TEAM_SETTING_TYPE = 2 //设置tab进入
    const val TO_TEAM_OTHER_TYPE = 3 //其他界面进入

    /**
     * 进入收藏界面类型
     */
    const val TO_FAVORITE_MAIN_TYPE = 1 //从主页或设置进入收藏界面
    const val TO_FAVORITE_VIA_TYPE = 2 //从沿途搜进入收藏界面
    const val TO_FAVORITE_TEAM_DESTINATION = 3 //从组队出行进入收藏界面

    /**
     * 手机poi推送
     */
    const val AIM_POI_PUSH_MSG = "aimPoiPushMsg" //poi数据
    const val AIM_ROUTE_PUSH_MSG = "aimRoutePushMsg" //路线数据
    const val POI_TYPE = "poiType" // 1.poi 2.路线

    //模拟导航车速 180、480、680
    const val SIM_NAVI_SPEED_LOW = 180
    const val SIM_NAVI_SPEED_MEDIUM = 480
    const val SIM_NAVI_SPEED_HIGH = 680

    /**
     * 预测用户家/公司的位置
     */
    const val REQ_ADDRESS_QUERY_TYPE = "user_profile" //用户区分不同场景，为后期新增场景做备份
    const val REQ_ADDRESS_LABEL_HOME = "home" //label 支持一个和多个属性的请求，只请求家，则只传home；请求家和公司，则传home|company
    const val REQ_ADDRESS_LABEL_COMPANY = "company" //label 支持一个和多个属性的请求，只请求家，则只传home；请求家和公司，则传home|company
    const val REQ_ADDRESS_LABEL_HOME_COMPANY = "home|company" //label 支持一个和多个属性的请求，只请求家，则只传home；请求家和公司，则传home|company
    const val ADDRESS_PREDICT_PUSH_MSG = "addressPredictPushMsg" //预测用户家/公司数据

    const val IS_DATA_INIT = "isDataInit" //用于地图分区初始化失败，删除标志

    /**
     * 组队业务类型-针对界面功能的，比如组队消息弹条
     */
    const val TYPE_GROUP_PUSH_MESSAGE = 100 //组队消息弹条
    const val TYPE_GROUP_CREATE = 200 //创建组队界面
    const val TYPE_GROUP_INVITE_JOIN = 300 //邀请好友界面
    const val TYPE_GROUP_MY = 400 //我的组队界面
    const val TYPE_GROUP_MAIN = 500 //主界面Activity
    const val TYPE_GROUP_SETTING = 600 //个人中心界面
    const val TYPE_GROUP_MY_MESSAGE = 700 //我的消息界面

    /**
     * 组队业务类型-针对高德回调的， 比如获取队伍状态结果回调通知
     */
    const val TYPE_GROUP_STATUS = 100 //获取队伍状态结果回调通知
    const val TYPE_GROUP_JOIN = 200 //加入队伍结果回调通知
    const val TYPE_GROUP_INFO = 300 //获取队伍信息回调通知
    const val TYPE_GROUP_CREATE_FUN = 400 //创建队伍回调通知
    const val TYPE_GROUP_FRIEND_LIST = 500 //获取历史好友回调通知
    const val TYPE_GROUP_INVITE = 600 //邀请好友回调通知
    const val TYPE_GROUP_INVITE_QRURL = 700 //获取队伍口令二维码链接回调通知
    const val TYPE_GROUP_URL_TRANSLATE = 800 //链接转为二维码回调通知
    const val TYPE_GROUP_NICK_NAME = 900 //请求修改队伍中的昵称回调通知
    const val TYPE_GROUP_DISSOLVE = 1000 //请求解散队伍回调通知
    const val TYPE_GROUP_QUIT = 1100 //请求退出队伍回调通知
    const val TYPE_GROUP_KICK = 1200 //队长踢人请求回调通知

    const val RESEND_VERIFICATION_CODE_TIME = 60 * 1000
    const val PHONE_NUMBER_LEN = 11
    const val PHONE_VERIFY_LEN = 4

    /**
     * 问题反馈类型
     */
    const val TYPE_ISSUE = 100 //问题反馈列表
    const val TYPE_ISSUE_POS = 200 //定位问题
    const val TYPE_ISSUE_INTERNET = 300 //互联问题
    const val TYPE_ISSUE_DATA_DOWNLOAD = 400 //数据下载问题
    const val TYPE_ISSUE_BROADCAST = 500 //播报问题
    const val TYPE_ISSUE_OTHER = 600 //其他问题
    const val TYPE_ISSUE_PHONE = 700 //手机编辑
    const val TYPE_ISSUE_EDIT = 800 //问题编辑

    /**
     * 错误信息模块页面类型
     */
    const val TYPE_PAGE_ISSUE = 100 //问题反馈列表界面
    const val TYPE_PAGE_ISSUE_POS = 200 //定位界面
    const val TYPE_PAGE_ISSUE_INTERNET = 300 //互联界面
    const val TYPE_PAGE_ISSUE_DATA_DOWNLOAD = 400 //数据下载界面
    const val TYPE_PAGE_ISSUE_BROADCAST = 500 //播报界面
    const val TYPE_PAGE_ISSUE_OTHER = 600 //其他界面
    const val TYPE_PAGE_ISSUE_PHONE = 700 //手机编辑界面
    const val TYPE_PAGE_ISSUE_EDIT_DEC = 800 //问题描述编辑界面

    const val KEY_CONTINUE_SAPA_NAVI = "continueSapaNavi" //服务区续航标志


    /**
     * 帮助-内容类型
     */
    const val TYPE_HELP_POPULAR_QUESTIONS = 0 //热门问题
    const val TYPE_HELP_DRAWING_DISPLAY = 1 //图面显示
    const val TYPE_HELP_ROUTE_PLANNING = 2 //路线规划
    const val TYPE_HELP_SEARCH_FUNCTION = 3 //搜索功能
    const val TYPE_HELP_VOICE_BROADCAST = 4 //语音播报
    const val TYPE_HELP_MAP_DATA = 5 //地图数据

    const val SETTING = "com.desaysv.setting"
    const val SETTING_SERVICE = "com.desaysv.setting.SettingService"
    const val ACTION_VEHICLE_INFO = "com.desaysv.setting.ACTION_PLATE_NUMBER"
    const val ACTION_BLUETOOTH = "com.desaysv.setting.ACTION_BLUETOOTH_SETTING"
    const val SOURCE_ID = "10113"
    const val SOURCE_NAVIGATION = 38 //导航声音类型

    const val USB_ROOT_PATH = "/storage/usb0" //U盘路径
    const val USB_MAP_DATA_PATH = "$USB_ROOT_PATH/amapauto9" //U盘离线数据路径
    var MAP_APP_FOREGROUND = false // 地图应用是否在前台

    var NAV_MAP_ADAS = Environment.getExternalStorageDirectory().path + "/naviMapAdas/" //智驾数据保存目录
    const val VOLUME_TYPE_NAVIGATION = 12 //导航音量类型
    const val GLOBAL_ID = "sys.tsp.global.id"
    const val JETOUR_PUSH_MESSAGE_TYPE = "notification.mqtt"

    var TR_TTS_BROADCAST = 0 //查询前⽅路况 tts播报⽅(由谁播报) 0：auto 1：系统
    var TR_PKG = "" //查询前⽅路况 包名
    var TR_REQUEST_CODE: String? = "" //查询前⽅路况 请求cide

    //仪表主题 1、2、3、4，对应数字、经典、导航、极简
    const val CARDASHBOARD_THEME_DIGIT = 0
    const val CARDASHBOARD_THEME_CLASSIC = 1
    const val CARDASHBOARD_THEME_NAVI = 2
    const val CARDASHBOARD_THEME_SIMPLE = 3

    const val KEY_CARDASHBOARD_SHOW_ADAS = "DJ_PROJECTION_SCREEN"
    var TEAM_ID = "" //组队ID

    const val AHA_TRIP_SORT_SCORE = "score"
    const val AHA_TRIP_SORT_DAY = "day"

    var DESAYSV_IOV_LICENSE_OK = false
}