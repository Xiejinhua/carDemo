// IMapScreenshotCallback.aidl
package com.desaysv.psmap.adapter;

// Declare any non-default types here with import statements

interface IMapDataCallback {
    oneway void onMassage(in String pkg,in String msg);
    oneway void onByteMassage(in String pkg,in String msg,in byte[] byteArray);
}