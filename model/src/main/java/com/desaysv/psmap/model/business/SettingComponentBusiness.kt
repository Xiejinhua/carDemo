package com.desaysv.psmap.model.business

import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.autonavi.gbl.data.model.Theme
import com.autonavi.gbl.user.behavior.model.ConfigKey
import com.autosdk.bussiness.widget.setting.SettingComponent
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.AutoConstant
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoProxy
import com.desaysv.ivi.vdb.event.id.carinfo.VDEventCarInfo
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.R
import com.google.gson.JsonObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 导航设置相关业务
 * 比如路线偏好
 */
@Singleton
class SettingComponentBusiness @Inject constructor(
    private val settingComponent: SettingComponent,
    private val application: Application,
    private val sharePreferenceFactory: SharePreferenceFactory
) : ISettingComponent {
    private var dayNightCheck = false
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)

    override fun setConfigKeyDayNightMode(intValue: Int) {
//        if (dayNightCheck) {
//            settingComponent.configKeyDayNightMode = intValue
//            if (settingAccountBusiness.isLogin()) {
//                mapSharePreference.putIntValue(BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.userDayNight.toString(), intValue)
//            } else {
//                mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.dayNightMode, intValue)
//            }
//        }
    }

    override fun getConfigKeyDayNightMode(): Int {
//        var mCurrentMode = SettingConst.MODE_DEFAULT
//        dayNightCheck = mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.openDayNight, true)
//        if (dayNightCheck) {
//            val dayNightMode = if (settingAccountBusiness.isLogin()) {
//                mapSharePreference.getIntValue(
//                    BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.userDayNight.toString(),
//                    SettingConst.MODE_DEFAULT
//                )
//            } else {
//                mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.dayNightMode, SettingConst.MODE_DEFAULT)
//            }
//            val configKeyDayNightMode = settingComponent.configKeyDayNightMode
//            mCurrentMode = if (configKeyDayNightMode == SettingConst.MODE_DEFAULT) {
//                Timber.d(" configKeyDayNightMode == SettingConst.MODE_DEFAULT")
//                dayNightMode
//            } else {
//                configKeyDayNightMode
//            }
//            Timber.d(" getConfigKeyDayNightMode dayNightType:%s", mCurrentMode)
//        }
//        return mCurrentMode
        return if (com.autosdk.BuildConfig.dayNightBySystemUI) SettingConst.MODE_DEFAULT else SettingConst.MODE_DAY
    }

    override fun setCarLogos(intValue: Int) {
        settingComponent.carLogos = intValue
    }

    override fun getCarLogos(): Int {
        return settingComponent.carLogos
    }

    override fun setConfigKeyPlateNumber(carNumber: String?) {
        settingComponent.configKeyPlateNumber = carNumber
        mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.vehicleNum, carNumber)
    }

    override fun getBaseConfig(): JsonObject {
        val settingJsonObject = JsonObject()
        settingJsonObject.addProperty(ConfigKey.ConfigKeyPlateNumber.toString(), getConfigKeyPlateNumber())
        settingJsonObject.addProperty(ConfigKey.ConfigKeyAvoidLimit.toString(), getConfigKeyAvoidLimit())
        settingJsonObject.addProperty(ConfigKey.ConfigKeyPowerType.toString(), getConfigKeyPowerType())
        return settingJsonObject
    }

    override fun getConfigKeyPlateNumber(): String? {
        val configKeyPlateNumber = settingComponent.configKeyPlateNumber
        return if (TextUtils.isEmpty(configKeyPlateNumber)) mapSharePreference.getStringValue(
            MapSharePreference.SharePreferenceKeyEnum.vehicleNum,
            ""
        ) else configKeyPlateNumber
    }

    override fun setConfigKeyAvoidLimit(intValue: Int) {
        settingComponent.configKeyAvoidLimit = intValue
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.vehicleLimit, intValue)
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.AvoidLimitSw, intValue)
            )
        )
    }

    override fun getConfigKeyAvoidLimit(): Boolean {
        val plateNumber = getLicensePlateNumber()
        val vehicleLimit = settingComponent.configKeyAvoidLimit
        val defaultLimit = mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.vehicleLimit, 0)
        val avoidLimit = if (vehicleLimit != 0) vehicleLimit else defaultLimit
        return !TextUtils.isEmpty(plateNumber) && avoidLimit != 0
    }

    override fun setConfigKeyPowerType(intValue: Int) {
        settingComponent.configKeyPowerType = intValue
    }

    override fun getConfigKeyPowerType(): Int {
        return settingComponent.configKeyPowerType
    }

    override fun setConfigKeyPlanPref(planPrefString: String?) {
        settingComponent.configKeyPlanPref = planPrefString
    }

    override fun getConfigKeyPlanPref(): String {
        val routePreference = try {
            settingComponent.configKeyPlanPref
        } catch (e: NumberFormatException) {
            "0"
        }
        return routePreference
    }

    override fun setConfigKeyBroadcastMode(intValue: Int) {
        settingComponent.configKeyBroadcastMode = intValue
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.volumeModel, intValue)
    }

    override fun getConfigKeyBroadcastMode(): Int {
        var volumeModelInt = settingComponent.configKeyBroadcastMode
        if (volumeModelInt > 3) {
            volumeModelInt = mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.volumeModel, SettingConst.BROADCAST_DETAIL)
            if (volumeModelInt > 3) {
                volumeModelInt = SettingConst.BROADCAST_DETAIL
            }
        }
        return volumeModelInt
    }

    override fun setConfigKeyMute(intValue: Int) {
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.volumeMute, intValue)
    }

    override fun getConfigKeyMute(): Int {
        var volumeMuteInt = mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.volumeMute, 0)
        if (volumeMuteInt > 1) {
            volumeMuteInt = 0
        }
        return volumeMuteInt
    }

    override fun setConfigKeyRoadWarn(intValue: Int) {
        settingComponent.configKeyRoadWarn = intValue
    }

    override fun getConfigKeyRoadWarn(): Int {
        return settingComponent.configKeyRoadWarn
    }

    override fun setConfigKeySafeBroadcast(intValue: Int) {
        settingComponent.configKeySafeBroadcast = intValue //巡航播报电子眼播报  0：off； 1：on
    }

    override fun getConfigKeySafeBroadcast(): Int {
        return settingComponent.configKeySafeBroadcast
    }

    override fun setConfigKeyDriveWarn(intValue: Int) {
        settingComponent.configKeyDriveWarn = intValue //巡航播报安全提醒  0：off； 1：on
    }

    override fun getConfigKeyDriveWarn(): Int {
        return settingComponent.configKeyDriveWarn
    }

    override fun getConfigKeyRoadEvent(): Int {
        return settingComponent.configKeyRoadEvent
    }

    override fun setConfigKeyRoadEvent(value: Int) {
        settingComponent.configKeyRoadEvent = value
    }

    override fun setConfigKeyMapviewMode(value: Int) {
        //settingComponent.configKeyMapviewMode = value
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.mapViewMode, value)
    }

    override fun getConfigKeyMapviewMode(): Int {
        var viewModelInt = -1;//settingComponent.configKeyMapviewMode
        if (viewModelInt > 2 || viewModelInt == -1) {
            viewModelInt = mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.mapViewMode, MapModeType.VISUALMODE_3D_CAR)
            if (viewModelInt > 2 || viewModelInt == -1) {
                viewModelInt = MapModeType.VISUALMODE_3D_CAR
            }
        }
        return viewModelInt
    }

    override fun setConfigKeyMyFavorite(value: Int) {
        settingComponent.configKeyMyFavorite = value
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.mapFavoritePoint, value)
    }

    override fun getConfigKeyMyFavorite(): Int {
        val configKeyMyFavorite = settingComponent.configKeyMyFavorite
        val favoritePoint = mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.mapFavoritePoint, 1)
        Timber.i("getConfigKeyMyFavorite configKeyMyFavorite:$configKeyMyFavorite favoritePoint:$favoritePoint")
        return if (configKeyMyFavorite < 0 || configKeyMyFavorite > 1) favoritePoint else configKeyMyFavorite
    }

    override fun themeDownLoadItem(currentTheme: Theme, currentThemeId: Int, context: Context?) {
        settingComponent.themeDownLoadItem(currentTheme, currentThemeId, context)
    }

    override fun themeDataOperate(themeId: Int, opType: Int) {
        settingComponent.themeDataOperate(themeId, opType)
    }

    override fun onRequestDataListCheckResult(dataVersion: String?): ArrayList<Theme>? {
        return settingComponent.onRequestDataListCheckResult(dataVersion)
    }

    /**
     * 自动比例尺
     * 0 关闭 1开启
     */
    override fun getAutoScale(): Int {
        return mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.mapAutoScale, 1)
    }

    /**
     * 初始化设置，根据用户设置显示光柱图或鹰眼图
     * 0 小地图  1 光柱图 2 极简
     */
    override fun getOverviewRoads(): Int {
        return mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.overviewRoads, 2)
    }

    /**
     * 是否设置步行最后一公里
     *   false 关闭  true 开启
     */
    override fun getKeyWalk(): Boolean {
        return mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.walkSwitch, true)
    }

    /**
     * 保存同步常去地点(家、公司) 0.关闭 1.打开
     */
    override fun setConfigKeyOftenArrived(value: Int): Int {
        return settingComponent.setConfigKeyOftenArrived(value)
    }

    /**
     * 同步常去地点(家、公司) 0.关闭 1.打开
     */
    override fun getConfigKeyOftenArrived(): Int {
        return settingComponent.configKeyOftenArrived
    }

    /**
     * 保存巡航播报开关 false.关闭 true.打开
     */
    override fun setCruiseBroadcastSwitch(value: Boolean) {
        mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.cruiseBroadcastSwitch, value)
    }

    /**
     * 同步巡航播报开关 false.关闭 true.打开
     */
    override fun getCruiseBroadcastSwitch(): Boolean {
        return mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.cruiseBroadcastSwitch, true)
    }

    /**
     * 保存巡航播报开关 false.关闭 true.打开
     */
    override fun setAhaScenicBroadcastSwitch(value: Boolean) {
        mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.ahaScenicBroadcastSwitch, value)
    }

    /**
     * 同步巡航景点推荐开关 false.关闭 true.打开
     */
    override fun getAhaScenicBroadcastSwitch(): Boolean {
        return mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.ahaScenicBroadcastSwitch, true)
    }

    /**
     * 保存地图字体大小 1标准 2大
     */
    override fun setMapFont(value: Int) {
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.mapFont, value)
    }

    /**
     * 同步地图字体大小 1标准 2大
     */
    override fun getMapFont(): Int {
        return mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.mapFont, 1)
    }

    /**
     * 保存自动记录行程 0打开 1关闭
     */
    override fun setAutoRecord(value: Int) {
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.autoRecord, value)
    }

    /**
     * 同步自动记录行程 0打开 1关闭
     */
    override fun getAutoRecord(): Int {
        return mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.autoRecord, 0)
    }

    /**
     * 保存是否显示车标罗盘 true打开 false关闭
     */
    override fun setShowCarCompass(value: Boolean) {
        mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.showCarCompass, value)
    }

    /**
     * 同步是否显示车标罗盘 true打开 false关闭
     */
    override fun getShowCarCompass(): Boolean {
        return mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.showCarCompass, true)
    }

    /**
     * 保存意图导航状态 true打开 false关闭
     */
    override fun setIntentionNavigation(value: Boolean) {
        mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.intentionNavigation, value)
    }

    /**
     * 同步意图导航状态 true打开 false关闭
     */
    override fun getIntentionNavigation(): Boolean {
        return mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.intentionNavigation, false)
    }

    /**
     * 保存个性化车标设置 1.捷途车标；2.汽车；3.普通车标
     */
    override fun setCarPersonalization(value: Int) {
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.personalizationCar, value)
    }

    /**
     * 同步个性化车标设置 1.捷途车标；2.汽车；3.普通车标
     */
    override fun getCarPersonalization(): Int {
        return mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.personalizationCar, 3)
    }

    /**
     * 获取车牌号
     * 引用 vdbus_extra.jar
     */
    override fun getLicensePlateNumber(): String {
        if (CommonUtils.isVehicle()) {
            val data = CarInfoProxy.getInstance().getItemValues(VDEventCarInfo.MODULE_AVM, 12)
            Timber.i("getLicensePlateNumber: data = ${data.contentToString()}")
            if (data != null && data.size == 8) {
                val isApaEnabled = CarConfigUtil.getDefault().getConfig(com.desaysv.ivi.extra.project.carconfig.Constants.ID_CAR_CONFIG_APA) == 1
                Timber.i("getLicensePlateNumber: apa = $isApaEnabled")
                if (isApaEnabled) {
                    val number = changeApaLPNFromMCU(application, data)
                    val plateNumber = if (TextUtils.equals(number, "00000000") || TextUtils.equals(number, "京A000000")) "" else number
                    AutoConstant.vehicleId = plateNumber
                    return plateNumber
                } else {
                    val allNumbers: Array<String> = application.resources.getStringArray(R.array.plate_number)
                    val builder = StringBuilder()

                    for (i in data.indices) {
                        builder.append(allNumbers[data[i]])
                    }

                    Timber.i("getLicensePlateNumber: plateNumber = $builder")
                    val plateNumber = if (TextUtils.equals(builder.toString(), "00000000") || TextUtils.equals(
                            builder.toString(),
                            "京A000000"
                        )
                    ) "" else builder.toString()
                    AutoConstant.vehicleId = plateNumber
                    return plateNumber
                }
            } else {
                AutoConstant.vehicleId = ""
                return ""
            }
        } else {
            AutoConstant.vehicleId = getConfigKeyPlateNumber() ?: ""
            return getConfigKeyPlateNumber() ?: ""
        }
    }

    private fun changeApaLPNFromMCU(mContext: Context, data: IntArray): String {
        //拿到第一位车牌号的映射表
        val lpnInfo1s: Array<String> = mContext.resources.getStringArray(R.array.apa_lpn_info1)
        //拿到第二位车牌号的映射表
        val lpnInfo2s: Array<String> = mContext.resources.getStringArray(R.array.apa_lpn_info2)
        //拿到第三到八位车牌号的映射表
        val lpnInfo3s: Array<String> = mContext.resources.getStringArray(R.array.apa_lpn_info3)

        val builder = java.lang.StringBuilder()
        //根据MCU保存的key获取指定的字符
        var i = 0
        val len = 8
        while (i < len) {
            var value = ""
            if (i == 0) { //第一位车牌号
                /**
                 * 数组下标值越界，直接返回空
                 * 出现场景：apa设置为0后设置车牌号，此时使用了内置AVM的协议，该协议的值范围为0 到 80多，之后
                 * 配置apa为1，数据还是之前的数据，但是apa的协议值偏小，直接拿数据当做数组下标就会出现下标越界问题
                 */
                if (data[0] > lpnInfo1s.size - 1) {
                    return ""
                }
                //存到MCU的值是数组下标值，也是协议的下标值
                value = lpnInfo1s[data[0]]
            } else if (i == 1) { //第二位车牌号
                /**
                 * 数组下标值越界，直接返回空
                 * 出现场景：apa设置为0后设置车牌号，此时使用了内置AVM的协议，该协议的值范围为0 到 80多，之后
                 * 配置apa为1，数据还是之前的数据，但是apa的协议值偏小，直接拿数据当做数组下标就会出现下标越界问题
                 */
                if (data[1] > lpnInfo2s.size - 1) {
                    return ""
                }
                value = lpnInfo2s[data[1]]
            } else if (i == len - 1) {  //最后一位车牌号
                //最后一位车牌号为无效值，代表是油车车牌号
                value = if (data[i] > lpnInfo3s.size - 1) "" else lpnInfo3s[data[i]]
            } else { //第三位及之后车牌号
                /**
                 * 数组下标值越界，直接返回空
                 * 出现场景：apa设置为0后设置车牌号，此时使用了内置AVM的协议，该协议的值范围为0 到 80多，之后
                 * 配置apa为1，数据还是之前的数据，但是apa的协议值偏小，直接拿数据当做数组下标就会出现下标越界问题
                 */
                if (data[i] > lpnInfo3s.size - 1) {
                    return ""
                }
                value = lpnInfo3s[data[i]]
            }
            builder.append(value)
            i++
        }
        Timber.i("changeApaLPNFromMCU: LPN = $builder")
        return builder.toString()
    }
}