package com.autosdk.common.location;

import android.content.Context;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;

import com.autonavi.gbl.pos.model.LocDataType;
import com.autonavi.gbl.pos.model.LocGpgsv;
import com.autosdk.bussiness.location.LocationController;

import java.math.BigInteger;
import java.util.Iterator;

import timber.log.Timber;

/**
 * 卫星星历获取
 */
public class GSVInstrument implements GpsStatus.Listener {

    public static String TAG = GSVInstrument.class.getSimpleName();

    /**
     * 星历卫星数据最大值
     */
    public static final int MAX_GPS_SATELLITE_NUM = 16;

    private final LocationManager mLocationManager;
    private LocGpgsv mLocGpgsvLow = new LocGpgsv();
    private LocGpgsv mLocGpgsvHight = new LocGpgsv();
    private GpsStatus mGpsStatus = null;

    /**
     * 可用卫星数
     */
    private int mUsedSatellite = 0;

    private GnssStatus.Callback mGnssStatusCallback = null;

    public GSVInstrument(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            mGpsStatus = mLocationManager.getGpsStatus(null);
    }

    /**
     * 获取卫星类型
     */
    public int getSatelliteType(int type) {
        int result = 0;

        switch (type) {
            case GnssStatus.CONSTELLATION_GPS: {
                result = SatelliteType.GPS;
                break;
            }
            case GnssStatus.CONSTELLATION_BEIDOU: {
                result = SatelliteType.BEIDOU;
                break;
            }
            case GnssStatus.CONSTELLATION_GALILEO: {
                result = SatelliteType.GALILEO;
                break;
            }
            case GnssStatus.CONSTELLATION_GLONASS: {
                result = SatelliteType.GLONASS;
                break;
            }
            default:
                result = SatelliteType.OTHER;
                break;
        }

        return result;
    }


    /**
     * 设置星历数据（针对Android N及以上版本）
     */
    public void setGSVDataForAndroidHigh(GnssStatus status) {
        int count = 0;
        if (status != null) {
            mLocGpgsvHight.dataType = LocDataType.LocDataGpgsv;
            mLocGpgsvHight.type = 0;
            mLocGpgsvHight.prn = new int[MAX_GPS_SATELLITE_NUM];
            mLocGpgsvHight.elevation = new int[MAX_GPS_SATELLITE_NUM];
            mLocGpgsvHight.azimuth = new int[MAX_GPS_SATELLITE_NUM];
            mLocGpgsvHight.snr = new int[MAX_GPS_SATELLITE_NUM];

            // 获取卫星颗数的默认最大值
            int maxSatellites = status.getSatelliteCount();
            for (int i = 0; i < maxSatellites; i++) {
                int type = status.getConstellationType(i);
                int svid = status.getSvid(i);
                int gnssType = getSatelliteType(type);
                //Timber.d("setGSVDataForAndroidHigh: type=%s, svid=%s,gnssType=%s", type, svid, gnssType);

                if (count < MAX_GPS_SATELLITE_NUM) {
                    int elevation = (int) status.getElevationDegrees(i);
                    int azimuth = (int) status.getAzimuthDegrees(i);
                    int snr = (int) status.getCn0DbHz(i);

                    mLocGpgsvHight.prn[count] = svid;
                    mLocGpgsvHight.elevation[count] = elevation;
                    mLocGpgsvHight.azimuth[count] = azimuth;
                    mLocGpgsvHight.snr[count] = snr;
                    //Timber.d("setGSVDataForAndroidHigh: count=%s, elevation=%s, azimuth=%s, snr=%s", count, elevation, azimuth, snr);
                    count++;
                }
            }

            mUsedSatellite = count;
            mLocGpgsvHight.num = Math.min(count, MAX_GPS_SATELLITE_NUM);
            if (mLocGpgsvHight.num > 0) {
                // 时间戳需与GNSS信号传入的时间戳同源
                mLocGpgsvHight.tickTime = BigInteger.valueOf(SystemClock.elapsedRealtime());
                LocationController.getInstance().setGsvInfo(mLocGpgsvHight);
            }
        }
    }

    private HandlerThread mGsvHandlerThread;

    /**
     * 开始监听星历数据
     */
    public void doStartGsv() {
        if (mLocationManager != null) {
            if (mGsvHandlerThread == null) {
                HandlerThread handlerThread = new HandlerThread("GnssMeasurementsCallback");
                handlerThread.start();
                mGsvHandlerThread = handlerThread;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mGnssStatusCallback = new GnssStatus.Callback() {
                    @Override
                    public void onStarted() {
                    }

                    @Override
                    public void onStopped() {
                    }

                    @Override
                    public void onFirstFix(int ttffMillis) {
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSatelliteStatusChanged(GnssStatus status) {
                        setGSVDataForAndroidHigh(status);
                    }
                };
                boolean ok = mLocationManager.registerGnssStatusCallback(mGnssStatusCallback, new Handler(mGsvHandlerThread.getLooper()));
                Timber.i("registerGnssStatusCallback %s", ok);
            } else {
                mLocationManager.addGpsStatusListener(this);
            }
        }

    }

    /**
     * 停止监听星历数据
     */
    public void doStopGSV() {
        if (mLocationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
            } else {
                mLocationManager.removeGpsStatusListener(this);
            }
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {
        Timber.d("Listener-onGpsStatusChanged: event = %s", event);
        switch (event) {
            // 第一次定位
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Timber.d("第一次定位");
                break;
            // 卫星状态改变
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                setGSVDataForAndroidLow();
                Timber.d("卫星状态改变");
                break;
            // 定位启动
            case GpsStatus.GPS_EVENT_STARTED:
                Timber.d("定位启动");
                break;
            // 定位结束
            case GpsStatus.GPS_EVENT_STOPPED:
                Timber.d("定位结束");
                break;
            default:
                break;
        }
    }


    /**
     * 设置星历数据（针对Android N以下版本）
     */
    public void setGSVDataForAndroidLow() {
        if (mLocationManager == null) {
            return;
        }
        mGpsStatus = mLocationManager.getGpsStatus(null);

        int count = 0;
        if (mGpsStatus != null) {
            mLocGpgsvLow.dataType = LocDataType.LocDataGpgsv;
            mLocGpgsvLow.type = 0;
            mLocGpgsvLow.prn = new int[MAX_GPS_SATELLITE_NUM];
            mLocGpgsvLow.elevation = new int[MAX_GPS_SATELLITE_NUM];
            mLocGpgsvLow.azimuth = new int[MAX_GPS_SATELLITE_NUM];
            mLocGpgsvLow.snr = new int[MAX_GPS_SATELLITE_NUM];

            // 获取卫星颗数的默认最大值
            int maxSatellites = mGpsStatus.getMaxSatellites();
            // 创建一个迭代器保存所有卫星
            Iterator<GpsSatellite> iters = mGpsStatus.getSatellites().iterator();
            while (iters.hasNext() && count <= maxSatellites) {
                GpsSatellite s = iters.next();
//                if (s.usedInFix()) { //usedInFix是判断卫星是否可用，但GSV需要的是所有可见的卫星星历信息，故无需增加该判断
                if (count < MAX_GPS_SATELLITE_NUM) {
                    int prn = s.getPrn();
                    int elevation = (int) s.getElevation();
                    int azimuth = (int) s.getAzimuth();
                    int snr = (int) s.getSnr();

                    mLocGpgsvLow.prn[count] = prn;
                    mLocGpgsvLow.elevation[count] = elevation;
                    mLocGpgsvLow.azimuth[count] = azimuth;
                    mLocGpgsvLow.snr[count] = snr;
                    Timber.d("setGSVDataForAndroidLow: count=%s, elevation=%s, azimuth=%s, snr=%s", count, elevation, azimuth, snr);
                    count++;
                }
//                }
            }

            mUsedSatellite = count;
            mLocGpgsvLow.num = Math.min(count, MAX_GPS_SATELLITE_NUM);
            if (mLocGpgsvLow.num > 0) {
                // 时间戳需与GNSS信号传入的时间戳同源
                mLocGpgsvLow.tickTime = BigInteger.valueOf(SystemClock.elapsedRealtime());
                LocationController.getInstance().setGsvInfo(mLocGpgsvLow);
            }
        } else {
            Timber.d("setGSVDataForAndroidLow: gpsStatus=null");
        }
    }

    /**
     * 获取可用卫星数量
     */
    public int getUsedSatellite() {
        return mUsedSatellite;
    }
}
