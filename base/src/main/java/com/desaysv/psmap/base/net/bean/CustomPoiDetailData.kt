package com.desaysv.psmap.base.net.bean

data class CustomPoiDetailData(
    // POI的唯一标识
    val poiId: String? = null,

    // POI的名称
    val poiName: String? = null,

    // POI的经度坐标
    val longitude: String? = null,

    // POI的纬度坐标
    val latitude: String? = null,

    // POI的图片路径
    val imagePath: String? = null,

    // POI的详细地址
    val address: String? = null,

    // POI所在的省份
    val province: String? = null,

    // POI所在的城市
    val city: String? = null,

    // POI的联系人姓名
    val contactName: String? = null,

    // POI的联系电话
    val contactPhone: String? = null,

    // 消费权益内容
    val consumerContent: String? = null,

    // 权益商家介绍
    val equityMerchants: String? = null,

    // POI类型的唯一标识
    val poiTypeId: String? = null,

    // POI的类型
    val poiType: String? = null,

    // POI的状态
    val status: String? = null,

    // POI的更新时间
    val updateTime: String? = null


)
