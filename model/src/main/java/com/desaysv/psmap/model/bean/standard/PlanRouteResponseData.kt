package com.desaysv.psmap.model.bean.standard

/**
 * @author 谢锦华
 * @time 2025/3/14
 * @description 语音指令-发起路线规划 （30402） 应答类
 */


data class ResponsePlanRouteData(
    var count: Int = 0,//路线规划结果⽅案数量注意：⽆路线规划结果时为0，最⼤值为3
    var fromPoiAddr: String = "",//起点地址
    var fromPoiLatitude: Double = 0.0,//起点纬度
    var fromPoiLongitude: Double = 0.0,//起点经度
    var fromPoiName: String = "",//起点名
    var midPoiArray: String = "",//中途点poi信息
    var midPoisNum: Int = 0,//中途点个数
    var protocolRouteInfos: List<ProtocolRouteInfo>? = null,//路径规划结果信息列表
    var toPoiAddr: String = "",//终点地址
    var toPoiLatitude: Double = 0.0,//终点纬度
    var toPoiLongitude: Double = 0.0,//终点经度
    var toPoiName: String = ""//终点名
) : ResponseDataData()

data class ProtocolRouteInfo(
    var distance: Double = 0.0,//总距离，单位：⽶
    var distanceAuto: String = "",//转换后的总距离
    var method: String = "",//路线标签，如“推荐”、“⽅案2”、“⽅案3”

    //路线偏好： 9（⼤路优先） 10（速度最快） 11（少收费） 12（⾼德推荐） 13
    //    （不⾛⾼速） 14（躲避拥堵） 15（少收费+不⾛⾼速） 16（躲避拥堵+不⾛⾼
    //速） 17（躲避拥堵+少收费） 18（躲避拥堵+少收费+不⾛⾼速） 34（⾼速优
    //先） 39（躲避拥堵+⾼速优先） 44（躲避拥堵+⼤路优先） 45（躲避拥堵+速
    //度最快）
    var newStrategy: Int = 0,
    var oddNum: Int = 0,//ODD 个数，单位：个
    var protocolCityInfos: List<ProtocolCityInfo>? = null,//途经点城市名称列表
    var routePreference: Int = 0,//路线偏好
    var time: Double = 0.0,//总时间，单位：秒
    var timeAuto: String = "",//转换后的总时间
    var tmcSegments: String = "",//路况信息详情
    var tmcSize: Int = 0,//路况信息（分为⼏段）
    var tolls: Int = 0,//收费信息，单位：元。此路线⽅案预估收费信息，如20元
    var totalOddDistance: String = "",//总距离
    var trafficLights: Int = 0,//红绿灯数，单位：个
    var viaCityNumbers: Int = 0//途经点城市个数
)

data class ProtocolCityInfo(
    var viaCityName: String = ""//途径城市名称
)