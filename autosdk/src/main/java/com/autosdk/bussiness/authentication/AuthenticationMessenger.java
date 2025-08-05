package com.autosdk.bussiness.authentication;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.autonavi.gbl.activation.model.AuthenticationGoodsInfo;
import com.autonavi.gbl.activation.model.AuthenticationResult;
import com.autonavi.gbl.activation.observer.IAuthenticationObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * 该类负责将来自SDK鉴权线程的消息转换到ui线程中，并通知相关的HMI listener.
 */
public class AuthenticationMessenger {

    public static final String TAG = AuthenticationMessenger.class.getSimpleName();

    /**
     * 鉴权商品信息状态更新
     */
    public static final int HANDLER_ON_STATUS_UPDATED = 100000;
    /**
     * 鉴权失败
     */
    public static final int HANDLER_ON_ERROR = 100001;

    private static class AuthenticationUtilsHolder {
        private static AuthenticationMessenger mInstance = new AuthenticationMessenger();
    }

    public static AuthenticationMessenger getInstance() {
        return AuthenticationUtilsHolder.mInstance;
    }

    public void sendMessage(Message msg) {
        mMessenger.sendMessage(msg);
    }

    public void sendMessage(int what) {
        mMessenger.sendMessage(newMessage(what));
    }

    public void sendMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.obj = obj;
        msg.what = what;
        mMessenger.sendMessage(msg);
    }

    public Message newMessage(int what){
        Message msg = Message.obtain();
        msg.what = what;
        return msg;
    }

    public void clearAllMessages() {
        mMessenger.removeCallbacksAndMessages(null);
    }

    private final Handler mMessenger = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            List<IAuthenticationObserver> mAuthenticationObserverSet = AuthenticationController.getInstance().getAuthenticationObservers();
            switch (what) {
                case HANDLER_ON_STATUS_UPDATED:
                    if(mAuthenticationObserverSet != null) {
                        Bundle bundle = msg.getData();
                        for(IAuthenticationObserver observer : mAuthenticationObserverSet) {
                            observer.onStatusUpdated(bundle.getInt("reqId"), (ArrayList<AuthenticationGoodsInfo>) msg.obj);
                        }
                    }
                    break;
                case HANDLER_ON_ERROR:
                    if(mAuthenticationObserverSet != null) {
                        for(IAuthenticationObserver observer : mAuthenticationObserverSet) {
                            observer.onError((AuthenticationResult) msg.obj);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
