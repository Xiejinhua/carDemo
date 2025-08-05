package com.autosdk.bussiness.location.listener;

/**
 * 车速回调接口，内部通过定时器1s 一次主动向外部获取车速
 * 外部调用需要实现并设置该监听器
 */
public interface DisplaySpeedChangedListener {
    /**
     * Auto 仪表显示车速监听，单位：千米/小时，如果是倒车，需要传入负数
     */
    int getDisplaySpeed();
}
