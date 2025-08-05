package com.desaysv.psmap.model.bean.iflytek

/**
 * 导航到用户相关 家/公司/收藏地
 */
data class SlotsUsrPoiQuery(
    val endLoc: EndLoc,
    val startLoc: StartLoc,
    val viaLoc: ViaLoc?,
    val landmark: LandMark?,
    val property: Property?,
)