package com.desaysv.psmap.model.bean.standard

/**
 *  收藏点信息查询 --80139
 */
data class FavoriteInformationInquiryData(
    val requestType: Int = 0, //获取收藏夹信息请求类型（500及以上版本⽀持）） 0:请求收藏夹内容（不包括家和公司） 1:请求收藏夹内容（包括家和公司） 2:请求家 3:请求公司 4:请求家和公司
    val maxCount: Int = 20, //需要获取的收藏夹记录的最⼤个数（500及以上版本⽀持）
    var newFavoriteData: MutableList<NewFavoriteData> = arrayListOf() //poi点信息数组
) : ResponseDataData()

data class NewFavoriteData(
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
