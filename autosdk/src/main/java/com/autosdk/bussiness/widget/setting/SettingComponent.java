package com.autosdk.bussiness.widget.setting;

import static com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_DELETE;
import static com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_PAUSE;
import static com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_START;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_CHECKING;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_ERR;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_READY;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_UNZIPPED;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_UNZIPPING;
import static com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING;

import android.content.Context;
import android.widget.Toast;

import com.autonavi.gbl.data.model.DownLoadMode;
import com.autonavi.gbl.data.model.OperationType;
import com.autonavi.gbl.data.model.Theme;
import com.autonavi.gbl.user.behavior.model.ConfigKey;
import com.autonavi.gbl.user.behavior.model.ConfigValue;
import com.autonavi.gbl.user.syncsdk.model.SyncMode;
import com.autosdk.bussiness.account.BehaviorController;
import com.autosdk.bussiness.data.ThemeDataController;
import com.autosdk.common.CommonConfigValue;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by AutoSdk on 2021/7/20.
 **/
public class SettingComponent {
    private static class SettingComponentHolder {
        private static SettingComponent INSTANCE = new SettingComponent();
    }
    public static SettingComponent getInstance() {
        return SettingComponentHolder.INSTANCE;
    }

//    /**
//     * 日夜模式 日夜模式。 16：自动模式，默认态； 17：日间模式； 18：夜间模式
//     * @param intValue
//     */
//    public void setConfigKeyDayNightMode(int intValue) {
//        ConfigValue castSimple = new ConfigValue();
//        castSimple.intValue = intValue;
//        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyDayNightMode, castSimple, SyncMode.SyncModeNow);
//    }
//
//    /**
//     * 获取日夜模式
//     */
//    public int getConfigKeyDayNightMode() {
//        ConfigValue configValue= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyDayNightMode);
//        if(configValue == null || configValue.intValue == 0){
//            //默认值
//            return SettingConst.MODE_DEFAULT;
//        }
//        int intValue=configValue.intValue;
//        return intValue;
//    }

    /**
     * 个性化车标
     * 0 默认 1汽车 2飞船 3车速
     */
    public void setCarLogos(int intValue) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = intValue;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyCarID,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 获取个性化车标
     * 0 默认 1汽车 2飞船 3车速
     */
    public int getCarLogos() {
        ConfigValue configKeyCarID= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyCarID);
        if(configKeyCarID==null){
            return 0;
        }
        return configKeyCarID.intValue;
    }

    /**
     * 保存车牌号
     * @param carNumber
     */
    public void setConfigKeyPlateNumber(String carNumber) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.strValue = carNumber;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyPlateNumber,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 获取车辆基本信息
     * 车牌，限行，动力类型
     * @return
     */
    public JsonObject getBaseConfig(){
        JsonObject settingJsonObject = new JsonObject();
        settingJsonObject.addProperty(String.valueOf(ConfigKey.ConfigKeyPlateNumber), getConfigKeyPlateNumber());
        settingJsonObject.addProperty(String.valueOf(ConfigKey.ConfigKeyAvoidLimit), getConfigKeyAvoidLimit());
        settingJsonObject.addProperty(String.valueOf(ConfigKey.ConfigKeyPowerType), getConfigKeyPowerType());
        return settingJsonObject;
    }

    /**
     * 获取车牌号
     */
    public String getConfigKeyPlateNumber() {
        //车牌号
        ConfigValue configKeyPlateNumber= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyPlateNumber);
        if(configKeyPlateNumber==null){
            return null;
        }
        String currentCarNumber=configKeyPlateNumber.strValue;
        return  currentCarNumber;
    }

    /**
     * 避开限行 0关闭 1打开
     * @param intValue
     */
    public void setConfigKeyAvoidLimit(int intValue) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = intValue;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyAvoidLimit,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 获取避开限行
     */
    public int getConfigKeyAvoidLimit() {
        ConfigValue configKeyAvoidLimit= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyAvoidLimit);
        if(configKeyAvoidLimit==null){
            return 0;
        }
        int currentCarNumber=configKeyAvoidLimit.intValue;
        return  currentCarNumber;
    }

    /**
     * 动力类型 -1: 无,未设置车牌号默认值 0:燃油车,已设置车牌号默认值 1:纯电动 2:插电式混动
     * @param intValue
     */
    public void setConfigKeyPowerType(int intValue) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = intValue;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyPowerType,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 获取动力类型
     */
    public int getConfigKeyPowerType() {
        //动力类型
        ConfigValue configKeyPowerType= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyPowerType);
        if(configKeyPowerType==null){
            return -1;
        }
        int intValueConfigKeyPower = configKeyPowerType.intValue;
        return  intValueConfigKeyPower;
    }

    /**
     * 获取车辆类型，发起算路及导航需要设置车辆类型，TODO：当前默认只有小车，货车摩托需要额外补充
     */
    public int getConfigVehicleType() {
        // 需要将setting里面配置转为sdk需要VehicleType
        // HMI定义：未设置车牌号默认值 0:燃油车,已设置车牌号默认值 1:纯电动 2:插电式混动
        // sdk定义：默认0，0:小车，1:货车, 2:纯电动车，3:纯电动货车，4:插电式混动汽车，5:插电式混动货车, 11:摩托车
        int type = SettingComponent.getInstance().getConfigKeyPowerType();
        if (type == 1 || type == 2) {
            return  2;
        }
        return 0;
    }

    /**
     * 路线偏好  默认0：高德推荐，字符类型,不包含2|4|8|16|32|64即为高德推荐 默认态； 2：躲避拥堵； 4：避免收费； 8：不走高速； 16：高速优先 32：速度最快  64：大路优先
     * @param planPrefString
     */
    public void setConfigKeyPlanPref(String planPrefString) {
        ConfigValue castSimple = new ConfigValue();
        //bl要求只能传str类似
        castSimple.strValue = planPrefString;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyPlanPref,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 获取路线偏好
     */
    public String getConfigKeyPlanPref() {
        ConfigValue mConfigValue = BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyPlanPref);
        if(mConfigValue==null || "".equals(mConfigValue.strValue)){
            return "0";
        }
        String strValue=  mConfigValue.strValue;
        return strValue != null ? strValue : "0";
    }

    /**
     * 导航播报模式  播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
     * @param intValue
     */
    public void setConfigKeyBroadcastMode(int intValue) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = intValue;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyBroadcastMode,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 获取导航播报模式 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
     */
    public int getConfigKeyBroadcastMode() {
        ConfigValue configKeyBroadcastMode= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyBroadcastMode);
        if(configKeyBroadcastMode==null){
            return SettingConst.BROADCAST_DETAIL;
        }
        return configKeyBroadcastMode.intValue == 0?SettingConst.BROADCAST_DETAIL:configKeyBroadcastMode.intValue;
    }

    /**
     * 导航静音模式  静音 1.静音 0.非静音
     * @param intValue
     */
    public void setConfigKeyMute(int intValue) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = intValue;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyMute,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 导航静音模式 静音 1.静音 0.非静音
     */
    public int getConfigKeyMute() {
        ConfigValue configKeyBroadcastMode= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyMute);
        if(configKeyBroadcastMode == null){
            return 0;
        }
        return configKeyBroadcastMode.intValue;
    }

    /**
     * 巡航播报前方路况  0：off； 1：on
     * @param intValue
     */
    public void setConfigKeyRoadWarn(int intValue) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = intValue;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyRoadWarn,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 获取巡航播报前方路况  0：off； 1：on
     */
    public int getConfigKeyRoadWarn() {
        ConfigValue configKeyRoadWarn= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyRoadWarn);
        if(configKeyRoadWarn==null){
            return 1;
        }
        return configKeyRoadWarn.intValue;
    }
    /**
     * 巡航播报电子眼播报  0：off； 1：on
     * @param intValue
     */
    public void setConfigKeySafeBroadcast(int intValue) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = intValue;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeySafeBroadcast ,castSimple, SyncMode.SyncModeNow);
    }

    /**
     * 获取巡航播报电子眼播报  0：off； 1：on
     */
    public int getConfigKeySafeBroadcast() {
        ConfigValue configKeySafeBroadcast= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeySafeBroadcast);
        if(configKeySafeBroadcast==null){
            return 1;
        }
        return configKeySafeBroadcast.intValue;
    }
    /**
     * 巡航播报安全提醒  0：off； 1：on
     * @param intValue
     */
    public void setConfigKeyDriveWarn(int intValue) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = intValue;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyDriveWarn,castSimple, SyncMode.SyncModeNow);
    }
    /**
     * 获取巡航播报安全提醒  0：off； 1：on
     */
    public int getConfigKeyDriveWarn() {
        ConfigValue configKeyDriveWarn= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyDriveWarn);
        if(configKeyDriveWarn==null){
            return 1;
        }
        return configKeyDriveWarn.intValue;
    }

    /**
     * 获取路况事件  1开 0 关
     */
    public int getConfigKeyRoadEvent() {
        ConfigValue configKeyRoadEvent= BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyRoadEvent);
        if(configKeyRoadEvent==null){
            return 1;
        }
        return configKeyRoadEvent.intValue;
    }

    /**
     * 设置路况事件  1开 0 关
     */
    public void setConfigKeyRoadEvent(int value) {
        ConfigValue configValue = new ConfigValue();
        configValue.intValue = value;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyRoadEvent, configValue, SyncMode.SyncModeNow);
    }
    /**
     * 保存地图模式
     */
    public void setConfigKeyMapviewMode(int value) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = value;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyMapviewMode, castSimple, SyncMode.SyncModeNow);
    }
    /**
     * 获取地图模式
     */
    public int getConfigKeyMapviewMode() {
        ConfigValue configMapviewModeValue = BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyMapviewMode);
        if(configMapviewModeValue==null){
            return CommonConfigValue.VISUALMODE_2D_CAR;//默认2D车头向上
        }
        if(configMapviewModeValue.intValue == 0){
            ConfigValue castSimple = new ConfigValue();
            castSimple.intValue = CommonConfigValue.VISUALMODE_2D_CAR;
            BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyMapviewMode, castSimple, SyncMode.SyncModeNow);
            return CommonConfigValue.VISUALMODE_2D_CAR;//默认2D车头向上
        }
        return configMapviewModeValue.intValue;
    }

    /**
     * 保存收藏点 1.显示 0.隐藏
     */
    public void setConfigKeyMyFavorite(int value) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = value;
        BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyMyFavorite, castSimple, SyncMode.SyncModeNow);
    }
    /**
     * 获取收藏点 1.显示 0.隐藏
     */
    public int getConfigKeyMyFavorite() {
        ConfigValue configKeyMyFavorite = BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyMyFavorite);
        if(configKeyMyFavorite == null){
            Timber.i("getConfigKeyMyFavorite configKeyMyFavorite:1");
            return 1;
        }
        return configKeyMyFavorite.intValue;
    }


    /**
     * 下载主题
     * @param currentTheme 当前主题数据
     * @param currentThemeId 当前主题id
     * @param context 上下文
     */
    public void themeDownLoadItem(Theme currentTheme, int currentThemeId, Context context) {
        int operationType = OperationType.AUTO_UNKNOWN_ERROR;
        switch (currentTheme.taskState) {
            case TASK_STATUS_CODE_ERR:
            case TASK_STATUS_CODE_READY:
            case TASK_STATUS_CODE_PAUSE:
                operationType = OPERATION_TYPE_START;
                break;
            case TASK_STATUS_CODE_WAITING:
            case TASK_STATUS_CODE_DOING:
                operationType = OPERATION_TYPE_PAUSE;
                break;
            case TASK_STATUS_CODE_CHECKING:
            case TASK_STATUS_CODE_UNZIPPING:
            case TASK_STATUS_CODE_UNZIPPED:
                break;
            case TASK_STATUS_CODE_SUCCESS:
                operationType = OPERATION_TYPE_DELETE;
                break;
            default:
                break;
        }
        if (operationType != OperationType.AUTO_UNKNOWN_ERROR) {
            if (operationType == OPERATION_TYPE_DELETE) {
                //Toast.makeText(context,"当前主题已下载",Toast.LENGTH_SHORT).show();
            } else {
                ArrayList<Integer> themeIdDiyLst = new ArrayList<>();
                themeIdDiyLst.add(currentThemeId);
                ThemeDataController.getInstance().operate(DownLoadMode.DOWNLOAD_MODE_NET, operationType, themeIdDiyLst);
            }
        }
    }

    /**
     * 继续、取消下载主题操作
     * @param themeId  主题id
     * @param opType 操作类型
     */
    public void themeDataOperate(int themeId, @OperationType.OperationType1 int opType) {
        ArrayList<Integer> themeIdList = new ArrayList<>();
        themeIdList.add(themeId);
        ThemeDataController.getInstance().operate(DownLoadMode.DOWNLOAD_MODE_NET, opType, themeIdList);
    }

    /**
     * 请求下载所有主题头像并且获取主题列表
     * @param dataVersion 数据版本号，mapView.getOperatorStyle().getMapAssetStyleVersion()
     */
    public ArrayList<Theme> onRequestDataListCheckResult(String dataVersion) {
        // 获取所有theme id集合
        ArrayList<Integer> themeIdList =ThemeDataController.getInstance().getThemeIdList(DownLoadMode.DOWNLOAD_MODE_NET);
        if (themeIdList == null){
            return null;
        }

        for (int i=0; i<themeIdList.size(); i++){
            // 请求下载主题头像
            ThemeDataController.getInstance().requestThemeImage(DownLoadMode.DOWNLOAD_MODE_NET, themeIdList.get(i));
        }
        ArrayList<Theme> themeList= ThemeDataController.getInstance().getThemeList(DownLoadMode.DOWNLOAD_MODE_NET,dataVersion);

        return themeList;
    }

    /**
     * 保存同步常去地点(家、公司) 0.关闭 1.打开
     */
    public int setConfigKeyOftenArrived(int value) {
        ConfigValue castSimple = new ConfigValue();
        castSimple.intValue = value;
        return BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyOftenArrived, castSimple, SyncMode.SyncModeNow);
    }
    /**
     * 同步常去地点(家、公司) 0.关闭 1.打开
     */
    public int getConfigKeyOftenArrived() {
        ConfigValue configMapviewModeValue = BehaviorController.getInstance().getConfig(ConfigKey.ConfigKeyOftenArrived);
        if(configMapviewModeValue == null){
            return 0;
        }
        return configMapviewModeValue.intValue;
    }
}
