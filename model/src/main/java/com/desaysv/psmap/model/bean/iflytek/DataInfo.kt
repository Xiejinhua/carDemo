package com.desaysv.psmap.model.bean.iflytek

data class DataInfo(
    val end_poi: PoiIFlyTek? = null,
    var start_poi: PoiIFlyTek? = null,
    var via_poi: PoiIFlyTek? = null,
    var reference_poi: PoiIFlyTek? = null,
    var avoid_poi: PoiIFlyTek? = null,
)