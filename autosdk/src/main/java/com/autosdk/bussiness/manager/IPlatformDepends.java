package com.autosdk.bussiness.manager;

import com.autonavi.gbl.util.model.KeyValue;
import com.autonavi.gbl.util.model.NetworkStatus;

import java.util.ArrayList;

/*
 * 获取系统平台依赖参数
 */
public interface IPlatformDepends {

    /*
    * 获取系统平台网络状态
    */
    public int getNetStatus();

    /*
    * 获取系统设备唯一号（32位字符串）
    */
    public String getDIU();
}
