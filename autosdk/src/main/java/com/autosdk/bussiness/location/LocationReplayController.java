package com.autosdk.bussiness.location;

import com.autonavi.gbl.pos.replay.PosReplayService;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.SingleServiceID;

/**
 * SDK自带的回放功能Controller
 */
public class LocationReplayController {

    public static String TAG = LocationReplayController.class.getSimpleName();

    private PosReplayService mPosReplayService;

    private static class LocReplayManagerHolder {
        private static LocationReplayController mInstance = new LocationReplayController();
    }

    private LocationReplayController() {
    }

    public static LocationReplayController getInstance() {
        return LocationReplayController.LocReplayManagerHolder.mInstance;
    }

    // 启动定位回放服务
    public void initReplayService() {
        if (mPosReplayService == null) {
            // 获取高德定位服务
            mPosReplayService = (PosReplayService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.PosReplaySingleServiceID);
            // 设置定位服务，由外部创建并初始化好
            mPosReplayService.setPosService(LocationController.getInstance().getPosService());
        }
    }

    public PosReplayService getmPosReplayService() {
        return mPosReplayService;
    }

    // 关闭定位回放服务
    public void closeService() {
        if (mPosReplayService != null) {
            ServiceMgr.getServiceMgrInstance().removeBLService(SingleServiceID.PosReplaySingleServiceID);
            mPosReplayService = null;
        }
    }

    /**
     * 开始回放
     * @param locPath   需要回放的日志路径
     * @param replaySpeedTime 回放的速度
     */
    public void startReplay(String locPath, long replaySpeedTime) {
        if (mPosReplayService != null) {
            mPosReplayService.setLocPath(locPath);
            // 设置回放速度
            mPosReplayService.setReplaySpeedTime(replaySpeedTime);
            // 设置观察者
            mPosReplayService.start();
        }
    }

    /**
     * 暂停回放
     */
    public void pauseReplay() {
        if (mPosReplayService != null) {
            mPosReplayService.pause();
        }
    }

    /**
     * 恢复回放
     */
    public void resumeReplay() {
        if (mPosReplayService != null) {
            mPosReplayService.resume();
        }
    }

    /**
     * 停止回放
     */
    public void stopReplay() {
        if (mPosReplayService != null) {
            mPosReplayService.stop();
        }
    }
}
