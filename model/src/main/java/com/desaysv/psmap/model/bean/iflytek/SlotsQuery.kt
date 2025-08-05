package com.desaysv.psmap.model.bean.iflytek

/**
 * 导航到poi/道路等
 */
data class SlotsQuery(
    val endLoc: EndLoc,
    val startLoc: StartLoc,
    val viaLoc: ViaLoc?,
    val landmark: LandMark?,
    val property: Property?,
    val distanceDescr: String?
)