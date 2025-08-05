package com.autosdk.common.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.format.Time;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.path.model.LinkType;
import com.autonavi.gbl.pos.model.LocDataType;
import com.autonavi.gbl.pos.model.LocGnss;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.pos.observer.IPosLocInfoObserver;
import com.autosdk.adapter.SdkAdapterManager;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.common.AutoState;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import timber.log.Timber;

/**
 * 定位信号获取处理
 */
public class LocationInstrument implements ILocator, LocationListener, IPosLocInfoObserver {

    public static String TAG = LocationInstrument.class.getSimpleName();

    protected Context mContext;

    protected LocationManager mLocationManager;

    private LocGnss mLocGnss = new LocGnss();
    /**
     * requestLocationUpdates中的频率
     */
    private long mInteval = 1000L;
    /**
     * requestLocationUpdates中距离通知
     */
    private float mDistance = 0.0F;

    /**
     * 是否正在内部进行Mock回放
     */
    protected boolean mIsMock = false;

    protected GSVInstrument mGSVInstrument;

    private final HandlerThread mHandlerThread = new HandlerThread("GPS_HandlerThread");

    /**
     * 当前道路类型
     */
    private int mCurRoadType;

    public LocationInstrument(Context context) {
        this.mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mGSVInstrument = new GSVInstrument(context);
    }

    @Override
    public void doStartLocate() {
        mHandlerThread.start();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mInteval, mDistance, this, mHandlerThread.getLooper());
        mGSVInstrument.doStartGsv();
        LocationController.getInstance().addLocInfoObserver(this);
    }

    @Override
    public void doStopLocate() {
        mLocationManager.removeUpdates(this);
        mGSVInstrument.doStopGSV();
        LocationController.getInstance().removeLocInfoObserver(this);
    }

    @Override
    public void onDestory() {
        doStopLocate();
        mIsMock = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null || (location.getLatitude() == 0 && location.getLongitude() == 0)) {
            Timber.i("onLocationChanged: location=null or lat=0 or lon=0");
            return;
        }

        /*todo 通知外部GPS已经定位
        if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.GPS_LOCATED);
        }*/

        Timber.i("onLocationChanged: location lat = %s, lon = %s, speed = %s", location.getLatitude(), location.getLongitude(), location.getSpeed());
        setGpsInfo(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void setGpsInfo(Location location) {
        if (null == location) {
            Timber.d("setGpsInfo setGpsInfo is null!");
            return;
        }
        if (mIsMock) {
            return;
        }

        mLocGnss.dataType = LocDataType.LocDataGnss;
        Time time = new Time();
        long timeoriging = System.currentTimeMillis();
        time.set(location.getTime());
        mLocGnss.year = time.year;
        mLocGnss.month = time.month + 1;
        mLocGnss.day = time.monthDay;
        mLocGnss.hour = time.hour;
        mLocGnss.minute = time.minute;
        mLocGnss.second = time.second;
        long timetarget = getTime(time);
        int mduration = (int) (timeoriging - timetarget);
        mLocGnss.millisecond = mduration;

        mLocGnss.point = new Coord2DDouble(0.0, 0.0);
        mLocGnss.point.lon = location.getLongitude();
        mLocGnss.point.lat = location.getLatitude();
        // 精度半径。米
        if (location.hasAccuracy()) {
            mLocGnss.accuracy = location.getAccuracy();
        }
        if (location.hasBearing()) {
            mLocGnss.course = location.getBearing();
        }
        if (location.hasAltitude()) {
            mLocGnss.alt = (float) location.getAltitude();
        }
        mLocGnss.speed = (float) (location.getSpeed() * 3.6);
        // 推荐使用系统滴答数，而非信号源的时间戳
        mLocGnss.tickTime = BigInteger.valueOf(SystemClock.elapsedRealtime());
        mLocGnss.isNS = (byte) ((mLocGnss.point.lat > 0) ? 'N' : 'S');
        mLocGnss.isEW = (byte) ((mLocGnss.point.lon > 0) ? 'E' : 'W');
        int count = 9;
        // 卫星个数需传可用卫星数，不是可见卫星数，具体项目可根据自己情况获取，如果不能获取，则先写死
        mLocGnss.num = count;
        //未知时使用-1.0f
        mLocGnss.hdop = -1.0f;
        mLocGnss.vdop = -1.0f;
        mLocGnss.pdop = -1.0f;
        // 定位模式（'A','D','E','N'）。此字段暂时未使用，赋'A'
        mLocGnss.mode = 'A';
        // 位置是否加密偏移: 0未偏移，1已经偏移 根据项目情况修改
        mLocGnss.isEncrypted = 0;
        switch (location.getProvider()) {
            case LocationManager.GPS_PROVIDER:
                // 信号来源: 0 GPS定位  1 网络定位(包括室内定位和基站定位)
                mLocGnss.sourType = 0;
                // GPS定位状态位。'A'：有效定位；
                mLocGnss.status = 'A';
                break;
            case LocationManager.NETWORK_PROVIDER:
                // 信号来源: 0 GPS定位  1 网络定位(包括室内定位和基站定位)
                mLocGnss.sourType = 1;
                // GPS定位状态位。'V'：无效定位
                mLocGnss.status = 'A';
                break;
            default:
                // 信号来源: 0 GPS定位  1 网络定位(包括室内定位和基站定位)
                mLocGnss.sourType = 1;
                // GPS定位状态位。'V'：无效定位
                mLocGnss.status = 'V';
                break;
        }
        LocationController.getInstance().setGnssInfo(mLocGnss);
        LocationController.getInstance().setGnssLocation(location);
        if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.GPS_LOCATED);
        }
    }

    @Override
    public void setPosMockMode(boolean isMock) {
        Timber.i("isMock %s", isMock);
        mIsMock = isMock;
    }

    @Override
    public void onLocInfoUpdate(LocInfo locInfo) {
        getTunnelStatus(locInfo);
    }

    private void getTunnelStatus(LocInfo locInfo) {
        if (locInfo == null || locInfo.matchInfo == null || locInfo.matchInfo.get(0) == null) {
            return;
        }

        int type = locInfo.matchInfo.get(0).linkType;

        if (mCurRoadType != LinkType.LinkTypeTunnel && type == LinkType.LinkTypeTunnel) {
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.NAVI_ENTERTUNNEL);
        } else if (mCurRoadType == LinkType.LinkTypeTunnel && type != LinkType.LinkTypeTunnel) {
            SdkAdapterManager.getInstance().sendNormalMessage(AutoState.NAVI__EXITTUNNEL);
        }
        mCurRoadType = type;
    }

    long getTime(Time time) {
        String date = String.format("%d-%d-%d %d-%d-%d", time.year, time.month + 1, time.monthDay, time.hour, time.minute, time.second);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        long timevv = 1;
        try {
            timevv = simpleDateFormat.parse(date).getTime();
        } catch (ParseException e) {
        }
        return timevv;
    }
}
