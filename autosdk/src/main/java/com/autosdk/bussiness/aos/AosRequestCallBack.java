package com.autosdk.bussiness.aos;

/**
 * Created by AutoSdk on 2021/9/10.
 * @author AutoSDK
 **/
public interface AosRequestCallBack<T> {
    void onSuccess(T data);
}
