package com.autosdk.common.lane;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 车道回放的模式
 */
public class LaneMockMode {

    public static final int LANE_MOCK_DISABLE = -1;

    /**
     * 算路模拟回放
     */
    public static final int LANE_MOCK_MODE_SIM = 0;

    /**
     * 仿真回放
     */
    public static final int LANE_MOCK_MODE_REPLAY = 1;

    @IntDef({LANE_MOCK_DISABLE, LANE_MOCK_MODE_SIM, LANE_MOCK_MODE_REPLAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LaneMockMode1 {
    }
}
