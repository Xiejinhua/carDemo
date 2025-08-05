package com.desaysv.psmap.base.impl

import androidx.lifecycle.LiveData

/**
 * Author : wangmansheng
 * Date : 2024-1-12
 * Description : 车辆信息管理接口
 */
interface ICarInfoProxy {
    fun init()
    fun unInit()

    /**
     * 车辆日夜模式
     *
     * @return 车辆日夜模式
     */
    @Deprecated(
        "This function is deprecated, please follow Android native day and night mode",
        ReplaceWith("Activity.onConfigurationChanged() android.content.res.Configuration.uiMode")
    )
    val dayNightMode: Int

    /**
     * 车辆日夜模式是否是黑夜模式
     *
     * @return 是否是黑夜模式
     */
    @Deprecated(
        "This function is deprecated, please follow Android native day and night mode",
        ReplaceWith("Activity.onConfigurationChanged() android.content.res.Configuration.uiMode")
    )
    val isNightMode: Boolean

    /**
     * 获取档位
     *
     * @return 档位
     */
    val gearPosition: Int

    /**
     * 电源状态
     */
    val powerState: Int

    /**
     * 车速 km/h
     *
     * @return 车速 km/h
     */
    val vehicleSpeed: Float

    /**
     * 显示车速 km/h
     *
     * @return 显示车速 km/h
     */
    val displayCarSpeed: Int

    /**
     * 获取VIN号
     *
     * @return VIN号
     */
    val vinCode: String?

    /**
     * 获取SN码
     *
     * @return SN码
     */
    val sNCode: String

    /**
     * 获取uuid码
     *
     * @return uuid码
     */
    val uuid: String?

    /**
     * 获取Sensor温度
     *
     * @return -45~80℃
     */
    val sensorTemperature: Double

    /**
     * 获取车型
     */
    val carModel: String?

    /**
     * 车重 kg
     */
    val vehicleWeight: Int

    /**
     * 电池最大量量 kwh
     */
    val maxBatteryEnergy: Double

    /**
     * 当前电量 kwh
     */
    val currentBatteryEnergy: Double

    /**
     * 当前电量百分比 0~100
     */
    val currentBatteryEnergyPercent: Double

    /**
     * 续航里程 km
     *
     * @return 当前续航里程 km
     */
    val rangeDist: Int

    /**
     * 灯光是否打开
     * 只判断大灯（近光灯 远光灯）
     *
     * @return true 打开 false 关闭
     */
    val lightStatus: Boolean

    /**
     * 驾驶模式
     *
     * @return
     */
    val driverMode: Int

    /**
     * 能量单位
     */
    val energyUnit: Int

    /**
     * 动力类型
     */
    val powerType: Int

    /**
     * 电动车类型
     */
    val electricVehicleType: Int

    /**
     * 低油量提醒
     */
    val lowFuelWarning: Boolean

    /**
     * 仪表投屏显示状态
     */
    val carDashboardStatus: CarDashboardStatus

    /**
     * 零件号
     */
    val partNum : String

    /**
     * 根据车型获取车速代价模型
     * @param model 车型
     * @return 车速代价模型
     */
    fun getSpeedCostList(model: String?): DoubleArray

    /**
     * 注册车身相关状态变化通知
     *
     * @param accStateCallback
     */
    fun registerVehicleInfoCallback(callback: VehicleInfoCallback)

    /**
     * 反注册注册车身相关状态变化通知
     *
     * @param accStateCallback
     */
    fun unregisterVehicleInfoCallback(callback: VehicleInfoCallback)

    /**
     * 获取分屏状态
     */
    fun getScreenStatus(): LiveData<Boolean>

    /**
     * 设置分屏状态
     */
    fun setScreenStatus(isScreenStatus: Boolean)

    /**
     * 获取launcher/应用状态
     * true launcher状态， false 应用状态
     */
    fun getLauncherStatus(): LiveData<Boolean>

    /**
     * 发送消息给仪表
     */
    fun <T> sendMessageToDashboard(data: T)

    /**
     * 获取互联导航状态
     * VDValueNavi.NaviStatus.START 开始导航， VDValueNavi.NaviStatus.EXIT结束导航
     */
    fun getPhoneLinkNaviStatus(): Int

    /**
     * 设置本地导航状态
     * true. VDValueNavi.NaviStatus.START 开始导航， false. VDValueNavi.NaviStatus.EXIT结束导航
     */
    fun publishNaviStatus(isStart: Boolean)

    /**
     * 通知结束本地导航
     */
    fun getPhoneLinkNaviStopLocalNavi(): LiveData<Boolean>

    /**
     * 通知系统车牌号变化
     */
    fun getLicensePlateChange(): LiveData<Boolean>


    /**
     * 是否是高阶项目
     */
    fun isJetOurGaoJie(): Boolean

    /**
     * 获取音量
     */
    fun getVolume(type: Int): Int

    /**
     * 设置音量大小
     */
    fun setVolume(type: Int, volume: Int)

    /**
     * 获取仪表主题
     */
    fun getDashboardTheme(): Int

    /**
     * 设置仪表主题
     */
    fun setDashboardTheme(theme: Int)

    /**
     * 是否是T1J FL2 国内燃油
     */
    fun isT1JFL2ICE(): Boolean

    /**
     * 是否是T1J FL2 国内PHEV
     */
    fun isT1JFL2PHEV(): Boolean

}

enum class CarDashboardStatus {
    CLOSE,
    MIDDLE_FIRST_THEME,
    LEFT_FIRST_THEME,
    SECOND_THEME,
    THIRD_THEME,
    RIGHT_MINI_MAP_THEME,
    FULL_MAP_THEME,
}