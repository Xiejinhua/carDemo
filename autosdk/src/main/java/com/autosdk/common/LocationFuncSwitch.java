package com.autosdk.common;


import com.autonavi.gbl.pos.model.LocFuncSwitch;

/**
 * 定义了常见的定位模块功能组合
 * HMI根据具体业务需求进行修改或者增加
 */
public class LocationFuncSwitch {

    /**
     * GNSS模式常用功能
     */
    public final static int GNSS = LocFuncSwitch.LocFuncDelayTurningLowSpeed | LocFuncSwitch.LocFuncTurningMainSideRoad | LocFuncSwitch.LocFuncConfusingTurning |
            LocFuncSwitch.LocFuncPosDataEnable | LocFuncSwitch.LocFuncReroutingRejectorPassOver | LocFuncSwitch.LocFuncReroutingRejectorARS |
            LocFuncSwitch.LocFuncReroutingRejectorDist | LocFuncSwitch.LocFuncReroutingByCruiseTunnelCorrection; //导航辅助偏航插件

    /**
     * DR前端融合模式默认功能组合
     */
    public final static int DR_FRONT_DEFAULT = LocFuncSwitch.LocFuncConfusingTurning | LocFuncSwitch.LocFuncMainSideRoadDecision | LocFuncSwitch.LocFuncForkQuickCorrection |
            LocFuncSwitch.LocFuncPosDataEnable     // 巡航
            | LocFuncSwitch.LocFuncReroutingRejectorPassOver | LocFuncSwitch.LocFuncReroutingRejectorARS | LocFuncSwitch.LocFuncReroutingRejectorDist;   // 导航

    /**
     * 后端融合(三轴陀螺+三轴加速度计)(0xf39ba或者997818)
     */
    public final static int DR_BACK_DEFAULT = LocFuncSwitch.LocFuncParallelRecognize | LocFuncSwitch.LocFuncDivergingPathsRecognize | LocFuncSwitch.LocFuncTurningSmooth   // 巡航
                            | LocFuncSwitch.LocFuncTurningMainSideRoad | LocFuncSwitch.LocFuncUTurnMatch | LocFuncSwitch.LocFuncAbsolutePosCorrection   // 巡航
                            | LocFuncSwitch.LocFuncTunnelCorrection | LocFuncSwitch.LocFuncLeaveRoundabout | LocFuncSwitch.LocFuncPosDataEnable          // 巡航
                            | LocFuncSwitch.LocFuncReroutingRejectorPassOver | LocFuncSwitch.LocFuncReroutingRejectorARS | LocFuncSwitch.LocFuncReroutingRejectorDist    // 导航
                            | LocFuncSwitch.LocFuncReroutingRejectorCross
                            | LocFuncSwitch.LocFuncViaductRecognize| LocFuncSwitch.LocFuncReroutingByCruiseViaduct;//高架识别

}
