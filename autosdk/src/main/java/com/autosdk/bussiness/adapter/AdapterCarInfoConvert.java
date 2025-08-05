package com.autosdk.bussiness.adapter;

import android.content.Intent;

import com.autosdk.bussiness.adapter.bean.AdapterCarAdditionInfo;
import com.autosdk.bussiness.adapter.bean.AdapterCarChargeInfo;
import com.autosdk.bussiness.adapter.bean.AdapterCarEnergyInfo;
import com.autosdk.bussiness.adapter.bean.AdapterCarInfo;

/**
 * @author AutoSDk
 */
public class AdapterCarInfoConvert {
    public static AdapterCarAdditionInfo convertCarAdditionInfo(Intent intent) {
        AdapterCarAdditionInfo additionInfo = new AdapterCarAdditionInfo();
        //ADDITION_COMSUP: 附加能耗操作类型（int）
        additionInfo.comsup = intent.getIntExtra("ADDITION_COMSUP", 0);
        //ENERGY_UNIT: 能量单位（int），默认kwh。
        additionInfo.energyUtil = intent.getIntExtra("ENERGY_UNIT", 0);
        //Cost_LoadPwrConsump: 附加操作的能量消耗值（int）
        additionInfo.costLoadPwrConsump = intent.getIntExtra("Cost_LoadPwrConsump", 0);
        return additionInfo;
    }

    public static int convertCarDriveModelInfo(Intent intent) {
        return intent.getIntExtra("DRIVEMODE_STATE", -1);
    }

    public static AdapterCarChargeInfo convertCarChangeInfo(Intent intent) {
        AdapterCarChargeInfo carChargeInfo = new AdapterCarChargeInfo();
        //充电状态通知（int）（100-开始充电；101-退出充电）
        carChargeInfo.chargeStatus = intent.getIntExtra("CHARGE_STATE", 0);
        //充电类型（int）（1：直流电 2：交流电 3：其他充电类型）
        carChargeInfo.changeType = intent.getIntExtra("CHARGE_TYPE", 0);
        if (carChargeInfo.chargeStatus == 100) {
            carChargeInfo.isCharge = true;
        }
        return carChargeInfo;
    }

    public static AdapterCarEnergyInfo convertCarEnergyInfo(Intent intent) {
        AdapterCarEnergyInfo carInfo = new AdapterCarEnergyInfo();
        //double 当前电量，单位默认kwh
        carInfo.initIalHvBattEnergy = Double.parseDouble(intent.getStringExtra("INITIAL_HV_BATTENERGY"));
        //int 电量预警信号
        carInfo.lowEnergyAlert = intent.getIntExtra("LOW_ENERGY_ALERT", 0);
        //int 电量告警信号
        carInfo.lowEnergyWarn = intent.getIntExtra("LOW_ENERGY_WARN", 0);
        //int 剩余可行驶里程（巡航半径），单位km（如走离线逻辑，则此项为必填）
        carInfo.rangeDist = intent.getIntExtra("RANGE_DIST", 0);
        //boolean 是否处于充电状态
        carInfo.isCharge = intent.getBooleanExtra("IS_CHARGE", false);
        //int 当前驾驶模式，0：节能模式 1：节能plus模式 2：运动模式 3：运动plus模式 4：舒适模式 5：雪地模式 6：预留模式1 7：预留模式2 8：预留模式3
        carInfo.curDriveMode = intent.getIntExtra("CUR_DRIVE_MODE", -1);
        //int 能量单位，默认传1
        carInfo.energyUnit = intent.getIntExtra("ENERGY_UNIT", 1);
        //int 最大限速，默认120
        carInfo.topSpeed = intent.getIntExtra("TOP_SPEED", 120);
        //double 电池最大负载电量
        carInfo.maxBattEnergy = Double.parseDouble(intent.getStringExtra("MAX_BATT_ENERGY"));
        //int 车重，单位KG
        carInfo.vehicleWeight = intent.getIntExtra("VEHICLE_WEIGHT", 0);
        //DoubleArray 坡度消耗权值
        carInfo.slopeCostList = convertStringToDoubleArray(intent.getStringArrayExtra("SLOPE_COSTLIST"));
        //DoubleArray 速度相关权值，可以重复传多组（每奇偶为一组 如 1（速度）、2（消耗）、3（速度）、4（消耗） 其中1、2为第一组中的速度、消耗， 3、4为第2组的速度、消耗以此类推）
        carInfo.speedCostList = convertStringToDoubleArray(intent.getStringArrayExtra("SPEED_COSTLIST"));
        //DoubleArray 转向消耗权值数组
        carInfo.tansCostList = convertStringToDoubleArray(intent.getStringArrayExtra("TRANS_COSTLIST"));
        //DoubleArray 曲率消耗权值
        carInfo.curveCostList = convertStringToDoubleArray(intent.getStringArrayExtra("CURVE_COSTLIST"));
        //double 附加消耗系数
        carInfo.auxCost = Double.parseDouble(intent.getStringExtra("AUX_COST"));
        //double 轮渡消耗（车辆静止场景下的消耗）
        carInfo.ferryRateCost = Double.parseDouble(intent.getStringExtra("FERRYRATE_COST"));
        //double 剩余电量百分比（3.2.9版本支持，4.3.0及以上版本支持）
        carInfo.percentOfResidualEnergy = Double.parseDouble(intent.getStringExtra("PERCENT_OF_RESIDUAL_ENERGY"));
        //DoubleArray 动力总成消耗,可以重复传多组(powerdemand1,costValue1;powerdemand2,costValue2;)
        carInfo.powerTrainLoss = convertStringToDoubleArray(intent.getStringArrayExtra("POWERTRAIN_LOSS"));
        return carInfo;
    }

    public static double[] convertStringToDoubleArray(String[] strings) {
        if (strings == null) {
            return null;
        }
        double[] doubles = new double[strings.length];
        for (int i = 0; i < strings.length; i++) {
            doubles[i] = Double.parseDouble(strings[i]);
        }
        return doubles;

    }

    public static AdapterCarInfo convertCarInfo(Intent intent) {
        AdapterCarInfo carInfo = new AdapterCarInfo();
        //电池最大负载电量
        carInfo.maxBattEnergy = Double.parseDouble(intent.getStringExtra("MAX_BATT_ENERGY"));
        //int，车重，单位kg
        carInfo.vehicleWeight = intent.getIntExtra("VEHICLE_WEIGHT", 0);
        // 车架号
        carInfo.vin = intent.getStringExtra("EXTRA_VIN");
        //发动机号
        carInfo.engineNo = intent.getStringExtra("EXTRA_ENGINENO");
        // 品牌
        carInfo.brand = intent.getStringExtra("EXTRA_BRAND");
        // 车辆型号
        carInfo.model = intent.getStringExtra("EXTRA_MODEL");
        // 能量单位，默认传1
        carInfo.energyUnit = intent.getIntExtra("ENERGY_UNIT", 1);
        //动力类型（-1：默认不选则类型0：燃油车1：电动车2：插电混动）4.3.0及以上版本支持
        carInfo.energyPowerType = intent.getIntExtra("ENERGY_POWER_TYPE", -1);
        return carInfo;
    }

}
