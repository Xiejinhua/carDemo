package com.autosdk.bussiness.location.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.pos.model.LocMatchInfo;

import static com.autosdk.bussiness.location.instrument.LocationBaseInstrument.GPS_PROVIDER_SOURCE_TYPE;
import static com.autosdk.bussiness.location.instrument.LocationBaseInstrument.NETWORK_PROVIDER_SOURCE_TYPE;

import timber.log.Timber;


/**
 * 定位工具类
 */
public class LocationUtil {

    public static String TAG = LocationUtil.class.getSimpleName();

    private static LocationUtil sInstance;
    private LocationManager mLocationManager;
    private Criteria mCriteria;
    private String mBestProvidor;

    private LocationUtil(){

    }

    public static LocationUtil getInstance(){
        if(null == sInstance){
            sInstance = new LocationUtil();
        }
        return sInstance;
    }

    /**
     * 获取定位信息
     * @return Location对象
     */
    @SuppressLint("MissingPermission")
    public Location getLocation(Context context){
        if(null == mLocationManager){
            mLocationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            if(null == mLocationManager){
                return null;
            }
        }
        if(null == mCriteria){
            mCriteria=new Criteria();
            mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            mCriteria.setSpeedRequired(false);
            mCriteria.setCostAllowed(false);
            mCriteria.setBearingRequired(false);
            mCriteria.setAltitudeRequired(false);
            mCriteria.setPowerRequirement(Criteria.POWER_LOW);
            if(null == mCriteria){
                return null;
            }
        }
        if(null == mBestProvidor){
            mBestProvidor = mLocationManager.getBestProvider(getCriteria(), true);
            if(null == mBestProvidor){
                return null;
            }
        }
        Location location = null;
        try {
            location = mLocationManager.getLastKnownLocation(mBestProvidor);
        }catch (SecurityException ex){
            Timber.d("getLocation ex = %s", ex.toString());
        }catch (IllegalArgumentException ex){
            Timber.d("getLocation ex = %s", ex.toString());
        } catch (Exception ex){
            Timber.d("getLocation ex = %s", ex.toString());
        }
        return location;
    }

    private Criteria getCriteria(){
        Criteria criteria=new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    /**
     * 通过将定位透传的LocInfo转为Location
     * @param locInfo
     * @return
     */
    public Location convertLocInfo2Location(LocInfo locInfo) {
        if (null != locInfo) {
            String provider;
            switch (locInfo.sourType) {
                case GPS_PROVIDER_SOURCE_TYPE:
                    provider = LocationManager.GPS_PROVIDER;
                    break;
                case NETWORK_PROVIDER_SOURCE_TYPE:
                    provider = LocationManager.NETWORK_PROVIDER;
                    break;
                default:
                    return null;
            }
            Location location = new Location(provider);
            if(locInfo.matchInfoCnt > 0 &&  locInfo.matchInfo != null && locInfo.matchInfo.size() > 0){
                LocMatchInfo matchInfo = locInfo.matchInfo.get(0);
                location.setLongitude(matchInfo.stPos.lon);
                location.setLatitude(matchInfo.stPos.lat);
                int angle = (int) matchInfo.course;
//                Timber.d("DashboardTrace set matchInfo.course " + angle);
                location.setBearing(angle);
            } else {
                Timber.d("DashboardTrace set angle failed");
            }
            location.setSpeed((float) locInfo.speed);
            location.setAltitude(locInfo.alt);
            //TODO:BL2.0 LocInfo结构域不太确定;
            location.setAccuracy(locInfo.posAcc);
            // 改为开机时间戳后不能用作Location时间
            location.setTime(System.currentTimeMillis());
            return location;
        }
        return null;
    }

}
