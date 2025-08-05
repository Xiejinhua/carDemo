package com.autosdk.bussiness.location.utils;

import com.autosdk.bussiness.location.LocationController;

import timber.log.Timber;

/**
 * 定位上下文保存定时器，频率1分钟
 */
public class LocStorageTimer {
    private static final int INTREV_AL = 300000;//300000毫秒 5分钟


    private CustomTimer mTimer;
    private CustomTimerTask mTimerTask;

    /**
     * 定时保存定位上下文
     */
    public void doStart() {
        cancle();
        if (mTimer == null) {
            mTimer = new CustomTimer();
        }
        if (mTimerTask == null) {
            Timber.i( "doStart LocStorageTimer ");
            mTimerTask = new CustomTimerTask() {
                @Override
                public void run() {
                    Timber.i( "saveLocStorage call");
                    LocationController.getInstance().saveLocStorage();
                }
            };
        }
        mTimer.scheduleAtFixedRate(mTimerTask, 0, INTREV_AL);
    }

    /**
     * 取消定时器
     */
    public void cancle() {
        if (mTimer != null && mTimerTask != null) {
            Timber.i( "cancle LocStorageTimer ");
            mTimer.cancel();
            mTimerTask.cancel();
            mTimerTask = null;
            mTimer = null;
        }
        Timber.i( "mTimer.cancel() ");
    }

}
