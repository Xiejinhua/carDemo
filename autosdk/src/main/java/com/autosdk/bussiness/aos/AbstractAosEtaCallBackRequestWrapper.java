package com.autosdk.bussiness.aos;

import com.autonavi.gbl.aosclient.model.CEtaRequestReponseParam;
import com.autonavi.gbl.aosclient.observer.ICallBackEtaRequest;

/**
 * Created by AutoSdk on 2021/9/10.
 **/
public abstract class AbstractAosEtaCallBackRequestWrapper implements ICallBackEtaRequest {

    public abstract void setAosRequestCallBack(AosRequestCallBack<CEtaRequestReponseParam> aosRequestCallBack);

    @Override
    public void onRecvAck(CEtaRequestReponseParam cEtaRequestReponseParam) {

    }
}
