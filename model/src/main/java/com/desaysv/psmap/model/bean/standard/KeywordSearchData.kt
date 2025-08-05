package com.desaysv.psmap.model.bean.standard

/**
 * 关键字搜索 （30300）
 */
data class KeywordSearchData(
    val requestType: Int? = 0, //请求类型 0:应⽤外搜索 1:应⽤内搜索 （4.3及以上版本⽀持） 2:请求应对⻓安scheme协议（460及以上版本⽀持） 默认0
    val searchType: Int, //搜索类型0：关键字搜索
    val keywords: String, //输⼊搜索的关键字
    val mylocLon: Double?, //传⼊当前位置的经度
    val mylocLat: Double?, //传⼊当前位置的纬度
    val maxCount: Int?, //搜索返回的最⼤个数
    val dev: Int?, //是否需要偏移 0 无需偏移1 需要偏移 默认0
    val city: String?, //搜索城市输入需要搜索的城市
    val needClassify: Int?, //是否需要分类（430及以上版本支持）
    val needSort: Int?, //是否需要排序（430及以上版本支持）
    val needRange: Int?, //是否需要范围（430及以上版本支持）
    val needChildPoi: Int?, //是否需要子poi（430及以上版本支持）
    val needCharge: Int?, //是否需要快慢充透出（430及以上版本支持） 0：不需要 1：需要
    val classify: String?, //选择的分类（430及以上版本支持）
    val sort: String?, //排序方式（430及以上版本支持）
    val range: String?, //选择的范围（430及以上版本支持）
    val charge: String?, //快慢充透出（430及以上版本支持）
    val sortrule: Int?, //排序规则 0：默认排序 1：距离排序（460及以上版本支持）
    var isRecommend: Boolean?, //460以上版本支持 是否为城市推荐页搜索
)

