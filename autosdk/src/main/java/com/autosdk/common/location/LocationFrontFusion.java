package com.autosdk.common.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.Time;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.pos.model.GPSDatetime;
import com.autonavi.gbl.pos.model.LocDataType;
import com.autonavi.gbl.pos.model.LocDrPos;
import com.autonavi.gbl.pos.model.LocDrType;
import com.autonavi.gbl.pos.model.LocFeedbackNode;
import com.autonavi.gbl.pos.model.LocMMFeedbackInfo;
import com.autonavi.gbl.pos.model.LocViaductValid;
import com.autonavi.gbl.pos.observer.IPosMapMatchFeedbackObserver;
import com.autosdk.bussiness.location.LocationController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * 前端融合
 */
public class LocationFrontFusion extends LocationInstrument implements IPosMapMatchFeedbackObserver {

    public static String TAG = LocationFrontFusion.class.getSimpleName();

    /**
     * GPS 时间差，时区
     */
    private static final int GPS_TIME_OFFSET = 0;

    public static final String MMF_KEY = "AmapAutoMMF";

    private Context mContext;

    private LocationManager mLocationManager;

    /**
     * requestLocationUpdates中的频率
     */
    private long mInteval = 1000L;
    /**
     * requestLocationUpdates中距离通知
     */
    private float mDistance = 0.0F;

    public LocationFrontFusion(Context context) {
        super(context);
    }

    @Override
    public void doStartLocate() {
        super.doStartLocate();
        LocationController.getInstance().addMapMatchFeedbackObserver(this);
    }

    @Override
    public void doStopLocate() {
        super.doStopLocate();
        LocationController.getInstance().removeMapMatchFeedbackObserver(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null || (location.getLatitude() == 0 && location.getLongitude() == 0)) {
            Timber.i("onLocationChanged: location=null or lat=0 or lon=0");
            return;
        }
        Timber.i("onLocationChanged: location lat=" + location.getLatitude() + ", lon = " + location.getLongitude());
        setLocDrPos(location);
    }

    /**
     * 设置前端融合信号
     * @param location
     */
    private void setLocDrPos(Location location) {
        if(location == null) {
            return;
        }
        Bundle drInfoBundle = location.getExtras();
        String drPos = drInfoBundle.getString("AmapAutoDRPos");
        Timber.d("setLocDrPos drPos %s ", drPos);
        LocDrPos locationInfo = new LocDrPos();
        double lon = location.getLongitude();
        double lat = location.getLatitude();
        double alt = location.getAltitude();
        float course = location.getBearing();
        long timeLong = location.getTime();
        float speed = location.getSpeed();
        long timeReal = timeLong + GPS_TIME_OFFSET * 60 * 60 * 1000;
        long ticktime64 = SystemClock.elapsedRealtime();
        byte nsByte = 'N';
        byte ewByte = 'E';
        int satnum = 0;
        float hdop = -1.0f;
        float vdop = -1.0f;
        float pdop = -1.0f;
        byte gpsStatusByte = 'A';
        float posAcc = location.getAccuracy();
        float courseAcc = 0;
        float altAcc = 0;
        float speedAcc = 0;
        int drStatus = 2;
        int moveStatus = 0;
        boolean isDeltaAltValid = false;
        float deltaAlt = 0;
        boolean isDeltaAltAccValid = false;
        float deltaAltAcc = 0;
        boolean isSlopeValueValid = false;
        float slopeValue = 0;
        boolean isSlopeAccValid = false;
        float slopeAcc = 0;
        boolean isMoveDistValid = false;
        double moveDist = 0;
        try {
            if(!location.getProvider().equals(LocationManager.NETWORK_PROVIDER) && drPos != null) {
                //如果不是网络定位，走默认赋值
                JSONObject jsonObject = new JSONObject(drPos);
                if(jsonObject.has("speed")) {
                    speed = (float)jsonObject.optDouble("speed");
                }
                ticktime64 = jsonObject.optLong("ticktime64", SystemClock.elapsedRealtime());
                String ns = jsonObject.optString("ns");
                if(TextUtils.isEmpty(ns)) {
                    nsByte = (byte) ((lat > 0) ? 'N' : 'S');
                } else {
                    nsByte = getLocationString2Byte(ns, nsByte);
                }
                String ew = jsonObject.optString("ew");
                if(TextUtils.isEmpty(ew)) {
                    ewByte = (byte) ((lon > 0) ? 'E' : 'W');
                } else {
                    ewByte = getLocationString2Byte(ew, ewByte);
                }
                satnum = jsonObject.optInt("satnum");
                hdop = (float)jsonObject.optDouble("hdop",-1.0f);
                vdop = (float)jsonObject.optDouble("vdop",-1.0f);
                pdop = (float)jsonObject.optDouble("pdop",-1.0f);
                String gpsStatus = jsonObject.optString("gpsStatus","A");
                gpsStatusByte = getLocationString2Byte(gpsStatus, gpsStatusByte);

                posAcc = (float)jsonObject.optDouble("posAcc");
                courseAcc = (float)jsonObject.optDouble("courseAcc");
                altAcc = (float)jsonObject.optDouble("altAcc");
                speedAcc = (float)jsonObject.optDouble("speedAcc");

                drStatus = jsonObject.optInt("DRStatus");
                moveStatus = jsonObject.optInt("moveStatus");
                isDeltaAltValid = jsonObject.optBoolean("isDeltaAltValid");
                deltaAlt = (float)jsonObject.optDouble("deltaAlt");

                isDeltaAltAccValid = jsonObject.optBoolean("isDeltaAltAccValid", false);
                deltaAltAcc = (float)jsonObject.optDouble("deltaAltAcc");

                isSlopeValueValid = jsonObject.optBoolean("isSlopeValueValid", false);
                slopeValue = (float)jsonObject.optDouble("slopeValue");

                isSlopeAccValid = jsonObject.optBoolean("isSlopeAccValid", false);
                slopeAcc = (float)jsonObject.optDouble("slopeAcc");

                isMoveDistValid = jsonObject.optBoolean("isMoveDistValid", false);
                moveDist = jsonObject.optDouble("moveDist");
            }

            Time time = new Time();
            time.set(timeReal);
            GPSDatetime dateTime = new GPSDatetime();
            dateTime.year = time.year;
            dateTime.month = time.month + 1;
            dateTime.day = time.monthDay;
            dateTime.hour = time.hour;
            dateTime.minute = time.minute;
            dateTime.second = time.second;
            locationInfo.dateTime = dateTime;

            locationInfo.dataType = LocDataType.LocDataDrFusion;

            Coord2DDouble mapPoint = new Coord2DDouble();
            mapPoint.lon = lon;
            mapPoint.lat = lat;
            locationInfo.stPos = mapPoint;
            locationInfo.stPosRaw = mapPoint;

            locationInfo.alt = (float) alt;
            locationInfo.slopeAcc = slopeValue;
            locationInfo.speed = speed;

            locationInfo.tickTime = BigInteger.valueOf(ticktime64);;
            locationInfo.NS = nsByte;
            locationInfo.EW = ewByte;

            locationInfo.satNum = satnum;
            locationInfo.hdop = hdop;
            locationInfo.vdop = vdop;
            locationInfo.pdop = pdop;
            locationInfo.gpsStatus = gpsStatusByte;

            locationInfo.moveStatus = moveStatus;
            // DR状态（0纯GPS、1纯DR、2DR+GPS）//没有传感器数据时置0
            int drType = LocDrType.LocDrTypeGNSS;
            if(drStatus < 3) {
                drType = drStatus;
            }
            locationInfo.drType = drType;

            LocViaductValid valid = new LocViaductValid();
            valid.deltaAlt = isDeltaAltValid;
            valid.deltaAltAcc = isDeltaAltAccValid;
            valid.slopeValue = isSlopeValueValid;
            valid.slopeAcc = isSlopeAccValid;
            valid.moveDist = isMoveDistValid;
            locationInfo.validField = valid;

            // 高程差（米）
            locationInfo.deltaAlt = deltaAlt;
            // 高程差精度（米）
            locationInfo.deltaAltAcc = deltaAltAcc;

            locationInfo.slopeValue = slopeValue;

            // 坡角精度(度）
            locationInfo.slopeAcc = slopeAcc;

            // 移动距离（变化量）（米）
            locationInfo.moveDist = moveDist;
            // 位置精度（米）
            locationInfo.posAcc = posAcc;
            // 航向精度（度）
            locationInfo.courseAcc = courseAcc;
            // 海拔精度（米）
            locationInfo.altAcc = altAcc;
            // 车速精度(公里/小时）
            locationInfo.speedAcc = speedAcc;
            locationInfo.course = course;

            LocationController.getInstance().setLocDrPosInfo(locationInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private byte getLocationString2Byte(String dr, byte defaultByte) {
        if(TextUtils.isEmpty(dr)) {
            return defaultByte;
        }
        byte[] nsBytes = dr.getBytes();
        if(nsBytes == null || nsBytes.length <= 0) {
            return defaultByte;
        }
        return nsBytes[0];
    }

    /**
     * 地图匹配信息回调
     * @param locMMFeedbackInfo
     */
    @Override
    public void onMapMatchFeedbackUpdate(LocMMFeedbackInfo locMMFeedbackInfo) {
        Timber.d("LocationFrontEndFusion onMapMatchFeedbackUpdate locMMFeedbackInfo = %s", locMMFeedbackInfo);
        if(locMMFeedbackInfo == null) {
            return;
        }
        String mmfInfo = getMMFJson(locMMFeedbackInfo);
        Timber.d("setLocDrPos onMapMatchFeedbackUpdate mmfInfo = " + mmfInfo);
        if(TextUtils.isEmpty(mmfInfo)) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("AmapAutoMMF",mmfInfo);
        mLocationManager.sendExtraCommand("gps", MMF_KEY, bundle);
    }

    /**
     * 将sdk内部返回的地图匹配反馈信息转换成JSON格式
     * @param locMMFeedback
     * @return
     */
    private String getMMFJson(LocMMFeedbackInfo locMMFeedback) {
        String mmfInfo = null;
        Timber.d("DRS sendMapMatchFeedback elapsedRealtime = %s ticktime = %s", SystemClock.elapsedRealtime(), locMMFeedback.ticktime);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ticktime", String.valueOf(locMMFeedback.ticktime));
            JSONArray jsonArray = new JSONArray();
            ArrayList<LocFeedbackNode> locFeedbackNodes = locMMFeedback.feedbackNodes;
            if(locFeedbackNodes == null || locFeedbackNodes.size() <= 0) {
                return null;
            }
            for(int i = 0;i < locFeedbackNodes.size();i++) {
                JSONObject object = new JSONObject();
                LocFeedbackNode feedbackNode = locFeedbackNodes.get(i);
                Coord3DDouble deltaPoint = feedbackNode.deltaPoint;
                if(deltaPoint != null) {
                    object.put("lon", deltaPoint.lon);
                    object.put("lat", deltaPoint.lat);
                    object.put("zLevel", deltaPoint.z);
                }
                object.put("roadAzi", feedbackNode.roadAzi);
                object.put("probability", feedbackNode.probability);
                object.put("feedBackType", feedbackNode.type);
                object.put("roadWidth", feedbackNode.roadWidth);
                jsonArray.put(i,object);
            }
            jsonObject.put("count", locMMFeedback.count);
            jsonObject.put("feedbackNodes", jsonArray);
            mmfInfo = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mmfInfo;
    }
}
