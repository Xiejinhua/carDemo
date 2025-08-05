package com.desaysv.psmap.base.net.bean

data class CustomPoiDetailBean(

    // 响应状态码
    val code: Int? = null,

    // 响应消息
    val msg: String? = null,

    // POI详情数据
    val data: CustomPoiDetailData? = null

)
