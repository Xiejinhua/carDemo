package com.desaysv.psmap.base.impl

import android.content.Context
import com.autonavi.gbl.data.model.OperationType
import com.autonavi.gbl.data.model.Theme
import com.autosdk.bussiness.widget.setting.SettingConst
import com.desaysv.psmap.base.def.MapModeType
import com.google.gson.JsonObject


/**
 * Author : wangmansheng
 * Description : 设置同步项接口
 */
interface ISettingComponent {
    /**
     * 日夜模式 日夜模式。 16：自动模式，默认态； 17：日间模式； 18：夜间模式
     * @param intValue
     */
    fun setConfigKeyDayNightMode(intValue: Int) {}

    /**
     * 获取日夜模式
     */
    fun getConfigKeyDayNightMode(): Int {
        //默认值
        return if (com.autosdk.BuildConfig.dayNightBySystemUI) SettingConst.MODE_DEFAULT else SettingConst.MODE_DAY
    }

    /**
     * 个性化车标
     * 0 默认 1汽车 2飞船 3车速
     */
    fun setCarLogos(intValue: Int) {}

    /**
     * 获取个性化车标
     * 0 默认 1汽车 2飞船 3车速
     */
    fun getCarLogos(): Int {
        return 0
    }

    /**
     * 保存车牌号
     * @param carNumber
     */
    fun setConfigKeyPlateNumber(carNumber: String?) {}

    /**
     * 获取车辆基本信息
     * 车牌，限行，动力类型
     * @return
     */
    fun getBaseConfig(): JsonObject {
        return JsonObject()
    }

    /**
     * 获取车牌号
     */
    fun getConfigKeyPlateNumber(): String? {
        return null
    }

    /**
     * 避开限行 0关闭 1打开
     * @param intValue
     */
    fun setConfigKeyAvoidLimit(intValue: Int) {}

    /**
     * 获取避开限行
     */
    fun getConfigKeyAvoidLimit(): Boolean {
        return false
    }

    /**
     * 动力类型 -1: 无,未设置车牌号默认值 0:燃油车,已设置车牌号默认值 1:纯电动 2:插电式混动
     * @param intValue
     */
    fun setConfigKeyPowerType(intValue: Int) {}

    /**
     * 获取动力类型
     */
    fun getConfigKeyPowerType(): Int {
        return -1
    }

    /**
     * 路线偏好  默认0：高德推荐，字符类型,不包含2|4|8|16|32|64即为高德推荐 默认态； 2：躲避拥堵； 4：避免收费； 8：不走高速； 16：高速优先 32：速度最快  64：大路优先
     * @param planPrefString
     */
    fun setConfigKeyPlanPref(planPrefString: String?) {}

    /**
     * 获取路线偏好
     */
    fun getConfigKeyPlanPref(): String {
        return "0"
    }

    /**
     * 导航播报模式  播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
     * @param intValue
     */
    fun setConfigKeyBroadcastMode(intValue: Int) {}

    /**
     * 获取导航播报模式 播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
     */
    fun getConfigKeyBroadcastMode(): Int {
        return SettingConst.BROADCAST_DETAIL
    }

    /**
     * 导航静音模式  静音 1.静音 0.非静音
     * @param intValue
     */
    fun setConfigKeyMute(intValue: Int) {}

    /**
     * 导航静音模式 静音 1.静音 0.非静音
     */
    fun getConfigKeyMute(): Int {
        return 0
    }

    /**
     * 巡航播报前方路况  0：off； 1：on
     * @param intValue
     */
    fun setConfigKeyRoadWarn(intValue: Int) {}

    /**
     * 获取巡航播报前方路况  0：off； 1：on
     */
    fun getConfigKeyRoadWarn(): Int {
        return 1
    }

    /**
     * 巡航播报电子眼播报  0：off； 1：on
     * @param intValue
     */
    fun setConfigKeySafeBroadcast(intValue: Int) {}

    /**
     * 获取巡航播报电子眼播报  0：off； 1：on
     */
    fun getConfigKeySafeBroadcast(): Int {
        return 1
    }

    /**
     * 巡航播报安全提醒  0：off； 1：on
     * @param intValue
     */
    fun setConfigKeyDriveWarn(intValue: Int) {}

    /**
     * 获取巡航播报安全提醒  0：off； 1：on
     */
    fun getConfigKeyDriveWarn(): Int {
        return 1
    }

    /**
     * 获取tmc开关  1开 0 关
     */
    fun getConfigKeyRoadEvent(): Int {
        return 1
    }

    /**
     * 保存tmc开关  1开 0 关
     */
    fun setConfigKeyRoadEvent(value: Int) {}

    /**
     * 保存地图模式
     */
    fun setConfigKeyMapviewMode(value: Int) {}

    /**
     * 获取地图模式
     */
    @MapModeType
    fun getConfigKeyMapviewMode(): Int {
        return MapModeType.VISUALMODE_2D_CAR //默认2D车头向上
    }

    /**
     * 保存收藏点 1.显示 0.隐藏
     */
    fun setConfigKeyMyFavorite(value: Int) {}

    /**
     * 获取收藏点 1.显示 0.隐藏
     */
    fun getConfigKeyMyFavorite(): Int {
        return 1
    }


    /**
     * 下载主题
     * @param currentTheme 当前主题数据
     * @param currentThemeId 当前主题id
     * @param context 上下文
     */
    fun themeDownLoadItem(currentTheme: Theme, currentThemeId: Int, context: Context?) {}

    /**
     * 继续、取消下载主题操作
     * @param themeId  主题id
     * @param opType 操作类型
     */
    fun themeDataOperate(themeId: Int, @OperationType.OperationType1 opType: Int) {}

    /**
     * 请求下载所有主题头像并且获取主题列表
     * @param dataVersion 数据版本号，mapView.getOperatorStyle().getMapAssetStyleVersion()
     */
    fun onRequestDataListCheckResult(dataVersion: String?): ArrayList<Theme>? {
        return ArrayList()
    }

    /**
     * 自动比例尺
     * 0 关闭 1开启
     */
    fun getAutoScale(): Int {
        return 1
    }

    /**
     * 初始化设置，根据用户设置显示光柱图或鹰眼图
     * 0 小地图  1 光柱图 2 极简
     */
    fun getOverviewRoads(): Int {
        return 0
    }

    /**
     * 是否设置步行最后一公里
     *   false 关闭  true 开启
     */
    fun getKeyWalk(): Boolean

    /**
     * 保存同步常去地点(家、公司) 0.关闭 1.打开
     */
    fun setConfigKeyOftenArrived(value: Int): Int {
        return 0
    }

    /**
     * 同步常去地点(家、公司) 0.关闭 1.打开
     */
    fun getConfigKeyOftenArrived(): Int {
        return 0
    }

    /**
     * 保存巡航播报开关 false.关闭 true.打开
     */
    fun setCruiseBroadcastSwitch(value: Boolean) {}

    /**
     * 同步巡航景点推荐开关 false.关闭 true.打开
     */
    fun getCruiseBroadcastSwitch(): Boolean {
        return true
    }

    /**
     * 保存巡航景点推荐开关 false.关闭 true.打开
     */
    fun setAhaScenicBroadcastSwitch(value: Boolean) {}

    /**
     * 同步巡航播报开关 false.关闭 true.打开
     */
    fun getAhaScenicBroadcastSwitch(): Boolean {
        return true
    }

    /**
     * 保存地图字体大小 1标准 2大
     */
    fun setMapFont(value: Int) {}

    /**
     * 同步地图字体大小 1标准 2大
     */
    fun getMapFont(): Int {
        return 1
    }

    /**
     * 保存自动记录行程 0打开 1关闭
     */
    fun setAutoRecord(value: Int) {}

    /**
     * 同步自动记录行程 0打开 1关闭
     */
    fun getAutoRecord(): Int {
        return 0
    }

    /**
     * 保存是否显示车标罗盘 true打开 false关闭
     */
    fun setShowCarCompass(value: Boolean) {}

    /**
     * 同步是否显示车标罗盘 true打开 false关闭
     */
    fun getShowCarCompass(): Boolean {
        return true
    }

    /**
     * 保存意图导航状态 true打开 false关闭
     */
    fun setIntentionNavigation(value: Boolean) {}

    /**
     * 同步意图导航状态 true打开 false关闭
     */
    fun getIntentionNavigation(): Boolean {
        return false
    }

    /**
     * 保存个性化车标设置 1.捷途车标；2.汽车；3.普通车标
     */
    fun setCarPersonalization(value: Int) {}

    /**
     * 同步个性化车标设置 1.捷途车标；2.汽车；3.普通车标
     */
    fun getCarPersonalization(): Int {
        return 3
    }

    /**
     * 获取车牌号
     * 引用 vdbus_extra.jar
     */
    fun getLicensePlateNumber(): String {
        return ""
    }
}