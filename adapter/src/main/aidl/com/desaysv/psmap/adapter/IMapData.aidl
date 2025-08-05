// IMapScreenshot.aidl
package com.desaysv.psmap.adapter;
import com.desaysv.psmap.adapter.IMapDataCallback;

// Declare any non-default types here with import statements

interface IMapData {
    void sendMassage(in String pkg,String massage);
    int getNaviStatus();
    void registerMapDataCallback(in String pkg,in IMapDataCallback callback);
    void unregisterMapDataCallback(in String pkg);
}