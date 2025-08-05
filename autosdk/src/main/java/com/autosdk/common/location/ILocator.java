package com.autosdk.common.location;

/**
 * HMI定位相关接口
 */
public interface ILocator {

    void doStartLocate();

    void doStopLocate();

    void onDestory();

    /**
     * 设置是否模拟回放模式
     * @param isMock
     */
    void setPosMockMode(boolean isMock);

    default void doTimerStart() {

    }

    default void doTimerStop() {

    }

}
