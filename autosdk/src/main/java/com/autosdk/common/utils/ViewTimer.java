package com.autosdk.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.autosdk.common.SdkApplicationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * 时间显示控制
 */
public class ViewTimer {
    /**
     * 定时器
     */
    private final static int TIME = 1;
    private final static int DELAYED_TIME = 60 * 1000;
    private final static int PRECISE_TIME_DELAYED_TIME = 1 * 1000;
    private static ViewTimer instance;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private List<IOnTimerListener> listeners = new ArrayList<IOnTimerListener>();
    private Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Timber.d("ViewTimer", "handle msg.what=%s", msg.what);
            switch (msg.what) {
                case TIME: {
                    doCallback();
                    handle.sendEmptyMessageDelayed(TIME, DELAYED_TIME);
                    break;
                }

                default:
                    break;
            }
        }
    };

    private ViewTimer() {
        registerReceiver();
    }

    public static ViewTimer getInstance() {
        if (instance == null) {
            instance = new ViewTimer();
        }
        return instance;
    }

    public void destory() {
        if (null != instance) {
            instance.handle.removeCallbacksAndMessages(null);
            instance.listeners.clear();
            instance.unRegisterReceiver();
            instance = null;
        }
    }

    public void addListener(IOnTimerListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(IOnTimerListener listener) {
        if (listener != null && listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    /**
     * 开始倒计时
     */
    public void startTimer() {
        Date date = new Date(System.currentTimeMillis());
        String time = sdf.format(date);
        String[] times = time.split(":");
        Timber.d("ViewTimer", "startTimer time=%s", time);
        if (times != null && times.length == 3) {
            int delayed = (60 - Integer.parseInt(times[2])) * 1000;
            /**
             * 如果第一次不是刚好整分钟的，则立即回调一次，避免无法及时刷新
             */
            handle.removeCallbacksAndMessages(null);
            handle.sendEmptyMessageDelayed(TIME, delayed);
            Timber.d("ViewTimer", "startTimer delayed=%s", delayed);
            if (delayed > 0) {
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        doCallback();
                    }
                });
            }
        }
    }

    private void doCallback() {
        Timber.d("ViewTimer", "doCallback");
        for (IOnTimerListener listener : listeners) {
            if (listener != null) {
                listener.onChange();
            }
        }
    }

    public interface IOnTimerListener {
        void onChange();
    }

    public static boolean isTime12() {
        String timeFormat = android.provider.Settings.System.getString(SdkApplicationUtils.getApplication().getContentResolver(),
                android.provider.Settings.System.TIME_12_24);
        Timber.d("ViewTimer", "timeFormat=%s", timeFormat);
        if (timeFormat != null) {
            if ("24".equals(timeFormat)) {
                return false;
            }
        }
        return true;
    }


    DataChangeReceiver receiver = new DataChangeReceiver();

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        SdkApplicationUtils.getApplication().registerReceiver(receiver, filter);
    }

    private void unRegisterReceiver() {
        SdkApplicationUtils.getApplication().unregisterReceiver(receiver);
    }

    public class DataChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("ViewTimer", "DataChangeReceiver onReceive action=%s", intent.getAction());
            if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                Timber.d("ViewTimer", "time change.");
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * 时区或者时间发生变化后要重新初始化，不然会更新时间和原有系统时间对应不上的问题
                         */
                        Timber.d("ViewTimer", "startTimer.");
                        startTimer();
                    }
                });
            }

        }
    }

}
