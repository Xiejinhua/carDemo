package com.autosdk.bussiness.speech.observer;

import com.autonavi.gbl.util.model.BinaryStream;

/**
 * Created by AutoSdk on 2020/11/10.
 **/
public class SpeechObserverImp implements ISpeechObserver {


    @Override
    public void onSampleRateChange(int sampleRate, int[] pcmLen) {

    }

    @Override
    public void onStart(int requestId) {

    }

    @Override
    public void onGetData(int requestId, BinaryStream pcmData, long duration) {

    }

    @Override
    public void onError(int requestId, int errCode) {

    }

    @Override
    public void onFinish(int requestId) {

    }

    @Override
    public void onStop(int requestId) {

    }
}
