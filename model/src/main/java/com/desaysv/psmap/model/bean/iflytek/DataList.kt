package com.desaysv.psmap.model.bean.iflytek

import com.google.gson.JsonObject

data class DataList(
    val result: List<PoiIFlyTek>,
    var semantic: JsonObject? = null
)