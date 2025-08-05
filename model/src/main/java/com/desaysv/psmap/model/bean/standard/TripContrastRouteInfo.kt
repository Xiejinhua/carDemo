package com.desaysv.psmap.model.bean.standard

/**
 * 备选路对比信息
 */
data class TripContrastRouteInfo(
    var route: List<TripContrastRoute>? = null
) : ResponseDataData()

data class TripContrastRoute(
    val num: Long,
    val type: Int,
    val label: String,
    val time: Int,
    val trafficLight: Int,
    val cost: Int,
    val distance: Int,
)
