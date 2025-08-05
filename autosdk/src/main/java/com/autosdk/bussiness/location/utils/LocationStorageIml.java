package com.autosdk.bussiness.location.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.autosdk.common.storage.MapSharePreference;


/**
 * Created by AutoSdk.
 */

public class LocationStorageIml {
    private SharedPreferences preference;
    private Editor editor;

    public LocationStorageIml(Context context) {
        MapSharePreference mapSharePreference = new MapSharePreference(context, MapSharePreference.SharePreferenceName.locationInfoStorage);
        preference = mapSharePreference.sharedPrefs();
        editor = preference.edit();
    }

    private static final String LATITUDE               = "latitude";
    private static final String LONGITUDE              = "longitude";
    private static final String ALTITUDE               = "altitude";
    private static final String BEARING                = "bearing";
    private static final String TIMESTAMP              = "timestamp";
    private static final String ACCURACY               = "accuracy";
    private static final String FIRST_LOCATE_COMPLETED = "fistLocateCompleted";

    public String getLatitude() {
        String strlat = preference.getString(LATITUDE, "0");
        return strlat;
    }

    public void setLatitude(String lat) {
        editor.putString(LATITUDE, lat);
        editor.apply();
    }

    public String getLongitude() {
        String strlon = preference.getString(LONGITUDE, "0");
        return strlon;
    }

    public void setLongitude(String lon) {
        editor.putString(LONGITUDE, lon);
        editor.apply();
    }

    public String getAltitude() {
        return preference.getString(ALTITUDE, "0");
    }

    public void setAltitude(String h) {
        editor.putString(ALTITUDE, h);
        editor.apply();
    }

    public String getBearing() {
        return preference.getString(BEARING, "0");
    }

    public void setBearing(String bearing) {
        editor.putString(BEARING, bearing);
        editor.apply();
    }

    public long getTimestamp() {
        return preference.getLong(TIMESTAMP, 0);
    }

    public void setTimestamp(long timestamp) {
        editor.putLong(TIMESTAMP, timestamp);
        editor.apply();
    }

    public float getAccuracy() {
        return preference.getFloat(ACCURACY, 500);
    }

    public void setAccuracy(float p) {
        editor.putFloat(ACCURACY, p);
        editor.apply();
    }

    public boolean isFistLocateCompleted() {
        return preference.getBoolean(FIRST_LOCATE_COMPLETED, false);
    }

    public void setFistLocateCompleted(boolean is) {
        editor.putBoolean(FIRST_LOCATE_COMPLETED, is);
        editor.apply();
    }

    private void getValue(String key) {

    }

    public void setValue(String key, String value) {

    }
}
