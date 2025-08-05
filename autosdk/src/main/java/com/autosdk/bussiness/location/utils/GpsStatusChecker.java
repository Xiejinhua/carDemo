package com.autosdk.bussiness.location.utils;


import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

/**
 *  GPS定位计时器，默认2s未收到定位信息，则判断为未定位
 */
public class GpsStatusChecker extends Thread {

    private static final int TIME_OUT = 2;
    private volatile int mTickCount = 0;

    private volatile boolean mRun = false;
    private AtomicInteger mState = new AtomicInteger(0);
    private OnTimeOutCallback callback;

    public GpsStatusChecker(){
        super("GpsStatusChecker");
    }

    public void setTimeOutListener(OnTimeOutCallback c){
        callback = c;
    }

    @Override
    public void run() {
        super.run();
        mRun = true;
        while (mRun) {
            switch (mState.get()) {
                case 0:
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    break;
                case 1:
                    try {
                        boolean isTimeout = false;
                        synchronized(this){
                            mTickCount++;
                            if (mTickCount > TIME_OUT) {
                                mTickCount = 0;
                                isTimeout = true;
                            }
                        }

                        if (callback != null && isTimeout) {
                            Timber.d("GpsStatusChecker onTimeOut");
                            callback.onTimeOut();
                        }
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (Exception e) {

                    }
                    break;
                default:
                    break;
            }
        }
    }

    public synchronized void clearCount() {
//        Timber.d("GpsStatusChecker clearCount");
        mTickCount = 0;
    }

    public synchronized void cancel() {
        mRun = false;
        notify();
    }

    public synchronized void doWait() {
        if (mState.get() == 0) {
            return;
        }
        mState.set(0);
        mTickCount = 0;
        notify();
    }

    public synchronized void doCount() {
        if (mState.get() == 1) {
            return;
        }
        mState.set(1);
        mTickCount = 0;
        notify();
    }

    public interface OnTimeOutCallback{
        public void onTimeOut();
    }
}
