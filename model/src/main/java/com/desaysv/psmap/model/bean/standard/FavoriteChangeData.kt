package com.desaysv.psmap.model.bean.standard

/**
 *  收藏点信息透出
 */
data class FavoriteChangeData(
    var favoriteData: MutableList<FavoriteData> = arrayListOf() //poi点信息数组
) : DisclosureData()

data class FavoriteData(
    var poiId: String = "",
    var name: String = "", //名称
    var addr: String = "", //地址
    var latitude: Double = 0.0, //纬度
    var longitude: Double = 0.0, //经度
    var entryLatitude: Double = 0.0, //到达点纬度
    var entryLongitude: Double = 0.0, //到达点经度
    var distance: Int = 0, //距离
    var phone: String = "", //电话
    var poitype: Int = 0, //6位的poi type
    var favouriteType: Int = 0, //⽆效字段
)
