package com.desaysv.psmap.model.bean.standard

data class ViaPoiOperaData(
    var viaProtocolPoi: ViaProtocolPoi? = null,
    var dev: Int? = 0,//坐标转换0：已经是⾼德坐标，不需转换1：⾮⾼德坐标，需要转换
    var action: Int?,//-1：删除所有途经点 0：增加途经点（需要传⼊要增加的途经点的经纬度） 1：删除途经点（需要传⼊要删除的途经点的经纬度）
) : ResponseDataData()

data class ViaProtocolPoi(
    var poiId: String = "",//poi唯⼀ID
    var poiName: String = "",
    var longitude: Double = 0.0,
    var latitude: Double = 0.0,
    var entryLongitude: Double = 0.0,//到达点经度 经纬度⼩数点后不得超过6位
    var entryLatitude: Double = 0.0,//到达点纬度 经纬度⼩数点后不得超过6位
    var nTypeCode: String = "",//到达点纬度 经纬度⼩数点后不得超过6位
    var midtype: Int = 0,
    var address: String = "",
)