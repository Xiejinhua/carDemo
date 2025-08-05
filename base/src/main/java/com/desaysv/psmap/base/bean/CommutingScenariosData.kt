package com.desaysv.psmap.base.bean

data class CommutingScenariosData(
    var type: Int, //0 回家， 1 去公司
    var title: String,
    var content: String,
    var lightBarItems: List<MapLightBarItem>? = null,
    var totalDistance: Long,
)