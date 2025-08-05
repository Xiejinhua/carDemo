package com.desaysv.psmap.model.car

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autosdk.bussiness.common.utils.ElectricInfoConverter
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.ivi.vdb.event.id.navi.VDValueNavi
import com.desaysv.psmap.base.BuildConfig
import com.desaysv.psmap.base.business.EngineerBusiness
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.CarDashboardStatus
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.VehicleInfoCallback
import com.desaysv.psmap.base.utils.BaseConstant
import timber.log.Timber
import javax.inject.Inject

/**
 * Author : wangmansheng
 * Date : 2024-1-12
 * Description : 模拟器车辆信息
 */
class SimulationCarInfoManager @Inject constructor(
    private val engineerBusiness: EngineerBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory
) : ICarInfoProxy {
    private val sharePreference =
        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.normal)
    private val screenStatus = MutableLiveData<Boolean>(true)
    private val launcherStatus = MutableLiveData(true)

    //通知结束本地导航
    private val phoneLinkNaviStopLocalNavi = MutableLiveData<Boolean>()

    private val licensePlateChange = MutableLiveData<Boolean>() //系统车牌号变化
    override fun init() {
        Timber.d("init success!!!")
    }

    override fun unInit() {}
    override val dayNightMode: Int
        get() = 0
    override val isNightMode: Boolean
        get() = false
    override val gearPosition: Int
        get() = 0
    override val powerState: Int
        get() = 0
    override val vehicleSpeed: Float
        get() = 0f
    override val displayCarSpeed: Int get() = 0
    override val vinCode: String
        get() = "SIM_VIN_ID" + (Math.random() * 9 + 1).toInt() * 100000
    override val sNCode: String
        get() = getVehicleSN()
    override val partNum: String
        get() = "SIM_PART_NUM" + (Math.random() * 9 + 1).toInt() * 100000
    override val uuid: String?
        get() {
            val openMapTestUuid = engineerBusiness.engineerConfig.openMapTestUuid //是否开启测试UUID
            if (openMapTestUuid) {
                val mapTestUuid = engineerBusiness.engineerConfig.mapTestUuid
                Timber.d("SimulationCarInfoManager mapTestUuid is %s", mapTestUuid)
                BaseConstant.UUID_EMPTY = TextUtils.isEmpty(mapTestUuid)
                return mapTestUuid
            }
            return BuildConfig.emulatorUuid
        }
    override val carModel: String
        get() = "U21A-0Z0"

    override val vehicleWeight: Int
        get() {
            return when (carModel) {
                "U21A-0Z0", "U21A-0ZA" -> 2656
                "U21A-0Z2" -> 2674
                "U21A-0Z4", "U21A-0Z6" -> 2695
                "U11A-0Z2", "U21A-0ZL", "U11A-0ZM" -> 2619
                "U11A-0Z5", "U11A-0ZK" -> 2641
                "U11A-0Z7", "U11A-0Z8" -> 2663
                else -> 2656
            }
        }
    override val maxBatteryEnergy: Double
        get() {
            return when (carModel) {
                "U21A-0Z0", "U21A-0Z2", "U21A-0Z4" -> 92.0
                "U21A-0ZA", "U21A-0Z6", "U21A-0ZL", "U11A-0ZM" -> 95.0
                "U11A-0Z2", "U11A-0Z5", "U11A-0Z7", "U11A-0ZK", "U11A-0Z8" -> 114.0
                else -> 92.0
            }
        }
    override val currentBatteryEnergy: Double
        get() = 18.15528 //测试 10kwh
    override val currentBatteryEnergyPercent: Double
        get() = currentBatteryEnergy / maxBatteryEnergy * 100.0
    override val rangeDist: Int
        get() = 100 //测试 离线续航100km
    override val sensorTemperature: Double
        get() = 0.0
    override val lightStatus: Boolean
        get() = false
    override val driverMode: Int
        get() = 0

    override val energyUnit: Int get() = ElectricInfoConverter.EGEnergyUnit.E_ENERGY_UNIT_KWH

    @get:ElectricInfoConverter.PowerType
    override val powerType: Int
        /**
         * 动力类型
         *
         * @return 电动车
         */
        get() = ElectricInfoConverter.PowerType.E_VEHICLE_ENERGY_ELECTRIC

    @get:ElectricInfoConverter.ElectricVehicleType
    override val electricVehicleType: Int
        /**
         * 电动车类型
         *
         * @return 纯电动客车
         */
        get() = ElectricInfoConverter.ElectricVehicleType.E_ELECTRIC_BUS

    override val lowFuelWarning: Boolean
        get() = false

    override fun getSpeedCostList(model: String?): DoubleArray = doubleArrayOf(
        5.0,
        103.1,
        20.0,
        75.0,
        45.0,
        98.1,
        80.0,
        164.3,
        120.0,
        283.0,
        150.0,
        407.5,
        200.0,
        675.7
    )

    override val carDashboardStatus: CarDashboardStatus get() = CarDashboardStatus.CLOSE

    override fun registerVehicleInfoCallback(callback: VehicleInfoCallback) {

    }

    override fun unregisterVehicleInfoCallback(callback: VehicleInfoCallback) {

    }


    /**
     * 获取分屏状态
     */
    override fun getScreenStatus(): LiveData<Boolean> {
        return screenStatus
    }

    /**
     * 设置分屏状态
     */
    override fun setScreenStatus(isScreenStatus: Boolean) {
        screenStatus.postValue(isScreenStatus)
    }

    override fun getLauncherStatus(): LiveData<Boolean> {
        return launcherStatus
    }

    override fun <T> sendMessageToDashboard(data: T) {

    }

    override fun getPhoneLinkNaviStatus(): Int {
        return VDValueNavi.NaviStatus.EXIT
    }

    override fun publishNaviStatus(isStart: Boolean) {

    }

    override fun getPhoneLinkNaviStopLocalNavi(): LiveData<Boolean> {
        return phoneLinkNaviStopLocalNavi
    }

    override fun getLicensePlateChange(): LiveData<Boolean> {
        return licensePlateChange
    }

    //手动添加VehicleSN
    @SuppressLint("SuspiciousIndentation")
    private fun getVehicleSN(): String {
        var deviceId = ""
        val vehicleSN = sharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.vehicleSN, "")
        if (TextUtils.isEmpty(vehicleSN)) {
            deviceId = "CG1000002" + ((Math.random() * 9 + 1).toInt() * 100000)
            sharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.vehicleSN, deviceId)
        } else {
            deviceId = vehicleSN
        }
        Timber.d(" ---------> getVehicleSN: VEHICLE_SN: $deviceId")
        return deviceId
    }

    override fun isJetOurGaoJie(): Boolean {
        return false
    }

    override fun getVolume(type: Int): Int {
        return 50
    }

    override fun setVolume(type: Int, volume: Int) {

    }

    override fun getDashboardTheme(): Int {
        return 0
    }

    override fun setDashboardTheme(theme: Int) {

    }

    override fun isT1JFL2ICE(): Boolean {
        return false
    }

    override fun isT1JFL2PHEV(): Boolean {
        return true
    }
}