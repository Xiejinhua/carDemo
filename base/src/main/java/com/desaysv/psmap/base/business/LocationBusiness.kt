package com.desaysv.psmap.base.business

import android.content.Context
import android.location.Location
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.pos.model.ELaneAlgType
import com.autonavi.gbl.pos.model.LocDataType
import com.autonavi.gbl.pos.model.LocFuncSwitch
import com.autonavi.gbl.pos.model.LocModeType
import com.autonavi.gbl.pos.model.LocMountAngle
import com.autonavi.gbl.pos.model.LocSensorOption
import com.autonavi.gbl.pos.model.LocType
import com.autonavi.gbl.pos.model.PlatformType
import com.autonavi.gbl.pos.model.PosWorkPath
import com.autosdk.bussiness.location.LocationController
import com.autosdk.common.AutoConstant
import com.autosdk.common.LocationFuncSwitch
import com.autosdk.common.lane.LaneMockMode
import com.autosdk.common.location.ILocator
import com.autosdk.common.location.LocationBackFusion
import com.autosdk.common.location.LocationFrontFusion
import com.autosdk.common.location.LocationInstrument
import com.desaysv.psmap.base.BuildConfig
import com.desaysv.psmap.base.impl.ICarInfoProxy
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationBusiness @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationController: LocationController,
    private val engineerBusiness: EngineerBusiness,
    private val iCarInfoProxy: ICarInfoProxy,
) {
    @LaneMockMode.LaneMockMode1
    var mLaneMockMode = LaneMockMode.LANE_MOCK_DISABLE

    lateinit var mLocationInstrument: ILocator

    /**
     * 初始化定位模块
     */
    fun initPos(@LaneMockMode.LaneMockMode1 laneMockMode: Int) {
        if (BuildConfig.isSupportLaneMode)
            mLaneMockMode = laneMockMode

        var locType = when (BuildConfig.autoLocType) {
            0 -> LocType.LocTypeGNSS
            1 -> LocType.LocTypeDrBack
            2 -> LocType.LocTypeDrFront
            else -> {
                LocType.LocTypeGNSS
            }
        }

        if (mLaneMockMode == LaneMockMode.LANE_MOCK_MODE_REPLAY) {
            locType = LocType.LocTypeDrBack
        } else if (mLaneMockMode == LaneMockMode.LANE_MOCK_MODE_SIM) {
            locType = LocType.LocTypeDrFront
        }
        Timber.i("initPos mockMode=$mLaneMockMode locType=$locType")
        val posWorkPath = PosWorkPath()
        val posRootDir = AutoConstant.PATH + "/pos"
        com.autosdk.common.utils.FileUtils.createDir(posRootDir)
        val posLogDir = "$posRootDir/log/"
        com.autosdk.common.utils.FileUtils.createDir(posLogDir)
        posWorkPath.locPath = posLogDir
        val contextDir = "$posRootDir/context/"
        com.autosdk.common.utils.FileUtils.createDir(contextDir)
        posWorkPath.contextPath = contextDir
        val locModeType: LocModeType = getLocModeType(locType, mLaneMockMode)
        locationController.initLocEngine(
            context,
            posWorkPath,
            locModeType,
            null,
            true,
            engineerBusiness.engineerConfig.isBlPosLog
        )
        //先启动HMI的定位信号获取服务
        initInstrument(locType, mLaneMockMode)
        locationController.setSpeedListener { iCarInfoProxy.vehicleSpeed }
        locationController.setDisplaySpeedListener { iCarInfoProxy.displayCarSpeed }
        locationController.doStartLocate()
    }

    /**
     * 获取LocModeType，根据项目传入的定位类型进行不同定位模式实现类初始化
     *
     * @param locType
     * @return
     */
    private fun getLocModeType(@LocType.LocType1 locType: Int, mockMode: Int): LocModeType {
        val locModeType = LocModeType()
        when (locType) {
            LocType.LocTypeGNSS -> {
                locModeType.locType = LocType.LocTypeGNSS
                locModeType.funcs = LocationFuncSwitch.GNSS
                locModeType.laneAlgType = ELaneAlgType.ELaneAlgLSP
            }

            LocType.LocTypeDrFront -> {
                locModeType.locType = LocType.LocTypeDrFront
                locModeType.funcs = LocationFuncSwitch.DR_FRONT_DEFAULT
            }

            LocType.LocTypeDrBack -> {
                locModeType.locType = LocType.LocTypeDrBack
                // 信号类型，设置后端融合模式下信号组合方式，该值为枚举LocDataType的位运算组合，引擎支持的组合方式参见LocSignalCombine。（当LocType == LocTypeDrBack时为必传参数，否则默认为0）
                locModeType.signalTypes =
                    LocDataType.LocDataAcce3D or LocDataType.LocDataAirPressure or LocDataType.LocDataGnss or
                            LocDataType.LocDataECompass or LocDataType.LocDataGpgsv or LocDataType.LocDataGyro or LocDataType.LocDataPulse
                locModeType.sensorOption = getSensorOption()
                locModeType.funcs = LocationFuncSwitch.DR_BACK_DEFAULT
                locModeType.mountAngle = getMountAngle()
            }

            else -> {}
        }
        //定位模块云+端功能是否开启,一般情况下有EHP项目才需要开启
//        if (BuildConfig.isSupportEhp) {
        locModeType.funcs = locModeType.funcs or LocFuncSwitch.LocFuncEHPEnable
//        }
        locModeType.platformType = PlatformType.PlatformAuto
        if (mockMode != LaneMockMode.LANE_MOCK_DISABLE) {
            locModeType.laneAlgType = ELaneAlgType.ELaneAlgHSP
        }
        locModeType.logConf.fileLimit = 20 //设置定位日志大小750SDK
        return locModeType
    }

    private fun initInstrument(@LocType.LocType1 locType: Int, mockMode: Int) {
        mLocationInstrument = when (locType) {
            LocType.LocTypeGNSS -> LocationInstrument(context)

            LocType.LocTypeDrFront -> if (mockMode == LaneMockMode.LANE_MOCK_MODE_SIM) {
                LocationInstrument(context)
            } else {
                LocationFrontFusion(context)
            }

            LocType.LocTypeDrBack ->
                LocationBackFusion(context)

            else -> LocationInstrument(context)
        }
        mLocationInstrument.doStartLocate()
    }

    /**
     * @param pause 是否暂停输入GPS给高德，定位回放会用到
     */
    fun pauseGPSInput(pause: Boolean) {
        mLocationInstrument.setPosMockMode(pause)
    }


    private fun getSensorOption(): LocSensorOption {
        val sensorOption = LocSensorOption()
        sensorOption.hasAcc = 3 // 加速度计轴数 {0|1|3}， 0 表示没有 后端融合项目可选
        sensorOption.hasGyro = 3 // 陀螺仪轴数 {0|1|3} ， 0 表示没有 后端融合项目必须有
        sensorOption.hasTemp = 0 // 有无陀螺温度传感器  0无 1有 后端融合项目必须有
        sensorOption.hasPressure = 0 // 有无气压计  0无 1有， 一般可不配置
        sensorOption.hasMag = 0 // 有无磁力计  0无 1有， 一般可不配置
        sensorOption.hasW4m = 0 // 有无四轮速传感器  0无 1有 未使用，可不配置
        sensorOption.hasGsv = 1 // 有无GSV信息（星历信息）， 0无 1有 TODO :根据项目情况配置，推荐1hz，后端融合项目必须有
        sensorOption.pulseFreq = 10 // 车速信息输入频率，单位 Hz TODO :根据项目情况配置，推荐10hz，后端融合项目必须有
        sensorOption.gyroFreq = 10 // 陀螺仪信息输入频率，单位 Hz TODO :根据项目情况配置，推荐10hz，后端融合项目必须有
        sensorOption.gpsFreq = 1 // GNSS信息输入频率，单位 Hz TODO :根据项目情况配置，推荐1hz，后端融合项目必须有
        sensorOption.accFreq = 10 // 加速度计信息输入频率，单位 Hz TODO :根据项目情况配置，推荐10hz，后端融合项目可选
        sensorOption.w4mFreq = 10 //四轮速信息输信息入频率，单位 Hz 未使用，可不配置
        return sensorOption
    }

    private fun getMountAngle(): LocMountAngle {
        val locMountAngle = LocMountAngle()
        locMountAngle.isValid = true // TODO： 安装角是否可用，需根据项目情况正确配置
        locMountAngle.yaw = -90.0 // TODO： 安装角yaw值，需根据项目情况正确配置
        locMountAngle.roll = 0.0 // TODO： 安装角roll值，需根据项目情况正确配置
        locMountAngle.pitch = 121.205 // TODO： 安装角pitch值，需根据项目情况正确配置
        return locMountAngle
    }

    fun getLastLocation(): Location = locationController.lastLocation

    /**
     * 下电/STR等的保存定位
     */
    fun saveCurPos() = locationController.saveCurPos()

    /**
     * 返回车速和保存定位定时器是否运行中
     */
    fun timerIsRunning(): Boolean {
        return locationController.timerIsRunning()
    }

    fun doStartTimerWithAccOn() {
        Timber.i("doStartTimerWithAccOn")
        locationController.doStartTimer()
        mLocationInstrument.doTimerStart()
    }

    fun doStopTimerWithAccOff() {
        Timber.i("doStopTimerWithAccOff")
        locationController.doStopTimer()
        mLocationInstrument.doTimerStop()
    }

    /**
     * WGS84坐标加密成GCJ02坐标
     * @param wgs84Pos
     * @return
     */
    fun encryptLonLat(wgs84Pos: Coord3DDouble?): Coord3DDouble? {
        return locationController.encryptLonLat(wgs84Pos)
    }
}