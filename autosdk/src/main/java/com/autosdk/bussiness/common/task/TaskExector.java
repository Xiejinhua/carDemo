package com.autosdk.bussiness.common.task;

/**
 * Auto创建线程池类型
 *
 * Created by AutoSdk.
 */
public enum TaskExector {
    /**
     * 默认线程池
     */
    DEFALUT(0),
    /**
     * 搜索模块专用线程池
     */
    SEARCH(1),
    /**
     * 网络请求专用线程池
     */
    NET_WORK(2),
    /*
     *供AE8引擎专用
    */
    AE8_ENGINE(3),
    /*
     *U盘更新
    */
    UDISK_DOWNLOAD(4),
    /**
     * 用户模块包线程池*/
    USER_BL(7),
    /**
     * 同步SDK读写操作线程池
     */
    SYNC_SDK_IO(8),
    /**
     * 适配层使用线程池
     */
    ADAPTER(9),

//    /**
//     * 日志打印使用线程池
//     */
//    LOGGER(10),
    /**
     * 激活日志打印使用线程池
     */
    ACTIVATE_LOG(11),;

    private int value;

    TaskExector(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

}
