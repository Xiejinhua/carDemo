package com.autosdk.common.storage;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.autosdk.common.SdkApplicationUtils;
import com.google.gson.Gson;

/**
 * sdk sp缓存管理
 *
 * @author AutoSdk
 */
public class MapSharePreference {

    public static final int DEFAULT_INI_VALUE = -1;
    private final SharedPreferences mSp;
    private final SharedPreferences.Editor mEditor;

    public MapSharePreference(SharePreferenceName name) {
        this(SdkApplicationUtils.getApplication(), name);
    }

    public MapSharePreference(Context context, SharePreferenceName name) {
        this.mSp = context.getSharedPreferences(name.toString(),
                Context.MODE_PRIVATE);
        this.mEditor = this.mSp.edit();
    }

    public MapSharePreference(Context context, String name) {
        this.mSp = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
        this.mEditor = this.mSp.edit();
    }

    public void clear() {
        this.mEditor.clear();
    }

    public int getIntValue(SharePreferenceKeyEnum key, int defValue) {
        return mSp.getInt(key.toString(), defValue);
    }

    public void putIntValue(SharePreferenceKeyEnum key, int value) {
        mEditor.putInt(key.toString(), value);
        commit();
    }

    public boolean getBooleanValue(SharePreferenceKeyEnum key, boolean defValue) {
        return mSp.getBoolean(key.toString(), defValue);
    }

    public void putBooleanValue(SharePreferenceKeyEnum key, boolean value) {
        mEditor.putBoolean(key.toString(), value);
        commit();
    }

    public float getFloatValue(SharePreferenceKeyEnum key, float defValue) {
        return mSp.getFloat(key.toString(), defValue);
    }

    public void putFloatValue(SharePreferenceKeyEnum key, float value) {
        mEditor.putFloat(key.toString(), value);
        commit();
    }

    public long getLongValue(SharePreferenceKeyEnum key, long defValue) {
        return mSp.getLong(key.toString(), defValue);
    }

    public void putLongValue(SharePreferenceKeyEnum key, long value) {
        mEditor.putLong(key.toString(), value);
        commit();
    }

    public String getStringValue(SharePreferenceKeyEnum key, String defValue) {
        return mSp.getString(key.toString(), defValue);
    }

    public void putStringValue(SharePreferenceKeyEnum key, String value) {
        mEditor.putString(key.toString(), value);
        commit();
    }

    public void put(SharePreferenceKeyEnum key, String value) {
        mEditor.putString(key.toString(), value);
        commit();
    }

    public boolean getBooleanValue(String key, boolean defValue) {
        return mSp.getBoolean(key, defValue);
    }

    public void putBooleanValue(String key, boolean value) {
        mEditor.putBoolean(key, value);
        commit();
    }

    public void putStringValue(String key, String value) {
        mEditor.putString(key, value);
        commit();
    }

    public String getStringValue(String key, String defValue) {
        return mSp.getString(key, defValue);
    }

    public int getIntValue(String key, int defValue) {
        return mSp.getInt(key, defValue);
    }

    public void putIntValue(String key, int value) {
        mEditor.putInt(key, value);
        commit();
    }

    public void putObject(SharePreferenceKeyEnum key, Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        mEditor.putString(key.toString(), json);
        commit();
    }

    public Object getObject(SharePreferenceKeyEnum key, Class clazz) {
        String json = getStringValue(key.toString(), "");
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public void remove(SharePreferenceKeyEnum key) {
        this.mEditor.remove(key.toString());
        commit();
    }

    public SharedPreferences.Editor edit() {
        return mEditor;
    }

    public SharedPreferences sharedPrefs() {
        return mSp;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void commit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mEditor.apply();
        } else {
            mEditor.commit();
        }
    }

    /**
     * 模块类型
     */
    public enum SharePreferenceName {
        /**
         * 定位
         */
        locationInfoStorage,
        route,
        gas,
        navi,
        sycn,
        userSetting,
        pushMessage,
        eggSetting,
        adapter,
        teamSetting,
        permission,
        account,
        active,
        normal
    }

    /**
     * 具体字段
     */
    public enum SharePreferenceKeyEnum {
        /**
         * 路线偏好
         */
        routePrefer,
        gasType, isService, routeCarResultData, sycnTime, locInfo, setting, mapTextSize, mapAutoScale, carLogos, themeId, themePath, autoRecord, threeDBuild, navimode, eggVelue,
        eggArValue, eggSdkLog, eggHmiLog, eggLane, eggLaneMode, eggServerType, eggLaneFileConfig, firstStartNavi, innerStyle, carInfo, eggLaneOffline, firstRun, timeFloating, fpsFloating, loopSimNavi, teamSpeaking, chatUserId, teamNick, teamId, volume, mapViewMode, sensorParaInfo, historyCity,
        storagePath, locationTipsCount, storageTipsCount, phoneTipsTime, overLayTipsCount, shouldShowRequestPermissionRationale, blLogcat, blPosLog, blLogcatLevel, realGps, eggReplay, accountInfo,
        recordFloatPosX, recordFloatPosY, baseDownLoadTipsTime, cruiseMute, isReboot, userAvatar, userName, mapAgreement, openMapTestUuid, mapTestUuid, spEhpLogOpen, spAdasLogOpen,
        startingPoint, startingPointLatitude, startingPointLongitude, volumeModel, volumeMute, parkNavi, openDayNight, overviewRoads, walkSwitch, mapFavoritePoint, openEhp, internetMapNum, publicationMapNum, weChatUserInfo, vehicleNum, vehicleLimit,
        userParkType, userMapType, userMapScale, userMediaType, userViewMode, mediaType, userDayNight, dayNightMode, vehicleSN, enableV2x, cruiseBroadcastSwitch, mapFont, oldTime, phoneLoginNumber, phoneNumList, showCarCompass, userLoginPhone,
        intentionNavigation, speechVoiceId, personalizationCar, commonCityList, disconnectNetworkTime, mDesLon, ahaScenicBroadcastSwitch,
        mDesLat,chargeType,accOffTime, usbReStart, accountProfile, douYinConfirm
    }
}
