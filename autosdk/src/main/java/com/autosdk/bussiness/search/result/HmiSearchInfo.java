package com.autosdk.bussiness.search.result;

import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.search.request.SearchPoiBizType;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 普通搜索结果，如周边查询、关键字匹配查询、公交查询等 对应Lqii的类型为其他：2、3、5
 */
public class HmiSearchInfo implements Serializable, Cloneable {
    /**
     * Lqii信息 控制页面跳转的相关逻辑放在这里面
     */
//    public LqiiInfo lqiiInfo;


    /**
     * 依赖于服务下发字段进行添加
     */
//    public ArrayList<CitySuggestion> citySuggestion;

    /**
     * 发起搜索时, 用户传入的业务类型
     * 非服务端下发数据, 由调用方在发起搜索时指定,搜索结束后原样返回给用户
     * 可用值参考 {@link SearchPoiBizType} , 可多个值组合
     */
    public int poiBizType = SearchPoiBizType.NORMAL;

    /**
     * 服务下发 解析得到 搜索结果列表
     */
    public ArrayList<POI> poiResults = new ArrayList<>();


    /**
     * 服务下发字段 [total]
     */
    public int poiTotalSize = 0;


    /**
     * Bus Line总数量 [busline_count]
     */
    public int buslineCount = 0;
    /**
     * 根据公交站点查询或周边查询
     */
//    public ArrayList<POI> stationList;

    /**
     * 是否发起POI码点请求 1:发起请求 0:不发起请求
     */
    public int codePoint;

    /**
     * 服务下发字段【is_general_search】  是否泛搜标识  优化  干掉
     */
    public int isGeneralSearch = 1;

    /**
     * 筛选项相关数据
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        HmiSearchInfo data = (HmiSearchInfo) super.clone();
        if (null != poiResults) {
            data.poiResults = (ArrayList<POI>) poiResults.clone();
        }
        return data;
    }
}

