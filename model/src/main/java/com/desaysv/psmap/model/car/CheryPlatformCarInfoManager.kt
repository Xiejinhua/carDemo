package com.desaysv.psmap.model.car

import android.app.Application
import android.car.Car
import android.car.media.CarAudioManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autosdk.bussiness.common.utils.ElectricInfoConverter
import com.desaysv.ivi.extra.project.carconfig.CarConfigUtil
import com.desaysv.ivi.extra.project.carconfig.Constants
import com.desaysv.ivi.extra.project.carinfo.NewEnergyID
import com.desaysv.ivi.extra.project.carinfo.ReadOnlyID
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoHelper
import com.desaysv.ivi.extra.project.carinfo.proxy.CarInfoProxy
import com.desaysv.ivi.extra.project.carinfo.proxy.Constants.SpiMsgType.MSG_ON_CHANGE_EVENT
import com.desaysv.ivi.vdb.client.VDBus
import com.desaysv.ivi.vdb.client.bind.VDServiceDef
import com.desaysv.ivi.vdb.client.listener.VDBindListener
import com.desaysv.ivi.vdb.client.listener.VDNotifyListener
import com.desaysv.ivi.vdb.event.VDEvent
import com.desaysv.ivi.vdb.event.base.VDKey
import com.desaysv.ivi.vdb.event.id.cabin.VDEventCabinLan
import com.desaysv.ivi.vdb.event.id.cabin.bean.VDCLCommonMessage
import com.desaysv.ivi.vdb.event.id.carinfo.VDEventCarInfo
import com.desaysv.ivi.vdb.event.id.carlan.VDValueCarLan
import com.desaysv.ivi.vdb.event.id.carlan.bean.VDNaviPerspective
import com.desaysv.ivi.vdb.event.id.carstate.VDEventCarState
import com.desaysv.ivi.vdb.event.id.carstate.VDValueCarState
import com.desaysv.ivi.vdb.event.id.navi.VDEventNavi
import com.desaysv.ivi.vdb.event.id.navi.VDValueNavi
import com.desaysv.ivi.vdb.event.id.vehicle.VDEventVehicleHal
import com.desaysv.ivi.vdb.event.id.vehicle.VDKeyVehicleHal
import com.desaysv.psmap.base.BuildConfig
import com.desaysv.psmap.base.business.EngineerBusiness
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.CarDashboardStatus
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.VehicleInfoCallback
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.model.car.dashboard.VDNaviDisplayAreaT1N
import com.google.gson.Gson
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 车辆数据管理
 */
class CheryPlatformCarInfoManager @Inject constructor(
    private val application: Application,
    private val engineerBusiness: EngineerBusiness
) : ICarInfoProxy {
    private val gson = Gson()

    private var isInit = false

    //true 为2/3屛， false 为全屏
    private val screenStatus = MutableLiveData(false)

    //是否可缩放  0 可缩放， 1不可缩放
    private val scalableStatus = MutableLiveData(false)

    //通知结束本地导航
    private val phoneLinkNaviStopLocalNavi = MutableLiveData<Boolean>()

    private val licensePlateChange = MutableLiveData<Boolean>() //系统车牌号变化

    /**
     * 车辆日夜模式
     * @return 车辆日夜模式
     */
    @Deprecated(
        "This function is deprecated, please follow Android native day and night mode",
        ReplaceWith("Activity.onConfigurationChanged() android.content.res.Configuration.uiMode")
    )
    override var dayNightMode = 0 //当前日夜模式
    var mGearPosition = 0 //档位位置变化
    var mPowerState = 0 //电源状态
    var mVehicleSpeed = 0f //车速 (km/h)
    var mDisplayVehicleSpeed = 0 //显示车速 (km/h)
    var mVinCode: String? = null //vin 车架号
    var mSNCode: String? = null //SN码
    var mPartNum: String? = null //零件号
    var mCarModel: String? = null //车型
    var mDippedHeadlightOpen: Boolean? = null //近光灯
    var mHeadlightOpen: Boolean? = null //远光灯
    var vehicleInfoCallbacks: MutableSet<VehicleInfoCallback> = HashSet()
    private var mTemperature = 0.0 //陀螺仪温度

    private var mRangeDistance = 0 //续航里程

    private var mLowFuelWarning = false //低油量提醒

    private var mDashboardStatus: CarDashboardStatus = CarDashboardStatus.CLOSE

    private val KEY_SCREEN_STATE = "launcher.map.model"

    private val KEY_FACTORY_RESET = "com.desaysv.setting.reset"

    private val KEY_SCALABLE_STATE = "launcher.map.scalable"

    private var mCarAudioManager: CarAudioManager? = null

    private var mDashboardTheme = -1

    val CARINFO_CMDID_DASHBOARD_SPEED = 165

    /**
     * 车身信息相关 监听
     */
    private fun registerVehicleCallback() {
        Timber.d("registerVehicleCallback")
        CarConfigUtil.getDefault().init(application.applicationContext)
        CarInfoProxy.getInstance().init(application.applicationContext)
        VDBus.getDefault().init(application.applicationContext)
        CarInfoProxy.getInstance().regServiceConnectListener { state ->
            Timber.i("init CarInfoProxy state=$state")
            mGearPosition = CarInfoProxy.getInstance()
                .getItemValue(VDEventCarInfo.MODULE_READONLY_INFO, ReadOnlyID.ID_GEARBOX_STATE)
            Timber.i("init ID_GEARBOX_STATE mGearPosition=$mGearPosition")
            //底油量
            mLowFuelWarning = CarInfoProxy.getInstance().getItemValue(
                VDEventCarInfo.MODULE_READONLY_INFO, ReadOnlyID
                    .ID_LOW_FUEL_WARNING
            ) == 1
            Timber.i("init mLowFuelWarning=$mLowFuelWarning")
            //续航
            val enduranceValues = CarInfoProxy.getInstance().getItemValues(
                VDEventCarInfo.MODULE_READONLY_INFO,
                ReadOnlyID.ID_ENDURANCE_KM
            )
            if (enduranceValues.size >= 2) {
                mRangeDistance = ((enduranceValues[0] shl 8) or enduranceValues[1]) and 0xFFFF
                Timber.i("init mRangeDistance=$mRangeDistance")
            }

            val hevRangeDis = CarInfoProxy.getInstance().getItemValue(
                VDEventCarInfo.MODULE_NEW_ENERGY, NewEnergyID.ID_HEV_MILEAGE
            )
            Timber.i("init hevRangeDis=$hevRangeDis")

            val evRangeDis = CarInfoProxy.getInstance().getItemValue(
                VDEventCarInfo.MODULE_NEW_ENERGY, NewEnergyID.ID_EV_MILEAGE
            )

            CarInfoProxy.getInstance().getItemValues(VDEventCarInfo.MODULE_READONLY_INFO, CARINFO_CMDID_DASHBOARD_SPEED)?.let {
                if (it.size > 1) {
                    mDisplayVehicleSpeed = ((it[0] and 0xFF) shl 8) or ((it[1] and 0xFF))
                    Timber.i("get mDisplayVehicleSpeed = $mDisplayVehicleSpeed")
                }
            }

            Timber.i("init evRangeDis=$evRangeDis")
        }

        val subscribeVehicleHAL = {
            //车速
            VDBus.getDefault().addSubscribe(VDEventVehicleHal.PERF_VEHICLE_SPEED)
            VDBus.getDefault().getOnce(VDEventVehicleHal.PERF_VEHICLE_SPEED)?.run {
                payload.getDoubleArray(VDKeyVehicleHal.DOUBLE_VECTOR)?.let { data ->
                    if (data.isNotEmpty()) {
                        mVehicleSpeed = (data[0] * 3.6).toFloat()//获取第一次的车速
                        Timber.i("get mVehicleSpeed=$mVehicleSpeed")
                    }
                }
            }
            VDBus.getDefault().addSubscribe(VDEventVehicleHal.SV_RADIO_SEND_INFO)
            VDBus.getDefault().subscribeCommit()
            //主动查询主题
            requestCarDashboardTheme()
        }

        val subscribePowerStatus = {
            //电源状态
            VDBus.getDefault().addSubscribe(VDEventCarState.POWER_STATUS)
            VDBus.getDefault().getOnce(VDEventCarState.POWER_STATUS)?.run {
                mPowerState = payload.getInt(VDKey.STATUS)
                Timber.i("get powerStatus = $mPowerState")
            }
            VDBus.getDefault().subscribeCommit()
        }

        val subscribeCabinLan = {
            VDBus.getDefault().addSubscribe(VDEventCabinLan.CABIN_LAN_MSG_COMMON)
            VDBus.getDefault().subscribeCommit()
        }

        val subscribePhonelinkNaviStatus = {
            VDBus.getDefault().addSubscribe(VDEventNavi.PHONELINK_NAVI_STATUS)
            VDBus.getDefault().subscribeCommit()
        }

        val vDBindListener = object : VDBindListener {
            override fun onVDConnected(serviceType: VDServiceDef.ServiceType?) {
                Timber.i("onVDConnected serviceType=$serviceType")
                if (serviceType == VDServiceDef.ServiceType.VEHICLE_HAL) {
                    subscribeVehicleHAL()
                }
                if (serviceType == VDServiceDef.ServiceType.CAR_STATE) {
                    subscribePowerStatus()
                }
                //仪表
                if (serviceType == VDServiceDef.ServiceType.CABIN_LAN) {
                    subscribeCabinLan()
                }

                //互联
                if (serviceType == VDServiceDef.ServiceType.NAVI) {
                    Timber.i("serviceType == VDServiceDef.ServiceType.NAVI")
                    subscribePhonelinkNaviStatus()
                }

            }

            override fun onVDDisconnected(serviceType: VDServiceDef.ServiceType?) {
                Timber.i("onVDDisconnected serviceType=$serviceType")
            }

        }
        VDBus.getDefault().registerVDBindListener(VDServiceDef.ServiceType.VEHICLE_HAL, vDBindListener)
        VDBus.getDefault().registerVDBindListener(VDServiceDef.ServiceType.CAR_STATE, vDBindListener)
        VDBus.getDefault().registerVDBindListener(VDServiceDef.ServiceType.CABIN_LAN, vDBindListener)
        VDBus.getDefault().registerVDBindListener(VDServiceDef.ServiceType.NAVI, vDBindListener)
        if (VDBus.getDefault().isServiceConnected(VDServiceDef.ServiceType.VEHICLE_HAL)) {
            subscribeVehicleHAL()
        } else {
            VDBus.getDefault().bindService(VDServiceDef.ServiceType.VEHICLE_HAL)
        }

        if (VDBus.getDefault().isServiceConnected(VDServiceDef.ServiceType.CAR_STATE)) {
            subscribePowerStatus()
        } else {
            VDBus.getDefault().bindService(VDServiceDef.ServiceType.CAR_STATE)
        }

        if (VDBus.getDefault().isServiceConnected(VDServiceDef.ServiceType.CABIN_LAN)) {
            subscribeCabinLan()
        } else {
            VDBus.getDefault().bindService(VDServiceDef.ServiceType.CABIN_LAN)
        }

        if (VDBus.getDefault().isServiceConnected(VDServiceDef.ServiceType.NAVI)) {
            subscribePhonelinkNaviStatus()
            Timber.i("subscribePhonelinkNaviStatus NAVI")
        } else {
            VDBus.getDefault().bindService(VDServiceDef.ServiceType.NAVI)
            Timber.i("subscribePhonelinkNaviStatus bindService NAVI")
        }

        if (!VDBus.getDefault().isServiceConnected(VDServiceDef.ServiceType.BT)) {
            VDBus.getDefault().bindService(VDServiceDef.ServiceType.BT)
            Timber.i("bindService bindService BT")
        }

        val vDNotifyListener = VDNotifyListener { event, value ->
            event?.run {
                when (id) {
                    VDEventVehicleHal.PERF_VEHICLE_SPEED -> {
                        payload.getDoubleArray(VDKeyVehicleHal.DOUBLE_VECTOR)?.let { data ->
                            if (data.isNotEmpty()) {
                                mVehicleSpeed = (data[0] * 3.6).toFloat()
                                //Timber.d("mVehicleSpeed is $mVehicleSpeed")
                            }
                        } //单位为m/s
                    }

                    VDEventVehicleHal.SV_RADIO_SEND_INFO -> {
                        payload.getIntArray(VDKeyVehicleHal.INT_VECTOR)?.let { data ->
                            if (data[0] == "43".toInt(16) && data.size >= 2) {
                                mDashboardTheme = data[1]
                                Timber.i("on mDashboardTheme = $mDashboardTheme")
                                vehicleInfoCallbacks.forEach {
                                    it.onCarDashboardTheme(mDashboardTheme)
                                }
                            }
                        }
                    }

                    VDEventCarState.POWER_STATUS -> {
                        //参考VDValueCarState.PowerStatus
                        val powerStatus = payload.getInt(VDKey.STATUS)
                        Timber.i("powerStatus = $powerStatus")
                        mPowerState = powerStatus
                        MainScope().launch {
                            vehicleInfoCallbacks.forEach {
                                if (mPowerState == VDValueCarState.PowerStatus.STATE_AVN_RUN_ON) {
                                    it.powerStatusChange(true)
                                } else if (mPowerState == VDValueCarState.PowerStatus.STATE_AVN_OFF) {
                                    //下电和STR（内存挂起，内存暂停, 休眠）
                                    it.powerStatusChange(false)
                                } else {
                                    //其他不处理
                                }

                            }
                        }
                    }

                    VDEventCabinLan.CABIN_LAN_MSG_COMMON -> {
                        val message = VDCLCommonMessage.getValue(event)
                        if (message.msgType == 0x2004 && !message.content.isNullOrEmpty()) {
                            when (message.subtype) {
                                //视角请求
                                0 -> {
                                    gson.fromJson(message.content, VDNaviPerspective::class.java)?.run {
                                        Timber.i("requestPerspective = $requestPerspective")
                                        val mapMode = when (requestPerspective) {
                                            VDValueCarLan.Perspective.HEAD_UP_2D -> MapModeType.VISUALMODE_2D_CAR

                                            VDValueCarLan.Perspective.HEAD_UP_3D -> MapModeType.VISUALMODE_3D_CAR

                                            VDValueCarLan.Perspective.NORTH_UP_2D -> MapModeType.VISUALMODE_2D_NORTH

                                            else -> MapModeType.VISUALMODE_2D_NORTH
                                        }
                                        vehicleInfoCallbacks.forEach {
                                            it.dashboardMapModeState(mapMode)
                                        }
                                    }
                                }
                                //投屏区域控制
                                1 -> {
                                    try {
                                        gson.fromJson(message.content, VDNaviDisplayAreaT1N::class.java)?.run {
                                            Timber.d("RequestNaviAreaDisplay = $RequestNaviAreaDisplay")
                                            mDashboardStatus = int2CarDashboardStatus(RequestNaviAreaDisplay)
                                            vehicleInfoCallbacks.forEach {
                                                it.dashboardMapDisplayStatus(mDashboardStatus)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e)
                                    }
                                }
                            }
                        }
                    }

                    VDEventNavi.PHONELINK_NAVI_STATUS -> {
                        val bundle = payload
                        val status = bundle.getInt("status")
                        Timber.i("PHONELINK_NAVI_STATUS status:$status")
                        if (status == VDValueNavi.NaviStatus.START) { //互联开始导航，需要结束本地导航
                            if (phoneLinkNaviStopLocalNavi.value == null) {
                                phoneLinkNaviStopLocalNavi.postValue(true)
                            } else {
                                phoneLinkNaviStopLocalNavi.postValue(!phoneLinkNaviStopLocalNavi.value!!)
                            }
                        }
                    }
                }
            }
        }

        VDBus.getDefault().registerVDNotifyListener(VDServiceDef.ServiceType.VEHICLE_HAL, vDNotifyListener)
        VDBus.getDefault().registerVDNotifyListener(VDServiceDef.ServiceType.CAR_STATE, vDNotifyListener)
        VDBus.getDefault().registerVDNotifyListener(VDServiceDef.ServiceType.CABIN_LAN, vDNotifyListener)
        VDBus.getDefault().registerVDNotifyListener(VDServiceDef.ServiceType.NAVI, vDNotifyListener)

        CarInfoHelper().run {
            this.listen(
                VDEventCarInfo.MODULE_READONLY_INFO,
                intArrayOf(ReadOnlyID.ID_GEARBOX_STATE, ReadOnlyID.ID_ENDURANCE_KM, ReadOnlyID.ID_LOW_FUEL_WARNING, CARINFO_CMDID_DASHBOARD_SPEED)
            )
            this.listen(VDEventCarInfo.MODULE_CAR_SETTING, intArrayOf(232))
            this.listen(VDEventCarInfo.MODULE_AVM, intArrayOf(12))
            this.start { messageType, moduleId, cmdId ->
                when (messageType) {
                    MSG_ON_CHANGE_EVENT -> {
                        when (moduleId) {
                            VDEventCarInfo.MODULE_READONLY_INFO -> {
                                when (cmdId) {
                                    CARINFO_CMDID_DASHBOARD_SPEED -> {
                                        val values = CarInfoProxy.getInstance().getItemValues(moduleId, cmdId)
                                        if (values != null && values.size > 1) {
                                            mDisplayVehicleSpeed = ((values[0] and 0xFF) shl 8) or ((values[1] and 0xFF))
                                            //Timber.d("mDisplayVehicleSpeed = $mDisplayVehicleSpeed")
                                        }
                                    }

                                    //变速箱挡位变化
                                    ReadOnlyID.ID_GEARBOX_STATE -> {
                                        mGearPosition = CarInfoProxy.getInstance().getItemValue(moduleId, cmdId)
                                        Timber.i("mGearPosition=$mGearPosition")
                                    }

                                    ReadOnlyID.ID_ENDURANCE_KM -> {
                                        // 2个byte 取值： param1：高八位 param2：低八位
                                        val values = CarInfoProxy.getInstance().getItemValues(moduleId, cmdId)
                                        Timber.d("ID_ENDURANCE_KM values=${values.contentToString()}")
                                        if (values.size >= 2) {
                                            mRangeDistance = ((values[0] shl 8) or values[1]) and 0xFFFF
                                        }
                                        Timber.d("mRangeDistance=$mRangeDistance")
                                    }

                                    //0:no warning 1:warning
                                    ReadOnlyID.ID_LOW_FUEL_WARNING -> {
                                        val lowState = CarInfoProxy.getInstance().getItemValue(moduleId, cmdId)
                                        Timber.i("lowState=$lowState")
                                        MainScope().launch {
                                            mLowFuelWarning = lowState == 1
                                            vehicleInfoCallbacks.forEach { callback ->
                                                callback.lowFuelWarningNotify(mLowFuelWarning)
                                            }
                                        }
                                    }

                                }
                            }

                            VDEventCarInfo.MODULE_AVM -> {
                                Timber.d("CarInfoHelper MODULE_AVM")
                                licensePlateChange.postValue(true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestCarDashboardTheme() {
        Timber.i("requestCarDashboardTheme")
        val event = VDEvent(VDEventVehicleHal.SV_QUERY_SOME_INFO, Bundle().apply {
            putIntArray(VDKeyVehicleHal.INT_VECTOR, intArrayOf(0x26))
        })
        VDBus.getDefault().set(event)
    }

    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (uri == Settings.Global.getUriFor(KEY_SCREEN_STATE)) {
                val screenState = Settings.Global.getInt(application.contentResolver, KEY_SCREEN_STATE, 0)
                Timber.i("onChange selfChange=$selfChange screenState = $screenState")
                //screenStatus.postValue(screenState == 0)
            } else if (uri == Settings.Global.getUriFor(KEY_SCALABLE_STATE)) {
                val scalableState = Settings.Global.getInt(application.contentResolver, KEY_SCALABLE_STATE, 0)
                Timber.i("onChange selfChange=$selfChange scalableState = $scalableState")
                //scalableStatus.postValue(scalableState == 0)
            }
        }
    }

    private val settingsContentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (uri == Settings.System.getUriFor(KEY_FACTORY_RESET)) {
                val status = Settings.System.getInt(application.contentResolver, KEY_FACTORY_RESET, 0)
                Timber.i("KEY_FACTORY_RESET onChange selfChange=$selfChange status = $status")
                if (status == 1) {
                    //恢复出厂设置
                    vehicleInfoCallbacks.forEach { callback ->
                        callback.factoryResetNotify(true)
                    }
                }
            }
        }
    }

    override fun init() {
        if (isInit) {
            Timber.i("already init!!!")
            return
        }
        //0 为2/3屛， 1 为全屏
        val screenState = Settings.Global.getInt(application.contentResolver, KEY_SCREEN_STATE, 0)
        Timber.i("screenState = $screenState")
        application.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(KEY_SCREEN_STATE),
            true,
            contentObserver
        )
        //screenStatus.postValue(screenState == 0)

        //是否可缩放  0 可缩放， 1不可缩放
        val scalableState = Settings.Global.getInt(application.contentResolver, KEY_SCALABLE_STATE, 0)
        application.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(KEY_SCALABLE_STATE),
            true,
            contentObserver
        )
        Timber.i("scalableState = $scalableState")
        //scalableStatus.postValue(scalableState == 0)

        application.contentResolver.registerContentObserver(
            Settings.System.getUriFor(KEY_FACTORY_RESET),
            true,
            settingsContentObserver
        )

        application.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(BaseConstant.KEY_CARDASHBOARD_SHOW_ADAS),
            false,
            contentObserver
        )
        registerVehicleCallback()

        mCarAudioManager = Car.createCar(application.applicationContext)?.getCarManager(Car.AUDIO_SERVICE)?.let { it as CarAudioManager }
        if (mCarAudioManager == null) {
            Timber.w("mCarAudioManager is null")
        }
        isInit = true
    }

    override fun unInit() {
        application.contentResolver.unregisterContentObserver(contentObserver)
        application.contentResolver.unregisterContentObserver(settingsContentObserver)
        VDBus.getDefault().release()
        isInit = false
    }

    //通知大灯状态变化
    private fun notifyLightChange() {
        vehicleInfoCallbacks.forEach {
            it.lightStatusChange(mDippedHeadlightOpen!! || mHeadlightOpen!!)
        }
    }

    @Deprecated(
        "This function is deprecated, please follow Android native day and night mode",
        ReplaceWith("Activity.onConfigurationChanged() android.content.res.Configuration.uiMode")
    )
    override val isNightMode: Boolean
        /**
         * 车辆日夜模式是否是黑夜模式
         *
         * @return 是否是黑夜模式
         */
        get() = dayNightMode == 0

    override val gearPosition: Int
        /**
         * 获取档位
         *
         * @return 档位 0x1 P, 0x2 R, 0x3 N, 0x4 D
         */
        get() {
            if (mGearPosition == 0) {
                mGearPosition = CarInfoProxy.getInstance()
                    .getItemValue(VDEventCarInfo.MODULE_READONLY_INFO, ReadOnlyID.ID_GEARBOX_STATE)
            }
            return mGearPosition
        }
    override val powerState: Int
        /**
         * 电源状态
         *
         */
        get() {
            return mPowerState
        }
    override val vehicleSpeed: Float
        /**
         * 车速 km/h
         *
         * @return 车速 km/h
         */
        get() {
            //倒车挡位
            if (mGearPosition == 0x2)
                return -mVehicleSpeed
            return mVehicleSpeed
        }
    override val displayCarSpeed: Int
        /**
         * 车速 km/h
         *
         * @return 车速 km/h
         */
        get() {
            return mDisplayVehicleSpeed
        }
    override val vinCode: String?
        /**
         * 获取VIN号
         *
         * @return VIN号
         */
        get() {
            if (TextUtils.isEmpty(mVinCode)) {
                mVinCode = if (CommonUtils.isProdEnvironment()) {
                    CommonUtils.getSystemProperties("sys.vehicle.hardware.vin.code", "")
                } else {
                    CommonUtils.getSystemProperties("persist.sys.vehicle.vin", "")
                }
                Timber.d("CarInfoControl vinCode = $mVinCode")
                if (TextUtils.isEmpty(mVinCode))
                    mVinCode = "CG1000002" + (Math.random() * 9 + 1).toInt() * 100000
            }
            Timber.d("vinCode = $mVinCode")
            return mVinCode
        }
    override val sNCode: String
        /**
         * 获取SN码
         *
         * @return SN码
         */
        get() {
            if (TextUtils.isEmpty(mSNCode)) {
                mSNCode = CarConfigUtil.getDefault().sn
                Timber.d("VehicleManager.getInstance().getSNCode() is $mSNCode")
            }
            return mSNCode!!
        }

    override val partNum: String
        /**
         * 获取零件号
         *
         * @return 零件号
         */
        get() {
            if (TextUtils.isEmpty(mPartNum)) {
                mPartNum = CarConfigUtil.getDefault().partNumber.trim()
                Timber.d("VehicleManager.getInstance().getPartNum() is $mPartNum")
            }
            return mPartNum!!
        }

    override val uuid: String?
        get() {
            if (engineerBusiness.engineerConfig.openMapTestUuid) {
                val mapTestUuid = engineerBusiness.engineerConfig.mapTestUuid
                Timber.d("map_test_uuid is %s", mapTestUuid)
                BaseConstant.UUID_EMPTY = TextUtils.isEmpty(mapTestUuid)
                return mapTestUuid
            }
            val uuid = Settings.System.getString(application.contentResolver, "key_uuid")
            //必须给个无效值，不然网络激活没有回调
            //val uuid = BuildConfig.emulatorUuid
            BaseConstant.UUID_EMPTY = TextUtils.isEmpty(uuid)
            if (!BaseConstant.UUID_EMPTY) {
                Timber.d("getUuid uuid is empty")
                CustomFileUtils.saveFile(uuid, BaseConstant.LOCATION_UUID)
            } else {
                val locationUuid = CustomFileUtils.getFile(BaseConstant.LOCATION_UUID)
                if (!TextUtils.isEmpty(locationUuid)) {
                    Timber.d("getUuid LOCATION_UUID is %s", locationUuid)
                    return locationUuid
                }
            }
            Timber.d("getUuid is %s", if (TextUtils.isEmpty(uuid)) BuildConfig.defaultUuid else uuid)
            return if (TextUtils.isEmpty(uuid)) BuildConfig.defaultUuid else uuid
        }
    override val sensorTemperature: Double
        /**
         * 获取Sensor温度
         *
         * @return -45~80℃
         */
        get() = 0.0

    override val carModel: String?
        /**
         * 获取车型
         *
         * @return
         */
        get() {
            if (TextUtils.isEmpty(mCarModel)) {
                mCarModel = CarConfigUtil.getDefault().getConfig(Constants.ID_MODEL_CODE).toString()
            }
            Timber.d("getCarModel is %s", mCarModel)
            if (TextUtils.isEmpty(mCarModel)) {
                Timber.d("getCarModel fail, default model is 0")
                return "0"
            }
            return mCarModel
        }
    override val vehicleWeight: Int
        /**
         * 根据车型获取车重
         *
         * @param carModel 车型
         * @return 车重 kg
         */
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
        /**
         * 根据车型获取电池总电量
         *
         * @param carModel 车型
         * @return 电池总电量 kwh
         */
        get() {
            return when (carModel) {
                "U21A-0Z0", "U21A-0Z2", "U21A-0Z4" -> 92.0
                "U21A-0ZA", "U21A-0Z6", "U21A-0ZL", "U11A-0ZM" -> 95.0
                "U11A-0Z2", "U11A-0Z5", "U11A-0Z7", "U11A-0ZK", "U11A-0Z8" -> 114.0
                else -> 92.0
            }
        }
    override val currentBatteryEnergy: Double
        /**
         * 当前电量 kwh
         */
        get() = 18.15528 //测试 10kwh

    override val currentBatteryEnergyPercent: Double
        get() = currentBatteryEnergy / maxBatteryEnergy * 100.0

    override val rangeDist: Int
        /**
         * 续航里程 km
         * @return 剩余续航里程 km
         */
        get() = mRangeDistance
    override val lightStatus: Boolean
        /**
         * 灯光是否打开
         * 只判断大灯（近光灯 远光灯）
         *
         * @return true 打开 false 关闭
         */
        get() = false

    override val driverMode: Int
        /**
         * 驾驶模式
         *
         * @return
         */
        get() = 0

    override val energyUnit: Int get() = ElectricInfoConverter.EGEnergyUnit.E_ENERGY_UNIT_KWH

    @get:ElectricInfoConverter.PowerType
    override val powerType: Int
        /**
         * 动力类型
         *
         * @return 电动车
         */
        get() = if (isT1JFL2ICE()) {
            ElectricInfoConverter.PowerType.E_VEHICLE_ENERGY_FUEL
        } else {
            ElectricInfoConverter.PowerType.E_VEHICLE_ENERGY_ELECTRIC
        }


    @get:ElectricInfoConverter.ElectricVehicleType
    override val electricVehicleType: Int
        /**
         * 电动车类型
         *
         * @return 纯电动客车
         */
        get() = ElectricInfoConverter.ElectricVehicleType.E_ELECTRIC_BUS

    override val lowFuelWarning: Boolean
        get() = mLowFuelWarning

    override fun getSpeedCostList(model: String?): DoubleArray {
        return when (model) {
            "U21A-0Z0", "U21A-0ZA" -> doubleArrayOf(
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

            "U21A-0Z2" -> doubleArrayOf(
                5.0,
                103.5,
                20.0,
                75.31,
                45.0,
                98.4,
                80.0,
                164.6,
                120.0,
                283.0,
                150.0,
                407.9,
                200.0,
                676.2
            )

            "U21A-0Z4", "U21A-0Z6" -> doubleArrayOf(
                5.0,
                103.9,
                20.0,
                75.7,
                45.0,
                98.7,
                80.0,
                165.0,
                120.0,
                283.8,
                150.0,
                408.4,
                200.0,
                678.6
            )

            "U11A-0Z2", "U21A-0ZL", "U11A-0ZM" -> doubleArrayOf(
                5.0,
                102.4,
                20.0,
                74.3,
                45.0,
                97.5,
                80.0,
                163.7,
                120.0,
                282.3,
                150.0,
                406.7,
                200.0,
                674.7
            )

            "U11A-0Z5", "U11A-0ZK" -> doubleArrayOf(
                5.0,
                102.9,
                20.0,
                74.7,
                45.0,
                97.8,
                80.0,
                164.0,
                120.0,
                282.7,
                150.0,
                407.2,
                200.0,
                675.3
            )

            "U11A-0Z7", "U11A-0Z8" -> doubleArrayOf(
                5.0,
                103.3,
                20.0,
                75.1,
                45.0,
                98.0,
                80.0,
                164.4,
                120.0,
                283.2,
                150.0,
                407.7,
                200.0,
                675.9
            )

            else -> doubleArrayOf(
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
        }
    }

    override val carDashboardStatus: CarDashboardStatus get() = mDashboardStatus

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
        val flag = Settings.Global.putInt(
            application.contentResolver, KEY_SCREEN_STATE, if (isScreenStatus) 0
            else 1
        )
        //screenStatus.postValue(isScreenStatus) 会与回调重复通知
        Timber.i("setScreenStatus isScreenStatus=$isScreenStatus flag=$flag")
    }

    override fun getLauncherStatus(): LiveData<Boolean> {
        return scalableStatus
    }

    override fun <T> sendMessageToDashboard(data: T) {
        if (data is VDEvent) {
            VDBus.getDefault().set(data)
            Timber.d("setEventToVDB event.id=${data.id}")
        }
    }

    /**
     * 获取互联导航状态
     * VDValueNavi.NaviStatus.START 开始导航， VDValueNavi.NaviStatus.EXIT结束导航
     */
    override fun getPhoneLinkNaviStatus(): Int {
        var status = -1
        val event = VDBus.getDefault().getOnce(VDEventNavi.PHONELINK_NAVI_STATUS)
        if (event != null) {
            val bundle = event.payload
            status = bundle.getInt("status")
        }
        return status
    }

    /**
     * 设置本地导航状态
     * true. VDValueNavi.NaviStatus.START 开始导航， false. VDValueNavi.NaviStatus.EXIT结束导航
     */
    override fun publishNaviStatus(isStart: Boolean) {
        Timber.d("publishNaviStatus isStart = $isStart")
        val event = VDEvent(VDEventNavi.LOCAL_NAVI_STATUS)
        val payload = Bundle()
        payload.putInt(VDKey.STATUS, if (isStart) VDValueNavi.NaviStatus.START else VDValueNavi.NaviStatus.EXIT)
        event.payload = payload
        VDBus.getDefault().set(event)
    }

    override fun getPhoneLinkNaviStopLocalNavi(): LiveData<Boolean> {
        return phoneLinkNaviStopLocalNavi
    }

    override fun getLicensePlateChange(): LiveData<Boolean> {
        return licensePlateChange
    }

    override fun registerVehicleInfoCallback(callback: VehicleInfoCallback) {
        Timber.d("registerVehicleInfoCallback")
        vehicleInfoCallbacks.add(callback)
    }

    override fun unregisterVehicleInfoCallback(callback: VehicleInfoCallback) {
        Timber.d("unregisterVehicleInfoCallback")
        vehicleInfoCallbacks.remove(callback)
    }

    private fun int2CarDashboardStatus(naviDisplayArea: Int): CarDashboardStatus {
        return when (naviDisplayArea) {
            //关闭投屏
            VDValueCarLan.NaviDisplayArea.CLOSE_CAST_SCREEN -> {
                CarDashboardStatus.CLOSE
            }

            10 -> {
                CarDashboardStatus.FULL_MAP_THEME
            }

            else -> { //其它主题默认不显示
                CarDashboardStatus.CLOSE
            }
        }
    }

    override fun isJetOurGaoJie(): Boolean {
        val isJetOurGaoJie = CarConfigUtil.getDefault().isT1NGaoJie
        Timber.i("isJetOurGaoJie:$isJetOurGaoJie")
        return isJetOurGaoJie
    }

    override fun getVolume(type: Int): Int {
        var mediaCurVolume = 0
        if (mCarAudioManager == null)
            mCarAudioManager = Car.createCar(application.applicationContext)?.getCarManager(Car.AUDIO_SERVICE)?.let { it as CarAudioManager }
        mCarAudioManager?.let {
            val groupId = it.getVolumeGroupIdForUsage(type)
            mediaCurVolume = it.getGroupVolume(groupId)
            Timber.i(" getVolume type = $type  groupId = $groupId volume = $mediaCurVolume")
        }
        return mediaCurVolume
    }

    override fun setVolume(type: Int, volume: Int) {
        if (mCarAudioManager == null)
            mCarAudioManager = Car.createCar(application.applicationContext)?.getCarManager(Car.AUDIO_SERVICE)?.let { it as CarAudioManager }
        mCarAudioManager?.let {
            val groupId = it.getVolumeGroupIdForUsage(type)
            it.setGroupVolume(groupId, volume, 0)
            Timber.i("setVolume: type = $type groupId = $groupId volume = $volume")
        }
    }

    override fun getDashboardTheme(): Int {
        if (mDashboardTheme == -1) {
            requestCarDashboardTheme()
        }
        Timber.i("getDashboardTheme mDashboardTheme = $mDashboardTheme")
        return mDashboardTheme
    }

    override fun setDashboardTheme(theme: Int) {
        val payload = Bundle()
        // 后三位 0x01(开启同步) 0x02(主题枚举) 0x02（中控切换完成）
        payload.putIntArray(VDKeyVehicleHal.INT_VECTOR, intArrayOf(0x29, 1, theme, 2))
        Timber.i("setDashboardTheme theme = $theme payload = $payload")
        val event = VDEvent(VDEventVehicleHal.SV_DISPATCH_RADIO_REQUEST_INFO, payload)
        VDBus.getDefault().set(event)
        MainScope().launch {
            delay(200)
            requestCarDashboardTheme()
        }
    }

    override fun isT1JFL2ICE(): Boolean {
        val isT1JFL2ICE = CarConfigUtil.getDefault().isT1JFL2ICE
        Timber.i("isT1J FL2 燃油版:$isT1JFL2ICE")
        return isT1JFL2ICE
    }

    override fun isT1JFL2PHEV(): Boolean {
        val isT1JFL2PHEV = CarConfigUtil.getDefault().isT1JFL2PHEV
        Timber.i("isT1J FL2 PHEV版:$isT1JFL2PHEV")
        return isT1JFL2PHEV
    }

}