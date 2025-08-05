package com.autosdk.bussiness.data.observer;

import com.autonavi.gbl.data.model.MergedStatusInfo;

import java.util.ArrayList;

/**
 * Created by AutoSdk on 2020/11/10.
 **/
public class MapDataObserverImp implements IMapDataObserver {

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
    public void onErrorNotify(int downLoadMode, int dataType, int id, int errType, String errMsg) {

    }

    @Override
    public void onErrorNotifyH(int i, int i1, int i2, int i3, String s) {
        
    }

    @Override
    public void onDeleteErrorData(int downLoadMode, int dataType, int id, int opCode) {

    }

    @Override
    public void onMergedStatusInfo(MergedStatusInfo mergedStatusInfo) {

    }
}
