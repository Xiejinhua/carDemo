package com.autosdk.service;

import android.content.Context;

import com.autosdk.adapter.callback.IElectricInfoCallBack;

import java.util.List;

/**
 * Created by AutoSdk on 2021/5/28.
 **/
public interface IModuleAdapterService {

    void startup();

    void destroy();

    void sendNormalMessage(int value);

    void sendCrossMessage(int isShow);

    void sendLoginMessage(int value);

    void sendDayNightMessage(int value);

    /**
     * 主要用于统计代码执行时间 、功能块执行时间
     * 1、新增统计类型添加
     * 2、begin开始事件埋点
     * 3、end结束事件埋点 ，输出过程耗时
     * 4、begin和end需要对应
     * 说明:主要用于性能测试方面进行功能耗时的统计 , 其他需求可通过添加统计类型后 ,进行适当位置埋点 ,可以得到事件耗时
     **/
    void beginCount(String type);

    void endCount(String type);

    void addElectricInfoCallBack(IElectricInfoCallBack callBack);

    void removeElectricInfoCallBack(IElectricInfoCallBack callBack);

    List<IElectricInfoCallBack> getElectricInfoCallBackList();
}
