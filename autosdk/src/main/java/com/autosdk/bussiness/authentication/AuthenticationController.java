package com.autosdk.bussiness.authentication;


import android.os.Bundle;
import android.os.Message;

import com.autonavi.gbl.activation.AuthenticationService;
import com.autonavi.gbl.activation.model.AuthenticationGoodsInfo;
import com.autonavi.gbl.activation.model.AuthenticationResult;
import com.autonavi.gbl.activation.observer.IAuthenticationObserver;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.ServiceInitStatus;
import com.autonavi.gbl.util.model.SingleServiceID;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 鉴权模块Controller
 */
public class AuthenticationController implements IAuthenticationObserver {

    public static String TAG = AuthenticationController.class.getSimpleName();

    /**
     * 鉴权服务观察者
     */
    private List<IAuthenticationObserver> mAuthenticationObserverSet;

    /**
     * SDK鉴权模块服务
     */
    private AuthenticationService mAuthenticationService;

    private AuthenticationMessenger mMessenger;

    private static class AuthenticationManagerHolder {
        private static AuthenticationController mInstance = new AuthenticationController();
    }

    private AuthenticationController() {
        mMessenger = AuthenticationMessenger.getInstance();
    }

    public static AuthenticationController getInstance() {
        return AuthenticationManagerHolder.mInstance;
    }

    /**
     * 初始化
     */
    public void init() {
        mAuthenticationService = (AuthenticationService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.AuthenticationServiceID);
        if(mAuthenticationService != null && mAuthenticationService.isInit() != ServiceInitStatus.ServiceInitDone) {
            mAuthenticationService.init();
            mAuthenticationService.addObserver(this);
        }
    }

    /**
     * 反初始化
     */
    public void unInit() {
        if(mAuthenticationService != null) {
            mAuthenticationService.removeObserver(this);
            mAuthenticationObserverSet.clear();
            mAuthenticationService.unInit();
        }
    }

    @Override
    public void onStatusUpdated(int reqId, ArrayList<AuthenticationGoodsInfo> info) {
        Message msg = mMessenger.newMessage(AuthenticationMessenger.HANDLER_ON_STATUS_UPDATED);
        msg.obj = info;
        Bundle bundle = new Bundle();
        bundle.putInt("reqId", reqId);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onError(AuthenticationResult result) {
        Message msg = mMessenger.newMessage(AuthenticationMessenger.HANDLER_ON_ERROR);
        msg.obj = result;
        mMessenger.sendMessage(msg);
    }

    /**
     * 注册鉴权观察者
     * @param observer
     */
    public void registerAuthenticationObserver(IAuthenticationObserver observer) {
        if (mAuthenticationObserverSet == null) {
            mAuthenticationObserverSet = new CopyOnWriteArrayList<>();
        }
        if (!mAuthenticationObserverSet.contains(observer)) {
            mAuthenticationObserverSet.add(observer);
        }
    }

    /**
     * 反注册鉴权观察者
     * @param observer
     */
    public void unRegisterAuthenticationObserver(IAuthenticationObserver observer) {
        if (mAuthenticationObserverSet != null && mAuthenticationObserverSet.contains(observer)) {
            mAuthenticationObserverSet.remove(observer);
        }
    }

    public List<IAuthenticationObserver> getAuthenticationObservers() {
        return mAuthenticationObserverSet;
    }

    /**
     * 获取鉴权商品信息
     * @param functionId 功能ID
     * @return
     */
    public ArrayList<AuthenticationGoodsInfo> getAuthenticationGoodsInfo(int functionId) {
        if(mAuthenticationService != null) {
            return mAuthenticationService.getGoodsInfoById(functionId);
        }
        return null;
    }

    /**
     * 同步鉴权信息
     */
    public void syncRequest() {
        if(mAuthenticationService != null) {
            mAuthenticationService.syncRequest();
        }
    }
}
