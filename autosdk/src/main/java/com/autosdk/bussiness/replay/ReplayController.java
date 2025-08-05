package com.autosdk.bussiness.replay;

import com.autonavi.gbl.recorder.Player;
import com.autonavi.gbl.recorder.Recorder;
import com.autonavi.gbl.recorder.RecorderService;
import com.autonavi.gbl.recorder.model.PlayParam;
import com.autonavi.gbl.recorder.model.RecordParam;
import com.autonavi.gbl.recorder.observer.IPlayerObserver;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.ServiceInitStatus;
import com.autonavi.gbl.util.model.SingleServiceID;

/**
 * 录制回放服务
 * @author AutoSDK
 */
public class ReplayController {

    public static String TAG = ReplayController.class.getSimpleName();
    public static int REPLAY_STOP = 0;//停止回放
    public static int REPLAY_PAUSE = 1;//暂停回放
    public static int REPLAY_RESUME= 2;//继续回放

    private RecorderService mRecorderService;

    /**
     * 回放工具
     */
    private Player mPlayer;

    /**
     * 录制工具
     */
    private Recorder mRecorder;

    private int mCurrentPlayStatus=-1;//当前播放状态

    private static class ReplayControllerManagerHolder {
        private static ReplayController mInstance = new ReplayController();
    }

    private ReplayController() {
        mRecorderService = (RecorderService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.RecorderSingleServiceID);
    }

    public static ReplayController getInstance() {
        return ReplayController.ReplayControllerManagerHolder.mInstance;
    }

    /**
     * 是否已经初始化
     * @return
     */
    public boolean isInit() {
        if(mRecorderService != null) {
            return mRecorderService.isInit() == ServiceInitStatus.ServiceInitDone ? true : false;
        }
        return false;
    }

    /**
     * 配置录制参数
     * @param param
     */
    public void setRecordParam(RecordParam param) {
        if(getRecorder() != null) {
            mRecorder.setParam(param);
        }
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        if(getRecorder() != null) {
            mRecorder.start();
        }
    }

    /**
     * 结束录制
     */
    public void stopRecord() {
        if(getRecorder() != null) {
            mRecorder.stop();
        }
    }

    /**
     * 获取录制工具
     * @return
     */
    public Recorder getRecorder() {
        if(!isInit()) {
            return null;
        }
        if(mRecorder == null) {
            mRecorder = mRecorderService.getRecorder();
        }
        return mRecorder;
    }

    /**
     * 获取回放工具
     * @return
     */
    public Player getPlayer() {
        if(!isInit()) {
            return null;
        }
        if(mPlayer == null) {
            mPlayer = mRecorderService.getPlayer();
        }
        return mPlayer;
    }

    /**
     * 设置回放参数
     * @param param
     */
    public void setPlayParam(PlayParam param) {
        if(getPlayer() != null) {
            mPlayer.setParam(param);
        }
    }

    /**
     * 注册回放观察者
     * @param observer
     */
    public void addObserver(IPlayerObserver observer) {
        if(getPlayer() != null) {
            mPlayer.addObserver(observer);
        }
    }

    /**
     * 移除回放观察者
     * @param observer
     */
    public void removeObserver(IPlayerObserver observer) {
        if(getPlayer() != null) {
            mPlayer.removeObserver(observer);
        }
    }

    /**
     * 开始回放
     */
    public void startPlay() {
        if(getPlayer() != null) {
            mPlayer.start();
        }
    }

    /**
     * 设置回放倍速
     * @param speed 速度不能为负数及0
     */
    public void setPlaySpeed(float speed) {
        if(getPlayer() != null) {
            mPlayer.setPlaySpeed(speed);
        }
    }

    /**
     * 停止回放
     */
    public void stopPlay() {
        if(getPlayer() != null) {
            mPlayer.stop();
            mCurrentPlayStatus=REPLAY_STOP;
        }
    }

    /**
     * 暂停回放
     */
    public void pausePlay() {
        if(getPlayer() != null) {
            mPlayer.pause();
            mCurrentPlayStatus=REPLAY_PAUSE;
        }
    }

    /**
     * 继续回放
     */
    public void resumePlay() {
        if (getPlayer() != null) {
            mPlayer.resume();
            mCurrentPlayStatus=REPLAY_RESUME;
        }
    }

    public int getCurrentPlayStatus() {
       return mCurrentPlayStatus;
    }
}
