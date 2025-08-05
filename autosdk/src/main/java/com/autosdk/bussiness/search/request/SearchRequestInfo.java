package com.autosdk.bussiness.search.request;

import androidx.annotation.Nullable;

import com.autosdk.bussiness.common.POI;

import java.io.Serializable;

/**
 * 发起搜索请求相关参数
 * <pre>
 *     new SearchKeywordInfo.Builder()
 *         .setKeyword(item.getName()) // 关键字, 必传
 *         .setPoi(mCurPOI) // 可空, 周边搜使用
 *         .setQueryType(SearchQueryType.NORMAL) // 搜索类型,默认为关键字搜索
 *         .setBizType(SearchPoiBizType.NORMAL | SearchPoiBizType.CATEGORY) // 搜索结果表示的业务类型, 由调用方定义,可多种组合
 *         .build()
 * </pre>
 */
public class SearchRequestInfo implements Serializable {

    private SearchRequestInfo() {
    }

    /**
     * 页码
     */
    private int page = 1;

    /**
     * 条数
     */
    private int size = 10;

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 查询类型，必传
     * QueryType.NORMAL:  关键字搜索
     * QueryType.AROUND:  周边搜索
     */
    @SearchQueryType
    private String queryType = SearchQueryType.NORMAL;

    /**
     * 范围, 单位米; 周边搜索参数，Aos服务器默认为3000，离线搜索默认为20KM
     */
    private String range;

    /**
     * 用于在指定的的poi附近进行搜索
     * 为空时,表示在地图中心点进行周边搜
     */
    @Nullable
    private POI poi;

    /**
     * 搜索结果所表示的业务类型, 由调用方定义
     * 具体值参考 {@link SearchPoiBizType}, 可多种组合
     */
    private int bizType = SearchPoiBizType.NORMAL;

    private String filter;

    //筛选项
    public String classify;

    //二级筛选项
    public String classifyLevel2;
    //二级筛选项
    public String classifyLevel3;

    /**
     * 商品ID
     */
    String spuId;

    String skuId;

    private boolean isNavi;

    /**
     * 离线搜索城市
     */
    private int cityCode = 0;

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public boolean isNavi() {
        return isNavi;
    }

    public void setNavi(boolean navi) {
        isNavi = navi;
    }

    /**
     * 搜索结果列表跳转类型  0 搜索   1 行中搜索美食、加油（充电）、卫生间   2 行中搜索其余类别
     */
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    private boolean isEndPoint;

    public boolean isEndPoint() {
        return isEndPoint;
    }

    public void setEndPoint(boolean endPoint) {
        isEndPoint = endPoint;
    }

    /**
     * 快捷搜索目的地页码
     */
    private int endpointPage = 1;

    public int getEndpointPage() {
        return endpointPage;
    }

    public void setEndpointPage(int endpointPage) {
        this.endpointPage = endpointPage;
    }

    /**
     * 使用筛选搜索时必传。筛选回传参数，使用搜索结果中的SearchClassifyInfo.retainState值回传
     */
    public String retainState;

    /**
     * 使用筛选搜索时必传。用户筛选级别。1：用户一筛发起请求， 2：用户二筛发起请求， 3：用户发起三筛请求， 其他情况发起请求可不传, level 1、2、3互斥
     */
    public String checkedLevel;

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String category;

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    private boolean isAlongWaySearch;

    public boolean isAlongWaySearch() {
        return isAlongWaySearch;
    }

    public void setAlongWaySearch(boolean alongWaySearch) {
        isAlongWaySearch = alongWaySearch;
    }

    public static class Builder {
        private SearchRequestInfo mSearchRequestInfo;

        public Builder() {
            mSearchRequestInfo = new SearchRequestInfo();
        }

        public Builder setIsNavi(boolean isNavi) {
            mSearchRequestInfo.isNavi = isNavi;
            return this;
        }

        public Builder setType(int type) {
            mSearchRequestInfo.type = type;
            return this;
        }
        public Builder setSize(int size) {
            mSearchRequestInfo.size = size;
            return this;
        }
        public Builder setPage(int page) {
            mSearchRequestInfo.page = page;
            return this;
        }

        /**
         * 设置搜索关键字
         */
        public Builder setKeyword(String keyword) {
            mSearchRequestInfo.keyword = keyword;
            return this;
        }

        /**
         * 查询类型，必传(预搜索时未使用)
         */
        public Builder setQueryType(@SearchQueryType String queryType) {
            mSearchRequestInfo.queryType = queryType;
            return this;
        }

        /**
         * 设置周边搜中心点信息, 主要是 经纬度和类别
         * 若为空,则默认使用地图中心点进行周边搜
         */
        public Builder setPoi(@Nullable POI poi) {
            mSearchRequestInfo.poi = poi;
            return this;
        }

        /**
         * 设置商品ID
         */
        public Builder setSpuId(String setSpuId) {
            mSearchRequestInfo.spuId = setSpuId;
            return this;
        }

        /**
         * 设置商品ID
         */
        public Builder setSkuId(String setSkuId) {
            mSearchRequestInfo.skuId = setSkuId;
            return this;
        }

        /**
         * 设置搜索结果表示的业务类型
         * 可用值参考 {@link SearchPoiBizType} ,可组合
         */
        public Builder setBizType(int searchPoiBizType) {
            mSearchRequestInfo.bizType = searchPoiBizType;
            return this;
        }

        /**
         * 范围, 单位米; 周边搜索参数，Aos服务器默认为3000，离线搜索默认为20KM
         */
        public Builder setRange(String range) {
            mSearchRequestInfo.range = range;
            return this;
        }

        /**
         * 离线搜索城市 adCode
         */
        public Builder setOfflineCity(int adCity) {
            mSearchRequestInfo.cityCode = adCity;
            return this;
        }

        /**
         * 筛选
         */
        public Builder setFilter(String filter) {
            mSearchRequestInfo.filter = filter;
            return this;
        }


        public SearchRequestInfo build() {
            return mSearchRequestInfo;
        }
    }

    public String getKeyword() {
        return keyword;
    }

    @SearchQueryType
    public String getQueryType() {
        return queryType;
    }

    @Nullable
    public POI getPoi() {
        return poi;
    }

    @SearchPoiBizType
    public int getBizType() {
        return bizType;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setQueryType(@SearchQueryType String queryType) {
        this.queryType = queryType;
    }

    public void setPoi(@Nullable POI poi) {
        this.poi = poi;
    }

    public void setBizType(@SearchPoiBizType int bizType) {
        this.bizType = bizType;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public String getClassifyLevel2() {
        return classifyLevel2;
    }

    public void setClassifyLevel2(String classifyLevel2) {
        this.classifyLevel2 = classifyLevel2;
    }

    public String getClassifyLevel3() {
        return classifyLevel3;
    }

    public void setClassifyLevel3(String classifyLevel3) {
        this.classifyLevel3 = classifyLevel3;
    }

    public String getRetainState() {
        return retainState;
    }

    public void setRetainState(String retainState) {
        this.retainState = retainState;
    }

    public String getCheckedLevel() {
        return checkedLevel;
    }

    public void setCheckedLevel(String checkedLevel) {
        this.checkedLevel = checkedLevel;
    }

    public String getSpuId() {
        return spuId;
    }

    public void setSpuId(String spuId) {
        this.spuId = spuId;
    }

    public String getSkuId() {
        return skuId;
    }

    public void setSkuId(String skuId) {
        this.skuId = skuId;
    }
}
