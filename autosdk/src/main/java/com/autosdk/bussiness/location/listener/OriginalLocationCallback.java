package com.autosdk.bussiness.location.listener;

/**
 * 定位状态分发
 */
public interface OriginalLocationCallback<T> {

  void onOriginalLocationChange(T t);

}
