package com.autosdk.adapter;

import android.content.Context;
import android.content.Intent;


public class SendAdapterCarInfoUtil {
    /**
     * 请求车辆信息
     *
     * @param context
     * @param type
     */
    public static void requestInfoByType(Context context, @AdapterConstants.RequestCarInfoType int type) {
        Intent intent = getRequestIntent(type);
        context.sendBroadcast(intent);
    }

    /**
     * 测试使用方法
     *
     * @param context
     * @param type
     */
    public static void sendInfoByType(Context context, @AdapterConstants.CarInfoType int type) {
        Intent intent = null;
        switch (type) {
            case AdapterConstants.EXTRA_AUTO_CAR_INFO_KEY_VALUE:
                intent = getCarInfoIntent(type);
                break;
            case AdapterConstants.EXTRA_AUTO_CAR_ENERGY_INFO_KEY_VALUE:
                intent = getCarEnergyIntent(type);
                break;
            case AdapterConstants.EXTRA_AUTO_CAR_CHARGE_STATUS_KEY_VALUE:
                intent = new Intent();
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", type);
                intent.putExtra("CHARGE_STATE", 101);//充电状态通知（int）（100-开始充电；101-退出充电）
                intent.putExtra("CHARGE_TYPE", 1);
                break;
            case AdapterConstants.EXTRA_AUTO_CAR_DRIVE_MODEL_KEY_VALUE:
                intent = new Intent();
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", type);
                intent.putExtra("DRIVEMODE_STATE", 4);//驾驶模式。0-节能模式；1-节能plus模式；2-运动模式；3-运动plus模式；4-舒适模式
                break;
            case AdapterConstants.EXTRA_AUTO_CAR_ADDITION_KEY_VALUE:
                intent = new Intent();
                intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                intent.putExtra("KEY_TYPE", type);
                intent.putExtra("ADDITION_COMSUP", 1);//ADDITION_COMSUP: 附加能耗操作类型（int）
                intent.putExtra("ENERGY_UNIT", 1);//ENERGY_UNIT: 能量单位（int），默认kwh。
                intent.putExtra("Cost_LoadPwrConsump", 2);//Cost_LoadPwrConsump: 附加操作的能量消耗值（int）
                break;
            default:
                break;

        }
        if (intent != null) {
            context.sendBroadcast(intent);
        }
    }

    private static Intent getRequestIntent(int type) {
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_SEND");
        intent.putExtra("KEY_TYPE", type);
        return intent;
    }

    private static Intent getCarInfoIntent(int type) {
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", type);
        intent.putExtra("MAX_BATT_ENERGY", "35.0");
        intent.putExtra("VEHICLE_WEIGHT", 1440);
        intent.putExtra("EXTRA_VIN", "车架号");
        intent.putExtra("EXTRA_ENGINENO", "发动机号");
        intent.putExtra("EXTRA_BRAND", "品牌");
        intent.putExtra("EXTRA_MODEL", "车辆型号");
        intent.putExtra("ENERGY_UNIT", 1);
        intent.putExtra("ENERGY_POWER_TYPE", 1);
        return intent;
    }

    private static Intent getCarEnergyIntent(int type) {
        Intent intent = new Intent();
        intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        intent.putExtra("KEY_TYPE", type);
        intent.putExtra("INITIAL_HV_BATTENERGY", "20");
        intent.putExtra("LOW_ENERGY_ALERT", 0);
        intent.putExtra("LOW_ENERGY_WARN", 0);
        intent.putExtra("RANGE_DIST", 450);
        intent.putExtra("IS_CHARGE", false);
        intent.putExtra("CUR_DRIVE_MODE", 4);
        intent.putExtra("ENERGY_UNIT", 1);
        intent.putExtra("TOP_SPEED", 150);
        intent.putExtra("MAX_BATT_ENERGY", "35.0");
        intent.putExtra("VEHICLE_WEIGHT", 1440);
//        access  decess
        String[] tansCostList = {"1200000","2400000"};
        intent.putExtra("TRANS_COSTLIST",tansCostList);
        String[] curveCostList = {"1200000","2400000"};
        intent.putExtra("CURVE_COSTLIST",curveCostList);
        String[] slopeCostList = {"1200000","2400000"};
        intent.putExtra("SLOPE_COSTLIST", slopeCostList);

        String[] speedCostList = {"7", "23.0", "120", "92.5"};
        intent.putExtra("SPEED_COSTLIST", speedCostList);

        String[] powerCostList = {"7", "23.5", "15", "30"};
        intent.putExtra("POWERTRAIN_LOSS", powerCostList);

        intent.putExtra("AUX_COST", "0.5");
        intent.putExtra("FERRYRATE_COST", "0.8");
        intent.putExtra("PERCENT_OF_RESIDUAL_ENERGY", "30");

        return intent;
    }
}
