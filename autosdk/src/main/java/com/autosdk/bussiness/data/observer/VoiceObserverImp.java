package com.autosdk.bussiness.data.observer;

import java.util.ArrayList;

/**
 * Created by AutoSdk on 2020/11/10.
 **/
public class VoiceObserverImp implements IVoiceDataObserver {


    @Override
    public void onInit(int downLoadMode, int dataType, int opCode) {

    }

    @Override
    public void onRequestDataListCheck(int downLoadMode, int dataType, int opCode) {

    }

    @Override
    public void onOperated(int downLoadMode, int dataType, int opType, ArrayList<Integer> opreatedIdList) {

    }

    @Override
    public void onDownLoadStatus(int downLoadMode, int dataType, int id, int taskCode, int opCode) {

    }

    @Override
    public void onPercent(int downLoadMode, int dataType, int id, int percentType, float percent) {

    }

    @Override
    public void onDownloadImage(int itemId, int opErrCode, String strFilePath, int dataType) {

    }

}
