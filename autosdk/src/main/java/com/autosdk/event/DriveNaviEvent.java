package com.autosdk.event;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 导航状态
 */
public interface DriveNaviEvent {

    @IntDef({
        GUIDE_START,
        GUIDE_STOP,
        ARRIVE_DESTINATION,
        SIMULATE_GUIDE_START,
        SIMULATE_GUIDE_PAUSE,
        SIMULATE_GUIDE_STOP
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DriveNaviEventVal {}

    /**
     * 开始导航
     */
    int GUIDE_START = 11;

    /**
     * 结束导航
     */
    int GUIDE_STOP = 12;

    /**
     * 导航到达目的地
     */
    int ARRIVE_DESTINATION = 13;

    /**
     * 开始模拟导航
     */
    int SIMULATE_GUIDE_START = 14;

    /**
     * 暂停模拟导航
     */
    int SIMULATE_GUIDE_PAUSE = 15;

    /**
     * 结束模拟导航
     */
    int SIMULATE_GUIDE_STOP = 16;

    /**
     * 退出导航页面
     */
    int EXIT_GUIDE = 17;

}