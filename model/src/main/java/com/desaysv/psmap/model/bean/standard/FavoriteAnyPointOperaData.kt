package com.desaysv.psmap.model.bean.standard

/**
 * 收藏任意点
 */
data class FavoriteAnyPointOperaData(
    val favoriteType: Int, //设置类型 1：家 2：公司 0：普通收藏点
    var poiName: String, //Poi名称
    val longitude: Double, //Poi经度
    val latitude: Double, //Poi纬度
    val entryLon: Double, //Poi到达点经度
    val entryLat: Double, //Poi到达点纬度
    val poiAddress: String, //Poi地址描述
    val isDev: Boolean, //是否需要国测加密 true：需要 false：不需要
    var poiId: String, //Poi的ID
    val poiType: String, //Poi类型POI 的6位类型编码
    var type: Int = 0 //设置类型 1：家 2：公司 0：普通收藏点
) : ResponseDataData()
