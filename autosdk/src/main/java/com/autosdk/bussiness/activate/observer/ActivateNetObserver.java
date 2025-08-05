package com.autosdk.bussiness.activate.observer;

import com.autosdk.bussiness.manager.SDKInitParams;

/**
 * @author AutoSDk
 */
public interface ActivateNetObserver {

    void notifyNetActivate(int resultCode,int type);
}
