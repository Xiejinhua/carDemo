package com.autosdk.bussiness.location.instrument;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.pos.PosService;
import com.autonavi.gbl.pos.model.LocAcce3d;
import com.autonavi.gbl.pos.model.LocDataType;
import com.autonavi.gbl.pos.model.LocDrPos;
import com.autonavi.gbl.pos.model.LocGnss;
import com.autonavi.gbl.pos.model.LocGpgsv;
import com.autonavi.gbl.pos.model.LocGyro;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.pos.model.LocPulse;
import com.autonavi.gbl.pos.model.LocSignData;
import com.autonavi.gbl.pos.model.LocSpeedometer;
import com.autonavi.gbl.pos.observer.IPosLocInfoObserver;
import com.autosdk.bussiness.location.constant.SdkLocStatus;
import com.autosdk.bussiness.location.listener.LocationListener;
import com.autosdk.bussiness.location.listener.OriginalLocationCallback;
import com.autosdk.bussiness.location.utils.GpsStatusChecker;
import com.autosdk.bussiness.location.utils.LocationStorageIml;
import com.autosdk.bussiness.location.utils.LocationUtil;
import com.autosdk.bussiness.search.utils.NumberUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;


/**
 * 定位适配，负责与系统交互
 * 处理了一些GPS和前端融合、后端融合公共部分逻辑
 */
public class LocationBaseInstrument implements IPosLocInfoObserver, GpsStatusChecker.OnTimeOutCallback {

    public static String TAG = LocationBaseInstrument.class.getSimpleName();

    public static final int GPS_PROVIDER_SOURCE_TYPE = 0;
    public static final int NETWORK_PROVIDER_SOURCE_TYPE = 1;

    protected static final int MSG_ON_LOCATION_OK = 0xF0;
    protected static final int MSG_ON_LOCATION_FAIL = 0xF1;
    protected static final int MSG_ONLOCATION_ORIGINAL_OK = 0xF2;
    protected static final int MSG_ON_LOCATION_GPS_SUCCESS = 0xF3;


    private LocSignData mGnssInfoData = new LocSignData();
    private LocSignData mLocGyroInfoData = new LocSignData();
    private LocSignData mLocAcce3DInfoData = new LocSignData();
    private LocSignData mLocPulseInfoData = new LocSignData();
    private LocSignData mLocSpeedometerInfoData = new LocSignData();
    private LocSignData mLocLocDrSignData = new LocSignData();
    private LocSignData mLocGsvSignData = new LocSignData();


    /**
     * 定位状态
     *
     * @see SdkLocStatus
     */
    protected SdkLocStatus mSdkLocStatus = SdkLocStatus.ON_LOCATION_GPS_FAIl;

    /**
     * 上次sp保存车标位置的时间
     */
    private long latestSaveTime = 0;

    /**
     * 每3秒本地sp存储一次车标位置和角度信息
     */
    private static final int POSITION_SAVE_INTERVAL = 3000;

    private PosService mPosService;

    private Location mLocation;
    private LocationListener mLocInfoUpdateListener;

    /**
     * 定位回调的位置信息
     */
    private LocInfo mLocInfo;

    private Coord3DDouble mDefaultPos;

    private LocationStorageIml mStorage;

//    public static final double[] gaode = {118.185962, 24.489438};//厦门高德
//    public static final double[] gaode = {113.307605, 23.3899329};//广州白云机场
    /**
     * 朝阳区阜荣街10号首开广场
     */
    public static final double[] GAODE = {116.473004, 39.993306};

    private List<OriginalLocationCallback<SdkLocStatus>> mOriginalGpsCallbacks = new CopyOnWriteArrayList<>();

    private Context mContext;

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == MSG_ON_LOCATION_FAIL) {//超过2s没有原始gps回调
                synchronized (mOriginalGpsCallbacks) {
                    mSdkLocStatus = SdkLocStatus.ON_LOCATION_GPS_FAIl;
                    for (OriginalLocationCallback<SdkLocStatus> callback : mOriginalGpsCallbacks) {
                        if (null != callback) {
                            callback.onOriginalLocationChange(SdkLocStatus.ON_LOCATION_GPS_FAIl);
                        }
                    }
                }
            } else if (what == MSG_ON_LOCATION_GPS_SUCCESS) {
                //原始gps定位回调
                synchronized (mOriginalGpsCallbacks) {
                    mSdkLocStatus = SdkLocStatus.ON_LOCATION_GPS_OK;
                    for (OriginalLocationCallback<SdkLocStatus> callback : mOriginalGpsCallbacks) {
                        if (null != callback) {
                            callback.onOriginalLocationChange(SdkLocStatus.ON_LOCATION_GPS_OK);
                        }
                    }
                }
            } else if (what == MSG_ON_LOCATION_OK) {//tbt定位回调
                mSdkLocStatus = SdkLocStatus.ON_LOCATION_OK;
                for (OriginalLocationCallback<SdkLocStatus> callback : mOriginalGpsCallbacks) {
                    if (null != callback) {
                        callback.onOriginalLocationChange(SdkLocStatus.ON_LOCATION_OK);
                    }
                }
            }
        }
    };
    private GpsStatusChecker mGpsStatusChecker;

    public LocationBaseInstrument(Context context, PosService posService) {
        mContext = context;
        mPosService = posService;
        initLocation();
    }

    public void setDefaultPos(Coord3DDouble defaultPos) {
        mDefaultPos = defaultPos;
    }

    /**
     * 初始化最后一次的定位位置
     */
    private void initLocation() {
        mStorage = new LocationStorageIml(mContext);
        String lon = mStorage.getLongitude();
        String lat = mStorage.getLatitude();
        mLocation = new Location("default");
        if (NumberUtil.str2Double(lon, 0) == 0 || NumberUtil.str2Double(lat, 0) == 0) {
            Timber.d("initLocation locationInfoStorage null");
            if (mDefaultPos == null) {
                mDefaultPos = new Coord3DDouble();
                mDefaultPos.lon = GAODE[0];
                mDefaultPos.lat = GAODE[1];
            }
            double defaultPoiLon = mDefaultPos.lon;
            mLocation.setLongitude(defaultPoiLon);
            double defaultPoiLlat = mDefaultPos.lat;
            mLocation.setLatitude(defaultPoiLlat);
            mLocation.setBearing(-1);
            mLocation.setTime(System.currentTimeMillis());
        } else {
            mLocation.setLatitude(NumberUtil.str2Double(lat, GAODE[1]));
            mLocation.setLongitude(NumberUtil.str2Double(lon, GAODE[0]));
            mLocation.setAltitude(NumberUtil.str2Double(mStorage.getAltitude(), 0));
            mLocation.setBearing(NumberUtil.str2Float(mStorage.getBearing(), 0));
            mLocation.setTime(mStorage.getTimestamp());
            mLocation.setAccuracy(mStorage.getAccuracy());
        }
    }

    public void doStart() {
        mGpsStatusChecker = new GpsStatusChecker();
        mGpsStatusChecker.setTimeOutListener(this);
        mGpsStatusChecker.doCount();
        mGpsStatusChecker.start();
    }

    public void doStop() {
        if (mGpsStatusChecker != null) {
            mGpsStatusChecker.clearCount();
            mGpsStatusChecker.cancel();
        }
    }

    public void onDestory() {
        if (mGpsStatusChecker != null) {
            mGpsStatusChecker.cancel();
            mGpsStatusChecker = null;
        }
        mContext = null;
    }

    @Override
    public void onLocInfoUpdate(LocInfo locInfo) {
        if (locInfo != null && locInfo.matchInfo != null && locInfo.matchInfo.size() > 0) {
//            Timber.d("onLocInfoUpdate: lon = " + locInfo.matchInfo.get(0).stPos.lon +
//                    ", lat = " + locInfo.matchInfo.get(0).stPos.lat + ", isSimulate = " + locInfo.isSimulate);
        } else {
            Timber.d("onLocInfoUpdate: locInfo is invalid");
        }
        if (null == locInfo) {
            Timber.d("onLocInfoUpdate: locInfo=null and return");
            return;
        }
        //模拟导航无需保存位置信息
        if (locInfo.isSimulate == 1) {
            return;
        }
        Location location = LocationUtil.getInstance().convertLocInfo2Location(locInfo);
        if (null == location) {
            Timber.d("onLocInfoUpdate: convertedlocation=null and return");
            return;
        }
        mLocInfo = locInfo;
        setLastLocation(location);
        boolean isGps = mLocation != null && LocationManager.GPS_PROVIDER.equals(mLocation.getProvider());
        mHandler.obtainMessage(MSG_ON_LOCATION_OK, isGps).sendToTarget();
        if (mLocInfoUpdateListener != null) {
            mLocInfoUpdateListener.onLocationChange(mLocation);
        }
    }

    /**
     * 立刻保存位置
     */
    public void saveCurPos() {
        if (mLocation == null) {
            return;
        }
        Timber.i("saveCurPos lat=" + mLocation.getLatitude() + ", lon = " + mLocation.getLongitude() + ", carAngle = " + mLocation.getBearing());
        mStorage.setLatitude(String.valueOf(mLocation.getLatitude()));
        mStorage.setLongitude(String.valueOf(mLocation.getLongitude()));
        mStorage.setAltitude(String.valueOf(mLocation.getAltitude()));
        mStorage.setBearing(String.valueOf(mLocation.getBearing()));
        mStorage.setTimestamp(System.currentTimeMillis());
        mStorage.setAccuracy(mLocation.getAccuracy());
    }

    /**
     * 添加gps定位状态监听
     * AutoLocStatus.ON_LOCATION_GPS_FAIl 默认超过2s没有原始gps定位回调
     * AutoLocStatus.ON_LOCATION_GPS_OK 原始GPS定位回调
     */
    public void addOriginalGpsLocation(OriginalLocationCallback<SdkLocStatus> originalCallback) {
        if (mOriginalGpsCallbacks == null) {
            mOriginalGpsCallbacks = new CopyOnWriteArrayList<>();
        }
        synchronized (mOriginalGpsCallbacks) {
            if (!mOriginalGpsCallbacks.contains(originalCallback)) {
                this.mOriginalGpsCallbacks.add(originalCallback);
            }
        }
    }

    /**
     * 移除gps定位状态监听
     *
     * @param originalCallback
     */
    public void removeOriginalGpsLocation(OriginalLocationCallback<SdkLocStatus> originalCallback) {
        if (mOriginalGpsCallbacks == null) {
            return;
        }
        synchronized (mOriginalGpsCallbacks) {
            if (mOriginalGpsCallbacks != null) {
                this.mOriginalGpsCallbacks.remove(originalCallback);
            }
        }
    }

    public void clearOriginalLocation() {
        if (mOriginalGpsCallbacks != null && mOriginalGpsCallbacks.size() > 0) {
            mOriginalGpsCallbacks.clear();
        }
    }

    @Override
    public void onTimeOut() {
        if (mHandler != null) {
            mHandler.obtainMessage(MSG_ON_LOCATION_FAIL).sendToTarget();
        } else {
            Timber.d("onTimeOut: mHandler is null");
        }
    }

    /**
     * 给当前位置赋值，
     * 并且每隔3秒保存一次位置
     */
    private void setAndSaveCurPos() {
//        Timber.i("setAndSaveCurPos");
        long time = SystemClock.uptimeMillis();
        if (time - latestSaveTime > POSITION_SAVE_INTERVAL) {
            latestSaveTime = time;
            saveCurPos();
        }
    }

    public Location getLastLocation() {
        if (mLocation == null) {
            initLocation();
        }
        return mLocation;
    }


    public void setLastLocation(Location location) {
        if (mLocation != null && location != null) {
            mLocation.set(location);
            setAndSaveCurPos();
        }
    }

    /**
     * 获取定位回调的位置信息
     *
     * @return
     */
    public LocInfo getLocInfo() {
        return mLocInfo;
    }

    /**
     * 设置位置，目前只用于设置模拟导航速度
     *
     * @param locInfo
     */
    public void setLocInfo(LocInfo locInfo) {
        this.mLocInfo = locInfo;
    }

    /**
     * 设置GNSS定位信息
     *
     * @param gnssInfo LocDataType.LocDataGnss
     */
    public void setGnssInfo(LocGnss gnssInfo) {
        if (mGpsStatusChecker == null) {
            return;
        }
        mGpsStatusChecker.clearCount();
        mHandler.obtainMessage(MSG_ON_LOCATION_GPS_SUCCESS).sendToTarget();
        mGnssInfoData.dataType = LocDataType.LocDataGnss;
        mGnssInfoData.gnss = gnssInfo;
//        Timber.d("====gps sourType = %s", mGnssInfoData.gnss.sourType);
        mPosService.setSignInfo(mGnssInfoData);
    }

    /**
     * 设置卫星星历数据
     *
     * @param locGpgsv 需设置LocDataType.LocDataGpgsv
     */
    public void setGsvInfo(LocGpgsv locGpgsv) {
        mLocGsvSignData.dataType = LocDataType.LocDataGpgsv;
        mLocGsvSignData.gpgsv = locGpgsv;
        mPosService.setSignInfo(mLocGsvSignData);
    }

    public LocGpgsv getGsvInfo() {
        return mLocGsvSignData.gpgsv;
    }

    /**
     * 设置前端融合信号
     *
     * @param locDrPos
     */
    public void setLocDrPosInfo(LocDrPos locDrPos) {
        mLocLocDrSignData.dataType = LocDataType.LocDataDrFusion;
        mLocLocDrSignData.drPos = locDrPos;
        mPosService.setSignInfo(mLocLocDrSignData);
    }

    /**
     * 设置后端融合陀螺信号，频率要求10Hz
     *
     * @param locGyro
     */
    public void setLocGyroInfo(LocGyro locGyro) {
        mLocGyroInfoData.dataType = LocDataType.LocDataGyro;
        mLocGyroInfoData.gyro = locGyro;
        mPosService.setSignInfo(mLocGyroInfoData);
    }

    public LocGyro getLocGyroInfo() {
        return mLocGyroInfoData.gyro;
    }

    /**
     * 设置后端融合加速度计信号，频率要求10Hz
     *
     * @param locAcce3d
     */
    public void setLocAcce3DInfo(LocAcce3d locAcce3d) {
        mLocAcce3DInfoData.dataType = LocDataType.LocDataAcce3D;
        mLocAcce3DInfoData.acce3D = locAcce3d;
        mPosService.setSignInfo(mLocAcce3DInfoData);
    }

    public LocAcce3d getLocAcce3DInfo() {
        return mLocAcce3DInfoData.acce3D;
    }

    /**
     * 设置车速信号，如果是后端融合频率要求10Hz，如果是GPS模式，+车速，则车速频率为1Hz，单位：公里/小时
     *
     * @param locPulse
     */
    public void setLocPulseInfo(LocPulse locPulse) {
        mLocPulseInfoData.dataType = LocDataType.LocDataPulse;
        mLocPulseInfoData.pulse = locPulse;
        mPosService.setSignInfo(mLocPulseInfoData);
    }

    /**
     * 设置仪表车速信号
     *
     * @param speedometer
     */
    public void setLocSpeedometerInfo(LocSpeedometer speedometer) {
        mLocSpeedometerInfoData.dataType = LocDataType.LocDataSpeedometer;
        mLocSpeedometerInfoData.speedometer = speedometer;
        mPosService.setSignInfo(mLocSpeedometerInfoData);
    }

    public LocPulse getLocPulseInfo() {
        return mLocPulseInfoData.pulse;
    }

    /**
     * 获取定位状态
     *
     * @return
     */
    public SdkLocStatus getLocationStatus() {
        return mSdkLocStatus;
    }

    /**
     * 设置定位状态
     *
     * @param sdkLocStatus
     */
    public void setSdkLocStatus(SdkLocStatus sdkLocStatus) {
        if (mSdkLocStatus != sdkLocStatus) {
            mSdkLocStatus = sdkLocStatus;
            if (mHandler != null) {
                int what = mSdkLocStatus == SdkLocStatus.ON_LOCATION_GPS_FAIl ? MSG_ON_LOCATION_FAIL : MSG_ON_LOCATION_OK;
                mHandler.obtainMessage(what).sendToTarget();
            }
        }
    }

    /**
     * 获取车标位置回调信息
     */
    public void addLocInfoUpdate(LocationListener listener) {
        this.mLocInfoUpdateListener = listener;
    }

    /**
     * 移除车标位置回调信息
     */
    public void removeLocInfoUpdate() {
        this.mLocInfoUpdateListener = null;
    }
}
