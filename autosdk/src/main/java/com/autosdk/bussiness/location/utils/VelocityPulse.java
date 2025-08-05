package com.autosdk.bussiness.location.utils;

import android.os.SystemClock;

import com.autonavi.gbl.pos.model.LocDataType;
import com.autonavi.gbl.pos.model.LocPulse;
import com.autonavi.gbl.pos.model.LocSpeedometer;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.location.listener.DisplaySpeedChangedListener;
import com.autosdk.bussiness.location.listener.SpeedChangedListener;

import java.math.BigInteger;

import timber.log.Timber;

/**
 * 车速定时器，频率1s
 */
public class VelocityPulse {

    private static final String TAG = "VelocityPulse";

    private static final int INTREV_AL = 100;

    private SpeedChangedListener mSpeedListener;
    private DisplaySpeedChangedListener mDisplaySpeedListener;

    public void setSpeedListener(SpeedChangedListener speedListener) {
        mSpeedListener = speedListener;
    }

    //设置仪表显示车速监听
    public void setDisplaySpeedListener(DisplaySpeedChangedListener displaySpeedListener) {
        mDisplaySpeedListener = displaySpeedListener;
    }

    private static boolean mPause = false;

    private CustomTimer mTimer;
    private CustomTimerTask mTimerTask;

    /**
     * 开始1s一次获取车速
     */
    public void doStart() {
        cancel();
        if (mTimer == null) {
            mTimer = new CustomTimer();
        }
        if (mTimerTask == null) {
            Timber.i("CarInfoProxyManager doStart2222222 ");
            mTimerTask = new CustomTimerTask() {
                @Override
                public void run() {
                    try {
                        if (mPause)
                            return;
                        float speed = -999;
                        float displaySpeed = -999;
                        if (mSpeedListener != null) {
                            speed = mSpeedListener.getSpeed();
                        }
                        if (mDisplaySpeedListener != null) {
                            displaySpeed = mDisplaySpeedListener.getDisplaySpeed();
                        }
                        long time = SystemClock.elapsedRealtime();
                        LocPulse locPulse = new LocPulse();
                        locPulse.dataType = LocDataType.LocDataPulse;
                        locPulse.interval = INTREV_AL;
                        locPulse.value = speed;
                        locPulse.tickTime = BigInteger.valueOf(time);
                        LocationController.getInstance().setLocPulseInfo(locPulse);

                        LocSpeedometer locSpeedometer = new LocSpeedometer();
                        locSpeedometer.dataType = LocDataType.LocDataSpeedometer;
                        locSpeedometer.interval = INTREV_AL;
                        locSpeedometer.value = displaySpeed;
                        locSpeedometer.tickTime = BigInteger.valueOf(time);
                        LocationController.getInstance().setLocSpeedometerInfo(locSpeedometer);
                    } catch (Exception e) {
                        Timber.d("doStart: Exception is %s", e.toString());
                    }
                }
            };
        }
        mTimer.scheduleAtFixedRate(mTimerTask, 0, INTREV_AL);
    }


    /**
     * 取消定时器
     */
    public void cancel() {
        if (mTimer != null && mTimerTask != null) {
            Timber.i("CarInfoProxyManager cancle111 ");
            mTimer.cancel();
            mTimerTask.cancel();
            mTimerTask = null;
            mTimer = null;
        }
        Timber.i("CarInfoProxyManager mTimer.cancel() ");
    }

    public boolean isCancel() {
        return mTimer == null || mTimerTask.state == CustomTimerTask.CANCELLED;
    }

    public void doPause(boolean pause) {
        mPause = pause;
        Timber.i("doPause %s", mPause);
    }
}
