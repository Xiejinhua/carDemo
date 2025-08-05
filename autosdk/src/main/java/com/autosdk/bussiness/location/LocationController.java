package com.autosdk.bussiness.location;

import static android.content.Context.LOCATION_SERVICE;
import static com.autosdk.bussiness.location.instrument.LocationBaseInstrument.GAODE;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.location.LocationManagerCompat;

import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.guide.GuideService;
import com.autonavi.gbl.pos.PosService;
import com.autonavi.gbl.pos.model.LocAcce3d;
import com.autonavi.gbl.pos.model.LocDrPos;
import com.autonavi.gbl.pos.model.LocGnss;
import com.autonavi.gbl.pos.model.LocGpgsv;
import com.autonavi.gbl.pos.model.LocGyro;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.pos.model.LocLogConf;
import com.autonavi.gbl.pos.model.LocModeType;
import com.autonavi.gbl.pos.model.LocPulse;
import com.autonavi.gbl.pos.model.LocSpeedometer;
import com.autonavi.gbl.pos.model.LocSwitchRoadType;
import com.autonavi.gbl.pos.model.PosWorkPath;
import com.autonavi.gbl.pos.observer.IPosLocInfoObserver;
import com.autonavi.gbl.pos.observer.IPosMapMatchFeedbackObserver;
import com.autonavi.gbl.pos.observer.IPosParallelRoadObserver;
import com.autonavi.gbl.pos.observer.IPosSensorParaObserver;
import com.autonavi.gbl.pos.observer.IPosSwitchParallelRoadObserver;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.ServiceInitStatus;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.location.constant.SdkLocStatus;
import com.autosdk.bussiness.location.instrument.LocationBaseInstrument;
import com.autosdk.bussiness.location.listener.DisplaySpeedChangedListener;
import com.autosdk.bussiness.location.listener.LocationListener;
import com.autosdk.bussiness.location.listener.OriginalLocationCallback;
import com.autosdk.bussiness.location.listener.SpeedChangedListener;
import com.autosdk.bussiness.location.utils.GpsStatusChecker;
import com.autosdk.bussiness.location.utils.LocStorageTimer;
import com.autosdk.bussiness.location.utils.VelocityPulse;
import com.autosdk.common.SdkApplicationUtils;

import java.math.BigInteger;

import timber.log.Timber;

/**
 * 定位模块管理
 */
public class LocationController {

    public static String TAG = LocationController.class.getSimpleName();

    private PosService mPosService;

    private LocationBaseInstrument mLocationBaseInsrument;

    /**
     * 保留最新的定位信息
     */
    private Location mLocation;

    /**
     * 定位模式为GPS模式下
     */
    private boolean mGpsTypeNeedSpeed;

    private VelocityPulse mVelocityPulse;

    private LocStorageTimer mLocStorageTimer;

    private GpsStatusChecker mGpsStatusChecker;

    private Location mGnssLocation = null;

    private static class LocManagerHolder {
        private static LocationController mInstance = new LocationController();
    }

    private LocationController() {
    }

    public static LocationController getInstance() {
        return LocManagerHolder.mInstance;
    }

    /**
     * 初始化定位模块
     *
     * @param path             定位工作目录
     * @param locModeType      定位工作模式结构体
     * @param defaultPos       设置的默认位置，在本地没有定位上下文时才生效（一般为第一次安装程序）
     * @param gpsTypeNeedSpeed 定位模式为GNSS模式下是否传入车速用来隧道推算,如果是true，需要另外设置setSpeedListener(SpeedChangedListener l)
     */
    public void initLocEngine(Context context, PosWorkPath path, LocModeType locModeType, Coord3DDouble defaultPos, boolean gpsTypeNeedSpeed, boolean openPosLog) {
        mPosService = (PosService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.PosSingleServiceID);
        if (defaultPos == null) {
            //如果没设置，则使用首开
            defaultPos = new Coord3DDouble(GAODE[0], GAODE[1], 0);
        }
        mPosService.setDefaultPos(defaultPos);
        mPosService.init(path, locModeType);
        mLocationBaseInsrument = new LocationBaseInstrument(context, mPosService);
        mGpsTypeNeedSpeed = gpsTypeNeedSpeed;
        if (gpsTypeNeedSpeed) {
            mVelocityPulse = new VelocityPulse();
        }
        mLocation = mLocationBaseInsrument.getLastLocation();
        switchPosRecord(openPosLog);
        mLocStorageTimer = new LocStorageTimer();
    }

    /**
     * 打开或者关闭定位记录
     *
     * @param open 打开/关闭定位记录
     */
    public void switchPosRecord(boolean open) {
        LocLogConf locConfig = new LocLogConf();
        //750 locConfig已废弃
//        int spaceMaxLimit = 240;
//        int fileMaxLimit = 40;
//        locConfig.spaceLimit = spaceMaxLimit;
//        locConfig.fileLimit = fileMaxLimit;
        //loc日志等级，0：简单定位信息；1：全部信息输出
        mPosService.signalRecordSwitch(open, locConfig);
    }

    /**
     * 开始定位
     */
    public synchronized void doStartLocate() {
        mPosService.addLocInfoObserver(mLocationBaseInsrument, 0);
        mLocationBaseInsrument.doStart();
        if (mGpsTypeNeedSpeed && mVelocityPulse != null) {
            mVelocityPulse.doStart();
        }
        if (mLocStorageTimer != null)
            mLocStorageTimer.doStart();

    }

    /**
     * @param pause 是否暂停输入车速给高德，定位回放会用到
     */
    public void pauseVelocityPulse(boolean pause) {
        if (mGpsTypeNeedSpeed && mVelocityPulse != null) {
            mVelocityPulse.doPause(pause);
        }
    }

    public synchronized void cancelGpsChecker() {
        if (mLocationBaseInsrument != null) {
            mLocationBaseInsrument.doStop();
        }
    }

    /**
     * 停止定位
     */
    public synchronized void doStopLocate() {
        if (mGpsTypeNeedSpeed && mVelocityPulse != null) {
            mVelocityPulse.cancel();
        }
        mLocationBaseInsrument.saveCurPos();
        mLocation = mLocationBaseInsrument.getLastLocation();
        if (mLocStorageTimer != null)
            mLocStorageTimer.cancle();
    }

    /**
     * 反初始化定位模块
     */
    public void uninitLocEngine() {
        mPosService.saveLocStorage();
        doStopLocate();
        mLocationBaseInsrument.doStop();
        mLocationBaseInsrument.onDestory();
        mPosService = null;
        mLocationBaseInsrument = null;
    }

    /**
     * 熄火等的保存定位
     */
    public void saveCurPos() {
        Timber.i("saveCurPos");
        if (mLocationBaseInsrument != null)
            mLocationBaseInsrument.saveCurPos();
        if (mPosService != null)
            mPosService.saveLocStorage();
    }

    /**
     * 保存定位上下文
     */
    public void saveLocStorage() {
        Timber.i("saveLocStorage");
        if (mPosService != null)
            mPosService.saveLocStorage();
    }

    public void doStartTimer() {
        Timber.i("doStartTimer");
        if (mGpsTypeNeedSpeed && mVelocityPulse != null) {
            mVelocityPulse.doStart();
        }
        if (mLocStorageTimer != null)
            mLocStorageTimer.doStart();
    }

    public void doStopTimer() {
        Timber.i("doStopTimer");
        if (mGpsTypeNeedSpeed && mVelocityPulse != null) {
            mVelocityPulse.cancel();
        }
        if (mLocStorageTimer != null)
            mLocStorageTimer.cancle();
    }

    public boolean timerIsRunning() {
        Timber.i("timerRunning");
        return !mVelocityPulse.isCancel();
    }

    /**
     * 添加定位信息观察者
     * 此接口用来添加客户端定位信号回调观察者
     *
     * @param observer
     */
    public void addLocInfoObserver(IPosLocInfoObserver observer) {
        if (mPosService.isInit() == ServiceInitStatus.ServiceInitDone) {
            mPosService.addLocInfoObserver(observer, 0);
        }
    }

    /**
     * 移除定位信息观察者
     *
     * @param observer
     */
    public void removeLocInfoObserver(IPosLocInfoObserver observer) {
        if (mPosService != null && mPosService.isInit() == ServiceInitStatus.ServiceInitDone) {
            mPosService.removeLocInfoObserver(observer);
        }
    }

    /**
     * 添加传感器标定信号回传观察
     * 使用者通过addSensorParaObserver接口把观察者添加到定位引擎中 使用者通过removeSensorParaObserver接口把观察者从定位模块中删除
     *
     * @param pObserver
     */
    public void addSensorParaObserver(IPosSensorParaObserver pObserver) {
        if (mPosService != null) {
            mPosService.addSensorParaObserver(pObserver);
        }
    }

    /**
     * 移除传感器标定信号回传观察
     * 用者通过addSensorParaObserver接口把观察者添加到定位引擎中 使用者通过removeSensorParaObserver接口把观察者从定位模块中删除
     *
     * @param pObserver
     */
    public void removeSensorParaObserver(IPosSensorParaObserver pObserver) {
        if (mPosService != null) {
            mPosService.removeSensorParaObserver(pObserver);
        }
    }

    /**
     * WGS84坐标加密成GCJ02坐标
     *
     * @param wgs84Pos
     * @return
     */
    public Coord3DDouble encryptLonLat(Coord3DDouble wgs84Pos) {
        if (mPosService != null) {
            return PosService.encryptLonLat(wgs84Pos);
        }
        return null;
    }

    /**
     * GPS模式下支持传入车速，用于隧道场景进行车标前进推算
     * 设置车速监听，Auto内部会触发1s一次通过getSpeed()方法获取车速，用于隧道推算
     */
    public void setSpeedListener(SpeedChangedListener speedListener) {
        if (mVelocityPulse != null) {
            Timber.i("setSpeedListener");
            mVelocityPulse.setSpeedListener(speedListener);
        }
    }

    /**
     * 设置仪表车速监听
     */
    public void setDisplaySpeedListener(DisplaySpeedChangedListener displaySpeedListener) {
        if (mVelocityPulse != null) {
            Timber.i("setDisplaySpeedListener");
            mVelocityPulse.setDisplaySpeedListener(displaySpeedListener);
        }
    }

    /**
     * 添加gps定位状态监听
     * AutoLocStatus.ON_LOCATION_GPS_FAIl 默认超过2s没有原始gps定位回调
     * AutoLocStatus.ON_LOCATION_GPS_OK 原始GPS定位回调
     */
    public void addOriginalGpsLocation(OriginalLocationCallback<SdkLocStatus> originalCallback) {
        if (mLocationBaseInsrument != null) {
            mLocationBaseInsrument.addOriginalGpsLocation(originalCallback);
        }
    }

    /**
     * 移除gps定位状态监听
     *
     * @param originalCallback
     */
    public void removeOriginalGpsLocation(OriginalLocationCallback<SdkLocStatus> originalCallback) {
        if (mLocationBaseInsrument != null) {
            mLocationBaseInsrument.removeOriginalGpsLocation(originalCallback);
        }
    }

    /**
     * 保存最新的当前位置
     */
    public void setLastLocation(Location location) {
        if (mLocationBaseInsrument != null) {
            mLocationBaseInsrument.setLastLocation(location);
        }
    }

    private final double[] EngineerPosition = {116.473004, 39.993306};
    private boolean openEngineerPosition = false;

    public void setOpenEngineerPosition(boolean open) {
        Timber.i("setOpenEngineerPosition open=%s", open);
        openEngineerPosition = open;
    }

    public boolean getOpenEngineerPosition() {
        Timber.i("getOpenEngineerPosition");
        return openEngineerPosition;
    }

    public void setEngineerPosition(double lon, double lat) {
        Timber.i("setEngineerPosition lon=%s lat=%s", lon, lat);
        if (lon >= 0 && lon <= 180 && lat >= 0 && lat <= 90) {
            openEngineerPosition = true;
            EngineerPosition[0] = lon;
            EngineerPosition[1] = lat;
        }
    }

    /**
     * 获取最新的当前位置
     *
     * @return
     */
    public Location getLastLocation() {
        if (mLocationBaseInsrument != null) {
            mLocation = mLocationBaseInsrument.getLastLocation();
        }
        if (mLocation == null) {
            Timber.d("getLastLocation mLocationBaseInsrument = %s, mLocation = %s", mLocationBaseInsrument, mLocation);
            Location location = new Location("default");
            //可以判断这时引擎未初始化,为了不让外部异常,这里统一返回首开
            location.setLongitude(GAODE[0]);
            location.setLatitude(GAODE[1]);
            location.setBearing(-1);
            location.setTime(System.currentTimeMillis());
            return location;
        }
        if (openEngineerPosition) {
            mLocation.setLongitude(EngineerPosition[0]);
            mLocation.setLatitude(EngineerPosition[1]);
            mLocation.setBearing(-1);
        }
        return mLocation;
    }

    /**
     * 获取定位回调的位置信息
     *
     * @return
     */
    public LocInfo getLocInfo() {
        if (mLocationBaseInsrument != null) {
            return mLocationBaseInsrument.getLocInfo();
        }
        return null;
    }

    public void setLocInfo(LocInfo locInfo) {
        if (mLocationBaseInsrument != null) {
            mLocationBaseInsrument.setLocInfo(locInfo);
        }
    }

    /**
     * tbt服务绑定定位服务
     *
     * @param service GuideService
     */
    public void bindPosServiceToGuide(GuideService service) {
//        ServiceMgr.getServiceMgrInstance().bindPos2Guide(mPosService, service);
//        Log.i("bindPosServiceToGuide: " + ServiceMgr.getServiceMgrInstance().bindPos2Guide(mPosService, service));
    }

    /**
     * 切换主辅路、高架
     *
     * @return
     */
    public void switchParallelRoad(@LocSwitchRoadType.LocSwitchRoadType1 int switchRoadType, BigInteger roadid) {
        if (mPosService != null) {
            Timber.d("switchParallelRoad: switchRoadType=%s", switchRoadType);
            mPosService.switchParallelRoad(switchRoadType, roadid);
        }
    }

    /**
     * 添加主辅路信息观察者
     *
     * @param pObserver
     */
    public void addPosParallelRoadObserver(IPosParallelRoadObserver pObserver) {
        if (mPosService != null) {
            mPosService.addParallelRoadObserver(pObserver);
        }
    }

    /**
     * 移除主辅路信息观察者
     *
     * @param pObserver
     */
    public void removePosParallelRoadObserver(IPosParallelRoadObserver pObserver) {
        if (mPosService != null) {
            mPosService.removeParallelRoadObserver(pObserver);
        }
    }

    /**
     * 添加主辅路切换完成通知
     * 使用者通过addSwitchParallelRoadObserver接口把观察者添加到定位引擎中 使用者通过removeSwitchParallelRoadObserver接口把观察者从定位模块中删除
     *
     * @param pObserver
     */
    public void addSwitchParallelRoadObserver(IPosSwitchParallelRoadObserver pObserver) {
        if (mPosService != null) {
            mPosService.addSwitchParallelRoadObserver(pObserver);
        }
    }

    /**
     * 移除主辅路切换完成通知
     * 使用者通过addSwitchParallelRoadObserver接口把观察者添加到定位引擎中 使用者通过removeSwitchParallelRoadObserver接口把观察者从定位模块中删除
     *
     * @param pObserver
     */
    public void removeSwitchParallelRoadObserver(IPosSwitchParallelRoadObserver pObserver) {
        if (mPosService != null) {
            mPosService.removeSwitchParallelRoadObserver(pObserver);
        }
    }

    /**
     * 添加地图匹配反馈回调
     * 使用者通过addMapMatchFeedbackObserver接口把观察者添加到定位引擎中 使用者通过removeMapMatchFeedbackObserver接口把观察者从定位模块中删除
     *
     * @param observer
     */
    public void addMapMatchFeedbackObserver(IPosMapMatchFeedbackObserver observer) {
        if (mPosService != null) {
            mPosService.addMapMatchFeedbackObserver(observer);
        }
    }

    /**
     * 移除地图匹配反馈回调
     * 使用者通过addMapMatchFeedbackObserver接口把观察者添加到定位引擎中 使用者通过removeMapMatchFeedbackObserver接口把观察者从定位模块中删除
     *
     * @param observer
     */
    public void removeMapMatchFeedbackObserver(IPosMapMatchFeedbackObserver observer) {
        if (mPosService != null) {
            mPosService.removeMapMatchFeedbackObserver(observer);
        }
    }

    /**
     * 设置GNSS定位信息
     * 要求频率1s一次
     *
     * @param gnssInfo 需设置LocDataType.LocDataGnss
     */
    public void setGnssInfo(LocGnss gnssInfo) {
        if (mPosService != null && mLocationBaseInsrument != null && gnssInfo != null) {
            openEngineerPosition = false;
            mLocationBaseInsrument.setGnssInfo(gnssInfo);
        }
    }

    /**
     * 设置卫星星历数据
     * 要求频率1s一次
     *
     * @param locGpgsv 需设置LocDataType.LocDataGpgsv
     */
    public void setGsvInfo(LocGpgsv locGpgsv) {
        if (mPosService != null && mLocationBaseInsrument != null && locGpgsv != null) {
            mLocationBaseInsrument.setGsvInfo(locGpgsv);
        }
    }

    /**
     * 设置前端融合信号
     *
     * @param locDrPosInfo
     */
    public void setLocDrPosInfo(LocDrPos locDrPosInfo) {
        if (mPosService != null && mLocationBaseInsrument != null && locDrPosInfo != null) {
            mLocationBaseInsrument.setLocDrPosInfo(locDrPosInfo);
        }
    }

    /**
     * 设置后端融合陀螺信号，频率要求10Hz
     *
     * @param locGyro
     */
    public void setLocGyroInfo(LocGyro locGyro) {
        if (mPosService != null && mLocationBaseInsrument != null && locGyro != null) {
            mLocationBaseInsrument.setLocGyroInfo(locGyro);
        }
    }

    /**
     * 设置后端融合加速度计信号，频率要求10Hz
     *
     * @param locAcce3d
     */
    public void setLocAcce3DInfo(LocAcce3d locAcce3d) {
        if (mPosService != null && mLocationBaseInsrument != null && locAcce3d != null) {
            mLocationBaseInsrument.setLocAcce3DInfo(locAcce3d);
        }
    }

    /**
     * 设置车速信号，如果是后端融合频率要求10Hz，如果是GPS模式，+车速，则车速频率为1Hz，单位：公里/小时
     *
     * @param locPulse
     */
    public void setLocPulseInfo(LocPulse locPulse) {
        if (mPosService != null && mLocationBaseInsrument != null && locPulse != null) {
            mLocationBaseInsrument.setLocPulseInfo(locPulse);
        }
    }
    /**
     * 设置仪表车速信号
     *
     * @param locSpeedometer
     */
    public void setLocSpeedometerInfo(LocSpeedometer locSpeedometer) {
        if (mPosService != null && mLocationBaseInsrument != null && locSpeedometer != null) {
            mLocationBaseInsrument.setLocSpeedometerInfo(locSpeedometer);
        }
    }

    public void setLocationStatus(SdkLocStatus sdkLocStatus) {
        mLocationBaseInsrument.setSdkLocStatus(sdkLocStatus);
    }

    /**
     * 获取定位状态
     *
     * @return
     */
    public SdkLocStatus getLocationStatus() {
        if (mPosService != null && mLocationBaseInsrument != null) {
            return mLocationBaseInsrument.getLocationStatus();
        }
        return SdkLocStatus.ON_LOCATION_FAIL;
    }

    /**
     * 设置客户端最后保存的位置上
     */
    public void setContextPos(Coord3DDouble contextPos, float heading) {
        if (mPosService != null) {
            mPosService.setContextPos(contextPos, heading);
        }
    }

    /**
     * 获取定位sdk的底层服务
     *
     * @return
     */
    public PosService getPosService() {
        return mPosService;
    }

    public boolean isLocationEnabled() {
        LocationManager mLocationManager = (LocationManager) SdkApplicationUtils.getApplication().getSystemService(LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(mLocationManager);
    }

    /**
     * 获取原生GNSS定位信息
     *
     * @return
     */
    public Location getGnssLocation() {
        return mGnssLocation;
    }

    public void setGnssLocation(Location location) {
        mGnssLocation = location;
    }

    /**
     * 获取卫星颗数
     *
     * @return
     */
    public int getGsvNum() {
        if (mLocationBaseInsrument != null && mLocationBaseInsrument.getGsvInfo() != null) {
            return mLocationBaseInsrument.getGsvInfo().num;
        }
        return 0;
    }

    /**
     * 获取传递给SDK的车速
     *
     * @return 传递给SDK的车速
     */
    public float getSpeed() {
        if (mLocationBaseInsrument != null && mLocationBaseInsrument.getLocPulseInfo() != null) {
            return mLocationBaseInsrument.getLocPulseInfo().value;
        }
        return 0;
    }

    public LocGyro getLocGyroInfo() {
        if (mLocationBaseInsrument != null) {
            return mLocationBaseInsrument.getLocGyroInfo();
        }
        return null;
    }

    public LocAcce3d getLocAcce3DInfo() {
        if (mLocationBaseInsrument != null) {
            return mLocationBaseInsrument.getLocAcce3DInfo();
        }
        return null;
    }

    /**
     * 获取车标位置回调信息
     */
    public void addLocInfoUpdate(LocationListener listener) {
        if (mLocationBaseInsrument != null) {
            mLocationBaseInsrument.addLocInfoUpdate(listener);
        }
    }

    /**
     * 移除车标位置回调信息
     */
    public void removeLocInfoUpdate() {
        if (mLocationBaseInsrument != null) {
            mLocationBaseInsrument.removeLocInfoUpdate();
        }
    }
}
