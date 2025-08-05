package com.desaysv.psmap.base.common

import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryResponseParam
import com.autonavi.gbl.aosclient.model.GRangeSpiderRequestParam
import com.autonavi.gbl.common.model.ElecInfoConfig
import com.autonavi.gbl.guide.model.guidecontrol.Param
import com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamElecVehicleCharge
import com.autosdk.bussiness.adapter.bean.AdapterCarAllInfo
import com.autosdk.bussiness.adapter.bean.AdapterCarChargeInfo
import com.autosdk.bussiness.adapter.bean.AdapterCarEnergyInfo
import com.autosdk.bussiness.adapter.bean.AdapterCarInfo
import com.autosdk.bussiness.aos.AosController
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.utils.ElectricInfoConverter
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.widget.BusinessApplicationUtils
import com.autosdk.bussiness.widget.search.EtaQueryCallback
import com.autosdk.bussiness.widget.search.SearchComponent
import com.desaysv.psmap.base.impl.ICarInfoProxy
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine


/**
 * Author : Zhipeng Zhu
 * Date : 2022-6-30
 * Description : 新能源管理类
 */
@Singleton
class EVManager @Inject constructor(
    private val carInfoProxy: ICarInfoProxy,
    private val naviController: NaviController
) {
    private val carAllInfo: AdapterCarAllInfo = AdapterCarAllInfo()

    fun init() {
        //todo 充电信息,需要实时更新充电信息
        /*carAllInfo.carChargeInfo = AdapterCarChargeInfo()
        carAllInfo.carChargeInfo.chargeStatus = 101 //充电状态通知（int）（100-开始充电；101-退出充电）
        carAllInfo.carChargeInfo.changeType = 1 //充电类型（int）（1：直流电（快充） 2：交流电（慢充） 3：其他充电类型）
        if (carAllInfo.carChargeInfo.chargeStatus === 100) {
            carAllInfo.carChargeInfo.isCharge = true
        } else {
            carAllInfo.carChargeInfo.isCharge = false
        }*/

        //车辆信息
        carAllInfo.carInfo = AdapterCarInfo()
        carAllInfo.carInfo.vin = carInfoProxy.vinCode
        // 车架号 可选
        carAllInfo.carInfo.engineNo = "" //发动机号(非必须，暂时无用)
        carAllInfo.carInfo.brand = "DesaySv" // 品牌
        carAllInfo.carInfo.model = carInfoProxy.carModel // 车辆型号
        carAllInfo.carInfo.maxBattEnergy = carInfoProxy.maxBatteryEnergy
        //电池最大负载电量kwh，必须
        carAllInfo.carInfo.vehicleWeight = carInfoProxy.vehicleWeight //int，车重，单位kg
        carAllInfo.carInfo.energyUnit = carInfoProxy.energyUnit // 能量单位，默认传1
        carAllInfo.carInfo.energyPowerType = carInfoProxy.powerType //动力类型（0：燃油车1：电动车2：插电混动）4.3.0及以上版本支持
        carAllInfo.carInfo.electricVehicleType = carInfoProxy.electricVehicleType

        //引擎信息
        carAllInfo.carEnergyInfo = AdapterCarEnergyInfo()
        carAllInfo.carEnergyInfo.initIalHvBattEnergy = carInfoProxy.currentBatteryEnergy//double 当前电量，单位默认kwh
        //carAllInfo.carEnergyInfo.lowEnergyAlert = 没用;//int 电量预警信号
        //carAllInfo.carEnergyInfo.lowEnergyWarn =  没用;//int 电量告警信号
        carAllInfo.carEnergyInfo.rangeDist = carInfoProxy.rangeDist//int 剩余可行驶里程（巡航半径），单位km（如走离线逻辑，则此项为必填）
        //carAllInfo.carEnergyInfo.isCharge = carAllInfo.carChargeInfo.isCharge;//boolean 是否处于充电状态
        carAllInfo.carEnergyInfo.curDriveMode = carInfoProxy.driverMode //int 当前驾驶模式，
        carAllInfo.carEnergyInfo.energyUnit = carInfoProxy.energyUnit //int 能量单位，默认传1
        carAllInfo.carEnergyInfo.topSpeed = 120 //int 最大限速，默认120
        carAllInfo.carEnergyInfo.maxBattEnergy = carInfoProxy.maxBatteryEnergy //double 电池最大负载电量kwh，必须
        carAllInfo.carEnergyInfo.vehicleWeight = carInfoProxy.vehicleWeight //int 车重，单位KG
        //carAllInfo.carEnergyInfo.slopeCostList = convertStringToDoubleArray(intent.getStringArrayExtra("SLOPE_COSTLIST"));//DoubleArray 坡度消耗权值
        carAllInfo.carEnergyInfo.speedCostList = carInfoProxy.getSpeedCostList(
            carInfoProxy.carModel
        ) //DoubleArray 速度相关权值，可以重复传多组（每奇偶为一组 如 1（速度）、2（消耗）、3（速度）、4（消耗） 其中1、2为第一组中的速度、消耗， 3、4为第2组的速度、消耗以此类推）
        //carAllInfo.carEnergyInfo.tansCostList = convertStringToDoubleArray(intent.getStringArrayExtra("TRANS_COSTLIST"));//DoubleArray 转向消耗权值数组
        //carAllInfo.carEnergyInfo.curveCostList = convertStringToDoubleArray(intent.getStringArrayExtra("CURVE_COSTLIST"));//DoubleArray 曲率消耗权值
        //carAllInfo.carEnergyInfo.auxCost = Double.parseDouble(intent.getStringExtra("AUX_COST"));//double 附加消耗系数
        //carAllInfo.carEnergyInfo.ferryRateCost = Double.parseDouble(intent.getStringExtra("FERRYRATE_COST"));//double 轮渡消耗（车辆静止场景下的消耗）
        carAllInfo.carEnergyInfo.percentOfResidualEnergy = carInfoProxy.currentBatteryEnergyPercent //剩余电量百分比，计算到达范围蜘蛛网用到
        //double 剩余电量百分比（3.2.9版本支持，4.3.0及以上版本支持）
        //arAllInfo.carEnergyInfo.powerTrainLoss = convertStringToDoubleArray(intent.getStringArrayExtra("POWERTRAIN_LOSS"));//DoubleArray 动力总成消耗,可以重复传多组(powerdemand1,costValue1;powerdemand2,costValue2;)

        //附加信息，暂无
        //carAllInfo.carAdditionInfo = AdapterCarInfoConvert.convertCarAdditionInfo(intent)
        BusinessApplicationUtils.setElectricInfo(carAllInfo)
    }

    /**
     * 交通开关
     *
     * @return 是否有交通信息
     */
    @get:ElectricInfoConverter.TrafficFlag
    val trafficFlag: Int get() = ElectricInfoConverter.TrafficFlag.TRAFFIC_YES

    /**
     * 更新新能源功能电量
     */
    fun updateCurrentBattEnergy(currentBatteryEnergy: Double? = null) {
        carAllInfo.carEnergyInfo.initIalHvBattEnergy = currentBatteryEnergy ?: carInfoProxy.currentBatteryEnergy
        carAllInfo.carEnergyInfo.percentOfResidualEnergy = carInfoProxy.currentBatteryEnergyPercent
    }

    /**
     * 当前剩余电量上报，每分钟一次
     */
    fun updateElecVehicleCharge() {
        carAllInfo.carEnergyInfo.initIalHvBattEnergy = carInfoProxy.currentBatteryEnergy
        carAllInfo.carEnergyInfo.percentOfResidualEnergy = carInfoProxy.currentBatteryEnergyPercent
        val param = Param()
        param.type = GuideParamElecVehicleCharge
        param.elecVehicle.vehicleCharge = carAllInfo.carEnergyInfo.initIalHvBattEnergy.toFloat()
        Timber.i("updateElecVehicleCharge ${param.elecVehicle.vehicleCharge}")
        naviController.setGuideParam(param)
    }


    /**
     * 更新新能源功能充电状态
     */
    fun updateCurrentBattEnergy(chargeStatus: Int, changeType: Int) {
        Timber.i("updateCurrentBattEnergy chargeStatus=$chargeStatus changeType=$changeType")
        carAllInfo.carChargeInfo = AdapterCarChargeInfo()
        carAllInfo.carChargeInfo.chargeStatus = chargeStatus //充电状态通知（int）（100-开始充电；101-退出充电）
        carAllInfo.carChargeInfo.changeType = changeType //充电类型（int）（1：直流电（快充） 2：交流电（慢充） 3：其他充电类型）
        if (carAllInfo.carChargeInfo.chargeStatus == 100) {
            carAllInfo.carChargeInfo.isCharge = true
            carAllInfo.carEnergyInfo.isCharge = true
        } else {
            carAllInfo.carChargeInfo.isCharge = false
            carAllInfo.carEnergyInfo.isCharge = false
        }
    }

    /**
     * 电动车配置
     *
     * @return 电动车配置
     */
    fun getElectricConfig(): ElecInfoConfig {
        carAllInfo.carEnergyInfo.initIalHvBattEnergy = carInfoProxy.currentBatteryEnergy //更新最新当前电量，单位默认kwh
        return ElectricInfoConverter.getElecInfoConfig()
    }

    /**
     * 电动车配置 自定义算路参数
     *
     * @param powerFlag       能耗配置开关：0x00000001表示eta是否加充电时间， 0x00000002表示路线上的充电站充电时间是否下发, 0x00000003 eta包含充电时间+下发充电站充电时间 [必须]
     * @param arrivingPercent 到达充电站最大充电百分比 [必须]
     * @param leavingPercent  到达充电站最小电量百分比 [必须]
     */
    fun getElectricConfig(@ElectricInfoConverter.EtaPowerFlag powerFlag: Int, arrivingPercent: Int, leavingPercent: Int): ElecInfoConfig? {
        carAllInfo.carEnergyInfo.initIalHvBattEnergy = carInfoProxy.currentBatteryEnergy //更新最新当前电量，单位默认kwh
        carAllInfo.carEnergyInfo.percentOfResidualEnergy = carInfoProxy.currentBatteryEnergyPercent
        val elecInfoConfig = ElectricInfoConverter.getElecInfoConfig()
        elecInfoConfig.powerflag = powerFlag
        elecInfoConfig.arrivingPercent = arrivingPercent
        elecInfoConfig.leavingPercent = leavingPercent
        return elecInfoConfig
    }

    val isLowEnergyAlarm: Boolean
        /**
         * 处于低电量警报
         *
         * @return 是否处于低电量警报
         */
        get() = carInfoProxy.currentBatteryEnergyPercent <= ElectricInfoConverter.getAlertEnergyPercent()
    val isLowEnergyWarn: Boolean
        /**
         * 处于低电量预警
         *
         * @return 是否处于低电量
         */
        get() = carInfoProxy.currentBatteryEnergyPercent <= ElectricInfoConverter.getLowEnergyPercent()

    /**
     * 算路是否显示低电量提示卡片
     */
    val isShowElectricTipView: Boolean
        get() {
            carAllInfo.carEnergyInfo.initIalHvBattEnergy = carInfoProxy.currentBatteryEnergy //更新最新当前电量，单位默认kwh
            carAllInfo.carEnergyInfo.percentOfResidualEnergy = carInfoProxy.currentBatteryEnergyPercent
            carAllInfo.carEnergyInfo.rangeDist = carInfoProxy.rangeDist
            return if (RouteRequestController.getInstance().carRouteResult == null) false else ElectricInfoConverter.isShowElectricTipView(
                RouteRequestController.getInstance().carRouteResult
            )
        }

    private var reqVehicleChargeId = 0L

    private var reqVehicleChargeCallback = object : EtaQueryCallback() {
        var myContinuation: Continuation<String>? = null

        override fun onRecvAck(gNavigationEtaqueryResponseParam: GNavigationEtaqueryResponseParam) {
            var vehicleCharge = ""
            val routeList = gNavigationEtaqueryResponseParam.route_list
            poi.chargeLeftPercentage = -1
            if (routeList != null && routeList.size > 0 && routeList[0].path != null && routeList[0].path.size > 0) {
                if (routeList[0].path[0].charge_left != -1 && poi.maxEnergy != 0.0) {
                    val e1 = Math.floor((routeList[0].path[0].charge_left / 100000f).toDouble())
                    val d = Math.floor(e1 / poi.maxEnergy * 100).toInt()
                    vehicleCharge = "$d%"
                    poi.chargeLeftPercentage = d
                } else {
                    vehicleCharge = if (routeList[0].path[0].distance > 500 * 1000) {
                        ""
                    } else {
                        if (etaQueryRequestParam.vehicle.elec.charge > 0) {
                            "当前电量不可达"
                        } else {
                            ""
                        }
                    }
                }
            } else {
                vehicleCharge = ""
            }
            val delay = System.currentTimeMillis() - oldCurTime
            Timber.d("SearchComponent delay==$delay")
            if (gNavigationEtaqueryResponseParam.mNetworkStatus != 4) {
                vehicleCharge = "到达电量未知"
            }
            /*val configKeyPowerType = SettingComponent.getInstance().configKeyPowerType
            if (configKeyPowerType == 0 || configKeyPowerType == 3 || configKeyPowerType == -1) {
                vehicleCharge = ""
            }
            if (poi is AlongWaySearchPoi) {
                poi.vehiclechargeleft = vehicleCharge
            } else {
                poi.chargeLeft = vehicleCharge
            }*/
            Timber.i("resume $vehicleCharge")
            myContinuation?.resume(vehicleCharge)
        }
    }

    suspend fun reqVehicleCharge(poi: POI): String {
        carAllInfo.carEnergyInfo.initIalHvBattEnergy = carInfoProxy.currentBatteryEnergy //更新最新当前电量，单位默认kwh
        carAllInfo.carEnergyInfo.percentOfResidualEnergy = carInfoProxy.currentBatteryEnergyPercent
        if (reqVehicleChargeId > 0) AosController.getInstance().abortRequest(reqVehicleChargeId)
        return withTimeoutOrNull(5000) {
            suspendCancellableCoroutine { continuation ->
                if (!ElectricInfoConverter.isElectric()) {
                    Timber.i("not isElectric")
                    continuation.resume("")
                }
                val oldCurTime = System.currentTimeMillis()
                val etaQueryRequestParam = SearchComponent.getInstance().getEtaQueryRequestParam(poi)
                if (etaQueryRequestParam == null) {
                    Timber.i("GNavigationEtaqueryRequestParam is null , poi.name = %s , poi.id = %s", poi.name, poi.id)
                    continuation.resume("")
                }
                reqVehicleChargeCallback.myContinuation = continuation
                etaQueryRequestParam.mTimeOut = 3000
                (reqVehicleChargeCallback as EtaQueryCallback).updateFiled(poi, etaQueryRequestParam, oldCurTime)
                reqVehicleChargeId = AosController.getInstance().sendReqNavigationEtaquery(etaQueryRequestParam, reqVehicleChargeCallback)
            }
        } ?: ""

    }

    val rangeSpiderRequestParam: GRangeSpiderRequestParam
        /**
         * 电量可达范围网接口需要的 请求参数
         *
         * @return 电量可达范围网 请求参数
         */
        get() {
            carAllInfo.carEnergyInfo.initIalHvBattEnergy = carInfoProxy.currentBatteryEnergy //更新最新当前电量，单位默认kwh
            carAllInfo.carEnergyInfo.percentOfResidualEnergy = carInfoProxy.currentBatteryEnergyPercent
            return ElectricInfoConverter.getRangeSpiderRequestParam()
        }
}
