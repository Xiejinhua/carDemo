package com.autosdk.bussiness.manager;

import com.autonavi.gbl.servicemanager.model.ALCLogLevel;
import com.autonavi.gbl.servicemanager.model.FileCopyCheckMode;
import com.autonavi.gbl.servicemanager.model.ServiceManagerEnum;

public class    SDKInitParams {


    /* SDK日志名称前缀，生成的日志放置在logPath目录下 */
    public String logFileName = "sdkdemo.android.";

    /* SDK日志输出路径 */
    public String logPath = "./";

    /* 日志输出级别，默认不输出 */
    public long logLevel = ALCLogLevel.LogLevelNone;

    /* 后台服务器类型，默认用正式发布环境类型（千万不要轻易切换类型） */
    public int serverType = ServiceManagerEnum.AosProductionEnv;

    /* AutoSDK底层JNI层的日志是否打印到logcat一同输出 */
    public boolean bSDKLogcat = true;

    /* 日志输出的方式是同步还是异步，默认异步 */
    public boolean bLogAsync = false;

    /* 访问后台服务的cookie存放路径 */
    public String cookieDBPath;

    /* GRestConfig.ini、GblConfig.json 配置文件存放路径 */
    public String restConfigPath;
    /* GNaviConfig.xml、global.db等配置文件存放路径 */
    public String cfgFilePath = "";
    /* 在线数据本地缓存路径 */
    public String onlinePath = "";

    /* 离线地图数据存放路径（包含精品三维离线数据） */
    public String offlinePath = "";

    /* 激活文件存放路径 */
    public String activateFilePath = "";

    /**
     * 配置车道级离线地图数据
     */
    public String lndsOfflinePath = "";

    //BL_SDK资源文件的原始目录，如 /android_assets/blRes/
    public String assetPath = "";
    //缓存目录，存放asset导出文件或日志文件等，如 /sdcard/amapauto9/
    public String cachePath  = "";
    //用户数据目录，保存用户生成数据，如/data/data/com.autonavi.amapauto/files
    public String userDataPath = "";

    /**
     * @brief 指定渠道名称，用于单包多渠道资源拷贝
     * @default 空字符串，只有单包多渠道客户需要指定
     * @note 开放客户使用，基础版不使用
     * @note 单包多渠道指的是assets/blRes/下存在channel目录，channel下有多个子目录，每一个目录的名称即为渠道名，例如C04XXXX
     */
    public String channelName = "";

    /**
     * @brief 文件拷贝校验模式
     * @default FileSize
     * @note 只在enableCopyAsset=true时生效
     */
    public @FileCopyCheckMode.FileCopyCheckMode1 int checkMode;
}
