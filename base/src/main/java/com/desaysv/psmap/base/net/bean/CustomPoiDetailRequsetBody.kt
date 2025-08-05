package com.desaysv.psmap.base.net.bean


data class CustomPoiDetailRequsetBody(

    // API接口编码
    val apiCode: String? = null,

    // 应用ID
    val appId: String? = null,

    // 请求体内容
    val body: String? = null,

    // POI的唯一标识
    val poiId: String? = null,

    // 随机字符串
    val randomstr: String? = null,

    // 签名
    val sign: String? = null,

    // 时间戳
    val timestamp: String? = null,

    // 事务ID
    val transationid: String? = null

)
