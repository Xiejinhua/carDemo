package com.desaysv.psmap.model.bean.standard

/**
 * 发起限⾏信息查询请求
 */
data class VehicleLimitOperaData(
    val carPlateNumber: String, //auto⻋牌信息
    val lat: Double, //纬度
    val lon: Double, //经度
    val date: String, //⽇期时间（该字段服务不⽀持，⽆需填写，否则会导致异常；默认查询的是当前时刻的限⾏）
    var trafficRestrictInfoResult: String = "" //限⾏信息
) : ResponseDataData()
