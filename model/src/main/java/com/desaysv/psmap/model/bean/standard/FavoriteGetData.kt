package com.desaysv.psmap.model.bean.standard

/**
 *  收藏点信息查询 --80072
 */
data class FavoriteGetData(
    var favoriteData: MutableList<FavoriteDetailData> = arrayListOf() //poi点信息数组
) : ResponseDataData()

data class FavoriteDetailData(
    var poiId: String = "",
    var name: String = "", //名称
    var addr: String = "", //地址
    var latitude: Double = 0.0, //纬度
    var longitude: Double = 0.0, //经度
    var distance: Int = 0, //距离
    var phone: String = "", //电话
    var poitype: Int = 0, //6位的poi type
)
