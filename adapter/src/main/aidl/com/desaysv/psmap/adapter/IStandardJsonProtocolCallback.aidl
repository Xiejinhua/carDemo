package com.desaysv.psmap.adapter;

interface IStandardJsonProtocolCallback {
    oneway void onMassage(in String msg);
    String onSyncMassage(in String msg);
}