// IMapScreenshotCallback.aidl
package com.desaysv.psmap.adapter;

// Declare any non-default types here with import statements

interface IMapScreenshotCallback {
    oneway void onMapScreenshotReady();
    oneway void onMapScreenshotpBitmapBuffer(in byte[] pBitmapBuffer);
    oneway void onErrorInfo(int code,String info);
}