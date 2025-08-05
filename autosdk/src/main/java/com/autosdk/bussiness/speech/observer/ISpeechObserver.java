package com.autosdk.bussiness.speech.observer;

import com.autonavi.gbl.speech.observer.ISpeechSynthesizeObserver;
import com.autonavi.gbl.util.model.BinaryStream;

/**
 * Created by AutoSdk on 2020/11/10.
 **/
public interface ISpeechObserver extends ISpeechSynthesizeObserver {
    /**
     * 采样率发生变化时通知播放器参数调整，设置新的分片大小，在引擎初始化过程中回调
     * @param sampleRate 采样率
     * @param pcmLen pcm分片大小
     */
    @Override
    void onSampleRateChange(int sampleRate, int[] pcmLen);

    /**
     * 合成开始时回调通知
     * @param requestId 请求id
     */
    @Override
    void onStart(int requestId);

    /**
     * 返回合成数据，分多次回调。
     * @param requestId 请求id
     * @param pcmData PCM数据
     * @param duration 音频时长，单位毫秒
     */
    @Override
    void onGetData(int requestId, BinaryStream pcmData, long duration);

    /**
     * 合成出错时回调通知
     * @param requestId 请求id
     * @param errCode 错误码
     */
    @Override
    void onError(int requestId, int errCode);

    /**
     * 合成结束时回调通知
     * @param requestId 请求id
     */
    @Override
    void onFinish(int requestId);

    /**
     * 停止合成时回调通知
     * @param requestId 请求id
     */
    @Override
    void onStop(int requestId);
}
