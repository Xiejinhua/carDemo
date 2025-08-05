package com.desaysv.psmap.base.config

import android.util.Log
import com.autonavi.gbl.servicemanager.model.ALCLogLevel
import com.autosdk.common.lane.LaneMockMode
import com.autosdk.common.lane.LaneMockMode.LANE_MOCK_DISABLE

/**
 * 工程模式配置
 */
class AutoEggConfig {
    var blLogLevel = ALCLogLevel.LogLevelNone
    var isBlLogcat = true

    /**
     * 是否开启定位日志
     */
    var isBlPosLog = true
    var hmiLogLevel = Log.DEBUG
    var isDev = false

    /**
     * 是否关闭后端融合，工程模式定位回放用到
     */
    var offDRBack = false

    /**
     * 是否开启测试UUID
     */
    var openMapTestUuid = false

    /**
     * 测试UUID
     */
    var mapTestUuid = ""

    //车道模拟仿真回放模式
    @LaneMockMode.LaneMockMode1
    var laneMockMode = LANE_MOCK_DISABLE

    /**
     * 前台帧率
     */
    var foreground_MapRenderModeNormal = FOREGROUND_MapRenderModeNormal// 正常场景下的帧率
    var foreground_MapRenderModeNavi = FOREGROUND_MapRenderModeNavi//导航场景下的帧率
    var foreground_MapRenderModeAnimation = FOREGROUND_MapRenderModeAnimation// 动画场景下的帧率
    var foreground_MapRenderModeGestureAction = FOREGROUND_MapRenderModeGestureAction//手势操作时帧率

    /**
     * 后台帧率
     */
    var backend_MapRenderModeNormal = BACKEND_MapRenderModeNormal// 正常场景下的帧率
    var backend_MapRenderModeNavi = BACKEND_MapRenderModeNavi//导航场景下的帧率
    var backend_MapRenderModeAnimation = BACKEND_MapRenderModeAnimation// 动画场景下的帧率
    var backend_MapRenderModeGestureAction = BACKEND_MapRenderModeGestureAction//手势操作时帧率

    /**
     * GPS调试是否打开
     */
    var gpsTest = false

    /**
     * 定位回放是否打开
     */
    var replayPosTest = false

    /**
     * 是否开启接续算路
     */
    var elecContinue = false

    /**
     * 是否开启用户家/公司的位置预测
     */
    var addressPredict = false

    /**
     * 是否开启同步常去地点(家、公司)数据
     */
    var frequentData = true

    companion object {
        /**
         * 前台帧率
         */
        const val FOREGROUND_MapRenderModeNormal = 20// 正常场景下的帧率
        const val FOREGROUND_MapRenderModeNavi = 20//导航场景下的帧率
        const val FOREGROUND_MapRenderModeAnimation = 30// 动画场景下的帧率
        const val FOREGROUND_MapRenderModeGestureAction = 40//手势操作时帧率

        /**
         * 后台帧率
         */
        const val BACKEND_MapRenderModeNormal = 10// 正常场景下的帧率
        const val BACKEND_MapRenderModeNavi = 10//导航场景下的帧率
        const val BACKEND_MapRenderModeAnimation = 20// 动画场景下的帧率
        const val BACKEND_MapRenderModeGestureAction = 10//手势操作时帧率
    }

}
