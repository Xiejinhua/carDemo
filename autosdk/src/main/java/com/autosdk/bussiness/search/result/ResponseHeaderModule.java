package com.autosdk.bussiness.search.result;

import java.io.Serializable;

public class ResponseHeaderModule implements Serializable {
    /**
     * 服务接口版本信息
     */
    public String version = "";
    /**
     * true / false, 是否请求成功
     */
    public boolean result = false;
    /**
     * 错误代码 见SearchRequest定义
     */
    public int errorCode;
    /**
     * 错误描述
     */
    public String errorMessage;
    /**
     * 服务器时间戳
     */
    public String timeStamp;
    /**
     * true:表示在线搜索的结果,false:表示离线搜索的结果
     */
    public boolean isOnLine = true;
}
