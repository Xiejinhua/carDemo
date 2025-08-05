package com.desaysv.psmap.model.bean.standard

/**
 * 路线信息
 */
data class RouteInfoDataRequest(
    val type: Int,
) : ResponseDataData()

data class RouteInfoDataResponse(
    val viaPOITotal: Int,
    val count: Int,
    val startPOIName: String,
    val startPOIAddr: String,
    val startPOILongitude: Double,
    val startPOILatitude: Double,
    val startPOIType: String,
    val endPOIName: String,
    val endPoiid: String,
    val endPOIAddr: String,
    val endPOILongitude: Double,
    val endPOILatitude: Double,
    val endPOIType: String,
    val arrivePOILongitude: Double,
    val arrivePOILatitude: Double,
    val arrivePOIType: String,
    val arrivePOIDistance: Int,
    val arrivePOIPhone: String,
    val viaNumbers: Int,
    val routePreference: Int,
    val newStrategy: Int,
    var protocolViaPOIInfos: List<ProtocolViaPOIInfo>? = null,
    var protocolRouteInfos: List<ProtocolRouteInfoData>? = null,
) : ResponseDataData()


data class ProtocolViaPOIInfo(
    val viaPOIName: String,
    val viaPoiid: String,
    val viaPOIAddr: String,
    val viaPOILongitude: Double,
    val viaPOILatitude: Double,
    val viaPOIType: String,
    val viaEntryLongitude: Double,
    val viaEntryLatitude: Double,
    val viaPOIDistance: Int,
    val viaPOIPhone: String,
)

data class ProtocolRouteInfoData(
    val method: String,//路线标签
    val routePreference: Int,//路线偏好
    val newStrategy: Int,
    val time: Double,
    val distance: Double,
    val timeAuto: String,
    val distanceAuto: String,
    val trafficLights: Int,
    val tolls: Int,
    val tmcSize: Int,
    val tmcSegments: String,
    val viaCityNumbers: Int,
    val oddNum: Int,
    val totalOddDistance: String,
    var streetNames: List<String>? = null,
    val viaPOItime: Int,
    val viaPOIdistance: Int,
    var protocolCityInfos: List<ProtocolCityInfo>? = null,//途径点城市名称列表
)