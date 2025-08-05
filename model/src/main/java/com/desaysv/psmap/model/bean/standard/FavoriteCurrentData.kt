package com.desaysv.psmap.model.bean.standard

data class FavoriteCurrentData(
    var isFavoriteSuccess: Int = 2, //收藏成功:1 收藏失败:2
    var poiId: String = "", //收藏点poi id
    var poiName: String = "", //收藏点poi名称
    var addr: String = "", //收藏点poi地址
    var phone: String = "", //收藏点poi电话
    var lat: Double = 0.0, //收藏点poi纬度
    var lon: Double = 0.0 //收藏点poi经度
) : ResponseDataData()
