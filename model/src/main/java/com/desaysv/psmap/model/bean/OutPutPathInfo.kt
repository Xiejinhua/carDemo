package com.desaysv.psmap.model.bean

import com.autonavi.gbl.common.path.model.POIInfo
import com.autonavi.gbl.common.path.model.RestrictionInfo
import com.autonavi.gbl.common.path.model.ViaPointInfo
import com.desaysv.psmap.base.bean.MapLightBarItem

/**
 * 路线数据对外透出类
 */
data class OutPutPathInfo(
    var PathID: Long?,//获取路线ID
    var Length: Long?,//获取路线长度
    var Strategy: Long?,//获取路线计算策略
    var TravelTime: Long?,//获取当前Path旅行时间
    var StaticTravelTime: Long?,//获取当前Path静态的旅行时间
    var TollCost: Int?,//获取路线总收费金额
    var TrafficLightCount: Long?,//获取路线的总红绿灯个数
    var LightBarItems: ArrayList<MapLightBarItem>,//获取路线的光柱信息
    var RestrictionInfo: RestrictionInfo?,
    var EndPoi: POIInfo?,//获取终点POI信息
    var ViaPointInfo: ArrayList<ViaPointInfo>?//获取途经点信息
)
