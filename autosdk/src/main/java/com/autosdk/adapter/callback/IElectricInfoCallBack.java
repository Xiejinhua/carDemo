package com.autosdk.adapter.callback;

import com.autosdk.adapter.AdapterConstants;

public interface IElectricInfoCallBack {
    void notifyElectricInfo(@AdapterConstants.CarInfoType int type, Object object);

}
