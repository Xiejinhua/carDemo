package com.autosdk.bussiness.search.constant;

/**
 * 搜索模块使用的常量
 */
public interface SearchConstant {
    String KEY_POI = "key_poi";
    String SIMPLE_ITEM_ID = "item_id";
    String SEARCH_WORD = "search_word";
    String KEY_FROM = "key_from";

    /** 商品详情页需要内容 **/
    String KEY_POI_ID = "key_poi_id";
    String KEY_SPU_ID = "key_spu_id";
    String KEY_SKU_ID = "key_sku_id";
    String KEY_SEARCH_REQUEST = "key_search_request";

    /**
     * 图片墙
     */
    String KEY_SEARCH_IMAGE = "key_search_image";

    /**
     * 搜索结果表示的业务类型
     */
    String KEY_POI_BIZ_TYPE = "key_poi_biz_type";
    String KEY_POI_ALONG_SEARCH_INFO = "key_poi_along_search_info";

    /**
     * 请求发起对象
     */
    String KEY_SEARCH_REQUEST_INFO = "key_search_request_info";

    String KEY_ADCITY = "key_adcity";
    String KEY_GOTO_TYPE = "key_goto_type";
    String KEY_FORRESULT = "forresult";

    int SEARCH_REQUEST_CODE = 101;
    int SEARCH_RESULT_CODE = 101;

    /**
     * 跳转至主图“我的位置” Code 值
     */
    int SEARCH_KEYWORD_MY_POSITION_REQUEST_CODE = 3;
    int SEARCH_KEYWORD_MY_POSITION_RESULT_CODE = 3;

    /**
     * 不执行绘制扎标操作（一般由上个页面完成扎标绘制 如搜索结果页）
     */
    int SEARCH_RESULT_DETAIL_NORMAL = 0;
    /**
     * 绘制大头蓝色扎标(收藏点跳到详情页)
     */
    int SEARCH_RESULT_DETAIL_BLUE = 1;

    /**
     * 只展示数字扎标（SearchHome跳转到详情页）
     */
    int SEARCH_RESULT_DETAIL_BLUE_NUMBER = 2;

    /**
     * 商品详情页
     */
    int SEARCH_RESULT_PRODUCT_INFO = 3;


}
