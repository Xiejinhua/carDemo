package com.autosdk.bussiness.navi.route.callback;


import com.autosdk.bussiness.navi.route.model.IRouteResultData;

public interface IRouteResultCallBack {

    void callback(IRouteResultData result, boolean isLocal);

    void errorCallback(int errorCode, String errorMessage, boolean isLocal);

}