package com.autosdk.bussiness.common.utils;

import android.location.Location;

import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryReqElecConstList;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryReqElecConstListPowertrainloss;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryReqElecConstListRangeEnergy;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryReqStartEnd;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryReqVehicle;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryRequestParam;
import com.autonavi.gbl.aosclient.model.GRangeSpiderPoint;
import com.autonavi.gbl.aosclient.model.GRangeSpiderRequestParam;
import com.autonavi.gbl.aosclient.model.GRangeSpiderVehicleElecCostlist;
import com.autonavi.gbl.aosclient.model.GRangeSpiderVehicleElecCostlistPowertrainloss;
import com.autonavi.gbl.aosclient.model.GRangeSpiderVehicleElecCostlistRange;
import com.autonavi.gbl.aosclient.model.GRangeSpiderVehicleElecCostlistSpeed;
import com.autonavi.gbl.common.model.ElecCostList;
import com.autonavi.gbl.common.model.ElecInfoConfig;
import com.autonavi.gbl.common.model.ElecSpeedCostList;
import com.autonavi.gbl.common.model.PowertrainLoss;
import com.autonavi.gbl.common.path.model.ElecPathInfo;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.common.path.option.RouteConstrainCode;
import com.autosdk.bussiness.adapter.bean.AdapterCarAllInfo;
import com.autosdk.bussiness.adapter.bean.AdapterCarEnergyInfo;
import com.autosdk.bussiness.adapter.bean.AdapterCarInfo;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;

import timber.log.Timber;


public class ElectricInfoConverter {

    private static final String TAG = ElectricInfoConverter.class.getName();

    private static final String CHARGE_STATION_TYPE = "011100";

    /**
     * 动力类型
     */
    public @interface PowerType {
        /**
         * 默认值
         */
        int E_VEHICLE_ENERGY_DEFAULT = -1;
        /**
         * 燃油车
         */
        int E_VEHICLE_ENERGY_FUEL = 0;
        /**
         * 电动车
         */
        int E_VEHICLE_ENERGY_ELECTRIC = 1;
        /**
         * 混合动力
         */
        int E_VEHICLE_ENERGY_HYBRID = 2;
        /**
         * 油气两用
         */
        int E_VEHICLE_ENERGY_GASCNG = 3;
        /**
         * 增程式电动：界面展示逻辑特殊，逻辑功能参照混合动力
         */
        int E_VEHICLE_ENERGY_PLUG_IN_ELECTRIC = 4;
    }

    /**
     * 电动车类型
     */
    public @interface ElectricVehicleType {
        /**
         * < 未获取到
         */
        int E_NONE = -1;
        /**
         * < 燃油客车(默认值)
         */
        int E_BUS = 0;
        /**
         * < 燃油动货车
         */
        int E_TRACK = 1;
        /**
         * < 纯电动客车
         */
        int E_ELECTRIC_BUS = 2;
        /**
         * < 纯电动货车
         */
        int E_ELECTRIC_TRACK = 3;
        /**
         * < 插电式混动客车
         */
        int E_HYBRID_BUS = 4;
        /**
         * < 插电式混动货车
         */
        int E_HYBRID_TRACK = 5;
    }

    /**
     * 单位
     */
    public @interface EGEnergyUnit {
        /**
         * 升：L
         */
        int E_ENERGY_UNIT_L = 0;
        /**
         * 千瓦时：KWH
         */
        int E_ENERGY_UNIT_KWH = 1;
        /**
         * 米：METRE
         */
        int E_ENERGY_UNIT_METRE = 2;
        /**
         * 秒：SECOND
         */
        int E_ENERGY_UNIT_SECOND = 3;
    }

    /**
     * 驾驶模式
     */
    public @interface EGDriveMode {
        /**
         * 舒适模式 COMFORT
         */
        int E_RANGE_MAP_REFACTR_COMF = 0;
        /**
         * 运动模式 SPORT
         */
        int E_RANGE_MAP_REFACTR_SPORT = 1;
        /**
         * 强力运动模式 SPORTPLUS
         */
        int E_RANGE_MAP_REFACTR_SPORT_PLUS = 2;
        /**
         * 节能模式 ECOPRO
         */
        int E_RANGE_MAP_REFACTR_ECO = 3;
        /**
         * 强力节能模式 ECOPROPLUS
         */
        int E_RANGE_MAP_REFACTR_ECO_PLUS = 4;
    }

    /**
     * 车辆信息
     */
    public static class CarElectricInfo {

        /**
         * 电池最大负载电量,必填
         */
        public double maxEnergy;

        /**
         * 车重，单位吨（t），必填
         */
        public int vehicleWeight;

        /**
         * 车架号
         */
        public String vin;

        /**
         * 发动机号
         */
        public String enginNo;

        /**
         * 品牌
         */
        public String brand;

        /**
         * 车辆型号
         */
        public String model;

        /**
         * < 能量单位，默认传1。若系统未赋值，则默认单位为KWH,0: L,1: KWH,2: METRE,3: SECOND
         */
        public @EGEnergyUnit int energyUnit;

        /**
         * 默认构造函数
         */
        CarElectricInfo() {
            maxEnergy = 0;
            vehicleWeight = 0;
            energyUnit = EGEnergyUnit.E_ENERGY_UNIT_KWH;
        }
    }


    /**
     * 速度能耗权值
     */
    public static class EnergySpeedCost {
        /**
         * 速度值
         */
        float speed;

        /**
         * 能量消耗值
         */
        float costValue;
    }

    /**
     * 动力衰减能耗权值
     */
    public static class STPowertrainLoss {

        /**
         * 动力衰减因子系数
         */
        float powerdemand;

        /**
         * 能量消耗值
         */
        float costValue;
    }

    /**
     * Range On信息
     */
    public static class NaviRangeOnInfo {
        /**
         * 是否有效值
         */
        public boolean isValid;

        /**
         * 当前电量值 取值范围（0~2047） kwh
         */
        public float initialHvBattEnergy;

        /**
         * 0 - 非预警 1 - 预警
         */
        public int lowEnergyAlert;

        /**
         * 剩余可行驶里程（巡航半径），单位km
         */
        public int rangeDis;
        /**
         * 是否处于充电状态 false - 否 true - 是
         */
        public boolean inCharge;
        /**
         * 当前驾驶模式 0:COMFORT, 1:SPORT, 2:SPORTPLUS, 3:ECOPRO, 4:ECOPROPLUS
         */
        public @EGDriveMode int driveMode;
        /**
         * 能量单位，默认KWH，若系统未赋值则默认KWH
         */
        public @EGEnergyUnit int energyUnit;
        /**
         * 最大限速
         */
        public int topSpeed;
        /**
         * 电池最大负载电量
         */
        public float maxEnergy;
        /**
         * 车重，单位吨（t）
         */
        public int vehicleWeight;
        /**
         * 坡度上升导致的消耗系数
         */
        public float slopeUp;
        /**
         * 坡度下降导致的消耗系数
         */
        public float slopeDown;
        /**
         * 速度上升引起的能量消耗系数
         */
        public float transAccess;
        /**
         * 速度下降引起的能量消耗系数
         */
        public float transDecess;
        /**
         * 曲率上升导致的消耗系数
         */
        public float curveAccess;
        /**
         * 曲率下降导致的消耗系数
         */
        public float curveDecess;
        /**
         * 附加消耗系数（空调等车载设备）
         */
        public float auxCost;
        /**
         * 轮渡消耗（车辆静止场景下的消耗）
         */
        public float ferryrateCost;
        /**
         * 速度相关消耗权值，可以重复传多组
         */
        public ArrayList<EnergySpeedCost> speedCost = new ArrayList<>();
        /**
         * 动力衰减消耗权值数组
         */
        public ArrayList<STPowertrainLoss> powertrainLoss = new ArrayList<>();
        /**
         * 剩余电量百分比
         */
        public float percentOfResidualEnergy;

        /**
         * 默认构造函数
         */
        NaviRangeOnInfo() {
            driveMode = EGDriveMode.E_RANGE_MAP_REFACTR_ECO;
            energyUnit = EGEnergyUnit.E_ENERGY_UNIT_KWH;
        }
    }

    /**
     * 代价模型组合开关
     */
    public @interface EGCostModelSwitch {
        /**
         * 表示 speed cost ，速度代价模型
         */
        short E_SPEED_COST_MODEL_SWITCH = 0x1;
        /**
         * 表示 curve cost ，曲率代价模型
         */
        short E_CURVE_COST_MODEL_SWITCH = 0x2;
        /**
         * 表示 slope cost ，坡度代价模型
         */
        short E_SLOPE_COST_MODEL_SWITCH = 0x4;
        /**
         * 表示 aux cost ，  附加消耗代价模型（空调、中控屏等车载设备）
         */
        short E_AUX_COST_MODEL_SWITCH = 0x8;
        /**
         * 表示 trans cost ，加减速代价模型
         */
        short E_TRANS_COST_MODEL_SWITCH = 0x10;
        /**
         * 表示 ferry cost   车船轮渡代价模型（车辆静止场景下的消耗）
         */
        short E_FERRY_COST_MODEL_SWITCH = 0x20;
        /**
         * 表示 powertrainloss 路线行驶代价模型
         */
        short E_POWER_COST_MODEL_SWITCH = 0x40;
    }

    /**
     * 代价模型组合开关
     */
    public @interface EtaPowerFlag {
        int ETA_CHARGE_TIME_IN = 0x00000001;//表示eta是否加充电时间
        int ETA_CHARGE_TIME_OUT = 0x00000002;//表示路线上的充电站充电时间是否下发
        int ETA_CHARGE_TIME_IN_OUT = 0x00000003;//eta包含充电时间+下发充电站充电时间
    }

    /**
     * 是否有交通信息，整型，1：有，0：无 [必须值]
     */
    public @interface TrafficFlag {
        int TRAFFIC_YES = 1;
        int TRAFFIC_NO = 0;
    }


    public static ElecInfoConfig getElecInfoConfig() {

        /**< 通过适配层从系统中获取车辆动力类型 */
        int powerType = getPowerType();

        /**< 只支持纯电动车 */
        if (powerType != PowerType.E_VEHICLE_ENERGY_ELECTRIC) {
            return null;
        }

        ElecInfoConfig elecConfig = new ElecInfoConfig();

        /** 通过适配层从系统中获取车身数据 */
        CarElectricInfo elecInfo = getCarElectricInfo();
        NaviRangeOnInfo rangeOnInfo = getRangeOnInfo();

        ElecCostList costList = new ElecCostList();

        /**
         *   与产品确认的costmodelswith Check逻辑如下（#19869065）
         *   1、如果costmodelswith不为0，则必须包含0x01.
         *   2、curve:
         *       1) curveAccess和curveDecess必须大于0
         *       2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
         *       3) curveAccess必须小于curveDecess
         *   3、slope:
         *       1) slope Up和slope Down必须大于0
         *       2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
         *       3) slope Up必须小于slope Down
         *   4、auxCost：
         *       1) 必须>=0
         *   5、trans：
         *       1) transAccess和transDccess必须大于0
         *       2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
         *       3) transAccess必须小于transDecess
         *   6、ferryRate：
         *       1) 必须>=0
         */

        for (int i = 0; i < rangeOnInfo.speedCost.size(); i++) {
            ElecSpeedCostList speedCost = new ElecSpeedCostList();
            speedCost.speed = (int) rangeOnInfo.speedCost.get(i).speed;
            speedCost.costValue = rangeOnInfo.speedCost.get(i).costValue;

            Timber.d("GetRangeOnElecInfoConfig index = %d, speed = %d, costValue = %f", i, speedCost.speed, speedCost.costValue);

            costList.speedCost.add(speedCost);

            elecConfig.costModelSwitch |= EGCostModelSwitch.E_SPEED_COST_MODEL_SWITCH;
        }

        for (int i = 0; i < rangeOnInfo.powertrainLoss.size(); i++) {
            PowertrainLoss powertrainLoss = new PowertrainLoss();
            powertrainLoss.powerdemand = rangeOnInfo.powertrainLoss.get(i).powerdemand;
            powertrainLoss.costValue = rangeOnInfo.powertrainLoss.get(i).costValue;

            Timber.d("GetRangeOnElecInfoConfig index = %d, powerdemand = %f, costValue = %f", i, powertrainLoss.powerdemand, powertrainLoss.costValue);

            costList.powertrainLoss.add(powertrainLoss);

            elecConfig.costModelSwitch |= EGCostModelSwitch.E_POWER_COST_MODEL_SWITCH;
        }

        /**
         * 2、curve:
         * 1) curveAccess和curveDecess必须大于0
         * 2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
         * 3) curveAccess必须小于curveDecess
         */
        if ((rangeOnInfo.curveAccess >= 0.00001) & (rangeOnInfo.curveDecess >= 0.00001)
                & (rangeOnInfo.vehicleWeight != 0)
                & ((rangeOnInfo.curveAccess / rangeOnInfo.vehicleWeight > 72.0) & (rangeOnInfo.curveAccess / rangeOnInfo.vehicleWeight < 72000.0))
                & ((rangeOnInfo.curveDecess / rangeOnInfo.vehicleWeight > 72.0) & (rangeOnInfo.curveDecess / rangeOnInfo.vehicleWeight < 72000.0))
                & (rangeOnInfo.curveAccess < rangeOnInfo.curveDecess)) {
            costList.curve.access = rangeOnInfo.curveAccess;
            costList.curve.decess = rangeOnInfo.curveDecess;
            elecConfig.costModelSwitch |= EGCostModelSwitch.E_POWER_COST_MODEL_SWITCH;
        }

        Timber.d("GetRangeOnElecInfoConfig curveAccess = %f, curveDecess = %f, vehicleWeight = %d, costModelSwitch = %d"
                , rangeOnInfo.curveAccess, rangeOnInfo.curveDecess, rangeOnInfo.vehicleWeight, elecConfig.costModelSwitch);

        /**
         * 3、slope:
         * 1) slope Up和slope Down必须大于0
         * 2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
         * 3) slope Up必须小于slope Down
         */
        if ((rangeOnInfo.slopeUp >= 0.00001) & (rangeOnInfo.slopeDown >= 0.00001)
                & (rangeOnInfo.vehicleWeight != 0)
                & ((rangeOnInfo.slopeUp / rangeOnInfo.vehicleWeight > 72.0) & (rangeOnInfo.slopeUp / rangeOnInfo.vehicleWeight < 72000.0))
                & ((rangeOnInfo.slopeDown / rangeOnInfo.vehicleWeight > 72.0) & (rangeOnInfo.slopeDown / rangeOnInfo.vehicleWeight < 72000.0))
                & (rangeOnInfo.slopeUp < rangeOnInfo.slopeDown)) {
            costList.slope.decess = rangeOnInfo.slopeDown;
            costList.slope.access = rangeOnInfo.slopeUp;
            elecConfig.costModelSwitch |= EGCostModelSwitch.E_SLOPE_COST_MODEL_SWITCH;
        }

        Timber.d("GetRangeOnElecInfoConfig slopeUp = %f, slopeDown = %f, vehicleWeight = %d, costModelSwitch = %d"
                , rangeOnInfo.slopeUp, rangeOnInfo.slopeDown, rangeOnInfo.vehicleWeight, elecConfig.costModelSwitch);

        /**
         *4、auxCost：
         * 1) 必须 >= 0
         */
        if (rangeOnInfo.auxCost >= 0.00001) {
            costList.auxValue = rangeOnInfo.auxCost;
            elecConfig.costModelSwitch |= EGCostModelSwitch.E_AUX_COST_MODEL_SWITCH;
        }

        Timber.d("GetRangeOnElecInfoConfig auxCost = %f, costModelSwitch = %d"
                , rangeOnInfo.auxCost, elecConfig.costModelSwitch);

        /**
         *5、trans：加减速代价模型，能量消耗
         * 1) transAccess和transDecess必须大于0
         * 2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
         * 3) transAccess必须大于transDecess
         */
        if ((rangeOnInfo.transAccess >= 0.00001) & (rangeOnInfo.transDecess >= 0.00001)
                & (rangeOnInfo.vehicleWeight != 0)
                & ((rangeOnInfo.transAccess / rangeOnInfo.vehicleWeight > 72.0) & (rangeOnInfo.transAccess / rangeOnInfo.vehicleWeight < 72000.0))
                & ((rangeOnInfo.transDecess / rangeOnInfo.vehicleWeight > 72.0) & (rangeOnInfo.transDecess / rangeOnInfo.vehicleWeight < 72000.0))
                & (rangeOnInfo.transAccess < rangeOnInfo.transDecess)) {
            costList.trans.access = rangeOnInfo.transAccess;
            costList.trans.decess = rangeOnInfo.transDecess;
            elecConfig.costModelSwitch |= EGCostModelSwitch.E_TRANS_COST_MODEL_SWITCH;
        }

        Timber.d("GetRangeOnElecInfoConfig transAccess = %f, transDecess = %f, vehicleWeight = %d, costModelSwitch = %d"
                , rangeOnInfo.transAccess, rangeOnInfo.transDecess, rangeOnInfo.vehicleWeight, elecConfig.costModelSwitch);

        /**
         *6、ferryRate：
         * 1) 必须 >= 0
         */
        if (rangeOnInfo.ferryrateCost >= 0.00001) {
            costList.ferryRate = rangeOnInfo.ferryrateCost;
            elecConfig.costModelSwitch |= EGCostModelSwitch.E_FERRY_COST_MODEL_SWITCH;
        }

        Timber.d("GetRangeOnElecInfoConfig ferryrateCost = %f, costModelSwitch = %d"
                , rangeOnInfo.ferryrateCost, elecConfig.costModelSwitch);

        //1、如果costmodelswith不为0，则必须包含0x01，即速度代价模型开关.
        if (0 == (elecConfig.costModelSwitch & EGCostModelSwitch.E_SPEED_COST_MODEL_SWITCH)) {
            elecConfig.costModelSwitch = 0;
        }

        Timber.d("GetRangeOnElecInfoConfig final costModelSwitch = %d", elecConfig.costModelSwitch);

        elecConfig.orgaName = elecInfo.brand;
        //elecConfig.driveTrain = 6;
        costList.type = 2;

        if (EGDriveMode.E_RANGE_MAP_REFACTR_ECO == rangeOnInfo.driveMode) {
            elecConfig.fesMode = 3;
            costList.type = 0;
        } else if (EGDriveMode.E_RANGE_MAP_REFACTR_ECO_PLUS == rangeOnInfo.driveMode) {
            elecConfig.fesMode = 4;
            costList.type = 1;
        } else if (EGDriveMode.E_RANGE_MAP_REFACTR_SPORT == rangeOnInfo.driveMode) {
            elecConfig.fesMode = 1;
        } else if (EGDriveMode.E_RANGE_MAP_REFACTR_SPORT_PLUS == rangeOnInfo.driveMode) {
            elecConfig.fesMode = 2;
        } else if (EGDriveMode.E_RANGE_MAP_REFACTR_COMF == rangeOnInfo.driveMode) {
            elecConfig.fesMode = 0;
        } else {
            Timber.d("Unkown drive mode:%d", rangeOnInfo.driveMode);
        }

        elecConfig.fesMode = (short) rangeOnInfo.driveMode;
        elecConfig.hasTraffic = 1;
        elecConfig.costUnit = (short) elecInfo.energyUnit;
        elecConfig.topSpeed = (short) rangeOnInfo.topSpeed;
        elecConfig.vehiclelMass = (short) rangeOnInfo.vehicleWeight;

        if (rangeOnInfo.maxEnergy >= 0) {
            elecConfig.maxVechicleCharge = rangeOnInfo.maxEnergy;
        }

        if (rangeOnInfo.initialHvBattEnergy >= 0) {
            elecConfig.vehicleCharge = rangeOnInfo.initialHvBattEnergy;
        }

        elecConfig.costList.add(costList);

        Timber.d("GetRangeOnElecInfoConfig driveMode = %d, energyUnit = %d, topSpeed = %d, vehicleWeight = %d, maxEnergy = %f, initialHvBattEnergy = %f"
                , rangeOnInfo.driveMode, elecInfo.energyUnit, rangeOnInfo.topSpeed, rangeOnInfo.vehicleWeight, rangeOnInfo.maxEnergy, rangeOnInfo.initialHvBattEnergy);
        return elecConfig;
    }

    public static GRangeSpiderRequestParam getRangeSpiderRequestParam() {
        GRangeSpiderRequestParam pRequest = new GRangeSpiderRequestParam();

        NaviRangeOnInfo rangeInfo = getRangeOnInfo();
        CarElectricInfo electInfo = getCarElectricInfo();
        @ElectricVehicleType int electricVehicleType = getElectricVehicleType();

        pRequest.mTimeOut = 8000;
        pRequest.rangespider.encoder = 0;
        pRequest.rangespider.vers = "1.0";
        pRequest.rangespider.returnvers = "1.0";
        pRequest.rangespider.travel = 0;
        pRequest.rangespider.quality = 3;
        /**< 不限制 */
        pRequest.rangespider.pointslimit = 0;
        pRequest.rangespider.join = 1;

        pRequest.rangespider.strategy.type = 4;
        pRequest.rangespider.strategy.flag = 0x2000;

        GRangeSpiderPoint stPoint = new GRangeSpiderPoint();
        Location location = LocationController.getInstance().getLastLocation();

        stPoint.lon = location.getLongitude();
        stPoint.lat = location.getLatitude();

        pRequest.rangespider.start.pointlist.add(stPoint);

        pRequest.rangespider.vehicle.type = electricVehicleType;
        pRequest.rangespider.vehicle.elec.orga = electInfo.brand;
        //pRequest.rangespider.vehicle.elec.drivetrain = 0;

        if (EGDriveMode.E_RANGE_MAP_REFACTR_ECO == rangeInfo.driveMode) {
            pRequest.rangespider.vehicle.elec.fesmode = 3;
        } else if (EGDriveMode.E_RANGE_MAP_REFACTR_ECO_PLUS == rangeInfo.driveMode) {
            pRequest.rangespider.vehicle.elec.fesmode = 4;
        } else if (EGDriveMode.E_RANGE_MAP_REFACTR_SPORT == rangeInfo.driveMode) {
            pRequest.rangespider.vehicle.elec.fesmode = 1;
        } else if (EGDriveMode.E_RANGE_MAP_REFACTR_SPORT_PLUS == rangeInfo.driveMode) {
            pRequest.rangespider.vehicle.elec.fesmode = 2;
        } else if (EGDriveMode.E_RANGE_MAP_REFACTR_COMF == rangeInfo.driveMode) {
            pRequest.rangespider.vehicle.elec.fesmode = 0;
        } else {
            Timber.d("Unkown drive mode:%d", rangeInfo.driveMode);
        }

        Timber.d("GetRangeSpiderRequestParam rangeInfo.topSpeed:%d, rangeInfo.vehicleWeight:%d,  rangeInfo.maxEnergy:%f, rangeInfo.initialHvBattEnergy:%f,rangeInfo.energyUnit:%d", rangeInfo.topSpeed, rangeInfo.vehicleWeight, rangeInfo.maxEnergy, rangeInfo.initialHvBattEnergy, rangeInfo.energyUnit);

        pRequest.rangespider.vehicle.elec.topspeed = rangeInfo.topSpeed;
        pRequest.rangespider.vehicle.elec.vehiclemass = rangeInfo.vehicleWeight;

        if (rangeInfo.maxEnergy >= 0) {
            pRequest.rangespider.vehicle.elec.maxvehiclecharge = rangeInfo.maxEnergy;
        }

        if (rangeInfo.initialHvBattEnergy >= 0) {
            pRequest.rangespider.vehicle.elec.vehiclecharge = rangeInfo.initialHvBattEnergy;
        }

        pRequest.rangespider.vehicle.elec.costunit = rangeInfo.energyUnit;

        ArrayList<GRangeSpiderVehicleElecCostlistSpeed> speedList = new ArrayList<>();

        for (int i = 0; i < rangeInfo.speedCost.size(); i++) {
            GRangeSpiderVehicleElecCostlistSpeed stSpeed = new GRangeSpiderVehicleElecCostlistSpeed();
            stSpeed.speed = (int) rangeInfo.speedCost.get(i).speed;
            stSpeed.value = rangeInfo.speedCost.get(i).costValue;
            speedList.add(stSpeed);
            pRequest.rangespider.vehicle.elec.costmodelswitch |= 0x1;
        }

        GRangeSpiderVehicleElecCostlist stCostlist = new GRangeSpiderVehicleElecCostlist();
        stCostlist.id = 100;
        stCostlist.type = 0;
        stCostlist.speed = speedList;

        if (rangeInfo.auxCost > 0.0001) {
            stCostlist.aux = rangeInfo.auxCost;
            pRequest.rangespider.vehicle.elec.costmodelswitch |= 0x8;
        }

        for (int i = 0; i < rangeInfo.powertrainLoss.size(); i++) {
            GRangeSpiderVehicleElecCostlistPowertrainloss powertrainloss = new GRangeSpiderVehicleElecCostlistPowertrainloss();
            powertrainloss.powerdemand = rangeInfo.powertrainLoss.get(i).powerdemand;
            powertrainloss.value = rangeInfo.powertrainLoss.get(i).costValue;
            stCostlist.powertrainloss.add(powertrainloss);
            pRequest.rangespider.vehicle.elec.costmodelswitch |= 0x40;
            Timber.d("index = %d, powerdemand = %f, costValue = %f",
                    i, powertrainloss.powerdemand, powertrainloss.value);
        }

        GRangeSpiderVehicleElecCostlistRange elecCostlistRange = new GRangeSpiderVehicleElecCostlistRange();
        elecCostlistRange.energy = rangeInfo.initialHvBattEnergy;
        stCostlist.range.add(elecCostlistRange);

        /** 低电量预警百分比值，需要根据实际车车企 */
        float severeAlertPercent = getAlertEnergyPercent();
        if (severeAlertPercent > 0 & (rangeInfo.percentOfResidualEnergy - severeAlertPercent > 2)) {
            GRangeSpiderVehicleElecCostlistRange elecCostlistRangeAlert = new GRangeSpiderVehicleElecCostlistRange();
            //产品需求：预警阈值：取电量告警配置阈值，默认值为5%；
            elecCostlistRangeAlert.energy = rangeInfo.initialHvBattEnergy * (100 - severeAlertPercent) / 100;
            stCostlist.range.add(elecCostlistRangeAlert);
        }

        Timber.d("costmodelswitch:0x%x", pRequest.rangespider.vehicle.elec.costmodelswitch);
        pRequest.rangespider.vehicle.elec.costlist.add(stCostlist);

        return pRequest;
    }

//    public List<Integer> getOffLineRangeSpider(){
//        List<Integer> list=new ArrayList<>();
//        AdapterCarAllInfo allInfo=BusinessApplicationUtils.getElectricInfo();
//        AdapterCarEnergyInfo carEnergyInfo=allInfo.carEnergyInfo;
//        if (carEnergyInfo!=null){
//            list.add(carEnergyInfo.rangeDist);
//            /** 低电量预警百分比值，需要根据实际车车企 */
//            float severeAlertPercent = getConfigSevereAlertPercent();
//            if (severeAlertPercent > 0 & (carEnergyInfo.percentOfResidualEnergy * 100 - severeAlertPercent > 2)) //根据产品需求： 当前电量百分比-预警阈值＞2% 才能绘制； 当配置项=0时，不绘制预警圈  https://yuque.antfin-inc.com/books/share/a4b51fa9-9767-4bcf-9af7-cd845c2a5ebc/norbme
//            {
//                list.add((int) (carEnergyInfo.rangeDist * (100 - severeAlertPercent) / 100)); //产品需求：预警阈值：取电量告警配置阈值，默认值为5%；
//            }
//        }
//        return list;
//    }


    public static GNavigationEtaqueryReqVehicle getEtaQueryReqVehicle() {
        GNavigationEtaqueryReqVehicle vehicle = new GNavigationEtaqueryReqVehicle();

        @ElectricVehicleType int electricVehicleType = getElectricVehicleType();

        vehicle.type =  Integer.toString(electricVehicleType);

        CarElectricInfo carElectricInfo = getCarElectricInfo();
        vehicle.weight = Integer.toString(carElectricInfo.vehicleWeight);

        NaviRangeOnInfo rangeOnInfo = getRangeOnInfo();

        if (rangeOnInfo.isValid) {
            vehicle.elec.mass = rangeOnInfo.vehicleWeight;
            vehicle.elec.charge = rangeOnInfo.initialHvBattEnergy;
            vehicle.elec.cost_uint = EGEnergyUnit.E_ENERGY_UNIT_KWH;
            vehicle.elec.has_traffic = 1;

            vehicle.elec.top_speed = rangeOnInfo.topSpeed;
            vehicle.elec.fes_mode = rangeOnInfo.driveMode;
            vehicle.elec.orga = carElectricInfo.brand;

            Timber.d("orga " + carElectricInfo.brand);

            GNavigationEtaqueryReqElecConstList stConstList = new GNavigationEtaqueryReqElecConstList();

            stConstList.aux = rangeOnInfo.auxCost;

            //0：eco\1：ecoplus\2：reg
            if (EGDriveMode.E_RANGE_MAP_REFACTR_ECO == rangeOnInfo.driveMode) {
                stConstList.type = 0;
            } else if (EGDriveMode.E_RANGE_MAP_REFACTR_ECO_PLUS == rangeOnInfo.driveMode) {
                stConstList.type = 1;
            } else {
                stConstList.type = 2;
            }

            stConstList.id = 0;

            for (int i = 0; i < rangeOnInfo.speedCost.size(); i++) {
                GNavigationEtaqueryReqElecConstListRangeEnergy stCostEnergy = new GNavigationEtaqueryReqElecConstListRangeEnergy();
                stCostEnergy.speed = Float.toString(rangeOnInfo.speedCost.get(i).speed);
                stCostEnergy.value = Float.toString(rangeOnInfo.speedCost.get(i).costValue);
                stConstList.speed.cost.add(stCostEnergy);

                vehicle.elec.cost_model_switch |= 0x1;
            }

            for (int i = 0; i < rangeOnInfo.powertrainLoss.size(); i++) {
                GNavigationEtaqueryReqElecConstListPowertrainloss powertrainloss = new GNavigationEtaqueryReqElecConstListPowertrainloss();
                powertrainloss.powerdemand = rangeOnInfo.powertrainLoss.get(i).powerdemand;
                powertrainloss.value = rangeOnInfo.powertrainLoss.get(i).costValue;
                stConstList.powertrainloss.add(powertrainloss);
                Timber.d("index = %d, powerdemand = %f, costValue = %f",
                        i, powertrainloss.powerdemand, powertrainloss.value);

                vehicle.elec.cost_model_switch |= 0x40;
            }

            /**
             *2、curve:
             * 1) curveAccess和curveDecess必须大于0
             * 2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
             * 3) curveAccess必须小于curveDecess
             */
            if ((rangeOnInfo.curveAccess >= 0.00001) && (rangeOnInfo.curveDecess >= 0.00001)
                    && (rangeOnInfo.vehicleWeight != 0)
                    && ((rangeOnInfo.curveAccess / rangeOnInfo.vehicleWeight > 72.0) && (rangeOnInfo.curveAccess / rangeOnInfo.vehicleWeight < 72000.0))
                    && ((rangeOnInfo.curveDecess / rangeOnInfo.vehicleWeight > 72.0) && (rangeOnInfo.curveDecess / rangeOnInfo.vehicleWeight < 72000.0))
                    && (rangeOnInfo.curveAccess < rangeOnInfo.curveDecess)) {
                stConstList.curve.access = rangeOnInfo.curveAccess;
                stConstList.curve.decess = rangeOnInfo.curveDecess;

                vehicle.elec.cost_model_switch |= 0x2;
            }
            Timber.d("curveAccess = %f, curveDecess = %f, vehicleWeight = %d, costModelSwitch = 0x%x",
                    rangeOnInfo.curveAccess, rangeOnInfo.curveDecess, rangeOnInfo.vehicleWeight, vehicle.elec.cost_model_switch);

            /**
             *3、slope:
             * 1) slope Up和slope Down必须大于0
             * 2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
             * 3) slope Up必须小于slope Down
             */
            if ((rangeOnInfo.slopeUp >= 0.00001) && (rangeOnInfo.slopeDown >= 0.00001)
                    && (rangeOnInfo.vehicleWeight != 0)
                    && ((rangeOnInfo.slopeUp / rangeOnInfo.vehicleWeight > 72.0) && (rangeOnInfo.slopeUp / rangeOnInfo.vehicleWeight < 72000.0))
                    && ((rangeOnInfo.slopeDown / rangeOnInfo.vehicleWeight > 72.0) && (rangeOnInfo.slopeDown / rangeOnInfo.vehicleWeight < 72000.0))
                    && (rangeOnInfo.slopeUp < rangeOnInfo.slopeDown)) {
                stConstList.slope.down = Float.toString(rangeOnInfo.slopeDown);
                stConstList.slope.up = Float.toString(rangeOnInfo.slopeUp);

                vehicle.elec.cost_model_switch |= 0x4;
            }
            Timber.d("slopeUp = %f, slopeDown = %f, vehicleWeight = %d, costModelSwitch = 0x%x"
                    , rangeOnInfo.slopeUp, rangeOnInfo.slopeDown, rangeOnInfo.vehicleWeight, vehicle.elec.cost_model_switch);

            /**
             *4、auxCost：
             * 1) 必须 >= 0
             */
            if (rangeOnInfo.auxCost >= 0.00001) {
                stConstList.aux = rangeOnInfo.auxCost;
                vehicle.elec.cost_model_switch |= 0x8;
            }
            Timber.d("auxCost = %f, costModelSwitch = 0x%x"
                    , rangeOnInfo.auxCost, vehicle.elec.cost_model_switch);

            /**
             *5、trans：
             * 1) transAccess和transDecess必须大于0
             * 2) 与vehicleMass相除的值必须在72.0到72000.0之间，开区间
             * 3) transAccess必须小于transDecess
             */
            if ((rangeOnInfo.transAccess >= 0.00001) && (rangeOnInfo.transDecess >= 0.00001)
                    && (rangeOnInfo.vehicleWeight != 0)
                    && ((rangeOnInfo.transAccess / rangeOnInfo.vehicleWeight > 72.0) && (rangeOnInfo.transAccess / rangeOnInfo.vehicleWeight < 72000.0))
                    && ((rangeOnInfo.transDecess / rangeOnInfo.vehicleWeight > 72.0) && (rangeOnInfo.transDecess / rangeOnInfo.vehicleWeight < 72000.0))
                    && (rangeOnInfo.transAccess < rangeOnInfo.transDecess)) {
                stConstList.trans.access = rangeOnInfo.transAccess;
                stConstList.trans.decess = rangeOnInfo.transDecess;

                vehicle.elec.cost_model_switch |= 0x10;
            }
            Timber.d("transAccess = %f, transDecess = %f, vehicleWeight = %d, costModelSwitch = 0x%x"
                    , rangeOnInfo.transAccess, rangeOnInfo.transDecess, rangeOnInfo.vehicleWeight, vehicle.elec.cost_model_switch);
            /**
             *6、ferryRate：
             * 1) 必须 >= 0
             */
            if (rangeOnInfo.ferryrateCost >= 0.00001) {
                stConstList.ferry_rate = rangeOnInfo.ferryrateCost;
                vehicle.elec.cost_model_switch |= 0x20;
            }

            //1、如果costmodelswith不为0，则必须包含0x01.
            if (0 == (vehicle.elec.cost_model_switch & 0x01)) {
                vehicle.elec.cost_model_switch = 0;
            }

            Timber.d("final costModelSwitch = 0x%x", vehicle.elec.cost_model_switch);
            vehicle.elec.cost_list.add(stConstList);
        }

        return vehicle;
    }

    public static GNavigationEtaqueryRequestParam getNavigationEtaQueryRequestParam(GNavigationEtaqueryReqStartEnd start, GNavigationEtaqueryReqStartEnd end, GNavigationEtaqueryReqStartEnd via) {
        if ((0 >= start.points.size()) || (0 >= end.points.size())) {
            return null;
        }

        GNavigationEtaqueryRequestParam requestParam = new GNavigationEtaqueryRequestParam();

        if (1 < start.points.size()) {
            requestParam.OneToN = "2";
        } else if (1 < end.points.size()) {
            requestParam.OneToN = "1";
        } else {
            requestParam.OneToN = "0";
        }

        //GetEtaqueryReqRoute(requestParam.route);
        requestParam.vehicle = getEtaQueryReqVehicle();
        //GetEtaqueryReqClient(requestParam.client);
        requestParam.end = end;
        requestParam.start = start;
        requestParam.via = via;
        requestParam.mTimeOut = 3000;

        return requestParam;
    }

    private static @PowerType int getPowerType() {
        AdapterCarAllInfo allInfo = BusinessApplicationUtils.getElectricInfo();
        if (allInfo.carInfo != null) {
            return allInfo.carInfo.energyPowerType;
        }
        return PowerType.E_VEHICLE_ENERGY_DEFAULT;
    }

    private static @ElectricVehicleType int getElectricVehicleType() {
        AdapterCarAllInfo allInfo = BusinessApplicationUtils.getElectricInfo();
        if (allInfo.carInfo != null) {
            return allInfo.carInfo.electricVehicleType;
        }
        return ElectricVehicleType.E_BUS;
    }

    private static CarElectricInfo getCarElectricInfo() {
        CarElectricInfo carElectricInfo = new CarElectricInfo();
        AdapterCarAllInfo allInfo = BusinessApplicationUtils.getElectricInfo();
        AdapterCarInfo carInfo = allInfo.carInfo;
        if (carInfo != null) {
            carElectricInfo.brand = carInfo.brand;
            carElectricInfo.enginNo = carInfo.engineNo;

            carElectricInfo.model = carInfo.model;
            carElectricInfo.vehicleWeight = (int) carInfo.vehicleWeight;
            carElectricInfo.vin = carInfo.vin;
            carElectricInfo.maxEnergy = carInfo.maxBattEnergy;
            carElectricInfo.energyUnit = carInfo.energyUnit;

        }
        return carElectricInfo;
    }

    private static NaviRangeOnInfo getRangeOnInfo() {
        NaviRangeOnInfo rangeOnInfo = new NaviRangeOnInfo();
        AdapterCarAllInfo allInfo = BusinessApplicationUtils.getElectricInfo();
        AdapterCarEnergyInfo carEnergyInfo = allInfo.carEnergyInfo;
        if (carEnergyInfo != null) {
            rangeOnInfo.isValid = true;
            rangeOnInfo.initialHvBattEnergy = (float) carEnergyInfo.initIalHvBattEnergy;
            rangeOnInfo.lowEnergyAlert = carEnergyInfo.lowEnergyAlert;
            rangeOnInfo.rangeDis = carEnergyInfo.rangeDist;
            rangeOnInfo.inCharge = carEnergyInfo.isCharge;
            rangeOnInfo.driveMode = carEnergyInfo.curDriveMode;
            rangeOnInfo.energyUnit = carEnergyInfo.energyUnit;
            rangeOnInfo.topSpeed = carEnergyInfo.topSpeed;
            rangeOnInfo.maxEnergy = (float) carEnergyInfo.maxBattEnergy;
            rangeOnInfo.vehicleWeight = carEnergyInfo.vehicleWeight;
            rangeOnInfo.auxCost = (float) carEnergyInfo.auxCost;
            rangeOnInfo.ferryrateCost = (float) carEnergyInfo.ferryRateCost;

            if (carEnergyInfo.slopeCostList != null && carEnergyInfo.slopeCostList.length >= 2) {
                rangeOnInfo.slopeUp = (float) carEnergyInfo.slopeCostList[0];
                rangeOnInfo.slopeDown = (float) carEnergyInfo.slopeCostList[1];
            }
            if (carEnergyInfo.tansCostList != null && carEnergyInfo.tansCostList.length >= 2) {
                rangeOnInfo.transAccess = (float) carEnergyInfo.tansCostList[0];
                rangeOnInfo.transDecess = (float) carEnergyInfo.tansCostList[1];
            }
            if (carEnergyInfo.curveCostList != null && carEnergyInfo.curveCostList.length >= 2) {
                rangeOnInfo.curveAccess = (float) carEnergyInfo.curveCostList[0];
                rangeOnInfo.curveDecess = (float) carEnergyInfo.curveCostList[1];
            }

            ArrayList<EnergySpeedCost> speedCostList = new ArrayList<>();
            if (carEnergyInfo.speedCostList != null && carEnergyInfo.speedCostList.length >= 2) {
                for (int i = 0; i < carEnergyInfo.speedCostList.length; i += 2) {
                    EnergySpeedCost speedCost = new EnergySpeedCost();
                    speedCost.speed = (float) carEnergyInfo.speedCostList[i];
                    speedCost.costValue = (float) carEnergyInfo.speedCostList[i + 1];
                    speedCostList.add(speedCost);
                }
            }
            ArrayList<STPowertrainLoss> powerTrainLoss = new ArrayList<>();
            if (carEnergyInfo.powerTrainLoss != null && carEnergyInfo.powerTrainLoss.length >= 2) {
                for (int i = 0; i < carEnergyInfo.powerTrainLoss.length; i += 2) {
                    STPowertrainLoss powertrainLoss = new STPowertrainLoss();
                    powertrainLoss.powerdemand = (float) carEnergyInfo.powerTrainLoss[i];
                    powertrainLoss.costValue = (float) carEnergyInfo.powerTrainLoss[i + 1];
                    powerTrainLoss.add(powertrainLoss);
                }
            }
            rangeOnInfo.speedCost = speedCostList;
            rangeOnInfo.powertrainLoss = powerTrainLoss;
            rangeOnInfo.percentOfResidualEnergy = (float) carEnergyInfo.percentOfResidualEnergy;
        }
        return rangeOnInfo;
    }

    public static long getRangeOnMapTime() {
        long rangeOnMapTime = 15 * 60 * 1000L;
        // to do 从设置项配置中获取
        return rangeOnMapTime;
    }

    public static float getAlertEnergyPercent() {
        float alertEnergyPercent = 5;
        // todo 从设置项配置中获取
        return alertEnergyPercent;
    }

    public static float getLowEnergyPercent() {
        float lowEnergyPercent = 20;
        // todo 从设置项配置中获取
        return lowEnergyPercent;
    }

    public static int getOffLineRangeSpider() {
        AdapterCarAllInfo allInfo = BusinessApplicationUtils.getElectricInfo();
        if (allInfo.carEnergyInfo != null) {
            return allInfo.carEnergyInfo.rangeDist;
        }
        return 0;
    }


    public static int getEnergyLeftPercentage(ElecPathInfo elecPathInfo) {
        //以返回值计算百分比单位0.01wh
        ElecInfoConfig config = getElecInfoConfig();
        ArrayList<Integer> vehiclechargeleft = elecPathInfo.mEnergyConsume.vehiclechargeleft;
        //获取最后一个值
        int left = vehiclechargeleft.get(vehiclechargeleft.size() - 1);
        BigDecimal b = new BigDecimal(left / (config.maxVechicleCharge * 100000));
        float result = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        return (int) (result * 100);
    }


    /**
     * 在线算路 电量路线是否包含耗尽点
     *
     * @param
     * @return
     */
    public static boolean onLineHasElectricExhausted(ElecPathInfo elecPathInfo) {
        return elecPathInfo != null && elecPathInfo.mEnergyConsume.energyEndFlag;
    }

    public static boolean isElectricRoute(ElecPathInfo elecPathInfo) {
        return elecPathInfo != null && elecPathInfo.mIsElecRoute;
    }


    /**
     * 离线算路 电量路线是否包含耗尽点
     *
     * @param distance 路线距离
     * @return
     */
    public static boolean offLineHasElectricExhausted(long distance) {
        NaviRangeOnInfo range = getRangeOnInfo();
        return (long) range.rangeDis < distance / 1000;
    }

    public static boolean isElectric() {
        ElecInfoConfig elecInfoConfig = getElecInfoConfig();
        int powerType = getPowerType();
        Timber.i("isElectric powerType = %d", powerType);
        return elecInfoConfig != null && powerType == PowerType.E_VEHICLE_ENERGY_ELECTRIC;
    }

    public static boolean isShowElectricTipView(RouteCarResultData routeCarResultData) {
        AdapterCarEnergyInfo carEnergyInfo = BusinessApplicationUtils.getElectricInfo().carEnergyInfo;
        //电车类型且有电量信息
        if (isElectric() && carEnergyInfo != null) {
            //充电中不显示弹条
            if (carEnergyInfo.isCharge) {
                return false;
            }
            PathInfo pathInfo = routeCarResultData.getPathResult().get(routeCarResultData.getFocusIndex());
            //离线场景下，对比里程
            if (routeCarResultData.isOffline() && offLineHasElectricExhausted(pathInfo.getLength())) {
                return true;
            }
            //在线场景下，对比续航
            if (!routeCarResultData.isOffline() && isShowOnLineElectricExhaustedTipView(routeCarResultData)) {
                return true;
            }
            if (routeCarResultData.getRouteConstrainCode() == RouteConstrainCode.RouteElecContinue && pathInfo.getChargeStationInfo() != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean isShowOnLineElectricExhaustedTipView(RouteCarResultData resultData) {
        PathInfo pathInfo = resultData.getPathResult().get(resultData.getFocusIndex());
        ElecPathInfo elecPathInfo = pathInfo.getElecPathInfo();
        if (onLineHasElectricExhausted(elecPathInfo)) {
            boolean isMidPOIChargeType = false;
            if (CHARGE_STATION_TYPE.equals(resultData.getToPOI().getTypeCode())) {
                return false;
            }
            ArrayList<POI> midPois = resultData.getMidPois();
            if (midPois != null && midPois.size() > 0 && midPois.size() == elecPathInfo.mEnergyConsume.vehiclechargeleft.size() - 1) {
                for (int i = 0; i < midPois.size(); i++) {
                    POI poi = midPois.get(i);
                    if (CHARGE_STATION_TYPE.equals(poi.getTypeCode()) && elecPathInfo.mEnergyConsume.vehiclechargeleft.get(0) != -1) {
                        isMidPOIChargeType = true;
                        break;
                    }
                }
            }
            return !isMidPOIChargeType;
        }
        return false;
    }

    public static ElecInfoConfig setDefaultRouteContinueSetting(ElecInfoConfig elecInfoConfig) {
        //设置到达充电站最大充电百分比
        elecInfoConfig.arrivingPercent = 80;
        //到达充电站最小电量百分比
        elecInfoConfig.leavingPercent = 20;
        //能量单位 kwh
        elecInfoConfig.costUnit = 1;
        //eta包含充电时间+下发充电站充电时间
        elecInfoConfig.powerflag = ElectricInfoConverter.EtaPowerFlag.ETA_CHARGE_TIME_IN_OUT;
        return elecInfoConfig;
    }

}
