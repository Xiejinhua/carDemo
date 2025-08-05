package com.desaysv.psmap.adapter;
import com.desaysv.psmap.adapter.IStandardJsonProtocolCallback;

interface IStandardJsonProtocol {
    void request(in String pkg,String massage);
    void registerJsonMessageCallback(in String pkg,in IStandardJsonProtocolCallback callback);
    void unregisterJsonMessageCallback(in String pkg);
}