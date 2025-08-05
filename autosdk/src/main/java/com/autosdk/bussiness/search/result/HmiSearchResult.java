package com.autosdk.bussiness.search.result;


public class HmiSearchResult implements Cloneable {
    public String mKeyword;
    /**
     * 普通搜索结果，如周边查询、关键字匹配查询、公交查询等 对应Lqii的类型为其他：2、3、5
     */
    public HmiSearchInfo searchInfo;
    /**
     * 搜索服务返回的状态信息
     */
    public ResponseHeaderModule responseHeader;

    public HmiSearchResult() {
        searchInfo = new HmiSearchInfo();
        responseHeader = new ResponseHeaderModule();
    }

}

