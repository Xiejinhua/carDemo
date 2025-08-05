package com.autosdk.common;

/**
 * 导航状态通知
 * @author AutoSDK
 */
public interface NaviStateListener {

    /**
     * 导航状态
     * @param state
     * @see AutoState
     */
    void onNaviStateChanged(int state);
}
