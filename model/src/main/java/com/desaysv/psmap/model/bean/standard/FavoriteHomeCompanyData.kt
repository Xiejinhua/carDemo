package com.desaysv.psmap.model.bean.standard

/**
 *  查询家和公司信息
 */
data class FavoriteHomeCompanyData(
    val requestFavoriteType: Int = 0, //1：家 2：公司 3：家和公司
    var protocolFavPoiInfos: MutableList<HomeCompanyData> = arrayListOf() //poi点信息数组
) : ResponseDataData()

data class HomeCompanyData(
    var favouriteType: Int = 0, //1：家2：公司
    var poiId: String = "",
    var favoritePoiName: String = "", //名称
    var poiAddress: String = "", //地址
    var poiDistance: Int = 0, //距离
    var lat: Double = 0.0, //纬度
    var lon: Double = 0.0, //经度
    var entry_lat: Double = 0.0, //到达点纬度
    var entry_lon: Double = 0.0, //到达点经度
    var category: Int = 0, //类别 1:表示家,2:表示公司，3:表示同时请求家和公司
    var poiType: Int = 0, //6位的poi type
)
