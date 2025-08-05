package com.autosdk.adapter;

import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.autosdk.adapter.callback.IElectricInfoCallBack;
import com.autosdk.common.AutoState;
import com.autosdk.common.NaviStateListener;
import com.autosdk.common.SdkApplicationUtils;
import com.autosdk.service.IModuleAdapterService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

/**
 * SdkAdapter控制器
 * 管理收发广播的具体操作业务逻辑
 * 降低工程代码中广播回调的代码侵入
 * Created by AutoSdk on 2021/5/27.
 **/
public class SdkAdapterManager implements IModuleAdapterService {
    public static String TAG = SdkAdapterManager.class.getSimpleName();

    private List<NaviStateListener> mNaviStatusListeners = new CopyOnWriteArrayList<>();

    private static class SdkAdapterHolder {
        private static final SdkAdapterManager mInstance = new SdkAdapterManager();

    }

    public static SdkAdapterManager getInstance() {
        return SdkAdapterHolder.mInstance;
    }

    private MyHandler broadcastHandler;


    private class MyHandler extends Handler {
        public MyHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                return;
            }
            final Intent sendIntent = new Intent(AdapterConstants.ACTION_SEND);
            switch (msg.what) {
                case 1://导航状态
                    int value = msg.arg1;
                    sendIntent.putExtra(AdapterConstants.KEY_TYPE, AdapterConstants.EXTRA_SEND_KEY_VALUE);
                    sendIntent.putExtra(AdapterConstants.EXTRA_STATE, value);
                    SdkApplicationUtils.getApplication().sendBroadcast(sendIntent);

                    if (mNaviStatusListeners != null && mNaviStatusListeners.size() > 0) {
                        for (int i = 0; i < mNaviStatusListeners.size(); i++) {
                            mNaviStatusListeners.get(i).onNaviStateChanged(value);
                        }
                    }
                    break;
                case 2:
                    sendIntent.putExtra(AdapterConstants.KEY_TYPE, AdapterConstants.EXTRA_SEND_KEY_VALUE);
                    sendIntent.putExtra(AdapterConstants.EXTRA_CROSS_MAP, msg.arg1);
                    SdkApplicationUtils.getApplication().sendBroadcast(sendIntent);
                    break;
                case 3:
                    sendIntent.putExtra(AdapterConstants.KEY_TYPE, AdapterConstants.EXTRA_DAY_NIGHT_KEY_VALUE);
                    sendIntent.putExtra(AdapterConstants.EXTRA_DAY_NIGHT_MODE, msg.arg1);
                    SdkApplicationUtils.getApplication().sendBroadcast(sendIntent);
                case 4:
                    sendIntent.putExtra(AdapterConstants.KEY_TYPE, AdapterConstants.EXTRA_AUTO_LOGIN_KEY_VALUE);
                    sendIntent.putExtra(AdapterConstants.EXTRA_LOGIN_ACCOUNT, msg.arg1);
                    SdkApplicationUtils.getApplication().sendBroadcast(sendIntent);
            }
        }
    }

    @Override
    public void startup() {
        Timber.i("startup");
        HandlerThread naviStateBroadcast = new HandlerThread("NaviStateBroadcast");
        naviStateBroadcast.start();
        broadcastHandler = new MyHandler(naviStateBroadcast.getLooper());
    }

    @Override
    public void destroy() {
        Timber.i("destroy");
        broadcastHandler = null;

    }

    public void sendNormalMessage(int value) {
        if (broadcastHandler != null)
            broadcastHandler.obtainMessage(1, value, value).sendToTarget();
    }

    public void sendCrossMessage(int isShow) {
        if (broadcastHandler != null)
            broadcastHandler.obtainMessage(2, isShow, isShow).sendToTarget();
    }

    @Override
    public void sendLoginMessage(int value) {
        if (broadcastHandler != null)
            broadcastHandler.obtainMessage(4, value, value).sendToTarget();
    }

    @Override
    public void sendDayNightMessage(int value) {
        if (broadcastHandler != null)
            broadcastHandler.obtainMessage(3, value, value).sendToTarget();
    }

    public void beginCount(@AdapterConstants.CountType String countType) {

    }

    public void endCount(@AdapterConstants.CountType String countType) {

    }

    public void addElectricInfoCallBack(IElectricInfoCallBack callBack) {

    }

    public void removeElectricInfoCallBack(IElectricInfoCallBack callBack) {

    }

    @Override
    public List<IElectricInfoCallBack> getElectricInfoCallBackList() {
        return null;
    }

    /**
     * 注册导航状态通知监听
     *
     * @param listener
     */
    public void addNaviStatusListener(NaviStateListener listener) {
        if (mNaviStatusListeners == null) {
            mNaviStatusListeners = new CopyOnWriteArrayList<>();
        }
        synchronized (mNaviStatusListeners) {
            if (!mNaviStatusListeners.contains(listener)) {
                this.mNaviStatusListeners.add(listener);
            }
        }
    }

    /**
     * 移除导航状态消息通知监听
     *
     * @param listener
     */
    public void removeNaviStatusListener(NaviStateListener listener) {
        if (mNaviStatusListeners == null) {
            return;
        }
        synchronized (mNaviStatusListeners) {
            if (mNaviStatusListeners != null) {
                this.mNaviStatusListeners.remove(listener);
            }
        }
    }
}
