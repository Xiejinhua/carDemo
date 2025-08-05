package com.autosdk.bussiness.search.request;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 关键字搜索查询方式
 */
@StringDef({SearchQueryType.NORMAL, SearchQueryType.AROUND, SearchQueryType.ID})
@Retention(RetentionPolicy.SOURCE)
public @interface SearchQueryType {
    // 普通关键字搜索 根据关键字查询，输入关键字搜索目的地结果，支持首拼搜索、拼音搜索、中文关键字搜索、POI地址、类型搜索、别名搜索、道路搜索、门牌号搜索、同义词搜索、城市搜索
    String NORMAL = "TQUERY";

    // 按经纬度周边搜索
    String AROUND = "RQBXY";

    // 根据POI的ID查询
    String ID = "IDQ";
}
