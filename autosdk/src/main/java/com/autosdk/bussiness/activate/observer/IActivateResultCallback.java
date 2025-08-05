package com.autosdk.bussiness.activate.observer;

/**
 * 激活结果回调
 */
public interface IActivateResultCallback {

    void notifyActivateResult(int resultCode,int type);
}
