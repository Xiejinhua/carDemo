package com.desaysv.psmap.model.bean.standard

/**
 * 家和公司变更通知
 */
data class FavoriteHomeCompanyChangeData(
    var editType: Int = 1, //操作类型： 1：增加/更新 2：删除
    var favoriteType: Int = 1, //变更的类型： 1：家 2：公司
    var favoritePoiName: String = "", //POI名称
    var poiId: String = "", //POI唯⼀标识
    var poiAddress: String = "", //POI地址
    var poiDistance: Int = 0 //距离
) : DisclosureData()
