package com.autosdk.bussiness.search.request;

import androidx.annotation.IntDef;

import com.autosdk.bussiness.search.SearchCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 搜索点的类型
 * 调用方传参时, 不同类型可通过 "或" 运算 进行组合, 如 int targetBizType = AROUND | CATEGORY  , 表示周边分类搜索
 * 接收方取值时通过 "与" 运算, 如: boolean isViaType = (targetBizType & VIA_POINT)>0 , 大于0则表示途经点
 * @author AutoSDK
 */
@IntDef({SearchPoiBizType.NORMAL, SearchPoiBizType.VIA_POINT, SearchPoiBizType.AROUND})
@Retention(RetentionPolicy.SOURCE)
public @interface SearchPoiBizType {

    /**
     * 默认值,普通点
     */
    int NORMAL = 1;

    /**
     * 途经点
     */
    int VIA_POINT = 1 << 1;

    /**
     * 周边搜（途经点没有周边搜）
     */
    int AROUND = 1 << 2;

    /**
     * 分类搜索
     * 启用时 {@link com.autosdk.bussiness.search.SearchController#keywordSearch(SearchRequestInfo, SearchCallback)} 是会传入 category 参数
     */
    int CATEGORY = 1 << 3;

    /**
     * 设置为家
     */
    int ADDHOME = 1 << 4;
    /**
     * 设置为公司
     */
    int ADDCOMPANY = 1 << 5;

    /**
     * 设置组队目的地
     */
    int ADDGROUP = 1 << 6;

    /**
     * 更改目的地
     */
    int END_POINT = 1<<7;
}
