package com.autosdk.bussiness.speech;

import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.speech.SpeechSynthesizeService;
import com.autonavi.gbl.speech.model.TTSParam;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.speech.observer.ISpeechObserver;

import timber.log.Timber;

/**
 * 导航语音--语音合成
 */
public class SpeechSynthesizeController implements ISpeechObserver {

    private SpeechSynthesizeService mSpeechSynthesizeService;
    private int mInitCode;
    private ISpeechObserver mISpeechObserver;

    public static SpeechSynthesizeController getInstance() {
        return SpeechSynthesizeControllerHolder.instance;
    }

    private static class SpeechSynthesizeControllerHolder {
        private static SpeechSynthesizeController instance = new SpeechSynthesizeController();
    }

    private SpeechSynthesizeController() {

    }

    public void initSpeechSynthesize() {
        mSpeechSynthesizeService = (SpeechSynthesizeService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.SpeechSynthesizeSingleServiceID);
        mInitCode = mSpeechSynthesizeService.init(this);
        Timber.d("initSpeechSynthesize: mInitCode = " + mInitCode);
    }

    /**
     * 是否初始化成功
     */
    public boolean isInitSuccess() {
        return mInitCode == Service.ErrorCodeOK;
    }

    public void unInit() {
        if(mSpeechSynthesizeService != null){
            mSpeechSynthesizeService.unInit();
            unregisterSpeechObserver();
            mSpeechSynthesizeService = null;
        }
    }

    /**
     * 设置合成参数，调用Start前设置
     * @param param
     * @param value
     * @return
     */
    public int setParam(@TTSParam.TTSParam1 int param, int value){
        if (mSpeechSynthesizeService != null) {
            return mSpeechSynthesizeService.setParam(param, value);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * 设置合成参数，调用Start前设置
     * @param text 待合成的文本
     * @param isSync false-异步调用，立即返回 true-同步调用，合成结束后返回
     * @param requestId 请求id，观察者可通过此id判断当前回调是哪个请求 由外部自行保证唯一性, 若已存在相同id在队列中等待执行，后面一条会返回失败
     * @return
     */
    public int synthesize(String text, boolean isSync, int requestId){
        if (mSpeechSynthesizeService != null) {
            return mSpeechSynthesizeService.synthesize(text, isSync, requestId);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * 停止合成
     * @param requestId	请求id，与Synthesize传入的值对应
     */
    public void stop(int requestId){
        if (mSpeechSynthesizeService != null) {
            mSpeechSynthesizeService.stop(requestId);
        }
    }

    /**
     * 停止所有合成
     */
    public void stopAll(){
        if (mSpeechSynthesizeService != null) {
            mSpeechSynthesizeService.stopAll();
        }
    }

    /**
     * 设置语音包，若正在合成会先停止当前合成进程
     * @param irfPath
     */
    public int setVoice(String irfPath){
        if (mSpeechSynthesizeService != null) {
            return mSpeechSynthesizeService.setVoice(irfPath);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    // ============================ 观察者 ================================
    public void registerSpeechObserver(ISpeechObserver l) {
        mISpeechObserver = l;
    }

    public void unregisterSpeechObserver() {
        mISpeechObserver = null;
    }

    @Override
    public void onSampleRateChange(int sampleRate, int[] pcmLen) {
        if (null != mISpeechObserver) {
            mISpeechObserver.onSampleRateChange(sampleRate, pcmLen);
        }
    }

    @Override
    public void onStart(int requestId) {
        if (null != mISpeechObserver) {
            mISpeechObserver.onStart(requestId);
        }
    }

    @Override
    public void onGetData(int requestId, BinaryStream pcmData, long duration) {
        if (null != mISpeechObserver) {
            mISpeechObserver.onGetData(requestId, pcmData, duration);
        }
    }

    @Override
    public void onError(int requestId, int errCode) {
        if (null != mISpeechObserver) {
            mISpeechObserver.onError(requestId, errCode);
        }
    }

    @Override
    public void onFinish(int requestId) {
        if (null != mISpeechObserver) {
            mISpeechObserver.onFinish(requestId);
        }
    }

    @Override
    public void onStop(int requestId) {
        if (null != mISpeechObserver) {
            mISpeechObserver.onStop(requestId);
        }
    }

}
